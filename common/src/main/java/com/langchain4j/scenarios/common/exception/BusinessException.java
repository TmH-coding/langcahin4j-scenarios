package com.langchain4j.scenarios.common.exception;

/**
 * 业务异常 - 业务逻辑中的异常情况处理
 *
 * 职责说明：
 * - 表示业务逻辑中的异常情况
 * - 区分业务异常和系统异常
 * - 提供错误代码和错误消息
 * - 支持异常的统一处理和转换
 * - 实现异常的链式传播
 *
 * 架构设计：
 * - 继承 RuntimeException（非检查异常）
 * - 包含错误代码和错误消息
 * - 支持两种构造方式（带代码和不带代码）
 * - 易于在异常处理器中统一处理
 *
 * 使用场景：
 * - 用户输入验证失败
 * - 业务规则检查失败
 * - 资源不存在或已被删除
 * - 权限检查失败
 * - 业务操作冲突
 * - 数据一致性检查失败
 *
 * 异常处理工作原理：
 * 1. 业务逻辑检查条件
 * 2. 条件不满足时抛出 BusinessException
 * 3. 异常向上传播到 Controller
 * 4. GlobalExceptionHandler 捕获异常
 * 5. 转换为 ApiResponse 返回给客户端
 * 6. 客户端根据错误代码处理异常
 *
 * 错误代码规范：
 * - 1001-1999：参数验证错误
 * - 2001-2999：资源不存在错误
 * - 3001-3999：权限检查错误
 * - 4001-4999：业务规则错误
 * - 5001-5999：数据一致性错误
 * - 6001-6999：操作冲突错误
 *
 * 性能考虑：
 * - 异常创建有性能开销
 * - 避免在循环中频繁抛出异常
 * - 异常应该用于异常情况，不是正常流程控制
 * - 建议在业务逻辑层抛出异常
 *
 * 安全考虑：
 * - 错误消息应该用户友好，不暴露系统细节
 * - 不要在异常消息中包含敏感信息
 * - 记录异常日志用于调试和审计
 * - 实现异常的统一处理和转换
 *
 * 扩展建议：
 * - 可以添加异常原因字段
 * - 可以添加异常上下文信息
 * - 可以实现异常的国际化
 * - 可以添加异常的重试策略
 */
public class BusinessException extends RuntimeException {

    private String code;
    private String message;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(String message) {
        super(message);
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
