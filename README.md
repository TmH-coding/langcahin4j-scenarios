# LangChain4j 多场景学习平台

一个完整的 Java 学习项目，展示如何使用 LangChain4j 构建 AI 驱动的应用。包含 5 个真实场景，从基础到进阶。

## 项目概述

本项目提供了 5 个独立的场景模块，每个模块展示不同的 LangChain4j 功能和最佳实践：

### 场景1：智能客服系统 (端口 8081)
**功能**：多轮对话、意图识别、知识库检索
- 核心技术：Conversation Memory、Prompt Engineering、Chain 组合
- 学习点：如何管理对话历史、构建系统提示词、处理多轮交互

**API 端点**：
```bash
# 发送消息
POST /api/customer-service/chat?message=你好

# 获取对话历史
GET /api/customer-service/history

# 清空历史
POST /api/customer-service/clear
```

### 场景2：文档智能分析 (端口 8082)
**功能**：PDF/文档解析、内容总结、Q&A
- 核心技术：Document Loader、Text Splitting、RAG、向量搜索
- 学习点：文档处理、分块策略、相似度搜索、上下文检索

**API 端点**：
```bash
# 加载文档
POST /api/document-analysis/load?path=/path/to/doc.pdf&documentId=doc1

# 总结文档
GET /api/document-analysis/summarize/doc1

# 查询文档
POST /api/document-analysis/query/doc1?query=什么是XXX
```

### 场景3：代码助手 (端口 8083)
**功能**：代码审查、Bug 检测、文档生成、重构建议
- 核心技术：Prompt 工程、流式输出、多语言支持
- 学习点：针对不同编程语言的提示词设计、代码分析

**API 端点**：
```bash
# 代码审查
POST /api/code-assistant/review?code=<code>&language=java

# 代码解释
POST /api/code-assistant/explain?code=<code>&language=java

# 生成文档
POST /api/code-assistant/document?code=<code>&language=java

# 重构建议
POST /api/code-assistant/refactor?code=<code>&language=java

# 检测 Bug
POST /api/code-assistant/bugs?code=<code>&language=java
```

### 场景4：数据分析助手 (端口 8084)
**功能**：SQL 生成、数据查询、报表生成、性能优化
- 核心技术：Agent 框架、Tool 定义、动态执行
- 学习点：Agent 设计、Tool 调用、数据库集成

**API 端点**：
```bash
# 生成 SQL
POST /api/data-analyst/generate-sql?requirement=查询用户&schema=<schema>

# 数据分析
POST /api/data-analyst/analyze?data=<data>&question=平均值是多少

# 生成报告
POST /api/data-analyst/report?data=<data>&reportType=summary

# 优化查询
POST /api/data-analyst/optimize?query=<sql>

# 解释查询
POST /api/data-analyst/explain-query?query=<sql>
```

### 场景5：内容创作平台 (端口 8085)
**功能**：文章生成、SEO 优化、多语言翻译、社交媒体内容
- 核心技术：Chain 编排、模板、批量处理、并发
- 学习点：复杂 Chain 设计、内容优化、多语言处理

**API 端点**：
```bash
# 生成文章
POST /api/content-creator/generate-article?topic=AI&style=professional&wordCount=1000

# SEO 优化
POST /api/content-creator/optimize-seo?content=<content>&keywords=AI,机器学习

# 翻译内容
POST /api/content-creator/translate?content=<content>&targetLanguage=English

# 生成社交媒体帖子
POST /api/content-creator/social-posts?topic=AI&postCount=5

# 改进写作
POST /api/content-creator/improve?content=<content>

# 生成大纲
POST /api/content-creator/outline?topic=AI&sections=5
```

## 项目结构

```
langchain4j-scenarios/
├── pom.xml                              # 主 Maven 配置
├── common/                              # 共享模块
│   ├── pom.xml
│   └── src/main/java/
│       └── com/langchain4j/scenarios/common/
│           ├── config/
│           │   └── LlmConfig.java       # LLM 配置
│           ├── model/
│           │   └── ConversationMessage.java
│           └── util/
│               ├── DocumentProcessingUtil.java
│               ├── ConversationMemoryUtil.java
│               └── PromptTemplateUtil.java
├── scenario-1-customer-service/         # 客服系统
├── scenario-2-document-analysis/        # 文档分析
├── scenario-3-code-assistant/           # 代码助手
├── scenario-4-data-analyst/             # 数据分析
├── scenario-5-content-creator/          # 内容创作
└── docs/                                # 文档
    ├── LEARNING_PATH.md                 # 学习路径
    ├── BEST_PRACTICES.md                # 最佳实践
    └── ARCHITECTURE.md                  # 架构设计
```

## 快速开始

### 前置要求
- Java 17+
- Maven 3.8+
- OpenAI API Key（或其他 LLM 提供商）

### 环境配置

1. 设置 API Key：
```bash
export OPENAI_API_KEY=your_api_key_here
```

2. 构建项目：
```bash
mvn clean install
```

3. 运行特定场景：
```bash
# 客服系统
cd scenario-1-customer-service
mvn spring-boot:run

# 文档分析
cd scenario-2-document-analysis
mvn spring-boot:run

# 代码助手
cd scenario-3-code-assistant
mvn spring-boot:run

# 数据分析
cd scenario-4-data-analyst
mvn spring-boot:run

# 内容创作
cd scenario-5-content-creator
mvn spring-boot:run
```

## 核心概念

### 1. LLM 集成
所有场景都通过 `ChatLanguageModel` 与 LLM 交互。配置在 `common/config/LlmConfig.java`。

### 2. Prompt 工程
使用 `PromptTemplateUtil` 管理系统提示词和模板。

### 3. 对话管理
`ConversationMemoryUtil` 管理对话历史，支持滑动窗口。

### 4. 文档处理
`DocumentProcessingUtil` 处理文档加载和分块。

### 5. 错误处理
所有服务都包含基本的错误处理和日志记录。

## 学习路径

### 初级（第1-2周）
1. 理解 LangChain4j 核心概念
2. 学习场景1：客服系统
3. 学习场景2：文档分析

**关键概念**：
- ChatLanguageModel 的使用
- ChatRequest/ChatResponse 的构建
- 对话历史管理
- 文档加载和分块

### 中级（第3-4周）
1. 学习场景3：代码助手
2. 学习场景4：数据分析

**关键概念**：
- 针对不同任务的 Prompt 设计
- Agent 框架基础
- Tool 定义和调用
- 流式处理

### 高级（第5-6周）
1. 学习场景5：内容创作
2. 性能优化和并发处理
3. 集成测试和部署

**关键概念**：
- 复杂 Chain 编排
- 批量处理
- 缓存策略
- 监控和日志

## 最佳实践

### 1. Prompt 工程
- 使用清晰的系统提示词定义 AI 角色
- 为不同任务使用不同的温度参数
- 在提示词中包含上下文和约束

### 2. 错误处理
- 总是处理 API 调用的异常
- 实现重试机制
- 记录详细的错误日志

### 3. 性能优化
- 使用对话历史的滑动窗口
- 实现缓存机制
- 考虑使用流式输出

### 4. 安全性
- 不要在代码中硬编码 API Key
- 验证用户输入
- 实现速率限制

## 扩展建议

1. **添加数据库支持**：集成 PostgreSQL 存储对话历史和文档
2. **实现向量数据库**：使用 Milvus 或 Weaviate 进行 RAG
3. **添加认证**：实现用户认证和授权
4. **监控和日志**：集成 ELK 或其他监控系统
5. **API 文档**：使用 Swagger/OpenAPI 生成 API 文档

## 常见问题

**Q: 如何切换 LLM 提供商？**
A: 修改 `LlmConfig.java` 中的配置，支持 OpenAI、Ollama 等。

**Q: 如何处理长文档？**
A: 使用 `DocumentProcessingUtil` 的分块功能，调整 `maxSegmentSize` 参数。

**Q: 如何改进 AI 响应质量？**
A: 优化系统提示词、调整温度参数、提供更多上下文。

## 参考资源

- [LangChain4j 官方文档](https://docs.langchain4j.dev/)
- [OpenAI API 文档](https://platform.openai.com/docs)
- [Spring Boot 文档](https://spring.io/projects/spring-boot)

## 许可证

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request！
