package com.langchain4j.scenarios.scenario2.controller;

import com.langchain4j.scenarios.scenario2.model.DocumentLoadRequest;
import com.langchain4j.scenarios.scenario2.model.DocumentQueryResponse;
import com.langchain4j.scenarios.scenario2.service.DocumentAnalysisService;
import com.langchain4j.scenarios.scenario2.service.DocumentCacheManager;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 文档分析 REST API 控制器 - RAG系统的HTTP接口层
 *
 * 职责说明：
 * - 提供文档加载、分析和查询的HTTP REST接口
 * - 支持文档总结和智能问答功能
 * - 实现RAG（检索增强生成）的前端接口
 * - 管理文档的生命周期
 * - 返回结构化的JSON响应
 *
 * 架构设计：
 * - 使用 @RestController 注解实现REST接口
 * - 使用 @RequestMapping 统一路由前缀
 * - 使用 @RequiredArgsConstructor 自动注入依赖
 * - 依赖 DocumentAnalysisService 进行文档处理
 * - 依赖 DocumentCacheManager 进行文档缓存管理
 * - 返回 DocumentQueryResponse 对象进行响应封装
 *
 * 使用场景：
 * - 上传和处理长文档（PDF、TXT、DOCX等）
 * - 对文档内容进行智能查询和问答
 * - 生成文档摘要和关键信息提取
 * - 企业知识库系统
 * - 法律文件分析
 * - 技术文档查询
 *
 * API端点说明：
 * - POST /api/document-analysis/load - 加载文档
 * - GET /api/document-analysis/summarize/{documentId} - 总结文档
 * - POST /api/document-analysis/query/{documentId} - 查询文档
 * - GET /api/document-analysis/documents - 获取文档列表
 *
 * 工作原理：
 * 1. 客户端发送HTTP请求到相应端点
 * 2. 控制器接收请求参数
 * 3. 调用 DocumentAnalysisService 或 DocumentCacheManager 处理业务逻辑
 * 4. 构建 DocumentQueryResponse 响应对象
 * 5. 返回JSON格式的响应给客户端
 * 6. 前端解析响应并更新UI
 *
 * 性能考虑：
 * - 每个请求都是独立处理，支持高并发
 * - 文档存储在内存中，支持快速访问
 * - 建议使用负载均衡器分散请求
 * - 可以配置连接池提高性能
 * - 建议添加请求缓存减少重复计算
 * - 监控API响应时间和吞吐量
 *
 * 安全考虑：
 * - 验证文件路径防止目录遍历攻击
 * - 验证用户输入的查询内容
 * - 限制文件大小防止内存溢出
 * - 实现请求速率限制防止滥用
 * - 记录所有API调用用于审计
 * - 使用HTTPS加密传输
 * - 实现文档访问控制
 *
 * 可靠性考虑：
 * - 处理异常并返回友好的错误消息
 * - 实现请求超时控制
 * - 支持请求重试机制
 * - 记录详细的错误日志
 * - 实现优雅的降级处理
 *
 * 扩展建议：
 * - 可以添加用户认证和授权
 * - 可以支持多语言查询
 * - 可以添加查询结果评分和反馈
 * - 可以实现文档版本管理
 * - 可以支持文档导出功能
 * - 可以添加查询分析和报告
 * - 可以实现查询结果缓存
 * - 可以支持批量文档处理
 *
 * 服务端口：8082
 */
@RestController
@RequestMapping("/api/document-analysis")
@RequiredArgsConstructor
public class DocumentAnalysisController {

    /** 注入文档分析服务 */
    private final DocumentAnalysisService documentAnalysisService;

    /** 注入文档缓存管理器 */
    private final DocumentCacheManager documentCacheManager;

    /**
     * 加载文档
     *
     * HTTP方法：POST
     * 端点：/api/document-analysis/load
     * 参数：
     * - path: 文档文件的完整路径
     * - documentId: 文档的唯一标识符
     *
     * 使用示例：
     * POST /api/document-analysis/load?path=/path/to/document.txt&documentId=doc1
     *
     * 功能：
     * - 从文件系统读取文档
     * - 分割成可管理的段落
     * - 存储到内存中供后续查询
     *
     * @param path 文档文件路径
     * @param documentId 文档唯一标识
     * @return 加载成功的提示信息
     */
    @PostMapping("/load")
    public String loadDocument(@RequestParam String path, @RequestParam String documentId) {
        try {
            documentAnalysisService.loadDocument(path, documentId);
            documentCacheManager.addDocument(documentId, documentId, 0);
            return "Document loaded successfully: " + documentId;
        } catch (Exception e) {
            return "Error loading document: " + e.getMessage();
        }
    }

    /**
     * 总结文档
     *
     * HTTP方法：GET
     * 端点：/api/document-analysis/summarize/{documentId}
     * 路径参数：documentId - 文档ID
     *
     * 使用示例：
     * GET /api/document-analysis/summarize/doc1
     *
     * 功能：
     * - 生成文档的摘要
     * - 提取关键信息
     * - 返回文档概览
     *
     * @param documentId 文档ID
     * @return 文档摘要
     */
    @GetMapping("/summarize/{documentId}")
    public String summarize(@PathVariable String documentId) {
        if (!documentCacheManager.documentExists(documentId)) {
            return "Document not found: " + documentId;
        }
        return documentAnalysisService.summarizeDocument(documentId);
    }

    /**
     * 查询文档
     *
     * HTTP方法：POST
     * 端点：/api/document-analysis/query/{documentId}
     * 路径参数：documentId - 文档ID
     * 查询参数：query - 查询关键词
     *
     * 使用示例：
     * POST /api/document-analysis/query/doc1?query=什么是AI
     *
     * 功能：
     * - 根据关键词搜索文档
     * - 找到相关的文本段落
     * - 返回最相关的结果
     *
     * @param documentId 文档ID
     * @param query 查询关键词
     * @return 查询结果
     */
    @PostMapping("/query/{documentId}")
    public DocumentQueryResponse query(@PathVariable String documentId, @RequestParam String query) {
        try {
            String result = documentAnalysisService.queryDocument(documentId, query);
            return DocumentQueryResponse.builder()
                    .result(result)
                    .documentId(documentId)
                    .timestamp(System.currentTimeMillis())
                    .status("success")
                    .build();
        } catch (Exception e) {
            return DocumentQueryResponse.builder()
                    .status("error")
                    .result(e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
    }

    /**
     * 获取文档列表
     *
     * HTTP方法：GET
     * 端点：/api/document-analysis/documents
     *
     * @return 所有已加载的文档列表
     */
    @GetMapping("/documents")
    public String getDocuments() {
        return "Loaded documents: " + documentCacheManager.getAllDocuments().size();
    }
}
