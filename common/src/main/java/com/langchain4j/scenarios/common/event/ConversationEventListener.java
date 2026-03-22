package com.langchain4j.scenarios.common.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 对话事件监听器 - Spring 事件驱动架构中的对话事件处理
 *
 * 职责说明：
 * - 监听对话相关事件
 * - 执行事件处理逻辑
 * - 支持异步事件处理
 * - 解耦事件发布和处理
 * - 实现事件驱动的业务流程
 *
 * 架构设计：
 * - 使用 @EventListener 注解监听事件
 * - 使用 @Component 注解注册为 Spring Bean
 * - 支持多个事件处理方法
 * - 可以与 @Async 结合实现异步处理
 * - 易于扩展和维护
 *
 * 使用场景：
 * - 对话开始时记录日志和初始化数据
 * - 对话消息时进行实时处理和分析
 * - 对话结束时进行清理和统计
 * - 对话错误时进行告警和恢复
 * - 对话分析时进行数据聚合
 *
 * 事件处理工作原理：
 * 1. 业务逻辑发布 ConversationEvent 事件
 * 2. Spring 事件发布器接收事件
 * 3. 事件监听器的对应方法被调用
 * 4. 监听器执行事件处理逻辑
 * 5. 不阻塞主业务流程
 *
 * 事件类型处理：
 * - START：对话开始事件
 *   * 初始化会话数据
 *   * 记录开始日志
 *   * 触发欢迎消息
 *
 * - MESSAGE：对话消息事件
 *   * 处理消息内容
 *   * 更新会话状态
 *   * 触发消息分析
 *
 * - END：对话结束事件
 *   * 清理会话数据
 *   * 记录结束日志
 *   * 生成对话统计
 *
 * 性能考虑：
 * - 事件监听器应该快速返回
 * - 避免在监听器中执行长时间操作
 * - 建议使用 @Async 异步处理
 * - 建议使用线程池处理多个事件
 * - 监控事件处理的性能指标
 *
 * 安全考虑：
 * - 验证事件数据的有效性
 * - 限制事件监听器的访问权限
 * - 记录事件处理的日志
 * - 实现事件处理的错误处理
 * - 防止事件处理中的异常传播
 *
 * 扩展建议：
 * - 可以添加更多事件类型处理
 * - 可以实现事件的优先级处理
 * - 可以添加事件处理的重试机制
 * - 可以实现事件的分布式处理
 * - 可以支持事件的条件过滤
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ConversationEventListener {

    /**
     * 监听对话开始事件
     */
    @EventListener
    public void onConversationStart(ConversationEvent event) {
        if ("START".equals(event.getEventType())) {
            log.info("对话开始 - 会话ID: {}, 用户ID: {}, 场景: {}",
                    event.getSessionId(),
                    event.getUserId(),
                    event.getScenarioType());
            // 执行对话开始逻辑
        }
    }

    /**
     * 监听对话结束事件
     */
    @EventListener
    public void onConversationEnd(ConversationEvent event) {
        if ("END".equals(event.getEventType())) {
            log.info("对话结束 - 会话ID: {}, 用户ID: {}",
                    event.getSessionId(),
                    event.getUserId());
            // 执行对话结束逻辑
        }
    }

    /**
     * 监听对话消息事件
     */
    @EventListener
    public void onConversationMessage(ConversationEvent event) {
        if ("MESSAGE".equals(event.getEventType())) {
            log.debug("对话消息 - 会话ID: {}, 消息长度: {}",
                    event.getSessionId(),
                    event.getMessage().length());
            // 执行消息处理逻辑
        }
    }
}
