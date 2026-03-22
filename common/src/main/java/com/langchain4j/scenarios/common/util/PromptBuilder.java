package com.langchain4j.scenarios.common.util;

import com.langchain4j.scenarios.common.model.ConversationMessage;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Prompt 构建器 - 统一的消息列表构建工具
 */
public class PromptBuilder {

    public static List<ChatMessage> buildMessages(String systemPrompt, String userMessage) {
        List<ChatMessage> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            messages.add(SystemMessage.from(systemPrompt));
        }
        messages.add(UserMessage.from(userMessage));
        return messages;
    }

    public static List<ChatMessage> buildMessagesWithHistory(String systemPrompt, List<ChatMessage> conversationHistory, String userMessage) {
        List<ChatMessage> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            messages.add(SystemMessage.from(systemPrompt));
        }
        if (conversationHistory != null && !conversationHistory.isEmpty()) {
            messages.addAll(conversationHistory);
        }
        messages.add(UserMessage.from(userMessage));
        return messages;
    }

    public static List<ChatMessage> convertConversationMessagesToChat(List<ConversationMessage> conversationMessages) {
        List<ChatMessage> chatMessages = new ArrayList<>();
        if (conversationMessages != null) {
            for (ConversationMessage msg : conversationMessages) {
                if ("user".equals(msg.getRole())) {
                    chatMessages.add(UserMessage.from(msg.getContent()));
                } else if ("assistant".equals(msg.getRole())) {
                    chatMessages.add(AiMessage.from(msg.getContent()));
                }
            }
        }
        return chatMessages;
    }

    public static String buildRagUserMessage(List<String> contextSegments, String question) {
        StringBuilder sb = new StringBuilder();
        sb.append("根据以下上下文回答问题：\n\n");
        if (contextSegments != null && !contextSegments.isEmpty()) {
            sb.append("【上下文】\n");
            for (int i = 0; i < contextSegments.size(); i++) {
                sb.append(i + 1).append(". ").append(contextSegments.get(i)).append("\n\n");
            }
        }
        sb.append("【问题】\n").append(question);
        return sb.toString();
    }

    public static List<ChatMessage> buildRagMessages(String systemPrompt, List<String> contextSegments, String question) {
        List<ChatMessage> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isEmpty()) {
            messages.add(SystemMessage.from(systemPrompt));
        }
        String ragUserMessage = buildRagUserMessage(contextSegments, question);
        messages.add(UserMessage.from(ragUserMessage));
        return messages;
    }
}

