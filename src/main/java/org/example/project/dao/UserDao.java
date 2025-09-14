package org.example.project.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;
import org.example.project.entity.UserEntity;

@Mapper
public interface UserDao extends BaseMapper<UserEntity> {
    @Select("SELECT * FROM users WHERE username = #{username}")
    UserEntity findByUsername(@Param("username") String username);

    @Select("SELECT * FROM users WHERE email = #{email}")
    UserEntity findByEmail(@Param("email")String email);

    @Insert("INSERT INTO users(username, email, password_hash, created_at) " +
            "VALUES(#{username}, #{email}, #{passwordHash}, NOW())")
    int insert(UserEntity user);
}
