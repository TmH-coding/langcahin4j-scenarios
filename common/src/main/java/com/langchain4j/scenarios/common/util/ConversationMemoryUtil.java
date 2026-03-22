package com.langchain4j.scenarios.common.util;

import com.langchain4j.scenarios.common.model.ConversationMessage;
import java.util.*;

/**
 * 对话内存管理工具类 - 多轮对话的上下文管理和内存优化
 *
 * 职责说明：
 * - 管理对话历史记录在内存中的存储
 * - 实现滑动窗口机制，限制内存中保存的消息数量
 * - 提供消息添加、查询、清空等操作
 * - 支持格式化输出对话历史用于 LLM 上下文
 * - 防止内存溢出和性能下降
 * - 维护对话的完整上下文信息
 *
 * 架构设计：
 * - 使用 Deque（双端队列）实现 FIFO 的消息队列
 * - 当消息数超过限制时，自动删除最早的消息（滑动窗口）
 * - 使用 LinkedList 作为 Deque 的实现
 * - 支持灵活的最大消息数配置
 * - 线程不安全，需要外部同步
 *
 * 使用场景：
 * - 在多轮对话中维护上下文
 * - 防止内存溢出（通过限制最大消息数）
 * - 为 LLM 提供对话历史作为上下文
 * - 构建对话历史用于 LLM 的 prompt
 * - 支持对话的持久化前准备
 * - 实现对话的内存管理
 *
 * 设计模式：
 * - 使用 Deque（双端队列）实现 FIFO 的消息队列
 * - 当消息数超过限制时，自动删除最早的消息
 * - 使用 Builder 模式构建消息对象
 * - 支持灵活的配置和扩展
 *
 * 性能考虑：
 * - 消息添加：O(1) 时间复杂度
 * - 消息查询：O(n) 时间复杂度（需要遍历所有消息）
 * - 内存占用：受最大消息数限制，可预测
 * - 格式化输出：O(n) 时间复杂度
 * - 建议最大消息数：10-50 条（根据消息大小调整）
 *
 * 内存管理：
 * - 每条消息包含：role（字符串）、content（字符串）、timestamp（long）
 * - 平均消息大小：500-2000 字节
 * - 20 条消息占用内存：10-40 KB
 * - 建议定期清理过期消息
 * - 考虑使用消息摘要减少内存占用
 *
 * 线程安全：
 * - 当前实现线程不安全
 * - 多线程环境需要外部同步
 * - 可以使用 Collections.synchronizedList() 包装
 * - 或使用 ConcurrentLinkedDeque 替代
 *
 * 扩展建议：
 * - 可以添加消息过期时间管理
 * - 可以实现消息压缩和摘要
 * - 可以添加消息持久化功能
 * - 可以实现消息搜索和过滤
 * - 可以添加消息统计和分析
 * - 可以实现消息加密存储
 */
public class ConversationMemoryUtil {

    /** 默认最大消息数：20条 - 平衡内存占用和上下文完整性 */
    private static final int DEFAULT_MAX_MESSAGES = 20;

    /** 消息队列，使用双端队列实现 FIFO 消息管理 */
    private final Deque<ConversationMessage> messages;

    /** 最大消息数限制 - 超过此数量时自动删除最早的消息 */
    private final int maxMessages;

    /**
     * 构造函数 - 使用默认最大消息数
     *
     * 说明：
     * - 创建对话内存管理器
     * - 使用默认最大消息数（20 条）
     * - 初始化空的消息队列
     *
     * 使用示例：
     * ConversationMemoryUtil memory = new ConversationMemoryUtil();
     * memory.addMessage(\\\"user\\\", \\\"你好\\\");
     * memory.addMessage(\\\"assistant\\\", \\\"你好，有什么我可以帮助的吗？\\\");
     */
    public ConversationMemoryUtil() {
        this(DEFAULT_MAX_MESSAGES);
    }

    /**
     * 构造函数 - 指定最大消息数
     *
     * 说明：
     * - 创建对话内存管理器
     * - 允许自定义最大消息数限制
     * - 初始化空的消息队列
     *
     * 参数说明：
     * - maxMessages: 最大消息数限制
     *   * 范围：1-1000（建议 10-50）
     *   * 示例：20（保存最近 20 条消息）
     *   * 示例：50（保存最近 50 条消息）
     *   * 超过此数量时自动删除最早的消息
     *
     * 使用示例：
     * // 创建只保存 10 条消息的内存管理器
     * ConversationMemoryUtil memory = new ConversationMemoryUtil(10);
     *
     * // 创建保存 50 条消息的内存管理器
     * ConversationMemoryUtil memory = new ConversationMemoryUtil(50);
     *
     * 性能考虑：
     * - 消息数越多，内存占用越大
     * - 消息数越多，格式化输出越慢
     * - 建议根据消息大小调整最大消息数
     * - 平均消息大小 500-2000 字节
     *
     * @param maxMessages 最大消息数限制
     */
    public ConversationMemoryUtil(int maxMessages) {
        this.messages = new LinkedList<>();
        this.maxMessages = maxMessages;
    }

    /**
     * 添加消息到对话历史
     *
     * 功能：
     * - 创建新的 ConversationMessage 对象
     * - 记录当前时间戳（毫秒精度）
     * - 添加到消息队列末尾
     * - 如果超过最大限制，删除最早的消息（滑动窗口）
     * - 实现自动的内存管理和清理
     *
     * 参数说明：
     * - role: 消息角色
     *   * \\\"user\\\"：用户消息
     *   * \\\"assistant\\\"：AI 消息
     *   * 示例：\\\"user\\\", \\\"assistant\\\"
     *
     * - content: 消息内容
     *   * 用户输入的问题或 AI 生成的回复
     *   * 支持任意长度的文本
     *   * 示例：\\\"你好，请帮我分析这个数据\\\"
     *
     * 工作流程：
     * 1. 创建 ConversationMessage 对象
     * 2. 设置 role、content、timestamp
     * 3. 添加到队列末尾
     * 4. 检查是否超过最大消息数
     * 5. 如果超过，删除最早的消息
     *
     * 使用示例：
     * ConversationMemoryUtil memory = new ConversationMemoryUtil();
     *
     * // 添加用户消息
     * memory.addMessage(\\\"user\\\", \\\"你好\\\");
     *
     * // 添加 AI 消息
     * memory.addMessage(\\\"assistant\\\", \\\"你好，有什么我可以帮助的吗？\\\");
     *
     * // 添加后续消息
     * memory.addMessage(\\\"user\\\", \\\"请分析这个数据\\\");
     * memory.addMessage(\\\"assistant\\\", \\\"数据分析结果如下...\\\");
     *
     * 滑动窗口示例：
     * ConversationMemoryUtil memory = new ConversationMemoryUtil(3);
     * memory.addMessage(\\\"user\\\", \\\"消息1\\\");  // 队列：[消息1]
     * memory.addMessage(\\\"assistant\\\", \\\"消息2\\\");  // 队列：[消息1, 消息2]
     * memory.addMessage(\\\"user\\\", \\\"消息3\\\");  // 队列：[消息1, 消息2, 消息3]
     * memory.addMessage(\\\"assistant\\\", \\\"消息4\\\");  // 队列：[消息2, 消息3, 消息4]（消息1 被删除）
     *
     * 性能考虑：
     * - 添加消息：O(1) 时间复杂度
     * - 删除最早消息：O(1) 时间复杂度
     * - 总体性能：非常高效
     *
     * 时间戳说明：
     * - 使用 System.currentTimeMillis() 获取当前时间戳
     * - 精度：毫秒级
     * - 用于消息排序和时间分析
     *
     * @param role 消息角色（\\\"user\\\" 或 \\\"assistant\\\"）
     * @param content 消息内容
     */
    public void addMessage(String role, String content) {
        // 创建新消息对象，包含角色、内容和时间戳
        ConversationMessage message = ConversationMessage.builder()
                .role(role)
                .content(content)
                .timestamp(System.currentTimeMillis())
                .build();

        // 添加到队列末尾
        messages.addLast(message);

        // 如果超过最大消息数，删除最早的消息（实现滑动窗口）
        if (messages.size() > maxMessages) {
            messages.removeFirst();
        }
    }

    /**
     * 获取所有消息列表
     *
     * 功能：
     * - 返回当前所有消息的副本
     * - 避免外部代码直接修改内部消息队列
     * - 支持安全的消息访问
     *
     * 返回值：
     * - List<ConversationMessage>：消息列表的副本
     * - 返回的是新的 ArrayList，修改不会影响内部队列
     * - 消息按添加顺序排列（最早的在前）
     *
     * 使用示例：
     * ConversationMemoryUtil memory = new ConversationMemoryUtil();
     * memory.addMessage(\\\"user\\\", \\\"你好\\\");
     * memory.addMessage(\\\"assistant\\\", \\\"你好\\\");
     *
     * List<ConversationMessage> messages = memory.getMessages();
     * for (ConversationMessage msg : messages) {
     *     System.out.println(msg.getRole() + \\\": \\\" + msg.getContent());
     * }
     * // 输出：
     * // user: 你好
     * // assistant: 你好
     *
     * 安全性说明：
     * - 返回的是副本，不是原始队列
     * - 修改返回的列表不会影响内部状态
     * - 适合外部代码安全访问消息
     *
     * 性能考虑：
     * - 创建副本需要 O(n) 时间复杂度
     * - 内存占用：额外的 ArrayList 对象
     * - 建议缓存结果避免频繁调用
     *
     * @return 消息列表的副本（避免外部修改）
     */
    public List<ConversationMessage> getMessages() {
        return new ArrayList<>(messages);
    }

    /**
     * 获取格式化的对话历史
     *
     * 功能：
     * - 将所有消息格式化为字符串
     * - 格式：ROLE: content\n
     * - 用于传递给 LLM 作为上下文
     * - 支持多轮对话的完整历史展示
     *
     * 返回值：
     * - String：格式化的对话历史字符串
     * - 每条消息占一行
     * - 角色转大写，然后是冒号和内容
     *
     * 格式说明：
     * - 角色转大写：USER、ASSISTANT
     * - 分隔符：冒号和空格
     * - 行分隔符：换行符 \n
     * - 示例：
     *   USER: 你好
     *   ASSISTANT: 你好，有什么我可以帮助的吗？
     *   USER: 请分析这个数据
     *   ASSISTANT: 数据分析结果如下...
     *
     * 使用示例：
     * ConversationMemoryUtil memory = new ConversationMemoryUtil();
     * memory.addMessage(\\\"user\\\", \\\"你好\\\");
     * memory.addMessage(\\\"assistant\\\", \\\"你好\\\");
     * memory.addMessage(\\\"user\\\", \\\"请分析数据\\\");
     *
     * String history = memory.getFormattedHistory();
     * System.out.println(history);
     * // 输出：
     * // USER: 你好
     * // ASSISTANT: 你好
     * // USER: 请分析数据
     *
     * LLM 上下文示例：
     * String history = memory.getFormattedHistory();
     * String prompt = \\\"对话历史：\\n\\\" + history + \\\"\\n请继续对话\\\";
     * String response = llmService.chat(prompt);
     *
     * 性能考虑：
     * - 时间复杂度：O(n)（需要遍历所有消息）
     * - 空间复杂度：O(m)（m 为输出字符串长度）
     * - 建议缓存结果避免频繁调用
     * - 对于大量消息，考虑流式处理
     *
     * 扩展建议：
     * - 可以添加自定义格式参数
     * - 可以实现消息摘要功能
     * - 可以添加时间戳显示
     * - 可以实现消息过滤
     *
     * @return 格式化的对话历史字符串
     */
    public String getFormattedHistory() {
        StringBuilder sb = new StringBuilder();
        for (ConversationMessage msg : messages) {
            // 角色转大写，然后是冒号和内容
            sb.append(msg.getRole().toUpperCase()).append(": ")
                    .append(msg.getContent()).append("\n");
        }
        return sb.toString();
    }

    /**
     * 清空所有对话历史
     *
     * 功能：
     * - 删除所有消息
     * - 重置对话状态
     * - 为新的对话做准备
     *
     * 使用场景：
     * - 开始新的对话
     * - 重置对话状态
     * - 释放内存
     * - 切换对话主题
     *
     * 使用示例：
     * ConversationMemoryUtil memory = new ConversationMemoryUtil();
     * memory.addMessage(\\\"user\\\", \\\"你好\\\");
     * memory.addMessage(\\\"assistant\\\", \\\"你好\\\");
     *
     * System.out.println(\\\"消息数：\\\" + memory.size());  // 输出：2
     *
     * memory.clear();
     *
     * System.out.println(\\\"消息数：\\\" + memory.size());  // 输出：0
     *
     * 性能考虑：
     * - 时间复杂度：O(n)（需要删除所有消息）
     * - 内存释放：立即释放所有消息占用的内存
     * - 建议在切换对话时调用
     */
    public void clear() {
        messages.clear();
    }

    /**
     * 获取当前消息数量
     *
     * 功能：
     * - 返回当前队列中的消息数
     * - 用于检查消息数量
     * - 支持消息数量监控
     *
     * 返回值：
     * - int：当前消息数量
     * - 范围：0 到 maxMessages
     * - 示例：0（空队列）、5（5 条消息）、20（满队列）
     *
     * 使用示例：
     * ConversationMemoryUtil memory = new ConversationMemoryUtil(20);
     * System.out.println(\\\"初始消息数：\\\" + memory.size());  // 输出：0
     *
     * memory.addMessage(\\\"user\\\", \\\"消息1\\\");
     * memory.addMessage(\\\"assistant\\\", \\\"消息2\\\");
     * System.out.println(\\\"当前消息数：\\\" + memory.size());  // 输出：2
     *
     * // 检查是否达到最大限制
     * if (memory.size() >= 20) {
     *     System.out.println(\\\"消息已满，最早的消息将被删除\\\");
     * }
     *
     * 性能考虑：
     * - 时间复杂度：O(1)
     * - 非常高效的操作
     *
     * @return 消息数量
     */
    public int size() {
        return messages.size();
    }
}
