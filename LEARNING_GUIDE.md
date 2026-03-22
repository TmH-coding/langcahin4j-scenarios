# LangChain4j 多场景学习项目 - 完整学习指南

## 项目概览

这是一个包含 8 个场景的 LangChain4j 学习项目，涵盖从基础 LLM 集成到高级 AI 应用的完整学习路径。

**项目特点：**
- 8 个真实场景，每个场景都有完整的实现和学习材料
- 从简单到复杂的递进式学习
- 包含 Agent 智能体和 MCP 协议两种工具调用方式
- 所有代码都有详细的中文注释和文档

---

## 核心概念速查表

| 概念 | 定义 | 应用场景 |
|------|------|---------|
| **LLM** | 大语言模型 | 所有场景的基础 |
| **Prompt** | 提示词 | 指导 LLM 的行为 |
| **Token** | 文本单位 | 计费和性能的基础 |
| **Embedding** | 向量表示 | RAG、相似度计算 |
| **RAG** | 检索增强生成 | 文档分析、知识库 |
| **Agent** | 智能体 | 多步推理、工具调用 |
| **MCP** | 标准化工具协议 | 企业级应用 |
| **Stream** | 流式输出 | 实时响应 |

---

## 学习路径

### 第一阶段：基础概念（Scenario 1-2）

#### Scenario 1: 客服系统
**核心知识点：**
- LLM 基础调用
- 多轮对话管理
- 对话历史维护
- 系统提示词设计

**关键代码模式：**
```java
// 基础 LLM 调用
Response<AiMessage> response = chatModel.generate(messages);
String answer = response.content().text();

// 多轮对话
List<ChatMessage> messages = new ArrayList<>();
messages.add(SystemMessage.from(systemPrompt));
messages.add(UserMessage.from(userMessage));
// 添加历史消息...
```

**学习要点：**
- 如何构建有效的系统提示词
- 对话历史的管理策略
- Token 消耗的优化

---

#### Scenario 2: 文档分析（RAG）
**核心知识点：**
- 向量化（Embedding）
- 向量存储
- 相似度检索
- RAG 流程

**RAG 工作流程：**
```
文档 → 分块 → 向量化 → 存储
                    ↓
查询 → 向量化 → 相似度检索 → 获取上下文 → LLM 生成答案
```

**关键概念：**
- **Embedding**：将文本转换为向量，捕捉语义信息
- **向量存储**：高效存储和检索向量
- **相似度计算**：找到最相关的文档片段

**学习要点：**
- 文档分块的策略（大小、重叠）
- 向量模型的选择
- 检索结果的排序和过滤

---

### 第二阶段：专业应用（Scenario 3-5）

#### Scenario 3: 代码助手
**核心知识点：**
- 代码理解和分析
- 多维度评估
- 结构化输出

**代码审查维度：**
- 功能正确性
- 性能优化
- 安全性
- 可读性
- 最佳实践

**学习要点：**
- 如何设计专业的提示词
- 结构化输出的格式定义
- 领域特定的知识融入

---

#### Scenario 4: 数据分析
**核心知识点：**
- SQL 生成
- 数据查询
- 分析报告生成
- 数据可视化建议

**关键技术：**
- 自然语言转 SQL
- 数据库 Schema 理解
- 查询优化建议

**学习要点：**
- 如何让 LLM 理解数据库结构
- SQL 生成的准确性控制
- 数据分析的逻辑推理

---

#### Scenario 5: 内容创作
**核心知识点：**
- 创意生成
- 风格控制
- SEO 优化
- 多语言支持

**内容创作流程：**
```
需求 → 大纲生成 → 内容创作 → SEO 优化 → 翻译 → 质量检查
```

**学习要点：**
- 创意任务的 Temperature 设置
- 风格一致性的维护
- 多语言处理的策略

---

### 第三阶段：高级特性（Scenario 6-7）

#### Scenario 6: Agent 智能体
**核心知识点：**
- ReAct 框架
- 工具调用
- 多步推理
- 循环控制

**Agent 工作流程：**
```
用户请求
    ↓
LLM 分析（Reasoning）
    ↓
调用工具（Acting）
    ↓
获取结果（Observation）
    ↓
继续推理或返回答案
```

**关键概念：**
- **Reasoning**：LLM 分析问题，决定调用哪些工具
- **Acting**：执行工具调用
- **Observation**：获取工具结果，反馈给 LLM

**Agent 循环伪代码：**
```java
while (iteration < maxIterations) {
    // 1. 调用 LLM
    AiMessage response = chatModel.generate(messages);

    // 2. 检查是否需要调用工具
    if (response.hasToolExecutionRequests()) {
        // 3. 执行工具
        for (ToolExecutionRequest tool : response.toolRequests()) {
            Object result = executeTool(tool);
            // 4. 将结果反馈给 LLM
            messages.add(UserMessage.from("工具结果: " + result));
        }
    } else {
        // LLM 给出了最终答案
        return response.text();
    }
}
```

**学习要点：**
- 工具的设计原则（单一职责、清晰参数）
- 防止无限循环的策略
- 错误处理和恢复

---

#### Scenario 7: MCP 协议
**核心知识点：**
- 标准化工具接口
- JSON Schema 定义
- 互操作性
- 权限控制

**MCP vs Agent 对比：**

| 特性 | Agent | MCP |
|------|-------|-----|
| 工具调用 | LLM 自主决定 | 遵循 JSON Schema |
| 标准化 | 工具接口不统一 | 所有工具遵循规范 |
| 互操作性 | 低 | 高 |
| 安全性 | 基础 | 完善 |
| 使用场景 | 研究、实验 | 企业级应用 |

**JSON Schema 示例：**
```json
{
  "name": "calculate",
  "description": "执行数学运算",
  "inputSchema": {
    "type": "object",
    "properties": {
      "operation": {"type": "string", "enum": ["+", "-", "*", "/"]},
      "num1": {"type": "number"},
      "num2": {"type": "number"}
    },
    "required": ["operation", "num1", "num2"]
  }
}
```

**学习要点：**
- JSON Schema 的完整规范
- 工具的版本管理
- 权限控制的实现

---

## 关键技术深度讲解

### 1. Prompt 工程

**提示词的三个层次：**

#### 基础提示词
```
直接告诉 LLM 做什么
例：翻译这句话为英文
```

#### 结构化提示词
```
定义角色、任务、约束、输出格式
例：
你是一个专业的代码审查员。
任务：审查以下代码
约束：只关注性能和安全性
输出格式：JSON
```

#### 高级提示词
```
包含示例、推理过程、错误处理
例：
你是一个数据分析专家。
任务：分析销售数据
示例：[提供示例]
推理过程：[说明思考步骤]
错误处理：[如何处理异常]
```

**提示词最佳实践：**
- 清晰明确：避免歧义
- 具体详细：提供足够的上下文
- 示例驱动：用例子说明期望
- 角色定义：明确 LLM 的身份
- 输出格式：指定返回格式

---

### 2. Token 管理

**Token 的重要性：**
- 计费基础：按 Token 数量计费
- 性能影响：Token 越多，响应越慢
- 上下文限制：每个模型有最大 Token 限制

**Token 优化策略：**

```java
// 1. 只保存最近的消息
if (messages.size() > MAX_MESSAGES) {
    messages = messages.subList(
        messages.size() - MAX_MESSAGES,
        messages.size()
    );
}

// 2. 压缩长文本
String compressed = compressText(longText);

// 3. 使用摘要替代完整文本
String summary = generateSummary(document);

// 4. 监控 Token 使用
int tokenCount = countTokens(text);
if (tokenCount > THRESHOLD) {
    // 采取措施
}
```

**Token 计数规则：**
- 英文：1 个单词 ≈ 1.3 个 Token
- 中文：1 个字 ≈ 1 个 Token
- 特殊字符：1 个 ≈ 1 个 Token

---

### 3. Embedding 和向量化

**Embedding 的本质：**
- 将文本转换为高维向量
- 捕捉文本的语义信息
- 相似的文本有相似的向量

**向量相似度计算：**

```
余弦相似度 = (向量A · 向量B) / (|向量A| × |向量B|)
范围：-1 到 1（通常 0 到 1）
```

**Embedding 应用：**
- RAG：检索相关文档
- 相似度计算：找相似内容
- 聚类：分组相似文本
- 推荐：推荐相似项目

**选择 Embedding 模型：**
- 小模型（如 all-MiniLM-L6-v2）：快速、低成本
- 大模型（如 text-embedding-3-large）：准确度高

---

### 4. 流式响应

**为什么需要流式响应：**
- 改善用户体验：实时看到输出
- 减少等待时间：不用等待完整响应
- 节省资源：可以中途停止

**流式响应实现：**

```java
// SSE（Server-Sent Events）
@GetMapping("/stream")
public SseEmitter stream() {
    SseEmitter emitter = new SseEmitter();

    StreamingResponseHandler<AiMessage> handler =
        new StreamingResponseHandler<AiMessage>() {
            @Override
            public void onNext(String token) {
                emitter.send(token);
            }

            @Override
            public void onComplete(Response<AiMessage> response) {
                emitter.send("[DONE]");
                emitter.complete();
            }
        };

    streamingChatModel.generate(messages, handler);
    return emitter;
}
```

---

### 5. 错误处理和恢复

**常见错误类型：**

| 错误 | 原因 | 解决方案 |
|------|------|---------|
| Rate Limit | 请求过于频繁 | 添加重试延迟 |
| Token Limit | 输入过长 | 压缩或分割输入 |
| API Error | 服务异常 | 重试或降级 |
| Invalid Input | 参数错误 | 验证输入 |

**重试策略：**

```java
// 指数退避重试
int maxRetries = 3;
int delay = 1000; // 1 秒

for (int i = 0; i < maxRetries; i++) {
    try {
        return callLLM();
    } catch (RateLimitException e) {
        if (i < maxRetries - 1) {
            Thread.sleep(delay);
            delay *= 2; // 指数增长
        } else {
            throw e;
        }
    }
}
```

---

## 实战技巧

### 1. 调试 LLM 应用

**常用调试方法：**

```java
// 1. 打印完整的消息列表
logger.debug("Messages: {}", messages);

// 2. 记录 Token 使用
logger.debug("Input tokens: {}, Output tokens: {}",
    response.tokenUsage().inputTokenCount(),
    response.tokenUsage().outputTokenCount());

// 3. 保存请求和响应
saveToFile("request.json", messages);
saveToFile("response.json", response);

// 4. 使用不同的 Temperature
// 低 Temperature（0.1-0.3）：确定性强，适合分析
// 高 Temperature（0.7-1.0）：创意强，适合创作
```

---

### 2. 性能优化

**优化策略：**

```java
// 1. 缓存相同的请求
@Cacheable(value = "llm_cache", key = "#message")
public String chat(String message) {
    return chatModel.generate(message);
}

// 2. 批量处理
List<String> results = messages.parallelStream()
    .map(this::processMessage)
    .collect(Collectors.toList());

// 3. 异步处理
CompletableFuture<String> future =
    CompletableFuture.supplyAsync(() -> chatModel.generate(message));
```

---

### 3. 成本控制

**成本优化：**

```java
// 1. 选择合适的模型
// GPT-4：最强但最贵
// GPT-3.5：平衡
// 本地模型：免费但需要资源

// 2. 优化 Token 使用
String optimized = compressPrompt(prompt);

// 3. 使用缓存
if (cache.contains(query)) {
    return cache.get(query);
}

// 4. 监控成本
logger.info("Cost: ${}", tokenCount * PRICE_PER_TOKEN);
```

---

## 常见问题解答

### Q1: 如何选择合适的 LLM 模型？

**A:** 根据以下因素选择：
- **准确性需求**：高 → GPT-4，低 → GPT-3.5
- **成本预算**：有限 → 本地模型，充足 → GPT-4
- **响应速度**：快 → GPT-3.5，慢可接受 → GPT-4
- **隐私要求**：高 → 本地模型，低 → 云服务

---

### Q2: Agent 和 MCP 应该怎么选？

**A:**
- **Agent**：需要复杂推理、多步骤任务、灵活工具组合
- **MCP**：需要标准化接口、多 LLM 支持、企业级应用

---

### Q3: 如何处理 LLM 的幻觉（Hallucination）？

**A:**
- 使用 RAG 提供真实数据
- 在提示词中要求引用来源
- 验证 LLM 的输出
- 使用更强大的模型

---

### Q4: 如何提高 RAG 的准确性？

**A:**
- 优化文档分块大小
- 选择合适的 Embedding 模型
- 调整检索的相似度阈值
- 使用多步检索策略

---

### Q5: 如何监控和优化成本？

**A:**
- 记录每次 API 调用的 Token 数
- 设置成本告警
- 定期分析成本趋势
- 优化提示词和缓存策略

---

## 学习建议

### 初级开发者
1. 从 Scenario 1 开始，理解基础 LLM 调用
2. 学习提示词工程的基本原理
3. 实践多轮对话的实现
4. 理解 Token 的概念和成本

### 中级开发者
1. 深入学习 RAG 技术（Scenario 2）
2. 学习专业应用的实现（Scenario 3-5）
3. 理解 Agent 的工作原理（Scenario 6）
4. 实践流式响应和性能优化

### 高级开发者
1. 学习 MCP 协议和标准化设计（Scenario 7）
2. 研究 Agent 的高级特性
3. 实现权限控制和审计日志
4. 优化成本和性能

---

## 项目资源

### 官方文档
- [LangChain4j 官方文档](https://docs.langchain4j.dev/)
- [OpenAI API 文档](https://platform.openai.com/docs)
- [Spring Boot 文档](https://spring.io/projects/spring-boot)

### 相关论文
- [ReAct: Synergizing Reasoning and Acting in Language Models](https://arxiv.org/abs/2210.03629)
- [Retrieval-Augmented Generation for Knowledge-Intensive NLP Tasks](https://arxiv.org/abs/2005.11401)

### 学习资源
- 每个 Scenario 都有详细的 README.md 和学习指南
- 代码中包含详细的中文注释
- 提供了完整的 API 示例

---

## 总结

通过这个项目，你将学到：

✅ LLM 的基础概念和调用方式
✅ 多轮对话的实现
✅ RAG 技术的应用
✅ 专业 AI 应用的开发
✅ Agent 智能体的工作原理
✅ MCP 标准化协议
✅ 性能优化和成本控制
✅ 生产环境的最佳实践

**下一步：**
- 选择感兴趣的 Scenario 深入学习
- 修改代码进行实验
- 添加新的工具和功能
- 集成到实际项目中
