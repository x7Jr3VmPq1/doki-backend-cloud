package com.megrez.rabbit.message;

import com.megrez.entity.VideoComments;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
// 增加评论消息
public class CommentAddMessage {
    private VideoComments videoComments; // 评论本体
    private Long timestamp = System.currentTimeMillis(); // 时间
}
