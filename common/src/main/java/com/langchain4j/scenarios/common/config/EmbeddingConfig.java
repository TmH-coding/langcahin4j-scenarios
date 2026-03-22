package com.langchain4j.scenarios.common.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Embedding 配置类 - 向量化和向量存储的配置
 *
 * 职责说明：
 * - 配置和初始化 Embedding 模型
 * - 配置向量存储（内存存储）
 * - 支持 RAG（检索增强生成）功能
 * - 提供向量化和相似度搜索能力
 *
 * 架构设计：
 * - 使用 AllMiniLmL6V2EmbeddingModel 作为向量化模型
 * - 使用 InMemoryEmbeddingStore 作为向量存储
 * - 支持条件化 Bean 创建
 * - 易于扩展为其他向量存储（如 Pinecone、Weaviate 等）
 *
 * 使用场景：
 * - 文档分析和 RAG 系统
 * - 语义搜索和相似度匹配
 * - 知识库系统
 * - 向量化存储和检索
 *
 * 向量模型说明：
 * - AllMiniLmL6V2：轻量级向量模型
 *   * 维度：384
 *   * 大小：约 22MB
 *   * 速度：快速
 *   * 准确度：中等
 *   * 适用场景：一般的语义搜索
 *
 * 向量存储说明：
 * - InMemoryEmbeddingStore：内存向量存储
 *   * 优点：快速、无需外部依赖
 *   * 缺点：数据不持久化、内存占用大
 *   * 适用场景：开发、测试、小规模应用
 *
 * 扩展建议：
 * - 可以使用 Pinecone、Weaviate、Milvus 等向量数据库
 * - 可以使用更强大的向量模型（如 OpenAI Embedding）
 * - 可以实现向量的持久化存储
 * - 可以添加向量的缓存机制
 *
 * 性能考虑：
 * - 向量化速度取决于文本长度
 * - 内存存储的搜索速度为 O(n)
 * - 建议使用向量数据库提高搜索性能
 * - 监控内存使用情况
 */
@Configuration
public class EmbeddingConfig {

    /**
     * 创建 Embedding 模型 Bean
     *
     * 功能：
     * - 初始化 AllMiniLmL6V2EmbeddingModel
     * - 提供文本向量化能力
     * - 支持条件化创建
     *
     * 模型特性：
     * - 轻量级模型，适合本地部署
     * - 支持中英文混合文本
     * - 向量维度：384
     * - 模型大小：约 22MB
     *
     * 使用示例：
     * @Autowired
     * private EmbeddingModel embeddingModel;
     *
     * public void embedText(String text) {
     *     Response<Embedding> response = embeddingModel.embed(text);
     *     Embedding embedding = response.content();
     *     // 使用向量进行相似度搜索
     * }
     *
     * @return EmbeddingModel - 向量化模型实例
     */
    @Bean
    @ConditionalOnProperty(name = "embedding.enabled", havingValue = "true", matchIfMissing = true)
    public EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }
}
