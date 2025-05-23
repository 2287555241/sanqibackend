package sanqibackend.sanqibackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import sanqibackend.sanqibackend.entity.User;
import sanqibackend.sanqibackend.service.UserService;
import sanqibackend.sanqibackend.utils.Result;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<User> loginController(@RequestBody User user) {
        String username = user.getUsername();
        String password = user.getPassword();
        User result = userService.loginService(username, password);
        if (result != null) {
            return Result.success(result, "登录成功！");
        } else {
            return Result.error("123", "账号或密码错误！");
        }
    }

    @PostMapping("/register")
    public Result<User> registController(@RequestBody User user) {
        User result = userService.registService(user);
        if (result != null) {
            return Result.success(result, "注册成功！");
        } else {
            return Result.error("456", "用户名格式不正确或已存在！");
        }
    }
}