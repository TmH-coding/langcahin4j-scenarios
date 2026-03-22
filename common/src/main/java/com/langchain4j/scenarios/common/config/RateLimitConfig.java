package com.langchain4j.scenarios.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import com.langchain4j.scenarios.common.interceptor.RateLimitInterceptor;

/**
 * 限流配置 - API 请求速率限制
 *
 * 职责说明：
 * - 配置 API 请求限流策略
 * - 防止恶意用户滥用 API
 * - 保护后端服务免受过载
 * - 实现基于 IP 地址的限流
 *
 * 架构设计：
 * - 使用 Spring MVC 拦截器模式实现限流
 * - 在请求到达 Controller 前进行限流检查
 * - 支持灵活的路径匹配和排除规则
 * - 基于 IP 地址维护请求计数
 *
 * 使用场景：
 * - 防止 DDoS 攻击
 * - 防止 API 被爬虫滥用
 * - 保护免费 API 的公平使用
 * - 防止单个用户过度消耗资源
 * - 保护 LLM API 调用成本
 *
 * 限流工作原理：
 * 1. 请求到达时，拦截器获取客户端 IP
 * 2. 检查该 IP 在时间窗口内的请求数
 * 3. 如果超过限制，返回 429 Too Many Requests
 * 4. 否则，允许请求继续处理
 * 5. 时间窗口过期后，计数器重置
 *
 * 安全考虑：
 * - 需要正确识别客户端真实 IP（考虑代理和负载均衡）
 * - 限流阈值应根据系统容量调整
 * - 应该为健康检查等关键端点排除限流
 * - 可以为不同用户级别设置不同的限流策略
 */
@Configuration
public class RateLimitConfig implements WebMvcConfigurer {

    /**
     * 注册限流拦截器
     *
     * 功能：
     * - 将 RateLimitInterceptor 注册到 Spring MVC 拦截器链
     * - 配置拦截器应用的路径模式
     * - 配置需要排除的路径
     *
     * 拦截器配置参数详解：
     *
     * addInterceptor(rateLimitInterceptor())
     * - 添加限流拦截器到拦截器链
     * - 拦截器会在 Controller 处理前后执行
     * - 执行顺序：preHandle → Controller → postHandle → afterCompletion
     *
     * addPathPatterns(\"/api/**\")
     * - 指定拦截器应用的路径模式
     * - \"/api/**\" 表示所有 /api 开头的路径
     * - 支持 Ant 风格的路径匹配：
     *   * /api/* - 匹配 /api 下的一级路径
     *   * /api/** - 匹配 /api 下的所有路径（包括多级）
     *   * /api/* send - 匹配 /api/xxx/send 的路径
     *
     * excludePathPatterns(\"/api/health\")
     * - 指定需要排除的路径
     * - 这些路径不会被拦截器处理
     * - 常见的排除路径：
     *   * /api/health - 健康检查端点
     *   * /api/metrics - 监控指标端点
     *   * /api/public/* - 公开端点
     * - 排除原因：
     *   * 健康检查需要快速响应，不应受限流影响
     *   * 监控系统需要频繁查询，不应受限流影响
     *   * 公开端点可能需要特殊的限流策略
     *
     * 使用示例：
     * // 配置多个拦截器
     * registry.addInterceptor(rateLimitInterceptor())
     *         .addPathPatterns(\"/api/**\")
     *         .excludePathPatterns(\"/api/health\", \"/api/metrics\");
     *
     * registry.addInterceptor(loggingInterceptor())
     *         .addPathPatterns(\"/**\");
     *
     * // 拦截器执行顺序
     * // 请求进入：rateLimitInterceptor.preHandle → loggingInterceptor.preHandle
     * // 响应返回：loggingInterceptor.postHandle → rateLimitInterceptor.postHandle
     *
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/health");
    }

    /**
     * 创建限流拦截器 Bean
     *
     * 功能：
     * - 创建 RateLimitInterceptor 实例
     * - 由 Spring 容器管理其生命周期
     * - 在 addInterceptors 方法中被注入
     *
     * RateLimitInterceptor 职责：
     * - 在 preHandle 方法中检查请求是否超过限流阈值
     * - 维护每个 IP 地址的请求计数
     * - 管理时间窗口和计数器重置
     * - 返回 429 状态码表示限流
     *
     * 限流算法选择：
     * - 固定窗口：简单但可能有边界问题
     * - 滑动窗口：更精确但实现复杂
     * - 令牌桶：支持突发流量
     * - 漏桶：平滑流量
     *
     * 限流参数建议：
     * - 时间窗口：1 分钟或 1 小时
     * - 请求限制：
     *   * 公开 API：100-1000 请求/小时
     *   * 认证用户：1000-10000 请求/小时
     *   * 高级用户：无限制或更高限制
     *
     * 使用示例：
     * // 在 RateLimitInterceptor 中实现
     * @Override
     * public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
     *     String clientIp = getClientIp(request);
     *     int requestCount = getRequestCount(clientIp);
     *
     *     if (requestCount > RATE_LIMIT) {
     *         response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
     *         return false;  // 阻止请求继续处理
     *     }
     *
     *     incrementRequestCount(clientIp);
     *     return true;  // 允许请求继续处理
     * }
     *
     * @return RateLimitInterceptor 限流拦截器实例
     */
    @Bean
    public RateLimitInterceptor rateLimitInterceptor() {
        return new RateLimitInterceptor();
    }
}
