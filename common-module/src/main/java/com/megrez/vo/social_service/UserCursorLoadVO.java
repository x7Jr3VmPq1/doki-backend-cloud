package com.megrez.vo.social_service;

import com.megrez.vo.user_service.UsersVO;
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
    private List<UsersVO> list = List.of();
    @Builder.Default
    private Boolean hasMore = false;
    @Builder.Default
    private String cursor = null;
}
