package com.wjl.mybatis.util;

import java.util.ArrayList;
import java.util.List;

public class SQLUtil {

    /**
      *获取Insert语句后面Values参数信息
      * @data 2018/5/17 9:53
      * @by wjl
    */
    public static String[] sqlInsertParameter(String sql){
        int startIndex = sql.indexOf("values");
        int endIndex = sql.length();
        String subString = sql.substring(startIndex + 6 ,endIndex).replace("(","").replace(")","").replace("#{","").replace("}","");
        String[] split = subString.split(",");
        return  split;
    }

    /**
     *获取slect 后面where语句
     * @data 2018/5/17 10:03
     * @by wjl
     */
    public static List<String> sqlSelectParameter(String sql){
        int startIndex = sql.indexOf("where");
        int endIndex = sql.length();
        String subString = sql.substring(startIndex + 5,endIndex);
        String[] split = subString.split("and");
        List<String> listArr = new ArrayList<String>();
        for (String string : split) {
            String[] sp2 = string.split("=");
            listArr.add(sp2[0].trim());
        }
        return listArr;
    }

    /**
     *将SQL语句的参数替换为?
     * @data 2018/5/17 10:11
     * @by wjl
     */
    public static String parameQuestion(String sql,String[] parameterName){
        for (int i = 0; i < parameterName.length; i++) {
            String string = parameterName[i];
            System.out.println(string);
            sql = sql.replace("#{" + string + "}","?");
            System.out.println(sql);
        }
        return sql;
    }

    public static String parameQuestion(String sql,List<String> parameterName){
        for (int i = 0; i < parameterName.size(); i++) {
            String string = parameterName.get(i);
            sql = sql.replace("#{"+string+"}","?");
        }
        return sql;
    }

}
