## 基础概念

### 1.Set接口

- Set表示的是**没有重复元素、且不保证顺序**的容器接口。扩展了Collection，没有定义任何新的方法，重写了Collection中的一些方法。

  ```java
  public interface Set<E> extends Collection<E> {
      // set中存储值的数量
      int size();                 
      // 是否为空的标志
      boolean isEmpty();          
      // 判断包含某个元素
      boolean contains(Object o); 
      // 返回迭代器
      Iterator<E> iterator();     
      // 将set变成数组返回
      Object[] toArray();         
      <T> T[] toArray(T[] a);     
      // 添加一个元素
      boolean add(E e);           
      // 删除一个对象
      boolean remove(Object o);   
      // 是否包含某一个Collection
      boolean containsAll(Collection<?> c);       
      // 添加某一个Collection
      boolean addAll(Collection<? extends E> c);  
      boolean retainAll(Collection<?> c); 
      // 删除某一个Colletion
      boolean removeAll(Collection<?> c);     
      // 清空所有元素
      void clear();                           
      // 判断某个对象是否相等
      boolean equals(Object o);               
      // 获取hashCode值
      int hashCode();
  }
  ```
  

### 2.应用场景

- 排重。如果对排重后的元素没有顺序要求，则HashSet可以方便的用于排重。
- 保存特殊值。Set可以用于保存各种特殊值，程序处理用户请求或数据记录时，根据是否为特殊值，进行特殊处理，比如保存IP地址的黑名单或白名单。
- 集合运算。使用Set可以方便的进行数学集合中的运算，如交集、并集等运算。比如用户标签计算，每个用户都有一些标签，两个用户的标签交集就表示他们的共同特征，交集大小除以并集大小可以表示他们的相似长度。

### 3.HashSet特点

- HashSet底层实现为HashMap。HashMap和HashSet都要求元素重写hashCode和equals方法。对于两个对象，equals相同，则hashCode也必须相同。
- 没有重复元素，添加重复元素会返回false。HashSet中的元素是HashMap中的key键。key值为唯一的，不会出现重复元素。
- 可以高效的添加、删除元素、判断元素是否存在，时间复杂度和HashMap一样。
- 元素没有顺序，因为在HashMap中key值是没有顺序的。

### 3.底层实现原理

```java
// HashSet内部是用HashMap实现的，内部有一个HashMap实例变量
private transient HashMap<E,Object> map;

// Map有键和值，HashSet相当于只有键，值都是相同的固定值。下面为固定值。
private static final Object PRESENT = new Object();
```

### 4.构造方法

```java
// 无参构造方法
public HashSet() {
    // 新建一个HashMap对象
    map = new HashMap<>();
}

// initialCapacity：数组容量
public HashSet(int initialCapacity) {
    map = new HashMap<>(initialCapacity);
}

// initialCapacity：数组容量    loadFactor：负载因子
public HashSet(int initialCapacity, float loadFactor) {
    map = new HashMap<>(initialCapacity, loadFactor);
}

// initialCapacity：数组容量    loadFactor：负载因子   dummy
HashSet(int initialCapacity, float loadFactor, boolean dummy) {
    map = new LinkedHashMap<>(initialCapacity, loadFactor);
}

// 利用集合构建HashSet
public HashSet(Collection<? extends E> c) {
    // 初始化容量
    map = new HashMap<>(Math.max((int) (c.size()/.75f) + 1, 16));
    addAll(c);
}
```

## 具体方法实现

### 1.添加

```java
/**
* 调用map的put方法，key为元素e，value为固定值PRESENT。
* put返回null表示添加了新key，添加成功了。否则返回存在key的oldValue。
**/
public boolean add(E e) {
    // HashMap中如果添加新元素则返回null,如果key已存在返回oldvalue
    return map.put(e, PRESENT)==null;
}


```

### 2.删除

```java
/**
* 调用map的remove()方法。如果返回存在key的value表示删除成功,否则返回null。
**/
public boolean remove(Object o) {
    return map.remove(o)==PRESENT;
}
```

### 3.包含

```java
/**
* 调用map的containsKey()方法。
**/
public boolean contains(Object o) {
    return map.containsKey(o);
}
```











