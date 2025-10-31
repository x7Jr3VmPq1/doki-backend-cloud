package com.megrez.service;

import com.megrez.mysql_entity.VideoDraft;
import com.megrez.mapper.DraftMapper;
import com.megrez.result.Response;
import com.megrez.result.Result;
import com.megrez.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class VideoUploadService {

    private static final Logger log = LoggerFactory.getLogger(VideoUploadService.class);
    private final DraftMapper draftMapper;

    public VideoUploadService(DraftMapper draftMapper) {
        this.draftMapper = draftMapper;
    }

    public Result<String> upload(MultipartFile video, Integer draftId) {
        if (video.isEmpty()) {
            return Result.error(Response.VIDEO_UPLOAD_EMPTY_VIDEO); // 文件为空，上传失败
        }

        // 尝试获取草稿
        VideoDraft selectById = draftMapper.selectById(draftId);
        if (selectById == null) {
            return Result.error(Response.VIDEO_DRAFT_NOT_FOUND);
        }

        try {
            // 先清除草稿上可能已经上传的视频
            FileUtils.deleteVideo(selectById.getFilename());
            // 尝试保存文件，并获取保存的文件名
            String savedName = FileUtils.saveVideo(video);
            // 创建更新草稿，更新文件名
            VideoDraft videoDraft = VideoDraft.builder()
                    .id(draftId)
                    .filename(savedName)
                    .sourceUploaded(1)
                    .uploadTime(System.currentTimeMillis())
                    .build();
            // 更新草稿
            draftMapper.updateById(videoDraft);
            return Result.success(null);

        } catch (IOException e) {
            // 抛出异常，打印日志
            log.error("保存视频文件异常：{}", e.getMessage());
        }
        return Result.error(Response.IMAGE_UPLOAD_SAVE_WRONG);
    }
}
