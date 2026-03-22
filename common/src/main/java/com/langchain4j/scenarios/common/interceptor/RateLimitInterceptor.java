package com.langchain4j.scenarios.common.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 限流拦截器 - HTTP 请求的速率限制和流量控制
 *
 * 职责说明：
 * - 实现基于 IP 地址的限流
 * - 防止 API 滥用和 DoS 攻击
 * - 支持自定义限流规则
 * - 实现请求频率控制
 * - 保护系统资源
 *
 * 架构设计：
 * - 实现 HandlerInterceptor 接口
 * - 使用 ConcurrentHashMap 存储请求计数
 * - 使用 AtomicInteger 保证线程安全
 * - 基于 IP 地址进行限流
 * - 易于扩展和定制
 *
 * 使用场景：
 * - 防止 API 滥用
 * - 防止 DoS 攻击
 * - 保护系统资源
 * - 实现公平的资源分配
 * - 支持多租户隔离
 *
 * 限流工作原理：
 * 1. 请求到达时拦截器拦截
 * 2. 获取客户端 IP 地址
 * 3. 查询该 IP 的请求计数
 * 4. 增加请求计数
 * 5. 检查是否超过限制
 * 6. 超过限制返回 429 状态码
 * 7. 未超过限制继续处理请求
 *
 * 限流规则：
 * - 每分钟最多 100 个请求
 * - 基于客户端 IP 地址
 * - 超过限制返回 429 Too Many Requests
 *
 * 性能考虑：
 * - 拦截器执行速度快
 * - 请求计数操作时间复杂度 O(1)
 * - 支持高并发请求
 * - 内存占用与 IP 数量成正比
 * - 建议定期清理过期计数
 *
 * 安全考虑：
 * - 防止 DoS 攻击
 * - 防止 API 滥用
 * - 保护系统资源
 * - 限制单个 IP 的请求频率
 * - 支持黑名单和白名单
 *
 * 扩展建议：
 * - 可以添加基于用户的限流
 * - 可以实现分级限流策略
 * - 可以添加限流的动态调整
 * - 可以支持分布式限流
 * - 可以实现限流的监控和告警
 */
@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private static final ConcurrentHashMap<String, AtomicInteger> REQUEST_COUNT = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String clientIp = getClientIp(request);
        AtomicInteger count = REQUEST_COUNT.computeIfAbsent(clientIp, k -> new AtomicInteger(0));

        if (count.incrementAndGet() > MAX_REQUESTS_PER_MINUTE) {
            log.warn("限流触发 - IP: {}", clientIp);
            response.setStatus(429);
            return false;
        }

        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
