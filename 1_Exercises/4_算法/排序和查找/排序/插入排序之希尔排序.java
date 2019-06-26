package com.lin.algorithm.sortEight;

/**
 * Created by baozang Cotter on 2018/12/6.
 * Funtion:希尔排序
 */
public class ShellIS {
    public static void main(String[] args){
        System.out.println(Arrs.get());

        new ShellIS().ShellSort(Arrs.arrs);

        System.out.println(Arrs.get());
    }

    public void ShellSort(int[] arrs){
        //dk为移动窗口，将插入排序的窗口主键缩小
        int dk = arrs.length/2;
        while(dk>=1){
            sort(dk,arrs);
            dk = dk/2;
        }


    }

    public void sort(int dk,int[] arrs) {

        for(int i=dk; i<arrs.length ;i++){
            //由于前面都是排序好的，如果当a[i]不小于前一位置，则肯定不小于前面的任何一个数
            if(arrs[i-dk] > arrs[i]){
                int j;
                int x = arrs[i];  //记录当前要排序的数
                //以窗口dk长度为标准
                for(j = i-dk; j>=0 && arrs[j]>x ; j=j-dk){
                    arrs[j+dk] = arrs[j];
                }
                arrs[j+dk] = x;
            }


        }
    }
}
