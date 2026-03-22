package com.langchain4j.scenarios.common.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

/**
 * 定时任务配置 - 异步调度和周期性任务
 *
 * 职责说明：
 * - 启用 Spring 定时任务功能
 * - 配置定时任务线程池
 * - 支持 @Scheduled 注解的周期性任务
 * - 管理定时任务的生命周期
 *
 * 架构设计：
 * - 使用 @EnableScheduling 启用定时任务
 * - 使用 ThreadPoolTaskScheduler 管理定时任务线程
 * - 支持 cron 表达式、固定延迟、固定速率等调度方式
 * - 优雅关闭：等待所有任务完成后再关闭
 *
 * 使用场景：
 * - 定期清理过期的缓存数据
 * - 定期同步数据库数据
 * - 定期生成报告或统计数据
 * - 定期检查系统健康状态
 * - 定期发送通知或提醒
 * - 定期执行数据备份
 *
 * 定时任务工作原理：
 * 1. 应用启动时，扫描所有 @Scheduled 注解的方法
 * 2. 根据调度规则（cron、固定延迟等）计算下次执行时间
 * 3. 在指定时间到达时，从线程池中获取线程执行任务
 * 4. 任务执行完成后，计算下次执行时间
 * 5. 重复直到应用关闭
 *
 * 安全考虑：
 * - 定时任务应该是幂等的（多次执行结果相同）
 * - 避免长时间运行的任务阻塞线程池
 * - 监控定时任务的执行时间和失败率
 * - 在分布式环境中需要防止重复执行
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {

    /**
     * 配置定时任务线程池
     *
     * 功能：
     * - 创建 ThreadPoolTaskScheduler Bean
     * - 配置线程池大小和线程命名
     * - 设置优雅关闭策略
     * - 支持多个定时任务并发执行
     *
     * 线程池参数详解：
     *
     * poolSize = 5
     * - 定时任务线程池的大小
     * - 同时可以执行的定时任务数量
     * - 如果有超过 5 个任务同时到达执行时间，其他任务会排队等待
     * - 建议值：
     *   * 小型应用：2-5
     *   * 中型应用：5-10
     *   * 大型应用：10-20
     * - 注意：不要设置过大，会浪费系统资源
     *
     * threadNamePrefix = "langchain4j-scheduler-"
     * - 线程名前缀
     * - 便于在日志和监控中识别定时任务线程
     * - 示例：langchain4j-scheduler-1, langchain4j-scheduler-2
     * - 线程名格式：前缀 + 序号
     *
     * awaitTerminationSeconds = 60
     * - 应用关闭时等待任务完成的最长时间（秒）
     * - 如果超过此时间，强制关闭线程池
     * - 防止应用无限期等待
     * - 建议值：30-120 秒
     *
     * waitForTasksToCompleteOnShutdown = true
     * - 应用关闭时是否等待任务完成
     * - true: 等待所有任务完成后再关闭（推荐）
     * - false: 立即关闭，可能丢失任务
     * - 与 awaitTerminationSeconds 配合使用
     *
     * 使用示例 - 固定延迟：
     * @Service
     * public class DataSyncService {
     *     @Scheduled(fixedDelay = 60000)  // 每次执行完成后延迟 60 秒再执行
     *     public void syncData() {
     *         // 同步数据逻辑
     *     }
     * }
     *
     * 使用示例 - 固定速率：
     * @Service
     * public class HealthCheckService {
     *     @Scheduled(fixedRate = 30000)  // 每 30 秒执行一次（不管上次是否完成）
     *     public void checkHealth() {
     *         // 健康检查逻辑
     *     }
     * }
     *
     * 使用示例 - Cron 表达式：
     * @Service
     * public class ReportService {
     *     @Scheduled(cron = "0 0 2 * * *")  // 每天凌晨 2 点执行
     *     public void generateDailyReport() {
     *         // 生成报告逻辑
     *     }
     *
     *     @Scheduled(cron = "0 * 5 * * * *")  // 每 5 分钟执行一次
     *     public void cleanupCache() {
     *         // 清理缓存逻辑
     *     }
     * }
     *
     * Cron 表达式格式：
     * 秒 分 小时 日期 月份 星期 [年份]
     * 0   0   2    *    *    *    - 每天凌晨 2 点
     * 0   5 *    *    *    *    - 每 5 分钟
     * 0   0   0    1    *    *    - 每月 1 号凌晨
     * 0   0   0    *    *    1    - 每周一凌晨
     *
     * 定时任务最佳实践：
     * 1. 任务应该是幂等的
     *    - 多次执行结果相同
     *    - 避免重复执行导致数据不一致
     *
     * 2. 避免长时间运行的任务
     *    - 如果任务耗时长，使用异步执行
     *    - 或者增加线程池大小
     *
     * 3. 添加异常处理
     *    - 定时任务异常不会自动重试
     *    - 需要手动处理异常和重试逻辑
     *
     * 4. 监控和日志
     *    - 记录任务执行时间
     *    - 监控任务失败率
     *    - 及时发现和处理问题
     *
     * 5. 分布式环境
     *    - 使用分布式锁防止重复执行
     *    - 或者使用专门的定时任务框架（如 XXL-Job）
     *
     * 示例 - 完整的定时任务：
     * @Service
     * public class CacheCleanupService {
     *     private static final Logger logger = LoggerFactory.getLogger(CacheCleanupService.class);
     *
     *     @Scheduled(cron = "0 0 3 * * *")  // 每天凌晨 3 点执行
     *     public void cleanupExpiredCache() {
     *         try {
     *             long startTime = System.currentTimeMillis();
     *             logger.info("开始清理过期缓存");
     *
     *             // 清理逻辑
     *             int deletedCount = cacheService.deleteExpiredEntries();
     *
     *             long duration = System.currentTimeMillis() - startTime;
     *             logger.info("清理完成，删除 {} 条记录，耗时 {} ms", deletedCount, duration);
     *         } catch (Exception e) {
     *             logger.error("清理缓存失败", e);
     *             // 发送告警
     *             alertService.sendAlert("缓存清理失败: " + e.getMessage());
     *         }
     *     }
     * }
     *
     * @return ThreadPoolTaskScheduler 配置好的定时任务调度器
     */
    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("langchain4j-scheduler-");
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.initialize();
        return scheduler;
    }
}
