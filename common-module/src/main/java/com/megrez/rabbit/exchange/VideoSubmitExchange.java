package com.megrez.rabbit.exchange;

public class VideoSubmitExchange {
    // 草稿交换机
    public static final String DIRECT_EXCHANGE_VIDEO_SUBMIT = "video.submit.exchange";

    // 审核队列
    public static final String QUEUE_DRAFT_AUDIT = "draft.audit.queue";
    public static final String RK_DRAFT_AUDIT = "draft.audit.key";

    // 转码队列
    public static final String QUEUE_VIDEO_PROCESSING = "video.processing.queue";
    public static final String RK_VIDEO_PROCESSING = "video.processing.key";

    // 发布视频队列
    public static final String QUEUE_VIDEO_PUBLISH = "video.publish.queue";
    public static final String RK_VIDEO_PUBLISH = "video.publish.key";

}
