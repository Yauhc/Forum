package org.example.project.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.project.common.R;
import org.example.project.entity.PostEntity;
import org.example.project.entity.UserEntity;
import org.example.project.service.PostService;
import org.example.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    // 获取所有帖子（带分页）
    @GetMapping
    public R listPosts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        // 1. 调用 MyBatis-Plus 的分页方法
        Page<PostEntity> pageData = postService.page(new Page<>(page, size));

        // 2. 把 PostEntity 转成 Map，加上 username
        List<Map<String, ? extends Serializable>> result = pageData.getRecords().stream()
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

        // 3. 返回分页结果
        return R.ok()
                .put("posts", result)         // 当前页的数据
                .put("total", pageData.getTotal()) // 总记录数
                .put("pages", pageData.getPages()) // 总页数
                .put("current", pageData.getCurrent()); // 当前页码
    }

}
