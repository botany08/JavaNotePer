## LinkedList

### 1.基础概念

- LinkedList同时实现了List接口和Deque接口，可以作为顺序容器(List)，队列(Queue)或者栈(Stack)使用。
- 需要使用栈或者队列时，可以使用LinkedList。因为Java官方已经声明不建议使用Stack类，另一方面Java里根本没有一个叫做Queue的类(Queue是接口)。
- 关于栈或队列，现在的首选是ArrayDeque，有着比LinkedList(当作栈或队列使用时)有着更好的性能。

### 2.特点以及时间复杂度

- LinkedList内部是用双向链表实现的，维护了长度、头节点和尾节点。

- 适用场景：列表长度未知，添加、删除操作比较多，经常从两端进行操作，而按照索引位置访问相对比较少

- 为追求效率LinkedList没有实现同步(synchronized)，如果需要多个线程并发访问，可以先采用Collections.synchronizedList()方法对其进行包装。  

- 增删改查的时间复杂度

  | 方法                                | 作用                                 | 时间复杂度 |
  | ----------------------------------- | ------------------------------------ | ---------- |
  | get(int index)                      | 根据索引查询元素,二分法查找,需要遍历 | O(n/2)     |
  | contains(Object o)                  | 根据内容查找元素,需要遍历            | O(n)       |
  | add(E e)/addLast(E e)/addFirst(E e) | 添加元素到头部或尾部                 | O(1)       |
  | add(int index, E element)           | 添加元素到指定索引位置,需要遍历      | O(n)       |
  | remove()/removeFirst()/removeLast() | 从头部或尾部删除元素                 | O(1)       |
  | remove(int index)/remove(Object o)  | 删除指定索引或内容的元素             | O(n)       |

### 3.实现原理

- 底层通过双向链表实现, 是双向链表不是双向循环链表。
- 难点主要是插入和删除元素时双向链表的维护过程，双向链表的每个节点用内部类Node表示。
- LinkedList通过first和last引用分别指向链表的第一个和最后一个元素。当链表为空的时候first和last都指向null。 

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/LinkedList底层实现.png)

### 4.链表的构造

```java
/**
* 链表类的组成
* 1. 一个链表对象只持有头部/尾部节点,以及长度属性.
* 2. 遍历一个链表,只能通过头部节点去寻找下一个节点.
* 3. 获取不到node对象,link只能对头尾进行操作,或者通过索引进行操作.
* 4. 链表的头节点,prev指向null. 尾节点,next指向null. 如果只存在一个元素,prev和next都指向null.
**/
public class LinkedList<E> extends AbstractSequentialList<E>
    implements List<E>, Deque<E>, Cloneable, java.io.Serializable {
	// 链表的长度
	transient int size = 0;
	// 链表的头部节点
	transient Node<E> first;
	// 链表的尾部节点
	transient Node<E> last;
}

// 默认构造方法
public LinkedList() {}

// 利用 集合C 构造一个LinkedList
public LinkedList(Collection<? extends E> c) {
    this();
    addAll(c);
}


// node节点类,LinkedList中的关键实现
private static class Node<E> {
    // 每个Node都有一个next，一个prev，一个当前元素item
    E item;
    Node<E> next;
    Node<E> prev;
	
    Node(Node<E> prev, E element, Node<E> next) {
        this.item = element;
        this.next = next;
        this.prev = prev;
    }
}
```

### 5.添加元素

```java
/**
* add(E e) 作用：添加元素到末尾
**/
public boolean add(E e) {
    linkLast(e);
    return true;
}

void linkLast(E e) {
    // 获取集合尾部元素 l
    final Node<E> l = last;
    // 新建e节点对象, pre为l, next为null, 
    final Node<E> newNode = new Node<>(l, e, null);
    // 将尾部指针指向 新建节点newNode
    last = newNode;
    
    // 如果尾部元素为空,表示集合为空,将头部指针指向 新建节点newNode
    // 此时 头部指针和尾部指针 都指向newNode
    if (l == null)
        first = newNode;
    else
        // 如果尾部元素不为空,则将next指向newNode
        l.next = newNode;
    size++;
    modCount++;
}

/**
* add(int index, E element) 作用：将元素element添加到指定索引位置
**/
public void add(int index, E element) {
    checkPositionIndex(index);
    // 如果添加的索引刚好等于末尾索引,直接添加在最后一位
    if (index == size)
        linkLast(element);
    else
        linkBefore(element, node(index));
}

// 在节点succ前面,插入新节点e
void linkBefore(E e, Node<E> succ) {
    // succ为index位置的元素
    final Node<E> pred = succ.prev;
    // 新建节点,prev为succ.prev, next为succ
    final Node<E> newNode = new Node<>(pred, e, succ);
    // 将succ.prev指向newNode
    succ.prev = newNode;
    // 如果succ为头部元素,则将头部指针指向newNode
    if (pred == null)
        first = newNode;
    else
        pred.next = newNode;
    size++;
    modCount++;
}

// 查找指定位置的节点
Node<E> node(int index) {
    // size>>1 相当于 size/2
    if (index < (size >> 1)) {
        // 从第一个节点开始找
        Node<E> x = first;
        for (int i = 0; i < index; i++)
            x = x.next;
        return x;
    } else {
        Node<E> x = last;
        for (int i = size - 1; i > index; i--)
            x = x.prev;
        return x;
    }
}

/**
* addAll(int index, Collection<? extends E> c) 作用：将集合c添加到指定索引元素前面
**/ 
public boolean addAll(int index, Collection<? extends E> c) {
    checkPositionIndex(index);
	
    // 将c转换为数组,并获取数组长度numNew,数组为空则添加失败
    Object[] a = c.toArray();
    int numNew = a.length;
    if (numNew == 0)
        return false;
	
    // 如果添加的位置在末尾,则pred指向尾部元素
    // 如果不是末尾,succ为索引处节点,pred为succ.prev
    Node<E> pred, succ;
    if (index == size) {
        succ = null;
        pred = last;
    } else {
        succ = node(index);
        pred = succ.prev;
    }
	
    // 循环数组元素
    for (Object o : a) {
        @SuppressWarnings("unchecked") E e = (E) o;
        // 新建节点newNode
        Node<E> newNode = new Node<>(pred, e, null);
        // 如果是头部元素则将头部指针指向newNode,否则将前一位元素pred的next指向newNode
        if (pred == null)
            first = newNode;
        else
            pred.next = newNode;
        // 将pred指向newNode,继续添加下一个元素
        pred = newNode;
    }
	
    // 如果添加位置为尾部,则将尾部指针指向最后newNode
    if (succ == null) {
        last = pred;
    } else {
        // 将最后一个newNode的next指向succ
        pred.next = succ;
        // 将succ的prev指向最后一个newNode
        succ.prev = pred;
    }

    size += numNew;
    modCount++;
    return true;
}

/**
* set(int index, E element) 作用：将索引位置元素修改为element
**/ 
public E set(int index, E element) {
    // 检查是否越界
    checkElementIndex(index);
    // 获取索引位置的节点
    Node<E> x = node(index);
    // 将节点元素item修改为新element,返回旧元素值
    E oldVal = x.item;
    x.item = element;
    return oldVal;
}

/**
* offer()：添加元素,底层调用add方法,属于Deque接口的方法
**/
public boolean offer(E e) {
    return add(e);
}
public boolean offerFirst(E e) {
    addFirst(e);
    return true;
}
public boolean offerLast(E e) {
    addLast(e);
    return true;
}

/**
*  push(E e)：将元素添加到头部
**/
public void push(E e) {
    addFirst(e);
}
```

### 6.删除元素

```java
/**
* remove(int index) 作用：删除索引index处的元素
**/
public E remove(int index) {
    // 检查索引越界
    checkElementIndex(index);
    // node方法返回该索引处的节点
    return unlink(node(index));
}

// 清除节点x的所有引用,并将x前后节点相连
E unlink(Node<E> x) {
    
    final E element = x.item;
    final Node<E> next = x.next;
    final Node<E> prev = x.prev;
	
    // prev==null 表示节点为头部节点
    if (prev == null) {
        first = next;
    } else {
        // prev.next原本指向x,跳过x直接指向next
        prev.next = next;
        // 将x.prev设置为空
        x.prev = null;
    }
	
    // next==null 表示节点为尾部节点
    if (next == null) {
        last = prev;
    } else {
        // next.prex原本指向x,跳过x直接指向prev
        next.prev = prev;
        // 将x.next设置为空
        x.next = null;
    }

    // 将节点x的元素设置为空,清除所有引用以便GC回收
    x.item = null;
    size--;
    modCount++;
    return element;
}

/**
* remove(Object o) 作用：删除集合中第一次出现的对象o
**/
public boolean remove(Object o) {
    // 分为null和非null的情况
    if (o == null) {
        for (Node<E> x = first; x != null; x = x.next) {
            if (x.item == null) {
                unlink(x);
                return true;
            }
        }
    } else {
        for (Node<E> x = first; x != null; x = x.next) {
            if (o.equals(x.item)) {
                unlink(x);
                return true;
            }
        }
    }
    return false;
}

/**
* poll() 作用：默认删除头部元素,也可以删除尾部元素
**/
public E poll() {
    final Node<E> f = first;
    return (f == null) ? null : unlinkFirst(f);
}
public E pollFirst() {
    final Node<E> f = first;
    return (f == null) ? null : unlinkFirst(f);
}
public E pollLast() {
    final Node<E> l = last;
    return (l == null) ? null : unlinkLast(l);
}

/**
* pop() 作用：删除头部元素
**/
public E pop() {
    return removeFirst();
}
```

### 7.查找获取元素

```java
/**
* get() 作用：获取指定位置的元素
**/
public E get(int index) {
    checkElementIndex(index);
    return node(index).item;
}

public E getFirst() {
    final Node<E> f = first;
    if (f == null)
        throw new NoSuchElementException();
    return f.item;
}

public E getLast() {
    final Node<E> l = last;
    if (l == null)
        throw new NoSuchElementException();
    return l.item;
}

/**
* peek() 作用：默认获取头部的元素
**/
public E peek() {
    final Node<E> f = first;
    return (f == null) ? null : f.item;
}

public E peekFirst() {
    final Node<E> f = first;
    return (f == null) ? null : f.item;
}

public E peekLast() {
    final Node<E> l = last;
    return (l == null) ? null : l.item;
}
```

### 8.堆栈和队列方法

```java
/**
* 堆栈：先进后出
**/
// 在栈顶添加一个元素
public void push(E e) {}
// 从栈顶取出一个元素并删除
public E pop() {}

/**
* 队列：先进先出
**/
// 往队尾添加一个元素
public boolean offer(E e) {}
// 从队首取出一个元素,不删除
public E peek() {}
// 删除队首一个元素
public E poll() {}
```

