package com.langchain4j.scenarios.common.util;

import org.springframework.stereotype.Component;
import java.util.UUID;

/**
 * 请求 ID 生成器 - 分布式追踪和请求关联的唯一标识生成
 *
 * 职责说明：
 * - 为每个请求生成唯一 ID
 * - 用于请求追踪和日志关联
 * - 支持分布式追踪和链路追踪
 * - 实现请求的端到端追踪
 * - 支持多个微服务间的请求关联
 * - 提供灵活的 ID 生成策略
 *
 * 架构设计：
 * - 使用 UUID 生成唯一标识
 * - 支持带前缀的 ID 生成
 * - Spring Component 注解标记为 Bean
 * - 易于注入和使用
 * - 线程安全的实现
 *
 * 使用场景：
 * - HTTP 请求追踪
 * - 异步任务追踪
 * - 消息队列追踪
 * - 分布式事务追踪
 * - 日志关联和聚合
 * - 性能监控和分析
 * - 错误追踪和调试
 *
 * 请求追踪工作流程：
 * 1. 请求到达时生成唯一 ID
 * 2. 将 ID 添加到请求头或上下文
 * 3. 在所有日志中记录 ID
 * 4. 传递给下游服务
 * 5. 在响应中返回 ID
 * 6. 用于日志聚合和分析
 *
 * 性能考虑：
 * - UUID 生成速度快
 * - 时间复杂度：O(1)
 * - 空间复杂度：O(1)
 * - 支持高并发场景
 * - 建议缓存生成的 ID
 *
 * 扩展建议：
 * - 可以添加自定义 ID 格式
 * - 可以实现 ID 池化
 * - 可以添加 ID 验证
 * - 可以实现 ID 压缩
 * - 可以支持多种 ID 生成策略
 */
@Component
public class RequestIdGenerator {

    /**
     * 生成请求 ID
     *
     * 功能：
     * - 生成唯一的请求 ID
     * - 使用 UUID 算法
     * - 移除 UUID 中的连字符
     * - 返回 32 位的十六进制字符串
     *
     * 返回值：
     * - String：唯一的请求 ID
     * - 格式：32 位十六进制字符串（无连字符）
     * - 示例：\\\"550e8400e29b41d4a716446655440000\\\"
     * - 长度：32 字符
     * - 唯一性：极高（UUID v4 的唯一性保证）
     *
     * 使用示例：
     * @Autowired
     * private RequestIdGenerator requestIdGenerator;
     *
     * @GetMapping(\\\"/api/users\\\")
     * public ResponseEntity<?> getUsers() {
     *     String requestId = requestIdGenerator.generateRequestId();
     *     logger.info(\\\"Request ID: {}\\\", requestId);
     *     // 处理请求
     *     return ResponseEntity.ok(\\\"...\\\");
     * }
     *
     * 日志集成示例：
     * String requestId = requestIdGenerator.generateRequestId();
     * MDC.put(\\\"requestId\\\", requestId);  // 放入 MDC 上下文
     * logger.info(\\\"Processing request\\\");  // 日志会自动包含 requestId
     * MDC.remove(\\\"requestId\\\");  // 清理 MDC
     *
     * 分布式追踪示例：
     * String requestId = requestIdGenerator.generateRequestId();
     * // 在请求头中传递
     * headers.put(\\\"X-Request-ID\\\", requestId);
     * // 调用下游服务
     * restTemplate.getForObject(url, String.class);
     *
     * 性能考虑：
     * - 时间复杂度：O(1)
     * - UUID 生成速度快
     * - 字符串替换速度快
     * - 支持高并发场景
     * - 建议缓存生成的 ID
     *
     * UUID 说明：
     * - UUID v4：随机生成
     * - 唯一性：2^122 种可能
     * - 碰撞概率：极低
     * - 标准格式：8-4-4-4-12（36 字符）
     * - 移除连字符后：32 字符
     *
     * @return 唯一的请求 ID（32 位十六进制字符串）
     */
    public String generateRequestId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成带前缀的请求 ID
     *
     * 功能：
     * - 生成带前缀的唯一请求 ID
     * - 支持自定义前缀
     * - 便于识别请求来源
     * - 支持请求分类
     *
     * 参数说明：
     * - prefix: ID 前缀
     *   * 类型：String
     *   * 示例：\\\"req\\\"、\\\"api\\\"、\\\"task\\\"
     *   * 用途：标识请求类型或来源
     *   * 建议：使用简短的前缀（3-5 字符）
     *
     * 返回值：
     * - String：带前缀的唯一请求 ID
     * - 格式：prefix-32位十六进制字符串
     * - 示例：\\\"req-550e8400e29b41d4a716446655440000\\\"
     * - 长度：prefix 长度 + 1 + 32
     *
     * 使用示例：
     * // 生成 API 请求 ID
     * String apiRequestId = requestIdGenerator.generateRequestId(\\\"api\\\");
     * // 结果：\\\"api-550e8400e29b41d4a716446655440000\\\"
     *
     * // 生成异步任务 ID
     * String taskId = requestIdGenerator.generateRequestId(\\\"task\\\");
     * // 结果：\\\"task-550e8400e29b41d4a716446655440000\\\"
     *
     * // 生成消息队列 ID
     * String msgId = requestIdGenerator.generateRequestId(\\\"msg\\\");
     * // 结果：\\\"msg-550e8400e29b41d4a716446655440000\\\"
     *
     * REST A​PI 集成示例：
     * @PostMapping(\\\"/api/chat\\\")
     * public ResponseEntity<?> chat(@RequestBody ChatRequest request) {
     *     String requestId = requestIdGenerator.generateRequestId(\\\"chat\\\");
     *     logger.info(\\\"Chat request: {}\\\", requestId);
     *     // 处理请求
     *     return ResponseEntity.ok(\\\"...\\\");
     * }
     *
     * 异步任务示例：
     * @Async
     * public void processTask(String data) {
     *     String taskId = requestIdGenerator.generateRequestId(\\\"task\\\");
     *     logger.info(\\\"Processing task: {}\\\", taskId);
     *     // 处理任务
     * }
     *
     * 消息队列示例：
     * public void sendMessage(String message) {
     *     String msgId = requestIdGenerator.generateRequestId(\\\"msg\\\");
     *     rabbitTemplate.convertAndSend(
     *         exchange,
     *         routingKey,
     *         message,
     *         msg -> {
     *             msg.getMessageProperties().setHeader(\\\"X-Message-ID\\\", msgId);
     *             return msg;
     *         }
     *     );
     * }
     *
     * 分布式追踪示例：
     * String requestId = requestIdGenerator.generateRequestId(\\\"dist\\\");
     * // 在请求头中传递
     * headers.put(\\\"X-Trace-ID\\\", requestId);
     * // 调用下游服务
     * restTemplate.getForObject(url, String.class);
     *
     * 性能考虑：
     * - 时间复杂度：O(1)
     * - 字符串拼接速度快
     * - 支持高并发场景
     * - 建议缓存生成的 ID
     *
     * 前缀命名建议：
     * - \\\"req\\\"：HTTP 请求
     * - \\\"api\\\"：A​PI 调用
     * - \\\"task\\\"：异步任务
     * - \\\"msg\\\"：消息队列
     * - \\\"trace\\\"：分布式追踪
     * - \\\"event\\\"：事件处理
     * - \\\"job\\\"：定时任务
     *
     * @param prefix ID 前缀
     * @return 带前缀的唯一请求 ID
     */
    public String generateRequestId(String prefix) {
        return prefix + "-" + generateRequestId();
    }
}
