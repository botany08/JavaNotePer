package com.lin.algorithm.tranform;

/**
 * Created by baozang Cotter on 2018/12/11.
 * Function:二进制和十进制相互转换
 */
public class Binary2Decimal {
    public static void main(String[] args){
        System.out.println(new Binary2Decimal().bin2deci(1111));
    }

    //十进制转二机制--int数存储
    //缺点：int只能存储32位的数
    public int tform1(int n){
        //位数标志
        //除2余数，放到最低位
        int t = 0;
        //余数
        int r;
        //二进制数int
        int binary = 0;
        //除2取余法
        while (n  > 0 ){
            r = n%2;
            n = n/2;
            binary += r * Math.pow(10,t);
            t++;
        }
        return binary;
    }

    //十进制转二机制--string存储
    public String tform2(int n){
        String str = "";
        while (n > 0){
            str = n%2 + str;
            n = n/2;
        }
        return str;
    }

    //十进制转二进制--位运算
    //>>>为逻辑移位符，向右移n位，高位补0
    //算数移位符，也是向右移n位，不同的是：正数高位补0，负数高位补1
    //移位符，向左移n位，低位补0
    public String tform3(int n){
        String str = "";
        for(int i=31;i>=0;i--){
            str += ((n >>> i) & 1)+"";
        }
        return str;
    }

    //十进制转二进制--调用api
    public String tform4(int n){
        return Integer.toBinaryString(n);
    }

    //二进制转换十进制--数学逻辑算法
    public int bin2deci(int n){
        //位数下标
        int p = 0;
        //余数
        int temp = 0;
        //十进制数
        int result = 0;

        //判断二进制数是否有值
        while (n > 0 ){
            temp = n % 10;
            n = n / 10;
            result += temp * Math.pow(2,p);
            p++;
        }

        return result ;

    }

}
