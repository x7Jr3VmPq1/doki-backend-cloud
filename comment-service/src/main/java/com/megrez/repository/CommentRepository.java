package com.megrez.repository;

import com.megrez.entity.VideoComments;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.awt.print.Pageable;
import java.util.List;

public interface CommentRepository extends MongoRepository<VideoComments,String> {

    /**
     * 根据视频ID查询所有评论
     * @param id 视频id
     * @return 评论集合
     */
    List<VideoComments> findByVideoId(Integer id);

}
