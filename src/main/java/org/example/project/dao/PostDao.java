package org.example.project.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.example.project.entity.PostEntity;

import java.util.List;

@Mapper
public interface PostDao extends BaseMapper<PostEntity> {
    @Select("SELECT * FROM posts")
    List<PostEntity> getAllPosts();
}
