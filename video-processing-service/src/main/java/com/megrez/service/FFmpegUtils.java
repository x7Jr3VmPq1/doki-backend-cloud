package com.megrez.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.megrez.mysql_entity.VideoMetadata;
import com.megrez.path.FilesServerPath;
import com.megrez.utils.CommandExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FFmpegUtils {

    private static final Logger log = LoggerFactory.getLogger(FFmpegUtils.class);

    public static boolean createThumbnail(String videoFilename) {
        // 查找视频原始路径
        String originalPath = FFmpegUtils.findOriginalVideoFile(videoFilename);

        if (originalPath == null) {
            throw new RuntimeException();
        }
        // 截图输出路径
        String outputImage = FilesServerPath.COVER_PATH + videoFilename + ".jpg";

        // 截图时间点
        String timestamp = "00:00:05";

        // FFmpeg 命令
        String[] command = new String[]{
                "ffmpeg",
                "-ss", timestamp,
                "-i", originalPath,
                "-frames:v", "1",
                "-q:v", "2",
                outputImage
        };

        // 执行命令
        return CommandExecutor.executeCommand(List.of(command)) != null;
    }

    public static boolean createSprite(String videoFilename, String scale, String fps, String tile) {

        // 查找视频原始路径
        String originalPath = FFmpegUtils.findOriginalVideoFile(videoFilename);

        if (originalPath == null) {
            throw new RuntimeException();
        }

        // 截图输出路径
        String outputPath = String.format("%s%s%s", FilesServerPath.SPRITE_PATH, videoFilename, ".jpg");

        // 构建 -vf 选项
        String vfFilter = String.format("\"fps=%s,scale=%s,tile=%s\"",
                fps, scale, tile);
        // 构建完整的命令数组
        String[] command = new String[]{
                "ffmpeg",
                "-i",
                originalPath,
                "-vf",
                vfFilter,
                "-frames:v",
                "1",
                outputPath
        };

        log.info("执行命令：{}", (Object) command);
        return CommandExecutor.executeCommand(List.of(command)) != null;
    }

    /**
     * 进行视频转码，统一转码为HLS
     * 支持各种输入格式
     *
     * @param videoFilename 视频文件名（不包含扩展名）
     * @return 可供选择的分辨率
     */
    public static List<Integer> transcodeVideo(String videoFilename) throws IOException {
        // 首先检测原始视频文件
        String originalVideoPath = findOriginalVideoFile(videoFilename);
        if (originalVideoPath == null) {
            System.err.println("未找到原始视频文件: " + videoFilename);
            throw new IOException();
        }
        // 读取视频元数据
        VideoMetadata videoMeta = getVideoMeta(videoFilename);

        if (videoMeta == null) {
            log.error("读取元数据失败");
            throw new RuntimeException();
        }

        Integer height = videoMeta.getHeight();
        Integer width = videoMeta.getWidth();

        boolean isWidth = width > height;

        List<Integer> targetResolutions = ResolutionTemplate.getTargetResolutions(width, height);

        StringBuilder masterContent = new StringBuilder();
        masterContent.append("#EXTM3U\n");
        masterContent.append("#EXT-X-VERSION:3\n"); // 确保版本兼容性

        // 获取视频根目录路径
        Path videoRootPath = Paths.get(FilesServerPath.VIDEO_PATH, videoFilename);
        Path masterM3u8Path = videoRootPath.resolve("master.m3u8");

        for (Integer resolution : targetResolutions) {

            long estimatedBandwidth = estimateBandwidth(resolution);

            String relativePath = resolution + "/index.m3u8";

            String streamInf = String.format(
                    "#EXT-X-STREAM-INF:BANDWIDTH=%d,RESOLUTION=%dx%d,CODECS=\"avc1.42c01e,mp4a.40.2\"\n",
                    estimatedBandwidth,
                    (isWidth ? (int) Math.round((double) resolution * width / height) : resolution), // 宽度 (估算)
                    (isWidth ? resolution : (int) Math.round((double) resolution * height / width))  // 高度 (估算)
            );

            masterContent.append(streamInf);
            masterContent.append(relativePath).append("\n");

            String resolutionParam = isWidth ? "scale=-2:" + resolution : "scale=" + resolution + ":-2";

            Path outputDir = Paths.get(
                    FilesServerPath.VIDEO_PATH,
                    videoFilename,
                    String.valueOf(resolution) // 当前分辨率
            );
            // 创建目录及其所有父目录
            try {
                Files.createDirectories(outputDir);
            } catch (IOException e) {
                // 如果目录创建失败，则无法进行转码，打印错误并跳过或返回 false
                System.err.println("创建输出目录失败: " + outputDir);
                e.printStackTrace();
                // 选择是跳过当前分辨率 (continue) 还是直接退出转码 (return false)
                throw new RuntimeException();
            }

            String outputPath = outputDir.resolve("index.m3u8").toString();

            // 构建转码命令
            String[] command = new String[]{
                    "ffmpeg",
                    "-i", originalVideoPath,
                    "-map", "0:v",
                    "-map", "0:a",
                    "-vf", resolutionParam, // 目标分辨率
                    "-c:v", "libx264", // 视频编码器
                    "-crf", "23",   // 质量因子
                    "-c:a", "aac", // 音频编码器
                    "-f", "hls",  // 输出为HLS
                    "-hls_time", "6",   // 每个分片的期望时长 (秒)
                    "-hls_list_size", "0",// 列表大小 0 表示包含所有分片
                    outputPath  // 输出列表路径
            };
            try {
                ProcessBuilder pb = new ProcessBuilder(command);

                pb.inheritIO();

                Process process = pb.start();

                int exitCode = process.waitFor();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                throw new IOException();
            }
        }

        // 写入 Master M3U8 文件
        Files.writeString(masterM3u8Path, masterContent.toString());
        log.info("成功创建 Master M3U8 文件: {}", masterM3u8Path);
        // 返回可供选择的分辨率
        return targetResolutions;
    }

    // ... (辅助函数，用于估算码率)
    private static long estimateBandwidth(int resolution) {
        if (resolution <= 360) return 400_000;      // 400 kbps
        if (resolution <= 480) return 800_000;      // 800 kbps
        if (resolution <= 720) return 1_500_000;    // 1.5 Mbps
        if (resolution <= 1080) return 3_000_000;   // 3.0 Mbps
        return 5_000_000;                           // 5.0 Mbps
    }

    /**
     * 查找原始视频文件
     * 支持多种视频格式：mp4, avi, mov, mkv, webm, flv, wmv等
     *
     * @param videoFilename 视频文件名（不包含扩展名）
     * @return 原始视频文件完整路径，未找到返回null
     */
    public static String findOriginalVideoFile(String videoFilename) {
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

            if (streams == null || !streams.isArray() || streams.isEmpty()) {
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
