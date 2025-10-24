create table comment_like
(
    id         bigint auto_increment
        primary key,
    comment_id bigint               not null,
    user_id    bigint               not null,
    is_like    tinyint(1)           not null,
    created_at bigint     default 0 null,
    is_deleted tinyint(1) default 0 not null,
    constraint uk_user_comment
        unique (user_id, comment_id)
);

create index idx_user_id
    on comment_like (user_id);

create table user
(
    id            bigint auto_increment comment '用户ID'
        primary key,
    phone_number  varchar(20)  not null comment '电话号码',
    password_hash varchar(255) null comment '密码哈希值',
    username      varchar(50)  not null comment '用户名',
    avatar_url    varchar(255) null comment '头像URL',
    bio           text         null comment '个人简介',
    created_at    bigint       not null comment '注册时间（毫秒时间戳）',
    updated_at    bigint       not null comment '更新时间（毫秒时间戳）',
    constraint username
        unique (username)
)
    comment '用户表';

create table user_statistics
(
    id              bigint unsigned auto_increment comment '主键ID'
        primary key,
    user_id         bigint unsigned          not null comment '用户ID',
    following_count int unsigned default '0' null comment '关注数量',
    follower_count  int unsigned default '0' null comment '粉丝数量',
    created_at      bigint                   null,
    updated_at      bigint                   null,
    like_count      int unsigned default '0' null comment '收到点赞数量'
)
    comment '用户关注与粉丝统计表';

create index idx_user_id
    on user_statistics (user_id);

create table video
(
    id             bigint auto_increment comment '视频ID，主键'
        primary key,
    uploader_id    bigint               not null comment '上传用户ID，关联user表',
    title          varchar(255)         not null comment '视频标题',
    description    text                 null comment '视频描述',
    tags           varchar(500)         null comment '视频标签，多个标签用逗号分隔',
    category_id    int                  null comment '视频分类ID，关联category表',
    video_filename varchar(255)         not null comment '处理后视频文件名',
    video_size     bigint               null comment '视频文件大小(字节)',
    video_duration int                  null comment '视频时长(秒)',
    video_format   varchar(20)          null comment '视频格式(MP4, AVI等)',
    video_bitrate  int                  null comment '视频码率(kbps)',
    publish_time   bigint               null comment '发布时间戳',
    permission     tinyint    default 1 not null comment '可见性: 0-仅自己, 1-公开, 2-关注者可见, 3-好友可见',
    allow_comment  tinyint(1) default 1 not null comment '是否允许评论: 0-不允许, 1-允许',
    created_time   bigint               not null comment '创建时间戳',
    updated_time   bigint               not null comment '更新时间戳',
    deleted        tinyint(1) default 0 not null comment '是否删除',
    video_width    int                  null comment '视频宽度(像素)',
    video_height   int                  null comment '视频高度(像素)'
)
    comment '视频表' collate = utf8mb4_unicode_ci;

create table video_comments
(
    id                bigint auto_increment
        primary key,
    video_id          bigint               not null,
    user_id           bigint               not null,
    content           text                 not null,
    parent_comment_id bigint               null,
    is_root           tinyint(1) default 1 not null,
    child_count       int        default 0 null,
    created_at        bigint     default 0 null,
    updated_at        bigint     default 0 null,
    like_count        int        default 0 null,
    dislike_count     int        default 0 null,
    img_url           varchar(255)         null,
    is_deleted        tinyint(1) default 0 not null
);

create index idx_parent_comment_id
    on video_comments (parent_comment_id);

create index idx_video_id
    on video_comments (video_id);

create table video_draft
(
    id                 bigint unsigned auto_increment comment 'ID'
        primary key,
    uploader_id        bigint unsigned      not null comment '上传者ID',
    filename           varchar(255)         null,
    title              varchar(255)         null,
    description        text                 null comment '描述',
    tags               text                 null comment '标签(JSON数组字符串)',
    cover_image        varchar(255)         null comment '封面图名称',
    scheduled_time     bigint unsigned      null comment '计划发布时间(秒级时间戳)',
    upload_time        varchar(255)         null,
    source_uploaded    tinyint(1) default 0 null comment '视频源文件是否上传完毕 0=否 1=是',
    permission         tinyint(1) default 0 null comment '权限 0=公开 1=仅限好友 2=私密',
    submitted          tinyint(1) default 0 null comment '用户是否提交了发布 0=否 1=是',
    review_status      tinyint(1) default 0 null comment '是否审核完毕 0=未开始 1=审核中 2=审核完毕',
    review_passed      tinyint(1) default 0 null comment '审核是否通过 0=否 1=是',
    review_reason      text                 null comment '不通过原因',
    transcoding_status tinyint(1) default 0 null comment '是否转码完毕 0=否 1=是',
    published          tinyint(1) default 0 null comment '是否发布 0=否 1=是',
    updated_time       bigint unsigned      not null comment '更新时间(秒级时间戳)',
    deleted            tinyint(1) default 0 null comment '是否删除 0=否 1=是',
    is_scheduled       tinyint(1) default 0 null comment '是否计划发布 0=否 1=是'
)
    comment '草稿表';

create index idx_review_status
    on video_draft (review_status);

create index idx_scheduled_time
    on video_draft (scheduled_time);

create index idx_transcoding_status
    on video_draft (transcoding_status);

create index idx_upload_time
    on video_draft (upload_time);

create index idx_uploader_id
    on video_draft (uploader_id);

create table video_likes
(
    id         bigint unsigned auto_increment comment '主键'
        primary key,
    user_id    bigint unsigned not null comment '用户ID',
    video_id   bigint unsigned not null comment '视频ID',
    created_at bigint          null comment '创建时间'
)
    comment '视频点赞记录表';

create index idx_user_id
    on video_likes (user_id);

create index idx_video_id
    on video_likes (video_id);

create table video_statistics
(
    id             int auto_increment comment '统计ID，主键'
        primary key,
    video_id       int                  not null comment '视频ID，关联video表',
    view_count     bigint     default 0 null comment '播放次数',
    like_count     bigint     default 0 null comment '点赞数',
    dislike_count  bigint     default 0 null comment '点踩数',
    comment_count  bigint     default 0 null comment '评论数',
    share_count    bigint     default 0 null comment '分享数',
    favorite_count bigint     default 0 null comment '收藏数',
    download_count bigint     default 0 null comment '下载数',
    created_time   bigint               not null comment '创建时间戳',
    updated_time   bigint               not null comment '更新时间戳',
    deleted        tinyint(1) default 0 not null comment '是否删除',
    constraint uk_video_id
        unique (video_id)
)
    comment '视频统计信息表' collate = utf8mb4_unicode_ci;

create index idx_created_time
    on video_statistics (created_time);

create index idx_like_count
    on video_statistics (like_count);

create index idx_view_count
    on video_statistics (view_count);

-- 用户关注关系表
create table user_follow
(
    id           bigint unsigned auto_increment comment '主键ID'
        primary key,
    follower_id  bigint unsigned      not null comment '关注者ID，关联user表',
    following_id bigint unsigned      not null comment '被关注者ID，关联user表',
    created_at   bigint               not null comment '关注时间戳',
    updated_at   bigint               not null comment '更新时间戳',
    is_deleted   tinyint(1) default 0 not null comment '是否删除 0=否 1=是',
    constraint uk_follower_following
        unique (follower_id, following_id),
    constraint fk_follower_user
        foreign key (follower_id) references user (id),
    constraint fk_following_user
        foreign key (following_id) references user (id)
)
    comment '用户关注关系表' collate = utf8mb4_unicode_ci;
-- 创建索引优化查询性能
create index idx_follower_id
    on user_follow (follower_id);

create index idx_following_id
    on user_follow (following_id);

create index idx_created_at
    on user_follow (created_at);

