package com.megrez.service;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 分辨率模板
 * 这个数字代表视频较短的那个边。
 * 如果是宽屏视频，代表高度，如果是竖屏视频，代表宽度。
 */

public class ResolutionTemplate {
    public static final int NORMAL = 480;
    public static final int MEDIUM = 720;
    public static final int HIGH = 1080;
    public static final int VERY_HIGH = 1440;
    public static final int ULTRA = 2160;

    /**
     * 所有标准分辨率的列表，按升序排列
     */
    private static final List<Integer> ALL_RESOLUTIONS = List.of(NORMAL, MEDIUM, HIGH, VERY_HIGH, ULTRA);

    /**
     * 根据输入视频的尺寸，获取所有适合作为目标转码的分辨率。
     *
     * 规则：
     * 1. 找到视频的较短边（即标准分辨率的代表值）。
     * 2. 将该较短边“向上取整”到最接近的标准分辨率（如果它不是标准分辨率）。
     * 3. 返回所有小于或等于这个“向上取整”后的标准分辨率的模板值。
     *
     * @param width 视频的宽度
     * @param height 视频的高度
     * @return 目标分辨率列表 (List<Integer>)
     */
    public static List<Integer> getTargetResolutions(int width, int height) {
        // 1. 找到视频的较短边
        int shortSide = Math.min(width, height);

        // 2. 将该较短边“向上取整”到最接近的标准分辨率
        int ceilingResolution = -1;
        for (int resolution : ALL_RESOLUTIONS) {
            if (shortSide <= resolution) {
                // 找到了第一个大于或等于短边的标准分辨率
                ceilingResolution = resolution;
                break;
            }
        }

        // 如果短边大于所有标准分辨率（例如：短边是3000），则取最大的标准分辨率作为上限
        if (ceilingResolution == -1) {
            ceilingResolution = ULTRA;
        }

        // 3. 返回所有小于或等于这个“向上取整”后的标准分辨率的模板值
        List<Integer> targetResolutions = new ArrayList<>();
        for (int resolution : ALL_RESOLUTIONS) {
            if (resolution <= ceilingResolution) {
                targetResolutions.add(resolution);
            }
        }

        return targetResolutions;
    }
}