package com.langchain4j.scenarios.scenario8.service;

import com.langchain4j.scenarios.common.util.DocumentProcessingUtil;
import com.langchain4j.scenarios.common.util.PromptBuilder;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * VectorRagService - 核心 RAG 服务
 *
 * 学习要点：
 * 1. 本服务不依赖任何具体向量存储实现，只依赖 EmbeddingStore<TextSegment> 接口
 *    - 切换 vector.store.type 配置就能换掉底层存储，本服务代码无需改动
 *    - 这是"对接口编程"的典型例子
 *
 * 2. 与 scenario-2 DocumentAnalysisService 的对比：
 *    scenario-2（旧方式）：
 *      relevantSegments = allSegments.stream()
 *          .filter(seg -> seg.text().toLowerCase().contains(query.toLowerCase()))
 *          // 缺点：找不到同义词，"汽车"搜不到"车辆"
 *
 *    scenario-8（向量方式）：
 *      queryEmbedding = embeddingModel.embed(query).content();
 *      matches = embeddingStore.findRelevant(queryEmbedding, maxResults);
 *      // 优点：语义匹配，"汽车"能搜到"车辆"、"轿车"、"驾车"等
 */
@Slf4j
@Service
public class VectorRagService {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final ChatLanguageModel chatModel;

    @Value("${rag.max-results:5}")
    private int maxResults;

    @Value("${rag.min-score:0.3}")
    private double minScore;

    private int totalDocumentsLoaded = 0;
    private int totalSegmentsStored = 0;

    public VectorRagService(EmbeddingModel embeddingModel,
                             EmbeddingStore<TextSegment> embeddingStore,
                             ChatLanguageModel chatModel) {
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
        this.chatModel = chatModel;
    }

    /**
     * 将文档文本向量化并存入向量库
     *
     * 完整流程（RAG Pipeline Step 1/2）：
     * 1. 文档分割（chunking）：使用 DocumentProcessingUtil，500字符/段，50字符重叠
     * 2. 逐段向量化（embedding）：调用 AllMiniLmL6V2EmbeddingModel，得到 384 维向量
     * 3. 批量存储（indexing）：调用 embeddingStore.addAll()，写入选定的向量存储
     *
     * @param content    文档原始文本
     * @param title      文档标题（记录在元数据，检索结果中显示）
     * @return           成功存储的段落数
     */
    public int ingestDocument(String content, String title) {
        // Step 1：分割文档
        Document document = new Document(content);
        List<TextSegment> segments = DocumentProcessingUtil.splitDocument(document, 500, 50);

        if (segments.isEmpty()) {
            log.warn("Document '{}' produced 0 segments, skipping.", title);
            return 0;
        }

        // Step 2：批量向量化（每个段落单独向量化）
        List<Embedding> embeddings = new ArrayList<>();
        for (TextSegment segment : segments) {
            Embedding embedding = embeddingModel.embed(segment.text()).content();
            embeddings.add(embedding);
        }

        // Step 3：写入向量存储（接口调用，实际行为取决于配置的后端）
        embeddingStore.addAll(embeddings, segments);

        totalDocumentsLoaded++;
        totalSegmentsStored += segments.size();
        log.info("[{}] Ingested document: title='{}', segments={}, totalStored={}",
                embeddingStore.getClass().getSimpleName(), title, segments.size(), totalSegmentsStored);

        return segments.size();
    }

    /**
     * 纯向量语义搜索（不调用 LLM，直接返回原始段落）
     *
     * 流程（RAG Pipeline Step 2）：
     * 1. 向量化查询：将用户问题转为 384 维查询向量
     * 2. 相似度搜索：在向量库中找到最相似的段落（余弦相似度）
     * 3. 过滤低分结果：score < minScore 的结果丢弃
     *
     * @param query      自然语言查询
     * @return           匹配的文本段落（含相似度分数）
     */
    public List<SearchResult> search(String query) {
        // 向量化查询
        Embedding queryEmbedding = embeddingModel.embed(query).content();

        // 在向量库中搜索最相似的段落
        List<EmbeddingMatch<TextSegment>> matches =
                embeddingStore.findRelevant(queryEmbedding, maxResults);

        // 过滤并转换
        List<SearchResult> results = new ArrayList<>();
        for (EmbeddingMatch<TextSegment> match : matches) {
            if (match.score() >= minScore) {
                results.add(new SearchResult(
                        match.embedded().text(),
                        match.score()
                ));
                log.debug("Match: score={:.4f}, preview='{}'",
                        match.score(),
                        match.embedded().text().substring(0, Math.min(80, match.embedded().text().length())));
            }
        }

        log.info("Search query='{}', rawMatches={}, filteredResults={}",
                query, matches.size(), results.size());
        return results;
    }

    /**
     * 基于向量检索的 RAG 问答（完整 RAG 流程）
     *
     * 完整 RAG Pipeline：
     * 1. Retrieval（检索）：向量化问题 → 搜索相似段落
     * 2. Augmentation（增强）：将检索到的段落注入 Prompt
     * 3. Generation（生成）：调用 LLM，基于上下文生成回答
     *
     * @param question   用户问题
     * @return           LLM 基于向量检索上下文生成的回答
     */
    public String ragAnswer(String question) {
        // Step 1：向量检索
        List<SearchResult> searchResults = search(question);

        if (searchResults.isEmpty()) {
            return "未找到与问题相关的文档内容，请先上传文档或尝试其他问题。";
        }

        // Step 2：构建 RAG Prompt（复用 PromptBuilder）
        List<String> contextTexts = searchResults.stream()
                .map(SearchResult::text)
                .toList();

        String ragUserMessage = PromptBuilder.buildRagUserMessage(contextTexts, question);
        String systemPrompt = "你是一个专业的知识库问答助手。请基于提供的上下文内容回答问题，" +
                "如果上下文中没有相关信息，请明确告知用户。回答要简洁、准确、有条理。";

        List<ChatMessage> messages = PromptBuilder.buildMessages(systemPrompt, ragUserMessage);

        // Step 3：调用 LLM 生成回答
        AiMessage response = chatModel.generate(messages).content();

        log.info("RAG answer generated for question='{}', contextChunks={}",
                question, contextTexts.size());
        return response.text();
    }

    public String getStoreType() {
        return embeddingStore.getClass().getSimpleName();
    }

    public int getTotalDocumentsLoaded() {
        return totalDocumentsLoaded;
    }

    public int getTotalSegmentsStored() {
        return totalSegmentsStored;
    }

    /**
     * 向量搜索结果的值对象
     */
    public record SearchResult(String text, double score) {}
}
