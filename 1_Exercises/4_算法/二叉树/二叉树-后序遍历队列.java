/*
题目：	输入一个整数数组，判断该数组是不是某二叉搜索树的后序遍历的结果。
		如果是则输出Yes,否则输出No。假设输入的数组的任意两个数字都互不相同。
思路：	主要在于序列为空的时候，不是后序遍历，返回的是false，但是在递归的时候，
		递归基要写true，因为如果写false，那么递归的所有结果都是false。
*/
class Solution {
	public static boolean verifySequenceOfBST(int[] seq){  
        if(seq==null || seq.length==0)  
            return false;  
        return verifySequenceOfBST(seq,0,seq.length-1);  
    }  
    private static boolean verifySequenceOfBST(int[] seq, int start, int end){   
        //递归退出条件
		if(start>end)  
            return true;
		//取出根结点
        int root = seq[end];
		//第一个元素为left
        int i = start;
		//取出根结点的右边第一个左子结点
        while(i <= end-1){
			//当为
            if(seq[i]>root)  
                break;  
            i++;  
        }
		//循环右子树，若右子树中有结点小于根结点，则不是后序遍历
        int j = i;  
        while(j<=end-1){  
            if(seq[j]<root)  
                return false;  
            j++;  
        }
		//循环根结点的左子树和右子树
        boolean left = true;  
        left = verifySequenceOfBST(seq,start,i-1);  
        boolean right = true;  
        right = verifySequenceOfBST(seq,i,end-1);  
        return left && right;  
          
    }  
}