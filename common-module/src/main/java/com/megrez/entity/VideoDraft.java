package com.megrez.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 视频草稿实体类
 * 
 * 用于存储用户正在编辑或待发布的视频草稿信息
 * 支持定时发布、审核流程、转码状态跟踪等功能
 * 
 * @author Doki Team
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VideoDraft {
    
    /**
     * 草稿ID，主键
     * 自增长，唯一标识一个视频草稿
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    
    /**
     * 上传者ID，关联user表
     * 标识草稿的创建者/上传者
     */
    private Integer uploaderId;
    
    /**
     * 原始文件名
     * 用户上传时的原始文件名，用于显示和下载
     */
    private String filename;
    
    /**
     * 视频标题
     * 用户编辑的视频标题，支持草稿保存
     */
    private String title;
    
    /**
     * 视频描述
     * 用户填写的视频描述信息，支持草稿保存
     */
    private String description;
    
    /**
     * 视频标签
     * 用户添加的标签，多个标签用逗号分隔
     */
    private String tags;
    
    /**
     * 封面图片路径
     * 视频的封面图片文件路径
     */
    private String coverImage;
    
    /**
     * 是否定时发布
     * 0-立即发布, 1-定时发布
     */
    private Integer isScheduled;
    
    /**
     * 定时发布时间戳
     * 当isScheduled=1时，指定具体的发布时间
     */
    private Long scheduledTime;
    
    /**
     * 上传时间戳
     * 文件上传到服务器的时间
     */
    private Long uploadTime;
    
    /**
     * 源文件是否已上传
     * 0-未上传, 1-已上传
     */
    private Integer sourceUploaded;
    
    /**
     * 权限设置
     * 0-公开,1-好友可见，2-私密
     */
    private Integer permission;
    
    /**
     * 是否已提交审核
     * 0-未提交, 1-已提交
     */
    private Integer submitted;
    
    /**
     * 审核状态
     * 0-待审核, 1-审核中, 2-审核完毕
     */
    private Integer reviewStatus;
    
    /**
     * 审核是否通过
     * 0-未通过, 1-已通过
     */
    private Integer reviewPassed;
    
    /**
     * 审核原因
     * 审核通过或拒绝的具体原因说明
     */
    private String reviewReason;
    
    /**
     * 转码状态
     * 0-未开始, 1-转码完成
     */
    private Integer transcodingStatus;
    
    /**
     * 是否已发布
     * 0-未发布, 1-已发布
     */
    private Integer published;
    
    /**
     * 最后更新时间戳
     * 草稿最后修改的时间
     */
    private Long updatedTime;
    
    /**
     * 是否删除
     * 逻辑删除标记，0-未删除, 1-已删除
     */
    @TableLogic(value = "0", delval = "1")
    private Integer deleted;
}
