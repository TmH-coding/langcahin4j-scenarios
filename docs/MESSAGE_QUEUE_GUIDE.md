# 消息队列集成指南

## 概述

本项目集成了RabbitMQ消息队列，支持异步消息处理、事件驱动架构和系统解耦。

## 核心组件

### 1. 配置类 (MessageQueueConfig)

定义了三个主要的交换机和队列：

- **对话交换机** (conversation.exchange)
  - 队列: conversation.queue
  - 路由键: conversation.*
  - 用途: 处理对话生命周期事件

- **通知交换机** (notification.exchange)
  - 队列: notification.queue
  - 路由键: notification.*
  - 用途: 处理多渠道通知

- **审计交换机** (audit.exchange)
  - 队列: audit.queue
  - 路由键: audit.*
  - 用途: 处理审计日志

### 2. 消息服务 (MessageQueueService)

提供三个主要方法：

```java
// 发送对话消息
messageQueueService.sendConversationMessage(ConversationMessage message);

// 发送通知消息
messageQueueService.sendNotificationMessage(String type, Object payload);

// 发送审计日志
messageQueueService.sendAuditLog(String action, String userId, String details);
```

### 3. 消息监听器

- **ConversationMessageListener**: 处理对话事件 (START, PROCESSING, COMPLETED, FAILED)
- **NotificationMessageListener**: 处理通知消息
- **AuditLogMessageListener**: 处理审计日志

## 使用示例

### 发送对话消息

```java
@Autowired
private MessageQueueService messageQueueService;

public void handleConversation(String conversationId, String userId, String message) {
    // 发送对话开始事件
    ConversationMessage msg = ConversationMessage.builder()
            .conversationId(conversationId)
            .userId(userId)
            .message(message)
            .eventType("START")
            .timestamp(LocalDateTime.now())
            .build();

    messageQueueService.sendConversationMessage(msg);
}
```

### 发送通知

```java
messageQueueService.sendNotificationMessage("email",
    new EmailNotification("user@example.com", "Subject", "Body"));
```

### 发送审计日志

```java
messageQueueService.sendAuditLog("USER_LOGIN", "user123", "User logged in from IP: 192.168.1.1");
```

## 配置说明

### 开发环境 (application-rabbitmq-dev.yml)

- 主机: localhost
- 端口: 5672
- 并发数: 5-10
- 预取数: 1
- 自动确认模式

### 生产环境 (application-rabbitmq-prod.yml)

- 主机: 环境变量 RABBITMQ_HOST
- 端口: 环境变量 RABBITMQ_PORT
- 并发数: 10-20
- 预取数: 5
- 手动确认模式
- 重试次数: 5次

## 启用消息队列

在 application.yml 中添加：

```yaml
spring:
  profiles:
    include: rabbitmq-dev  # 或 rabbitmq-prod
```

或通过环境变量：

```bash
export SPRING_PROFILES_INCLUDE=rabbitmq-dev
```

## Docker Compose 集成

在 docker-compose.yml 中已包含RabbitMQ服务：

```yaml
rabbitmq:
  image: rabbitmq:3.12-management
  ports:
    - "5672:5672"
    - "15672:15672"
  environment:
    RABBITMQ_DEFAULT_USER: guest
    RABBITMQ_DEFAULT_PASS: guest
```

访问管理界面: http://localhost:15672 (用户名/密码: guest/guest)

## 最佳实践

1. **消息幂等性**: 确保消息处理是幂等的，防止重复处理
2. **错误处理**: 使用重试机制处理临时故障
3. **监控**: 监控队列深度和消费速率
4. **性能调优**: 根据负载调整并发数和预取数
5. **死信队列**: 考虑为失败消息配置死信队列

## 故障排查

### 连接失败

检查RabbitMQ服务是否运行：
```bash
docker ps | grep rabbitmq
```

### 消息未被消费

1. 检查监听器是否正确注册
2. 验证队列绑定是否正确
3. 查看应用日志中的错误信息

### 性能问题

- 增加并发数
- 调整预取数
- 检查消息处理逻辑是否有阻塞

## 下一步

- 配置死信队列处理失败消息
- 实现消息追踪和监控
- 集成分布式追踪 (Sleuth/Jaeger)
