package com.wjl.mybatis.sql;

import com.wjl.mybatis.aop.MyInvocationHandlerMbatis;

import java.lang.reflect.Proxy;

public class SqlSession {

    public static <T> T getMapper(Class clazz){
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(),new Class[]{clazz},new MyInvocationHandlerMbatis(clazz));
    }

}
