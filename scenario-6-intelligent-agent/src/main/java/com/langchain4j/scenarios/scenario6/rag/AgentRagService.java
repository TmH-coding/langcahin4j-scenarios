package com.langchain4j.scenarios.scenario6.rag;

import com.langchain4j.scenarios.common.util.DocumentProcessingUtil;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Agent RAG 服务 - 为 Agent 提供文档库检索能力
 *
 * 核心概念讲解：
 * ============
 * 1. 什么是 InMemoryEmbeddingStore？
 *    - LangChain4j 内置的内存向量存储
 *    - 将文本段落和对应向量一起存储
 *    - 支持余弦相似度搜索（找到最接近的段落）
 *    - 无需外部数据库，适合学习和小规模应用
 *
 * 2. RAG 在 Agent 中的位置：
 *    用户问题 → Agent 调用 searchDocumentLibrary 工具
 *             → 本服务向量化问题 → 在向量库中检索相似段落
 *             → 返回相关文本 → LLM 基于上下文回答
 *
 * 3. 与 scenario-2 的区别：
 *    - scenario-2：简单关键词 contains() 匹配（字面匹配）
 *    - scenario-6（本服务）：向量嵌入相似度搜索（语义匹配）
 *    - 语义搜索能找到"含义相近"但词语不同的段落
 *
 * 4. 向量检索流程：
 *    文档入库：文本 → EmbeddingModel → 向量 → InMemoryEmbeddingStore
 *    检索：查询文本 → EmbeddingModel → 查询向量 → 余弦相似度比较 → Top-K 结果
 */
@Slf4j
@Service
public class AgentRagService {

    /** 向量化模型（由 common 模块的 EmbeddingConfig Bean 注入） */
    private final EmbeddingModel embeddingModel;

    /**
     * 内存向量存储
     * 存储结构：TextSegment（包含原文+元数据） + 对应的 384 维向量
     * 注意：应用重启后数据丢失，适合学习使用
     */
    private final EmbeddingStore<TextSegment> embeddingStore;

    /**
     * 文档元数据记录
     * key: documentId, value: DocumentInfo（名称、段落数、加载时间）
     * 与向量存储分开管理，方便展示文档列表
     */
    private final Map<String, DocumentInfo> documentRegistry = new ConcurrentHashMap<>();

    public AgentRagService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
        // InMemoryEmbeddingStore 是 LangChain4j 内置类，不需要 Spring Bean 注册
        this.embeddingStore = new InMemoryEmbeddingStore<>();
    }

    /**
     * 将文档加载到向量库
     *
     * 步骤：
     * 1. 用 DocumentProcessingUtil 分割文档为段落
     * 2. 批量向量化所有段落
     * 3. 存入 InMemoryEmbeddingStore
     *
     * @param content    文档文本内容
     * @param documentId 文档唯一ID
     * @param title      文档标题（用于展示）
     * @return 成功加载的段落数
     */
    public int loadDocument(String content, String documentId, String title) {
        // 1. 创建 Document 对象并分割（500字符/段，50字符重叠，与 scenario-2 保持一致）
        Document document = new Document(content);
        List<TextSegment> segments = DocumentProcessingUtil.splitDocument(document, 500, 50);

        // 2. 批量向量化：每个段落转为 384 维向量
        List<Embedding> embeddings = new ArrayList<>();
        for (TextSegment segment : segments) {
            Embedding embedding = embeddingModel.embed(segment.text()).content();
            embeddings.add(embedding);
        }

        // 3. 批量存入向量库（段落+向量一一对应）
        embeddingStore.addAll(embeddings, segments);

        // 4. 记录文档元数据
        documentRegistry.put(documentId, new DocumentInfo(documentId, title, segments.size(),
                System.currentTimeMillis()));

        log.info("Document loaded to vector store: id={}, title={}, segments={}",
                documentId, title, segments.size());
        return segments.size();
    }

    /**
     * 语义检索文档库
     *
     * 核心：用向量相似度（而非关键词）找最相关的段落
     *
     * @param query   用户查询（自然语言）
     * @param maxResults 返回最多几个段落
     * @return 相关文本段落列表（已按相似度降序排列）
     */
    public List<String> search(String query, int maxResults) {
        if (documentRegistry.isEmpty()) {
            return List.of("文档库为空，请先上传文档。");
        }

        // 1. 向量化查询语句
        Embedding queryEmbedding = embeddingModel.embed(query).content();

        // 2. 在向量库中搜索最相似的段落
        //    findRelevant(queryEmbedding, maxResults) 内部用余弦相似度排序
        List<EmbeddingMatch<TextSegment>> matches =
                embeddingStore.findRelevant(queryEmbedding, maxResults);

        // 3. 提取匹配的文本内容（过滤低相似度结果，阈值 0.3）
        List<String> results = new ArrayList<>();
        for (EmbeddingMatch<TextSegment> match : matches) {
            if (match.score() >= 0.3) {  // score 范围 0-1，越高越相似
                results.add(match.embedded().text());
                log.debug("Match found: score={}, text_preview={}",
                        match.score(),
                        match.embedded().text().substring(0, Math.min(50, match.embedded().text().length())));
            }
        }

        return results.isEmpty() ? List.of("未找到与查询相关的文档内容。") : results;
    }

    /** 获取文档库中所有文档的元数据 */
    public List<DocumentInfo> listDocuments() {
        return new ArrayList<>(documentRegistry.values());
    }

    /** 检查文档库是否为空 */
    public boolean isEmpty() {
        return documentRegistry.isEmpty();
    }

    /** 获取文档总数 */
    public int getDocumentCount() {
        return documentRegistry.size();
    }

    /**
     * 文档元数据记录
     * 使用 Java Record（Java 16+）简化不可变数据类
     */
    public record DocumentInfo(
            String documentId,
            String title,
            int segmentCount,
            long loadTime
    ) {}
}
