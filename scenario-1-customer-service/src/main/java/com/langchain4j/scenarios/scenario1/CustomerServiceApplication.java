package com.langchain4j.scenarios.scenario1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 客服系统应用启动类 - Spring Boot 应用入口点
 *
 * 职责说明：
 * - 作为 Spring Boot 应用的主入口点
 * - 配置组件扫描范围包括 common 和 scenario1 包
 * - 启动内嵌 Tomcat 服务器
 * - 初始化 Spring 应用上下文
 * - 加载所有配置和依赖注入
 *
 * 架构设计：
 * - 使用 @SpringBootApplication 注解启用 Spring Boot 自动配置
 * - 使用 @ComponentScan 指定要扫描的包范围
 * - 包括 common 模块的所有共享组件
 * - 包括 scenario1 模块的所有业务组件
 * - 支持多模块项目的组件发现
 *
 * 使用场景：
 * - 启动客服系统应用
 * - 初始化所有 Spring Bean
 * - 加载配置文件和属性
 * - 启动 Web 服务器
 * - 支持开发、测试、生产环境
 *
 * 启动方式：
 * 1. IDE 中直接运行 main 方法
 * 2. 命令行：mvn spring-boot:run
 * 3. 编译后运行：java -jar scenario-1-customer-service.jar
 * 4. Docker 容器中运行
 *
 * 配置说明：
 * - @SpringBootApplication：
 *   * 启用 @Configuration 自动配置
 *   * 启用 @ComponentScan 组件扫描
 *   * 启用 @EnableAutoConfiguration 自动配置
 *
 * - @ComponentScan：
 *   * basePackages 指定扫描的包
 *   * 包括 com.langchain4j.scenarios.common（共享模块）
 *   * 包括 com.langchain4j.scenarios.scenario1（业务模块）
 *   * 确保所有 @Component、@Service、@Controller 等被发现
 *
 * 启动流程：
 * 1. JVM 加载 CustomerServiceApplication 类
 * 2. 执行 main 方法
 * 3. 调用 SpringApplication.run()
 * 4. Spring 初始化应用上下文
 * 5. 扫描指定包中的所有组件
 * 6. 创建 Bean 并进行依赖注入
 * 7. 启动内嵌 Tomcat 服务器
 * 8. 监听指定端口（默认 8080，配置为 8081）
 * 9. 应用启动完成，接受请求
 *
 * 服务端口：8081
 * 配置文件：application.yml 或 application.properties
 *
 * 性能考虑：
 * - 启动时间通常 < 10 秒
 * - 内存占用通常 < 500MB
 * - 支持快速启动和热重载
 * - 可以配置启动参数优化性能
 *
 * 安全考虑：
 * - 不要在代码中硬编码敏感信息
 * - 使用环境变量或配置文件管理密钥
 * - 启用 Spring Security 进行身份验证
 * - 实现请求日志和审计
 * - 配置 HTTPS 和 SSL/TLS
 *
 * 可靠性考虑：
 * - 实现优雅关闭机制
 * - 处理启动异常
 * - 支持健康检查
 * - 实现监控和告警
 *
 * 扩展建议：
 * - 可以添加启动事件监听器
 * - 可以实现自定义初始化逻辑
 * - 可以添加启动参数处理
 * - 可以实现应用配置验证
 * - 可以支持多环境配置
 * - 可以添加启动日志记录
 */
@SpringBootApplication
@ComponentScan(basePackages = {
    "com.langchain4j.scenarios.common",
    "com.langchain4j.scenarios.scenario1"
})
public class CustomerServiceApplication {

    /**
     * 应用主入口
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(CustomerServiceApplication.class, args);
    }
}
