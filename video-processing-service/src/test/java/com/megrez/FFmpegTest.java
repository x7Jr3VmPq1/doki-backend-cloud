package com.megrez;

import com.megrez.service.FFmpegUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;

@SpringBootTest
public class FFmpegTest {
    @Test
    void testCreateThumbnail() {
        System.out.println(
                FFmpegUtils.createThumbnail("bf4c21a8-0d6b-4257-bc80-2a9f56718162")
        );
    }

    @Test
    void testGetVideoMeta() {
        System.out.println(FFmpegUtils.getVideoMeta("bf4c21a8-0d6b-4257-bc80-2a9f56718162"));
    }


    @Test
    void testTranscode() throws IOException {
        System.out.println(FFmpegUtils.transcodeVideo("3c989be2-8ea5-4745-a6c2-76a234437268"));
    }

    @Test
    void findFullPath(){
        String originalPath = FFmpegUtils.findOriginalVideoFile("04dac439-002f-45fa-9272-84a87951666e");

        assert originalPath != null;
        File file = new File(originalPath);

        String fileName = file.getName();
        System.out.println(fileName);
    }
}
