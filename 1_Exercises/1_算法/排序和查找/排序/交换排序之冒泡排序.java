package com.lin.algorithm.sortEight;

/**
 * Created by baozang Cotter on 2018/12/7.
 */
public class BubbleSort {
    public static void main(String[] args){
    System.out.println(Arrs.get());
    new BubbleSort().sortC(Arrs.arrs);
    System.out.println(Arrs.get());

    }

    //冒泡排序-大的往下沉
    public void sortA(int[] arrs){
        int len = arrs.length;
        for(int i=0;i<len-1;i++){
            //i表示已经排序的位置
            for(int j=0;j<len-1-i;j++){
                //如果前一位大于后一位，则交换位置，下沉
                if(arrs[j] > arrs[j+1]){
                    int tmp = arrs[j];
                    arrs[j] = arrs[j+1];
                    arrs[j+1] = tmp;
                }
            }
        }
    }

    //冒泡排序-小的往上浮
    public void sortB(int[] arrs){
        int len = arrs.length;
        for(int i=0;i<len-1;i++){
            //如果后一位小于前一位，则交换位置，上浮
            for(int j=len-1;j>i;j--){
                if(arrs[j] < arrs[j-1]){
                    int tmp = arrs[j];
                    arrs[j] = arrs[j-1];
                    arrs[j-1] = tmp;
                }
            }
        }
    }

    //冒泡排序-加入位置指示
    //pos表示最后一次交换的位置
    public void sortC(int[] arrs){
        //i置为最后一个位置
        int i = arrs.length-1;
        while(i > 0){
            //重置，如果没有发生排序则默认为0
            int pos = 0;
            //如果前一位比后一位大，则交换位置
            //记录最后一次交换的位置
            for(int j=0;j<i;j++){
                if(arrs[j] > arrs[j+1]){
                    int tmp = arrs[j];
                    arrs[j] = arrs[j+1];
                    arrs[j+1] = tmp;
                    pos = j;
                }
            }
            //重置i位置，则i后都是已经排序好的
            i = pos;
        }
    }


}
