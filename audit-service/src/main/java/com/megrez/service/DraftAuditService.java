package com.megrez.service;

import com.megrez.entity.VideoDraft;
import com.megrez.mapper.DraftMapper;
import com.megrez.rabbit.exchange.VideoSubmitExchange;
import com.megrez.utils.JSONUtils;
import com.megrez.utils.RabbitMQUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class DraftAuditService {
    private final DraftMapper draftMapper;
    private final RabbitMQUtils rabbitMQUtils;

    public DraftAuditService(DraftMapper draftMapper, RabbitMQUtils rabbitMQUtils) {
        this.draftMapper = draftMapper;
        this.rabbitMQUtils = rabbitMQUtils;
    }

    @RabbitListener(queues = VideoSubmitExchange.QUEUE_DRAFT_AUDIT)
    public void DraftAudit(String draft) {
        // 解析消息中的草稿实体
        VideoDraft videoDraft = JSONUtils.fromJSON(draft, VideoDraft.class);
        // TODO 执行审核逻辑，暂时全部通过
        videoDraft.setReviewStatus(1);
        videoDraft.setReviewPassed(1);
        // 更新记录为：已通过
        draftMapper.updateById(videoDraft);
        // 发布审核已通过消息
        rabbitMQUtils.sendMessage(
                VideoSubmitExchange.DIRECT_EXCHANGE_VIDEO_SUBMIT,
                VideoSubmitExchange.RK_VIDEO_PROCESSING,
                JSONUtils.toJSON(videoDraft));
    }
}
