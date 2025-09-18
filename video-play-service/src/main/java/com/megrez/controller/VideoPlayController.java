package com.megrez.controller;

import com.megrez.path.FilesServerPath;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;

@RestController
@RequestMapping("/video/play")
public class VideoPlayController {


    private static final Logger log = LoggerFactory.getLogger(VideoPlayController.class);

    @GetMapping("/{filename}")
    public void streamVideo(
            @PathVariable String filename,
            @RequestHeader(value = "Range", required = false) String rangeHeader,
            HttpServletResponse response) throws IOException {


        File videoFile = new File(FilesServerPath.VIDEO_PATH, filename + "\\" + "video.mp4");
        if (!videoFile.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        long fileLength = videoFile.length();
        long start = 0;
        long end = fileLength - 1;

        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            String[] ranges = rangeHeader.substring(6).split("-");
            start = Long.parseLong(ranges[0]);
            if (ranges.length > 1) {
                end = Long.parseLong(ranges[1]);
            }
        }

        long contentLength = end - start + 1;
        response.setStatus(rangeHeader != null ? 206 : 200);
        response.setContentType(Files.probeContentType(videoFile.toPath()));
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Length", String.valueOf(contentLength));
        response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);

        try (RandomAccessFile raf = new RandomAccessFile(videoFile, "r");
             OutputStream os = response.getOutputStream()) {
            raf.seek(start);
            byte[] buffer = new byte[8192];
            long bytesToRead = contentLength;
            int len;
            while ((len = raf.read(buffer, 0, (int) Math.min(buffer.length, bytesToRead))) != -1 && bytesToRead > 0) {
                os.write(buffer, 0, len);
                bytesToRead -= len;
            }
        }
    }

}
