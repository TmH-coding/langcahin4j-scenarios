package com.langchain4j.scenarios.scenario7.controller;

import com.langchain4j.scenarios.scenario7.mcp.McpTool;
import com.langchain4j.scenarios.scenario7.mcp.McpToolExecutionRequest;
import com.langchain4j.scenarios.scenario7.mcp.McpToolExecutionResult;
import com.langchain4j.scenarios.scenario7.service.McpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * MCP 协议 REST API 控制器
 *
 * 提供 MCP 相关的 HTTP 端点，用于：
 * 1. 获取可用工具列表
 * 2. 执行工具
 * 3. 与 MCP 助手对话
 * 4. 获取学习指南
 */
@Slf4j
@RestController
@RequestMapping("/api/mcp")
@RequiredArgsConstructor
public class McpController {
    private final McpService mcpService;

    /**
     * 获取所有可用的 MCP 工具
     *
     * 响应示例：
     * {
     *   "tools": [
     *     {
     *       "name": "get_current_time",
     *       "description": "获取当前日期和时间",
     *       "inputSchema": {...},
     *       "category": "utility",
     *       "requiresConfirmation": false
     *     },
     *     ...
     *   ],
     *   "count": 6
     * }
     */
    @GetMapping("/tools")
    public ResponseEntity<Map<String, Object>> getTools() {
        try {
            List<McpTool> tools = mcpService.getAvailableTools();
            Map<String, Object> response = new HashMap<>();
            response.put("tools", tools);
            response.put("count", tools.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to get tools", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 执行 MCP 工具
     *
     * 请求体示例：
     * {
     *   "requestId": "req_123",
     *   "toolName": "calculate",
     *   "arguments": {
     *     "operation": "+",
     *     "num1": 100,
     *     "num2": 50
     *   }
     * }
     *
     * 响应示例：
     * {
     *   "requestId": "req_123",
     *   "success": true,
     *   "result": {
     *     "success": true,
     *     "operation": "+",
     *     "num1": 100,
     *     "num2": 50,
     *     "result": 150
     *   },
     *   "executionTime": 5
     * }
     */
    @PostMapping("/execute")
    public ResponseEntity<McpToolExecutionResult> executeTool(@RequestBody McpToolExecutionRequest request) {
        try {
            if (request.getRequestId() == null) {
                request.setRequestId("req_" + System.currentTimeMillis());
            }
            request.setTimestamp(System.currentTimeMillis());

            McpToolExecutionResult result = mcpService.executeTool(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Failed to execute tool", e);
            McpToolExecutionResult errorResult = new McpToolExecutionResult();
            errorResult.setSuccess(false);
            errorResult.setErrorMessage(e.getMessage());
            return ResponseEntity.status(500).body(errorResult);
        }
    }

    /**
     * 与 MCP 助手对话
     *
     * 请求体示例：
     * {
     *   "message": "现在几点了？"
     * }
     *
     * 响应示例：
     * {
     *   "message": "现在是 2026-03-22 09:38:21",
     *   "status": "success"
     * }
     */
    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, String> request) {
        try {
            String userMessage = request.get("message");
            if (userMessage == null || userMessage.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Message is required"));
            }

            String response = mcpService.chat(userMessage);
            Map<String, Object> result = new HashMap<>();
            result.put("message", response);
            result.put("status", "success");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Chat failed", e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", e.getMessage(),
                    "status", "error"
            ));
        }
    }

    /**
     * 获取 MCP 学习指南
     */
    @GetMapping("/guide")
    public ResponseEntity<Map<String, Object>> getGuide() {
        try {
            Map<String, Object> guide = mcpService.getLearningGuide();
            return ResponseEntity.ok(guide);
        } catch (Exception e) {
            log.error("Failed to get guide", e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "MCP Protocol");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * 获取 MCP 与 Agent 的对比
     */
    @GetMapping("/comparison")
    public ResponseEntity<Map<String, Object>> getComparison() {
        Map<String, Object> comparison = new HashMap<>();

        Map<String, Object> agent = new HashMap<>();
        agent.put("name", "Agent（智能体）");
        agent.put("framework", "ReAct（Reasoning + Acting）");
        agent.put("tool_calling", "LLM 自主决定调用哪些工具");
        agent.put("multi_step", "支持多步推理和决策");
        agent.put("standardization", "工具接口不统一");
        agent.put("use_case", "复杂任务、多步骤推理");
        agent.put("example", "查询用户信息 → 发送邮件 → 分析结果");

        Map<String, Object> mcp = new HashMap<>();
        mcp.put("name", "MCP（Model Context Protocol）");
        mcp.put("framework", "标准化工具接口协议");
        mcp.put("tool_calling", "遵循 JSON Schema 规范");
        mcp.put("multi_step", "支持多步推理（通过 LLM）");
        mcp.put("standardization", "所有工具遵循相同规范");
        mcp.put("use_case", "互操作性、安全性、版本管理");
        mcp.put("example", "标准化的工具定义和执行");

        comparison.put("agent", agent);
        comparison.put("mcp", mcp);

        List<String> advantages = new ArrayList<>();
        advantages.add("MCP 强调标准化和互操作性");
        advantages.add("Agent 强调自主性和灵活性");
        advantages.add("MCP 适合企业级应用");
        advantages.add("Agent 适合复杂推理任务");
        comparison.put("key_differences", advantages);

        return ResponseEntity.ok(comparison);
    }
}
