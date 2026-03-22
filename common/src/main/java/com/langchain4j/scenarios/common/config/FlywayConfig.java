package com.langchain4j.scenarios.common.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * 数据库迁移配置 - Flyway 版本管理和自动化迁移
 *
 * 职责说明：
 * - 配置 Flyway 数据库版本管理工具
 * - 自动执行数据库迁移脚本
 * - 维护数据库版本历史
 * - 支持多环境数据库配置
 *
 * 架构设计：
 * - 使用 Flyway 管理数据库 schema 版本
 * - 将迁移脚本存储在 classpath:db/migration 目录
 * - 按版本号顺序自动执行迁移
 * - 记录迁移历史到 flyway_schema_history 表
 *
 * 使用场景：
 * - 应用启动时自动初始化数据库
 * - 版本升级时自动执行数据库变更
 * - 多环境数据库同步
 * - 团队协作时的数据库版本管理
 * - 数据库回滚和版本控制
 *
 * 数据库迁移工作原理：
 * 1. 应用启动时，Flyway 检查 flyway_schema_history 表
 * 2. 扫描 classpath:db/migration 目录下的迁移脚本
 * 3. 比较已执行的版本和待执行的版本
 * 4. 按顺序执行未执行的迁移脚本
 * 5. 记录迁移历史和执行结果
 *
 * 安全考虑：
 * - 迁移脚本应该是幂等的（多次执行结果相同）
 * - 避免在迁移脚本中执行危险操作（删除表、清空数据等）
 * - 备份数据库后再执行迁移
 * - 在生产环境前在测试环境验证迁移
 * - 保留迁移脚本的版本历史
 */
@Configuration
@ConditionalOnProperty(name = "spring.flyway.enabled", havingValue = "true", matchIfMissing = false)
public class FlywayConfig {

    /**
     * Flyway 自动配置
     *
     * 功能：
     * - 初始化 Flyway 配置
     * - Spring Boot 自动检测并执行迁移脚本
     * - 记录迁移历史
     *
     * Flyway 配置参数详解：
     *
     * @ConditionalOnProperty(name = "spring.flyway.enabled", havingValue = "true", matchIfMissing = false)
     * - 条件化配置：仅当 spring.flyway.enabled=true 时启用
     * - matchIfMissing = false：属性缺失时不启用（默认禁用）
     * - 作用：
     *   * 支持可选的数据库迁移功能
     *   * 避免在不需要迁移的环境中执行迁移
     *   * 支持多环境配置
     *
     * 迁移脚本位置：
     * - classpath:db/migration
     * - 脚本文件命名规则：V{版本号}__{描述}.sql
     * - 示例：
     *   * V1__Initial_schema.sql
     *   * V2__Add_user_table.sql
     *   * V3__Add_conversation_table.sql
     *   * V4__Add_audit_log_table.sql
     *
     * 版本号规则：
     * - 格式：V{主版本}.{次版本}__
     * - 示例：
     *   * V1__Initial_schema.sql
     *   * V1.1__Add_index.sql
     *   * V2__Major_refactor.sql
     * - 版本号必须递增
     * - 不能跳过版本号
     *
     * 使用示例 - 迁移脚本结构：
     * // V1__Initial_schema.sql
     * CREATE TABLE users (
     *     id VARCHAR(36) PRIMARY KEY,
     *     username VARCHAR(100) NOT NULL UNIQUE,
     *     email VARCHAR(100) NOT NULL UNIQUE,
     *     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
     * );
     *
     * CREATE TABLE conversations (
     *     id VARCHAR(36) PRIMARY KEY,
     *     user_id VARCHAR(36) NOT NULL,
     *     title VARCHAR(255),
     *     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
     *     FOREIGN KEY (user_id) REFERENCES users(id)
     * );
     *
     * // V2__Add_audit_log_table.sql
     * CREATE TABLE audit_logs (
     *     id VARCHAR(36) PRIMARY KEY,
     *     operator VARCHAR(100),
     *     operation_type VARCHAR(50),
     *     operation_details TEXT,
     *     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
     * );
     *
     * // V3__Add_soft_delete_column.sql
     * ALTER TABLE conversations ADD COLUMN deleted_at TIMESTAMP NULL;
     * ALTER TABLE audit_logs ADD COLUMN deleted_at TIMESTAMP NULL;
     *
     * flyway_schema_history 表：
     * - Flyway 自动创建此表记录迁移历史
     * - 表结构：
     *   * installed_rank: 迁移执行顺序
     *   * version: 迁移版本号
     *   * description: 迁移描述
     *   * type: 迁移类型（SQL、JDBC 等）
     *   * script: 迁移脚本名称
     *   * checksum: 脚本内容的校验和
     *   * installed_by: 执行迁移的用户
     *   * installed_on: 迁移执行时间
     *   * execution_time: 执行耗时（毫秒）
     *   * success: 是否成功
     *
     * 使用示例 - 查询迁移历史：
     * SELECT * FROM flyway_schema_history ORDER BY installed_rank;
     *
     * 迁移最佳实践：
     * 1. 脚本设计
     *    - 每个脚本只做一个逻辑变更
     *    - 脚本应该是幂等的
     *    - 避免在脚本中使用 IF EXISTS 等条件语句
     *
     * 2. 版本管理
     *    - 版本号必须递增
     *    - 不能修改已执行的脚本
     *    - 如果需要修复，创建新的迁移脚本
     *
     * 3. 数据迁移
     *    - 大数据迁移应该分批进行
     *    - 避免长时间锁表
     *    - 考虑使用触发器或应用层逻辑
     *
     * 4. 回滚策略
     *    - Flyway 不支持自动回滚
     *    - 需要手动编写回滚脚本
     *    - 或者使用 Flyway 的 undo 功能（企业版）
     *
     * 5. 测试
     *    - 在测试环境验证迁移脚本
     *    - 测试迁移的成功和失败场景
     *    - 测试数据完整性
     *
     * 6. 监控
     *    - 监控迁移执行时间
     *    - 监控迁移失败情况
     *    - 记录迁移日志
     *
     * 常见问题：
     * 1. 迁移脚本执行失败
     *    - 检查脚本语法
     *    - 检查数据库权限
     *    - 检查版本号是否正确
     *
     * 2. 迁移脚本被修改
     *    - Flyway 会检测脚本内容的校验和
     *    - 如果脚本被修改，迁移会失败
     *    - 需要恢复原始脚本或创建新的迁移脚本
     *
     * 3. 版本号冲突
     *    - 多个开发者创建相同版本号的脚本
     *    - 需要重新编号脚本
     *    - 使用版本控制系统协调
     *
     * 与 Spring Boot 的集成：
     * - Spring Boot 自动配置 Flyway
     * - 应用启动时自动执行迁移
     * - 支持通过 application.yml 配置 Flyway
     *
     * 配置示例（application.yml）：
     * spring:
     *   flyway:
     *     enabled: true
     *     locations: classpath:db/migration
     *     baselineOnMigrate: true
     *     validateOnMigrate: true
     *
     * 与多数据源的集成：
     * - 为每个数据源配置独立的 Flyway
     * - 使用不同的迁移脚本目录
     * - 分别管理每个数据源的版本
     */
    public FlywayConfig() {
        // Flyway 由 Spring Boot 自动配置
    }
}
