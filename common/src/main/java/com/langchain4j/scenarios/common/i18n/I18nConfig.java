package com.langchain4j.scenarios.common.i18n;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import java.util.Locale;

/**
 * 国际化配置 - 多语言支持和消息本地化
 *
 * 职责说明：
 * - 配置 Spring 国际化（i18n）框架
 * - 支持多语言消息资源
 * - 实现语言自动切换和持久化
 * - 提供统一的消息访问接口
 *
 * 架构设计：
 * - 使用 ResourceBundleMessageSource 管理消息资源
 * - 使用 CookieLocaleResolver 解析和存储用户语言偏好
 * - 支持消息缓存提高性能
 * - 支持 UTF-8 编码处理多字节字符
 *
 * 使用场景：
 * - 多语言应用支持（中文、英文等）
 * - 错误消息本地化
 * - 验证消息本地化
 * - 业务消息本地化
 * - 用户界面文本本地化
 *
 * 国际化工作流程：
 * 1. 用户请求到达时，LocaleResolver 解析用户语言偏好
 * 2. 从 Cookie 中读取用户选择的语言
 * 3. 如果 Cookie 中没有，使用默认语言（简体中文）
 * 4. MessageSource 根据语言加载对应的消息资源
 * 5. 应用使用本地化的消息进行响应
 *
 * 消息资源文件结构：
 * - messages.properties - 默认消息（英文）
 * - messages_zh_CN.properties - 简体中文消息
 * - messages_en_US.properties - 美国英文消息
 * - messages_ja_JP.properties - 日文消息
 *
 * 性能考虑：
 * - 消息缓存 1 小时，减少文件 I/O
 * - 缓存时间过短会导致频繁重新加载
 * - 缓存时间过长会导致消息更新延迟
 * - 可以通过清除缓存强制重新加载
 *
 * 扩展建议：
 * - 可以添加数据库消息源支持
 * - 可以实现动态消息加载
 * - 可以添加消息版本控制
 * - 可以实现消息热更新
 */
@Configuration
public class I18nConfig {

    /**
     * 配置消息源
     *
     * 功能：
     * - 加载国际化消息资源文件
     * - 支持多语言消息查询
     * - 缓存消息提高性能
     * - 支持 UTF-8 编码
     *
     * 消息源配置参数详解：
     *
     * setBasename("messages")
     * - 消息资源文件的基础名称
     * - Spring 会自动查找以下文件：
     *   * messages.properties - 默认消息（英文）
     *   * messages_zh_CN.properties - 简体中文
     *   * messages_en_US.properties - 美国英文
     *   * messages_ja_JP.properties - 日文
     * - 文件位置：classpath 根目录或 resources 目录
     * - 示例文件路径：src/main/resources/messages_zh_CN.properties
     *
     * setDefaultEncoding("UTF-8")
     * - 消息文件的字符编码
     * - UTF-8 支持所有语言的字符
     * - 确保消息文件使用 UTF-8 编码保存
     * - 避免中文、日文等字符乱码
     *
     * setCacheSeconds(3600)
     * - 消息缓存时间（秒）
     * - 3600 秒 = 1 小时
     * - 缓存时间内不会重新加载消息文件
     * - 建议值：
     *   * 开发环境：0（禁用缓存，便于测试）
     *   * 生产环境：3600-86400（1-24 小时）
     * - 性能考虑：
     *   * 缓存时间越长，性能越好
     *   * 但消息更新延迟越长
     *   * 平衡性能和实时性
     *
     * 使用示例 - 在 Controller 中使用：
     * @RestController
     * public class UserController {
     *     @Autowired
     *     private MessageSource messageSource;
     *
     *     @GetMapping(\"/users/{id}\")
     *     public ResponseEntity<?> getUser(@PathVariable Long id, Locale locale) {
     *         User user = userService.findById(id);
     *         if (user == null) {
     *             String message = messageSource.getMessage(\"user.not.found\", null, locale);
     *             return ResponseEntity.notFound().build();
     *         }
     *         return ResponseEntity.ok(user);
     *     }
     * }
     *
     * 使用示例 - 在 Service 中使用：
     * @Service
     * public class UserService {
     *     @Autowired
     *     private MessageSource messageSource;
     *
     *     public void validateUser(User user, Locale locale) throws ValidationException {
     *         if (user.getAge() < 18) {
     *             String message = messageSource.getMessage(\"user.age.invalid\", null, locale);
     *             throw new ValidationException(message);
     *         }
     *     }
     * }
     *
     * 使用示例 - 带参数的消息：
     * // messages_zh_CN.properties
     * // welcome.message=欢迎 {0}，您有 {1} 条未读消息
     *
     * String message = messageSource.getMessage(
     *     \"welcome.message\",
     *     new Object[]{\"张三\", 5},
     *     locale
     * );
     * // 结果：欢迎 张三，您有 5 条未读消息
     *
     * 消息文件示例：
     * # messages_zh_CN.properties
     * user.not.found=用户不存在
     * user.age.invalid=用户年龄必须大于等于 18 岁
     * error.internal=系统内部错误
     * success.save=保存成功
     *
     * # messages_en_US.properties
     * user.not.found=User not found
     * user.age.invalid=User age must be at least 18
     * error.internal=Internal server error
     * success.save=Save successful
     *
     * 常见问题：
     * 1. 消息找不到
     *    - 检查消息文件是否存在
     *    - 检查消息键是否正确
     *    - 检查文件编码是否为 UTF-8
     *    - 检查文件是否在 classpath 中
     *
     * 2. 中文显示乱码
     *    - 确保消息文件使用 UTF-8 编码
     *    - 确保 IDE 设置为 UTF-8 编码
     *    - 确保 setDefaultEncoding(\"UTF-8\") 已设置
     *
     * 3. 消息更新不生效
     *    - 检查缓存时间是否过长
     *    - 重启应用清除缓存
     *    - 在开发环境禁用缓存
     *
     * @return ResourceBundleMessageSource 配置好的消息源
     */
    @Bean
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");                  // 消息资源文件基础名称
        messageSource.setDefaultEncoding("UTF-8");              // 设置字符编码为 UTF-8
        messageSource.setCacheSeconds(3600);                   // 缓存 1 小时
        return messageSource;
    }

    /**
     * 配置语言解析器
     *
     * 功能：
     * - 解析用户的语言偏好
     * - 从 Cookie 中读取用户选择的语言
     * - 提供默认语言（简体中文）
     * - 持久化用户语言选择
     *
     * 语言解析器配置参数详解：
     *
     * setDefaultLocale(Locale.SIMPLIFIED_CHINESE)
     * - 默认语言设置
     * - 当用户没有选择语言时使用
     * - Locale.SIMPLIFIED_CHINESE 表示简体中文
     * - 其他常见 Locale：
     *   * Locale.ENGLISH - 英文
     *   * Locale.CHINESE - 中文（通用）
     *   * Locale.TRADITIONAL_CHINESE - 繁体中文
     *   * Locale.JAPAN - 日文
     *   * Locale.FRANCE - 法文
     *   * Locale.GERMANY - 德文
     * - 建议：根据应用主要用户群体选择
     *
     * setCookieName(\"lang\")
     * - Cookie 名称，用于存储用户语言偏好
     * - 用户选择语言时，会将选择保存到此 Cookie
     * - Cookie 名称应该简洁且有意义
     * - 示例：\"lang\", \"language\", \"locale\", \"user_lang\"
     * - 避免与其他 Cookie 冲突
     *
     * setCookieMaxAge(3600)
     * - Cookie 的最大生存时间（秒）
     * - 3600 秒 = 1 小时
     * - 用户语言偏好会在 1 小时后过期
     * - 建议值：
     *   * 短期：3600 秒（1 小时）- 频繁变化的偏好
     *   * 中期：86400 秒（1 天）- 一般应用
     *   * 长期：2592000 秒（30 天）- 持久化偏好
     *   * 永久：-1（浏览器关闭时删除）
     * - 当前设置 3600 秒适合大多数场景
     *
     * 使用示例 - 在 Controller 中切换语言：
     * @RestController
     * public class LanguageController {
     *     @PostMapping(\"/api/language/switch\")
     *     public ResponseEntity<?> switchLanguage(
     *             @RequestParam String lang,
     *             HttpServletResponse response) {
     *         Locale locale = new Locale(lang);
     *         // LocaleResolver 会自动将语言保存到 Cookie
     *         return ResponseEntity.ok(\"Language switched to \" + lang);
     *     }
     * }
     *
     * 使用示例 - 获取当前语言：
     * @RestController
     * public class UserController {
     *     @GetMapping(\"/api/user/profile\")
     *     public ResponseEntity<?> getProfile(Locale locale) {
     *         // locale 参数由 Spring 自动注入
     *         // 值来自 LocaleResolver 的解析结果
     *         System.out.println(\"Current language: \" + locale.getLanguage());
     *         return ResponseEntity.ok(\"...\");
     *     }
     * }
     *
     * 使用示例 - 前端切换语言：
     * // JavaScript 代码
     * function switchLanguage(lang) {
     *     fetch('/api/language/switch?lang=' + lang, {
     *         method: 'POST',
     *         credentials: 'include'  // 包含 Cookie
     *     })
     *     .then(response => response.json())
     *     .then(data => {
     *         // 刷新页面以应用新语言
     *         location.reload();
     *     });
     * }
     *
     * Cookie 工作流程：
     * 1. 用户首次访问，没有 lang Cookie
     * 2. LocaleResolver 使用默认语言（简体中文）
     * 3. 用户选择语言（如英文）
     * 4. 应用将语言选择保存到 lang Cookie
     * 5. 用户下次访问时，LocaleResolver 从 Cookie 读取语言
     * 6. 应用使用用户选择的语言
     * 7. Cookie 在 1 小时后过期
     *
     * 其他 LocaleResolver 实现：
     * - SessionLocaleResolver：将语言存储在 Session 中
     * - FixedLocaleResolver：使用固定的语言
     * - AcceptHeaderLocaleResolver：从 Accept-Language 请求头读取
     *
     * 常见问题：
     * 1. 语言切换不生效
     *    - 检查 Cookie 是否被正确设置
     *    - 检查浏览器 Cookie 设置是否允许
     *    - 检查 Cookie 名称是否正确
     *
     * 2. 默认语言不生效
     *    - 检查 setDefaultLocale() 是否正确设置
     *    - 检查消息文件是否存在
     *    - 检查 Locale 对象是否正确
     *
     * 3. Cookie 过期时间太短
     *    - 增加 setCookieMaxAge() 的值
     *    - 考虑使用 Session 存储语言偏好
     *    - 实现数据库存储用户语言偏好
     *
     * @return LocaleResolver 配置好的语言解析器
     */
    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver();
        resolver.setDefaultLocale(Locale.SIMPLIFIED_CHINESE);  // 默认语言：简体中文
        resolver.setCookieName("lang");                         // Cookie 名称
        resolver.setCookieMaxAge(3600);                         // Cookie 生存时间：1 小时
        return resolver;
    }
}
