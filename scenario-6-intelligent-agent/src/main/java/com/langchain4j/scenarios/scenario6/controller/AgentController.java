package com.langchain4j.scenarios.scenario6.controller;

import com.langchain4j.scenarios.scenario6.agent.IntelligentAgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Agent 控制器 - 提供 HTTP 接口
 *
 * 端点说明：
 * - POST /api/agent/chat - 与 Agent 对话
 * - GET /api/agent/tools - 获取可用工具列表
 * - GET /api/agent/examples - 获取使用示例
 */
@Slf4j
@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentController {

    private final IntelligentAgentService agentService;

    /**
     * 与 Agent 对话
     *
     * 请求示例：
     * POST /api/agent/chat
     * {
     *   "message": "帮我查询用户 user123 的信息"
     * }
     *
     * 响应示例：
     * {
     *   "message": "用户ID: user123, 姓名: 张三, 邮箱: zhangsan@example.com, 电话: 13800138000, 部门: 技术部"
     * }
     */
    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        log.info("Received chat request: {}", userMessage);

        try {
            String response = agentService.chat(userMessage);
            Map<String, Object> result = new HashMap<>();
            result.put("message", response);
            result.put("status", "success");
            return result;
        } catch (Exception e) {
            log.error("Chat error", e);
            Map<String, Object> result = new HashMap<>();
            result.put("message", "处理请求时出错: " + e.getMessage());
            result.put("status", "error");
            return result;
        }
    }

    /**
     * 获取可用工具列表
     *
     * 请求示例：
     * GET /api/agent/tools
     *
     * 响应示例：
     * {
     *   "tools": [
     *     "getCurrentTime - 获取当前时间",
     *     "calculate - 执行数学运算（+, -, *, /）",
     *     ...
     *   ]
     * }
     */
    @GetMapping("/tools")
    public Map<String, Object> getTools() {
        Map<String, Object> result = new HashMap<>();
        result.put("tools", agentService.getAvailableTools());
        result.put("count", agentService.getAvailableTools().size());
        return result;
    }

    /**
     * 获取使用示例
     *
     * 请求示例：
     * GET /api/agent/examples
     *
     * 响应示例：
     * {
     *   "examples": [
     *     "例子1: 现在几点了？",
     *     "例子2: 帮我计算 100 + 50",
     *     ...
     *   ]
     * }
     */
    @GetMapping("/examples")
    public Map<String, Object> getExamples() {
        Map<String, Object> result = new HashMap<>();
        result.put("examples", agentService.getExamples());
        return result;
    }

    /**
     * 健康检查端点
     */
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "Intelligent Agent");
    }
}
