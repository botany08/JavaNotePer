/*
题目：	从上往下打印出二叉树的每个结点，同一层的结点按照从左向右的顺序打印。不用打印成多行。
思路：	每一次打印一个结点的时候，如果该结点有子结点，则把该结点的子结点放到一个队列的末尾。
		接下来到队列的头部取出最早进入队列的结点，重复前面的打印操作，直到队列中所有的结点都被打印出来为止。
*/
package swordForOffer;  
  
import java.util.LinkedList;  
import java.util.Queue;  
import utils.BinaryTreeNode;  

/**
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
    public ArrayList<Integer> PrintFromTopToBottom(TreeNode root){
		//输出数组
		ArrayList<Integer> arrayList = new ArrayList<Integer>();
		//当根结点为空时，返回null
        if(root == null)  
            return null;
		//打印队列
        Queue<TreeNode> queue = new LinkedList<TreeNode>();
		//初始化打印队列
        queue.add(root);  
        while(!queue.isEmpty()){
			//取出队首元素
            TreeNode node = queue.poll();  
            arrayList.add(node.val);  
            if(node.left != null)  
                queue.add(node.left);  
            if(node.right != null)  
                queue.add(node.right);  
        }  
    }  
}  