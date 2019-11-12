## LinkedHashMap

### 1.基本介绍

- LinkedHashMap是HashMap的子类。同时内部还有一个双向链表维护键值对的顺序，每个键值对既位于哈希表中，也位于这个双向链表中。
- LinkedHashMap支持两种顺序，一种是插入顺序，另外一种是访问顺序。
  1. 插入顺序：先添加的在前面，后添加的在后面，修改操作不影响顺序。(先进先出,类似于队列)
  2. 访问顺序：所谓访问是指get/put操作，对一个键执行get/put操作后，其对应的键值对会移到链表末尾。最末尾的就是最近访问的，最开始的是最久没被访问的。 
- 在链表中的顺序默认是插入顺序，也可以配置为访问顺序。LinkedHashMap及其节点类LinkedHashMap.Entry重写了若干方法以维护这种关系。

### 2.实现原理

<img src="https://javanote.oss-cn-shenzhen.aliyuncs.com/12_LinkedHashMap原理图.png" style="zoom: 80%;" />

- LikedHashMap其结构在HashMap结构上增加了链表结构。每一个bucket组成一个双链表,再将多个bucket链接起来,最后整个map成为一个双链表。数据结构为(数组+单链表+红黑树+双链表)
- Map接口的实现类都有一个对应的Set接口的实现类，比如HashMap有HashSet，TreeMap有TreeSet。
- LinkedHashMap也有一个对应的Set接口的实现类LinkedHashSet。LinkedHashSet是HashSet的子类，内部Map的实现类是LinkedHashMap，可以保持插入顺序。

### 3.基本用法

- 构造方法分类

  ```java
  // 插入顺序
  public LinkedHashMap()
  public LinkedHashMap(int initialCapacity)
  public LinkedHashMap(int initialCapacity, float loadFactor)
  public LinkedHashMap(Map<? extends K, ? extends V> m)
  
  // 访问顺序
  // 参数accessOrder：指定是否按访问顺序(true就是访问顺序)
  public LinkedHashMap(int initialCapacity,float loadFactor,boolean accessOrder) 
  ```

- 插入顺序实例

  ```java
  /** 
  * 插入顺序相当于队列,先进先出
  * 应用背景：
  * Map经常用来处理一些数据。其处理模式是,键值对输入→处理→输出,输出时希望保持原来的顺序。
  * 例如使用键值对格式的配置文件,有一些配置项是重复的,要去重并保留最后重复的一项,然后按原来的键顺序输出。
  * LinkedHashMap就是一个合适的数据结构。
  **/
  public static void main(String[] args){
      // 初始化为插入顺序
      Map<String,String> map = new LinkedHashMap<>();
  
      for(int i=1; i<100; i++) {
          map.put(i+"","value");
      }
      // 循环取出Map中元素
      for(Map.Entry entry : map.entrySet()) {
          System.out.println(
              String.format("key:[%s],value:[%s]",entry.getKey(),entry.getValue()));
      }
  }
  ```

- 访问顺序实例

  ```java
  /**
  * 访问顺序: 内部链表的排序为[非最近访问 → 最近访问]
  * 应用背景：(LRU算法)
  * 一般而言，缓存容量有限，不能无限存储所有数据。
  * 如果缓存满了，当需要存储新数据时，就需要将一些老的数据清理出去，采用的策略一般称为替换算法。
  * LRU被称为“最近最少使用”替换算法。思路是，最近刚被使用的很快再次被用的可能性最高，而最久没被访问的很快再次被
  * 用的可能性最低，所以被优先清理。(所以刚被使用的放到最底层，防止被清理。)
  **/
  public static void main(String[] args){
      // 初始化为访问顺序
      Map<String,String> map = new LinkedHashMap<>(16,0.75f,true);
  
      for(int i=1; i<10; i++) {
          map.put(i+"","value");
      }
      // 循环取出Map中元素
      for(Map.Entry entry : map.entrySet()) {
          System.out.println(
              String.format("key:[%s],value:[%s]",entry.getKey(),entry.getValue()));
      }
  }
  ```

## 方法的具体实现

### 1.主要属性

```java
// 双向链表的头部
transient LinkedHashMap.Entry<K,V> head;	 	
// 双向链表的尾部
transient LinkedHashMap.Entry<K,V> tail;    	
// true为插入顺序，false为访问顺序
final boolean accessOrder;  		
```

### 2.内部类Entry

```java
/**
* 继承HashMap中的node类，带有双向链表的指针before和after。
* Entry类相当于在HashMap的Node类包装了双向链表的属性。
* HashMap是由Node类组成,LinkedHashMap是由Entry类组成的。
**/
static class Entry<K,V> extends HashMap.Node<K,V> {
    Entry<K,V> before, after;
    Entry(int hash, K key, V value, Node<K,V> next) {
        super(hash, key, value, next);
    }
}
```

### 3.构造方法

```java
// 默认无参初始化,插入顺序
public LinkedHashMap() {
    super();
    accessOrder = false;
}

// initialCapacity为数组长度,插入顺序
public LinkedHashMap(int initialCapacity) {
    super(initialCapacity);
    accessOrder = false;
}

// initialCapacity数组长度,loadFactor负载因子,插入顺序
public LinkedHashMap(int initialCapacity, float loadFactor) {
    super(initialCapacity, loadFactor);
    accessOrder = false;
}

// initialCapacity数组长度,loadFactor负载因子,accessOrder选择顺序(插入顺序false,访问顺序true)
public LinkedHashMap(int initialCapacity,float loadFactor,boolean accessOrder) {
    super(initialCapacity, loadFactor);
    this.accessOrder = accessOrder;
}
```

### 4.添加元素

- 由于LikedHashMap继承于HashMap，可以直接调用HashMap的put方法。关键在只需重写关键方法即可，就是方法newNode()。

- 当LinkedHashMap是访问顺序时,链表的变换原理。

  <img src="https://javanote.oss-cn-shenzhen.aliyuncs.com/13_访问顺序原理.png" style="zoom: 80%;" />

```java
// 创建一个新的Map结点
Node<K,V> newNode(int hash, K key, V value, Node<K,V> e) {
    // 创建一个新的Entry对象,此时before和after都为空
    LinkedHashMap.Entry<K,V> p = new LinkedHashMap.Entry<K,V>(hash, key, value, e);
    linkNodeLast(p);
    return p;
}

// 将结点添加到最后一位
private void linkNodeLast(LinkedHashMap.Entry<K,V> p) {
    // 取出旧尾结点
    LinkedHashMap.Entry<K,V> last = tail;
    // 将双向链表指针指向当前结点
    tail = p;
    
    // 当 旧尾结点 为空时,将头指针和尾真正都指向 当前结点P
    if (last == null)
        head = p;
    else {
        // 将当前结点p的前一位before 指向旧尾结点
        p.before = last;
        // 将旧尾结点的后一位after 指向p
        last.after = p;
    }
}

// 保证访问顺序的回调函数：将执行过get或put的结点,移动到链表的末尾
void afterNodeAccess(Node<K,V> e) { 
    LinkedHashMap.Entry<K,V> last;
    
    // 如果是访问顺序,将该结点移动到双向链表的末尾
    if (accessOrder && (last = tail) != e) {
        LinkedHashMap.Entry<K,V> p = (LinkedHashMap.Entry<K,V>)e, b = p.before, a = p.after;
        p.after = null;
        
        // 相当于p是头结点
        if (b == null)
            head = a;
        else
            b.after = a;
        
        // 相当于p不是尾结点
        if (a != null)
            a.before = b;
        else
            last = b;
        
        // 相当于链表的尾结点为空
        if (last == null)
            head = p;
        else {
            p.before = last;
            last.after = p;
        }
        
        tail = p;
        ++modCount;
    }
}
```

### 5.获取元素

```java
public V get(Object key) {
    Node<K,V> e;
    // 调用HashMap的getNode()方法获取结点
    if ((e = getNode(hash(key), key)) == null)
        return null;
    // 如果是访问顺序,将当前结点放到链表的末尾
    if (accessOrder)
        afterNodeAccess(e);
    return e.value;
}
```

