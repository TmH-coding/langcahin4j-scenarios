# 性能优化手册

## 性能优化概览

性能优化的三个关键指标：
- **响应时间**：用户等待的时间
- **吞吐量**：单位时间内处理的请求数
- **资源使用**：CPU、内存、网络带宽

---

## 1. Token 优化

### 问题：Token 消耗过多

**症状：**
- 成本增加
- 响应变慢
- 频繁超过 Token 限制

### 优化策略

#### 1.1 压缩输入

```java
// 移除多余空格
String compressed = text.replaceAll("\\s+", " ");

// 移除注释
String noComments = text.replaceAll("//.*", "");

// 移除重复内容
String deduplicated = deduplicateText(text);
```

#### 1.2 限制历史消息

```java
// 只保存最近 N 条消息
int maxMessages = 20;
if (messages.size() > maxMessages) {
    messages = messages.subList(
        messages.size() - maxMessages,
        messages.size()
    );
}
```

#### 1.3 使用摘要

```java
// 定期生成对话摘要
if (messages.size() > 50) {
    String summary = generateSummary(messages);
    messages.clear();
    messages.add(SystemMessage.from(systemPrompt));
    messages.add(UserMessage.from("摘要: " + summary));
}
```

#### 1.4 选择合适的模型

```java
// 简单任务使用 GPT-3.5
ChatLanguageModel model = OpenAiChatModel.builder()
    .modelName("gpt-3.5-turbo")  // 便宜
    .build();

// 复杂任务使用 GPT-4
ChatLanguageModel model = OpenAiChatModel.builder()
    .modelName("gpt-4-turbo-preview")  // 贵但强大
    .build();
```

#### 1.5 监控 Token 使用

```java
Response<AiMessage> response = chatModel.generate(messages);
int inputTokens = response.tokenUsage().inputTokenCount();
int outputTokens = response.tokenUsage().outputTokenCount();
int totalTokens = inputTokens + outputTokens;

logger.info("Token usage - Input: {}, Output: {}, Total: {}",
    inputTokens, outputTokens, totalTokens);

// 计算成本
double cost = totalTokens * PRICE_PER_TOKEN;
if (cost > BUDGET_THRESHOLD) {
    logger.warn("Cost exceeds threshold: ${}", cost);
}
```

---

## 2. 缓存优化

### 问题：重复调用相同请求

**症状：**
- 相同问题重复调用 LLM
- 浪费 Token 和时间

### 缓存策略

#### 2.1 本地缓存

```java
@Service
public class ChatService {
    private final Cache<String, String> cache = CacheBuilder.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(1, TimeUnit.HOURS)
        .build();

    public String chat(String message) {
        String cached = cache.getIfPresent(message);
        if (cached != null) {
            logger.info("Cache hit for message: {}", message);
            return cached;
        }

        String result = chatModel.generate(message).content().text();
        cache.put(message, result);
        return result;
    }
}
```

#### 2.2 Spring 缓存

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

#### 2.3 分布式缓存（Redis）

```java
@Service
public class ChatService {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public String chat(String message) {
        String cached = redisTemplate.opsForValue().get(message);
        if (cached != null) {
            return cached;
        }

        String result = chatModel.generate(message).content().text();
        redisTemplate.opsForValue().set(message, result, 1, TimeUnit.HOURS);
        return result;
    }
}
```

#### 2.4 缓存键设计

```java
// 好的做法：包含所有影响结果的因素
String cacheKey = String.format("chat:%s:%s:%s",
    userId,
    message,
    temperature
);

// 不好的做法：只用消息
String cacheKey = message;
```

---

## 3. 并发优化

### 问题：单线程处理，吞吐量低

**症状：**
- 无法处理多个并发请求
- 响应时间长

### 并发策略

#### 3.1 异步处理

```java
@Service
public class ChatService {
    @Async
    public CompletableFuture<String> chatAsync(String message) {
        String result = chatModel.generate(message).content().text();
        return CompletableFuture.completedFuture(result);
    }
}

// 使用
CompletableFuture<String> future = chatService.chatAsync(message);
String result = future.get(30, TimeUnit.SECONDS);
```

#### 3.2 线程池

```java
@Configuration
public class ThreadPoolConfig {
    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("chat-");
        executor.initialize();
        return executor;
    }
}
```

#### 3.3 批量处理

```java
// 并行处理多个消息
List<String> messages = Arrays.asList("消息1", "消息2", "消息3");
List<String> results = messages.parallelStream()
    .map(this::chat)
    .collect(Collectors.toList());
```

---

## 4. 数据库优化

### 问题：数据库查询慢

**症状：**
- 对话历史查询慢
- 向量检索慢

### 优化策略

#### 4.1 索引优化

```java
// 为常用查询字段添加索引
@Entity
public class Conversation {
    @Id
    private Long id;

    @Column(name = "user_id")
    @Index(name = "idx_user_id")
    private String userId;

    @Column(name = "created_at")
    @Index(name = "idx_created_at")
    private LocalDateTime createdAt;
}
```

#### 4.2 查询优化

```java
// 不好的做法：查询所有字段
List<Conversation> all = conversationRepository.findAll();

// 好的做法：只查询需要的字段
@Query("SELECT new com.example.ConversationDTO(c.id, c.message) FROM Conversation c WHERE c.userId = :userId")
List<ConversationDTO> findByUserId(@Param("userId") String userId);
```

#### 4.3 分页查询

```java
// 不好的做法：一次查询所有
List<Conversation> all = conversationRepository.findAll();

// 好的做法：分页查询
Page<Conversation> page = conversationRepository.findAll(
    PageRequest.of(0, 20, Sort.by("createdAt").descending())
);
```

#### 4.4 向量数据库

```java
// 使用专业向量数据库替代内存存储
EmbeddingStore<TextSegment> store = new MilvusEmbeddingStore.builder()
    .host("localhost")
    .port(19530)
    .collectionName("documents")
    .build();
```

---

## 5. 网络优化

### 问题：API 调用慢

**症状：**
- 网络延迟高
- 连接超时

### 优化策略

#### 5.1 连接复用

```java
@Configuration
public class HttpClientConfig {
    @Bean
    public HttpClient httpClient() {
        return HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .version(HttpClient.Version.HTTP_2)  // 使用 HTTP/2
            .build();
    }
}
```

#### 5.2 请求超时

```java
ChatLanguageModel model = OpenAiChatModel.builder()
    .apiKey(apiKey)
    .timeout(Duration.ofSeconds(30))  // 设置超时
    .build();
```

#### 5.3 流式响应

```java
// 不好的做法：等待完整响应
String response = chatModel.generate(message).content().text();

// 好的做法：流式返回
streamingChatModel.generate(messages, new StreamingResponseHandler<AiMessage>() {
    @Override
    public void onNext(String token) {
        // 立即返回 token
    }
});
```

---

## 6. 内存优化

### 问题：内存占用过高

**症状：**
- 内存使用不断增加
- OutOfMemoryError

### 优化策略

#### 6.1 及时释放资源

```java
// 不好的做法：保存所有对话
List<String> allMessages = new ArrayList<>();
for (int i = 0; i < 1000000; i++) {
    allMessages.add(generateMessage());
}

// 好的做法：流式处理
try (Stream<String> messages = generateMessages()) {
    messages.forEach(this::processMessage);
}
```

#### 6.2 定期清理

```java
@Scheduled(fixedDelay = 3600000)  // 每小时
public void cleanupOldConversations() {
    LocalDateTime cutoff = LocalDateTime.now().minusHours(24);
    conversationRepository.deleteOlderThan(cutoff);
}
```

#### 6.3 监控内存

```java
Runtime runtime = Runtime.getRuntime();
long usedMemory = runtime.totalMemory() - runtime.freeMemory();
long maxMemory = runtime.maxMemory();
double percentage = (double) usedMemory / maxMemory * 100;

logger.info("Memory usage: {}/{} MB ({}%)",
    usedMemory / 1024 / 1024,
    maxMemory / 1024 / 1024,
    percentage);

if (percentage > 80) {
    logger.warn("Memory usage high, triggering cleanup");
    System.gc();
}
```

---

## 7. 成本优化

### 问题：API 成本过高

**症状：**
- 月度账单增加
- 超过预算

### 优化策略

#### 7.1 模型选择

```java
// 根据任务复杂度选择模型
if (isSimpleTask(task)) {
    return useGpt35Turbo();  // 便宜
} else {
    return useGpt4();  // 贵但强大
}
```

#### 7.2 批量处理

```java
// 不好的做法：逐个处理
for (String message : messages) {
    chatModel.generate(message);
}

// 好的做法：批量处理
List<String> results = messages.parallelStream()
    .map(this::chat)
    .collect(Collectors.toList());
```

#### 7.3 成本监控

```java
@Component
public class CostMonitor {
    private double totalCost = 0;

    public void recordCost(int tokens, double pricePerToken) {
        double cost = tokens * pricePerToken;
        totalCost += cost;

        if (totalCost > MONTHLY_BUDGET) {
            logger.error("Monthly budget exceeded: ${}", totalCost);
            // 采取措施
        }
    }
}
```

---

## 8. 性能测试

### 基准测试

```java
@SpringBootTest
public class PerformanceTest {
    @Autowired
    private ChatService chatService;

    @Test
    public void benchmarkChat() {
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 100; i++) {
            chatService.chat("测试消息");
        }

        long duration = System.currentTimeMillis() - startTime;
        double avgTime = (double) duration / 100;

        System.out.println("Average response time: " + avgTime + "ms");
        assertTrue(avgTime < 1000, "Response time too slow");
    }
}
```

### 压力测试

```java
@Test
public void stressTest() throws InterruptedException {
    ExecutorService executor = Executors.newFixedThreadPool(10);
    CountDownLatch latch = new CountDownLatch(1000);

    for (int i = 0; i < 1000; i++) {
        executor.submit(() -> {
            try {
                chatService.chat("测试消息");
            } finally {
                latch.countDown();
            }
        });
    }

    latch.await(5, TimeUnit.MINUTES);
    executor.shutdown();
}
```

---

## 9. 监控和告警

### 关键指标

```java
@Component
public class MetricsCollector {
    private final MeterRegistry meterRegistry;

    public void recordChatCall(long duration, int tokens) {
        // 记录响应时间
        meterRegistry.timer("chat.duration")
            .record(duration, TimeUnit.MILLISECONDS);

        // 记录 Token 使用
        meterRegistry.counter("chat.tokens")
            .increment(tokens);

        // 记录成本
        double cost = tokens * PRICE_PER_TOKEN;
        meterRegistry.gauge("chat.cost", cost);
    }
}
```

### 告警规则

```yaml
# Prometheus 告警规则
groups:
  - name: chat_alerts
    rules:
      - alert: HighResponseTime
        expr: chat_duration_seconds > 5
        for: 5m
        annotations:
          summary: "Chat response time is high"

      - alert: HighTokenUsage
        expr: rate(chat_tokens[5m]) > 10000
        for: 5m
        annotations:
          summary: "Token usage is high"
```

---

## 10. 优化检查清单

- [ ] 是否优化了 Token 使用？
- [ ] 是否实现了缓存？
- [ ] 是否使用了异步处理？
- [ ] 是否添加了数据库索引？
- [ ] 是否优化了网络连接？
- [ ] 是否监控了内存使用？
- [ ] 是否控制了成本？
- [ ] 是否进行了性能测试？
- [ ] 是否设置了监控告警？
- [ ] 是否定期审查性能指标？
