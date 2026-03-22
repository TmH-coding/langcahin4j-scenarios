package com.langchain4j.scenarios.common.config;

import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

/**
 * 分布式追踪配置 - 请求链路追踪和性能监控
 *
 * 职责说明：
 * - 配置 Micrometer Tracing 分布式追踪
 * - 为每个请求生成唯一的 traceId 和 spanId
 * - 自动在日志中添加追踪信息
 * - 支持跨服务请求链路追踪
 *
 * 架构设计：
 * - 使用 Micrometer Tracing 作为追踪门面
 * - 支持多种追踪后端（Zipkin、Jaeger 等）
 * - 自动拦截 HTTP 请求和响应
 * - 支持自定义 span 和事件
 *
 * 使用场景：
 * - 追踪用户请求在系统中的完整路径
 * - 识别性能瓶颈和慢查询
 * - 调试分布式系统中的问题
 * - 分析服务间的调用关系
 * - 性能分析和优化
 *
 * 分布式追踪工作原理：
 * 1. 请求到达时，生成唯一的 traceId
 * 2. 每个操作（span）都有唯一的 spanId
 * 3. 跨服务调用时，traceId 在请求头中传播
 * 4. 所有 span 数据收集到追踪系统
 * 5. 追踪系统展示完整的请求链路
 *
 * 安全考虑：
 * - 不要在追踪数据中记录敏感信息
 * - 限制追踪系统的访问权限
 * - 定期清理过期的追踪数据
 * - 监控追踪系统的性能
 */
@Slf4j
@Configuration
@ConditionalOnClass(Tracer.class)
public class TracingConfig {

    /**
     * 分布式追踪自动配置
     *
     * 功能：
     * - 初始化 Micrometer Tracing
     * - 为每个请求生成唯一的 traceId 和 spanId
     * - 自动在日志中添加追踪信息
     * - 支持跨服务传播
     *
     * Micrometer Tracing 参数详解：
     *
     * @ConditionalOnClass(Tracer.class)
     * - 条件化配置：仅当 Tracer 类存在时启用
     * - Tracer 来自 micrometer-tracing-core 依赖
     * - 如果依赖不存在，此配置不会被加载
     * - 作用：
     *   * 避免依赖缺失导致的错误
     *   * 支持可选的追踪功能
     *
     * traceId（追踪 ID）
     * - 唯一标识一个完整的请求链路
     * - 格式：16 位十六进制字符串
     * - 示例：4bf92f3577b34da6a3ce929d0e0e4736
     * - 作用：
     *   * 关联同一请求的所有操作
     *   * 在日志中快速搜索相关日志
     *   * 追踪请求在系统中的完整路径
     *
     * spanId（跨度 ID）
     * - 唯一标识一个具体的操作
     * - 格式：8 位十六进制字符串
     * - 示例：b7ad6b7169203331
     * - 作用：
     *   * 标识具体的操作（HTTP 请求、数据库查询等）
     *   * 记录操作的开始和结束时间
     *   * 记录操作的状态和错误信息
     *
     * 使用示例 - 日志中的追踪信息：
     * 2026-03-21 11:24:23 [langchain4j-async-1] INFO  [4bf92f3577b34da6a3ce929d0e0e4736,b7ad6b7169203331] ChatService - 处理聊天消息
     * 2026-03-21 11:24:24 [langchain4j-async-1] INFO  [4bf92f3577b34da6a3ce929d0e0e4736,c9be5c8270c34442] UserService - 获取用户信息
     * 2026-03-21 11:24:25 [langchain4j-async-1] INFO  [4bf92f3577b34da6a3ce929d0e0e4736,d1cf6d9381d45553] LlmService - 调用 LLM API
     *
     * 使用示例 - 跨服务追踪：
     * // 服务 A 处理请求
     * GET /api/chat HTTP/1.1
     * // 自动生成 traceId: 4bf92f3577b34da6a3ce929d0e0e4736
     *
     * // 服务 A 调用服务 B
     * GET /api/user/123 HTTP/1.1
     * X-Trace-Id: 4bf92f3577b34da6a3ce929d0e0e4736
     * X-Span-Id: b7ad6b7169203331
     *
     * // 服务 B 接收请求，继承 traceId，生成新的 spanId
     * // 处理完成后返回响应
     *
     * // 追踪系统收集所有 span 数据
     * // 展示完整的请求链路：
     * // GET /api/chat (4bf92f3577b34da6a3ce929d0e0e4736)
     * //   ├─ GET /api/user/123 (b7ad6b7169203331)
     * //   ├─ POST /api/llm/chat (c9be5c8270c34442)
     * //   └─ POST /api/audit/log (d1cf6d9381d45553)
     *
     * 使用示例 - 自定义 span：
     * @Service
     * public class ChatService {
     *     @Autowired
     *     private Tracer tracer;
     *
     *     public String processChat(String message) {
     *         // 创建新的 span
     *         Span span = tracer.nextSpan().name("process-chat").start();
     *         try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
     *             // 在这个 span 中执行操作
     *             return llmService.chat(message);
     *         } finally {
     *             span.end();
     *         }
     *     }
     * }
     *
     * 追踪后端配置：
     *
     * 1. Zipkin（推荐用于开发）
     *    - 轻量级追踪系统
     *    - 提供 Web UI 查看追踪数据
     *    - 配置：
     *      spring.zipkin.base-url=http://localhost:9411
     *      spring.sleuth.sampler.probability=1.0
     *
     * 2. Jaeger（推荐用于生产）
     *    - 高性能分布式追踪系统
     *    - 支持采样和聚合
     *    - 配置：
     *      otel.exporter.jaeger.endpoint=http://localhost:14250
     *
     * 3. 其他后端
     *    - Datadog
     *    - New Relic
     *    - Elastic APM
     *
     * 采样策略：
     * - 采样率 1.0：记录所有请求（开发环境）
     * - 采样率 0.1：记录 10% 的请求（生产环境）
     * - 自定义采样：根据条件选择性记录
     *
     * 追踪最佳实践：
     * 1. 启用追踪
     *    - 在开发环境启用 100% 采样
     *    - 在生产环境使用较低的采样率
     *
     * 2. 自定义 span
     *    - 为关键操作创建 span
     *    - 记录操作的关键信息
     *
     * 3. 错误处理
     *    - 记录异常信息到 span
     *    - 标记 span 为失败状态
     *
     * 4. 性能优化
     *    - 避免过度采样导致性能下降
     *    - 定期清理过期的追踪数据
     *
     * 5. 安全性
     *    - 不要记录敏感信息（密码、令牌等）
     *    - 限制追踪系统的访问权限
     *
     * 与日志的集成：
     * - traceId 和 spanId 自动添加到日志
     * - 便于在日志系统中搜索相关日志
     * - 支持日志和追踪的关联分析
     *
     * 与指标的集成：
     * - 追踪数据可以生成性能指标
     * - 支持性能分析和优化
     */
    public TracingConfig() {
        log.info("分布式追踪已启用 - Micrometer Tracing");
    }
}
