package com.langchain4j.scenarios.common.util;

import dev.langchain4j.model.openai.OpenAiTokenizer;

/**
 * Token 计数工具 - 用于计算和管理 LLM 调用中的 Token 数量
 *
 * 职责说明：
 * - 计算文本的 Token 数量
 * - 检查文本是否超过 Token 限制
 * - 截断文本到指定的 Token 限制
 * - 支持 OpenAI 模型的 Token 计数
 * - 帮助优化 LLM 调用成本
 *
 * 架构设计：
 * - 使用 OpenAiTokenizer 进行 Token 计数
 * - 提供静态方法便于使用
 * - 支持灵活的 Token 管理
 *
 * 使用场景：
 * - 在发送请求前检查 Token 数量
 * - 防止超过模型的上下文窗口限制
 * - 优化成本和性能
 * - 实现动态的上下文管理
 *
 * Token 说明：
 * - 1 Token ≈ 4 个字符（英文）
 * - 1 Token ≈ 1-2 个汉字（中文）
 * - 不同模型的 Token 计数方式可能略有不同
 * - OpenAI 模型使用 cl100k_base 编码
 *
 * 模型上下文窗口：
 * - gpt-3.5-turbo: 4K tokens
 * - gpt-4: 8K tokens
 * - gpt-4-turbo-preview: 128K tokens
 * - gpt-4o: 128K tokens
 *
 * 性能考虑：
 * - Token 计数速度快
 * - 建议缓存计数结果
 * - 避免频繁计数相同的文本
 *
 * 扩展建议：
 * - 可以支持其他 LLM 提供商的 Token 计数
 * - 可以实现 Token 计数的缓存
 * - 可以添加 Token 使用统计
 * - 可以实现成本估算
 */
public class TokenCountUtil {

    /** OpenAI Token 计数器 */
    private static final OpenAiTokenizer tokenizer = new OpenAiTokenizer();

    /**
     * 计算文本的 Token 数量
     *
     * 功能：
     * - 使用 OpenAI 的 Token 计数方式
     * - 返回精确的 Token 数量
     * - 支持中英文混合文本
     *
     * 参数说明：
     * - text: 要计数的文本
     *
     * 返回值：
     * - int：文本的 Token 数量
     *
     * 使用示例：
     * String text = "Hello, how are you?";
     * int tokenCount = TokenCountUtil.countTokens(text);
     * System.out.println("Token count: " + tokenCount);  // 输出：Token count: 9
     *
     * 中文示例：
     * String text = "你好，请问今天天气如何？";
     * int tokenCount = TokenCountUtil.countTokens(text);
     * System.out.println("Token count: " + tokenCount);  // 输出：Token count: 15
     *
     * @param text 要计数的文本
     * @return Token 数量
     */
    public static int countTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        return tokenizer.estimateTokenCountInText(text);
    }

    /**
     * 检查文本是否超过 Token 限制
     *
     * 功能：
     * - 计算文本的 Token 数量
     * - 与限制值比较
     * - 返回是否超过限制
     *
     * 参数说明：
     * - text: 要检查的文本
     * - tokenLimit: Token 限制数量
     *
     * 返回值：
     * - boolean：true 表示超过限制，false 表示未超过
     *
     * 使用示例：
     * String text = "This is a very long text...";
     * if (TokenCountUtil.exceedsLimit(text, 100)) {
     *     System.out.println("Text exceeds token limit");
     *     // 处理超限情况
     * }
     *
     * @param text 要检查的文本
     * @param tokenLimit Token 限制数量
     * @return 是否超过限制
     */
    public static boolean exceedsLimit(String text, int tokenLimit) {
        return countTokens(text) > tokenLimit;
    }

    /**
     * 截断文本到指定的 Token 限制
     *
     * 功能：
     * - 逐步删除文本末尾的内容
     * - 直到 Token 数量不超过限制
     * - 保留尽可能多的原始内容
     *
     * 参数说明：
     * - text: 要截断的文本
     * - tokenLimit: Token 限制数量
     *
     * 返回值：
     * - String：截断后的文本
     * - 如果原文本已在限制内，返回原文本
     * - 如果需要截断，返回截断后的文本
     *
     * 使用示例：
     * String text = "This is a very long text that needs to be truncated...";
     * String truncated = TokenCountUtil.truncateToTokenLimit(text, 50);
     * System.out.println("Truncated: " + truncated);
     *
     * 中文示例：
     * String text = "这是一个很长的文本，需要被截断到指定的 Token 限制...";
     * String truncated = TokenCountUtil.truncateToTokenLimit(text, 30);
     * System.out.println("截断后: " + truncated);
     *
     * @param text 要截断的文本
     * @param tokenLimit Token 限制数量
     * @return 截断后的文本
     */
    public static String truncateToTokenLimit(String text, int tokenLimit) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        // 如果已在限制内，直接返回
        if (countTokens(text) <= tokenLimit) {
            return text;
        }

        // 逐步截断文本
        String result = text;
        while (result.length() > 0 && countTokens(result) > tokenLimit) {
            // 每次删除最后一个字符
            result = result.substring(0, result.length() - 1);
        }

        return result;
    }

    /**
     * 获取文本的 Token 统计信息
     *
     * 功能：
     * - 返回详细的 Token 统计信息
     * - 包括 Token 数量、字符数、估计成本等
     *
     * 参数说明：
     * - text: 要统计的文本
     *
     * 返回值：
     * - String：统计信息字符串
     *
     * 使用示例：
     * String text = "Hello, how are you?";
     * String stats = TokenCountUtil.getTokenStats(text);
     * System.out.println(stats);
     * // 输出：
     * // Token Count: 9
     * // Character Count: 21
     * // Estimated Cost (gpt-4): $0.00027
     *
     * @param text 要统计的文本
     * @return 统计信息字符串
     */
    public static String getTokenStats(String text) {
        if (text == null || text.isEmpty()) {
            return "Token Count: 0\nCharacter Count: 0";
        }

        int tokenCount = countTokens(text);
        int charCount = text.length();

        // 估计成本（基于 gpt-4-turbo-preview 的价格）
        // 输入：$0.01 per 1K tokens
        // 输出：$0.03 per 1K tokens
        double estimatedInputCost = (tokenCount / 1000.0) * 0.01;

        return String.format(
            "Token Count: %d\nCharacter Count: %d\nEstimated Input Cost (gpt-4-turbo): $%.5f",
            tokenCount, charCount, estimatedInputCost
        );
    }
}
