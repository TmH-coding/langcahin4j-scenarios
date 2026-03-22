# Scenario 6: Intelligent Agent - Agent 智能体学习案例

## 项目概述

这是一个完整的 Agent（智能体）学习案例，展示了如何使用 LangChain4j 构建能够自主思考和决策的 AI 系统。

**核心特性：**
- ✅ 6 个预定义工具（时间、计算、数据库查询、天气、邮件、数据分析）
- ✅ 完整的 Agent 循环实现（ReAct 框架）
- ✅ 多步骤任务支持
- ✅ 详细的中文学习指南
- ✅ REST API 接口

---

## 快速开始

### 1. 启动应用

```bash
# 设置 OpenAI API Key
export OPENAI_API_KEY=sk-your-key-here

# 启动 scenario-6
mvn spring-boot:run -pl scenario-6-intelligent-agent
```

应用将在 `http://localhost:8086` 启动

### 2. 查看可用工具

```bash
curl http://localhost:8086/api/agent/tools
```

响应：
```json
{
  "tools": [
    "getCurrentTime - 获取当前时间",
    "calculate - 执行数学运算（+, -, *, /）",
    "queryUserInfo - 查询用户信息",
    "queryWeather - 查询天气信息",
    "sendEmail - 发送邮件",
    "analyzeData - 分析数据（sales, users, revenue）"
  ],
  "count": 6
}
```

### 3. 与 Agent 对话

```bash
curl -X POST http://localhost:8086/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "现在几点了？"}'
```

响应：
```json
{
  "message": "现在是 2026-03-22 09:22:44",
  "status": "success"
}
```

### 4. 查看使用示例

```bash
curl http://localhost:8086/api/agent/examples
```

---

## 核心概念

### Agent 是什么？

Agent 是一个能够自主思考和决策的系统：

```
普通 LLM：用户输入 → LLM 生成答案 → 返回答案

Agent：用户输入 → LLM 分析 → 调用工具 → 获取数据 → LLM 推理 → 返回答案
```

### Agent 的工作原理（ReAct 框架）

```
1. Reasoning（推理）：LLM 分析问题，决定调用哪些工具
2. Acting（行动）：执行工具调用
3. Observation（观察）：获取工具返回的结果
4. 循环：根据观察结果继续推理和行动
```

### 工具（Tool）

工具是 Agent 可以调用的函数。每个工具都有：
- **名称**：工具的标识符
- **描述**：工具的功能说明
- **参数**：工具需要的输入
- **返回值**：工具的输出

---

## 使用示例

### 示例 1：简单查询

**请求：** "现在几点了？"

**Agent 执行过程：**
1. LLM 识别需要时间信息
2. 调用 `getCurrentTime()` 工具
3. 返回当前时间

```bash
curl -X POST http://localhost:8086/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "现在几点了？"}'
```

### 示例 2：数学计算

**请求：** "帮我计算 100 + 50"

**Agent 执行过程：**
1. LLM 识别数学问题
2. 调用 `calculate("+", 100, 50)` 工具
3. 返回结果 150

```bash
curl -X POST http://localhost:8086/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "帮我计算 100 + 50"}'
```

### 示例 3：多步骤任务

**请求：** "查询用户 user123 的信息，然后给他发邮件说'您的账户已激活'"

**Agent 执行过程：**
1. 调用 `queryUserInfo("user123")` 获取用户信息
2. 从返回结果中提取邮箱地址
3. 调用 `sendEmail(邮箱, "账户激活", "您的账户已激活")`
4. 返回完成信息

```bash
curl -X POST http://localhost:8086/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "查询用户 user123 的信息，然后给他发邮件说'\''您的账户已激活'\''"}'
```

### 示例 4：条件判断

**请求：** "查询北京的天气，如果是晴天就告诉我可以去公园"

**Agent 执行过程：**
1. 调用 `queryWeather("北京")` 获取天气信息
2. LLM 根据天气结果判断
3. 返回相应的建议

```bash
curl -X POST http://localhost:8086/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "查询北京的天气，如果是晴天就告诉我可以去公园"}'
```

### 示例 5：数据分析

**请求：** "分析销售数据并告诉我增长率"

**Agent 执行过程：**
1. 调用 `analyzeData("sales")` 获取销售数据
2. LLM 提取增长率信息
3. 返回分析结果

```bash
curl -X POST http://localhost:8086/api/agent/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "分析销售数据并告诉我增长率"}'
```

---

## 项目结构

```
scenario-6-intelligent-agent/
├── src/main/java/com/langchain4j/scenarios/scenario6/
│   ├── Scenario6Application.java          # Spring Boot 主应用
│   ├── agent/
│   │   └── IntelligentAgentService.java   # Agent 核心逻辑
│   ├── tool/
│   │   └── AgentTools.java                # 工具定义（6个工具）
│   ├── controller/
│   │   └── AgentController.java           # REST API 控制器
│   └── demo/
│       └── AgentDemoScenarios.java        # 演示和测试场景
├── src/main/resources/
│   └── application.properties             # 应用配置
├── pom.xml                                # Maven 配置
├── AGENT_LEARNING_GUIDE.md                # 详细学习指南
└── README.md                              # 本文件
```

---

## 关键文件说明

### 1. AgentTools.java - 工具定义

定义了 Agent 可以调用的 6 个工具：

| 工具 | 功能 | 参数 |
|------|------|------|
| `getCurrentTime()` | 获取当前时间 | 无 |
| `calculate()` | 数学运算 | operation, num1, num2 |
| `queryUserInfo()` | 查询用户信息 | userId |
| `queryWeather()` | 查询天气 | city |
| `sendEmail()` | 发送邮件 | recipient, subject, content |
| `analyzeData()` | 分析数据 | dataType |

### 2. IntelligentAgentService.java - Agent 核心逻辑

实现了 Agent 循环：

```java
public String chat(String userMessage) {
    // 1. 初始化消息列表
    List<ChatMessage> messages = new ArrayList<>();
    messages.add(SystemMessage.from(AGENT_SYSTEM_PROMPT));
    messages.add(UserMessage.from(userMessage));

    // 2. Agent 循环
    while (iteration < maxIterations) {
        // 3. 调用 LLM
        Response<AiMessage> response = chatModel.generate(messages);

        // 4. 检查是否需要调用工具
        if (aiMessage.hasToolExecutionRequests()) {
            // 5. 执行工具
            for (ToolExecutionRequest toolRequest : toolRequests) {
                String toolResult = executeToolRequest(toolRequest);
                messages.add(UserMessage.from("工具执行结果: " + toolResult));
            }
        } else {
            // LLM 给出了最终答案
            return aiMessage.text();
        }
    }
}
```

### 3. AgentController.java - REST API

提供了 4 个端点：

- `POST /api/agent/chat` - 与 Agent 对话
- `GET /api/agent/tools` - 获取可用工具列表
- `GET /api/agent/examples` - 获取使用示例
- `GET /api/agent/health` - 健康检查

---

## 学习路径

### 初级：理解基础概念

1. 阅读 `AGENT_LEARNING_GUIDE.md` 的"核心概念"部分
2. 理解 Agent 与普通 LLM 的区别
3. 理解 ReAct 框架的工作原理

### 中级：学习工具定义

1. 查看 `AgentTools.java` 中的 6 个工具
2. 理解 `@Tool` 注解的作用
3. 理解工具的设计原则

### 高级：理解 Agent 循环

1. 阅读 `IntelligentAgentService.java` 的代码
2. 理解 Agent 循环的每个步骤
3. 理解如何处理工具调用和结果

### 实战：扩展 Agent

1. 添加新的工具
2. 修改系统提示词
3. 实现更复杂的任务

---

## 进阶功能

### 1. 添加新工具

在 `AgentTools.java` 中添加新方法：

```java
@Tool("新工具的描述")
public String newTool(String param) {
    // 实现工具逻辑
    return result;
}
```

然后在 `IntelligentAgentService.java` 的 `executeToolRequest()` 方法中添加对应的 case。

### 2. 修改系统提示词

编辑 `IntelligentAgentService.java` 中的 `AGENT_SYSTEM_PROMPT` 常量，改变 Agent 的行为。

### 3. 实现多轮对话

保存对话历史，在每次请求时将历史消息添加到消息列表。

### 4. 添加权限控制

在 `AgentController.java` 中添加权限检查，限制某些用户只能调用特定工具。

---

## 性能优化

### 1. Token 优化

```java
// 只保存最近的 N 条消息
if (messages.size() > MAX_MESSAGES) {
    messages = messages.subList(messages.size() - MAX_MESSAGES, messages.size());
}
```

### 2. 工具缓存

缓存常用工具的结果，避免重复调用。

### 3. 异步处理

使用异步处理长时间运行的工具调用。

---

## 常见问题

**Q: Agent 和 Function Calling 有什么区别？**

A: Function Calling 通常只调用一次，而 Agent 可以多次调用不同的工具，支持多步推理。

**Q: 如何防止 Agent 陷入无限循环？**

A: 设置最大迭代次数（本项目设置为 10）。

**Q: 如何提高 Agent 的准确性？**

A: 编写清晰的系统提示词，设计好的工具接口，提供详细的工具文档。

---

## 相关资源

- [LangChain4j 官方文档](https://docs.langchain4j.dev/)
- [OpenAI Function Calling](https://platform.openai.com/docs/guides/function-calling)
- [ReAct 论文](https://arxiv.org/abs/2210.03629)
- [AGENT_LEARNING_GUIDE.md](./AGENT_LEARNING_GUIDE.md) - 详细学习指南

---

## 总结

通过本学习案例，你已经学到了：

✅ Agent 的核心概念和工作原理
✅ 如何定义和使用工具
✅ 如何实现 Agent 循环
✅ 如何处理多步骤任务
✅ 如何构建实际的 Agent 应用

下一步可以尝试：
- 添加更多工具
- 实现更复杂的任务
- 集成到实际应用中
- 优化 Agent 的性能
