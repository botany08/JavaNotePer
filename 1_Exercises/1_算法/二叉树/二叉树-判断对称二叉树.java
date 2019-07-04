/*
题目：	请实现一个函数，用来判断一颗二叉树是不是对称的。
		注意，如果一个二叉树同此二叉树的镜像是同样的，定义其为对称的。
思路：	想一下打印输出某二叉树的镜像，实现的思路是：采用层序遍历的思路对每一个遍历的节点，
		如果其有孩子节点，那么就交换两者。直到遍历的节点没有孩子节点为止，然而此题是对二叉树木镜像的判断，
		明显是更简单的，只需要进行两个判断：对节点的左孩子与其兄弟节点右孩子的判断以及对节点右孩子与其兄弟节点左孩子的判断。
		这样就完成对对一棵二叉树是否对称的判断。
*/
public class Solution {
	//参数为根结点
    boolean isSymmetrical(TreeNode pRoot)
    {
		//若根结点为空，则是对称的二叉树
        if(pRoot==null) 
            return true; 
        return isSymmetricalCore(pRoot.left,pRoot.right);

    }
    private boolean isSymmetricalCore(TreeNode left,TreeNode right){
		//当左子结点为空且右子结点为空，则为true
        if (left == null && right == null) return true;
		//当左右子结点只有一个为空时，则为false
        if (left == null || right == null) return false;
		//当左子结点的值等于右子结点的值
        if (left.val == right.val)
            return isSymmetricalCore(left.right, right.left)
                    && isSymmetricalCore(left.left, right.right);
        return false;  
    }
}