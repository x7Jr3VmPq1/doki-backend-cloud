package com.megrez.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.megrez.mysql_entity.VideoStatistics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 视频统计数据访问层
 * 
 * 提供视频统计相关的数据库操作接口
 * 继承MyBatis-Plus的BaseMapper，提供基础的CRUD操作
 * 
 * @author Doki Team
 * @since 1.0.0
 */
@Mapper
public interface VideoStatisticsMapper extends BaseMapper<VideoStatistics> {
    
    /**
     * 增加视频播放次数
     * 
     * @param videoId 视频ID
     * @param increment 增加的次数，默认为1
     * @return 影响的行数
     */
    @Update("UPDATE video_statistics SET view_count = view_count + #{increment}, updated_time = #{currentTime} WHERE video_id = #{videoId}")
    int incrementViewCount(@Param("videoId") Integer videoId, @Param("increment") Long increment, @Param("currentTime") Long currentTime);
    
    /**
     * 增加视频点赞数
     * 
     * @param videoId 视频ID
     * @param increment 增加的次数，默认为1
     * @return 影响的行数
     */
    @Update("UPDATE video_statistics SET like_count = like_count + #{increment}, updated_time = #{currentTime} WHERE video_id = #{videoId}")
    int incrementLikeCount(@Param("videoId") Integer videoId, @Param("increment") Long increment, @Param("currentTime") Long currentTime);
    
    /**
     * 增加视频点踩数
     * 
     * @param videoId 视频ID
     * @param increment 增加的次数，默认为1
     * @return 影响的行数
     */
    @Update("UPDATE video_statistics SET dislike_count = dislike_count + #{increment}, updated_time = #{currentTime} WHERE video_id = #{videoId}")
    int incrementDislikeCount(@Param("videoId") Integer videoId, @Param("increment") Long increment, @Param("currentTime") Long currentTime);
    
    /**
     * 增加视频评论数
     * 
     * @param videoId 视频ID
     * @param increment 增加的次数，默认为1
     * @return 影响的行数
     */
    @Update("UPDATE video_statistics SET comment_count = comment_count + #{increment}, updated_time = #{currentTime} WHERE video_id = #{videoId}")
    int incrementCommentCount(@Param("videoId") Integer videoId, @Param("increment") Long increment, @Param("currentTime") Long currentTime);
    
    /**
     * 增加视频分享数
     * 
     * @param videoId 视频ID
     * @param increment 增加的次数，默认为1
     * @return 影响的行数
     */
    @Update("UPDATE video_statistics SET share_count = share_count + #{increment}, updated_time = #{currentTime} WHERE video_id = #{videoId}")
    int incrementShareCount(@Param("videoId") Integer videoId, @Param("increment") Long increment, @Param("currentTime") Long currentTime);
    
    /**
     * 增加视频收藏数
     * 
     * @param videoId 视频ID
     * @param increment 增加的次数，默认为1
     * @return 影响的行数
     */
    @Update("UPDATE video_statistics SET favorite_count = favorite_count + #{increment}, updated_time = #{currentTime} WHERE video_id = #{videoId}")
    int incrementFavoriteCount(@Param("videoId") Integer videoId, @Param("increment") Long increment, @Param("currentTime") Long currentTime);
    
    /**
     * 增加视频下载数
     * 
     * @param videoId 视频ID
     * @param increment 增加的次数，默认为1
     * @return 影响的行数
     */
    @Update("UPDATE video_statistics SET download_count = download_count + #{increment}, updated_time = #{currentTime} WHERE video_id = #{videoId}")
    int incrementDownloadCount(@Param("videoId") Integer videoId, @Param("increment") Long increment, @Param("currentTime") Long currentTime);
}
