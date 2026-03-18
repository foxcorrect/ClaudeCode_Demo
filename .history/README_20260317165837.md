# 微信小程序匹配聊天系统后端

基于Java、Spring Boot、MyBatis构建的微信小程序匹配聊天系统后端。

## 功能特性

1. **用户管理**
   - 微信登录认证
   - 用户信息管理
   - 在线状态管理

2. **匹配系统**
   - 随机匹配算法（每人同时匹配5人）
   - 匹配池管理（基于Redis）
   - 满意度反馈机制
   - 微信交换功能

3. **实时聊天**
   - WebSocket实时通信
   - HTTP备用通道
   - 离线消息存储
   - 消息已读状态管理

4. **通知系统**
   - 实时WebSocket推送
   - 微信模板消息
   - 应用内通知
   - 未读计数管理

5. **数据管理**
   - 7天数据自动清理
   - 定时任务调度
   - Redis缓存管理

## 技术栈

- **后端框架**: Spring Boot 3.1.5
- **Java版本**: 17
- **数据库**: MySQL 8.0+
- **ORM框架**: MyBatis-Plus 3.5.3
- **缓存**: Redis 7.x
- **消息队列**: RabbitMQ（可选）
- **WebSocket**: Spring WebSocket
- **安全认证**: JWT
- **构建工具**: Maven

## 数据库设计

### 核心表结构

1. **user表**: 用户基本信息
2. **match_record表**: 匹配记录
3. **chat_message表**: 聊天消息
4. **wechat_relation表**: 微信交换记录
5. **notification表**: 通知记录

详细表结构见 `db/schema.sql`

## API文档

### 认证相关
- `POST /api/auth/login` - 微信登录
- `GET /api/auth/user-info/{userId}` - 获取用户信息
- `POST /api/auth/refresh-token` - 刷新Token
- `POST /api/auth/logout` - 退出登录

### 用户管理
- `GET /api/users/{userId}` - 获取用户信息
- `PUT /api/users/{userId}` - 更新用户信息
- `GET /api/users/online` - 获取在线用户列表
- `GET /api/users/available` - 获取可匹配用户列表

### 匹配系统
- `POST /api/match/join` - 加入匹配池
- `POST /api/match/execute` - 执行匹配（匹配5人）
- `POST /api/match/feedback` - 提交满意度反馈
- `POST /api/match/exchange-wechat` - 交换微信
- `DELETE /api/match/delete` - 删除匹配

### 聊天系统
- `POST /api/chat/send` - 发送消息（HTTP备用）
- `GET /api/chat/history/{matchId}` - 获取聊天历史
- `GET /api/chat/unread-count` - 获取未读消息数
- `POST /api/chat/mark-read` - 标记消息已读

### 通知系统
- `GET /api/notifications` - 获取用户通知列表
- `GET /api/notifications/unread-count` - 获取未读通知数
- `POST /api/notifications/{notificationId}/read` - 标记通知已读
- `DELETE /api/notifications/{notificationId}` - 删除通知

### WebSocket端点
- `ws://localhost:8080/api/ws/chat?userId={userId}&token={token}` - WebSocket连接

## 快速开始

### 环境要求
1. JDK 17+
2. MySQL 8.0+
3. Redis 7.x
4. Maven 3.8+

### 安装步骤

1. **克隆项目**
   ```bash
   git clone <项目地址>
   cd chat_java
   ```

2. **创建数据库**
   ```sql
   mysql -u root -p
   source db/schema.sql
   ```

3. **修改配置文件**
   编辑 `src/main/resources/application.yml`:
   - 修改数据库连接信息
   - 修改Redis连接信息
   - 配置微信小程序AppID和AppSecret

4. **编译项目**
   ```bash
   mvn clean package -DskipTests
   ```

5. **运行项目**
   ```bash
   java -jar target/match-backend-1.0.0.jar
   ```

6. **访问API**
   - 服务地址: http://localhost:8080/api
   - WebSocket地址: ws://localhost:8080/api/ws/chat

## 配置说明

### 关键配置项

```yaml
# 数据库配置
spring.datasource.url: jdbc:mysql://localhost:3306/wechat_match
spring.datasource.username: root
spring.datasource.password: your_password

# Redis配置
spring.redis.host: localhost
spring.redis.port: 6379

# 微信小程序配置
wechat.mini-program.app-id: your_app_id
wechat.mini-program.app-secret: your_app_secret

# JWT配置
jwt.secret: your_jwt_secret_key
jwt.expiration: 86400000

# 匹配系统配置
match.system.max-concurrent-matches: 5
match.system.chat-expire-days: 7
```

### 环境变量

支持以下环境变量：
- `DATABASE_URL`: 数据库连接URL
- `REDIS_HOST`: Redis主机地址
- `WECHAT_APP_ID`: 微信小程序AppID
- `WECHAT_APP_SECRET`: 微信小程序AppSecret
- `JWT_SECRET`: JWT密钥

## 定时任务

1. **数据清理任务**: 每天凌晨3点执行
   - 清理7天前的匹配记录
   - 清理7天前的聊天消息
   - 清理过期通知
   - 重置用户每日匹配次数

2. **用户状态维护**: 每小时执行
3. **匹配池维护**: 每30分钟执行

## 部署说明

### Docker部署

```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/match-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
```

### 生产环境建议

1. **数据库**: 使用主从复制，配置连接池
2. **Redis**: 使用集群模式，配置持久化
3. **应用服务器**: 使用Nginx负载均衡
4. **监控**: 集成Spring Boot Actuator + Prometheus
5. **日志**: 使用ELK Stack收集和分析日志

## 项目结构

```
src/main/java/com/wechat/match/
├── config/          # 配置类
├── controller/      # REST API控制器
├── service/         # 业务逻辑层
├── mapper/         # 数据访问层
├── entity/         # 数据库实体
├── dto/            # 数据传输对象
├── websocket/      # WebSocket处理器
├── scheduler/      # 定时任务
├── util/           # 工具类
└── exception/      # 异常处理
```

## 注意事项

1. **微信API限制**: 注意微信API调用频率限制，建议添加限流和重试机制
2. **WebSocket连接数**: 注意WebSocket连接数限制，建议使用连接池
3. **数据安全**: 敏感信息加密存储，防止SQL注入和XSS攻击
4. **性能优化**: 数据库合理索引，Redis缓存热点数据
5. **高可用**: 建议使用集群部署，实现负载均衡和故障转移

## 后续开发计划

1. **管理后台**: 用户管理、数据统计、系统监控
2. **高级匹配算法**: 基于兴趣标签、地理位置等
3. **消息类型扩展**: 支持图片、语音、视频消息
4. **第三方集成**: 集成更多社交平台
5. **国际化**: 支持多语言

## 许可证

本项目采用MIT许可证。

## 联系方式

如有问题或建议，请通过以下方式联系：
- 邮箱: [your-email@example.com]
- GitHub: [your-github-username]