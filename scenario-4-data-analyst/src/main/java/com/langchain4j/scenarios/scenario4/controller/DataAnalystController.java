package com.langchain4j.scenarios.scenario4.controller;

import com.langchain4j.scenarios.scenario4.model.QueryAnalysisResult;
import com.langchain4j.scenarios.scenario4.service.DataAnalystService;
import com.langchain4j.scenarios.scenario4.service.QueryExecutionTracker;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 数据分析 REST API 控制器 - 数据分析和SQL生成的HTTP接口层
 *
 * 职责说明：
 * - 提供数据分析和SQL生成的HTTP REST接口
 * - 支持数据查询、报表生成和查询优化
 * - 提供查询性能分析和优化建议
 * - 管理查询执行历史和统计
 * - 返回结构化的JSON响应
 *
 * 架构设计：
 * - 使用 @RestController 注解实现REST接口
 * - 使用 @RequestMapping 统一路由前缀
 * - 使用 @RequiredArgsConstructor 自动注入依赖
 * - 依赖 DataAnalystService 进行数据分析
 * - 依赖 QueryExecutionTracker 进行查询追踪
 * - 返回 QueryAnalysisResult 对象进行响应封装
 *
 * 使用场景：
 * - 自动生成SQL查询语句
 * - 分析数据集并提供洞察
 * - 生成数据报告和统计
 * - 优化数据库查询性能
 * - 电商数据分析平台
 * - 业务智能和数据仓库
 *
 * API端点说明：
 * - POST /api/data-analyst/generate-sql - 生成SQL查询
 * - POST /api/data-analyst/analyze - 分析数据
 * - POST /api/data-analyst/report - 生成报告
 * - POST /api/data-analyst/optimize - 优化查询
 * - POST /api/data-analyst/explain-query - 解释查询
 * - GET /api/data-analyst/stats - 获取统计
 *
 * 工作原理：
 * 1. 客户端发送HTTP请求到相应端点
 * 2. 控制器接收请求参数
 * 3. 调用 DataAnalystService 进行数据分析
 * 4. 记录查询到 QueryExecutionTracker
 * 5. 构建 QueryAnalysisResult 响应对象
 * 6. 返回JSON格式的响应给客户端
 * 7. 前端解析响应并显示结果
 *
 * 性能考虑：
 * - 每个请求都是独立处理，支持高并发
 * - 数据分析时间取决于数据大小和复杂度
 * - 建议使用负载均衡器分散请求
 * - 可以配置连接池提高性能
 * - 建议添加请求缓存减少重复计算
 * - 监控API响应时间和吞吐量
 *
 * 安全考虑：
 * - 验证用户输入的SQL和数据内容
 * - 限制查询长度防止内存溢出
 * - 实现请求速率限制防止滥用
 * - 记录所有API调用用于审计
 * - 使用HTTPS加密传输
 * - 实现查询访问控制
 * - 防止SQL注入攻击
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
 * - 可以支持更多数据源
 * - 可以添加查询结果缓存
 * - 可以实现查询结果导出
 * - 可以支持数据可视化
 * - 可以添加查询分析和报告
 * - 可以实现查询结果分页
 * - 可以支持批量数据分析
 *
 * 服务端口：8084
 */
@RestController
@RequestMapping("/api/data-analyst")
@RequiredArgsConstructor
public class DataAnalystController {

    /** 注入数据分析服务 */
    private final DataAnalystService dataAnalystService;

    /** 注入查询执行追踪器 */
    private final QueryExecutionTracker queryTracker;

    /**
     * 生成SQL查询语句
     *
     * HTTP方法：POST
     * 端点：/api/data-analyst/generate-sql
     * 参数：
     * - requirement: 查询需求描述
     * - schema: 数据库表结构信息
     *
     * 使用示例：
     * POST /api/data-analyst/generate-sql?requirement=查询所有用户&schema=users(id,name,email)
     *
     * 功能：
     * - 根据用户需求生成SQL语句
     * - 支持复杂查询
     * - 考虑数据库架构
     *
     * @param requirement 查询需求
     * @param schema 数据库架构
     * @return 生成的SQL语句
     */
    @PostMapping("/generate-sql")
    public QueryAnalysisResult generateSQL(@RequestParam String requirement, @RequestParam String schema) {
        try {
            String sql = dataAnalystService.generateSQL(requirement, schema);
            queryTracker.recordQuery(sql, "generate");
            return QueryAnalysisResult.builder()
                    .result(sql)
                    .queryType("SELECT")
                    .timestamp(System.currentTimeMillis())
                    .status("success")
                    .build();
        } catch (Exception e) {
            return QueryAnalysisResult.builder()
                    .status("error")
                    .result(e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
    }

    /**
     * 分析数据
     *
     * HTTP方法：POST
     * 端点：/api/data-analyst/analyze
     * 参数：
     * - data: 要分析的数据
     * - question: 用户的问题
     *
     * 使用示例：
     * POST /api/data-analyst/analyze?data=1,2,3,4,5&question=平均值是多少
     *
     * 功能：
     * - 对数据进行统计分析
     * - 回答关于数据的问题
     * - 提供数据洞察
     *
     * @param data 要分析的数据
     * @param question 用户的问题
     * @return 分析结果
     */
    @PostMapping("/analyze")
    public String analyzeData(@RequestParam String data, @RequestParam String question) {
        return dataAnalystService.analyzeData(data, question);
    }

    /**
     * 生成报告
     *
     * HTTP方法：POST
     * 端点：/api/data-analyst/report
     * 参数：
     * - data: 报告数据源
     * - reportType: 报告类型（默认：summary）
     *
     * 使用示例：
     * POST /api/data-analyst/report?data=...&reportType=summary
     *
     * 功能：
     * - 根据数据生成各类报告
     * - 支持多种报告类型
     * - 包含统计数据和趋势分析
     *
     * @param data 数据源
     * @param reportType 报告类型
     * @return 生成的报告
     */
    @PostMapping("/report")
    public String generateReport(@RequestParam String data, @RequestParam(defaultValue = "summary") String reportType) {
        return dataAnalystService.generateReport(data, reportType);
    }

    /**
     * 优化查询建议
     *
     * HTTP方法：POST
     * 端点：/api/data-analyst/optimize
     * 参数：query - SQL查询语句
     *
     * 使用示例：
     * POST /api/data-analyst/optimize?query=SELECT%20*%20FROM%20users
     *
     * 功能：
     * - 分析SQL查询性能
     * - 提供优化建议
     * - 改进查询效率
     *
     * @param query SQL查询语句
     * @return 优化建议
     */
    @PostMapping("/optimize")
    public String suggestOptimization(@RequestParam String query) {
        queryTracker.recordQuery(query, "optimize");
        return dataAnalystService.suggestOptimization(query);
    }

    /**
     * 解释查询
     *
     * HTTP方法：POST
     * 端点：/api/data-analyst/explain-query
     * 参数：query - SQL查询语句
     *
     * 使用示例：
     * POST /api/data-analyst/explain-query?query=SELECT%20*%20FROM%20users%20WHERE%20age%20>%2018
     *
     * 功能：
     * - 解释SQL查询的执行逻辑
     * - 说明查询步骤
     * - 帮助理解复杂查询
     *
     * @param query SQL查询语句
     * @return 查询解释
     */
    @PostMapping("/explain-query")
    public String explainQuery(@RequestParam String query) {
        return dataAnalystService.explainQuery(query);
    }

    /**
     * 获取查询统计
     *
     * HTTP方法：GET
     * 端点：/api/data-analyst/stats
     *
     * @return 查询统计信息
     */
    @GetMapping("/stats")
    public String getStats() {
        return "Total queries: " + queryTracker.getTotalQueries();
    }
}
