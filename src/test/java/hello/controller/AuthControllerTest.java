package hello.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hello.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.http.HttpSession;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SpringExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Mock
    private UserService userService;
    @Mock
    private AuthenticationManager authenticationManager;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(userService, authenticationManager)).build();
    }

    @Test
    void returnNotLoginByDefault() throws Exception {
        // 假设登录结果是 200 ok
        // 验证返回的结果是不是用户没有登录的状态
        // 对只有一个虚方法的接口变成一个 lambda 表达式：
        mockMvc.perform(get("/auth"))
                .andExpect(status().isOk())
                .andExpect(mvcResult -> Assertions.assertTrue(mvcResult.getResponse().getContentAsString(Charset.defaultCharset()).contains("用户没有登录")));
    }

    @Test
    void testLogin() throws Exception {
        // 测试登录状态的测试：
        //     未登录时，/auth 接口返回未登录状态
        //     使用/auth/login 登录
        //     检查/auth 的返回值，处于登录状态

        mockMvc.perform(get("/auth"))
                .andExpect(status().isOk())
                .andExpect(mvcResult -> Assertions.assertTrue(mvcResult.getResponse().getContentAsString(Charset.defaultCharset()).contains("用户没有登录")));

        // auth/login 登录：
        Map<String, String> usernamePassword = new HashMap<>();
        usernamePassword.put("username", "myUser");
        usernamePassword.put("password", "myPassword");

        Mockito.when(userService.loadUserByUsername("myUser")).thenReturn(new User("myUser", encoder.encode("myPassword"),
                Collections.emptyList()));
        Mockito.when(userService.getUserByUsername("myUser")).thenReturn(new hello.entity.User(123, "myUser",
                encoder.encode("myPassword")));

        // 因为这是一个登录接口，只返回200是不够的，还要知道登录是否成功：因此先要拿到登录的返回值
        MvcResult response =
                mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON_VALUE)
                        // 用户的输入
                        .content(new ObjectMapper().writeValueAsString(usernamePassword)))
                        // 验证是否为200
                        .andExpect(status().isOk())
                        // 验证 response body 是否包含 login successful
                        .andExpect(mvcResult -> {
                                    System.out.println("是否包含 login successful： "+mvcResult.getResponse().getContentAsString());
                                    Assertions.assertTrue(mvcResult.getResponse().getContentAsString().contains(
                                            "login successful"));
                                })
                        // 这个 return 有意思，查看一下 api， 看是什么意思
                        .andReturn();
//        System.out.println(Arrays.toString(response.getResponse().getCookies()));
        // 拿session 执行一次登录操作：
        HttpSession session = response.getRequest().getSession();

        mockMvc.perform(get("/auth").session((MockHttpSession)session))
                .andExpect(status().isOk())
                // 预期得到一个登录后的结果
                .andExpect(mvcResult -> {
                    // 把预期的响应打印出来，
                    System.out.println(mvcResult.getResponse().getContentAsString());
                    Assertions.assertTrue(mvcResult.getResponse().getContentAsString().contains(
                            "myUser"));
                });
    }
}