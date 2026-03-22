# MCP 协议学习指南

## 目录
1. [核心概念](#核心概念)
2. [MCP 工作原理](#mcp-工作原理)
3. [JSON Schema 规范](#json-schema-规范)
4. [工具定义](#工具定义)
5. [MCP 与 Agent 对比](#mcp-与-agent-对比)
6. [实战示例](#实战示例)
7. [进阶概念](#进阶概念)
8. [常见问题](#常见问题)

---

## 核心概念

### 什么是 MCP？

MCP（Model Context Protocol）是 Anthropic 提出的标准化工具接口协议。它定义了 LLM 与外部工具交互的标准方式。

**MCP 的核心理念：**
- 标准化：所有工具遵循相同的接口规范
- 互操作性：不同的 LLM 和客户端都能理解 MCP 工具
- 安全性：清晰的权限和访问控制
- 可扩展性：易于添加新工具而不破坏现有系统

### MCP 的三个核心特性

#### 1. 标准化（Standardization）

所有 MCP 工具都遵循相同的接口规范：

```json
{
  "name": "tool_name",
  "description": "工具的功能描述",
  "inputSchema": {
    "type": "object",
    "properties": {
      "param1": {"type": "string"},
      "param2": {"type": "number"}
    },
    "required": ["param1"]
  },
  "category": "utility",
  "requiresConfirmation": false
}
```

#### 2. 互操作性（Interoperability）

- 不同的 LLM（GPT-4, Claude, Gemini）都能理解 MCP 工具
- 不同的客户端都能调用 MCP 工具
- 支持工具的跨平台使用

#### 3. 安全性（Security）

- 清晰的权限和访问控制
- 工具可以标记为需要用户确认
- 完整的审计日志

---

## MCP 工作原理

### MCP 的执行流程

```
┌─────────────────────────────────────────────────────────────┐
│ 用户请求：                                                   │
│ "帮我计算 100 + 50"                                          │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ 第1步：LLM 理解请求                                          │
│ "用户要求计算 100 + 50，我需要调用 calculate 工具"          │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ 第2步：LLM 构建工具请求                                      │
│ {                                                            │
│   "toolName": "calculate",                                  │
│   "arguments": {                                            │
│     "operation": "+",                                       │
│     "num1": 100,                                            │
│     "num2": 50                                              │
│   }                                                         │
│ }                                                           │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ 第3步：验证工具请求                                          │
│ - 检查工具是否存在                                           │
│ - 验证参数是否符合 JSON Schema                              │
│ - 检查用户是否有权限调用该工具                               │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ 第4步：执行工具                                              │
│ calculate("+", 100, 50) → 150                               │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ 第5步：返回结果                                              │
│ {                                                            │
│   "success": true,                                          │
│   "result": 150                                             │
│ }                                                           │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ 第6步：LLM 解释结果                                          │
│ "计算结果是 150"                                             │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│ 返回最终答案给用户                                           │
└─────────────────────────────────────────────────────────────┘
```

### MCP 与 Agent 的执行流程对比

**Agent 流程：**
```
用户请求 → LLM 分析 → 调用工具1 → 获取结果 → LLM 继续推理 → 调用工具2 → 获取结果 → LLM 总结 → 返回答案
```

**MCP 流程：**
```
用户请求 → LLM 分析 → 构建工具请求 → 验证请求 → 执行工具 → 返回结果 → LLM 解释 → 返回答案
```

---

## JSON Schema 规范

### JSON Schema 基础

JSON Schema 是一个用于验证 JSON 数据结构的标准。MCP 使用 JSON Schema 来定义工具的参数。

### 基本类型

```json
{
  "type": "object",
  "properties": {
    "string_param": {
      "type": "string",
      "description": "字符串参数"
    },
    "number_param": {
      "type": "number",
      "description": "数字参数"
    },
    "integer_param": {
      "type": "integer",
      "description": "整数参数"
    },
    "boolean_param": {
      "type": "boolean",
      "description": "布尔参数"
    },
    "array_param": {
      "type": "array",
      "items": {"type": "string"},
      "description": "数组参数"
    }
  },
  "required": ["string_param"]
}
```

### 枚举类型

```json
{
  "type": "object",
  "properties": {
    "operation": {
      "type": "string",
      "enum": ["+", "-", "*", "/"],
      "description": "运算符"
    }
  },
  "required": ["operation"]
}
```

### 嵌套对象

```json
{
  "type": "object",
  "properties": {
    "user": {
      "type": "object",
      "properties": {
        "name": {"type": "string"},
        "age": {"type": "integer"}
      },
      "required": ["name"]
    }
  }
}
```

---

## 工具定义

### 工具的设计原则

#### 1. 单一职责
每个工具只做一件事。

✅ 好：`calculate(operation, num1, num2)` - 只做数学运算
❌ 差：`calculateAndSendEmail(operation, num1, num2, email)` - 做了两件事

#### 2. 清晰的参数
参数名要能表达含义。

✅ 好：`queryUserInfo(userId)`
❌ 差：`query(id)`

#### 3. 有意义的返回值
返回结果要能帮助 LLM 做决策。

✅ 好：返回详细的用户信息
❌ 差：返回 "success" 或 "failed"

#### 4. 完整的文档
JSON Schema 要清楚描述工具的功能。

### 本项目中的 6 个工具

#### 工具 1: get_current_time

```json
{
  "name": "get_current_time",
  "description": "获取当前日期和时间",
  "inputSchema": {
    "type": "object",
    "properties": {
      "timezone": {
        "type": "string",
        "description": "时区（可选，默认为 UTC）"
      }
    }
  },
  "category": "utility",
  "requiresConfirmation": false
}
```

#### 工具 2: calculate

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

#### 工具 3: query_user_info

```json
{
  "name": "query_user_info",
  "description": "查询用户的详细信息",
  "inputSchema": {
    "type": "object",
    "properties": {
      "userId": {
        "type": "string",
        "description": "用户 ID"
      }
    },
    "required": ["userId"]
  },
  "category": "database",
  "requiresConfirmation": false
}
```

#### 工具 4: query_weather

```json
{
  "name": "query_weather",
  "description": "查询指定城市的天气信息",
  "inputSchema": {
    "type": "object",
    "properties": {
      "city": {
        "type": "string",
        "description": "城市名称"
      }
    },
    "required": ["city"]
  },
  "category": "external",
  "requiresConfirmation": false
}
```

#### 工具 5: send_email

```json
{
  "name": "send_email",
  "description": "发送邮件给指定收件人",
  "inputSchema": {
    "type": "object",
    "properties": {
      "recipient": {
        "type": "string",
        "description": "收件人邮箱"
      },
      "subject": {
        "type": "string",
        "description": "邮件主题"
      },
      "content": {
        "type": "string",
        "description": "邮件内容"
      }
    },
    "required": ["recipient", "subject", "content"]
  },
  "category": "communication",
  "requiresConfirmation": true
}
```

#### 工具 6: analyze_data

```json
{
  "name": "analyze_data",
  "description": "分析指定类型的数据",
  "inputSchema": {
    "type": "object",
    "properties": {
      "dataType": {
        "type": "string",
        "enum": ["sales", "users", "revenue"],
        "description": "数据类型"
      }
    },
    "required": ["dataType"]
  },
  "category": "analytics",
  "requiresConfirmation": false
}
```

---

## MCP 与 Agent 对比

### 核心区别

| 特性 | Agent | MCP |
|------|-------|-----|
| 框架 | ReAct（推理+行动） | 标准化工具接口 |
| 工具调用 | LLM 自主决定 | 遵循 JSON Schema |
| 多步推理 | 支持（循环调用） | 支持（通过 LLM） |
| 标准化 | 工具接口不统一 | 所有工具遵循规范 |
| 互操作性 | 低（特定于框架） | 高（标准化协议） |
| 安全性 | 基础 | 完善（权限控制） |
| 版本管理 | 不支持 | 支持 |
| 使用场景 | 复杂任务、多步骤推理 | 企业级应用、生产环境 |

### 选择指南

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

## 实战示例

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

**响应：**
```json
{
  "requestId": "req_002",
  "success": true,
  "result": {
    "success": true,
    "userId": "user123",
    "name": "张三",
    "email": "zhangsan@example.com",
    "phone": "13800138000",
    "status": "active"
  },
  "executionTime": 8
}
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

**响应：**
```json
{
  "requestId": "req_003",
  "success": true,
  "result": {
    "success": true,
    "messageId": "msg_1234567890",
    "recipient": "user@example.com",
    "subject": "测试邮件",
    "status": "sent",
    "timestamp": 1234567890
  },
  "executionTime": 12
}
```

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

LLM 根据条件选择不同的工具：

```
用户请求 → LLM 判断条件 → 选择工具A或工具B → 执行 → 返回结果
```

### 3. 错误恢复

当工具执行失败时，LLM 可以尝试其他方式：

```
调用工具 → 失败 → LLM 分析错误 → 尝试其他方式 → 成功
```

### 4. 权限控制

不同的用户有不同的工具访问权限：

```java
if (!userHasPermission(userId, toolName)) {
    return new McpToolExecutionResult(
        false,
        "User does not have permission to call this tool"
    );
}
```

### 5. 审计日志

记录所有工具的执行情况：

```java
auditLog.record(
    userId,
    toolName,
    arguments,
    result,
    executionTime
);
```

---

## 常见问题

### Q1: MCP 和 Function Calling 有什么区别？

**A:** Function Calling 是 LLM 的一个功能，允许 LLM 调用函数。MCP 是一个标准化的协议，定义了如何定义和调用工具。MCP 可以基于 Function Calling 实现。

### Q2: MCP 支持多少个工具？

**A:** 理论上没有限制，但实际上工具太多会增加 LLM 的决策难度。建议 5-10 个工具为最佳。

### Q3: 如何确保 MCP 工具的安全性？

**A:** 通过以下方式确保安全性：
- 权限控制：限制用户能调用的工具
- 用户确认：敏感操作需要用户确认
- 审计日志：记录所有操作用于审计
- 输入验证：验证工具参数是否符合 JSON Schema

### Q4: MCP 可以用于实时应用吗？

**A:** 可以。MCP 的执行速度取决于工具的实现。如果工具实现得当，MCP 可以用于实时应用。

### Q5: 如何扩展 MCP 工具？

**A:** 添加新工具的步骤：
1. 在 `McpTools.java` 中实现工具方法
2. 在 `McpService.java` 的 `getAvailableTools()` 中添加工具定义
3. 在 `McpService.java` 的 `executeTool()` 中添加工具执行逻辑

---

## 总结

通过本学习案例，你已经学到了：

✅ MCP 的核心概念和特性
✅ JSON Schema 的基本用法
✅ 如何定义标准化的工具
✅ MCP 与 Agent 的区别
✅ 如何实现 MCP 框架
✅ MCP 的安全性和权限控制

下一步可以尝试：
- 添加更多工具
- 实现权限控制
- 实现审计日志
- 集成到实际应用中
- 与其他 LLM 集成
