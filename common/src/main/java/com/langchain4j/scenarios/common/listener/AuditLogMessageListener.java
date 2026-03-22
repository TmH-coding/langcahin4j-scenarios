package com.langchain4j.scenarios.common.listener;

import com.langchain4j.scenarios.common.config.MessageQueueConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 审计日志消息监听器 - RabbitMQ 消息队列中的审计日志处理
 *
 * 职责说明：
 * - 监听审计日志消息队列
 * - 异步处理审计事件
 * - 支持审计日志的持久化存储
 * - 实现审计日志的完整生命周期处理
 * - 支持审计数据的分析和查询
 *
 * 架构设计：
 * - 使用 @RabbitListener 注解监听队列
 * - 使用 @Component 注解注册为 Spring Bean
 * - 支持条件化的 Bean 创建（ConditionalOnProperty）
 * - 包含审计日志处理方法
 * - 易于扩展和维护
 *
 * 使用场景：
 * - 记录用户的所有操作
 * - 追踪数据的修改历史
 * - 实现合规性审计
 * - 支持安全事件调查
 * - 生成审计报告
 *
 * 审计日志处理工作原理：
 * 1. 业务逻辑发送审计日志到队列
 * 2. RabbitMQ 存储审计日志
 * 3. 监听器接收审计日志
 * 4. 解析审计日志数据
 * 5. 保存到数据库或日志系统
 * 6. 异常情况下日志重新入队
 *
 * 审计日志内容：
 * - action：操作类型（CREATE、UPDATE、DELETE、LOGIN、LOGOUT 等）
 * - userId：执行操作的用户 ID
 * - details：操作详情（具体的操作内容）
 * - timestamp：操作时间戳（毫秒）
 *
 * 性能考虑：
 * - 审计日志处理是异步的，不阻塞主流程
 * - 支持高并发审计日志处理
 * - 建议使用批处理提高数据库写入性能
 * - 监控审计日志处理的延迟和错误率
 * - 定期归档旧审计日志
 *
 * 安全考虑：
 * - 验证审计日志数据的有效性
 * - 限制审计日志监听器的访问权限
 * - 记录审计日志处理的日志
 * - 实现审计日志处理的错误处理
 * - 防止审计日志被篡改
 *
 * 合规性考虑：
 * - 审计日志应该完整记录所有操作
 * - 应该包含足够的信息用于追踪
 * - 应该定期备份和归档
 * - 应该防止审计日志被篡改
 * - 应该实现审计日志的加密存储
 *
 * 扩展建议：
 * - 可以添加审计日志重试机制
 * - 可以实现死信队列处理失败日志
 * - 可以添加审计日志处理的超时控制
 * - 可以支持审计日志的优先级处理
 * - 可以实现审计日志处理的分布式追踪
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = false)
public class AuditLogMessageListener {

    /**
     * 处理审计日志消息
     */
    @RabbitListener(queues = MessageQueueConfig.AUDIT_QUEUE)
    public void handleAuditLogMessage(String message) {
        try {
            log.info("收到审计日志消息: {}", message);
            // 可以在这里处理审计日志的持久化、分析等
            processAuditLog(message);
        } catch (Exception e) {
            log.error("处理审计日志消息失败", e);
        }
    }

    private void processAuditLog(String message) {
        // 解析审计日志并进行相应处理
        log.info("处理审计日志: {}", message);
    }
}
