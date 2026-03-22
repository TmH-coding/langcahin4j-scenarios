package com.langchain4j.scenarios.scenario3.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 代码审查结果模型 - REST API 响应数据传输对象
 *
 * 职责说明：
 * - 封装服务器返回的代码审查结果数据
 * - 包含审查结果、问题数量、严重程度等信息
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
 * - 返回代码审查的结果
 * - 返回错误信息和异常状态
 * - 前端接收并显示审查结果
 * - 记录审查历史和统计分析
 * - 支持多代码并发审查
 *
 * 字段说明：
 * - result: 审查结果
 *   * 格式：纯文本或结构化数据
 *   * 用途：返回审查的具体结果和建议
 *   * 示例：\"Code length: 150 characters. Suggestions: Follow naming conventions\"
 *
 * - language: 编程语言
 *   * 格式：语言标识符
 *   * 用途：标识审查的代码语言
 *   * 示例：\"java\"、\"python\"、\"javascript\"
 *
 * - issueCount: 问题数量
 *   * 格式：整数
 *   * 用途：返回找到的问题数量
 *   * 示例：3
 *
 * - severity: 严重程度
 *   * 格式：严重程度级别
 *   * 可能的值：\"low\"、\"medium\"、\"high\"、\"critical\"
 *   * 用途：标记问题的严重程度
 *   * 示例：\"high\"
 *
 * - timestamp: 响应时间戳
 *   * 格式：毫秒级时间戳
 *   * 用途：记录响应时间
 *   * 示例：1234567890000
 *
 * - status: 响应状态
 *   * 可能的值：\"success\"、\"error\"、\"partial\"
 *   * 用途：标记响应是否成功
 *   * 示例：\"success\"
 *
 * 工作原理：
 * 1. 控制器接收代码审查请求
 * 2. 调用 CodeAssistantService.reviewCode() 获取结果
 * 3. 构建 CodeReviewResult 对象
 * 4. 设置 result、language、issueCount、severity、timestamp、status 字段
 * 5. 如果发生异常，设置 status 为 error 并填充 result
 * 6. 返回 ResponseEntity 包装的 CodeReviewResult
 * 7. Spring 自动将对象序列化为JSON
 * 8. 前端接收JSON并解析显示
 *
 * 响应示例：
 * {
 *   \"result\": \"Code length: 150 characters. Suggestions: Follow naming conventions\",
 *   \"language\": \"java\",
 *   \"issueCount\": 3,
 *   \"severity\": \"medium\",
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
 * - 可以添加 issues 字段返回具体的问题列表
 * - 可以添加 traceId 字段用于请求追踪
 * - 可以添加 duration 字段记录处理时间
 * - 可以添加 suggestions 字段返回改进建议
 * - 可以添加 metrics 字段返回代码指标
 * - 可以添加 confidence 字段返回审查置信度
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeReviewResult {
    /** 审查结果 */
    private String result;

    /** 编程语言 */
    private String language;

    /** 问题数量 */
    private int issueCount;

    /** 严重程度（low、medium、high） */
    private String severity;

    /** 响应时间戳 */
    private long timestamp;

    /** 响应状态 */
    private String status;
}
