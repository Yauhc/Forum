package org.example.project.controller;

import org.example.project.common.R;
import org.example.project.entity.PostEntity;
import org.example.project.entity.UserEntity;
import org.example.project.service.PostService;
import org.example.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("myforum")
public class MyForumController {
    @Autowired
    private PostService postService;

    @Autowired
    private UserService userService;

    // 获取所有帖子
    @GetMapping
    public R listPosts() {
        List<PostEntity> posts = postService.findAll();

        // 映射 userId -> username
        List<Map<String, ? extends Serializable>> result = posts.stream()
                .map(post -> {
                    UserEntity user = userService.getById(post.getUserId());
                    String username = (user != null) ? user.getUsername() : "未知用户";

                    return Map.of(
                            "id", post.getId(),
                            "title", post.getTitle(),
                            "content", post.getContent(),
                            "author", username,
                            "forumId", post.getForumId(),
                            "createdAt", post.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());

        return R.ok().put("posts", result);
    }
}
