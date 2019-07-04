package com.lin.algorithm.tranform;

/**
 * Created by baozang Cotter on 2018/12/11.
 * Function:十进制和十六进制相互转换
 */
public class Decimal2Hex {
    public static void main(String[] args){
        new Decimal2Hex().hex2Dec2("1f");
    }

    //十进制转换为十六进制 -- 自带api转换
    public String dec2Hex1(int n){
        return Integer.toHexString(n);
    }

    //十进制转换为十六进制 -- 数学逻辑
    //除以16的余数
    public String dec2Hex2(int n){
        StringBuilder sb = new StringBuilder();
        char[] hexArr = {'1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};

        while(n > 0){
           sb = sb.append(hexArr[n%16-1]);
            n = n / 16;
        }

        return sb.reverse().toString();
    }

    //16进制转换为10进制--自带的api接口
    public int hex2Dec1(String n){
        return Integer.parseInt(n,16);
    }

    //16进制转换为10进制--自己写
    public int hex2Dec2(String n){
        String str = "123456789abcdef";
        int[] dec = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
        //位数
        int p = 0;
        //接收的十进制
        int result = 0;

        //16进制数
        char[] nArr = n.toCharArray();
        for(int i=nArr.length-1;i>=0;i--){
            int r = str.indexOf(nArr[i]);
            result += dec[r] * Math.pow(16,p);
            p++;
        }
        return result;
    }

}
