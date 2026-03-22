# 企业级功能集成指南

## 新增功能概览

### 1. 数据持久化
- **ConversationRecord** - 对话记录实体
- **ConversationRecordRepository** - 对话记录仓储
- **ConversationRecordService** - 对话记录服务

### 2. API文档
- **SwaggerConfig** - Swagger/OpenAPI配置
- 自动生成API文档
- 支持在线测试

### 3. 数据传输对象
- **ChatRequest** - 对话请求DTO
- **ChatResponse** - 对话响应DTO
- 参数验证和文档

### 4. 缓存管理
- **CacheConfig** - Redis缓存配置
- 支持缓存过期时间设置
- 支持缓存空值禁用

### 5. 定时任务
- **SchedulingConfig** - 定时任务配置
- **ScheduledTasks** - 定时任务实现
- 支持Cron表达式

### 6. 安全配置
- **SecurityConfig** - Spring Security配置
- JWT认证支持
- CORS和CSRF配置

### 7. 事件驱动
- **ConversationEvent** - 对话事件
- **ConversationEventListener** - 事件监听器
- 支持事件驱动架构

### 8. 审计日志
- **AuditLog** - 审计日志实体
- **AuditLogRepository** - 审计日志仓储
- **AuditLogService** - 审计日志服务

### 9. 健康检查
- **CustomHealthIndicator** - 自定义健康检查
- 检查外部依赖
- Actuator集成

### 10. 指标监控
- **MetricsConfig** - Micrometer指标配置
- Prometheus导出支持
- 应用性能监控

### 11. 请求日志
- **RequestLoggingConfig** - 请求日志配置
- 记录请求和响应
- 用于调试和审计

### 12. 分页工具
- **PageUtils** - 分页工具类
- 分页参数验证
- 分页结果转换

### 13. HTTP客户端
- **RestTemplateConfig** - RestTemplate配置
- 连接池支持
- 超时配置

### 14. API版本控制
- **ApiVersionConfig** - API版本控制配置
- 支持多个API版本
- 便于API升级

## 配置文件更新

### application.yml 新增配置

```yaml
# 缓存配置
spring:
  cache:
    type: redis
    redis:
      time-to-live: 3600000

# Actuator配置
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  endpoint:
    health:
      show-details: always

# 日志配置
logging:
  level:
    org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping: TRACE
```

## 使用示例

### 1. 发布事件

```java
@Service
public class ChatService {
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public void chat(String message) {
        // 发布对话开始事件
        ConversationEvent event = new ConversationEvent(
            this, "START", sessionId, userId, message, "customer-service"
        );
        eventPublisher.publishEvent(event);
    }
}
```

### 2. 记录审计日志

```java
@Service
public class AuditService {
    @Autowired
    private AuditLogService auditLogService;

    public void recordOperation(String operator, String operation) {
        auditLogService.recordAudit(
            operator,
            operation,
            "target",
            "details",
            "success",
            "192.168.1.1",
            "Mozilla/5.0..."
        );
    }
}
```

### 3. 使用缓存

```java
@Service
public class CacheService {
    @Cacheable(value = "conversations", key = "#sessionId")
    public ConversationRecord getConversation(String sessionId) {
        return recordService.getRecord(sessionId);
    }
}
```

### 4. 定时任务

```java
@Component
public class MaintenanceTasks {
    @Scheduled(cron = "0 0 2 * * *")
    public void dailyMaintenance() {
        // 每天凌晨2点执行
    }
}
```

### 5. 健康检查

```
GET /actuator/health
```

响应：
```json
{
  "status": "UP",
  "components": {
    "customHealth": {
      "status": "UP",
      "details": {
        "llm": "connected",
        "database": "connected"
      }
    }
  }
}
```

### 6. 指标监控

```
GET /actuator/metrics
GET /actuator/prometheus
```

## 最佳实践

1. **事件驱动** - 使用事件解耦业务逻辑
2. **审计追踪** - 记录所有重要操作
3. **缓存策略** - 合理使用缓存提高性能
4. **定时维护** - 定期清理过期数据
5. **健康检查** - 监控外部依赖状态
6. **指标监控** - 收集应用性能指标

## 下一步

1. 集成消息队列（RabbitMQ/Kafka）
2. 实现分布式追踪（Sleuth/Jaeger）
3. 添加限流控制（Sentinel/Resilience4j）
4. 实现数据库迁移（Flyway/Liquibase）
5. 添加文件上传处理
6. 实现批量处理功能
