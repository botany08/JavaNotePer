package com.lin.algorithm.sortEight;

/**
 * Created by baozang Cotter on 2018/12/7.
 */
public class QuickSort {
    public static void main(String[] args){
    System.out.println(Arrs.get());

    new QuickSort().sort(Arrs.arrs,0,Arrs.arrs.length-1);

    System.out.println(Arrs.get());
    }

    public void sort(int[] arrs,int low,int high){
        if(low < high){
            //获取基准值
            int middle = getMiddle(arrs,low,high);
            //对左边进行快速排序
            sort(arrs,0,middle-1);
            //对右边进行快速排序
            sort(arrs,middle+1,high);
        }
    }

    public int getMiddle(int[] arrs,int low,int high){
        //定义基准值
        int key = arrs[low];
        while(low < high){
            //大于基准值，往左移动
            while(low < high && arrs[high] >= key){
                high--;
            }
            //将小于基准值的数放在右边
            arrs[low] = arrs[high];
            //小于基准值，往右移动
            while(low < high && arrs[low] <= key){
                low++;
            }
            //将大于基准值的数放在左边
            arrs[high] = arrs[low];
        }
        //此时low==high，将基准值放在这里
        arrs[low] = key;
        //返回基准值所在位置
        return low;
    }
}
