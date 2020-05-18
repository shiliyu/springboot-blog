package hello.configuration;

import org.springframework.context.annotation.Configuration;

@Configuration
public class JavaConfiguration {
    // 通过这种方式告诉 sb 我要的 bean 依赖谁，怎么样去创建：
    // spring boot 是如何读取到 configuration 的
    // spring boot 入口是SpringBootApplication
    // @EnableAutoConfiguration
    // @SpringBootApplication(scanBasePackages = {"my.package"})
    // 告诉 sb 启动的时候扫描哪些包，它可以扫描指定的包，指定的类下面的 configuration 的东西
    // @SpringBootApplication(scanBasePackageClasses = {"my.package.class"}) 来指定要用哪个配置的class

    // 要么在配置类里声明 @Bean，
    // 要么在 UserService 上 @Service 注解：







}
