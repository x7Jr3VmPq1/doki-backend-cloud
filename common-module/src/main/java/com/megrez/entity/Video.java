package com.megrez.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 视频实体类
 * <p>
 * 用于存储已发布的视频信息
 * 包含视频基本信息、文件信息、发布设置等
 *
 * @author Doki Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Video {

    /**
     * 视频ID，主键
     * 自增长，唯一标识一个视频
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 上传用户ID，关联user表
     * 标识视频的上传者/发布者
     */
    private Integer uploaderId;

    /**
     * 视频标题
     * 用户设置的视频标题
     */
    private String title;

    /**
     * 视频描述
     * 用户填写的视频描述信息
     */
    private String description;

    /**
     * 视频标签
     * 用户添加的标签，多个标签用逗号分隔
     */
    private String tags;

    /**
     * 视频分类ID，关联category表
     * 标识视频所属的分类
     */
    private Integer categoryId;

    /**
     * 处理后视频文件名
     * 经过转码处理后的视频文件名
     */
    private String videoFilename;

    /**
     * 视频文件大小(字节)
     * 视频文件的实际大小
     */
    private Long videoSize;

    /**
     * 视频时长(秒)
     * 视频的总播放时长
     */
    private Integer videoDuration;

    /**
     * 视频格式(MP4, AVI等)
     * 视频文件的格式类型
     */
    private String videoFormat;

    /**
     * 视频分辨率(如: 1920x1080)
     * 视频的显示分辨率
     */
    private Integer videoWidth;

    private Integer videoHeight;

    /**
     * 视频码率(kbps)
     * 视频的编码码率
     */
    private Integer videoBitrate;

    /**
     * 发布时间戳
     * 视频正式发布的时间
     */
    private Long publishTime;

    /**
     * 可见性: 0-仅自己, 1-公开, 2-关注者可见, 3-好友可见
     * 控制视频的可见范围
     */
    private Integer permission;

    /**
     * 是否允许评论: 0-不允许, 1-允许
     * 控制是否允许其他用户评论此视频
     */
    private Integer allowComment;

    /**
     * 创建时间戳
     * 视频记录创建的时间
     */
    @JsonIgnore
    private Long createdTime;

    /**
     * 更新时间戳
     * 视频信息最后修改的时间
     */
    @JsonIgnore
    private Long updatedTime;


    /**
     * 封面图文件名
     */
    private String coverName;

    /**
     * 是否删除
     * 逻辑删除标记，0-未删除, 1-已删除
     */
    @TableLogic(value = "0", delval = "1")
    @JsonIgnore
    private Integer deleted;

    /**
     * 测试数据标记
     */
    @JsonIgnore
    private Integer isTest;
}
