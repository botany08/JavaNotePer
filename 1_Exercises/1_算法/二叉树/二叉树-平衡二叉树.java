/*
题目：	输入一棵二叉树，判断该二叉树是否是平衡二叉树。
		如果某二叉树中任意结点的左右子树的深度相差不超过1，那么它就是一棵平衡二叉树。
思路：	在遍历树的每个结点的时候，调用函数TreeDepth得到它的左右子树的深度。
		如果每个结点的左右子树的深度相差不超过1，按照定义它就是一棵平衡的二叉树。
*/
//采用后序遍历
public class Solution {
    //后续遍历时，遍历到一个节点，其左右子树已经遍历  依次自底向上判断，每个节点只需要遍历一次
    private boolean isBalanced=true;
    public boolean IsBalanced_Solution(TreeNode root) {
         
        getDepth(root);
        return isBalanced;
    }
    public int getDepth(TreeNode root){
        if(root==null)
            return 0; 
        int left=getDepth(root.left);
        int right=getDepth(root.right);
         
        if(Math.abs(left-right)>1){
            isBalanced=false;
        }
        return right>left ?right+1:left+1; 
    }
}

//利用求深度来判断，在已经不是平衡树的情况下会多计算，时间复杂度比较大
public class Solution{
   public boolean IsBalanced_Solution(TreeNode root){    
       if(root ==null)    
           return true;    
       int left = TreeDepth(root.left);    
       int right = TreeDepth(root.right);    
       int diff = left - right;    
       if(diff > 1 || diff <-1)    
           return false;    
       return IsBalanced_Solution(root.left) && IsBalanced_Solution(root.right);    
   }
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