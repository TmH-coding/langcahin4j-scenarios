# 分布式追踪指南

## 概述

本项目集成了Spring Cloud Sleuth和Brave，提供分布式请求追踪能力，支持跨服务链路追踪。

## 核心特性

### 1. 自动追踪

- 为每个HTTP请求自动生成唯一的traceId和spanId
- 自动在日志中添加追踪信息
- 支持跨服务传播

### 2. 日志集成

所有日志自动包含追踪信息：

```
[langchain4j-scenarios,4588348f3d2d9d51,4588348f3d2d9d51] INFO  - 请求处理完成
```

格式: `[应用名,traceId,spanId]`

### 3. 采样策略

- **开发环境**: 100% 采样（所有请求都被追踪）
- **生产环境**: 10% 采样（降低性能开销）

## 配置说明

### 开发环境 (application-tracing-dev.yml)

```yaml
management:
  tracing:
    sampling:
      probability: 1.0  # 100% 采样
```

### 生产环境 (application-tracing-prod.yml)

```yaml
management:
  tracing:
    sampling:
      probability: 0.1  # 10% 采样
```

## 使用示例

### 在代码中访问追踪信息

```java
@Autowired
private Tracer tracer;

public void processRequest() {
    // 获取当前span
    Span currentSpan = tracer.currentSpan();

    // 获取traceId
    String traceId = currentSpan.context().traceId();

    // 创建新的span
    Span newSpan = tracer.nextSpan().name("custom-operation").start();
    try {
        // 执行操作
    } finally {
        newSpan.finish();
    }
}
```

### 跨服务追踪

Sleuth自动在HTTP请求头中添加追踪信息：

```
X-B3-TraceId: 4588348f3d2d9d51
X-B3-SpanId: 4588348f3d2d9d51
X-B3-ParentSpanId: 0
X-B3-Sampled: 1
```

RestTemplate和WebClient会自动传播这些头信息。

## 与消息队列集成

Sleuth自动追踪RabbitMQ消息：

```java
// 发送消息时自动添加追踪信息
messageQueueService.sendConversationMessage(message);

// 消费消息时自动继承追踪上下文
@RabbitListener(queues = "conversation.queue")
public void handleMessage(ConversationMessage message) {
    // 日志自动包含原始请求的traceId
}
```

## 监控和导出

### 查看追踪日志

所有日志都包含追踪信息，可以通过grep搜索：

```bash
grep "4588348f3d2d9d51" application.log
```

### 集成Jaeger（可选）

添加Jaeger导出器以可视化追踪：

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-exporter-jaeger</artifactId>
    <version>1.1.5</version>
</dependency>
```

配置Jaeger导出：

```yaml
management:
  tracing:
    exporter: jaeger
  otlp:
    tracing:
      endpoint: http://localhost:4317
```

## 最佳实践

1. **采样策略**: 生产环境使用较低采样率以降低开销
2. **日志聚合**: 使用ELK或Splunk聚合包含traceId的日志
3. **性能监控**: 监控追踪本身的性能开销
4. **错误追踪**: 使用traceId快速定位分布式系统中的错误

## 故障排查

### 追踪信息未出现在日志中

检查日志配置是否包含追踪信息：

```yaml
logging:
  pattern:
    level: "[%X{traceId},%X{spanId}] %5p"
```

### 跨服务追踪不工作

确保所有服务都使用相同的Sleuth版本，并且HTTP客户端正确配置。
