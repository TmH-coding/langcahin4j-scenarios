package com.langchain4j.scenarios.scenario7.tool;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * MCP 工具实现
 *
 * 这个类实现了 MCP 协议中的工具。与 Agent 工具不同，MCP 工具遵循标准化的接口规范：
 * - 每个工具都有明确的 JSON Schema 定义
 * - 工具参数和返回值都有严格的类型定义
 * - 支持工具的版本管理和向后兼容性
 *
 * MCP 工具的优势：
 * 1. 标准化：所有工具遵循相同的接口规范
 * 2. 可互操作性：不同的 LLM 和客户端都能理解 MCP 工具
 * 3. 安全性：工具的权限和访问控制更清晰
 * 4. 可扩展性：易于添加新工具而不破坏现有系统
 */
public class McpTools {

    /**
     * 获取当前时间
     *
     * MCP 工具定义：
     * {
     *   "name": "get_current_time",
     *   "description": "获取当前日期和时间",
     *   "inputSchema": {
     *     "type": "object",
     *     "properties": {
     *       "timezone": {"type": "string", "description": "时区（可选，默认为 UTC）"}
     *     }
     *   }
     * }
     */
    public static Map<String, Object> getCurrentTime(Map<String, Object> args) {
        try {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String timeString = now.format(formatter);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("timestamp", System.currentTimeMillis());
            result.put("time", timeString);
            result.put("timezone", "Asia/Shanghai");

            return result;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Failed to get current time: " + e.getMessage());
            return error;
        }
    }

    /**
     * 执行数学运算
     *
     * MCP 工具定义：
     * {
     *   "name": "calculate",
     *   "description": "执行数学运算（+, -, *, /）",
     *   "inputSchema": {
     *     "type": "object",
     *     "properties": {
     *       "operation": {"type": "string", "enum": ["+", "-", "*", "/"]},
     *       "num1": {"type": "number"},
     *       "num2": {"type": "number"}
     *     },
     *     "required": ["operation", "num1", "num2"]
     *   }
     * }
     */
    public static Map<String, Object> calculate(Map<String, Object> args) {
        try {
            String operation = (String) args.get("operation");
            double num1 = ((Number) args.get("num1")).doubleValue();
            double num2 = ((Number) args.get("num2")).doubleValue();

            double result = 0;
            switch (operation) {
                case "+":
                    result = num1 + num2;
                    break;
                case "-":
                    result = num1 - num2;
                    break;
                case "*":
                    result = num1 * num2;
                    break;
                case "/":
                    if (num2 == 0) {
                        Map<String, Object> error = new HashMap<>();
                        error.put("success", false);
                        error.put("error", "Division by zero");
                        return error;
                    }
                    result = num1 / num2;
                    break;
                default:
                    Map<String, Object> error = new HashMap<>();
                    error.put("success", false);
                    error.put("error", "Invalid operation: " + operation);
                    return error;
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("operation", operation);
            response.put("num1", num1);
            response.put("num2", num2);
            response.put("result", result);

            return response;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Calculation failed: " + e.getMessage());
            return error;
        }
    }

    /**
     * 查询用户信息
     *
     * MCP 工具定义：
     * {
     *   "name": "query_user_info",
     *   "description": "查询用户的详细信息",
     *   "inputSchema": {
     *     "type": "object",
     *     "properties": {
     *       "userId": {"type": "string", "description": "用户 ID"}
     *     },
     *     "required": ["userId"]
     *   }
     * }
     */
    public static Map<String, Object> queryUserInfo(Map<String, Object> args) {
        try {
            String userId = (String) args.get("userId");

            // 模拟数据库查询
            Map<String, Object> users = new HashMap<>();

            Map<String, Object> user1 = new HashMap<>();
            user1.put("userId", "user123");
            user1.put("name", "张三");
            user1.put("email", "zhangsan@example.com");
            user1.put("phone", "13800138000");
            user1.put("status", "active");
            users.put("user123", user1);

            Map<String, Object> user2 = new HashMap<>();
            user2.put("userId", "user456");
            user2.put("name", "李四");
            user2.put("email", "lisi@example.com");
            user2.put("phone", "13900139000");
            user2.put("status", "active");
            users.put("user456", user2);

            if (users.containsKey(userId)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.putAll((Map<String, Object>) users.get(userId));
                return response;
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "User not found: " + userId);
                return error;
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Query failed: " + e.getMessage());
            return error;
        }
    }

    /**
     * 查询天气信息
     *
     * MCP 工具定义：
     * {
     *   "name": "query_weather",
     *   "description": "查询指定城市的天气信息",
     *   "inputSchema": {
     *     "type": "object",
     *     "properties": {
     *       "city": {"type": "string", "description": "城市名称"}
     *     },
     *     "required": ["city"]
     *   }
     * }
     */
    public static Map<String, Object> queryWeather(Map<String, Object> args) {
        try {
            String city = (String) args.get("city");

            Map<String, Object> weatherData = new HashMap<>();

            Map<String, Object> beijing = new HashMap<>();
            beijing.put("city", "北京");
            beijing.put("weather", "晴天");
            beijing.put("temperature", 15);
            beijing.put("humidity", 45);
            weatherData.put("北京", beijing);

            Map<String, Object> shanghai = new HashMap<>();
            shanghai.put("city", "上海");
            shanghai.put("weather", "多云");
            shanghai.put("temperature", 18);
            shanghai.put("humidity", 60);
            weatherData.put("上海", shanghai);

            if (weatherData.containsKey(city)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.putAll((Map<String, Object>) weatherData.get(city));
                return response;
            } else {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "City not found: " + city);
                return error;
            }
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Weather query failed: " + e.getMessage());
            return error;
        }
    }

    /**
     * 发送邮件
     *
     * MCP 工具定义：
     * {
     *   "name": "send_email",
     *   "description": "发送邮件给指定收件人",
     *   "inputSchema": {
     *     "type": "object",
     *     "properties": {
     *       "recipient": {"type": "string", "description": "收件人邮箱"},
     *       "subject": {"type": "string", "description": "邮件主题"},
     *       "content": {"type": "string", "description": "邮件内容"}
     *     },
     *     "required": ["recipient", "subject", "content"]
     *   }
     * }
     */
    public static Map<String, Object> sendEmail(Map<String, Object> args) {
        try {
            String recipient = (String) args.get("recipient");
            String subject = (String) args.get("subject");
            String content = (String) args.get("content");

            // 模拟邮件发送
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("messageId", "msg_" + System.currentTimeMillis());
            response.put("recipient", recipient);
            response.put("subject", subject);
            response.put("status", "sent");
            response.put("timestamp", System.currentTimeMillis());

            return response;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Email sending failed: " + e.getMessage());
            return error;
        }
    }

    /**
     * 分析数据
     *
     * MCP 工具定义：
     * {
     *   "name": "analyze_data",
     *   "description": "分析指定类型的数据",
     *   "inputSchema": {
     *     "type": "object",
     *     "properties": {
     *       "dataType": {"type": "string", "enum": ["sales", "users", "revenue"]}
     *     },
     *     "required": ["dataType"]
     *   }
     * }
     */
    public static Map<String, Object> analyzeData(Map<String, Object> args) {
        try {
            String dataType = (String) args.get("dataType");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("dataType", dataType);

            switch (dataType) {
                case "sales":
                    response.put("totalSales", 1000000);
                    response.put("growthRate", "15.5%");
                    response.put("topProduct", "Product A");
                    break;
                case "users":
                    response.put("totalUsers", 50000);
                    response.put("activeUsers", 35000);
                    response.put("growthRate", "8.2%");
                    break;
                case "revenue":
                    response.put("totalRevenue", 5000000);
                    response.put("monthlyRevenue", 416667);
                    response.put("growthRate", "12.3%");
                    break;
                default:
                    response.put("success", false);
                    response.put("error", "Unknown data type: " + dataType);
            }

            return response;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Data analysis failed: " + e.getMessage());
            return error;
        }
    }
}
