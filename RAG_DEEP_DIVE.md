# RAG 深度学习指南

## 第一部分：RAG 核心概念

### 什么是 RAG？

RAG = **R**etrieval **A**ugmented **G**eneration（检索增强生成）

**核心思想：** 不让 LLM 凭记忆回答，而是先从知识库中检索相关信息，再基于这些信息生成答案。

**对比：**

```
普通 LLM 调用：
用户问题 → LLM → 答案（基于训练数据）

RAG 流程：
用户问题 → 向量化 → 检索相关文档 → 构建提示词 → LLM → 答案（基于检索的文档）
```

**为什么需要 RAG？**
- LLM 的训练数据有截止日期（知识过时）
- LLM 容易产生幻觉（编造信息）
- 企业有私密的内部知识库
- 需要可追溯的信息来源

---

### RAG 的三个核心步骤

#### 步骤 1：向量化（Embedding）

**概念：** 将文本转换为数字向量

```
文本：     "我想退货"
         ↓
向量：    [0.123, -0.456, 0.789, ..., 0.234]  (384 维)
```

**为什么需要向量？**
- 计算机无法直接理解文本
- 向量可以计算相似度
- 向量空间中，相似的文本距离近

**Embedding 模型：**
- `AllMiniLmL6V2`：轻量级，384 维，速度快
- `OpenAI text-embedding-3-small`：更强大，1536 维
- `OpenAI text-embedding-3-large`：最强大，3072 维

#### 步骤 2：存储和检索

**存储：**
```
文档 1: "退货政策：7天内无理由退货"
  ↓ 向量化
向量 1: [0.123, -0.456, ...]
  ↓ 存储
向量数据库

文档 2: "退货费用：首次免费，之后 10 元/次"
  ↓ 向量化
向量 2: [0.234, -0.567, ...]
  ↓ 存储
向量数据库
```

**检索：**
```
用户问题：  "退货要多少钱？"
  ↓ 向量化
查询向量：  [0.245, -0.578, ...]
  ↓ 计算相似度
相似度 1：  0.92（与文档 2 相似）
相似度 2：  0.45（与文档 1 相似）
  ↓ 返回相似度最高的文档
检索结果：  文档 2
```

#### 步骤 3：构建 RAG 提示词

**普通提示词：**
```
系统提示词：你是一个客服
用户问题：退货要多少钱？
```

**RAG 提示词：**
```
系统提示词：你是一个客服，基于以下信息回答问题

检索到的信息：
- 退货政策：7天内无理由退货
- 退货费用：首次免费，之后 10 元/次
- 退货流程：联系客服 → 获取退货单 → 寄回 → 确认收货 → 退款

用户问题：退货要多少钱？
```

**LLM 的回答会基于检索到的信息，而不是凭记忆。**

---

## 第二部分：向量相似度计算

### 向量相似度的本质

**向量相似度 = 两个向量的接近程度**

```
向量 A: [1, 0, 0]
向量 B: [0.9, 0.1, 0]
向量 C: [0, 1, 0]

A 和 B 相似度高（都指向同一方向）
A 和 C 相似度低（指向不同方向）
```

### 常见的相似度计算方法

#### 1. 余弦相似度（Cosine Similarity）

**公式：**
```
cos(A, B) = (A · B) / (|A| × |B|)

其中：
- A · B 是点积
- |A| 和 |B| 是向量的模长
- 结果范围：-1 到 1（通常 0 到 1）
```

**例子：**
```
向量 A: [1, 0]
向量 B: [1, 0]
相似度 = 1.0（完全相同）

向量 A: [1, 0]
向量 C: [0, 1]
相似度 = 0.0（完全不同）

向量 A: [1, 0]
向量 D: [0.7, 0.7]
相似度 ≈ 0.7（部分相似）
```

**LangChain4j 中的使用：**
```java
// EmbeddingMatch 中的 score() 就是余弦相似度
List<EmbeddingMatch<TextSegment>> matches =
    embeddingStore.findRelevant(queryEmbedding, 3);

for (EmbeddingMatch<TextSegment> match : matches) {
    double similarity = match.score();  // 0.0 到 1.0
    System.out.println("相似度: " + similarity);
}
```

#### 2. 相似度阈值

**问题：** 什么样的相似度才算"相关"？

```
相似度 > 0.9：非常相关（几乎相同）
相似度 > 0.7：相关（同一主题）
相似度 > 0.5：可能相关（有一定关联）
相似度 < 0.5：不相关（完全不同）
```

**实践中的阈值设置：**
```java
List<EmbeddingMatch<TextSegment>> matches =
    embeddingStore.findRelevant(queryEmbedding, 10);

// 过滤低相似度结果
List<EmbeddingMatch<TextSegment>> relevant = matches.stream()
    .filter(m -> m.score() > 0.7)  // 只保留相似度 > 0.7 的
    .limit(3)                       // 最多返回 3 个
    .collect(Collectors.toList());
```

---

## 第三部分：Scenario-2 实战练习

### 架构概览

```
文档上传
  ↓
分块处理（Chunking）
  ↓
向量化（Embedding）
  ↓
存储到向量数据库
  ↓
用户查询
  ↓
向量化查询
  ↓
相似度检索
  ↓
构建 RAG 提示词
  ↓
LLM 生成答案
```

### 练习 3.1：启动 Scenario-2

```bash
# 进入项目目录
cd /c/Users/sdbj/Desktop/xz/langchain4j-scenarios

# 编译
mvn clean compile -pl scenario-2-document-analysis -am

# 启动（需要 OPENAI_A​PI_KEY）
export OPENAI_A​PI_KEY=sk-your-key-here
mvn spring-boot:run -pl scenario-2-document-analysis
```

**预期输出：**
```
Tomcat started on port(s): 8082
```

### 练习 3.2：上传文档

**创建测试文档 `test_doc.txt`：**

```
退货政策

1. 退货时间：自收货之日起 7 天内
2. 退货条件：商品未使用，包装完整
3. 退货流程：
   - 联系客服获取退货单号
   - 将商品寄回（运费自理）
   - 我们确认收货后进行退款
4. 退货费用：
   - 首次退货：免费
   - 第二次及以后：每次 10 元
5. 退款时间：确认收货后 3-5 个工作日

常见问题

Q: 过期了还能退货吗？
A: 不能。必须在 7 天内申请。

Q: 运费谁承担？
A: 退货运费由客户承担。

Q: 退款到哪里？
A: 原路返回到购买时的支付账户。
```

**上传文档：**

```bash
curl -X POST "http://localhost:8082/api/document-analysis/upload" \
  -F "file=@test_doc.txt" \
  -F "documentId=doc1"
```

**预期响应：**
```json
{
  "success": true,
  "message": "文档上传成功",
  "documentId": "doc1",
  "chunks": 5
}
```

### 练习 3.3：查询文档

**查询 1：直接相关的问题**

```bash
curl -X POST "http://localhost:8082/api/document-analysis/query/doc1" \
  -H "Content-Type: application/json" \
  -d '{"query":"退货要多少钱？"}'
```

**预期响应：**
```json
{
  "query": "退货要多少钱？",
  "answer": "根据我们的退货政策，退货费用如下：首次退货是免费的，第二次及以后每次需要 10 元。",
  "sources": [
    "退货费用：首次退货：免费，第二次及以后：每次 10 元"
  ]
}
```

**查询 2：间接相关的问题**

```bash
curl -X POST "http://localhost:8082/api/document-analysis/query/doc1" \
  -H "Content-Type: application/json" \
  -d '{"query":"怎样退货？"}'
```

**预期响应：**
```json
{
  "query": "怎样退货？",
  "answer": "退货流程如下：首先联系客服获取退货单号，然后将商品寄回（运费自理），最后我们确认收货后进行退款。",
  "sources": [
    "退货流程：联系客服获取退货单号，将商品寄回（运费自理），我们确认收货后进行退款"
  ]
}
```

**查询 3：不相关的问题**

```bash
curl -X POST "http://localhost:8082/api/document-analysis/query/doc1" \
  -H "Content-Type: application/json" \
  -d '{"query":"你们的产品质量怎么样？"}'
```

**预期响应：**
```json
{
  "query": "你们的产品质量怎么样？",
  "answer": "抱歉，我在文档中没有找到关于产品质量的信息。",
  "sources": []
}
```

---

## 第四部分：调试技巧

### 技巧 4.1：观察向量化过程

**修改 `DocumentAnalysisService.java`，添加调试输出：**

```java
public void loadDocument(String documentId, String content) {
    // 分块处理
    List<TextSegment> chunks = splitDocument(content, 512);
    System.out.println("文档分块数: " + chunks.size());

    // 向量化每个块
    for (int i = 0; i < chunks.size(); i++) {
        TextSegment chunk = chunks.get(i);

        long startTime = System.currentTimeMillis();
        Response<Embedding> response = embeddingModel.embed(chunk);
        long duration = System.currentTimeMillis() - startTime;

        Embedding embedding = response.content();
        float[] vector = embedding.vector();

        System.out.println("块 " + i + ":");
        System.out.println("  内容: " + chunk.text().substring(0, 50) + "...");
        System.out.println("  向量维度: " + vector.length);
        System.out.println("  向量化耗时: " + duration + "ms");

        // 存储
        embeddingStore.add(embedding, chunk);
    }
}
```

**预期输出：**
```
文档分块数: 5
块 0:
  内容: 退货政策

1. 退货时间：自收货之日起 7 天内...
  向量维度: 384
  向量化耗时: 234ms
块 1:
  内容: 2. 退货条件：商品未使用，包装完整...
  向量维度: 384
  向量化耗时: 198ms
...
```

### 技巧 4.2：观察检索过程

**修改 `queryDocument()` 方法：**

```java
public String queryDocument(String documentId, String query) {
    // 向量化查询
    long t1 = System.currentTimeMillis();
    Response<Embedding> queryResponse = embeddingModel.embed(query);
    long t2 = System.currentTimeMillis();

    Embedding queryEmbedding = queryResponse.content();
    System.out.println("查询向量化耗时: " + (t2-t1) + "ms");

    // 检索相似文档
    long t3 = System.currentTimeMillis();
    List<EmbeddingMatch<TextSegment>> matches =
        embeddingStore.findRelevant(queryEmbedding, 10);
    long t4 = System.currentTimeMillis();

    System.out.println("检索耗时: " + (t4-t3) + "ms");
    System.out.println("检索结果数: " + matches.size());

    // 打印相似度
    for (int i = 0; i < matches.size(); i++) {
        EmbeddingMatch<TextSegment> match = matches.get(i);
        double similarity = match.score();
        String text = match.embedded().text().substring(0, 50);

        System.out.println("[" + i + "] 相似度: " + String.format("%.4f", similarity)
            + " | " + text + "...");
    }

    // 过滤相似度
    List<EmbeddingMatch<TextSegment>> relevant = matches.stream()
        .filter(m -> m.score() > 0.7)
        .limit(3)
        .collect(Collectors.toList());

    System.out.println("过滤后结果数（相似度 > 0.7）: " + relevant.size());

    // 构建 RAG 提示词
    String context = relevant.stream()
        .map(m -> m.embedded().text())
        .collect(Collectors.joining("\n\n"));

    // 调用 LLM
    String ragPrompt = "基于以下信息回答问题:\n" + context + "\n\n问题: " + query;
    // ... 调用 LLM
}
```

**预期输出：**
```
查询向量化耗时: 145ms
检索耗时: 23ms
检索结果数: 10
[0] 相似度: 0.9234 | 退货费用：首次退货：免费，第二次及以后：每次 10 元
[1] 相似度: 0.8567 | 退货流程：联系客服获取退货单号，将商品寄回...
[2] 相似度: 0.7823 | 退货时间：自收货之日起 7 天内
[3] 相似度: 0.6234 | 常见问题 Q: 过期了还能退货吗？
...
过滤后结果数（相似度 > 0.7）: 3
```

### 技巧 4.3：调整相似度阈值

**问题：** 检索结果不相关

**诊断：**
```java
// 查看所有相似度分数
List<EmbeddingMatch<TextSegment>> matches =
    embeddingStore.findRelevant(queryEmbedding, 10);

System.out.println("相似度分布：");
for (EmbeddingMatch<TextSegment> match : matches) {
    System.out.println("  " + String.format("%.4f", match.score()));
}
```

**解决方案：**
```
如果最高相似度 < 0.5：
  - 可能是 Embedding 模型不适合
  - 或者文档分块太小
  - 或者查询和文档差异太大

如果相似度分布不均匀（如 0.9, 0.3, 0.2）：
  - 调整阈值到 0.7 或 0.8
  - 只返回相似度最高的 3 个

如果相似度都很低（都 < 0.6）：
  - 考虑使用更强大的 Embedding 模型
  - 或者重新分块文档
```

---

## 第五部分：常见问题

### Q1：为什么检索结果不相关？

**原因 1：Embedding 模型不适合**
```java
// 当前使用的是轻量级模型
EmbeddingModel model = new AllMiniLmL6V2EmbeddingModel();

// 改用更强大的模型
EmbeddingModel model = new OpenAiEmbeddingModel.builder()
    .modelName("text-embedding-3-large")
    .build();
```

**原因 2：文档分块不合理**
```java
// 块太小（< 100 字符）：信息不足
// 块太大（> 1000 字符）：信息混杂

// 推荐：512 字符，50 字符重叠
List<TextSegment> chunks = splitDocument(content, 512, 50);
```

**原因 3：相似度阈值设置不当**
```java
// 阈值太高（> 0.9）：漏掉相关结果
// 阈值太低（< 0.5）：包含无关结果

// 推荐：0.7
List<EmbeddingMatch<TextSegment>> relevant = matches.stream()
    .filter(m -> m.score() > 0.7)
    .collect(Collectors.toList());
```

### Q2：向量化速度太慢怎么办？

**原因 1：使用了太强大的模型**
```java
// 改用轻量级模型
EmbeddingModel model = new AllMiniLmL6V2EmbeddingModel();  // 快
// 而不是
EmbeddingModel model = new OpenAiEmbeddingModel(...);     // 慢
```

**原因 2：逐个向量化**
```java
// 不好：逐个向量化
for (TextSegment chunk : chunks) {
    embeddingModel.embed(chunk);  // 每次都调用 API
}

// 好：批量向量化（如果模型支持）
List<Response<Embedding>> responses = embeddingModel.embedAll(chunks);
```

### Q3：内存占用过高怎么办？

**原因 1：使用了内存存储**
```java
// 当前使用的是内存存储
EmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();

// 改用专业向量数据库
EmbeddingStore<TextSegment> store = new MilvusEmbeddingStore.builder()
    .host("localhost")
    .port(19530)
    .build();
```

**原因 2：文档过多**
```java
// 定期清理旧文档
if (embeddingStore.size() > 10000) {
    embeddingStore.clear();
    System.out.println("向量存储已清理");
}
```

---

## 第六部分：性能优化

### 优化 1：缓存查询结果

```java
@Service
public class DocumentAnalysisService {
    private final Cache<String, String> queryCache = CacheBuilder.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(1, TimeUnit.HOURS)
        .build();

    public String queryDocument(String documentId, String query) {
        String cacheKey = documentId + ":" + query;
        String cached = queryCache.getIfPresent(cacheKey);

        if (cached != null) {
            System.out.println("缓存命中");
            return cached;
        }

        // 执行查询
        String result = performQuery(documentId, query);
        queryCache.put(cacheKey, result);
        return result;
    }
}
```

### 优化 2：异步向量化

```java
@Service
public class DocumentAnalysisService {
    @Async
    public CompletableFuture<Void> loadDocumentAsync(String documentId, String content) {
        loadDocument(documentId, content);
        return CompletableFuture.completedFuture(null);
    }
}

// 使用
documentService.loadDocumentAsync("doc1", content)
    .thenRun(() -> System.out.println("文档加载完成"));
```

### 优化 3：批量检索

```java
// 不好：逐个查询
for (String query : queries) {
    queryDocument(documentId, query);
}

// 好：并行查询
List<String> results = queries.parallelStream()
    .map(query -> queryDocument(documentId, query))
    .collect(Collectors.toList());
```

---

## 学习检查清单

- [ ] 理解 RAG 的三个步骤
- [ ] 理解向量化的概念
- [ ] 理解相似度计算
- [ ] 能够启动 Scenario-2
- [ ] 能够上传和查询文档
- [ ] 理解相似度阈值的作用
- [ ] 能够调试检索过程
- [ ] 理解常见问题的解决方案
- [ ] 能够优化性能

---

## 下一步

完成 RAG 学习后，你可以：

1. **进阶：多文档 RAG**
   - 同时查询多个文档
   - 实现文档排序和融合

2. **进阶：混合检索**
   - 结合关键词检索和向量检索
   - 提高检索准确率

3. **进阶：Agent + RAG**
   - 让 Agent 自动选择查询哪个文档
   - 实现更复杂的知识库查询

准备好进入下一阶段吗？
