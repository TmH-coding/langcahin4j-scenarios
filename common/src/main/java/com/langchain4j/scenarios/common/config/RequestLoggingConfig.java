package com.langchain4j.scenarios.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * 请求日志配置 - HTTP 请求和响应日志记录
 *
 * 职责说明：
 * - 配置详细的 HTTP 请求日志记录
 * - 记录请求头、参数、请求体等信息
 * - 用于调试、审计和性能分析
 * - 帮助快速定位问题
 *
 * 架构设计：
 * - 使用 Spring 的 CommonsRequestLoggingFilter
 * - 在 Servlet 过滤器层面记录请求信息
 * - 支持灵活的日志配置选项
 * - 可以限制记录的请求体大小
 *
 * 使用场景：
 * - 开发环境调试 API 问题
 * - 生产环境审计用户操作
 * - 性能分析和优化
 * - 安全审计和合规性检查
 * - 问题排查和故障诊断
 *
 * 日志记录工作原理：
 * 1. 请求到达时，过滤器拦截请求
 * 2. 提取请求信息（客户端 IP、URL、参数等）
 * 3. 将信息写入日志
 * 4. 请求继续处理
 * 5. 响应返回时，记录响应信息
 *
 * 安全考虑：
 * - 不要在生产环境记录敏感信息（密码、令牌等）
 * - 限制请求体大小避免日志过大
 * - 定期清理日志文件
 * - 使用日志脱敏工具隐藏敏感数据
 * - 考虑日志存储的安全性
 */
@Configuration
public class RequestLoggingConfig {

    /**
     * 创建请求日志过滤器
     *
     * 功能：
     * - 创建 CommonsRequestLoggingFilter Bean
     * - 配置日志记录的详细程度
     * - 设置请求体大小限制
     * - 自定义日志消息前缀
     *
     * 日志配置参数详解：
     *
     * setIncludeClientInfo(true)
     * - 是否记录客户端信息
     * - true: 记录客户端 IP、会话 ID 等
     * - false: 不记录客户端信息
     * - 日志示例：
     *   * client=192.168.1.100
     *   * session=ABC123DEF456
     * - 用途：
     *   * 追踪用户操作
     *   * 识别恶意请求来源
     *   * 分析用户行为
     *
     * setIncludeQueryString(true)
     * - 是否记录查询字符串（URL 参数）
     * - true: 记录 URL 中的参数
     * - false: 不记录
     * - 日志示例：
     *   * query=userId=123&action=delete
     * - 注意：
     *   * 可能包含敏感信息（密钥、令牌等）
     *   * 需要谨慎处理
     *
     * setIncludePayload(true)
     * - 是否记录请求体（POST/PUT 数据）
     * - true: 记录请求体内容
     * - false: 不记录
     * - 日志示例：
     *   * payload={\"username\":\"admin\",\"password\":\"***\"}
     * - 注意：
     *   * 可能包含大量数据
     *   * 可能包含敏感信息
     *   * 需要配合 maxPayloadLength 使用
     *
     * setMaxPayloadLength(10000)
     * - 记录的最大请求体大小（字节）
     * - 10000 = 10 KB
     * - 超过此大小的请求体会被截断
     * - 建议值：
     *   * 开发环境：10000-50000 字节
     *   * 生产环境：1000-5000 字节
     * - 作用：
     *   * 防止日志文件过大
     *   * 避免记录大型文件上传
     *   * 保护敏感数据
     *
     * setIncludeHeaders(true)
     * - 是否记录请求头
     * - true: 记录所有请求头
     * - false: 不记录
     * - 日志示例：
     *   * headers={Content-Type=application/json, Authorization=Bearer xxx}
     * - 常见请求头：
     *   * Content-Type: 请求体格式
     *   * Authorization: 认证令牌
     *   * User-Agent: 客户端信息
     *   * Accept: 期望的响应格式
     * - 注意：
     *   * Authorization 头可能包含敏感令牌
     *   * 需要脱敏处理
     *
     * setAfterMessagePrefix(\"REQUEST DATA : \")
     * - 日志消息的前缀
     * - 用于在日志中快速识别请求日志
     * - 日志示例：
     *   * REQUEST DATA : uri=/api/chat;client=192.168.1.100;...
     * - 可以自定义前缀便于日志搜索
     *
     * 使用示例 - 日志输出：
     * REQUEST DATA : uri=/api/chat;client=192.168.1.100;method=POST;
     * query=;user=admin;payload={\"message\":\"Hello\"}
     *
     * 使用示例 - 在 logback.xml 中配置日志级别：
     * <logger name=\"org.springframework.web.filter.CommonsRequestLoggingFilter\" level=\"DEBUG\"/>
     *
     * 使用示例 - 日志脱敏：
     * @Component
     * public class SensitiveDataMasker {
     *     public String maskSensitiveData(String logMessage) {
     *         // 隐藏密码
     *         logMessage = logMessage.replaceAll(\"password=\\\\w+\", \"password=***\");
     *         // 隐藏令牌
     *         logMessage = logMessage.replaceAll(\"Authorization=Bearer \\\\w+\", \"Authorization=Bearer ***\");
     *         return logMessage;
     *     }
     * }
     *
     * 日志记录最佳实践：
     * 1. 开发环境
     *    - 启用所有日志选项便于调试
     *    - 设置较大的 maxPayloadLength
     *    - 记录详细的请求和响应信息
     *
     * 2. 生产环境
     *    - 禁用或限制日志记录
     *    - 设置较小的 maxPayloadLength
     *    - 对敏感信息进行脱敏
     *    - 定期清理日志文件
     *
     * 3. 性能考虑
     *    - 日志记录会增加请求处理时间
     *    - 在高并发场景下可能影响性能
     *    - 考虑使用异步日志记录
     *
     * 4. 安全考虑
     *    - 不要记录密码、API 密钥等敏感信息
     *    - 不要记录个人隐私信息（身份证号、电话号码等）
     *    - 限制日志文件的访问权限
     *    - 定期审计日志内容
     *
     * 5. 存储和分析
     *    - 使用集中式日志系统（ELK、Splunk 等）
     *    - 设置日志保留策略
     *    - 建立日志告警规则
     *    - 定期分析日志发现问题
     *
     * @return CommonsRequestLoggingFilter 配置好的请求日志过滤器
     */
    @Bean
    public CommonsRequestLoggingFilter requestLoggingFilter() {
        CommonsRequestLoggingFilter loggingFilter = new CommonsRequestLoggingFilter();
        loggingFilter.setIncludeClientInfo(true);
        loggingFilter.setIncludeQueryString(true);
        loggingFilter.setIncludePayload(true);
        loggingFilter.setMaxPayloadLength(10000);
        loggingFilter.setIncludeHeaders(true);
        loggingFilter.setAfterMessagePrefix("REQUEST DATA : ");
        return loggingFilter;
    }
}
