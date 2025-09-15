package org.example.project.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpSession;
import org.example.project.common.R;
import org.example.project.entity.PostEntity;
import org.example.project.entity.UserEntity;
import org.example.project.service.PostService;
import org.example.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;
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
    @GetMapping("/listPosts")
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

    // 发布帖子
    @PostMapping("/createPost")
    public R createPost(@RequestBody Map<String, String> payload, HttpSession session) {
        // 1. 获取当前登录用户
        UserEntity user = (UserEntity) session.getAttribute("user");
        if (user == null) {
            return R.error("未登录，请先登录");
        }

        // 2. 获取请求参数
        String title = payload.get("title");
        String content = payload.get("content");

        if(title == null || title.isBlank() || content == null || content.isBlank()) {
            return R.error("标题和内容不能为空");
        }

        // 3. 构建 PostEntity 并保存
        PostEntity post = new PostEntity();
        post.setTitle(title);
        post.setContent(content);
        post.setUserId(Long.valueOf(user.getId()));
        post.setCreatedAt(LocalDateTime.now());

        postService.save(post);

        // 4. 返回成功
        return R.ok("发布成功").put("postId", post.getId());
    }
}
