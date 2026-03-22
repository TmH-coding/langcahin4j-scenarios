# Agent 智能体学习指南

## 目录
1. [核心概念](#核心概念)
2. [Agent 工作原理](#agent-工作原理)
3. [工具定义](#工具定义)
4. [Agent 循环](#agent-循环)
5. [实战示例](#实战示例)
6. [进阶概念](#进阶概念)
7. [常见问题](#常见问题)

---

## 核心概念

### 什么是 Agent？

Agent（智能体）是一个能够自主思考和决策的系统。与普通 LLM 不同：

**普通 LLM 的工作流程：**
```
用户输入 → LLM 生成答案 → 返回答案
```
- 问题：LLM 的知识可能过时，无法执行实时操作
- 例子：问"现在几点了？"，LLM 无法知道实时时间

**Agent 的工作流程：**
```
用户输入 → LLM 分析 → 调用工具 → 获取实时数据 → LLM 继续推理 → 返回准确答案
```
- 优势：能够执行实时操作，获取最新数据，完成复杂任务
- 例子：问"现在几点了？"，Agent 调用时间工具获取实时时间

### Agent 的三个核心能力

1. **Reasoning（推理）**
   - LLM 分析用户的请求
   - 决定需要调用哪些工具
   - 理解工具返回的结果

2. **Acting（行动）**
   - 执行工具调用
   - 获取实时数据或执行操作
   - 返回结果给 LLM

3. **Observation（观察）**
   - 获取工具返回的结果
   - 将结果反馈给 LLM
   - 支持 LLM 的下一步推理

---

## Agent 工作原理

### ReAct 框架

ReAct（Reasoning + Acting）是 Agent 的标准工作框架：

```
┌─────────────────────────────────────────────────────────────┐
│ 用户请求：                                                   │
│ "帮我查询用户 user123 的信息并发送邮件"                      │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ 第1步：LLM 思考（Reasoning）                                 │
│ "我需要：                                                    │
│  1. 调用 queryUserInfo 获取用户信息                         │
│  2. 调用 sendEmail 发送邮件"                                │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ 第2步：执行工具（Acting）                                    │
│ 调用 queryUserInfo("user123")                               │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ 第3步：观察结果（Observation）                               │
│ 返回：用户ID: user123, 姓名: 张三, 邮箱: zhangsan@...      │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ 第4步：LLM 继续推理                                          │
│ "我已获得用户信息，现在发送邮件"                             │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ 第5步：执行工具                                              │
│ 调用 sendEmail(recipient, subject, content)                │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ 第6步：观察结果                                              │
│ 返回：邮件已成功发送                                         │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ 第7步：LLM 总结                                              │
│ "已完成！用户信息已查询，邮件已发送"                         │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ 返回最终答案给用户                                           │
└─────────────────────────────────────────────────────────────┘
```

### Agent 循环的伪代码

```python
def agent_loop(user_message):
    messages = [system_prompt, user_message]

    for iteration in range(max_iterations):
        # 1. 调用 LLM
        response = llm.generate(messages)

        # 2. 检查是否需要调用工具
        if response.has_tool_calls():
            # 3. 执行工具
            for tool_call in response.tool_calls:
                tool_result = execute_tool(tool_call)
                # 4. 将结果反馈给 LLM
                messages.append(tool_result)
        else:
            # LLM 给出了最终答案
            return response.text()

    return "无法完成任务"
```

---

## 工具定义

### 什么是 Tool？

Tool 是 Agent 可以调用的函数。每个 Tool 都有：
- **名称**：工具的标识符
- **描述**：工具的功能说明
- **参数**：工具需要的输入
- **返回值**：工具的输出

### Tool 的设计原则

1. **单一职责**
   - 每个 Tool 只做一件事
   - ✅ 好：`queryUserInfo(userId)` - 只查询用户信息
   - ❌ 差：`queryUserAndSendEmail(userId, email)` - 做了两件事

2. **清晰的参数**
   - 参数名要能表达含义
   - ✅ 好：`calculate(operation, num1, num2)`
   - ❌ 差：`calc(op, a, b)`

3. **有意义的返回值**
   - 返回结果要能帮助 LLM 做决策
   - ✅ 好：返回详细的用户信息
   - ❌ 差：返回 "success" 或 "failed"

4. **完整的文档**
   - JavaDoc 要清楚描述工具的功能
   - 包括参数说明和返回值说明

### 本项目中的 6 个工具

| 工具名 | 功能 | 参数 | 返回值 |
|--------|------|------|--------|
| `getCurrentTime` | 获取当前时间 | 无 | 时间字符串 |
| `calculate` | 数学运算 | operation, num1, num2 | 计算结果 |
| `queryUserInfo` | 查询用户信息 | userId | 用户详细信息 |
| `queryWeather` | 查询天气 | city | 天气信息 |
| `sendEmail` | 发送邮件 | recipient, subject, content | 发送结果 |
| `analyzeData` | 分析数据 | dataType | 分析结果 |

---

## Agent 循环

### 循环的关键步骤

#### 步骤 1：初始化
```java
List<ChatMessage> messages = new ArrayList<>();
messages.add(SystemMessage.from(AGENT_SYSTEM_PROMPT));
messages.add(UserMessage.from(userMessage));
```

#### 步骤 2：调用 LLM
```java
Response<AiMessage> response = chatModel.generate(messages);
AiMessage aiMessage = response.content();
```

#### 步骤 3：检查工具调用
```java
if (aiMessage.hasToolExecutionRequests()) {
    // 需要调用工具
} else {
    // LLM 给出了最终答案
    return aiMessage.text();
}
```

#### 步骤 4：执行工具
```java
List<ToolExecutionRequest> toolRequests = aiMessage.toolExecutionRequests();
for (ToolExecutionRequest toolRequest : toolRequests) {
    String toolResult = executeToolRequest(toolRequest);
    messages.add(UserMessage.from("工具执行结果: " + toolResult));
}
```

#### 步骤 5：循环
回到步骤 2，继续调用 LLM

### 防止无限循环

```java
int maxIterations = 10;
int iteration = 0;

while (iteration < maxIterations) {
    iteration++;
    // ... Agent 循环逻辑
}
```

---

## 实战示例

### 示例 1：简单查询

**用户请求：** "现在几点了？"

**Agent 执行过程：**
1. LLM 识别需要时间信息
2. LLM 调用 `getCurrentTime()` 工具
3. 工具返回 "2026-03-22 09:22:44"
4. LLM 返回 "现在是 2026-03-22 09:22:44"

**HTTP 请求：**
```bash
curl -X POST http://localhost:8086/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "现在几点了？"}'
```

**响应：**
```json
{
  "message": "现在是 2026-03-22 09:22:44",
  "status": "success"
}
```

### 示例 2：多步骤任务

**用户请求：** "查询用户 user123 的信息，然后给他发邮件说'您的账户已激活'"

**Agent 执行过程：**
1. LLM 识别需要两个步骤
2. 第一步：调用 `queryUserInfo("user123")`
   - 返回：用户ID: user123, 姓名: 张三, 邮箱: zhangsan@example.com, ...
3. LLM 继续推理，提取邮箱地址
4. 第二步：调用 `sendEmail("zhangsan@example.com", "账户激活", "您的账户已激活")`
   - 返回：邮件已成功发送
5. LLM 总结：已完成查询和发送邮件

### 示例 3：条件判断

**用户请求：** "如果北京天气晴天，就告诉我可以去公园；否则建议我在家看书"

**Agent 执行过程：**
1. LLM 识别需要查询天气
2. 调用 `queryWeather("北京")`
   - 返回：晴天，温度 15°C，风力 3 级
3. LLM 根据结果判断
4. 返回：天气晴天，建议您去公园散步

---

## 进阶概念

### 1. 工具链（Tool Chain）

多个工具的组合使用：

```
用户请求 → 工具1 → 工具2 → 工具3 → 最终答案
```

例子：
- 查询用户信息 → 查询用户的订单 → 分析订单数据 → 返回分析结果

### 2. 条件分支

Agent 根据条件选择不同的工具：

```
用户请求 → LLM 判断条件 → 选择工具A或工具B → 执行 → 返回结果
```

### 3. 错误恢复

当工具执行失败时，Agent 可以尝试其他方式：

```
调用工具 → 失败 → LLM 分析错误 → 尝试其他方式 → 成功
```

### 4. 上下文管理

Agent 需要记住之前的对话内容：

```java
List<ChatMessage> messages = new ArrayList<>();
// 保存所有历史消息
messages.add(SystemMessage.from(systemPrompt));
messages.add(UserMessage.from(userMessage1));
messages.add(AiMessage.from(aiResponse1));
messages.add(UserMessage.from(userMessage2));
// ... 继续对话
```

### 5. Token 优化

管理 Token 消耗，避免超过模型限制：

```java
// 只保存最近的 N 条消息
if (messages.size() > MAX_MESSAGES) {
    messages = messages.subList(messages.size() - MAX_MESSAGES, messages.size());
}
```

---

## 常见问题

### Q1: Agent 和 Function Calling 有什么区别？

**Function Calling：**
- LLM 决定调用哪个函数
- 通常只调用一次
- 用于单步操作

**Agent：**
- LLM 可以多次调用不同的工具
- 支持多步推理和决策
- 用于复杂任务

### Q2: 如何防止 Agent 陷入无限循环？

1. 设置最大迭代次数
2. 设置超时时间
3. 监控 Token 消耗
4. 添加日志记录

### Q3: 如何提高 Agent 的准确性？

1. 编写清晰的系统提示词
2. 设计好的工具接口
3. 提供详细的工具文档
4. 使用更强大的 LLM 模型
5. 添加工具调用的验证

### Q4: Agent 可以调用多少个工具？

理论上没有限制，但实际上：
- 工具太多会增加 LLM 的决策难度
- 建议 5-10 个工具为最佳
- 可以将相关工具分组

### Q5: 如何测试 Agent？

1. 编写单元测试测试每个工具
2. 编写集成测试测试 Agent 循环
3. 手动测试各种场景
4. 监控 Agent 的性能指标

---

## 总结

Agent 是 AI 应用的未来方向，它能够：
- ✅ 执行实时操作
- ✅ 获取最新数据
- ✅ 完成复杂任务
- ✅ 自主决策和推理

通过本学习案例，你已经理解了：
- Agent 的核心概念
- Agent 的工作原理
- 如何定义工具
- 如何实现 Agent 循环
- 如何处理多步骤任务

下一步可以尝试：
- 添加更多工具
- 实现更复杂的任务
- 优化 Agent 的性能
- 集成到实际应用中
