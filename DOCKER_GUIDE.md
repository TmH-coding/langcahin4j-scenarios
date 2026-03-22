# Docker 部署指南

## Dockerfile 配置

### 1. 单个场景的 Dockerfile

```dockerfile
# 使用官方Java运行时作为基础镜像
FROM openjdk:17-jdk-slim

# 设置工作目录
WORKDIR /app

# 复制JAR文件
COPY target/scenario-1-customer-service-1.0.0.jar app.jar

# 暴露端口
EXPOSE 8081

# 设置环境变量
ENV OPENAI_API_KEY=${OPENAI_API_KEY}
ENV SPRING_PROFILES_ACTIVE=prod

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 2. 多阶段构建 Dockerfile

```dockerfile
# 构建阶段
FROM maven:3.8.1-openjdk-17 AS builder

WORKDIR /build

# 复制源代码
COPY . .

# 构建项目
RUN mvn clean package -DskipTests

# 运行阶段
FROM openjdk:17-jdk-slim

WORKDIR /app

# 从构建阶段复制JAR
COPY --from=builder /build/scenario-1-customer-service/target/scenario-1-customer-service-1.0.0.jar app.jar

EXPOSE 8081

ENV OPENAI_API_KEY=${OPENAI_API_KEY}
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Docker Compose 配置

### docker-compose.yml

```yaml
version: '3.8'

services:
  # MySQL 数据库
  mysql:
    image: mysql:8.0
    container_name: langchain4j-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: langchain4j
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
    networks:
      - langchain4j-network

  # 客服系统
  customer-service:
    build:
      context: .
      dockerfile: scenario-1-customer-service/Dockerfile
    container_name: customer-service
    environment:
      OPENAI_API_KEY: ${OPENAI_API_KEY}
      SPRING_PROFILES_ACTIVE: prod
      DB_HOST: mysql
      DB_PORT: 3306
      DB_NAME: langchain4j
      DB_USER: root
      DB_PASSWORD: root
    ports:
      - "8081:8081"
    depends_on:
      - mysql
    networks:
      - langchain4j-network

  # 文档分析
  document-analysis:
    build:
      context: .
      dockerfile: scenario-2-document-analysis/Dockerfile
    container_name: document-analysis
    environment:
      OPENAI_API_KEY: ${OPENAI_API_KEY}
      SPRING_PROFILES_ACTIVE: prod
      DB_HOST: mysql
      DB_PORT: 3306
      DB_NAME: langchain4j
      DB_USER: root
      DB_PASSWORD: root
    ports:
      - "8082:8082"
    depends_on:
      - mysql
    networks:
      - langchain4j-network

  # 代码助手
  code-assistant:
    build:
      context: .
      dockerfile: scenario-3-code-assistant/Dockerfile
    container_name: code-assistant
    environment:
      OPENAI_API_KEY: ${OPENAI_API_KEY}
      SPRING_PROFILES_ACTIVE: prod
      DB_HOST: mysql
      DB_PORT: 3306
      DB_NAME: langchain4j
      DB_USER: root
      DB_PASSWORD: root
    ports:
      - "8083:8083"
    depends_on:
      - mysql
    networks:
      - langchain4j-network

  # 数据分析
  data-analyst:
    build:
      context: .
      dockerfile: scenario-4-data-analyst/Dockerfile
    container_name: data-analyst
    environment:
      OPENAI_API_KEY: ${OPENAI_API_KEY}
      SPRING_PROFILES_ACTIVE: prod
      DB_HOST: mysql
      DB_PORT: 3306
      DB_NAME: langchain4j
      DB_USER: root
      DB_PASSWORD: root
    ports:
      - "8084:8084"
    depends_on:
      - mysql
    networks:
      - langchain4j-network

  # 内容创作
  content-creator:
    build:
      context: .
      dockerfile: scenario-5-content-creator/Dockerfile
    container_name: content-creator
    environment:
      OPENAI_API_KEY: ${OPENAI_API_KEY}
      SPRING_PROFILES_ACTIVE: prod
      DB_HOST: mysql
      DB_PORT: 3306
      DB_NAME: langchain4j
      DB_USER: root
      DB_PASSWORD: root
    ports:
      - "8085:8085"
    depends_on:
      - mysql
    networks:
      - langchain4j-network

volumes:
  mysql_data:

networks:
  langchain4j-network:
    driver: bridge
```

## 构建和运行

### 1. 构建镜像

```bash
# 构建所有镜像
docker-compose build

# 构建特定镜像
docker-compose build customer-service
```

### 2. 启动容器

```bash
# 启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f customer-service
```

### 3. 停止容器

```bash
# 停止所有服务
docker-compose down

# 停止并删除数据
docker-compose down -v
```

### 4. 进入容器

```bash
# 进入容器
docker-compose exec customer-service bash

# 查看容器状态
docker-compose ps
```

## 环境变量配置

### .env 文件

```bash
# OpenAI API 密钥
OPENAI_API_KEY=sk-xxx

# 数据库配置
DB_HOST=mysql
DB_PORT=3306
DB_NAME=langchain4j
DB_USER=root
DB_PASSWORD=root

# Spring 配置
SPRING_PROFILES_ACTIVE=prod
```

## 常见问题

**Q: 如何查看容器日志？**
A: `docker-compose logs -f service-name`

**Q: 如何重启服务？**
A: `docker-compose restart service-name`

**Q: 如何更新镜像？**
A: `docker-compose build --no-cache && docker-compose up -d`

**Q: 如何清理所有容器和镜像？**
A: `docker-compose down -v && docker system prune -a`
