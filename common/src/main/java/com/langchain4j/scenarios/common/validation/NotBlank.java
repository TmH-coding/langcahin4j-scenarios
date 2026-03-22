package com.langchain4j.scenarios.common.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * 自定义验证注解：非空字符串 - 字段级别的参数验证
 *
 * 职责说明：
 * - 定义自定义验证注解
 * - 验证字符串字段不能为空或仅包含空格
 * - 支持字段和参数级别的验证
 * - 提供自定义错误消息
 * - 集成 Jakarta Validation 框架
 *
 * 架构设计：
 * - 使用 @Constraint 注解指定验证器
 * - 使用 @Target 限制应用范围
 * - 使用 @Retention 保留运行时信息
 * - 支持自定义错误消息
 * - 易于在 DTO 中使用
 *
 * 使用场景：
 * - 验证 DTO 字段不能为空
 * - 验证 API 请求参数
 * - 验证表单输入
 * - 验证用户提交的数据
 * - 实现参数的自动验证
 *
 * 验证工作原理：
 * 1. 在字段或参数上添加 @NotBlank 注解
 * 2. Spring 在处理请求时触发验证
 * 3. NotBlankValidator 执行验证逻辑
 * 4. 验证失败时返回错误消息
 * 5. 错误消息返回给客户端
 *
 * 应用范围：
 * - FIELD：应用于类字段
 * - PARAMETER：应用于方法参数
 *
 * 性能考虑：
 * - 验证速度快
 * - 支持高并发请求
 * - 建议在 Controller 层进行验证
 * - 避免重复验证
 *
 * 安全考虑：
 * - 防止空值注入
 * - 防止仅空格的输入
 * - 验证所有用户输入
 * - 提供清晰的错误消息
 *
 * 扩展建议：
 * - 可以添加长度限制
 * - 可以添加格式验证
 * - 可以添加正则表达式验证
 * - 可以支持国际化错误消息
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NotBlankValidator.class)
@Documented
public @interface NotBlank {
    String message() default "字段不能为空";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
