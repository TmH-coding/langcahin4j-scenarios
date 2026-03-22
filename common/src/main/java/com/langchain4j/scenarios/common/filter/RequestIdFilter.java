package com.langchain4j.scenarios.common.filter;

import com.langchain4j.scenarios.common.util.RequestIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 请求 ID 过滤器 - HTTP 请求的唯一标识生成和追踪
 *
 * 职责说明：
 * - 为每个 HTTP 请求生成唯一 ID
 * - 将请求 ID 添加到响应头
 * - 将请求 ID 添加到 MDC 用于日志追踪
 * - 支持分布式追踪和链路追踪
 * - 实现请求的端到端追踪
 *
 * 架构设计：
 * - 继承 OncePerRequestFilter 确保每个请求只执行一次
 * - 使用 RequestIdGenerator 生成唯一 ID
 * - 使用 SLF4J MDC 存储请求 ID
 * - 支持请求头的标准化
 * - 易于集成和使用
 *
 * 使用场景：
 * - HTTP 请求追踪
 * - 分布式系统中的链路追踪
 * - 日志关联和聚合
 * - 性能监控和分析
 * - 错误追踪和调试
 *
 * 请求追踪工作原理：
 * 1. 请求到达时过滤器拦截
 * 2. 生成唯一的请求 ID
 * 3. 将 ID 添加到响应头（X-Request-ID）
 * 4. 将 ID 添加到 MDC 上下文
 * 5. 继续处理请求
 * 6. 所有日志自动包含请求 ID
 * 7. 请求完成后清理 MDC
 *
 * 请求头规范：
 * - 请求头名称：X-Request-ID
 * - 请求头值：REQ-{32位UUID}
 * - 示例：REQ-550e8400e29b41d4a716446655440000
 *
 * 性能考虑：
 * - 过滤器执行速度快
 * - 请求 ID 生成时间复杂度 O(1)
 * - 支持高并发请求
 * - MDC 操作性能开销小
 * - 建议在所有请求前执行
 *
 * 安全考虑：
 * - 请求 ID 不包含敏感信息
 * - 请求 ID 用于追踪，不用于认证
 * - 限制请求 ID 的访问权限
 * - 记录请求 ID 用于审计
 *
 * 扩展建议：
 * - 可以添加请求头的自定义名称
 * - 可以支持从请求头中读取已有的 ID
 * - 可以添加请求 ID 的验证
 * - 可以实现请求 ID 的压缩
 * - 可以支持多种 ID 生成策略
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RequestIdFilter extends OncePerRequestFilter {

    private final RequestIdGenerator requestIdGenerator;

    private static final String REQUEST_ID_HEADER = "X-Request-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 生成请求ID
        String requestId = requestIdGenerator.generateRequestId("REQ");

        // 添加到响应头
        response.setHeader(REQUEST_ID_HEADER, requestId);

        // 添加到MDC用于日志追踪
        org.slf4j.MDC.put("requestId", requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            org.slf4j.MDC.remove("requestId");
        }
    }
}
