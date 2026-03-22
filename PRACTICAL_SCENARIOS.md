# LangChain4j 实战场景指南

## 项目概述

本项目包含5个完整的实战场景，每个场景都对应真实的业务需求和使用案例。

---

## 场景1：智能客服系统 (端口 8081)

### 真实业务背景
电商平台的24/7自动客服系统，处理订单咨询、退货、投诉等问题。

### 核心功能
- 多轮对话管理
- 意图识别和理解
- 知识库检索
- 会话管理

### 实战场景

#### 场景1.1：订单查询
```
用户: 我的订单什么时候到？
AI: 请提供订单号
用户: 订单号是 12345678
AI: 您的订单已发货，预计3天内送达
```

#### 场景1.2：退货处理
```
用户: 我想退货
AI: 请告诉我退货原因
用户: 产品质量不好
AI: 我们为您处理退货，请按照以下步骤...
```

#### 场景1.3：产品咨询
```
用户: 这个产品有什么颜色？
AI: 该产品有黑色、白色、蓝色三种颜色
用户: 库存充足吗？
AI: 库存充足，可以立即发货
```

#### 场景1.4：投诉处理
```
用户: 我要投诉客服态度不好
AI: 非常抱歉，请详细说明情况
用户: 客服没有耐心回答我的问题
AI: 我们会立即调查并改进...
```

### 关键技术点
- ConversationMemoryUtil：维护对话历史
- SessionManager：管理多用户会话
- PromptTemplateUtil：定义系统角色

### 运行示例
```bash
# 启动服务
cd scenario-1-customer-service
mvn spring-boot:run

# 测试API
curl -X POST "http://localhost:8081/api/customer-service/chat?message=你好"
```

---

## 场景2：文档智能分析 (端口 8082)

### 真实业务背景
企业知识库系统，快速检索和分析大量文档（合同、政策、手册等）。

### 核心功能
- 文档加载和分割
- 内容检索和查询
- 文档摘要生成
- RAG（检索增强生成）

### 实战场景

#### 场景2.1：合同分析
```
1. 上传合同文档
2. 查询付款条件
3. 查询违约责任
4. 生成合同摘要
```

#### 场景2.2：员工手册查询
```
员工查询: 年假政策
系统返回: 每年享受15天带薪年假...

员工查询: 薪酬标准
系统返回: 基本工资 + 绩效奖金...
```

#### 场景2.3：技术文档查询
```
开发者查询: 如何进行API认证？
系统返回: 使用Bearer Token...

开发者查询: 错误处理方法
系统返回: 所有错误都返回标准JSON格式...
```

#### 场景2.4：法律文件分析
```
律师查询: 管辖权条款
系统返回: 本协议受中国法律管辖...

律师查询: 保密条款
系统返回: 双方承诺保守商业秘密...
```

### 关键技术点
- DocumentProcessingUtil：文本分块和处理
- DocumentCacheManager：文档缓存管理
- 向量相似度搜索

### 运行示例
```bash
# 启动服务
cd scenario-2-document-analysis
mvn spring-boot:run

# 加载文档
curl -X POST "http://localhost:8082/api/document-analysis/load?path=/path/to/doc.txt&documentId=doc1"

# 查询文档
curl -X POST "http://localhost:8082/api/document-analysis/query/doc1?query=付款条件"
```

---

## 场景3：代码助手 (端口 8083)

### 真实业务背景
开发团队的代码审查和优化系统，自动检查代码质量、安全性和性能。

### 核心功能
- 代码质量审查
- Bug检测
- 安全漏洞扫描
- 文档自动生成
- 重构建议

### 实战场景

#### 场景3.1：代码质量审查
```java
// 问题代码
public boolean login(String username, String password) {
    User user = userDao.findByUsername(username);
    if (user.getPassword().equals(password)) {
        return true;
    }
    return false;
}

// 问题分析
1. 没有检查null值 - NullPointerException
2. 密码明文比较 - 安全风险
3. 没有日志记录 - 难以追踪
4. 没有异常处理 - 容易崩溃
```

#### 场景3.2：Bug检测
```java
// Bug代码
public int calculateTotal(List<Integer> prices) {
    int total = 0;
    for (int i = 0; i <= prices.size(); i++) {  // 错误！
        total += prices.get(i);
    }
    return total;
}

// Bug分析
1. 循环条件错误: i <= prices.size() 应该是 i < prices.size()
   - 会导致 ArrayIndexOutOfBoundsException
2. 没有检查空列表
3. 没有处理整数溢出
```

#### 场景3.3：安全审查
```java
// 安全问题代码
public User getUserByUsername(String username) {
    String sql = "SELECT * FROM users WHERE username = '" + username + "'";
    return executeQuery(sql);
}

// 安全问题
1. SQL注入漏洞 - 严重安全风险
   - 应该使用参数化查询
2. 没有输入验证
3. 没有日志记录
```

#### 场景3.4：重构建议
```java
// 冗长的方法
public void processOrder(Order order) {
    if (order != null && order.getItems() != null && order.getItems().size() > 0) {
        double total = 0;
        for (Item item : order.getItems()) {
            total += item.getPrice() * item.getQuantity();
        }
        if (total > 100) {
            order.setDiscount(0.1);
        } else if (total > 50) {
            order.setDiscount(0.05);
        }
        order.setTotal(total);
    }
}

// 重构方向
1. 提取计算总价的逻辑为单独方法
2. 提取折扣计算为单独方法
3. 使用Stream API简化循环
4. 使用策略模式处理不同的折扣规则
```

### 关键技术点
- CodeReviewHistoryManager：审查历史追踪
- 多语言支持（Java、Python、JavaScript等）
- 安全漏洞检测

### 运行示例
```bash
# 启动服务
cd scenario-3-code-assistant
mvn spring-boot:run

# 代码审查
curl -X POST "http://localhost:8083/api/code-assistant/review?code=public%20void%20test()%20{}&language=java"

# Bug检测
curl -X POST "http://localhost:8083/api/code-assistant/bugs?code=...&language=java"
```

---

## 场景4：数据分析助手 (端口 8084)

### 真实业务背景
电商数据分析平台，自动生成SQL查询、分析销售数据、优化数据库性能。

### 核心功能
- SQL自动生成
- 数据分析和统计
- 报告生成
- 查询性能优化

### 实战场景

#### 场景4.1：销售数据分析
```
需求: 查询2024年3月的总销售额
生成的SQL: SELECT SUM(amount) FROM orders WHERE order_date BETWEEN '2024-03-01' AND '2024-03-31'

数据: 3月销售额: 1000000, 2月销售额: 800000
问题: 计算3月相比2月的增长率
分析: 增长率 = (1000000 - 800000) / 800000 = 25%
```

#### 场景4.2：用户行为分析
```
需求: 查询过去30天内有购买行为的用户数
生成的SQL: SELECT COUNT(DISTINCT user_id) FROM orders WHERE order_date > DATE_SUB(NOW(), INTERVAL 30 DAY)

用户数据: 新用户: 1000, 7天留存: 650, 30天留存: 420
问题: 计算7天和30天的留存率
分析: 7天留存率 = 650/1000 = 65%, 30天留存率 = 420/1000 = 42%
```

#### 场景4.3：库存管理分析
```
需求: 查询库存少于100件的商品
生成的SQL: SELECT * FROM products WHERE stock < 100

库存数据: 商品A: 当前库存50, 日均销售10
问题: 预测库存不足时间
分析: 预计5天后库存不足
```

#### 场景4.4：查询性能优化
```
原始查询（低效）:
SELECT o.*, u.*, p.*
FROM orders o
JOIN users u ON o.user_id = u.id
JOIN products p ON o.product_id = p.id
WHERE o.order_date > '2024-01-01'

优化建议:
1. 只选择需要的列，避免SELECT *
2. 使用INNER JOIN替代LEFT JOIN
3. 在WHERE条件中的列上建立索引
4. 考虑分区表处理大数据量

优化后的查询:
SELECT o.id, o.order_date, o.amount, u.name, p.name
FROM orders o
INNER JOIN users u ON o.user_id = u.id
INNER JOIN products p ON o.product_id = p.id
WHERE o.order_date > '2024-01-01'
```

### 关键技术点
- QueryExecutionTracker：查询追踪
- SQL生成和优化
- 性能分析

### 运行示例
```bash
# 启动服务
cd scenario-4-data-analyst
mvn spring-boot:run

# 生成SQL
curl -X POST "http://localhost:8084/api/data-analyst/generate-sql?requirement=查询用户&schema=users(id,name)"

# 分析数据
curl -X POST "http://localhost:8084/api/data-analyst/analyze?data=1,2,3,4,5&question=平均值"
```

---

## 场景5：内容创作平台 (端口 8085)

### 真实业务背景
内容营销平台，自动生成营销文案、优化SEO、翻译多语言内容。

### 核心功能
- 文章生成
- SEO优化
- 多语言翻译
- 社交媒体内容生成
- 写作改进

### 实战场景

#### 场景5.1：博客文章生成
```
需求1: 生成2000字专业技术文章
主题: Java并发编程最佳实践
风格: professional
字数: 2000

需求2: 生成1500字轻松易懂的文章
主题: 如何选择适合自己的编程语言
风格: casual
字数: 1500
```

#### 场景5.2：SEO优化
```
原始内容:
Java是一种编程语言。它很流行。
很多公司使用Java开发应用。

目标关键词: Java编程, Spring框架, 企业应用开发

优化建议:
1. 在标题中包含主关键词
2. 在前100字内出现关键词
3. 使用H2、H3标题包含长尾关键词
4. 内部链接指向相关文章
5. 优化元描述（150-160字）
```

#### 场景5.3：多语言翻译
```
英文原文:
Our product is the best solution for enterprise applications.
It provides high performance, scalability, and reliability.

翻译成中文:
我们的产品是企业应用的最佳解决方案。
它提供高性能、可扩展性和可靠性。

翻译成日文:
当社の製品はエンタープライズアプリケーションの最適なソリューションです。
```

#### 场景5.4：社交媒体内容生成
```
主题: 新产品发布：AI代码助手
生成5条社交媒体帖子:

1. 🚀 激动地宣布：我们推出了AI代码助手！
   自动代码审查、Bug检测、文档生成...
   #AI #编程 #开发工具

2. 💡 厌倦了手动代码审查？
   我们的AI助手可以在几秒内完成...
   #开发效率 #AI工具

...
```

#### 场景5.5：完整的内容营销流程
```
步骤1: 生成主文章 (2000字)
步骤2: SEO优化
步骤3: 生成5条社交媒体帖子
步骤4: 翻译成中文
步骤5: 生成文章大纲

可交付物:
- 英文文章 (2000字)
- 中文文章 (2000字)
- 5条社交媒体帖子
- SEO优化建议
- 文章大纲
```

### 关键技术点
- ContentGenerationStatistics：生成统计
- 多语言支持
- SEO优化算法

### 运行示例
```bash
# 启动服务
cd scenario-5-content-creator
mvn spring-boot:run

# 生成文章
curl -X POST "http://localhost:8085/api/content-creator/generate-article?topic=AI&style=professional&wordCount=1000"

# SEO优化
curl -X POST "http://localhost:8085/api/content-creator/optimize-seo?content=...&keywords=AI,机器学习"
```

---

## 学习路径

### 初级（第1-2周）
1. 理解多轮对话的实现（场景1）
2. 学习会话管理和内存管理
3. 实现简单的客服对话

### 中级（第3-4周）
1. 学习文档处理和RAG（场景2）
2. 学习代码分析和审查（场景3）
3. 实现文档查询功能

### 高级（第5-6周）
1. 学习数据分析和SQL生成（场景4）
2. 学习内容生成和优化（场景5）
3. 实现完整的业务流程

---

## 快速开始

### 启动所有服务
```bash
# 终端1：客服系统
cd scenario-1-customer-service
mvn spring-boot:run

# 终端2：文档分析
cd scenario-2-document-analysis
mvn spring-boot:run

# 终端3：代码助手
cd scenario-3-code-assistant
mvn spring-boot:run

# 终端4：数据分析
cd scenario-4-data-analyst
mvn spring-boot:run

# 终端5：内容创作
cd scenario-5-content-creator
mvn spring-boot:run
```

### 使用Postman测试
```bash
# 导入Postman集合
postman-collection.json

# 或使用curl测试
curl -X POST "http://localhost:8081/api/customer-service/chat?message=你好"
```

---

## 关键概念

### 1. 多轮对话管理
- 维护对话历史
- 理解用户意图
- 生成上下文相关的回复

### 2. 文档处理和RAG
- 文本分块和分割
- 向量相似度搜索
- 检索增强生成

### 3. 代码分析
- 静态代码分析
- 安全漏洞检测
- 性能优化建议

### 4. 数据分析
- SQL自动生成
- 数据统计和分析
- 查询性能优化

### 5. 内容生成
- 文本生成和优化
- SEO优化
- 多语言翻译

---

## 最佳实践

1. **错误处理**：所有API都应该返回结构化的错误响应
2. **日志记录**：记录所有重要操作用于审计和调试
3. **性能优化**：使用缓存减少重复计算
4. **安全性**：验证所有用户输入，防止注入攻击
5. **可扩展性**：使用异步处理处理长时间运行的任务

---

## 常见问题

**Q: 如何集成真实的LLM？**
A: 替换mock实现，调用真实的LLM API（OpenAI、Claude等）

**Q: 如何处理大文档？**
A: 使用流式处理和分页，避免一次性加载整个文档

**Q: 如何提高响应速度？**
A: 使用缓存、异步处理、数据库索引优化

**Q: 如何支持更多语言？**
A: 在PromptTemplateUtil中添加新的系统提示词

---

## 下一步

1. 运行所有示例代码
2. 修改示例以适应你的业务需求
3. 集成真实的LLM API
4. 部署到生产环境
5. 监控和优化性能
