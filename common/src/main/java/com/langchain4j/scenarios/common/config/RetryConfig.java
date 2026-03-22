package com.langchain4j.scenarios.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * 重试配置 - 自动重试和故障恢复
 *
 * 职责说明：
 * - 配置自动重试机制处理临时故障
 * - 实现指数退避策略避免雪崩
 * - 支持 @Retryable 注解的方法重试
 * - 提高系统可靠性和容错能力
 *
 * 架构设计：
 * - 使用 Spring Retry 框架实现重试逻辑
 * - 使用 RetryTemplate 提供编程式重试
 * - 使用 @Retryable 注解提供声明式重试
 * - 支持自定义重试策略和退避策略
 *
 * 使用场景：
 * - 调用外部 API 时的临时网络故障
 * - 数据库连接超时或暂时不可用
 * - LLM API 调用失败（速率限制、超时等）
 * - 消息队列发送失败
 * - 分布式系统中的暂时性故障
 *
 * 重试工作原理：
 * 1. 方法执行失败，抛出异常
 * 2. 检查异常是否在重试列表中
 * 3. 如果是，等待退避时间后重试
 * 4. 重复直到成功或达到最大重试次数
 * 5. 如果仍然失败，抛出异常给调用者
 *
 * 安全考虑：
 * - 只对幂等操作进行重试（避免重复执行）
 * - 设置合理的最大重试次数（避免无限重试）
 * - 使用指数退避避免雪崩效应
 * - 监控重试次数和失败率
 */
@Configuration
@EnableRetry
public class RetryConfig {

    /**
     * 创建重试模板
     *
     * 功能：
     * - 创建 RetryTemplate Bean 用于编程式重试
     * - 配置重试策略（最大重试次数）
     * - 配置退避策略（重试间隔）
     * - 支持在代码中手动调用重试逻辑
     *
     * 重试配置参数详解：
     *
     * SimpleRetryPolicy - 简单重试策略
     * - 基于重试次数的重试策略
     * - 每次失败都会重试，直到达到最大次数
     * - maxAttempts = 3：最多尝试 3 次（1 次初始 + 2 次重试）
     * - 其他重试策略：
     *   * NeverRetryPolicy: 不重试
     *   * AlwaysRetryPolicy: 无限重试（不推荐）
     *   * CircuitBreakerRetryPolicy: 熔断器模式
     *   * CompositeRetryPolicy: 组合多个策略
     *
     * ExponentialBackOffPolicy - 指数退避策略
     * - 重试间隔随着重试次数指数增长
     * - 避免同时发送大量请求导致雪崩
     * - initialInterval = 1000：初始等待时间 1 秒
     * - maxInterval = 10000：最大等待时间 10 秒
     * - multiplier = 2：每次重试间隔翻倍
     *
     * 退避时间计算：
     * - 第 1 次重试：1 秒
     * - 第 2 次重试：2 秒（1 * 2）
     * - 第 3 次重试：4 秒（2 * 2，但不超过 maxInterval）
     * - 第 4 次重试：8 秒
     * - 第 5 次重试：10 秒（达到 maxInterval 上限）
     *
     * 其他退避策略：
     * - FixedBackOffPolicy: 固定间隔重试
     * - UniformRandomBackOffPolicy: 随机间隔重试
     * - ExponentialRandomBackOffPolicy: 指数随机间隔
     *
     * 使用示例 - 编程式重试：
     * @Autowired
     * private RetryTemplate retryTemplate;
     *
     * public String callExternalApi() {
     *     return retryTemplate.execute(context -> {
     *         // 这段代码会自动重试
     *         return externalApiClient.call();
     *     });
     * }
     *
     * 使用示例 - 声明式重试（需要 @EnableRetry）：
     * @Service
     * public class UserService {
     *     @Retryable(
     *         value = {IOException.class},
     *         maxAttempts = 3,
     *         backoff = @Backoff(delay = 1000, multiplier = 2)
     *     )
     *     public User getUserFromApi(String userId) {
     *         return apiClient.getUser(userId);
     *     }
     *
     *     @Recover
     *     public User recoverGetUser(IOException e, String userId) {
     *         // 重试失败后的恢复逻辑
     *         return new User(userId, "Unknown");
     *     }
     * }
     *
     * 重试决策树：
     * 1. 是否是幂等操作？
     *    - 是：可以安全重试
     *    - 否：需要谨慎，可能导致重复执行
     * 2. 异常是否是临时性的？
     *    - 是：应该重试（网络超时、速率限制等）
     *    - 否：不应该重试（参数错误、权限不足等）
     * 3. 重试是否会改善情况？
     *    - 是：设置合理的重试次数和间隔
     *    - 否：立即失败，不浪费时间
     *
     * 监控和日志：
     * - 记录每次重试的时间和异常
     * - 监控重试成功率
     * - 监控重试导致的延迟
     * - 根据监控数据调整重试策略
     *
     * @return RetryTemplate 配置好的重试模板
     */
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // 设置重试策略：最多重试3次
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        // 设置退避策略：指数退避
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);
        backOffPolicy.setMaxInterval(10000);
        backOffPolicy.setMultiplier(2);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }
}
