package com.lin.algorithm.sortEight;

/**
 * Created by baozang Cotter on 2018/12/7.
 * Function:归并排序
 */
public class MergeSort {
    public static void main(String[] args){
        System.out.println(Arrs.get());
        new MergeSort().sort(Arrs.arrs,0,Arrs.arrs.length-1);
        System.out.println(Arrs.get());
    }

    public void sort(int[] arrs, int left, int right){
        if(left >= right) return;
        //取出中间索引
        int center = (left + right) / 2;
        //对左边数组进行排序
        sort(arrs,left,center);
        //对右边数组进行排序
        sort(arrs,center+1,right);
        //按顺序合并左右两个数组
        merge(arrs,left,center,right);

    }

    public void merge(int[] arrs,int left,int center,int right){
        //右边数组的起始索引
        int mid = center+1;
        //临时数组-用来合并左右两个数组
        int[] tmpArr = new int[arrs.length];
        //临时数组起始索引
        int buf = left;
        //原始数组起始索引 - 用来复制临时数组到实际数组中
        int tmp = left;

        //按照顺序合并左右数组
        while(left<=center && mid<=right){
            if(arrs[left] <= arrs[mid]){
                tmpArr[buf++] = arrs[left++];
            }else {
                tmpArr[buf++] = arrs[mid++];
            }
        }

        //将左边或者右边剩下的数组复制到tmpArr中
        //实际山一般只会执行其中一个
        while(left <=  center){
            tmpArr[buf++] = arrs[left++];
        }
        while(mid <= right){
            tmpArr[buf++] = arrs[mid++];
        }

        //复制临时数组到实际数组
        while (tmp <= right){
            arrs[tmp] = tmpArr[tmp++];
        }


    }
}
