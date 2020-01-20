package com.wjl.mybatis;

import com.wjl.mybatis.entity.User;
import com.wjl.mybatis.mapper.UserMapper;
import com.wjl.mybatis.sql.SqlSession;

import java.sql.SQLException;
import java.util.List;

public class Test {
    public static void main(String[] args) throws SQLException, IllegalAccessException, InstantiationException {
        UserMapper mapper = SqlSession.getMapper(UserMapper.class);
        User userList = mapper.getUserList("测试");
        System.out.println(userList);
    }
}
