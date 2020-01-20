package com.wjl.mybatis.annatation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MyInsert {
    //注意这里源码上是个数组
    String value();
}
