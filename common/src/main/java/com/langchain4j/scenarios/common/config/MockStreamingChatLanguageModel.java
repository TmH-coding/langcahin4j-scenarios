package com.langchain4j.scenarios.common.config;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.StreamingResponseHandler;
import dev.langchain4j.model.output.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class MockStreamingChatLanguageModel implements StreamingChatLanguageModel {

    @Override
    public void generate(List<ChatMessage> messages, StreamingResponseHandler<AiMessage> handler) {
        log.info("Mock Streaming LLM called with {} messages", messages.size());

        String userMessage = "";
        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatMessage msg = messages.get(i);
            if (msg instanceof dev.langchain4j.data.message.UserMessage) {
                userMessage = msg.text();
                break;
            }
        }

        log.info("User message: {}", userMessage);

        String response = "Mock streaming response to: " + userMessage;
        handler.onNext(response);
        handler.onComplete(Response.from(AiMessage.from(response)));
    }

    @Override
    public String toString() {
        return "MockStreamingChatLanguageModel";
    }
}
