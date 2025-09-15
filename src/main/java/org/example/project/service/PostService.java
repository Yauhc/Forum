package org.example.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.project.entity.PostEntity;

import java.util.List;

public interface PostService extends IService<PostEntity> {
    List<PostEntity> findAll();
}
