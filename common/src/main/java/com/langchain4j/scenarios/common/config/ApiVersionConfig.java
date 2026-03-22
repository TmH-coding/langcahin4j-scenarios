package com.langchain4j.scenarios.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;

/**
 * API 版本控制配置 - 多版本 API 管理
 *
 * 职责说明：
 * - 配置 API 版本控制策略
 * - 支持多个 API 版本共存
 * - 便于 API 升级和向后兼容
 * - 支持灰度发布和版本迁移
 *
 * 架构设计：
 * - 使用 URL 路径前缀标识 API 版本（/api/v1, /api/v2 等）
 * - 支持不同版本的 Controller 并存
 * - 支持版本间的数据转换和适配
 * - 支持逐步淘汰旧版本 API
 *
 * 使用场景：
 * - API 功能升级时保持向后兼容
 * - 支持客户端逐步升级到新版本
 * - 灰度发布新功能
 * - A/B 测试不同 API 版本
 * - 长期支持多个 API 版本
 *
 * API 版本管理工作原理：
 * 1. 客户端在 URL 中指定 API 版本（/api/v1/... 或 /api/v2/...）
 * 2. Spring MVC 根据路径匹配规则路由到对应版本的 Controller
 * 3. 不同版本的 Controller 可以有不同的实现
 * 4. 版本间可以共享通用逻辑，差异部分单独实现
 * 5. 旧版本逐步淘汰，最终下线
 *
 * 安全考虑：
 * - 确保版本间的数据一致性
 * - 防止版本间的数据泄露
 * - 监控版本使用情况，及时下线旧版本
 * - 文档化版本差异和迁移指南
 */
@Configuration
public class ApiVersionConfig implements WebMvcConfigurer {

    /**
     * 配置路径匹配规则
     *
     * 功能：
     * - 配置 Spring MVC 的路径匹配策略
     * - 支持版本化的 URL 路径
     * - 支持灵活的路由规则
     *
     * 路径匹配配置参数详解：
     *
     * PathMatchConfigurer
     * - Spring MVC 路径匹配配置器
     * - 用于自定义 URL 路径的匹配规则
     * - 支持多种路径匹配策略
     *
     * 版本控制策略选项：
     *
     * 1. URL 路径前缀版本控制（推荐）
     * - 格式：/api/v1/users, /api/v2/users
     * - 优点：
     *   * 清晰明确，易于理解
     *   * 便于 URL 路由和缓存
     *   * 支持 CDN 和代理缓存
     *   * 便于监控和日志分析
     * - 缺点：
     *   * URL 较长
     *   * 需要维护多个 Controller
     *
     * 2. 请求头版本控制
     * - 格式：Accept: application/vnd.api+json;version=2
     * - 优点：
     *   * URL 保持简洁
     *   * 同一 URL 支持多个版本
     * - 缺点：
     *   * 不易于缓存
     *   * 客户端需要设置特殊请求头
     *
     * 3. 查询参数版本控制
     * - 格式：/api/users?version=2
     * - 优点：
     *   * 实现简单
     *   * 易于测试
     * - 缺点：
     *   * 不易于缓存
     *   * URL 较长
     *
     * 使用示例 - URL 路径版本控制：
     * @RestController
     * @RequestMapping(\"/api/v1/users\")
     * public class UserControllerV1 {
     *     @GetMapping(\"/{id}\")
     *     public UserDtoV1 getUser(@PathVariable String id) {
     *         // V1 版本的实现
     *         return userService.getUserV1(id);
     *     }
     * }
     *
     * @RestController
     * @RequestMapping(\"/api/v2/users\")
     * public class UserControllerV2 {
     *     @GetMapping(\"/{id}\")
     *     public UserDtoV2 getUser(@PathVariable String id) {
     *         // V2 版本的实现，可能包含新字段
     *         return userService.getUserV2(id);
     *     }
     * }
     *
     * 使用示例 - 版本间的数据转换：
     * @Service
     * public class UserService {
     *     public UserDtoV1 getUserV1(String id) {
     *         User user = userRepository.findById(id);
     *         return convertToV1(user);
     *     }
     *
     *     public UserDtoV2 getUserV2(String id) {
     *         User user = userRepository.findById(id);
     *         return convertToV2(user);
     *     }
     *
     *     private UserDtoV1 convertToV1(User user) {
     *         // 转换为 V1 格式（可能需要移除新字段）
     *         return new UserDtoV1(user.getId(), user.getName());
     *     }
     *
     *     private UserDtoV2 convertToV2(User user) {
     *         // 转换为 V2 格式（可能包含新字段）
     *         return new UserDtoV2(user.getId(), user.getName(), user.getEmail());
     *     }
     * }
     *
     * API 版本生命周期：
     * 1. 新版本发布
     *    - 发布新版本 API（如 v2）
     *    - 保持旧版本（v1）继续可用
     *    - 文档化版本差异
     *
     * 2. 过渡期
     *    - 同时支持多个版本
     *    - 鼓励客户端升级到新版本
     *    - 监控版本使用情况
     *
     * 3. 淘汰期
     *    - 宣布旧版本即将下线
     *    - 给客户端充分的升级时间
     *    - 提供迁移指南和支持
     *
     * 4. 下线
     *    - 停止支持旧版本
     *    - 返回 410 Gone 或 301 Redirect
     *    - 完全移除旧版本代码
     *
     * 版本管理最佳实践：
     * 1. 明确的版本策略
     *    - 定义版本号规则
     *    - 定义版本支持周期
     *    - 定义版本淘汰计划
     *
     * 2. 向后兼容性
     *    - 新版本应该向后兼容
     *    - 避免破坏性变更
     *    - 如果必须破坏兼容性，创建新版本
     *
     * 3. 文档化
     *    - 文档化版本差异
     *    - 提供迁移指南
     *    - 提供版本对比表
     *
     * 4. 监控和告警
     *    - 监控各版本的使用情况
     *    - 监控版本间的错误率差异
     *    - 及时发现版本问题
     *
     * 5. 测试
     *    - 为每个版本编写测试
     *    - 测试版本间的兼容性
     *    - 测试数据转换逻辑
     *
     * 与 OpenAPI/Swagger 的集成：
     * - 为每个版本生成独立的 OpenAPI 文档
     * - 在 Swagger UI 中显示版本选择器
     * - 文档化版本差异
     *
     * @param configurer 路径匹配配置器
     */
    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 配置API版本匹配规则
        // 支持 /api/v1/... 和 /api/v2/... 等多个版本
    }
}
