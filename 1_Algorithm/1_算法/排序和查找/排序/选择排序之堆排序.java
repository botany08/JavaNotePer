package com.lin.algorithm.sortEight;


/**
 * Created by baozang Cotter on 2018/12/7.
 * Funtion:堆排序
 */
public class HeapSort {
    public static void main(String[] args){
        System.out.println(Arrs.get());

        HeapSort heapSort = new HeapSort();
        //将堆顶元素放到最后一位
        //剩下其他元素进行重建最小堆
        for(int i=0;i<Arrs.arrs.length;i++){
            int last = Arrs.arrs.length-1-i;
            heapSort.createHeap(Arrs.arrs,last);
            heapSort.swap(Arrs.arrs,0,last);
        }

        System.out.println(Arrs.get());
    }

    public void createHeap(int[] arrs , int last){
        //获取最后一个双亲节点
        //遍历所有双亲节点
        for(int i= (last-1)/2; i>=0 ;i--){
            int parent = i;
            //获取左节点
            while(2*parent+1 <= last) {
                //假设左节点为最小值
                int smaller = 2*parent+1;
                //判断是否存在右节点
                if(smaller < last){
                    //比较左右节点，选择最小的
                    if(arrs[smaller] > arrs[smaller+1])
                        smaller = smaller+1;
                }
                //比较双亲节点和子节点，选择最小的
                //向下继续构建最小堆
                if(arrs[smaller] < arrs[parent]){
                    swap(arrs,parent,smaller);
                    //传递到子节点，继续构建最小堆
                    parent = smaller;
                }else {
                    break;
                }
            }
        }
    }

    public void swap(int[] arrs,int a,int b){
        if(a == b) return;
        arrs[a] = arrs[a] + arrs[b];
        arrs[b] = arrs[a] - arrs[b];
        arrs[a] = arrs[a] - arrs[b];
    }
}
