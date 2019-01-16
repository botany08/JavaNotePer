/*
题目：	用两个栈来实现一个队列，完成队列的Push和Pop操作。 队列中的元素为int类型。
思路：	两个栈，有两个端口，那么肯定一个是用来入队的，另一个用来出队的。
		同时，由于栈是先进后出的，那么经过两次的入栈则会变为先进先出，
		即，第一次先进后出，第二次后进先出，两个加起来就变成了先进先出。
*/
public class Solution{
	private static Stack<Integer> stack1=new Stack<Integer>();
    private static Stack<Integer> stack2=new Stack<Integer>();
	/*
	加入队列中的元素只加入到栈1中
	*/
    public static void push(int item){
        stack1.push(item);
    }
    /*
     * 删除一个元素时，检查栈2是否为空，栈2不为空则弹出栈2栈顶元素
     * 栈2为空，则把栈1中的元素全部弹出、压入到栈2中，然后从栈2栈顶弹出元素
     */
    public static int pop(){
        if(!stack2.empty())
            return stack2.pop();
        else{
            if(stack1.empty())
                throw new RuntimeException("队列为空");
            while(!stack1.empty()){
                Integer item=stack1.pop();
                stack2.push(item);
            }
            return stack2.pop();
        }
	}
}