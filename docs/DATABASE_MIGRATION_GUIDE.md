# 数据库迁移指南

## 概述

本项目使用Flyway进行数据库版本管理，支持自动化的数据库架构演进和版本控制。

## 核心特性

### 1. 自动迁移

- 应用启动时自动执行待迁移的SQL脚本
- 按版本号顺序执行
- 记录迁移历史到flyway_schema_history表

### 2. 版本控制

迁移脚本命名规则：`V{版本号}__{描述}.sql`

示例：
- `V1__Initial_Schema.sql` - 初始架构
- `V2__Add_User_Table.sql` - 添加用户表
- `V3__Add_Indexes.sql` - 添加索引

### 3. 环境隔离

- **开发环境**: 允许清理数据库（clean-disabled: false）
- **生产环境**: 禁止清理数据库（clean-disabled: true）

## 配置说明

### 开发环境 (application-flyway-dev.yml)

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    out-of-order: false
    validate-on-migrate: true
    clean-disabled: false  # 允许清理
```

### 生产环境 (application-flyway-prod.yml)

```yaml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
    out-of-order: false
    validate-on-migrate: true
    clean-disabled: true  # 禁止清理
```

## 初始架构

### 对话记录表 (conversation_records)

```sql
CREATE TABLE conversation_records (
    id BIGSERIAL PRIMARY KEY,
    conversation_id VARCHAR(255) NOT NULL UNIQUE,
    user_id VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    response TEXT,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);
```

### 审计日志表 (audit_logs)

```sql
CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    action VARCHAR(255) NOT NULL,
    user_id VARCHAR(255),
    details TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 通知表 (notifications)

```sql
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    recipient VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    subject VARCHAR(255),
    content TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    sent_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 文件上传表 (file_uploads)

```sql
CREATE TABLE file_uploads (
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    file_size BIGINT NOT NULL,
    file_type VARCHAR(100),
    file_path VARCHAR(500) NOT NULL,
    user_id VARCHAR(255),
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP
);
```

## 创建新的迁移脚本

### 步骤1: 创建SQL文件

在 `src/main/resources/db/migration` 目录下创建新文件：

```
V2__Add_User_Preferences.sql
```

### 步骤2: 编写迁移脚本

```sql
-- V2__Add_User_Preferences.sql
CREATE TABLE user_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL UNIQUE,
    language VARCHAR(10) DEFAULT 'zh_CN',
    theme VARCHAR(50) DEFAULT 'light',
    notifications_enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_user_id ON user_preferences(user_id);
```

### 步骤3: 启动应用

应用启动时会自动执行新的迁移脚本。

## 查看迁移历史

连接到数据库并查询迁移历史：

```sql
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

输出示例：

```
installed_rank | version | description | type | script | checksum | installed_by | installed_on | execution_time | success
1              | 1       | Initial Schema | SQL | V1__Initial_Schema.sql | 123456 | postgres | 2024-01-01 | 150 | true
```

## 最佳实践

### 1. 脚本设计

- 每个脚本应该是幂等的（可以安全地重复执行）
- 使用 `IF NOT EXISTS` 和 `IF EXISTS` 子句
- 避免在迁移中使用复杂的业务逻辑

### 2. 版本管理

- 版本号必须递增
- 不要修改已执行的迁移脚本
- 如果需要修改，创建新的迁移脚本来撤销和重新应用

### 3. 性能考虑

- 大表上的索引创建可能很慢，考虑使用 `CONCURRENTLY`
- 避免在迁移中执行长时间运行的操作
- 对于大数据量操作，考虑分批处理

### 4. 测试

- 在开发环境充分测试迁移脚本
- 使用 `flyway:clean` 重置数据库进行测试
- 验证迁移的可回滚性

## 故障排查

### 迁移失败

检查错误日志：

```
ERROR: Migration V1__Initial_Schema.sql failed
```

常见原因：
- SQL语法错误
- 表或列已存在
- 权限不足

### 版本冲突

如果版本号重复，Flyway会拒绝执行：

```
ERROR: Duplicate migration version 1
```

解决方案：
- 使用唯一的版本号
- 检查是否有其他开发者创建了相同版本的脚本

### 回滚

Flyway不支持自动回滚。如果需要回滚：

1. 创建新的迁移脚本来撤销更改
2. 或在开发环境使用 `flyway:clean` 重置

## 与Docker集成

在docker-compose.yml中配置PostgreSQL：

```yaml
postgres:
  image: postgres:15
  environment:
    POSTGRES_DB: langchain4j
    POSTGRES_USER: postgres
    POSTGRES_PASSWORD: password
  volumes:
    - postgres_data:/var/lib/postgresql/data
```

应用启动时会自动执行迁移。
