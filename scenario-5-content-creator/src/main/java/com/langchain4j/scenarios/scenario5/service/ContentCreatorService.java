package com.langchain4j.scenarios.scenario5.service;

import com.langchain4j.scenarios.common.util.PromptBuilder;
import com.langchain4j.scenarios.common.util.PromptTemplateUtil;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 内容创作服务 - 内容生成和优化的核心引擎
 *
 * 职责说明：
 * - 提供内容生成、优化和翻译的核心逻辑
 * - 支持多种内容类型和写作风格
 * - 提供SEO优化和多语言翻译功能
 * - 生成社交媒体内容和文章大纲
 * - 提供写作改进和质量建议
 *
 * 架构设计：
 * - 使用 @Service 注解注册为 Spring Bean
 * - 使用 @RequiredArgsConstructor 自动注入依赖
 * - 依赖 ChatLanguageModel 进行LLM调用
 * - 提供多个独立的内容生成方法
 * - 支持不同类型的内容操作
 *
 * 使用场景：
 * - 自动生成文章和博客内容
 * - SEO优化和关键词管理
 * - 多语言内容翻译和本地化
 * - 社交媒体内容生成和批量发布
 * - 写作改进和质量检查
 * - 内容营销平台
 * - 自媒体运营工具
 * - 国际化内容管理
 *
 * 核心特性：
 * - 内容生成：支持多种风格（professional、casual、academic）和长度
 * - SEO优化：自动优化关键词、标题和描述
 * - 多语言支持：翻译成多种语言（English、Chinese、Spanish等）
 * - 社交媒体：生成适配各平台的内容和标签
 * - 写作改进：提供语法、风格和清晰度建议
 * - 大纲生成：为文章生成结构化大纲
 *
 * 工作原理：
 * 1. 接收内容生成请求和参数
 * 2. 根据请求类型调用相应的生成方法
 * 3. 使用LLM进行内容生成（当前为mock实现）
 * 4. 返回生成的内容和建议
 * 5. 支持多种生成类型的组合
 *
 * 字段说明：
 * - chatModel: LLM语言模型
 *   * 类型：ChatLanguageModel
 *   * 用途：生成内容和提供建议
 *   * 来源：Spring 依赖注入
 *
 * 性能考虑：
 * - 内容生成时间取决于内容长度和复杂度
 * - LLM调用可能需要1-5秒
 * - 建议使用异步处理提高响应速度
 * - 可以配置生成参数优化性能
 * - 监控LLM调用的延迟
 * - 支持批量内容生成
 * - 可以缓存常用的生成模板
 *
 * 安全考虑：
 * - 验证生成内容防止注入攻击
 * - 限制生成内容长度防止内存溢出
 * - 不要在生成结果中暴露敏感信息
 * - 实现请求速率限制
 * - 记录所有生成用于审计
 * - 实现生成内容的加密存储
 * - 防止生成内容被篡改
 * - 验证用户权限和配额
 *
 * 可靠性考虑：
 * - 处理LLM调用异常
 * - 实现请求超时控制
 * - 支持请求重试机制
 * - 记录详细的错误日志
 * - 实现优雅的降级处理
 * - 支持生成的恢复机制
 * - 处理并发生成请求
 *
 * 扩展建议：
 * - 可以支持更多内容类型
 * - 可以实现自定义生成规则
 * - 可以支持内容生成规则定制
 * - 可以添加生成结果评分
 * - 可以实现生成报告导出
 * - 可以添加生成趋势分析
 * - 可以实现生成结果缓存
 * - 可以支持批量内容生成
 * - 可以添加A/B测试功能
 * - 可以实现内容版本管理
 */
@Service
@RequiredArgsConstructor
public class ContentCreatorService {

    /** 注入的LLM模型 */
    private final ChatLanguageModel chatModel;

    /**
     * 生成文章
     *
     * 功能：
     * - 根据主题生成文章
     * - 支持不同的写作风格
     * - 控制文章长度
     *
     * 参数说明：
     * - topic: 文章主题
     * - style: 写作风格（如 "professional", "casual", "academic"）
     * - wordCount: 目标字数
     *
     * @param topic 文章主题
     * @param style 写作风格
     * @param wordCount 目标字数
     * @return 生成的文章
     */
    public String generateArticle(String topic, String style, int wordCount) {
        String systemPrompt = PromptTemplateUtil.getTemplate("content_creator_system");
        String userMessage = String.format(
            "请用 %s 风格生成一篇关于 '%s' 的文章，目标字数约 %d 字。",
            style, topic, wordCount
        );

        List<ChatMessage> messages = PromptBuilder.buildMessages(systemPrompt, userMessage);
        return chatModel.generate(messages).content().text();
    }

    /**
     * SEO优化
     *
     * 功能：
     * - 优化内容以提高搜索引擎排名
     * - 集成关键词
     * - 优化标题和描述
     *
     * @param content 要优化的内容
     * @param keywords 目标关键词（逗号分隔）
     * @return 优化建议
     */
    public String optimizeForSEO(String content, String keywords) {
        String systemPrompt = PromptTemplateUtil.getTemplate("content_creator_system");
        String userMessage = String.format(
            "请为以下内容进行 SEO 优化，目标关键词：%s\n\n内容：%s",
            keywords, content
        );

        List<ChatMessage> messages = PromptBuilder.buildMessages(systemPrompt, userMessage);
        return chatModel.generate(messages).content().text();
    }

    /**
     * 翻译内容
     *
     * 功能：
     * - 将内容翻译成目标语言
     * - 保持原意和风格
     * - 支持多种语言
     *
     * @param content 要翻译的内容
     * @param targetLanguage 目标语言（如 "English", "Spanish", "Chinese"）
     * @return 翻译后的内容
     */
    public String translateContent(String content, String targetLanguage) {
        String systemPrompt = PromptTemplateUtil.getTemplate("content_creator_system");
        String userMessage = String.format(
            "请将以下内容翻译成 %s，保持原意和风格：\n\n%s",
            targetLanguage, content
        );

        List<ChatMessage> messages = PromptBuilder.buildMessages(systemPrompt, userMessage);
        return chatModel.generate(messages).content().text();
    }

    /**
     * 生成社交媒体帖子
     *
     * 功能：
     * - 为社交媒体生成内容
     * - 支持批量生成
     * - 包含相关的标签
     *
     * 参数说明：
     * - topic: 帖子主题
     * - postCount: 要生成的帖子数量
     *
     * @param topic 帖子主题
     * @param postCount 帖子数量
     * @return 生成的社交媒体帖子
     */
    public String generateSocialMediaPosts(String topic, int postCount) {
        String systemPrompt = PromptTemplateUtil.getTemplate("content_creator_system");
        String userMessage = String.format(
            "请为主题 '%s' 生成 %d 条社交媒体帖子，每条帖子包含相关的标签和表情符号。",
            topic, postCount
        );

        List<ChatMessage> messages = PromptBuilder.buildMessages(systemPrompt, userMessage);
        return chatModel.generate(messages).content().text();
    }

    /**
     * 改进写作
     *
     * 功能：
     * - 分析文本质量
     * - 提供改进建议
     * - 改进语法和风格
     *
     * @param content 要改进的内容
     * @return 改进建议
     */
    public String improveWriting(String content) {
        String systemPrompt = PromptTemplateUtil.getTemplate("content_creator_system");
        String userMessage = String.format(
            "请分析以下内容的质量，并提供改进建议（包括语法、风格、清晰度等）：\n\n%s",
            content
        );

        List<ChatMessage> messages = PromptBuilder.buildMessages(systemPrompt, userMessage);
        return chatModel.generate(messages).content().text();
    }

    /**
     * 生成大纲
     *
     * 功能：
     * - 为文章生成结构化大纲
     * - 支持自定义章节数
     * - 帮助组织内容
     *
     * 参数说明：
     * - topic: 文章主题
     * - sections: 章节数量
     *
     * @param topic 文章主题
     * @param sections 章节数量
     * @return 文章大纲
     */
    public String generateOutline(String topic, int sections) {
        String systemPrompt = PromptTemplateUtil.getTemplate("content_creator_system");
        String userMessage = String.format(
            "请为主题 '%s' 生成一个包含 %d 个章节的结构化文章大纲，每个章节包含标题和简要描述。",
            topic, sections
        );

        List<ChatMessage> messages = PromptBuilder.buildMessages(systemPrompt, userMessage);
        return chatModel.generate(messages).content().text();
    }
}
