/*
题目描述：给定一个二叉树和其中的一个结点，请找出中序遍历顺序的下一个结点并且返回。  
注意，树中的结点不仅包含左右子结点，同时包含指向父结点的指针。  
思路：分为三种情况：  
1.处理没有父亲节点，即根节点情况,找右子树的最左孩子  
2.自己是父亲的左子树
2-1：自己有右孩子，输出右孩子
2-3：自己没有右孩子，输出父亲
3.自己是父亲的右子树分为两种情况  
3-1：自己有右孩子，输出右孩子  
3-2：在自己的父亲及上面找到一个节点是它父亲的左孩子，如果一直找不到说明他是最后的一个节点
*/
TreeLinkNode GetNext(TreeLinkNode pNode) {
	//若结点为空，返回null
	if(pNode==null)  
          return null;  
	TreeLinkNode pre = pNode.next;  
    TreeLinkNode tmp;
	//第一种情况：若父结点为空，则该结点为根结点
    if(pre == null){ 
		tmp = pNode.right;
		//当右子节点为空，返回null
        if(tmp == null)  
			return tmp;
		//取出最左孩子
        while(tmp.left!=null)  
            tmp = tmp.left;  
        return tmp;  
     }
	 //第二种情况：该结点为父结点的左子结点
     if(pNode==pre.left){
		//若没有右子结点，则返回父结点
		if(pNode.right==null)  
			return pre;
		//若有右子结点，返回右子结点
        return pNode.right;  
     }
	 //第三种情况：该结点为父结点的右子结点
     else{
		//若存在右子结点，返回右子结点
		if(pNode.right!=null)  
			return pNode.right;
		//若没有右子结点，寻找父结点（祖父结点）A，如果A是其父结点的左子节点，
		//则返回其父结点，如果没有则说明该结点为最后一个结点，返回null
        while(pre.next!=null){             
            tmp = pre.next;                
            if(tmp.left == pre)            
				return tmp;  
            pre = tmp;                      
         }  
     }  
     return null;  
}  