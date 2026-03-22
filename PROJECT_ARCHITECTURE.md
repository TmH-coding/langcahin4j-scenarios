# 项目架构文档

## 项目概览

LangChain4j 多场景学习项目是一个包含 8 个场景的完整学习平台，展示了从基础 LLM 集成到高级 AI 应用的完整技术栈。

**项目目标：**
- 提供完整的 LLM 应用学习路径
- 展示真实场景的实现方式
- 提供最佳实践和优化建议
- 支持快速原型开发和扩展

---

## 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                     客户端层（Client）                        │
│              REST API / Web UI / 移动应用                    │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                   API 网关层（Gateway）                       │
│              路由、认证、限流、日志、监控                      │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                   控制器层（Controller）                       │
│         处理 HTTP 请求、参数验证、响应格式化                  │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                   业务逻辑层（Service）                        │
│      LLM 调用、工具执行、数据处理、业务规则                   │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                   数据访问层（Repository）                     │
│         数据库查询、缓存、向量存储、文件系统                   │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                   外部服务层（External）                       │
│         OpenAI API、数据库、向量数据库、消息队列              │
└─────────────────────────────────────────────────────────────┘
```

---

## 模块结构

### 1. Common 模块（共享基础设施）

**职责：**
- 提供 LLM 配置和初始化
- 提供通用工具类
- 提供数据模型和常量

**核心文件：**
```
common/
├── config/
│   ├── LlmConfig.java              # LLM Bean 配置
│   ├── LlmProperties.java          # LLM 配置属性
│   └── EmbeddingConfig.java        # Embedding 配置
├── util/
│   ├── PromptBuilder.java          # 提示词构建器
│   ├── PromptTemplateUtil.java     # 提示词模板
│   ├── TokenCountUtil.java         # Token 计数工具
│   └── ConversationMemoryUtil.java # 对话历史管理
├── model/
│   ├── ConversationMessage.java    # 对话消息模型
│   └── ApiResponse.java            # API 响应模型
└── constant/
    └── Constants.java              # 常量定义
```

**关键设计：**
- 使用 Spring 配置类管理 Bean 生命周期
- 使用 @ConfigurationProperties 读取配置
- 提供工具类简化常见操作

---

### 2. Scenario 1-5 模块（业务场景）

**通用结构：**
```
scenario-X/
├── controller/
│   └── XxxController.java          # REST API 端点
├── service/
│   └── XxxService.java             # 业务逻辑
├── model/
│   └── XxxRequest/Response.java    # 数据模型
├── repository/
│   └── XxxRepository.java          # 数据访问
└── resources/
    └── application.properties      # 配置文件
```

**数据流：**
```
HTTP Request
    ↓
Controller（参数验证）
    ↓
Service（业务逻辑）
    ↓
Repository（数据访问）
    ↓
LLM / Database / Cache
    ↓
Response（格式化）
    ↓
HTTP Response
```

---

### 3. Scenario 6: Agent 模块

**特殊之处：**
- 实现了 ReAct 框架
- 支持多步推理和工具调用
- 包含工具执行和结果反馈机制

**核心文件：**
```
scenario-6/
├── tool/
│   └── AgentTools.java             # 6 个工具定义
├── service/
│   └── IntelligentAgentService.java # Agent 核心循环
├── controller/
│   └── AgentController.java        # REST API
└── demo/
    └── AgentDemoScenarios.java     # 演示场景
```

**Agent 循环流程：**
```
用户请求
    ↓
初始化消息列表
    ↓
调用 LLM
    ↓
检查工具调用请求
    ├─ 有 → 执行工具 → 反馈结果 → 继续循环
    └─ 无 → 返回最终答案
```

---

### 4. Scenario 7: MCP 模块

**特殊之处：**
- 实现了标准化工具接口
- 使用 JSON Schema 定义工具
- 强调互操作性和安全性

**核心文件：**
```
scenario-7/
├── mcp/
│   ├── McpTool.java                # 工具定义
│   ├── McpToolExecutionRequest.java # 执行请求
│   └── McpToolExecutionResult.java  # 执行结果
├── tool/
│   └── McpTools.java               # 工具实现
├── service/
│   └── McpService.java             # MCP 核心逻辑
└── controller/
    └── McpController.java          # REST API
```

---

## 数据流设计

### 多轮对话数据流

```
用户消息
    ↓
ConversationMemoryUtil.addMessage("user", message)
    ↓
获取对话历史
    ↓
PromptBuilder.buildMessagesWithHistory()
    ↓
ChatLanguageModel.generate()
    ↓
AI 回复
    ↓
ConversationMemoryUtil.addMessage("assistant", reply)
    ↓
返回给用户
```

### RAG 数据流

```
文档
    ↓
分块处理
    ↓
向量化（Embedding）
    ↓
存储到向量数据库
    ↓
用户查询
    ↓
查询向量化
    ↓
相似度检索
    ↓
获取上下文
    ↓
构建 RAG 提示词
    ↓
LLM 生成答案
```

### Agent 数据流

```
用户请求
    ↓
LLM 分析（Reasoning）
    ↓
生成工具调用请求
    ↓
执行工具（Acting）
    ↓
获取工具结果（Observation）
    ↓
反馈给 LLM
    ↓
继续推理或返回答案
```

---

## 配置管理

### 配置层次

```
application.properties（默认配置）
    ↓
application-{profile}.properties（环境特定配置）
    ↓
环境变量（敏感信息）
    ↓
@ConfigurationProperties（类型安全配置）
```

### 配置示例

```properties
# LLM 配置
llm.provider=openai
llm.model=gpt-4-turbo-preview
llm.temperature=0.7
llm.max-tokens=2048
llm.timeout=60

# 应用配置
spring.application.name=scenario-1-customer-service
server.port=8081

# 日志配置
logging.level.root=INFO
logging.level.dev.langchain4j=DEBUG
```

---

## 依赖关系

### 模块依赖图

```
scenario-1 ─┐
scenario-2 ─┤
scenario-3 ─┼─→ common ─→ Spring Boot ─→ LangChain4j ─→ OpenAI API
scenario-4 ─┤
scenario-5 ─┤
scenario-6 ─┤
scenario-7 ─┘
```

### 关键依赖

```xml
<!-- LangChain4j 核心 -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-core</artifactId>
</dependency>

<!-- OpenAI 集成 -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-open-ai</artifactId>
</dependency>

<!-- Embedding 模型 -->
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-embeddings-all-minilm-l6-v2</artifactId>
</dependency>

<!-- Spring Boot -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

---

## 扩展指南

### 添加新的 Scenario

**步骤：**

1. 创建新模块目录
```bash
mkdir scenario-X-name
```

2. 创建 pom.xml
```xml
<parent>
    <groupId>com.langchain4j.scenarios</groupId>
    <artifactId>langchain4j-scenarios</artifactId>
    <version>1.0.0</version>
</parent>

<artifactId>scenario-X-name</artifactId>
```

3. 创建标准目录结构
```
src/main/java/com/langchain4j/scenarios/scenarioX/
├── controller/
├── service/
├── model/
└── repository/
```

4. 实现 Service 类
```java
@Service
@RequiredArgsConstructor
public class XxxService {
    private final ChatLanguageModel chatModel;

    public String process(String input) {
        // 实现业务逻辑
    }
}
```

5. 实现 Controller 类
```java
@RestController
@RequestMapping("/api/xxx")
@RequiredArgsConstructor
public class XxxController {
    private final XxxService service;

    @PostMapping("/process")
    public ResponseEntity<Map<String, Object>> process(@RequestBody Map<String, String> request) {
        // 处理请求
    }
}
```

6. 更新父 pom.xml
```xml
<modules>
    ...
    <module>scenario-X-name</module>
</modules>
```

---

### 添加新工具

**对于 Agent（Scenario 6）：**

```java
// 1. 在 AgentTools.java 中添加工具
@Tool("工具描述")
public String newTool(String param) {
    // 实现工具逻辑
    return result;
}

// 2. 在 IntelligentAgentService.java 中添加执行逻辑
case "newTool":
    result = AgentTools.newTool(args);
    break;
```

**对于 MCP（Scenario 7）：**

```java
// 1. 在 McpTools.java 中添加工具
public static Map<String, Object> newTool(Map<String, Object> args) {
    // 实现工具逻辑
    Map<String, Object> result = new HashMap<>();
    result.put("success", true);
    result.put("data", "...");
    return result;
}

// 2. 在 McpService.java 中添加工具定义
tools.add(new McpTool(
    "new_tool",
    "工具描述",
    buildSchema(...),
    "category",
    false
));

// 3. 在 McpService.executeTool() 中添加执行逻辑
case "new_tool":
    toolResult = McpTools.newTool(args);
    break;
```

---

## 性能考虑

### 缓存策略

```
L1 缓存：本地内存缓存（快速，容量小）
    ↓
L2 缓存：Redis 分布式缓存（中等速度，容量大）
    ↓
L3 缓存：数据库（慢，容量最大）
```

### 并发处理

```
同步处理：简单，但吞吐量低
    ↓
异步处理：复杂，但吞吐量高
    ↓
流式处理：实时，但需要特殊处理
```

---

## 安全考虑

### API 安全

```
认证（Authentication）
    ↓
授权（Authorization）
    ↓
输入验证（Input Validation）
    ↓
输出过滤（Output Filtering）
    ↓
审计日志（Audit Logging）
```

### 敏感信息保护

```
API Key：使用环境变量
    ↓
用户数据：加密存储
    ↓
日志：过滤敏感信息
    ↓
传输：使用 HTTPS
```

---

## 监控和告警

### 关键指标

```
响应时间（Response Time）
    ↓
吞吐量（Throughput）
    ↓
错误率（Error Rate）
    ↓
Token 使用（Token Usage）
    ↓
成本（Cost）
```

### 监控实现

```
应用层：Spring Actuator
    ↓
指标收集：Micrometer
    ↓
时间序列数据库：Prometheus
    ↓
可视化：Grafana
    ↓
告警：AlertManager
```

---

## 部署架构

### 开发环境

```
本地开发
    ↓
单机运行
    ↓
内存数据库
    ↓
本地 LLM 或 API Key
```

### 生产环境

```
容器化（Docker）
    ↓
编排（Kubernetes）
    ↓
负载均衡（Load Balancer）
    ↓
分布式缓存（Redis）
    ↓
数据库集群（PostgreSQL）
    ↓
向量数据库（Milvus）
    ↓
监控系统（Prometheus + Grafana）
```

---

## 技术栈总结

| 层级 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 17+ |
| 框架 | Spring Boot | 3.2.0 |
| LLM | LangChain4j | 0.31.0 |
| LLM 提供商 | OpenAI | 最新 |
| 向量化 | AllMiniLmL6V2 | 0.31.0 |
| 向量存储 | InMemory/Milvus | - |
| 缓存 | Redis | 7.0+ |
| 数据库 | PostgreSQL | 14+ |
| 构建 | Maven | 3.8+ |
| 容器 | Docker | 20.10+ |

---

## 常见问题

### Q1: 如何添加新的 LLM 提供商？

**A:**
1. 在 LlmConfig 中添加新的 Bean
2. 使用 @ConditionalOnProperty 条件化加载
3. 更新配置文件

### Q2: 如何实现多租户支持？

**A:**
1. 在数据模型中添加 tenantId
2. 在 Repository 中过滤租户数据
3. 在 Controller 中提取租户信息

### Q3: 如何扩展到微服务架构？

**A:**
1. 将每个 Scenario 拆分为独立服务
2. 使用 API 网关统一入口
3. 使用消息队列解耦服务
4. 使用服务注册和发现

---

## 最佳实践总结

1. **分层架构**：清晰的职责划分
2. **依赖注入**：使用 Spring 管理依赖
3. **配置管理**：使用 @ConfigurationProperties
4. **错误处理**：具体的异常处理
5. **日志记录**：结构化日志
6. **测试覆盖**：单元测试和集成测试
7. **文档完整**：代码注释和 API 文档
8. **性能优化**：缓存、异步、并发
9. **安全第一**：输入验证、敏感信息保护
10. **监控告警**：关键指标监控

---

## 相关资源

- [项目 README](./README.md)
- [学习指南](./LEARNING_GUIDE.md)
- [快速参考](./QUICK_REFERENCE.md)
- [最佳实践](./BEST_PRACTICES.md)
- [性能优化](./PERFORMANCE_OPTIMIZATION.md)
- [故障排查](./TROUBLESHOOTING.md)
- [A​PI 参考](./API_REFERENCE.md)
