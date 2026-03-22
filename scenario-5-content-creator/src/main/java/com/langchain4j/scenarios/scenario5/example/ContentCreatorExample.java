package com.langchain4j.scenarios.scenario5.example;

import com.langchain4j.scenarios.scenario5.service.ContentCreatorService;
import org.springframework.stereotype.Component;

/**
 * 内容创作实战示例 - 真实场景的内容生成演示
 *
 * 职责说明：
 * - 展示内容创作系统的实际应用场景
 * - 演示文章生成、SEO优化、翻译、社交媒体内容的完整流程
 * - 提供学习和测试的示例代码
 * - 展示不同类型的内容生成场景
 *
 * 架构设计：
 * - 使用 @Component 注解注册为 Spring Bean
 * - 依赖 ContentCreatorService 进行内容生成
 * - 提供多个独立的示例方法
 * - 支持示例的独立运行和组合运行
 * - 用于演示和测试目的
 *
 * 使用场景：
 * - 内容营销平台
 * - 自媒体运营工具
 * - 国际化内容管理
 * - 博客和文章生成
 * - SEO优化和关键词管理
 * - 多语言内容翻译
 * - 社交媒体内容生成
 *
 * 真实场景说明：
 * 1. 博客文章生成：快速生成高质量文章
 * 2. SEO优化：提升搜索引擎排名
 * 3. 多语言翻译：快速进入新市场
 * 4. 社交媒体：批量生成社交内容
 * 5. 内容改进：提高内容质量
 * 6. 完整流程：端到端的内容营销工作流
 *
 * 工作原理：
 * 1. 每个示例方法模拟一个真实的内容生成场景
 * 2. 定义内容生成需求和参数
 * 3. 执行内容生成和优化操作
 * 4. 展示生成结果和建议
 * 5. 说明最佳实践和优化方向
 * 6. 可以运行单个示例或所有示例
 *
 * 性能考虑：
 * - 每个示例包含2-5个生成操作
 * - 总执行时间通常 < 10 秒
 * - 可以并行运行多个示例
 * - 建议在测试环境中运行
 * - 支持大规模内容生成
 *
 * 安全考虑：
 * - 示例中使用的数据是虚拟的
 * - 不包含真实的用户信息
 * - 可以安全地在任何环境中运行
 * - 不会修改生产数据
 * - 不会发送真实的社交媒体帖子
 *
 * 扩展建议：
 * - 可以添加更多真实场景示例
 * - 可以实现自动化测试用例
 * - 可以添加性能基准测试
 * - 可以实现场景录制和回放
 * - 可以支持自定义场景脚本
 * - 可以添加场景的参数化
 * - 可以支持场景的并行执行
 * - 可以添加场景的性能分析
 */
@Component
public class ContentCreatorExample {

    private final ContentCreatorService contentCreatorService;

    public ContentCreatorExample(ContentCreatorService contentCreatorService) {
        this.contentCreatorService = contentCreatorService;
    }

    /**
     * 示例1：博客文章生成
     *
     * 业务场景：
     * - 需要快速生成高质量博客文章
     * - 支持不同的写作风格
     * - 控制文章长度
     */
    public void blogArticleGenerationExample() {
        System.out.println("=== 博客文章生成 ===");

        // 场景1：专业技术文章
        String technicalArticle = contentCreatorService.generateArticle(
            "Java并发编程最佳实践",
            "professional",
            2000
        );
        System.out.println("技术文章 (2000字):");
        System.out.println(technicalArticle);

        // 场景2：轻松易懂的文章
        String casualArticle = contentCreatorService.generateArticle(
            "如何选择适合自己的编程语言",
            "casual",
            1500
        );
        System.out.println("\n轻松文章 (1500字):");
        System.out.println(casualArticle);

        // 场景3：学术风格文章
        String academicArticle = contentCreatorService.generateArticle(
            "人工智能在医疗领域的应用",
            "academic",
            3000
        );
        System.out.println("\n学术文章 (3000字):");
        System.out.println(academicArticle);
    }

    /**
     * 示例2：SEO优化
     *
     * 业务场景：
     * - 优化现有内容以提升搜索排名
     * - 集成目标关键词
     * - 优化标题和描述
     */
    public void seoOptimizationExample() {
        System.out.println("\n=== SEO优化 ===");

        String originalContent = """
            Java是一种编程语言。它很流行。
            很多公司使用Java开发应用。
            Java有很多框架。
            """;

        String keywords = "Java编程,Spring框架,企业应用开发";

        System.out.println("原始内容:");
        System.out.println(originalContent);

        String optimizedContent = contentCreatorService.optimizeForSEO(originalContent, keywords);
        System.out.println("\nSEO优化建议:");
        System.out.println(optimizedContent);

        System.out.println("\n优化要点:");
        System.out.println("1. 在标题中包含主关键词");
        System.out.println("2. 在前100字内出现关键词");
        System.out.println("3. 使用H2、H3标题包含长尾关键词");
        System.out.println("4. 内部链接指向相关文章");
        System.out.println("5. 优化元描述（150-160字）");
    }

    /**
     * 示例3：多语言翻译
     *
     * 业务场景：
     * - 快速进入国际市场
     * - 翻译营销文案
     * - 保持品牌一致性
     */
    public void multiLanguageTranslationExample() {
        System.out.println("\n=== 多语言翻译 ===");

        String englishContent = """
            Our product is the best solution for enterprise applications.
            It provides high performance, scalability, and reliability.
            Join thousands of companies using our platform.
            """;

        System.out.println("英文原文:");
        System.out.println(englishContent);

        // 翻译成中文
        String chineseContent = contentCreatorService.translateContent(englishContent, "Chinese");
        System.out.println("\n中文翻译:");
        System.out.println(chineseContent);

        // 翻译成日文
        String japaneseContent = contentCreatorService.translateContent(englishContent, "Japanese");
        System.out.println("\n日文翻译:");
        System.out.println(japaneseContent);

        // 翻译成西班牙文
        String spanishContent = contentCreatorService.translateContent(englishContent, "Spanish");
        System.out.println("\n西班牙文翻译:");
        System.out.println(spanishContent);
    }

    /**
     * 示例4：社交媒体内容生成
     *
     * 业务场景：
     * - 批量生成社交媒体帖子
     * - 包含相关标签
     * - 优化参与度
     */
    public void socialMediaContentExample() {
        System.out.println("\n=== 社交媒体内容生成 ===");

        // 场景1：产品发布
        String productLaunchPosts = contentCreatorService.generateSocialMediaPosts(
            "新产品发布：AI代码助手",
            5
        );
        System.out.println("产品发布帖子 (5条):");
        System.out.println(productLaunchPosts);

        // 场景2：技术分享
        String techSharePosts = contentCreatorService.generateSocialMediaPosts(
            "Java性能优化技巧",
            3
        );
        System.out.println("\n技术分享帖子 (3条):");
        System.out.println(techSharePosts);

        System.out.println("\n社交媒体最佳实践:");
        System.out.println("1. 使用相关的#标签提高可发现性");
        System.out.println("2. 在最佳发布时间发送（通常是工作日上午9-11点）");
        System.out.println("3. 包含号召性用语（CTA）");
        System.out.println("4. 使用表情符号增加视觉吸引力");
        System.out.println("5. 定期与粉丝互动");
    }

    /**
     * 示例5：内容改进和大纲生成
     *
     * 业务场景：
     * - 改进现有内容质量
     * - 为新文章生成结构
     * - 提高内容一致性
     */
    public void contentImprovementExample() {
        System.out.println("\n=== 内容改进和大纲生成 ===");

        // 场景1：改进现有内容
        String poorContent = """
            Java很好。很多人用。可以做很多事情。
            它有框架。框架很有用。
            """;

        System.out.println("原始内容（质量差）:");
        System.out.println(poorContent);

        String improvement = contentCreatorService.improveWriting(poorContent);
        System.out.println("\n改进建议:");
        System.out.println(improvement);

        // 场景2：生成文章大纲
        String outline = contentCreatorService.generateOutline(
            "微服务架构设计",
            5
        );
        System.out.println("\n文章大纲 (5个章节):");
        System.out.println(outline);

        System.out.println("\n内容改进要点:");
        System.out.println("1. 使用主动语态而不是被动语态");
        System.out.println("2. 避免重复和冗余");
        System.out.println("3. 使用具体的例子和数据");
        System.out.println("4. 保持段落简洁（3-4句话）");
        System.out.println("5. 使用清晰的标题和子标题");
    }

    /**
     * 示例6：完整的内容营销流程
     *
     * 业务流程：
     * 1. 生成文章
     * 2. 优化SEO
     * 3. 生成社交媒体帖子
     * 4. 翻译成其他语言
     */
    public void completeContentMarketingWorkflowExample() {
        System.out.println("\n=== 完整的内容营销流程 ===");

        String topic = "云计算安全最佳实践";

        System.out.println("步骤1: 生成主文章");
        String article = contentCreatorService.generateArticle(topic, "professional", 2000);
        System.out.println("✓ 已生成2000字专业文章");

        System.out.println("\n步骤2: SEO优化");
        String seoOptimized = contentCreatorService.optimizeForSEO(
            article,
            "云计算安全,数据保护,合规性"
        );
        System.out.println("✓ 已优化SEO");

        System.out.println("\n步骤3: 生成社交媒体内容");
        String socialPosts = contentCreatorService.generateSocialMediaPosts(topic, 5);
        System.out.println("✓ 已生成5条社交媒体帖子");

        System.out.println("\n步骤4: 翻译成中文");
        String chineseArticle = contentCreatorService.translateContent(article, "Chinese");
        System.out.println("✓ 已翻译成中文");

        System.out.println("\n步骤5: 生成文章大纲");
        String outline = contentCreatorService.generateOutline(topic, 5);
        System.out.println("✓ 已生成文章大纲");

        System.out.println("\n完整流程完成！");
        System.out.println("可交付物:");
        System.out.println("- 英文文章 (2000字)");
        System.out.println("- 中文文章 (2000字)");
        System.out.println("- 5条社交媒体帖子");
        System.out.println("- SEO优化建议");
        System.out.println("- 文章大纲");
    }

    /**
     * 运行所有示例
     */
    public void runAllExamples() {
        blogArticleGenerationExample();
        seoOptimizationExample();
        multiLanguageTranslationExample();
        socialMediaContentExample();
        contentImprovementExample();
        completeContentMarketingWorkflowExample();
    }
}
