# LangChain4j 学习路径

完整的学习指南，从基础到进阶，帮助你掌握 LangChain4j 和 AI 集成开发。

## 第一阶段：基础概念（第1-2周）

### 周1：LangChain4j 核心概念

**目标**：理解 LangChain4j 的基本架构和核心组件

#### Day 1-2：环境搭建和基础
1. 安装 Java 17+ 和 Maven
2. 配置 OpenAI API Key
3. 创建第一个 Spring Boot 项目
4. 运行 scenario-1-customer-service

**关键文件**：
- `common/config/LlmConfig.java` - LLM 配置
- `scenario-1-customer-service/src/main/java/com/langchain4j/scenarios/scenario1/service/CustomerServiceAI.java`

**学习要点**：
```java
// ChatLanguageModel 的基本使用
ChatLanguageModel chatModel = OpenAiChatModel.builder()
    .apiKey(apiKey)
    .modelName("gpt-4-turbo-preview")
    .temperature(0.7)
    .build();

// 构建和发送请求
ChatRequest request = ChatRequest.builder()
    .messages(messages)
    .parameters(ChatRequestParameters.builder()
        .temperature(0.7)
        .build())
    .build();

ChatResponse response = chatModel.chat(request);
```

#### Day 3-4：消息和对话管理
1. 理解 ChatMessage 的三种类型：SystemMessage、UserMessage、AiMessage
2. 学习对话历史管理
3. 实现简单的多轮对话

**关键概念**：
- SystemMessage：定义 AI 的角色和行为
- UserMessage：用户输入
- AiMessage：AI 响应
- 对话历史的滑动窗口

**练习**：
```java
// 修改 CustomerServiceAI 中的温度参数，观察响应的变化
// 尝试增加对话历史的大小，观察性能影响
```

#### Day 5：Prompt 工程基础
1. 学习如何编写有效的系统提示词
2. 理解温度参数的影响
3. 学习上下文的重要性

**最佳实践**：
- 清晰定义 AI 的角色
- 提供具体的约束和指导
- 使用示例来说明期望的行为
- 根据任务调整温度参数

### 周2：文档处理和 RAG 基础

**目标**：学习如何处理文档和实现基本的 RAG

#### Day 1-2：文档加载和分块
1. 学习 Document Loader
2. 理解文本分块策略
3. 运行 scenario-2-document-analysis

**关键概念**：
```java
// 文档加载
Document document = FileSystemDocumentLoader.loadDocument(Paths.get(path));

// 文档分块
DocumentSplitter splitter = DocumentSplitters.recursive(500, 50);
List<TextSegment> segments = splitter.split(document);
```

**参数说明**：
- `maxSegmentSize`：每个分块的最大字符数
- `overlapSize`：分块之间的重叠字符数

#### Day 3-4：相似度搜索和检索
1. 理解向量嵌入的概念
2. 学习相似度搜索
3. 实现简单的 RAG 流程

**练习**：
```java
// 修改 DocumentAnalysisService 中的分块大小
// 尝试不同的搜索策略
// 观察检索质量的变化
```

#### Day 5：集成和优化
1. 整合文档处理和 LLM
2. 优化检索质量
3. 处理大型文档

---

## 第二阶段：进阶功能（第3-4周）

### 周3：代码分析和专业化提示词

**目标**：学习针对特定领域的 Prompt 工程

#### Day 1-2：代码审查系统
1. 运行 scenario-3-code-assistant
2. 学习代码特定的提示词设计
3. 理解多语言支持

**关键学习点**：
```java
// 针对不同编程语言的提示词
String prompt = String.format(
    "Review the following %s code for bugs, performance issues, and best practices:\n\n```%s\n%s\n```",
    language, language, code
);
```

#### Day 3-4：多任务处理
1. 代码审查
2. Bug 检测
3. 文档生成
4. 重构建议

**练习**：
- 为不同的代码审查任务优化提示词
- 测试不同编程语言的支持
- 评估响应质量

#### Day 5：性能和质量优化
1. 缓存常见查询
2. 批量处理代码
3. 实现流式输出

### 周4：Agent 框架和工具调用

**目标**：学习 Agent 框架和动态工具调用

#### Day 1-2：Agent 基础
1. 运行 scenario-4-data-analyst
2. 理解 Agent 的工作流程
3. 学习 Tool 定义

**Agent 工作流程**：
```
用户输入 → Agent 分析 → 选择工具 → 执行工具 → 处理结果 → 返回响应
```

#### Day 3-4：Tool 定义和调用
1. 定义自定义 Tool
2. 实现 Tool 的执行逻辑
3. 处理 Tool 的返回值

**示例**：
```java
// SQL 生成工具
public String generateSQL(String requirement, String schema) {
    // 使用 LLM 生成 SQL
    // 验证 SQL 的有效性
    // 返回结果
}
```

#### Day 5：复杂 Agent 场景
1. 多步骤推理
2. 错误恢复
3. 结果验证

---

## 第三阶段：高级应用（第5-6周）

### 周5：内容创作和复杂 Chain

**目标**：学习复杂的 Chain 编排和批量处理

#### Day 1-2：内容生成
1. 运行 scenario-5-content-creator
2. 学习内容生成的最佳实践
3. 理解创意任务的提示词设计

**关键概念**：
- 更高的温度参数用于创意任务
- 结构化的输出格式
- 多步骤的内容优化

#### Day 3-4：Chain 编排
1. 顺序 Chain：文章生成 → SEO 优化 → 翻译
2. 并行 Chain：同时生成多个社交媒体帖子
3. 条件 Chain：根据内容类型选择不同的处理流程

**示例流程**：
```
用户输入 → 生成大纲 → 生成初稿 → SEO 优化 → 最终审查 → 输出
```

#### Day 5：性能优化
1. 批量处理
2. 缓存策略
3. 并发处理

### 周6：集成、测试和部署

**目标**：学习如何集成、测试和部署 AI 应用

#### Day 1-2：集成测试
1. 编写单元测试
2. 编写集成测试
3. 测试不同的 LLM 提供商

**测试策略**：
```java
@Test
public void testCustomerServiceChat() {
    String response = customerServiceAI.chat("你好");
    assertNotNull(response);
    assertTrue(response.length() > 0);
}
```

#### Day 3-4：监控和日志
1. 添加详细的日志记录
2. 监控 API 调用
3. 跟踪成本

#### Day 5：部署和优化
1. Docker 容器化
2. 性能基准测试
3. 成本优化

---

## 学习资源

### 官方文档
- [LangChain4j 文档](https://docs.langchain4j.dev/)
- [OpenAI API 文档](https://platform.openai.com/docs)
- [Spring Boot 文档](https://spring.io/projects/spring-boot)

### 推荐阅读
1. "Prompt Engineering Guide" - OpenAI
2. "Building LLM Applications" - LangChain
3. "Java 并发编程" - 用于理解异步处理

### 实践项目
1. 构建自己的客服机器人
2. 创建文档分析工具
3. 开发代码审查系统
4. 实现数据分析助手

---

## 常见陷阱和解决方案

### 1. 对话历史过长导致性能下降
**问题**：保存所有对话历史会导致 API 调用变慢
**解决**：使用滑动窗口，只保留最近的 N 条消息

### 2. 提示词不清晰导致响应质量差
**问题**：AI 的响应不符合预期
**解决**：优化系统提示词，提供更多上下文和示例

### 3. 文档分块大小不合适
**问题**：分块太大会丢失细节，太小会增加成本
**解决**：根据文档类型调整分块大小，通常 500-1000 字符较好

### 4. API 调用成本过高
**问题**：频繁调用 LLM 导致成本增加
**解决**：实现缓存、批量处理、使用更便宜的模型

### 5. 错误处理不足
**问题**：API 调用失败导致应用崩溃
**解决**：实现重试机制、降级策略、详细的错误日志

---

## 进度检查清单

### 基础阶段完成标志
- [ ] 能够独立配置 LLM
- [ ] 理解 ChatMessage 的三种类型
- [ ] 能够实现简单的多轮对话
- [ ] 理解文档分块的概念
- [ ] 能够实现基本的 RAG

### 进阶阶段完成标志
- [ ] 能够为不同任务优化提示词
- [ ] 理解 Agent 框架的工作原理
- [ ] 能够定义和使用自定义 Tool
- [ ] 能够处理复杂的 Chain 编排
- [ ] 理解性能优化的基本策略

### 高级阶段完成标志
- [ ] 能够设计和实现复杂的 AI 应用
- [ ] 能够编写全面的测试
- [ ] 能够监控和优化应用性能
- [ ] 能够部署到生产环境
- [ ] 能够处理实际的业务需求

---

## 下一步建议

1. **深入学习 Prompt 工程**：阅读 OpenAI 的 Prompt Engineering Guide
2. **探索高级 LLM 功能**：Function Calling、Vision、Audio
3. **学习向量数据库**：Milvus、Weaviate、Pinecone
4. **研究 RAG 优化**：混合搜索、重排序、查询优化
5. **参与开源项目**：为 LangChain4j 贡献代码
