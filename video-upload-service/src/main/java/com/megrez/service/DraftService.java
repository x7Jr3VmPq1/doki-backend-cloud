package com.megrez.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.megrez.mysql_entity.VideoDraft;
import com.megrez.mapper.DraftMapper;
import com.megrez.rabbit.exchange.VideoSubmitExchange;
import com.megrez.result.Response;
import com.megrez.result.Result;
import com.megrez.utils.FileUtils;
import com.megrez.utils.JSONUtils;
import com.megrez.utils.RabbitMQUtils;
import com.megrez.vo.videoupload_service.VideoDraftVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Service
public class DraftService {
    private final DraftMapper draftMapper;
    private final RabbitMQUtils rabbitMQUtils;

    public DraftService(DraftMapper draftMapper, RabbitMQUtils rabbitMQUtils, ObjectMapper objectMapper) {
        this.draftMapper = draftMapper;
        this.rabbitMQUtils = rabbitMQUtils;
    }

    public Result<VideoDraftVO> get(Integer userId) {
        // 查询原草稿
        VideoDraft byUploaderId = draftMapper.getByUploaderId(userId);
        if (byUploaderId == null) {
            return Result.success(null);
        }
        // 创建用户视图
        VideoDraftVO videoDraftVO = new VideoDraftVO();
        // 拷贝需要的值
        BeanUtils.copyProperties(byUploaderId, videoDraftVO);
        videoDraftVO.setId(byUploaderId.getId());
        // 返回
        return Result.success(videoDraftVO);
    }

    public Result<VideoDraftVO> createDraft(Integer userId) {
        // 先查询是否已经存在一个草稿
        VideoDraft byUploaderId = draftMapper.getByUploaderId(userId);
        if (byUploaderId != null) {
            // 存在草稿，则把这个草稿返回
            VideoDraftVO draftVO = VideoDraftVO.builder().build();
            BeanUtils.copyProperties(byUploaderId, draftVO);
            return Result.success(draftVO);
        }

        // 没有已存在的草稿，创建新草稿
        // 构建初始草稿
        VideoDraft videoDraft = VideoDraft.builder().
                uploaderId(userId). // 上传用户
                        updatedTime(System.currentTimeMillis()). // 当前时间
                        build();
        // 保存
        draftMapper.insert(videoDraft);
        // 构建返回视图
        VideoDraftVO draftVO = VideoDraftVO.builder().id(videoDraft.getId()).build();
        // 返回
        return Result.success(draftVO);
    }

    public Result<VideoDraftVO> updateDraft(Integer userId, VideoDraftVO updateDraft) {
        // 先查询草稿是否存在
        VideoDraft draft = draftMapper.selectById(updateDraft.getId());
        // 判断权限
        if (checkDraft(userId, draft)) {
            // 拷贝更新后的数据
            BeanUtils.copyProperties(updateDraft, draft);
            // 保存记录
            int row = draftMapper.updateById(draft);
            if (row > 0) {
                return Result.success(null);
            }
        }
        // 没有修改任何数据，返回错误
        return Result.error(Response.VIDEO_UPLOAD_UPDATE_DRAFT_WRONG);
    }

    public Result<Void> delete(Integer userId, Integer draftId) {
        // 先查询草稿是否存在
        VideoDraft draft = draftMapper.selectById(draftId);
        // 判断权限
        if (checkDraft(userId, draft)) {
            // 执行删除草稿和上传的视频
            int deleted = draftMapper.deleteById(draftId);
            if (draft.getFilename() != null) {
                FileUtils.deleteVideo(draft.getFilename());
            }
            if (deleted > 0) {
                return Result.success(null);
            }
        }
        return Result.error(Response.VIDEO_UPLOAD_UPDATE_DRAFT_WRONG);
    }

    /**
     * 提交发布业务实现
     *
     * @param userId  用户ID
     * @param draftVO 草稿参数
     * @return 操作结果
     */
    public Result<Void> submit(Integer userId, VideoDraftVO draftVO) {
        // 先查询草稿是否存在
        VideoDraft draft = draftMapper.selectById(draftVO.getId());
        // 判断权限
        if (checkDraft(userId, draft)) {
            // 视频未上传完毕，禁止发布
            if (draft.getSourceUploaded() == 0) {
                return Result.error(Response.VIDEO_NOT_UPLOAD);
            }
            // 设置提交状态为：已提交
            draft.setSubmitted(1);
            BeanUtils.copyProperties(draftVO, draft);
            draftMapper.updateById(draft);
            // TODO 定时发布

            // 向审核队列发送审核消息
            rabbitMQUtils.sendMessage(
                    VideoSubmitExchange.DIRECT_EXCHANGE_VIDEO_SUBMIT,
                    VideoSubmitExchange.RK_DRAFT_AUDIT,
                    JSONUtils.toJSON(draft));
            // 返回成功
            return Result.success(null);
        }
        return Result.error(Response.FORBIDDEN);
    }

    /**
     * 校验方法，判断草稿的存在/用户的权限
     *
     * @return
     */
    public boolean checkDraft(Integer userId, VideoDraft draft) {
        // 任意一个为null，返回错误
        if (userId == null || draft == null) {
            return false;
        }
        // 判断用户ID和草稿持有者ID是否匹配
        return draft.getUploaderId().equals(userId);
    }
}
