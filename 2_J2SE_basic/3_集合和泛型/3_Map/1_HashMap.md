## 基础概念

### 1.背景

- HashMap继承于AbstractMap抽象类，后者实现了Map接口。
- HashMap是基于Hash Table（哈希表）实现的。
- 从Java2到Java8，HashMap的实现原理不停地发生变化，实现HashMap时一个重要的考量，就是如何尽可能地规避哈希碰撞。

### 2. 哈希分布和哈希碰撞

- 完美哈希函数

  1. 完美哈希函数(简称PHF)是没有冲突的哈希函数,也就是函数H将N个KEY值映射到M个整数上,这里 M>=N。对于任意的 KEY1,KEY2, 哈希函数 H(KEY1) != H(KEY2)。
  2. 如果 M = = N ,则 H 是最小完美哈希函数(简称MPHF)。
  3. 完美哈希函数是静态的,就意味着事前必须知道需要哈希的所有数据。同时生成的算法比较复杂,需要很长的时间来建立索引。没有办法实时添加更新,给他的应用范围提了个极大的限制。

- 哈希碰撞(哈希值一样)

  1. 定义：如果两个输入串的hash函数的值是一样的，则称这两个串是一个哈希碰撞。

  2. 基本类型的哈希：一个Boolean对象有true和false两个值，因此Boolean对象的Hash值可以通过一个二进制位bit表达，即0b0, 0b1。对于一些Number对象，比如Integer、Long、Double等，他们都可以使用自身原始的值作为Hash值。


  3. 原因：即使能够为每个POJO或者String对象构造一个理论上不会有冲突的哈希函数，但是hashCode()函数的返回值是int型。根据鸽笼理论，当我们的对象超过2^32个时，这些对象会发生哈希碰撞。

- 哈希实现(用哈希表来存储)

  1. 两个相同哈希值的数也可以在哈希表中存储。通过允许哈希碰撞来节省内存，可以用来提升总体性能。

  2. 许多关联数组的实现，包括HashMap，使用了大小为M的桶来储存N个对象( M≤N )。

  3. 在这种情况下，我们使用模值hashValue % M作为桶的索引，而不是hashValue本身。

     代码实现：int index = X.hashCode() % M

- HashMap中解决哈希碰撞的方式(存储的方式)

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/7_分离链接法.png)

  buckets称为哈希表(散列表,哈希桶)。先获取key的哈希值，通过哈希值计算得到哈希表的存储位置(如上图中,JonhnSminth计算得到152)，再将key-value存储到哈希表152的位置。

  HashMap中存储数据,解决哈希冲突的方式有两种。

  **开放寻址**

  1. 根据key的哈希值计算出存储的表索引。如果已经被占据,则通过一定的寻找方式来找到空白的索引点。如果最后找不到，则扩充表。
  2. 一个索引地址只能存放一个值。适合数量确定，冲突比较少的情况。

  **分离链接**

  1. 将哈希表的每个索引作为一个链表的头结点。当插入的key2计算出的索引已经被key1占据时,则将key2插入到key1后面,共同存放在一个索引下。
  2. 一个索引地址可以存放多个值，每个索引都可以当做一个链表。如果链表过长,也需要扩充表索引。
  3. 查找值的时候,则需先查索引,再遍历该索引下的链表。

  **采用分离链接的原因**

  1. 数据量小的时候，开放寻址可以加载到缓存，效率比较高。
  2. 数据量大的时候，开放寻址就是一个大型数组，效率低。而当分离链接的链表不长时，时间复杂度就是O(1)。
  3. 在调用remove频繁的情况下，开放寻址会干扰总体的性能。

### 3.Java8中的分离链表

- 主要改变

  1. 在Java8之前,假设对象的Hash值服从平均分布,那么获取一个对象需要的次数时间复杂度应该是O(N/M)。
  2. Java8在没有降低哈希冲突的度的情况下,使用红黑树代替链表,将这个值降低到了O(log(N/M))。数据越多,O(NM)和O(log(NM))的差别就会越明显。
  3. 在实际应用中,哈希值有时也会集中在几个特定值上。因此使用平衡树比如红黑树有着比使用链表更强的性能。

- 链表和树的切换标识

  1. 使用链表还是树，与哈希桶中的元素数目有关。定义了Java8的HashMap在使用树和使用链表之间切换的阈值。
  2. 当冲突的元素数增加到8时，链表变为树.当减少至6时，树切换为链表。中间有2个缓冲值的原因是避免频繁的切换浪费计算机资源。

  ```java
  // 切换为红黑树
  static final int TREEIFY_THRESHOLD = 8;
  // 切换为链表
  static final int UNTREEIFY_THRESHOLD = 6;
  ```

- 红黑树的使用

  1. 使用Node类替代了Entry类，Node类具有导出类TreeNode，通过这种继承关系，一个链表很容易被转换成树。
  2. 红黑树的实现基本与JCF中的TreeMap相同。通常，树的有序性通过两个或更多对象比较大小来保证。
  3. 在HashMap中,树一般是通过对象的Hash值作为对象的排序键。使用对象的hash值来作为排序键,有一个tieBreakOrder()方法来处理这个排序。

- 哈希桶的动态扩容

  1. 小数目的哈希桶可以有效的利用内存，但是会产生更高概率的哈希碰撞，最终损失性能。
  2. HashMap会在数据量达到一定大小时，将哈希桶的数量扩充到两倍。当哈希桶的数量变为两倍后，N/M会对应下降，哈希桶索引值重复的Key的数量也得以减少。
  3. 哈希桶的默认数量是16，最大值是2^30。当哈希桶的数量成倍增长时，所有的数据需要重新插入。
  4. 如果使用包含桶数量的构造器，构造HashMap时,可以节约不必要的重新构造分离链表的时间。

- 哈希桶扩容的临界值

  1. 确定是否需要对桶进行扩展的临界值是 [loadFactor × currentBucketSize]，其中loadFactor是负载因子，currentBucketSize是当前桶的数量。
  2. 当数据量到达这个大小时，扩容就会发生，直到桶的数量达到2^30为止。
  3. 默认的负载因子是0.75，它与默认桶大小16，一同作为构造默认的HashMap的参数。

- 辅助哈希函数

  ```java
  // 作用：使用辅助哈希函数的目的是通过改变初始的哈希值，降低发生哈希冲突的概率
  static final int hash(Object key) {
      int h;
      return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
  }
  ```

- String对象的哈希函数

  ```java
  // String对象的Hash函数的时间开销与String值的长度成正比。
  public int hashCode() {
      int h = hash;
      if (h == 0 && value.length > 0) {
          char val[] = value;
  
          for (int i = 0; i < value.length; i++) {
              h = 31 * h + val[i];
          }
          hash = h;
      }
      return h;
  }
  ```

  

##HashMap的底层实现

### 1.存储结构-字段

<img src="https://javanote.oss-cn-shenzhen.aliyuncs.com/8_HashMap的数据结构.png" style="zoom:50%;" />

数据结构上,HashMap是数组+链表+红黑树(JDK1.8增加了红黑树部分)实现的。

- 哈希表索引的数组(上图数组中的黑点)

  ```java
  public class HashMap<K,V> {
      // 数组对象
  	transient Node<K,V>[] table;
  	
      // 内部类Node,本身上是一个映射(键值对),就是链表或者树上的元素
      static class Node<K,V> implements Map.Entry<K,V> {
          final int hash;
          final K key;
          V value;
          Node<K,V> next;
          
          Node(int hash, K key, V value, Node<K,V> next) {
              this.hash = hash;
              this.key = key;
              this.value = value;
              this.next = next;
          }
      }
  }
  ```

- 哈希表的实现

  ```java
  map.put("美团","小美");
  /**
  * 1. 系统调用”美团”这个key的hashCode()方法得到其hashCode值(该方法适用于每个Java对象)。
  * 2. 然后再通过Hash算法的后两步运算(高位运算和取模运算)来定位该键值对的存储位置。
  * 3. 有时两个key会定位到相同的位置，表示发生了Hash碰撞。
  * 4. 发生Hash碰撞后,将两个key都存入到该索引上的链表中。
  **/
  ```

  1. 声明Map对象后,内部哈希表还没有初始化。当调用第一个put()方法时,才会初始化哈希表。
  2. 在每个数组元素上都一个链表结构。当数据被Hash后，得到数组下标，把数据放在对应下标元素的链表上。
  3. Hash算法计算结果越分散均匀，Hash碰撞的概率就越小，map的存取效率就会越高。
  4. 哈希桶数组较大，较差的hash算法也会比较分散，但是所占的空间就比较大。哈希桶数组较小，要求的hash算法也比较高，否则哈希碰撞会变多，花费的时间会变长。

- HashMap中重要的属性

  ```java
  // HashMap底层的实际数组实际占内存总大小，哈希桶数量。
  transient Node<K,V>[] table; 	
  // HashMap中实际存在的键值对数量
  transient int size; 			
  // HashMap所能容纳的最大数据量的Node(键值对)个数
  int threshold;				
  // 负载因子
  final float loadFactor;		
  ```

  **负载因子**

  1. 是哈希表在其容量自动增加之前,评价容量已经被填充的一种尺度。衡量的是一个散列表的空间的使用程度，负载因子越大表示散列表的装填程度越高，反之愈小。

  2. 对于使用链表法的散列表来说，查找一个元素的平均时间是O(1+a)。

     如果负载因子越大，对空间的利用更充分，然而后果是查找效率的降低。

     如果负载因子太小，那么散列表的数据将过于稀疏，对空间造成严重浪费。

  3. 系统默认负载因子为0.75，一般情况下我们是无需修改的。

  4. 当哈希表中的条目数超出了加载因子与当前容量的乘积时，则要对该哈希表进行 rehash 操作(即重建内部数据结构)，从而哈希表将具有大约两倍的桶数。

  **阈值**

  1. 就是在此 [Loadfactor*length(数组长度)] 对应下允许的最大数组元素数目，size超过这个数目就重新resize(扩容)，扩容后的HashMap容量是之前容量的两倍。

### 2.功能实现-方法

#### 2.1确定哈希表(哈希桶)数组索引位置

哈希算法分为三步：取key的hashCode值、高位运算、取模运算

```java
/**
* hash()和indexFor()方法
* jdk1.8改进了hash()方法,删除了1.7的indexFor()方法
**/
static final int hash(Object key) {   
    int h;
    // h = key.hashCode() 为第一步 取hashCode值
    // h ^ (h >>> 16)     为第二步 高位参与运算
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}

static int indexFor(int h, int length) {  
    // h & (length-1)     为第三步 取模运算
    return h & (length-1);  
}
```

- 高位参与运算(扰动函数)以及取模运算

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/9_哈希算法.jpg)

  1. Object类的hashCode()方法一般返回32bit的哈希值。

  2. h ^ (h >>> 16) ：哈希值右位移16位，正好是32bit的一半。然后将本身的高半区和低半区做异或，是为了 **混合原始哈希码的高位和低位，以此来加大低位的随机性，减少碰撞率**。

     与运算是提取共同特征，或运算是综合各自特征，异或运算是只提取不同特征，舍弃共同特征。

  3. ( n-1 ) & hash： HashMap的数组长度要取2的整次幂 ，这样低位都是“11111”，相当于一个“低位掩码”，只提取hash值的低位特征。

#### 2.2初始化哈希表(put方法)

- 基本流程
  1. 传入一个key,通过哈希算法计算出散列表(哈希桶)的索引。
  2. 如果散列表当前索引处(table[i])为null,则新建一个节点当做链表的头结点。
  3. 如果table[i]不为null,判断头结点与插入值的key是否相等,如果相等则更新value值。
  4. 如果头结点key不相等,判断头结点是否为红黑树,如果是树则将结点传入树内。如果不是,进入链表循环筛选。
  5. 判断头结点p.next是否为空,如果为空则插入当前键值对。并且判断链表长度,如果大于等于8则转为红黑树处理。
  6. 如果p.next不为空,则判断这个节点是否等于key,如果是则更新value值,如果不是则继续下一个节点。
  7. 如果e值为空则说明新建了一个节点,将size的长度增加1并检查散列表长度是否需要扩容。如果e值不为空,则说明用新value值覆盖了旧value值,返回旧value值,不用增加size的长度。

```java
/**
* put()方法：往Map中添加键值对
**/
public V put(K key, V value) {
    return putVal(hash(key), key, value, false, true);
}

// 初始化哈希表的核心方法
final V putVal(int hash, K key, V value, boolean onlyIfAbsent,boolean evict) {
    Node<K,V>[] tab;  // 结点(散列表)数组
    Node<K,V> p; // 结点(数组元素)
    int n, i; // n为散列表长度
    
    // 如果散列表table为空或长度为0,则扩容散列表
    if ((tab = table) == null || (n = tab.length) == 0)
        n = (tab = resize()).length;
    
    // [i = (n - 1) & hash] 为取模后,哈希桶的索引值
    // 如果此索引值上没有结点,则新建一个结点
    if ((p = tab[i = (n - 1) & hash]) == null)
        tab[i] = newNode(hash, key, value, null);
    else {
        // 当 e != null 时,表示key值重复,替换了key的value.
        // 当 e == null 时,表示新建了一个结点
        Node<K,V> e; 
        K k;
        
        /**
        * p为哈希桶当前索引上的值, p.hash == hash 表示结点和插入key的哈希值相等
        * (k = p.key) == key || (key != null && key.equals(k)) 
        * 表示key值"=="相等 或 key不等于null且equals相等
        * == 是比较内存地址,equals是可以重写的
        * 含义：哈希桶当前索引上的结点 和 插入的键值对 key相等,value值会被替换
        **/
        if (p.hash == hash && ((k = p.key) == key || (key != null && key.equals(k))))
            // 将 e 指向 哈希桶当前索引上的结点对象p
            e = p;
        
        // TreeNode表示红黑树,即判定p是否为红黑树,如果是红黑树则直接插入树中
        else if (p instanceof TreeNode)
            e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
        else {
            for (int binCount = 0; ; ++binCount) {
                // 如果哈希桶当前索引上 只有一个结点, 即链表上只有一个头结点
                if ((e = p.next) == null) {
                    // 将插入键值对新建为一个结点,放在头结点p后面
                    p.next = newNode(hash, key, value, null);
                    // static final int TREEIFY_THRESHOLD = 8; 当大于等于8,转换为红黑树
                    if (binCount >= TREEIFY_THRESHOLD - 1) 
                        treeifyBin(tab, hash);
                    break;
                }
                // 如果在链表上发现key值相等的结点
                if (e.hash == hash && ((k = e.key) == key || (key != null && key.equals(k))))
                    break;
                
                // 链表往后移动一位
                p = e;
            }
        }
        
        // 如果当前插入的 键值对结点 不为空
        // 当 e != null 时,表示key值重复,替换了key的value.
        // 当 e == null 时,表示新建了一个结点
        if (e != null) { 
            V oldValue = e.value;
            // onlyIfAbsent==false 
            // 用新的value值 替换 e.value旧值
            if (!onlyIfAbsent || oldValue == null)
                e.value = value;
            afterNodeAccess(e);
            return oldValue;
        }
       
    }
    
    ++modCount;
    // 此时 e == null 
    // 长度增加1,并且判断长度是否大于阈值,如果大于则开始扩容
    if (++size > threshold)
        resize();
    // 暂时没用
    afterNodeInsertion(evict);
    return null;
}
```

### 3.扩容机制

#### 3.1JDK1.7的扩容方法  

```java
final Node<K,V>[] resize() {
    Node<K,V>[] oldTab = table;
    int oldCap = (oldTab == null) ? 0 : oldTab.length;
    int oldThr = threshold;
    int newCap, newThr = 0;
    if (oldCap > 0) {
        if (oldCap >= MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return oldTab;
        }
        else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                 oldCap >= DEFAULT_INITIAL_CAPACITY)
            newThr = oldThr << 1; // double threshold
    }
    else if (oldThr > 0) // initial capacity was placed in threshold
        newCap = oldThr;
    else {               // zero initial threshold signifies using defaults
        newCap = DEFAULT_INITIAL_CAPACITY;
        newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
    }
    if (newThr == 0) {
        float ft = (float)newCap * loadFactor;
        newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                  (int)ft : Integer.MAX_VALUE);
    }
    threshold = newThr;
    @SuppressWarnings({"rawtypes","unchecked"})
    Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
    table = newTab;
    if (oldTab != null) {
        for (int j = 0; j < oldCap; ++j) {
            Node<K,V> e;
            if ((e = oldTab[j]) != null) {
                oldTab[j] = null;
                if (e.next == null)
                    newTab[e.hash & (newCap - 1)] = e;
                else if (e instanceof TreeNode)
                    ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                else { // preserve order
                    Node<K,V> loHead = null, loTail = null;
                    Node<K,V> hiHead = null, hiTail = null;
                    Node<K,V> next;
                    do {
                        next = e.next;
                        if ((e.hash & oldCap) == 0) {
                            if (loTail == null)
                                loHead = e;
                            else
                                loTail.next = e;
                            loTail = e;
                        }
                        else {
                            if (hiTail == null)
                                hiHead = e;
                            else
                                hiTail.next = e;
                            hiTail = e;
                        }
                    } while ((e = next) != null);
                    if (loTail != null) {
                        loTail.next = null;
                        newTab[j] = loHead;
                    }
                    if (hiTail != null) {
                        hiTail.next = null;
                        newTab[j + oldCap] = hiHead;
                    }
                }
            }
        }
    }
    return newTab;
}
```



#### 3.2JDK1.8对扩容机制的优化  



### 4.线程安全

### 5.JDK1.8和JDK1.7性能对比 

## 具体方法实现

### 1.主要属性

### 2.构造方法

### 3.常用方法

### 4.获取视图



