package com.langchain4j.scenarios.scenario3.service;

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
 * 代码助手服务 - 代码审查和分析的核心引擎
 *
 * 职责说明：
 * - 提供代码审查、分析和优化建议的核心逻辑
 * - 支持多种编程语言的代码分析
 * - 提供代码文档生成和重构建议
 * - 检测代码中的潜在问题和Bug
 * - 支持代码解释和优化建议
 *
 * 架构设计：
 * - 使用 @Service 注解注册为 Spring Bean
 * - 使用 @RequiredArgsConstructor 自动注入依赖
 * - 依赖 ChatLanguageModel 进行LLM调用
 * - 提供多个独立的代码分析方法
 * - 支持不同类型的代码审查
 *
 * 使用场景：
 * - 代码质量审查和规范检查
 * - Bug检测和修复建议
 * - 代码文档自动生成
 * - 代码重构指导和优化建议
 * - 安全漏洞检测
 * - 性能问题分析
 *
 * 核心特性：
 * - 多语言支持：Java、Python、JavaScript等
 * - 智能分析：检测常见的代码问题
 * - 最佳实践：提供行业标准的改进建议
 * - 代码解释：帮助理解复杂代码
 * - 文档生成：自动生成代码注释和文档
 *
 * 工作原理：
 * 1. 接收代码和编程语言参数
 * 2. 根据请求类型调用相应的分析方法
 * 3. 使用LLM进行代码分析
 * 4. 返回分析结果和建议
 * 5. 支持多种分析类型的组合
 *
 * 字段说明：
 * - chatModel: LLM语言模型
 *   * 类型：ChatLanguageModel
 *   * 用途：生成代码审查和分析结果
 *   * 来源：Spring 依赖注入
 *
 * 性能考虑：
 * - 代码分析时间取决于代码大小和复杂度
 * - LLM调用可能需要1-5秒
 * - 建议使用异步处理提高响应速度
 * - 可以配置分析参数优化性能
 * - 监控LLM调用的延迟
 *
 * 安全考虑：
 * - 验证代码内容防止注入攻击
 * - 限制代码长度防止内存溢出
 * - 不要在分析结果中暴露敏感信息
 * - 实现请求速率限制
 * - 记录所有分析用于审计
 * - 实现代码内容的加密存储
 * - 防止代码被篡改
 *
 * 可靠性考虑：
 * - 处理LLM调用异常
 * - 实现请求超时控制
 * - 支持请求重试机制
 * - 记录详细的错误日志
 * - 实现优雅的降级处理
 * - 支持分析的恢复机制
 *
 * 扩展建议：
 * - 可以添加更多编程语言支持
 * - 可以实现自定义审查规则
 * - 可以支持代码审查规则定制
 * - 可以添加代码审查结果评分
 * - 可以实现代码审查报告导出
 * - 可以添加代码审查趋势分析
 * - 可以实现代码审查结果缓存
 * - 可以支持批量代码审查
 */
@Service
@RequiredArgsConstructor
public class CodeAssistantService {

    /** 注入的LLM模型 */
    private final ChatLanguageModel chatModel;

    /**
     * 代码审查
     *
     * 功能：
     * - 分析代码质量
     * - 检测潜在问题
     * - 提供改进建议
     *
     * @param code 要审查的代码
     * @param language 编程语言（如 "java", "python", "javascript"）
     * @return 审查结果和建议
     */
    public String reviewCode(String code, String language) {
        String systemPrompt = PromptTemplateUtil.getTemplate("code_assistant_system");
        String userMessage = String.format(
            "请审查以下 %s 代码，并提供改进建议：\n\n```%s\n%s\n```",
            language, language, code
        );

        List<ChatMessage> messages = PromptBuilder.buildMessages(systemPrompt, userMessage);
        return chatModel.generate(messages).content().text();
    }

    /**
     * 代码解释
     *
     * 功能：
     * - 解释代码的功能和逻辑
     * - 说明关键算法
     * - 帮助理解复杂代码
     *
     * @param code 要解释的代码
     * @param language 编程语言
     * @return 代码解释
     */
    public String explainCode(String code, String language) {
        String systemPrompt = PromptTemplateUtil.getTemplate("code_assistant_system");
        String userMessage = String.format(
            "请解释以下 %s 代码的功能和逻辑：\n\n```%s\n%s\n```",
            language, language, code
        );

        List<ChatMessage> messages = PromptBuilder.buildMessages(systemPrompt, userMessage);
        return chatModel.generate(messages).content().text();
    }

    /**
     * 生成代码文档
     *
     * 功能：
     * - 自动生成代码注释和文档
     * - 生成函数签名说明
     * - 生成参数和返回值说明
     *
     * @param code 要生成文档的代码
     * @param language 编程语言
     * @return 生成的文档
     */
    public String generateDocumentation(String code, String language) {
        String systemPrompt = PromptTemplateUtil.getTemplate("code_assistant_system");
        String userMessage = String.format(
            "请为以下 %s 代码生成详细的文档注释，包括函数说明、参数说明和返回值说明：\n\n```%s\n%s\n```",
            language, language, code
        );

        List<ChatMessage> messages = PromptBuilder.buildMessages(systemPrompt, userMessage);
        return chatModel.generate(messages).content().text();
    }

    /**
     * 重构建议
     *
     * 功能：
     * - 提供代码重构方案
     * - 改进代码结构
     * - 提高代码可维护性
     *
     * @param code 要重构的代码
     * @param language 编程语言
     * @return 重构建议
     */
    public String suggestRefactoring(String code, String language) {
        String systemPrompt = PromptTemplateUtil.getTemplate("code_assistant_system");
        String userMessage = String.format(
            "请为以下 %s 代码提供重构建议，改进代码结构和可维护性：\n\n```%s\n%s\n```",
            language, language, code
        );

        List<ChatMessage> messages = PromptBuilder.buildMessages(systemPrompt, userMessage);
        return chatModel.generate(messages).content().text();
    }

    /**
     * 检测Bug
     *
     * 功能：
     * - 检测代码中的潜在Bug
     * - 评估问题严重程度
     * - 提供修复建议
     *
     * @param code 要检测的代码
     * @param language 编程语言
     * @return Bug检测结果
     */
    public String detectBugs(String code, String language) {
        String systemPrompt = PromptTemplateUtil.getTemplate("code_assistant_system");
        String userMessage = String.format(
            "请检测以下 %s 代码中的潜在 Bug，并评估严重程度，提供修复建议：\n\n```%s\n%s\n```",
            language, language, code
        );

        List<ChatMessage> messages = PromptBuilder.buildMessages(systemPrompt, userMessage);
        return chatModel.generate(messages).content().text();
    }
}

