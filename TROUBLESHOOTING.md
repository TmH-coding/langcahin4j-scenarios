# 常见错误和解决方案

## LLM 调用错误

### 1. RateLimitException - 速率限制

**错误信息：**
```
RateLimitException: Rate limit exceeded
```

**原因：**
- 请求过于频繁
- 超过了 API 的速率限制

**解决方案：**
```java
// 方案 1: 添加延迟
Thread.sleep(1000);
response = chatModel.generate(messages);

// 方案 2: 指数退避重试
int maxRetries = 3;
int delay = 1000;

for (int i = 0; i < maxRetries; i++) {
    try {
        return chatModel.generate(messages);
    } catch (RateLimitException e) {
        if (i < maxRetries - 1) {
            Thread.sleep(delay);
            delay *= 2;
        } else {
            throw e;
        }
    }
}

// 方案 3: 使用队列限流
BlockingQueue<String> queue = new LinkedBlockingQueue<>(10);
queue.put(message);
```

---

### 2. TokenLimitException - Token 限制

**错误信息：**
```
TokenLimitException: Input exceeds token limit
```

**原因：**
- 输入文本过长
- 超过了模型的最大 Token 限制

**解决方案：**
```java
// 方案 1: 压缩文本
String compressed = text.replaceAll("\\s+", " ");

// 方案 2: 截断文本
String truncated = TokenCountUtil.truncateToTokenLimit(text, 2000);

// 方案 3: 分割处理
List<String> chunks = splitText(text, 1000);
for (String chunk : chunks) {
    processChunk(chunk);
}

// 方案 4: 使用摘要
String summary = generateSummary(text);
```

---

### 3. InvalidApiKeyException - API Key 错误

**错误信息：**
```
InvalidApiKeyException: Invalid API key
```

**原因：**
- API Key 不正确
- API Key 已过期
- 环境变量未设置

**解决方案：**
```bash
# 检查环境变量
echo $OPENAI_API_KEY

# 设置环境变量
export OPENAI_API_KEY=sk-your-key-here

# 验证 API Key 格式
# OpenAI Key 应该以 sk- 开头
```

---

### 4. ConnectionException - 连接错误

**错误信息：**
```
ConnectionException: Failed to connect to API
```

**原因：**
- 网络连接问题
- API 服务不可用
- 代理配置错误

**解决方案：**
```java
// 方案 1: 添加超时设置
ChatLanguageModel model = OpenAiChatModel.builder()
    .apiKey(apiKey)
    .timeout(Duration.ofSeconds(60))
    .build();

// 方案 2: 配置代理
System.setProperty("http.proxyHost", "proxy.example.com");
System.setProperty("http.proxyPort", "8080");

// 方案 3: 重试机制
int maxRetries = 3;
for (int i = 0; i < maxRetries; i++) {
    try {
        return chatModel.generate(messages);
    } catch (ConnectionException e) {
        if (i < maxRetries - 1) {
            Thread.sleep(1000 * (i + 1));
        } else {
            throw e;
        }
    }
}
```

---

## 多轮对话错误

### 5. 对话历史丢失

**症状：**
- AI 无法记住之前的对话
- 每次都重复回答相同的问题

**原因：**
- 对话历史未正确保存
- 消息列表未传递给 LLM

**解决方案：**
```java
// 正确的做法
List<ChatMessage> messages = new ArrayList<>();
messages.add(SystemMessage.from(systemPrompt));

// 添加历史消息
for (ConversationMessage msg : conversationHistory) {
    if ("user".equals(msg.getRole())) {
        messages.add(UserMessage.from(msg.getContent()));
    } else {
        messages.add(AiMessage.from(msg.getContent()));
    }
}

// 添加当前消息
messages.add(UserMessage.from(currentMessage));

// 调用 LLM
Response<AiMessage> response = chatModel.generate(messages);
```

---

### 6. 对话上下文过长

**症状：**
- 响应变慢
- Token 消耗过多
- 成本增加

**原因：**
- 保存了过多的对话历史
- 未进行消息压缩

**解决方案：**
```java
// 方案 1: 限制历史消息数量
int maxMessages = 20;
if (messages.size() > maxMessages) {
    messages = messages.subList(
        messages.size() - maxMessages,
        messages.size()
    );
}

// 方案 2: 定期生成摘要
if (messages.size() > 50) {
    String summary = generateConversationSummary(messages);
    messages.clear();
    messages.add(SystemMessage.from(systemPrompt));
    messages.add(UserMessage.from("之前的对话摘要: " + summary));
}

// 方案 3: 使用滑动窗口
List<ChatMessage> window = messages.stream()
    .skip(Math.max(0, messages.size() - 10))
    .collect(Collectors.toList());
```

---

## RAG 错误

### 7. 检索结果不相关

**症状：**
- 返回的文档与查询无关
- 答案不准确

**原因：**
- Embedding 模型不适合
- 相似度阈值设置不当
- 文档分块不合理

**解决方案：**
```java
// 方案 1: 调整相似度阈值
List<EmbeddingMatch<TextSegment>> matches =
    embeddingStore.findRelevant(queryEmbedding, 5);

// 过滤低相似度结果
List<EmbeddingMatch<TextSegment>> filtered = matches.stream()
    .filter(m -> m.score() > 0.7)  // 调整阈值
    .collect(Collectors.toList());

// 方案 2: 使用更好的 Embedding 模型
EmbeddingModel model = new OpenAiEmbeddingModel.builder()
    .modelName("text-embedding-3-large")  // 更强大的模型
    .build();

// 方案 3: 优化文档分块
List<TextSegment> chunks = splitDocument(document,
    chunkSize = 512,      // 调整块大小
    overlapSize = 50      // 添加重叠
);
```

---

### 8. 向量存储性能差

**症状：**
- 检索速度慢
- 内存占用过高

**原因：**
- 使用了内存存储
- 向量数量过多

**解决方案：**
```java
// 方案 1: 使用专业向量数据库
// 替代 InMemoryEmbeddingStore
EmbeddingStore<TextSegment> store = new MilvusEmbeddingStore.builder()
    .host("localhost")
    .port(19530)
    .build();

// 方案 2: 添加索引
embeddingStore.createIndex();

// 方案 3: 分页检索
int pageSize = 100;
for (int i = 0; i < totalDocuments; i += pageSize) {
    List<TextSegment> batch = documents.subList(i,
        Math.min(i + pageSize, totalDocuments));
    embeddingStore.addAll(batch);
}
```

---

## Agent 错误

### 9. Agent 陷入无限循环

**症状：**
- 程序一直运行不停
- 不断调用相同的工具

**原因：**
- 未设置最大迭代次数
- 工具返回结果无法改变 LLM 的决策

**解决方案：**
```java
// 方案 1: 设置最大迭代次数
int maxIterations = 10;
int iteration = 0;

while (iteration < maxIterations) {
    iteration++;
    // Agent 循环逻辑
}

// 方案 2: 设置超时
long startTime = System.currentTimeMillis();
long timeout = 30000; // 30 秒

while (System.currentTimeMillis() - startTime < timeout) {
    // Agent 循环逻辑
}

// 方案 3: 检测重复调用
Set<String> calledTools = new HashSet<>();
for (ToolExecutionRequest tool : toolRequests) {
    if (calledTools.contains(tool.name())) {
        // 检测到重复调用，停止
        break;
    }
    calledTools.add(tool.name());
}
```

---

### 10. 工具调用失败

**症状：**
- 工具执行返回错误
- Agent 无法继续

**原因：**
- 工具参数错误
- 工具实现有 Bug
- 外部服务不可用

**解决方案：**
```java
// 方案 1: 验证工具参数
Map<String, Object> args = parseArguments(toolRequest);
if (!validateArguments(args, toolSchema)) {
    return new ToolExecutionResult(false, "Invalid arguments");
}

// 方案 2: 添加错误处理
try {
    Object result = executeTool(toolRequest);
    return new ToolExecutionResult(true, result);
} catch (Exception e) {
    logger.error("Tool execution failed", e);
    return new ToolExecutionResult(false, "Error: " + e.getMessage());
}

// 方案 3: 重试机制
int maxRetries = 3;
for (int i = 0; i < maxRetries; i++) {
    try {
        return executeTool(toolRequest);
    } catch (Exception e) {
        if (i < maxRetries - 1) {
            Thread.sleep(1000);
        } else {
            throw e;
        }
    }
}
```

---

## 调试技巧

### 启用详细日志
```properties
# application.properties
logging.level.dev.langchain4j=DEBUG
logging.level.com.langchain4j.scenarios=DEBUG
```

### 打印完整的消息列表
```java
logger.debug("Messages: {}", messages.stream()
    .map(m -> m.getClass().getSimpleName() + ": " + m.toString())
    .collect(Collectors.toList()));
```

### 记录 Token 使用
```java
Response<AiMessage> response = chatModel.generate(messages);
logger.info("Input tokens: {}, Output tokens: {}",
    response.tokenUsage().inputTokenCount(),
    response.tokenUsage().outputTokenCount());
```

### 保存请求和响应
```java
ObjectMapper mapper = new ObjectMapper();
mapper.writeValue(new File("request.json"), messages);
mapper.writeValue(new File("response.json"), response);
```

---

## 性能问题

### 11. 响应缓慢

**原因：**
- LLM 处理时间长
- 网络延迟
- 工具执行时间长

**解决方案：**
```java
// 方案 1: 使用流式响应
streamingChatModel.generate(messages, handler);

// 方案 2: 异步处理
CompletableFuture<String> future = CompletableFuture.supplyAsync(
    () -> chatModel.generate(messages).content().text()
);

// 方案 3: 缓存结果
@Cacheable(value = "llm_cache", key = "#message")
public String chat(String message) {
    return chatModel.generate(message).content().text();
}
```

---

### 12. 内存泄漏

**症状：**
- 内存占用不断增加
- 最终导致 OutOfMemoryError

**原因：**
- 对话历史未清理
- 向量存储占用过多内存
- 缓存未过期

**解决方案：**
```java
// 方案 1: 定期清理对话历史
@Scheduled(fixedDelay = 3600000) // 每小时
public void cleanupConversations() {
    conversationRepository.deleteOlderThan(
        LocalDateTime.now().minusHours(24)
    );
}

// 方案 2: 使用 LRU 缓存
Cache<String, String> cache = CacheBuilder.newBuilder()
    .maximumSize(1000)
    .expireAfterWrite(1, TimeUnit.HOURS)
    .build();

// 方案 3: 监控内存使用
Runtime runtime = Runtime.getRuntime();
long usedMemory = runtime.totalMemory() - runtime.freeMemory();
if (usedMemory > MAX_MEMORY) {
    // 采取措施
}
```

---

## 快速诊断清单

- [ ] API Key 是否正确设置？
- [ ] 网络连接是否正常？
- [ ] Token 数量是否超过限制？
- [ ] 对话历史是否过长？
- [ ] 工具参数是否正确？
- [ ] 是否有足够的内存？
- [ ] 日志中是否有错误信息？
- [ ] 是否需要重试？
