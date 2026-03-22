package com.langchain4j.scenarios.scenario6.controller;

import com.langchain4j.scenarios.scenario6.rag.AgentRagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Agent 文档库管理控制器
 *
 * 端点说明：
 * - POST /api/agent/documents/upload-text  - 上传纯文本文档
 * - GET  /api/agent/documents              - 查看文档库状态
 * - POST /api/agent/documents/search       - 直接搜索文档库（调试用）
 *
 * 设计说明：
 * - 单独的 Controller 保持 AgentController 的简洁性
 * - 路径前缀 /api/agent/documents 与 Agent 主路径统一
 */
@Slf4j
@RestController
@RequestMapping("/api/agent/documents")
@RequiredArgsConstructor
public class AgentDocumentController {

    private final AgentRagService agentRagService;

    /**
     * 上传纯文本文档到向量库
     *
     * 请求体：
     * {
     *   "documentId": "doc001",
     *   "title": "公司产品手册",
     *   "content": "这是文档内容..."
     * }
     *
     * 使用场景：
     * - 将知识库文档加入向量库
     * - 让 Agent 能够回答基于文档的问题
     * - 支持多文档管理（不同 documentId 区分）
     */
    @PostMapping("/upload-text")
    public Map<String, Object> uploadText(@RequestBody Map<String, String> request) {
        String documentId = request.get("documentId");
        String title = request.getOrDefault("title", documentId);
        String content = request.get("content");

        if (content == null || content.isBlank()) {
            return Map.of("status", "error", "message", "content 不能为空");
        }
        if (documentId == null || documentId.isBlank()) {
            return Map.of("status", "error", "message", "documentId 不能为空");
        }

        try {
            int segmentCount = agentRagService.loadDocument(content, documentId, title);
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("documentId", documentId);
            result.put("title", title);
            result.put("segmentCount", segmentCount);
            result.put("message", "文档已成功加载到向量库，共 " + segmentCount + " 个段落");
            return result;
        } catch (Exception e) {
            log.error("Failed to upload document: {}", documentId, e);
            return Map.of("status", "error", "message", "文档加载失败: " + e.getMessage());
        }
    }

    /**
     * 查看文档库状态
     *
     * 响应：
     * {
     *   "documentCount": 3,
     *   "isEmpty": false,
     *   "documents": [{ "documentId": "...", "title": "...", "segmentCount": 10 }]
     * }
     */
    @GetMapping
    public Map<String, Object> listDocuments() {
        Map<String, Object> result = new HashMap<>();
        result.put("documentCount", agentRagService.getDocumentCount());
        result.put("isEmpty", agentRagService.isEmpty());
        result.put("documents", agentRagService.listDocuments());
        return result;
    }

    /**
     * 直接搜索文档库（调试和验证用）
     *
     * 请求体：{ "query": "产品价格是多少", "maxResults": 3 }
     *
     * 使用场景：
     * - 验证文档是否正确入库
     * - 调试检索效果
     * - 不通过 Agent，直接测试 RAG 能力
     */
    @PostMapping("/search")
    public Map<String, Object> search(@RequestBody Map<String, Object> request) {
        String query = (String) request.get("query");
        int maxResults = request.containsKey("maxResults") ?
                ((Number) request.get("maxResults")).intValue() : 3;

        if (query == null || query.isBlank()) {
            return Map.of("status", "error", "message", "query 不能为空");
        }

        var segments = agentRagService.search(query, maxResults);
        return Map.of(
                "status", "success",
                "query", query,
                "resultsCount", segments.size(),
                "segments", segments
        );
    }
}
