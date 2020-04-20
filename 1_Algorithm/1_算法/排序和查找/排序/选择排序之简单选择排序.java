package com.lin.algorithm.sortEight;

/**
 * Created by baozang Cotter on 2018/12/7.
 * Funtion:简单选择排序
 */
public class SimpleSelectionSort {
    public static void main(String[] args){
        System.out.println(Arrs.get());

        new SimpleSelectionSort().sort(Arrs.arrs);

        System.out.println(Arrs.get());
    }

    public void sort(int[] arrs){
        //i锁定要排序的位置
        for(int i=0;i<arrs.length;i++){
            //k表示最小值下标
            int k = i;
            //从剩余数组中选择最小值的下标
            for(int j=i+1;j<arrs.length;j++){
                if(arrs[k] > arrs[j]) {
                    k=j;
                }
            }
            swap(arrs,k,i);
        }
    }

    //交换数组中两个位置的值
    public void swap(int[] arrs,int k,int i){
        if(k == i) return;
        arrs[k] = arrs[k] + arrs[i];
        arrs[i] = arrs[k] - arrs[i];
        arrs[k] = arrs[k] - arrs[i];
    }

}
