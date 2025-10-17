# Doki 短视频社交平台 - 后端服务

## 项目概述

Doki是一个基于微服务架构的短视频社交平台，类似于抖音/TikTok的功能。项目采用Spring Boot + Spring Cloud技术栈，提供完整的短视频上传、处理、播放、社交等功能。

## 技术栈

- **框架**: Spring Boot 3.1.12 + Spring Cloud 2022.0.4
- **服务发现**: Nacos
- **数据库**: MyBatis Plus
- **消息队列**: RabbitMQ
- **视频处理**: FFmpeg
- **认证**: JWT
- **网关**: Spring Cloud Gateway

## 项目结构

```
doki-backend-cloud/
├── common-module/              # 公共模块
├── gateway/                    # API网关服务
├── user-service/              # 用户服务
├── image-service/             # 图片服务
├── video-upload-service/      # 视频上传服务
├── video-processing-service/  # 视频处理服务
├── video-play-service/        # 视频播放服务
├── audit-service/             # 内容审核服务
├── comment-service/           # 评论服务
├── like-favorite-service/     # 点赞收藏服务
├── social-service/            # 社交服务
├── feed-service/              # 推荐流服务
├── notification-dm-service/    # 通知私信服务
├── search-service/             # 搜索服务
└── analytics-service/         # 数据分析服务
```

## 服务功能说明

### 🔐 核心服务

#### Gateway (网关服务)
- **端口**: 10010
- **功能**:
  - 统一API入口
  - 路由转发
  - 负载均衡
  - 跨域处理
  - 请求限流

#### User Service (用户服务)
- **端口**: 9090
- **功能**:
  - 用户注册/登录
  - JWT认证
  - 用户信息管理
  - 密码修改
  - 用户资料编辑
  - 头像上传

### 📹 视频服务

#### Video Upload Service (视频上传服务)
- **功能**:
  - 视频文件上传
  - 上传进度监控
  - 文件格式验证
  - 分片上传支持
  - 上传状态管理

#### Video Processing Service (视频处理服务)
- **功能**:
  - 视频转码处理
  - 多格式支持 (MP4, AVI, MOV, MKV, WebM等)
  - 视频缩略图生成
  - 精灵图生成 (进度条预览)
  - 视频元数据提取
  - 异步处理队列

#### Video Play Service (视频播放服务)
- **功能**:
  - 视频流媒体播放
  - 多清晰度支持
  - 播放统计
  - 播放历史记录
  - 播放权限控制

### 🖼️ 媒体服务

#### Image Service (图片服务)
- **功能**:
  - 图片上传处理
  - 图片压缩优化
  - 多尺寸生成
  - 图片格式转换
  - CDN集成

### 🔍 内容服务

#### Audit Service (内容审核服务)
- **功能**:
  - 视频内容审核
  - 图片内容审核
  - 文本内容审核
  - 违规内容检测
  - 审核结果管理

#### Comment Service (评论服务)
- **功能**:
  - 评论发布/删除
  - 评论回复
  - 评论点赞
  - 评论举报
  - 评论审核

### ❤️ 互动服务

#### Like Favorite Service (点赞收藏服务)
- **功能**:
  - 视频点赞/取消点赞
  - 视频收藏/取消收藏
  - 点赞统计
  - 收藏夹管理
  - 互动数据统计

#### Social Service (社交服务)
- **功能**:
  - 用户关注/取消关注
  - 粉丝管理
  - 关注列表
  - 粉丝列表
  - 社交关系分析

### 📱 内容分发

#### Feed Service (信息流服务)
- **功能**:
  - 个性化推荐算法
  - 热门内容推荐
  - 关注用户动态
  - 内容排序
  - 推荐策略配置

#### Search Service (搜索服务)
- **功能**:
  - 视频内容搜索
  - 用户搜索
  - 标签搜索
  - 搜索建议
  - 搜索历史
  - 全文检索

### 💬 通信服务

#### Notification DM Service (通知私信服务)
- **功能**:
  - 系统通知推送
  - 私信发送/接收
  - 消息状态管理
  - 通知设置
  - 消息历史记录

### 📊 数据分析

#### Analytics Service (数据分析服务)
- **功能**:
  - 用户行为分析
  - 视频播放统计
  - 内容热度分析
  - 用户画像构建
  - 数据报表生成

## 核心功能流程

### 视频上传处理流程

1. **用户上传视频** → Video Upload Service
2. **文件验证** → 格式检查、大小限制
3. **消息队列** → RabbitMQ 异步处理
4. **视频转码** → Video Processing Service
5. **内容审核** → Audit Service
6. **生成预览** → 缩略图、精灵图
7. **发布上线** → Feed Service 推荐

### 用户认证流程

1. **用户注册/登录** → User Service
2. **JWT Token生成** → 安全认证
3. **网关验证** → Gateway 权限控制
4. **服务调用** → 各微服务API

## 技术特性

### 🚀 性能优化
- **异步处理**: RabbitMQ消息队列
- **视频转码**: FFmpeg多格式支持
- **智能缩放**: 根据视频比例动态调整
- **缓存策略**: Redis缓存热点数据

### 🔒 安全特性
- **JWT认证**: 无状态安全认证
- **内容审核**: 多维度内容安全检测
- **权限控制**: 细粒度权限管理
- **数据加密**: 敏感数据加密存储

### 📈 可扩展性
- **微服务架构**: 服务独立部署
- **水平扩展**: 支持负载均衡
- **数据库分片**: 支持大数据量
- **CDN集成**: 全球内容分发

## 部署说明

### 环境要求
- Java 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- RabbitMQ 3.8+
- Nacos 2.0+
- FFmpeg 4.0+

### 启动顺序
1. **基础设施**: MySQL, Redis, RabbitMQ, Nacos
2. **公共模块**: common-module
3. **网关服务**: gateway
4. **核心服务**: user-service, image-service
5. **视频服务**: video-upload-service, video-processing-service, video-play-service
6. **业务服务**: 其他微服务

### 配置说明
- **服务发现**: Nacos配置中心
- **数据库**: 各服务独立数据库
- **消息队列**: RabbitMQ交换机配置
- **文件存储**: 本地文件系统或OSS

## API文档

### 网关路由配置
```
/user/** → user-service
/image/** → image-service
/video/upload/** → video-upload-service
/video/play/** → video-play-service
/video/processing/** → video-processing-service
/audit/** → audit-service
```

## 许可证

MIT License

