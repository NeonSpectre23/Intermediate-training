# Intermediate-training

## 项目简介

Intermediate-training是一个在线编程评测系统（Online Judge），用于用户进行编程练习、提交代码并获得实时评测结果。该系统包含后端服务、前端应用两个主要部分，通过集成Judge0的API服务实现代码评测，支持多种编程语言的代码评测和代码混淆功能。

## 主要功能

### 1. 用户管理
- 用户注册、登录、个人信息管理

### 2. 题目管理
- 题目列表展示和搜索
- 题目详情查看
- 题目添加、编辑、删除

### 3. 代码提交与评测
- 支持多种编程语言的代码提交
- 实时代码评测和结果返回
- 评测结果包含运行时间、内存消耗等信息

### 4. 代码混淆
- 支持C语言和Python代码的混淆
- 提供多种混淆策略

### 5. 收藏与点赞
- 支持题目收藏功能
- 支持帖子点赞功能

## 项目亮点

1. **Judge0 API集成**：采用Judge0的API服务替代传统代码沙箱，提供更稳定、高效的代码评测环境，支持多种编程语言
2. **代码混淆技术**：实现了C语言和Python代码的多种混淆策略，增强代码安全性
3. **响应式设计**：前端采用Vue 3 + TypeScript + Ant Design Vue构建，提供流畅的用户体验
4. **模块化架构**：后端基于Spring Boot 2.7.x，采用分层设计，代码结构清晰，易于扩展
5. **全面的测试支持**：后端使用JUnit进行单元测试，前端使用Vitest进行单元测试，确保系统稳定性
6. **丰富的用户交互**：支持题目收藏、帖子点赞等社交功能，增强用户粘性
7. **高性能**：采用MyBatis Plus进行数据库操作，优化查询性能，支持大量用户并发访问

## 技术栈

### 后端（oj-backend）
- Java 21
- Spring Boot 2.7.x
- MyBatis Plus
- MySQL


### 前端（oj-frontend）
- Vue 3
- TypeScript
- Ant Design Vue
- Vite

## 项目结构

```
Intermediate-training/
├── oj-backend/          # 后端服务
│   ├── src/            # 源代码
│   ├── sql/            # 数据库脚本
│   └── pom.xml         # Maven配置
├── oj-frontend/         # 前端应用
│   ├── src/            # 源代码
│   ├── public/         # 静态资源
│   └── package.json    # npm配置
├── Tools/              # 辅助工具
└── README.md           # 项目说明文档
```

## 部署与运行

### 环境要求
- JDK 21
- Maven 3.8+
- Node.js 16+
- MySQL 8.0+
- Redis 6.0+

### 后端部署

1. **创建数据库**
   - 执行 `oj-backend/sql/create_table.sql` 创建数据库表结构
   
2. **配置数据库连接**
   - 修改 `oj-backend/src/main/resources/application.yml` 中的数据库连接信息
   
3. **启动后端服务**
   ```bash
   cd oj-backend
   ./mvnw.cmd spring-boot:run
   ```

### 前端部署

1. **安装依赖**
   ```bash
   cd oj-frontend
   yarn install
   yarn add monaco-editor-webpack-plugin -D
   yarn add monaco-editor -D
   yarn add moment
   ```
   
2. **启动开发服务器**
   ```bash
   npm run serve
   ```
   
3. **构建生产版本**
   ```bash
   npm run build
   ```


## 开发说明

### API文档
- 后端提供Swagger API文档，访问地址：`http://localhost:8080/swagger-ui.html`

### 测试
- 后端：使用JUnit进行单元测试
- 前端：使用Vitest进行单元测试

## 贡献指南

1. Fork本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开Pull Request

## 许可证

本项目采用MIT许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

