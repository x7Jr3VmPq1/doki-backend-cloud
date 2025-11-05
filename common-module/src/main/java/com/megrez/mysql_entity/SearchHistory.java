package com.megrez.mysql_entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchHistory {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String keyword;
    private Integer count = 0;
    private Long createdAt = System.currentTimeMillis();
    private Long updatedAt = System.currentTimeMillis();
}
