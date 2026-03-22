package com.langchain4j.scenarios.scenario4.service;

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
 * 数据分析服务 - 数据分析和SQL生成的核心引擎
 *
 * 职责说明：
 * - 提供数据分析、SQL生成和查询优化的核心逻辑
 * - 支持自动生成SQL查询语句
 * - 提供数据统计分析和洞察
 * - 生成各类数据报告
 * - 提供查询性能优化建议
 *
 * 架构设计：
 * - 使用 @Service 注解注册为 Spring Bean
 * - 使用 @RequiredArgsConstructor 自动注入依赖
 * - 依赖 ChatLanguageModel 进行LLM调用
 * - 提供多个独立的数据分析方法
 * - 支持不同类型的数据操作
 *
 * 使用场景：
 * - 自动生成SQL查询语句
 * - 分析数据集并提供洞察
 * - 生成数据报告和统计
 * - 优化数据库查询性能
 * - 电商数据分析平台
 * - 业务智能和数据仓库
 *
 * 核心特性：
 * - SQL生成：根据需求自动生成SQL
 * - 数据分析：统计和趋势分析
 * - 性能优化：提供查询优化建议
 * - 查询解释：说明SQL执行逻辑
 * - 报告生成：生成各类数据报告
 *
 * 工作原理：
 * 1. 接收数据和分析需求
 * 2. 根据请求类型调用相应的分析方法
 * 3. 使用LLM进行数据分析（当前为mock实现）
 * 4. 返回分析结果和建议
 * 5. 支持多种分析类型的组合
 *
 * 字段说明：
 * - chatModel: LLM语言模型
 *   * 类型：ChatLanguageModel
 *   * 用途：生成SQL和分析结果
 *   * 来源：Spring 依赖注入
 *
 * 性能考虑：
 * - 数据分析时间取决于数据大小和复杂度
 * - LLM调用可能需要1-5秒
 * - 建议使用异步处理提高响应速度
 * - 可以配置分析参数优化性能
 * - 监控LLM调用的延迟
 *
 * 安全考虑：
 * - 验证SQL内容防止注入攻击
 * - 限制查询长度防止内存溢出
 * - 不要在分析结果中暴露敏感信息
 * - 实现请求速率限制
 * - 记录所有分析用于审计
 * - 实现查询内容的加密存储
 * - 防止查询被篡改
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
 * - 可以支持更多数据源
 * - 可以实现自定义分析规则
 * - 可以支持数据分析规则定制
 * - 可以添加分析结果评分
 * - 可以实现分析报告导出
 * - 可以添加分析趋势分析
 * - 可以实现分析结果缓存
 * - 可以支持批量数据分析
 */
@Service
@RequiredArgsConstructor
public class DataAnalystService {

    /** 注入的LLM模型 */
    private final ChatLanguageModel chatModel;

    /**
     * 生成SQL查询语句
     *
     * 功能：
     * - 根据用户需求生成SQL语句
     * - 支持复杂查询
     * - 考虑数据库架构
     *
     * 参数说明：
     * - requirement: 用户的查询需求描述
     * - schema: 数据库表结构信息
     *
     * @param requirement 查询需求
     * @param schema 数据库架构
     * @return 生成的SQL语句
     */
    public String generateSQL(String requirement, String schema) {
        String systemPrompt = PromptTemplateUtil.getTemplate("data_analyst_system");
        String userMessage = String.format(
            "根据以下数据库架构生成 SQL 查询语句：\n\n架构：%s\n\n需求：%s",
            schema, requirement
        );

        List<ChatMessage> messages = PromptBuilder.buildMessages(systemPrompt, userMessage);
        return chatModel.generate(messages).content().text();
    }

    /**
     * 分析数据
     *
     * 功能：
     * - 对数据进行统计分析
     * - 回答关于数据的问题
     * - 提供数据洞察
     *
     * @param data 要分析的数据
     * @param question 用户的问题
     * @return 分析结果
     */
    public String analyzeData(String data, String question) {
        String systemPrompt = PromptTemplateUtil.getTemplate("data_analyst_system");
        String userMessage = String.format(
            "请分析以下数据并回答问题：\n\n数据：%s\n\n问题：%s",
            data, question
        );

        List<ChatMessage> messages = PromptBuilder.buildMessages(systemPrompt, userMessage);
        return chatModel.generate(messages).content().text();
    }

    /**
     * 生成报告
     *
     * 功能：
     * - 根据数据生成各类报告
     * - 支持多种报告类型
     * - 包含统计数据和趋势分析
     *
     * 参数说明：
     * - data: 报告数据源
     * - reportType: 报告类型（如 "summary", "detailed", "trend"）
     *
     * @param data 数据源
     * @param reportType 报告类型
     * @return 生成的报告
     */
    public String generateReport(String data, String reportType) {
        String systemPrompt = PromptTemplateUtil.getTemplate("data_analyst_system");
        String userMessage = String.format(
            "请根据以下数据生成 %s 类型的报告：\n\n数据：%s",
            reportType, data
        );

        List<ChatMessage> messages = PromptBuilder.buildMessages(systemPrompt, userMessage);
        return chatModel.generate(messages).content().text();
    }

    /**
     * 优化查询建议
     *
     * 功能：
     * - 分析SQL查询性能
     * - 提供优化建议
     * - 改进查询效率
     *
     * @param query SQL查询语句
     * @return 优化建议
     */
    public String suggestOptimization(String query) {
        String systemPrompt = PromptTemplateUtil.getTemplate("data_analyst_system");
        String userMessage = String.format(
            "请分析以下 SQL 查询并提供性能优化建议：\n\n```sql\n%s\n```",
            query
        );

        List<ChatMessage> messages = PromptBuilder.buildMessages(systemPrompt, userMessage);
        return chatModel.generate(messages).content().text();
    }

    /**
     * 解释查询
     *
     * 功能：
     * - 解释SQL查询的执行逻辑
     * - 说明查询步骤
     * - 帮助理解复杂查询
     *
     * @param query SQL查询语句
     * @return 查询解释
     */
    public String explainQuery(String query) {
        String systemPrompt = PromptTemplateUtil.getTemplate("data_analyst_system");
        String userMessage = String.format(
            "请解释以下 SQL 查询的执行逻辑和步骤：\n\n```sql\n%s\n```",
            query
        );

        List<ChatMessage> messages = PromptBuilder.buildMessages(systemPrompt, userMessage);
        return chatModel.generate(messages).content().text();
    }
}
