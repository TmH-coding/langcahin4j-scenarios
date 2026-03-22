package com.langchain4j.scenarios.scenario6.demo;

/**
 * Agent 使用演示和测试场景
 *
 * 这个类展示了 Agent 在各种场景下的使用方式。
 * 可以通过 HTTP 请求或直接调用来测试。
 *
 * ============ 测试场景 ============
 *
 * 场景1：简单查询
 * 请求：{"message": "现在几点了？"}
 * 预期：Agent 调用 getCurrentTime 工具，返回当前时间
 *
 * 场景2：数学计算
 * 请求：{"message": "帮我计算 100 + 50 等于多少"}
 * 预期：Agent 调用 calculate 工具，返回 150
 *
 * 场景3：数据库查询
 * 请求：{"message": "查询用户 user123 的信息"}
 * 预期：Agent 调用 queryUserInfo 工具，返回用户详细信息
 *
 * 场景4：多步骤任务
 * 请求：{"message": "查询用户 user123 的信息，然后给他发邮件说'您的账户已激活'"}
 * 预期：
 *   1. Agent 调用 queryUserInfo("user123")
 *   2. 获取用户邮箱
 *   3. Agent 调用 sendEmail(邮箱, "账户激活", "您的账户已激活")
 *   4. 返回完成信息
 *
 * 场景5：条件判断
 * 请求：{"message": "查询北京的天气，如果是晴天就告诉我可以去公园"}
 * 预期：
 *   1. Agent 调用 queryWeather("北京")
 *   2. 根据天气结果判断
 *   3. 返回相应的建议
 *
 * 场景6：数据分析
 * 请求：{"message": "分析销售数据并告诉我增长率"}
 * 预期：Agent 调用 analyzeData("sales")，返回分析结果
 *
 * ============ 工作流程图 ============
 *
 * 场景4的详细工作流程：
 *
 * 用户请求
 *   ↓
 * "查询用户 user123 的信息，然后给他发邮件说'您的账户已激活'"
 *   ↓
 * [Agent 循环 - 第1次迭代]
 * LLM 分析：需要查询用户信息
 *   ↓
 * 调用工具：queryUserInfo("user123")
 *   ↓
 * 工具返回：用户ID: user123, 姓名: 张三, 邮箱: zhangsan@example.com, ...
 *   ↓
 * [Agent 循环 - 第2次迭代]
 * LLM 分析：已获得用户信息，现在需要发送邮件
 *   ↓
 * 调用工具：sendEmail("zhangsan@example.com", "账户激活", "您的账户已激活")
 *   ↓
 * 工具返回：邮件已成功发送
 *   ↓
 * [Agent 循环 - 第3次迭代]
 * LLM 分析：任务已完成
 *   ↓
 * 返回最终答案：已完成！用户信息已查询，邮件已发送
 *   ↓
 * 返回给用户
 *
 * ============ 关键学习点 ============
 *
 * 1. Agent 的自主性
 *    - Agent 自动决定调用哪些工具
 *    - 不需要用户指定调用顺序
 *    - 可以根据结果动态调整策略
 *
 * 2. 多步骤推理
 *    - Agent 可以完成需要多个步骤的任务
 *    - 每个步骤的结果会影响下一步的决策
 *    - 支持条件分支和循环
 *
 * 3. 错误处理
 *    - 如果工具调用失败，Agent 可以尝试其他方式
 *    - 工具返回的错误信息会被 LLM 理解
 *    - Agent 可以向用户解释失败原因
 *
 * 4. 上下文管理
 *    - Agent 记住所有历史消息
 *    - 可以参考之前的对话内容
 *    - 支持多轮对话
 *
 * ============ 性能考虑 ============
 *
 * 1. Token 消耗
 *    - 每次 LLM 调用都会消耗 Token
 *    - 多步骤任务会消耗更多 Token
 *    - 需要监控 Token 使用情况
 *
 * 2. 响应时间
 *    - 每次工具调用都需要时间
 *    - 多步骤任务响应时间较长
 *    - 可以考虑异步处理
 *
 * 3. 工具性能
 *    - 工具的执行时间会影响整体性能
 *    - 需要优化工具的实现
 *    - 可以考虑缓存工具结果
 *
 * ============ 安全考虑 ============
 *
 * 1. 工具权限
 *    - 不是所有用户都应该能调用所有工具
 *    - 需要实现权限控制
 *    - 敏感操作需要额外验证
 *
 * 2. 输入验证
 *    - 需要验证用户输入
 *    - 防止注入攻击
 *    - 限制输入长度
 *
 * 3. 输出过滤
 *    - 不要在输出中暴露敏感信息
 *    - 需要过滤工具返回的结果
 *    - 记录所有操作用于审计
 */
public class AgentDemoScenarios {

    // 这个类主要用于文档和演示目的
    // 实际的测试应该通过 HTTP 请求进行

    public static void main(String[] args) {
        System.out.println("Agent 智能体学习案例");
        System.out.println("==================");
        System.out.println();
        System.out.println("启动应用后，可以通过以下方式测试 Agent：");
        System.out.println();
        System.out.println("1. 查看可用工具：");
        System.out.println("   curl http://localhost:8086/api/agent/tools");
        System.out.println();
        System.out.println("2. 查看使用示例：");
        System.out.println("   curl http://localhost:8086/api/agent/examples");
        System.out.println();
        System.out.println("3. 与 Agent 对话：");
        System.out.println("   curl -X POST http://localhost:8086/api/agent/chat \\");
        System.out.println("     -H 'Content-Type: application/json' \\");
        System.out.println("     -d '{\"message\": \"现在几点了？\"}'");
        System.out.println();
        System.out.println("4. 健康检查：");
        System.out.println("   curl http://localhost:8086/api/agent/health");
        System.out.println();
        System.out.println("详细学习指南请查看：AGENT_LEARNING_GUIDE.md");
    }
}
