package com.langchain4j.scenarios.scenario2.service;

import com.langchain4j.scenarios.common.util.DocumentProcessingUtil;
import com.langchain4j.scenarios.common.util.PromptBuilder;
import com.langchain4j.scenarios.common.util.PromptTemplateUtil;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 文档分析服务 - RAG（检索增强生成）核心引擎
 *
 * 职责说明：
 * - 实现文档加载、分割和分析的核心逻辑
 * - 支持文档总结和智能问答功能
 * - 实现简单的RAG（检索增强生成）功能
 * - 管理文档段落的存储和检索
 * - 支持文档内容的相似度搜索
 *
 * 架构设计：
 * - 使用 @Service 注解注册为 Spring Bean
 * - 使用 @RequiredArgsConstructor 自动注入依赖
 * - 依赖 ChatLanguageModel 进行LLM调用
 * - 使用 DocumentProcessingUtil 进行文档分割
 * - 使用 HashMap 存储文档段落（内存存储）
 * - 支持文档的完整生命周期管理
 *
 * 使用场景：
 * - 处理长文档（PDF、TXT、DOCX等）
 * - 对文档内容进行智能查询和问答
 * - 生成文档摘要和关键信息提取
 * - 企业知识库系统
 * - 法律文件分析
 * - 技术文档查询
 *
 * 核心特性：
 * - 文档分块：将长文档分割成可管理的段落（500字符/段）
 * - 段落重叠：相邻段落重叠50字符保持上下文连贯
 * - 文档存储：在内存中维护文档段落
 * - 相似度搜索：根据查询关键词找到相关段落
 * - 多文档支持：支持同时处理多个文档
 *
 * 工作原理：
 * 1. 从文件系统读取文档内容
 * 2. 创建 Document 对象
 * 3. 使用 DocumentProcessingUtil 分割文档
 * 4. 将分割后的段落存储到 documentStore
 * 5. 根据查询关键词搜索相关段落
 * 6. 调用LLM生成摘要或回答
 * 7. 返回结果给调用者
 *
 * 字段说明：
 * - chatModel: LLM语言模型
 *   * 类型：ChatLanguageModel
 *   * 用途：生成文档摘要和问答
 *   * 来源：Spring 依赖注入
 *
 * - documentStore: 文档存储
 *   * 类型：Map<String, List<TextSegment>>
 *   * key：documentId（文档唯一标识）
 *   * value：该文档的文本段落列表
 *   * 用途：存储所有已加载的文档段落
 *
 * 性能考虑：
 * - 文档分割时间取决于文档大小
 * - 查询时间复杂度为 O(n)，n为段落数
 * - 内存占用与文档大小成正比
 * - 建议使用向量数据库替代关键词搜索
 * - 可以配置文档分割参数优化性能
 * - 监控内存使用情况
 *
 * 安全考虑：
 * - 验证文件路径防止目录遍历攻击
 * - 限制文件大小防止内存溢出
 * - 验证文件类型防止恶意文件
 * - 不要在文档中暴露敏感信息
 * - 实现文档访问控制
 * - 记录所有文档操作用于审计
 *
 * 可靠性考虑：
 * - 处理文件读取异常
 * - 处理文档分割异常
 * - 实现请求超时控制
 * - 支持请求重试机制
 * - 记录详细的错误日志
 *
 * 扩展建议：
 * - 可以使用向量数据库替代关键词搜索
 * - 可以支持多种文件格式（PDF、DOCX等）
 * - 可以实现文档的增量更新
 * - 可以添加文档版本控制
 * - 可以支持文档的权限管理
 * - 可以实现文档的缓存机制
 * - 可以支持分布式文档存储
 */
@Service
@RequiredArgsConstructor
public class DocumentAnalysisService {

    /** 注入的LLM模型 */
    private final ChatLanguageModel chatModel;

    /** 文档存储：key为文档ID，value为该文档的文本段落列表 */
    private final Map<String, List<TextSegment>> documentStore = new HashMap<>();

    /**
     * 加载文档
     *
     * 功能流程：
     * 1. 从文件系统读取文档内容
     * 2. 创建 Document 对象
     * 3. 使用 DocumentProcessingUtil 分割文档
     * 4. 将分割后的段落存储到 documentStore
     *
     * 参数说明：
     * - documentPath: 文档文件的完整路径
     * - documentId: 文档的唯一标识符
     *
     * 分割参数：
     * - maxSegmentSize: 500 - 每个段落最多500个字符
     * - overlapSize: 50 - 相邻段落重叠50个字符
     *
     * @param documentPath 文档文件路径
     * @param documentId 文档唯一标识
     * @throws RuntimeException 如果文件读取失败
     */
    public void loadDocument(String documentPath, String documentId) {
        try {
            // 从文件系统读取文档内容
            String content = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(documentPath)));

            // 创建 Document 对象
            Document document = new Document(content);

            // 分割文档：500字符/段落，50字符重叠
            List<TextSegment> segments = DocumentProcessingUtil.splitDocument(document, 500, 50);

            // 存储到文档库
            documentStore.put(documentId, segments);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load document: " + e.getMessage(), e);
        }
    }

    /**
     * 总结文档
     *
     * 功能：
     * - 获取文档的所有段落
     * - 生成文档摘要
     *
     * @param documentId 文档ID
     * @return 文档摘要
     */
    public String summarizeDocument(String documentId) {
        // 从文档库获取文档段落
        List<TextSegment> segments = documentStore.get(documentId);

        // 检查文档是否存在
        if (segments == null || segments.isEmpty()) {
            return "Document not found";
        }

        // 取前10个段落进行摘要
        List<TextSegment> summarySegments = segments.stream()
                .limit(10)
                .toList();

        // 构建摘要提示词
        String systemPrompt = PromptTemplateUtil.getTemplate("document_analysis_system");
        String userMessage = "请根据以下文档内容生成一个简洁的摘要：\n\n" +
                summarySegments.stream()
                        .map(TextSegment::text)
                        .reduce((a, b) -> a + "\n\n" + b)
                        .orElse("");

        // 构建消息列表
        List<ChatMessage> messages =
                PromptBuilder.buildMessages(systemPrompt, userMessage);

        // 调用LLM生成摘要
        AiMessage response = chatModel.generate(messages).content();

        return response.text();
    }

    /**
     * 回答问题
     *
     * 功能：
     * - 根据用户问题生成答案
     *
     * @param question 用户问题
     * @return 答案
     */
    public String askQuestion(String question) {
        // 获取所有文档的所有段落
        List<TextSegment> allSegments = documentStore.values().stream()
                .flatMap(List::stream)
                .toList();

        if (allSegments.isEmpty()) {
            return "No documents available";
        }

        // 简单的关键词匹配找到相关段落
        List<TextSegment> relevantSegments = allSegments.stream()
                .filter(seg -> seg.text().toLowerCase().contains(question.toLowerCase()))
                .limit(3)
                .toList();

        // 构建RAG消息
        String systemPrompt = PromptTemplateUtil.getTemplate("document_analysis_system");
        List<String> contextStrings = relevantSegments.stream()
                .map(TextSegment::text)
                .toList();
        String userMessage = PromptBuilder.buildRagUserMessage(contextStrings, question);

        // 构建消息列表
        List<ChatMessage> messages = PromptBuilder.buildMessages(systemPrompt, userMessage);

        // 调用LLM生成答案
        return chatModel.generate(messages).content().text();
    }

    /**
     * 查询文档
     *
     * 功能流程：
     * 1. 获取指定文档的所有段落
     * 2. 根据查询关键词过滤相关段落
     * 3. 使用LLM生成基于上下文的答案
     *
     * 参数说明：
     * - documentId: 要查询的文档ID
     * - query: 查询关键词
     *
     * @param documentId 文档ID
     * @param query 查询关键词
     * @return 查询结果
     */
    public String queryDocument(String documentId, String query) {
        // 从文档库获取文档段落
        List<TextSegment> segments = documentStore.get(documentId);

        // 检查文档是否存在
        if (segments == null || segments.isEmpty()) {
            return "Document not found";
        }

        // 关键词匹配：找到包含查询词的段落
        List<TextSegment> relevantSegments = segments.stream()
                .filter(seg -> seg.text().toLowerCase().contains(query.toLowerCase()))
                .limit(3)  // 只返回前3个相关段落
                .toList();

        // 检查是否找到相关段落
        if (relevantSegments.isEmpty()) {
            return "No relevant information found for: " + query;
        }

        // 构建RAG消息
        String systemPrompt = PromptTemplateUtil.getTemplate("document_analysis_system");
        List<String> contextStrings = relevantSegments.stream()
                .map(TextSegment::text)
                .toList();
        String userMessage = PromptBuilder.buildRagUserMessage(contextStrings, query);

        // 构建消息列表
        List<ChatMessage> messages = PromptBuilder.buildMessages(systemPrompt, userMessage);

        // 调用LLM生成答案
        return chatModel.generate(messages).content().text();
    }
}


