package com.langchain4j.scenarios.common.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志仓储 - 数据访问层
 *
 * 职责说明：
 * - 提供审计日志的数据访问接口
 * - 支持多维度的审计日志查询
 * - 使用 Spring Data JPA 简化数据库操作
 * - 自动生成 SQL 查询语句
 * - 支持审计日志的持久化和检索
 *
 * 架构设计：
 * - 继承 JpaRepository 获得基础 CRUD 操作
 * - 使用方法名约定自动生成查询（Query by Example）
 * - 支持自定义查询方法
 * - 支持分页和排序
 * - 遵循 Repository 模式实现数据访问层分离
 * - 支持 Spring Data JPA 的高级查询功能
 *
 * 使用场景：
 * - 查询特定用户的操作记录
 * - 查询特定操作类型的记录
 * - 查询时间范围内的操作
 * - 生成审计报告
 * - 安全审计和合规性检查
 * - 用户行为分析
 * - 故障排查和事件追踪
 *
 * 工作原理：
 * 1. 业务层调用 Repository 方法
 * 2. Spring Data JPA 自动生成 SQL 查询
 * 3. 执行数据库查询操作
 * 4. 返回查询结果给业务层
 * 5. 支持事务管理和连接池
 * 6. 自动处理异常和错误
 *
 * 性能优化建议：
 * - 在 operator 字段上创建索引
 * - 在 operationType 字段上创建索引
 * - 在 operationTime 字段上创建索引
 * - 在 (operator, operationTime) 上创建复合索引
 * - 在 (operationType, operationTime) 上创建复合索引
 * - 定期清理过期数据
 * - 使用分页避免一次加载过多数据
 * - 建议为常用查询字段添加数据库索引
 * - 大数据量场景建议使用批处理
 * - 定期归档旧审计日志以提高查询性能
 *
 * 安全考虑：
 * - 所有查询都在事务内执行
 * - 审计日志应该完整记录所有操作
 * - 应该包含足够的信息用于追踪和调查
 * - 限制审计日志的访问权限
 * - 防止审计日志被篡改或删除
 * - 敏感信息应该加密存储
 * - 定期备份审计日志数据
 * - 实现审计日志的访问控制
 *
 * 合规性考虑：
 * - 审计日志应该满足法规要求（如 GDPR、SOX）
 * - 应该保留足够长的时间（通常 1-7 年）
 * - 应该支持审计日志的导出和报告
 * - 应该实现审计日志的完整性验证
 * - 应该支持审计日志的加密存储
 *
 * 扩展建议：
 * - 可以添加按 targetObject 查询的方法
 * - 可以添加按 result 查询的方法
 * - 可以添加按 ipAddress 查询的方法
 * - 可以添加复杂的组合查询方法
 * - 可以添加统计和聚合查询
 * - 可以实现审计日志的全文搜索
 * - 可以支持审计日志的导出功能
 * - 可以实现审计日志的可视化分析
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    /**
     * 根据操作用户查询审计日志
     *
     * 功能：
     * - 查询特定用户执行的所有操作
     * - 返回按操作时间排序的日志列表
     * - 用于用户行为分析和安全审计
     *
     * 参数说明：
     * - operator: 用户 ID
     *   * 格式：user-123, admin-456 等
     *   * 不能为 null
     *
     * 返回值：
     * - 该用户的所有审计日志列表
     * - 如果用户没有操作记录，返回空列表
     * - 列表按操作时间排序（通常是升序）
     *
     * 使用示例：
     * List<AuditLog> userLogs = auditLogRepository.findByOperator("user-123");
     * for (AuditLog log : userLogs) {
     *     System.out.println(log.getOperationType() + " at " + log.getOperationTime());
     * }
     *
     * SQL 生成：
     * SELECT * FROM audit_logs WHERE operator = ? ORDER BY operation_time ASC
     *
     * 性能考虑：
     * - 应该在 operator 字段上创建索引
     * - 如果用户有大量操作，应该使用分页
     * - 可以添加 @Query 注解优化查询
     *
     * 应用场景：
     * - 用户行为分析：了解用户的操作习惯
     * - 安全审计：检查用户是否有异常操作
     * - 故障排查：追踪用户在问题发生前的操作
     * - 权限检查：验证用户是否有权限执行操作
     *
     * @param operator 用户 ID
     * @return 该用户的所有审计日志列表
     */
    List<AuditLog> findByOperator(String operator);

    /**
     * 根据操作类型查询审计日志
     *
     * 功能：
     * - 查询特定操作类型的所有记录
     * - 返回按操作时间排序的日志列表
     * - 用于操作统计和分析
     *
     * 参数说明：
     * - operationType: 操作类型
     *   * 支持的值：CREATE, READ, UPDATE, DELETE, EXPORT, IMPORT, LOGIN, LOGOUT 等
     *   * 不能为 null
     *
     * 返回值：
     * - 该操作类型的所有审计日志列表
     * - 如果没有该类型的操作，返回空列表
     * - 列表按操作时间排序
     *
     * 使用示例：
     * // 查询所有删除操作
     * List<AuditLog> deleteLogs = auditLogRepository.findByOperationType("DELETE");
     * System.out.println("Total DELETE operations: " + deleteLogs.size());
     *
     * // 查询所有登录操作
     * List<AuditLog> loginLogs = auditLogRepository.findByOperationType("LOGIN");
     * for (AuditLog log : loginLogs) {
     *     System.out.println("User " + log.getOperator() + " logged in at " + log.getOperationTime());
     * }
     *
     * SQL 生成：
     * SELECT * FROM audit_logs WHERE operation_type = ? ORDER BY operation_time ASC
     *
     * 性能考虑：
     * - 应该在 operationType 字段上创建索引
     * - 如果操作记录很多，应该使用分页
     * - 可以添加时间范围限制避免查询过多数据
     *
     * 应用场景：
     * - 操作统计：统计各类操作的执行次数
     * - 安全分析：分析特定操作类型的执行情况
     * - 性能监控：监控高频操作的性能
     * - 合规性检查：检查特定操作是否符合规范
     *
     * 扩展建议：
     * - 可以添加分页参数：findByOperationType(String operationType, Pageable pageable)
     * - 可以添加时间范围：findByOperationTypeAndOperationTimeBetween(...)
     * - 可以添加结果过滤：findByOperationTypeAndResult(...)
     *
     * @param operationType 操作类型
     * @return 该操作类型的所有审计日志列表
     */
    List<AuditLog> findByOperationType(String operationType);

    /**
     * 查询指定时间范围内的审计日志
     *
     * 功能：
     * - 查询特定时间段内的所有操作记录
     * - 返回按操作时间排序的日志列表
     * - 用于时间序列分析和事件追踪
     *
     * 参数说明：
     * - startTime: 开始时间（包含）
     *   * 格式：LocalDateTime
     *   * 示例：LocalDateTime.of(2026, 3, 1, 0, 0, 0)
     *
     * - endTime: 结束时间（包含）
     *   * 格式：LocalDateTime
     *   * 示例：LocalDateTime.of(2026, 3, 31, 23, 59, 59)
     *
     * 返回值：
     * - 时间范围内的所有审计日志列表
     * - 如果时间范围内没有操作，返回空列表
     * - 列表按操作时间排序
     *
     * 使用示例：
     * // 查询今天的操作
     * LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
     * LocalDateTime tomorrow = today.plusDays(1);
     * List<AuditLog> todayLogs = auditLogRepository.findByOperationTimeBetween(today, tomorrow);
     *
     * // 查询最近 7 天的操作
     * LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
     * List<AuditLog> weekLogs = auditLogRepository.findByOperationTimeBetween(sevenDaysAgo, LocalDateTime.now());
     *
     * // 查询特定月份的操作
     * LocalDateTime monthStart = LocalDateTime.of(2026, 3, 1, 0, 0, 0);
     * LocalDateTime monthEnd = LocalDateTime.of(2026, 3, 31, 23, 59, 59);
     * List<AuditLog> monthLogs = auditLogRepository.findByOperationTimeBetween(monthStart, monthEnd);
     *
     * SQL 生成：
     * SELECT * FROM audit_logs WHERE operation_time BETWEEN ? AND ? ORDER BY operation_time ASC
     *
     * 性能考虑：
     * - 应该在 operationTime 字段上创建索引
     * - 时间范围越大，查询的数据越多
     * - 应该使用分页避免一次加载过多数据
     * - 可以添加其他条件（如 operator）进一步缩小范围
     *
     * 应用场景：
     * - 事件追踪：追踪特定时间段内发生的事件
     * - 审计报告：生成日/周/月审计报告
     * - 故障分析：分析故障发生时间段的操作
     * - 性能分析：分析不同时间段的性能
     * - 合规性检查：检查特定时间段的操作是否符合规范
     *
     * 扩展建议：
     * - 可以添加分页参数：findByOperationTimeBetween(..., Pageable pageable)
     * - 可以添加操作者过滤：findByOperatorAndOperationTimeBetween(...)
     * - 可以添加操作类型过滤：findByOperationTypeAndOperationTimeBetween(...)
     * - 可以添加结果过滤：findByResultAndOperationTimeBetween(...)
     *
     * @param startTime 开始时间（包含）
     * @param endTime 结束时间（包含）
     * @return 时间范围内的所有审计日志列表
     */
    List<AuditLog> findByOperationTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
}
