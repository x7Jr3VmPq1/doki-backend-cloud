package com.megrez.service;

/**
 * 视频元数据类
 */
public class VideoMetadata {
    public int width;
    public int height;
    public String rFrameRate;
    public String duration;
    public String bitRate;

    @Override
    public String toString() {
        return String.format(
                "width=%d, height=%d, r_frame_rate=%s, duration=%s, bit_rate=%s",
                width, height, rFrameRate, duration, bitRate
        );
    }
}
