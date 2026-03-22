package com.langchain4j.scenarios.scenario1.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天请求模型 - REST API 请求数据传输对象
 *
 * 职责说明：
 * - 封装客户端发送的聊天请求数据
 * - 支持消息内容、会话ID、用户ID等信息
 * - 用于 REST API 请求体的JSON反序列化
 * - 支持参数验证和业务逻辑处理
 *
 * 架构设计：
 * - 使用 Lombok 注解简化代码（@Data、@Builder、@NoArgsConstructor、@AllArgsConstructor）
 * - 支持 Builder 模式构建对象
 * - 支持 Getter/Setter 自动生成
 * - 支持 equals/hashCode/toString 自动生成
 * - 支持无参和全参构造函数
 * - 可从JSON反序列化
 *
 * 使用场景：
 * - 前端发送用户消息到后端
 * - 支持多会话并发请求
 * - 支持用户身份追踪
 * - 支持对话上下文管理
 * - 用于电商、SaaS、在线服务等平台的客服系统
 *
 * 字段说明：
 * - message: 用户消息内容
 *   * 格式：纯文本
 *   * 用途：用户输入的问题或陈述
 *   * 示例：\"我想查询订单状态\"
 *   * 约束：不能为空，长度限制
 *
 * - sessionId: 会话标识符
 *   * 格式：UUID格式
 *   * 用途：关联多轮对话，可选字段
 *   * 示例：\"550e8400-e29b-41d4-a716-446655440000\"
 *   * 说明：如果为空，服务器会创建新会话
 *
 * - userId: 用户标识符
 *   * 格式：用户ID或邮箱
 *   * 用途：用户身份追踪，可选字段
 *   * 示例：\"user-123\" 或 \"user@example.com\"
 *   * 说明：用于记录审计日志和用户行为分析
 *
 * 工作原理：
 * 1. 前端构建 ChatRequest 对象
 * 2. 将对象序列化为JSON
 * 3. 发送HTTP POST请求到 /api/customer-service/chat
 * 4. 后端接收JSON并反序列化为 ChatRequest 对象
 * 5. 控制器验证请求参数
 * 6. 调用 CustomerServiceAI 处理请求
 * 7. 返回 ChatResponse 响应
 *
 * 请求示例：
 * {
 *   \"message\": \"我想查询订单状态\",
 *   \"sessionId\": \"550e8400-e29b-41d4-a716-446655440000\",
 *   \"userId\": \"user-123\"
 * }
 *
 * 性能考虑：
 * - 对象创建和反序列化速度快
 * - JSON反序列化后的对象大小通常 < 1KB
 * - 建议使用对象池减少GC压力
 * - 可以缓存常用的请求模板
 *
 * 安全考虑：
 * - 验证 message 内容防止XSS攻击
 * - 限制 message 的长度防止内存溢出
 * - 验证 sessionId 格式防止注入攻击
 * - 验证 userId 格式防止权限提升
 * - 实现请求速率限制防止滥用
 * - 记录所有请求用于审计
 * - 使用HTTPS加密传输
 *
 * 验证规则：
 * - message: 必填，长度1-5000字符
 * - sessionId: 可选，UUID格式
 * - userId: 可选，长度1-100字符
 *
 * 扩展建议：
 * - 可以添加 attachments 字段用于文件上传
 * - 可以添加 metadata 字段用于扩展信息
 * - 可以添加 priority 字段用于优先级设置
 * - 可以添加 language 字段用于多语言支持
 * - 可以添加 context 字段用于额外上下文
 * - 可以添加 timestamp 字段用于客户端时间戳
 * - 可以添加 deviceInfo 字段用于设备信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    /** 用户消息内容 */
    private String message;

    /** 会话ID（可选，用于多会话管理） */
    private String sessionId;

    /** 用户ID（可选，用于用户追踪） */
    private String userId;
}
