package com.example.administrator.myapplication;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tool {
    public static String[] getNumber(String str) {
        // 需要取整数和小数的字符串
        // 控制正则表达式的匹配行为的参数(小数)
        String strs[] = str.split(",");
        for(int i = 0;i < strs.length;i++) {
            Pattern p = Pattern.compile("(\\d+\\.\\d+)");
            //Matcher类的构造方法也是私有的,不能随意创建,只能通过Pattern.matcher(CharSequence input)方法得到该类的实例.
            Matcher m = p.matcher(strs[i]);
            //m.find用来判断该字符串中是否含有与"(\\d+\\.\\d+)"相匹配的子串
            if (m.find()) {
                //如果有相匹配的,则判断是否为null操作
                //group()中的参数：0表示匹配整个正则，1表示匹配第一个括号的正则,2表示匹配第二个正则,在这只有一个括号,即1和0是一样的
                strs[i] = m.group(1) == null ? "" : m.group(1);
            } else {
                //如果匹配不到小数，就进行整数匹配
                p = Pattern.compile("(\\d+)");
                m = p.matcher(strs[i]);
                if (m.find()) {
                    //如果有整数相匹配
                    strs[i] = m.group(1) == null ? "" : m.group(1);
                } else {
                    //如果没有小数和整数相匹配,即字符串中没有整数和小数，就设为空
                    strs[i] = "";
                }
            }
        }
        return strs;
    }


    public static String[] invert_strs(String[] strs){
        String[] change_strs = strs;
        String temp;
        for(int start = 0,end = change_strs.length-1;start<end;start++,end--){
            temp = change_strs[start];
            change_strs[start] = change_strs[end];
            change_strs[end] = temp;
        }
        return change_strs;
    }
}
