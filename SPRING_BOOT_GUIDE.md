# Spring Boot 集成指南

## 项目配置

### 1. 主 pom.xml 配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.langchain4j.scenarios</groupId>
    <artifactId>langchain4j-scenarios</artifactId>
    <version>1.0.0</version>
    <packaging>pom</packaging>

    <name>LangChain4j Multi-Scenario Learning Platform</name>
    <description>Complete learning platform for LangChain4j with 5 real-world scenarios</description>

    <modules>
        <module>common</module>
        <module>scenario-1-customer-service</module>
        <module>scenario-2-document-analysis</module>
        <module>scenario-3-code-assistant</module>
        <module>scenario-4-data-analyst</module>
        <module>scenario-5-content-creator</module>
    </modules>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <spring-boot.version>3.2.0</spring-boot.version>
        <langchain4j.version>0.31.0</langchain4j.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <!-- Spring Boot BOM -->
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <!-- LangChain4j -->
            <dependency>
                <groupId>dev.langchain4j</groupId>
                <artifactId>langchain4j-core</artifactId>
                <version>${langchain4j.version}</version>
            </dependency>
            <dependency>
                <groupId>dev.langchain4j</groupId>
                <artifactId>langchain4j-open-ai</artifactId>
                <version>${langchain4j.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <version>${spring-boot.version}</version>
            </plugin>
        </plugins>
    </build>
</project>
```

### 2. 场景模块 pom.xml 配置

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.langchain4j.scenarios</groupId>
        <artifactId>langchain4j-scenarios</artifactId>
        <version>1.0.0</version>
    </parent>

    <artifactId>scenario-1-customer-service</artifactId>
    <name>Scenario 1: Customer Service</name>

    <dependencies>
        <!-- 内部依赖 -->
        <dependency>
            <groupId>com.langchain4j.scenarios</groupId>
            <artifactId>common</artifactId>
            <version>1.0.0</version>
        </dependency>

        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <!-- LangChain4j -->
        <dependency>
            <groupId>dev.langchain4j</groupId>
            <artifactId>langchain4j-core</artifactId>
        </dependency>
        <dependency>
            <groupId>dev.langchain4j</groupId>
            <artifactId>langchain4j-open-ai</artifactId>
        </dependency>

        <!-- Utilities -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.30</version>
            <scope>provided</scope>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

## Spring Boot 配置文件

### 1. application.yml 配置

```yaml
# 应用配置
spring:
  application:
    name: langchain4j-scenarios

  # JPA 配置
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MySQL8Dialect

  # 数据源配置
  datasource:
    url: jdbc:mysql://localhost:3306/langchain4j?useSSL=false&serverTimezone=UTC
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver

  # Jackson 配置
  jackson:
    serialization:
      write-dates-as-timestamps: false
    default-property-inclusion: non_null

# 服务器配置
server:
  port: 8080
  servlet:
    context-path: /
  compression:
    enabled: true
    min-response-size: 1024

# 日志配置
logging:
  level:
    root: INFO
    com.langchain4j.scenarios: DEBUG
    org.springframework.web: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/application.log
    max-size: 10MB
    max-history: 10

# LLM 配置
llm:
  provider: openai
  openai:
    api-key: ${OPENAI_API_KEY}
    model: gpt-4-turbo-preview
    temperature: 0.7
    top-p: 1.0
    timeout: 30s

# 应用特定配置
app:
  # 客服系统配置
  customer-service:
    max-sessions: 1000
    session-timeout: 1800000  # 30分钟
    max-messages: 20

  # 文档分析配置
  document-analysis:
    max-segment-size: 500
    overlap-size: 50
    max-documents: 100

  # 代码助手配置
  code-assistant:
    supported-languages:
      - java
      - python
      - javascript
      - go
      - rust

  # 数据分析配置
  data-analyst:
    max-query-size: 10000
    query-timeout: 60s

  # 内容创作配置
  content-creator:
    max-word-count: 5000
    supported-languages:
      - English
      - Chinese
      - Spanish
      - French
      - German
```

### 2. application-dev.yml 开发环境配置

```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true

  datasource:
    url: jdbc:mysql://localhost:3306/langchain4j_dev?useSSL=false&serverTimezone=UTC
    username: root
    password: root

server:
  port: 8080

logging:
  level:
    root: DEBUG
    com.langchain4j.scenarios: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

### 3. application-prod.yml 生产环境配置

```yaml
spring:
  jpa:
    show-sql: false
    hibernate:
      ddl-auto: validate

  datasource:
    url: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=true&serverTimezone=UTC
    username: ${DB_USER}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

server:
  port: 8080
  compression:
    enabled: true
    min-response-size: 1024

logging:
  level:
    root: WARN
    com.langchain4j.scenarios: INFO
  file:
    name: /var/log/langchain4j/application.log
    max-size: 100MB
    max-history: 30
```

## Spring Boot 启动类最佳实践

### 1. 主启动类

```java
package com.langchain4j.scenarios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * LangChain4j 多场景学习平台主启动类
 *
 * 功能说明：
 * - 启动Spring Boot应用
 * - 启用异步处理
 * - 启用定时任务
 * - 配置组件扫描
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@ComponentScan(basePackages = {
    "com.langchain4j.scenarios.common",
    "com.langchain4j.scenarios.scenario1",
    "com.langchain4j.scenarios.scenario2",
    "com.langchain4j.scenarios.scenario3",
    "com.langchain4j.scenarios.scenario4",
    "com.langchain4j.scenarios.scenario5"
})
public class LangChain4jScenariosApplication {

    public static void main(String[] args) {
        SpringApplication.run(LangChain4jScenariosApplication.class, args);
    }
}
```

### 2. 全局异常处理

```java
package com.langchain4j.scenarios.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

/**
 * 全局异常处理器
 *
 * 功能说明：
 * - 统一处理所有异常
 * - 返回结构化的错误响应
 * - 记录异常日志
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex,
            WebRequest request) {
        log.warn("业务异常: {}", ex.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .code(ex.getCode())
                .message(ex.getMessage())
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(
            Exception ex,
            WebRequest request) {
        log.error("系统异常", ex);

        ErrorResponse error = ErrorResponse.builder()
                .code("SYSTEM_ERROR")
                .message("系统内部错误")
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
```

### 3. 错误响应模型

```java
package com.langchain4j.scenarios.common.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 错误响应模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    /** 错误代码 */
    private String code;

    /** 错误信息 */
    private String message;

    /** 时间戳 */
    private long timestamp;
}
```

### 4. 业务异常类

```java
package com.langchain4j.scenarios.common.exception;

/**
 * 业务异常
 */
public class BusinessException extends RuntimeException {
    private String code;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
```

## Spring Boot 拦截器和过滤器

### 1. 请求日志拦截器

```java
package com.langchain4j.scenarios.common.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 请求日志拦截器
 *
 * 功能说明：
 * - 记录所有HTTP请求
 * - 记录请求耗时
 * - 记录响应状态
 */
@Slf4j
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final String START_TIME = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long startTime = System.currentTimeMillis();
        request.setAttribute(START_TIME, startTime);

        log.info("请求开始 - 方法: {}, 路径: {}, IP: {}",
                request.getMethod(),
                request.getRequestURI(),
                getClientIp(request));

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        long startTime = (long) request.getAttribute(START_TIME);
        long duration = System.currentTimeMillis() - startTime;

        log.info("请求完成 - 路径: {}, 状态: {}, 耗时: {}ms",
                request.getRequestURI(),
                response.getStatus(),
                duration);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
```

### 2. 拦截器配置

```java
package com.langchain4j.scenarios.common.config;

import com.langchain4j.scenarios.common.interceptor.RequestLoggingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private RequestLoggingInterceptor requestLoggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestLoggingInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/health");
    }
}
```

## Spring Boot 启动和运行

### 1. 启动命令

```bash
# 开发环境启动
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# 生产环境启动
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=prod"

# 打包为JAR
mvn clean package

# 运行JAR
java -jar target/scenario-1-customer-service-1.0.0.jar --spring.profiles.active=prod
```

### 2. 环境变量配置

```bash
# Linux/Mac
export OPENAI_API_KEY=sk-xxx
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=langchain4j
export DB_USER=root
export DB_PASSWORD=password

# Windows
set OPENAI_API_KEY=sk-xxx
set DB_HOST=localhost
set DB_PORT=3306
set DB_NAME=langchain4j
set DB_USER=root
set DB_PASSWORD=password
```

## Spring Boot 最佳实践

### 1. 依赖注入

```java
// ✅ 推荐：使用构造函数注入
@Service
@RequiredArgsConstructor
public class CustomerServiceAI {
    private final ChatLanguageModel chatModel;
    private final ConversationMemoryUtil memory;
}

// ❌ 不推荐：使用字段注入
@Service
public class CustomerServiceAI {
    @Autowired
    private ChatLanguageModel chatModel;
}
```

### 2. 配置属性

```java
// ✅ 推荐：使用 @ConfigurationProperties
@Configuration
@ConfigurationProperties(prefix = "app.customer-service")
@Data
public class CustomerServiceProperties {
    private int maxSessions;
    private long sessionTimeout;
    private int maxMessages;
}

// 在Service中使用
@Service
@RequiredArgsConstructor
public class CustomerServiceAI {
    private final CustomerServiceProperties properties;
}
```

### 3. 异步处理

```java
// ✅ 推荐：使用 @Async 处理长时间操作
@Service
public class DocumentAnalysisService {
    @Async
    public CompletableFuture<String> analyzeDocumentAsync(String documentId) {
        // 长时间操作
        String result = analyzeDocument(documentId);
        return CompletableFuture.completedFuture(result);
    }
}

// 在Controller中使用
@PostMapping("/analyze-async")
public CompletableFuture<String> analyzeAsync(@RequestParam String documentId) {
    return documentAnalysisService.analyzeDocumentAsync(documentId);
}
```

### 4. 缓存

```java
// ✅ 推荐：使用 @Cacheable 缓存结果
@Service
public class CodeAssistantService {
    @Cacheable(value = "codeReviews", key = "#code.hashCode()")
    public String reviewCode(String code, String language) {
        // 代码审查逻辑
        return result;
    }
}
```

## 启动检查清单

- [ ] 配置 application.yml
- [ ] 设置环境变量 OPENAI_API_KEY
- [ ] 配置数据库连接
- [ ] 运行 `mvn clean install`
- [ ] 启动应用 `mvn spring-boot:run`
- [ ] 验证应用启动成功（查看日志）
- [ ] 测试 API 端点
- [ ] 检查日志文件

## 常见问题

**Q: 如何切换环境？**
A: 使用 `--spring.profiles.active=dev/prod` 参数

**Q: 如何配置多个数据源？**
A: 创建多个 DataSource Bean，使用 @Primary 标注主数据源

**Q: 如何实现请求限流？**
A: 使用 Spring Cloud Alibaba Sentinel 或 Bucket4j

**Q: 如何监控应用性能？**
A: 集成 Spring Boot Actuator 和 Micrometer
