package com.megrez.vo;

import com.megrez.entity.User;
import com.megrez.entity.VideoComments;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VideoCommentsVO {
    VideoComments videoComments; // 评论体
    User user; // 用户信息
    Boolean liked; // 是否点赞
}
