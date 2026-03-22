package com.langchain4j.scenarios.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对话响应 DTO - 服务器返回的聊天响应数据传输对象
 *
 * 职责说明：
 * - 封装服务器返回的对话响应数据
 * - 提供统一的响应格式和结构
 * - 支持成功和错误响应的统一处理
 * - 提供 A​PI 文档和 Swagger 集成
 * - 实现响应数据的类型安全
 * - 支持多场景的对话响应
 *
 * 架构设计：
 * - 使用 DTO 模式隔离 A​PI 层和业务层
 * - 使用 Swagger 注解提供 A​PI 文档
 * - 使用 Lombok 简化代码
 * - 支持灵活的响应数据配置
 * - 支持成功和失败响应的统一处理
 *
 * 使用场景：
 * - 服务器返回聊天回复
 * - 返回对话处理结果
 * - 返回错误信息和状态
 * - 前端应用接收响应
 * - 移动应用处理响应
 * - 第三方应用集成
 * - A​PI 测试和调试
 *
 * 响应流程：
 * 1. 服务器处理 ChatRequest 请求
 * 2. 业务逻辑生成响应数据
 * 3. 构建 ChatResponse 对象
 * 4. 序列化为 JSON 格式
 * 5. 返回 HTTP 200 响应
 * 6. 前端接收并解析响应
 * 7. 更新用户界面
 *
 * 响应状态说明：
 * - success：请求成功，包含有效的响应消息
 * - error：请求失败，包含错误信息
 * - timeout：请求超时，LLM 响应超时
 * - rate_limit：触发限流，请求被拒绝
 * - validation_error：参数验证失败
 *
 * 安全考虑：
 * - 验证响应数据的完整性
 * - 不返回敏感的系统信息
 * - 限制错误消息的详细程度
 * - 记录响应日志用于审计
 * - 实现响应数据的加密传输
 *
 * 扩展建议：
 * - 可以添加响应元数据（如处理时间、模型版本等）
 * - 可以添加响应评分和反馈字段
 * - 可以添加响应的多语言支持
 * - 可以添加响应的流式处理支持
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "对话响应")
public class ChatResponse {

    /**
     * 响应消息 - AI 生成的聊天回复
     *
     * 说明：
     * - AI 对用户消息的回复内容
     * - 必填字段，不能为空
     * - 支持任意长度的文本
     * - 使用 UTF-8 编码
     * - 示例：\\\"根据您的问题，我的建议是...\\\"
     *
     * 内容类型：
     * - 直接回答：对用户问题的直接回答
     *   * 示例：\\\"这个问题的答案是...\\\"
     *   * 示例：\\\"根据数据分析，结果是...\\\"
     *
     * - 分析结果：对用户数据的分析结果
     *   * 示例：\\\"文档分析结果如下...\\\"
     *   * 示例：\\\"代码审查发现以下问题...\\\"
     *
     * - 生成内容：AI 生成的新内容
     *   * 示例：\\\"public class User { ... }\\\"
     *   * 示例：\\\"这是一篇关于...的文章\\\"
     *
     * - 错误信息：处理失败时的错误说明
     *   * 示例：\\\"处理您的请求时出错\\\"
     *   * 示例：\\\"无法访问所需的资源\\\"
     *
     * 应用场景：
     * - 客服系统：返回客服 AI 的回复
     * - 文档分析：返回文档分析结果
     * - 代码助手：返回生成的代码
     * - 数据分析：返回数据分析结果
     * - 内容创作：返回生成的内容
     *
     * 使用示例 - 前端 JavaScript：
     * fetch('/api/chat', {
     *     method: 'POST',
     *     headers: { 'Content-Type': 'application/json' },
     *     body: JSON.stringify({ message: \\\"你好\\\" })
     * })
     * .then(response => response.json())
     * .then(data => {
     *     console.log(\\\"AI 回复：\\\" + data.message);
     *     document.getElementById('response').textContent = data.message;
     * });
     *
     * 使用示例 - Java 客户端：
     * ChatResponse response = restTemplate.postForObject(
     *     \\\"http://localhost:8081/api/chat\\\",
     *     request,
     *     ChatResponse.class
     * );
     * System.out.println(\\\"响应消息：\\\" + response.getMessage());
     *
     * 使用示例 - 处理响应：
     * ChatResponse response = chatService.chat(request);
     * if (\\\"success\\\".equals(response.getStatus())) {
     *     String message = response.getMessage();
     *     // 处理成功响应
     *     displayMessage(message);
     * } else {
     *     String error = response.getErrorMessage();
     *     // 处理错误响应
     *     showError(error);
     * }
     *
     * 性能考虑：
     * - 消息长度可能很大，建议流式处理
     * - 建议在客户端实现消息分页显示
     * - 可以实现消息缓存提高性能
     *
     * 安全考虑：
     * - 验证消息内容的有效性
     * - 防止 XSS 攻击（前端需要转义）
     * - 不返回敏感的系统信息
     * - 记录消息用于审计
     */
    @Schema(description = "响应消息", example = "你好，有什么我可以帮助的吗？")
    private String message;

    /**
     * 会话ID - 对话会话的唯一标识
     *
     * 说明：
     * - 用于关联同一会话的所有消息
     * - 由服务器生成或返回
     * - 格式：UUID 或自定义格式
     * - 示例：\\\"session-123-abc-def\\\"
     * - 非空字段，必须设置
     *
     * 应用场景：
     * - 恢复完整的对话历史
     * - 分析单个会话的对话流程
     * - 支持会话级别的操作（导出、删除等）
     * - 实现会话的持久化和恢复
     * - 支持多标签页的会话管理
     *
     * 使用流程：
     * 1. 首次请求时，服务器生成新的 sessionId
     * 2. 服务器在响应中返回 sessionId
     * 3. 客户端保存 sessionId
     * 4. 后续请求都使用相同的 sessionId
     * 5. 服务器根据 sessionId 恢复对话历史
     *
     * 使用示例：
     * // 首次请求 - 不提供 sessionId
     * ChatRequest request1 = ChatRequest.builder()
     *     .message(\\\"你好\\\")
     *     .build();
     *
     * // 响应包含 sessionId
     * ChatResponse response1 = chatService.chat(request1);
     * String sessionId = response1.getSessionId();
     * localStorage.setItem(\\\"sessionId\\\", sessionId);
     *
     * // 后续请求 - 使用相同的 sessionId
     * ChatRequest request2 = ChatRequest.builder()
     *     .message(\\\"请继续\\\")
     *     .sessionId(sessionId)
     *     .build();
     *
     * 前端存储示例：
     * // 保存 sessionId 到 localStorage
     * localStorage.setItem(\\\"sessionId\\\", response.getSessionId());
     *
     * // 从 localStorage 读取 sessionId
     * const sessionId = localStorage.getItem(\\\"sessionId\\\");
     * if (sessionId) {
     *     request.sessionId = sessionId;
     * }
     *
     * 与数据库的关系：
     * - sessionId 用于查询 ConversationRecord
     * - 支持恢复完整的对话历史
     * - 支持多维度的数据分析
     * - 支持会话级别的统计
     */
    @Schema(description = "会话ID", example = "session-123")
    private String sessionId;

    /**
     * 响应时间戳 - 响应生成的时间戳（毫秒）
     *
     * 说明：
     * - 记录响应生成的时间
     * - 单位：毫秒（自 1970-01-01 00:00:00 UTC 以来的毫秒数）
     * - 类型：long（64 位整数）
     * - 通常由服务器在生成响应时设置
     * - 示例：1711000000000
     *
     * 应用场景：
     * - 计算请求响应时间
     * - 按时间顺序排序消息
     * - 实现消息的时间序列分析
     * - 支持对话的时间线展示
     * - 实现消息的过期清理
     * - 性能监控和分析
     *
     * 时间戳获取方式：
     * - System.currentTimeMillis()：获取当前时间戳
     * - System.nanoTime()：获取纳秒精度时间（用于性能测量）
     * - LocalDateTime.now().toInstant().toEpochMilli()：从 LocalDateTime 转换
     *
     * 使用示例：
     * ChatResponse response = chatService.chat(request);
     * long responseTime = response.getTimestamp();
     * System.out.println(\\\"响应时间戳：\\\" + responseTime);
     *
     * // 计算请求响应耗时
     * long requestTime = System.currentTimeMillis();
     * long duration = requestTime - response.getTimestamp();
     * System.out.println(\\\"处理耗时：\\\" + duration + \\\" 毫秒\\\");
     *
     * // 前端计算响应时间
     * const startTime = Date.now();
     * fetch('/api/chat', { ... })
     *     .then(response => response.json())
     *     .then(data => {
     *         const endTime = Date.now();
     *         const duration = endTime - startTime;
     *         console.log(\\\"响应耗时：\\\" + duration + \\\" 毫秒\\\");
     *     });
     *
     * 性能考虑：
     * - 时间戳比较速度快
     * - 支持高效的时间范围查询
     * - 可以用于实现消息的过期清理
     * - 可以用于性能监控和分析
     *
     * 与数据库的转换：
     * - 时间戳可以转换为 LocalDateTime
     * - LocalDateTime dateTime = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime();
     * - 用于持久化到数据库
     */
    @Schema(description = "响应时间戳", example = "1234567890")
    private long timestamp;

    /**
     * 响应状态 - 对话处理的结果状态
     *
     * 说明：
     * - 标识对话处理的结果状态
     * - 非空字段，必须设置
     * - 可能的值：success, error, timeout, rate_limit, validation_error
     * - 用于前端判断是否处理成功
     * - 示例：\\\"success\\\"
     *
     * 状态说明：
     * - \\\"success\\\"：请求成功
     *   * 对话处理成功
     *   * message 字段包含有效的响应
     *   * errorMessage 字段为空
     *   * HTTP 状态码：200
     *
     * - \\\"error\\\"：请求失败
     *   * 对话处理出错
     *   * message 字段可能为空
     *   * errorMessage 字段包含错误说明
     *   * HTTP 状态码：500
     *
     * - \\\"timeout\\\"：请求超时
     *   * LLM 响应超时
     *   * message 字段为空
     *   * errorMessage 字段包含超时说明
     *   * HTTP 状态码：504
     *
     * - \\\"rate_limit\\\"：触发限流
     *   * 请求频率过高
     *   * message 字段为空
     *   * errorMessage 字段包含限流说明
     *   * HTTP 状态码：429
     *
     * - \\\"validation_error\\\"：参数验证失败
     *   * 请求参数不合法
     *   * message 字段为空
     *   * errorMessage 字段包含验证错误
     *   * HTTP 状态码：400
     *
     * 应用场景：
     * - 前端判断是否处理成功
     * - 前端显示相应的错误提示
     * - 前端实现重试逻辑
     * - 后端日志记录和监控
     * - 性能分析和统计
     *
     * 使用示例：
     * ChatResponse response = chatService.chat(request);
     * if (\\\"success\\\".equals(response.getStatus())) {
     *     // 处理成功响应
     *     String message = response.getMessage();
     *     displayMessage(message);
     * } else if (\\\"error\\\".equals(response.getStatus())) {
     *     // 处理错误响应
     *     String error = response.getErrorMessage();
     *     showError(\\\"处理失败：\\\" + error);
     * } else if (\\\"timeout\\\".equals(response.getStatus())) {
     *     // 处理超时响应
     *     showError(\\\"请求超时，请重试\\\");
     * } else if (\\\"rate_limit\\\".equals(response.getStatus())) {
     *     // 处理限流响应
     *     showError(\\\"请求过于频繁，请稍后再试\\\");
     * } else if (\\\"validation_error\\\".equals(response.getStatus())) {
     *     // 处理验证错误
     *     showError(\\\"请求参数不合法：\\\" + response.getErrorMessage());
     * }
     *
     * 前端处理示例：
     * fetch('/api/chat', { ... })
     *     .then(response => response.json())
     *     .then(data => {
     *         switch(data.status) {
     *             case 'success':
     *                 displayMessage(data.message);
     *                 break;
     *             case 'error':
     *                 showError(\\\"处理失败：\\\" + data.errorMessage);
     *                 break;
     *             case 'timeout':
     *                 showError(\\\"请求超时\\\");
     *                 break;
     *             case 'rate_limit':
     *                 showError(\\\"请求过于频繁\\\");
     *                 break;
     *             default:
     *                 showError(\\\"未知错误\\\");
     *         }
     *     });
     *
     * 性能考虑：
     * - 状态字段用于快速判断
     * - 建议使用枚举而不是字符串
     * - 可以实现状态缓存
     *
     * 安全考虑：
     * - 不返回敏感的系统信息
     * - 限制错误消息的详细程度
     * - 记录状态用于审计
     */
    @Schema(description = "响应状态", example = "success")
    private String status;

    /**
     * 错误信息 - 处理失败时的错误说明
     *
     * 说明：
     * - 当处理失败时，包含错误说明
     * - 可选字段，可以为 null 或空字符串
     * - 仅在 status 不为 success 时有意义
     * - 用于前端显示错误提示
     * - 示例：\\\"消息内容不能为空\\\"
     *
     * 错误类型：
     * - 验证错误：参数验证失败
     *   * 示例：\\\"消息内容不能为空\\\"
     *   * 示例：\\\"消息长度必须在1-5000之间\\\"
     *
     * - 业务错误：业务逻辑处理失败
     *   * 示例：\\\"用户不存在\\\"
     *   * 示例：\\\"会话已过期\\\"
     *
     * - 系统错误：系统内部错误
     *   * 示例：\\\"系统内部错误\\\"
     *   * 示例：\\\"数据库连接失败\\\"
     *
     * - 外部服务错误：调用外部服务失败
     *   * 示例：\\\"LLM 服务不可用\\\"
     *   * 示例：\\\"API 调用失败\\\"
     *
     * - 限流错误：触发限流
     *   * 示例：\\\"请求过于频繁，请稍后再试\\\"
     *   * 示例：\\\"超过每日请求限制\\\"
     *
     * - 超时错误：请求超时
     *   * 示例：\\\"请求超时，请重试\\\"
     *   * 示例：\\\"LLM 响应超时\\\"
     *
     * 应用场景：
     * - 前端显示错误提示
     * - 用户了解失败原因
     * - 日志记录和监控
     * - 错误分析和统计
     * - 用户支持和调试
     *
     * 使用示例：
     * ChatResponse response = chatService.chat(request);
     * if (!\\\"success\\\".equals(response.getStatus())) {
     *     String errorMessage = response.getErrorMessage();
     *     if (errorMessage != null && !errorMessage.isEmpty()) {
     *         System.out.println(\\\"错误：\\\" + errorMessage);
     *         showErrorToUser(errorMessage);
     *     }
     * }
     *
     * 前端处理示例：
     * fetch('/api/chat', { ... })
     *     .then(response => response.json())
     *     .then(data => {
     *         if (data.status !== 'success') {
     *             const errorMsg = data.errorMessage || '未知错误';
     *             alert(\\\"处理失败：\\\" + errorMsg);
     *         }
     *     });
     *
     * 错误处理最佳实践：
     * - 始终检查 status 字段
     * - 根据 status 显示相应的错误提示
     * - 不要直接显示系统错误信息
     * - 记录错误用于调试
     * - 实现重试逻辑
     *
     * 安全考虑：
     * - 不返回敏感的系统信息
     * - 限制错误消息的详细程度
     * - 不暴露内部实现细节
     * - 记录错误用于审计
     * - 实现错误日志脱敏
     */
    @Schema(description = "错误信息", example = "")
    private String errorMessage;
}
