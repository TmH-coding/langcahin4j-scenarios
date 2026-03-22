package com.langchain4j.scenarios.common.audit;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志服务 - 合规性和安全追踪
 *
 * 职责说明：
 * - 记录系统中所有重要操作的审计日志
 * - 提供多维度的审计日志查询功能
 * - 支持合规性检查和安全审计
 * - 保留完整的操作追踪记录
 * - 支持事务管理和数据一致性保证
 *
 * 架构设计：
 * - 使用 @Service 注解标记为 Spring 业务层组件
 * - 使用 @Transactional 确保审计日志的事务一致性
 * - 依赖 AuditLogRepository 进行数据访问
 * - 记录操作者、操作类型、操作对象、操作结果等信息
 * - 遵循 Repository 模式实现数据访问层分离
 * - 支持 Spring Data JPA 的高级查询功能
 *
 * 使用场景：
 * - 安全审计：追踪用户的所有操作
 * - 合规性检查：满足法规要求的操作记录
 * - 故障排查：分析问题发生前的操作序列
 * - 性能分析：统计各类操作的执行情况
 * - 用户行为分析：了解用户的操作习惯
 * - 安全事件调查：追踪异常操作
 *
 * 审计信息包含：
 * - operator: 执行操作的用户ID
 * - operationType: 操作类型（如 CREATE, UPDATE, DELETE, QUERY）
 * - targetObject: 操作的目标对象（如 conversation_record, user_profile）
 * - details: 操作的详细信息（如修改的字段、参数值）
 * - result: 操作结果（SUCCESS, FAILURE, PARTIAL_SUCCESS）
 * - ipAddress: 操作者的IP地址
 * - userAgent: 操作者的浏览器/客户端信息
 * - operationTime: 操作发生的时间
 *
 * 工作原理：
 * 1. 业务逻辑执行重要操作
 * 2. 调用 recordAudit() 记录审计日志
 * 3. 服务方法创建 AuditLog 对象
 * 4. 自动设置操作时间为当前时间
 * 5. 调用 Repository 保存到数据库
 * 6. 自动处理事务提交或回滚
 * 7. 返回保存后的审计日志对象
 *
 * 性能考虑：
 * - 审计日志记录是同步操作，建议异步处理以避免阻塞主流程
 * - 支持按多个维度查询（操作者、操作类型、时间范围）
 * - 建议为常用查询字段添加数据库索引
 * - 大数据量场景建议使用批处理
 * - 定期归档旧审计日志以提高查询性能
 * - 监控审计日志表的大小和增长速度
 *
 * 安全考虑：
 * - 所有数据库操作都在事务内执行
 * - 审计日志应该完整记录所有操作
 * - 应该包含足够的信息用于追踪和调查
 * - 限制审计日志的访问权限
 * - 防止审计日志被篡改或删除
 * - 敏感信息应该加密存储
 * - 定期备份审计日志数据
 *
 * 合规性考虑：
 * - 审计日志应该满足法规要求（如 GDPR、SOX）
 * - 应该保留足够长的时间（通常 1-7 年）
 * - 应该支持审计日志的导出和报告
 * - 应该实现审计日志的完整性验证
 * - 应该支持审计日志的加密存储
 *
 * 扩展建议：
 * - 可以添加异步审计日志记录提高性能
 * - 可以实现审计日志的消息队列处理
 * - 可以添加审计日志的压缩存储
 * - 可以支持审计日志的全文搜索
 * - 可以实现审计日志的可视化分析
 * - 可以添加审计日志的告警机制
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * 记录审计日志
     *
     * 功能：
     * - 创建并保存一条审计日志记录
     * - 自动设置操作时间为当前时间
     * - 返回保存后的审计日志对象
     *
     * 使用示例：
     * AuditLog log = auditLogService.recordAudit(
     *     "user-123",                    // 操作者ID
     *     "UPDATE",                      // 操作类型
     *     "conversation_record",         // 目标对象
     *     "Updated message content",     // 操作详情
     *     "SUCCESS",                     // 操作结果
     *     "192.168.1.100",              // IP地址
     *     "Mozilla/5.0..."              // User Agent
     * );
     *
     * 操作类型规范：
     * - CREATE: 创建新对象
     * - READ: 查询/读取对象
     * - UPDATE: 修改对象
     * - DELETE: 删除对象
     * - EXPORT: 导出数据
     * - IMPORT: 导入数据
     * - LOGIN: 用户登录
     * - LOGOUT: 用户登出
     * - CONFIG_CHANGE: 配置变更
     *
     * @param operator 执行操作的用户ID
     * @param operationType 操作类型
     * @param targetObject 操作的目标对象类型
     * @param details 操作的详细信息
     * @param result 操作结果（SUCCESS/FAILURE/PARTIAL_SUCCESS）
     * @param ipAddress 操作者的IP地址
     * @param userAgent 操作者的User Agent信息
     * @return 保存后的审计日志对象
     */
    public AuditLog recordAudit(String operator, String operationType, String targetObject,
                               String details, String result, String ipAddress, String userAgent) {
        AuditLog log = AuditLog.builder()
                .operator(operator)
                .operationType(operationType)
                .targetObject(targetObject)
                .details(details)
                .result(result)
                .operationTime(LocalDateTime.now())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .build();
        return auditLogRepository.save(log);
    }

    /**
     * 查询用户的审计日志
     *
     * 功能：
     * - 获取特定用户执行的所有操作记录
     * - 用于用户行为分析和安全审计
     * - 返回按操作时间排序的日志列表
     *
     * 应用场景：
     * - 用户行为分析：了解用户的操作习惯
     * - 安全审计：检查用户是否有异常操作
     * - 故障排查：追踪用户在问题发生前的操作
     *
     * 使用示例：
     * List<AuditLog> userLogs = auditLogService.getAuditLogsByOperator("user-123");
     * for (AuditLog log : userLogs) {
     *     System.out.println(log.getOperationType() + " at " + log.getOperationTime());
     * }
     *
     * @param operator 用户ID
     * @return 该用户的所有审计日志列表
     */
    public List<AuditLog> getAuditLogsByOperator(String operator) {
        return auditLogRepository.findByOperator(operator);
    }

    /**
     * 查询操作类型的审计日志
     *
     * 功能：
     * - 获取特定操作类型的所有日志记录
     * - 用于操作类型的统计分析
     * - 支持按操作类型进行安全审计
     *
     * 应用场景：
     * - 操作统计：统计各类操作的执行次数
     * - 安全分析：分析特定操作类型的执行情况
     * - 性能监控：监控高频操作的性能
     *
     * 支持的操作类型：
     * - CREATE: 创建操作
     * - UPDATE: 修改操作
     * - DELETE: 删除操作
     * - EXPORT: 导出操作
     * - LOGIN: 登录操作
     *
     * 使用示例：
     * List<AuditLog> deleteLogs = auditLogService.getAuditLogsByOperationType("DELETE");
     * System.out.println("Total DELETE operations: " + deleteLogs.size());
     *
     * @param operationType 操作类型
     * @return 该操作类型的所有审计日志列表
     */
    public List<AuditLog> getAuditLogsByOperationType(String operationType) {
        return auditLogRepository.findByOperationType(operationType);
    }

    /**
     * 查询时间范围内的审计日志
     *
     * 功能：
     * - 获取特定时间段内的所有操作记录
     * - 用于时间序列分析和事件追踪
     * - 支持按天、周、月进行审计报告
     *
     * 应用场景：
     * - 事件追踪：追踪特定时间段内发生的事件
     * - 审计报告：生成日/周/月审计报告
     * - 故障分析：分析故障发生时间段的操作
     *
     * 使用示例：
     * LocalDateTime start = LocalDateTime.of(2026, 3, 1, 0, 0, 0);
     * LocalDateTime end = LocalDateTime.of(2026, 3, 31, 23, 59, 59);
     * List<AuditLog> monthlyLogs = auditLogService.getAuditLogsByTimeRange(start, end);
     * System.out.println("Operations in March: " + monthlyLogs.size());
     *
     * @param startTime 开始时间（包含）
     * @param endTime 结束时间（包含）
     * @return 时间范围内的所有审计日志列表
     */
    public List<AuditLog> getAuditLogsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return auditLogRepository.findByOperationTimeBetween(startTime, endTime);
    }
}
