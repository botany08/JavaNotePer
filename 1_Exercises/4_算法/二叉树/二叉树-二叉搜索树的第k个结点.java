/*
题目：	给定一棵二叉搜索树，请找出其中的第K大的结点。例如下图中的二叉树，按加点数值大小顺序第三个结点的值是4。
思路：	二叉搜索树的一个重要性质就是它的中序遍历是排序的，因此这道题目只需要用中序遍历算法遍历一棵二叉搜索树，
		就很容易找出它的第K大结点。
*/
class SolutionMethod1{
	public static int flag;
    public TreeNode KthNode(TreeNode pRoot,int k){
		//若根结点为null或k为0，返回null
        if(pRoot == null || k == 0){  
            return null;  
        }
        return inorderTraversalCore(pRoot,k);  
    }  
      
    public TreeNode inorderTraversalCore(TreeNode root,int k){
		//目标结点
        TreeNode target = null;  
        //找到最左边的那个结点  
        if(root.left != null )  
            target = inorderTraversalCore(root.left,flag);  
        //若没有左子结点  
        if(target == null){
			//当k为1时，就是本身，否则消耗掉1，找右子树
            if(flag == 1)  
                target = root;  
            flag--;  
        }
        if(target == null && root.right != null)  
            target = inorderTraversalCore(root.right, flag;  
          
        return target;  
    }  
}  