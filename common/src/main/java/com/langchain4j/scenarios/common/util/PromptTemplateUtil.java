package com.langchain4j.scenarios.common.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 提示词模板工具类 - 系统提示词的集中管理和动态生成
 *
 * 职责说明：
 * - 管理所有场景的系统提示词（System Prompt）
 * - 提供提示词模板的获取和格式化功能
 * - 支持变量替换，实现动态提示词生成
 * - 确保 LLM 在不同场景中的行为一致
 * - 支持提示词的集中管理和版本控制
 * - 实现提示词的复用和优化
 *
 * 架构设计：
 * - 使用静态 Map 存储所有提示词模板
 * - 使用静态初始化块定义模板
 * - 提供静态方法访问模板
 * - 支持模板变量替换
 * - 易于扩展和维护
 *
 * 使用场景：
 * - 为每个场景定义专业的系统角色提示
 * - 确保 LLM 在不同场景中的行为一致
 * - 支持提示词的集中管理和版本控制
 * - 实现提示词的动态生成和个性化
 * - 支持多语言提示词
 * - 实现 A/B 测试不同的提示词
 *
 * 提示词工程最佳实践：
 * - 系统提示词应该清晰定义 AI 的角色和行为
 * - 提示词应该包含具体的指导和约束
 * - 定期优化提示词以提高输出质量
 * - 使用清晰的语言和具体的例子
 * - 避免模糊和歧义的表述
 * - 包含安全约束和伦理指导
 *
 * 性能考虑：
 * - 模板存储在内存中，访问速度快
 * - 静态初始化只执行一次
 * - 变量替换使用字符串替换，性能良好
 * - 建议缓存格式化后的提示词
 *
 * 扩展建议：
 * - 可以从数据库加载提示词
 * - 可以实现提示词版本管理
 * - 可以添加提示词评分和反馈
 * - 可以实现提示词的 A/B 测试
 * - 可以添加多语言支持
 * - 可以实现提示词的热更新
 */
public class PromptTemplateUtil {

    /** 提示词模板存储 - 使用 Map 存储所有场景的系统提示词 */
    private static final Map<String, String> TEMPLATES = new HashMap<>();

    /**
     * 静态初始化块 - 定义所有场景的系统提示词
     *
     * 说明：
     * - 在类加载时执行一次
     * - 定义所有场景的系统提示词
     * - 每个场景有独立的提示词模板
     * - 提示词定义了 AI 的角色、行为和约束
     *
     * 提示词设计原则：
     * - 清晰定义 AI 的角色和身份
     * - 指定 AI 应该做什么和不应该做什么
     * - 提供具体的行为指导
     * - 包含安全约束和伦理指导
     * - 使用清晰的语言和具体的例子
     */
    static {
        // ========== 场景1：客服系统 ==========
        // 定义客服 AI 的角色和行为规范
        // 角色：专业的客服代表
        // 行为：礼貌、专业、有帮助
        // 约束：不知道的问题建议联系支持
        TEMPLATES.put("customer_service_system",
            "你是一位专业的客服代表。你的职责是：\n" +
            "1. 以礼貌、专业的态度回答客户问题\n" +
            "2. 提供准确、有帮助的信息\n" +
            "3. 如果不知道答案，建议客户联系技术支持\n" +
            "4. 保持友好、耐心的沟通风格\n" +
            "5. 遵守公司政策和服务标准\n" +
            "回复格式：简洁明了，避免冗长的解释。");

        // ========== 场景2：文档分析 ==========
        // 定义文档分析 AI 的角色和行为规范
        // 角色：专业的文档分析师
        // 行为：仔细分析、准确总结、引用相关部分
        // 约束：必须引用文档中的相关部分
        TEMPLATES.put("document_analysis_system",
            "你是一位专业的文档分析师。你的职责是：\n" +
            "1. 仔细分析提供的文档内容\n" +
            "2. 提供准确的摘要和答案\n" +
            "3. 必须引用文档中的相关部分来支持你的答案\n" +
            "4. 只基于提供的文档内容回答问题\n" +
            "5. 如果文档中没有相关信息，明确说明\n" +
            "回复格式：[引用] 文档中的相关内容 [分析] 你的分析结果。");

        // ========== 场景3：代码助手 ==========
        // 定义代码审查 AI 的角色和行为规范
        // 角色：专业的软件工程师
        // 行为：审查代码、建议改进、解释复杂逻辑
        // 约束：遵循最佳实践、提供清晰解释
        TEMPLATES.put("code_assistant_system",
            "你是一位资深的软件工程师。你的职责是：\n" +
            "1. 审查代码质量、性能和安全性\n" +
            "2. 检测潜在的 Bug 和问题\n" +
            "3. 提供改进建议和最佳实践\n" +
            "4. 解释复杂的代码逻辑\n" +
            "5. 遵循行业标准和编码规范\n" +
            "审查维度：功能正确性、性能优化、代码可读性、安全性、错误处理。\n" +
            "回复格式：问题 -> 严重程度 -> 改进建议 -> 代码示例。");

        // ========== 场景4：数据分析 ==========
        // 定义数据分析 AI 的角色和行为规范
        // 角色：数据分析专家
        // 行为：帮助分析数据、生成 SQL、创建报表
        // 约束：确保准确性和清晰性
        TEMPLATES.put("data_analyst_system",
            "你是一位数据分析专家。你的职责是：\n" +
            "1. 帮助用户分析数据和生成洞察\n" +
            "2. 生成准确、高效的 SQL 查询语句\n" +
            "3. 提供数据可视化和报表建议\n" +
            "4. 解释数据分析结果\n" +
            "5. 优化查询性能和数据处理\n" +
            "SQL 生成要求：包含详细注释、考虑性能优化、避免 N+1 查询。\n" +
            "回复格式：[分析] 数据分析结果 [SQL] 生成的查询语句 [优化] 性能建议。");

        // ========== 场景5：内容创作 ==========
        // 定义内容创作 AI 的角色和行为规范
        // 角色：专业的内容创作者
        // 行为：生成高质量、有吸引力的内容
        // 约束：优化 SEO 和可读性
        TEMPLATES.put("content_creator_system",
            "你是一位专业的内容创作者。你的职责是：\n" +
            "1. 生成高质量、有吸引力的内容\n" +
            "2. 优化内容的 SEO 效果\n" +
            "3. 确保内容的可读性和易理解性\n" +
            "4. 遵循品牌风格和语调\n" +
            "5. 创建引人入胜的标题和摘要\n" +
            "内容要求：原创、有价值、符合目标受众、包含关键词、结构清晰。\n" +
            "回复格式：标题 -> 摘要 -> 正文 -> SEO 关键词 -> 调用行动。");

        // ========== 英文版本（备用）==========
        TEMPLATES.put("customer_service_system_en",
            "You are a helpful customer service representative. " +
            "Answer customer questions professionally and courteously. " +
            "If you don't know the answer, suggest contacting support.");

        TEMPLATES.put("document_analysis_system_en",
            "You are an expert document analyst. " +
            "Analyze documents carefully and provide accurate summaries and answers. " +
            "Always cite relevant sections when answering questions.");

        TEMPLATES.put("code_assistant_system_en",
            "You are an expert software engineer. " +
            "Review code for bugs, suggest improvements, and explain complex logic. " +
            "Follow best practices and provide clear explanations.");

        TEMPLATES.put("data_analyst_system_en",
            "You are a data analyst expert. " +
            "Help users analyze data, generate SQL queries, and create reports. " +
            "Ensure accuracy and clarity in all analysis.");

        TEMPLATES.put("content_creator_system_en",
            "You are a professional content creator. " +
            "Generate high-quality, engaging content. " +
            "Optimize for SEO and readability.");
    }

    /**
     * 获取指定的提示词模板
     *
     * 功能：
     * - 根据键名获取对应的提示词模板
     * - 如果模板不存在，返回空字符串
     * - 支持安全的模板访问
     *
     * 参数说明：
     * - key: 模板键名
     *   * 格式：场景名称 + _system
     *   * 示例：\\\"customer_service_system\\\"
     *   * 示例：\\\"document_analysis_system\\\"
     *   * 示例：\\\"code_assistant_system\\\"
     *   * 示例：\\\"data_analyst_system\\\"
     *   * 示例：\\\"content_creator_system\\\"
     *
     * 返回值：
     * - String：提示词模板内容
     * - 如果模板不存在，返回空字符串
     * - 返回的是原始模板，未进行变量替换
     *
     * 使用示例：
     * String template = PromptTemplateUtil.getTemplate(\\\"customer_service_system\\\");
     * System.out.println(template);
     * // 输出：You are a helpful customer service representative...
     *
     * // 检查模板是否存在
     * String template = PromptTemplateUtil.getTemplate(\\\"unknown_template\\\");
     * if (template.isEmpty()) {
     *     System.out.println(\\\"模板不存在\\\");
     * }
     *
     * 应用场景：
     * - 获取场景的系统提示词
     * - 构建 LLM 的 prompt
     * - 验证模板是否存在
     * - 实现提示词的动态加载
     *
     * 性能考虑：
     * - 时间复杂度：O(1)
     * - 非常高效的操作
     * - 模板存储在内存中
     *
     * @param key 模板键名（如 \\\"customer_service_system\\\"）
     * @return 提示词模板内容，如果不存在则返回空字符串
     */
    public static String getTemplate(String key) {
        return TEMPLATES.getOrDefault(key, "");
    }

    /**
     * 格式化提示词 - 替换模板中的变量
     *
     * 功能：
     * - 将模板中的 {key} 替换为对应的值
     * - 支持多个变量替换
     * - 实现动态提示词生成
     * - 支持提示词的个性化
     *
     * 参数说明：
     * - template: 包含 {key} 占位符的模板字符串
     *   * 格式：\\\"Review {language} code for {task}\\\"
     *   * 占位符：{key} 格式
     *   * 示例：\\\"You are a {role} expert in {domain}\\\"
     *
     * - variables: 变量映射表
     *   * 键：占位符名称（不包括花括号）
     *   * 值：替换值
     *   * 示例：{\\\"language\\\" -> \\\"Java\\\", \\\"task\\\" -> \\\"code review\\\"}
     *
     * 返回值：
     * - String：替换后的提示词字符串
     * - 所有 {key} 都被替换为对应的值
     * - 如果变量不存在，占位符保持不变
     *
     * 使用示例：
     * Map<String, String> vars = new HashMap<>();
     * vars.put(\\\"language\\\", \\\"Java\\\");
     * vars.put(\\\"task\\\", \\\"code review\\\");
     * String prompt = PromptTemplateUtil.formatPrompt(
     *     \\\"Review {language} code for {task}\\\",
     *     vars
     * );
     * // 结果：\\\"Review Java code for code review\\\"
     *
     * 高级示例：
     * Map<String, String> vars = new HashMap<>();
     * vars.put(\\\"role\\\", \\\"senior\\\");
     * vars.put(\\\"domain\\\", \\\"backend development\\\");
     * String template = \\\"You are a {role} expert in {domain}. \\\" +
     *                   \\\"Help users with {domain} questions.\\\";
     * String prompt = PromptTemplateUtil.formatPrompt(template, vars);
     * // 结果：\\\"You are a senior expert in backend development. \\\" +
     * //        \\\"Help users with backend development questions.\\\"
     *
     * 应用场景：
     * - 生成场景特定的提示词
     * - 实现提示词的个性化
     * - 支持多用户的不同提示词
     * - 实现 A/B 测试不同的提示词
     * - 支持多语言提示词
     *
     * 性能考虑：
     * - 时间复杂度：O(n*m)（n 为变量数，m 为模板长度）
     * - 对于大量变量替换，考虑使用正则表达式
     * - 建议缓存格式化后的提示词
     *
     * 最佳实践：
     * - 使用有意义的变量名
     * - 验证所有必需的变量都已提供
     * - 处理缺失变量的情况
     * - 记录格式化后的提示词用于调试
     *
     * @param template 包含 {key} 占位符的模板字符串
     * @param variables 变量映射表
     * @return 替换后的提示词字符串
     */
    public static String formatPrompt(String template, Map<String, String> variables) {
        String result = template;
        // 遍历所有变量，逐个替换
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }
}
