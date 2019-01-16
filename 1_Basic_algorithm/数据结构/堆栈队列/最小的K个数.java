/*
题目：输入n个整数，找出其中最小的K个数。例如输入4,5,1,6,2,7,3,8这8个数字，则最小的4个数字是1,2,3,4,。
思路：利用最大堆来解决，对应堆结构的是优先级队列。
*/
public class Solution {
	public ArrayList<Integer> GetLeastNumbers_Solution(int[] input, int k) {
		//输出数组
		ArrayList<Integer> result = new ArrayList<Integer>();
		int length = input.length;
		//当k大于输入长度或等于0，返回空
		if(k > length || k == 0){
	    	return result;
	    }
		//声明一个大小为k的最大堆，传入一个比较器
        PriorityQueue<Integer> maxHeap = new PriorityQueue<Integer>(k, new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
				//当o2>01时返回正整数
                return o2.compareTo(o1);
            }
        });
		//循环输入数组
        for (int i = 0; i < length; i++) {
			//当队列还没满时，一直插入
            if (maxHeap.size() != k) {
				//将指定元素插入队尾
                maxHeap.offer(input[i]);
			//如果插入元素比队首小，删除队首插入新元素，最大堆
            } else if (maxHeap.peek() > input[i]) {//peek，检索队首第一个元素
				//检索并删除队首元素
                Integer temp = maxHeap.poll();
                temp = null;
                maxHeap.offer(input[i]);
            }
        }
		//将最大堆中的数依次输出，就是最小的k个数
        for (Integer integer : maxHeap) {
            result.add(integer);
        }
        return result;
    }
}