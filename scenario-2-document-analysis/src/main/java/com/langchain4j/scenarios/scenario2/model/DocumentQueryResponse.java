package com.langchain4j.scenarios.scenario2.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文档查询响应模型 - REST API 响应数据传输对象
 *
 * 职责说明：
 * - 封装服务器返回的文档查询响应数据
 * - 包含查询结果、文档ID、段落数等信息
 * - 用于 REST API 响应体的JSON序列化
 * - 支持成功和错误响应的统一格式
 *
 * 架构设计：
 * - 使用 Lombok 注解简化代码（@Data、@Builder、@NoArgsConstructor、@AllArgsConstructor）
 * - 支持 Builder 模式构建对象
 * - 支持 Getter/Setter 自动生成
 * - 支持 equals/hashCode/toString 自动生成
 * - 支持无参和全参构造函数
 * - 可序列化为JSON格式
 *
 * 使用场景：
 * - 返回文档查询的结果
 * - 返回错误信息和异常状态
 * - 前端接收并显示查询结果
 * - 记录查询历史和统计分析
 * - 支持多文档并发查询
 *
 * 字段说明：
 * - result: 查询结果
 *   * 格式：纯文本或结构化数据
 *   * 用途：返回查询的具体结果
 *   * 示例：\"Found 3 relevant segments for: 付款条件\"
 *
 * - documentId: 文档标识符
 *   * 格式：文档唯一ID
 *   * 用途：标识查询的文档
 *   * 示例：\"contract_001\"
 *
 * - relevantSegments: 相关段落数
 *   * 格式：整数
 *   * 用途：返回找到的相关段落数量
 *   * 示例：3
 *
 * - timestamp: 响应时间戳
 *   * 格式：毫秒级时间戳
 *   * 用途：记录响应时间
 *   * 示例：1234567890000
 *
 * - status: 响应状态
 *   * 可能的值：\"success\"、\"error\"、\"not_found\"
 *   * 用途：标记响应是否成功
 *   * 示例：\"success\"
 *
 * 工作原理：
 * 1. 控制器接收查询请求
 * 2. 调用 DocumentAnalysisService.queryDocument() 获取结果
 * 3. 构建 DocumentQueryResponse 对象
 * 4. 设置 result、documentId、relevantSegments、timestamp、status 字段
 * 5. 如果发生异常，设置 status 为 error 并填充 result
 * 6. 返回 ResponseEntity 包装的 DocumentQueryResponse
 * 7. Spring 自动将对象序列化为JSON
 * 8. 前端接收JSON并解析显示
 *
 * 响应示例：
 * {
 *   \"result\": \"Found 3 relevant segments for: 付款条件\",
 *   \"documentId\": \"contract_001\",
 *   \"relevantSegments\": 3,
 *   \"timestamp\": 1234567890000,
 *   \"status\": \"success\"
 * }
 *
 * 性能考虑：
 * - 对象创建和序列化速度快
 * - JSON序列化后的大小通常 < 5KB
 * - 建议使用对象池减少GC压力
 * - 可以缓存常用的响应模板
 *
 * 安全考虑：
 * - 不要在响应中暴露敏感信息
 * - 验证 result 内容防止XSS攻击
 * - 限制 result 的长度防止内存溢出
 * - 不要在 result 中暴露系统内部细节
 * - 对敏感数据进行加密存储
 * - 实现响应的访问控制
 *
 * 扩展建议：
 * - 可以添加 code 字段用于错误分类
 * - 可以添加 data 字段用于返回详细数据
 * - 可以添加 traceId 字段用于请求追踪
 * - 可以添加 duration 字段记录处理时间
 * - 可以添加 segments 字段返回具体的段落内容
 * - 可以添加 confidence 字段返回匹配置信度
 * - 可以添加 pagination 字段用于分页
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentQueryResponse {
    /** 查询结果 */
    private String result;

    /** 文档ID */
    private String documentId;

    /** 相关段落数 */
    private int relevantSegments;

    /** 响应时间戳 */
    private long timestamp;

    /** 响应状态 */
    private String status;
}
