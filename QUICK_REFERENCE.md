# 快速参考卡片

## 核心概念速查表

### LLM 基础调用
```java
// 基础调用
Response<AiMessage> response = chatModel.generate(messages);
String answer = response.content().text();

// 获取 Token 使用情况
int inputTokens = response.tokenUsage().inputTokenCount();
int outputTokens = response.tokenUsage().outputTokenCount();
```

### 消息构建
```java
List<ChatMessage> messages = new ArrayList<>();
messages.add(SystemMessage.from("你是一个专业的..."));
messages.add(UserMessage.from("用户问题"));
messages.add(AiMessage.from("AI 回复"));
```

### 多轮对话
```java
// 保存对话历史
ConversationMemoryUtil.addMessage("user", userMessage);
List<ChatMessage> history = ConversationMemoryUtil.getMessages();

// 调用 LLM
Response<AiMessage> response = chatModel.generate(history);

// 保存 AI 回复
ConversationMemoryUtil.addMessage("assistant", response.content().text());
```

### Embedding 和向量化
```java
// 向量化文本
Response<Embedding> embedding = embeddingModel.embed(text);
Embedding vector = embedding.content();

// 存储向量
embeddingStore.add(vector, textSegment);

// 检索相似文本
List<EmbeddingMatch<TextSegment>> matches =
    embeddingStore.findRelevant(queryEmbedding, 3);
```

### Agent 工具调用
```java
// 检查是否需要调用工具
if (aiMessage.hasToolExecutionRequests()) {
    for (ToolExecutionRequest toolRequest : aiMessage.toolExecutionRequests()) {
        // 执行工具
        Object result = executeToolRequest(toolRequest);
        // 反馈结果
        messages.add(UserMessage.from("工具结果: " + result));
    }
}
```

### MCP 工具执行
```java
// 构建工具请求
McpToolExecutionRequest request = new McpToolExecutionRequest(
    "req_123",
    "calculate",
    Map.of("operation", "+", "num1", 100, "num2", 50)
);

// 执行工具
McpToolExecutionResult result = mcpService.executeTool(request);
```

---

## 常用代码片段

### Token 计数
```java
// 计算 Token 数
int tokenCount = TokenCountUtil.countTokens(text);

// 检查是否超过限制
if (TokenCountUtil.exceedsLimit(text, 2000)) {
    text = TokenCountUtil.truncateToTokenLimit(text, 2000);
}
```

### 提示词构建
```java
// 使用 PromptBuilder
List<ChatMessage> messages = PromptBuilder.buildMessages(
    systemPrompt,
    userMessage
);

// 带历史的提示词
List<ChatMessage> messagesWithHistory = PromptBuilder.buildMessagesWithHistory(
    systemPrompt,
    conversationHistory,
    userMessage
);

// RAG 提示词
List<ChatMessage> ragMessages = PromptBuilder.buildRagUserMessage(
    contextSegments,
    question
);
```

### 错误处理
```java
try {
    Response<AiMessage> response = chatModel.generate(messages);
    return response.content().text();
} catch (RateLimitException e) {
    // 处理速率限制
    Thread.sleep(1000);
    return retryCall();
} catch (TokenLimitException e) {
    // 处理 Token 限制
    String compressed = compressText(text);
    return callWithCompressed(compressed);
} catch (Exception e) {
    logger.error("LLM call failed", e);
    return "Error: " + e.getMessage();
}
```

### 流式响应
```java
@GetMapping("/stream")
public SseEmitter stream(@RequestParam String message) {
    SseEmitter emitter = new SseEmitter();

    StreamingResponseHandler<AiMessage> handler =
        new StreamingResponseHandler<AiMessage>() {
            @Override
            public void onNext(String token) {
                try {
                    emitter.send(token);
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            }

            @Override
            public void onComplete(Response<AiMessage> response) {
                try {
                    emitter.send("[DONE]");
                    emitter.complete();
                } catch (IOException e) {
                    emitter.completeWithError(e);
                }
            }
        };

    streamingChatModel.generate(messages, handler);
    return emitter;
}
```

### 缓存
```java
@Cacheable(value = "llm_cache", key = "#message")
public String chat(String message) {
    return chatModel.generate(message).content().text();
}

@CacheEvict(value = "llm_cache", allEntries = true)
public void clearCache() {
}
```

### 异步处理
```java
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
    return chatModel.generate(messages).content().text();
});

String result = future.get(30, TimeUnit.SECONDS);
```

---

## Temperature 参考

| 值 | 特性 | 适用场景 |
|----|------|---------|
| 0.0-0.3 | 确定性强，输出稳定 | 分析、翻译、代码生成 |
| 0.4-0.6 | 平衡创意和确定性 | 一般对话、总结 |
| 0.7-0.9 | 创意强，输出多样 | 创意写作、头脑风暴 |
| 1.0+ | 最大随机性 | 创意极强的任务 |

---

## 端口映射

| 场景 | 端口 | 功能 |
|------|------|------|
| Scenario 1 | 8081 | 客服系统 |
| Scenario 2 | 8082 | 文档分析（RAG） |
| Scenario 3 | 8083 | 代码助手 |
| Scenario 4 | 8084 | 数据分析 |
| Scenario 5 | 8085 | 内容创作 |
| Scenario 6 | 8086 | Agent 智能体 |
| Scenario 7 | 8087 | MCP 协议 |

---

## 常用 curl 命令

### 客服对话
```bash
curl -X POST http://localhost:8081/api/customer-service/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "你好，请问如何退货？"}'
```

### 文档查询
```bash
curl -X POST http://localhost:8082/api/document-analysis/query/doc1 \
  -H "Content-Type: application/json" \
  -d '{"query": "合同的主要条款是什么？"}'
```

### 代码审查
```bash
curl -X POST http://localhost:8083/api/code-assistant/review \
  -H "Content-Type: application/json" \
  -d '{"code": "public void test() { ... }"}'
```

### SQL 生成
```bash
curl -X POST http://localhost:8084/api/data-analyst/generate-sql \
  -H "Content-Type: application/json" \
  -d '{"requirement": "查询过去7天的订单数量"}'
```

### 内容生成
```bash
curl -X POST http://localhost:8085/api/content-creator/generate-article \
  -H "Content-Type: application/json" \
  -d '{"topic": "AI 的未来", "length": 1000}'
```

### Agent 对话
```bash
curl -X POST http://localhost:8086/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "现在几点了？"}'
```

### MCP 工具执行
```bash
curl -X POST http://localhost:8087/api/mcp/execute \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "req_123",
    "toolName": "calculate",
    "arguments": {"operation": "+", "num1": 100, "num2": 50}
  }'
```

---

## 环境变量

```bash
# OpenAI API Key
export OPENAI_API_KEY=sk-your-key-here

# 可选：代理设置
export HTTP_PROXY=http://proxy.example.com:8080
export HTTPS_PROXY=http://proxy.example.com:8080
```

---

## 启动命令

```bash
# 启动所有模块
mvn spring-boot:run

# 启动特定模块
mvn spring-boot:run -pl scenario-1-customer-service
mvn spring-boot:run -pl scenario-6-intelligent-agent
mvn spring-boot:run -pl scenario-7-mcp-protocol

# 编译
mvn clean compile

# 测试
mvn test

# 打包
mvn clean package
```
