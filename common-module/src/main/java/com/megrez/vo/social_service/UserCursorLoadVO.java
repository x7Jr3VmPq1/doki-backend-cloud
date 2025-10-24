package com.megrez.vo.social_service;

import com.megrez.entity.User;
import com.megrez.vo.comment_service.VideoCommentsVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserCursorLoadVO {
    @Builder.Default
    private List<User> list = List.of();
    @Builder.Default
    private Boolean hasMore = false;
    @Builder.Default
    private String cursor = null;
}
