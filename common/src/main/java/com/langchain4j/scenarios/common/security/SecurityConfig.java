package com.langchain4j.scenarios.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 配置 - API 安全和认证授权
 *
 * 职责说明：
 * - 配置 Spring Security 安全过滤链
 * - 定义 API 访问权限规则
 * - 配置 CORS 和 CSRF 保护
 * - 支持认证和授权
 *
 * 架构设计：
 * - 使用 Spring Security 6.x 新 API（Lambda DSL）
 * - 配置 SecurityFilterChain Bean
 * - 支持灵活的授权规则
 * - 集成 CORS 配置
 *
 * 使用场景：
 * - 保护 API 端点免受未授权访问
 * - 实现基于角色的访问控制（RBAC）
 * - 防止 CSRF 攻击
 * - 支持跨域请求
 * - 实现认证和授权
 *
 * Spring Security 工作原理：
 * 1. 请求到达时，通过 SecurityFilterChain 处理
 * 2. 检查请求是否需要认证
 * 3. 如果需要，验证用户身份
 * 4. 检查用户是否有权限访问资源
 * 5. 允许或拒绝请求
 *
 * 安全考虑：
 * - 生产环境应启用 CSRF 保护
 * - 应该使用 HTTPS 传输敏感数据
 * - 应该实现强认证机制（如 JWT）
 * - 应该定期审计安全配置
 * - 应该监控安全事件
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 配置安全过滤链
     *
     * 功能：
     * - 定义 API 访问权限规则
     * - 配置 CSRF 保护
     * - 配置 CORS 支持
     * - 配置认证和授权
     *
     * Spring Security 6.x API 变更：
     * - 使用 Lambda DSL 替代链式调用
     * - 使用 requestMatchers() 替代 antMatchers()
     * - 使用 authorizeHttpRequests() 替代 authorizeRequests()
     * - 使用 Lambda 表达式配置各个功能
     *
     * 安全过滤链配置参数详解：
     *
     * .csrf(csrf -> csrf.disable())
     * - 禁用 CSRF 保护
     * - CSRF（跨站请求伪造）是一种安全威胁
     * - 攻击者诱骗用户在其他网站执行恶意操作
     * - 禁用原因：
     *   * REST API 通常使用 token 认证（如 JWT）
     *   * Token 存储在请求头中，不易被 CSRF 攻击
     *   * 前后端分离架构中，CSRF 保护的必要性降低
     * - 生产环境建议：
     *   * 如果使用 Session 认证，应启用 CSRF 保护
     *   * 如果使用 JWT 认证，可以禁用 CSRF 保护
     *   * 应该根据实际情况评估风险
     * - 启用 CSRF 保护的方式：
     *   * .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
     *
     * .authorizeHttpRequests(authz -> authz...)
     * - 配置 HTTP 请求的授权规则
     * - 定义哪些请求需要认证，哪些可以公开访问
     * - 规则按顺序匹配，第一个匹配的规则生效
     * - 因此应该将更具体的规则放在前面
     *
     * .requestMatchers(\"/swagger-ui/**\", \"/v3/api-docs/**\", \"/swagger-resources/**\").permitAll()
     * - 允许所有人访问 Swagger UI 和 API 文档
     * - 路径说明：
     *   * /swagger-ui/** - Swagger UI 界面
     *   * /v3/api-docs/** - OpenAPI 文档（JSON/YAML）
     *   * /swagger-resources/** - Swagger 资源文件
     * - 作用：
     *   * 便于开发者查看 API 文档
     *   * 便于测试 API 端点
     *   * 不需要认证即可访问
     * - 安全考虑：
     *   * 生产环境应该限制 API 文档的访问权限
     *   * 可以使用 IP 白名单或认证来保护文档
     *   * 避免暴露 API 结构给未授权用户
     *
     * .requestMatchers(\"/actuator/health\").permitAll()
     * - 允许所有人访问健康检查端点
     * - 作用：
     *   * 负载均衡器可以检查应用健康状态
     *   * 监控系统可以定期检查应用可用性
     *   * 不需要认证即可访问
     * - 常见的健康检查端点：
     *   * /actuator/health - 基本健康状态
     *   * /actuator/health/liveness - 应用是否存活
     *   * /actuator/health/readiness - 应用是否就绪
     * - 安全考虑：
     *   * 健康检查端点应该返回最少的信息
     *   * 不要在健康检查中暴露敏感信息
     *   * 可以限制健康检查的访问频率
     *
     * .anyRequest().authenticated()
     * - 所有其他请求都需要认证
     * - 这是一个 catch-all 规则
     * - 确保未明确允许的请求都需要认证
     * - 作用：
     *   * 保护所有 API 端点
     *   * 防止未授权访问
     *   * 提高系统安全性
     *
     * .cors(cors -> {})
     * - 启用 CORS 支持
     * - CORS（跨源资源共享）允许跨域请求
     * - 空的 Lambda 表达式表示使用默认 CORS 配置
     * - 默认配置由 CorsConfig 提供
     * - 作用：
     *   * 允许前端应用跨域调用 API
     *   * 支持浏览器的 CORS 预检请求
     *   * 与 CorsConfig 配置配合使用
     *
     * 使用示例 - 更复杂的授权规则：
     * @Bean
     * public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
     *     http
     *         .csrf(csrf -> csrf.disable())
     *         .authorizeHttpRequests(authz -> authz
     *             // 公开端点
     *             .requestMatchers(\"/api/public/**\").permitAll()
     *             .requestMatchers(\"/swagger-ui/**\", \"/v3/api-docs/**\").permitAll()
     *             // 需要特定角色的端点
     *             .requestMatchers(\"/api/admin/**\").hasRole(\"ADMIN\")
     *             .requestMatchers(\"/api/user/**\").hasRole(\"USER\")
     *             // 所有其他请求需要认证
     *             .anyRequest().authenticated()
     *         )
     *         .cors(cors -> {})
     *         .httpBasic(Customizer.withDefaults());  // 启用 HTTP Basic 认证
     *
     *     return http.build();
     * }
     *
     * 使用示例 - 添加 JWT 认证：
     * @Bean
     * public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
     *     http
     *         .csrf(csrf -> csrf.disable())
     *         .authorizeHttpRequests(authz -> authz
     *             .requestMatchers(\"/api/auth/login\").permitAll()
     *             .anyRequest().authenticated()
     *         )
     *         .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
     *
     *     return http.build();
     * }
     *
     * 使用示例 - 添加异常处理：
     * @Bean
     * public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
     *     http
     *         .csrf(csrf -> csrf.disable())
     *         .authorizeHttpRequests(authz -> authz
     *             .anyRequest().authenticated()
     *         )
     *         .exceptionHandling(ex -> ex
     *             .authenticationEntryPoint((request, response, authException) -> {
     *                 response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
     *                 response.setContentType(\"application/json\");
     *                 response.getWriter().write(\"{\\\"error\\\":\\\"Unauthorized\\\"}\");
     *             })
     *         );
     *
     *     return http.build();
     * }
     *
     * 授权规则最佳实践：
     * 1. 最小权限原则
     *    - 只授予必要的权限
     *    - 默认拒绝，明确允许
     *    - 避免过度授权
     *
     * 2. 规则顺序
     *    - 更具体的规则放在前面
     *    - 更通用的规则放在后面
     *    - 避免规则冲突
     *
     * 3. 公开端点
     *    - 明确列出所有公开端点
     *    - 避免意外暴露敏感端点
     *    - 定期审计公开端点
     *
     * 4. 认证机制
     *    - 选择合适的认证方式（JWT、OAuth2 等）
     *    - 实现强认证（多因素认证等）
     *    - 安全存储凭证
     *
     * 5. 监控和日志
     *    - 记录认证失败事件
     *    - 监控未授权访问尝试
     *    - 及时发现安全问题
     *
     * Spring Security 6.x 迁移指南：
     * - 旧 API：.antMatchers(\"/api/**\").authenticated()
     * - 新 API：.requestMatchers(\"/api/**\").authenticated()
     * - 旧 API：.authorizeRequests()
     * - 新 API：.authorizeHttpRequests()
     * - 旧 API：.csrf().disable()
     * - 新 API：.csrf(csrf -> csrf.disable())
     *
     * @param http HttpSecurity 对象，用于配置安全过滤链
     * @return SecurityFilterChain 配置好的安全过滤链
     * @throws Exception 配置过程中可能抛出的异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-resources/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated()
                )
                .cors(cors -> {});

        return http.build();
    }
}
