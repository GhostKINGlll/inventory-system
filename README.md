# 进销存管理系统（Inventory System）

基于 **Spring Boot 3 + Thymeleaf + Spring Data JPA + MySQL** 的中小企业进销存管理系统，覆盖商品、采购、销售、库存、客户、报表、样品单等完整业务流程，并集成 AI 助手与邮件验证码。

## 技术栈

| 分类 | 技术 |
| --- | --- |
| 语言 / 运行时 | Java 17+ |
| 框架 | Spring Boot 3.1.5、Spring MVC、Spring Data JPA |
| 模板引擎 | Thymeleaf |
| 数据库 | MySQL 5.7+ / 8.x |
| 构建工具 | Maven |
| 安全 | spring-security-crypto（BCrypt 密码加密） |
| 其他 | POI（Excel 导出）、Tess4J（OCR）、Spring Mail、火山方舟大模型 API |

## 功能模块

- **商品管理**：商品档案、SKU、库存上下限预警
- **采购管理**：采购单录入、入库、供应商往来
- **销售管理**：销售开单、出库、客户对账单
- **库存管理**：实时库存、盘点、调拨
- **客户管理**：客户档案、消费记录、对账单导出
- **报表统计**：销售/采购/利润多维度统计，支持 Excel 导出
- **样品单 / 阶梯价**：样品申请流转、批量采购阶梯报价
- **AI 助手**：基于大模型的经营数据问答
- **用户与权限**：注册、邮箱验证码、找回密码、VIP 权限控制、管理后台

## 目录结构

```
spring-boot-project
├── pom.xml
├── src/main/java/com/inventory
│   ├── InventoryApplication.java     # 启动类 + 默认管理员初始化
│   ├── config/                       # 拦截器、AI 配置、全局模型
│   ├── controller/                   # 15 个控制器
│   ├── service/                      # 业务逻辑
│   ├── dao/                          # JPA Repository
│   └── entity/                       # 实体（商品、销售、采购、客户…）
├── src/main/resources
│   ├── application.yml               # 主配置（密钥全部走环境变量）
│   ├── application-local.yml.example # 本地配置模板（复制为 application-local.yml）
│   ├── templates/                    # Thymeleaf 页面
│   └── static/                       # 静态资源
└── Dockerfile / docker-compose.yml   # 一键容器化运行（可选）
```

## 快速开始

### 1. 准备环境

- JDK 17 或更高版本
- Maven 3.8+
- MySQL 5.7+ / 8.x，并创建数据库：

```sql
CREATE DATABASE inventory_db DEFAULT CHARACTER SET utf8mb4;
```

### 2. 配置数据库与密钥

复制配置模板（**真实密码不要写进 `application.yml`，也不要提交到仓库**）：

```bash
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
```

然后编辑 `src/main/resources/application-local.yml`，填入：

- MySQL 用户名 / 密码
- QQ 邮箱账号与 **SMTP 授权码**（用于注册验证码、找回密码）
- 火山方舟 API Key（用于 AI 助手，可选）

> 也可以不用 `application-local.yml`，直接使用环境变量：`DB_URL`、`DB_USERNAME`、`DB_PASSWORD`、`MAIL_USERNAME`、`MAIL_PASSWORD`、`AI_API_KEY` 等，详见 `application.yml`。

### 3. 启动

```bash
mvn spring-boot:run
```

或先打包再运行：

```bash
mvn clean package -DskipTests
java -jar target/inventory-system-1.0.0.jar
```

### 4. 访问

打开浏览器访问 <http://localhost:8080>

**默认管理员账号**（首次启动自动创建）：

```
用户名：admin
密码：  123456
```

> 登录要求账号具备 `vip = true`。若管理员无法登录，请在数据库中执行：
> `UPDATE users SET vip = 1 WHERE username = 'admin';`

## Docker 运行（可选）

```bash
docker compose up -d --build
```

访问 <http://localhost:8080>。数据库数据保存在 `mysql-data` 卷中。

## 安全说明

- 仓库中的 `application.yml` **只包含占位符**，所有敏感信息通过环境变量或未入库的 `application-local.yml` 注入。
- 若不慎将真实密钥提交到公开仓库，请立即在对应平台**重置**（QQ 邮箱重新生成授权码、方舟重新生成 API Key），并清理 Git 历史。
- 生产环境请务必修改默认管理员密码，并把 `spring.jpa.hibernate.ddl-auto` 改为 `none` 或 `validate`。

## License

本项目仅用于学习与作品展示。
