package com.langchain4j.scenarios.scenario3.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 代码审查请求模型 - REST API 请求数据传输对象
 *
 * 职责说明：
 * - 封装客户端发送的代码审查请求数据
 * - 包含代码内容、编程语言、审查类型等信息
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
 * - 前端发送代码到后端进行审查
 * - 支持多种编程语言
 * - 支持不同类型的审查
 * - 用于开发团队的代码审查系统
 *
 * 字段说明：
 * - code: 要审查的代码
 *   * 格式：源代码文本
 *   * 用途：包含要审查的代码内容
 *   * 示例：\"public void test() { }\"
 *   * 约束：不能为空，长度限制
 *
 * - language: 编程语言
 *   * 格式：语言标识符
 *   * 用途：标识代码的编程语言
 *   * 示例：\"java\"、\"python\"、\"javascript\"
 *   * 约束：不能为空，必须是支持的语言
 *
 * - reviewType: 审查类型
 *   * 格式：审查类型标识符
 *   * 可能的值：\"quality\"、\"security\"、\"performance\"、\"all\"
 *   * 用途：指定审查的重点
 *   * 示例：\"security\"
 *   * 约束：可选字段，默认为 \"all\"
 *
 * 工作原理：
 * 1. 前端构建 CodeReviewRequest 对象
 * 2. 将对象序列化为JSON
 * 3. 发送HTTP POST请求到 /api/code-assistant/review
 * 4. 后端接收JSON并反序列化为 CodeReviewRequest 对象
 * 5. 控制器验证请求参数
 * 6. 调用 CodeAssistantService 进行代码审查
 * 7. 返回审查结果
 *
 * 请求示例：
 * {
 *   \"code\": \"public void test() { }\",
 *   \"language\": \"java\",
 *   \"reviewType\": \"quality\"
 * }
 *
 * 性能考虑：
 * - 对象创建和反序列化速度快
 * - JSON反序列化后的对象大小取决于代码长度
 * - 建议使用对象池减少GC压力
 * - 限制代码长度防止内存溢出
 *
 * 安全考虑：
 * - 验证 code 内容防止注入攻击
 * - 限制 code 的长度防止内存溢出
 * - 验证 language 格式防止注入攻击
 * - 验证 reviewType 防止非法值
 * - 实现请求速率限制
 * - 记录所有请求用于审计
 *
 * 验证规则：
 * - code: 必填，长度1-100000字符
 * - language: 必填，长度1-20字符
 * - reviewType: 可选，长度1-20字符
 *
 * 扩展建议：
 * - 可以添加 userId 字段用于用户追踪
 * - 可以添加 projectId 字段用于项目关联
 * - 可以添加 rules 字段用于自定义审查规则
 * - 可以添加 metadata 字段用于扩展信息
 * - 可以添加 priority 字段用于优先级设置
 * - 可以添加 timeout 字段用于超时控制
 * - 可以添加 tags 字段用于代码分类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeReviewRequest {
    /** 要审查的代码 */
    private String code;

    /** 编程语言 */
    private String language;

    /** 审查类型（quality、security、performance） */
    private String reviewType;
}
