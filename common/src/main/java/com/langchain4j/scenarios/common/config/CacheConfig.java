package com.langchain4j.scenarios.common.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

/**
 * 缓存配置 - Redis 缓存管理和性能优化
 *
 * 职责说明：
 * - 启用 Spring Cache 框架
 * - 配置 Redis 作为缓存后端
 * - 设置缓存过期策略
 * - 支持 @Cacheable、@CacheEvict 等注解
 *
 * 架构设计：
 * - 使用 @EnableCaching 启用缓存功能
 * - 使用 RedisCacheManager 管理缓存
 * - 配置统一的缓存过期时间
 * - 禁用空值缓存避免缓存穿透
 *
 * 使用场景：
 * - 缓存 LLM 的响应结果
 * - 缓存用户信息和会话数据
 * - 缓存频繁查询的数据
 * - 减少数据库查询，提高性能
 * - 缓存 API 调用结果
 *
 * 缓存策略：
 * - TTL（生存时间）：1 小时
 * - 空值缓存：禁用（避免缓存 null 值）
 * - 缓存键生成：使用方法名和参数
 * - 缓存更新：支持主动更新和被动过期
 *
 * 性能优化：
 * - 减少数据库查询次数
 * - 降低 LLM API 调用成本
 * - 提高应用响应速度
 * - 减轻后端服务压力
 * - 支持高并发访问
 *
 * 缓存穿透、击穿、雪崩问题：
 * - 缓存穿透：查询不存在的数据，每次都穿过缓存到数据库
 *   * 解决方案：禁用空值缓存，使用布隆过滤器
 * - 缓存击穿：热点数据过期，大量请求穿过缓存
 *   * 解决方案：使用互斥锁，延长热点数据过期时间
 * - 缓存雪崩：大量缓存同时过期，导致数据库压力激增
 *   * 解决方案：随机化过期时间，使用本地缓存
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 配置 Redis 缓存管理器
     *
     * 功能：
     * - 创建 RedisCacheManager Bean
     * - 配置缓存的默认行为
     * - 设置缓存过期时间
     * - 配置缓存键和值的序列化方式
     *
     * 缓存配置参数：
     *
     * entryTtl(Duration.ofHours(1))
     * - 缓存条目的生存时间（TTL）
     * - 1 小时后自动过期
     * - 可根据业务需求调整：
     *   * 热数据：30 分钟（频繁访问的数据）
     *   * 普通数据：1 小时（一般数据）
     *   * 冷数据：24 小时（不常访问的数据）
     *   * 实时数据：5-10 分钟（需要及时更新的数据）
     * - 过短会导致缓存效率低，过长会导致数据不一致
     *
     * disableCachingNullValues()
     * - 禁用空值缓存
     * - 避免缓存穿透问题
     * - 缓存穿透：查询不存在的数据，每次都穿过缓存到数据库
     * - 解决方案：
     *   * 不缓存 null 值（当前方案）
     *   * 使用布隆过滤器
     *   * 缓存特殊值（如 "NOT_FOUND"）
     *
     * 使用示例 - 基础缓存：
     * @Service
     * public class UserService {
     *     @Cacheable(value = "users", key = "#userId")
     *     public User getUserById(String userId) {
     *         // 首次调用时执行，结果缓存 1 小时
     *         return userRepository.findById(userId);
     *     }
     * }
     *
     * 使用示例 - 缓存更新：
     * @Service
     * public class UserService {
     *     @CacheEvict(value = "users", key = "#userId")
     *     public void updateUser(String userId, User user) {
     *         // 更新用户后清除缓存
     *         userRepository.save(user);
     *     }
     *
     *     @CacheEvict(value = "users", allEntries = true)
     *     public void clearAllUserCache() {
     *         // 清除所有用户缓存
     *     }
     * }
     *
     * 使用示例 - 缓存条件：
     * @Service
     * public class ProductService {
     *     @Cacheable(value = "products", key = "#productId", condition = "#productId > 0")
     *     public Product getProduct(String productId) {
     *         // 只缓存 productId > 0 的结果
     *         return productRepository.findById(productId);
     *     }
     * }
     *
     * 使用示例 - 缓存除非：
     * @Service
     * public class OrderService {
     *     @Cacheable(value = "orders", key = "#orderId", unless = "#result == null")
     *     public Order getOrder(String orderId) {
     *         // 不缓存 null 结果
     *         return orderRepository.findById(orderId);
     *     }
     * }
     *
     * 缓存注解说明：
     * - @Cacheable: 如果缓存中有数据，返回缓存；否则执行方法并缓存结果
     *   * 适用于查询操作
     *   * 参数：value（缓存名称）、key（缓存键）、condition（条件）、unless（除非）
     *
     * - @CacheEvict: 清除指定缓存
     *   * 适用于更新和删除操作
     *   * 参数：value（缓存名称）、key（缓存键）、allEntries（清除所有）
     *
     * - @CachePut: 总是执行方法，并更新缓存
     *   * 适用于需要更新缓存的操作
     *   * 参数：value（缓存名称）、key（缓存键）
     *
     * - @Caching: 组合多个缓存操作
     *   * 适用于复杂的缓存场景
     *   * 参数：cacheable、evict、put
     *
     * 缓存键生成规则：
     * - 默认：方法名 + 参数值
     * - 自定义：key = "#userId" 使用参数名
     * - 复杂键：key = "#user.id + ':' + #user.name"
     * - 静态键：key = "'static_key'"
     * - 条件键：key = "#userId > 0 ? #userId : 'default'"
     *
     * 缓存键最佳实践：
     * - 使用有意义的键名
     * - 避免键冲突
     * - 考虑键的长度
     * - 使用分隔符组织键的层级
     *
     * 性能考虑：
     * - 缓存命中率：监控缓存的命中率
     * - 缓存大小：监控 Redis 内存使用
     * - 缓存更新：及时更新过期的缓存
     * - 缓存预热：应用启动时预加载热点数据
     *
     * 监控和调试：
     * - 使用 Redis CLI 查看缓存数据
     * - 监控缓存命中率和大小
     * - 记录缓存操作日志
     * - 定期分析缓存效率
     *
     * 常见问题：
     * 1. 缓存不生效
     *    - 确保方法上有 @Cacheable 注解
     *    - 确保通过 Spring 容器调用（不能直接 new）
     *    - 确保方法不是 private（代理无法拦截）
     *    - 检查缓存条件是否满足
     *
     * 2. 缓存数据不一致
     *    - 确保更新操作使用 @CacheEvict
     *    - 检查缓存过期时间是否合理
     *    - 实现缓存预热机制
     *    - 使用消息队列同步缓存
     *
     * 3. 缓存穿透
     *    - 禁用空值缓存（当前配置）
     *    - 使用布隆过滤器
     *    - 缓存特殊值
     *
     * @param connectionFactory Redis 连接工厂
     * @return RedisCacheManager 缓存管理器
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))              // 设置缓存过期时间为 1 小时
                .disableCachingNullValues();                // 禁用缓存空值

        return RedisCacheManager.create(connectionFactory);
    }
}
