package com.megrez.vo.comment_service;

import com.megrez.mysql_entity.User;
import com.megrez.mongo_document.VideoComments;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VideoCommentsVO {
    VideoComments comments; // 评论体
    User user; // 用户信息
    Boolean liked; // 是否点赞
}
