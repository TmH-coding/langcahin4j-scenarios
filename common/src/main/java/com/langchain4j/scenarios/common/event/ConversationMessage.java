package com.langchain4j.scenarios.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话消息事件 - 消息队列中传输的对话消息数据结构
 *
 * 职责说明：
 * - 定义消息队列中传输的对话消息格式
 * - 支持消息的序列化和反序列化
 * - 包含对话的完整上下文信息
 * - 支持消息的异步处理
 * - 实现消息的可靠传输
 *
 * 架构设计：
 * - 实现 Serializable 接口支持序列化
 * - 使用 Lombok 简化代码
 * - 支持 Builder 模式灵活构建
 * - 包含消息的完整生命周期信息
 * - 易于在消息队列中传输
 *
 * 使用场景：
 * - 发送对话消息到 RabbitMQ 队列
 * - 异步处理对话消息
 * - 记录对话消息到数据库
 * - 分析对话消息数据
 * - 实现对话的分布式处理
 *
 * 消息流程：
 * 1. 用户发送对话请求
 * 2. 业务逻辑处理请求
 * 3. 构建 ConversationMessage 对象
 * 4. 发送到消息队列
 * 5. 消息监听器接收消息
 * 6. 异步处理消息
 * 7. 更新数据库或触发其他操作
 *
 * 事件类型规范：
 * - \"START\" - 对话开始
 * - \"PROCESSING\" - 对话处理中
 * - \"COMPLETED\" - 对话完成
 * - \"FAILED\" - 对话失败
 *
 * 性能考虑：
 * - 消息序列化速度快
 * - 支持高并发消息处理
 * - 建议使用消息压缩减少网络传输
 * - 建议使用消息批处理提高吞吐量
 *
 * 安全考虑：
 * - 消息可能包含敏感信息
 * - 考虑对消息进行加密
 * - 限制消息监听器的访问权限
 * - 记录消息用于审计
 *
 * 扩展建议：
 * - 可以添加消息优先级字段
 * - 可以添加消息重试次数字段
 * - 可以添加消息超时配置
 * - 可以实现消息的版本控制
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String conversationId;
    private String userId;
    private String message;
    private String response;
    private String eventType; // START, PROCESSING, COMPLETED, FAILED
    private LocalDateTime timestamp;
    private String metadata;
}
