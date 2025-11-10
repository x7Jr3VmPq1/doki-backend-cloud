package com.megrez.mysql_entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatistics {

    @JsonIgnore
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer userId;

    private Integer followingCount = 0;

    private Integer followerCount = 0;

    private Integer likeCount = 0;

    private long createdAt = System.currentTimeMillis();

    private long updatedAt = System.currentTimeMillis();
}
