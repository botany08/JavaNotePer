package com.lin.algorithm.sortEight;

/**
 * Created by baozang Cotter on 2018/12/6.
 * function:直接插入排序
 */
public class StraightIS {
    public static void main(String[] args){
        System.out.println(Arrs.get());

        new StraightIS().sortA(Arrs.arrs);

        System.out.println(Arrs.get());

    }

    //正序-从小到大-从左往右排
    public void sortA(int[] arrs){
        //头部第一个当做已经排好序的
        //再把后面的元素逐个插入到已排好序的数组中去
        for(int i=1; i<arrs.length;i++){
            int j;
            int x = arrs[i];
            for(j=i;j>0 && arrs[j-1]>x ; j--){
                arrs[j] = arrs[j-1];
            }
            arrs[j] = x;
        }
    }

    //正序-从小到大-从右往左排
    public void sortC(int[] arrs){
        for(int x=arrs.length-2; x>=0; x--){
            int num = arrs[x];
            int j;
            for(j=x; j<arrs.length-1 && num<arrs[j+1]; j++ ){
                arrs[j] = arrs[j+1];
            }
            arrs[j] = num;
            System.out.println(Arrs.get());
        }
    }

    //倒序-从大到小
    public void sortB(int[] arrs){
        for(int x=1; x<arrs.length; x++){
            int num = arrs[x];
            int j;
            for(j=x; j>0 && num > arrs[j-1]; j--){
                arrs[j] = arrs[j-1];
            }
            arrs[j] = num;
        }
    }
}
