package com.langchain4j.scenarios.scenario7.mcp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MCP 工具定义
 *
 * MCP（Model Context Protocol）是 Anthropic 提出的标准化工具接口协议。
 * 每个 MCP 工具都包含：
 * - name: 工具的唯一标识符
 * - description: 工具的功能描述
 * - inputSchema: 工具参数的 JSON Schema 定义
 *
 * 这个类定义了 MCP 工具的标准结构。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class McpTool {
    /**
     * 工具名称（唯一标识符）
     * 例如：get_weather, send_email, query_database
     */
    private String name;

    /**
     * 工具的功能描述
     * 用于帮助 LLM 理解工具的用途
     */
    private String description;

    /**
     * 工具参数的 JSON Schema 定义
     * 定义了工具接受的参数类型和结构
     * 例如：
     * {
     *   "type": "object",
     *   "properties": {
     *     "city": {"type": "string", "description": "城市名称"},
     *     "unit": {"type": "string", "enum": ["celsius", "fahrenheit"]}
     *   },
     *   "required": ["city"]
     * }
     */
    private Object inputSchema;

    /**
     * 工具的分类
     * 用于组织和管理工具
     */
    private String category;

    /**
     * 工具是否需要用户确认
     * 某些敏感操作（如删除、转账）需要用户确认
     */
    private boolean requiresConfirmation;
}
