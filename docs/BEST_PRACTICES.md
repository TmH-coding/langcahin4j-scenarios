# LangChain4j 最佳实践指南

在使用 LangChain4j 构建 AI 应用时的核心最佳实践和模式。

## 1. Prompt 工程最佳实践

### 1.1 系统提示词设计

**原则**：清晰、具体、有约束

```java
// ❌ 不好的系统提示词
"You are a helpful assistant."

// ✅ 好的系统提示词
"You are a professional customer service representative. " +
"Answer customer questions professionally and courteously. " +
"If you don't know the answer, suggest contacting support. " +
"Keep responses concise (under 200 words). " +
"Always be polite and empathetic."
```

**关键要素**：
- 定义角色和专业领域
- 指定行为约束
- 设定输出格式
- 提供处理边界情况的指导

### 1.2 温度参数选择

```java
// 精确任务（代码、SQL、数据分析）
temperature = 0.3 - 0.5

// 平衡任务（客服、文档分析）
temperature = 0.5 - 0.7

// 创意任务（内容创作、头脑风暴）
temperature = 0.7 - 1.0
```

### 1.3 上下文管理

```java
// ❌ 不好：没有上下文
String prompt = "What is the answer?";

// ✅ 好：提供充分的上下文
String prompt = String.format(
    "Based on the following document:\n\n%s\n\n" +
    "Question: %s\n\n" +
    "Provide a concise answer with relevant citations.",
    documentContent, question
);
```

### 1.4 使用示例和模式

```java
String systemPrompt = "You are a code reviewer. " +
    "Review code and provide feedback in this format:\n" +
    "1. Issues Found: [list issues]\n" +
    "2. Suggestions: [list suggestions]\n" +
    "3. Overall Quality: [score 1-10]\n" +
    "Example:\n" +
    "1. Issues Found: Missing null check\n" +
    "2. Suggestions: Add input validation\n" +
    "3. Overall Quality: 7/10";
```

---

## 2. 对话管理最佳实践

### 2.1 内存管理

```java
// ✅ 使用滑动窗口限制历史记录
ConversationMemoryUtil memory = new ConversationMemoryUtil(20); // 最多保存20条消息

// 定期清理过期对话
if (memory.size() > 20) {
    memory.clear();
    // 重新开始对话
}
```

**为什么重要**：
- 减少 API 调用成本
- 提高响应速度
- 防止上下文污染

### 2.2 消息格式化

```java
// ✅ 结构化的消息格式
List<ChatMessage> messages = new ArrayList<>();
messages.add(new SystemMessage(systemPrompt));

// 添加相关的历史消息
for (ConversationMessage msg : memory.getMessages()) {
    if ("user".equals(msg.getRole())) {
        messages.add(new UserMessage(msg.getContent()));
    } else {
        messages.add(new dev.langchain4j.model.common.request.AiMessage(msg.getContent()));
    }
}

messages.add(new UserMessage(currentUserMessage));
```

### 2.3 错误恢复

```java
// ✅ 实现重试机制
public String chatWithRetry(String message, int maxRetries) {
    for (int i = 0; i < maxRetries; i++) {
        try {
            return chat(message);
        } catch (Exception e) {
            if (i == maxRetries - 1) {
                throw e;
            }
            // 指数退避
            Thread.sleep((long) Math.pow(2, i) * 1000);
        }
    }
    return null;
}
```

---

## 3. 文档处理最佳实践

### 3.1 分块策略

```java
// ✅ 根据文档类型选择合适的分块大小
// 代码文档：300-500 字符
// 技术文档：500-800 字符
// 长篇文章：800-1000 字符

DocumentSplitter splitter = DocumentSplitters.recursive(
    500,  // maxSegmentSize
    50    // overlapSize - 10% 的重叠
);

List<TextSegment> segments = splitter.split(document);
```

**参数指导**：
- `maxSegmentSize`：根据内容复杂度调整
- `overlapSize`：通常为 maxSegmentSize 的 10%
- 重叠有助于保持上下文连贯性

### 3.2 文档预处理

```java
// ✅ 清理和规范化文档
public Document preprocessDocument(Document document) {
    String content = document.text();

    // 移除多余空白
    content = content.replaceAll("\\s+", " ");

    // 移除特殊字符（如果需要）
    content = content.replaceAll("[^\\w\\s\\p{P}]", "");

    return new Document(content);
}
```

### 3.3 检索优化

```java
// ✅ 多策略检索
public List<TextSegment> retrieveRelevantSegments(
        List<TextSegment> segments,
        String query,
        int topK) {

    // 1. 关键词匹配
    List<TextSegment> keywordMatches = segments.stream()
        .filter(seg -> seg.text().toLowerCase().contains(query.toLowerCase()))
        .limit(topK)
        .toList();

    // 2. 如果关键词匹配不足，使用语义相似度
    if (keywordMatches.size() < topK) {
        // 使用向量相似度搜索
    }

    return keywordMatches;
}
```

---

## 4. 错误处理最佳实践

### 4.1 异常处理

```java
// ✅ 完善的异常处理
public String askAssistant(String prompt) {
    try {
        ChatRequest request = ChatRequest.builder()
                .messages(messages)
                .build();

        ChatResponse response = chatModel.chat(request);
        return response.aiMessage().text();

    } catch (RateLimitException e) {
        // 处理速率限制
        logger.warn("Rate limit exceeded, retrying...");
        Thread.sleep(5000);
        return askAssistant(prompt);

    } catch (AuthenticationException e) {
        // 处理认证错误
        logger.error("Authentication failed: {}", e.getMessage());
        throw new RuntimeException("Invalid API key");

    } catch (Exception e) {
        // 通用异常处理
        logger.error("Unexpected error: {}", e.getMessage(), e);
        throw new RuntimeException("Failed to get response from AI", e);
    }
}
```

### 4.2 输入验证

```java
// ✅ 在系统边界验证输入
@PostMapping("/chat")
public String chat(@RequestParam String message) {
    // 验证输入
    if (message == null || message.trim().isEmpty()) {
        throw new IllegalArgumentException("Message cannot be empty");
    }

    if (message.length() > 5000) {
        throw new IllegalArgumentException("Message too long (max 5000 chars)");
    }

    // 清理输入
    String cleanMessage = message.trim();

    return customerServiceAI.chat(cleanMessage);
}
```

### 4.3 日志记录

```java
// ✅ 详细的日志记录
private static final Logger logger = LoggerFactory.getLogger(CustomerServiceAI.class);

public String chat(String userMessage) {
    logger.info("Received message from user: {}", userMessage);

    try {
        String response = chatModel.chat(request);
        logger.debug("AI response: {}", response);
        return response;

    } catch (Exception e) {
        logger.error("Error processing message: {}", userMessage, e);
        throw e;
    }
}
```

---

## 5. 性能优化最佳实践

### 5.1 缓存策略

```java
// ✅ 实现简单的缓存
private final Map<String, String> cache = new ConcurrentHashMap<>();
private final int CACHE_SIZE = 1000;

public String getOrCompute(String key, Function<String, String> compute) {
    return cache.computeIfAbsent(key, k -> {
        if (cache.size() >= CACHE_SIZE) {
            // 移除最旧的条目
            cache.remove(cache.keySet().iterator().next());
        }
        return compute.apply(k);
    });
}
```

### 5.2 批量处理

```java
// ✅ 批量处理多个请求
public List<String> processBatch(List<String> messages) {
    return messages.parallelStream()
        .map(this::chat)
        .collect(Collectors.toList());
}
```

### 5.3 异步处理

```java
// ✅ 使用异步处理长时间操作
@Async
public CompletableFuture<String> chatAsync(String message) {
    return CompletableFuture.supplyAsync(() -> chat(message));
}

// 使用
CompletableFuture<String> future = customerServiceAI.chatAsync("Hello");
future.thenAccept(response -> logger.info("Response: {}", response));
```

---

## 6. 安全性最佳实践

### 6.1 API Key 管理

```java
// ❌ 不好：硬编码 API Key
String apiKey = "sk-xxx";

// ✅ 好：从环境变量读取
String apiKey = System.getenv("OPENAI_API_KEY");

// ✅ 更好：使用配置文件
@Value("${openai.api.key}")
private String apiKey;
```

### 6.2 输入清理

```java
// ✅ 防止注入攻击
public String sanitizeInput(String input) {
    // 移除潜在的危险字符
    return input.replaceAll("[<>\"'%;()&+]", "");
}
```

### 6.3 速率限制

```java
// ✅ 实现速率限制
@Component
public class RateLimiter {
    private final Map<String, Queue<Long>> userRequests = new ConcurrentHashMap<>();
    private final int MAX_REQUESTS = 10;
    private final long TIME_WINDOW = 60000; // 1 分钟

    public boolean isAllowed(String userId) {
        Queue<Long> requests = userRequests.computeIfAbsent(userId, k -> new LinkedList<>());

        long now = System.currentTimeMillis();
        while (!requests.isEmpty() && requests.peek() < now - TIME_WINDOW) {
            requests.poll();
        }

        if (requests.size() < MAX_REQUESTS) {
            requests.offer(now);
            return true;
        }
        return false;
    }
}
```

---

## 7. 测试最佳实践

### 7.1 单元测试

```java
@Test
public void testCustomerServiceChat() {
    // Arrange
    String userMessage = "你好";

    // Act
    String response = customerServiceAI.chat(userMessage);

    // Assert
    assertNotNull(response);
    assertTrue(response.length() > 0);
    assertFalse(response.contains("error"));
}
```

### 7.2 Mock 测试

```java
@Test
public void testChatWithMockModel() {
    // Mock ChatLanguageModel
    ChatLanguageModel mockModel = mock(ChatLanguageModel.class);
    ChatResponse mockResponse = mock(ChatResponse.class);

    when(mockModel.chat(any(ChatRequest.class)))
        .thenReturn(mockResponse);

    when(mockResponse.aiMessage().text())
        .thenReturn("Mocked response");

    // 测试
    CustomerServiceAI service = new CustomerServiceAI(mockModel);
    String result = service.chat("test");

    assertEquals("Mocked response", result);
}
```

### 7.3 集成测试

```java
@SpringBootTest
public class CustomerServiceIntegrationTest {

    @Autowired
    private CustomerServiceAI customerServiceAI;

    @Test
    public void testFullConversation() {
        String response1 = customerServiceAI.chat("你好");
        assertNotNull(response1);

        String response2 = customerServiceAI.chat("我想了解你的服务");
        assertNotNull(response2);

        String history = customerServiceAI.getConversationHistory();
        assertTrue(history.contains("你好"));
    }
}
```

---

## 8. 监控和日志最佳实践

### 8.1 关键指标

```java
// ✅ 记录关键指标
public String chat(String message) {
    long startTime = System.currentTimeMillis();

    try {
        String response = chatModel.chat(request);

        long duration = System.currentTimeMillis() - startTime;
        metrics.recordLatency("chat", duration);
        metrics.incrementCounter("chat.success");

        return response;

    } catch (Exception e) {
        metrics.incrementCounter("chat.error");
        throw e;
    }
}
```

### 8.2 结构化日志

```java
// ✅ 使用结构化日志便于分析
logger.info("Chat request processed",
    "userId", userId,
    "messageLength", message.length(),
    "responseTime", duration,
    "model", "gpt-4-turbo-preview"
);
```

---

## 9. 成本优化最佳实践

### 9.1 模型选择

```java
// ✅ 根据任务选择合适的模型
// 简单任务：gpt-3.5-turbo（便宜）
// 复杂任务：gpt-4-turbo-preview（贵但更强）
// 嵌入任务：text-embedding-3-small（最便宜）
```

### 9.2 请求优化

```java
// ✅ 减少不必要的 API 调用
// 1. 使用缓存
// 2. 批量处理
// 3. 只发送必要的上下文
// 4. 使用更短的提示词
```

### 9.3 成本监控

```java
// ✅ 跟踪 API 使用成本
public class CostTracker {
    private final Map<String, Double> modelCosts = Map.of(
        "gpt-4-turbo-preview", 0.01,  // 每 1K tokens
        "gpt-3.5-turbo", 0.0005
    );

    public void trackUsage(String model, int tokens) {
        double cost = (tokens / 1000.0) * modelCosts.get(model);
        logger.info("API usage cost: ${}", cost);
    }
}
```

---

## 10. 常见陷阱和解决方案

| 问题 | 原因 | 解决方案 |
|------|------|--------|
| 响应质量差 | 提示词不清晰 | 优化系统提示词，提供更多上下文 |
| 成本过高 | 频繁调用 API | 实现缓存、批量处理、使用更便宜的模型 |
| 响应缓慢 | 对话历史过长 | 使用滑动窗口限制历史记录 |
| 内存泄漏 | 缓存无限增长 | 实现 LRU 缓存或定期清理 |
| 错误处理不足 | 没有重试机制 | 实现指数退避重试 |
| 安全问题 | API Key 硬编码 | 使用环境变量或配置文件 |

---

## 总结

遵循这些最佳实践可以帮助你：
- 构建更可靠的 AI 应用
- 降低运营成本
- 提高用户体验
- 确保系统安全性
- 便于维护和扩展
