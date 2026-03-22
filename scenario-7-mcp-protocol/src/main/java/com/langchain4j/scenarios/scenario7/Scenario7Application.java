package com.langchain4j.scenarios.scenario7;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Scenario 7: MCP Protocol 应用启动类
 *
 * 这是 MCP（Model Context Protocol）学习案例的主应用程序。
 * MCP 是 Anthropic 提出的标准化工具接口协议，用于规范 LLM 与外部工具的交互。
 *
 * 启动命令：
 * mvn spring-boot:run -pl scenario-7-mcp-protocol
 *
 * 访问地址：
 * - MCP 服务器: http://localhost:8087/api/mcp/server
 * - 工具列表: GET http://localhost:8087/api/mcp/tools
 * - 执行工具: POST http://localhost:8087/api/mcp/execute
 * - 学习指南: GET http://localhost:8087/api/mcp/guide
 * - 健康检查: GET http://localhost:8087/api/mcp/health
 */
@SpringBootApplication(scanBasePackages = {
        "com.langchain4j.scenarios.common",
        "com.langchain4j.scenarios.scenario7"
})
public class Scenario7Application {

    public static void main(String[] args) {
        SpringApplication.run(Scenario7Application.class, args);
    }
}
