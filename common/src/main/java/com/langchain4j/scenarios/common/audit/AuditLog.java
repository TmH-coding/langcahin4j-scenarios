package com.langchain4j.scenarios.common.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 审计日志实体 - 操作追踪和合规性记录
 *
 * 职责说明：
 * - 记录系统中所有重要操作的详细信息
 * - 支持完整的操作追踪和审计
 * - 用于合规性检查和安全审计
 * - 提供操作历史和问题排查的依据
 *
 * 架构设计：
 * - 使用 JPA Entity 映射到数据库表
 * - 使用 Lombok 简化 getter/setter 和构造器
 * - 使用 Builder 模式便于对象创建
 * - 所有关键字段都设置为非空约束
 *
 * 数据库表结构：
 * - 表名：audit_logs
 * - 主键：id（自增长）
 * - 索引建议：
 *   * 在 operator 字段上创建索引（查询用户操作）
 *   * 在 operationType 字段上创建索引（查询操作类型）
 *   * 在 operationTime 字段上创建索引（查询时间范围）
 *   * 在 (operator, operationTime) 上创建复合索引（常见查询）
 *
 * 使用场景：
 * - 安全审计：追踪用户的所有操作
 * - 合规性检查：满足法规要求的操作记录
 * - 故障排查：分析问题发生前的操作序列
 * - 性能分析：统计各类操作的执行情况
 * - 用户行为分析：了解用户的操作习惯
 *
 * 审计信息完整性：
 * - 操作者信息：operator（谁执行了操作）
 * - 操作信息：operationType, targetObject, details（做了什么）
 * - 结果信息：result（操作是否成功）
 * - 时间信息：operationTime（何时执行）
 * - 环境信息：ipAddress, userAgent（从哪里执行）
 *
 * 数据保留策略：
 * - 审计日志应该长期保留（通常 1-7 年）
 * - 定期备份和归档
 * - 防止审计日志被篡改或删除
 * - 实现审计日志的只读访问
 *
 * 性能考虑：
 * - 审计日志表可能会很大（百万级记录）
 * - 应该定期清理过期数据
 * - 应该使用分区表优化查询性能
 * - 应该使用异步写入避免阻塞主业务
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    /**
     * 主键 ID
     *
     * 说明：
     * - 自增长主键
     * - 唯一标识每条审计日志记录
     * - 数据库自动生成
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 操作者用户 ID
     *
     * 说明：
     * - 执行操作的用户标识
     * - 非空字段，必须设置
     * - 用于追踪谁执行了操作
     * - 格式：user-123, admin-456 等
     *
     * 应用场景：
     * - 用户行为分析
     * - 安全审计
     * - 权限检查
     */
    @Column(nullable = false)
    private String operator;

    /**
     * 操作类型
     *
     * 说明：
     * - 操作的类型分类
     * - 非空字段，必须设置
     * - 用于统计和分析操作
     *
     * 支持的操作类型：
     * - CREATE: 创建新对象
     * - READ: 查询/读取对象
     * - UPDATE: 修改对象
     * - DELETE: 删除对象
     * - EXPORT: 导出数据
     * - IMPORT: 导入数据
     * - LOGIN: 用户登录
     * - LOGOUT: 用户登出
     * - CONFIG_CHANGE: 配置变更
     * - PERMISSION_CHANGE: 权限变更
     *
     * 应用场景：
     * - 操作统计：统计各类操作的执行次数
     * - 安全分析：分析特定操作类型的执行情况
     * - 性能监控：监控高频操作的性能
     */
    @Column(nullable = false)
    private String operationType;

    /**
     * 操作的目标对象类型
     *
     * 说明：
     * - 操作作用的对象类型
     * - 非空字段，必须设置
     * - 用于追踪操作影响的资源
     *
     * 支持的目标对象：
     * - conversation_record: 对话记录
     * - user_profile: 用户资料
     * - document: 文档
     * - system_config: 系统配置
     * - permission: 权限
     * - role: 角色
     * - api_key: API 密钥
     *
     * 应用场景：
     * - 资源追踪：追踪特定资源的所有操作
     * - 影响分析：分析操作对哪些资源的影响
     * - 数据恢复：根据操作历史恢复数据
     */
    @Column(nullable = false)
    private String targetObject;

    /**
     * 操作的详细信息
     *
     * 说明：
     * - 操作的具体内容和参数
     * - 可选字段，可以为 null
     * - 使用 LONGTEXT 类型支持长文本
     * - 用于详细的问题排查和分析
     *
     * 内容示例：
     * - 修改操作：{"field": "status", "oldValue": "active", "newValue": "inactive"}
     * - 删除操作：{"id": "conv-123", "reason": "user request"}
     * - 创建操作：{"name": "New Conversation", "type": "customer_service"}
     * - 查询操作：{"filters": {"status": "active"}, "limit": 100}
     *
     * 最佳实践：
     * - 使用 JSON 格式存储结构化数据
     * - 不要存储敏感信息（密码、密钥等）
     * - 限制详情的长度，避免数据库过大
     * - 对敏感字段进行脱敏处理
     *
     * 应用场景：
     * - 故障排查：了解操作的具体内容
     * - 数据恢复：根据操作详情恢复数据
     * - 安全分析：分析异常操作的具体内容
     */
    @Column(columnDefinition = "LONGTEXT")
    private String details;

    /**
     * 操作结果
     *
     * 说明：
     * - 操作的执行结果
     * - 非空字段，必须设置
     * - 用于追踪操作是否成功
     *
     * 支持的结果值：
     * - SUCCESS: 操作成功
     * - FAILURE: 操作失败
     * - PARTIAL_SUCCESS: 部分成功（如批量操作中部分失败）
     * - TIMEOUT: 操作超时
     * - CANCELLED: 操作被取消
     *
     * 应用场景：
     * - 成功率统计：统计操作的成功率
     * - 故障分析：分析失败操作的原因
     * - 性能监控：监控操作的成功率和性能
     */
    @Column(nullable = false)
    private String result;

    /**
     * 操作执行时间
     *
     * 说明：
     * - 操作发生的时间戳
     * - 非空字段，必须设置
     * - 使用 LocalDateTime 类型（精确到秒）
     * - 自动设置为当前时间
     *
     * 应用场景：
     * - 时间序列分析：按时间顺序分析操作
     * - 事件追踪：追踪特定时间段的事件
     * - 审计报告：生成日/周/月审计报告
     * - 性能分析：分析不同时间段的性能
     *
     * 查询示例：
     * - 查询今天的操作：operationTime >= today 00:00:00
     * - 查询最近 7 天的操作：operationTime >= now - 7 days
     * - 查询特定时间段的操作：operationTime between start and end
     */
    @Column(nullable = false)
    private LocalDateTime operationTime;

    /**
     * 操作者的 IP 地址
     *
     * 说明：
     * - 执行操作的客户端 IP 地址
     * - 可选字段，可以为 null
     * - 用于追踪操作的来源
     *
     * 格式示例：
     * - IPv4: 192.168.1.100
     * - IPv6: 2001:0db8:85a3:0000:0000:8a2e:0370:7334
     * - 本地：127.0.0.1
     *
     * 应用场景：
     * - 安全审计：检查操作是否来自异常 IP
     * - 地理位置分析：分析操作的地理分布
     * - 异常检测：检测来自异常 IP 的操作
     * - 访问控制：限制特定 IP 的访问
     *
     * 获取方式：
     * - HttpServletRequest.getRemoteAddr()
     * - 需要处理代理情况（X-Forwarded-For 头）
     */
    private String ipAddress;

    /**
     * 操作者的用户代理信息
     *
     * 说明：
     * - 执行操作的客户端浏览器/应用信息
     * - 可选字段，可以为 null
     * - 用于追踪操作的客户端类型
     *
     * 格式示例：
     * - 浏览器：Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36
     * - 移动应用：MyApp/1.0 (Android 12; Build/ABC123)
     * - API 客户端：curl/7.68.0
     *
     * 应用场景：
     * - 客户端分析：分析使用的客户端类型
     * - 兼容性检查：检查特定客户端的兼容性
     * - 异常检测：检测异常的用户代理
     * - 安全分析：分析来自异常客户端的操作
     *
     * 获取方式：
     * - HttpServletRequest.getHeader("User-Agent")
     */
    private String userAgent;
}
