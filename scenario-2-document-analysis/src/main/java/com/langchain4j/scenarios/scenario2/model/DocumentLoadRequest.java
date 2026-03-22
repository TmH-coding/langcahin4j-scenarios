package com.langchain4j.scenarios.scenario2.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文档加载请求模型 - REST API 请求数据传输对象
 *
 * 职责说明：
 * - 封装客户端发送的文档加载请求数据
 * - 包含文档路径、ID、标题、类型等信息
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
 * - 前端上传文档到后端进行分析
 * - 支持多种文档类型
 * - 支持文档元数据传递
 * - 用于企业知识库系统
 *
 * 字段说明：
 * - path: 文档文件路径
 *   * 格式：完整的文件系统路径
 *   * 用途：定位要加载的文档文件
 *   * 示例：\"/documents/contract_2024.txt\"
 *   * 约束：不能为空，必须是有效的文件路径
 *
 * - documentId: 文档唯一标识
 *   * 格式：自定义ID格式
 *   * 用途：在系统中唯一标识文档
 *   * 示例：\"contract_001\" 或 \"doc-123-abc\"
 *   * 约束：不能为空，长度限制
 *
 * - title: 文档标题
 *   * 格式：文本字符串
 *   * 用途：文档的显示名称
 *   * 示例：\"2024年度合同\"
 *   * 约束：可选字段
 *
 * - documentType: 文档类型
 *   * 格式：文件扩展名或MIME类型
 *   * 用途：标识文档格式
 *   * 示例：\"pdf\"、\"txt\"、\"docx\"
 *   * 约束：可选字段，用于文件类型验证
 *
 * 工作原理：
 * 1. 前端构建 DocumentLoadRequest 对象
 * 2. 将对象序列化为JSON
 * 3. 发送HTTP POST请求到 /api/document-analysis/load
 * 4. 后端接收JSON并反序列化为 DocumentLoadRequest 对象
 * 5. 控制器验证请求参数
 * 6. 调用 DocumentAnalysisService 加载文档
 * 7. 返回加载结果
 *
 * 请求示例：
 * {
 *   \"path\": \"/documents/contract_2024.txt\",
 *   \"documentId\": \"contract_001\",
 *   \"title\": \"2024年度合同\",
 *   \"documentType\": \"txt\"
 * }
 *
 * 性能考虑：
 * - 对象创建和反序列化速度快
 * - JSON反序列化后的对象大小通常 < 1KB
 * - 建议使用对象池减少GC压力
 *
 * 安全考虑：
 * - 验证 path 防止目录遍历攻击
 * - 验证 documentId 格式防止注入攻击
 * - 限制 title 的长度防止内存溢出
 * - 验证 documentType 防止恶意文件类型
 * - 实现请求速率限制
 * - 记录所有请求用于审计
 *
 * 验证规则：
 * - path: 必填，有效的文件路径
 * - documentId: 必填，长度1-100字符
 * - title: 可选，长度0-200字符
 * - documentType: 可选，长度1-20字符
 *
 * 扩展建议：
 * - 可以添加 userId 字段用于用户追踪
 * - 可以添加 tags 字段用于文档分类
 * - 可以添加 metadata 字段用于扩展信息
 * - 可以添加 priority 字段用于优先级设置
 * - 可以添加 expiryTime 字段用于文档过期时间
 * - 可以添加 accessControl 字段用于权限管理
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentLoadRequest {
    /** 文档文件路径 */
    private String path;

    /** 文档唯一标识 */
    private String documentId;

    /** 文档标题 */
    private String title;

    /** 文档类型（pdf、txt、docx等） */
    private String documentType;
}
