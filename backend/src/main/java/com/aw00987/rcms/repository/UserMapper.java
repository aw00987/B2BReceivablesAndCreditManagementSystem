package com.aw00987.rcms.repository;

import com.aw00987.rcms.dto.UserResponseDto;
import com.aw00987.rcms.entity.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserMapper {

    void insert(User user);

    void update(User user);

    @Delete("delete from users where id = #{id}")
    void deleteById(@Param("id") Long id);

    @Select("select * from users where id = #{id}")
    Optional<User> selectById(@Param("id") Long id);

    @Select("select username, real_name, role from users " +
            "where status = 'ENABLED' " +
            "order by id limit #{pageSize} offset #{offset}")
    List<UserResponseDto> selectPage(@Param("offset") long offset, @Param("pageSize") int pageSize);

    @Select("select count(id) from users where status = 'ENABLED'")
    long selectCount();

    @Select("select id, username, real_name from users " +
            "where real_name like concat('%', #{realName}, '%') " +
            "and status = 'ENABLED' " +
            "order by real_name limit 10")
    List<User> selectUserSimpleDictionary(@Param("realName") String realName);

    @Select("select * from users where username = #{username}")
    Optional<User> selectByUsername(@Param("username") String username);

}
