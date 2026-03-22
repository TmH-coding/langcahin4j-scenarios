package com.langchain4j.scenarios.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步任务执行器配置 - 线程池管理和异步处理
 *
 * 职责说明：
 * - 配置线程池用于异步处理长时间运行的任务
 * - 提高应用响应速度，避免阻塞主线程
 * - 支持 @Async 注解的异步方法执行
 * - 管理线程生命周期和资源
 *
 * 架构设计：
 * - 使用 ThreadPoolTaskExecutor 作为线程池实现
 * - 配置核心线程数、最大线程数、队列容量
 * - 优雅关闭：等待所有任务完成后再关闭
 * - 线程命名便于调试和监控
 *
 * 使用场景：
 * - 异步发送消息到 RabbitMQ
 * - 异步记录审计日志
 * - 异步调用外部 API
 * - 异步生成报告或导出数据
 * - 长时间运行的 LLM 推理任务
 * - 异步发送邮件、短信等通知
 *
 * 线程池工作原理：
 * 1. 提交任务时，如果核心线程未满，创建新线程
 * 2. 核心线程满后，任务进入队列等待
 * 3. 队列满后，如果总线程数未达最大，创建新线程
 * 4. 所有线程都忙且队列满，任务被拒绝
 * 5. 应用关闭时，等待所有任务完成后再关闭线程池
 *
 * 性能考虑：
 * - 线程创建和销毁有开销
 * - 过多线程会增加上下文切换开销
 * - 应该根据 CPU 核心数和任务特性调整参数
 * - 监控线程池的使用情况
 *
 * 安全考虑：
 * - 异步任务中的异常应该被捕获和处理
 * - 避免在异步任务中修改共享状态
 * - 使用线程安全的数据结构
 * - 实现任务超时机制
 */
@Configuration
public class AsyncConfig {

    /**
     * 创建异步任务执行器
     *
     * 功能：
     * - 创建 Spring 管理的线程池
     * - 支持 @Async 注解的异步方法
     * - 提供优雅的关闭机制
     * - 自动处理线程生命周期
     *
     * 线程池参数详解：
     *
     * corePoolSize = 5
     * - 核心线程数，始终保持活跃
     * - 即使空闲也不会被销毁
     * - 应用启动时会创建这些线程
     * - 建议值：
     *   * CPU 密集型：CPU 核心数
     *   * IO 密集型：CPU 核心数 * 2-4
     *   * 混合型：CPU 核心数 * 1.5-2
     * - 当前设置 5 适合中等规模应用
     *
     * maxPoolSize = 10
     * - 最大线程数
     * - 当队列满时，会创建新线程直到达到此数
     * - 超过此数的任务会被拒绝
     * - 建议值：corePoolSize 的 2-3 倍
     * - 当前设置 10 = 5 * 2，合理
     * - 过大会浪费资源，过小会导致任务被拒绝
     *
     * queueCapacity = 100
     * - 任务队列容量
     * - 当所有核心线程都忙时，任务进入此队列
     * - 队列满后才会创建新线程
     * - 建议值：根据任务数量调整，通常 50-200
     * - 当前设置 100 适合中等并发
     * - 过小会导致任务被拒绝，过大会占用内存
     *
     * threadNamePrefix = "langchain4j-async-"
     * - 线程名前缀
     * - 便于在日志和监控中识别线程
     * - 示例：langchain4j-async-1, langchain4j-async-2
     * - 便于调试和性能分析
     *
     * waitForTasksToCompleteOnShutdown = true
     * - 应用关闭时是否等待任务完成
     * - true: 等待所有任务完成后再关闭（推荐）
     * - false: 立即关闭，可能丢失任务
     * - 当前设置 true 确保任务不会丢失
     * - 适合需要保证数据一致性的场景
     *
     * awaitTerminationSeconds = 60
     * - 等待任务完成的最长时间（秒）
     * - 如果超过此时间，强制关闭线程池
     * - 防止应用无限期等待
     * - 当前设置 60 秒，适合大多数场景
     * - 可根据任务特性调整：
     *   * 快速任务：30 秒
     *   * 长时间任务：120-300 秒
     *
     * 使用示例 - 基础异步方法：
     * @Service
     * public class MessageService {
     *     @Async("taskExecutor")
     *     public void sendMessageAsync(String message) {
     *         // 这个方法会在线程池中异步执行
     *         // 不会阻塞调用者
     *         rabbitTemplate.convertAndSend("exchange", "key", message);
     *     }
     * }
     *
     * // 调用异步方法
     * messageService.sendMessageAsync("Hello");  // 立即返回
     *
     * 使用示例 - 异步方法返回 Future：
     * @Service
     * public class ReportService {
     *     @Async("taskExecutor")
     *     public CompletableFuture<String> generateReportAsync(String reportId) {
     *         try {
     *             String result = generateReport(reportId);
     *             return CompletableFuture.completedFuture(result);
     *         } catch (Exception e) {
     *             return CompletableFuture.failedFuture(e);
     *         }
     *     }
     * }
     *
     * // 调用异步方法并获取结果
     * CompletableFuture<String> future = reportService.generateReportAsync("report-123");
     * future.thenAccept(result -> System.out.println("Report: " + result));
     *
     * 使用示例 - 异步方法异常处理：
     * @Service
     * public class NotificationService {
     *     @Async("taskExecutor")
     *     public void sendNotificationAsync(String userId, String message) {
     *         try {
     *             // 发送通知
     *             sendNotification(userId, message);
     *         } catch (Exception e) {
     *             log.error("Failed to send notification to user: {}", userId, e);
     *             // 可以选择重试或记录到死信队列
     *         }
     *     }
     * }
     *
     * 线程池监控：
     * - 监控活跃线程数
     * - 监控队列大小
     * - 监控任务拒绝率
     * - 监控任务执行时间
     *
     * 常见问题：
     * 1. 异步方法不生效
     *    - 确保类上有 @Service 或 @Component 注解
     *    - 确保方法上有 @Async 注解
     *    - 确保通过 Spring 容器调用（不能直接 new）
     *    - 确保方法不是 private（代理无法拦截）
     *
     * 2. 任务被拒绝
     *    - 增加 queueCapacity
     *    - 增加 maxPoolSize
     *    - 优化任务处理速度
     *    - 实现任务优先级队列
     *
     * 3. 内存泄漏
     *    - 确保异步任务正确完成
     *    - 避免在异步任务中创建大量对象
     *    - 定期监控内存使用
     *    - 实现任务超时机制
     *
     * 最佳实践：
     * 1. 异步方法应该是 void 或返回 Future/CompletableFuture
     * 2. 异步方法中应该有异常处理
     * 3. 避免在异步方法中修改共享状态
     * 4. 使用线程安全的数据结构
     * 5. 监控线程池的使用情况
     * 6. 定期审查和调整线程池参数
     *
     * @return Executor 配置好的线程池执行器
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);                              // 核心线程数
        executor.setMaxPoolSize(10);                              // 最大线程数
        executor.setQueueCapacity(100);                           // 队列容量
        executor.setThreadNamePrefix("langchain4j-async-");       // 线程名前缀
        executor.setWaitForTasksToCompleteOnShutdown(true);       // 关闭时等待任务完成
        executor.setAwaitTerminationSeconds(60);                  // 等待时间（秒）
        executor.initialize();                                    // 初始化线程池
        return executor;
    }
}
