package com.lin.algorithm.search;

/**
 * Created by baozang Cotter on 2018/12/11.
 */
public class BinarySearch {
    public static void main(String[] args){
        int[] data = {12,34,13,9,34,55,99,45,32};
        System.out.println(new BinarySearch().search(data,34));
    }

    public int search(int[] data,int goal){
        int low = 0;
        int high = data.length-1;
        int mid;

        //数组无元素时，查找失败
        if(data.length == 0) return -1;

        while(low <= high){
            mid = (low+high)/2;

            if(data[mid] == goal){
                return mid;
            }else if (data[mid] > goal){
                high = mid-1;
            }else {
                low = mid +1;
            }
        }
        //查找无结果
        return -1;
    }
}
