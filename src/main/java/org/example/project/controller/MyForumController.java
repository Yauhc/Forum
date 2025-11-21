package org.example.project.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.project.common.R;
import org.example.project.common.error.ErrorCode;
import org.example.project.common.exception.BusinessException;
import org.example.project.dto.PostCreateRequest;
import org.example.project.dto.PostDto;
import org.example.project.dto.UserProfileDto;
import org.example.project.entity.PostEntity;
import org.example.project.entity.UserEntity;
import org.example.project.service.CurrentUserResolver;
import org.example.project.service.PostService;
import org.example.project.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * フォーラム API。
 */
@Slf4j
@RestController
@RequestMapping("/myforum")
@RequiredArgsConstructor
public class MyForumController {

    private final PostService postService;
    private final UserService userService;
    private final CurrentUserResolver currentUserResolver;

    /**
     * 投稿一覧を取得する。
     */
    @GetMapping("/listPosts")
    public R listPosts(@RequestParam(defaultValue = "1") long page,
                       @RequestParam(defaultValue = "10") long size) {
        Page<PostEntity> pageData = postService.page(new Page<>(page, size));
        var userIds = pageData.getRecords().stream()
                .map(PostEntity::getUserId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, String> authorMap = userIds.isEmpty() ? Map.of() :
                userService.listByIds(userIds).stream()
                        .collect(Collectors.toMap(UserEntity::getId, UserEntity::getUsername));

        List<PostDto> result = pageData.getRecords().stream()
                .map(post -> {
                    PostDto dto = new PostDto();
                    dto.setId(post.getId());
                    dto.setTitle(post.getTitle());
                    dto.setContent(post.getContent());
                    dto.setForumId(post.getForumId());
                    dto.setCreatedAt(post.getCreatedAt());
                    dto.setUpdatedAt(post.getUpdatedAt());
                    dto.setAuthor(authorMap.getOrDefault(post.getUserId(), "unknown"));
                    return dto;
                })
                .toList();

        return R.ok()
                .put("posts", result)
                .put("total", pageData.getTotal())
                .put("pages", pageData.getPages())
                .put("current", pageData.getCurrent());
    }

    /**
     * 投稿を作成する。
     */
    @PostMapping("/createPost")
    public ResponseEntity<R> createPost(@Valid @RequestBody PostCreateRequest request, HttpSession session) {
        UserProfileDto user = currentUserResolver.resolve(session)
                .orElseThrow(() -> new BusinessException(ErrorCode.UNAUTHORIZED, "ログインしてください"));

        PostEntity post = new PostEntity();
        post.setTitle(request.getTitle().trim());
        post.setContent(request.getContent().trim());
        post.setForumId(request.getForumId());
        post.setUserId(user.getId());
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        postService.save(post);
        log.info("post created by {} with id {}", user.getUsername(), post.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(R.ok("投稿しました").put("postId", post.getId()));
    }
}
