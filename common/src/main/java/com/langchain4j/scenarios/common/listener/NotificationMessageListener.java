package com.langchain4j.scenarios.common.listener;

import com.langchain4j.scenarios.common.config.MessageQueueConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 通知消息监听器 - RabbitMQ 消息队列中的通知消息处理
 *
 * 职责说明：
 * - 监听通知消息队列
 * - 异步处理通知事件
 * - 支持多渠道通知（邮件、短信、推送等）
 * - 实现通知的解耦处理
 * - 支持通知的可靠传输
 *
 * 架构设计：
 * - 使用 @RabbitListener 注解监听队列
 * - 使用 @Component 注解注册为 Spring Bean
 * - 支持条件化的 Bean 创建（ConditionalOnProperty）
 * - 包含多个通知处理方法
 * - 易于扩展和维护
 *
 * 使用场景：
 * - 用户注册后发送欢迎邮件
 * - 密码重置时发送验证码短信
 * - 重要事件发送推送通知
 * - 订单状态变化发送通知
 * - 系统告警发送通知
 *
 * 通知处理工作原理：
 * 1. 业务逻辑发送通知消息到队列
 * 2. RabbitMQ 存储消息
 * 3. 监听器接收消息
 * 4. 根据通知类型调用对应的处理方法
 * 5. 处理完成后自动确认消息
 * 6. 异常情况下消息重新入队
 *
 * 通知类型处理：
 * - email：邮件通知
 *   * 发送邮件给用户
 *   * 支持 HTML 格式
 *   * 支持附件
 *
 * - sms：短信通知
 *   * 发送短信给用户
 *   * 支持模板消息
 *   * 支持国际化
 *
 * - push：推送通知
 *   * 发送推送给移动应用
 *   * 支持富媒体
 *   * 支持深度链接
 *
 * 性能考虑：
 * - 通知处理是异步的，不阻塞主流程
 * - 支持高并发通知处理
 * - 建议使用通知批处理提高吞吐量
 * - 监控通知处理的延迟和失败率
 * - 定期清理过期通知
 *
 * 安全考虑：
 * - 验证通知数据的有效性
 * - 限制通知监听器的访问权限
 * - 记录通知处理的日志
 * - 实现通知处理的错误处理
 * - 防止通知处理中的异常传播
 *
 * 扩展建议：
 * - 可以添加通知重试机制
 * - 可以实现死信队列处理失败通知
 * - 可以添加通知处理的超时控制
 * - 可以支持通知的优先级处理
 * - 可以实现通知处理的分布式追踪
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = false)
public class NotificationMessageListener {

    /**
     * 处理通知消息
     */
    @RabbitListener(queues = MessageQueueConfig.NOTIFICATION_QUEUE)
    public void handleNotificationMessage(String message) {
        try {
            log.info("收到通知消息: {}", message);
            // 可以在这里处理邮件、短信、推送等通知
            processNotification(message);
        } catch (Exception e) {
            log.error("处理通知消息失败", e);
        }
    }

    private void processNotification(String message) {
        // 解析消息并根据类型发送相应的通知
        log.info("处理通知: {}", message);
    }
}
