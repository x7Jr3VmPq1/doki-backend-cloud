package com.megrez.service;

import com.megrez.config.RabbitConfig;
import com.megrez.entity.Video;
import com.megrez.entity.VideoDraft;
import com.megrez.entity.VideoMetadata;
import com.megrez.entity.VideoStatistics;
import com.megrez.mapper.VideoMapper;
import com.megrez.mapper.VideoStatisticsMapper;
import com.megrez.utils.FFmpegUtils;
import com.megrez.utils.JSONUtils;
import com.megrez.utils.RabbitMQUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class ProcessingService {

    private static final Logger logger = LoggerFactory.getLogger(ProcessingService.class);

    private final VideoMapper videoMapper;
    private final VideoStatisticsMapper videoStatisticsMapper;
    private final RabbitMQUtils rabbitMQUtils;

    public ProcessingService(VideoMapper videoMapper, VideoStatisticsMapper videoStatisticsMapper, RabbitMQUtils rabbitMQUtils) {
        this.videoMapper = videoMapper;
        this.videoStatisticsMapper = videoStatisticsMapper;
        this.rabbitMQUtils = rabbitMQUtils;
    }

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


            // 生成视频缩略图
            logger.info("开始生成视频缩略图...");
            boolean thumbnailSuccess = FFmpegUtils.createThumbnail(videoFilename);
            if (thumbnailSuccess) {
                logger.info("视频缩略图生成成功");
            } else {
                logger.error("视频缩略图生成失败");
            }

            // 获取视频元数据信息
            logger.info("获取视频元数据...");
            VideoMetadata videoMeta = FFmpegUtils.getVideoMeta(videoFilename);
            if (videoMeta != null) {
                logger.info("视频元数据获取成功");
                logger.info("视频元数据: {}", videoMeta);

                // 创建Video对象，合并草稿数据和元数据
                Video video = createVideoFromDraftAndMetadata(videoDraft, videoMeta, videoFilename);
                logger.info("创建Video对象成功: {}", video);

                try {
                    // 发送发布视频消息
                    rabbitMQUtils.sendMessage(
                            RabbitConfig.DIRECT_EXCHANGE_VIDEO_SUBMIT,
                            RabbitConfig.RK_VIDEO_PUBLISH,
                            JSONUtils.toJSON(video)
                    );

                } catch (Exception e) {
                    logger.error("插入视频数据到数据库时发生异常: {}", e.getMessage(), e);
                }
            } else {
                logger.error("获取视频元数据失败");
            }

        } catch (Exception e) {
            logger.error("视频处理失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 根据草稿数据和视频元数据创建Video对象
     *
     * @param videoDraft    视频草稿对象
     * @param videoMeta     视频元数据对象
     * @param videoFilename 处理后的视频文件名
     * @return 创建的Video对象
     */
    private Video createVideoFromDraftAndMetadata(VideoDraft videoDraft, VideoMetadata videoMeta, String videoFilename) {
        long currentTime = Instant.now().toEpochMilli();

        return Video.builder()
                // 从草稿获取基本信息
                .uploaderId(videoDraft.getUploaderId())
                .title(videoDraft.getTitle())
                .description(videoDraft.getDescription())
                .tags(videoDraft.getTags())
                .permission(videoDraft.getPermission())
                .allowComment(1) // 默认允许评论

                // 视频文件信息
                .videoFilename(videoFilename)
                .videoFormat(".mp4")

                // 从元数据获取技术参数
                .videoWidth(videoMeta.getWidth())
                .videoHeight(videoMeta.getHeight())
                .videoDuration(videoMeta.getDuration() != null ? videoMeta.getDuration().intValue() : null)
                .videoBitrate(videoMeta.getBitRate() != null ? videoMeta.getBitRate().intValue() / 1000 : null) // 转换为kbps
                .videoSize(videoMeta.getFileSize()) // 设置文件大小

                // 时间戳设置
                .publishTime(currentTime)
                .createdTime(currentTime)
                .updatedTime(currentTime)

                // 默认值
                .deleted(0)
                .build();
    }

    /**
     * 创建视频统计记录
     *
     * @param videoId 视频ID
     */
    private void createVideoStatistics(Integer videoId) {
        try {
            long currentTime = Instant.now().toEpochMilli();

            VideoStatistics statistics = VideoStatistics.builder()
                    .videoId(videoId)
                    .viewCount(0L)
                    .likeCount(0L)
                    .dislikeCount(0L)
                    .commentCount(0L)
                    .shareCount(0L)
                    .favoriteCount(0L)
                    .downloadCount(0L)
                    .createdTime(currentTime)
                    .updatedTime(currentTime)
                    .deleted(0)
                    .build();

            int result = videoStatisticsMapper.insert(statistics);
            if (result > 0) {
                logger.info("视频统计记录创建成功，视频ID: {}", videoId);
            } else {
                logger.error("视频统计记录创建失败，视频ID: {}", videoId);
            }
        } catch (Exception e) {
            logger.error("创建视频统计记录时发生异常，视频ID: {}, 错误: {}", videoId, e.getMessage(), e);
        }
    }
}
