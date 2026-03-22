package com.langchain4j.scenarios.scenario4.example;

import com.langchain4j.scenarios.scenario4.service.DataAnalystService;
import org.springframework.stereotype.Component;

/**
 * 数据分析实战示例 - 真实场景的数据分析演示
 *
 * 职责说明：
 * - 展示数据分析系统的实际应用场景
 * - 演示SQL生成、数据分析、报告生成的完整流程
 * - 提供学习和测试的示例代码
 * - 展示不同类型的数据分析场景
 *
 * 架构设计：
 * - 使用 @Component 注解注册为 Spring Bean
 * - 依赖 DataAnalystService 进行数据分析
 * - 提供多个独立的示例方法
 * - 支持示例的独立运行和组合运行
 * - 用于演示和测试目的
 *
 * 使用场景：
 * - 电商数据分析平台
 * - 分析销售数据、用户行为
 * - 自动生成SQL查询
 * - 优化数据库查询性能
 * - 生成数据报告
 *
 * 真实场景说明：
 * 1. 销售分析：查询销售额、增长率
 * 2. 用户分析：分析用户行为、留存率
 * 3. 库存管理：查询库存状态、预警
 * 4. 性能优化：优化慢查询
 *
 * 工作原理：
 * 1. 每个示例方法模拟一个真实的数据分析场景
 * 2. 定义数据分析需求
 * 3. 执行SQL生成和数据分析操作
 * 4. 展示分析结果和建议
 * 5. 说明优化方向
 * 6. 可以运行单个示例或所有示例
 *
 * 性能考虑：
 * - 每个示例包含2-4个分析操作
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
public class DataAnalystExample {

    private final DataAnalystService dataAnalystService;

    public DataAnalystExample(DataAnalystService dataAnalystService) {
        this.dataAnalystService = dataAnalystService;
    }

    /**
     * 示例1：销售数据分析
     *
     * 业务需求：
     * - 查询本月销售额
     * - 对比上月增长率
     * - 按地区统计销售
     */
    public void salesAnalysisExample() {
        System.out.println("=== 销售数据分析 ===");

        // 需求1：生成本月销售额查询
        String requirement1 = "查询2024年3月的总销售额";
        String schema = "orders(id, order_date, amount, region), products(id, name, price)";

        String sql1 = dataAnalystService.generateSQL(requirement1, schema);
        System.out.println("需求: " + requirement1);
        System.out.println("生成的SQL: " + sql1);

        // 需求2：分析销售数据
        String salesData = "3月销售额: 1000000, 2月销售额: 800000, 1月销售额: 900000";
        String question = "计算3月相比2月的增长率";

        String analysis = dataAnalystService.analyzeData(salesData, question);
        System.out.println("\n数据: " + salesData);
        System.out.println("问题: " + question);
        System.out.println("分析结果: " + analysis);

        // 需求3：生成销售报告
        String report = dataAnalystService.generateReport(salesData, "monthly");
        System.out.println("\n月度销售报告:");
        System.out.println(report);
    }

    /**
     * 示例2：用户行为分析
     *
     * 业务需求：
     * - 分析用户活跃度
     * - 计算用户留存率
     * - 识别高价值用户
     */
    public void userBehaviorAnalysisExample() {
        System.out.println("\n=== 用户行为分析 ===");

        // 需求1：查询活跃用户
        String requirement = "查询过去30天内有购买行为的用户数";
        String schema = "users(id, created_at), orders(user_id, order_date, amount)";

        String sql = dataAnalystService.generateSQL(requirement, schema);
        System.out.println("需求: " + requirement);
        System.out.println("生成的SQL: " + sql);

        // 需求2：分析用户留存
        String userData = "新用户: 1000, 7天留存: 650, 30天留存: 420, 90天留存: 280";
        String retentionQuestion = "计算7天和30天的留存率";

        String retentionAnalysis = dataAnalystService.analyzeData(userData, retentionQuestion);
        System.out.println("\n用户数据: " + userData);
        System.out.println("问题: " + retentionQuestion);
        System.out.println("分析结果: " + retentionAnalysis);

        // 需求3：生成用户报告
        String userReport = dataAnalystService.generateReport(userData, "user_retention");
        System.out.println("\n用户留存报告:");
        System.out.println(userReport);
    }

    /**
     * 示例3：库存管理分析
     *
     * 业务需求：
     * - 查询低库存商品
     * - 预测库存不足
     * - 优化库存配置
     */
    public void inventoryAnalysisExample() {
        System.out.println("\n=== 库存管理分析 ===");

        // 需求1：查询低库存商品
        String requirement = "查询库存少于100件的商品";
        String schema = "products(id, name, stock), sales(product_id, quantity, date)";

        String sql = dataAnalystService.generateSQL(requirement, schema);
        System.out.println("需求: " + requirement);
        System.out.println("生成的SQL: " + sql);

        // 需求2：分析库存趋势
        String inventoryData = "商品A: 当前库存50, 日均销售10, 商品B: 当前库存200, 日均销售5";
        String inventoryQuestion = "预测各商品的库存不足时间";

        String inventoryAnalysis = dataAnalystService.analyzeData(inventoryData, inventoryQuestion);
        System.out.println("\n库存数据: " + inventoryData);
        System.out.println("问题: " + inventoryQuestion);
        System.out.println("分析结果: " + inventoryAnalysis);
    }

    /**
     * 示例4：查询性能优化
     *
     * 业务需求：
     * - 识别慢查询
     * - 提供优化建议
     * - 改进查询性能
     */
    public void queryOptimizationExample() {
        System.out.println("\n=== 查询性能优化 ===");

        // 原始查询（低效）
        String slowQuery = """
            SELECT o.*, u.*, p.*
            FROM orders o
            JOIN users u ON o.user_id = u.id
            JOIN products p ON o.product_id = p.id
            WHERE o.order_date > '2024-01-01'
            AND u.region = 'Beijing'
            """;

        System.out.println("原始查询:");
        System.out.println(slowQuery);

        // 获取优化建议
        String optimization = dataAnalystService.suggestOptimization(slowQuery);
        System.out.println("\n优化建议:");
        System.out.println(optimization);

        // 解释查询
        String explanation = dataAnalystService.explainQuery(slowQuery);
        System.out.println("\n查询解释:");
        System.out.println(explanation);

        // 优化后的查询
        String optimizedQuery = """
            SELECT o.id, o.order_date, o.amount, u.name, p.name
            FROM orders o
            INNER JOIN users u ON o.user_id = u.id
            INNER JOIN products p ON o.product_id = p.id
            WHERE o.order_date > '2024-01-01'
            AND u.region = 'Beijing'
            """;

        System.out.println("\n优化后的查询:");
        System.out.println(optimizedQuery);

        System.out.println("\n优化要点:");
        System.out.println("1. 只选择需要的列，避免SELECT *");
        System.out.println("2. 使用INNER JOIN替代LEFT JOIN（如果不需要外连接）");
        System.out.println("3. 在WHERE条件中的列上建立索引");
        System.out.println("4. 考虑分区表处理大数据量");
    }

    /**
     * 运行所有示例
     */
    public void runAllExamples() {
        salesAnalysisExample();
        userBehaviorAnalysisExample();
        inventoryAnalysisExample();
        queryOptimizationExample();
    }
}
