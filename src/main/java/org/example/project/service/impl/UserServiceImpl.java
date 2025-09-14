package org.example.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.project.dao.UserDao;
import org.example.project.entity.UserEntity;
import org.example.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserDao, UserEntity> implements UserService {
    @Autowired
    private UserDao userDao;

    @Override
    public UserEntity findByUsername(String username) {
        return lambdaQuery().eq(UserEntity::getUsername, username).one();
    }

    @Override
    public UserEntity findByEmail(String email) {
        return lambdaQuery().eq(UserEntity::getEmail, email).one();
    }
}
