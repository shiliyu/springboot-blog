package hello.service;

import hello.entity.User;
import hello.mapper.UserMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService implements UserDetailsService {
    // 通过 @Service 和 @Component 来代替 @Bean
    // 进行 bean 的声明

    // 为服务配置一个密码的加密器：
    // 当用户新建的时候，我们把用户名和加密后的密码存在内存中；
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    // sb 程序是多线程的，better use thread-safe container
//    private Map<String, User> users = new ConcurrentHashMap<>();
    private UserMapper userMapper;

    // this is for test only
    // 预先定义的用户名和密码，用于测试对服务进行de改造
    @Inject
    public UserService(BCryptPasswordEncoder bCryptPasswordEncoder,
                        UserMapper userMapper) {
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.userMapper = userMapper;
//        save("laowang", "laowang");
    }

    // 当用户注册的时候，保存用户名和密码，以便登录的时候比对
    // plain-texted password is not allowed here!
    public void save(String username, String password) {
//        users.put(username, new User(1, username, bCryptPasswordEncoder.encode(password)));
        userMapper.save(username, bCryptPasswordEncoder.encode(password));
        System.out.println("当前时间: " + new Date(System.currentTimeMillis()));
    }

//    public User getUserById(Integer id) {
//        return null;
//    }

    // 从数据库里查
    public User getUserByUsername(String username) {
        return userMapper.findUserByUsername(username);
    }

    // 通过这样的方式加载一个用户
    // 一个用户名对应的用户信息：
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = getUserByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException(username + "not found!");
        }
        // 前端传过来一个用户名
//        User user = users.get(username);

        // 我们返回给 sb 一个用户信息：
        return new org.springframework.security.core.userdetails.User(username, user.getCryptedPassword(), Collections.emptyList());
    }
}
