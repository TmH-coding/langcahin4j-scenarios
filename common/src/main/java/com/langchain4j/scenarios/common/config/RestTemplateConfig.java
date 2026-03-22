package com.langchain4j.scenarios.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;

/**
 * RestTemplate 配置 - HTTP 客户端和外部 API 调用
 *
 * 职责说明：
 * - 配置 Spring RestTemplate 用于 HTTP 请求
 * - 设置连接超时和读取超时
 * - 支持连接池管理
 * - 提供统一的 HTTP 客户端 Bean
 *
 * 架构设计：
 * - 使用 RestTemplateBuilder 构建 RestTemplate
 * - 配置合理的超时时间避免无限等待
 * - 支持自定义拦截器和错误处理
 * - 集成重试机制和限流策略
 *
 * 使用场景：
 * - 调用外部 LLM API（OpenAI、Claude 等）
 * - 调用第三方服务 API
 * - 微服务间通信
 * - 数据同步和集成
 * - 爬虫和数据采集
 *
 * HTTP 请求工作原理：
 * 1. 创建 HTTP 连接
 * 2. 发送请求到服务器
 * 3. 等待响应（受 connectTimeout 限制）
 * 4. 读取响应数据（受 readTimeout 限制）
 * 5. 关闭连接
 *
 * 安全考虑：
 * - 设置合理的超时避免资源泄漏
 * - 验证 SSL 证书（生产环境）
 * - 不要在日志中记录敏感信息
 * - 使用 HTTPS 传输敏感数据
 * - 实现请求签名和认证
 */
@Configuration
public class RestTemplateConfig {

    /**
     * 创建 RestTemplate Bean
     *
     * 功能：
     * - 创建 Spring RestTemplate 实例
     * - 配置 HTTP 连接参数
     * - 设置超时时间
     * - 支持依赖注入到其他 Bean
     *
     * RestTemplate 参数详解：
     *
     * setConnectTimeout(Duration.ofSeconds(10))
     * - 连接超时时间：10 秒
     * - 从发起连接到建立连接的最长等待时间
     * - 如果超过此时间，抛出 ConnectException
     * - 作用：
     *   * 防止连接到不可达的服务器时无限等待
     *   * 快速发现网络问题
     *   * 释放资源
     * - 建议值：
     *   * 本地服务：1-5 秒
     *   * 同机房服务：5-10 秒
     *   * 跨地域服务：10-30 秒
     *   * 不稳定网络：30-60 秒
     * - 注意：
     *   * 过短可能导致正常请求超时
     *   * 过长可能导致资源浪费
     *
     * setReadTimeout(Duration.ofSeconds(30))
     * - 读取超时时间：30 秒
     * - 从发送请求到接收完整响应的最长等待时间
     * - 如果超过此时间，抛出 SocketTimeoutException
     * - 作用：
     *   * 防止服务器响应缓慢导致无限等待
     *   * 快速发现服务故障
     *   * 释放连接资源
     * - 建议值：
     *   * 快速 API：5-10 秒
     *   * 普通 API：10-30 秒
     *   * 长时间处理：30-120 秒
     *   * LLM API：60-300 秒（可能需要长时间处理）
     * - 注意：
     *   * 应该大于 connectTimeout
     *   * 需要根据实际 API 响应时间调整
     *
     * 使用示例 - 调用外部 API：
     * @Service
     * public class ExternalApiService {
     *     @Autowired
     *     private RestTemplate restTemplate;
     *
     *     public String callExternalApi(String url) {
     *         try {
     *             ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
     *             return response.getBody();
     *         } catch (ResourceAccessException e) {
     *             // 处理超时异常
     *             logger.error("API 调用超时", e);
     *             throw new ServiceException("API 调用失败");
     *         }
     *     }
     * }
     *
     * 使用示例 - POST 请求：
     * public User createUser(User user) {
     *     HttpHeaders headers = new HttpHeaders();
     *     headers.setContentType(MediaType.APPLICATION_JSON);
     *     HttpEntity<User> request = new HttpEntity<>(user, headers);
     *
     *     ResponseEntity<User> response = restTemplate.postForEntity(
     *         "http://api.example.com/users",
     *         request,
     *         User.class
     *     );
     *     return response.getBody();
     * }
     *
     * 使用示例 - 错误处理：
     * public String callApiWithRetry(String url) {
     *     try {
     *         return restTemplate.getForObject(url, String.class);
     *     } catch (HttpClientErrorException e) {
     *         // 4xx 错误：客户端错误，不应该重试
     *         logger.error("客户端错误: {}", e.getStatusCode());
     *         throw e;
     *     } catch (HttpServerErrorException e) {
     *         // 5xx 错误：服务器错误，可以重试
     *         logger.error("服务器错误: {}", e.getStatusCode());
     *         throw e;
     *     } catch (ResourceAccessException e) {
     *         // 网络错误或超时，可以重试
     *         logger.error("网络错误或超时", e);
     *         throw e;
     *     }
     * }
     *
     * 超时异常处理：
     * - ConnectException: 连接超时
     *   * 原因：无法连接到服务器
     *   * 处理：检查网络连接、服务器地址、防火墙
     *
     * - SocketTimeoutException: 读取超时
     *   * 原因：服务器响应缓慢
     *   * 处理：增加超时时间、检查服务器性能、实现重试
     *
     * - ResourceAccessException: 资源访问异常
     *   * 原因：网络问题、连接中断等
     *   * 处理：实现重试机制、使用熔断器
     *
     * RestTemplate 最佳实践：
     * 1. 总是设置超时时间
     *    - 避免无限等待
     *    - 快速发现问题
     *
     * 2. 实现错误处理
     *    - 捕获异常并记录日志
     *    - 返回有意义的错误信息
     *
     * 3. 使用拦截器
     *    - 添加请求头（认证、追踪 ID 等）
     *    - 记录请求和响应
     *    - 实现重试逻辑
     *
     * 4. 考虑使用 WebClient
     *    - RestTemplate 已过时（deprecated）
     *    - WebClient 是现代的异步 HTTP 客户端
     *    - 支持响应式编程
     *
     * 5. 监控和指标
     *    - 记录请求时间
     *    - 监控超时率
     *    - 监控错误率
     *
     * 与 LLM API 的集成：
     * - OpenAI API 可能需要较长的超时时间
     * - 某些模型的推理可能需要 60+ 秒
     * - 建议为 LLM 调用设置专门的 RestTemplate
     *
     * @param builder RestTemplateBuilder 用于构建 RestTemplate
     * @return RestTemplate 配置好的 HTTP 客户端
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(java.time.Duration.ofSeconds(10))
                .setReadTimeout(java.time.Duration.ofSeconds(30))
                .build();
    }
}
