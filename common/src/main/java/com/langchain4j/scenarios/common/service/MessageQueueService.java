package com.langchain4j.scenarios.common.service;

import com.langchain4j.scenarios.common.config.MessageQueueConfig;
import com.langchain4j.scenarios.common.event.ConversationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 消息队列服务 - RabbitMQ 消息发送和异步处理
 *
 * 职责说明：
 * - 发送对话消息到 RabbitMQ 队列
 * - 发送通知消息到 RabbitMQ 队列
 * - 发送审计日志到 RabbitMQ 队列
 * - 支持异步消息处理和解耦
 * - 实现消息的可靠传输
 * - 支持消息的重试和死信处理
 *
 * 架构设计：
 * - 使用 RabbitTemplate 发送消息
 * - 支持多个交换机和队列
 * - 使用路由键进行灵活的消息路由
 * - 支持消息的 JSON 序列化
 * - 使用 Topic Exchange 实现灵活的消息路由
 * - 支持条件化的 Bean 创建（ConditionalOnProperty）
 *
 * 使用场景：
 * - 异步处理对话消息，不阻塞主流程
 * - 发送邮件、短信等通知，解耦通知逻辑
 * - 异步记录审计日志，提高系统性能
 * - 支持消息的重试和死信队列处理
 * - 实现系统间的异步通信
 * - 支持消息的削峰填谷
 *
 * 消息发送工作原理：
 * 1. 调用发送方法，传入消息数据
 * 2. 构建消息对象（如需要）
 * 3. 使用 RabbitTemplate 发送到指定交换机
 * 4. 根据路由键匹配相应的队列
 * 5. 消费者从队列中取出消息处理
 * 6. 消费者处理完成后发送 ACK
 *
 * 消息队列配置：
 * - conversation.exchange：对话消息交换机
 * - notification.exchange：通知消息交换机
 * - audit.exchange：审计日志交换机
 * - 每个交换机对应一个队列
 * - 使用 Topic Exchange 支持灵活的路由
 *
 * 性能考虑：
 * - 异步发送，不阻塞调用者
 * - RabbitTemplate 会自动处理连接管理
 * - 支持批量发送优化
 * - 消费者可以独立扩展
 * - 支持消息的优先级处理
 *
 * 安全考虑：
 * - 消息应该包含足够的信息用于追踪
 * - 不要在消息中存储敏感信息（密码等）
 * - 实现消息的幂等性处理
 * - 监控消息发送失败情况
 * - 实现消息的加密传输
 * - 验证消息的来源和完整性
 *
 * 扩展建议：
 * - 可以添加消息的持久化存储
 * - 可以实现消息的优先级队列
 * - 可以添加消息的延迟处理
 * - 可以实现消息的分片处理
 * - 可以添加消息的监控和告警
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = false)
public class MessageQueueService {

    private final RabbitTemplate rabbitTemplate;

    public MessageQueueService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 发送对话消息到队列
     *
     * 功能：
     * - 将用户对话消息异步发送到 RabbitMQ
     * - 不阻塞主流程，提高响应速度
     * - 支持消息的异步处理和分析
     * - 实现对话消息的解耦处理
     *
     * 参数说明：
     * - message: ConversationMessage 对象
     *   * 包含对话 ID、用户 ID、消息内容等
     *   * 由 ConversationMessage 类定义
     *   * 示例：对话 ID、用户 ID、消息内容
     *
     * 消息路由：
     * - 交换机：conversation.exchange
     * - 路由键：conversation.exchange.event
     * - 队列：conversation.queue
     * - 匹配规则：conversation.* 匹配此路由键
     *
     * 使用示例：
     * ConversationMessage message = new ConversationMessage();
     * message.setConversationId(\\\"conv-123\\\");
     * message.setUserId(\\\"user-456\\\");
     * message.setContent(\\\"Hello, AI!\\\");
     * messageQueueService.sendConversationMessage(message);
     *
     * 错误处理：
     * - 如果发送失败，记录错误日志
     * - 不抛出异常，避免影响主流程
     * - 可以配置重试机制
     * - 消息发送失败不会中断业务流程
     *
     * 性能考虑：
     * - 消息发送是异步的，不会阻塞调用者
     * - RabbitTemplate 会自动处理连接管理
     * - 支持批量发送优化
     * - 建议在异步线程中调用
     *
     * 应用场景：
     * - 记录用户的对话消息
     * - 异步处理对话内容
     * - 支持对话分析和统计
     * - 实现对话的持久化
     *
     * @param message 对话消息对象
     */
    public void sendConversationMessage(ConversationMessage message) {
        try {
            String routingKey = MessageQueueConfig.CONVERSATION_EXCHANGE + ".event";
            rabbitTemplate.convertAndSend(
                    MessageQueueConfig.CONVERSATION_EXCHANGE,
                    routingKey,
                    message
            );
            log.info("对话消息已发送: conversationId={}", message.getConversationId());
        } catch (Exception e) {
            log.error("发送对话消息失败", e);
        }
    }

    /**
     * 发送通知消息到队列
     *
     * 功能：
     * - 将通知消息异步发送到 RabbitMQ
     * - 支持多种通知类型（邮件、短信等）
     * - 解耦通知逻辑，提高系统性能
     * - 实现通知的异步处理
     *
     * 参数说明：
     * - notificationType: 通知类型
     *   * email: 邮件通知
     *   * sms: 短信通知
     *   * push: 推送通知
     *   * 自定义类型
     *   * 示例：\\\"email\\\"、\\\"sms\\\"、\\\"push\\\"
     *
     * - payload: 通知内容
     *   * 可以是任何对象
     *   * 会被 JSON 序列化
     *   * 示例：EmailNotification、SmsNotification 对象
     *
     * 消息路由：
     * - 交换机：notification.exchange
     * - 路由键：notification.exchange.{notificationType}
     * - 队列：notification.queue
     * - 匹配规则：notification.* 匹配此路由键
     *
     * 使用示例：
     * // 发送邮件通知
     * EmailNotification email = new EmailNotification();
     * email.setTo(\\\"user@example.com\\\");
     * email.setSubject(\\\"Welcome\\\");
     * email.setBody(\\\"Welcome to our platform\\\");
     * messageQueueService.sendNotificationMessage(\\\"email\\\", email);
     *
     * // 发送短信通知
     * SmsNotification sms = new SmsNotification();
     * sms.setPhoneNumber(\\\"+86 13800138000\\\");
     * sms.setContent(\\\"Your verification code is 123456\\\");
     * messageQueueService.sendNotificationMessage(\\\"sms\\\", sms);
     *
     * // 发送推送通知
     * PushNotification push = new PushNotification();
     * push.setTitle(\\\"New Message\\\");
     * push.setContent(\\\"You have a new message\\\");
     * messageQueueService.sendNotificationMessage(\\\"push\\\", push);
     *
     * 错误处理：
     * - 如果发送失败，记录错误日志
     * - 不抛出异常，避免影响主流程
     * - 可以配置重试机制或死信队列
     * - 通知发送失败不会中断业务流程
     *
     * 性能考虑：
     * - 异步发送，不阻塞调用者
     * - 支持高并发通知发送
     * - 消费者可以独立扩展
     * - 建议使用线程池处理多个通知
     *
     * 应用场景：
     * - 用户注册后发送欢迎邮件
     * - 密码重置时发送验证码短信
     * - 重要事件发送推送通知
     * - 订单状态变化发送通知
     * - 系统告警发送通知
     *
     * @param notificationType 通知类型
     * @param payload 通知内容
     */
    public void sendNotificationMessage(String notificationType, Object payload) {
        try {
            String routingKey = MessageQueueConfig.NOTIFICATION_EXCHANGE + "." + notificationType;
            rabbitTemplate.convertAndSend(
                    MessageQueueConfig.NOTIFICATION_EXCHANGE,
                    routingKey,
                    payload
            );
            log.info("通知消息已发送: type={}", notificationType);
        } catch (Exception e) {
            log.error("发送通知消息失败", e);
        }
    }

    /**
     * 发送审计日志到队列
     *
     * 功能：
     * - 将审计日志异步发送到 RabbitMQ
     * - 不阻塞主流程，提高系统性能
     * - 支持审计日志的异步处理和存储
     * - 实现审计日志的解耦处理
     *
     * 参数说明：
     * - action: 操作类型
     *   * CREATE: 创建操作
     *   * UPDATE: 更新操作
     *   * DELETE: 删除操作
     *   * LOGIN: 登录操作
     *   * LOGOUT: 登出操作
     *   * 自定义操作类型
     *   * 示例：\\\"CREATE\\\"、\\\"UPDATE\\\"、\\\"DELETE\\\"
     *
     * - userId: 执行操作的用户 ID
     *   * 类型：String
     *   * 示例：\\\"user-123\\\"
     *   * 用于追踪操作者
     *
     * - details: 操作详情
     *   * 可以包含操作的具体内容
     *   * 用于审计和问题排查
     *   * 示例：\\\"Deleted conversation with ID: conv-456\\\"
     *
     * 消息路由：
     * - 交换机：audit.exchange
     * - 路由键：audit.exchange.log
     * - 队列：audit.queue
     * - 匹配规则：audit.* 匹配此路由键
     *
     * 使用示例：
     * messageQueueService.sendAuditLog(
     *     \\\"DELETE\\\",
     *     \\\"user-123\\\",
     *     \\\"Deleted conversation with ID: conv-456\\\"
     * );
     *
     * // 创建操作审计
     * messageQueueService.sendAuditLog(
     *     \\\"CREATE\\\",
     *     \\\"user-456\\\",
     *     \\\"Created new conversation\\\"
     * );
     *
     * // 更新操作审计
     * messageQueueService.sendAuditLog(
     *     \\\"UPDATE\\\",
     *     \\\"user-789\\\",
     *     \\\"Updated user profile\\\"
     * );
     *
     * 审计日志结构：
     * - action: 操作类型
     * - userId: 用户 ID
     * - details: 操作详情
     * - timestamp: 操作时间戳（毫秒）
     *
     * 错误处理：
     * - 如果发送失败，记录错误日志
     * - 不抛出异常，避免影响主流程
     * - 审计日志丢失不应该影响业务操作
     * - 建议配置死信队列处理失败的审计日志
     *
     * 性能考虑：
     * - 异步发送，不阻塞调用者
     * - 支持高并发审计日志发送
     * - 消费者可以独立处理和存储
     * - 建议使用批量插入优化数据库写入
     *
     * 合规性考虑：
     * - 审计日志应该完整记录所有操作
     * - 应该包含足够的信息用于追踪
     * - 应该定期备份和归档
     * - 应该防止审计日志被篡改
     * - 应该实现审计日志的加密存储
     *
     * 应用场景：
     * - 记录用户的所有操作
     * - 追踪数据的修改历史
     * - 实现合规性审计
     * - 支持安全事件调查
     * - 生成审计报告
     *
     * @param action 操作类型
     * @param userId 用户 ID
     * @param details 操作详情
     */
    public void sendAuditLog(String action, String userId, String details) {
        try {
            String routingKey = MessageQueueConfig.AUDIT_EXCHANGE + ".log";
            AuditLogMessage auditLog = AuditLogMessage.builder()
                    .action(action)
                    .userId(userId)
                    .details(details)
                    .timestamp(System.currentTimeMillis())
                    .build();

            rabbitTemplate.convertAndSend(
                    MessageQueueConfig.AUDIT_EXCHANGE,
                    routingKey,
                    auditLog
            );
            log.info("审计日志已发送: action={}, userId={}", action, userId);
        } catch (Exception e) {
            log.error("发送审计日志失败", e);
        }
    }

    /**
     * 审计日志消息类 - 审计日志的数据结构和序列化
     *
     * 职责说明：
     * - 定义审计日志的数据结构
     * - 支持 JSON 序列化和反序列化
     * - 用于在消息队列中传输审计信息
     * - 提供 Builder 模式的便捷构建
     *
     * 字段说明：
     * - action: 操作类型（CREATE、UPDATE、DELETE 等）
     *   * 类型：String
     *   * 示例：\\\"CREATE\\\"、\\\"UPDATE\\\"、\\\"DELETE\\\"
     *   * 用于分类审计日志
     *
     * - userId: 执行操作的用户 ID
     *   * 类型：String
     *   * 示例：\\\"user-123\\\"
     *   * 用于追踪操作者
     *
     * - details: 操作详情（具体的操作内容）
     *   * 类型：String
     *   * 示例：\\\"Updated user profile\\\"
     *   * 用于记录操作的具体内容
     *
     * - timestamp: 操作时间戳（毫秒）
     *   * 类型：long
     *   * 示例：1711000000000
     *   * 用于记录操作的时间
     *
     * 使用示例：
     * AuditLogMessage log = AuditLogMessage.builder()
     *     .action(\\\"UPDATE\\\")
     *     .userId(\\\"user-123\\\")
     *     .details(\\\"Updated user profile\\\")
     *     .timestamp(System.currentTimeMillis())
     *     .build();
     *
     * 序列化示例（JSON）：
     * {
     *   \\\"action\\\": \\\"UPDATE\\\",
     *   \\\"userId\\\": \\\"user-123\\\",
     *   \\\"details\\\": \\\"Updated user profile\\\",
     *   \\\"timestamp\\\": 1711000000000
     * }
     *
     * 反序列化示例：
     * ObjectMapper mapper = new ObjectMapper();
     * AuditLogMessage log = mapper.readValue(json, AuditLogMessage.class);
     *
     * 性能考虑：
     * - 序列化速度快
     * - 支持高并发处理
     * - 建议使用对象池优化内存使用
     *
     * 扩展建议：
     * - 可以添加操作结果字段（成功/失败）
     * - 可以添加操作的 IP 地址
     * - 可以添加操作的浏览器信息
     * - 可以添加操作的变更内容
     */
    public static class AuditLogMessage {
        private String action;
        private String userId;
        private String details;
        private long timestamp;

        public AuditLogMessage() {}

        public AuditLogMessage(String action, String userId, String details, long timestamp) {
            this.action = action;
            this.userId = userId;
            this.details = details;
            this.timestamp = timestamp;
        }

        public static AuditLogMessageBuilder builder() {
            return new AuditLogMessageBuilder();
        }

        public String getAction() { return action; }
        public String getUserId() { return userId; }
        public String getDetails() { return details; }
        public long getTimestamp() { return timestamp; }

        public static class AuditLogMessageBuilder {
            private String action;
            private String userId;
            private String details;
            private long timestamp;

            public AuditLogMessageBuilder action(String action) {
                this.action = action;
                return this;
            }

            public AuditLogMessageBuilder userId(String userId) {
                this.userId = userId;
                return this;
            }

            public AuditLogMessageBuilder details(String details) {
                this.details = details;
                return this;
            }

            public AuditLogMessageBuilder timestamp(long timestamp) {
                this.timestamp = timestamp;
                return this;
            }

            public AuditLogMessage build() {
                return new AuditLogMessage(action, userId, details, timestamp);
            }
        }
    }
}
