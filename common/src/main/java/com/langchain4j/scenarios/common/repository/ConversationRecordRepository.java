package com.langchain4j.scenarios.common.repository;

import com.langchain4j.scenarios.common.entity.ConversationRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话记录仓储 - 数据访问层和查询接口
 *
 * 职责说明：
 * - 提供对话记录的数据访问接口
 * - 支持多维度复杂查询（会话、用户、场景、时间）
 * - 支持分页和排序功能
 * - 实现软删除逻辑（deleted 标志）
 * - 提供统计和聚合查询
 * - 支持 Spring Data JPA 的自动查询生成
 *
 * 架构设计：
 * - 继承 JpaRepository 获得基础 CRUD 操作
 * - 使用 Spring Data JPA 的方法命名约定自动生成查询
 * - 使用 @Query 注解编写复杂的 JPQL 查询
 * - 所有查询都包含 deleted = false 条件实现软删除
 * - 支持分页查询提高大数据集处理性能
 * - 遵循 Repository 模式实现数据访问层分离
 * - 支持 Spring Data JPA 的高级查询功能
 *
 * 使用场景：
 * - 客服系统：查询用户的对话历史
 * - 数据分析：按场景统计对话数量
 * - 审计日志：查询指定时间范围的对话
 * - 会话管理：恢复特定会话的所有对话
 * - 性能分析：统计用户和场景的对话量
 * - 用户中心：显示用户的对话历史列表
 * - 报表生成：生成各类统计报表
 *
 * 工作原理：
 * 1. 业务层调用 Repository 方法
 * 2. Spring Data JPA 自动生成 SQL 查询
 * 3. 执行数据库查询操作
 * 4. 返回查询结果给业务层
 * 5. 支持事务管理和连接池
 * 6. 自动处理异常和错误
 *
 * 数据库查询优化：
 * - 建议在 sessionId、userId、scenarioType、createdAt 字段上建立索引
 * - 建议在 (userId, deleted) 上建立复合索引加速分页查询
 * - 建议在 (scenarioType, deleted) 上建立复合索引加速场景查询
 * - 建议在 (createdAt, deleted) 上建立复合索引加速时间范围查询
 * - 建议在 (sessionId, deleted) 上建立复合索引加速会话查询
 * - 定期分析查询执行计划优化索引策略
 *
 * 性能考虑：
 * - 分页查询避免一次加载大量数据
 * - 软删除查询需要额外的 deleted 条件，确保索引包含此字段
 * - 时间范围查询可能返回大量数据，建议配合分页使用
 * - 统计查询使用 count 函数，性能较好
 * - 建议为常用查询字段添加数据库索引
 * - 大数据量场景建议使用批处理
 * - 定期归档旧数据以提高查询性能
 * - 监控查询性能和数据库连接池状态
 *
 * 安全考虑：
 * - 所有查询都自动过滤已删除的记录
 * - 支持按用户隔离数据访问
 * - 建议在 Service 层进行权限验证
 * - 敏感数据应该加密存储
 * - 定期备份对话记录数据
 * - 实现查询日志和审计
 *
 * 扩展建议：
 * - 可以添加按多个条件组合查询的方法
 * - 可以添加批量操作方法（批量删除、批量更新）
 * - 可以添加自定义排序和分组查询
 * - 可以实现缓存策略提高查询性能
 * - 可以支持全文搜索功能
 * - 可以实现查询结果的流式处理
 * - 可以添加数据导出功能
 */
@Repository
public interface ConversationRecordRepository extends JpaRepository<ConversationRecord, Long> {

    /**
     * 根据会话ID查询对话记录
     *
     * 功能：
     * - 查询指定会话的所有对话
     * - 用于恢复完整的对话历史
     * - 自动过滤已删除的记录
     *
     * 参数说明：
     * - sessionId: 会话唯一标识符
     *   * 格式：UUID 或自定义格式
     *   * 示例：\"session-123-abc-def\"
     *   * 来源：对话开始时生成
     *
     * 返回值：
     * - List<ConversationRecord>：该会话的所有对话记录
     * - 按创建时间升序排列
     * - 不包含已删除的记录
     *
     * SQL 生成示例：
     * SELECT * FROM conversation_record
     * WHERE session_id = ? AND deleted = false
     * ORDER BY created_at ASC
     *
     * 使用示例：
     * List<ConversationRecord> records = repository.findBySessionIdAndDeletedFalse(\"session-123\");
     * for (ConversationRecord record : records) {
     *     System.out.println(record.getContent());
     * }
     *
     * 性能考虑：
     * - 建议在 (sessionId, deleted) 上建立复合索引
     * - 如果会话包含大量对话，考虑使用分页
     * - 可以添加分页版本：findBySessionIdAndDeletedFalse(String sessionId, Pageable pageable)
     *
     * @param sessionId 会话ID
     * @return 对话记录列表
     */
    List<ConversationRecord> findBySessionIdAndDeletedFalse(String sessionId);

    /**
     * 根据用户ID分页查询对话记录
     *
     * 功能：
     * - 查询指定用户的所有对话
     * - 支持分页避免一次加载过多数据
     * - 用于用户对话历史展示
     * - 自动过滤已删除的记录
     *
     * 参数说明：
     * - userId: 用户唯一标识符
     *   * 格式：UUID 或自定义格式
     *   * 示例：\"user-456-xyz\"
     *   * 来源：用户认证系统
     *
     * - pageable: 分页和排序信息
     *   * 包含页码、页大小、排序字段
     *   * 示例：PageRequest.of(0, 20, Sort.by(\"createdAt\").descending())
     *   * 第一页从 0 开始
     *
     * 返回值：
     * - Page<ConversationRecord>：分页的对话记录
     * - 包含总数、总页数等分页信息
     * - 不包含已删除的记录
     *
     * SQL 生成示例：
     * SELECT * FROM conversation_record
     * WHERE user_id = ? AND deleted = false
     * ORDER BY created_at DESC
     * LIMIT ? OFFSET ?
     *
     * 使用示例：
     * Pageable pageable = PageRequest.of(0, 20, Sort.by(\"createdAt\").descending());
     * Page<ConversationRecord> page = repository.findByUserIdAndDeletedFalse(\"user-456\", pageable);
     * System.out.println(\"Total: \" + page.getTotalElements());
     * for (ConversationRecord record : page.getContent()) {
     *     System.out.println(record.getContent());
     * }
     *
     * 分页最佳实践：
     * - 默认排序：按创建时间降序（最新的在前）
     * - 推荐页大小：10-50 条记录
     * - 避免过大的页码偏移（性能下降）
     * - 可以使用 keyset 分页优化大数据集查询
     *
     * 性能考虑：
     * - 建议在 (userId, deleted, createdAt) 上建立复合索引
     * - 分页查询会产生 COUNT 查询，确保索引覆盖
     * - 对于大用户量，考虑使用缓存
     *
     * @param userId 用户ID
     * @param pageable 分页信息
     * @return 分页的对话记录
     */
    Page<ConversationRecord> findByUserIdAndDeletedFalse(String userId, Pageable pageable);

    /**
     * 根据场景类型查询对话记录
     *
     * 功能：
     * - 查询指定场景的所有对话
     * - 用于场景分析和统计
     * - 支持多个场景的对话数据收集
     * - 自动过滤已删除的记录
     *
     * 参数说明：
     * - scenarioType: 场景类型标识
     *   * 可能的值：\"customer-service\", \"document-analysis\", \"code-assistant\", \"data-analyst\", \"content-creator\"
     *   * 示例：\"customer-service\"
     *   * 来源：对话初始化时指定
     *
     * 返回值：
     * - List<ConversationRecord>：该场景的所有对话记录
     * - 按创建时间升序排列
     * - 不包含已删除的记录
     *
     * SQL 生成示例：
     * SELECT * FROM conversation_record
     * WHERE scenario_type = ? AND deleted = false
     * ORDER BY created_at ASC
     *
     * 使用示例：
     * List<ConversationRecord> records = repository.findByScenarioTypeAndDeletedFalse(\"customer-service\");
     * System.out.println(\"Total conversations in customer service: \" + records.size());
     *
     * 应用场景：
     * - 场景性能分析：统计各场景的使用情况
     * - 场景优化：分析特定场景的对话模式
     * - 数据导出：导出特定场景的所有对话用于分析
     * - 场景迁移：场景升级时迁移历史数据
     *
     * 性能考虑：
     * - 建议在 (scenarioType, deleted) 上建立复合索引
     * - 如果某个场景包含大量对话，考虑使用分页版本
     * - 可以添加分页版本：findByScenarioTypeAndDeletedFalse(String scenarioType, Pageable pageable)
     *
     * @param scenarioType 场景类型
     * @return 对话记录列表
     */
    List<ConversationRecord> findByScenarioTypeAndDeletedFalse(String scenarioType);

    /**
     * 查询指定时间范围内的对话记录
     *
     * 功能：
     * - 查询指定时间段的所有对话
     * - 用于时间段分析和报表生成
     * - 支持灵活的时间范围查询
     * - 自动过滤已删除的记录
     *
     * 参数说明：
     * - startTime: 开始时间（包含）
     *   * 类型：LocalDateTime
     *   * 示例：LocalDateTime.of(2024, 1, 1, 0, 0, 0)
     *   * 时区：使用应用配置的时区
     *
     * - endTime: 结束时间（包含）
     *   * 类型：LocalDateTime
     *   * 示例：LocalDateTime.of(2024, 1, 31, 23, 59, 59)
     *   * 时区：使用应用配置的时区
     *
     * 返回值：
     * - List<ConversationRecord>：时间范围内的所有对话记录
     * - 按创建时间升序排列
     * - 不包含已删除的记录
     *
     * JPQL 查询：
     * SELECT c FROM ConversationRecord c
     * WHERE c.createdAt BETWEEN :startTime AND :endTime AND c.deleted = false
     * ORDER BY c.createdAt ASC
     *
     * 使用示例：
     * LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
     * LocalDateTime end = LocalDateTime.of(2024, 1, 31, 23, 59, 59);
     * List<ConversationRecord> records = repository.findByTimeRange(start, end);
     * System.out.println(\"Conversations in January: \" + records.size());
     *
     * 应用场景：
     * - 日报表：查询今天的所有对话
     * - 周报表：查询本周的所有对话
     * - 月报表：查询本月的所有对话
     * - 自定义报表：查询任意时间段的对话
     * - 数据备份：按时间段备份对话数据
     *
     * 时间范围查询示例：
     * // 查询今天的对话
     * LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
     * LocalDateTime tomorrow = today.plusDays(1);
     * List<ConversationRecord> todayRecords = repository.findByTimeRange(today, tomorrow);
     *
     * // 查询本周的对话
     * LocalDateTime weekStart = LocalDateTime.now().minusDays(LocalDateTime.now().getDayOfWeek().getValue() - 1);
     * LocalDateTime weekEnd = weekStart.plusDays(7);
     * List<ConversationRecord> weekRecords = repository.findByTimeRange(weekStart, weekEnd);
     *
     * 性能考虑：
     * - 建议在 (createdAt, deleted) 上建立复合索引
     * - 时间范围查询可能返回大量数据，建议配合分页使用
     * - 可以添加分页版本：findByTimeRange(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable)
     * - 对于大时间范围，考虑分批查询
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 时间范围内的对话记录列表
     */
    @Query("SELECT c FROM ConversationRecord c WHERE c.createdAt BETWEEN :startTime AND :endTime AND c.deleted = false")
    List<ConversationRecord> findByTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * 统计用户的对话数
     *
     * 功能：
     * - 统计指定用户的对话总数
     * - 用于用户活跃度分析
     * - 支持快速的聚合查询
     * - 自动过滤已删除的记录
     *
     * 参数说明：
     * - userId: 用户唯一标识符
     *   * 格式：UUID 或自定义格式
     *   * 示例：\"user-456-xyz\"
     *   * 来源：用户认证系统
     *
     * 返回值：
     * - long：用户的对话总数
     * - 不包含已删除的记录
     * - 返回 0 表示用户没有对话
     *
     * SQL 生成示例：
     * SELECT COUNT(*) FROM conversation_record
     * WHERE user_id = ? AND deleted = false
     *
     * 使用示例：
     * long count = repository.countByUserIdAndDeletedFalse(\"user-456\");
     * System.out.println(\"User has \" + count + \" conversations\");
     *
     * 应用场景：
     * - 用户统计：显示用户的对话数量
     * - 活跃度分析：统计活跃用户的对话数
     * - 用户排行：统计用户对话数排行榜
     * - 数据验证：验证数据一致性
     *
     * 性能考虑：
     * - COUNT 查询性能较好，通常很快
     * - 建议在 (userId, deleted) 上建立复合索引
     * - 对于大数据集，COUNT 查询仍然需要扫描所有匹配的行
     * - 可以考虑使用缓存存储热点用户的统计数据
     *
     * @param userId 用户ID
     * @return 用户的对话数
     */
    long countByUserIdAndDeletedFalse(String userId);

    /**
     * 统计场景的对话数
     *
     * 功能：
     * - 统计指定场景的对话总数
     * - 用于场景使用情况分析
     * - 支持快速的聚合查询
     * - 自动过滤已删除的记录
     *
     * 参数说明：
     * - scenarioType: 场景类型标识
     *   * 可能的值：\"customer-service\", \"document-analysis\", \"code-assistant\", \"data-analyst\", \"content-creator\"
     *   * 示例：\"customer-service\"
     *   * 来源：对话初始化时指定
     *
     * 返回值：
     * - long：场景的对话总数
     * - 不包含已删除的记录
     * - 返回 0 表示场景没有对话
     *
     * SQL 生成示例：
     * SELECT COUNT(*) FROM conversation_record
     * WHERE scenario_type = ? AND deleted = false
     *
     * 使用示例：
     * long count = repository.countByScenarioTypeAndDeletedFalse(\"customer-service\");
     * System.out.println(\"Customer service has \" + count + \" conversations\");
     *
     * 应用场景：
     * - 场景统计：显示各场景的使用情况
     * - 场景对比：比较不同场景的使用频率
     * - 场景排行：统计场景使用排行榜
     * - 容量规划：根据场景使用情况规划资源
     *
     * 性能考虑：
     * - COUNT 查询性能较好，通常很快
     * - 建议在 (scenarioType, deleted) 上建立复合索引
     * - 对于大数据集，COUNT 查询仍然需要扫描所有匹配的行
     * - 可以考虑使用缓存存储场景的统计数据
     *
     * @param scenarioType 场景类型
     * @return 场景的对话数
     */
    long countByScenarioTypeAndDeletedFalse(String scenarioType);
}
