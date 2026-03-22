# 企业级功能集成指南

## 概述

本项目集成了完整的企业级功能栈，包括消息队列、分布式追踪、数据库迁移和配置中心。本指南说明如何启用和使用这些功能。

## 快速启动

### 启用所有企业功能

在 `application.yml` 中配置：

```yaml
spring:
  profiles:
    include:
      - rabbitmq-dev      # 消息队列
      - tracing-dev       # 分布式追踪
      - flyway-dev        # 数据库迁移
      - nacos-dev         # 配置中心
```

或通过环境变量：

```bash
export SPRING_PROFILES_INCLUDE=rabbitmq-dev,tracing-dev,flyway-dev,nacos-dev
```

### 使用Docker Compose启动完整栈

```bash
# 启动所有服务（PostgreSQL, Redis, RabbitMQ, Nacos, MySQL）
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f customer-service
```

## 功能集成架构

```
┌─────────────────────────────────────────────────────────┐
│                   应用层 (5个场景模块)                    │
├─────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ 客服系统      │  │ 文档分析      │  │ 代码助手      │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
├─────────────────────────────────────────────────────────┤
│                   企业级功能层                           │
├─────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ 消息队列      │  │ 分布式追踪    │  │ 配置中心      │  │
│  │ (RabbitMQ)   │  │ (Sleuth)     │  │ (Nacos)      │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
├─────────────────────────────────────────────────────────┤
│                   基础设施层                             │
├─────────────────────────────────────────────────────────┤
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ PostgreSQL   │  │ Redis        │  │ MySQL        │  │
│  │ (数据持久化)  │  │ (缓存)       │  │ (Nacos DB)   │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
```

## 各功能详细说明

### 1. 消息队列 (RabbitMQ)

**用途**: 异步处理、事件驱动、系统解耦

**配置文件**:
- `application-rabbitmq-dev.yml` - 开发环境
- `application-rabbitmq-prod.yml` - 生产环境

**核心类**:
- `MessageQueueConfig.java` - 交换机和队列配置
- `MessageQueueService.java` - 消息发送服务
- `ConversationMessageListener.java` - 对话消息监听
- `NotificationMessageListener.java` - 通知消息监听
- `AuditLogMessageListener.java` - 审计日志监听

**使用示例**:

```java
@Autowired
private MessageQueueService messageQueueService;

// 发送对话消息
ConversationMessage msg = ConversationMessage.builder()
    .conversationId("conv-123")
    .userId("user-456")
    .message("用户消息")
    .eventType("START")
    .timestamp(LocalDateTime.now())
    .build();
messageQueueService.sendConversationMessage(msg);
```

详见: `docs/MESSAGE_QUEUE_GUIDE.md`

### 2. 分布式追踪 (Spring Cloud Sleuth)

**用途**: 跨服务链路追踪、性能监控、故障诊断

**配置文件**:
- `application-tracing-dev.yml` - 开发环境（100%采样）
- `application-tracing-prod.yml` - 生产环境（10%采样）

**核心类**:
- `TracingConfig.java` - 追踪配置

**特性**:
- 自动为每个请求生成traceId和spanId
- 日志自动包含追踪信息
- 支持跨服务传播
- 可集成Jaeger进行可视化

**日志示例**:

```
[langchain4j-scenarios,4588348f3d2d9d51,4588348f3d2d9d51] INFO - 请求处理完成
```

详见: `docs/DISTRIBUTED_TRACING_GUIDE.md`

### 3. 数据库迁移 (Flyway)

**用途**: 数据库版本管理、自动化架构演进

**配置文件**:
- `application-flyway-dev.yml` - 开发环境
- `application-flyway-prod.yml` - 生产环境

**核心类**:
- `FlywayConfig.java` - Flyway配置

**迁移脚本**:
- `db/migration/V1__Initial_Schema.sql` - 初始架构

**特性**:
- 应用启动时自动执行待迁移脚本
- 按版本号顺序执行
- 记录迁移历史到flyway_schema_history表

**创建新迁移**:

```sql
-- V2__Add_User_Preferences.sql
CREATE TABLE user_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL UNIQUE,
    language VARCHAR(10) DEFAULT 'zh_CN'
);
```

详见: `docs/DATABASE_MIGRATION_GUIDE.md`

### 4. 配置中心 (Nacos)

**用途**: 集中配置管理、动态配置更新、服务发现

**配置文件**:
- `application-nacos-dev.yml` - 开发环境
- `application-nacos-prod.yml` - 生产环境

**核心类**:
- `NacosConfig.java` - Nacos配置

**特性**:
- 从Nacos服务器加载配置
- 支持动态刷新（无需重启）
- 服务自动注册和发现
- 命名空间隔离

**访问Nacos控制台**:

```
http://localhost:8848/nacos
用户名/密码: nacos/nacos
```

**在代码中使用动态配置**:

```java
@RestController
@RefreshScope
public class ConfigController {
    @Value("${app.name:default}")
    private String appName;

    @GetMapping("/config")
    public String getConfig() {
        return appName;  // 配置更新时自动刷新
    }
}
```

详见: `docs/NACOS_CONFIG_GUIDE.md`

## 环境配置

### 开发环境

启用所有功能的开发配置：

```bash
export SPRING_PROFILES_INCLUDE=rabbitmq-dev,tracing-dev,flyway-dev,nacos-dev
export OPENAI_API_KEY=sk-xxx
mvn spring-boot:run
```

### 生产环境

使用生产配置和环境变量：

```bash
export SPRING_PROFILES_INCLUDE=rabbitmq-prod,tracing-prod,flyway-prod,nacos-prod
export OPENAI_API_KEY=sk-xxx
export RABBITMQ_HOST=rabbitmq
export RABBITMQ_PORT=5672
export NACOS_SERVER_ADDR=nacos:8848
export NACOS_NAMESPACE=prod
export DB_URL=jdbc:postgresql://postgres:5432/langchain4j
export DB_USERNAME=postgres
export DB_PASSWORD=password
```

## Docker服务访问

| 服务 | 地址 | 用户名 | 密码 |
|------|------|--------|------|
| PostgreSQL | localhost:5432 | postgres | password |
| Redis | localhost:6379 | - | - |
| RabbitMQ | localhost:5672 | guest | guest |
| RabbitMQ管理界面 | http://localhost:15672 | guest | guest |
| Nacos | http://localhost:8848/nacos | nacos | nacos |
| MySQL | localhost:3306 | root | password |

## 集成工作流

### 1. 用户请求流程

```
用户请求
  ↓
[限流拦截器] → 检查速率限制
  ↓
[请求ID生成] → 生成唯一请求ID
  ↓
[分布式追踪] → 生成traceId和spanId
  ↓
[业务处理] → 执行业务逻辑
  ↓
[消息发送] → 发送异步消息到RabbitMQ
  ↓
[审计日志] → 记录操作到审计队列
  ↓
[响应返回] → 返回结果给用户
```

### 2. 异步处理流程

```
消息发送
  ↓
[RabbitMQ] → 消息队列存储
  ↓
[消息监听器] → 异步消费消息
  ↓
[业务处理] → 执行异步操作
  ↓
[数据持久化] → 保存到PostgreSQL
  ↓
[缓存更新] → 更新Redis缓存
```

### 3. 配置更新流程

```
Nacos配置变更
  ↓
[配置中心] → 推送配置更新
  ↓
[@RefreshScope] → 自动刷新Bean
  ↓
[应用生效] → 无需重启应用
```

## 监控和维护

### 查看RabbitMQ队列

```bash
# 访问管理界面
http://localhost:15672

# 查看队列深度
docker exec rabbitmq rabbitmqctl list_queues
```

### 查看数据库迁移历史

```sql
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

### 查看Nacos配置

```
http://localhost:8848/nacos
→ 配置管理 → 配置列表
```

### 查看分布式追踪日志

```bash
# 搜索特定traceId的所有日志
grep "4588348f3d2d9d51" application.log
```

## 故障排查

### 消息队列连接失败

```bash
# 检查RabbitMQ服务
docker ps | grep rabbitmq

# 查看RabbitMQ日志
docker logs rabbitmq
```

### 数据库迁移失败

```bash
# 查看迁移历史
docker exec postgres psql -U postgres -d langchain4j -c "SELECT * FROM flyway_schema_history;"

# 检查应用日志
docker logs customer-service | grep -i flyway
```

### Nacos连接失败

```bash
# 检查Nacos服务
docker ps | grep nacos

# 查看Nacos日志
docker logs nacos
```

## 性能优化建议

1. **消息队列**: 调整并发数和预取数以平衡吞吐量和延迟
2. **分布式追踪**: 生产环境使用较低采样率（10%）以降低开销
3. **数据库**: 为常用查询添加索引，使用连接池
4. **缓存**: 合理设置Redis过期时间，避免缓存雪崩
5. **配置中心**: 缓存配置以减少Nacos访问

## 下一步

- 集成Jaeger进行分布式追踪可视化
- 配置Prometheus和Grafana进行监控
- 实现配置加密和权限管理
- 集成ELK进行日志聚合分析
- 部署到Kubernetes进行容器编排
