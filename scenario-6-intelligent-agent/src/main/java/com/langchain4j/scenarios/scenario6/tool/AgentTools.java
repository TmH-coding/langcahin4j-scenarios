package com.langchain4j.scenarios.scenario6.tool;

import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 工具定义 - Agent 可以调用的工具集合
 *
 * 核心概念讲解：
 * ============
 * 1. 什么是 Tool（工具）？
 *    - Tool 是 Agent 可以调用的函数/方法
 *    - 每个 Tool 都有明确的输入参数和返回值
 *    - LLM 根据用户需求决定是否调用某个 Tool
 *    - Tool 的返回结果会反馈给 LLM 进行下一步推理
 *
 * 2. Tool 的工作流程：
 *    用户请求 → LLM 分析 → 选择合适的 Tool → 调用 Tool → 获取结果 → LLM 继续推理 → 返回最终答案
 *
 * 3. @Tool 注解的作用：
 *    - 标记方法为可被 Agent 调用的工具
 *    - 方法名和参数会被 LLM 理解
 *    - 方法的 JavaDoc 注释会被用作工具描述
 *
 * 4. Tool 设计原则：
 *    - 单一职责：每个 Tool 只做一件事
 *    - 清晰的参数：参数名要能表达含义
 *    - 有意义的返回值：返回结果要能帮助 LLM 做决策
 *    - 完整的文档：JavaDoc 要清楚描述工具的功能
 */
@Slf4j
@Component
public class AgentTools {

    /**
     * 工具1：获取当前时间
     *
     * 使用场景：
     * - 用户问"现在几点了？"
     * - Agent 需要知道当前时间来做决策
     *
     * 工作原理：
     * 1. LLM 识别用户需要时间信息
     * 2. LLM 调用此工具
     * 3. 工具返回当前时间
     * 4. LLM 将时间信息用于后续推理
     */
    @Tool("获取当前时间，返回格式为 yyyy-MM-dd HH:mm:ss")
    public String getCurrentTime() {
        String currentTime = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        log.info("Tool called: getCurrentTime, result: {}", currentTime);
        return currentTime;
    }

    /**
     * 工具2：计算器 - 执行数学运算
     *
     * 使用场景：
     * - 用户问"100 + 50 等于多少？"
     * - 用户问"计算 1000 * 0.15 的结果"
     *
     * 参数说明：
     * - operation: 运算符（+, -, *, /）
     * - num1: 第一个数字
     * - num2: 第二个数字
     *
     * 工作原理：
     * 1. LLM 识别用户的数学问题
     * 2. LLM 提取操作数和运算符
     * 3. LLM 调用此工具，传入参数
     * 4. 工具执行计算并返回结果
     * 5. LLM 将结果返回给用户
     */
    @Tool("执行数学运算，支持 +, -, *, / 四种操作")
    public String calculate(String operation, double num1, double num2) {
        double result;
        try {
            result = switch (operation) {
                case "+" -> num1 + num2;
                case "-" -> num1 - num2;
                case "*" -> num1 * num2;
                case "/" -> {
                    if (num2 == 0) {
                        throw new IllegalArgumentException("除数不能为0");
                    }
                    yield num1 / num2;
                }
                default -> throw new IllegalArgumentException("不支持的操作符: " + operation);
            };
            String resultStr = String.format("%.2f", result);
            log.info("Tool called: calculate, operation: {}, num1: {}, num2: {}, result: {}",
                    operation, num1, num2, resultStr);
            return resultStr;
        } catch (Exception e) {
            log.error("Calculate error", e);
            return "计算错误: " + e.getMessage();
        }
    }

    /**
     * 工具3：数据库查询 - 模拟查询用户信息
     *
     * 使用场景：
     * - 用户问"查询用户 user123 的信息"
     * - 用户问"用户 john 的邮箱是什么？"
     *
     * 参数说明：
     * - userId: 用户ID
     *
     * 工作原理：
     * 1. LLM 识别用户需要查询数据
     * 2. LLM 提取用户ID
     * 3. LLM 调用此工具
     * 4. 工具从"数据库"返回用户信息
     * 5. LLM 将信息格式化后返回给用户
     *
     * 注意：这里使用模拟数据，实际应用中应连接真实数据库
     */
    @Tool("查询用户信息，根据用户ID返回用户的详细信息")
    public String queryUserInfo(String userId) {
        // 模拟数据库中的用户数据
        Map<String, Map<String, String>> userDatabase = new HashMap<>();
        userDatabase.put("user123", Map.of(
                "name", "张三",
                "email", "zhangsan@example.com",
                "phone", "13800138000",
                "department", "技术部"
        ));
        userDatabase.put("user456", Map.of(
                "name", "李四",
                "email", "lisi@example.com",
                "phone", "13900139000",
                "department", "销售部"
        ));
        userDatabase.put("user789", Map.of(
                "name", "王五",
                "email", "wangwu@example.com",
                "phone", "14000140000",
                "department", "市场部"
        ));

        if (userDatabase.containsKey(userId)) {
            Map<String, String> user = userDatabase.get(userId);
            String result = String.format("用户ID: %s, 姓名: %s, 邮箱: %s, 电话: %s, 部门: %s",
                    userId, user.get("name"), user.get("email"), user.get("phone"), user.get("department"));
            log.info("Tool called: queryUserInfo, userId: {}, result: {}", userId, result);
            return result;
        } else {
            log.warn("Tool called: queryUserInfo, userId: {} not found", userId);
            return "用户不存在: " + userId;
        }
    }

    /**
     * 工具4：天气查询 - 模拟查询天气信息
     *
     * 使用场景：
     * - 用户问"北京今天天气怎么样？"
     * - 用户问"上海明天会下雨吗？"
     *
     * 参数说明：
     * - city: 城市名称
     *
     * 工作原理：
     * 1. LLM 识别用户需要天气信息
     * 2. LLM 提取城市名称
     * 3. LLM 调用此工具
     * 4. 工具返回天气数据
     * 5. LLM 将天气信息返回给用户
     */
    @Tool("查询指定城市的天气信息")
    public String queryWeather(String city) {
        // 模拟天气数据
        Map<String, String> weatherData = new HashMap<>();
        weatherData.put("北京", "晴天，温度 15°C，风力 3 级");
        weatherData.put("上海", "多云，温度 18°C，风力 2 级");
        weatherData.put("深圳", "晴天，温度 22°C，风力 2 级");
        weatherData.put("杭州", "阴天，温度 16°C，风力 4 级");

        String weather = weatherData.getOrDefault(city, "暂无该城市的天气数据");
        log.info("Tool called: queryWeather, city: {}, result: {}", city, weather);
        return weather;
    }

    /**
     * 工具5：发送邮件 - 模拟发送邮件
     *
     * 使用场景：
     * - 用户问"帮我给张三发邮件"
     * - 用户问"发送通知给所有用户"
     *
     * 参数说明：
     * - recipient: 收件人邮箱
     * - subject: 邮件主题
     * - content: 邮件内容
     *
     * 工作原理：
     * 1. LLM 识别用户需要发送邮件
     * 2. LLM 提取收件人、主题、内容
     * 3. LLM 调用此工具
     * 4. 工具执行发送操作
     * 5. 工具返回发送结果
     * 6. LLM 将结果反馈给用户
     */
    @Tool("发送邮件到指定邮箱地址")
    public String sendEmail(String recipient, String subject, String content) {
        try {
            // 模拟邮件发送
            log.info("Sending email to: {}, subject: {}, content: {}", recipient, subject, content);

            // 验证邮箱格式
            if (!recipient.contains("@")) {
                return "邮件发送失败: 无效的邮箱地址";
            }

            // 模拟发送延迟
            Thread.sleep(100);

            String result = String.format("邮件已成功发送到 %s，主题: %s", recipient, subject);
            log.info("Tool called: sendEmail, result: {}", result);
            return result;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "邮件发送失败: " + e.getMessage();
        }
    }

    /**
     * 工具6：数据分析 - 分析数据集
     *
     * 使用场景：
     * - 用户问"分析这些数据的平均值"
     * - 用户问"计算数据的统计信息"
     *
     * 参数说明：
     * - dataType: 数据类型（sales, users, revenue）
     *
     * 工作原理：
     * 1. LLM 识别用户需要数据分析
     * 2. LLM 提取数据类型
     * 3. LLM 调用此工具
     * 4. 工具执行分析并返回结果
     * 5. LLM 将分析结果返回给用户
     */
    @Tool("分析指定类型的数据，返回统计信息")
    public String analyzeData(String dataType) {
        try {
            String analysis = switch (dataType.toLowerCase()) {
                case "sales" -> "销售数据分析: 总销售额 ¥1,000,000, 平均订单金额 ¥5,000, 增长率 15%";
                case "users" -> "用户数据分析: 总用户数 10,000, 活跃用户 6,500, 新增用户 1,200";
                case "revenue" -> "收入数据分析: 总收入 ¥500,000, 平均收入 ¥50,000, 同比增长 20%";
                default -> "不支持的数据类型: " + dataType;
            };
            log.info("Tool called: analyzeData, dataType: {}, result: {}", dataType, analysis);
            return analysis;
        } catch (Exception e) {
            log.error("Data analysis error", e);
            return "数据分析失败: " + e.getMessage();
        }
    }
}
