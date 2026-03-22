package com.langchain4j.scenarios.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger 配置 - API 文档自动生成
 *
 * 职责说明：
 * - 配置 OpenAPI 3.0 规范的 API 文档
 * - 提供 Swagger UI 用户界面
 * - 支持在线 API 测试和调试
 * - 生成 API 文档的 JSON/YAML 格式
 *
 * 架构设计：
 * - 使用 SpringDoc OpenAPI 库自动扫描 Spring 注解
 * - 使用 OpenAPI 对象配置 API 元数据
 * - 支持自定义 API 信息和联系方式
 * - 集成 Swagger UI 提供交互式文档
 *
 * 使用场景：
 * - 前端开发者查看 API 文档
 * - 后端开发者测试 API 端点
 * - 自动生成 API 文档，减少手工维护
 * - 第三方开发者集成 API
 * - API 版本管理和演进
 *
 * OpenAPI 工作流程：
 * 1. 应用启动时扫描所有 @RestController 和 @RequestMapping 注解
 * 2. 自动生成 OpenAPI 规范的 JSON 文档
 * 3. Swagger UI 读取 JSON 文档并渲染交互式界面
 * 4. 用户可以在 UI 中查看、测试 API 端点
 *
 * 访问方式：
 * - Swagger UI: http://localhost:8081/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8081/v3/api-docs
 * - OpenAPI YAML: http://localhost:8081/v3/api-docs.yaml
 *
 * 安全考虑：
 * - 生产环境应限制 API 文档的访问权限
 * - 不要在文档中暴露敏感信息（密钥、内部 IP 等）
 * - 可以通过配置隐藏某些端点的文档
 */
@Configuration
public class SwaggerConfig {

    /**
     * 创建自定义 OpenAPI 配置
     *
     * 功能：
     * - 定义 API 文档的基本信息
     * - 配置 API 标题、版本、描述
     * - 设置联系方式和文档链接
     * - 自定义 API 文档的外观和元数据
     *
     * OpenAPI 配置参数详解：
     *
     * title: "LangChain4j 多场景学习平台 API"
     * - API 文档的标题
     * - 显示在 Swagger UI 的顶部
     * - 用于标识 API 的名称和用途
     * - 建议：简洁明了，能够快速识别 API 的功能
     *
     * version: "1.0.0"
     * - API 的版本号
     * - 遵循语义化版本规范（Semantic Versioning）
     * - 格式：MAJOR.MINOR.PATCH
     *   * MAJOR: 不兼容的 API 变更
     *   * MINOR: 向后兼容的功能添加
     *   * PATCH: 向后兼容的 bug 修复
     * - 示例：
     *   * 1.0.0: 初始版本
     *   * 1.1.0: 添加新端点
     *   * 1.1.1: 修复 bug
     *   * 2.0.0: 重大变更
     *
     * description: "完整的LangChain4j学习平台，包含5个真实场景"
     * - API 的详细描述
     * - 说明 API 的功能和用途
     * - 可以包含 Markdown 格式的文本
     * - 用户在 Swagger UI 中可以看到此描述
     *
     * contact.name: "LangChain4j Team"
     * - 联系人或团队名称
     * - 用户遇到问题时可以联系此人/团队
     *
     * contact.url: "https://github.com/langchain4j/langchain4j"
     * - 联系方式的 URL
     * - 可以是官网、GitHub、邮件等
     * - 用户可以点击此链接获取帮助
     *
     * 使用示例：
     * // 访问 Swagger UI
     * 浏览器打开：http://localhost:8081/swagger-ui.html
     *
     * // 获取 OpenAPI JSON 文档
     * curl http://localhost:8081/v3/api-docs
     *
     * // 获取 OpenAPI YAML 文档
     * curl http://localhost:8081/v3/api-docs.yaml
     *
     * // 在 Swagger UI 中测试 API
     * 1. 打开 Swagger UI
     * 2. 找到要测试的端点
     * 3. 点击 "Try it out" 按钮
     * 4. 输入请求参数
     * 5. 点击 "Execute" 按钮
     * 6. 查看响应结果
     *
     * Swagger UI 功能：
     * - 查看所有 API 端点
     * - 查看请求/响应格式
     * - 查看参数说明和类型
     * - 在线测试 API
     * - 查看响应示例
     * - 下载 OpenAPI 规范文件
     *
     * 与 @Operation 注解的配合：
     * @RestController
     * @RequestMapping(\"/api/chat\")
     * public class ChatController {
     *     @PostMapping(\"/send\")
     *     @Operation(summary = \"发送聊天消息\", description = \"用户发送消息给 AI\")
     *     public ChatResponse sendMessage(@RequestBody ChatRequest request) {
     *         // 实现逻辑
     *     }
     * }
     *
     * 与 @Parameter 注解的配合：
     * @GetMapping(\"/{userId}\")
     * @Operation(summary = \"获取用户信息\")
     * public User getUser(
     *     @Parameter(description = \"用户 ID\", required = true)
     *     @PathVariable String userId
     * ) {
     *     // 实现逻辑
     * }
     *
     * 与 @Schema 注解的配合：
     * public class ChatRequest {
     *     @Schema(description = \"用户消息内容\", example = \"你好\")
     *     private String message;
     * }
     *
     * @return OpenAPI 配置对象，包含 API 文档的元数据
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LangChain4j 多场景学习平台 API")
                        .version("1.0.0")
                        .description("完整的LangChain4j学习平台，包含5个真实场景")
                        .contact(new Contact()
                                .name("LangChain4j Team")
                                .url("https://github.com/langchain4j/langchain4j")));
    }
}
