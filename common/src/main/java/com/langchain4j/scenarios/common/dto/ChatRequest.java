package com.langchain4j.scenarios.common.dto;

import com.langchain4j.scenarios.common.validation.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 对话请求 DTO - 客户端发送的聊天请求数据传输对象
 *
 * 职责说明：
 * - 封装客户端发送的对话请求参数
 * - 支持参数验证和约束
 * - 提供 API 文档和 Swagger 集成
 * - 实现请求参数的类型安全
 * - 支持多场景的对话请求
 *
 * 架构设计：
 * - 使用 DTO 模式隔离 API 层和业务层
 * - 使用 Jakarta Validation 进行参数验证
 * - 使用 Swagger 注解提供 API 文档
 * - 使用 Lombok 简化代码
 * - 支持灵活的对话参数配置
 *
 * 使用场景：
 * - 前端应用发送聊天消息
 * - 移动应用调用聊天 API
 * - 第三方应用集成
 * - API 测试和调试
 * - 多场景的对话请求
 *
 * 请求流程：
 * 1. 客户端构建 ChatRequest 对象
 * 2. 客户端发送 HTTP POST 请求
 * 3. Spring 反序列化 JSON 为 ChatRequest 对象
 * 4. 验证框架验证请求参数
 * 5. Controller 接收验证后的请求
 * 6. 业务逻辑处理请求
 * 7. 返回 ChatResponse 响应
 *
 * 参数验证：
 * - message：必填，长度 1-5000
 * - sessionId：可选，用于会话管理
 * - userId：可选，用于用户识别
 * - scenarioType：可选，用于场景区分
 *
 * 安全考虑：
 * - 验证消息长度防止 DoS 攻击
 * - 验证参数格式防止注入攻击
 * - 限制请求频率防止滥用
 * - 记录请求日志用于审计
 *
 * 扩展建议：
 * - 可以添加消息类型字段（文本、图片、文件等）
 * - 可以添加优先级字段
 * - 可以添加超时配置
 * - 可以添加自定义参数
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "对话请求")
public class ChatRequest {

    /**
     * 消息内容 - 用户发送的聊天消息
     *
     * 说明：
     * - 用户输入的问题或指令
     * - 必填字段，不能为空
     * - 长度限制：1-5000 字符
     * - 使用自定义 @NotBlank 验证器
     * - 使用 @Size 验证长度
     *
     * 验证规则：
     * - @NotBlank：不能为空或仅包含空格
     *   * 错误消息：\"消息内容不能为空\"
     *   * 适用于字符串类型
     *
     * - @Size(min = 1, max = 5000)：长度限制
     *   * 最小长度：1 字符
     *   * 最大长度：5000 字符
     *   * 错误消息：\"消息长度必须在1-5000之间\"
     *
     * 应用场景：
     * - 用户输入的问题
     *   * 示例：\"请分析这个 PDF 文档\"
     *   * 示例：\"生成一个 Java 类\"
     *
     * - 用户上传的文本
     *   * 示例：\"这是我的代码...\"
     *   * 示例：\"请翻译这段文本\"
     *
     * - 用户的指令
     *   * 示例：\"总结这篇文章\"
     *   * 示例：\"生成测试用例\"
     *
     * 使用示例 - 前端 JavaScript：
     * const request = {
     *     message: \"你好，请帮我分析这个数据\",
     *     sessionId: \"session-123\",
     *     userId: \"user-456\",
     *     scenarioType: \"data-analyst\"
     * };
     *
     * fetch('/api/chat', {
     *     method: 'POST',
     *     headers: { 'Content-Type': 'application/json' },
     *     body: JSON.stringify(request)
     * })
     * .then(response => response.json())
     * .then(data => console.log(data));
     *
     * 使用示例 - Java 客户端：
     * ChatRequest request = ChatRequest.builder()
     *     .message(\"你好\")
     *     .sessionId(\"session-123\")
     *     .userId(\"user-456\")
     *     .scenarioType(\"customer-service\")
     *     .build();
     *
     * 验证失败示例：
     * // 消息为空 - 验证失败
     * ChatRequest request1 = ChatRequest.builder()
     *     .message(\"\")
     *     .build();
     * // 错误：消息内容不能为空
     *
     * // 消息过长 - 验证失败
     * ChatRequest request2 = ChatRequest.builder()
     *     .message(\"a\".repeat(5001))
     *     .build();
     * // 错误：消息长度必须在1-5000之间
     *
     * 性能考虑：
     * - 消息长度限制防止过大的请求
     * - 建议在客户端也进行长度检查
     * - 可以实现消息分片处理超长消息
     *
     * 安全考虑：
     * - 验证消息内容防止注入攻击
     * - 考虑对消息进行内容过滤
     * - 记录消息用于审计
     */
    @NotBlank(message = "消息内容不能为空")
    @Size(min = 1, max = 5000, message = "消息长度必须在1-5000之间")
    @Schema(description = "消息内容", example = "你好")
    private String message;

    /**
     * 会话ID - 对话会话的唯一标识
     *
     * 说明：
     * - 用于关联同一会话的所有消息
     * - 可选字段，可以为 null
     * - 由客户端生成或由服务器返回
     * - 格式：UUID 或自定义格式
     * - 示例：\"session-123-abc-def\"
     *
     * 应用场景：
     * - 恢复完整的对话历史
     * - 分析单个会话的对话流程
     * - 支持会话级别的操作（导出、删除等）
     * - 实现会话的持久化和恢复
     *
     * 使用流程：
     * 1. 客户端首次发送请求时，不提供 sessionId
     * 2. 服务器生成新的 sessionId 并返回
     * 3. 客户端保存 sessionId
     * 4. 后续请求都使用相同的 sessionId
     * 5. 服务器根据 sessionId 恢复对话历史
     *
     * 使用示例：
     * // 首次请求 - 不提供 sessionId
     * ChatRequest request1 = ChatRequest.builder()
     *     .message(\"你好\")
     *     .build();
     *
     * // 响应包含 sessionId
     * ChatResponse response1 = chatService.chat(request1);
     * String sessionId = response1.getSessionId();
     *
     * // 后续请求 - 使用相同的 sessionId
     * ChatRequest request2 = ChatRequest.builder()
     *     .message(\"请继续\")
     *     .sessionId(sessionId)
     *     .build();
     *
     * 与数据库的关系：
     * - sessionId 用于查询 ConversationRecord
     * - 支持恢复完整的对话历史
     * - 支持多维度的数据分析
     */
    @Schema(description = "会话ID", example = "session-123")
    private String sessionId;

    /**
     * 用户ID - 发起对话的用户唯一标识
     *
     * 说明：
     * - 用于标识哪个用户发起的对话
     * - 可选字段，可以为 null
     * - 由认证系统提供
     * - 格式：UUID 或自定义格式
     * - 示例：\"user-123-abc-def\"
     *
     * 应用场景：
     * - 查询用户的所有对话历史
     * - 分析用户的使用行为
     * - 实现用户级别的权限控制
     * - 生成用户的个性化推荐
     * - 实现用户的配额管理
     *
     * 获取方式：
     * - 从 JWT token 中提取
     * - 从 Spring Security 的 Principal 中获取
     * - 从请求头中读取
     * - 从数据库中查询
     *
     * 使用示例：
     * @PostMapping(\"/chat\")
     * public ChatResponse chat(
     *     @RequestBody ChatRequest request,
     *     @AuthenticationPrincipal UserDetails userDetails) {
     *     String userId = userDetails.getUsername();
     *     request.setUserId(userId);
     *     return chatService.chat(request);
     * }
     *
     * 安全考虑：
     * - 验证 userId 的有效性
     * - 防止用户冒充其他用户
     * - 限制用户访问其他用户的数据
     * - 记录用户操作用于审计
     */
    @Schema(description = "用户ID", example = "user-123")
    private String userId;

    /**
     * 场景类型 - 对话所属的应用场景
     *
     * 说明：
     * - 标识对话属于哪个应用场景
     * - 可选字段，可以为 null
     * - 可能的值：
     *   * \"customer-service\" - 客服系统
     *   * \"document-analysis\" - 文档分析
     *   * \"code-assistant\" - 代码助手
     *   * \"data-analyst\" - 数据分析
     *   * \"content-creator\" - 内容创作
     *
     * 应用场景：
     * - 按场景统计对话数量
     * - 分析不同场景的使用情况
     * - 为不同场景应用不同的处理逻辑
     * - 生成场景级别的报表和分析
     * - 实现场景特定的 LLM 配置
     *
     * 使用示例：
     * ChatRequest request = ChatRequest.builder()
     *     .message(\"请分析这个数据\")
     *     .scenarioType(\"data-analyst\")
     *     .build();
     *
     * // 根据场景类型选择不同的处理逻辑
     * if (\"customer-service\".equals(request.getScenarioType())) {
     *     // 客服系统的处理逻辑
     * } else if (\"code-assistant\".equals(request.getScenarioType())) {
     *     // 代码助手的处理逻辑
     * }
     *
     * 场景特定的配置：
     * - 不同场景可以使用不同的 LLM 模型
     * - 不同场景可以有不同的系统提示词
     * - 不同场景可以有不同的参数配置
     * - 不同场景可以有不同的限流策略
     */
    @Schema(description = "场景类型", example = "customer-service")
    private String scenarioType;
}
