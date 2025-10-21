package com.megrez.rabbit.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
// 删除评论消息
public class CommentDelMessage extends CommentAddMessage {
}
