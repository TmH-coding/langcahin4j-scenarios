package com.langchain4j.scenarios.common.config;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Mock ChatLanguageModel for testing without OpenAI API key
 *
 * Simulates LLM responses for testing RAG and Agent functionality
 */
@Slf4j
public class MockChatLanguageModel implements ChatLanguageModel {

    @Override
    public Response<AiMessage> generate(List<ChatMessage> messages) {
        log.info("Mock LLM called with {} messages", messages.size());

        // Get the last user message to understand the context
        String userMessage = "";
        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatMessage msg = messages.get(i);
            if (msg.type().toString().equals("USER")) {
                userMessage = msg.text();
                break;
            }
        }

        log.info("User message: {}", userMessage);

        // Simulate tool calling for searchDocumentLibrary
        if (userMessage.toLowerCase().contains("document") ||
            userMessage.toLowerCase().contains("policy") ||
            userMessage.toLowerCase().contains("vacation") ||
            userMessage.toLowerCase().contains("refund")) {

            // Return a tool execution request for searchDocumentLibrary
            ToolExecutionRequest toolRequest = ToolExecutionRequest.builder()
                    .name("searchDocumentLibrary")
                    .arguments("{\"query\": \"" + userMessage + "\"}")
                    .build();

            AiMessage aiMessage = AiMessage.from(toolRequest);
            return Response.from(aiMessage);
        }

        // For other queries, return a simple response
        String response = "Mock response to: " + userMessage;
        return Response.from(AiMessage.from(response));
    }

    @Override
    public String toString() {
        return "MockChatLanguageModel";
    }
}
