package hello.controller;

import hello.entity.Result;
import hello.entity.User;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import hello.service.UserService;

import javax.inject.Inject;
import java.util.Map;

@Controller
public class AuthController {
    // 映射一个 @GetMapping 到某一个接口
    // http 请求和响应本质就是字节流
    // 如果期望服务器进程返回的是 json 字符串


    // 需要一个验权服务：
    private AuthenticationManager authenticationManager;

    // 这是提供用户信息的服务：
    // 之前是 UserDetailsService， 现在重构为 Userervice:
    private UserService userService;

    @Inject
    public AuthController(UserService userService,
                          AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/auth")
    @ResponseBody
    public Object auth() {
        // 第二次带着 cookie 来的时候就可以在这里直接访问用户信息了
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication == null ? null : authentication.getName();

        User loggedInUser = userService.getUserByUsername(username);

        if (loggedInUser == null) {
            // anonymous user:
            return new Result("ok", "用户没有登录", false);
        } else {
            return new Result("ok", "用户已登录", true, loggedInUser);
        }
    }

    @PostMapping("/auth/register")
    @ResponseBody
    public Result register(@RequestBody Map<String, String> usernameAndPasswordJson) {
        String username = usernameAndPasswordJson.get("username");
        String password = usernameAndPasswordJson.get("password");

        if (username == null || password == null) {
            return Result.failsAsIn("username/password==null");
        }

        if (username.length() < 1 || username.length() > 15) {
            return Result.failsAsIn("invalid username");
        }
        if (password.length() < 1 || password.length() > 15) {
            return Result.failsAsIn("invalid password");
        }

        try { // 把2次数据库操作变为1次，而且避免了并发问题！
            userService.save(username, password);
            return Result.succeedsWith("register succeeds!");
        } catch (DuplicateKeyException e) {  // 预期的异常
            return Result.failsAsIn("user already exists!");
        }

        // 加载这个用户，看是否存在
//        User user = userService.getUserByUsername(username);

        // not safe: 并发情况下可能同时判断为 null，造成可以同时执行 save 操作
        // 带来结果：save 操作会被相同的username 执行2遍
        // 竞争条件带来的问题：
//        if (user == null) {
//            userService.save(username, password);
//            return new Result("ok", "register succeeds!", true);
//        } else {
//            return new Result("failed", "user already exists", false);
//        }

    }

    @PostMapping("/auth/login")
    @ResponseBody
    public Result login(@RequestBody Map<String, Object> usernameAndPasswordJson) {
        // 拿到前端传过来的密码
        String username = usernameAndPasswordJson.get("username").toString();
        String password = usernameAndPasswordJson.get("password").toString();

        UserDetails userDetails = null;
        try {
            // 去数据库里拿到真正的密码
            userDetails = userService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            return Result.failsAsIn("用户不存在");
        }

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userDetails, password,
                userDetails.getAuthorities());
        try {
            // 命令 authenticationManager 拿着真正的密码和用户传过来的密码进行比对：
            // 比对的结果在这里处理：
            authenticationManager.authenticate(token);
            // 把用户信息保存在一个地方：
            // 用户信息就是 cookie
            // 方便下次可以拿到
            SecurityContextHolder.getContext().setAuthentication(token);
            // when login succeeds, return the real user:
            User loggedInUser = userService.getUserByUsername(username);
            return new Result("ok", "login successful", true, loggedInUser);

        } catch (BadCredentialsException e) {
            // not authenticated！
            return Result.failsAsIn("密码不正确！");
        }
    }

    @GetMapping("/auth/logout")
    @ResponseBody
    public Object logout() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        if (username == null) {
            return Result.failsAsIn("not logged in yet!");
        } else {
            SecurityContextHolder.clearContext();
            return Result.succeedsWith("logout succeeds！");
        }
    }
}

