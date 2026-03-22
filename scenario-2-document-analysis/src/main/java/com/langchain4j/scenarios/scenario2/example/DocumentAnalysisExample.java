package com.langchain4j.scenarios.scenario2.example;

import com.langchain4j.scenarios.scenario2.service.DocumentAnalysisService;
import org.springframework.stereotype.Component;

/**
 * 文档分析实战示例 - 真实场景的RAG系统演示
 *
 * 职责说明：
 * - 展示文档分析系统的实际应用场景
 * - 演示文档加载、查询、总结的完整流程
 * - 提供学习和测试的示例代码
 * - 展示不同类型的文档处理场景
 *
 * 架构设计：
 * - 使用 @Component 注解注册为 Spring Bean
 * - 依赖 DocumentAnalysisService 进行文档处理
 * - 提供多个独立的示例方法
 * - 支持示例的独立运行和组合运行
 * - 用于演示和测试目的
 *
 * 使用场景：
 * - 企业知识库系统
 * - 处理大量企业文档（合同、政策、手册等）
 * - 快速检索和分析文档内容
 * - 自动生成文档摘要和问答
 *
 * 真实场景说明：
 * 1. 合同分析：快速查找合同条款（付款条件、违约责任等）
 * 2. 政策查询：员工查询公司政策（假期、薪酬、福利等）
 * 3. 技术文档：开发者查询API文档（认证、错误处理等）
 * 4. 法律文件：律师查询法律条款（管辖权、保密条款等）
 *
 * 工作原理：
 * 1. 每个示例方法模拟一个真实的文档处理场景
 * 2. 加载相应的文档文件
 * 3. 执行多个查询操作
 * 4. 生成文档摘要
 * 5. 展示完整的处理过程
 * 6. 可以运行单个示例或所有示例
 *
 * 性能考虑：
 * - 每个示例包含2-4个查询操作
 * - 总执行时间通常 < 10 秒
 * - 可以并行运行多个示例
 * - 建议在测试环境中运行
 *
 * 安全考虑：
 * - 示例中使用的数据是虚拟的
 * - 不包含真实的用户信息
 * - 可以安全地在任何环境中运行
 * - 不会修改生产数据
 *
 * 扩展建议：
 * - 可以添加更多真实场景示例
 * - 可以实现自动化测试用例
 * - 可以添加性能基准测试
 * - 可以实现场景录制和回放
 * - 可以支持自定义场景脚本
 * - 可以添加场景的参数化
 */
@Component
public class DocumentAnalysisExample {

    private final DocumentAnalysisService documentAnalysisService;

    public DocumentAnalysisExample(DocumentAnalysisService documentAnalysisService) {
        this.documentAnalysisService = documentAnalysisService;
    }

    /**
     * 示例1：合同分析场景
     *
     * 业务流程：
     * 1. 上传合同文档
     * 2. 查询特定条款（如付款条件、违约责任）
     * 3. 生成合同摘要
     */
    public void contractAnalysisExample() {
        System.out.println("=== 合同分析场景 ===");

        // 加载合同文档
        String contractPath = "/documents/contract_2024.txt";
        String contractId = "contract_001";

        try {
            documentAnalysisService.loadDocument(contractPath, contractId);
            System.out.println("✓ 合同已加载: " + contractId);

            // 查询付款条件
            String paymentQuery = documentAnalysisService.queryDocument(contractId, "付款条件");
            System.out.println("\n查询: 付款条件");
            System.out.println("结果: " + paymentQuery);

            // 查询违约责任
            String liabilityQuery = documentAnalysisService.queryDocument(contractId, "违约责任");
            System.out.println("\n查询: 违约责任");
            System.out.println("结果: " + liabilityQuery);

            // 生成合同摘要
            String summary = documentAnalysisService.summarizeDocument(contractId);
            System.out.println("\n合同摘要: " + summary);
        } catch (Exception e) {
            System.out.println("错误: " + e.getMessage());
        }
    }

    /**
     * 示例2：员工手册查询场景
     *
     * 业务流程：
     * 1. 上传员工手册
     * 2. 员工查询假期政策、薪酬标准等
     * 3. 快速获得答案
     */
    public void employeeHandbookExample() {
        System.out.println("\n=== 员工手册查询场景 ===");

        String handbookPath = "/documents/employee_handbook.txt";
        String handbookId = "handbook_001";

        try {
            documentAnalysisService.loadDocument(handbookPath, handbookId);
            System.out.println("✓ 员工手册已加载");

            // 查询假期政策
            String vacationQuery = documentAnalysisService.queryDocument(handbookId, "年假");
            System.out.println("\n查询: 年假政策");
            System.out.println("结果: " + vacationQuery);

            // 查询薪酬标准
            String salaryQuery = documentAnalysisService.queryDocument(handbookId, "薪酬");
            System.out.println("\n查询: 薪酬标准");
            System.out.println("结果: " + salaryQuery);

            // 查询福利待遇
            String benefitQuery = documentAnalysisService.queryDocument(handbookId, "福利");
            System.out.println("\n查询: 福利待遇");
            System.out.println("结果: " + benefitQuery);
        } catch (Exception e) {
            System.out.println("错误: " + e.getMessage());
        }
    }

    /**
     * 示例3：技术文档查询场景
     *
     * 业务流程：
     * 1. 上传API文档
     * 2. 开发者查询API使用方法
     * 3. 快速获得代码示例
     */
    public void technicalDocumentExample() {
        System.out.println("\n=== 技术文档查询场景 ===");

        String apiDocPath = "/documents/api_documentation.txt";
        String apiDocId = "api_doc_001";

        try {
            documentAnalysisService.loadDocument(apiDocPath, apiDocId);
            System.out.println("✓ API文档已加载");

            // 查询认证方法
            String authQuery = documentAnalysisService.queryDocument(apiDocId, "认证");
            System.out.println("\n查询: 如何进行API认证？");
            System.out.println("结果: " + authQuery);

            // 查询错误处理
            String errorQuery = documentAnalysisService.queryDocument(apiDocId, "错误处理");
            System.out.println("\n查询: 错误处理方法");
            System.out.println("结果: " + errorQuery);

            // 生成文档摘要
            String summary = documentAnalysisService.summarizeDocument(apiDocId);
            System.out.println("\n文档摘要: " + summary);
        } catch (Exception e) {
            System.out.println("错误: " + e.getMessage());
        }
    }

    /**
     * 示例4：法律文件分析场景
     *
     * 业务流程：
     * 1. 上传法律文件
     * 2. 律师查询特定法律条款
     * 3. 快速定位相关内容
     */
    public void legalDocumentExample() {
        System.out.println("\n=== 法律文件分析场景 ===");

        String legalDocPath = "/documents/legal_agreement.txt";
        String legalDocId = "legal_doc_001";

        try {
            documentAnalysisService.loadDocument(legalDocPath, legalDocId);
            System.out.println("✓ 法律文件已加载");

            // 查询管辖权
            String jurisdictionQuery = documentAnalysisService.queryDocument(legalDocId, "管辖权");
            System.out.println("\n查询: 管辖权条款");
            System.out.println("结果: " + jurisdictionQuery);

            // 查询保密条款
            String confidentialityQuery = documentAnalysisService.queryDocument(legalDocId, "保密");
            System.out.println("\n查询: 保密条款");
            System.out.println("结果: " + confidentialityQuery);
        } catch (Exception e) {
            System.out.println("错误: " + e.getMessage());
        }
    }

    /**
     * 运行所有示例
     */
    public void runAllExamples() {
        contractAnalysisExample();
        employeeHandbookExample();
        technicalDocumentExample();
        legalDocumentExample();
    }
}
