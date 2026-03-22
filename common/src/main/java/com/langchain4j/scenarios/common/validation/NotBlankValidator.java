package com.langchain4j.scenarios.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * NotBlank 验证器实现 - 自定义验证注解的具体实现
 *
 * 职责说明：
 * - 实现 NotBlank 注解的验证逻辑
 * - 验证字符串字段不能为空或仅包含空格
 * - 支持 Jakarta Validation 框架
 * - 提供灵活的验证规则
 *
 * 架构设计：
 * - 实现 ConstraintValidator 接口
 * - 支持泛型验证
 * - 易于扩展和定制
 * - 与 Spring Validation 框架集成
 *
 * 使用场景：
 * - 验证 DTO 字段不能为空
 * - 验证 API 请求参数
 * - 验证表单输入
 * - 验证用户提交的数据
 *
 * 验证规则：
 * - 值不能为 null
 * - 值不能为空字符串
 * - 值不能仅包含空格
 *
 * 验证工作原理：
 * 1. Spring 在处理请求时触发验证
 * 2. 调用 isValid() 方法进行验证
 * 3. 检查值是否为 null
 * 4. 检查值是否为空或仅空格
 * 5. 返回验证结果
 *
 * 性能考虑：
 * - 验证速度快
 * - 字符串 trim() 操作性能开销小
 * - 支持高并发请求
 *
 * 安全考虑：
 * - 防止空值注入
 * - 防止仅空格的输入
 * - 验证所有用户输入
 *
 * 扩展建议：
 * - 可以添加长度限制
 * - 可以添加格式验证
 * - 可以添加正则表达式验证
 */
public class NotBlankValidator implements ConstraintValidator<NotBlank, String> {

    @Override
    public void initialize(NotBlank annotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && !value.trim().isEmpty();
    }
}
