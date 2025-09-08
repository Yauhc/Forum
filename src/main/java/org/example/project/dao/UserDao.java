package org.example.project.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.project.entity.UserEntity;

@Mapper
public interface UserDao extends BaseMapper<UserEntity> {
}
