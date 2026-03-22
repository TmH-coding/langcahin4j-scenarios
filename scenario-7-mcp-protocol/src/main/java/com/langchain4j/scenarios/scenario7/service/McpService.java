package com.langchain4j.scenarios.scenario7.service;

import com.langchain4j.scenarios.scenario7.mcp.McpTool;
import com.langchain4j.scenarios.scenario7.mcp.McpToolExecutionRequest;
import com.langchain4j.scenarios.scenario7.mcp.McpToolExecutionResult;
import com.langchain4j.scenarios.scenario7.tool.McpTools;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * MCP 服务
 *
 * 这个服务实现了 MCP（Model Context Protocol）的核心功能：
 * 1. 管理 MCP 工具的定义和元数据
 * 2. 执行 LLM 的工具调用请求
 * 3. 处理工具执行结果并反馈给 LLM
 *
 * MCP 与 Agent 的区别：
 * - Agent：LLM 自主决定调用哪些工具，支持多步推理
 * - MCP：标准化的工具接口，强调互操作性和安全性
 *
 * MCP 的优势：
 * 1. 标准化：所有工具遵循相同的 JSON Schema 规范
 * 2. 可互操作性：不同的 LLM 和客户端都能理解 MCP 工具
 * 3. 安全性：工具的权限和访问控制更清晰
 * 4. 版本管理：支持工具的版本控制和向后兼容性
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class McpService {
    private final ChatLanguageModel chatModel;

    private static final String MCP_SYSTEM_PROMPT = """
            你是一个 MCP（Model Context Protocol）智能助手。

            你可以使用以下工具来帮助用户：
            1. get_current_time - 获取当前时间
            2. calculate - 执行数学运算
            3. query_user_info - 查询用户信息
            4. query_weather - 查询天气信息
            5. send_email - 发送邮件
            6. analyze_data - 分析数据

            重要规则：
            - 每个工具都有明确的 JSON Schema 定义
            - 工具参数必须符合 Schema 规范
            - 工具执行结果包含 success 字段表示是否成功
            - 如果工具执行失败，返回结果中会包含 error 字段

            你的职责：
            1. 理解用户的请求
            2. 决定是否需要调用工具
            3. 根据工具的 JSON Schema 构建正确的参数
            4. 解释工具的执行结果
            5. 给出最终答案
            """;

    /**
     * 获取所有可用的 MCP 工具
     */
    public List<McpTool> getAvailableTools() {
        List<McpTool> tools = new ArrayList<>();

        // 工具 1: 获取当前时间
        tools.add(new McpTool(
                "get_current_time",
                "获取当前日期和时间",
                buildSchema(
                        "object",
                        Map.of("timezone", Map.of("type", "string", "description", "时区（可选）")),
                        List.of()
                ),
                "utility",
                false
        ));

        // 工具 2: 数学计算
        tools.add(new McpTool(
                "calculate",
                "执行数学运算（+, -, *, /）",
                buildSchema(
                        "object",
                        Map.of(
                                "operation", Map.of("type", "string", "enum", List.of("+", "-", "*", "/")),
                                "num1", Map.of("type", "number"),
                                "num2", Map.of("type", "number")
                        ),
                        List.of("operation", "num1", "num2")
                ),
                "utility",
                false
        ));

        // 工具 3: 查询用户信息
        tools.add(new McpTool(
                "query_user_info",
                "查询用户的详细信息",
                buildSchema(
                        "object",
                        Map.of("userId", Map.of("type", "string", "description", "用户 ID")),
                        List.of("userId")
                ),
                "database",
                false
        ));

        // 工具 4: 查询天气
        tools.add(new McpTool(
                "query_weather",
                "查询指定城市的天气信息",
                buildSchema(
                        "object",
                        Map.of("city", Map.of("type", "string", "description", "城市名称")),
                        List.of("city")
                ),
                "external",
                false
        ));

        // 工具 5: 发送邮件
        tools.add(new McpTool(
                "send_email",
                "发送邮件给指定收件人",
                buildSchema(
                        "object",
                        Map.of(
                                "recipient", Map.of("type", "string", "description", "收件人邮箱"),
                                "subject", Map.of("type", "string", "description", "邮件主题"),
                                "content", Map.of("type", "string", "description", "邮件内容")
                        ),
                        List.of("recipient", "subject", "content")
                ),
                "communication",
                true  // 需要用户确认
        ));

        // 工具 6: 数据分析
        tools.add(new McpTool(
                "analyze_data",
                "分析指定类型的数据",
                buildSchema(
                        "object",
                        Map.of("dataType", Map.of("type", "string", "enum", List.of("sales", "users", "revenue"))),
                        List.of("dataType")
                ),
                "analytics",
                false
        ));

        return tools;
    }

    /**
     * 与 MCP 助手对话
     */
    public String chat(String userMessage) {
        try {
            List<ChatMessage> messages = new ArrayList<>();
            messages.add(SystemMessage.from(MCP_SYSTEM_PROMPT));
            messages.add(UserMessage.from(userMessage));

            Response<AiMessage> response = chatModel.generate(messages);
            AiMessage aiMessage = response.content();
            return aiMessage.text();
        } catch (Exception e) {
            log.error("Chat failed", e);
            return "Error: " + e.getMessage();
        }
    }

    /**
     * 执行 MCP 工具
     */
    public McpToolExecutionResult executeTool(McpToolExecutionRequest request) {
        long startTime = System.currentTimeMillis();
        McpToolExecutionResult result = new McpToolExecutionResult();
        result.setRequestId(request.getRequestId());

        try {
            Map<String, Object> args = (Map<String, Object>) request.getArguments();
            Object toolResult = null;

            switch (request.getToolName()) {
                case "get_current_time":
                    toolResult = McpTools.getCurrentTime(args);
                    break;
                case "calculate":
                    toolResult = McpTools.calculate(args);
                    break;
                case "query_user_info":
                    toolResult = McpTools.queryUserInfo(args);
                    break;
                case "query_weather":
                    toolResult = McpTools.queryWeather(args);
                    break;
                case "send_email":
                    toolResult = McpTools.sendEmail(args);
                    break;
                case "analyze_data":
                    toolResult = McpTools.analyzeData(args);
                    break;
                default:
                    result.setSuccess(false);
                    result.setErrorMessage("Unknown tool: " + request.getToolName());
                    return result;
            }

            Map<String, Object> toolResultMap = (Map<String, Object>) toolResult;
            boolean success = (boolean) toolResultMap.getOrDefault("success", false);

            result.setSuccess(success);
            result.setResult(toolResult);

            if (!success) {
                result.setErrorMessage((String) toolResultMap.get("error"));
            }

        } catch (Exception e) {
            log.error("Tool execution failed", e);
            result.setSuccess(false);
            result.setErrorMessage("Tool execution error: " + e.getMessage());
        }

        result.setExecutionTime(System.currentTimeMillis() - startTime);
        return result;
    }

    /**
     * 构建 JSON Schema
     */
    private Map<String, Object> buildSchema(String type, Map<String, Object> properties, List<String> required) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", type);
        schema.put("properties", properties);
        if (!required.isEmpty()) {
            schema.put("required", required);
        }
        return schema;
    }

    /**
     * 获取 MCP 学习指南
     */
    public Map<String, Object> getLearningGuide() {
        Map<String, Object> guide = new HashMap<>();

        guide.put("title", "MCP 协议学习指南");
        guide.put("description", "Model Context Protocol - Anthropic 的标准化工具接口协议");

        Map<String, String> concepts = new HashMap<>();
        concepts.put("MCP", "Model Context Protocol - 标准化的工具接口协议");
        concepts.put("JSON Schema", "用于定义工具参数的标准格式");
        concepts.put("Tool Definition", "包含名称、描述、参数定义的工具元数据");
        concepts.put("Interoperability", "不同 LLM 和客户端都能理解 MCP 工具");
        guide.put("concepts", concepts);

        Map<String, String> advantages = new HashMap<>();
        advantages.put("standardization", "所有工具遵循相同的接口规范");
        advantages.put("interoperability", "支持多个 LLM 和客户端");
        advantages.put("security", "清晰的权限和访问控制");
        advantages.put("versioning", "支持工具版本管理");
        guide.put("advantages", advantages);

        List<String> tools = new ArrayList<>();
        for (McpTool tool : getAvailableTools()) {
            tools.add(tool.getName() + " - " + tool.getDescription());
        }
        guide.put("available_tools", tools);

        return guide;
    }
}
