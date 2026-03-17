package com.megrez.mysql_entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


// 敏感词实体类
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SensitiveWords {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String word;

    private long createdAt;
}
