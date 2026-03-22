package com.langchain4j.scenarios.scenario3.controller;

import com.langchain4j.scenarios.scenario3.model.CodeReviewRequest;
import com.langchain4j.scenarios.scenario3.model.CodeReviewResult;
import com.langchain4j.scenarios.scenario3.service.CodeAssistantService;
import com.langchain4j.scenarios.scenario3.service.CodeReviewHistoryManager;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 代码助手 REST API 控制器 - 代码审查和分析的HTTP接口层
 *
 * 职责说明：
 * - 提供代码审查、分析和优化的HTTP REST接口
 * - 支持多种编程语言（Java、Python、JavaScript等）
 * - 提供代码文档生成和重构建议功能
 * - 管理代码审查历史和统计
 * - 返回结构化的JSON响应
 *
 * 架构设计：
 * - 使用 @RestController 注解实现REST接口
 * - 使用 @RequestMapping 统一路由前缀
 * - 使用 @RequiredArgsConstructor 自动注入依赖
 * - 依赖 CodeAssistantService 进行代码分析
 * - 依赖 CodeReviewHistoryManager 进行历史管理
 * - 返回 CodeReviewResult 对象进行响应封装
 *
 * 使用场景：
 * - 代码质量审查和规范检查
 * - Bug检测和修复建议
 * - 代码文档自动生成
 * - 代码重构指导和优化建议
 * - 安全漏洞检测
 * - 性能问题分析
 *
 * API端点说明：
 * - POST /api/code-assistant/review - 代码审查
 * - POST /api/code-assistant/explain - 代码解释
 * - POST /api/code-assistant/document - 生成文档
 * - POST /api/code-assistant/refactor - 重构建议
 * - POST /api/code-assistant/bugs - Bug检测
 * - GET /api/code-assistant/stats - 获取统计
 *
 * 工作原理：
 * 1. 客户端发送HTTP请求到相应端点
 * 2. 控制器接收代码和语言参数
 * 3. 调用 CodeAssistantService 进行代码分析
 * 4. 记录审查历史到 CodeReviewHistoryManager
 * 5. 构建 CodeReviewResult 响应对象
 * 6. 返回JSON格式的响应给客户端
 * 7. 前端解析响应并显示结果
 *
 * 性能考虑：
 * - 每个请求都是独立处理，支持高并发
 * - 代码分析时间取决于代码大小和复杂度
 * - 建议使用负载均衡器分散请求
 * - 可以配置连接池提高性能
 * - 建议添加请求缓存减少重复计算
 * - 监控API响应时间和吞吐量
 *
 * 安全考虑：
 * - 验证用户输入的代码内容
 * - 限制代码长度防止内存溢出
 * - 实现请求速率限制防止滥用
 * - 记录所有API调用用于审计
 * - 使用HTTPS加密传输
 * - 实现代码访问控制
 * - 防止代码注入攻击
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
 * - 可以支持更多编程语言
 * - 可以添加代码审查结果评分
 * - 可以实现代码审查规则定制
 * - 可以支持代码审查报告导出
 * - 可以添加代码审查趋势分析
 * - 可以实现代码审查结果缓存
 * - 可以支持批量代码审查
 *
 * 服务端口：8083
 */
@RestController
@RequestMapping("/api/code-assistant")
@RequiredArgsConstructor
public class CodeAssistantController {

    /** 注入代码助手服务 */
    private final CodeAssistantService codeAssistantService;

    /** 注入审查历史管理器 */
    private final CodeReviewHistoryManager reviewHistoryManager;

    /**
     * 代码审查
     *
     * HTTP方法：POST
     * 端点：/api/code-assistant/review
     * 参数：
     * - code: 要审查的代码
     * - language: 编程语言（默认：java）
     *
     * 使用示例：
     * POST /api/code-assistant/review?code=public%20void%20test()%20{}&language=java
     *
     * 功能：
     * - 分析代码质量
     * - 检测潜在问题
     * - 提供改进建议
     *
     * @param code 要审查的代码
     * @param language 编程语言
     * @return 审查结果
     */
    @PostMapping("/review")
    public CodeReviewResult reviewCode(@RequestParam String code,
                                       @RequestParam(defaultValue = "java") String language) {
        try {
            String result = codeAssistantService.reviewCode(code, language);
            reviewHistoryManager.recordReview(language, 1);
            return CodeReviewResult.builder()
                    .result(result)
                    .language(language)
                    .issueCount(1)
                    .severity("medium")
                    .timestamp(System.currentTimeMillis())
                    .status("success")
                    .build();
        } catch (Exception e) {
            return CodeReviewResult.builder()
                    .status("error")
                    .result(e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
    }

    /**
     * 代码解释
     *
     * HTTP方法：POST
     * 端点：/api/code-assistant/explain
     * 参数：
     * - code: 要解释的代码
     * - language: 编程语言（默认：java）
     *
     * 使用示例：
     * POST /api/code-assistant/explain?code=int%20sum%20=%200;&language=java
     *
     * 功能：
     * - 解释代码的功能和逻辑
     * - 说明关键算法
     * - 帮助理解复杂代码
     *
     * @param code 要解释的代码
     * @param language 编程语言
     * @return 代码解释
     */
    @PostMapping("/explain")
    public String explainCode(@RequestParam String code,
                             @RequestParam(defaultValue = "java") String language) {
        return codeAssistantService.explainCode(code, language);
    }

    /**
     * 生成代码文档
     *
     * HTTP方法：POST
     * 端点：/api/code-assistant/document
     * 参数：
     * - code: 要生成文档的代码
     * - language: 编程语言（默认：java）
     *
     * 使用示例：
     * POST /api/code-assistant/document?code=public%20int%20add(int%20a,%20int%20b)%20{}&language=java
     *
     * 功能：
     * - 自动生成代码注释和文档
     * - 生成函数签名说明
     * - 生成参数和返回值说明
     *
     * @param code 要生成文档的代码
     * @param language 编程语言
     * @return 生成的文档
     */
    @PostMapping("/document")
    public String generateDocumentation(@RequestParam String code,
                                       @RequestParam(defaultValue = "java") String language) {
        return codeAssistantService.generateDocumentation(code, language);
    }

    /**
     * 重构建议
     *
     * HTTP方法：POST
     * 端点：/api/code-assistant/refactor
     * 参数：
     * - code: 要重构的代码
     * - language: 编程语言（默认：java）
     *
     * 使用示例：
     * POST /api/code-assistant/refactor?code=...&language=java
     *
     * 功能：
     * - 提供代码重构方案
     * - 改进代码结构
     * - 提高代码可维护性
     *
     * @param code 要重构的代码
     * @param language 编程语言
     * @return 重构建议
     */
    @PostMapping("/refactor")
    public String suggestRefactoring(@RequestParam String code,
                                    @RequestParam(defaultValue = "java") String language) {
        return codeAssistantService.suggestRefactoring(code, language);
    }

    /**
     * 检测Bug
     *
     * HTTP方法：POST
     * 端点：/api/code-assistant/bugs
     * 参数：
     * - code: 要检测的代码
     * - language: 编程语言（默认：java）
     *
     * 使用示例：
     * POST /api/code-assistant/bugs?code=...&language=java
     *
     * 功能：
     * - 检测代码中的潜在Bug
     * - 评估问题严重程度
     * - 提供修复建议
     *
     * @param code 要检测的代码
     * @param language 编程语言
     * @return Bug检测结果
     */
    @PostMapping("/bugs")
    public String detectBugs(@RequestParam String code,
                            @RequestParam(defaultValue = "java") String language) {
        return codeAssistantService.detectBugs(code, language);
    }

    /**
     * 获取审查统计
     *
     * HTTP方法：GET
     * 端点：/api/code-assistant/stats
     *
     * @return 审查统计信息
     */
    @GetMapping("/stats")
    public String getStats() {
        return "Total reviews: " + reviewHistoryManager.getTotalReviews();
    }
}
