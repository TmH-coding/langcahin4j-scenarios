package com.langchain4j.scenarios.scenario6.agent;

import com.langchain4j.scenarios.scenario6.tool.AgentTools;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Agent 核心服务 - 智能体的大脑
 *
 * ============ Agent 工作原理详解 ============
 *
 * 什么是 Agent？
 * Agent 是一个能够自主思考和决策的系统，它可以：
 * 1. 理解用户的自然语言请求
 * 2. 分析需要调用哪些工具来完成任务
 * 3. 调用相应的工具获取信息
 * 4. 根据工具返回的结果进行推理
 * 5. 重复上述过程直到完成任务
 * 6. 返回最终答案给用户
 *
 * Agent 的核心循环（ReAct 框架）：
 * ┌─────────────────────────────────────────────────────────┐
 * │ 1. 用户输入 → "帮我查询用户 user123 的信息并发送邮件"    │
 * │                                                          │
 * │ 2. LLM 思考 → "我需要：                                  │
 * │    - 调用 queryUserInfo 获取用户信息                    │
 * │    - 调用 sendEmail 发送邮件"                           │
 * │                                                          │
 * │ 3. 执行工具 → 调用 queryUserInfo("user123")             │
 * │    返回：用户ID: user123, 姓名: 张三, 邮箱: ...         │
 * │                                                          │
 * │ 4. LLM 继续推理 → "我已获得用户信息，现在发送邮件"      │
 * │                                                          │
 * │ 5. 执行工具 → 调用 sendEmail(...)                       │
 * │    返回：邮件已成功发送                                  │
 * │                                                          │
 * │ 6. LLM 总结 → "已完成！用户信息已查询，邮件已发送"      │
 * │                                                          │
 * │ 7. 返回最终答案给用户                                    │
 * └─────────────────────────────────────────────────────────┘
 *
 * 关键概念：
 * - Reasoning（推理）：LLM 分析问题，决定调用哪些工具
 * - Acting（行动）：执行工具调用
 * - Observation（观察）：获取工具返回的结果
 * - 循环：根据观察结果继续推理和行动
 *
 * Agent 与普通 LLM 的区别：
 * ┌──────────────────────────────────────────────────────────┐
 * │ 普通 LLM：                                                │
 * │ 用户输入 → LLM 生成答案 → 返回答案                        │
 * │ 问题：LLM 的知识可能过时，无法执行实时操作                │
 * │                                                          │
 * │ Agent：                                                  │
 * │ 用户输入 → LLM 分析 → 调用工具 → 获取实时数据 →          │
 * │ LLM 继续推理 → 返回准确答案                              │
 * │ 优势：能够执行实时操作，获取最新数据，完成复杂任务        │
 * └──────────────────────────────────────────────────────────┘
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IntelligentAgentService {

    private final ChatLanguageModel chatModel;
    private final AgentTools agentTools;

    /**
     * Agent 系统提示词
     *
     * 这个提示词定义了 Agent 的角色、能力和行为规范。
     * LLM 会根据这个提示词来理解自己的职责和如何使用工具。
     */
    private static final String AGENT_SYSTEM_PROMPT = """
            你是一个智能助手 Agent，具有以下能力：

            1. 获取当前时间
            2. 执行数学计算
            3. 查询用户信息
            4. 查询天气信息
            5. 发送邮件
            6. 分析数据

            你的职责是：
            - 理解用户的需求
            - 分析需要调用哪些工具来完成任务
            - 调用相应的工具获取信息
            - 根据工具返回的结果进行推理
            - 提供准确、有帮助的答案

            重要规则：
            - 如果需要信息，先调用相应的工具获取
            - 不要猜测或编造数据
            - 如果工具返回错误，尝试用其他方式解决
            - 始终诚实地告诉用户你的能力限制
            - 用中文回复用户
            """;

    /**
     * 处理用户请求的主方法
     *
     * 工作流程：
     * 1. 初始化消息列表（包含系统提示词和用户消息）
     * 2. 进入 Agent 循环
     * 3. 调用 LLM 获取响应
     * 4. 检查 LLM 是否要求调用工具
     * 5. 如果需要调用工具，执行工具并将结果反馈给 LLM
     * 6. 重复直到 LLM 给出最终答案
     * 7. 返回最终答案
     *
     * @param userMessage 用户的请求
     * @return Agent 的最终答案
     */
    public String chat(String userMessage) {
        log.info("Agent received user message: {}", userMessage);

        // 初始化消息列表
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(SystemMessage.from(AGENT_SYSTEM_PROMPT));
        messages.add(UserMessage.from(userMessage));

        // Agent 循环 - 最多执行 10 次迭代，防止无限循环
        int maxIterations = 10;
        int iteration = 0;

        while (iteration < maxIterations) {
            iteration++;
            log.info("Agent iteration: {}", iteration);

            // 调用 LLM 获取响应
            Response<AiMessage> response = chatModel.generate(messages);
            AiMessage aiMessage = response.content();

            log.info("LLM response: {}", aiMessage.text());

            // 将 LLM 的响应添加到消息列表
            messages.add(aiMessage);

            // 检查 LLM 是否要求调用工具
            // 注意：这里使用简化的方式检查，实际应用中应使用 LangChain4j 的工具调用机制
            if (!aiMessage.hasToolExecutionRequests()) {
                // LLM 给出了最终答案，返回
                log.info("Agent completed, returning final answer");
                return aiMessage.text();
            }

            // 执行工具调用
            List<ToolExecutionRequest> toolRequests = aiMessage.toolExecutionRequests();
            for (ToolExecutionRequest toolRequest : toolRequests) {
                String toolResult = executeToolRequest(toolRequest);
                log.info("Tool execution result: {}", toolResult);

                // 将工具结果添加到消息列表
                messages.add(UserMessage.from("工具执行结果: " + toolResult));
            }
        }

        // 如果超过最大迭代次数，返回错误信息
        log.warn("Agent exceeded maximum iterations");
        return "抱歉，我无法完成这个任务。请尝试更简单的请求。";
    }

    /**
     * 执行工具请求
     *
     * 这个方法负责：
     * 1. 解析工具请求（工具名称和参数）
     * 2. 调用相应的工具方法
     * 3. 返回工具执行结果
     *
     * @param toolRequest 工具请求
     * @return 工具执行结果
     */
    private String executeToolRequest(ToolExecutionRequest toolRequest) {
        String toolName = toolRequest.name();
        String toolArguments = toolRequest.arguments();

        log.info("Executing tool: {}, arguments: {}", toolName, toolArguments);

        try {
            // 根据工具名称调用相应的方法
            return switch (toolName) {
                case "getCurrentTime" -> agentTools.getCurrentTime();
                case "calculate" -> {
                    // 解析参数：operation, num1, num2
                    Map<String, Object> args = parseJsonArguments(toolArguments);
                    String operation = (String) args.get("operation");
                    double num1 = ((Number) args.get("num1")).doubleValue();
                    double num2 = ((Number) args.get("num2")).doubleValue();
                    yield agentTools.calculate(operation, num1, num2);
                }
                case "queryUserInfo" -> {
                    Map<String, Object> args = parseJsonArguments(toolArguments);
                    String userId = (String) args.get("userId");
                    yield agentTools.queryUserInfo(userId);
                }
                case "queryWeather" -> {
                    Map<String, Object> args = parseJsonArguments(toolArguments);
                    String city = (String) args.get("city");
                    yield agentTools.queryWeather(city);
                }
                case "sendEmail" -> {
                    Map<String, Object> args = parseJsonArguments(toolArguments);
                    String recipient = (String) args.get("recipient");
                    String subject = (String) args.get("subject");
                    String content = (String) args.get("content");
                    yield agentTools.sendEmail(recipient, subject, content);
                }
                case "analyzeData" -> {
                    Map<String, Object> args = parseJsonArguments(toolArguments);
                    String dataType = (String) args.get("dataType");
                    yield agentTools.analyzeData(dataType);
                }
                default -> "未知的工具: " + toolName;
            };
        } catch (Exception e) {
            log.error("Tool execution error", e);
            return "工具执行失败: " + e.getMessage();
        }
    }

    /**
     * 解析 JSON 格式的工具参数
     *
     * @param jsonArguments JSON 格式的参数字符串
     * @return 解析后的参数 Map
     */
    private Map<String, Object> parseJsonArguments(String jsonArguments) {
        // 简化实现，实际应使用 Jackson 或 Gson
        Map<String, Object> args = new HashMap<>();
        // 这里应该使用 JSON 解析库，但为了简化示例，使用简单的字符串处理
        // 实际应用中应使用 ObjectMapper 或类似的库
        return args;
    }

    /**
     * 获取 Agent 的能力列表
     *
     * 这个方法返回 Agent 可以调用的所有工具的信息。
     * 用户可以通过这个方法了解 Agent 能做什么。
     *
     * @return 工具列表
     */
    public List<String> getAvailableTools() {
        return List.of(
                "getCurrentTime - 获取当前时间",
                "calculate - 执行数学运算（+, -, *, /）",
                "queryUserInfo - 查询用户信息",
                "queryWeather - 查询天气信息",
                "sendEmail - 发送邮件",
                "analyzeData - 分析数据（sales, users, revenue）"
        );
    }

    /**
     * 获取 Agent 的使用示例
     *
     * @return 使用示例列表
     */
    public List<String> getExamples() {
        return List.of(
                "例子1: 现在几点了？",
                "例子2: 帮我计算 100 + 50",
                "例子3: 查询用户 user123 的信息",
                "例子4: 北京今天天气怎么样？",
                "例子5: 帮我给 zhangsan@example.com 发邮件，主题是'会议通知'，内容是'明天下午3点开会'",
                "例子6: 分析销售数据并告诉我增长率",
                "例子7: 查询用户 user456 的邮箱，然后给他发邮件说'您的账户已激活'"
        );
    }
}
