package com.megrez.service;

import com.megrez.result.Response;
import com.megrez.result.Result;
import com.megrez.utils.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class VideoUploadService {
    public Result upload(MultipartFile video) {
        if (video.isEmpty()) {
            return Result.error(Response.VIDEO_UPLOAD_EMPTY_VIDEO); // 文件为空，上传失败
        }
        try {
            // 保存文件
            String name = FileUtils.saveVideo(video);
            if (name == null) {
                return Result.error(Response.VIDEO_UPLOAD_SAVE_FAILED); // 保存失败
            }
            // 保存成功，返回视频名
            return Result.success(name);
        } catch (IOException e) {
            return Result.error(Response.VIDEO_UPLOAD_SAVE_FAILED); // 保存失败
        }
    }
}
