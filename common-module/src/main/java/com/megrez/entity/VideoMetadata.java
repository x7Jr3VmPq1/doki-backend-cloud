package com.megrez.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 视频元数据实体类
 * 
 * 用于存储从FFmpeg获取的视频元数据信息
 * 包含视频的基本技术参数如分辨率、帧率、时长、码率等
 * 
 * @author Doki Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoMetadata {
    
    /**
     * 视频宽度（像素）
     */
    private Integer width;
    
    /**
     * 视频高度（像素）
     */
    private Integer height;
    
    /**
     * 视频帧率
     * 格式如："30/1" 表示30fps
     */
    private String frameRate;
    
    /**
     * 视频时长（秒）
     * 浮点数格式
     */
    private Double duration;
    
    /**
     * 视频码率（bps）
     * 比特率，单位：比特每秒
     */
    private Long bitRate;
    
    /**
     * 视频文件大小（字节）
     * 视频文件的实际大小
     */
    private Long fileSize;
    
    /**
     * 视频分辨率字符串
     * 格式如："1920x1080"
     */
    public String getResolution() {
        if (width != null && height != null) {
            return width + "x" + height;
        }
        return null;
    }
    
    /**
     * 获取标准化的帧率数值
     * 将"30/1"格式转换为30.0
     */
    public Double getFrameRateValue() {
        if (frameRate == null || frameRate.isEmpty()) {
            return null;
        }
        
        try {
            if (frameRate.contains("/")) {
                String[] parts = frameRate.split("/");
                if (parts.length == 2) {
                    double numerator = Double.parseDouble(parts[0]);
                    double denominator = Double.parseDouble(parts[1]);
                    return denominator != 0 ? numerator / denominator : null;
                }
            } else {
                return Double.parseDouble(frameRate);
            }
        } catch (NumberFormatException e) {
            // 解析失败时返回null
        }
        
        return null;
    }
}
