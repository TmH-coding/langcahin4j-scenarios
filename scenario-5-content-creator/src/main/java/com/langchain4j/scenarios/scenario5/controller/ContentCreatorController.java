package com.langchain4j.scenarios.scenario5.controller;

import com.langchain4j.scenarios.scenario5.model.ContentGenerationResult;
import com.langchain4j.scenarios.scenario5.service.ContentCreatorService;
import com.langchain4j.scenarios.scenario5.service.ContentGenerationStatistics;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 内容创作 REST API 控制器 - 内容生成和优化的HTTP接口
 *
 * 职责说明：
 * - 提供内容生成、优化和翻译的HTTP接口
 * - 处理客户端的内容生成请求
 * - 调用 ContentCreatorService 执行业务逻辑
 * - 记录生成统计和性能指标
 * - 返回结构化的JSON响应
 *
 * 架构设计：
 * - 使用 @RestController 注解定义REST控制器
 * - 使用 @RequestMapping 定义基础路径 /api/content-creator
 * - 使用 @RequiredArgsConstructor 自动注入依赖
 * - 依赖 ContentCreatorService 进行内容生成
 * - 依赖 ContentGenerationStatistics 记录统计数据
 * - 支持多个独立的HTTP端点
 * - 使用 @PostMapping 和 @GetMapping 定义HTTP方法
 *
 * 使用场景：
 * - 自动生成文章和博客内容
 * - SEO优化和关键词管理
 * - 多语言内容翻译和本地化
 * - 社交媒体内容生成和批量发布
 * - 写作改进和质量检查
 * - 内容营销平台
 * - 自媒体运营工具
 * - 国际化内容管理
 *
 * HTTP端点说明：
 * - POST /api/content-creator/generate-article：生成文章
 * - POST /api/content-creator/optimize-seo：SEO优化
 * - POST /api/content-creator/translate：翻译内容
 * - POST /api/content-creator/social-posts：生成社交媒体帖子
 * - POST /api/content-creator/improve：改进写作
 * - POST /api/content-creator/outline：生成文章大纲
 * - GET /api/content-creator/stats：获取生成统计
 *
 * 工作原理：
 * 1. 客户端发送HTTP请求到相应端点
 * 2. 控制器接收请求参数
 * 3. 验证请求参数的有效性
 * 4. 调用 ContentCreatorService 执行业务逻辑
 * 5. 记录生成统计信息
 * 6. 构建 ContentGenerationResult 响应对象
 * 7. 返回JSON格式的响应
 * 8. 客户端接收并处理响应
 *
 * 字段说明：
 * - contentCreatorService: 内容创作服务
 *   * 类型：ContentCreatorService
 *   * 用途：执行内容生成和优化操作
 *   * 来源：Spring 依赖注入
 *
 * - statistics: 内容生成统计器
 *   * 类型：ContentGenerationStatistics
 *   * 用途：记录生成统计和性能指标
 *   * 来源：Spring 依赖注入
 *
 * 性能考虑：
 * - 内容生成时间取决于内容长度和复杂度
 * - LLM调用可能需要1-5秒
 * - 建议使用异步处理提高响应速度
 * - 可以配置请求超时时间
 * - 监控LLM调用的延迟
 * - 支持批量内容生成
 * - 可以缓存常用的生成模板
 * - 建议使用CDN加速静态资源
 *
 * 安全考虑：
 * - 验证请求参数防止注入攻击
 * - 限制请求体大小防止内存溢出
 * - 实现请求速率限制防止滥用
 * - 验证用户权限和配额
 * - 记录所有请求用于审计
 * - 实现请求内容的加密传输
 * - 防止敏感信息泄露
 * - 实现CORS跨域控制
 * - 启用HTTPS和SSL/TLS
 * - 实现请求签名验证
 *
 * 可靠性考虑：
 * - 处理LLM调用异常
 * - 实现请求超时控制
 * - 支持请求重试机制
 * - 记录详细的错误日志
 * - 实现优雅的降级处理
 * - 支持生成的恢复机制
 * - 处理并发请求
 * - 实现健康检查端点
 *
 * 扩展建议：
 * - 可以添加更多内容生成端点
 * - 可以实现异步内容生成
 * - 可以添加内容生成队列
 * - 可以支持内容生成模板
 * - 可以实现内容版本管理
 * - 可以添加内容审核功能
 * - 可以支持内容预览功能
 * - 可以实现内容导出功能
 * - 可以添加内容分析功能
 * - 可以支持内容协作编辑
 *
 * 端口：8085
 * 基础路径：/api/content-creator
 */
@RestController
@RequestMapping("/api/content-creator")
@RequiredArgsConstructor
public class ContentCreatorController {

    /** 注入内容创作服务 */
    private final ContentCreatorService contentCreatorService;

    /** 注入内容生成统计器 */
    private final ContentGenerationStatistics statistics;

    /**
     * 生成文章
     *
     * HTTP方法：POST
     * 端点：/api/content-creator/generate-article
     * 参数：
     * - topic: 文章主题
     * - style: 写作风格（默认：professional）
     * - wordCount: 目标字数（默认：1000）
     *
     * 使用示例：
     * POST /api/content-creator/generate-article?topic=AI&style=professional&wordCount=1000
     *
     * 功能：
     * - 根据主题生成文章
     * - 支持不同的写作风格
     * - 控制文章长度
     *
     * @param topic 文章主题
     * @param style 写作风格
     * @param wordCount 目标字数
     * @return 生成的文章
     */
    @PostMapping("/generate-article")
    public ContentGenerationResult generateArticle(@RequestParam String topic,
                                                   @RequestParam(defaultValue = "professional") String style,
                                                   @RequestParam(defaultValue = "1000") int wordCount) {
        try {
            String content = contentCreatorService.generateArticle(topic, style, wordCount);
            statistics.recordGeneration("article", wordCount);
            return ContentGenerationResult.builder()
                    .content(content)
                    .contentType("article")
                    .wordCount(wordCount)
                    .timestamp(System.currentTimeMillis())
                    .status("success")
                    .build();
        } catch (Exception e) {
            return ContentGenerationResult.builder()
                    .status("error")
                    .content(e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
    }

    /**
     * SEO优化
     *
     * HTTP方法：POST
     * 端点：/api/content-creator/optimize-seo
     * 参数：
     * - content: 要优化的内容
     * - keywords: 目标关键词（逗号分隔）
     *
     * 使用示例：
     * POST /api/content-creator/optimize-seo?content=...&keywords=AI,机器学习
     *
     * 功能：
     * - 优化内容以提高搜索引擎排名
     * - 集成关键词
     * - 优化标题和描述
     *
     * @param content 要优化的内容
     * @param keywords 目标关键词
     * @return 优化建议
     */
    @PostMapping("/optimize-seo")
    public String optimizeForSEO(@RequestParam String content, @RequestParam String keywords) {
        return contentCreatorService.optimizeForSEO(content, keywords);
    }

    /**
     * 翻译内容
     *
     * HTTP方法：POST
     * 端点：/api/content-creator/translate
     * 参数：
     * - content: 要翻译的内容
     * - targetLanguage: 目标语言
     *
     * 使用示例：
     * POST /api/content-creator/translate?content=...&targetLanguage=English
     *
     * 功能：
     * - 将内容翻译成目标语言
     * - 保持原意和风格
     * - 支持多种语言
     *
     * @param content 要翻译的内容
     * @param targetLanguage 目标语言
     * @return 翻译后的内容
     */
    @PostMapping("/translate")
    public String translateContent(@RequestParam String content, @RequestParam String targetLanguage) {
        return contentCreatorService.translateContent(content, targetLanguage);
    }

    /**
     * 生成社交媒体帖子
     *
     * HTTP方法：POST
     * 端点：/api/content-creator/social-posts
     * 参数：
     * - topic: 帖子主题
     * - postCount: 要生成的帖子数量（默认：5）
     *
     * 使用示例：
     * POST /api/content-creator/social-posts?topic=AI&postCount=5
     *
     * 功能：
     * - 为社交媒体生成内容
     * - 支持批量生成
     * - 包含相关的标签
     *
     * @param topic 帖子主题
     * @param postCount 帖子数量
     * @return 生成的社交媒体帖子
     */
    @PostMapping("/social-posts")
    public ContentGenerationResult generateSocialMediaPosts(@RequestParam String topic,
                                                           @RequestParam(defaultValue = "5") int postCount) {
        try {
            String content = contentCreatorService.generateSocialMediaPosts(topic, postCount);
            statistics.recordGeneration("social-post", postCount * 100);
            return ContentGenerationResult.builder()
                    .content(content)
                    .contentType("social-post")
                    .wordCount(postCount * 100)
                    .timestamp(System.currentTimeMillis())
                    .status("success")
                    .build();
        } catch (Exception e) {
            return ContentGenerationResult.builder()
                    .status("error")
                    .content(e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
    }

    /**
     * 改进写作
     *
     * HTTP方法：POST
     * 端点：/api/content-creator/improve
     * 参数：content - 要改进的内容
     *
     * 使用示例：
     * POST /api/content-creator/improve?content=...
     *
     * 功能：
     * - 分析文本质量
     * - 提供改进建议
     * - 改进语法和风格
     *
     * @param content 要改进的内容
     * @return 改进建议
     */
    @PostMapping("/improve")
    public String improveWriting(@RequestParam String content) {
        return contentCreatorService.improveWriting(content);
    }

    /**
     * 生成大纲
     *
     * HTTP方法：POST
     * 端点：/api/content-creator/outline
     * 参数：
     * - topic: 文章主题
     * - sections: 章节数量（默认：5）
     *
     * 使用示例：
     * POST /api/content-creator/outline?topic=AI&sections=5
     *
     * 功能：
     * - 为文章生成结构化大纲
     * - 支持自定义章节数
     * - 帮助组织内容
     *
     * @param topic 文章主题
     * @param sections 章节数量
     * @return 文章大纲
     */
    @PostMapping("/outline")
    public ContentGenerationResult generateOutline(@RequestParam String topic,
                                                  @RequestParam(defaultValue = "5") int sections) {
        try {
            String content = contentCreatorService.generateOutline(topic, sections);
            statistics.recordGeneration("outline", sections * 50);
            return ContentGenerationResult.builder()
                    .content(content)
                    .contentType("outline")
                    .wordCount(sections * 50)
                    .timestamp(System.currentTimeMillis())
                    .status("success")
                    .build();
        } catch (Exception e) {
            return ContentGenerationResult.builder()
                    .status("error")
                    .content(e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
    }

    /**
     * 获取生成统计
     *
     * HTTP方法：GET
     * 端点：/api/content-creator/stats
     *
     * @return 生成统计信息
     */
    @GetMapping("/stats")
    public String getStats() {
        return "Total generations: " + statistics.getTotalGenerations() +
               ", Total words: " + statistics.getTotalWordCount();
    }
}
