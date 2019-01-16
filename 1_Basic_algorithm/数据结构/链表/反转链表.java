/*
题目：输入一个链表，反转链表后，输出链表的所有元素。
思路：
*/
链接：https://www.nowcoder.com/questionTerminal/75e878df47f24fdc9dc3e400ec6058ca
来源：牛客网

public class Solution {
    public ListNode ReverseList(ListNode head) {
       	//head为当前节点，如果当前节点为空的话，那就什么也不做，直接返回null；
        if(head==null)
            return null;
        ListNode pre = null;
        ListNode next = null;
        while(head!=null){
            //保存head的下一个结点
            next = head.next;
            //反转head的指针指向pre，pre和head往右移动一位
            head.next = pre;
            pre = head;
            head = next;
        }
        //如果head为null的时候，pre就为最后一个节点了，但是链表已经反转完毕，pre就是反转后链表的第一个节点
        //直接输出pre就是我们想要得到的反转后的链表
        return pre;
    }
}