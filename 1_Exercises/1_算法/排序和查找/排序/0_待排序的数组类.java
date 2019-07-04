package com.lin.algorithm.sortEight;

/**
 * Created by baozang Cotter on 2018/12/6.
 */
public class Arrs {
    public static int[] arrs = {49,38,65,97,76,13,27,49};

    public static Arrs a = new Arrs();

    public static String get(){
       return a.toString();
    }

    @Override
    public  String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for(int i=0; i<arrs.length; i++){
            sb.append(arrs[i]+",");
        }
        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append("}");
        return sb.toString();
    }
}
