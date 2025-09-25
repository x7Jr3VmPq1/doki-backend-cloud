package com.megrez.utils;


import com.megrez.path.FilesServerPath;

import java.io.IOException;
import java.util.List;

public class FFmpegUtils {

    public static boolean createThumbnail(String videoFilename) {
        // 输入视频路径
        String inputVideo = FilesServerPath.VIDEO_PATH + videoFilename + "\\video.mp4";

        // 截图输出路径
        String outputImage = FilesServerPath.VIDEO_PATH + videoFilename + "\\thumbnail.jpg";

        // 截图时间点
        String timestamp = "00:00:05";

        // FFmpeg 命令
        String[] command = new String[]{
                "ffmpeg",
                "-ss", timestamp,
                "-i", inputVideo,
                "-frames:v", "1",
                "-q:v", "2",
                outputImage
        };

        // 执行命令并返回执行结果
        return CommandExecutor.executeCommand(List.of(command)) != null;
    }

    /**
     * 获取视频时长
     *
     * @param videoFilename 视频路径
     * @return 视频时长（秒）
     */
    public static double getVideoDuration(String videoFilename) {
        // 输入视频路径
        String inputVideo = FilesServerPath.VIDEO_PATH + videoFilename + "\\video.mp4";
        // 构建命令
        String[] command = new String[]{
                "ffprobe",
                "-i", inputVideo,
                "-show_entries", "format=duration",
                "-v", "quiet",
                "-of", "csv=p=0"
        };
        String result = CommandExecutor.executeCommand(List.of(command));
        // 如果解析失败，返回时长为-1
        return result == null ? -1 : Double.parseDouble(result);
    }

    /**
     * 进行视频转码，统一转码为mp4格式
     * 支持各种输入格式，输出为标准mp4格式
     *
     * @param videoFilename 视频文件名（不包含扩展名）
     * @return 是否转码成功
     */
    public static boolean transcodeVideo(String videoFilename) {
        // 首先检测原始视频文件
        String originalVideoPath = findOriginalVideoFile(videoFilename);
        if (originalVideoPath == null) {
            System.err.println("未找到原始视频文件: " + videoFilename);
            return false;
        }
        
        // 输出文件路径
        String outputPath = FilesServerPath.VIDEO_PATH + videoFilename + "\\video.mp4";
        
        // 构建FFmpeg转码命令
        String[] command = new String[]{
                "ffmpeg",
                "-i", originalVideoPath,
                "-c:v", "libx264",           // 视频编码器
                "-c:a", "aac",               // 音频编码器
                "-preset", "medium",         // 编码速度预设
                "-crf", "23",                // 质量参数（18-28，越小质量越好）
                "-maxrate", "2M",            // 最大码率
                "-bufsize", "4M",            // 缓冲区大小
                "-vf", "scale=1920:1080:force_original_aspect_ratio=decrease,pad=1920:1080:(ow-iw)/2:(oh-ih)/2", // 视频缩放和填充
                "-movflags", "faststart",    // 优化网络播放
                "-y",                        // 覆盖输出文件
                outputPath
        };
        
        try {
            Process process = Runtime.getRuntime().exec(command);
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * 查找原始视频文件
     * 支持多种视频格式：mp4, avi, mov, mkv, webm, flv, wmv等
     *
     * @param videoFilename 视频文件名（不包含扩展名）
     * @return 原始视频文件完整路径，未找到返回null
     */
    private static String findOriginalVideoFile(String videoFilename) {
        String[] videoExtensions = {".mp4", ".avi", ".mov", ".mkv", ".webm", ".flv", ".wmv", ".m4v", ".3gp"};
        String basePath = FilesServerPath.VIDEO_PATH + videoFilename + "\\";
        
        for (String ext : videoExtensions) {
            String fullPath = basePath + "video" + ext;
            if (new java.io.File(fullPath).exists()) {
                return fullPath;
            }
        }
        
        return null;
    }

    public static String getVideoMeta(String videoFilename) {
        // 查找原始视频文件
        String originalVideoPath = findOriginalVideoFile(videoFilename);
        if (originalVideoPath == null) {
            // 如果找不到原始文件，尝试使用转码后的mp4文件
            originalVideoPath = FilesServerPath.VIDEO_PATH + videoFilename + "\\video.mp4";
        }
        
        List<String> command = List.of("ffmpeg", "-i", originalVideoPath);
        return CommandExecutor.executeCommand(command);
    }

    /**
     * 生成视频精灵图，用于前端进度条预览
     * 精灵图是将视频按时间间隔截取多帧，然后拼接成一张图片
     * 会根据视频宽高比动态调整精灵图尺寸
     *
     * @param videoFilename 视频文件名
     * @param spriteCount 精灵图帧数，建议10-20帧
     * @return 是否生成成功
     */
    public static boolean createVideoSprite(String videoFilename, int spriteCount) {
        // 输入视频路径
        String inputVideo = FilesServerPath.VIDEO_PATH + videoFilename + "\\video.mp4";
        
        // 输出精灵图路径
        String outputSprite = FilesServerPath.VIDEO_PATH + videoFilename + "\\sprite.jpg";
        
        // 获取视频时长
        double duration = getVideoDuration(videoFilename);
        if (duration <= 0) {
            return false;
        }
        
        // 获取视频尺寸信息
        VideoDimensions dimensions = getVideoDimensions(videoFilename);
        if (dimensions == null) {
            return false;
        }
        
        // 根据视频宽高比计算精灵图尺寸
        SpriteDimensions spriteDims = calculateSpriteDimensions(dimensions, spriteCount);
        
        // 计算每帧的时间间隔
        double interval = duration / spriteCount;
        
        // 构建FFmpeg命令生成精灵图
        String[] command = new String[]{
                "ffmpeg",
                "-i", inputVideo,
                "-vf", String.format("fps=1/%f,scale=%d:%d,tile=%dx1", interval, spriteDims.frameWidth, spriteDims.frameHeight, spriteCount),
                "-frames:v", String.valueOf(spriteCount),
                "-q:v", "2",
                "-y", // 覆盖输出文件
                outputSprite
        };
        
        // 执行命令并返回执行结果
        return CommandExecutor.executeCommand(List.of(command)) != null;
    }

    /**
     * 生成视频精灵图（简化版本，使用动态尺寸）
     * 默认生成10帧精灵图，根据视频宽高比自动调整尺寸
     *
     * @param videoFilename 视频文件名
     * @return 是否生成成功
     */
    public static boolean createVideoSprite(String videoFilename) {
        return createVideoSprite(videoFilename, 10);
    }

    /**
     * 生成高质量精灵图，用于详细预览
     * 生成更多帧数，更大的图片尺寸
     *
     * @param videoFilename 视频文件名
     * @param spriteCount 精灵图帧数
     * @param frameWidth 每帧宽度
     * @param frameHeight 每帧高度
     * @return 是否生成成功
     */
    public static boolean createHighQualitySprite(String videoFilename, int spriteCount, int frameWidth, int frameHeight) {
        // 输入视频路径
        String inputVideo = FilesServerPath.VIDEO_PATH + videoFilename + "\\video.mp4";
        
        // 输出精灵图路径
        String outputSprite = FilesServerPath.VIDEO_PATH + videoFilename + "\\sprite_hq.jpg";
        
        // 获取视频时长
        double duration = getVideoDuration(videoFilename);
        if (duration <= 0) {
            return false;
        }
        
        // 计算每帧的时间间隔
        double interval = duration / spriteCount;
        
        // 构建FFmpeg命令生成高质量精灵图
        String[] command = new String[]{
                "ffmpeg",
                "-i", inputVideo,
                "-vf", String.format("fps=1/%f,scale=%d:%d,tile=%dx1", interval, frameWidth, frameHeight, spriteCount),
                "-frames:v", String.valueOf(spriteCount),
                "-q:v", "1", // 更高质量
                "-y", // 覆盖输出文件
                outputSprite
        };
        
        // 执行命令并返回执行结果
        return CommandExecutor.executeCommand(List.of(command)) != null;
    }

    /**
     * 生成视频精灵图并返回精灵图信息
     * 包含每帧对应的时间戳信息，用于前端精确定位
     * 会根据视频宽高比动态调整精灵图尺寸
     *
     * @param videoFilename 视频文件名
     * @param spriteCount 精灵图帧数
     * @return 精灵图信息对象，包含文件路径和时间戳数组
     */
    public static SpriteInfo createVideoSpriteWithInfo(String videoFilename, int spriteCount) {
        // 输入视频路径
        String inputVideo = FilesServerPath.VIDEO_PATH + videoFilename + "\\video.mp4";
        
        // 输出精灵图路径
        String outputSprite = FilesServerPath.VIDEO_PATH + videoFilename + "\\sprite.jpg";
        
        // 获取视频时长
        double duration = getVideoDuration(videoFilename);
        if (duration <= 0) {
            return null;
        }
        
        // 获取视频尺寸信息
        VideoDimensions dimensions = getVideoDimensions(videoFilename);
        if (dimensions == null) {
            return null;
        }
        
        // 根据视频宽高比计算精灵图尺寸
        SpriteDimensions spriteDims = calculateSpriteDimensions(dimensions, spriteCount);
        
        // 计算每帧的时间间隔和时间戳
        double interval = duration / spriteCount;
        double[] timestamps = new double[spriteCount];
        for (int i = 0; i < spriteCount; i++) {
            timestamps[i] = i * interval;
        }
        
        // 构建FFmpeg命令生成精灵图
        String[] command = new String[]{
                "ffmpeg",
                "-i", inputVideo,
                "-vf", String.format("fps=1/%f,scale=%d:%d,tile=%dx1", interval, spriteDims.frameWidth, spriteDims.frameHeight, spriteCount),
                "-frames:v", String.valueOf(spriteCount),
                "-q:v", "2",
                "-y", // 覆盖输出文件
                outputSprite
        };
        
        // 执行命令
        boolean success = CommandExecutor.executeCommand(List.of(command)) != null;
        
        if (success) {
            return new SpriteInfo(outputSprite, timestamps, spriteCount, spriteDims.frameWidth, spriteDims.frameHeight);
        }
        
        return null;
    }

    /**
     * 获取视频尺寸信息
     *
     * @param videoFilename 视频文件名
     * @return 视频尺寸信息，失败返回null
     */
    public static VideoDimensions getVideoDimensions(String videoFilename) {
        String videoPath = FilesServerPath.VIDEO_PATH + videoFilename + "\\video.mp4";
        
        // 使用ffprobe获取视频尺寸
        String[] command = new String[]{
                "ffprobe",
                "-v", "quiet",
                "-print_format", "json",
                "-show_streams",
                "-select_streams", "v:0",
                videoPath
        };
        
        String result = CommandExecutor.executeCommand(List.of(command));
        if (result == null || result.isEmpty()) {
            return null;
        }
        
        try {
            // 简单的JSON解析，提取width和height
            // 这里使用简单的字符串匹配，实际项目中建议使用JSON库
            int width = extractIntValue(result, "\"width\":");
            int height = extractIntValue(result, "\"height\":");
            
            if (width > 0 && height > 0) {
                return new VideoDimensions(width, height);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * 从JSON字符串中提取整数值
     */
    private static int extractIntValue(String json, String key) {
        try {
            int startIndex = json.indexOf(key);
            if (startIndex == -1) return -1;
            
            startIndex += key.length();
            int endIndex = json.indexOf(",", startIndex);
            if (endIndex == -1) {
                endIndex = json.indexOf("}", startIndex);
            }
            
            if (endIndex == -1) return -1;
            
            String valueStr = json.substring(startIndex, endIndex).trim();
            return Integer.parseInt(valueStr);
        } catch (Exception e) {
            return -1;
        }
    }
    
    /**
     * 根据视频尺寸计算精灵图尺寸
     * 保持视频宽高比，同时控制文件大小
     *
     * @param dimensions 视频尺寸
     * @param spriteCount 精灵图帧数
     * @return 精灵图尺寸
     */
    public static SpriteDimensions calculateSpriteDimensions(VideoDimensions dimensions, int spriteCount) {
        int originalWidth = dimensions.width;
        int originalHeight = dimensions.height;
        
        // 计算宽高比
        double aspectRatio = (double) originalWidth / originalHeight;
        
        // 目标尺寸策略：
        // 1. 横屏视频：高度固定为90，宽度按比例计算
        // 2. 竖屏视频：宽度固定为90，高度按比例计算
        // 3. 正方形视频：90x90
        
        int frameWidth, frameHeight;
        
        if (aspectRatio > 1.2) {
            // 横屏视频 (16:9, 4:3等)
            frameHeight = 90;
            frameWidth = (int) (90 * aspectRatio);
        } else if (aspectRatio < 0.8) {
            // 竖屏视频 (9:16, 3:4等)
            frameWidth = 90;
            frameHeight = (int) (90 / aspectRatio);
        } else {
            // 接近正方形的视频
            frameWidth = 90;
            frameHeight = 90;
        }
        
        // 限制最大尺寸，避免文件过大
        int maxWidth = 200;
        int maxHeight = 200;
        
        if (frameWidth > maxWidth) {
            frameHeight = (int) (frameHeight * maxWidth / frameWidth);
            frameWidth = maxWidth;
        }
        
        if (frameHeight > maxHeight) {
            frameWidth = (int) (frameWidth * maxHeight / frameHeight);
            frameHeight = maxHeight;
        }
        
        return new SpriteDimensions(frameWidth, frameHeight);
    }
    
    /**
     * 视频尺寸信息类
     */
    public static class VideoDimensions {
        private final int width;
        private final int height;
        
        public VideoDimensions(int width, int height) {
            this.width = width;
            this.height = height;
        }
        
        public int getWidth() {
            return width;
        }
        
        public int getHeight() {
            return height;
        }
        
        public double getAspectRatio() {
            return (double) width / height;
        }
    }
    
    /**
     * 精灵图尺寸信息类
     */
    public static class SpriteDimensions {
        private final int frameWidth;
        private final int frameHeight;
        
        public SpriteDimensions(int frameWidth, int frameHeight) {
            this.frameWidth = frameWidth;
            this.frameHeight = frameHeight;
        }
        
        public int getFrameWidth() {
            return frameWidth;
        }
        
        public int getFrameHeight() {
            return frameHeight;
        }
    }

    /**
     * 精灵图信息类
     */
    public static class SpriteInfo {
        private final String spritePath;
        private final double[] timestamps;
        private final int frameCount;
        private final int frameWidth;
        private final int frameHeight;

        public SpriteInfo(String spritePath, double[] timestamps, int frameCount, int frameWidth, int frameHeight) {
            this.spritePath = spritePath;
            this.timestamps = timestamps;
            this.frameCount = frameCount;
            this.frameWidth = frameWidth;
            this.frameHeight = frameHeight;
        }

        public String getSpritePath() {
            return spritePath;
        }

        public double[] getTimestamps() {
            return timestamps;
        }

        public int getFrameCount() {
            return frameCount;
        }

        public int getFrameWidth() {
            return frameWidth;
        }

        public int getFrameHeight() {
            return frameHeight;
        }
    }
}
