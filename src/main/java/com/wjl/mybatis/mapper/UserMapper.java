package com.wjl.mybatis.mapper;

import com.wjl.mybatis.annatation.MyInsert;
import com.wjl.mybatis.annatation.MyParam;
import com.wjl.mybatis.annatation.MyResultType;
import com.wjl.mybatis.annatation.MySelect;
import com.wjl.mybatis.entity.User;

import java.util.List;

public interface UserMapper {
    @MyInsert("insert into tb_user(name,age) values(#{name},#{age})")
    int insert(@MyParam("name") String name, @MyParam("age") Integer age);

    @MySelect("select name,age from tb_user where name=#{name}")
    @MyResultType(User.class)
    User getUserList(@MyParam("name")String name);
}
