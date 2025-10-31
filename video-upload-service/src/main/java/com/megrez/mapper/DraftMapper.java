package com.megrez.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.megrez.mysql_entity.VideoDraft;
import org.apache.ibatis.annotations.*;


@Mapper
public interface DraftMapper extends BaseMapper<VideoDraft> {

    /**
     * 根据上传者ID获取未提交草稿信息
     *
     * @param uploaderId 上传者ID
     * @return 草稿列表
     */
    @Select("SELECT * FROM video_draft WHERE uploader_id = #{uploaderId} " +
            "AND submitted = 0 AND deleted = 0")
    VideoDraft getByUploaderId(Integer uploaderId);
}
