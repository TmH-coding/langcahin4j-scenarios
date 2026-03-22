# Scenario 7: MCP Protocol - Model Context Protocol 学习案例

## 项目概述

这是一个完整的 MCP（Model Context Protocol）学习案例，展示了 Anthropic 提出的标准化工具接口协议。

**核心特性：**
- ✅ 6 个标准化 MCP 工具（时间、计算、数据库查询、天气、邮件、数据分析）
- ✅ 完整的 JSON Schema 工具定义
- ✅ MCP 与 Agent 对比分析
- ✅ 标准化的工具执行框架
- ✅ REST API 接口
- ✅ 详细的中文学习指南

---

## 快速开始

### 1. 启动应用

```bash
# 设置 OpenAI API Key
export OPENAI_API_KEY=sk-your-key-here

# 启动 scenario-7
mvn spring-boot:run -pl scenario-7-mcp-protocol
```

应用将在 `http://localhost:8087` 启动

### 2. 查看可用工具

```bash
curl http://localhost:8087/api/mcp/tools
```

响应：
```json
{
  "tools": [
    {
      "name": "get_current_time",
      "description": "获取当前日期和时间",
      "inputSchema": {...},
      "category": "utility",
      "requiresConfirmation": false
    },
    ...
  ],
  "count": 6
}
```

### 3. 执行 MCP 工具

```bash
curl -X POST http://localhost:8087/api/mcp/execute \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "req_123",
    "toolName": "calculate",
    "arguments": {
      "operation": "+",
      "num1": 100,
      "num2": 50
    }
  }'
```

### 4. 与 MCP 助手对话

```bash
curl -X POST http://localhost:8087/api/mcp/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "现在几点了？"}'
```

### 5. 查看 MCP 与 Agent 对比

```bash
curl http://localhost:8087/api/mcp/comparison
```

---

## 核心概念

### 什么是 MCP？

MCP（Model Context Protocol）是 Anthropic 提出的标准化工具接口协议。它定义了 LLM 与外部工具交互的标准方式。

**MCP 的三个核心特性：**

1. **标准化（Standardization）**
   - 所有工具遵循相同的接口规范
   - 使用 JSON Schema 定义工具参数
   - 统一的工具执行和结果返回格式

2. **互操作性（Interoperability）**
   - 不同的 LLM 都能理解 MCP 工具
   - 不同的客户端都能调用 MCP 工具
   - 支持工具的跨平台使用

3. **安全性（Security）**
   - 清晰的权限和访问控制
   - 工具可以标记为需要用户确认
   - 完整的审计日志

### MCP 与 Agent 的区别

| 特性 | Agent | MCP |
|------|-------|-----|
| 框架 | ReAct（推理+行动） | 标准化工具接口 |
| 工具调用 | LLM 自主决定 | 遵循 JSON Schema |
| 多步推理 | 支持（循环调用） | 支持（通过 LLM） |
| 标准化 | 工具接口不统一 | 所有工具遵循规范 |
| 使用场景 | 复杂任务、多步骤推理 | 互操作性、安全性、版本管理 |
| 适用领域 | 研究、实验、复杂推理 | 企业级应用、生产环境 |

---

## MCP 工具定义

### JSON Schema 规范

每个 MCP 工具都必须定义 JSON Schema，用于描述工具的参数：

```json
{
  "name": "calculate",
  "description": "执行数学运算（+, -, *, /）",
  "inputSchema": {
    "type": "object",
    "properties": {
      "operation": {
        "type": "string",
        "enum": ["+", "-", "*", "/"],
        "description": "运算符"
      },
      "num1": {
        "type": "number",
        "description": "第一个数字"
      },
      "num2": {
        "type": "number",
        "description": "第二个数字"
      }
    },
    "required": ["operation", "num1", "num2"]
  },
  "category": "utility",
  "requiresConfirmation": false
}
```

### 本项目中的 6 个工具

| 工具 | 功能 | 参数 | 分类 |
|------|------|------|------|
| `get_current_time` | 获取当前时间 | timezone（可选） | utility |
| `calculate` | 数学运算 | operation, num1, num2 | utility |
| `query_user_info` | 查询用户信息 | userId | database |
| `query_weather` | 查询天气 | city | external |
| `send_email` | 发送邮件 | recipient, subject, content | communication |
| `analyze_data` | 分析数据 | dataType | analytics |

---

## 项目结构

```
scenario-7-mcp-protocol/
├── src/main/java/com/langchain4j/scenarios/scenario7/
│   ├── Scenario7Application.java          # Spring Boot 主应用
│   ├── mcp/
│   │   ├── McpTool.java                   # MCP 工具定义
│   │   ├── McpToolExecutionRequest.java   # 工具执行请求
│   │   └── McpToolExecutionResult.java    # 工具执行结果
│   ├── tool/
│   │   └── McpTools.java                  # 工具实现（6个工具）
│   ├── service/
│   │   └── McpService.java                # MCP 核心逻辑
│   └── controller/
│       └── McpController.java             # REST API 控制器
├── src/main/resources/
│   └── application.properties             # 应用配置
├── pom.xml                                # Maven 配置
└── README.md                              # 本文件
```

---

## 关键文件说明

### 1. McpTool.java - 工具定义

定义了 MCP 工具的标准结构：
- `name`: 工具的唯一标识符
- `description`: 工具的功能描述
- `inputSchema`: 工具参数的 JSON Schema 定义
- `category`: 工具的分类
- `requiresConfirmation`: 是否需要用户确认

### 2. McpService.java - MCP 核心逻辑

实现了 MCP 的核心功能：
- `getAvailableTools()`: 获取所有可用工具
- `executeTool()`: 执行工具请求
- `chat()`: 与 MCP 助手对话
- `getLearningGuide()`: 获取学习指南

### 3. McpController.java - REST API

提供了 5 个端点：
- `GET /api/mcp/tools` - 获取可用工具列表
- `POST /api/mcp/execute` - 执行工具
- `POST /api/mcp/chat` - 与助手对话
- `GET /api/mcp/guide` - 获取学习指南
- `GET /api/mcp/comparison` - 获取 MCP 与 Agent 对比

---

## 使用示例

### 示例 1：执行计算工具

**请求：**
```bash
curl -X POST http://localhost:8087/api/mcp/execute \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "req_001",
    "toolName": "calculate",
    "arguments": {
      "operation": "+",
      "num1": 100,
      "num2": 50
    }
  }'
```

**响应：**
```json
{
  "requestId": "req_001",
  "success": true,
  "result": {
    "success": true,
    "operation": "+",
    "num1": 100,
    "num2": 50,
    "result": 150
  },
  "executionTime": 5
}
```

### 示例 2：查询用户信息

**请求：**
```bash
curl -X POST http://localhost:8087/api/mcp/execute \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "req_002",
    "toolName": "query_user_info",
    "arguments": {
      "userId": "user123"
    }
  }'
```

### 示例 3：发送邮件（需要确认）

**请求：**
```bash
curl -X POST http://localhost:8087/api/mcp/execute \
  -H "Content-Type: application/json" \
  -d '{
    "requestId": "req_003",
    "toolName": "send_email",
    "arguments": {
      "recipient": "user@example.com",
      "subject": "测试邮件",
      "content": "这是一封测试邮件"
    }
  }'
```

---

## MCP 的优势

### 1. 标准化
- 所有工具遵循相同的接口规范
- 易于理解和使用
- 减少学习成本

### 2. 互操作性
- 不同的 LLM 都能理解 MCP 工具
- 不同的客户端都能调用 MCP 工具
- 支持工具的跨平台使用

### 3. 安全性
- 清晰的权限和访问控制
- 工具可以标记为需要用户确认
- 完整的审计日志

### 4. 版本管理
- 支持工具的版本控制
- 支持向后兼容性
- 易于升级和维护

---

## MCP 与 Agent 的选择

**选择 Agent 当：**
- 需要复杂的多步推理
- 需要 LLM 自主决定调用哪些工具
- 需要灵活的工具组合
- 用于研究和实验

**选择 MCP 当：**
- 需要标准化的工具接口
- 需要多个 LLM 和客户端的互操作性
- 需要清晰的权限和访问控制
- 用于企业级应用和生产环境

---

## 学习路径

### 初级：理解基础概念
1. 理解 MCP 的三个核心特性
2. 理解 JSON Schema 的基本用法
3. 理解工具的标准化定义

### 中级：学习工具定义
1. 查看 McpTool.java 中的工具定义
2. 理解 JSON Schema 的完整规范
3. 理解工具的分类和权限控制

### 高级：理解 MCP 框架
1. 阅读 McpService.java 的代码
2. 理解工具的执行流程
3. 理解 MCP 与 Agent 的区别

### 实战：扩展 MCP
1. 添加新的工具
2. 修改工具的 JSON Schema
3. 实现工具的权限控制

---

## 进阶功能

### 1. 添加新工具

在 `McpTools.java` 中添加新方法：

```java
public static Map<String, Object> newTool(Map<String, Object> args) {
    // 实现工具逻辑
    Map<String, Object> result = new HashMap<>();
    result.put("success", true);
    result.put("data", "...");
    return result;
}
```

然后在 `McpService.java` 的 `getAvailableTools()` 中添加工具定义。

### 2. 实现权限控制

在 `McpTool` 中添加权限字段，在执行前检查用户权限。

### 3. 实现工具版本管理

为每个工具添加版本号，支持多个版本的工具共存。

### 4. 实现审计日志

记录所有工具的执行情况，用于安全审计。

---

## 性能优化

### 1. 工具缓存
缓存常用工具的结果，避免重复执行。

### 2. 异步处理
使用异步处理长时间运行的工具调用。

### 3. 工具分组
将相关工具分组，减少 LLM 的决策复杂度。

---

## 常见问题

**Q: MCP 和 Function Calling 有什么区别？**

A: Function Calling 是 LLM 的一个功能，允许 LLM 调用函数。MCP 是一个标准化的协议，定义了如何定义和调用工具。MCP 可以基于 Function Calling 实现。

**Q: MCP 支持多少个工具？**

A: 理论上没有限制，但实际上工具太多会增加 LLM 的决策难度。建议 5-10 个工具为最佳。

**Q: 如何确保 MCP 工具的安全性？**

A: 通过权限控制、用户确认、审计日志等方式确保安全性。

---

## 总结

通过本学习案例，你已经学到了：

✅ MCP 的核心概念和特性
✅ JSON Schema 的基本用法
✅ 如何定义标准化的工具
✅ MCP 与 Agent 的区别
✅ 如何实现 MCP 框架

下一步可以尝试：
- 添加更多工具
- 实现权限控制
- 实现审计日志
- 集成到实际应用中
