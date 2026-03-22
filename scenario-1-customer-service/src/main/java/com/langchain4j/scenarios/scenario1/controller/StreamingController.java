package com.langchain4j.scenarios.scenario1.controller;

import com.langchain4j.scenarios.scenario1.service.CustomerServiceAI;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.StreamingResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 流式响应控制器 - 支持 Server-Sent Events (SSE) 的实时流式输出
 *
 * 职责说明：
 * - 提供 SSE 端点支持流式响应
 * - 实现实时 Token 流式输出
 * - 管理 SSE 连接的生命周期
 * - 处理流式响应的错误和超时
 *
 * 架构设计：
 * - 使用 Spring 的 SseEmitter 实现 SSE
 * - 使用 StreamingChatLanguageModel 进行流式 LLM 调用
 * - 使用异步线程池处理流式响应
 * - 支持多个并发的 SSE 连接
 *
 * 使用场景：
 * - 实时流式输出 LLM 响应
 * - 改善用户体验，减少等待时间
 * - 支持长文本生成的实时显示
 * - 适合聊天应用、内容生成等场景
 *
 * 工作原理：
 * 1. 客户端发起 SSE 连接请求
 * 2. 服务器创建 SseEmitter 对象
 * 3. 在异步线程中调用流式 LLM
 * 4. LLM 每生成一个 Token 就通过 SSE 发送给客户端
 * 5. 生成完成后发送 [DONE] 标记并关闭连接
 *
 * 性能考虑：
 * - 使用线程池管理异步任务
 * - 支持多个并发连接
 * - 建议配置合理的线程池大小
 * - 监控 SSE 连接的数量和持续时间
 *
 * 安全考虑：
 * - 验证用户输入防止注入攻击
 * - 限制消息长度防止内存溢出
 * - 实现请求速率限制
 * - 记录所有流式请求用于审计
 * - 处理连接断开的情况
 *
 * 扩展建议：
 * - 可以添加认证和授权
 * - 可以实现连接超时控制
 * - 可以添加流式响应的缓存
 * - 可以支持多种流式格式（JSON、纯文本等）
 */
@RestController
@RequestMapping("/api/customer-service")
@RequiredArgsConstructor
public class StreamingController {

    private final StreamingChatLanguageModel streamingChatModel;
    private final CustomerServiceAI customerServiceAI;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * 流式聊天端点
     *
     * 功能：
     * - 接收用户消息
     * - 通过 SSE 流式返回 LLM 响应
     * - 支持实时 Token 输出
     *
     * 参数说明：
     * - message: 用户输入的消息
     *
     * 返回值：
     * - SseEmitter：SSE 发射器，用于流式发送数据
     *
     * 使用示例（客户端 JavaScript）：
     * const eventSource = new EventSource('/api/customer-service/chat/stream?message=你好');
     * eventSource.onmessage = (event) => {
     *     if (event.data === '[DONE]') {
     *         eventSource.close();
     *     } else {
     *         console.log('Token:', event.data);
     *     }
     * };
     * eventSource.onerror = (error) => {
     *     console.error('SSE Error:', error);
     *     eventSource.close();
     * };
     *
     * 使用示例（curl）：
     * curl -N "http://localhost:8081/api/customer-service/chat/stream?message=你好"
     *
     * @param message 用户消息
     * @return SseEmitter 对象
     */
    @GetMapping("/chat/stream")
    public SseEmitter streamChat(@RequestParam String message) {
        SseEmitter emitter = new SseEmitter(300000L); // 5分钟超时

        // 在异步线程中处理流式响应
        executorService.execute(() -> {
            try {
                // 构建消息列表
                List<ChatMessage> messages = List.of(
                    UserMessage.from(message)
                );

                // 创建流式响应处理器
                StreamingResponseHandler<dev.langchain4j.data.message.AiMessage> handler =
                    new StreamingResponseHandler<dev.langchain4j.data.message.AiMessage>() {
                        @Override
                        public void onNext(String token) {
                            try {
                                // 发送每个 Token 给客户端
                                emitter.send(SseEmitter.event()
                                    .data(token)
                                    .id(System.currentTimeMillis() + "")
                                    .build());
                            } catch (IOException e) {
                                onError(e);
                            }
                        }

                        @Override
                        public void onComplete(Response<dev.langchain4j.data.message.AiMessage> response) {
                            try {
                                // 发送完成标记
                                emitter.send(SseEmitter.event()
                                    .data("[DONE]")
                                    .id(System.currentTimeMillis() + "")
                                    .build());
                                emitter.complete();
                            } catch (IOException e) {
                                onError(e);
                            }
                        }

                        @Override
                        public void onError(Throwable error) {
                            try {
                                emitter.send(SseEmitter.event()
                                    .data("Error: " + error.getMessage())
                                    .id(System.currentTimeMillis() + "")
                                    .build());
                                emitter.completeWithError(error);
                            } catch (IOException e) {
                                // 连接已关闭
                            }
                        }
                    };

                // 调用流式 LLM
                streamingChatModel.generate(messages, handler);

            } catch (Exception e) {
                try {
                    emitter.completeWithError(e);
                } catch (Exception ignored) {
                    // 连接已关闭
                }
            }
        });

        return emitter;
    }
}
