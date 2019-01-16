/*
题目：	请实现两个函数，分别用来序列化和反序列化二叉树
思路：	如果二叉树的序列化是从根节点开始，那么对应的而反序列化也是从根节点开始的。
		因此可以使用二叉树的前序遍历来序列化二叉树，当前序遍历碰到null值是，使用“#”表示，每一个节点的数值之间用“，”隔开。
*/
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
    public int index = -1;  // 节点在序列中的索引
	//序列化-将对象转换为字符
    String Serialize(TreeNode root) {
        StringBuffer s = new StringBuffer();
		//若根结点为null
        if (root == null) {
            s.append("#,");
            return s.toString();
        }
        s.append(root.val + ",");
        s.append(Serialize(root.left));
        s.append(Serialize(root.right));
        return s.toString();
    }
	//反序列化-将字符转换为对象
    TreeNode Deserialize(String str) {
        index++;
        int length = str.length();
		//如果索引大于长度，则为null
        if (index >= length) {
            return null;
        }
		//分割形成string[]
        String[] nodeSeq = str.split(",");
        TreeNode pNode = null;
		//当结点不为null，利用索引来赋值
        if (!nodeSeq[index].equals("#")) {
            pNode = new TreeNode(Integer.valueOf(nodeSeq[index]));
            pNode.left = Deserialize(str);
            pNode.right = Deserialize(str);
        }
		//若为null就直接返回该空结点
        return pNode;
    }
}