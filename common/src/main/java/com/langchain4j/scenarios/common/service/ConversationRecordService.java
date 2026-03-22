package com.langchain4j.scenarios.common.service;

import com.langchain4j.scenarios.common.entity.ConversationRecord;
import com.langchain4j.scenarios.common.repository.ConversationRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话记录服务 - 核心业务逻辑层
 *
 * 职责说明：
 * - 管理对话记录的持久化操作（增删改查）
 * - 提供多维度查询功能（按会话、用户、场景、时间范围）
 * - 实现软删除机制（逻辑删除，保留数据完整性）
 * - 提供统计分析接口（用户对话数、场景对话数）
 * - 支持事务管理和数据一致性保证
 *
 * 架构设计：
 * - 使用 @Service 注解标记为 Spring 业务层组件
 * - 使用 @Transactional 确保数据库操作的事务一致性
 * - 使用 @RequiredArgsConstructor 自动注入依赖（Lombok）
 * - 依赖 ConversationRecordRepository 进行数据访问
 * - 遵循 Repository 模式实现数据访问层分离
 * - 支持 Spring Data JPA 的高级查询功能
 *
 * 使用场景：
 * - 客服系统：保存和查询客户对话历史
 * - 数据分析：统计各场景的对话数据
 * - 审计追踪：记录用户的所有交互行为
 * - 性能优化：支持分页查询大量数据
 * - 用户中心：显示用户的对话历史列表
 * - 排行榜：统计最活跃的用户
 *
 * 工作原理：
 * 1. 业务层调用服务方法
 * 2. 服务方法验证输入参数
 * 3. 调用 Repository 进行数据库操作
 * 4. 自动处理事务提交或回滚
 * 5. 返回操作结果给调用者
 * 6. 异常情况下自动回滚事务
 *
 * 性能考虑：
 * - 使用分页查询避免一次加载过多数据
 * - 支持按多个维度查询（会话、用户、场景、时间）
 * - 统计操作使用数据库聚合函数提高效率
 * - 软删除避免物理删除的性能开销
 * - 建议为常用查询字段添加数据库索引
 * - 大数据量场景建议使用批处理
 *
 * 安全考虑：
 * - 所有数据库操作都在事务内执行
 * - 软删除保留完整的审计日志
 * - 支持按用户隔离数据访问
 * - 建议在 Controller 层进行权限验证
 * - 敏感数据应该加密存储
 * - 定期备份对话记录数据
 *
 * 扩展建议：
 * - 可以添加缓存层提高查询性能
 * - 可以实现异步保存提高吞吐量
 * - 可以添加数据导出功能
 * - 可以支持全文搜索功能
 * - 可以实现对话记录的压缩存储
 * - 可以添加数据分析和统计功能
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ConversationRecordService {

    private final ConversationRecordRepository recordRepository;

    /**
     * 保存对话记录
     *
     * 功能：
     * - 将新的对话记录持久化到数据库
     * - 自动设置创建时间为当前时间
     * - 返回保存后的记录（包含自动生成的ID）
     *
     * 使用示例：
     * ConversationRecord record = ConversationRecord.builder()
     *     .sessionId("session-123")
     *     .userId("user-456")
     *     .role("user")
     *     .content("你好，请帮我...")
     *     .scenarioType("customer-service")
     *     .build();
     * ConversationRecord saved = service.saveRecord(record);
     *
     * @param record 待保存的对话记录对象
     * @return 保存后的对话记录（包含数据库生成的ID）
     */
    public ConversationRecord saveRecord(ConversationRecord record) {
        record.setCreatedAt(LocalDateTime.now());
        return recordRepository.save(record);
    }

    /**
     * 根据会话ID查询对话记录
     *
     * 功能：
     * - 获取特定会话的所有对话消息
     * - 自动过滤已删除的记录（deleted=false）
     * - 返回按创建时间排序的对话列表
     *
     * 应用场景：
     * - 客服系统：显示用户与AI的完整对话历史
     * - 对话恢复：用户重新打开之前的对话
     *
     * @param sessionId 会话ID（唯一标识一次对话会话）
     * @return 该会话的所有未删除对话记录列表
     */
    public List<ConversationRecord> getRecordsBySessionId(String sessionId) {
        return recordRepository.findBySessionIdAndDeletedFalse(sessionId);
    }

    /**
     * 根据用户ID分页查询对话记录
     *
     * 功能：
     * - 获取特定用户的所有对话记录
     * - 支持分页查询（避免一次加载过多数据）
     * - 自动过滤已删除的记录
     *
     * 应用场景：
     * - 用户中心：显示用户的对话历史列表
     * - 数据分析：统计用户的对话行为
     *
     * 分页参数示例：
     * Pageable pageable = PageRequest.of(0, 20, Sort.by("createdAt").descending());
     * Page<ConversationRecord> page = service.getRecordsByUserId("user-123", pageable);
     *
     * @param userId 用户ID
     * @param pageable 分页参数（页码、每页数量、排序规则）
     * @return 分页结果对象，包含总数、当前页数据等
     */
    public Page<ConversationRecord> getRecordsByUserId(String userId, Pageable pageable) {
        return recordRepository.findByUserIdAndDeletedFalse(userId, pageable);
    }

    /**
     * 根据场景类型查询对话记录
     *
     * 功能：
     * - 获取特定场景的所有对话记录
     * - 用于场景级别的数据分析
     * - 自动过滤已删除的记录
     *
     * 应用场景：
     * - 数据分析：统计各场景的使用情况
     * - 性能监控：分析不同场景的响应时间
     *
     * 支持的场景类型：
     * - "customer-service" - 客服系统
     * - "document-analysis" - 文档分析
     * - "code-assistant" - 代码助手
     * - "data-analyst" - 数据分析
     * - "content-creator" - 内容创作
     *
     * @param scenarioType 场景类型标识
     * @return 该场景的所有未删除对话记录列表
     */
    public List<ConversationRecord> getRecordsByScenarioType(String scenarioType) {
        return recordRepository.findByScenarioTypeAndDeletedFalse(scenarioType);
    }

    /**
     * 查询指定时间范围内的对话记录
     *
     * 功能：
     * - 获取特定时间段内的所有对话
     * - 用于时间序列分析
     * - 支持按天、周、月统计
     *
     * 应用场景：
     * - 数据分析：统计某时间段的对话量
     * - 性能报告：生成日/周/月报告
     *
     * 使用示例：
     * LocalDateTime start = LocalDateTime.of(2026, 3, 1, 0, 0, 0);
     * LocalDateTime end = LocalDateTime.of(2026, 3, 31, 23, 59, 59);
     * List<ConversationRecord> records = service.getRecordsByTimeRange(start, end);
     *
     * @param startTime 开始时间（包含）
     * @param endTime 结束时间（包含）
     * @return 时间范围内的所有对话记录
     */
    public List<ConversationRecord> getRecordsByTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return recordRepository.findByTimeRange(startTime, endTime);
    }

    /**
     * 统计用户的对话数
     *
     * 功能：
     * - 快速获取用户的对话总数
     * - 用于用户活跃度统计
     * - 只计算未删除的记录
     *
     * 应用场景：
     * - 用户分析：了解用户使用频率
     * - 排行榜：统计最活跃的用户
     *
     * @param userId 用户ID
     * @return 该用户的对话总数
     */
    public long countUserConversations(String userId) {
        return recordRepository.countByUserIdAndDeletedFalse(userId);
    }

    /**
     * 统计场景的对话数
     *
     * 功能：
     * - 快速获取场景的对话总数
     * - 用于场景热度统计
     * - 只计算未删除的记录
     *
     * 应用场景：
     * - 场景分析：了解各场景的使用热度
     * - 资源分配：根据使用量分配计算资源
     *
     * @param scenarioType 场景类型
     * @return 该场景的对话总数
     */
    public long countScenarioConversations(String scenarioType) {
        return recordRepository.countByScenarioTypeAndDeletedFalse(scenarioType);
    }

    /**
     * 软删除对话记录
     *
     * 功能：
     * - 逻辑删除记录（不真正删除数据库数据）
     * - 设置 deleted 标志为 true
     * - 更新修改时间为当前时间
     * - 保留数据完整性用于审计和恢复
     *
     * 设计理由：
     * - 软删除允许数据恢复（如用户误删）
     * - 保留完整的审计日志
     * - 支持数据分析（可查看历史数据）
     * - 避免级联删除的复杂性
     *
     * 使用示例：
     * service.deleteRecord(123L);  // 删除ID为123的记录
     *
     * @param recordId 待删除的对话记录ID
     */
    public void deleteRecord(Long recordId) {
        ConversationRecord record = recordRepository.findById(recordId).orElse(null);
        if (record != null) {
            record.setDeleted(true);
            record.setUpdatedAt(LocalDateTime.now());
            recordRepository.save(record);
        }
    }
}
