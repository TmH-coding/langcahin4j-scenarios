package com.langchain4j.scenarios.scenario5.service;

import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内容生成统计器 - 内容生成历史和统计管理
 *
 * 职责说明：
 * - 记录内容生成的历史和统计数据
 * - 管理生成记录的存储和查询
 * - 支持按内容类型的统计分析
 * - 记录生成的字数和性能指标
 * - 支持生成历史的查询和分析
 *
 * 架构设计：
 * - 使用 @Component 注解注册为 Spring Bean
 * - 使用 ConcurrentHashMap 存储生成历史（线程安全）
 * - 内嵌 GenerationRecord 静态类存储生成信息
 * - 使用 Lombok 注解简化代码
 * - 支持并发访问和修改
 *
 * 使用场景：
 * - 记录每次内容生成的结果
 * - 统计不同内容类型的生成数量
 * - 分析生成性能和字数统计
 * - 生成生成报告和统计
 * - 用于性能监控和分析
 *
 * 工作原理：
 * 1. 每次内容生成后，记录生成信息
 * 2. 生成唯一的生成记录ID
 * 3. 存储生成记录到历史记录中
 * 4. 更新内容类型统计信息
 * 5. 累计总字数统计
 * 6. 支持生成历史和统计数据的查询
 *
 * 字段说明：
 * - generationHistory: 生成历史记录
 *   * 类型：ConcurrentHashMap<String, GenerationRecord>
 *   * key：recordId（生成记录唯一标识）
 *   * value：生成记录对象
 *   * 用途：存储所有生成记录
 *
 * - contentTypeStats: 内容类型统计
 *   * 类型：ConcurrentHashMap<String, Integer>
 *   * key：内容类型（article、social-post等）
 *   * value：该内容类型的生成次数
 *   * 用途：统计各内容类型的生成数量
 *
 * - totalWordCount: 总字数统计
 *   * 类型：long
 *   * 用途：记录所有生成内容的总字数
 *
 * GenerationRecord 字段说明：
 * - recordId: 生成记录唯一标识
 *   * 格式：UUID格式
 *   * 用途：唯一标识每次生成
 *
 * - contentType: 内容类型
 *   * 格式：内容类型标识符
 *   * 用途：记录生成的内容类型
 *
 * - wordCount: 字数统计
 *   * 格式：整数
 *   * 用途：记录生成内容的字数
 *
 * - timestamp: 生成时间戳
 *   * 格式：毫秒级时间戳
 *   * 用途：记录生成的执行时间
 *
 * 性能考虑：
 * - 使用 ConcurrentHashMap 支持高并发访问
 * - 记录添加时间复杂度为 O(1)
 * - 统计查询时间复杂度为 O(1)
 * - 支持数千个生成记录
 * - 建议定期清理过期记录
 * - 可以配置记录保留时间
 * - 监控记录数量和内存占用
 *
 * 安全考虑：
 * - 验证生成数据的有效性
 * - 防止历史记录被篡改
 * - 记录操作日志
 * - 实现历史记录的访问控制
 * - 防止生成信息泄露
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
 * - 可以支持按时间范围的统计查询
 */
@Component
public class ContentGenerationStatistics {

    /** 生成历史记录 */
    private final Map<String, GenerationRecord> generationHistory = new ConcurrentHashMap<>();

    /** 内容类型统计 */
    private final Map<String, Integer> contentTypeStats = new ConcurrentHashMap<>();

    /** 总字数统计 */
    private long totalWordCount = 0;

    /**
     * 记录生成
     *
     * @param contentType 内容类型
     * @param wordCount 字数
     */
    public void recordGeneration(String contentType, int wordCount) {
        String recordId = UUID.randomUUID().toString();
        GenerationRecord record = GenerationRecord.builder()
                .recordId(recordId)
                .contentType(contentType)
                .wordCount(wordCount)
                .timestamp(System.currentTimeMillis())
                .build();
        generationHistory.put(recordId, record);
        contentTypeStats.merge(contentType, 1, Integer::sum);
        totalWordCount += wordCount;
    }

    /**
     * 获取内容类型统计
     *
     * @return 内容类型统计信息
     */
    public Map<String, Integer> getContentTypeStats() {
        return new HashMap<>(contentTypeStats);
    }

    /**
     * 获取总生成数
     *
     * @return 总生成数
     */
    public int getTotalGenerations() {
        return generationHistory.size();
    }

    /**
     * 获取总字数
     *
     * @return 总字数
     */
    public long getTotalWordCount() {
        return totalWordCount;
    }

    /**
     * 生成记录
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class GenerationRecord {
        private String recordId;
        private String contentType;
        private int wordCount;
        private long timestamp;
    }
}
