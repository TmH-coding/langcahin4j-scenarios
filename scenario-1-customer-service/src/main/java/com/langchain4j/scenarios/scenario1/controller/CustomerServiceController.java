package com.langchain4j.scenarios.scenario1.controller;

import com.langchain4j.scenarios.scenario1.model.ChatRequest;
import com.langchain4j.scenarios.scenario1.model.ChatResponse;
import com.langchain4j.scenarios.scenario1.service.CustomerServiceAI;
import com.langchain4j.scenarios.scenario1.service.SessionManager;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 客服系统 REST API 控制器 - 多轮对话的HTTP接口层
 *
 * 职责说明：
 * - 提供HTTP REST接口供前端调用
 * - 处理客户的聊天请求和会话管理
 * - 管理对话历史和会话生命周期
 * - 返回结构化的JSON响应
 * - 处理请求参数验证和异常处理
 *
 * 架构设计：
 * - 使用 @RestController 注解实现REST接口
 * - 使用 @RequestMapping 统一路由前缀
 * - 使用 @RequiredArgsConstructor 自动注入依赖
 * - 依赖 CustomerServiceAI 进行对话处理
 * - 依赖 SessionManager 进行会话管理
 * - 返回 ChatResponse 对象进行响应封装
 *
 * 使用场景：
 * - 前端通过这些接口与客服AI交互
 * - 支持多轮对话和多会话并发处理
 * - 提供对话历史查询和清空功能
 * - 支持会话创建和统计查询
 * - 用于电商、SaaS、在线服务等平台的客服系统
 *
 * API端点说明：
 * - POST /api/customer-service/session/create - 创建新会话
 * - POST /api/customer-service/chat - 发送消息并获取回复
 * - GET /api/customer-service/history - 获取对话历史
 * - POST /api/customer-service/clear - 清空对话历史
 * - GET /api/customer-service/stats - 获取会话统计
 *
 * 工作原理：
 * 1. 客户端发送HTTP请求到相应端点
 * 2. 控制器接收请求参数
 * 3. 调用 CustomerServiceAI 或 SessionManager 处理业务逻辑
 * 4. 构建 ChatResponse 响应对象
 * 5. 返回JSON格式的响应给客户端
 * 6. 前端解析响应并更新UI
 *
 * 性能考虑：
 * - 每个请求都是独立处理，支持高并发
 * - 会话存储在内存中，支持快速访问
 * - 建议使用负载均衡器分散请求
 * - 可以配置连接池提高性能
 * - 建议添加请求缓存减少重复计算
 * - 监控API响应时间和吞吐量
 *
 * 安全考虑：
 * - 验证用户输入的消息内容
 * - 限制消息长度防止内存溢出
 * - 实现请求速率限制防止滥用
 * - 记录所有API调用用于审计
 * - 使用HTTPS加密传输
 * - 实现会话超时机制
 * - 防止会话劫持和跨站请求伪造
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
 * - 可以支持多语言对话
 * - 可以添加对话评分和反馈
 * - 可以实现对话转人工客服
 * - 可以支持文件上传和处理
 * - 可以添加对话分析和报告
 * - 可以实现对话导出功能
 * - 可以支持对话搜索和检索
 *
 * 服务端口：8081
 */
@RestController
@RequestMapping("/api/customer-service")
@RequiredArgsConstructor
public class CustomerServiceController {

    /** 注入客服AI服务 */
    private final CustomerServiceAI customerServiceAI;

    /** 注入会话管理器 */
    private final SessionManager sessionManager;

    /**
     * 创建新会话
     *
     * HTTP方法：POST
     * 端点：/api/customer-service/session/create
     *
     * 使用示例：
     * POST /api/customer-service/session/create
     *
     * @return 新会话的ID
     */
    @PostMapping("/session/create")
    public String createSession() {
        return sessionManager.createSession();
    }

    /**
     * 发送消息并获取AI回复
     *
     * HTTP方法：POST
     * 端点：/api/customer-service/chat
     * 参数：message - 用户消息内容
     *
     * 使用示例：
     * POST /api/customer-service/chat?message=你好，我想咨询产品信息
     *
     * @param message 用户输入的消息
     * @return AI生成的回复
     */
    @PostMapping("/chat")
    public ChatResponse chat(@RequestParam String message,
                            @RequestParam(required = false) String sessionId) {
        try {
            String response = customerServiceAI.chat(message);
            return ChatResponse.builder()
                    .message(response)
                    .sessionId(sessionId)
                    .timestamp(System.currentTimeMillis())
                    .status("success")
                    .build();
        } catch (Exception e) {
            return ChatResponse.builder()
                    .status("error")
                    .errorMessage(e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
    }

    /**
     * 获取对话历史
     *
     * HTTP方法：GET
     * 端点：/api/customer-service/history
     *
     * 使用示例：
     * GET /api/customer-service/history
     *
     * @return 格式化的对话历史
     */
    @GetMapping("/history")
    public String getHistory() {
        return customerServiceAI.getConversationHistory();
    }

    /**
     * 清空对话历史
     *
     * HTTP方法：POST
     * 端点：/api/customer-service/clear
     *
     * 使用示例：
     * POST /api/customer-service/clear
     *
     * @return 清空成功的提示信息
     */
    @PostMapping("/clear")
    public String clearHistory() {
        customerServiceAI.clearHistory();
        return "Conversation history cleared";
    }

    /**
     * 获取会话统计信息
     *
     * HTTP方法：GET
     * 端点：/api/customer-service/stats
     *
     * @return 活跃会话数
     */
    @GetMapping("/stats")
    public String getStats() {
        return "Active sessions: " + sessionManager.getActiveSessionCount();
    }
}
