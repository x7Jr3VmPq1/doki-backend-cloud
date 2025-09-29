package com.megrez.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.megrez.entity.VideoMetadata;
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

    /**
     * 根据指定视频名获取元数据
     *
     * @param videoFilename 视频文件名
     * @return 视频元数据对象，解析失败返回null
     */
    public static VideoMetadata getVideoMeta(String videoFilename) {
        // 查找原始视频文件
        String originalVideoPath = findOriginalVideoFile(videoFilename);
        if (originalVideoPath == null) {
            // 如果找不到原始文件，尝试使用转码后的mp4文件
            originalVideoPath = FilesServerPath.VIDEO_PATH + videoFilename + "\\video.mp4";
        }

        List<String> command = List.of(
                "ffprobe",
                "-v", "quiet",              // 静默输出
                "-print_format", "json",    // JSON 输出
                "-select_streams", "v:0",   // 只选第一个视频流
                "-show_entries",            // 只输出需要字段
                "stream=width,height,r_frame_rate:format=duration,bit_rate,size",
                originalVideoPath
        );
        
        String jsonResult = CommandExecutor.executeCommand(command);
        if (jsonResult == null || jsonResult.trim().isEmpty()) {
            return null;
        }
        
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(jsonResult);
            
            // 解析streams数组中的第一个视频流
            JsonNode streams = rootNode.get("streams");
            JsonNode format = rootNode.get("format");
            
            if (streams == null || !streams.isArray() || streams.size() == 0) {
                return null;
            }
            
            JsonNode videoStream = streams.get(0);
            
            // 提取视频流信息
            Integer width = videoStream.has("width") ? videoStream.get("width").asInt() : null;
            Integer height = videoStream.has("height") ? videoStream.get("height").asInt() : null;
            String frameRate = videoStream.has("r_frame_rate") ? videoStream.get("r_frame_rate").asText() : null;
            
            // 提取格式信息
            Double duration = null;
            Long bitRate = null;
            Long fileSize = null;
            
            if (format != null) {
                if (format.has("duration")) {
                    try {
                        duration = format.get("duration").asDouble();
                    } catch (Exception e) {
                        // 如果解析失败，尝试作为字符串解析
                        String durationStr = format.get("duration").asText();
                        if (durationStr != null && !durationStr.isEmpty()) {
                            duration = Double.parseDouble(durationStr);
                        }
                    }
                }
                
                if (format.has("bit_rate")) {
                    try {
                        bitRate = format.get("bit_rate").asLong();
                    } catch (Exception e) {
                        // 如果解析失败，尝试作为字符串解析
                        String bitRateStr = format.get("bit_rate").asText();
                        if (bitRateStr != null && !bitRateStr.isEmpty()) {
                            bitRate = Long.parseLong(bitRateStr);
                        }
                    }
                }
                
                if (format.has("size")) {
                    try {
                        fileSize = format.get("size").asLong();
                    } catch (Exception e) {
                        // 如果解析失败，尝试作为字符串解析
                        String sizeStr = format.get("size").asText();
                        if (sizeStr != null && !sizeStr.isEmpty()) {
                            fileSize = Long.parseLong(sizeStr);
                        }
                    }
                }
            }
            
            // 如果FFprobe没有返回文件大小，尝试直接从文件系统获取
            if (fileSize == null) {
                try {
                    java.io.File file = new java.io.File(originalVideoPath);
                    if (file.exists()) {
                        fileSize = file.length();
                    }
                } catch (Exception e) {
                    System.err.println("无法获取文件大小: " + e.getMessage());
                }
            }
            
            return VideoMetadata.builder()
                    .width(width)
                    .height(height)
                    .frameRate(frameRate)
                    .duration(duration)
                    .bitRate(bitRate)
                    .fileSize(fileSize)
                    .build();
                    
        } catch (Exception e) {
            System.err.println("解析视频元数据失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
