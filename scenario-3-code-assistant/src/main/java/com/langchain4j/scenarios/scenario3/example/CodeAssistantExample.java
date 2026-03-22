package com.langchain4j.scenarios.scenario3.example;

import com.langchain4j.scenarios.scenario3.service.CodeAssistantService;
import org.springframework.stereotype.Component;

/**
 * 代码助手实战示例 - 真实场景的代码审查演示
 *
 * 职责说明：
 * - 展示代码助手系统的实际应用场景
 * - 演示代码审查、分析、优化的完整流程
 * - 提供学习和测试的示例代码
 * - 展示不同类型的代码问题检测
 *
 * 架构设计：
 * - 使用 @Component 注解注册为 Spring Bean
 * - 依赖 CodeAssistantService 进行代码分析
 * - 提供多个独立的示例方法
 * - 支持示例的独立运行和组合运行
 * - 用于演示和测试目的
 *
 * 使用场景：
 * - 开发团队代码审查系统
 * - 自动审查代码质量
 * - 检测潜在的Bug和安全问题
 * - 提供重构建议
 * - 生成代码文档
 *
 * 真实场景说明：
 * 1. 代码审查：检查代码质量和规范
 * 2. Bug检测：发现潜在的逻辑错误
 * 3. 安全审查：检查安全漏洞
 * 4. 文档生成：自动生成代码注释
 * 5. 重构建议：提供代码改进方案
 *
 * 工作原理：
 * 1. 每个示例方法模拟一个真实的代码审查场景
 * 2. 提供具体的代码示例
 * 3. 执行代码分析操作
 * 4. 展示分析结果和建议
 * 5. 说明问题和改进方向
 * 6. 可以运行单个示例或所有示例
 *
 * 性能考虑：
 * - 每个示例包含1-2个分析操作
 * - 总执行时间通常 < 10 秒
 * - 可以并行运行多个示例
 * - 建议在测试环境中运行
 *
 * 安全考虑：
 * - 示例中使用的代码是虚拟的
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
public class CodeAssistantExample {

    private final CodeAssistantService codeAssistantService;

    public CodeAssistantExample(CodeAssistantService codeAssistantService) {
        this.codeAssistantService = codeAssistantService;
    }

    /**
     * 示例1：代码质量审查
     *
     * 场景：审查一个用户登录方法
     */
    public void codeQualityReviewExample() {
        System.out.println("=== 代码质量审查 ===");

        String code = """
            public boolean login(String username, String password) {
                User user = userDao.findByUsername(username);
                if (user.getPassword().equals(password)) {
                    return true;
                }
                return false;
            }
            """;

        System.out.println("代码:");
        System.out.println(code);

        String review = codeAssistantService.reviewCode(code, "java");
        System.out.println("审查结果:");
        System.out.println(review);

        // 问题分析
        System.out.println("\n问题分析:");
        System.out.println("1. 没有检查null值 - 可能导致NullPointerException");
        System.out.println("2. 密码明文比较 - 安全风险");
        System.out.println("3. 没有日志记录 - 难以追踪");
        System.out.println("4. 没有异常处理 - 容易崩溃");
    }

    /**
     * 示例2：Bug检测
     *
     * 场景：检测一个数据处理方法中的Bug
     */
    public void bugDetectionExample() {
        System.out.println("\n=== Bug检测 ===");

        String code = """
            public int calculateTotal(List<Integer> prices) {
                int total = 0;
                for (int i = 0; i <= prices.size(); i++) {
                    total += prices.get(i);
                }
                return total;
            }
            """;

        System.out.println("代码:");
        System.out.println(code);

        String bugs = codeAssistantService.detectBugs(code, "java");
        System.out.println("Bug检测结果:");
        System.out.println(bugs);

        System.out.println("\nBug分析:");
        System.out.println("1. 循环条件错误: i <= prices.size() 应该是 i < prices.size()");
        System.out.println("   - 会导致 ArrayIndexOutOfBoundsException");
        System.out.println("2. 没有检查空列表");
        System.out.println("3. 没有处理整数溢出");
    }

    /**
     * 示例3：安全审查
     *
     * 场景：检查SQL查询中的安全问题
     */
    public void securityReviewExample() {
        System.out.println("\n=== 安全审查 ===");

        String code = """
            public User getUserByUsername(String username) {
                String sql = "SELECT * FROM users WHERE username = '" + username + "'";
                return executeQuery(sql);
            }
            """;

        System.out.println("代码:");
        System.out.println(code);

        String review = codeAssistantService.reviewCode(code, "java");
        System.out.println("安全审查结果:");
        System.out.println(review);

        System.out.println("\n安全问题:");
        System.out.println("1. SQL注入漏洞 - 严重安全风险");
        System.out.println("   - 应该使用参数化查询");
        System.out.println("2. 没有输入验证");
        System.out.println("3. 没有日志记录");
    }

    /**
     * 示例4：代码文档生成
     *
     * 场景：为一个复杂的算法生成文档
     */
    public void documentationGenerationExample() {
        System.out.println("\n=== 代码文档生成 ===");

        String code = """
            public static int binarySearch(int[] arr, int target) {
                int left = 0, right = arr.length - 1;
                while (left <= right) {
                    int mid = left + (right - left) / 2;
                    if (arr[mid] == target) return mid;
                    if (arr[mid] < target) left = mid + 1;
                    else right = mid - 1;
                }
                return -1;
            }
            """;

        System.out.println("代码:");
        System.out.println(code);

        String documentation = codeAssistantService.generateDocumentation(code, "java");
        System.out.println("生成的文档:");
        System.out.println(documentation);
    }

    /**
     * 示例5：重构建议
     *
     * 场景：为一个冗长的方法提供重构建议
     */
    public void refactoringExample() {
        System.out.println("\n=== 重构建议 ===");

        String code = """
            public void processOrder(Order order) {
                if (order != null && order.getItems() != null && order.getItems().size() > 0) {
                    double total = 0;
                    for (Item item : order.getItems()) {
                        total += item.getPrice() * item.getQuantity();
                    }
                    if (total > 100) {
                        order.setDiscount(0.1);
                    } else if (total > 50) {
                        order.setDiscount(0.05);
                    }
                    order.setTotal(total);
                }
            }
            """;

        System.out.println("代码:");
        System.out.println(code);

        String suggestions = codeAssistantService.suggestRefactoring(code, "java");
        System.out.println("重构建议:");
        System.out.println(suggestions);

        System.out.println("\n重构方向:");
        System.out.println("1. 提取计算总价的逻辑为单独方法");
        System.out.println("2. 提取折扣计算为单独方法");
        System.out.println("3. 使用Stream API简化循环");
        System.out.println("4. 使用策略模式处理不同的折扣规则");
    }

    /**
     * 运行所有示例
     */
    public void runAllExamples() {
        codeQualityReviewExample();
        bugDetectionExample();
        securityReviewExample();
        documentationGenerationExample();
        refactoringExample();
    }
}
