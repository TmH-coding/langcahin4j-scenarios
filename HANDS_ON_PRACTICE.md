# 实战练习和调试技巧工作簿

## 第一部分：基础 LLM 调用实战

### 练习 1.1：启用详细日志

**目标：** 观察 LLM 调用的完整过程

**步骤 1：修改日志配置**

编辑 `scenario-1-customer-service/src/main/resources/application.properties`：

```properties
# 启用 DEBUG 日志
logging.level.root=INFO
logging.level.dev.langchain4j=DEBUG
logging.level.com.langchain4j.scenarios=DEBUG
logging.level.org.springframework.web=DEBUG

# 输出格式（包含时间、线程、日志级别、类名、消息）
logging.pattern.console=%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
```

**步骤 2：启动应用并观察日志**

```bash
# 设置 API Key
export OPENAI_A​PI_KEY=sk-your-key-here

# 启动应用
mvn spring-boot:run -pl scenario-1-customer-service
```

**预期日志输出：**

```
10:05:23.456 [main] INFO  o.s.b.w.e.t.TomcatWebServer - Tomcat started on port(s): 8081
10:05:24.123 [http-nio-8081-exec-1] DEBUG c.l.s.s.c.CustomerServiceController - Received request: {message=你好}
10:05:24.234 [http-nio-8081-exec-1] DEBUG c.l.s.s.s.CustomerServiceAI - Building messages list...
10:05:24.345 [http-nio-8081-exec-1] DEBUG d.l.m.c.OpenAiChatModel - Calling OpenAI A​PI...
10:05:26.567 [http-nio-8081-exec-1] DEBUG d.l.m.c.OpenAiChatModel - Response received
10:05:26.678 [http-nio-8081-exec-1] DEBUG c.l.s.s.s.CustomerServiceAI - AI response: 你好！...
```

---

### 练习 1.2：观察 Token 使用

**目标：** 理解 Token 计数和成本计算

**步骤 1：修改 CustomerServiceAI.java 添加 Token 统计**

在 `chat()` 方法中添加以下代码：

```java
public String chat(String userMessage) {
    memory.addMessage("user", userMessage);
    String systemPrompt = PromptTemplateUtil.getTemplate("customer_service_system");
    List<ChatMessage> conversationHistory = PromptBuilder.convertConversationMessagesToChat(
        memory.getMessages());
    List<ChatMessage> messages = PromptBuilder.buildMessagesWithHistory(
        systemPrompt, conversationHistory, userMessage);

    // 调用 LLM
    Response<dev.langchain4j.data.message.AiMessage> response = chatModel.generate(messages);
    String assistantMessage = response.content().text();

    // ===== 新增：Token 统计 =====
    int inputTokens = response.tokenUsage().inputTokenCount();
    int outputTokens = response.tokenUsage().outputTokenCount();
    int totalTokens = inputTokens + outputTokens;

    // 计算成本（OpenAI 定价示例）
    double inputCost = inputTokens * 0.003 / 1000;      // $0.003 per 1K tokens
    double outputCost = outputTokens * 0.006 / 1000;    // $0.006 per 1K tokens
    double totalCost = inputCost + outputCost;

    System.out.println("=== Token 统计 ===");
    System.out.println("输入 Token: " + inputTokens);
    System.out.println("输出 Token: " + outputTokens);
    System.out.println("总计 Token: " + totalTokens);
    System.out.println("成本: $" + String.format("%.6f", totalCost));
    System.out.println("================");
    // ===== 新增结束 =====

    memory.addMessage("assistant", assistantMessage);
    return assistantMessage;
}
```

**步骤 2：测试并观察输出**

```bash
curl -X POST "http://localhost:8081/api/customer-service/chat" \
  -H "Content-Type: application/json" \
  -d '{"message":"你好，我想了解退货政策"}'
```

**预期输出：**

```
=== Token 统计 ===
输入 Token: 156
输出 Token: 89
总计 Token: 245
成本: $0.000714
================
```

**理解 Token 使用：**
- 系统提示词通常占 50-100 tokens
- 用户消息通常占 10-50 tokens
- AI 回复通常占 50-200 tokens
- 对话历史会显著增加输入 tokens

---

### 练习 1.3：测试多轮对话

**目标：** 验证对话历史是否正确维护

**步骤 1：创建测试脚本**

创建文件 `test_conversation.sh`：

```bash
#!/bin/bash

BASE_URL="http://localhost:8081/api/customer-service"

echo "=== 第一轮对话 ==="
curl -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d '{"message":"我想退货"}' | jq .

echo -e "\n=== 第二轮对话（应该记得第一轮）==="
curl -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d '{"message":"退货费用是多少？"}' | jq .

echo -e "\n=== 第三轮对话 ==="
curl -X POST "$BASE_URL/chat" \
  -H "Content-Type: application/json" \
  -d '{"message":"多久能收到退款？"}' | jq .

echo -e "\n=== 查看完整对话历史 ==="
curl -X GET "$BASE_URL/history" | jq .
```

**步骤 2：运行测试**

```bash
chmod +x test_conversation.sh
./test_conversation.sh
```

**验证点：**
- 第二轮回复应该提到"退货"（来自第一轮）
- 第三轮回复应该提到"退货"和"费用"（来自前两轮）
- 对话历史应该包含所有三轮对话

---

## 第二部分：调试技巧

### 技巧 2.1：打印完整的消息列表

**目标：** 理解发送给 LLM 的确切内容

在 `PromptBuilder.java` 中添加调试方法：

```java
public static void debugPrintMessages(List<ChatMessage> messages) {
    System.out.println("\n=== 消息列表调试 ===");
    for (int i = 0; i < messages.size(); i++) {
        ChatMessage msg = messages.get(i);
        String type = msg.getClass().getSimpleName();
        String content = msg.toString().substring(0, Math.min(100, msg.toString().length()));
        System.out.println("[" + i + "] " + type + ": " + content + "...");
    }
    System.out.println("总消息数: " + messages.size());
    System.out.println("====================\n");
}
```

在 `CustomerServiceAI.chat()` 中使用：

```java
List<ChatMessage> messages = PromptBuilder.buildMessagesWithHistory(
    systemPrompt, conversationHistory, userMessage);

// 调试输出
PromptBuilder.debugPrintMessages(messages);

Response<dev.langchain4j.data.message.AiMessage> response = chatModel.generate(messages);
```

**预期输出：**

```
=== 消息列表调试 ===
[0] SystemMessage: 你是一个专业的客服...
[1] UserMessage: 我想退货
[2] AiMessage: 好的，我可以帮您处理退货...
[3] UserMessage: 退货费用是多少？
总消息数: 4
====================
```

---

### 技巧 2.2：记录请求和响应

**目标：** 保存 LLM 调用的完整记录用于分析

创建 `LlmCallLogger.java`：

```java
package com.langchain4j.scenarios.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LlmCallLogger {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String LOG_DIR = "llm_calls";

    static {
        new File(LOG_DIR).mkdirs();
    }

    public static void logCall(String scenario, Object request, Object response) {
        try {
            String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
            String filename = LOG_DIR + "/" + scenario + "_" + timestamp + ".json";

            Map<String, Object> log = new HashMap<>();
            log.put("timestamp", LocalDateTime.now());
            log.put("scenario", scenario);
            log.put("request", request);
            log.put("response", response);

            mapper.writerWithDefaultPrettyPrinter()
                .writeValue(new File(filename), log);

            System.out.println("LLM 调用已记录: " + filename);
        } catch (Exception e) {
            System.err.println("记录 LLM 调用失败: " + e.getMessage());
        }
    }
}
```

在 `CustomerServiceAI.chat()` 中使用：

```java
Response<dev.langchain4j.data.message.AiMessage> response = chatModel.generate(messages);
String assistantMessage = response.content().text();

// 记录调用
LlmCallLogger.logCall("scenario-1",
    Map.of("messages", messages, "userMessage", userMessage),
    Map.of("response", assistantMessage, "tokens", response.tokenUsage())
);
```

---

### 技巧 2.3：性能监控

**目标：** 测量 LLM 调用的响应时间

```java
public String chat(String userMessage) {
    long startTime = System.currentTimeMillis();

    memory.addMessage("user", userMessage);
    String systemPrompt = PromptTemplateUtil.getTemplate("customer_service_system");
    List<ChatMessage> conversationHistory = PromptBuilder.convertConversationMessagesToChat(
        memory.getMessages());
    List<ChatMessage> messages = PromptBuilder.buildMessagesWithHistory(
        systemPrompt, conversationHistory, userMessage);

    long beforeLlmCall = System.currentTimeMillis();
    Response<dev.langchain4j.data.message.AiMessage> response = chatModel.generate(messages);
    long afterLlmCall = System.currentTimeMillis();

    String assistantMessage = response.content().text();
    memory.addMessage("assistant", assistantMessage);

    long endTime = System.currentTimeMillis();

    // 性能统计
    long totalTime = endTime - startTime;
    long llmTime = afterLlmCall - beforeLlmCall;
    long otherTime = totalTime - llmTime;

    System.out.println("=== 性能统计 ===");
    System.out.println("LLM 调用时间: " + llmTime + "ms");
    System.out.println("其他处理时间: " + otherTime + "ms");
    System.out.println("总耗时: " + totalTime + "ms");
    System.out.println("================");

    return assistantMessage;
}
```

---

## 第三部分：常见问题诊断

### 问题 3.1：API Key 错误

**症状：** `InvalidApiKeyException: Invalid A​PI key`

**诊断步骤：**

```bash
# 1. 检查环境变量是否设置
echo $OPENAI_A​PI_KEY

# 2. 验证 Key 格式（应该以 sk- 开头）
echo $OPENAI_A​PI_KEY | head -c 5

# 3. 检查 Key 是否包含空格
echo "[$OPENAI_A​PI_KEY]"

# 4. 在 application.properties 中添加调试
llm.api-key-set=${OPENAI_A​PI_KEY:not-set}
```

**解决方案：**

```bash
# 重新设置 API Key
export OPENAI_A​PI_KEY=sk-your-actual-key-here

# 验证设置
echo $OPENAI_A​PI_KEY

# 重启应用
mvn spring-boot:run -pl scenario-1-customer-service
```

---

### 问题 3.2：Token 限制错误

**症状：** `TokenLimitException: Input exceeds token limit`

**诊断步骤：**

```java
// 在 CustomerServiceAI.java 中添加
List<ChatMessage> messages = PromptBuilder.buildMessagesWithHistory(
    systemPrompt, conversationHistory, userMessage);

// 计算总 Token 数
int estimatedTokens = messages.stream()
    .mapToInt(msg -> msg.toString().length() / 4)  // 粗略估计
    .sum();

System.out.println("估计 Token 数: " + estimatedTokens);

if (estimatedTokens > 4000) {
    System.out.println("警告：Token 数接近限制！");
    // 清理对话历史
    memory.clear();
}
```

**解决方案：**

```java
// 限制对话历史长度
private final ConversationMemoryUtil memory = new ConversationMemoryUtil(10);  // 改为 10 条

// 或者定期清理
if (memory.getMessages().size() > 15) {
    memory.clear();
    System.out.println("对话历史已清理");
}
```

---

### 问题 3.3：响应缓慢

**症状：** 请求需要 10+ 秒才能返回

**诊断步骤：**

```java
// 添加详细的时间统计
long t1 = System.currentTimeMillis();
List<ChatMessage> messages = PromptBuilder.buildMessagesWithHistory(...);
long t2 = System.currentTimeMillis();

Response<AiMessage> response = chatModel.generate(messages);
long t3 = System.currentTimeMillis();

System.out.println("消息构建: " + (t2-t1) + "ms");
System.out.println("LLM 调用: " + (t3-t2) + "ms");

// 如果 LLM 调用 > 5000ms，可能是网络问题
// 如果消息构建 > 1000ms，可能是对话历史过长
```

**解决方案：**

```java
// 1. 使用流式响应（见下一部分）
// 2. 减少对话历史
// 3. 使用异步处理
@Async
public CompletableFuture<String> chatAsync(String userMessage) {
    String result = chat(userMessage);
    return CompletableFuture.completedFuture(result);
}
```

---

## 第四部分：流式响应调试

### 练习 4.1：启用 SSE 流式响应

**目标：** 实时看到 AI 生成的每个 token

**步骤 1：在 CustomerServiceController 中添加流式端点**

```java
@GetMapping("/chat/stream")
public SseEmitter streamChat(@RequestParam String message) {
    SseEmitter emitter = new SseEmitter(60000L);

    new Thread(() -> {
        try {
            StreamingChatLanguageModel streamingModel = /* 注入的流式模型 */;

            List<ChatMessage> messages = PromptBuilder.buildMessages(
                PromptTemplateUtil.getTemplate("customer_service_system"),
                message
            );

            streamingModel.generate(messages, new StreamingResponseHandler<AiMessage>() {
                @Override
                public void onNext(String token) {
                    try {
                        emitter.send(SseEmitter.event()
                            .id(UUID.randomUUID().toString())
                            .name("token")
                            .data(token)
                            .reconnectTime(1000));
                    } catch (IOException e) {
                        onError(e);
                    }
                }

                @Override
                public void onComplete(Response<AiMessage> response) {
                    try {
                        emitter.send(SseEmitter.event()
                            .name("done")
                            .data("完成"));
                        emitter.complete();
                    } catch (IOException e) {
                        onError(e);
                    }
                }

                @Override
                public void onError(Throwable error) {
                    try {
                        emitter.send(SseEmitter.event()
                            .name("error")
                            .data(error.getMessage()));
                        emitter.completeWithError(error);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            try {
                emitter.completeWithError(e);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }).start();

    return emitter;
}
```

**步骤 2：测试流式响应**

```bash
# 使用 curl 测试 SSE
curl -N "http://localhost:8081/api/customer-service/chat/stream?message=你好"

# 预期输出：逐个 token 返回
# data: 你
# data: 好
# data: ！
# ...
# event: done
# data: 完成
```

---

## 调试检查清单

- [ ] API Key 是否正确设置？
- [ ] 日志级别是否设置为 DEBUG？
- [ ] Token 数量是否超过限制？
- [ ] 对话历史是否过长？
- [ ] 网络连接是否正常？
- [ ] 是否有足够的内存？
- [ ] 响应时间是否在可接受范围内？
- [ ] 是否正确处理了异常？

---

## 下一步

完成这些练习后，你将理解：
1. ✅ LLM 调用的完整过程
2. ✅ Token 计数和成本计算
3. ✅ 多轮对话的实现原理
4. ✅ 常见问题的诊断方法
5. ✅ 流式响应的工作原理

准备好进入**第二阶段：RAG 和向量检索**吗？
