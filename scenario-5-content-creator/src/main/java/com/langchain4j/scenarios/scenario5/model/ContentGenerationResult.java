package com.langchain4j.scenarios.scenario5.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 内容生成结果模型 - REST API 响应数据传输对象
 *
 * 职责说明：
 * - 封装服务器返回的内容生成结果数据
 * - 包含生成内容、内容类型、字数统计等信息
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
 * - 返回生成的文章内容
 * - 返回社交媒体帖子
 * - 返回文章大纲
 * - 返回错误信息和异常状态
 * - 前端接收并显示生成结果
 * - 记录内容生成历史和统计分析
 * - 支持多内容类型并发处理
 *
 * 字段说明：
 * - content: 生成的内容
 *   * 格式：纯文本或结构化数据
 *   * 用途：返回生成的具体内容
 *   * 示例："这是一篇关于AI的文章..."
 *
 * - contentType: 内容类型
 *   * 格式：内容类型标识符
 *   * 可能的值："article"、"social-post"、"outline"等
 *   * 用途：标识生成的内容类型
 *   * 示例："article"
 *
 * - wordCount: 字数统计
 *   * 格式：整数
 *   * 用途：记录生成内容的字数
 *   * 示例：2000
 *
 * - timestamp: 响应时间戳
 *   * 格式：毫秒级时间戳
 *   * 用途：记录响应时间
 *   * 示例：1234567890000
 *
 * - status: 响应状态
 *   * 可能的值："success"、"error"、"partial"
 *   * 用途：标记响应是否成功
 *   * 示例："success"
 *
 * 工作原理：
 * 1. 控制器接收内容生成请求
 * 2. 调用 ContentCreatorService 进行生成
 * 3. 构建 ContentGenerationResult 对象
 * 4. 设置 content、contentType、wordCount、timestamp、status 字段
 * 5. 如果发生异常，设置 status 为 error 并填充 content
 * 6. 返回 ResponseEntity 包装的 ContentGenerationResult
 * 7. Spring 自动将对象序列化为JSON
 * 8. 前端接收JSON并解析显示
 *
 * 响应示例：
 * {
 *   "content": "这是一篇关于AI的文章...",
 *   "contentType": "article",
 *   "wordCount": 2000,
 *   "timestamp": 1234567890000,
 *   "status": "success"
 * }
 *
 * 性能考虑：
 * - 对象创建和序列化速度快
 * - JSON序列化后的大小通常 < 50KB
 * - 建议使用对象池减少GC压力
 * - 可以缓存常用的响应模板
 * - 支持流式传输大型内容
 *
 * 安全考虑：
 * - 不要在响应中暴露敏感信息
 * - 验证 content 内容防止XSS攻击
 * - 限制 content 的长度防止内存溢出
 * - 不要在 content 中暴露系统内部细节
 * - 对敏感数据进行加密存储
 * - 实现响应的访问控制
 * - 防止内容被篡改
 *
 * 可靠性考虑：
 * - 处理内容生成异常
 * - 实现请求超时控制
 * - 支持请求重试机制
 * - 记录详细的错误日志
 * - 实现优雅的降级处理
 * - 支持生成的恢复机制
 *
 * 扩展建议：
 * - 可以添加 code 字段用于错误分类
 * - 可以添加 data 字段返回详细数据
 * - 可以添加 traceId 字段用于请求追踪
 * - 可以添加 duration 字段记录处理时间
 * - 可以添加 charCount 字段返回字符数
 * - 可以添加 language 字段标识内容语言
 * - 可以添加 metrics 字段返回性能指标
 * - 可以添加 suggestions 字段返回改进建议
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContentGenerationResult {
    /** 生成的内容 */
    private String content;

    /** 内容类型（article、post、outline等） */
    private String contentType;

    /** 字数统计 */
    private int wordCount;

    /** 响应时间戳 */
    private long timestamp;

    /** 响应状态 */
    private String status;
}
