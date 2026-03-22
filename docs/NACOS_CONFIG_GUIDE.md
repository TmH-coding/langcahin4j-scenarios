# Nacos配置中心指南

## 概述

本项目集成了Nacos配置中心，支持动态配置管理、服务发现和注册，实现微服务架构中的集中配置管理。

## 核心特性

### 1. 配置管理

- 集中管理所有应用配置
- 支持多环境配置隔离（dev/prod）
- 动态配置更新，无需重启应用
- 配置版本控制和回滚

### 2. 服务发现

- 自动服务注册
- 健康检查
- 负载均衡支持
- 服务实例动态感知

### 3. 命名空间隔离

- 开发环境: namespace=dev
- 生产环境: namespace=prod
- 支持多个业务分组

## 配置说明

### 开发环境 (application-nacos-dev.yml)

```yaml
spring:
  cloud:
    nacos:
      config:
        server-addr: localhost:8848
        namespace: dev
        group: DEFAULT_GROUP
        auto-refresh: true
      discovery:
        server-addr: localhost:8848
        namespace: dev
        register-enabled: true
```

### 生产环境 (application-nacos-prod.yml)

```yaml
spring:
  cloud:
    nacos:
      config:
        server-addr: ${NACOS_SERVER_ADDR:nacos:8848}
        namespace: ${NACOS_NAMESPACE:prod}
        auto-refresh: true
      discovery:
        server-addr: ${NACOS_SERVER_ADDR:nacos:8848}
        namespace: ${NACOS_NAMESPACE:prod}
```

## 启用Nacos

在 application.yml 中添加：

```yaml
spring:
  profiles:
    include: nacos-dev  # 或 nacos-prod
```

或通过环境变量：

```bash
export SPRING_PROFILES_INCLUDE=nacos-dev
```

## 在Nacos中创建配置

### 步骤1: 访问Nacos控制台

```
http://localhost:8848/nacos
```

默认用户名/密码: nacos/nacos

### 步骤2: 创建配置

1. 选择命名空间: dev
2. 点击"配置管理" -> "配置列表"
3. 点击"+"创建新配置

配置示例：

```yaml
# Data ID: application-custom.yml
# Group: DEFAULT_GROUP

# 数据库配置
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/langchain4j
    username: postgres
    password: password

# Redis配置
  redis:
    host: localhost
    port: 6379

# RabbitMQ配置
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

### 步骤3: 应用启动时自动加载

应用启动时会自动从Nacos加载配置。

## 动态配置更新

### 使用@RefreshScope注解

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

### 监听配置变化

```java
@Component
public class ConfigListener {

    @NacosConfigListener(dataId = "application-custom.yml", groupId = "DEFAULT_GROUP")
    public void onConfigChange(String config) {
        log.info("配置已更新: {}", config);
        // 处理配置变化
    }
}
```

## 服务发现

### 自动注册

应用启动时自动注册到Nacos：

```
服务名: langchain4j-scenarios
实例IP: 127.0.0.1
实例端口: 8081
命名空间: dev
```

### 服务调用

使用RestTemplate或WebClient调用其他服务：

```java
@Autowired
private RestTemplate restTemplate;

public void callOtherService() {
    // Nacos会自动解析服务名为实际地址
    String response = restTemplate.getForObject(
        "http://other-service/api/endpoint",
        String.class
    );
}
```

## Docker Compose集成

在docker-compose.yml中添加Nacos服务：

```yaml
nacos:
  image: nacos/nacos-server:v2.2.0
  ports:
    - "8848:8848"
    - "9848:9848"
  environment:
    MODE: standalone
    SPRING_DATASOURCE_PLATFORM: mysql
    MYSQL_SERVICE_HOST: mysql
    MYSQL_SERVICE_PORT: 3306
    MYSQL_SERVICE_DB_NAME: nacos
    MYSQL_SERVICE_USER: root
    MYSQL_SERVICE_PASSWORD: password
  depends_on:
    - mysql
```

## 最佳实践

### 1. 配置分层

- 应用级配置: application.yml
- 环境级配置: application-{env}.yml
- Nacos配置: 动态配置和敏感信息

### 2. 敏感信息管理

将敏感信息（密码、密钥）存储在Nacos中：

```yaml
# 在Nacos中创建
spring:
  datasource:
    password: ${DB_PASSWORD}  # 从环境变量读取

  rabbitmq:
    password: ${RABBITMQ_PASSWORD}
```

### 3. 配置版本管理

- 为重要配置创建版本
- 记录配置变更原因
- 支持快速回滚

### 4. 监控和告警

- 监控配置变更频率
- 告警异常配置更新
- 记录配置变更审计日志

## 故障排查

### 连接失败

检查Nacos服务是否运行：

```bash
docker ps | grep nacos
```

### 配置未加载

1. 检查命名空间是否正确
2. 验证Data ID和Group是否匹配
3. 查看应用日志中的错误信息

### 服务发现不工作

1. 确认服务已注册到Nacos
2. 检查命名空间隔离
3. 验证网络连接

## 与其他功能集成

### 与消息队列集成

在Nacos中管理RabbitMQ配置：

```yaml
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST}
    port: ${RABBITMQ_PORT}
    username: ${RABBITMQ_USERNAME}
    password: ${RABBITMQ_PASSWORD}
```

### 与数据库迁移集成

在Nacos中管理数据库连接：

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

  flyway:
    enabled: true
```

## 下一步

- 集成Nacos权限管理
- 配置灰度发布
- 实现配置加密
- 集成配置审计日志
