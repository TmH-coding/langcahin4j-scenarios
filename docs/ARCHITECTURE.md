# LangChain4j 架构设计指南

深入理解 LangChain4j 应用的架构设计和模式。

## 1. 整体架构

```
┌─────────────────────────────────────────────────────────────┐
│                     REST API Layer                          │
│  (Controllers - 处理 HTTP 请求)                             │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────────┐
│                   Service Layer                             │
│  (业务逻辑 - 与 LLM 交互)                                    │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────────┐
│              LangChain4j Core Layer                          │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │ ChatLanguage │  │   Document   │  │    Memory    │      │
│  │    Model     │  │   Processor  │  │  Management  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└────────────────────┬────────────────────────────────────────┘
                     │
┌────────────────────▼────────────────────────────────────────┐
│              External Services                              │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   OpenAI     │  │   Database   │  │   Vector DB  │      │
│  │     API      │  │  (Optional)  │  │  (Optional)  │      │
│  └──────────────┘  └──────────────┘  └──────────────┘      │
└─────────────────────────────────────────────────────────────┘
```

## 2. 模块设计

### 2.1 Common 模块（共享基础设施）

```
common/
├── config/
│   └── LlmConfig.java              # LLM 配置和初始化
├── model/
│   └── ConversationMessage.java     # 数据模型
└── util/
    ├── DocumentProcessingUtil.java  # 文档处理工具
    ├── ConversationMemoryUtil.java  # 对话管理工具
    └── PromptTemplateUtil.java      # 提示词管理工具
```

**职责**：
- 提供统一的 LLM 配置
- 定义共享的数据模型
- 提供通用的工具函数

### 2.2 场景模块（业务实现）

每个场景模块遵循相同的结构：

```
scenario-X-name/
├── controller/
│   └── XxxController.java           # REST API 端点
├── service/
│   └── XxxService.java              # 业务逻辑
├── model/
│   └── XxxRequest/Response.java     # 请求/响应模型
└── resources/
    └── application.properties       # 配置文件
```

**职责分离**：
- **Controller**：处理 HTTP 请求、参数验证、错误响应
- **Service**：实现业务逻辑、与 LLM 交互、数据处理
- **Model**：定义数据结构

## 3. 数据流

### 3.1 客服系统数据流

```
用户请求
    ↓
Controller 验证输入
    ↓
Service 添加到内存
    ↓
构建 ChatRequest（包含历史）
    ↓
调用 ChatLanguageModel
    ↓
解析 ChatResponse
    ↓
保存到内存
    ↓
返回响应
```

### 3.2 文档分析数据流

```
加载文档
    ↓
DocumentLoader 读取文件
    ↓
DocumentSplitter 分块
    ↓
存储到内存
    ↓
用户查询
    ↓
检索相关分块
    ↓
构建上下文
    ↓
调用 LLM
    ↓
返回答案
```

## 4. 关键设计决策

### 4.1 为什么使用 Spring Boot？

- **自动配置**：减少样板代码
- **依赖注入**：便于测试和维护
- **内置 Web 支持**：快速构建 REST API
- **生态完善**：与其他库集成容易

### 4.2 为什么分离 Common 模块？

- **代码复用**：避免重复代码
- **一致性**：所有场景使用相同的配置
- **可维护性**：修改一处，所有场景受益
- **可测试性**：独立测试共享组件

### 4.3 为什么使用内存存储？

- **简单快速**：适合学习和演示
- **无外部依赖**：降低复杂度
- **易于理解**：专注于 LangChain4j 核心

**生产环境建议**：
- 使用 PostgreSQL 存储对话历史
- 使用 Redis 缓存
- 使用 Milvus/Weaviate 存储向量

## 5. 扩展点

### 5.1 添加数据库支持

```java
// 1. 添加 Spring Data JPA 依赖
// 2. 创建 Entity 类
@Entity
public class Conversation {
    @Id
    private String id;
    private String userId;
    private String message;
    private String response;
    private LocalDateTime timestamp;
}

// 3. 创建 Repository
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, String> {
    List<Conversation> findByUserId(String userId);
}

// 4. 修改 Service 使用数据库
@Service
public class CustomerServiceAI {
    @Autowired
    private ConversationRepository repository;

    public String chat(String message) {
        // 从数据库加载历史
        List<Conversation> history = repository.findByUserId(userId);
        // ... 处理逻辑
        // 保存到数据库
        repository.save(conversation);
    }
}
```

### 5.2 添加向量数据库支持

```java
// 1. 添加 Milvus 依赖
// 2. 配置向量数据库连接
@Configuration
public class VectorDbConfig {
    @Bean
    public EmbeddingStore embeddingStore() {
        return MilvusEmbeddingStore.builder()
            .host("localhost")
            .port(19530)
            .collectionName("documents")
            .build();
    }
}

// 3. 使用向量搜索
@Service
public class DocumentAnalysisService {
    @Autowired
    private EmbeddingStore embeddingStore;

    public List<TextSegment> search(String query) {
        return embeddingStore.search(query, 5);
    }
}
```

### 5.3 添加认证和授权

```java
// 1. 添加 Spring Security 依赖
// 2. 创建认证过滤器
@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {
        String apiKey = request.getHeader("X-API-Key");
        if (!isValidApiKey(apiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        filterChain.doFilter(request, response);
    }
}

// 3. 在 Controller 中使用
@RestController
@RequestMapping("/api/customer-service")
public class CustomerServiceController {
    @PostMapping("/chat")
    public String chat(@RequestHeader("X-API-Key") String apiKey,
                      @RequestParam String message) {
        // 处理请求
    }
}
```

## 6. 性能考虑

### 6.1 对话历史管理

```
问题：对话历史过长导致 API 调用变慢
解决：使用滑动窗口

┌─────────────────────────────────────┐
│ 对话历史（最多 20 条）              │
├─────────────────────────────────────┤
│ 消息 1  │ 消息 2  │ ... │ 消息 20  │
└─────────────────────────────────────┘
         ↑                    ↑
      移除                  保留
```

### 6.2 文档分块优化

```
文档大小 vs 分块大小

小文档（< 10KB）：
  分块大小：300-500 字符
  重叠：50 字符

中等文档（10-100KB）：
  分块大小：500-800 字符
  重叠：100 字符

大文档（> 100KB）：
  分块大小：800-1000 字符
  重叠：150 字符
```

### 6.3 缓存策略

```java
// LRU 缓存实现
private final Map<String, String> cache = new LinkedHashMap<String, String>(16, 0.75f, true) {
    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        return size() > 1000;  // 最多缓存 1000 条
    }
};
```

## 7. 错误处理架构

```
┌─────────────────────────────────────┐
│        API 请求                     │
└────────────┬────────────────────────┘
             │
┌────────────▼────────────────────────┐
│    参数验证                         │
│  (IllegalArgumentException)         │
└────────────┬────────────────────────┘
             │
┌────────────▼────────────────────────┐
│    业务逻辑                         │
│  (RateLimitException,               │
│   AuthenticationException)          │
└────────────┬────────────────────────┘
             │
┌────────────▼────────────────────────┐
│    全局异常处理                     │
│  (GlobalExceptionHandler)           │
└────────────┬────────────────────────┘
             │
┌────────────▼────────────────────────┐
│    返回错误响应                     │
└─────────────────────────────────────┘
```

## 8. 测试架构

```
单元测试
├── Service 测试（Mock LLM）
├── Util 测试
└── Model 测试

集成测试
├── Controller 测试
├── Service 测试（真实 LLM）
└── 端到端测试

性能测试
├── 并发测试
├── 负载测试
└── 成本分析
```

## 9. 部署架构

### 9.1 单机部署

```
┌──────────────────────┐
│   Spring Boot App    │
│  (所有 5 个场景)     │
└──────────────────────┘
```

### 9.2 微服务部署

```
┌──────────────────┐
│  API Gateway     │
└────────┬─────────┘
         │
    ┌────┴────┬────────┬────────┬────────┐
    │          │        │        │        │
┌───▼──┐  ┌───▼──┐ ┌───▼──┐ ┌──▼───┐ ┌──▼───┐
│Sce 1 │  │Sce 2 │ │Sce 3 │ │Sce 4 │ │Sce 5 │
└──────┘  └──────┘ └──────┘ └──────┘ └──────┘
```

### 9.3 容器化部署

```dockerfile
FROM openjdk:17-slim
WORKDIR /app
COPY target/scenario-1-customer-service.jar app.jar
ENV OPENAI_API_KEY=${OPENAI_API_KEY}
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## 10. 监控和可观测性

### 10.1 关键指标

```
应用指标：
- 请求延迟（P50, P95, P99）
- 错误率
- 吞吐量

LLM 指标：
- API 调用次数
- Token 使用量
- 成本

业务指标：
- 用户满意度
- 响应质量
- 缓存命中率
```

### 10.2 日志结构

```json
{
  "timestamp": "2026-03-21T09:30:00Z",
  "level": "INFO",
  "logger": "CustomerServiceAI",
  "message": "Chat request processed",
  "userId": "user123",
  "messageLength": 50,
  "responseTime": 1234,
  "model": "gpt-4-turbo-preview",
  "tokensUsed": 150
}
```

## 总结

这个架构设计提供了：
- **清晰的分层**：便于理解和维护
- **高内聚低耦合**：易于扩展和测试
- **可扩展性**：支持从学习到生产的演进
- **最佳实践**：遵循 Spring Boot 和 LangChain4j 的最佳实践
