package com.langchain4j.scenarios.common.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 通知消息 - 多渠道通知的数据模型
 *
 * 职责说明：
 * - 定义通知消息结构
 * - 支持多种通知类型（邮件、短信、推送）
 * - 支持多个接收者
 * - 记录通知的发送状态和结果
 * - 提供通知的序列化和反序列化
 *
 * 架构设计：
 * - 使用 Lombok 注解简化代码（@Data、@Builder、@NoArgsConstructor、@AllArgsConstructor）
 * - 支持 Builder 模式构建对象
 * - 支持 Getter/Setter 自动生成
 * - 支持 equals/hashCode/toString 自动生成
 * - 支持无参和全参构造函数
 * - 可序列化用于消息队列传输
 *
 * 使用场景：
 * - 邮件通知：发送电子邮件给用户
 * - 短信通知：发送短信给用户手机
 * - 推送通知：发送应用推送给用户设备
 * - 通知队列：将通知消息放入消息队列异步处理
 * - 通知追踪：记录通知的发送状态和结果
 * - 通知重试：支持失败通知的重试机制
 *
 * 字段说明：
 * - id: 通知唯一标识符
 *   * 格式：UUID 或自定义格式
 *   * 用途：追踪和关联通知
 *   * 示例：\"notif-123-abc-def\"
 *
 * - type: 通知类型
 *   * 可能的值：\"email\"、\"sms\"、\"push\"
 *   * 用途：确定通知发送方式
 *   * 示例：\"email\"
 *
 * - recipient: 接收者
 *   * 邮件：邮箱地址（user@example.com）
 *   * 短信：手机号码（13800138000）
 *   * 推送：用户 ID（user-123）
 *   * 示例：\"user@example.com\"
 *
 * - subject: 主题
 *   * 邮件：邮件主题
 *   * 推送：推送标题
 *   * 短信：不使用
 *   * 示例：\"欢迎加入我们的平台\"
 *
 * - content: 内容
 *   * 邮件：邮件正文（支持 HTML）
 *   * 短信：短信内容
 *   * 推送：推送内容
 *   * 示例：\"<h1>欢迎</h1><p>感谢您的注册...</p>\"
 *
 * - sendTime: 发送时间
 *   * 格式：时间戳（毫秒）
 *   * 用途：记录通知发送时间
 *   * 示例：1234567890000
 *
 * - sent: 是否已发送
 *   * 值：true/false
 *   * 用途：标记通知是否已发送
 *   * 示例：true
 *
 * - result: 发送结果
 *   * 可能的值：\"SUCCESS\"、\"FAILURE\"、\"PENDING\"、\"RETRY\"
 *   * 用途：记录通知发送的结果
 *   * 示例：\"SUCCESS\"
 *
 * 工作原理：
 * 1. 业务逻辑创建 Notification 对象
 * 2. 使用 Builder 模式设置各个字段
 * 3. 将通知对象发送到 NotificationService
 * 4. 服务根据 type 路由到相应的发送方法
 * 5. 发送方法将通知放入消息队列
 * 6. 消费者异步处理通知发送
 * 7. 更新通知的 sent 和 result 字段
 * 8. 返回发送结果给调用者
 *
 * 使用示例：
 * // 创建邮件通知
 * Notification emailNotif = Notification.builder()
 *     .id(UUID.randomUUID().toString())
 *     .type(\"email\")
 *     .recipient(\"user@example.com\")
 *     .subject(\"欢迎加入\")
 *     .content(\"<h1>欢迎</h1><p>感谢您的注册</p>\")
 *     .sendTime(System.currentTimeMillis())
 *     .sent(false)
 *     .result(\"PENDING\")
 *     .build();
 *
 * // 创建短信通知
 * Notification smsNotif = Notification.builder()
 *     .id(UUID.randomUUID().toString())
 *     .type(\"sms\")
 *     .recipient(\"13800138000\")
 *     .content(\"您的验证码是：123456\")
 *     .sendTime(System.currentTimeMillis())
 *     .sent(false)
 *     .result(\"PENDING\")
 *     .build();
 *
 * // 创建推送通知
 * Notification pushNotif = Notification.builder()
 *     .id(UUID.randomUUID().toString())
 *     .type(\"push\")
 *     .recipient(\"user-123\")
 *     .subject(\"新消息\")
 *     .content(\"您有一条来自 AI 助手的新消息\")
 *     .sendTime(System.currentTimeMillis())
 *     .sent(false)
 *     .result(\"PENDING\")
 *     .build();
 *
 * 性能考虑：
 * - 通知对象是轻量级的，创建和序列化速度快
 * - 支持批量创建多个通知对象
 * - 可以缓存常用的通知模板
 * - 建议使用对象池减少 GC 压力
 * - 序列化后的大小通常 < 1KB
 *
 * 安全考虑：
 * - 不要在通知中包含敏感信息（密码、密钥等）
 * - 验证 recipient 的有效性
 * - 验证 type 的合法性
 * - 限制 content 的长度
 * - 防止通知内容注入攻击
 * - 加密敏感的通知数据
 * - 实现通知的访问控制
 *
 * 扩展建议：
 * - 可以添加优先级字段（priority）
 * - 可以添加重试次数字段（retryCount）
 * - 可以添加过期时间字段（expiryTime）
 * - 可以添加标签字段（tags）用于分类
 * - 可以添加元数据字段（metadata）用于扩展
 * - 可以支持模板变量替换
 * - 可以支持多语言通知
 * - 可以添加通知的追踪信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    /** 通知ID */
    private String id;

    /** 通知类型（email、sms、push） */
    private String type;

    /** 接收者 */
    private String recipient;

    /** 主题 */
    private String subject;

    /** 内容 */
    private String content;

    /** 发送时间 */
    private long sendTime;

    /** 是否已发送 */
    private boolean sent;

    /** 发送结果 */
    private String result;
}
