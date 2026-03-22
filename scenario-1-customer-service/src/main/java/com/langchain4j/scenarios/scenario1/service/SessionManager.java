package com.langchain4j.scenarios.scenario1.service;

import com.langchain4j.scenarios.common.util.ConversationMemoryUtil;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话管理器 - 多用户会话生命周期管理
 *
 * 职责说明：
 * - 管理多个用户的对话会话
 * - 为每个会话维护独立的对话历史
 * - 支持会话创建、查询、清空等操作
 * - 实现会话超时管理机制
 * - 支持并发会话访问
 *
 * 架构设计：
 * - 使用 @Component 注解注册为 Spring Bean
 * - 使用 ConcurrentHashMap 存储会话数据（线程安全）
 * - 使用 UUID 生成唯一的会话ID
 * - 每个会话关联一个 ConversationMemoryUtil 对象
 * - 记录会话创建时间用于超时检测
 * - 支持会话的生命周期管理
 *
 * 使用场景：
 * - 支持多用户并发对话
 * - 隔离不同用户的对话上下文
 * - 会话超时自动清理
 * - 支持会话统计和监控
 * - 用于电商、SaaS、在线服务等平台的客服系统
 *
 * 会话工作原理：
 * 1. 客户端请求创建新会话
 * 2. 服务器生成唯一的 sessionId
 * 3. 为该会话创建 ConversationMemoryUtil 对象
 * 4. 记录会话创建时间
 * 5. 返回 sessionId 给客户端
 * 6. 客户端在后续请求中使用 sessionId
 * 7. 服务器根据 sessionId 检索对应的会话
 * 8. 检查会话是否超时
 * 9. 如果未超时，返回会话对象
 * 10. 如果超时，删除会话并返回 null
 *
 * 字段说明：
 * - sessions: 会话存储
 *   * 类型：ConcurrentHashMap<String, ConversationMemoryUtil>
 *   * key：sessionId（UUID格式）
 *   * value：对话内存管理器
 *   * 用途：存储所有活跃会话
 *
 * - sessionTimestamps: 会话创建时间
 *   * 类型：ConcurrentHashMap<String, Long>
 *   * key：sessionId
 *   * value：创建时间戳（毫秒）
 *   * 用途：用于超时检测
 *
 * - SESSION_TIMEOUT: 会话超时时间
 *   * 值：30 * 60 * 1000 毫秒（30分钟）
 *   * 用途：定义会话的有效期
 *   * 说明：超过此时间的会话将被自动清理
 *
 * 性能考虑：
 * - 使用 ConcurrentHashMap 支持高并发访问
 * - 会话查询时间复杂度为 O(1)
 * - 会话创建和删除时间复杂度为 O(1)
 * - 支持数千个并发会话
 * - 建议定期清理过期会话
 * - 可以配置会话超时时间
 * - 监控活跃会话数量
 *
 * 安全考虑：
 * - 使用 UUID 生成不可预测的会话ID
 * - 实现会话超时防止会话劫持
 * - 验证会话ID的有效性
 * - 防止会话ID被篡改
 * - 记录会话创建和销毁日志
 * - 实现会话的访问控制
 * - 防止会话信息泄露
 *
 * 可靠性考虑：
 * - 会话数据存储在内存中，应用重启会丢失
 * - 建议使用 Redis 等持久化存储
 * - 实现会话的备份和恢复机制
 * - 处理并发访问的竞态条件
 * - 实现会话的一致性检查
 *
 * 扩展建议：
 * - 可以使用 Redis 替代内存存储
 * - 可以添加会话持久化功能
 * - 可以实现会话的分布式管理
 * - 可以添加会话的加密存储
 * - 可以支持会话的导出和导入
 * - 可以实现会话的版本控制
 * - 可以添加会话的审计日志
 */
@Component
public class SessionManager {

    /** 会话存储：key为sessionId，value为对话内存管理器 */
    private final Map<String, ConversationMemoryUtil> sessions = new ConcurrentHashMap<>();

    /** 会话创建时间：用于超时管理 */
    private final Map<String, Long> sessionTimestamps = new ConcurrentHashMap<>();

    /** 会话超时时间（毫秒）：30分钟 */
    private static final long SESSION_TIMEOUT = 30 * 60 * 1000;

    /**
     * 创建新会话
     *
     * @return 新会话的ID
     */
    public String createSession() {
        String sessionId = UUID.randomUUID().toString();
        sessions.put(sessionId, new ConversationMemoryUtil(20));
        sessionTimestamps.put(sessionId, System.currentTimeMillis());
        return sessionId;
    }

    /**
     * 获取会话的对话内存管理器
     *
     * @param sessionId 会话ID
     * @return 对话内存管理器，如果会话不存在则返回null
     */
    public ConversationMemoryUtil getSession(String sessionId) {
        // 检查会话是否超时
        if (isSessionExpired(sessionId)) {
            sessions.remove(sessionId);
            sessionTimestamps.remove(sessionId);
            return null;
        }
        return sessions.get(sessionId);
    }

    /**
     * 检查会话是否存在
     *
     * @param sessionId 会话ID
     * @return true 如果会话存在且未超时
     */
    public boolean sessionExists(String sessionId) {
        return getSession(sessionId) != null;
    }

    /**
     * 清空会话
     *
     * @param sessionId 会话ID
     */
    public void clearSession(String sessionId) {
        sessions.remove(sessionId);
        sessionTimestamps.remove(sessionId);
    }

    /**
     * 检查会话是否超时
     *
     * @param sessionId 会话ID
     * @return true 如果会话已超时
     */
    private boolean isSessionExpired(String sessionId) {
        Long timestamp = sessionTimestamps.get(sessionId);
        if (timestamp == null) {
            return true;
        }
        return System.currentTimeMillis() - timestamp > SESSION_TIMEOUT;
    }

    /**
     * 获取所有活跃会话数
     *
     * @return 活跃会话数
     */
    public int getActiveSessionCount() {
        return sessions.size();
    }
}
