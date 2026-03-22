package com.langchain4j.scenarios.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS 跨域配置 - 前后端通信支持和跨域资源共享
 *
 * 职责说明：
 * - 配置跨域资源共享（CORS）策略
 * - 允许前端应用跨域请求后端 API
 * - 支持浏览器预检请求（OPTIONS）
 * - 配置允许的源、方法、头部
 *
 * 架构设计：
 * - 使用 CorsFilter 实现 CORS 过滤
 * - 使用 UrlBasedCorsConfigurationSource 配置 URL 路径
 * - 支持通配符和具体路径配置
 * - 配置预检请求缓存时间
 *
 * 使用场景：
 * - 前端 Web 应用调用后端 API
 * - 移动应用跨域请求
 * - 第三方应用集成
 * - 开发环境跨域调试
 * - 微服务间的跨域通信
 *
 * CORS 工作流程：
 * 1. 浏览器发送预检请求（OPTIONS）
 * 2. 服务器返回 CORS 头部信息
 * 3. 浏览器检查是否允许跨域
 * 4. 允许则发送实际请求，否则阻止
 *
 * 安全考虑：
 * - 生产环境应限制允许的源（不使用 *）
 * - 只允许必要的 HTTP 方法
 * - 限制允许的请求头
 * - 设置合理的预检缓存时间
 * - 避免暴露敏感信息
 * - 实现请求验证和授权
 *
 * 性能考虑：
 * - 预检请求会增加网络往返
 * - 合理设置缓存时间减少预检请求
 * - 避免过度限制导致功能受限
 * - 监控 CORS 相关的错误
 */
@Configuration
public class CorsConfig {

    /**
     * 创建 CORS 过滤器
     *
     * 功能：
     * - 拦截所有请求并检查 CORS 策略
     * - 添加 CORS 响应头
     * - 处理预检请求（OPTIONS）
     * - 支持凭证传递
     *
     * CORS 配置参数详解：
     *
     * addAllowedOrigin("*")
     * - 允许的源（Origin）
     * - "*" 表示允许所有源
     * - 生产环境建议：
     *   * config.setAllowedOrigins(Arrays.asList("https://example.com", "https://app.example.com"))
     *   * 明确列出允许的域名
     *   * 避免使用通配符
     * - 安全性：* 允许任何网站访问，存在安全风险
     * - 源的格式：protocol://domain:port
     *   * 示例：https://example.com, http://localhost:3000
     *
     * addAllowedMethod("GET", "POST", "PUT", "DELETE", "OPTIONS")
     * - 允许的 HTTP 方法
     * - GET: 获取资源
     * - POST: 创建资源
     * - PUT: 更新资源
     * - DELETE: 删除资源
     * - OPTIONS: 预检请求（浏览器自动发送）
     * - PATCH: 部分更新资源
     * - HEAD: 获取资源头信息
     * - 建议：只允许必要的方法
     * - 安全性：限制方法可以减少攻击面
     *
     * addAllowedHeader("*")
     * - 允许的请求头
     * - "*" 表示允许所有请求头
     * - 常见请求头：
     *   * Content-Type: 请求体格式（application/json 等）
     *   * Authorization: 认证令牌（Bearer token 等）
     *   * X-Requested-With: AJAX 标识
     *   * Accept: 响应格式
     *   * Accept-Language: 语言偏好
     * - 生产环境建议：
     *   * config.setAllowedHeaders(Arrays.asList("Content-Type", "Authorization"))
     *   * 明确列出允许的请求头
     *   * 避免使用通配符
     * - 安全性：限制请求头可以防止某些攻击
     *
     * setAllowCredentials(true)
     * - 是否允许发送凭证（Cookie、HTTP 认证）
     * - true: 允许浏览器发送凭证
     * - false: 不允许
     * - 注意：当 allowCredentials=true 时，allowedOrigins 不能使用 "*"
     * - 凭证包括：
     *   * Cookie
     *   * HTTP 认证信息
     *   * TLS 客户端证书
     * - 应用场景：
     *   * 需要发送 Cookie 的请求
     *   * 需要 HTTP 基本认证的请求
     *   * 需要客户端证书的请求
     *
     * setMaxAge(3600L)
     * - 预检请求的缓存时间（秒）
     * - 3600 秒 = 1 小时
     * - 浏览器在此时间内不会再发送预检请求
     * - 建议值：
     *   * 开发环境：60-300 秒（快速测试）
     *   * 生产环境：3600-86400 秒（减少预检请求）
     * - 性能考虑：
     *   * 缓存时间越长，预检请求越少
     *   * 但配置变更需要等待缓存过期
     *   * 平衡性能和灵活性
     *
     * registerCorsConfiguration("/api/**", config)
     * - 注册 CORS 配置到特定路径
     * - "/api/**" 表示所有 /api 开头的路径
     * - 可以为不同路径配置不同的 CORS 策略
     * - 路径匹配规则：
     *   * /api/** - 匹配 /api 及其所有子路径
     *   * /api/v1/** - 匹配 /api/v1 及其所有子路径
     *   * /public/** - 匹配 /public 及其所有子路径
     *
     * 使用示例 - 前端 JavaScript 代码：
     * fetch('http://localhost:8081/api/chat', {
     *     method: 'POST',
     *     headers: {
     *         'Content-Type': 'application/json',
     *         'Authorization': 'Bearer token'
     *     },
     *     credentials: 'include',  // 发送凭证
     *     body: JSON.stringify({ message: 'Hello' })
     * })
     * .then(response => response.json())
     * .then(data => console.log(data));
     *
     * 预检请求示例：
     * OPTIONS /api/chat HTTP/1.1
     * Host: localhost:8081
     * Origin: http://localhost:3000
     * Access-Control-Request-Method: POST
     * Access-Control-Request-Headers: Content-Type
     *
     * 响应示例：
     * HTTP/1.1 200 OK
     * Access-Control-Allow-Origin: http://localhost:3000
     * Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
     * Access-Control-Allow-Headers: *
     * Access-Control-Allow-Credentials: true
     * Access-Control-Max-Age: 3600
     *
     * 生产环境配置示例：
     * CorsConfiguration config = new CorsConfiguration();
     * config.setAllowedOrigins(Arrays.asList(
     *     "https://example.com",
     *     "https://app.example.com"
     * ));
     * config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
     * config.setAllowedHeaders(Arrays.asList("Content-Type", "Authorization"));
     * config.setAllowCredentials(true);
     * config.setMaxAge(3600L);
     *
     * 常见问题：
     * 1. 跨域请求被阻止
     *    - 检查 Origin 是否在允许列表中
     *    - 检查请求方法是否被允许
     *    - 检查请求头是否被允许
     *    - 查看浏览器控制台的 CORS 错误信息
     *
     * 2. 预检请求失败
     *    - 确保 OPTIONS 方法被允许
     *    - 检查预检请求的响应头
     *    - 验证 CORS 配置是否正确
     *
     * 3. Cookie 无法发送
     *    - 确保 allowCredentials 设置为 true
     *    - 确保前端请求中设置 credentials: 'include'
     *    - 确保 allowedOrigins 不使用 "*"
     *
     * 最佳实践：
     * 1. 明确指定允许的源，不使用通配符
     * 2. 只允许必要的 HTTP 方法
     * 3. 只允许必要的请求头
     * 4. 根据环境调整 CORS 配置
     * 5. 定期审查 CORS 配置
     * 6. 监控 CORS 相关的错误
     * 7. 实现额外的安全措施（如 CSRF 保护）
     *
     * @return CorsFilter CORS 过滤器
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.addAllowedOrigin("*");                       // 允许所有源（生产环境应限制）
        config.addAllowedMethod("GET");                     // 允许 GET 请求
        config.addAllowedMethod("POST");                    // 允许 POST 请求
        config.addAllowedMethod("PUT");                     // 允许 PUT 请求
        config.addAllowedMethod("DELETE");                  // 允许 DELETE 请求
        config.addAllowedMethod("OPTIONS");                 // 允许 OPTIONS 预检请求
        config.addAllowedHeader("*");                       // 允许所有请求头
        config.setAllowCredentials(true);                   // 允许发送凭证
        config.setMaxAge(3600L);                            // 预检请求缓存 1 小时

        source.registerCorsConfiguration("/api/**", config);
        return new CorsFilter(source);
    }
}
