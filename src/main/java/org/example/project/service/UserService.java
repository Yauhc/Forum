package org.example.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.project.entity.UserEntity;

public interface UserService extends IService<UserEntity> {
    UserEntity findByUsername(String username);
    UserEntity findByEmail(String email);
}
