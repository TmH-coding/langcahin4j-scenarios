package com.langchain4j.scenarios.scenario1.example;

import com.langchain4j.scenarios.scenario1.service.CustomerServiceAI;
import org.springframework.stereotype.Component;

/**
 * 客服系统实战示例 - 真实场景的多轮对话演示
 *
 * 职责说明：
 * - 展示客服系统的实际应用场景
 * - 演示多轮对话的完整流程
 * - 提供学习和测试的示例代码
 * - 展示不同类型的客服问题处理
 *
 * 架构设计：
 * - 使用 @Component 注解注册为 Spring Bean
 * - 依赖 CustomerServiceAI 进行对话处理
 * - 提供多个独立的示例方法
 * - 支持示例的独立运行和组合运行
 * - 用于演示和测试目的
 *
 * 使用场景：
 * - 电商平台客服系统
 * - 处理用户订单咨询、退货、投诉等问题
 * - 支持多轮对话，理解用户意图
 * - 提供快速、准确的解决方案
 *
 * 真实场景说明：
 * 1. 订单查询：用户询问订单状态、物流信息
 * 2. 退货处理：用户申请退货、退款
 * 3. 产品咨询：用户询问产品规格、库存
 * 4. 投诉处理：用户投诉产品质量、服务态度
 *
 * 工作原理：
 * 1. 每个示例方法模拟一个真实的客服场景
 * 2. 用户发送多条消息到AI客服
 * 3. AI根据对话历史生成智能回复
 * 4. 展示完整的对话过程
 * 5. 可以运行单个示例或所有示例
 *
 * 性能考虑：
 * - 每个示例包含2-4轮对话
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
public class CustomerServiceExample {

    private final CustomerServiceAI customerServiceAI;

    public CustomerServiceExample(CustomerServiceAI customerServiceAI) {
        this.customerServiceAI = customerServiceAI;
    }

    /**
     * 示例1：订单查询场景
     *
     * 用户流程：
     * 1. 用户：我的订单什么时候到？
     * 2. AI：请提供订单号
     * 3. 用户：订单号是 12345678
     * 4. AI：您的订单已发货，预计3天内送达
     */
    public void orderQueryExample() {
        System.out.println("=== 订单查询场景 ===");

        // 第一轮对话
        String response1 = customerServiceAI.chat("我的订单什么时候到？");
        System.out.println("用户: 我的订单什么时候到？");
        System.out.println("AI: " + response1);

        // 第二轮对话
        String response2 = customerServiceAI.chat("订单号是 12345678");
        System.out.println("用户: 订单号是 12345678");
        System.out.println("AI: " + response2);
    }

    /**
     * 示例2：退货处理场景
     *
     * 用户流程：
     * 1. 用户：我想退货
     * 2. AI：请告诉我退货原因
     * 3. 用户：产品质量不好
     * 4. AI：我们为您处理退货，请按照以下步骤...
     */
    public void returnProcessExample() {
        System.out.println("\n=== 退货处理场景 ===");

        String response1 = customerServiceAI.chat("我想退货");
        System.out.println("用户: 我想退货");
        System.out.println("AI: " + response1);

        String response2 = customerServiceAI.chat("产品质量不好，与描述不符");
        System.out.println("用户: 产品质量不好，与描述不符");
        System.out.println("AI: " + response2);
    }

    /**
     * 示例3：产品咨询场景
     *
     * 用户流程：
     * 1. 用户：这个产品有什么颜色？
     * 2. AI：该产品有黑色、白色、蓝色三种颜色
     * 3. 用户：库存充足吗？
     * 4. AI：库存充足，可以立即发货
     */
    public void productInquiryExample() {
        System.out.println("\n=== 产品咨询场景 ===");

        String response1 = customerServiceAI.chat("这个产品有什么颜色？");
        System.out.println("用户: 这个产品有什么颜色？");
        System.out.println("AI: " + response1);

        String response2 = customerServiceAI.chat("库存充足吗？");
        System.out.println("用户: 库存充足吗？");
        System.out.println("AI: " + response2);
    }

    /**
     * 示例4：投诉处理场景
     *
     * 用户流程：
     * 1. 用户：我要投诉客服态度不好
     * 2. AI：非常抱歉，请详细说明情况
     * 3. 用户：客服没有耐心回答我的问题
     * 4. AI：我们会立即调查并改进，给您补偿...
     */
    public void complaintHandlingExample() {
        System.out.println("\n=== 投诉处理场景 ===");

        String response1 = customerServiceAI.chat("我要投诉客服态度不好");
        System.out.println("用户: 我要投诉客服态度不好");
        System.out.println("AI: " + response1);

        String response2 = customerServiceAI.chat("客服没有耐心回答我的问题");
        System.out.println("用户: 客服没有耐心回答我的问题");
        System.out.println("AI: " + response2);
    }

    /**
     * 运行所有示例
     */
    public void runAllExamples() {
        orderQueryExample();
        returnProcessExample();
        productInquiryExample();
        complaintHandlingExample();

        System.out.println("\n=== 对话历史 ===");
        System.out.println(customerServiceAI.getConversationHistory());
    }
}
