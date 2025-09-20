package com.megrez.utils;

import com.megrez.path.FilesServerPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

/**
 * 文件操作工具类
 */
public class FileUtils {
    private static final Logger log = LoggerFactory.getLogger(FileUtils.class);

    /**
     * 将 MultipartFile 转成 Base64 字符串
     *
     * @param file 前端上传的文件
     * @return Base64 编码字符串
     */
    public static String multipartFileToBase64(MultipartFile file) throws IOException {

        // 获取文件字节数组
        byte[] bytes = file.getBytes();

        // 编码成 Base64 字符串

        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * 判断字符串是否为 Base64 编码
     * 支持带 data URI 前缀，例如 "data:image/png;base64,..."
     */
    public static boolean isBase64(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }

        // 去掉可能的 data URI 前缀
        if (str.contains(",")) {
            String[] parts = str.split(",", 2);
            if (parts[0].startsWith("data:") && parts[0].contains(";base64")) {
                str = parts[1];
            }
        }

        // Base64 字符串长度必须是 4 的倍数
        if (str.length() % 4 != 0) {
            return false;
        }

        try {
            Base64.getDecoder().decode(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // 根据文件流保存视频，返回视频URL
    public static String saveVideo(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        // 获取原始文件名（含扩展名）
        String originalFilename = file.getOriginalFilename();
        log.info("保存视频：{}", originalFilename);
        // 获取文件扩展名
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        // 文件夹的唯一名称
        String uniqueFileName = UUID.randomUUID().toString();
        // 创建文件夹，用来保存视频文件
        Path folderPath = Paths.get(FilesServerPath.VIDEO_PATH + uniqueFileName);


        Files.createDirectories(folderPath);
        Path targetFilePath = folderPath.resolve("video" + fileExtension);


        log.info("保存视频：{}", targetFilePath);

        // 将上传的文件保存到目标路径
        file.transferTo(targetFilePath.toFile());

        // 返回唯一文件夹名
        return uniqueFileName;
    }

    /**
     * 删除文件夹（包括其中的文件和子文件夹）
     *
     * @param folder 需要删除的文件夹
     */
    public static void deleteFolder(File folder) {
        if (folder == null || !folder.exists()) {
            return;
        }

        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    // 如果是文件夹就递归删除
                    if (file.isDirectory()) {
                        deleteFolder(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
        // 删除空文件夹
        folder.delete();
    }

    public static void deleteVideo(String filename) {
        File file = new File(FilesServerPath.VIDEO_PATH + filename);
        deleteFolder(file);
    }
}
