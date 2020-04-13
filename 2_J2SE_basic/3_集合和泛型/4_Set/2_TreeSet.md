## 基础概念

### 1.TreeSet定义及特点

- `TreeSet`是有序的，与`HashSet`和`HashMap`的关系一样，`TreeSet`是基于`TreeMap`的。
- 没有重复元素。
- 添加、删除元素、判断元素是否存在，时间复杂度和`TreeMap`一样，为O(log2(N))，N为元素个数。
- 有序，TreeSet同样实现了`SortedSet`和`NavigatableSet`接口，可以方便的根据顺序进行查找和操作，如第一个、最后一个、某一取值范围、某一值的邻近元素等。
- 为了有序，`TreeSet`要求元素实现`Comparable`接口或通过构造方法提供一个`Comparator`对象。

### 2.构造方法

```java
// 新建一个TreeMap实例-内比较器
public TreeSet() {
    this(new TreeMap<E,Object>());
}

// 新建一个TreeMap实例-外比较器
public TreeSet(Comparator<? super E> comparator) {
    this(new TreeMap<>(comparator));
}

// 传入TreeMap的实例构建,NavigableMap接口的主要实现类是TreeMap
TreeSet(NavigableMap<E,Object> m) {
    this.m = m;
}

// 传入SortedSet实例构建-外比较器
public TreeSet(SortedSet<E> s) {
    this(s.comparator());
    addAll(s);
}

// 传入集合实例构建-内比较器
public TreeSet(Collection<? extends E> c) {
    this();
    addAll(c);
}
```

## 底层原理

### 1.基本属性

```java
// 固定的value值
private static final Object PRESENT = new Object(); 	
// 底层的TreeMap
private transient NavigableMap<E,Object> m;	
```

### 2.添加
```java
// 添加单个元素
public boolean add(E e) {
    // 调用map的put方法
    return m.put(e, PRESENT)==null;
}

/**
* 添加集合中的全部元素
**/
public  boolean addAll(Collection<? extends E> c) {
    
    if (m.size()==0 && c.size() > 0 && c instanceof SortedSet && m instanceof TreeMap) {
        // 转换为SortedSet和TreeMap
        SortedSet<? extends E> set = (SortedSet<? extends E>) c;
        TreeMap<E,Object> map = (TreeMap<E, Object>) m;
        
        // 获取外比较器
        Comparator<?> cc = set.comparator();
        Comparator<? super E> mc = map.comparator();
        
        // 当外比较器一样
        if (cc==mc || (cc != null && cc.equals(mc))) {
            map.addAllForTreeSet(set, PRESENT);
            return true;
        }
    }
    // 内比较器的方法
    return super.addAll(c);
}
```

### 3.删除
```java
// 调用map的删除方法
public boolean remove(Object o) {
    return m.remove(o)==PRESENT;
}
```

### 4.子集视图
```java
public SortedSet<E> subSet(E fromElement, E toElement) {
    return subSet(fromElement, true, toElement, false);
}

public NavigableSet<E> subSet(E fromElement, boolean fromInclusive,E toElement, boolean toInclusive) {
    // 返回一个新的子集,对子集的修改不会影响到原来的Set
    return new TreeSet<>(m.subMap(fromElement, fromInclusive,toElement, toInclusive));
}
```

