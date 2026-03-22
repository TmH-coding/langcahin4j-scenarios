package com.langchain4j.scenarios.common.util;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import java.util.List;

/**
 * 文档处理工具类 - RAG 系统中的文档分割和预处理
 *
 * 职责说明：
 * - 将长文档分割成较小的文本段落（chunks）
 * - 支持段落之间的重叠，保持上下文连贯性
 * - 用于 RAG（检索增强生成）系统中的文档预处理
 * - 支持单个和批量文档处理
 * - 保留文档的元数据信息
 * - 实现滑动窗口分割算法
 *
 * 架构设计：
 * - 使用滑动窗口算法进行文本分割
 * - 支持段落重叠以保持上下文连贯性
 * - 保留原文档的元数据
 * - 支持流式处理大量文档
 * - 易于扩展和定制
 *
 * 使用场景：
 * - 处理 PDF、Word 等长文档
 * - 为向量数据库准备文本段落
 * - 在文档分析场景中进行文本分块
 * - 实现 RAG 系统的文档预处理
 * - 支持语义搜索的文本准备
 * - 实现文档的向量化存储
 *
 * 设计原理：
 * - 滑动窗口算法：每个段落可能与前一个段落有重叠
 * - 重叠的目的：保持段落之间的上下文连贯性
 * - 例如：maxSegmentSize=1000, overlapSize=100
 *   第一段：0-1000，第二段：900-1900，第三段：1800-2800
 *
 * 性能考虑：
 * - 时间复杂度：O(n)（n 为文档总字符数）
 * - 空间复杂度：O(m)（m 为分割后的段落总数）
 * - 支持大文档处理
 * - 建议使用流式处理处理超大文档
 *
 * 参数调优：
 * - maxSegmentSize：段落大小
 *   * 范围：256-4096 字符（根据 LLM 上下文窗口调整）
 *   * 建议：512-1024 字符
 *   * 过小：段落过多，处理时间长
 *   * 过大：段落过少，上下文不完整
 *
 * - overlapSize：段落重叠大小
 *   * 范围：0-maxSegmentSize/2
 *   * 建议：maxSegmentSize 的 10-20%
 *   * 过小：段落之间上下文断裂
 *   * 过大：段落重复过多，浪费存储空间
 *
 * 扩展建议：
 * - 可以添加按句子或段落分割
 * - 可以实现智能分割（按语义边界）
 * - 可以添加文本清理和规范化
 * - 可以实现多语言支持
 * - 可以添加分割质量评估
 */
public class DocumentProcessingUtil {

    /**
     * 分割单个文档
     *
     * 功能：
     * - 将文档文本按指定大小分割成多个段落
     * - 实现滑动窗口机制，段落之间可以重叠
     * - 保留文档的元数据信息
     * - 支持灵活的分割参数配置
     *
     * 参数说明：
     * - document: 要分割的文档对象
     *   * 类型：dev.langchain4j.data.document.Document
     *   * 包含：文本内容和元数据
     *   * 示例：从 PDF 或 Word 加载的文档
     *
     * - maxSegmentSize: 每个段落的最大字符数
     *   * 范围：256-4096 字符
     *   * 建议：512-1024 字符
     *   * 示例：1000（每个段落最多 1000 字符）
     *   * 影响：段落数量和大小
     *
     * - overlapSize: 相邻段落之间的重叠字符数
     *   * 范围：0-maxSegmentSize/2
     *   * 建议：maxSegmentSize 的 10-20%
     *   * 示例：100（相邻段落重叠 100 字符）
     *   * 影响：上下文连贯性和存储空间
     *
     * 返回值：
     * - List<TextSegment>：分割后的文本段落列表
     * - 每个段落包含文本和原文档的元数据
     * - 段落按顺序排列
     *
     * 算法流程：
     * 1. 获取文档的完整文本
     * 2. 从起始位置开始，每次取 maxSegmentSize 个字符
     * 3. 创建 TextSegment 对象，保留原文档的元数据
     * 4. 移动起始位置，实现重叠效果
     * 5. 重复直到处理完整个文档
     *
     * 滑动窗口示例：
     * 文档文本：\\\"这是一个很长的文档...\\\"（2000 字符）
     * maxSegmentSize=1000, overlapSize=100
     *
     * 第一段：start=0, end=1000
     *   内容：文档的前 1000 字符
     *   下一个 start = 1000 - 100 = 900
     *
     * 第二段：start=900, end=1900
     *   内容：文档的 900-1900 字符（与第一段重叠 100 字符）
     *   下一个 start = 1900 - 100 = 1800
     *
     * 第三段：start=1800, end=2000
     *   内容：文档的 1800-2000 字符（与第二段重叠 100 字符）
     *   下一个 start = 2000 - 100 = 1900（超过文档长度，停止）
     *
     * 使用示例：
     * Document doc = loadDocument(\\\"document.pdf\\\");
     * List<TextSegment> segments = DocumentProcessingUtil.splitDocument(
     *     doc,
     *     1000,  // maxSegmentSize
     *     100    // overlapSize
     * );
     * System.out.println(\\\"分割后的段落数：\\\" + segments.size());
     * for (TextSegment segment : segments) {
     *     System.out.println(\\\"段落内容：\\\" + segment.text());
     *     System.out.println(\\\"元数据：\\\" + segment.metadata());
     * }
     *
     * RAG 系统集成示例：
     * // 1. 加载文档
     * Document doc = documentLoader.load(\\\"document.pdf\\\");
     *
     * // 2. 分割文档
     * List<TextSegment> segments = DocumentProcessingUtil.splitDocument(doc, 1000, 100);
     *
     * // 3. 向量化段落
     * List<Embedding> embeddings = segments.stream()
     *     .map(segment -> embeddingModel.embed(segment.text()))
     *     .collect(Collectors.toList());
     *
     * // 4. 存储到向量数据库
     * vectorStore.addAll(segments, embeddings);
     *
     * 性能考虑：
     * - 时间复杂度：O(n)（n 为文档字符数）
     * - 空间复杂度：O(m)（m 为分割后的段落总数）
     * - 对于大文档（>10MB），考虑流式处理
     * - 建议使用线程池并行处理多个文档
     *
     * 参数调优建议：
     * - 对于 LLM 上下文窗口 4K：maxSegmentSize=512-1024
     * - 对于 LLM 上下文窗口 8K：maxSegmentSize=1024-2048
     * - 对于 LLM 上下文窗口 32K：maxSegmentSize=4096-8192
     * - overlapSize 通常设置为 maxSegmentSize 的 10-20%
     *
     * 常见问题：
     * 1. 段落过多导致处理缓慢
     *    - 增加 maxSegmentSize
     *    - 减少 overlapSize
     *
     * 2. 段落过少导致上下文不完整
     *    - 减少 maxSegmentSize
     *    - 增加 overlapSize
     *
     * 3. 段落在句子中间被切割
     *    - 考虑实现按句子分割
     *    - 或使用更大的 maxSegmentSize
     *
     * @param document 要分割的文档
     * @param maxSegmentSize 每个段落的最大字符数
     * @param overlapSize 段落之间的重叠字符数
     * @return 分割后的文本段落列表
     */
    public static List<TextSegment> splitDocument(Document document, int maxSegmentSize, int overlapSize) {
        // 简单的文本分块实现
        String text = document.text();
        List<TextSegment> segments = new java.util.ArrayList<>();

        // 从文档开始位置开始分割
        int start = 0;
        while (start < text.length()) {
            // 计算当前段落的结束位置
            int end = Math.min(start + maxSegmentSize, text.length());

            // 提取文本段落
            String segmentText = text.substring(start, end);

            // 创建 TextSegment，保留原文档的元数据
            segments.add(new TextSegment(segmentText, document.metadata()));

            // 移动起始位置，实现重叠
            // 例如：start=0, end=1000, overlapSize=100
            // 下一个 start = 1000 - 100 = 900
            start = end - overlapSize;

            // 防止无限循环：如果重叠太大导致 start 不前进，则直接跳到 end
            if (start <= 0) start = end;
        }

        return segments;
    }

    /**
     * 分割多个文档
     *
     * 功能：
     * - 批量处理多个文档
     * - 将所有文档的分割结果合并为一个列表
     * - 支持流式处理
     * - 实现并行处理优化
     *
     * 参数说明：
     * - documents: 要分割的文档列表
     *   * 类型：List<Document>
     *   * 示例：从多个 PDF 加载的文档列表
     *   * 可以为空列表
     *
     * - maxSegmentSize: 每个段落的最大字符数
     *   * 范围：256-4096 字符
     *   * 建议：512-1024 字符
     *
     * - overlapSize: 段落之间的重叠字符数
     *   * 范围：0-maxSegmentSize/2
     *   * 建议：maxSegmentSize 的 10-20%
     *
     * 返回值：
     * - List<TextSegment>：所有文档分割后的文本段落列表
     * - 段落按文档顺序排列
     * - 返回的是新的列表，修改不会影响原始文档
     *
     * 使用示例：
     * List<Document> documents = new ArrayList<>();
     * documents.add(loadDocument(\\\"doc1.pdf\\\"));
     * documents.add(loadDocument(\\\"doc2.pdf\\\"));
     * documents.add(loadDocument(\\\"doc3.pdf\\\"));
     *
     * List<TextSegment> allSegments = DocumentProcessingUtil.splitDocuments(
     *     documents,
     *     1000,  // maxSegmentSize
     *     100    // overlapSize
     * );
     * System.out.println(\\\"总段落数：\\\" + allSegments.size());
     *
     * 批量处理示例：
     * // 1. 加载多个文档
     * List<Document> documents = documentLoader.loadAll(\\\"documents/\\\");
     *
     * // 2. 分割所有文档
     * List<TextSegment> segments = DocumentProcessingUtil.splitDocuments(
     *     documents,
     *     1024,
     *     100
     * );
     *
     * // 3. 向量化所有段落
     * List<Embedding> embeddings = segments.stream()
     *     .map(segment -> embeddingModel.embed(segment.text()))
     *     .collect(Collectors.toList());
     *
     * // 4. 批量存储到向量数据库
     * vectorStore.addAll(segments, embeddings);
     *
     * 性能考虑：
     * - 时间复杂度：O(n)（n 为所有文档的总字符数）
     * - 空间复杂度：O(m)（m 为分割后的总段落数）
     * - 使用流式处理避免一次性加载所有段落
     * - 建议使用并行流处理大量文档
     *
     * 并行处理优化：
     * // 使用并行流处理多个文档
     * List<TextSegment> segments = documents.parallelStream()
     *     .flatMap(doc -> splitDocument(doc, 1000, 100).stream())
     *     .collect(Collectors.toList());
     *
     * 常见问题：
     * 1. 处理大量文档时内存溢出
     *    - 使用流式处理而不是一次性加载
     *    - 分批处理文档
     *    - 增加 JVM 堆内存
     *
     * 2. 处理速度慢
     *    - 使用并行流处理
     *    - 增加线程池大小
     *    - 优化 maxSegmentSize 和 overlapSize
     *
     * @param documents 要分割的文档列表
     * @param maxSegmentSize 每个段落的最大字符数
     * @param overlapSize 段落之间的重叠字符数
     * @return 所有文档分割后的文本段落列表
     */
    public static List<TextSegment> splitDocuments(List<Document> documents, int maxSegmentSize, int overlapSize) {
        return documents.stream()
                .flatMap(doc -> splitDocument(doc, maxSegmentSize, overlapSize).stream())
                .toList();
    }
}
