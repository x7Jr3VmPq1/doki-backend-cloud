# RabbitMQ 交换机定义

这个包定义了Doki平台中各个业务场景的RabbitMQ交换机，用于异步消息处理。

## 交换机说明

### 评论相关
- **CommentAddExchange** - 评论添加交换机，处理新评论的通知和统计
- **CommentDeleteExchange** - 评论删除交换机，处理评论删除的统计和图片清理
- **CommentLikeExchange** - 评论点赞交换机，处理评论点赞的通知和统计

### 视频相关
- **VideoLikeExchange** - 视频点赞交换机，处理视频点赞的通知和统计
- **VideoSubmitExchange** - 视频提交交换机，处理视频审核、转码、发布的流程
- **VideoPublishedExchange** - 视频发布交换机，处理视频发布后的搜索索引、通知、统计

### 社交相关
- **SocialFollowExchange** - 社交关注交换机，处理用户关注关系的相关业务
