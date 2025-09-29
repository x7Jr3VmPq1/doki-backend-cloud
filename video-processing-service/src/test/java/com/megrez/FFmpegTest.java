package com.megrez;

import com.megrez.utils.FFmpegUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

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

}
