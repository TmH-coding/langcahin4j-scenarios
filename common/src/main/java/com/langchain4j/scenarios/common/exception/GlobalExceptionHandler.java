package com.langchain4j.scenarios.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器 - 统一的异常处理和错误响应
 *
 * 职责说明：
 * - 统一处理所有异常
 * - 返回结构化的错误响应
 * - 记录异常日志
 * - 提供一致的错误信息格式
 * - 支持多种异常类型的处理
 *
 * 架构设计：
 * - 使用 @RestControllerAdvice 注解实现全局异常处理
 * - 使用 @ExceptionHandler 注解处理特定异常类型
 * - 使用 @Slf4j 进行日志记录
 * - 支持多个异常处理方法
 * - 按异常类型优先级处理（具体异常优先于通用异常）
 * - 返回 ResponseEntity 包装的错误响应
 *
 * 使用场景：
 * - 参数验证失败时返回 400 错误
 * - 业务逻辑异常时返回 400 错误
 * - 系统异常时返回 500 错误
 * - 所有异常都记录日志用于调试
 * - 提供统一的错误响应格式给客户端
 * - 防止敏感信息泄露
 *
 * 异常处理工作原理：
 * 1. 请求处理过程中发生异常
 * 2. Spring 捕获异常
 * 3. 查找匹配的 @ExceptionHandler 方法
 * 4. 执行异常处理方法
 * 5. 返回结构化的错误响应
 * 6. 记录异常日志
 * 7. 返回给客户端
 *
 * 异常处理优先级：
 * 1. MethodArgumentNotValidException - 参数验证异常（最具体）
 * 2. BusinessException - 业务异常
 * 3. Exception - 通用异常（最通用）
 *
 * 错误响应格式：
 * {
 *   "code": 400/500,           // HTTP 状态码
 *   "message": "错误信息",      // 错误描述
 *   "errors": {...},           // 详细错误信息（可选）
 *   "timestamp": 1234567890    // 错误发生时间戳
 * }
 *
 * 性能考虑：
 * - 异常处理速度快，不会成为性能瓶颈
 * - 日志记录可能有性能开销，建议配置日志级别
 * - 避免在异常处理中执行复杂操作
 * - 异常处理是同步的，不会阻塞其他请求
 *
 * 安全考虑：
 * - 不要在错误响应中暴露敏感信息（如数据库错误、堆栈跟踪）
 * - 系统异常返回通用错误消息，不暴露内部实现细节
 * - 记录完整的异常信息用于调试，但不返回给客户端
 * - 验证异常返回详细的字段错误信息，帮助客户端修正
 * - 实现异常日志的访问控制
 * - 防止异常信息被用于攻击
 *
 * 扩展建议：
 * - 可以添加更多异常类型的处理方法
 * - 可以实现异常的国际化（多语言错误消息）
 * - 可以添加异常的追踪 ID（用于日志关联）
 * - 可以实现异常的分类和统计
 * - 可以添加异常的告警机制
 * - 可以支持异常的自定义处理逻辑
 * - 可以实现异常的重试机制
 * - 可以添加异常的性能监控
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> response = new HashMap<>();
        response.put("code", 400);
        response.put("message", "参数验证失败");
        response.put("errors", errors);
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(BusinessException ex) {
        log.warn("业务异常: {}", ex.getMessage());

        Map<String, Object> response = new HashMap<>();
        response.put("code", 400);
        response.put("message", ex.getMessage());
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        log.error("系统异常", ex);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 500);
        response.put("message", "系统内部错误");
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
