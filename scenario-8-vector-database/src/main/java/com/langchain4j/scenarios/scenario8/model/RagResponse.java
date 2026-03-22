package com.langchain4j.scenarios.scenario8.model;

public record RagResponse(String question, String answer, String storeType, long elapsedMs) {}
