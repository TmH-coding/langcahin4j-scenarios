package com.langchain4j.scenarios.scenario6;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Scenario 6: Intelligent Agent 应用启动类
 *
 * 这是 Agent 智能体学习案例的主应用程序。
 * 启动后可以通过 HTTP 接口与 Agent 交互。
 *
 * 启动命令：
 * mvn spring-boot:run -pl scenario-6-intelligent-agent
 *
 * 访问地址：
 * - 与 Agent 对话: POST http://localhost:8086/api/agent/chat
 * - 查看可用工具: GET http://localhost:8086/api/agent/tools
 * - 查看使用示例: GET http://localhost:8086/api/agent/examples
 * - 健康检查: GET http://localhost:8086/api/agent/health
 */
@SpringBootApplication(scanBasePackages = {
        "com.langchain4j.scenarios.common",
        "com.langchain4j.scenarios.scenario6"
})
public class Scenario6Application {

    public static void main(String[] args) {
        SpringApplication.run(Scenario6Application.class, args);
    }
}
