package com.langchain4j.scenarios.scenario8;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Scenario 8: Vector Database Integration
 *
 * 学习目标：
 * - 理解向量存储的核心概念（EmbeddingStore 接口）
 * - 对比 4 种向量存储方案的特点和适用场景
 * - 掌握 EmbeddingStoreIngestor 的用法（文档批量向量化）
 * - 理解向量检索与关键词检索的根本区别
 *
 * 运行：mvn spring-boot:run -pl scenario-8-vector-database
 * 端口：8088
 *
 * 切换向量存储（application.properties）：
 *   vector.store.type=inmemory   （默认，无需外部服务）
 *   vector.store.type=milvus     （需要 Docker 启动 Milvus）
 *   vector.store.type=qdrant     （需要 Docker 启动 Qdrant）
 *   vector.store.type=weaviate   （需要 Docker 启动 Weaviate）
 */
@SpringBootApplication(scanBasePackages = {
        "com.langchain4j.scenarios.common",
        "com.langchain4j.scenarios.scenario8"
})
public class Scenario8Application {

    public static void main(String[] args) {
        SpringApplication.run(Scenario8Application.class, args);
    }
}
