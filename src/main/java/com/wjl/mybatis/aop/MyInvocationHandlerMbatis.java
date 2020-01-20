package com.wjl.mybatis.aop;

import com.wjl.mybatis.annatation.MyInsert;
import com.wjl.mybatis.annatation.MyParam;
import com.wjl.mybatis.annatation.MyResultType;
import com.wjl.mybatis.annatation.MySelect;
import com.wjl.mybatis.util.JdbcUtil;
import com.wjl.mybatis.util.SQLUtil;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MyInvocationHandlerMbatis implements InvocationHandler {
    private Object object;

    public MyInvocationHandlerMbatis(Object object){
        this.object = object;
    }

    /**
      * 大体的实现思路
      * 1.需要获取到方法上的注解
      * 2.从注解上获取到我们的sql语句
      * 3.获取到这个方法的参数
      * 4.获取参数上的注解
      * 5.获取注解的名称和参数的值并且将他们存放到一个集合中
      * 6.获取到sql上的属性名称
      * 7.通过属性名称将sql上的#{value}替换成?
      * 8.通过jdbc执行返回结果
      *
      * @data 2018/5/20 15:38
      * @by wjl
    */
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        /**
         * 在这一部分
         * 我们完全可以获取到一个不指定的注解
         * 然后判断注解类型来判断他是哪一种语句然后分别交给某一个方法去执行
         * 我们可以用 switch 也可以用一个一个 if
         * switch的好处在于不用多次获取指定注解 而是获取一个不指定的注解判断注解类型之后跳转指定的方法
         * 但是我在这里使用了if因为本次设计暂时没有考虑性能问题
         */
        MyInsert inserAnnatation = method.getDeclaredAnnotation(MyInsert.class);
        if (inserAnnatation != null){
            int affectedNum = insertSql(inserAnnatation,method,args);
            return affectedNum;
        }

        MySelect selectAnnatation = method.getDeclaredAnnotation(MySelect.class);

        /**
         * 这一步有很多写法这里我们用泛型来接收
         * 在这一步我已经将反射代码封装到Util里了
         * 如果查询到了一个单的就返回单的
         * 如果查询到了一个list返回的就是一个list,用户只需要接收自己想要的结果即可
         */
        if (selectAnnatation != null) {
            MyResultType resultType = method.getDeclaredAnnotation(MyResultType.class);
            if (resultType == null){
                throw new Exception("Return type cannot be null");
            }
            return selectSql(resultType.value(),selectAnnatation,method,args);
        }
        return null;
    }

    /**
      *
      * 写一下大致的思路步骤
      *  1.获取注解上的sql语句
      *  2.获取方法上的参数，也是获取参数上的注解
      * （这一步实际上如果没有任何注解的话我们就可以认定他是传入了一个参数要么是基本类型要么就是pojo我们只需要获取值和字段名称就可以了,
      *   这么写其实更简单，这里我就懒得多写了）
      *  3.获取到需要传入的参数数组
      *  4.替换sql中的参数
      *  5.执行sql
      *  6.判断返回结果是不是只有一个，一个的话我们就取出来返回单个，有多个的话就返回集合,当然这里也有其他更多的写法
      * @data 2018/5/20 17:41
      * @by wjl
    */
    private Object selectSql(Class<?> selectResultType,MySelect selectAnnatation, Method method, Object[] args) throws Exception {
        String oldSql = selectAnnatation.value();
        //获取方法的参数
        Parameter[] parameters = method.getParameters();
            //获取参数map
            ConcurrentHashMap<String,Object> paramMap = getParamMap(parameters,args);
            Object result = carriedOutSelectSql(selectResultType,oldSql,paramMap);
            return result;
    }

    private Object carriedOutSelectSql(Class<?> selectResultType,String oldSql, ConcurrentHashMap<String, Object> paramMap) throws IllegalAccessException, SQLException, InstantiationException {
        // 这一步就出错了，不应该是列表而应该是数组，写的时候没注意导致了下面的方法无法复用了....
        //这里就重写一个方法不去改动其他代码了....
        List<String> parameterName = SQLUtil.sqlSelectParameter(oldSql);
        //获取执行的sql
        String newSql = SQLUtil.parameQuestion(oldSql, parameterName);
        //获取参数list
        List<Object> param = selectParam(parameterName, paramMap);
        /**
         * 这里我们要获取到他的字节码交给封装好的jdbc执行操作
         * 这里我们知道mybatis返回值类型是需要填写的但是我们这里并没有操作这一步所以就写死在这里了,
         * 我们也可以通过获取用户的result注解来获取返回值对象的字节码文件
         * 这里就不再过多操作了
         */
        List<?> result = JdbcUtil.getListBean(newSql, selectResultType, param);
        if (result.size() > 1){
            return result;
        }
        return result.get(0);
    }

    private List<Object> selectParam(List<String> parameterName, ConcurrentHashMap<String, Object> paramMap) {
        List<Object> param = new ArrayList<Object>();
        for (String s : parameterName) {
            param.add(paramMap.get(s));
        }
        return param;
    }


    //这里我们执行insert语句
    private int insertSql(MyInsert inserAnnatation,Method method,Object[] args) throws Exception {

        String oldSql = inserAnnatation.value();
        Parameter[] parameters = method.getParameters();
        //获取到装有参数名称和值的map容器
        ConcurrentHashMap<String, Object> paramMap = getParamMap(parameters,args);
        /**
         * 前面的准备工作已经做完了
         * 这里用到的一个Util是我自己写的 都是一些基本的字符操作
         * 这里有一个bug,如果我在sql语句中加了空格的话就会导致截取出来的参数带有空格
         * 会影响到之后的替换和取值的进程,这里暂时不做处理
         */
        return carriedOutInsertSql(oldSql,paramMap);
    }

    //执行insert语句
    private int carriedOutInsertSql(String oldSql,ConcurrentHashMap<String, Object> paramMap) throws SQLException {
        String[] insertParameter = SQLUtil.sqlInsertParameter(oldSql);
        //在这里按照参数的位置装载我们的参数
        List<Object> param = sqlParam(insertParameter,paramMap);
        String newSql = SQLUtil.parameQuestion(oldSql, insertParameter);
        System.out.println(newSql);
        //执行sql语句
        return JdbcUtil.updateRecord(newSql, param);
    }

    //根据名称对数据的map和需要添加的参数名称
    //返回参数列表
    // 本来以为这个地方可以复用的.........
    private List<Object> sqlParam(String[] insertParameter, ConcurrentHashMap<String, Object> paramMap) {
        List<Object> param = new ArrayList<Object>();
        for (String s : insertParameter) {
            param.add(paramMap.get(s));
        }
        return param;
    }

    //获取到对应参数名称和他的值的Map容器
    private ConcurrentHashMap<String, Object> getParamMap(Parameter[] parameters,Object[] args) throws Exception {
        ConcurrentHashMap<String, Object> paramMap = new ConcurrentHashMap<>();
        for (int i = 0; i < parameters.length; i++) {
            MyParam myParam = parameters[i].getAnnotation(MyParam.class);
            if (myParam != null){
                String paramName = myParam.value();
                Object paramValue = args[i];
                paramMap.put(paramName,paramValue);
            }
        }
        if (paramMap == null){
            throw new Exception("Failed to initialize parameter container");
        }
        return paramMap;
    }
}
