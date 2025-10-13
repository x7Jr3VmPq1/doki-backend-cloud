package com.megrez.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoLike {
    private Integer id;
    private Integer userId;
    private Integer videoId;
    private Long createdAt;
}
