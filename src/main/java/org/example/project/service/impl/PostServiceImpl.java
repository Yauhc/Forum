package org.example.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.project.dao.PostDao;
import org.example.project.entity.PostEntity;
import org.example.project.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("postService")
public class PostServiceImpl extends ServiceImpl<PostDao, PostEntity> implements PostService {
    @Autowired
    private PostDao postDao;

    @Override
    public List<PostEntity> findAll() {
        return lambdaQuery().list();
    }
}
