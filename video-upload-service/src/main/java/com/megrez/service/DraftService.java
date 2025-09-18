package com.megrez.service;

import com.megrez.entity.VideoDraft;
import com.megrez.mapper.DraftMapper;
import com.megrez.result.Response;
import com.megrez.result.Result;
import com.megrez.vo.VideoDraftVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DraftService {
    private final DraftMapper draftMapper;

    public DraftService(DraftMapper draftMapper) {
        this.draftMapper = draftMapper;
    }

    public Result get(Integer uploaderId) {
        // 查询原草稿
        VideoDraft byUploaderId = draftMapper.getByUploaderId(uploaderId);
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

    public Result createDraft(Integer userId) {
        // 先查询是否已经存在一个草稿
        VideoDraft byUploaderId = draftMapper.getByUploaderId(userId);
        if (byUploaderId != null) {
            // 存在草稿，返回错误
            return Result.error(Response.VIDEO_UPLOAD_REPEAT_DRAFT);
        }

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

    public Result updateDraft(Integer uploaderId, VideoDraftVO updateDraft) {
        // 先查询草稿是否存在
        VideoDraft draft = draftMapper.selectById(updateDraft.getId());
        if (draft == null) {
            // 没有查询到，返回更新失败错误
            return Result.error(Response.VIDEO_UPLOAD_UPDATE_DRAFT_WRONG);
        }
        if (!draft.getUploaderId().equals(uploaderId)) {
            // 权限不足
            return Result.error(Response.FORBIDDEN);
        }
        // 拷贝更新后的数据
        BeanUtils.copyProperties(updateDraft, draft);
        // 保存记录
        int row = draftMapper.updateById(draft);
        if (row > 0) {
            return Result.success(null);
        }
        // 没有修改任何数据，返回错误
        return Result.error(Response.VIDEO_UPLOAD_UPDATE_DRAFT_WRONG);
    }

    public Result delete(Integer userId, Integer draftId) {
        // 根据草稿ID查询草稿
        VideoDraft draft = draftMapper.selectById(draftId);
        // 没有查询到，返回失败
        if (draft == null) {
            return Result.error(Response.VIDEO_UPLOAD_UPDATE_DRAFT_WRONG);
        }
        // 没有权限
        if (!draft.getUploaderId().equals(userId)) {
            return Result.error(Response.FORBIDDEN);
        }
        // 执行删除
        int deleted = draftMapper.deleteById(draftId);
        if (deleted > 0) {
            return Result.success(null);
        }
        return Result.error(Response.UNKNOWN_WRONG);
    }
}
