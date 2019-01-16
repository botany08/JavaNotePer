package com.lin.algorithm.tranform;

/**
 * Created by baozang Cotter on 2018/12/11.
 * Fuction:八进制和十进制相互转换
 */
public class Octal2Decimal {
    public static void main(String[] args){
        System.out.println(new Octal2Decimal().oct2Dec2(144));
    }

    //十进制转换为八进制--自带api
    public String dec2Oct1(int n){
        return Integer.toOctalString(10);
    }

    //十进制转换为八进制--数学逻辑
    //除以8
    public String dec2Oct2(int n){
        StringBuilder sb = new StringBuilder();
        //利用String来接收8进制的值
        while (n > 0){
            int r = n % 8;
            sb.append(r+"");
            n = n /8;
        }

        return sb.reverse().toString();
    }

    //八进制转换为十进制--自带api
    public int oct2Dec1(int n){
        String str = n+"";
        return Integer.parseInt(str,8);
    }

    //八进制转换为十进制--数学逻辑
    //逐位相加
    public int oct2Dec2(int n){
        //返回结果接收
        int result = 0;
        //位数
        int p = 0;

        //计算十进制
        while (n > 0){
            int tmp = n % 10;
            result += tmp * Math.pow(8,p);
            n = n /10;
            p++;
        }

        return result;
    }
}
