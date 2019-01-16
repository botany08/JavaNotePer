/*
题目：	从上到下按层打印二叉树，同一层结点从左至右输出。每一层输出一行。
思路：	按层次打印，需要一个打印数量标志位和最大数量标志位。
*/
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/*
public class TreeNode {
    int val = 0;
    TreeNode left = null;
    TreeNode right = null;

    public TreeNode(int val) {
        this.val = val;

    }

}
*/
public class Solution {
    public ArrayList<ArrayList<Integer> > Print(TreeNode pRoot) {
		//输出数组
        ArrayList<ArrayList<Integer>> resultList = new ArrayList<ArrayList<Integer>>();
		//当根结点为空，返回空的打印数组
        if(pRoot==null) 
			return resultList;
        //打印队列
        Queue<TreeNode> nodeQueue = new LinkedList<TreeNode>();
        int nextNum =0; //本层已经打印的数量
        int toBePrint = 1; //本层需要打印的总数量
        nodeQueue.add(pRoot);
		
		//单独一层的输出数组
        ArrayList<Integer> result = new ArrayList<Integer>();
		
		//当打印队列不为空
        while(!nodeQueue.isEmpty()){
			//poll()为取出并删除队首元素
            TreeNode curNode = nodeQueue.poll();
            result.add(curNode.val);
            nextNum++;
			//打印一个本层结点，将该结点的子结点按照先左后右添加进队列
            if(curNode.left!=null) 
				nodeQueue.add(curNode.left);
            if(curNode.right!=null) 
				nodeQueue.add(curNode.right);
            //判断本层是否打印完毕
            if(nextNum == toBePrint){
                resultList.add(result);
				//当本层打印完，队列中就是下一层结点的数目
                toBePrint = nodeQueue.size();
                nextNum = 0;
				//清空单独一层的输出数组
                result = new ArrayList<Integer>();
            }
        }
        return resultList;
    }
	
}