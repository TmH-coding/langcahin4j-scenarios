package com.langchain4j.scenarios.scenario7.mcp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MCP 工具执行结果
 *
 * 工具执行完成后，返回一个 ToolExecutionResult。
 * 这个结果包含：
 * - 执行是否成功
 * - 返回的数据
 * - 可能的错误信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class McpToolExecutionResult {
    /**
     * 对应的请求 ID
     */
    private String requestId;

    /**
     * 执行是否成功
     */
    private boolean success;

    /**
     * 工具的执行结果
     */
    private Object result;

    /**
     * 错误信息（如果执行失败）
     */
    private String errorMessage;

    /**
     * 执行耗时（毫秒）
     */
    private long executionTime;
}
