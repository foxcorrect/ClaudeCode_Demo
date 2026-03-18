# API 接口文档

本文档描述了微信小程序匹配聊天系统的后端API接口。所有接口返回统一的`ApiResponse`格式：

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1631234567890
}
```

**状态码说明：**
- 200: 成功
- 400: 参数错误
- 401: 未授权
- 403: 禁止访问
- 500: 服务器内部错误

---

## 认证相关接口 (AuthController)

基础路径：`/auth`

### 1. 微信登录
- **接口地址:** `POST /auth/login`
- **功能描述:** 使用微信code进行登录
- **请求头:** 无特殊要求
- **请求体:**
  ```json
  {
    "code": "微信登录code",
    "encryptedData": "加密数据",
    "iv": "加密算法的初始向量"
  }
  ```
- **响应数据:** `String` (JWT Token)
- **示例响应:**
  ```json
  {
    "code": 200,
    "message": "登录成功",
    "data": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "timestamp": 1631234567890
  }
  ```

### 2. 获取用户信息（通过Token）
- **接口地址:** `GET /auth/user-info`
- **功能描述:** 通过请求头中的Token获取用户信息
- **请求头:** `Authorization: Bearer {token}`
- **请求参数:** 无
- **响应数据:** `UserInfoResponse` (用户信息)
- **注意:** 当前实现需要前端传递用户ID参数，接口暂时返回错误提示

### 3. 获取用户信息（通过用户ID）
- **接口地址:** `GET /auth/user-info/{userId}`
- **功能描述:** 通过用户ID获取用户信息
- **路径参数:**
  - `userId`: 用户ID
- **响应数据:** `UserInfoResponse` (用户信息)

### 4. 刷新Token
- **接口地址:** `POST /auth/refresh-token`
- **功能描述:** 刷新JWT Token
- **请求头:** `Authorization: {token}`
- **请求参数:** 无
- **响应数据:** `String` (新的JWT Token)

### 5. 退出登录
- **接口地址:** `POST /auth/logout`
- **功能描述:** 用户退出登录
- **请求头:** `Authorization: {token}`
- **请求参数:** 无
- **响应数据:** `null`

---

## 用户管理接口 (UserController)

基础路径：`/users`

### 1. 获取用户信息
- **接口地址:** `GET /users/{userId}`
- **功能描述:** 获取指定用户的详细信息
- **路径参数:**
  - `userId`: 用户ID
- **响应数据:** `UserInfoResponse` (用户信息)

### 2. 更新用户信息
- **接口地址:** `PUT /users/{userId}`
- **功能描述:** 更新用户信息
- **路径参数:**
  - `userId`: 用户ID
- **请求体:** `User` 实体对象
- **响应数据:** `null`

### 3. 更新用户状态
- **接口地址:** `PUT /users/{userId}/status`
- **功能描述:** 更新用户状态
- **路径参数:**
  - `userId`: 用户ID
- **请求参数:**
  - `status`: 状态值 (Integer)
- **响应数据:** `null`

### 4. 获取在线用户列表
- **接口地址:** `GET /users/online`
- **功能描述:** 获取当前在线用户列表
- **请求参数:** 无
- **响应数据:** `List<User>` (用户列表)

### 5. 获取可匹配用户列表
- **接口地址:** `GET /users/available`
- **功能描述:** 获取可匹配的用户列表
- **请求参数:** 无
- **响应数据:** `List<User>` (用户列表)

### 6. 分页查询用户
- **接口地址:** `GET /users/page`
- **功能描述:** 分页查询用户
- **请求参数:**
  - `pageNum`: 页码，默认值: 1
  - `pageSize`: 每页大小，默认值: 20
- **响应数据:** `Page<User>` (分页结果)

### 7. 搜索用户
- **接口地址:** `GET /users/search`
- **功能描述:** 根据关键词搜索用户
- **请求参数:**
  - `keyword`: 搜索关键词
- **响应数据:** `List<User>` (用户列表)

### 8. 获取用户统计信息
- **接口地址:** `GET /users/stats`
- **功能描述:** 获取用户统计信息
- **请求参数:** 无
- **响应数据:** `Object` (统计信息对象)

---

## 匹配相关接口 (MatchController)

基础路径：`/match`

### 1. 加入匹配池
- **接口地址:** `POST /match/join`
- **功能描述:** 将用户加入匹配池
- **请求参数:**
  - `userId`: 用户ID
- **响应数据:** `null`

### 2. 离开匹配池
- **接口地址:** `POST /match/leave`
- **功能描述:** 将用户从匹配池移除
- **请求参数:**
  - `userId`: 用户ID
- **响应数据:** `null`

### 3. 获取匹配状态
- **接口地址:** `GET /match/status`
- **功能描述:** 获取用户的匹配状态
- **请求参数:**
  - `userId`: 用户ID
- **响应数据:** `Integer` (状态码)

### 4. 执行匹配
- **接口地址:** `POST /match/execute`
- **功能描述:** 为用户匹配5个对象
- **请求参数:**
  - `userId`: 用户ID
- **响应数据:** `List<MatchResultResponse>` (匹配结果列表)

### 5. 获取用户匹配记录
- **接口地址:** `GET /match/records`
- **功能描述:** 获取用户的匹配记录
- **请求参数:**
  - `userId`: 用户ID
- **响应数据:** `List<MatchRecord>` (匹配记录列表)

### 6. 获取活跃匹配记录
- **接口地址:** `GET /match/active`
- **功能描述:** 获取用户的活跃匹配记录
- **请求参数:**
  - `userId`: 用户ID
- **响应数据:** `List<MatchRecord>` (匹配记录列表)

### 7. 提交满意度反馈
- **接口地址:** `POST /match/feedback`
- **功能描述:** 提交匹配满意度反馈
- **请求参数:**
  - `userId`: 用户ID
  - `matchId`: 匹配记录ID
  - `satisfaction`: 满意度评分 (Integer)
- **响应数据:** `null`

### 8. 交换微信
- **接口地址:** `POST /match/exchange-wechat`
- **功能描述:** 交换双方的微信联系方式
- **请求参数:**
  - `userId1`: 用户1ID
  - `userId2`: 用户2ID
  - `matchId`: 匹配记录ID
- **响应数据:** `null`

### 9. 删除匹配
- **接口地址:** `DELETE /match/delete`
- **功能描述:** 删除匹配记录
- **请求参数:**
  - `userId`: 用户ID
  - `matchId`: 匹配记录ID
- **响应数据:** `null`

### 10. 获取匹配池大小
- **接口地址:** `GET /match/pool-size`
- **功能描述:** 获取当前匹配池中的用户数量
- **请求参数:** 无
- **响应数据:** `Integer` (匹配池大小)

### 11. 清理过期匹配
- **接口地址:** `POST /match/cleanup`
- **功能描述:** 清理过期的匹配记录
- **请求参数:** 无
- **响应数据:** `Integer` (清理的记录数量)

---

## 聊天相关接口 (ChatController)

基础路径：`/chat`

### 1. 发送消息
- **接口地址:** `POST /chat/send`
- **功能描述:** 发送聊天消息（HTTP备用通道）
- **请求体:**
  ```json
  {
    "matchId": "匹配ID",
    "senderId": 123,
    "receiverId": 456,
    "messageType": 1,
    "content": "消息内容"
  }
  ```
- **响应数据:** `null`

### 2. 获取聊天历史
- **接口地址:** `GET /chat/history/{matchId}`
- **功能描述:** 获取指定匹配的聊天历史
- **路径参数:**
  - `matchId`: 匹配记录ID
- **请求参数:**
  - `pageNum`: 页码，默认值: 1
  - `pageSize`: 每页大小，默认值: 20
- **响应数据:** `List<ChatMessage>` (聊天消息列表)

### 3. 获取未读消息数量
- **接口地址:** `GET /chat/unread-count`
- **功能描述:** 获取用户的未读消息数量
- **请求参数:**
  - `userId`: 用户ID
- **响应数据:** `Integer` (未读消息数量)

### 4. 标记消息已读
- **接口地址:** `POST /chat/mark-read`
- **功能描述:** 将消息标记为已读
- **请求参数:**
  - `userId`: 用户ID
  - `matchId`: 匹配记录ID
- **响应数据:** `null`

### 5. 获取离线消息
- **接口地址:** `GET /chat/offline`
- **功能描述:** 获取用户的离线消息
- **请求参数:**
  - `userId`: 用户ID
- **响应数据:** `List<ChatMessage>` (离线消息列表)

### 6. 清理过期聊天消息
- **接口地址:** `POST /chat/cleanup`
- **功能描述:** 清理过期的聊天消息
- **请求参数:** 无
- **响应数据:** `Integer` (清理的消息数量)

---

## 通知相关接口 (NotificationController)

基础路径：`/notifications`

### 1. 获取用户通知列表
- **接口地址:** `GET /notifications`
- **功能描述:** 获取用户的通知列表
- **请求参数:**
  - `userId`: 用户ID
  - `pageNum`: 页码，默认值: 1
  - `pageSize`: 每页大小，默认值: 20
- **响应数据:** `List<Notification>` (通知列表)

### 2. 获取未读通知数量
- **接口地址:** `GET /notifications/unread-count`
- **功能描述:** 获取用户的未读通知数量
- **请求参数:**
  - `userId`: 用户ID
- **响应数据:** `Integer` (未读通知数量)

### 3. 标记通知已读
- **接口地址:** `POST /notifications/{notificationId}/read`
- **功能描述:** 将指定通知标记为已读
- **路径参数:**
  - `notificationId`: 通知ID
- **请求参数:**
  - `userId`: 用户ID
- **响应数据:** `null`

### 4. 标记所有通知已读
- **接口地址:** `POST /notifications/mark-all-read`
- **功能描述:** 将用户的所有通知标记为已读
- **请求参数:**
  - `userId`: 用户ID
- **响应数据:** `null`

### 5. 删除通知
- **接口地址:** `DELETE /notifications/{notificationId}`
- **功能描述:** 删除指定通知
- **路径参数:**
  - `notificationId`: 通知ID
- **请求参数:**
  - `userId`: 用户ID
- **响应数据:** `null`

### 6. 清理过期通知
- **接口地址:** `POST /notifications/cleanup`
- **功能描述:** 清理过期的通知
- **请求参数:** 无
- **响应数据:** `Integer` (清理的通知数量)

### 7. 创建测试通知
- **接口地址:** `POST /notifications/test`
- **功能描述:** 创建测试通知
- **请求参数:**
  - `userId`: 用户ID
- **响应数据:** `null`

---

## 标签管理接口 (TagController)

基础路径：`/tags`

### 1. 获取所有可用标签
- **接口地址:** `GET /tags`
- **功能描述:** 获取所有可用标签
- **请求参数:** 无
- **响应数据:** `List<Tag>` (标签列表)

### 2. 根据分类获取标签
- **接口地址:** `GET /tags/category/{category}`
- **功能描述:** 根据分类获取标签
- **路径参数:**
  - `category`: 分类名称
- **响应数据:** `List<Tag>` (标签列表)

### 3. 根据类型获取标签
- **接口地址:** `GET /tags/type/{type}`
- **功能描述:** 根据类型获取标签
- **路径参数:**
  - `type`: 标签类型 (Integer)
- **响应数据:** `List<Tag>` (标签列表)

### 4. 搜索标签
- **接口地址:** `GET /tags/search`
- **功能描述:** 根据关键词搜索标签
- **请求参数:**
  - `keyword`: 搜索关键词
- **响应数据:** `List<Tag>` (标签列表)

### 5. 获取用户标签列表
- **接口地址:** `GET /tags/user/{userId}`
- **功能描述:** 获取用户的所有标签
- **路径参数:**
  - `userId`: 用户ID
- **响应数据:** `List<UserTag>` (用户标签列表)

### 6. 获取用户标签ID列表
- **接口地址:** `GET /tags/user/{userId}/ids`
- **功能描述:** 获取用户的标签ID列表
- **路径参数:**
  - `userId`: 用户ID
- **响应数据:** `List<Long>` (标签ID列表)

### 7. 添加用户标签
- **接口地址:** `POST /tags/user/{userId}`
- **功能描述:** 为用户添加标签
- **路径参数:**
  - `userId`: 用户ID
- **请求参数:**
  - `tagId`: 标签ID
  - `weight`: 权重，默认值: 1
- **响应数据:** `null`

### 8. 批量添加用户标签
- **接口地址:** `POST /tags/user/{userId}/batch`
- **功能描述:** 批量为用户添加标签
- **路径参数:**
  - `userId`: 用户ID
- **请求体:** `List<Long>` (标签ID列表)
- **请求参数:**
  - `weight`: 权重，默认值: 1
- **响应数据:** `null`

### 9. 移除用户标签
- **接口地址:** `DELETE /tags/user/{userId}`
- **功能描述:** 移除用户的标签
- **路径参数:**
  - `userId`: 用户ID
- **请求参数:**
  - `tagId`: 标签ID
- **响应数据:** `null`

### 10. 清空用户标签
- **接口地址:** `DELETE /tags/user/{userId}/all`
- **功能描述:** 清空用户的所有标签
- **路径参数:**
  - `userId`: 用户ID
- **响应数据:** `null`

### 11. 更新用户标签权重
- **接口地址:** `PUT /tags/user/{userId}`
- **功能描述:** 更新用户标签的权重
- **路径参数:**
  - `userId`: 用户ID
- **请求参数:**
  - `tagId`: 标签ID
  - `weight`: 新的权重值
- **响应数据:** `null`

### 12. 计算两个用户的标签匹配度
- **接口地址:** `GET /tags/match-score`
- **功能描述:** 计算两个用户的标签匹配度
- **请求参数:**
  - `userId1`: 用户1ID
  - `userId2`: 用户2ID
- **响应数据:** `Integer` (匹配度分数)

### 13. 获取用户的标签匹配推荐用户
- **接口地址:** `GET /tags/recommendations/{userId}`
- **功能描述:** 根据标签匹配度获取推荐用户
- **路径参数:**
  - `userId`: 用户ID
- **请求参数:**
  - `limit`: 推荐数量，默认值: 10
- **响应数据:** `List<Long>` (推荐用户ID列表)

---

## 实体类说明

### ApiResponse<T>
通用API响应类
- `code`: 状态码
- `message`: 消息
- `data`: 响应数据
- `timestamp`: 时间戳

### UserInfoResponse
用户信息响应
（具体字段参考实体类定义）

### LoginRequest
登录请求
- `code`: 微信登录code
- `encryptedData`: 加密数据
- `iv`: 加密算法的初始向量

### SendMessageRequest
发送消息请求
- `matchId`: 匹配ID
- `senderId`: 发送者ID
- `receiverId`: 接收者ID
- `messageType`: 消息类型 (1-文本，2-图片，3-语音)
- `content`: 消息内容

### MatchResultResponse
匹配结果响应
（具体字段参考实体类定义）

### ChatMessage
聊天消息实体
- `id`: 消息ID
- `matchId`: 匹配ID
- `senderId`: 发送者ID
- `receiverId`: 接收者ID
- `messageType`: 消息类型
- `content`: 消息内容
- `isRead`: 是否已读
- `sendTime`: 发送时间

### Notification
通知实体
- `id`: 通知ID
- `userId`: 用户ID
- `type`: 通知类型
- `title`: 通知标题
- `content`: 通知内容
- `relatedId`: 相关ID
- `isRead`: 是否已读
- `createTime`: 创建时间

### Tag
标签实体
- `id`: 标签ID
- `name`: 标签名称
- `category`: 分类
- `type`: 类型
- `weight`: 权重

### UserTag
用户标签关联实体
- `id`: 关联ID
- `userId`: 用户ID
- `tagId`: 标签ID
- `weight`: 权重
- `createTime`: 创建时间

---

## 使用说明

1. **认证:** 除登录接口外，其他接口大多需要用户ID参数，部分接口可能需要Token认证
2. **参数传递:** 注意区分路径参数、查询参数和请求体参数
3. **错误处理:** 所有接口使用统一的错误响应格式
4. **分页:** 支持分页的接口使用`pageNum`和`pageSize`参数

## 更新记录

- 2026-03-18: 创建初始API文档
