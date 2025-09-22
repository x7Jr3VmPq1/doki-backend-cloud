package com.megrez.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoDraft {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer uploaderId;
    private String filename;
    private String title;
    private String description;
    private String tags;
    private String coverImage;
    private Integer isScheduled;
    private Long scheduledTime;
    private Long uploadTime;
    private Integer sourceUploaded;
    private Integer permission;
    private Integer submitted;
    private Integer reviewStatus;
    private Integer reviewPassed;
    private String reviewReason;
    private Integer transcodingStatus;
    private Integer published;
    private Long updatedTime;
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
}
