# 本地启动指南

## 环境要求

- JDK 17
- MySQL 8
- Node.js 18+（运行 React 前端必需）
- Maven（或用 backend 自带的 `mvnw.cmd`）

## 1. 初始化数据库

在项目根目录执行（将 `root` 和密码换成你的 MySQL 账号）：

```powershell
mysql -u root -p -e "DROP DATABASE IF EXISTS test; CREATE DATABASE test DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p --default-character-set=utf8mb4 test < backend/src/main/resources/sql/init.sql
```

或在 MySQL 客户端中：

```sql
DROP DATABASE IF EXISTS test;
CREATE DATABASE test DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE test;
SET NAMES utf8mb4;
-- 将下方路径替换为你的项目根目录
SOURCE /path/to/project/backend/src/main/resources/sql/init.sql;
SHOW TABLES;
```

> 若导入报中文乱码或 `ERROR 1366`，务必使用 `--default-character-set=utf8mb4` 或先执行 `SET NAMES utf8mb4;`。
> `init.sql` 不含测试用户。农户、买家可在注册页自助注册；演示账号见下文。

## 1.5 导入演示数据（推荐）

完成 `init.sql` 后，导入测试账号与商品数据，避免页面空白。

PowerShell 请用（`<` 在 PowerShell 里不可用）：

```powershell
Get-Content docs/seed-data.sql -Raw -Encoding UTF8 | mysql -u root -p --default-character-set=utf8mb4 test
```

或在 **MySQL Workbench / Navicat** 中直接打开 `docs/seed-data.sql` 执行。

**测试账号（密码均为 `123456`）**

| 用户名 | 角色 | 说明 |
|--------|------|------|
| farmer01 | 农户 | 演示农户账号 |
| buyer01 | 买家 | 演示买家账号 |
| expert01 | 专家 | 登录账号；专家列表中显示为 **「王专家」** |
| bank01 | 银行工作人员 | 对应审批人 **「赵经理」** |
| admin01 | 平台管理员 | 平台管理账号 |

> 农户端「专家预约 → 专家列表」读取的是 `experts` 表，不是 `tb_user`。`expert01` 是登录名，对应专家档案 **王专家**。

## 2. 配置后端

复制本地配置模板并填写 MySQL 密码（该文件已被 `.gitignore` 忽略，不会提交到 Git）：

```powershell
copy backend\src\main\resources\application-local.yml.example backend\src\main\resources\application-local.yml
```

编辑 `backend/src/main/resources/application-local.yml`：

```yaml
spring:
  datasource:
    password: 你的MySQL密码

deepseek:
  api-key: 你的DeepSeek密钥   # 可选，物价预测等功能需要
```

也可通过环境变量 `DB_PASSWORD` 覆盖默认密码，无需创建 `application-local.yml`。

默认连接：`jdbc:mysql://localhost:3306/test`，端口 `8080`。更多项见 `application.yml`。

## 3. 启动后端

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

启动成功后 API 地址：`http://localhost:8080`

验证测试（可选）：

```powershell
cd backend
.\mvnw.cmd test
```

## 4. 启动 React 前端

```powershell
cd frontend
copy .env.example .env
npm install
npm run dev
```

浏览器访问 `http://localhost:5173`，注册并登录即可使用各功能模块。

API 地址在 `frontend/.env` 中配置，默认：

```env
VITE_API_BASE=http://localhost:8080
```

| 路由 | 功能 | 可见角色 |
|------|------|----------|
| `/` | 首页 · 新闻轮播 | 全部（新闻仅农户/买家） |
| `/loans` | 融资服务 | 农户、银行 |
| `/appointment` | 专家预约 | 农户、买家、专家 |
| `/buy` | 农产品商城 | 买家 |
| `/knowledge` | 知识库 | 农户、买家、专家、管理员 |
| `/products` | 农产品管理 | 农户 |
| `/need` | 求购平台 | 买家 |
| `/price` | 物价预测 | 买家 |
| `/profile` | 个人中心 | 全部 |
| `/admin` | 用户管理 | 管理员 |

验证测试（可选）：

```powershell
cd frontend
npm test
```
