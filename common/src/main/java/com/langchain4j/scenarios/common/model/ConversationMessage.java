package com.langchain4j.scenarios.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对话消息数据模型 - 内存中的对话消息表示
 *
 * 职责说明：
 * - 表示对话中的单条消息
 * - 记录消息的角色、内容和时间戳
 * - 用于构建对话历史和上下文
 * - 支持多轮对话的内存管理
 * - 用于 LLM 上下文窗口的消息组织
 *
 * 架构设计：
 * - 使用 Lombok 简化 getter/setter 和构造函数
 * - 使用 Builder 模式支持灵活的对象构建
 * - 轻量级设计，适合内存存储
 * - 与 ConversationRecord 实体区分（内存 vs 持久化）
 *
 * 使用场景：
 * - 在 ConversationMemoryUtil 中存储对话历史
 * - 在各个场景中维护多轮对话
 * - 构建 LLM 的消息列表
 * - 支持对话回放和分析
 * - 实现对话上下文管理
 *
 * 与 ConversationRecord 的区别：
 * - ConversationMessage：内存中的临时消息，用于当前会话
 * - ConversationRecord：数据库中的持久化消息，用于历史记录
 * - ConversationMessage 通常转换为 ConversationRecord 进行持久化
 *
 * 性能考虑：
 * - 消息存储在内存中，适合短期使用
 * - 对话历史过长时考虑清理或分页
 * - 时间戳使用毫秒精度，支持精确的时间排序
 *
 * 扩展建议：
 * - 可以添加消息 ID 用于去重
 * - 可以添加消息元数据（如来源、优先级等）
 * - 可以添加消息向量化表示用于语义搜索
 * - 可以实现消息压缩以节省内存
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationMessage {
    /**
     * 消息角色 - 消息的发送者身份
     *
     * 说明：
     * - 标识消息是由用户还是 AI 发送
     * - 可能的值：\"user\" 或 \"assistant\"
     * - 用于区分对话中的不同参与者
     * - 必须设置，不能为 null
     *
     * 角色说明：
     * - \"user\"：用户发送的消息
     *   * 用户输入的问题或指令
     *   * 用户上传的文档或数据
     *   * 用户的反馈和评论
     *
     * - \"assistant\"：AI 发送的消息
     *   * AI 生成的回复
     *   * AI 的分析结果
     *   * AI 的建议和推荐
     *
     * 应用场景：
     * - 构建对话历史时区分消息来源
     * - 分析用户和 AI 的交互模式
     * - 生成对话摘要和统计
     * - 实现对话的可视化展示
     *
     * 使用示例：
     * ConversationMessage userMsg = ConversationMessage.builder()
     *     .role(\"user\")
     *     .content(\"请分析这个数据\")
     *     .timestamp(System.currentTimeMillis())
     *     .build();
     *
     * ConversationMessage assistantMsg = ConversationMessage.builder()
     *     .role(\"assistant\")
     *     .content(\"数据分析结果如下...\")
     *     .timestamp(System.currentTimeMillis())
     *     .build();
     *
     * 与 LLM API 的集成：
     * - OpenAI API 使用 \"user\" 和 \"assistant\" 角色
     * - 其他 LLM 可能使用不同的角色名称
     * - 需要在调用 LLM 前进行角色转换
     */
    private String role;

    /**
     * 消息内容 - 对话消息的实际文本内容
     *
     * 说明：
     * - 存储消息的完整文本
     * - 可以为 null（某些情况下消息可能为空）
     * - 支持任意长度的文本
     * - 用于 LLM 的输入和输出
     *
     * 内容类型：
     * - 用户消息：用户输入的问题或指令
     *   * 示例：\"请分析这个 PDF 文档\"
     *   * 示例：\"生成一个 Java 类\"
     *   * 示例：\"这个代码有什么问题？\"
     *
     * - AI 消息：AI 生成的回复
     *   * 示例：\"文档分析结果如下...\"
     *   * 示例：\"public class User { ... }\"
     *   * 示例：\"这段代码存在以下问题...\"
     *
     * 性能考虑：
     * - 长消息会占用更多内存
     * - 对话历史过长时考虑清理或分页
     * - 可以实现消息摘要或压缩
     * - 可以使用流式处理处理大型消息
     *
     * 安全考虑：
     * - 消息可能包含敏感信息
     * - 考虑加密存储
     * - 限制访问权限
     * - 定期清理过期数据
     *
     * 使用示例：
     * String content = message.getContent();
     * if (content.length() > 1000) {
     *     String summary = content.substring(0, 1000) + \"...\";
     * }
     *
     * // 发送给 LLM
     * String response = llmService.chat(content);
     */
    private String content;

    /**
     * 消息时间戳 - 消息创建的时间戳（毫秒）
     *
     * 说明：
     * - 记录消息创建的时间
     * - 单位：毫秒（自 1970-01-01 00:00:00 UTC 以来的毫秒数）
     * - 类型：long（64 位整数）
     * - 通常由应用在创建消息时设置
     * - 示例：1711000000000
     *
     * 应用场景：
     * - 按时间顺序排序消息
     * - 计算消息间的时间间隔
     * - 实现消息的时间序列分析
     * - 支持对话的时间线展示
     * - 实现消息的过期清理
     *
     * 时间戳获取方式：
     * - System.currentTimeMillis()：获取当前时间戳
     * - System.nanoTime()：获取纳秒精度时间（用于性能测量）
     * - LocalDateTime.now().toInstant().toEpochMilli()：从 LocalDateTime 转换
     *
     * 使用示例：
     * ConversationMessage message = ConversationMessage.builder()
     *     .role(\"user\")
     *     .content(\"你好\")
     *     .timestamp(System.currentTimeMillis())
     *     .build();
     *
     * // 计算消息间隔
     * long interval = message2.getTimestamp() - message1.getTimestamp();
     * System.out.println(\"消息间隔：\" + interval + \" 毫秒\");
     *
     * // 按时间排序消息
     * List<ConversationMessage> sorted = messages.stream()
     *     .sorted(Comparator.comparingLong(ConversationMessage::getTimestamp))
     *     .collect(Collectors.toList());
     *
     * 性能考虑：
     * - 时间戳比较速度快
     * - 支持高效的时间范围查询
     * - 可以用于实现消息的过期清理
     *
     * 与数据库的转换：
     * - 时间戳可以转换为 LocalDateTime
     * - LocalDateTime dateTime = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime();
     * - 用于持久化到数据库
     */
    private long timestamp;
}
