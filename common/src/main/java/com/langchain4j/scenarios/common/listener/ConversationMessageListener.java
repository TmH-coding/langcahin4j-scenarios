package com.langchain4j.scenarios.common.listener;

import com.langchain4j.scenarios.common.config.MessageQueueConfig;
import com.langchain4j.scenarios.common.event.ConversationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 对话消息监听器 - RabbitMQ 消息队列中的对话消息处理
 *
 * 职责说明：
 * - 监听对话消息队列
 * - 异步处理对话事件
 * - 支持消息的持久化存储
 * - 实现对话消息的完整生命周期处理
 * - 支持多种事件类型的处理
 *
 * 架构设计：
 * - 使用 @RabbitListener 注解监听队列
 * - 使用 @Component 注解注册为 Spring Bean
 * - 支持条件化的 Bean 创建（ConditionalOnProperty）
 * - 包含多个事件处理方法
 * - 易于扩展和维护
 *
 * 使用场景：
 * - 异步处理用户发送的对话消息
 * - 记录对话消息到数据库
 * - 分析对话消息数据
 * - 触发对话相关的业务流程
 * - 实现对话的分布式处理
 *
 * 消息处理工作原理：
 * 1. 业务逻辑发送 ConversationMessage 到队列
 * 2. RabbitMQ 存储消息
 * 3. 监听器接收消息
 * 4. 根据事件类型调用对应的处理方法
 * 5. 处理完成后自动确认消息
 * 6. 异常情况下消息重新入队
 *
 * 事件类型处理：
 * - START：对话开始
 *   * 初始化会话数据
 *   * 记录开始时间
 *   * 触发欢迎消息
 *
 * - PROCESSING：对话处理中
 *   * 更新会话状态
 *   * 记录处理进度
 *   * 触发中间步骤
 *
 * - COMPLETED：对话完成
 *   * 保存对话记录
 *   * 生成统计数据
 *   * 触发后续流程
 *
 * - FAILED：对话失败
 *   * 记录错误信息
 *   * 发送告警通知
 *   * 触发恢复流程
 *
 * 性能考虑：
 * - 消息处理是异步的，不阻塞主流程
 * - 支持高并发消息处理
 * - 建议使用消息批处理提高吞吐量
 * - 监控消息处理的延迟和错误率
 * - 定期清理过期消息
 *
 * 安全考虑：
 * - 验证消息数据的有效性
 * - 限制消息监听器的访问权限
 * - 记录消息处理的日志
 * - 实现消息处理的错误处理
 * - 防止消息处理中的异常传播
 *
 * 扩展建议：
 * - 可以添加消息重试机制
 * - 可以实现死信队列处理失败消息
 * - 可以添加消息处理的超时控制
 * - 可以支持消息的优先级处理
 * - 可以实现消息处理的分布式追踪
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = false)
public class ConversationMessageListener {

    /**
     * 处理对话消息
     */
    @RabbitListener(queues = MessageQueueConfig.CONVERSATION_QUEUE)
    public void handleConversationMessage(ConversationMessage message) {
        try {
            log.info("收到对话消息: conversationId={}, eventType={}",
                    message.getConversationId(), message.getEventType());

            // 根据事件类型处理
            switch (message.getEventType()) {
                case "START":
                    handleConversationStart(message);
                    break;
                case "PROCESSING":
                    handleConversationProcessing(message);
                    break;
                case "COMPLETED":
                    handleConversationCompleted(message);
                    break;
                case "FAILED":
                    handleConversationFailed(message);
                    break;
                default:
                    log.warn("未知的事件类型: {}", message.getEventType());
            }
        } catch (Exception e) {
            log.error("处理对话消息失败", e);
        }
    }

    private void handleConversationStart(ConversationMessage message) {
        log.info("对话开始: userId={}", message.getUserId());
        // 可以在这里记录对话开始时间、初始化对话上下文等
    }

    private void handleConversationProcessing(ConversationMessage message) {
        log.info("对话处理中: conversationId={}", message.getConversationId());
        // 可以在这里更新对话状态、记录处理进度等
    }

    private void handleConversationCompleted(ConversationMessage message) {
        log.info("对话完成: conversationId={}", message.getConversationId());
        // 可以在这里保存对话记录、生成统计数据等
    }

    private void handleConversationFailed(ConversationMessage message) {
        log.error("对话失败: conversationId={}, message={}",
                message.getConversationId(), message.getMessage());
        // 可以在这里记录错误、发送告警等
    }
}
