package com.langchain4j.scenarios.scenario8.controller;

import com.langchain4j.scenarios.common.response.ApiResponse;
import com.langchain4j.scenarios.scenario8.model.IngestRequest;
import com.langchain4j.scenarios.scenario8.model.RagResponse;
import com.langchain4j.scenarios.scenario8.model.SearchRequest;
import com.langchain4j.scenarios.scenario8.service.VectorRagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * VectorController - 向量数据库 REST API
 *
 * 端点设计原则：
 * - /ingest     文档向量化入库（对应 RAG 的 Indexing 阶段）
 * - /search     纯向量搜索（不调用 LLM，用于理解向量检索本身）
 * - /ask        完整 RAG 问答（向量检索 + LLM 生成）
 * - /status     查看当前使用的向量存储类型和统计信息
 * - /examples   使用示例
 *
 * 端口：8088
 */
@Slf4j
@RestController
@RequestMapping("/api/vector")
@RequiredArgsConstructor
public class VectorController {

    private final VectorRagService vectorRagService;

    /**
     * 文档入库（向量化存储）
     *
     * POST /api/vector/ingest
     * Body: { "content": "文档内容", "title": "文档标题" }
     */
    @PostMapping("/ingest")
    public ApiResponse<Map<String, Object>> ingest(@RequestBody IngestRequest request) {
        try {
            int segmentCount = vectorRagService.ingestDocument(request.content(), request.title());
            return ApiResponse.success(Map.of(
                    "title", request.title(),
                    "segmentsStored", segmentCount,
                    "storeType", vectorRagService.getStoreType(),
                    "totalSegments", vectorRagService.getTotalSegmentsStored()
            ), "文档向量化入库成功");
        } catch (Exception e) {
            log.error("Ingest failed", e);
            return ApiResponse.error("文档入库失败: " + e.getMessage());
        }
    }

    /**
     * 纯向量相似度搜索（不调用 LLM）
     *
     * POST /api/vector/search
     * Body: { "query": "查询内容" }
     */
    @PostMapping("/search")
    public ApiResponse<List<VectorRagService.SearchResult>> search(@RequestBody SearchRequest request) {
        try {
            List<VectorRagService.SearchResult> results = vectorRagService.search(request.query());
            return ApiResponse.success(results,
                    String.format("找到 %d 个相关段落（使用 %s）", results.size(), vectorRagService.getStoreType()));
        } catch (Exception e) {
            log.error("Search failed", e);
            return ApiResponse.error("搜索失败: " + e.getMessage());
        }
    }

    /**
     * 完整 RAG 问答（向量检索 + LLM 生成）
     *
     * POST /api/vector/ask
     * Body: { "query": "你的问题" }
     */
    @PostMapping("/ask")
    public ApiResponse<RagResponse> ask(@RequestBody SearchRequest request) {
        try {
            long start = System.currentTimeMillis();
            String answer = vectorRagService.ragAnswer(request.query());
            long elapsed = System.currentTimeMillis() - start;

            return ApiResponse.success(new RagResponse(
                    request.query(),
                    answer,
                    vectorRagService.getStoreType(),
                    elapsed
            ));
        } catch (Exception e) {
            log.error("RAG answer failed", e);
            return ApiResponse.error("问答失败: " + e.getMessage());
        }
    }

    /**
     * 状态信息（当前向量存储类型、文档统计）
     *
     * GET /api/vector/status
     */
    @GetMapping("/status")
    public ApiResponse<Map<String, Object>> status() {
        return ApiResponse.success(Map.of(
                "activeStoreType", vectorRagService.getStoreType(),
                "totalDocuments", vectorRagService.getTotalDocumentsLoaded(),
                "totalSegments", vectorRagService.getTotalSegmentsStored(),
                "embeddingModel", "AllMiniLmL6V2 (384维, 本地运行)",
                "port", 8088
        ));
    }

    /**
     * 使用示例（帮助开发者快速上手）
     *
     * GET /api/vector/examples
     */
    @GetMapping("/examples")
    public ApiResponse<Map<String, Object>> examples() {
        return ApiResponse.success(Map.of(
                "step1_ingest", Map.of(
                        "url", "POST /api/vector/ingest",
                        "body", Map.of(
                                "content", "Java 是一种面向对象的编程语言，由 Sun 公司开发...",
                                "title", "Java 编程语言简介"
                        )
                ),
                "step2_search", Map.of(
                        "url", "POST /api/vector/search",
                        "body", Map.of("query", "面向对象编程"),
                        "note", "向量搜索会找到含义相近的段落，即使词语不完全相同"
                ),
                "step3_ask", Map.of(
                        "url", "POST /api/vector/ask",
                        "body", Map.of("query", "Java 的主要特点是什么？"),
                        "note", "完整 RAG：向量检索相关上下文 + LLM 基于上下文回答"
                ),
                "switch_store", Map.of(
                        "description", "修改 application.properties 中的 vector.store.type",
                        "options", List.of("inmemory（默认）", "milvus（需Docker）", "qdrant（需Docker）", "weaviate（需Docker）")
                )
        ));
    }
}
