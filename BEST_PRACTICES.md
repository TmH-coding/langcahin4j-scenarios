# 最佳实践指南

## 代码规范

### 命名规范

```java
// 类名：PascalCase
public class CustomerServiceAI { }
public class DocumentAnalysisService { }

// 方法名：camelCase
public String chat(String message) { }
public List<McpTool> getAvailableTools() { }

// 常量：UPPER_SNAKE_CASE
private static final String SYSTEM_PROMPT = "...";
private static final int MAX_ITERATIONS = 10;

// 变量：camelCase
String userMessage = "...";
List<ChatMessage> messages = new ArrayList<>();
```

### 注释规范

```java
/**
 * 与 LLM 进行对话
 *
 * 这个方法实现了多轮对话的核心逻辑。
 * 它维护对话历史，调用 LLM 生成回复。
 *
 * @param userMessage 用户输入的消息
 * @return LLM 生成的回复
 * @throws Exception 如果 LLM 调用失败
 *
 * 使用示例：
 * String reply = chat("你好");
 * System.out.println(reply);
 */
public String chat(String userMessage) {
    // 实现逻辑
}
```

### 异常处理

```java
// 好的做法：具体的异常处理
try {
    Response<AiMessage> response = chatModel.generate(messages);
    return response.content().text();
} catch (RateLimitException e) {
    logger.warn("Rate limit exceeded, retrying...", e);
    Thread.sleep(1000);
    return retryCall();
} catch (TokenLimitException e) {
    logger.error("Token limit exceeded", e);
    throw new ApplicationException("Input too long", e);
} catch (Exception e) {
    logger.error("Unexpected error", e);
    throw new ApplicationException("LLM call failed", e);
}

// 不好的做法：捕获所有异常
try {
    // ...
} catch (Exception e) {
    e.printStackTrace();
}
```

---

## 设计模式

### 1. 工厂模式 - 创建 LLM 实例

```java
@Configuration
public class LlmConfig {
    @Bean
    public ChatLanguageModel chatLanguageModel(LlmProperties properties) {
        return OpenAiChatModel.builder()
            .apiKey(System.getenv("OPENAI_API_KEY"))
            .modelName(properties.getModel())
            .temperature(properties.getTemperature())
            .maxTokens(properties.getMaxTokens())
            .timeout(Duration.ofSeconds(properties.getTimeout()))
            .build();
    }
}
```

### 2. 策略模式 - 不同的提示词策略

```java
public interface PromptStrategy {
    String buildPrompt(String input);
}

public class CodeReviewStrategy implements PromptStrategy {
    @Override
    public String buildPrompt(String code) {
        return "请审查以下代码...\n" + code;
    }
}

public class ContentCreationStrategy implements PromptStrategy {
    @Override
    public String buildPrompt(String topic) {
        return "请创作关于 " + topic + " 的内容...";
    }
}
```

### 3. 装饰器模式 - 添加功能

```java
public class CachedChatLanguageModel implements ChatLanguageModel {
    private final ChatLanguageModel delegate;
    private final Cache<String, String> cache;

    @Override
    public Response<AiMessage> generate(List<ChatMessage> messages) {
        String key = hashMessages(messages);
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        Response<AiMessage> response = delegate.generate(messages);
        cache.put(key, response);
        return response;
    }
}
```

### 4. 观察者模式 - 事件通知

```java
public interface ChatEventListener {
    void onMessageReceived(String message);
    void onResponseGenerated(String response);
    void onError(Exception e);
}

public class ChatService {
    private List<ChatEventListener> listeners = new ArrayList<>();

    public void addListener(ChatEventListener listener) {
        listeners.add(listener);
    }

    public String chat(String message) {
        listeners.forEach(l -> l.onMessageReceived(message));
        try {
            String response = chatModel.generate(message);
            listeners.forEach(l -> l.onResponseGenerated(response));
            return response;
        } catch (Exception e) {
            listeners.forEach(l -> l.onError(e));
            throw e;
        }
    }
}
```

---

## 架构最佳实践

### 1. 分层架构

```
Controller 层（REST API）
    ↓
Service 层（业务逻辑）
    ↓
Repository 层（数据访问）
    ↓
Model 层（数据模型）
```

**好处：**
- 职责清晰
- 易于测试
- 易于维护

### 2. 依赖注入

```java
// 好的做法：使用构造函数注入
@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatLanguageModel chatModel;
    private final ConversationRepository repository;

    public String chat(String message) {
        // 使用注入的依赖
    }
}

// 不好的做法：直接创建实例
public class ChatService {
    private ChatLanguageModel chatModel = new OpenAiChatModel(...);
}
```

### 3. 配置管理

```java
// 使用配置类
@ConfigurationProperties(prefix = "llm")
@Component
@Data
public class LlmProperties {
    private String model = "gpt-4-turbo-preview";
    private Double temperature = 0.7;
    private Integer maxTokens = 2048;
    private Integer timeout = 60;
}

// 在 application.properties 中配置
llm.model=gpt-4-turbo-preview
llm.temperature=0.7
llm.max-tokens=2048
llm.timeout=60
```

---

## 测试最佳实践

### 1. 单元测试

```java
@SpringBootTest
class ChatServiceTest {
    @MockBean
    private ChatLanguageModel chatModel;

    @Autowired
    private ChatService chatService;

    @Test
    void testChat() {
        // 准备
        String userMessage = "你好";
        AiMessage aiMessage = AiMessage.from("你好，有什么帮助吗？");
        Response<AiMessage> response = Response.from(aiMessage);

        when(chatModel.generate(any())).thenReturn(response);

        // 执行
        String result = chatService.chat(userMessage);

        // 验证
        assertEquals("你好，有什么帮助吗？", result);
        verify(chatModel).generate(any());
    }
}
```

### 2. 集成测试

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatControllerTest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testChatEndpoint() {
        // 准备
        Map<String, String> request = Map.of("message", "你好");

        // 执行
        ResponseEntity<Map> response = restTemplate.postForEntity(
            "/api/customer-service/chat",
            request,
            Map.class
        );

        // 验证
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().get("message"));
    }
}
```

---

## 日志最佳实践

### 1. 日志级别

```java
// DEBUG：详细的调试信息
logger.debug("Processing message: {}", message);

// INFO：重要的业务事件
logger.info("User {} logged in", userId);

// WARN：警告信息
logger.warn("Rate limit approaching: {}/{}", current, limit);

// ERROR：错误信息
logger.error("Failed to process request", exception);
```

### 2. 结构化日志

```java
// 好的做法：包含上下文信息
logger.info("Chat completed",
    "userId", userId,
    "messageLength", message.length(),
    "responseTime", duration,
    "tokenUsage", tokenCount
);

// 不好的做法：只有文本
logger.info("Chat completed");
```

---

## 安全最佳实践

### 1. API Key 管理

```java
// 好的做法：使用环境变量
String apiKey = System.getenv("OPENAI_API_KEY");

// 不好的做法：硬编码
String apiKey = "sk-xxx";
```

### 2. 输入验证

```java
@PostMapping("/chat")
public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, String> request) {
    // 验证输入
    String message = request.get("message");
    if (message == null || message.trim().isEmpty()) {
        return ResponseEntity.badRequest()
            .body(Map.of("error", "Message is required"));
    }

    if (message.length() > 10000) {
        return ResponseEntity.badRequest()
            .body(Map.of("error", "Message too long"));
    }

    // 处理请求
    return ResponseEntity.ok(chatService.chat(message));
}
```

### 3. 敏感信息过滤

```java
// 不要在日志中输出敏感信息
logger.debug("API Key: {}", apiKey);  // 不好

// 应该过滤敏感信息
logger.debug("API Key: {}***", apiKey.substring(0, 5));  // 好
```

---

## 性能最佳实践

### 1. 缓存策略

```java
@Service
public class ChatService {
    @Cacheable(value = "chat_cache", key = "#message")
    public String chat(String message) {
        return chatModel.generate(message).content().text();
    }

    @CacheEvict(value = "chat_cache", allEntries = true)
    public void clearCache() {
    }
}
```

### 2. 异步处理

```java
@Service
public class ChatService {
    @Async
    public CompletableFuture<String> chatAsync(String message) {
        String result = chatModel.generate(message).content().text();
        return CompletableFuture.completedFuture(result);
    }
}
```

### 3. 连接池

```java
@Configuration
public class HttpClientConfig {
    @Bean
    public HttpClient httpClient() {
        return HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }
}
```

---

## 文档最佳实践

### 1. README 结构

```markdown
# 项目名称

## 概述
简要说明项目的目的和功能

## 快速开始
- 安装步骤
- 配置步骤
- 运行步骤

## 使用示例
提供代码示例

## API 文档
详细的 API 说明

## 常见问题
FAQ

## 贡献指南
如何贡献代码
```

### 2. 代码注释

```java
// 好的注释：解释为什么，而不是什么
// 使用指数退避重试，避免频繁的 API 调用
int delay = 1000;
for (int i = 0; i < maxRetries; i++) {
    try {
        return callLLM();
    } catch (RateLimitException e) {
        Thread.sleep(delay);
        delay *= 2;
    }
}

// 不好的注释：重复代码
// 循环 3 次
for (int i = 0; i < 3; i++) {
    // ...
}
```

---

## 版本管理最佳实践

### 1. 语义化版本

```
MAJOR.MINOR.PATCH
1.2.3

- MAJOR：不兼容的 API 变更
- MINOR：向后兼容的功能添加
- PATCH：向后兼容的 Bug 修复
```

### 2. 变更日志

```markdown
## [1.2.0] - 2026-03-22

### Added
- 新增 MCP 协议支持
- 新增流式响应功能

### Changed
- 优化 Token 计数算法

### Fixed
- 修复对话历史丢失问题
```

---

## 部署最佳实践

### 1. 环境配置

```properties
# application-dev.properties
llm.model=gpt-3.5-turbo
llm.temperature=0.7

# application-prod.properties
llm.model=gpt-4-turbo-preview
llm.temperature=0.3
```

### 2. 健康检查

```java
@RestController
@RequestMapping("/health")
public class HealthController {
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}
```

### 3. 监控和告警

```java
@Component
public class MetricsCollector {
    private final MeterRegistry meterRegistry;

    public void recordChatCall(long duration, int tokens) {
        meterRegistry.timer("chat.duration").record(duration, TimeUnit.MILLISECONDS);
        meterRegistry.counter("chat.tokens").increment(tokens);
    }
}
```

---

## 代码审查清单

- [ ] 代码是否遵循命名规范？
- [ ] 是否有适当的异常处理？
- [ ] 是否有单元测试？
- [ ] 是否有适当的日志？
- [ ] 是否有安全漏洞？
- [ ] 是否有性能问题？
- [ ] 是否有内存泄漏？
- [ ] 文档是否完整？
- [ ] 是否有硬编码的值？
- [ ] 是否有重复的代码？
