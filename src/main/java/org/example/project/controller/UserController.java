package org.example.project.controller;

import org.example.project.common.R;
import org.example.project.entity.UserEntity;
import org.example.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 注册
    @PostMapping("/register")
    public R register(@RequestBody UserEntity user) {
        // 检查用户名是否存在
        if (userService.findByUsername(user.getUsername()) != null) {
            return R.error(1001, "用户名已存在");
        }
        // 检查邮箱是否存在
        if (userService.findByEmail(user.getEmail()) != null) {
            return R.error(1002, "邮箱已被使用");
        }

        // 密码加密（BCrypt）
        String rawPassword = user.getPasswordHash(); // 前端传过来的原始密码
        String encodedPassword = passwordEncoder.encode(rawPassword);
        user.setPasswordHash(encodedPassword);

        // 保存用户
        boolean ok = userService.save(user);
        if (!ok) {
            return R.error(500, "注册失败");
        }

        // 返回注册成功信息（可以返回部分用户信息，不要返回密码）
        return R.ok().put("user", Map.of(
                "username", user.getUsername(),
                "email", user.getEmail()
        ));
    }
    
    // 登录
    @PostMapping("/login")
    public R login(@RequestParam String username, @RequestParam String password) {
        // 根据用户名查询用户
        UserEntity user = userService.findByUsername(username);
        if (user == null) {
            return R.error(400, "用户不存在");
        }

        // 校验密码
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            return R.error(401, "密码错误");
        }

        // 登录成功返回用户信息（不返回密码）
        return R.ok().put("user", Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail()
        )).put("redirectUrl", "/myforum.html");
    }

    @GetMapping("/{id}")
    public String getUser(@PathVariable Long id) {
        return "用户ID: " + id;
    }

}
