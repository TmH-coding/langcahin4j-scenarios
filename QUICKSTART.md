# 快速开始指南

5 分钟内启动你的第一个 LangChain4j 应用。

## 前置要求

- Java 17+
- Maven 3.8+
- OpenAI API Key

## 第一步：获取 API Key

1. 访问 [OpenAI Platform](https://platform.openai.com)
2. 创建 API Key
3. 设置环境变量：

```bash
# Linux/Mac
export OPENAI_API_KEY=sk-your-key-here

# Windows (PowerShell)
$env:OPENAI_API_KEY="sk-your-key-here"

# Windows (CMD)
set OPENAI_API_KEY=sk-your-key-here
```

## 第二步：构建项目

```bash
cd langchain4j-scenarios
mvn clean install
```

## 第三步：运行场景

### 场景1：客服系统

```bash
cd scenario-1-customer-service
mvn spring-boot:run
```

访问：`http://localhost:8081/api/customer-service/chat?message=你好`

### 场景2：文档分析

```bash
cd scenario-2-document-analysis
mvn spring-boot:run
```

### 场景3：代码助手

```bash
cd scenario-3-code-assistant
mvn spring-boot:run
```

### 场景4：数据分析

```bash
cd scenario-4-data-analyst
mvn spring-boot:run
```

### 场景5：内容创作

```bash
cd scenario-5-content-creator
mvn spring-boot:run
```

## 测试 API

### 使用 curl

```bash
# 客服系统
curl "http://localhost:8081/api/customer-service/chat?message=你好"

# 代码审查
curl -X POST "http://localhost:8083/api/code-assistant/review" \
  -d "code=public class Hello { public static void main(String[] args) { System.out.println(\"Hello\"); } }" \
  -d "language=java"

# 生成文章
curl -X POST "http://localhost:8085/api/content-creator/generate-article" \
  -d "topic=人工智能" \
  -d "style=professional" \
  -d "wordCount=500"
```

### 使用 Postman

1. 导入 `postman-collection.json`
2. 设置环境变量
3. 运行请求

## 常见问题

**Q: 如何修改 LLM 模型？**
A: 编辑 `common/src/main/java/com/langchain4j/scenarios/common/config/LlmConfig.java`

**Q: 如何使用本地 Ollama？**
A: 修改 LlmConfig 使用 OllamaChatModel

**Q: 如何添加数据库支持？**
A: 参考 `docs/ARCHITECTURE.md` 中的扩展点部分

## 下一步

1. 阅读 `docs/LEARNING_PATH.md` 了解学习路径
2. 阅读 `docs/BEST_PRACTICES.md` 学习最佳实践
3. 阅读 `docs/ARCHITECTURE.md` 理解架构设计
4. 修改代码进行实验

## 获取帮助

- 查看 README.md 了解项目概述
- 查看各场景的 Service 类了解实现细节
- 查看 common 模块了解共享工具
