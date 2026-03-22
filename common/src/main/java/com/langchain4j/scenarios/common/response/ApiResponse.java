package com.langchain4j.scenarios.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一 API 响应包装器 - 标准化的 REST API 响应格式
 *
 * 职责说明：
 * - 统一所有 REST API 的响应格式
 * - 提供成功和失败响应的工厂方法
 * - 包含响应数据、状态码、消息、时间戳和请求 ID
 * - 支持泛型数据类型
 * - 实现分布式追踪的请求关联
 *
 * 架构设计：
 * - 使用泛型 <T> 支持任意类型的响应数据
 * - 提供静态工厂方法简化响应构建
 * - 使用 Lombok 注解减少样板代码
 * - 支持 Builder 模式灵活构建
 * - 易于 JSON 序列化和反序列化
 *
 * 使用场景：
 * - 所有 REST API 端点的统一响应格式
 * - 成功响应返回数据和消息
 * - 失败响应返回错误代码和错误消息
 * - 支持分布式追踪的请求 ID 传递
 * - 前端统一处理 API 响应
 * - 移动应用统一处理 API 响应
 *
 * 响应格式工作原理：
 * 1. 业务逻辑处理请求
 * 2. 调用工厂方法构建响应
 * 3. 设置响应代码、消息、数据
 * 4. 自动添加时间戳
 * 5. 返回 JSON 格式的响应
 * 6. 前端解析响应并处理
 *
 * 响应代码规范：
 * - 0：成功响应
 * - 1：通用错误
 * - 400：请求参数错误
 * - 401：未授权
 * - 403：禁止访问
 * - 404：资源不存在
 * - 500：服务器内部错误
 * - 自定义代码：业务特定错误
 *
 * 性能考虑：
 * - 响应序列化速度快
 * - 支持高并发 API 调用
 * - 时间戳使用系统时间，性能开销小
 * - 建议在网关层添加响应缓存
 *
 * 安全考虑：
 * - 不要在响应中包含敏感信息（密码、密钥等）
 * - 错误消息应该通用，不要暴露系统细节
 * - 使用请求 ID 追踪所有请求
 * - 实现速率限制防止滥用
 * - 验证所有输入数据
 *
 * 扩展建议：
 * - 可以添加分页信息字段
 * - 可以添加错误详情字段
 * - 可以添加响应元数据
 * - 可以实现国际化错误消息
 * - 可以添加性能指标信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    /** 响应代码：0表示成功，非0表示失败 */
    private int code;

    /** 响应消息 */
    private String message;

    /** 响应数据 */
    private T data;

    /** 时间戳 */
    private long timestamp;

    /** 请求ID（用于追踪） */
    private String requestId;

    /**
     * 成功响应 - 返回成功状态和数据
     *
     * 功能：
     * - 构建成功响应对象
     * - 自动设置响应代码为 0
     * - 自动设置消息为 "success"
     * - 自动添加当前时间戳
     *
     * 参数说明：
     * - data: 响应数据
     *   * 类型：泛型 <T>
     *   * 可以是任意类型（对象、列表、基本类型等）
     *   * 示例：User 对象、List<User>、String、Integer
     *   * 可以为 null（表示没有返回数据）
     *
     * 返回值：
     * - ApiResponse<T>：包含成功状态的响应对象
     * - code：0（成功）
     * - message："success"
     * - data：传入的数据
     * - timestamp：当前时间戳（毫秒）
     *
     * 使用示例：
     * // 返回单个对象
     * User user = userService.getUserById(1);
     * return ApiResponse.success(user);
     *
     * // 返回列表
     * List<User> users = userService.getAllUsers();
     * return ApiResponse.success(users);
     *
     * // 返回分页结果
     * Page<User> page = userService.getUsers(pageable);
     * return ApiResponse.success(page);
     *
     * // 返回 null（表示操作成功但无数据）
     * userService.deleteUser(id);
     * return ApiResponse.success(null);
     *
     * REST API 集成示例：
     * @GetMapping(\"/users/{id}\")\n     * public ApiResponse<User> getUser(@PathVariable Long id) {\n     *     User user = userService.getUserById(id);\n     *     return ApiResponse.success(user);\n     * }
     *
     * 前端处理示例：
     * fetch('/api/users/1')\n     *     .then(response => response.json())\n     *     .then(data => {\n     *         if (data.code === 0) {\n     *             console.log('用户信息:', data.data);\n     *         }\n     *     });
     *
     * 性能考虑：
     * - 时间复杂度：O(1)
     * - 空间复杂度：O(1)
     * - 非常高效的操作
     *
     * @param data 响应数据
     * @return 成功响应对象
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(0)
                .message("success")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 成功响应（带自定义消息）- 返回成功状态、数据和自定义消息
     *
     * 功能：
     * - 构建成功响应对象
     * - 自动设置响应代码为 0
     * - 使用自定义消息替代默认 "success"
     * - 自动添加当前时间戳
     *
     * 参数说明：
     * - data: 响应数据
     *   * 类型：泛型 <T>
     *   * 可以是任意类型
     *   * 示例：User 对象、List<User>、String
     *
     * - message: 自定义消息
     *   * 类型：String
     *   * 示例："用户创建成功"、"数据导入完成"、"操作已完成"
     *   * 用于向前端传递业务相关的消息
     *   * 可以用于国际化处理
     *
     * 返回值：
     * - ApiResponse<T>：包含成功状态的响应对象
     * - code：0（成功）
     * - message：传入的自定义消息
     * - data：传入的数据
     * - timestamp：当前时间戳（毫秒）
     *
     * 使用示例：
     * // 创建用户成功
     * User newUser = userService.createUser(request);
     * return ApiResponse.success(newUser, \"用户创建成功\");
     *
     * // 导入数据成功
     * int count = dataService.importData(file);
     * return ApiResponse.success(count, \"成功导入 \" + count + \" 条数据\");
     *
     * // 操作完成
     * userService.updateUser(id, request);
     * return ApiResponse.success(null, \"用户信息已更新\");
     *
     * REST API 集成示例：
     * @PostMapping(\"/users\")\n     * public ApiResponse<User> createUser(@RequestBody UserRequest request) {\n     *     User user = userService.createUser(request);\n     *     return ApiResponse.success(user, \"用户创建成功\");\n     * }
     *
     * 前端处理示例：
     * fetch('/api/users', { method: 'POST', body: JSON.stringify(data) })\n     *     .then(response => response.json())\n     *     .then(data => {\n     *         if (data.code === 0) {\n     *             alert(data.message);  // 显示自定义消息\n     *             console.log('新用户:', data.data);\n     *         }\n     *     });
     *
     * 性能考虑：
     * - 时间复杂度：O(1)
     * - 字符串拼接速度快
     *
     * @param data 响应数据
     * @param message 自定义消息
     * @return 成功响应对象
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .code(0)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 失败响应 - 返回失败状态和错误消息
     *
     * 功能：
     * - 构建失败响应对象
     * - 自动设置响应代码为 1（通用错误）
     * - 设置错误消息
     * - 自动添加当前时间戳
     * - 不包含响应数据
     *
     * 参数说明：
     * - message: 错误消息
     *   * 类型：String
     *   * 示例："用户不存在"、"参数验证失败"、"操作失败"
     *   * 应该是用户友好的消息
     *   * 不要暴露系统内部细节
     *
     * 返回值：
     * - ApiResponse<T>：包含失败状态的响应对象
     * - code：1（通用错误）
     * - message：传入的错误消息
     * - data：null（没有数据）
     * - timestamp：当前时间戳（毫秒）
     *
     * 使用示例：
     * // 用户不存在
     * User user = userService.getUserById(id);
     * if (user == null) {\n     *     return ApiResponse.error(\"用户不存在\");\n     * }
     *
     * // 参数验证失败
     * if (!isValidEmail(email)) {\n     *     return ApiResponse.error(\"邮箱格式不正确\");\n     * }
     *
     * // 业务逻辑失败
     * try {\n     *     userService.deleteUser(id);\n     * } catch (Exception e) {\n     *     return ApiResponse.error(\"删除用户失败\");\n     * }
     *
     * REST API 集成示例：
     * @GetMapping(\"/users/{id}\")\n     * public ApiResponse<User> getUser(@PathVariable Long id) {\n     *     User user = userService.getUserById(id);\n     *     if (user == null) {\n     *         return ApiResponse.error(\"用户不存在\");\n     *     }\n     *     return ApiResponse.success(user);\n     * }
     *
     * 前端处理示例：
     * fetch('/api/users/1')\n     *     .then(response => response.json())\n     *     .then(data => {\n     *         if (data.code !== 0) {\n     *             alert(data.message);  // 显示错误消息\n     *         }\n     *     });
     *
     * 性能考虑：
     * - 时间复杂度：O(1)
     * - 非常高效的操作
     *
     * @param message 错误消息
     * @return 失败响应对象
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .code(1)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 失败响应（带错误代码）- 返回失败状态、错误代码和错误消息
     *
     * 功能：
     * - 构建失败响应对象
     * - 设置自定义错误代码
     * - 设置错误消息
     * - 自动添加当前时间戳
     * - 不包含响应数据
     *
     * 参数说明：
     * - code: 错误代码
     *   * 类型：int
     *   * 范围：非 0 值（0 表示成功）
     *   * 示例：400（请求错误）、401（未授权）、403（禁止）、404（不存在）、500（服务器错误）
     *   * 可以使用 HTTP 状态码或自定义业务错误码
     *   * 建议建立错误码规范文档
     *
     * - message: 错误消息
     *   * 类型：String
     *   * 示例："请求参数不合法"、"用户未授权"、"资源不存在"
     *   * 应该是用户友好的消息
     *   * 不要暴露系统内部细节
     *
     * 返回值：
     * - ApiResponse<T>：包含失败状态的响应对象
     * - code：传入的错误代码
     * - message：传入的错误消息
     * - data：null（没有数据）
     * - timestamp：当前时间戳（毫秒）
     *
     * 错误代码规范：
     * - 400：请求参数错误（参数验证失败）
     * - 401：未授权（需要登录）
     * - 403：禁止访问（权限不足）
     * - 404：资源不存在（用户不存在、文件不存在）
     * - 500：服务器内部错误（数据库错误、系统异常）
     * - 自定义代码：业务特定错误（1001、1002 等）
     *
     * 使用示例：
     * // 请求参数错误
     * if (pageNum < 1 || pageSize < 1) {\n     *     return ApiResponse.error(400, \"分页参数不合法\");\n     * }
     *
     * // 用户未授权
     * if (!isAuthenticated()) {\n     *     return ApiResponse.error(401, \"请先登录\");\n     * }
     *
     * // 权限不足
     * if (!hasPermission(userId, resource)) {\n     *     return ApiResponse.error(403, \"您没有权限访问此资源\");\n     * }
     *
     * // 资源不存在
     * User user = userService.getUserById(id);\n     * if (user == null) {\n     *     return ApiResponse.error(404, \"用户不存在\");\n     * }
     *
     * // 服务器错误
     * try {\n     *     userService.createUser(request);\n     * } catch (Exception e) {\n     *     return ApiResponse.error(500, \"创建用户失败，请稍后重试\");\n     * }
     *
     * REST API 集成示例：
     * @GetMapping(\"/users/{id}\")\n     * public ApiResponse<User> getUser(@PathVariable Long id) {\n     *     if (id < 1) {\n     *         return ApiResponse.error(400, \"用户 ID 不合法\");\n     *     }\n     *     User user = userService.getUserById(id);\n     *     if (user == null) {\n     *         return ApiResponse.error(404, \"用户不存在\");\n     *     }\n     *     return ApiResponse.success(user);\n     * }
     *
     * 前端处理示例：
     * fetch('/api/users/1')\n     *     .then(response => response.json())\n     *     .then(data => {\n     *         if (data.code === 400) {\n     *             alert('请求参数错误: ' + data.message);\n     *         } else if (data.code === 401) {\n     *             redirectToLogin();\n     *         } else if (data.code === 403) {\n     *             alert('您没有权限');\n     *         } else if (data.code === 404) {\n     *             alert('资源不存在');\n     *         } else if (data.code === 500) {\n     *             alert('服务器错误，请稍后重试');\n     *         }\n     *     });
     *
     * 性能考虑：
     * - 时间复杂度：O(1)
     * - 非常高效的操作
     *
     * @param code 错误代码
     * @param message 错误消息
     * @return 失败响应对象
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
