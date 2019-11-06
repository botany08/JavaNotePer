## ArrayList

### 1.基础概念

- ArrayList实现了List接口，元素存放和添加的顺序是一致的。
- 允许放入null元素，允许放入重复的元素。
- 没有实现同步，是非线程安全，其他方面和Vector相同。

### 2.特点以及效率

- 顺序存储,数据可重复,内部采用动态数组实现 。

- 增删改查时间复杂度
  
  | 方法                      | 作用                                | 时间复杂度 |
  | ------------------------- | ----------------------------------- | ---------- |
  | get(int index)            | 根据索引查找元素,数组访问           | O(1)       |
  | contains(Object o)        | 根据元素内容查询元素,需要遍历       | O(n)       |
  | add(E e)                  | 添加元素到末尾,数组添加             | O(1)       |
  | add(int index, E element) | 添加元素到指定位置,需要移动元素     | O(n)       |
  | remove(int index)         | 删除指定索引位置的元素,需要移动元素 | O(n)       |
  | remove(Object o)          | 删除指定对象的元素,需要移动元素     | O(n)       |
  | set(int index, E element) | 修改指定位置的元素值                | O(1)       |

###3.底层实现原理

- 底层通过动态数组实现
  1. ArrayList的当前元素个数称为size。
  2. ArrayList的容量称为capacity，表示底层数组的实际大小，容器内存储元素的个数不能多于当前容量。
  3. 当向容器中添加元素时，size会增加。如果容量capacity不足，容器会自动增大底层数组的大小。

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/ArrayList底层实现.png)

### 4.常用方法时间开销

- add()方法的时间开销跟插入位置有关。
- addAll()方法的时间开销跟添加元素的个数成正比。其余方法大都是线性时间。
- size(), isEmpty(), get(), set()方法均能在常数时间内完成。
- 为追求效率，ArrayList没有实现同步，如果需要多个线程并发访问，用户可以手动同步，也可使用Vector替代。

### 5.ArrayList动态数组扩容详解

####5.1初始容量和容量变化

```java
/**
* 构造方法决定初始容量
* 1. 容量变化的规则：((旧容量 * 3) / 2) + 1
* 2. 一旦容量发生变化，就要带来额外的内存开销，和时间上的开销。
**/
// 默认的构造方法,初始容量被设置为10。当元素超过10个后会重新分配空间,使数组capacity增大到16.
List arrayList = new ArrayList();

// 带参构造方法,初始容量被设置为4。当元素超过4个后,会重新分配空间。
List arrayList = new ArrayList(4);

// 数组最大容量
private static final int MAX_ARRAY_SIZE = 2147483639;
```

####5.2自动改变数组长度的原理

- ArrayList类的实质

  ArrayList底层采用Object类型的数组实现，当使用不带参数的构造方法生成ArrayList对象时，实际上会在底层生成一个长度为10的Object类型数组。

```java
/**
* 初始化步骤
* 1. ArrayList定义了一个私有的未被序列化的Object数组elementData，用来存储ArrayList的对象列表(注意只定义未初
*    始)
* 2. 以指定初始容量(Capacity)或把指定的Collection转换为引用型数组后实例化elementData数组。
* 3. 如果没有指定，则预置初始容量为10进行实例化。
* 4. 把私有数组预先实例化，然后通过copyOf方法覆盖原数组，是实现自动改变ArrayList的大小(size)的关键。
**/
private  transient  Object[]  elementData;


// 构造方法1 - 参数为正整数
public ArrayList(int initialCapacity) {
    // 当声明数组为2147483630左右时,会发生内存溢出错误,OutOfMemoryError
    if (initialCapacity > 0) {
        // 初始化为Object数组,初始容量为initialCapacity
        this.elementData = new Object[initialCapacity];
        
    } else if (initialCapacity == 0) {
        // 参数为0,初始化为空Object数组
        // private static final Object[] EMPTY_ELEMENTDATA = {};
        this.elementData = EMPTY_ELEMENTDATA;
        
    } else {
        throw new IllegalArgumentException("Illegal Capacity: "+initialCapacity);
    }
}

// 构造方法2 - 无构造参数
public ArrayList() {
    // 当不传参数时，初始化为长度为0 的Object数组
    // private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};
    this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
}


// 构造方法3 - 参数为数组
public ArrayList(Collection<? extends E> c) {
    // 用Collection类初始化elementData数组
    // toArray()方法 转化出来的数组是复制了原数据的一个副本而不只是原数据的一个引用
    elementData = c.toArray();
    
    if ((size = elementData.length) != 0) {
        /**
        * toArray()方法返回的可能不是Object[].class
        * 原因：如果集合c 重写了 List接口toArray()方法,并且返回类型不一致,此时优先调用重写的方法。
        **/
        if (elementData.getClass() != Object[].class)
            // 用copyOf方法覆盖原数组实现数组长度增长
            elementData = Arrays.copyOf(elementData, size, Object[].class);
    } else {
        // 当size==0,初始化为空Object[]
        this.elementData = EMPTY_ELEMENTDATA;
    }
}
```

- ArrayList自动改变size的机制

  1. 为了实现这一机制，java引进了Capacity和size概念，以区别数组的length。为了保证用户增加新的列表对象,Java设置了最小容量(minCapacity)。
  2. 通常情况大于列表对象的数目,所以Capactiy虽然是底层数组的长度(length),但是对于用户来讲,它是无意义的。
  3. 而size存储着列表对象的数量,才是用户所需要的。为了防止用户错误修改,这一属性被设置为private的,不过可以通过size()获取。
  4. Capacity初始值(initialCapacity)可以由用户直接指定或由用户指定的Collection集合存储的对象数目确定。而size的被声明为int型变量,默认为0,当用户指定Collection创建ArrayList时,size值等于initialCapacity。

  ```java
  /**
  * add()方法 - 改变数组长度
  **/
  public boolean add(E e) {
      // 作用：size增加1,为此次所需的长度,以此调整底层数组长度
      ensureCapacityInternal(size + 1);
      // 将新添加的对象e,放到数组的size+1的位置
      // elementData为底层动态obj数组   transient Object[] elementData; 
      elementData[size++] = e;
      return true;
  }
  
  private void ensureCapacityInternal(int minCapacity) {
      // calculateCapacity：返回此次所需的数组长度(最小容量)
      // ensureExplicitCapacity：调整底层数组容量
      ensureExplicitCapacity(calculateCapacity(elementData, minCapacity));
  }
  
  private static int calculateCapacity(Object[] elementData, int minCapacity) {
      if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
          // private static final int DEFAULT_CAPACITY = 10;
          // 当elementData为空obj数组时,比较所需长度和默认长度10,返回最大值
          return Math.max(DEFAULT_CAPACITY, minCapacity);
      }
      return minCapacity;
  }
  
  private void ensureExplicitCapacity(int minCapacity) {
      // protected transient int modCount = 0;  修改次数增加1
      modCount++;
      
      // 当最小容量 大于 当前数组容量, 扩充底层数组容量 
      if (minCapacity - elementData.length > 0)
          // grow：扩充数组容量capacity
          grow(minCapacity);
  }
  
  /**
  * 数组最大容量值为什么是 Integer.MAX_VALUE - 8 ？
  * private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
  * 原因：在数组的对象头里有一个_length字段,记录数组长度,
  **/
  private void grow(int minCapacity) {
      // 现有容量：elementData.length
      int oldCapacity = elementData.length;
      // 扩充容量的计算方式：oldCapacity+(oldCapacity >> 1) 相当于 oldCapacity+(oldCapacity/2)
      // 初始化时,扩充后容量依旧为0.但是如果最小容量大于扩充后容量,则变成最小容量,所以初始化为10.
      int newCapacity = oldCapacity + (oldCapacity >> 1);
      
      // 如果 扩充后的容量 小于 最小容量, 则 扩充后的容量修改为最小容量
      if (newCapacity - minCapacity < 0)
          newCapacity = minCapacity;
      
      // 如果 扩充后的容量 大于 数组最大容量值MAX_ARRAY_SIZE
      if (newCapacity - MAX_ARRAY_SIZE > 0)
          newCapacity = hugeCapacity(minCapacity);
      
     
      // Arrays.copyOf()方法将旧有数组,复制到扩充容量后的数组
      elementData = Arrays.copyOf(elementData, newCapacity);
  }
  
  private static int hugeCapacity(int minCapacity) {
      // 如果最小容量小于0,则抛出内存溢出错误
      if (minCapacity < 0) 
          throw new OutOfMemoryError();
      
    	// 如果最小容量 大于 最大容量值, 则取Integer类型最大容量值
      return (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
  }
  
  /**
  * 总结：底层数组容量调整思路 （初始化情形）
  * 1. 添加元素时,计算添加后数组的最小容量. (当前容量为0,最小默认容量为10)
  * 2. 当最小容量 大于 当前数组容量, 扩充底层数组容量. 
  * 3. 计算扩充后容量 oldCapacity+(oldCapacity/2). (扩充后容量为0)
  * 4. 如果扩充后的容量 小于 最小容量, 则扩充后的容量修改为最小容量,否则为扩充后容量.(扩充后容量0修改为10)
  * 5. 计算扩充后容量最大值溢出问题.(10 小于 MAX_ARRAY_SIZE)
  * 6. Arrays.copyOf()方法将旧有数组,复制到扩充容量后的数组. (将当前数组复制到容量为10的数组,元素不变)
  **/
  ```

  ```java
  /**
  * remove()方法 - 删除元素
  **/
  public E remove(int index) {
      // 检查删除的索引
      rangeCheck(index);
  	
      // 修改次数加1
      modCount++;
      // 获取删除索引对应的元素
      E oldValue = elementData(index);
  	
      // numMoved表示 删除索引 后有几个元素(长度)
      int numMoved = size - index - 1;
      
      /**
      * 数组的拷贝
      * System.arraycopy()：JVM 提供的数组拷贝实现,内存层面实现
      * Arrays.copyof(): 底层也是使用System.arraycopy进行拷贝
      **/
      if (numMoved > 0)
          // elementData原数组,index+1原数组起始索引
          // elementData目标数组,index目标数组起始索引,numMoved起始索引后的长度
          System.arraycopy(elementData, index+1, elementData, index, numMoved);
      
      // 将数组最后一位元素 置为 null
      elementData[--size] = null; 
  	
      // 返回删除的元素
      return oldValue;
  }
  
  private void rangeCheck(int index) {
      // 当删除索引 大于 数据长度时,抛出异常
      if (index >= size)
          throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
  }
  
  E elementData(int index) {
      // 返回删除索引对应的元素
      return (E) elementData[index];
  }
  ```

####5.3数组扩容的核心方法

-  System.arraycopy数组的拷贝

  ```java
  /**
  * 作用：复制指定源数组src到目标数组dest。复制从src的srcPos索引开始，复制的个数是length，复制到dest的索引从
  * destPos开始。
  **/
  public static native void arraycopy(Object src, int srcPos, Object dest, int destPos, int length);
  ```

### 6.添加元素

```java
/**
* add()方法
**/
// 作用：将指定的元素追加到此List的末尾
public boolean add(E e) {...}
// 作用：在List的指定位置插入指定的元素。将当前位于该位置的元素(如果有)和任何后续元素向右移动一位。
// 原理：先把数组往后移动一位，然后利用数组elementData[index]赋值。
public void add(int index, E element) {...}


/**
* addAll()方法
**/
// 作用：按集合c的Iterator返回的顺序,将所有元素追加到本List的末尾。
public boolean addAll(Collection<? extends E> c) {...}
// 作用：将集合c的所有元素插入到此List中，从指定的位置index开始。
// 原理：先将index后面的元素向右移动c.size()的长度，空出了插入数组的位置，再将集合c复制到这段位置，插入完成。
public boolean addAll(int index, Collection<? extends E> c) {...}


/**
* set()方法
**/
// 用元素element替换此List中指定位置index的元素。
public E set(int index, E element) {...}
```

### 7.遍历元素

```java
/**
* get()方法 - 获取元素
**/
public E get(int index) {
    // 检查index越界问题
    rangeCheck(index);
    // 返回数组index元素
    return elementData(index);
}

private void rangeCheck(int index) {
    if (index >= size)
        throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
}

E elementData(int index) {
    return (E) elementData[index];
}
```

### 8.删除元素

```java
/**
* remove()方法
**/

// 作用：删除索引index位置上的元素,并将索引后元素向左移动一位
// 原理：将数组索引后元素往左移动一位，利用数组赋值System.arraycopy方法实现，再把最后一位置为空。
public E remove(int index) {...}

// 作用：删除List中第一个出现的元素o,如果不包含该元素则不更改.
public boolean remove(Object o) {
    if (o == null) {
        // 依次循环数组元素,删除为null的元素, 通过 == 比较
        for (int index = 0; index < size; index++)
            if (elementData[index] == null) {
                fastRemove(index);
                return true;
            }
    } else {
        // 依次循环数组元素,删除为null的元素, 通过 equals() 比较
        for (int index = 0; index < size; index++)
            if (o.equals(elementData[index])) {
                fastRemove(index);
                return true;
            }
    }
    return false;
}

// 作用：将索引index后元素向左移动一位,并将List最后一位置为null,相当于删除index上的元素。
private void fastRemove(int index) {
    modCount++;
    int numMoved = size - index - 1;
    if (numMoved > 0)
        System.arraycopy(elementData, index+1, elementData, index,numMoved);
    elementData[--size] = null; 
}

/**
* removeAll()方法
* 删除集合中含有 与集合c相等的元素,如果报错则将未比较的元素直接复制到末尾
**/
public boolean removeAll(Collection<?> c) {
    Objects.requireNonNull(c);
    return batchRemove(c, false);
}

private boolean batchRemove(Collection<?> c, boolean complement) {
    final Object[] elementData = this.elementData;
    int r = 0, w = 0;
    boolean modified = false;
    try {
        // 判断本身元素和目标集合元素是否相等
        for (; r < size; r++)
            if (c.contains(elementData[r]) == complement)
                // 将不相等的元素放进新集合中
                elementData[w++] = elementData[r];
    } finally {
        // 报错的情况下,直接执行finally, r不等于size， 将未比较的元素复制到w的末尾
        if (r != size) {
            System.arraycopy(elementData, r, elementData, w, size - r);
            w += size - r;
        }
        
        // 集合中有元素移除,则 w不等于size, 清空w后面的元素
        if (w != size) {
            for (int i = w; i < size; i++)
                elementData[i] = null;
            modCount += size - w;
            size = w;
            modified = true;
        }
    }
    return modified;
}
/**
* removeRange()方法
* 左闭右开 [fromIndex,toIndex)
**/
protected void removeRange(int fromIndex, int toIndex) {
    modCount++;
    // numMoved表示 toIndex索引后的数据长度
    int numMoved = size - toIndex;
    System.arraycopy(elementData, toIndex, elementData, fromIndex, numMoved);

    // newSize表示 数组末尾要删除的数据起始索引
    int newSize = size - (toIndex-fromIndex);
    for (int i = newSize; i < size; i++) {
        elementData[i] = null;
    }
    size = newSize;
}
    
/**
* clear()方法
**/
// 清空List中所有元素
public void clear() {
    modCount++;
    // 把元素指针全置为null,让GC回收
    for (int i = 0; i < size; i++)
        elementData[i] = null;
    size = 0;
}
```

### 9.其他常用方法

```java
/**
* toArray()方法
* 1. 复制一个新数组Object[],复制值而不是复制引用,修改新数组不会影响到旧数组.
* 2. 底层用 Arrays.copyOf()方法实现
**/
public Object[] toArray() {
    return Arrays.copyOf(elementData, size);
}

/**
* toArray()方法: 将当前集合复制到目标数组
* 1. 如果目标数组a的长度 小于 当前集合的长度,则只复制目标数组长度的元素.
* 2. 如果目标数组a的长度 大于 当前集合的长度,则a中不属于集合长度的元素不会被改变,除了第一个值被置为null.
**/
public <T> T[] toArray(T[] a) {
    if (a.length < size)
        return (T[]) Arrays.copyOf(elementData, size, a.getClass());

    System.arraycopy(elementData, 0, a, 0, size);
    
    if (a.length > size)
        a[size] = null;
    return a;
}

/**
* clone()方法
* 1. 复制新的ArrayList对象,修改新对象不会影响到旧对象.
* 2. 底层用 Arrays.copyOf()方法实现
**/
public Object clone() {
    try {
        ArrayList<?> v = (ArrayList<?>) super.clone();
        v.elementData = Arrays.copyOf(elementData, size);
        v.modCount = 0;
        return v;
    } catch (CloneNotSupportedException e) {
        // this shouldn't happen, since we are Cloneable
        throw new InternalError(e);
    }
}

/**
* contains()方法:判断是否存在对象o
**/
public boolean contains(Object o) {
    return indexOf(o) >= 0;
}

// 返回对象o第一次出现的索引位置,不存在则返回-1
public int indexOf(Object o) {
    if (o == null) {
        for (int i = 0; i < size; i++)
            if (elementData[i]==null)
                return i;
    } else {
        for (int i = 0; i < size; i++)
            // 比较的方法equals()
            if (o.equals(elementData[i]))
                return i;
    }
    return -1;
}

/**
* isEmpty()方法:只能判断是否没有元素,不能判断对象是否为null
**/
public boolean isEmpty() {
    return size == 0;
}

/**
* subList()方法
* 1. 返回一个[fromIndex,toIndex)子集合 
* 2. 本质上是对原集合的引用,对该子集合的修改会影响到原集合
* 3. 取一个子集合,最好使用新建一个集合的方法去复制。
**/
public List<E> subList(int fromIndex, int toIndex) {
    subListRangeCheck(fromIndex, toIndex, size);
    return new SubList(this, 0, fromIndex, toIndex);
}

private class SubList extends AbstractList<E> implements RandomAccess {..}
```





## Vector

