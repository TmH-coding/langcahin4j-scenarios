package com.langchain4j.scenarios.common.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 分页工具类 - Spring Data 分页的辅助工具和转换
 *
 * 职责说明：
 * - 提供分页辅助方法
 * - 支持分页验证和参数规范化
 * - 支持分页对象创建和转换
 * - 实现分页信息的统一格式
 * - 简化分页操作的复杂性
 * - 提供分页的最佳实践
 *
 * 架构设计：
 * - 使用 Spring Data 的 Pageable 和 Page 接口
 * - 提供静态工具方法
 * - 支持泛型分页处理
 * - 包含内部 PageInfo DTO
 * - 易于集成和使用
 *
 * 使用场景：
 * - 创建分页请求对象
 * - 验证和规范化分页参数
 * - 转换 Spring Data Page 对象
 * - 构建统一的分页响应
 * - 支持 REST A​PI 的分页查询
 * - 实现分页的前端展示
 *
 * 分页最佳实践：
 * - 验证页码和页大小参数
 * - 设置合理的最大页大小限制
 * - 提供默认的分页参数
 * - 返回统一的分页信息格式
 * - 支持分页导航信息
 *
 * 性能考虑：
 * - 分页查询避免一次性加载大量数据
 * - 建议使用数据库分页而不是内存分页
 * - 对于大数据集，考虑使用 keyset 分页
 * - 建议在数据库中建立适当的索引
 *
 * 扩展建议：
 * - 可以添加排序参数处理
 * - 可以实现自定义分页策略
 * - 可以添加分页缓存
 * - 可以实现分页的 A/B 测试
 */
public class PageUtils {

    /**
     * 创建分页对象
     *
     * 功能：
     * - 根据页码和页大小创建 Pageable 对象
     * - 验证和规范化分页参数
     * - 处理无效的分页参数
     * - 提供默认的分页参数
     *
     * 参数说明：
     * - pageNum: 页码（从 1 开始）
     *   * 范围：1 到无限大
     *   * 示例：1（第一页）、2（第二页）
     *   * 无效值：<1 时重置为 1
     *   * 注意：Spring Data 使用 0 开始的页码，此方法自动转换
     *
     * - pageSize: 页大小（每页记录数）
     *   * 范围：1 到 100
     *   * 示例：10（每页 10 条）、20（每页 20 条）
     *   * 无效值：<1 或 >100 时重置为 10
     *   * 最大限制：100（防止过大的查询）
     *
     * 返回值：
     * - Pageable：Spring Data 的分页对象
     * - 可直接用于 Repository 查询
     * - 包含页码和页大小信息
     *
     * 参数验证规则：
     * - pageNum < 1：重置为 1
     * - pageSize < 1：重置为 10
     * - pageSize > 100：重置为 10
     * - 其他情况：使用提供的值
     *
     * 使用示例：
     * // 创建第一页，每页 20 条
     * Pageable pageable = PageUtils.createPageable(1, 20);
     * Page<User> page = userRepository.findAll(pageable);
     *
     * // 创建第二页，每页 10 条
     * Pageable pageable = PageUtils.createPageable(2, 10);
     * Page<User> page = userRepository.findByStatus(\\\"active\\\", pageable);
     *
     * // 处理无效参数
     * Pageable pageable = PageUtils.createPageable(0, 150);  // 重置为 (1, 10)
     * Pageable pageable = PageUtils.createPageable(-5, -10);  // 重置为 (1, 10)
     *
     * REST A​PI 集成示例：
     * @GetMapping(\\\"/users\\\")
     * public Page<UserDTO> getUsers(
     *     @RequestParam(defaultValue = \\\"1\\\") int pageNum,
     *     @RequestParam(defaultValue = \\\"10\\\") int pageSize) {
     *     Pageable pageable = PageUtils.createPageable(pageNum, pageSize);
     *     return userRepository.findAll(pageable)
     *         .map(this::convertToDTO);
     * }
     *
     * 性能考虑：
     * - 时间复杂度：O(1)
     * - 非常高效的操作
     * - 建议在 Controller 层使用
     *
     * 最佳实践：
     * - 始终验证分页参数
     * - 设置合理的最大页大小
     * - 提供默认的分页参数
     * - 在 REST A​PI 中使用此方法
     *
     * @param pageNum 页码（从 1 开始）
     * @param pageSize 页大小（每页记录数）
     * @return Spring Data 的 Pageable 对象
     */
    public static Pageable createPageable(int pageNum, int pageSize) {
        // 验证参数
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1 || pageSize > 100) {
            pageSize = 10;
        }
        return PageRequest.of(pageNum - 1, pageSize);
    }

    /**
     * 创建分页结果
     *
     * 功能：
     * - 根据内容、页码、页大小和总数创建 Page 对象
     * - 支持手动构建分页结果
     * - 用于非数据库查询的分页处理
     *
     * 参数说明：
     * - content: 当前页的内容列表
     *   * 类型：List<T>
     *   * 示例：当前页的用户列表
     *   * 可以为空列表
     *
     * - pageNum: 页码（从 1 开始）
     *   * 范围：1 到无限大
     *   * 示例：1（第一页）
     *
     * - pageSize: 页大小（每页记录数）
     *   * 范围：1 到 100
     *   * 示例：10（每页 10 条）
     *
     * - total: 总记录数
     *   * 类型：long
     *   * 示例：100（总共 100 条记录）
     *   * 用于计算总页数
     *
     * 返回值：
     * - Page<T>：Spring Data 的分页对象
     * - 包含内容、分页信息和总数
     * - 可用于 REST A​PI 响应
     *
     * 使用示例：
     * // 手动构建分页结果
     * List<User> users = userService.getUsers(1, 10);
     * long total = userService.getTotalCount();
     * Page<User> page = PageUtils.createPage(users, 1, 10, total);
     *
     * // 用于非数据库查询的分页
     * List<User> allUsers = externalService.fetchAllUsers();
     * List<User> pageContent = allUsers.stream()
     *     .skip((pageNum - 1) * pageSize)
     *     .limit(pageSize)
     *     .collect(Collectors.toList());
     * Page<User> page = PageUtils.createPage(
     *     pageContent,
     *     pageNum,
     *     pageSize,
     *     allUsers.size()
     * );
     *
     * REST A​PI 响应示例：
     * @GetMapping(\\\"/users\\\")
     * public Page<UserDTO> getUsers(
     *     @RequestParam(defaultValue = \\\"1\\\") int pageNum,
     *     @RequestParam(defaultValue = \\\"10\\\") int pageSize) {
     *     List<User> users = userService.getUsers(pageNum, pageSize);
     *     long total = userService.getTotalCount();
     *     Page<User> page = PageUtils.createPage(users, pageNum, pageSize, total);
     *     return page.map(this::convertToDTO);
     * }
     *
     * 性能考虑：
     * - 时间复杂度：O(1)
     * - 空间复杂度：O(n)（n 为内容大小）
     * - 建议用于小数据集
     *
     * @param content 当前页的内容列表
     * @param pageNum 页码（从 1 开始）
     * @param pageSize 页大小（每页记录数）
     * @param total 总记录数
     * @return Spring Data 的 Page 对象
     */
    public static <T> Page<T> createPage(List<T> content, int pageNum, int pageSize, long total) {
        Pageable pageable = createPageable(pageNum, pageSize);
        return new PageImpl<>(content, pageable, total);
    }

    /**
     * 获取分页信息
     *
     * 功能：
     * - 将 Spring Data Page 对象转换为统一的 PageInfo DTO
     * - 提供统一的分页信息格式
     * - 简化分页信息的访问
     *
     * 参数说明：
     * - page: Spring Data 的 Page 对象
     *   * 类型：Page<T>
     *   * 来源：Repository 查询结果
     *   * 包含分页信息和内容
     *
     * 返回值：
     * - PageInfo<T>：统一的分页信息 DTO
     * - 包含内容、分页参数和导航信息
     * - 易于序列化为 JSON
     *
     * PageInfo 字段说明：
     * - content：当前页的内容列表
     * - pageNum：当前页码（从 1 开始）
     * - pageSize：每页记录数
     * - total：总记录数
     * - totalPages：总页数
     * - hasNext：是否有下一页
     * - hasPrevious：是否有上一页
     *
     * 使用示例：
     * // 查询数据
     * Pageable pageable = PageUtils.createPageable(1, 10);
     * Page<User> page = userRepository.findAll(pageable);
     *
     * // 转换为 PageInfo
     * PageInfo<User> pageInfo = PageUtils.getPageInfo(page);
     * System.out.println(\\\"当前页：\\\" + pageInfo.getPageNum());
     * System.out.println(\\\"总页数：\\\" + pageInfo.getTotalPages());
     * System.out.println(\\\"有下一页：\\\" + pageInfo.isHasNext());
     *
     * REST A​PI 响应示例：
     * @GetMapping(\\\"/users\\\")
     * public PageInfo<UserDTO> getUsers(
     *     @RequestParam(defaultValue = \\\"1\\\") int pageNum,
     *     @RequestParam(defaultValue = \\\"10\\\") int pageSize) {
     *     Pageable pageable = PageUtils.createPageable(pageNum, pageSize);
     *     Page<User> page = userRepository.findAll(pageable);
     *     PageInfo<User> pageInfo = PageUtils.getPageInfo(page);
     *     return pageInfo;  // 自动序列化为 JSON
     * }
     *
     * 前端使用示例：
     * // JavaScript
     * fetch('/api/users?pageNum=1&pageSize=10')
     *     .then(response => response.json())
     *     .then(data => {
     *         console.log(\\\"当前页：\\\" + data.pageNum);
     *         console.log(\\\"总页数：\\\" + data.totalPages);
     *         console.log(\\\"有下一页：\\\" + data.hasNext);
     *         data.content.forEach(user => {
     *             console.log(user.name);
     *         });
     *     });
     *
     * 性能考虑：
     * - 时间复杂度：O(1)
     * - 非常高效的操作
     * - 建议在 Controller 层使用
     *
     * @param page Spring Data 的 Page 对象
     * @return 统一的分页信息 DTO
     */
    public static <T> PageInfo<T> getPageInfo(Page<T> page) {
        return PageInfo.<T>builder()
                .content(page.getContent())
                .pageNum(page.getNumber() + 1)
                .pageSize(page.getSize())
                .total(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    /**
     * 分页信息 DTO - 统一的分页响应格式
     *
     * 职责说明：
     * - 封装分页信息
     * - 提供统一的分页响应格式
     * - 支持 JSON 序列化
     * - 简化前端分页处理
     *
     * 字段说明：
     * - content：当前页的内容列表
     *   * 类型：List<T>
     *   * 示例：当前页的用户列表
     *
     * - pageNum：当前页码（从 1 开始）
     *   * 类型：int
     *   * 范围：1 到 totalPages
     *   * 示例：1（第一页）
     *
     * - pageSize：每页记录数
     *   * 类型：int
     *   * 范围：1 到 100
     *   * 示例：10（每页 10 条）
     *
     * - total：总记录数
     *   * 类型：long
     *   * 示例：100（总共 100 条记录）
     *
     * - totalPages：总页数
     *   * 类型：int
     *   * 计算：(total + pageSize - 1) / pageSize
     *   * 示例：10（总共 10 页）
     *
     * - hasNext：是否有下一页
     *   * 类型：boolean
     *   * 示例：true（有下一页）
     *   * 用于前端分页导航
     *
     * - hasPrevious：是否有上一页
     *   * 类型：boolean
     *   * 示例：false（没有上一页，当前是第一页）
     *   * 用于前端分页导航
     *
     * 使用示例：
     * PageInfo<User> pageInfo = PageUtils.getPageInfo(page);
     * System.out.println(\\\"当前页：\\\" + pageInfo.getPageNum());
     * System.out.println(\\\"总页数：\\\" + pageInfo.getTotalPages());
     * System.out.println(\\\"有下一页：\\\" + pageInfo.isHasNext());
     * System.out.println(\\\"有上一页：\\\" + pageInfo.isHasPrevious());
     *
     * JSON 序列化示例：
     * {
     *   \\\"content\\\": [...],
     *   \\\"pageNum\\\": 1,
     *   \\\"pageSize\\\": 10,
     *   \\\"total\\\": 100,
     *   \\\"totalPages\\\": 10,
     *   \\\"hasNext\\\": true,
     *   \\\"hasPrevious\\\": false
     * }
     *
     * 前端使用示例：
     * // 显示分页信息
     * console.log(`第 ${pageInfo.pageNum} 页，共 ${pageInfo.totalPages} 页`);
     *
     * // 显示分页导航
     * if (pageInfo.hasPrevious) {
     *     showPreviousButton();
     * }
     * if (pageInfo.hasNext) {
     *     showNextButton();
     * }
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PageInfo<T> {
        /** 当前页的内容列表 */
        private List<T> content;
        /** 当前页码（从 1 开始） */
        private int pageNum;
        /** 每页记录数 */
        private int pageSize;
        /** 总记录数 */
        private long total;
        /** 总页数 */
        private int totalPages;
        /** 是否有下一页 */
        private boolean hasNext;
        /** 是否有上一页 */
        private boolean hasPrevious;
    }
}
