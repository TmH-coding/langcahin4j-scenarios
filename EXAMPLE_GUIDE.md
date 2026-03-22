# 实战示例运行指南

## 项目结构

```
scenario-1-customer-service/
├── example/
│   └── CustomerServiceExample.java    # 客服系统实战示例
├── service/
│   ├── CustomerServiceAI.java
│   └── SessionManager.java
└── controller/
    └── CustomerServiceController.java

scenario-2-document-analysis/
├── example/
│   └── DocumentAnalysisExample.java    # 文档分析实战示例
├── service/
│   ├── DocumentAnalysisService.java
│   └── DocumentCacheManager.java
└── controller/
    └── DocumentAnalysisController.java

scenario-3-code-assistant/
├── example/
│   └── CodeAssistantExample.java       # 代码助手实战示例
├── service/
│   ├── CodeAssistantService.java
│   └── CodeReviewHistoryManager.java
└── controller/
    └── CodeAssistantController.java

scenario-4-data-analyst/
├── example/
│   └── DataAnalystExample.java         # 数据分析实战示例
├── service/
│   ├── DataAnalystService.java
│   └── QueryExecutionTracker.java
└── controller/
    └── DataAnalystController.java

scenario-5-content-creator/
├── example/
│   └── ContentCreatorExample.java      # 内容创作实战示例
├── service/
│   ├── ContentCreatorService.java
│   └── ContentGenerationStatistics.java
└── controller/
    └── ContentCreatorController.java
```

## 运行示例代码

### 方式1：在IDE中运行

1. 打开IntelliJ IDEA或Eclipse
2. 导入项目
3. 找到对应的Example类
4. 右键 → Run 'XXXExample.main()'

### 方式2：使用Maven运行

```bash
# 编译项目
mvn clean compile

# 运行特定的示例
mvn exec:java -Dexec.mainClass="com.langchain4j.scenarios.scenario1.example.CustomerServiceExample"
```

### 方式3：启动服务后测试

```bash
# 启动客服系统
cd scenario-1-customer-service
mvn spring-boot:run

# 在另一个终端测试
curl -X POST "http://localhost:8081/api/customer-service/chat?message=你好"
```

## 示例代码详解

### 场景1：客服系统示例

**文件**: `scenario-1-customer-service/src/main/java/com/langchain4j/scenarios/scenario1/example/CustomerServiceExample.java`

**主要方法**:
- `orderQueryExample()` - 订单查询场景
- `returnProcessExample()` - 退货处理场景
- `productInquiryExample()` - 产品咨询场景
- `complaintHandlingExample()` - 投诉处理场景
- `runAllExamples()` - 运行所有示例

**学习要点**:
1. 如何维护多轮对话历史
2. 如何理解用户意图
3. 如何生成上下文相关的回复
4. 如何处理不同类型的用户请求

### 场景2：文档分析示例

**文件**: `scenario-2-document-analysis/src/main/java/com/langchain4j/scenarios/scenario2/example/DocumentAnalysisExample.java`

**主要方法**:
- `contractAnalysisExample()` - 合同分析
- `employeeHandbookExample()` - 员工手册查询
- `technicalDocumentExample()` - 技术文档查询
- `legalDocumentExample()` - 法律文件分析
- `runAllExamples()` - 运行所有示例

**学习要点**:
1. 如何加载和处理大文档
2. 如何进行文本检索和查询
3. 如何生成文档摘要
4. 如何实现RAG功能

### 场景3：代码助手示例

**文件**: `scenario-3-code-assistant/src/main/java/com/langchain4j/scenarios/scenario3/example/CodeAssistantExample.java`

**主要方法**:
- `codeQualityReviewExample()` - 代码质量审查
- `bugDetectionExample()` - Bug检测
- `securityReviewExample()` - 安全审查
- `documentationGenerationExample()` - 文档生成
- `refactoringExample()` - 重构建议
- `runAllExamples()` - 运行所有示例

**学习要点**:
1. 如何分析代码质量
2. 如何检测潜在的Bug
3. 如何识别安全漏洞
4. 如何生成代码文档
5. 如何提供重构建议

### 场景4：数据分析示例

**文件**: `scenario-4-data-analyst/src/main/java/com/langchain4j/scenarios/scenario4/example/DataAnalystExample.java`

**主要方法**:
- `salesAnalysisExample()` - 销售数据分析
- `userBehaviorAnalysisExample()` - 用户行为分析
- `inventoryAnalysisExample()` - 库存管理分析
- `queryOptimizationExample()` - 查询性能优化
- `runAllExamples()` - 运行所有示例

**学习要点**:
1. 如何自动生成SQL查询
2. 如何分析数据和生成报告
3. 如何优化数据库查询
4. 如何识别性能瓶颈

### 场景5：内容创作示例

**文件**: `scenario-5-content-creator/src/main/java/com/langchain4j/scenarios/scenario5/example/ContentCreatorExample.java`

**主要方法**:
- `blogArticleGenerationExample()` - 博客文章生成
- `seoOptimizationExample()` - SEO优化
- `multiLanguageTranslationExample()` - 多语言翻译
- `socialMediaContentExample()` - 社交媒体内容
- `contentImprovementExample()` - 内容改进
- `completeContentMarketingWorkflowExample()` - 完整流程
- `runAllExamples()` - 运行所有示例

**学习要点**:
1. 如何生成高质量内容
2. 如何优化SEO
3. 如何进行多语言翻译
4. 如何生成社交媒体内容
5. 如何实现完整的内容营销流程

## 修改和扩展示例

### 添加新的场景

1. 在Example类中添加新方法
2. 在`runAllExamples()`中调用新方法
3. 提供详细的注释和说明

### 集成真实的LLM

1. 替换mock实现
2. 调用真实的LLM API
3. 处理API响应和错误

### 自定义业务逻辑

1. 修改Service类中的业务逻辑
2. 更新Controller中的API端点
3. 添加新的数据模型

## 常见问题

**Q: 示例代码在哪里？**
A: 每个场景模块的`example`包中都有对应的Example类

**Q: 如何运行示例？**
A: 可以在IDE中直接运行，或使用Maven命令运行

**Q: 如何修改示例以适应我的需求？**
A: 修改Example类中的参数和逻辑，或创建新的Example类

**Q: 示例代码是否可以用于生产环境？**
A: 示例代码是为了演示和学习，生产环境需要添加错误处理、日志、安全检查等

## 下一步

1. 运行所有示例代码
2. 理解每个示例的业务逻辑
3. 修改示例以适应你的需求
4. 集成真实的LLM API
5. 部署到生产环境
