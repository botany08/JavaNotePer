/*
题目：输入一棵二叉树，求该树的深度。从根结点到叶结点依次经过的结点（含根、叶结点）形成树的一条路径，最长路径的长度为树的深度。
思路：
*/
public class Solution {
    public int TreeDepth(TreeNode pRoot){
		//判空
        if(pRoot == null){
            return 0;
        }
        int left = TreeDepth(pRoot.left);
        int right = TreeDepth(pRoot.right);
        return Math.max(left, right) + 1;
    }
}
