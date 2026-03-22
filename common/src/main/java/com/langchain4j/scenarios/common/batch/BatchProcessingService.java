package com.langchain4j.scenarios.common.batch;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 批量处理服务 - 大数据集的高效处理和分批执行
 *
 * 职责说明：
 * - 支持批量处理大数据集
 * - 分批执行操作，避免内存溢出
 * - 提高处理效率和系统性能
 * - 支持同步和异步处理
 * - 实现灵活的批次大小配置
 * - 支持任意类型的数据处理
 *
 * 架构设计：
 * - 使用泛型支持任意类型的数据
 * - 使用 Consumer 函数式接口定义处理逻辑
 * - 支持灵活的批次大小配置
 * - 支持同步和异步两种处理方式
 * - 易于集成和使用
 *
 * 使用场景：
 * - 批量导入大量数据到数据库
 * - 批量处理 LLM 推理任务
 * - 批量发送消息或通知
 * - 批量生成报告或导出数据
 * - 处理大文件时的分块处理
 * - 批量数据转换和清洗
 * - 批量索引和搜索优化
 *
 * 批量处理工作原理：
 * 1. 将大数据集分成多个小批次
 * 2. 逐个处理每个批次
 * 3. 每个批次处理完成后再处理下一个
 * 4. 避免一次性加载所有数据到内存
 * 5. 提高处理效率和系统稳定性
 *
 * 性能考虑：
 * - 批次大小应该根据内存和性能调整
 * - 同步处理会阻塞调用线程
 * - 异步处理不阻塞调用线程
 * - 建议使用线程池优化异步处理
 * - 监控批处理的进度和性能
 *
 * 安全考虑：
 * - 批次大小应该根据内存和性能调整
 * - 异步处理应该有错误处理机制
 * - 应该监控批处理的进度和性能
 * - 应该支持批处理的中断和恢复
 * - 应该记录批处理的日志
 *
 * 扩展建议：
 * - 可以添加进度监控和回调
 * - 可以实现批处理的中断和恢复
 * - 可以添加性能监控和统计
 * - 可以实现批处理的重试机制
 * - 可以支持分布式批处理
 */
@Service
@RequiredArgsConstructor
public class BatchProcessingService {

    /**
     * 批量处理数据（同步）
     *
     * 功能：
     * - 将数据分成多个批次
     * - 逐个处理每个批次
     * - 等待所有批次处理完成后返回
     * - 支持任意类型的数据和处理逻辑
     * - 实现内存高效的批处理
     *
     * 参数说明：
     * - data: 要处理的数据列表
     *   * 可以是任意类型的列表
     *   * 支持大数据集
     *   * 如果为 null 或空，直接返回
     *   * 示例：用户列表、产品列表、文档列表
     *
     * - batchSize: 每个批次的大小
     *   * 表示每次处理多少条数据
     *   * 建议值：
     *     * 小数据量：100-1000
     *     * 中等数据量：1000-10000
     *     * 大数据量：10000-100000
     *   * 应该根据内存和性能调整
     *   * 过小会导致处理次数过多
     *   * 过大会导致内存溢出
     *
     * - processor: 处理函数
     *   * Consumer<List<T>> 函数式接口
     *   * 接收一个批次的数据列表
     *   * 执行具体的处理逻辑
     *   * 处理完成后自动处理下一个批次
     *
     * 使用示例 - 批量插入数据库：
     * List<User> users = loadUsersFromFile();
     * batchProcessingService.processBatch(users, 1000, batch -> {
     *     userRepository.saveAll(batch);
     *     log.info(\\\"已插入 {} 条用户记录\\\", batch.size());
     * });
     *
     * 使用示例 - 批量调用 LLM A​PI：
     * List<String> documents = loadDocuments();
     * batchProcessingService.processBatch(documents, 100, batch -> {
     *     List<String> summaries = batch.stream()
     *         .map(doc -> llmService.summarize(doc))
     *         .collect(Collectors.toList());
     *     summaryRepository.saveAll(summaries);
     * });
     *
     * 使用示例 - 批量发送邮件：
     * List<String> emails = loadEmailAddresses();
     * batchProcessingService.processBatch(emails, 50, batch -> {
     *     batch.forEach(email -> emailService.send(email, \\\"Welcome!\\\"));
     * });
     *
     * 处理流程：
     * 1. 检查数据是否为 null 或空
     * 2. 从索引 0 开始，每次增加 batchSize
     * 3. 计算当前批次的结束位置
     * 4. 提取当前批次的数据子列表
     * 5. 调用 processor 处理当前批次
     * 6. 重复直到所有数据处理完成
     *
     * 性能考虑：
     * - 同步处理会阻塞调用线程
     * - 适合数据量不太大的场景
     * - 如果数据量很大，考虑使用异步处理
     * - 批次大小应该根据实际情况调整
     * - 建议监控处理时间和内存使用
     *
     * 错误处理：
     * - 如果 processor 抛出异常，会中断处理
     * - 应该在 processor 中处理异常
     * - 或者在调用处捕获异常
     * - 建议记录处理失败的批次信息
     *
     * 内存优化：
     * - 使用 subList 创建视图，不复制数据
     * - 创建新的 ArrayList 避免修改原列表
     * - 处理完成后及时释放引用
     * - 建议在处理大数据集时使用流式处理
     *
     * @param data 要处理的数据列表
     * @param batchSize 每个批次的大小
     * @param processor 处理函数，接收一个批次的数据
     * @param <T> 数据类型
     */
    public <T> void processBatch(List<T> data, int batchSize, Consumer<List<T>> processor) {
        if (data == null || data.isEmpty()) {
            return;
        }

        for (int i = 0; i < data.size(); i += batchSize) {
            int end = Math.min(i + batchSize, data.size());
            List<T> batch = new ArrayList<>(data.subList(i, end));
            processor.accept(batch);
        }
    }

    /**
     * 批量处理数据（异步）
     *
     * 功能：
     * - 在后台线程中执行批量处理
     * - 不阻塞调用线程
     * - 支持长时间运行的处理任务
     * - 提高应用响应速度
     * - 实现非阻塞的批处理
     *
     * 参数说明：
     * - data: 要处理的数据列表
     *   * 类型：List<T>
     *   * 示例：大量的产品列表
     *
     * - batchSize: 每个批次的大小
     *   * 范围：1 到数据总数
     *   * 示例：5000（每批处理 5000 条）
     *
     * - processor: 处理函数
     *   * Consumer<List<T>> 函数式接口
     *   * 在后台线程中执行
     *
     * 使用示例 - 异步批量导入：
     * List<Product> products = loadProductsFromFile();
     * batchProcessingService.processBatchAsync(products, 5000, batch -> {
     *     productRepository.saveAll(batch);
     *     log.info(\\\"已异步导入 {} 条产品记录\\\", batch.size());
     * });
     * // 立即返回，不等待处理完成
     *
     * 使用示例 - 异步批量生成报告：
     * List<String> reportIds = getReportIds();
     * batchProcessingService.processBatchAsync(reportIds, 100, batch -> {
     *     batch.forEach(id -> reportService.generateReport(id));
     * });
     *
     * 使用示例 - 异步批量处理文档：
     * List<Document> documents = loadDocuments();
     * batchProcessingService.processBatchAsync(documents, 50, batch -> {
     *     batch.forEach(doc -> {
     *         try {
     *             String summary = llmService.summarize(doc.getContent());
     *             doc.setSummary(summary);
     *             documentRepository.save(doc);
     *         } catch (Exception e) {
     *             log.error(\\\"处理文档失败: {}\\\", doc.getId(), e);
     *         }
     *     });
     * });
     *
     * 异步处理的优势：
     * - 不阻塞主线程
     * - 提高应用响应速度
     * - 支持长时间运行的任务
     * - 用户可以继续进行其他操作
     * - 提高系统吞吐量
     *
     * 异步处理的注意事项：
     * - 无法直接获取处理结果
     * - 无法直接捕获异常
     * - 应该在 processor 中处理异常
     * - 应该记录处理进度和结果
     * - 应该监控后台线程的状态
     *
     * 错误处理：
     * - 后台线程中的异常不会传播到调用者
     * - 应该在 processor 中使用 try-catch
     * - 应该记录错误日志
     * - 可以发送告警通知
     * - 建议实现重试机制
     *
     * 性能考虑：
     * - 创建新线程有开销
     * - 不适合频繁的小任务
     * - 适合大数据量的处理
     * - 可以考虑使用线程池优化
     * - 建议监控线程数量
     *
     * 改进建议：
     * - 使用 ExecutorService 替代 new Thread()
     * - 支持线程池的重用
     * - 支持任务的取消和超时
     * - 支持进度监控和回调
     * - 实现线程池的动态调整
     *
     * 示例改进版本：
     * @Autowired
     * private Executor taskExecutor;  // 来自 AsyncConfig
     *
     * public <T> void processBatchAsync(List<T> data, int batchSize, Consumer<List<T>> processor) {
     *     taskExecutor.execute(() -> processBatch(data, batchSize, processor));
     * }
     *
     * @param data 要处理的数据列表
     * @param batchSize 每个批次的大小
     * @param processor 处理函数，接收一个批次的数据
     * @param <T> 数据类型
     */
    public <T> void processBatchAsync(List<T> data, int batchSize, Consumer<List<T>> processor) {
        new Thread(() -> processBatch(data, batchSize, processor)).start();
    }
}
