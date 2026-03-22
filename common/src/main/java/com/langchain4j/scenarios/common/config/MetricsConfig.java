package com.langchain4j.scenarios.common.config;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Micrometer 指标配置 - 应用性能监控和指标收集
 *
 * 职责说明：
 * - 配置 Micrometer 指标收集框架
 * - 添加通用标签便于指标分类
 * - 支持 Prometheus 等监控系统导出
 * - 提供应用性能和健康状态的可观测性
 *
 * 架构设计：
 * - 使用 Micrometer 作为指标门面
 * - 支持多种监控后端（Prometheus、Grafana 等）
 * - 自动收集 JVM、HTTP、数据库等指标
 * - 支持自定义业务指标
 *
 * 使用场景：
 * - 监控应用性能指标（响应时间、吞吐量等）
 * - 监控系统资源使用（CPU、内存、线程等）
 * - 监控业务指标（用户数、交易量等）
 * - 生成告警和通知
 * - 性能分析和优化
 *
 * 指标收集工作原理：
 * 1. 应用启动时，Micrometer 自动注册各种指标收集器
 * 2. 在请求处理、数据库查询等关键点收集指标
 * 3. 指标数据存储在 MeterRegistry 中
 * 4. 定期导出到监控系统（Prometheus、Grafana 等）
 * 5. 监控系统展示和分析指标数据
 *
 * 安全考虑：
 * - 不要在指标中暴露敏感信息
 * - 限制指标端点的访问权限
 * - 定期清理过期的指标数据
 * - 监控指标存储的安全性
 */
@Configuration
public class MetricsConfig {

    /**
     * 自定义 MeterRegistry 配置
     *
     * 功能：
     * - 为所有指标添加通用标签
     * - 便于在监控系统中分类和过滤指标
     * - 支持多维度的指标分析
     * - 提高指标的可读性和可维护性
     *
     * MeterRegistry 参数详解：
     *
     * commonTags(...)
     * - 为所有指标添加通用标签
     * - 标签是键值对，用于标识指标的属性
     * - 所有指标都会自动包含这些标签
     * - 便于在监控系统中分组和过滤
     *
     * application = "langchain4j-scenarios"
     * - 应用名称标签
     * - 用于识别指标来自哪个应用
     * - 在多应用环境中很重要
     * - 示例：
     *   * application=langchain4j-scenarios
     *   * application=customer-service
     *   * application=document-analysis
     * - 作用：
     *   * 在 Prometheus 中查询特定应用的指标
     *   * 在 Grafana 中创建应用级别的仪表板
     *   * 快速识别指标来源
     *
     * environment = "production"
     * - 环境标签
     * - 用于区分不同的部署环境
     * - 常见值：
     *   * production: 生产环境
     *   * staging: 预发布环境
     *   * development: 开发环境
     *   * test: 测试环境
     * - 作用：
     *   * 分别监控不同环境的性能
     *   * 对比不同环境的指标差异
     *   * 快速定位问题所在的环境
     *
     * 使用示例 - 自动收集的指标：
     * // JVM 指标
     * jvm.memory.used{application="langchain4j-scenarios", environment="production"}
     * jvm.threads.live{application="langchain4j-scenarios", environment="production"}
     * jvm.gc.pause{application="langchain4j-scenarios", environment="production"}
     *
     * // HTTP 指标
     * http.server.requests{application="langchain4j-scenarios", environment="production", method="GET", status="200"}
     * http.server.requests{application="langchain4j-scenarios", environment="production", method="POST", status="201"}
     *
     * // 数据库指标
     * db.connection.active{application="langchain4j-scenarios", environment="production"}
     * db.connection.idle{application="langchain4j-scenarios", environment="production"}
     *
     * 使用示例 - 自定义业务指标：
     * @Service
     * public class ChatService {
     *     @Autowired
     *     private MeterRegistry meterRegistry;
     *
     *     public void processChat(String message) {
     *         // 记录聊天消息数
     *         meterRegistry.counter("chat.messages.total").increment();
     *
     *         // 记录消息长度
     *         meterRegistry.gauge("chat.message.length", message.length());
     *
     *         // 记录处理时间
     *         Timer.Sample sample = Timer.start(meterRegistry);
     *         try {
     *             // 处理逻辑
     *         } finally {
     *             sample.stop(Timer.builder("chat.processing.time")
     *                     .publishPercentiles(0.5, 0.95, 0.99)
     *                     .register(meterRegistry));
     *         }
     *     }
     * }
     *
     * Prometheus 查询示例：
     * // 查询应用的平均响应时间
     * rate(http.server.requests_sum[5m]) / rate(http.server.requests_count[5m])
     *
     * // 查询应用的错误率
     * rate(http.server.requests_total{status=~"5.."}[5m]) / rate(http.server.requests_total[5m])
     *
     * // 查询 JVM 内存使用率
     * jvm.memory.used / jvm.memory.max
     *
     * Grafana 仪表板示例：
     * - 应用概览：请求数、错误率、响应时间
     * - JVM 监控：内存、线程、垃圾回收
     * - 业务指标：聊天消息数、处理时间等
     * - 告警规则：高错误率、高响应时间等
     *
     * 指标最佳实践：
     * 1. 使用有意义的指标名称
     *    - 遵循命名规范
     *    - 便于理解和查询
     *
     * 2. 添加适当的标签
     *    - 便于分组和过滤
     *    - 支持多维度分析
     *
     * 3. 监控关键指标
     *    - 响应时间
     *    - 错误率
     *    - 吞吐量
     *    - 资源使用
     *
     * 4. 设置告警规则
     *    - 高错误率
     *    - 高响应时间
     *    - 资源不足
     *
     * 5. 定期审查指标
     *    - 发现性能瓶颈
     *    - 优化系统性能
     *    - 改进用户体验
     *
     * @return MeterRegistryCustomizer 指标注册表定制器
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCustomizer() {
        return registry -> registry.config()
                .commonTags(
                        "application", "langchain4j-scenarios",
                        "environment", "production"
                );
    }
}
