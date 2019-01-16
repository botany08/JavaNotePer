/*
题目：
输入某二叉树的前序遍历和中序遍历的结果，请重建出该二叉树。
假设输入的前序遍历和中序遍历的结果中都不含重复的数字。
例如输入前序遍历序列{1,2,4,7,3,5,6,8}和中序遍历序列{4,7,2,1,5,3,8,6}，
则重建二叉树并返回。
*/
public class Solution {
    public TreeNode reConstructBinaryTree(int [] pre,int [] in) {
        TreeNode root=reConstructBinaryTree(pre,0,pre.length-1,in,0,in.length-1);
        return root;
    }
    //前序遍历{1,2,4,7,3,5,6,8}和中序遍历序列{4,7,2,1,5,3,8,6}
    private TreeNode reConstructBinaryTree(int [] pre,int startPre,int endPre,int [] in,int startIn,int endIn) {
        //防止递归溢出，当左面没有子树或者右面没有子树时，返回null
        if(startPre>endPre||startIn>endIn)
            return null;
		//取前序遍历的第一个节点
        TreeNode root=new TreeNode(pre[startPre]);
        //循环中序遍历列
        for(int i=startIn;i<=endIn;i++){
			//取中序遍历列中的第一个节点
            if(in[i]==pre[startPre]){
				//i代表根结点，i-startIn表示根左面的节点
                root.left=reConstructBinaryTree(pre,startPre+1,startPre+i-startIn,in,startIn,i-1);
                root.right=reConstructBinaryTree(pre,i-startIn+startPre+1,endPre,in,i+1,endIn);
                break;       
            }
        }         
        return root;
    }
}