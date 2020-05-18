package hello.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import hello.entity.User;

@Mapper
public interface UserMapper {
    @Select("SELECT * FROM user WHERE id = #{id}")
    User findUserById(@Param("id") Integer id);

    @Select("SELECT * FROM user WHERE username = #{username}")
    User findUserByUsername(String username);

    @Select("INSERT INTO user(username, crypted_password, created_at, updated_at)" +
            "values(#{username}, #{cryptedPassword}, now(), now())")
    void save(@Param("username") String username, @Param("cryptedPassword") String cryptedPassword);
}
