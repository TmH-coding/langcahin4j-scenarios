package com.langchain4j.scenarios.scenario7.mcp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MCP 工具执行请求
 *
 * 当 LLM 决定调用某个工具时，会生成一个 ToolExecutionRequest。
 * 这个请求包含：
 * - 工具名称
 * - 工具参数
 * - 请求 ID（用于追踪）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class McpToolExecutionRequest {
    /**
     * 请求的唯一标识符
     */
    private String requestId;

    /**
     * 要执行的工具名称
     */
    private String toolName;

    /**
     * 工具的输入参数（JSON 格式）
     */
    private Object arguments;

    /**
     * 请求的时间戳
     */
    private long timestamp;
}
