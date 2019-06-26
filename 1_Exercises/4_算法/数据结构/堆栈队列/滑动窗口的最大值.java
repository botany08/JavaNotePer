/*
题目：	给定一个数组和滑动窗口的大小，找出所有滑动窗口里数值的最大值。
		例如，如果输入数组{2,3,4,2,6,2,5,1}及滑动窗口的大小3，那么一共存在6个滑动窗口，
		他们的最大值分别为{4,4,6,6,6,5}； 针对数组{2,3,4,2,6,2,5,1}的滑动窗口有以下6个： 
		{[2,3,4],2,6,2,5,1}， {2,[3,4,2],6,2,5,1}， {2,3,[4,2,6],2,5,1}， 
		{2,3,4,[2,6,2],5,1}， {2,3,4,2,[6,2,5],1}， {2,3,4,2,6,[2,5,1]}。
思路：	用一个双端队列，队列第一个位置保存当前窗口的最大值，当窗口滑动一次
		1.判断当前最大值是否过期
		2.新增加的值从队尾开始比较，把所有比他小的值丢掉
*/
public class Solution {
   public ArrayList<Integer> maxInWindows(int [] num, int size)
    {
        ArrayList<Integer> res = new ArrayList<>();
        if(size == 0) return res;
        int begin; 
        ArrayDeque<Integer> q = new ArrayDeque<>();
		//循环输入数组
        for(int i = 0; i < num.length; i++){
			//begin是用于保存当前窗口的第一个值在原始数组中的【下标】
            begin = i - size + 1;
			//添加双端队列，双端队列保存的是下标
            if(q.isEmpty())
                q.add(i);
			//当窗口第一个元素的下标大于队列中第一个值，则删除，保持窗口的值
            else if(begin > q.peekFirst())
                q.pollFirst();
         	//当窗口队列不为空  且  
            while((!q.isEmpty()) && num[q.peekLast()] <= num[i])
                q.pollLast();
            q.add(i);
			//第一个窗口  
            if(begin >= 0)
                res.add(num[q.peekFirst()]);
        }
        return res;
    }
}