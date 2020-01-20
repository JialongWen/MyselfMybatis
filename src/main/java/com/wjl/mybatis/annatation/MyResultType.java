package com.wjl.mybatis.annatation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MyResultType {
    Class<?> value();
}
