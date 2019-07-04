package com.lin.algorithm.tranform;

/**
 * Created by baozang Cotter on 2018/12/11.
 * Fuction:2进制和16进制和8进制相互转换
 */
public class Binary2HexOct {
    public static void main(String[] args){
        System.out.println(new Binary2HexOct().bin2HexOct(31));
    }

    //16进制或8进制转换为2进制
    public String hexOct2Bin(String n,int radix){
        int ra = 0;
        if(radix == 8) ra = 3;
        if(radix == 16 ) ra = 4;

        String str = "123456789abcdef";
        int[] a = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
        //接收字符
        StringBuilder sb = new StringBuilder();
        //输入的值
        char[] nArr = n.toCharArray();
        for(int i=nArr.length-1;i>=0;i--){
            int r = a[str.indexOf(nArr[i])];
            //填充0
            StringBuilder sb2 = new StringBuilder(Integer.toBinaryString(r)).reverse();
            for(int j=sb2.length()+1;j<=ra;j++){
                sb2.append("0");
            }
            sb = sb.append(sb2);
        }

        return sb.reverse().toString();
    }

    //2进制转换为16进制
    public String bin2HexOct(int n){
        StringBuilder sb = new StringBuilder();
        //int有32位
        for(int i=0;i<8;i++){
            int temp = n & 15;
            if(temp > 9 ){
                sb.append((char)(temp-10+'A'));
            }else {
                sb.append(temp);
            }
            n = n >>> 4;
        }
        return sb.reverse().toString();
    }

}
