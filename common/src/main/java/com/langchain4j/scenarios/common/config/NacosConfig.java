package com.langchain4j.scenarios.common.config;

import com.alibaba.cloud.nacos.NacosConfigManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

/**
 * Nacos 配置中心配置 - 动态配置管理和服务发现
 *
 * 职责说明：
 * - 配置 Nacos 配置中心集成
 * - 支持动态配置更新
 * - 支持服务发现和注册
 * - 支持多环境配置管理
 *
 * 架构设计：
 * - 使用 Alibaba Cloud Nacos 作为配置中心
 * - 支持从 Nacos 服务器加载配置
 * - 支持配置的动态刷新（无需重启应用）
 * - 支持服务注册和发现
 *
 * 使用场景：
 * - 集中管理多个应用的配置
 * - 动态更新应用配置（如 LLM API 密钥、限流阈值等）
 * - 服务发现和负载均衡
 * - 多环境配置隔离
 * - 灰度发布和配置灰度
 *
 * Nacos 工作原理：
 * 1. 应用启动时连接到 Nacos 服务器
 * 2. 从 Nacos 加载配置文件
 * 3. 监听配置变更事件
 * 4. 配置更新时自动刷新应用配置
 * 5. 支持服务注册和健康检查
 *
 * 安全考虑：
 * - 保护 Nacos 服务器的访问权限
 * - 不要在配置中存储明文密码
 * - 使用加密存储敏感配置
 * - 定期审计配置变更
 * - 限制配置修改的权限
 */
@Slf4j
@Configuration
@ConditionalOnClass(NacosConfigManager.class)
public class NacosConfig {

    /**
     * Nacos 自动配置
     *
     * 功能：
     * - 初始化 Nacos 配置中心连接
     * - 从 Nacos 服务器加载配置
     * - 支持动态配置刷新
     * - 支持服务发现
     *
     * Nacos 配置参数详解：
     *
     * @ConditionalOnClass(NacosConfigManager.class)
     * - 条件化配置：仅当 NacosConfigManager 类存在时启用
     * - NacosConfigManager 来自 spring-cloud-starter-alibaba-nacos-config 依赖
     * - 如果依赖不存在，此配置不会被加载
     * - 作用：
     *   * 避免依赖缺失导致的错误
     *   * 支持可选的 Nacos 功能
     *
     * Nacos 服务器配置（application.yml）：
     * spring:
     *   cloud:
     *     nacos:
     *       server-addr: localhost:8848
     *       config:
     *         file-extension: yml
     *         namespace: dev
     *         group: DEFAULT_GROUP
     *       discovery:
     *         namespace: dev
     *         group: DEFAULT_GROUP
     *
     * 配置参数说明：
     * - server-addr: Nacos 服务器地址和端口
     * - file-extension: 配置文件格式（yml 或 properties）
     * - namespace: 命名空间（用于环境隔离）
     * - group: 配置分组（用于逻辑分组）
     *
     * 使用示例 - 配置文件结构：
     * Nacos 中的配置文件：
     * - Data ID: langchain4j-scenarios.yml
     * - Group: DEFAULT_GROUP
     * - Namespace: dev
     * - Content:
     *   llm:
     *     provider: openai
     *     model: gpt-4-turbo-preview
     *     temperature: 0.7
     *   rate-limit:
     *     enabled: true
     *     requests-per-minute: 100
     *   cache:
     *     ttl: 3600
     *
     * 使用示例 - 动态配置刷新：
     * @RestController
     * @RequestMapping(\"/api/config\")
     * public class ConfigController {
     *     @Value(\"${llm.temperature:0.7}\")
     *     private double temperature;
     *
     *     @GetMapping(\"/temperature\")
     *     public double getTemperature() {
     *         return temperature;
     *     }
     * }
     *
     * // 在 Nacos 中修改 llm.temperature 的值
     * // 应用会自动刷新，无需重启
     *
     * 使用示例 - 监听配置变更：
     * @Component
     * public class ConfigListener {
     *     @NacosConfigListener(dataId = \"langchain4j-scenarios.yml\", groupId = \"DEFAULT_GROUP\")
     *     public void onConfigChange(String config) {
     *         log.info(\"配置已更新: {}\", config);
     *         // 处理配置变更
     *     }
     * }
     *
     * 使用示例 - 服务注册和发现：
     * @SpringBootApplication
     * @EnableDiscoveryClient
     * public class Application {
     *     public static void main(String[] args) {
     *         SpringApplication.run(Application.class, args);
     *     }
     * }
     *
     * // 应用启动时自动注册到 Nacos
     * // 其他应用可以通过服务名发现此应用
     *
     * Nacos 命名空间和分组：
     * 1. 命名空间（Namespace）
     *    - 用于环境隔离
     *    - 常见值：dev, test, staging, production
     *    - 不同命名空间的配置完全隔离
     *    - 示例：
     *      * dev 命名空间：开发环境配置
     *      * production 命名空间：生产环境配置
     *
     * 2. 分组（Group）
     *    - 用于逻辑分组
     *    - 常见值：DEFAULT_GROUP, service-a, service-b
     *    - 同一命名空间内可以有多个分组
     *    - 示例：
     *      * DEFAULT_GROUP：默认配置
     *      * chat-service：聊天服务配置
     *      * llm-service：LLM 服务配置
     *
     * 3. Data ID
     *    - 配置文件的唯一标识
     *    - 通常使用应用名称
     *    - 示例：
     *      * langchain4j-scenarios.yml
     *      * langchain4j-scenarios-dev.yml
     *
     * Nacos 配置优先级：
     * 1. 命令行参数（最高优先级）
     * 2. 系统环境变量
     * 3. Nacos 配置中心
     * 4. application.yml 配置文件
     * 5. application-{profile}.yml 配置文件（最低优先级）
     *
     * 配置最佳实践：
     * 1. 环境隔离
     *    - 为每个环境创建独立的命名空间
     *    - 避免配置混淆
     *
     * 2. 敏感信息保护
     *    - 不要在配置中存储明文密码
     *    - 使用 Nacos 的加密功能
     *    - 或者使用环境变量
     *
     * 3. 配置版本管理
     *    - 记录配置变更历史
     *    - 支持配置回滚
     *    - 审计配置变更
     *
     * 4. 监控和告警
     *    - 监控配置变更事件
     *    - 监控应用对配置的响应
     *    - 及时发现配置问题
     *
     * 5. 文档化
     *    - 文档化所有配置项
     *    - 说明配置的含义和影响
     *    - 提供配置示例
     *
     * 与 Spring Cloud 的集成：
     * - Spring Cloud Alibaba 提供 Nacos 集成
     * - 自动配置 Nacos 客户端
     * - 支持配置自动刷新
     * - 支持服务发现和负载均衡
     *
     * 与 LangChain4j 的集成：
     * - 在 Nacos 中管理 LLM 配置
     * - 动态切换 LLM 提供商
     * - 动态调整 LLM 参数（温度、topP 等）
     * - 动态管理 API 密钥
     *
     * 常见问题：
     * 1. 配置无法加载
     *    - 检查 Nacos 服务器是否运行
     *    - 检查服务器地址和端口
     *    - 检查命名空间和分组是否正确
     *
     * 2. 配置变更不生效
     *    - 检查是否使用了 @Value 注解
     *    - 检查是否使用了 @RefreshScope 注解
     *    - 检查是否监听了配置变更事件
     *
     * 3. 性能问题
     *    - Nacos 客户端会定期拉取配置
     *    - 可能增加网络流量
     *    - 可以调整拉取间隔
     */
    public NacosConfig() {
        log.info("Nacos 配置中心已启用");
    }
}
