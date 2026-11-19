# PMLS 网点物资管理系统

基于 Spring Boot 4 的网点级物资/库存管理后端，含 JWT+RBAC、事务与防超卖、
网点数据隔离、Redis 缓存、EasyExcel 导入导出、统计看板与单页演示台。

## 技术栈

| 项 | 版本 |
|---|---|
| JDK | 17（测试与 spring-boot:run 实测 17.0.19） |
| Spring Boot | 4.1.1（Tomcat 11 / Spring 7 / Jackson 3） |
| 持久层 | MyBatis-Plus 3.5.17 + MySQL 8.0 |
| 缓存 | Redis 7（spring-data-redis 4.1.1，Lettuce） |
| 安全 | Spring Security 7.1 + JJWT 0.12.7 |
| Excel | EasyExcel 4.0.3（POI 5.2.5） |
| 测试 | JUnit Jupiter 6.0.3 + Surefire 3.5.6 |

## 本地启动

```bash
docker start 1-mysql-1 sc-redis      # 3306 / 6379
mvn clean compile
mvn spring-boot:run                  # 或 IDEA Run Application
```

浏览器打开 `http://localhost:8080/index.html`，右上角登录：

| 账号 | 密码 | 角色 |
|---|---|---|
| admin | 123456 | ADMIN |
| nanchang01 | 123456 | OPERATOR（数据锁定 branchId=1） |

> 本机 8080 曾被其他演示项目（Jenkins / demo-backend）占用。启动前
> `docker stop demo-frontend demo-backend jenkins registry sc-nacos rabbitmq`，
> 并确认 `netstat -ano | findstr ":8080" | findstr LISTENING` 无输出。

## 接口一览

| 模块 | 路径 |
|---|---|
| 认证 | POST `/api/auth/login` |
| 库存 | GET `/api/stocks`、POST `/api/stocks/in|out|transfer` |
| 盘点 | POST `/api/inventory/checks`、PUT `/api/inventory/checks/{id}/items|audit` |
| 物资 | GET `/api/materials`、`/nocache`、`/page`；POST/PUT/DELETE `/api/materials` |
| 网点 | GET `/api/branches/page`；POST/PUT/DELETE `/api/branches` |
| 看板 | GET `/api/dashboard/category-share|branch-rank|monthly-trend` |
| Excel | POST `/api/excel/materials/import`、GET `/api/excel/stocks/export` |

所有业务接口需 `Authorization: Bearer <token>`；`/index.html` 由
`SecurityConfig` 显式 `permitAll()` 放行。

## 实测数据（均有日志/响应）

| 验证项 | 结果 |
|---|---|
| 缓存命中 | 10 次 `/materials` → 物资表查询 1 次（缓存过期回源）；10 次 `/nocache` → 10 次 |
| Redis 内容 | `pmls:material:list`，JSON 带 `@class` 可还原 `List<Material>`，TTL 正数 |
| Cache Aside | 新增物资后 `EXISTS pmls:material:list` = 0 |
| Excel 导入 | 200 条 / 766 ms / 0 错误 |
| JUnit | `StockTransactionTest` 3 例，0 失败 0 错误，@Transactional 自动回滚 |
| 并发防超卖 | 50 线程 / 984 ms / 成功 30 / 拒绝 20 / 异常 0 / 库存 0 无负数 / 日志 30 条 |
| 分页 | `/materials/page?size=3` → total=9；`/branches/page` → total=3 |

复现命令：

```bash
mvn -Dtest=StockTransactionTest test
# 重置库存后跑并发
mysql ... -e "UPDATE t_stock SET quantity=30, version=0 WHERE branch_id=1 AND material_id=1;"
java -cp "target\test-classes" com.example1.ConcurrentOutBenchmark
# 跑完恢复：DELETE 日志 WHERE remark='bench_50_out'；quantity 恢复原值
```

## 设计取舍与已知限制

- **缓存不一定更快**：本机小数据量下端到端耗时基本不变。缓存收益取决于
  数据规模、查询复杂度与数据库是否成为瓶颈，本项目用 SQL 日志条数验证而非毫秒数。
- **每个请求回查一次 `t_user`**：`JwtAuthFilter` 为保证「禁用/删除即时生效」
  不缓存用户状态；可短 TTL 缓存优化，但会牺牲即时性。
- **分页手写 LIMIT + COUNT**：未启用 MyBatis-Plus 分页插件（依赖已引入），
  避免 Boot 4 拦截器适配问题；深分页可用游标/延迟关联优化。
- **Spring Boot 4 用 Jackson 3**（包名 `tools.jackson`）。为 Redis 序列化
  额外引入 Jackson 2.21.5，两者包名不同可共存，不冲突。
- 前端为单页静态演示台，不含构建工具与前端框架；项目定位是后端机制演示，
  不是生产系统（无 Dockerfile / CI / 接口文档 / 刷新令牌 / 限流 / Controller 层测试）。
- 配置文件中的口令是**演示用默认值**；部署时通过环境变量 `DB_PASSWORD`、
  `JWT_SECRET` 覆盖，并把真实值放在不入库的 `application-local.yml`。

## 常见问题

| 现象 | 处理 |
|---|---|
| `Port 8080 was already in use` | 停占用容器或改 `server.port` |
| `/index.html` 403 | `permitAll()` 必须在 `anyRequest().authenticated()` 之前 |
| `/index.html` 404 | 未 `mvn clean compile`，或文件不在 `src/main/resources/static/` |
| 页面是别的项目的 | 核对 `curl -s ... \| findstr "<title>"` 的内容，别只看 HTTP 200 |
| `Invalid bound statement: insertBatch` | `resources/mapper/MaterialMapper.xml` 缺失或位置错 |
| 中文在 PowerShell 里乱码 | 显示编码问题，`[Console]::OutputEncoding = UTF8`；库中数据本身正常 |
