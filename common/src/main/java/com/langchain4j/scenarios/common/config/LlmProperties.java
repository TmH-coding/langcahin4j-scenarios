package com.langchain4j.scenarios.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * LLM 配置属性类 - 从 application.properties 读取 LLM 配置
 *
 * 职责说明：
 * - 读取和管理 LLM 相关的配置属性
 * - 支持从 application.properties 或 application.yml 读取配置
 * - 提供默认值确保应用可以正常运行
 * - 支持多环境配置（开发、测试、生产）
 *
 * 架构设计：
 * - 使用 @ConfigurationProperties 注解绑定配置属性
 * - 使用 @Component 注册为 Spring Bean
 * - 使用 @Data 自动生成 getter/setter
 * - 前缀为 "llm"，对应配置文件中的 llm.* 属性
 *
 * 配置示例（application.properties）：
 * llm.model=gpt-4-turbo-preview
 * llm.temperature=0.7
 * llm.max-tokens=2048
 * llm.timeout=60
 *
 * 配置示例（application.yml）：
 * llm:
 *   model: gpt-4-turbo-preview
 *   temperature: 0.7
 *   max-tokens: 2048
 *   timeout: 60
 *
 * 使用场景：
 * - 在 LlmConfig 中注入此类获取配置
 * - 在 Service 中注入此类获取 LLM 参数
 * - 支持运行时动态调整 LLM 参数
 * - 支持不同环境的不同配置
 *
 * 性能考虑：
 * - 配置在应用启动时加载一次
 * - 访问速度快，无性能开销
 * - 支持配置热更新（需要额外配置）
 *
 * 扩展建议：
 * - 可以添加更多 LLM 参数（如 top_p, frequency_penalty 等）
 * - 可以支持多个 LLM 提供商的配置
 * - 可以实现配置的动态验证
 * - 可以添加配置的加密存储
 */
@Component
@ConfigurationProperties(prefix = "llm")
@Data
public class LlmProperties {

    /**
     * LLM 模型名称
     * 默认值：gpt-4-turbo-preview
     * 可选值：gpt-4, gpt-3.5-turbo, gpt-4-turbo-preview 等
     */
    private String model = "gpt-4-turbo-preview";

    /**
     * 温度参数（0-1）
     * 默认值：0.7
     * 0.0：完全确定性，适合事实性任务
     * 0.5-0.7：平衡创意和确定性（推荐）
     * 0.8-1.0：最大随机性，适合创意任务
     */
    private Double temperature = 0.7;

    /**
     * 最大 Token 数
     * 默认值：2048
     * 限制生成的最大 Token 数
     * 根据模型的上下文窗口调整
     */
    private Integer maxTokens = 2048;

    /**
     * 请求超时时间（秒）
     * 默认值：60
     * LLM 调用的最大等待时间
     * 超过此时间将抛出超时异常
     */
    private Integer timeout = 60;
}
