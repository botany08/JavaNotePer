## 基础概念

<img src="https://javanote.oss-cn-shenzhen.aliyuncs.com/1_Collection接口及其子类.png"  />

### 1.定义

- Deque的含义是“double ended queue”，即双端队列，既可以当作栈使用，也可以当作队列使用。
- 一般而言，由于需要移动元素，数组的插入和删除效率比较低，但`ArrayDeque`的效率比`ArrayList`高。

### 2.Queue接口和Deque接口  

| Queue-队列  | Deque           | 说明                                 |
| :---------- | :-------------- | ------------------------------------ |
| `add(e)`    | `addLast(e)`    | 向队尾插入元素，失败则抛出异常       |
| `offer(e)`  | `offerLast(e)`  | 向队尾插入元素，失败则返回false      |
| `remove()`  | `removeFirst()` | 获取并删除队首元素，失败则抛出异常   |
| `poll()`    | `pollFirst()`   | 获取并删除队首元素，失败则返回null   |
| `element()` | `getFirst()`    | 获取但不删除队首元素，失败则抛出异常 |
| `peek()`    | `peekFirst()`   | 获取但不删除队首元素，失败则返回null |

### 3.Stack类和Deque接口  

| Stack-栈  | Deque           | 说明                                 |
| --------- | --------------- | ------------------------------------ |
| `push(e)` | `addFirst(e)`   | 向栈顶插入元素，失败则抛出异常       |
| 无        | `offerFirst(e)` | 向栈顶插入元素，失败则返回false      |
| `pop()`   | `removeFirst()` | 获取并删除栈顶元素，失败则抛出异常   |
| 无        | `pollFirst()`   | 获取并删除栈顶元素，失败则返回null   |
| `peek()`  | `peekFirst()`   | 获取但不删除栈顶元素，失败则抛出异常 |
| 无        | `peekFirst()`   | 获取但不删除栈顶元素，失败则返回null |

### 4.时间复杂度

- 在两端添加、删除元素的效率很高，动态扩展需要的内存分配以及数组拷贝开销可以被平摊，具体来说，添加N个元素的效率为O(N)。
- 根据元素内容查找和删除的效率比较低，为O(N)。
- 与`ArrayList`和`LinkedList`不同，**没有索引位置的概念**，**不能根据索引位置进行操作**。
- `ArrayDeque`实现了双端队列接口，可以作为队列、栈、或双端队列使用，相比`LinkedList`效率要更高一些，实现原理上，**采用动态扩展的循环数组**，使用高效率的位操作。

## 底层实现

### 1.数据结构

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/6_Deque底层实现.png)

- `ArrayDeque`底层通过循环数组实现，是为了满足可以同时在数组两端插入或删除元素，也就是说数组的任何一点都可能被看作起点或者终点。
- 循环数组是由一个数组和两个头尾指针实现的。
- `ArrayDeque`是非线程安全的`(not thread-safe)`，当多个线程同时使用的时候，需要程手动同步。
- `ArrayDeque`不允许放入null元素。

###2.构造原理

#### 2.1基本属性

```java
/**
* 1. head和tail的值就是数组的索引,但不是0或最后一个索引,是会经常变化的。
* 2. ArrayDeque的高效是通过head和tail实现的,如果通过数组索引实现,就会经常进行数组的复制删除。
**/

// 底层存储数据的对象数组
transient Object[] elements;
// head指向首端第一个有效元素,逻辑意义上
transient int head;
// tail指向尾端第一个可以插入元素的空位,逻辑意义上
transient int tail;
// 数组最小容量
private static final int MIN_INITIAL_CAPACITY = 8;
```

#### 2.2构造方法(初始化容量)

```java
/**
* 无参构造
**/
public ArrayDeque() {
    // 默认初始化数组大小为 16 = 2^4
    elements = new Object[16];
}


/**
* numElements：指定数组大小
* 数组容量为2的整数次幂,因为循环数组必须时刻至少留一个空位，tail变量指向下一个空位，为了容纳numElements个元素，
* 至少需要numElements+1个位置。
**/
public ArrayDeque(int numElements) {
    allocateElements(numElements);
}

private void allocateElements(int numElements) {
    elements = new Object[calculateSize(numElements)];
}

// 作用：由于容量必须是2的幂次方,返回 大于输入参数且最近的2的整数次幂的数
private static int calculateSize(int numElements) {
    // private static final int MIN_INITIAL_CAPACITY = 8; 最小容量为2^3
    int initialCapacity = MIN_INITIAL_CAPACITY;
    
    // 当指定数组容量大于8时才进行处理,默认为8
    if (numElements >= initialCapacity) {
        initialCapacity = numElements;
        
        // 让高位参与运算,或运算的情况下,可以得到比原数更大的值且为 2^n - 1
        initialCapacity |= (initialCapacity >>>  1);
        initialCapacity |= (initialCapacity >>>  2);
        initialCapacity |= (initialCapacity >>>  4);
        initialCapacity |= (initialCapacity >>>  8);
        initialCapacity |= (initialCapacity >>> 16);
         
        // 得到2的整数次幂
        initialCapacity++;

        if (initialCapacity < 0)   
            initialCapacity >>>= 1;
    }
    
    return initialCapacity;
}

/**
* Collection<? extends E> c：指定集合
**/
public ArrayDeque(Collection<? extends E> c) {
    allocateElements(c.size());
    addAll(c);
}
```

### 3.循环数组的实现

#### 3.1原理

循环数组是由一个普通数组和两个首尾指针组成的。head指向数组开头，tail指向最后一个元素的下一个null。

- 如果head和tail相同，则数组为空，长度为0。  

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/7_数组长度1.jpg)

- 如果tail大于head，则第一个元素为elements[head]，最后一个为elements[tail-1]，长度为tail-head，元素索引从head到tail-1。

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/8_数组长度2.jpg)

- 如果tail小于head，且为0。则第一个元素为elements[head]，最后一个为elements[elements.length-1]，长度为elements.length-head，元素索引从head到elements.length-1。

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/9_数组长度3.jpg)

- 如果tail小于head，且大于0。则会形成循环，第一个元素为elements[head]，最后一个是elements[tail-1]，元素索引从head到elements.length-1，然后再从0到tail-1。长度为length-head+tail

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/10_数组长度4.jpg)

#### 3.2数组长度扩容

```java
/**
* 添加方法--扩容
**/
public void addLast(E e) {
    if (e == null)
        throw new NullPointerException();
    elements[tail] = e;
    /**
    * 数组长度扩容条件：因为tail指向末尾元素的下一个null,所以tail+1==head表示数组已经满了,需要扩容
    * tail+1==head原理：
    * 1. 当head>tail是成立的,
    * 2. 当head(0)<tail(7)时.假设容量为8,head=0,tail=7.tail+1表示超过最大容量索引,
    *    和(elements.length - 1))最大索引  做与运算就会得到0.
    **/
    if ( (tail = (tail + 1) & (elements.length - 1)) == head)
        doubleCapacity();
}

// 扩容：将容量扩大两倍
private void doubleCapacity() {
    assert head == tail;
    int p = head;
    int n = elements.length;
    int r = n - p; 
    
    // 向左移动1位,表示扩大两倍
    int newCapacity = n << 1;
    if (newCapacity < 0)
        throw new IllegalStateException("Sorry, deque too big");
    
    // 新建数组
    Object[] a = new Object[newCapacity];
   
    // 进行数组复制
    System.arraycopy(elements, p, a, 0, r);
    System.arraycopy(elements, 0, a, r, p);
    elements = a;
    head = 0;
    tail = n;
}
```

## 具体操作方法实现

### 1.队列方法

```java
// 在队列末尾插入指定元素
public boolean offer(E e) {
    return offerLast(e);
}

// 获取但不删除,队列头部元素。如果队列为空返回null。
public E peek() {
    return peekFirst();
}

// 获取并删除,队列头部元素。如果队列为空返回null。
public E poll() {
    return pollFirst();
}
```

### 2.栈方法

```java
// 在栈顶添加指定元素
public void push(E e) {
    addFirst(e);
}

// 获取但不删除,栈顶的元素。如果队列为空返回null。
public E peek() {
    return peekFirst();
}

// 获取并删除，栈顶的元素。如果队列为空返回null。
public E pop() {
    return removeFirst();
}
```

### 3.添加元素

```java
/**
* 在数组末尾tail添加有一个指定元素e
**/
public boolean add(E e) {
    addLast(e);
    return true;
}

public void addLast(E e) {
    // 添加元素不能为null
    if (e == null)
        throw new NullPointerException();
    
    // 由于tail指向最后一个元素后的null位置,所以直接添加即可。在后面将tail向后移动一位。
    elements[tail] = e;
    
    // 判断是否需要扩容
    if ( (tail = (tail + 1) & (elements.length - 1)) == head)
        doubleCapacity();
}
```

### 4.删除元素

```java
/**
* 删除数组head第一个元素
**/
public E remove() {
    return removeFirst();
}

public E removeFirst() {
    E x = pollFirst();
    if (x == null)
        throw new NoSuchElementException();
    return x;
}

public E pollFirst() {
    // 获取head指针
    int h = head;
    
    @SuppressWarnings("unchecked")
    // 获取head处的元素
    E result = (E) elements[h];
    
    
    if (result == null)
        return null;
    
    // 将数组取出位置设为null
    elements[h] = null;
    
    // 如果在数组的索引内,则 head+1
    // 如果在数组的索引范围外,则 回到0
    head = (h + 1) & (elements.length - 1);
    return result;
}

/**
* 删除最后tail的元素
**/
public E removeLast() {
    E x = pollLast();
    if (x == null)
        throw new NoSuchElementException();
    return x;
}

public E pollLast() {
    // 将tail指针向左移动一位
    // 当tail=0,则tail-1=-1,-1的补码为 1111 1111,length-1为0000 0111, 与运算后为 0000 0111,是数组最后一位
    int t = (tail - 1) & (elements.length - 1);
    @SuppressWarnings("unchecked")
    E result = (E) elements[t];
    
    if (result == null)
        return null;
    // 将删除的地方设为null
    elements[t] = null;
    tail = t;
    return result;
}
```

### 5.是否包含元素

```java
/**
* 判断是否包含元素o
**/
public boolean contains(Object o) {
    // 因为数组中不含null元素
    if (o == null)
        return false;
    
    // 数组最后一个索引
    int mask = elements.length - 1;
    // 逻辑头head
    int i = head;
    Object x;
    
    // 遍历数组,从逻辑头head开始遍历
    while ( (x = elements[i]) != null) {
        if (o.equals(x))
            return true;
        // 从数组最后一个索引 跳转到 数组第一个索引
        i = (i + 1) & mask;
    }
    return false;
}
```

