package com.langchain4j.scenarios.scenario3.service;

import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 代码审查历史管理器 - 审查记录和统计管理
 *
 * 职责说明：
 * - 记录代码审查的历史和统计数据
 * - 管理审查记录的存储和查询
 * - 支持按编程语言的统计分析
 * - 记录审查的问题数量和时间戳
 * - 支持审查历史的查询和分析
 *
 * 架构设计：
 * - 使用 @Component 注解注册为 Spring Bean
 * - 使用 ConcurrentHashMap 存储审查历史（线程安全）
 * - 内嵌 ReviewRecord 静态类存储审查信息
 * - 使用 Lombok 注解简化代码
 * - 支持并发访问和修改
 *
 * 使用场景：
 * - 记录每次代码审查的结果
 * - 统计不同编程语言的审查数量
 * - 分析代码审查的趋势
 * - 生成审查报告和统计
 * - 用于性能监控和分析
 *
 * 工作原理：
 * 1. 每次代码审查完成后，记录审查信息
 * 2. 生成唯一的审查记录ID
 * 3. 存储审查记录到历史记录中
 * 4. 更新语言统计信息
 * 5. 支持查询审查历史和统计数据
 *
 * 字段说明：
 * - reviewHistory: 审查历史记录
 *   * 类型：ConcurrentHashMap<String, ReviewRecord>
 *   * key：recordId（审查记录唯一标识）
 *   * value：审查记录对象
 *   * 用途：存储所有审查记录
 *
 * - languageStats: 语言统计
 *   * 类型：ConcurrentHashMap<String, Integer>
 *   * key：编程语言（java、python等）
 *   * value：该语言的审查次数
 *   * 用途：统计各语言的审查数量
 *
 * ReviewRecord 字段说明：
 * - recordId: 审查记录唯一标识
 *   * 格式：UUID格式
 *   * 用途：唯一标识每次审查
 *
 * - language: 编程语言
 *   * 格式：语言标识符
 *   * 用途：记录审查的代码语言
 *
 * - issueCount: 问题数量
 *   * 格式：整数
 *   * 用途：记录审查发现的问题数
 *
 * - timestamp: 审查时间戳
 *   * 格式：毫秒级时间戳
 *   * 用途：记录审查的时间
 *
 * 性能考虑：
 * - 使用 ConcurrentHashMap 支持高并发访问
 * - 记录添加时间复杂度为 O(1)
 * - 统计查询时间复杂度为 O(1)
 * - 支持数千个审查记录
 * - 建议定期清理过期记录
 * - 可以配置记录保留时间
 * - 监控记录数量和内存占用
 *
 * 安全考虑：
 * - 验证审查数据的有效性
 * - 防止历史记录被篡改
 * - 记录操作日志
 * - 实现历史记录的访问控制
 * - 防止历史信息泄露
 *
 * 可靠性考虑：
 * - 历史记录存储在内存中，应用重启会丢失
 * - 建议使用数据库或Redis持久化存储
 * - 实现历史记录的备份和恢复机制
 * - 处理并发访问的竞态条件
 * - 实现历史记录的一致性检查
 *
 * 扩展建议：
 * - 可以使用数据库替代内存存储
 * - 可以添加历史记录的持久化功能
 * - 可以实现历史记录的分布式管理
 * - 可以添加历史记录的加密存储
 * - 可以支持历史记录的导出和导入
 * - 可以实现历史记录的版本控制
 * - 可以添加历史记录的审计日志
 */
@Component
public class CodeReviewHistoryManager {

    /** 审查历史记录 */
    private final Map<String, ReviewRecord> reviewHistory = new ConcurrentHashMap<>();

    /** 语言统计 */
    private final Map<String, Integer> languageStats = new ConcurrentHashMap<>();

    /**
     * 记录审查
     *
     * @param language 编程语言
     * @param issueCount 问题数
     */
    public void recordReview(String language, int issueCount) {
        String recordId = UUID.randomUUID().toString();
        ReviewRecord record = ReviewRecord.builder()
                .recordId(recordId)
                .language(language)
                .issueCount(issueCount)
                .timestamp(System.currentTimeMillis())
                .build();
        reviewHistory.put(recordId, record);
        languageStats.merge(language, 1, Integer::sum);
    }

    /**
     * 获取语言统计
     *
     * @return 语言统计信息
     */
    public Map<String, Integer> getLanguageStats() {
        return new HashMap<>(languageStats);
    }

    /**
     * 获取总审查数
     *
     * @return 总审查数
     */
    public int getTotalReviews() {
        return reviewHistory.size();
    }

    /**
     * 审查记录
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReviewRecord {
        private String recordId;
        private String language;
        private int issueCount;
        private long timestamp;
    }
}
