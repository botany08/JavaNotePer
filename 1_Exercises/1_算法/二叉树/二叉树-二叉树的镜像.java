/*
题目：	操作给定的二叉树，将其变换为源二叉树的镜像。
思路：	所以我们可以采用前序遍历的方法进行操作，每当遇到一个结点的时候，
		首先判断其是否有左右孩子（有其中之一即可），如果有的话，就交换其左右孩子，
		然后继续遍历其左右子树，只要不为空就继续交换其左右孩子节点
		（在交换具有孩子结点的结点的时候，其孩子结点也一并被交换了）。
*/
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
    public void Mirror(TreeNode root) {
		//当结点为null，返回null
        if(root == null) 
			return;
		//当为叶子结点时，返回null
		if(root.left==null&&root.right==null)
			return;
		//交换左右子结点
        TreeNode tempNode = root.left;
        root.left = root.right;
        root.right = tempNode;
		//当左结点不为null，交换左右子结点，先左后右（前序遍历）
        if(root.left!=null)
            Mirror(root.left);
        if(root.right!=null)
            Mirror(root.right);
    }
}