/*
题目：	请实现一个函数按照之字形打印二叉树，即第一行按照从左到右的顺序打印，
		第二层按照从右至左的顺序打印，第三行按照从左到右的顺序打印，其他行以此类推。
思路：	之字形打印其实与上一个按层次打印基本相同，唯一不同的是每一层的顺序不一样，
		所以在这里可以定义一个变量，打印一层之后将其与1异或，作为打印顺序的标志位。
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
        ArrayList<ArrayList<Integer>> resultList = new ArrayList<ArrayList<Integer>>();
		//当根结点为空，返回空的打印数组
        if(pRoot==null) 
			return resultList;
        //创建一个队列用于保存需要打印的结点
        Queue<TreeNode> nodeQueue = new LinkedList<TreeNode>();
        int nextNum =0; //本层已经打印的数量
        int toBePrint = 1; //本层需要打印的总数量
        int isLeftToRight = 0;//用来判断方向
        nodeQueue.add(pRoot);
		
		//单独一层的打印数组
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
				//判断顺序输出或者逆序输出
                if(isLeftToRight==1){
                    resultList.add(reverse(result));
                }else{
                    resultList.add(result);
                }
				//当本层打印完，队列中就是下一层结点的数目
                toBePrint = nodeQueue.size();
                nextNum = 0;
                result = new ArrayList<Integer>();
				//异或11=0 00=0 10=1 01=1
                isLeftToRight = isLeftToRight^1;
            }
        }
        return resultList;
    }
	//反转数组元素
    private ArrayList<Integer> reverse(ArrayList<Integer> result){
        ArrayList<Integer> reverseResult = new ArrayList<Integer>();
        for(int i=result.size()-1;i>=0;i--){
            reverseResult.add(result.get(i));
        }
        return reverseResult;
    }

}