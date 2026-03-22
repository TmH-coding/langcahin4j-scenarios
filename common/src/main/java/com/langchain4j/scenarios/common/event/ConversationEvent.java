package com.langchain4j.scenarios.common.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 对话事件 - Spring 事件驱动架构中的对话事件
 *
 * 职责说明：
 * - 发布对话相关事件
 * - 支持事件驱动架构
 * - 解耦业务逻辑和事件处理
 * - 实现异步事件处理
 * - 支持多个事件监听器
 *
 * 架构设计：
 * - 继承 Spring 的 ApplicationEvent
 * - 包含对话的完整上下文信息
 * - 支持事件类型区分
 * - 自动记录事件时间戳
 * - 易于在监听器中处理
 *
 * 使用场景：
 * - 对话开始事件：用户发起新对话
 * - 对话消息事件：用户发送消息
 * - 对话结束事件：对话完成或中断
 * - 对话错误事件：对话处理出错
 * - 对话分析事件：对话数据分析
 *
 * 事件驱动工作原理：
 * 1. 业务逻辑发布事件
 * 2. Spring 事件发布器发送事件
 * 3. 事件监听器接收事件
 * 4. 监听器异步处理事件
 * 5. 不阻塞主业务流程
 *
 * 事件类型规范：
 * - \"conversation_started\" - 对话开始
 * - \"message_received\" - 消息接收
 * - \"message_sent\" - 消息发送
 * - \"conversation_ended\" - 对话结束
 * - \"conversation_error\" - 对话错误
 * - \"conversation_analyzed\" - 对话分析
 *
 * 性能考虑：
 * - 事件发布是同步的，但监听器可以异步处理
 * - 建议使用 @Async 注解异步处理事件
 * - 避免在事件监听器中执行长时间操作
 * - 建议使用线程池处理多个事件
 *
 * 安全考虑：
 * - 事件可能包含敏感信息
 * - 限制事件监听器的访问权限
 * - 记录事件用于审计
 * - 实现事件的加密传输
 *
 * 扩展建议：
 * - 可以添加事件优先级字段
 * - 可以添加事件重试机制
 * - 可以实现事件的持久化
 * - 可以支持事件的分布式传播
 */
@Getter
public class ConversationEvent extends ApplicationEvent {
    private String eventType;
    private String sessionId;
    private String userId;
    private String message;
    private String scenarioType;
    private long eventTimestamp;

    public ConversationEvent(Object source, String eventType, String sessionId, String userId, String message, String scenarioType) {
        super(source);
        this.eventType = eventType;
        this.sessionId = sessionId;
        this.userId = userId;
        this.message = message;
        this.scenarioType = scenarioType;
        this.eventTimestamp = System.currentTimeMillis();
    }
}
