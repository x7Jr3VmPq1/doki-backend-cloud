package com.megrez.service;

import com.megrez.config.RabbitConfig;
import com.megrez.entity.VideoDraft;
import com.megrez.utils.FFmpegUtils;
import com.megrez.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
public class ProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessingService.class);

    /**
     * 对提交的草稿视频进行转码和封面图/精灵图生成
     *
     * @param draft 草稿消息
     */
    @RabbitListener(queues = RabbitConfig.QUEUE_VIDEO_PROCESSING)
    public void VideoProcessing(String draft) {
        try {
            logger.info("开始处理视频草稿: {}", draft);
            
            // 解析草稿消息
            VideoDraft videoDraft = JSONUtils.fromJSON(draft, VideoDraft.class);
            String videoFilename = videoDraft.getFilename();
            
            if (videoFilename == null || videoFilename.isEmpty()) {
                logger.error("视频文件名不能为空");
                return;
            }
            
            logger.info("处理视频文件: {}", videoFilename);
            
            // 1. 视频转码处理
            logger.info("开始视频转码...");
            boolean transcodeSuccess = FFmpegUtils.transcodeVideo(videoFilename);
            if (transcodeSuccess) {
                logger.info("视频转码完成");
            } else {
                logger.error("视频转码失败");
                return; // 转码失败，停止后续处理
            }
            
            // 2. 生成视频缩略图
            logger.info("开始生成视频缩略图...");
            boolean thumbnailSuccess = FFmpegUtils.createThumbnail(videoFilename);
            if (thumbnailSuccess) {
                logger.info("视频缩略图生成成功");
            } else {
                logger.error("视频缩略图生成失败");
            }
            
            // 3. 生成精灵图（用于进度条预览）
            logger.info("开始生成精灵图...");
            boolean spriteSuccess = FFmpegUtils.createVideoSprite(videoFilename, 10);
            if (spriteSuccess) {
                logger.info("精灵图生成成功");
            } else {
                logger.error("精灵图生成失败");
            }
            
            // 4. 生成高质量精灵图（可选，用于详细预览）
            logger.info("开始生成高质量精灵图...");
            boolean highQualitySpriteSuccess = FFmpegUtils.createHighQualitySprite(videoFilename, 15, 120, 120);
            if (highQualitySpriteSuccess) {
                logger.info("高质量精灵图生成成功");
            } else {
                logger.warn("高质量精灵图生成失败，但不影响主要功能");
            }
            
            // 5. 获取视频元数据信息
            logger.info("获取视频元数据...");
            String videoMeta = FFmpegUtils.getVideoMeta(videoFilename);
            if (videoMeta != null) {
                logger.info("视频元数据获取成功");
                logger.debug("视频元数据: {}", videoMeta);
            }
            
            // 6. 获取视频时长
            double duration = FFmpegUtils.getVideoDuration(videoFilename);
            if (duration > 0) {
                logger.info("视频时长: {} 秒", duration);
            } else {
                logger.warn("无法获取视频时长");
            }
            
            // 7. 生成带时间戳信息的精灵图（用于前端精确定位）
            logger.info("生成带时间戳信息的精灵图...");
            FFmpegUtils.SpriteInfo spriteInfo = FFmpegUtils.createVideoSpriteWithInfo(videoFilename, 10);
            if (spriteInfo != null) {
                logger.info("带时间戳的精灵图生成成功");
                logger.info("精灵图路径: {}", spriteInfo.getSpritePath());
                logger.info("帧数: {}, 尺寸: {}x{}", 
                    spriteInfo.getFrameCount(), 
                    spriteInfo.getFrameWidth(), 
                    spriteInfo.getFrameHeight());
            }
            
            logger.info("视频处理完成: {}", videoFilename);
            
        } catch (Exception e) {
            logger.error("视频处理失败: {}", e.getMessage(), e);
        }
    }
}
