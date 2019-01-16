/*
题目：	输入一颗二叉树和一个整数，打印出二叉树中结点值的和为输入整数的所有路径。
		路径定义为从树的根结点开始往下一直到叶结点所经过的结点形成一条路径。
思路：	当用前序遍历的方式访问到某一节点时，我们把该结点添加到路径上，并累加该结点的值。
		如果该结点为叶结点并且路径中结点值的和刚好为输入的整数，则当前的路径符合要求，我们把它打印出来。
		如果当前的结点不是叶结点，则继续访问它的子节点。
*/
public class Solution {
	//listAll表示全部路径的集合，list表示一条路径
    private ArrayList<ArrayList<Integer>> listAll = new ArrayList<ArrayList<Integer>>();
    private ArrayList<Integer> list = new ArrayList<Integer>();

    public ArrayList<ArrayList<Integer>> FindPath(TreeNode root,int target) {
        //若根结点为null，直接返回空数组
		if(root == null) 
			return listAll;
		//添加本结点的值
        list.add(root.val);
		//消耗target的值
        target -= root.val;
		//当到达叶子结点且刚好等于这个整数值，将路径保存到最终结果
        if(target == 0 && root.left == null && root.right == null)
            listAll.add(new ArrayList<Integer>(list));
		//当添加到这个结点时仍然不满足，则回退一位
        FindPath(root.left, target);
        FindPath(root.right, target);
        list.remove(list.size()-1);
        return listAll;
    }
}