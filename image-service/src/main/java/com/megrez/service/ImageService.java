package com.megrez.service;

import com.megrez.path.FilesServerPath;
import com.megrez.ImageType;
import com.megrez.rabbit.dto.CommentDelMessage;
import com.megrez.rabbit.exchange.CommentDeleteExchange;
import com.megrez.utils.JSONUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

@Service
public class ImageService {

    private static final Logger log = LoggerFactory.getLogger(ImageService.class);

    public String saveImage(String base64, ImageType type) throws Exception {
        // 1. 检查上传目录是否存在，不存在则创建
        File dir;
        switch (type) {
            case USER_AVATAR -> dir = new File(FilesServerPath.AVATAR_PATH);
            case VIDEO_COVER -> dir = new File(FilesServerPath.COVER_PATH);
            case COMMENT_IMG -> dir = new File(FilesServerPath.COMMENT_IMG_PATH);
            default -> throw new RuntimeException("参数异常");
        }
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                log.warn("目录创建失败: {}", dir.getAbsolutePath());
            }
        }

        // 2. 生成唯一文件名，避免覆盖
        String fileName = UUID.randomUUID() + ".jpg";
        File file = new File(dir, fileName);

        // 3. 处理 Base64 字符串
        if (base64.contains(",")) {
            base64 = base64.split(",")[1]; // 去掉可能的 "data:image/png;base64," 前缀
        }
        byte[] data = Base64.getDecoder().decode(base64);

        // 4. 保存文件
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
            log.info("文件上传成功！文件名：{}", fileName);
        } catch (Exception e) {
            throw new Exception("文件保存失败");
        }

        // 5. 返回文件名
        return fileName;
    }

    public byte[] getImage(String filename, ImageType type) throws IOException {
        Path filePath;
        // 1. 判断需求的类型
        switch (type) {
            case USER_AVATAR -> filePath = Paths.get(FilesServerPath.AVATAR_PATH, filename);
            case VIDEO_COVER -> filePath = Paths.get(FilesServerPath.COVER_PATH, filename);
            case COMMENT_IMG -> filePath = Paths.get(FilesServerPath.COMMENT_IMG_PATH, filename);
            default -> throw new RuntimeException("参数异常");
        }
        // 2. 如果不存在这个文件，抛出异常
        if (!Files.exists(filePath)) {
            throw new RuntimeException("文件未找到");
        }
        // 3. 找到，返回字节流
        return Files.readAllBytes(filePath);
    }

    /**
     * 处理删除评论消息，删除评论对应的图片文件。
     *
     * @param message 评论删除消息
     */
    @RabbitListener(queues = CommentDeleteExchange.QUEUE_COMMENT_DELETE_IMAGE)
    public void deleteCommentImage(String message) {
        CommentDelMessage commentDelMessage = JSONUtils.fromJSON(message, CommentDelMessage.class);
        // 文件名的形式是这样是：e56d267c-52c8-4027-b973-afb66e4dff5c.jpg
        String filename = commentDelMessage.getVideoComments().getImgUrl();
        if (filename != null && !filename.isEmpty()) {
            filename = filename.trim(); // 去除文件名上可能存在的空格
            // 执行删除
            Path path = Paths.get(FilesServerPath.COMMENT_IMG_PATH, filename);
            try {
                if (Files.exists(path)) {
                    Files.delete(path);
                    log.info("已删除评论ID为 {} 上的图片:{} ", commentDelMessage.getVideoComments().getId(), filename);
                }
            } catch (IOException e) {
                log.error("发生IO错误", e);
            }
        }
    }

}
