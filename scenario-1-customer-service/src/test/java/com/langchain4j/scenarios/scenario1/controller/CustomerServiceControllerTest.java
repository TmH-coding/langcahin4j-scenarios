package com.langchain4j.scenarios.scenario1.controller;

import com.langchain4j.scenarios.common.response.ApiResponse;
import com.langchain4j.scenarios.common.test.BaseSpringBootTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/**
 * 客服系统API测试
 *
 * 功能说明：
 * - 测试所有API端点
 * - 验证响应格式
 * - 验证业务逻辑
 */
@AutoConfigureMockMvc
public class CustomerServiceControllerTest extends BaseSpringBootTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 测试聊天API
     */
    @Test
    public void testChatApi() throws Exception {
        mockMvc.perform(post("/api/customer-service/chat")
                .param("message", "你好"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").exists());
    }

    /**
     * 测试获取历史API
     */
    @Test
    public void testGetHistoryApi() throws Exception {
        mockMvc.perform(post("/api/customer-service/chat")
                .param("message", "你好"))
                .andExpect(status().isOk());

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/customer-service/history"))
                .andExpect(status().isOk());
    }

    /**
     * 测试清空历史API
     */
    @Test
    public void testClearHistoryApi() throws Exception {
        mockMvc.perform(post("/api/customer-service/clear"))
                .andExpect(status().isOk());
    }
}
