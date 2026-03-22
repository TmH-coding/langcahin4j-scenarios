package com.langchain4j.scenarios.common.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LLM 配置类 - 大语言模型集成和初始化
 *
 * 职责说明：
 * - 配置和初始化大语言模型（LLM）Bean
 * - 支持多个 LLM 提供商的切换（通过配置属性）
 * - 当前实现支持 OpenAI API
 * - 提供统一的 ChatLanguageModel 接口
 *
 * 架构设计：
 * - 使用 @Configuration 标记为 Spring 配置类
 * - 使用 @ConditionalOnProperty 实现条件化 Bean 创建
 * - 支持通过环境变量配置 API 密钥
 * - 支持通过配置文件切换 LLM 提供商
 *
 * 使用场景：
 * - 所有场景模块都依赖此配置来获取 ChatLanguageModel Bean
 * - 通过依赖注入获取 LLM 实例
 * - 支持在不修改代码的情况下切换 LLM 提供商
 * - 支持多环境配置（开发、测试、生产）
 *
 * 配置方式：
 * 1. 环境变量：export OPENAI_API_KEY=sk-xxx
 * 2. 配置文件：application.yml 中设置 llm.provider=openai
 * 3. 系统属性：-Dllm.provider=openai
 *
 * 支持的 LLM 提供商：
 * - OpenAI：gpt-4, gpt-3.5-turbo 等
 * - 可扩展支持其他提供商（如 Claude、Gemini 等）
 *
 * 安全考虑：
 * - API 密钥应该通过环境变量传递，不要硬编码
 * - 不要在日志中打印 API 密钥
 * - 定期轮换 API 密钥
 * - 使用 API 密钥管理服务（如 AWS Secrets Manager）
 */
@Configuration
public class LlmConfig {

    /**
     * 创建 OpenAI 聊天模型 Bean
     *
     * 功能：
     * - 初始化 OpenAI ChatLanguageModel
     * - 配置模型参数（温度、topP 等）
     * - 支持条件化创建（当 llm.provider=openai 时）
     * - 提供统一的 ChatLanguageModel 接口供其他组件使用
     *
     * 参数说明：
     * - apiKey: 从环境变量 OPENAI_API_KEY 读取
     *   * 获取方式：System.getenv("OPENAI_API_KEY")
     *   * 必须设置，否则会抛出异常
     *   * 格式：sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
     *   * 获取方式：https://platform.openai.com/api-keys
     *   * 安全性：不要在代码中硬编码，使用环境变量
     *
     * - modelName: 使用的模型名称
     *   * gpt-4-turbo-preview: 最强大的模型，支持 128K tokens
     *     - 优点：最强大的推理能力，支持长上下文
     *     - 缺点：成本最高，响应速度较慢
     *     - 适用场景：复杂推理、长文档分析
     *
     *   * gpt-4: 标准 GPT-4 模型
     *     - 优点：强大的推理能力，成本中等
     *     - 缺点：上下文窗口较小（8K tokens）
     *     - 适用场景：一般推理任务
     *
     *   * gpt-3.5-turbo: 快速且经济的模型
     *     - 优点：快速响应，成本低
     *     - 缺点：推理能力较弱
     *     - 适用场景：简单任务、客服系统
     *
     *   * 选择建议：根据场景选择
     *     - 客服系统：gpt-3.5-turbo（快速、经济）
     *     - 文档分析：gpt-4-turbo-preview（长上下文）
     *     - 代码生成：gpt-4（推理能力强）
     *     - 内容创作：gpt-4（创意能力强）
     *
     * - temperature: 控制输出的随机性（0-1）
     *   * 0.0: 完全确定性，适合事实性任务
     *     - 特点：输出完全确定，每次相同
     *     - 适用场景：数据提取、事实查询、代码生成
     *     - 示例：提取文本中的日期、名称等
     *
     *   * 0.5-0.7: 平衡创意和确定性（推荐）
     *     - 特点：既有创意又相对稳定
     *     - 适用场景：大多数应用场景
     *     - 示例：客服回复、文本总结
     *
     *   * 0.8-1.0: 最大随机性，适合创意任务
     *     - 特点：输出多样化，每次不同
     *     - 适用场景：创意写作、头脑风暴
     *     - 示例：故事创作、广告文案
     *
     *   * 应用场景建议：
     *     - 客服系统：0.5-0.7（保持一致性）
     *     - 内容创作：0.8-1.0（增加多样性）
     *     - 代码生成：0.2-0.5（保持准确性）
     *     - 数据分析：0.0-0.3（保持确定性）
     *
     * - topP: 核采样参数（0-1）
     *   * 1.0: 不限制，使用所有 token
     *     - 特点：考虑所有可能的 token
     *     - 适用场景：需要多样性的场景
     *
     *   * 0.9: 只考虑累积概率为 0.9 的 token
     *     - 特点：排除低概率的 token，减少不合理输出
     *     - 适用场景：大多数应用场景
     *
     *   * 0.5: 只考虑累积概率为 0.5 的 token
     *     - 特点：只考虑最可能的 token，输出更确定
     *     - 适用场景：需要确定性的场景
     *
     *   * 与 temperature 配合使用：
     *     - 通常设置为 1.0（不限制）
     *     - 由 temperature 控制随机性
     *     - 可以同时使用以获得更精细的控制
     *
     * 条件化创建：
     * - @ConditionalOnProperty 注解确保只在满足条件时创建 Bean
     * - name: 配置属性名称（llm.provider）
     * - havingValue: 期望的属性值（openai）
     * - matchIfMissing: 属性缺失时是否创建（true = 默认创建）
     * - 作用：支持多个 LLM 提供商的条件化配置
     *
     * 使用示例 - 基础使用：
     * @Autowired
     * private ChatLanguageModel chatModel;
     *
     * public String chat(String message) {
     *     return chatModel.generate(message);
     * }
     *
     * 使用示例 - 在服务中使用：
     * @Service
     * public class ChatService {
     *     @Autowired
     *     private ChatLanguageModel chatModel;
     *
     *     public String generateResponse(String userMessage) {
     *         return chatModel.generate(userMessage);
     *     }
     * }
     *
     * 使用示例 - 带系统提示词：
     * @Service
     * public class CustomerServiceBot {
     *     @Autowired
     *     private ChatLanguageModel chatModel;
     *
     *     public String handleCustomerQuery(String query) {
     *         String systemPrompt = "You are a helpful customer service representative.";
     *         String response = chatModel.generate(systemPrompt + "\n\nCustomer: " + query);
     *         return response;
     *     }
     * }
     *
     * 环境配置示例：
     * # 开发环境
     * export OPENAI_API_KEY=sk-dev-xxx
     * export LLM_PROVIDER=openai
     *
     * # 生产环境
     * export OPENAI_API_KEY=sk-prod-xxx
     * export LLM_PROVIDER=openai
     *
     * 配置文件示例（application.yml）：
     * llm:
     *   provider: openai
     *   model: gpt-4-turbo-preview
     *   temperature: 0.7
     *   topP: 1.0
     *
     * 成本考虑：
     * - gpt-4-turbo-preview：最贵
     * - gpt-4：中等价格
     * - gpt-3.5-turbo：最便宜
     * - 根据应用场景选择合适的模型以控制成本
     *
     * 性能考虑：
     * - gpt-3.5-turbo：最快
     * - gpt-4：中等速度
     * - gpt-4-turbo-preview：较慢
     * - 根据应用需求选择合适的模型以平衡性能和质量
     *
     * 扩展建议：
     * - 可以添加其他 LLM 提供商的配置（Claude、Gemini 等）
     * - 可以实现 LLM 提供商的动态切换
     * - 可以添加 LLM 调用的缓存和重试机制
     * - 可以实现 LLM 调用的监控和日志记录
     *
     * 常见问题：
     * 1. API 密钥无效
     *    - 检查环境变量是否正确设置
     *    - 检查 API 密钥是否过期
     *    - 检查 API 密钥是否有权限
     *
     * 2. 模型不可用
     *    - 检查模型名称是否正确
     *    - 检查 API 账户是否有权限使用该模型
     *    - 检查模型是否已停用
     *
     * 3. 响应速度慢
     *    - 考虑使用更快的模型（gpt-3.5-turbo）
     *    - 检查网络连接
     *    - 检查 OpenAI 服务状态
     *
     * @return ChatLanguageModel - OpenAI 聊天模型实例
     */
    @Bean
    @ConditionalOnProperty(name = "llm.provider", havingValue = "openai", matchIfMissing = true)
    public ChatLanguageModel openAiChatModel() {
        return OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))  // 从环境变量读取 API 密钥
                .modelName("gpt-4-turbo-preview")          // 指定使用的模型
                .temperature(0.7)                          // 设置温度参数，控制输出多样性
                .topP(1.0)                                 // 设置核采样参数
                .build();
    }

    /**
     * 创建 OpenAI 流式聊天模型 Bean
     *
     * 功能：
     * - 初始化 OpenAI StreamingChatLanguageModel
     * - 支持流式响应（Server-Sent Events）
     * - 用于实时流式输出场景
     * - 支持条件化创建
     *
     * 使用场景：
     * - 实时流式输出 LLM 响应
     * - 改善用户体验，减少等待时间
     * - 支持 SSE（Server-Sent Events）
     * - 适合长文本生成场景
     *
     * 使用示例：
     * @Autowired
     * private StreamingChatLanguageModel streamingChatModel;
     *
     * public void streamChat(String message) {
     *     streamingChatModel.generate(
     *         List.of(UserMessage.from(message)),
     *         new StreamingResponseHandler<AiMessage>() {
     *             @Override
     *             public void onNext(String token) {
     *                 System.out.print(token);
     *             }
     *             @Override
     *             public void onComplete(Response<AiMessage> response) {
     *                 System.out.println("\n[完成]");
     *             }
     *         }
     *     );
     * }
     *
     * @return StreamingChatLanguageModel - 流式聊天模型实例
     */
    @Bean
    @ConditionalOnProperty(name = "llm.provider", havingValue = "openai", matchIfMissing = true)
    public StreamingChatLanguageModel openAiStreamingChatModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName("gpt-4-turbo-preview")
                .temperature(0.7)
                .topP(1.0)
                .build();
    }
}
