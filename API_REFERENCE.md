# LangChain4j A​PI 速查手册

## 核心类和接口

### ChatLanguageModel - LLM 调用

```java
// 创建实例
ChatLanguageModel model = OpenAiChatModel.builder()
    .apiKey(System.getenv("OPENAI_API_KEY"))
    .modelName("gpt-4-turbo-preview")
    .temperature(0.7)
    .maxTokens(2048)
    .timeout(Duration.ofSeconds(60))
    .build();

// 调用 LLM
Response<AiMessage> response = model.generate(messages);
AiMessage aiMessage = response.content();
String text = aiMessage.text();

// 获取 Token 使用情况
TokenUsage tokenUsage = response.tokenUsage();
int inputTokens = tokenUsage.inputTokenCount();
int outputTokens = tokenUsage.outputTokenCount();
```

### StreamingChatLanguageModel - 流式调用

```java
// 创建实例
StreamingChatLanguageModel model = OpenAiStreamingChatModel.builder()
    .apiKey(System.getenv("OPENAI_API_KEY"))
    .modelName("gpt-4-turbo-preview")
    .build();

// 流式调用
model.generate(messages, new StreamingResponseHandler<AiMessage>() {
    @Override
    public void onNext(String token) {
        System.out.print(token);
    }

    @Override
    public void onComplete(Response<AiMessage> response) {
        System.out.println("\n[完成]");
    }

    @Override
    public void onError(Throwable error) {
        error.printStackTrace();
    }
});
```

### EmbeddingModel - 向量化

```java
// 创建实例
EmbeddingModel model = new AllMiniLmL6V2EmbeddingModel();

// 向量化文本
Response<Embedding> response = model.embed("这是一段文本");
Embedding embedding = response.content();
float[] vector = embedding.vector();

// 向量化文本段
TextSegment segment = TextSegment.from("这是一段文本");
Response<Embedding> response = model.embed(segment);
```

### EmbeddingStore - 向量存储

```java
// 创建内存存储
EmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();

// 添加向量
Embedding embedding = embeddingModel.embed(segment).content();
store.add(embedding, segment);

// 检索相似向量
List<EmbeddingMatch<TextSegment>> matches = store.findRelevant(
    queryEmbedding,
    3  // 返回前 3 个最相似的
);

// 遍历结果
for (EmbeddingMatch<TextSegment> match : matches) {
    double score = match.score();  // 相似度分数
    TextSegment segment = match.embedded();
}
```

---

## 消息类

### ChatMessage 及其子类

```java
// 系统消息
ChatMessage systemMessage = SystemMessage.from("你是一个专业的...");

// 用户消息
ChatMessage userMessage = UserMessage.from("用户问题");

// AI 消息
ChatMessage aiMessage = AiMessage.from("AI 回复");

// 工具消息
ChatMessage toolMessage = ToolExecutionResultMessage.from(
    "tool_call_id",
    "工具执行结果"
);

// 构建消息列表
List<ChatMessage> messages = new ArrayList<>();
messages.add(systemMessage);
messages.add(userMessage);
messages.add(aiMessage);
```

### AiMessage - AI 消息

```java
AiMessage aiMessage = response.content();

// 获取文本
String text = aiMessage.text();

// 检查是否有工具调用
if (aiMessage.hasToolExecutionRequests()) {
    List<ToolExecutionRequest> requests = aiMessage.toolExecutionRequests();
    for (ToolExecutionRequest request : requests) {
        String toolName = request.name();
        String arguments = request.arguments();
    }
}
```

---

## 工具相关类

### Tool 注解

```java
// 定义工具
@Tool("获取当前时间")
public String getCurrentTime() {
    return LocalDateTime.now().toString();
}

// 带参数的工具
@Tool("执行数学运算")
public double calculate(
    @P("运算符") String operation,
    @P("第一个数字") double num1,
    @P("第二个数字") double num2
) {
    switch (operation) {
        case "+": return num1 + num2;
        case "-": return num1 - num2;
        case "*": return num1 * num2;
        case "/": return num1 / num2;
        default: throw new IllegalArgumentException("Unknown operation");
    }
}
```

### ToolExecutionRequest

```java
// 工具执行请求
ToolExecutionRequest request = aiMessage.toolExecutionRequests().get(0);

String toolName = request.name();
String arguments = request.arguments();
String id = request.id();

// 解析参数
Map<String, Object> args = parseJson(arguments);
```

---

## 响应类

### Response<T>

```java
Response<AiMessage> response = chatModel.generate(messages);

// 获取内容
AiMessage content = response.content();

// 获取 Token 使用情况
TokenUsage tokenUsage = response.tokenUsage();

// 获取完整响应
String finishReason = response.finishReason();
```

### TokenUsage

```java
TokenUsage usage = response.tokenUsage();

int inputTokens = usage.inputTokenCount();
int outputTokens = usage.outputTokenCount();
int totalTokens = usage.totalTokenCount();

// 计算成本
double cost = totalTokens * PRICE_PER_TOKEN;
```

---

## 文档处理

### Document

```java
// 创建文档
Document document = Document.from("文档内容");

// 添加元数据
document.metadata().put("source", "file.txt");
document.metadata().put("author", "张三");

// 获取内容
String content = document.text();
```

### TextSegment

```java
// 创建文本段
TextSegment segment = TextSegment.from("这是一段文本");

// 添加元数据
segment.metadata().put("page", "1");

// 获取内容
String text = segment.text();
```

### DocumentParser

```java
// 解析 PDF
DocumentParser parser = new ApachePdfBoxDocumentParser();
List<Document> documents = parser.parse(new File("document.pdf"));

// 分块处理
for (Document doc : documents) {
    List<TextSegment> segments = splitDocument(doc, 512);
}
```

---

## 常用工具类

### TokenCountUtil

```java
// 计算 Token 数
int tokenCount = TokenCountUtil.countTokens(text);

// 检查是否超过限制
boolean exceeds = TokenCountUtil.exceedsLimit(text, 2000);

// 截断到指定 Token 数
String truncated = TokenCountUtil.truncateToTokenLimit(text, 2000);
```

### PromptBuilder

```java
// 构建消息列表
List<ChatMessage> messages = PromptBuilder.buildMessages(
    systemPrompt,
    userMessage
);

// 带历史的消息
List<ChatMessage> withHistory = PromptBuilder.buildMessagesWithHistory(
    systemPrompt,
    conversationHistory,
    userMessage
);

// RAG 消息
List<ChatMessage> ragMessages = PromptBuilder.buildRagUserMessage(
    contextSegments,
    question
);
```

### ConversationMemoryUtil

```java
// 添加消息
ConversationMemoryUtil.addMessage("user", "用户消息");
ConversationMemoryUtil.addMessage("assistant", "AI 回复");

// 获取消息
List<ConversationMessage> messages = ConversationMemoryUtil.getMessages();

// 清空历史
ConversationMemoryUtil.clear();
```

---

## 配置类

### LlmProperties

```java
@ConfigurationProperties(prefix = "llm")
@Component
@Data
public class LlmProperties {
    private String model = "gpt-4-turbo-preview";
    private Double temperature = 0.7;
    private Integer maxTokens = 2048;
    private Integer timeout = 60;
}
```

### LlmConfig

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

    @Bean
    public EmbeddingModel embeddingModel() {
        return new AllMiniLmL6V2EmbeddingModel();
    }

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return new InMemoryEmbeddingStore<>();
    }
}
```

---

## 异常类

### LangChain4j 异常

```java
// 速率限制异常
try {
    chatModel.generate(messages);
} catch (RateLimitException e) {
    // 处理速率限制
}

// Token 限制异常
try {
    chatModel.generate(messages);
} catch (TokenLimitException e) {
    // 处理 Token 限制
}

// API 异常
try {
    chatModel.generate(messages);
} catch (RuntimeException e) {
    // 处理其他异常
}
```

---

## 常用配置

### application.properties

```properties
# LLM 配置
llm.provider=openai
llm.model=gpt-4-turbo-preview
llm.temperature=0.7
llm.max-tokens=2048
llm.timeout=60

# 日志配置
logging.level.root=INFO
logging.level.dev.langchain4j=DEBUG
logging.level.com.langchain4j.scenarios=DEBUG

# Spring 配置
spring.application.name=scenario-1-customer-service
server.port=8081
```

---

## 常用代码片段

### 基础调用

```java
List<ChatMessage> messages = new ArrayList<>();
messages.add(SystemMessage.from("你是一个专业的..."));
messages.add(UserMessage.from("用户问题"));

Response<AiMessage> response = chatModel.generate(messages);
String answer = response.content().text();
```

### 多轮对话

```java
List<ChatMessage> messages = new ArrayList<>();
messages.add(SystemMessage.from(systemPrompt));

for (String userInput : userInputs) {
    messages.add(UserMessage.from(userInput));
    Response<AiMessage> response = chatModel.generate(messages);
    String aiReply = response.content().text();
    messages.add(AiMessage.from(aiReply));
}
```

### RAG 检索

```java
// 向量化查询
Embedding queryEmbedding = embeddingModel.embed(query).content();

// 检索相似文档
List<EmbeddingMatch<TextSegment>> matches = embeddingStore.findRelevant(
    queryEmbedding,
    3
);

// 构建 RAG 提示词
String context = matches.stream()
    .map(m -> m.embedded().text())
    .collect(Collectors.joining("\n"));

String ragPrompt = "基于以下信息回答问题:\n" + context + "\n问题: " + query;
```

### 工具调用

```java
if (aiMessage.hasToolExecutionRequests()) {
    for (ToolExecutionRequest toolRequest : aiMessage.toolExecutionRequests()) {
        Object result = executeToolRequest(toolRequest);
        messages.add(ToolExecutionResultMessage.from(
            toolRequest.id(),
            result.toString()
        ));
    }
}
```

---

## 版本信息

- **LangChain4j 版本**：0.31.0
- **Spring Boot 版本**：3.2.0
- **Java 版本**：17+
- **OpenAI A​PI**：最新版本

---

## 相关资源

- [LangChain4j 官方文档](https://docs.langchain4j.dev/)
- [OpenAI A​PI 文档](https://platform.openai.com/docs)
- [Spring Boot 文档](https://spring.io/projects/spring-boot)
