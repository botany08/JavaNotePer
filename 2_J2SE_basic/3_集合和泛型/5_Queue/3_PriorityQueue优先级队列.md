## 基本概念

### 1. PriorityQueue 定义

- PriorityQueue是优先级队列，首先实现了队列接口(Queue)，与LinkedList类似。
- 队列长度没有限制，与一般队列的区别是有优先级的概念。每个元素都有优先级，队头永远都是优先级最高的。
- PriorityQueue内部算法是用堆实现的，基础数据存在对象数组中。
- 默认为最小堆，可以修改比较器方法变成最大堆。

### 2.Queue接口

```java
/**
* Queue扩展了Collection,主要是三个方面
* 1. 在尾部添加元素(add, offer)。
* 2. 查看头部元素(element, peek)，返回头部元素但不删除。
* 3. 删除头部元素(remove, poll)，返回头部元素并删除。
**/
public interface Queue<E> extends Collection<E> {
    boolean add(E e);
    boolean offer(E e);
    E remove();
    E poll();
    E element();
    E peek();
}
```

### 3.时间复杂度

- 实现了优先级队列，最先出队的总是优先级最高的，即排序中的第一个。
- 优先级可以有相同的，内部元素不是完全有序的，如果遍历输出，除了第一个，其他没有特定顺序。
- 查看头部元素的效率很高，为O(1)。
- 入队、出队效率比较高，为O(log2(N))，构建堆heapify的效率为O(N)。
- 根据值查找和删除元素的效率比较低，为O(N)。

## 底层实现

### 1.基本属性

```java
//默认底层数组大小
private static final int DEFAULT_INITIAL_CAPACITY = 11;   

//内存实际数组
transient Object[] queue;                            

//数组中元素的数量
private int size = 0;                                   

//比较器对象
private final Comparator<? super E> comparator;
```

### 2.构造方法

```java
// 无参构造方法
public PriorityQueue() {
    this(DEFAULT_INITIAL_CAPACITY, null);
}

// initialCapacity：默认底层数组大小
public PriorityQueue(int initialCapacity) {
    this(initialCapacity, null);
}

// 与TreeMap/TreeSet类似，在PriorityQueue中，要么元素实现Comparable接口，要么传递一个比较器Comparator。
// initialCapacity：底层数组大小 comparator：外比较器
public PriorityQueue(int initialCapacity,Comparator<? super E> comparator) {
    // 当指定数组长度小于1,报错
    if (initialCapacity < 1)
        throw new IllegalArgumentException();
    // 初始化数组大小和比较器
    this.queue = new Object[initialCapacity];
    this.comparator = comparator;
}
```

## 具体方法实现

### 1.添加元素

- **添加元素到队列尾**的基本步骤
  1. 将元素添加到数组的末尾
  2. 判断是否需要扩容
  3. 向上调整堆(根据比较器决定是最大堆还是最小堆)

```java
/**
* 添加一个元素：默认为最小堆
**/
public boolean offer(E e) {
    // 添加元素为null,则报错
    if (e == null)
        throw new NullPointerException();
    modCount++;
    
    // 扩容数组容量(重要)
    int i = size;
    if (i >= queue.length) // 扩容条件
        grow(i + 1);
    
    // size增加
    size = i + 1;
    
    // 如果添加的是第一个元素,则直接添加
    if (i == 0)
        queue[0] = e;
    // 添加到堆的末尾后,进行调整(重要)
    else
        siftUp(i, e);
    return true;
}


// 作用：扩容数组容量
private void grow(int minCapacity) {
    // 旧容量
    int oldCapacity = queue.length;
    
    // 新容量：如果旧容量小于64则 乘以2再加2,如果大于等于64则 向右位移1位[就是增加一半]
    int newCapacity = oldCapacity + ((oldCapacity < 64) ?(oldCapacity + 2) :(oldCapacity >> 1));
    
    // private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    // 新容量超过了最大容量MAX_ARRAY_SIZE
    if (newCapacity - MAX_ARRAY_SIZE > 0)
        newCapacity = hugeCapacity(minCapacity);
    
    // 将旧数组数据复制到新数组
    queue = Arrays.copyOf(queue, newCapacity);
}

// 作用：确定最大容量
private static int hugeCapacity(int minCapacity) {
    // 当旧容量小于0,报错
    if (minCapacity < 0) 
        throw new OutOfMemoryError();
    //  当旧容量超过最大数组容量,则使用int最大值。否则使用最大数组容量。
    return (minCapacity > MAX_ARRAY_SIZE) ?Integer.MAX_VALUE :MAX_ARRAY_SIZE;
}


// 作用：进行堆调整(k为堆末尾,x为被添加的元素)
private void siftUp(int k, E x) {
    // 使用外比较器
    if (comparator != null)
        siftUpUsingComparator(k, x);
    // 使用内比较器
    else
        siftUpComparable(k, x);
}

// 使用内比较器
private void siftUpComparable(int k, E x) {
    Comparable<? super E> key = (Comparable<? super E>) x;
    
    // 一直向父结点传递,向上调整,直到根结点
    while (k > 0) {
        // 父结点索引 (k-1)/2
        int parent = (k - 1) >>> 1;
        // 父结点对象
        Object e = queue[parent];
        
        // 当 子结点 大于等于 父结点 , 不调整, 默认为最小堆
        if (key.compareTo((E) e) >= 0)
            break;
        
        queue[k] = e;
        // 指针指向父结点
        k = parent;
    }
    
    queue[k] = key;
}

// 使用外比较器
private void siftUpUsingComparator(int k, E x) {
    // 一直向父结点传递,向上调整,直到根结点
    while (k > 0) {
        // 获取父结点 (k-1)/2
        int parent = (k - 1) >>> 1;
        Object e = queue[parent];
        
        // 当 子结点 大于等于 父结点 , 不调整, 默认为最小堆
        if (comparator.compare(x, (E) e) >= 0)
            break;
        
        queue[k] = e;
        
        // 指针指向父结点
        k = parent;
    }
    queue[k] = x;
}

```

### 2.删除元素

- **删除队首元素**的基本步骤
  1. 将队尾元素和队首元素进行交换
  2. 删除队尾元素
  3. 从根结点开始向下调整整个堆(根据比较器决定是最大堆还是最小堆)

```java
/**
* 删除队首的元素[删除堆顶的元素]
**/
public E poll() {
    // 如果队列为空则返回空
    if (size == 0)
        return null;
    
    // 队尾的索引
    int s = --size;
    // 修改次数增加1
    modCount++;
    
    // result：队首元素
    E result = (E) queue[0];
    // x：队尾元素
    E x = (E) queue[s];
    
    // 将最后一位设为null[删除最后一位元素]
    queue[s] = null;
    
    // 向下调整堆[重要]
    if (s != 0)
        siftDown(0, x);
    return result;
}

// 向下调整  k：插入的位置[队首]  x：插入的元素[最后一位元素]
private void siftDown(int k, E x) {
    if (comparator != null)
        siftDownUsingComparator(k, x);
    else
        siftDownComparable(k, x);
}

// 向下调整：内比较器
private void siftDownComparable(int k, E x) {
    Comparable<? super E> key = (Comparable<? super E>)x;
    
    // size >>> 1 相当于 size/2, 得到的是最后一个非叶子结点
    int half = size >>> 1;  
    
    // 从上往下遍历父结点
    while (k < half) {
        // 当根结点为0时, 2k+1表示左子结点, 2k+2表示右子结点
        int child = (k << 1) + 1; 
        // 获取左子结点对象
        Object c = queue[child];
        // 获取右子结点
        int right = child + 1;
        
        // 当右子结点小于数组长度  且  左子结点大于右子结点 时
        if (right < size && ((Comparable<? super E>) c).compareTo((E) queue[right]) > 0)
            // 取右子结点
            c = queue[child = right];
        
        // 当父结点小于子结点时,满足最小堆条件不调整
        if (key.compareTo((E) c) <= 0)
            break;
        
        // 当不满足最小堆条件时,交换父结点和子节点
        queue[k] = c;
        // 指针指向交换的那个子结点,继续向下调整
        k = child;
    }
    queue[k] = key;
}

// 向下调整：外比较器
private void siftDownUsingComparator(int k, E x) {
    int half = size >>> 1;
    while (k < half) {
        int child = (k << 1) + 1;
        Object c = queue[child];
        int right = child + 1;
        if (right < size &&
            comparator.compare((E) c, (E) queue[right]) > 0)
            c = queue[child = right];
        if (comparator.compare(x, (E) c) <= 0)
            break;
        queue[k] = c;
        k = child;
    }
    queue[k] = x;
}


/**
* 根据值删除元素
**/
boolean removeEq(Object o) {
    // 遍历数组,找到要删除对象的索引
    for (int i = 0; i < size; i++) {
        if (o == queue[i]) {
            removeAt(i);
            return true;
        }
    }
    return false;
}

// 根据索引删除对象
private E removeAt(int i) {
    modCount++;
    int s = --size;
    // 如果删除的是最后一个元素,则直接删除,不用调整堆
    if (s == i) 
        queue[i] = null;
    // 删除的是堆中间的元素
    else {
        // moved：最后一个元素
        E moved = (E) queue[s];
        // 删除最后一个元素
        queue[s] = null;
        
        // 从删除的位置开始,进行向下调整
        siftDown(i, moved);
        
        // 如果调整的位置还是最后一个元素,说明不用进行向下调整,则进行向上调整
        if (queue[i] == moved) {
            siftUp(i, moved);
            // 表示调整成功,返回被删除值
            if (queue[i] != moved)
                return moved;
        }
    }
    return null;
}
```

### 3.查找获取元素

```java
/**
* 获取队首的元素[获取堆顶的元素]
**/
public E peek() {
    return (size == 0) ? null : (E) queue[0];
}
```

## 堆的应用场景

### 1.求前K个最大元素

- 问题描述

  求前K个最大的元素，元素个数不确定，数据量大并且动态变化。需要实时得出当前前K个最大元素。

  问题的变体有：求前K个最小的元素，求第K个最大的，求第K个最小的。

- 解决思路

  1. 维护一个长度为K的数组，最前面的K个元素就是目前最大的K个元素。每新增一个元素,遍历查找最小值。如果小于最小值则不变,如果大于最小值则替换最小值。
  2. 使用最小堆维护K个元素。在最小堆中，根即第一个元素永远都是最小的。新增元素与根比较, 如果小于根结点则不变,如果大于根结点则替换根,然后向下调整堆。调整的效率为O(log2(K)),总体效率为O(N*log2(K))。使用最小堆,可以获得第K个最大的元素,直接用数组索引去找就可以。

### 2.求中值

- 问题描述

  中值就排序后中间那个元素的值。如果元素个数为奇数，中值是没有歧义的。如果是偶数，中值可能有不同的定义，可以为偏小的那个，也可以是偏大的那个，或者两者的平均值，或者任意一个。
  
- 解决思路
  
  1. 维护一个固定大小的数组，将所有值都放到数组中，排序后取中间的值即可。排序可以使用Arrays.sort()方法，效率为O(N*log2(N))。
  2. 当数组里面的值是不断扩大时
     - 初始化中位数m。用最大堆维护<=m的元素,用最小堆维护>=m的元素,两个堆不包含m.
     - 添加新元素时,比如为e。将e与m进行比较，若e<=m则将其加入到最大堆中，否则将其加入到最小堆中。
     - 更换中位数。如果最小堆和最大堆的元素个数的差值>=2,则将m加入到元素个数少的堆中,然后移除元素较多的堆的根节点并赋值给中位数m。
  
  

