package com.langchain4j.scenarios.scenario1.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天响应模型 - REST API 响应数据传输对象
 *
 * 职责说明：
 * - 封装服务器返回的聊天响应数据
 * - 包含AI回复内容和元数据信息
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
 * - 返回AI生成的聊天回复
 * - 返回错误信息和异常状态
 * - 前端接收并显示AI回复
 * - 记录对话历史和统计分析
 * - 支持多会话并发响应
 *
 * 字段说明：
 * - message: 响应消息内容
 *   * 格式：纯文本或HTML格式
 *   * 用途：AI生成的回复内容
 *   * 示例：\"感谢您的咨询，我已为您查询订单信息...\"
 *
 * - sessionId: 会话标识符
 *   * 格式：UUID格式
 *   * 用途：关联多轮对话
 *   * 示例：\"550e8400-e29b-41d4-a716-446655440000\"
 *
 * - timestamp: 响应时间戳
 *   * 格式：毫秒级时间戳
 *   * 用途：记录响应时间
 *   * 示例：1234567890000
 *
 * - status: 响应状态
 *   * 可能的值：\"success\"、\"error\"、\"pending\"
 *   * 用途：标记响应是否成功
 *   * 示例：\"success\"
 *
 * - errorMessage: 错误信息
 *   * 格式：错误描述文本
 *   * 用途：当status为error时返回错误信息
 *   * 示例：\"会话已过期，请重新创建\"
 *
 * 工作原理：
 * 1. 控制器调用 CustomerServiceAI.chat() 获取回复
 * 2. 构建 ChatResponse 对象
 * 3. 设置 message、sessionId、timestamp、status 字段
 * 4. 如果发生异常，设置 status 为 error 并填充 errorMessage
 * 5. 返回 ResponseEntity 包装的 ChatResponse
 * 6. Spring 自动将对象序列化为JSON
 * 7. 前端接收JSON并解析显示
 *
 * 性能考虑：
 * - 对象创建和序列化速度快
 * - 支持批量创建多个响应对象
 * - JSON序列化后的大小通常 < 2KB
 * - 建议使用对象池减少GC压力
 * - 可以缓存常用的响应模板
 *
 * 安全考虑：
 * - 不要在响应中暴露敏感信息（密码、密钥等）
 * - 验证 message 内容防止XSS攻击
 * - 限制 message 的长度防止内存溢出
 * - 不要在 errorMessage 中暴露系统内部细节
 * - 对敏感数据进行加密存储
 * - 实现响应的访问控制
 *
 * 扩展建议：
 * - 可以添加 code 字段用于错误分类
 * - 可以添加 data 字段用于返回额外数据
 * - 可以添加 traceId 字段用于请求追踪
 * - 可以添加 duration 字段记录处理时间
 * - 可以添加 metadata 字段用于扩展信息
 * - 可以支持多语言错误消息
 * - 可以添加分页信息用于列表响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    /** 响应消息内容 */
    private String message;

    /** 会话ID */
    private String sessionId;

    /** 响应时间戳 */
    private long timestamp;

    /** 响应状态：success、error */
    private String status;

    /** 错误信息（如果有） */
    private String errorMessage;
}
