package com.langchain4j.scenarios.scenario2.service;

import org.springframework.stereotype.Component;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 文档缓存管理器 - 文档元数据和访问统计管理
 *
 * 职责说明：
 * - 缓存已加载的文档元数据
 * - 管理文档的加载时间和访问统计
 * - 支持文档缓存的查询和清理
 * - 记录文档的访问历史
 * - 支持文档生命周期管理
 *
 * 架构设计：
 * - 使用 @Component 注解注册为 Spring Bean
 * - 使用 ConcurrentHashMap 存储文档元数据（线程安全）
 * - 内嵌 DocumentMetadata 静态类存储文档信息
 * - 使用 Lombok 注解简化代码
 * - 支持并发访问和修改
 *
 * 使用场景：
 * - 缓存文档的元数据信息
 * - 记录文档的访问统计
 * - 支持文档的快速查询
 * - 实现文档的生命周期管理
 * - 用于性能监控和分析
 *
 * 缓存工作原理：
 * 1. 文档加载时，创建 DocumentMetadata 对象
 * 2. 将元数据存储到 documentCache
 * 3. 记录文档的加载时间
 * 4. 每次访问时，更新访问计数和最后访问时间
 * 5. 支持查询文档是否存在
 * 6. 支持删除文档缓存
 *
 * 字段说明：
 * - documentCache: 文档元数据缓存
 *   * 类型：ConcurrentHashMap<String, DocumentMetadata>
 *   * key：documentId（文档唯一标识）
 *   * value：文档元数据对象
 *   * 用途：存储所有已加载文档的元数据
 *
 * DocumentMetadata 字段说明：
 * - documentId: 文档唯一标识
 *   * 格式：自定义ID格式
 *   * 用途：唯一标识文档
 *   * 示例：\"contract_001\"
 *
 * - title: 文档标题
 *   * 格式：文本字符串
 *   * 用途：文档的显示名称
 *   * 示例：\"2024年度合同\"
 *
 * - segmentCount: 段落数
 *   * 格式：整数
 *   * 用途：记录文档分割后的段落数
 *   * 示例：50
 *
 * - loadTime: 加载时间
 *   * 格式：毫秒级时间戳
 *   * 用途：记录文档加载的时间
 *   * 示例：1234567890000
 *
 * - lastAccessTime: 最后访问时间
 *   * 格式：毫秒级时间戳
 *   * 用途：记录文档最后被访问的时间
 *   * 示例：1234567890000
 *
 * - accessCount: 访问计数
 *   * 格式：整数
 *   * 用途：记录文档被访问的次数
 *   * 示例：10
 *
 * 性能考虑：
 * - 使用 ConcurrentHashMap 支持高并发访问
 * - 缓存查询时间复杂度为 O(1)
 * - 缓存添加和删除时间复杂度为 O(1)
 * - 支持数千个文档的缓存
 * - 建议定期清理过期文档
 * - 可以配置缓存大小限制
 * - 监控缓存命中率
 *
 * 安全考虑：
 * - 验证 documentId 的有效性
 * - 防止缓存被篡改
 * - 记录缓存操作日志
 * - 实现缓存的访问控制
 * - 防止缓存信息泄露
 *
 * 可靠性考虑：
 * - 缓存数据存储在内存中，应用重启会丢失
 * - 建议使用 Redis 等持久化存储
 * - 实现缓存的备份和恢复机制
 * - 处理并发访问的竞态条件
 * - 实现缓存的一致性检查
 *
 * 扩展建议：
 * - 可以使用 Redis 替代内存存储
 * - 可以添加缓存过期时间设置
 * - 可以实现缓存的持久化功能
 * - 可以添加缓存的加密存储
 * - 可以支持缓存的导出和导入
 * - 可以实现缓存的版本控制
 * - 可以添加缓存的审计日志
 */
@Component
public class DocumentCacheManager {

    /** 文档元数据缓存 */
    private final Map<String, DocumentMetadata> documentCache = new ConcurrentHashMap<>();

    /**
     * 添加文档到缓存
     *
     * @param documentId 文档ID
     * @param title 文档标题
     * @param segmentCount 段落数
     */
    public void addDocument(String documentId, String title, int segmentCount) {
        DocumentMetadata metadata = DocumentMetadata.builder()
                .documentId(documentId)
                .title(title)
                .segmentCount(segmentCount)
                .loadTime(System.currentTimeMillis())
                .accessCount(0)
                .build();
        documentCache.put(documentId, metadata);
    }

    /**
     * 获取文档元数据
     *
     * @param documentId 文档ID
     * @return 文档元数据
     */
    public DocumentMetadata getDocument(String documentId) {
        DocumentMetadata metadata = documentCache.get(documentId);
        if (metadata != null) {
            metadata.setAccessCount(metadata.getAccessCount() + 1);
            metadata.setLastAccessTime(System.currentTimeMillis());
        }
        return metadata;
    }

    /**
     * 检查文档是否存在
     *
     * @param documentId 文档ID
     * @return true 如果文档存在
     */
    public boolean documentExists(String documentId) {
        return documentCache.containsKey(documentId);
    }

    /**
     * 获取所有文档列表
     *
     * @return 文档列表
     */
    public List<DocumentMetadata> getAllDocuments() {
        return new ArrayList<>(documentCache.values());
    }

    /**
     * 删除文档缓存
     *
     * @param documentId 文档ID
     */
    public void removeDocument(String documentId) {
        documentCache.remove(documentId);
    }

    /**
     * 文档元数据
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DocumentMetadata {
        private String documentId;
        private String title;
        private int segmentCount;
        private long loadTime;
        private long lastAccessTime;
        private int accessCount;
    }
}
