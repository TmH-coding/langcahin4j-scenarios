package com.langchain4j.scenarios.scenario1.service;

import com.langchain4j.scenarios.common.util.ConversationMemoryUtil;
import com.langchain4j.scenarios.common.util.PromptBuilder;
import com.langchain4j.scenarios.common.util.PromptTemplateUtil;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 智能客服系统服务 - 多轮对话的AI核心引擎
 *
 * 职责说明：
 * - 实现多轮对话的客服AI核心逻辑
 * - 维护对话历史和上下文信息
 * - 调用LLM模型生成智能回复
 * - 支持对话清空和历史查询
 * - 管理对话内存和上下文窗口
 *
 * 架构设计：
 * - 使用 @Service 注解注册为 Spring Bean
 * - 使用 @RequiredArgsConstructor 自动注入依赖
 * - 依赖 ChatLanguageModel 进行LLM调用
 * - 使用 ConversationMemoryUtil 管理对话历史
 * - 使用 PromptTemplateUtil 获取系统提示词
 * - 支持对话上下文的完整管理
 *
 * 使用场景：
 * - 处理客户咨询和问题
 * - 提供24/7自动化客服支持
 * - 学习多轮对话的实现方式
 * - 支持电商、SaaS、在线服务等平台
 * - 用于客服系统的核心业务逻辑
 *
 * 核心特性：
 * - 对话内存管理：保存最近20条消息
 * - 系统提示词：定义客服AI的角色和行为
 * - 上下文感知：每次回复都考虑完整的对话历史
 * - 多轮对话支持：支持连续的用户-AI交互
 * - 对话历史查询：支持查看完整的对话记录
 *
 * 工作原理：
 * 1. 用户发送消息到 chat() 方法
 * 2. 将用户消息添加到对话历史
 * 3. 获取客服系统提示词
 * 4. 获取格式化的对话历史作为上下文
 * 5. 调用 ChatLanguageModel 生成回复
 * 6. 将AI回复添加到对话历史
 * 7. 返回回复内容给用户
 * 8. 对话历史自动维护最近20条消息
 *
 * 字段说明：
 * - chatModel: LLM语言模型
 *   * 类型：ChatLanguageModel
 *   * 用途：生成AI回复
 *   * 来源：Spring 依赖注入
 *
 * - memory: 对话内存管理器
 *   * 类型：ConversationMemoryUtil
 *   * 容量：20条消息
 *   * 用途：维护对话历史和上下文
 *
 * 性能考虑：
 * - 对话内存使用滑动窗口算法
 * - 最多保存20条消息，内存占用小
 * - LLM调用可能需要1-5秒
 * - 建议使用异步处理提高响应速度
 * - 可以配置对话内存大小
 * - 监控LLM调用的延迟
 *
 * 安全考虑：
 * - 验证用户输入防止注入攻击
 * - 限制消息长度防止内存溢出
 * - 不要在对话中暴露敏感信息
 * - 实现请求速率限制
 * - 记录所有对话用于审计
 * - 实现对话内容的加密存储
 * - 防止对话被篡改
 *
 * 可靠性考虑：
 * - 处理LLM调用异常
 * - 实现请求超时控制
 * - 支持请求重试机制
 * - 记录详细的错误日志
 * - 实现优雅的降级处理
 * - 支持对话的恢复机制
 *
 * 扩展建议：
 * - 可以添加多语言支持
 * - 可以实现对话评分和反馈
 * - 可以支持对话转人工客服
 * - 可以添加对话分析和报告
 * - 可以实现对话导出功能
 * - 可以支持对话搜索和检索
 * - 可以添加对话的知识库集成
 * - 可以实现对话的个性化定制
 */
@Service
@RequiredArgsConstructor
public class CustomerServiceAI {

    /** 注入的LLM模型 */
    private final ChatLanguageModel chatModel;

    /** 对话内存管理器，保存最近20条消息 */
    private final ConversationMemoryUtil memory = new ConversationMemoryUtil(20);

    /**
     * 处理用户消息并返回AI回复
     *
     * 功能流程：
     * 1. 将用户消息添加到对话历史
     * 2. 获取客服系统提示词
     * 3. 构建完整的对话上下文
     * 4. 调用LLM生成回复
     * 5. 将AI回复添加到对话历史
     * 6. 返回回复内容
     *
     * @param userMessage 用户输入的消息
     * @return AI生成的回复
     */
    public String chat(String userMessage) {
        // 将用户消息添加到对话历史
        memory.addMessage("user", userMessage);

        // 获取客服系统提示词
        String systemPrompt = PromptTemplateUtil.getTemplate("customer_service_system");

        // 获取对话历史消息并转换为 ChatMessage
        List<ChatMessage> conversationHistory = PromptBuilder.convertConversationMessagesToChat(
            memory.getMessages());

        // 构建消息列表（包含系统提示词、历史消息和当前用户消息）
        List<ChatMessage> messages = PromptBuilder.buildMessagesWithHistory(
            systemPrompt, conversationHistory, userMessage);

        // 调用LLM生成回复
        Response<dev.langchain4j.data.message.AiMessage> response = chatModel.generate(messages);
        String assistantMessage = response.content().text();

        // 将AI回复添加到对话历史
        memory.addMessage("assistant", assistantMessage);

        return assistantMessage;
    }

    /**
     * 清空对话历史
     *
     * 使用场景：
     * - 开始新的客户对话
     * - 重置对话状态
     */
    public void clearHistory() {
        memory.clear();
    }

    /**
     * 获取完整的对话历史
     *
     * 功能：
     * - 返回格式化的对话历史
     * - 用于前端显示或日志记录
     *
     * @return 格式化的对话历史字符串
     */
    public String getConversationHistory() {
        return memory.getFormattedHistory();
    }
}


