package com.langchain4j.scenarios.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 对话记录实体 - 用户与 AI 的对话持久化存储
 *
 * 职责说明：
 * - 持久化存储用户与 AI 的对话消息
 * - 支持对话历史查询和检索
 * - 支持多维度的数据分析（用户、场景、时间等）
 * - 实现软删除保护数据完整性
 * - 记录对话的完整上下文信息
 *
 * 架构设计：
 * - 使用 JPA @Entity 注解标记为持久化实体
 * - 使用 Lombok 简化 getter/setter 和构造函数
 * - 使用 Builder 模式支持灵活的对象构建
 * - 使用软删除（deleted 标志）而非物理删除
 * - 支持自动时间戳管理
 *
 * 使用场景：
 * - 客服系统：存储客户与 AI 的对话历史
 * - 文档分析：记录用户上传文档和 AI 分析结果
 * - 代码助手：保存代码生成和优化的对话
 * - 数据分析：存储数据查询和分析的对话
 * - 内容创作：记录创意写作的对话过程
 *
 * 数据库表结构：
 * - 表名：conversation_records
 * - 主键：id（自增长）
 * - 索引建议：
 *   * (session_id, deleted) - 会话查询
 *   * (user_id, deleted) - 用户查询
 *   * (scenario_type, deleted) - 场景查询
 *   * (created_at, deleted) - 时间范围查询
 *   * (user_id, created_at, deleted) - 用户时间查询
 *
 * 性能考虑：
 * - 消息内容使用 LONGTEXT 支持长文本
 * - 软删除查询需要额外的 deleted 条件
 * - 建议定期归档旧数据到历史表
 * - 建议使用分区表处理大数据量
 *
 * 扩展建议：
 * - 可以添加消息向量化存储用于语义搜索
 * - 可以添加消息评分和反馈字段
 * - 可以添加消息标签和分类字段
 * - 可以实现消息加密存储敏感内容
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "conversation_records")
public class ConversationRecord {
    /**
     * 主键 - 对话记录的唯一标识
     *
     * 说明：
     * - 自增长主键
     * - 数据库自动生成
     * - 用于唯一标识每条对话记录
     * - 类型：Long（64 位整数）
     *
     * 使用示例：
     * ConversationRecord record = repository.findById(123L);
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 会话ID - 对话会话的唯一标识
     *
     * 说明：
     * - 用于关联同一会话的所有消息
     * - 由应用生成（通常为 UUID）
     * - 格式：UUID 或自定义格式
     * - 示例：\"550e8400-e29b-41d4-a716-446655440000\"
     * - 非空字段，必须设置
     *
     * 应用场景：
     * - 恢复完整的对话历史
     * - 分析单个会话的对话流程
     * - 支持会话级别的操作（导出、删除等）
     *
     * 查询示例：
     * List<ConversationRecord> records = repository.findBySessionIdAndDeletedFalse(sessionId);
     */
    @Column(nullable = false)
    private String sessionId;

    /**
     * 用户ID - 发起对话的用户唯一标识
     *
     * 说明：
     * - 用于标识哪个用户发起的对话
     * - 由认证系统提供
     * - 格式：UUID 或自定义格式
     * - 示例：\"user-123-abc-def\"
     * - 非空字段，必须设置
     *
     * 应用场景：
     * - 查询用户的所有对话历史
     * - 分析用户的使用行为
     * - 实现用户级别的权限控制
     * - 生成用户的个性化推荐
     *
     * 查询示例：
     * Page<ConversationRecord> page = repository.findByUserIdAndDeletedFalse(userId, pageable);
     * long count = repository.countByUserIdAndDeletedFalse(userId);
     */
    @Column(nullable = false)
    private String userId;

    /**
     * 消息角色 - 消息的发送者身份
     *
     * 说明：
     * - 标识消息是由用户还是 AI 发送
     * - 可能的值：\"user\" 或 \"assistant\"
     * - 非空字段，必须设置
     * - 用于区分对话中的不同参与者
     *
     * 角色说明：
     * - \"user\"：用户发送的消息
     *   * 用户输入的问题或指令
     *   * 用户上传的文档或数据
     *   * 用户的反馈和评论
     *
     * - \"assistant\"：AI 发送的消息
     *   * AI 生成的回复
     *   * AI 的分析结果
     *   * AI 的建议和推荐
     *
     * 应用场景：
     * - 构建对话历史时区分消息来源
     * - 分析用户和 AI 的交互模式
     * - 生成对话摘要和统计
     *
     * 使用示例：
     * if (\"user\".equals(record.getRole())) {
     *     // 处理用户消息
     * } else if (\"assistant\".equals(record.getRole())) {
     *     // 处理 AI 消息
     * }
     */
    @Column(nullable = false)
    private String role;

    /**
     * 消息内容 - 对话消息的实际文本内容
     *
     * 说明：
     * - 存储消息的完整文本
     * - 使用 LONGTEXT 类型支持长文本
     * - 可以为 null（某些情况下消息可能为空）
     * - 最大长度：4GB（MySQL LONGTEXT）
     *
     * 内容类型：
     * - 用户消息：用户输入的问题或指令
     *   * 示例：\"请分析这个 PDF 文档\"
     *   * 示例：\"生成一个 Java 类\"
     *
     * - AI 消息：AI 生成的回复
     *   * 示例：\"文档分析结果如下...\"
     *   * 示例：\"public class User { ... }\"
     *
     * 性能考虑：
     * - LONGTEXT 字段不会被索引
     * - 查询时避免在 WHERE 子句中使用此字段
     * - 如果需要搜索内容，考虑使用全文索引或搜索引擎
     *
     * 安全考虑：
     * - 消息可能包含敏感信息
     * - 考虑加密存储
     * - 限制访问权限
     * - 定期清理过期数据
     *
     * 使用示例：
     * String content = record.getContent();
     * if (content.length() > 1000) {
     *     String summary = content.substring(0, 1000) + \"...\";
     * }
     */
    @Column(columnDefinition = "LONGTEXT")
    private String content;

    /**
     * 场景类型 - 对话所属的应用场景
     *
     * 说明：
     * - 标识对话属于哪个应用场景
     * - 非空字段，必须设置
     * - 可能的值：
     *   * \"customer-service\" - 客服系统
     *   * \"document-analysis\" - 文档分析
     *   * \"code-assistant\" - 代码助手
     *   * \"data-analyst\" - 数据分析
     *   * \"content-creator\" - 内容创作
     *
     * 应用场景：
     * - 按场景统计对话数量
     * - 分析不同场景的使用情况
     * - 为不同场景应用不同的处理逻辑
     * - 生成场景级别的报表和分析
     *
     * 查询示例：
     * List<ConversationRecord> records = repository.findByScenarioTypeAndDeletedFalse(\"customer-service\");
     * long count = repository.countByScenarioTypeAndDeletedFalse(\"customer-service\");
     */
    @Column(nullable = false)
    private String scenarioType;

    /**
     * 创建时间 - 对话记录的创建时间戳
     *
     * 说明：
     * - 记录消息创建的时间
     * - 非空字段，必须设置
     * - 类型：LocalDateTime（精确到秒）
     * - 通常由应用在创建记录时设置
     * - 示例：2026-03-21T11:24:23
     *
     * 应用场景：
     * - 按时间范围查询对话
     * - 生成时间序列分析
     * - 实现对话的时间排序
     * - 计算对话的时间间隔
     *
     * 查询示例：
     * LocalDateTime start = LocalDateTime.of(2026, 3, 21, 0, 0, 0);
     * LocalDateTime end = LocalDateTime.of(2026, 3, 21, 23, 59, 59);
     * List<ConversationRecord> records = repository.findByTimeRange(start, end);
     *
     * 性能考虑：
     * - 建议在 createdAt 字段上建立索引
     * - 支持时间范围查询的快速执行
     * - 支持按日期分区表
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间 - 对话记录的最后更新时间戳
     *
     * 说明：
     * - 记录消息最后修改的时间
     * - 可以为 null（如果从未修改）
     * - 类型：LocalDateTime（精确到秒）
     * - 通常由应用在修改记录时更新
     * - 示例：2026-03-21T11:25:00
     *
     * 应用场景：
     * - 追踪记录的修改历史
     * - 实现乐观锁并发控制
     * - 检测数据是否被修改
     * - 生成审计日志
     *
     * 使用示例：
     * if (record.getUpdatedAt() != null) {
     *     Duration duration = Duration.between(record.getCreatedAt(), record.getUpdatedAt());
     *     System.out.println(\"修改耗时：\" + duration.getSeconds() + \" 秒\");
     * }
     *
     * 注意：
     * - 通常不需要手动设置此字段
     * - 可以使用 @UpdateTimestamp 注解自动管理
     * - 如果记录从未修改，此字段为 null
     */
    private LocalDateTime updatedAt;

    /**
     * 是否已删除 - 软删除标志
     *
     * 说明：
     * - 标记记录是否已被逻辑删除
     * - 非空字段，默认值为 false
     * - 类型：Boolean
     * - 使用软删除而非物理删除
     * - 保护数据完整性和可恢复性
     *
     * 软删除的优势：
     * - 数据可恢复：删除后仍可恢复
     * - 审计追踪：保留完整的操作历史
     * - 数据一致性：避免级联删除问题
     * - 性能优化：删除操作只需更新一个字段
     *
     * 软删除的劣势：
     * - 查询需要额外的 deleted 条件
     * - 数据库存储空间占用较大
     * - 需要定期清理过期数据
     *
     * 应用场景：
     * - 用户删除对话时，标记为已删除
     * - 管理员清理过期数据时，标记为已删除
     * - 数据恢复时，将标志改回 false
     *
     * 查询示例：
     * // 查询未删除的记录
     * List<ConversationRecord> records = repository.findBySessionIdAndDeletedFalse(sessionId);
     *
     * // 查询已删除的记录
     * List<ConversationRecord> deletedRecords = repository.findByDeletedTrue();
     *
     * 删除示例：
     * record.setDeleted(true);
     * record.setUpdatedAt(LocalDateTime.now());
     * repository.save(record);
     *
     * 恢复示例：
     * record.setDeleted(false);
     * record.setUpdatedAt(LocalDateTime.now());
     * repository.save(record);
     *
     * 性能考虑：
     * - 建议在 deleted 字段上建立索引
     * - 建议在 (deleted, created_at) 上建立复合索引
     * - 支持快速过滤已删除的记录
     */
    @Column(nullable = false)
    private Boolean deleted = false;
}
