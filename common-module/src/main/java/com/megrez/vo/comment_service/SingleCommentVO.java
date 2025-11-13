package com.megrez.vo.comment_service;

import com.megrez.mongo_document.VideoComments;
import com.megrez.mysql_entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SingleCommentVO {

    VideoComments comments; // 评论主体

    User user; // 用户信息

    Boolean liked; // 是否点赞

    @Builder.Default
    Integer page = -1; // 如果这条评论是次级评论，则表示它所在回复列表的页码是多少，默认是-1
}
