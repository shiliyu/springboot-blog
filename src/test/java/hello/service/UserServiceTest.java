package hello.service;

import hello.entity.User;
import hello.mapper.UserMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    BCryptPasswordEncoder mockEncoder;
    @Mock
    UserMapper mockUserMapper;

    @InjectMocks
    UserService userService;

    @Test
    void testSave() {
        // call userService
        // check if userService sended request to userMapper or not.
        // 验证 userService 是否将请求转发给了 userMapper

        Mockito.when(mockEncoder.encode("myPassword")).thenReturn("cryptedPassword");
        // 想要测试的逻辑： 即调用要测试的方法
        userService.save("myUser", "myPassword");
        // 通过 mockUserMapper 来验证：
        Mockito.verify(mockUserMapper).save("myUser", "cryptedPassword");
    }

    @Test
    void testGetUserByUsername() {
        userService.getUserByUsername("myUser");
        Mockito.verify(mockUserMapper).findUserByUsername("myUser");
    }

    @Test
    void returnUserDetailsWhenUserFound() {
        Mockito.when(mockUserMapper.findUserByUsername("myUser")).thenReturn(new User(123, "myUser", "cryptedPassword"));
        // 真是的逻辑：调用遥测的方法
        UserDetails userDetails = userService.loadUserByUsername("myUser");
        Assertions.assertEquals("myUser", userDetails.getUsername());
        Assertions.assertEquals("cryptedPassword", userDetails.getPassword());
    }

    @Test
    void throwExceptionWhenUserNotFound() {
        // 这句话是多余的， 如果不对它进行配置的话，就会返回null
        Mockito.when(mockUserMapper.findUserByUsername("myUser")).thenReturn(null);
        // 断言：expectedException， actualException
        Assertions.assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("myUser"));
    }
}
