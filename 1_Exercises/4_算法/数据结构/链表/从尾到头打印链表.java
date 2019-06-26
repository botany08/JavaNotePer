/*
题目：输入一个链表，从尾到头打印链表每个节点的值。
思路：可以利用栈，栈是先进后出，先进栈再出栈就是倒序输出。
*/
public ArrayList<Integer> printListFromTailToHead(ListNode listNode){
	Stack<ListNode> stack=new Stack<ListNode>();
	ArrayList<Integer>	arrayList = new ArrayList<>();
	while(listNode!=null)
	{
		stack.push(listNode);
		listNode=listNode.next;
	}
	while(!stack.isEmpty())
	{
		arrayList.add(stack.pop().val)
	}
	return arrayList;
}