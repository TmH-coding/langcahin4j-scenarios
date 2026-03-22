package com.langchain4j.scenarios.common.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 消息队列配置 - RabbitMQ 企业级集成
 *
 * 职责说明：
 * - 配置 RabbitMQ 的交换机、队列和绑定关系
 * - 支持异步消息处理和事件驱动架构
 * - 实现系统解耦和高可用性
 * - 支持多个业务场景的消息流转
 *
 * 架构设计：
 * - 使用 Topic Exchange（主题交换机）支持灵活的路由规则
 * - 使用 Jackson2JsonMessageConverter 进行 JSON 序列化
 * - 条件化配置：仅当 spring.rabbitmq.enabled=true 时启用
 * - 持久化队列：确保消息不会因服务重启而丢失
 *
 * RabbitMQ 核心概念：
 * - Exchange（交换机）：接收消息并根据规则转发到队列
 * - Queue（队列）：存储消息，等待消费者处理
 * - Binding（绑定）：定义交换机和队列的关系
 * - Routing Key（路由键）：消息的标签，用于匹配绑定规则
 *
 * 消息流程：
 * Producer → Exchange → (Routing Key 匹配) → Queue → Consumer
 *
 * 使用场景：
 * - 对话消息：异步处理用户对话，解耦 API 和处理逻辑
 * - 通知消息：发送邮件、短信等通知，不阻塞主流程
 * - 审计日志：异步记录操作日志，提高系统性能
 */
@Configuration
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = false)
public class MessageQueueConfig {

    // ==================== 交换机名称常量 ====================
    // 交换机是消息的入口点，负责根据路由键将消息转发到相应的队列
    public static final String CONVERSATION_EXCHANGE = "conversation.exchange";
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    public static final String AUDIT_EXCHANGE = "audit.exchange";

    // ==================== 队列名称常量 ====================
    // 队列是消息的存储点，消费者从队列中取出消息进行处理
    public static final String CONVERSATION_QUEUE = "conversation.queue";
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    public static final String AUDIT_QUEUE = "audit.queue";

    // ==================== 路由键常量 ====================
    // 路由键用于匹配消息应该发送到哪个队列
    // 使用通配符：* 匹配一个单词，# 匹配零个或多个单词
    public static final String CONVERSATION_ROUTING_KEY = "conversation.*";
    public static final String NOTIFICATION_ROUTING_KEY = "notification.*";
    public static final String AUDIT_ROUTING_KEY = "audit.*";

    /**
     * 消息转换器配置
     *
     * 功能：
     * - 将 Java 对象转换为 JSON 格式的消息
     * - 将接收到的 JSON 消息转换回 Java 对象
     * - 使用 Jackson 库进行序列化/反序列化
     *
     * 优势：
     * - JSON 格式易于跨语言通信
     * - 支持复杂对象的序列化
     * - 易于调试和监控
     * - 与 REST API 兼容
     *
     * 使用示例：
     * // 发送消息时自动转换为 JSON
     * ConversationMessage message = new ConversationMessage();
     * message.setContent("Hello");
     * rabbitTemplate.convertAndSend("exchange", "key", message);
     * // 消息被转换为 JSON 字符串发送
     *
     * // 接收消息时自动转换为 Java 对象
     * @RabbitListener(queues = "conversation.queue")
     * public void handleMessage(ConversationMessage message) {
     *     // message 已自动从 JSON 转换为 Java 对象
     * }
     *
     * @return Jackson2JsonMessageConverter 消息转换器
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate 配置
     *
     * 功能：
     * - 提供发送消息的模板类
     * - 简化消息发送的代码
     * - 自动使用配置的消息转换器
     *
     * 使用示例：
     * rabbitTemplate.convertAndSend("conversation.exchange", "conversation.start", message);
     *
     * @param connectionFactory RabbitMQ 连接工厂
     * @return 配置好的 RabbitTemplate
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    // ==================== 对话消息配置 ====================
    // 用于处理用户与 AI 的对话消息

    /**
     * 对话交换机
     *
     * 功能：
     * - 接收对话相关的消息
     * - 使用 Topic 类型支持灵活的路由
     *
     * 参数说明：
     * - name: 交换机名称
     * - durable: true - 交换机持久化（服务重启后仍存在）
     * - autoDelete: false - 不自动删除（即使没有队列绑定）
     *
     * @return TopicExchange 对话交换机
     */
    @Bean
    public TopicExchange conversationExchange() {
        return new TopicExchange(CONVERSATION_EXCHANGE, true, false);
    }

    /**
     * 对话队列
     *
     * 功能：
     * - 存储对话消息
     * - 等待消费者处理
     *
     * 参数说明：
     * - name: 队列名称
     * - durable: true - 队列持久化（消息不会因服务重启而丢失）
     *
     * @return Queue 对话队列
     */
    @Bean
    public Queue conversationQueue() {
        return new Queue(CONVERSATION_QUEUE, true);
    }

    /**
     * 对话绑定
     *
     * 功能：
     * - 将对话队列绑定到对话交换机
     * - 定义路由规则：conversation.* 的消息会被转发到此队列
     *
     * 路由规则示例：
     * - conversation.start → 匹配
     * - conversation.end → 匹配
     * - notification.start → 不匹配
     *
     * @param conversationQueue 对话队列
     * @param conversationExchange 对话交换机
     * @return Binding 绑定关系
     */
    @Bean
    public Binding conversationBinding(Queue conversationQueue, TopicExchange conversationExchange) {
        return BindingBuilder.bind(conversationQueue)
                .to(conversationExchange)
                .with(CONVERSATION_ROUTING_KEY);
    }

    // ==================== 通知消息配置 ====================
    // 用于处理系统通知（邮件、短信等）

    /**
     * 通知交换机
     *
     * @return TopicExchange 通知交换机
     */
    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE, true, false);
    }

    /**
     * 通知队列
     *
     * @return Queue 通知队列
     */
    @Bean
    public Queue notificationQueue() {
        return new Queue(NOTIFICATION_QUEUE, true);
    }

    /**
     * 通知绑定
     *
     * @param notificationQueue 通知队列
     * @param notificationExchange 通知交换机
     * @return Binding 绑定关系
     */
    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(notificationQueue)
                .to(notificationExchange)
                .with(NOTIFICATION_ROUTING_KEY);
    }

    // ==================== 审计日志配置 ====================
    // 用于异步记录审计日志

    /**
     * 审计交换机
     *
     * @return TopicExchange 审计交换机
     */
    @Bean
    public TopicExchange auditExchange() {
        return new TopicExchange(AUDIT_EXCHANGE, true, false);
    }

    /**
     * 审计队列
     *
     * @return Queue 审计队列
     */
    @Bean
    public Queue auditQueue() {
        return new Queue(AUDIT_QUEUE, true);
    }

    /**
     * 审计绑定
     *
     * @param auditQueue 审计队列
     * @param auditExchange 审计交换机
     * @return Binding 绑定关系
     */
    @Bean
    public Binding auditBinding(Queue auditQueue, TopicExchange auditExchange) {
        return BindingBuilder.bind(auditQueue)
                .to(auditExchange)
                .with(AUDIT_ROUTING_KEY);
    }
}
