package com.langchain4j.scenarios.scenario8.config;

import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore;
import dev.langchain4j.store.embedding.weaviate.WeaviateEmbeddingStore;
import dev.langchain4j.data.segment.TextSegment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * VectorStoreConfig - 统一配置 4 种向量存储
 *
 * 设计原则：
 * - 使用 @ConditionalOnProperty 实现运行时切换（零代码改动）
 * - 所有实现均暴露为 EmbeddingStore<TextSegment> 接口
 * - VectorRagService 只依赖接口，不感知具体实现
 *
 * 切换方式：在 application.properties 设置 vector.store.type=xxx
 */
@Configuration
public class VectorStoreConfig {

    /**
     * 内存向量存储（默认方案，无需外部服务）
     *
     * 原理：将所有向量保存在 Java ArrayList 中
     * 搜索：遍历全部向量，计算余弦相似度，返回 Top-K（O(n) 线性搜索）
     *
     * 优点：
     * - 零配置，无需任何外部服务
     * - 代码最简单，适合学习和原型开发
     * - 搜索准确（穷举搜索）
     *
     * 缺点：
     * - 应用重启后数据全部丢失（无持久化）
     * - 向量数量大时内存占用高，搜索变慢
     * - 单机，无法水平扩展
     *
     * 适用场景：
     * - 本地开发和测试
     * - 文档数量 < 10,000 段落的小规模应用
     * - 学习 RAG 概念的入门场景
     */
    @Bean
    @ConditionalOnProperty(name = "vector.store.type", havingValue = "inmemory", matchIfMissing = true)
    public EmbeddingStore<TextSegment> inMemoryEmbeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }

    /**
     * Milvus 向量存储（高性能向量数据库）
     *
     * 原理：专为向量设计的分布式数据库
     * 搜索：使用 HNSW/IVF_FLAT 等近似最近邻（ANN）算法，O(log n) 搜索
     *
     * 优点：
     * - 持久化存储，重启不丢数据
     * - 支持十亿级向量，性能随规模线性扩展
     * - 丰富的索引类型（HNSW、IVF_FLAT、IVF_SQ8 等）
     * - 支持 metadata 过滤（混合搜索）
     *
     * 缺点：
     * - 需要独立部署（Docker 或 K8s）
     * - 架构相对复杂（依赖 etcd、MinIO）
     *
     * 本地启动：
     * docker run -d --name milvus-standalone \
     *   -p 19530:19530 -p 9091:9091 \
     *   milvusdb/milvus:v2.3.0 standalone
     */
    @Bean
    @ConditionalOnProperty(name = "vector.store.type", havingValue = "milvus")
    public EmbeddingStore<TextSegment> milvusEmbeddingStore(
            @Value("${milvus.host:localhost}") String host,
            @Value("${milvus.port:19530}") int port,
            @Value("${milvus.collection-name:langchain4j_rag}") String collectionName) {
        return MilvusEmbeddingStore.builder()
                .host(host)
                .port(port)
                .collectionName(collectionName)
                .dimension(384)
                .metricType(io.milvus.param.MetricType.COSINE)
                .build();
    }

    /**
     * Qdrant 向量存储（现代轻量级向量数据库）
     *
     * 原理：用 Rust 编写的向量数据库，提供 gRPC 和 REST 两种接口
     * LangChain4j 默认使用 gRPC 接口
     *
     * 优点：
     * - 极低内存占用（Rust 实现，无 GC）
     * - 部署比 Milvus 简单（单一进程，无外部依赖）
     * - 支持丰富的 payload（metadata）过滤
     * - 开箱即用的 REST API（方便调试）
     *
     * 缺点：
     * - 生态相对 Milvus 更新
     * - 超大规模（千亿级）支持不如 Milvus
     *
     * 本地启动：
     * docker run -d --name qdrant \
     *   -p 6333:6333 -p 6334:6334 \
     *   qdrant/qdrant:latest
     */
    @Bean
    @ConditionalOnProperty(name = "vector.store.type", havingValue = "qdrant")
    public EmbeddingStore<TextSegment> qdrantEmbeddingStore(
            @Value("${qdrant.host:localhost}") String host,
            @Value("${qdrant.grpc-port:6334}") int grpcPort,
            @Value("${qdrant.collection-name:langchain4j_rag}") String collectionName) {
        return QdrantEmbeddingStore.builder()
                .host(host)
                .port(grpcPort)
                .collectionName(collectionName)
                .build();
    }

    /**
     * Weaviate 向量存储（GraphQL API 向量数据库）
     *
     * 原理：知识图谱 + 向量数据库的融合，使用 GraphQL API
     *
     * 优点：
     * - 内置多种模块（text2vec-openai 等），可选择是否用外部 Embedding
     * - GraphQL API 灵活的查询能力
     * - 支持多模态（图片、文本混合检索）
     *
     * 缺点：
     * - GraphQL API 学习曲线较陡
     * - 与 LangChain4j 集成的版本兼容性需要注意
     * - 配置比 Qdrant 复杂
     *
     * 本地启动：
     * docker run -d --name weaviate \
     *   -p 8080:8080 -p 50051:50051 \
     *   -e AUTHENTICATION_ANONYMOUS_ACCESS_ENABLED=true \
     *   -e PERSISTENCE_DATA_PATH=/var/lib/weaviate \
     *   semitechnologies/weaviate:1.22.0
     */
    @Bean
    @ConditionalOnProperty(name = "vector.store.type", havingValue = "weaviate")
    public EmbeddingStore<TextSegment> weaviateEmbeddingStore(
            @Value("${weaviate.host:localhost}") String host,
            @Value("${weaviate.port:8080}") int port,
            @Value("${weaviate.class-name:LangChain4jRag}") String className) {
        return WeaviateEmbeddingStore.builder()
                .scheme("http")
                .host(host + ":" + port)
                .objectClass(className)
                .avoidDups(true)
                .build();
    }
}
