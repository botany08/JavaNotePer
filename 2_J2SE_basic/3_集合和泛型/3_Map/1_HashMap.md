##  基础概念

### 1.背景

- `HashMap`继承于`AbstractMap`抽象类，后者实现了`Map`接口。
- `HashMap`是基于`HashTable`(哈希表)实现的。
- 从Java2到Java8，`HashMap`的实现原理不停地发生变化，实现`HashMap`时一个重要的考量，就是如何**尽可能地规避哈希碰撞**。

### 2. 哈希分布和哈希碰撞

- **完美哈希函数**
  1. 完美哈希函数(简称PHF)是没有冲突的哈希函数,也就是函数H将N个KEY值映射到M个整数上,这里 `M>=N`。对于任意的 `KEY1`,`KEY2`, 哈希函数 `H(KEY1) != H(KEY2)`。
  2. 如果 M = = N ,则 H 是**最小完美哈希函数**(简称MPHF)。
  3. 完美哈希函数是静态的,就意味着事前必须知道需要哈希的所有数据。同时生成的算法比较复杂,需要很长的时间来建立索引。没有办法实时添加更新,给他的应用范围提了个极大的限制。
  
- **哈希碰撞(哈希值一样)**
  1. 定义：如果两个输入串的hash函数的值是一样的，则称这两个串是一个哈希碰撞。
  2. 基本类型的哈希：一个Boolean对象有true和false两个值，因此Boolean对象的Hash值可以通过一个二进制位bit表达，即0b0, 0b1。对于一些Number对象，比如Integer、Long、Double等，他们都可以使用自身原始的值作为Hash值。
  3. 原因：即使能够为每个`POJO`或者`String`对象构造一个理论上不会有冲突的哈希函数，但是hashCode()函数的返回值是int型。根据鸽笼理论，当我们的对象超过2^32个时，这些对象会发生哈希碰撞。

- **哈希实现(用哈希表来存储)**

  1. 两个相同哈希值的数也可以在哈希表中存储。通过允许哈希碰撞来节省内存，可以用来提升总体性能。

  2. 许多关联数组的实现，包括HashMap，使用了大小为M的桶来储存N个对象( M≤N )。

  3. 在这种情况下，我们使用模值hashValue % M作为桶的索引，而不是hashValue本身。

     代码实现：int index = X.hashCode() % M

- **HashMap中解决哈希碰撞的方式(存储的方式)**

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/7_分离链接法.png)

  1. `buckets`称为哈希表(散列表,哈希桶)。先获取`key`的哈希值，通过哈希值计算得到哈希表的存储位置(如上图中,`JonhnSminth`计算得到152)，再将`key-value`存储到哈希表152的位置。

  2. `HashMap`中存储数据,**解决哈希冲突的方式**有两种。
     - **开放寻址**
       1. 根据key的哈希值计算出存储的表索引。如果已经被占据，则通过一定的寻找方式来找到空白的索引点。如果最后找不到，则扩充表。
       2. 一个索引地址只能存放一个值。适合数量确定，冲突比较少的情况。
     
     - **分离链接**
       1. 将哈希表的每个索引作为一个链表的头结点。当插入的`key2`计算出的索引已经被`key1`占据时,则将`key2`插入到`key1`后面,共同存放在一个索引下。
       2. 一个索引地址可以存放多个值，每个索引都可以当做一个链表。如果链表过长,也需要扩充表索引。
       3. 查找值的时候,则需先查索引,再遍历该索引下的链表。

     - **采用分离链接的原因**
       1. 数据量小的时候，开放寻址可以加载到缓存，效率比较高。
       2. 数据量大的时候，开放寻址就是一个大型数组，效率低。而当分离链接的链表不长时，时间复杂度就是O(1)。
       3. 在调用remove频繁的情况下，开放寻址会干扰总体的性能。

### 3.Java8中的分离链表

- **主要改变**

  1. 在Java8之前,假设对象的Hash值服从平均分布,那么获取一个对象需要的次数时间复杂度应该是`O(N/M)`。
  2. Java8在没有降低哈希冲突的度的情况下，使用红黑树代替链表，将这个值降低到了`O(log(N/M))`。数据越多,`O(N/M)`和`O(log(N/M))`的差别就会越明显。
  3. 在实际应用中,哈希值有时也会集中在几个特定值上。因此使用平衡树比如红黑树有着比使用链表更强的性能。

- **链表和树的切换标识**

  1. 使用链表还是树，与哈希桶中的元素数目有关。定义了Java8的`HashMap`在使用树和使用链表之间切换的阈值。
  2. 当冲突的元素数增加到8时，链表变为树。当减少至6时，树切换为链表。中间有2个缓冲值的原因是避免频繁的切换浪费计算机资源。

  ```java
  // 切换为红黑树
  static final int TREEIFY_THRESHOLD = 8;
  // 切换为链表
  static final int UNTREEIFY_THRESHOLD = 6;
  ```

- **红黑树的使用**

  1. 使用`Node`类替代了`Entry`类，`Node`类具有导出类`TreeNode`，通过这种继承关系，一个链表很容易被转换成树。
  2. 红黑树的实现基本与`JCF`中的`TreeMap`相同。通常，树的有序性通过两个或更多对象比较大小来保证。
  3. 在`HashMap`中,树一般是通过对象的`Hash`值作为对象的排序键。使用对象的`Hash`值来作为排序键,有一个`TieBreakOrder()`方法来处理这个排序。

- **哈希桶的动态扩容**

  1. 小数目的哈希桶可以有效的利用内存，但是会产生更高概率的哈希碰撞，最终损失性能。
  2. `HashMap`会在数据量达到一定大小时，将哈希桶的数量扩充到两倍。当哈希桶的数量变为两倍后，`N/M`会对应下降，哈希桶索引值重复的`Key`的数量也得以减少。
  3. 哈希桶的默认数量是16，最大值是2^30。当哈希桶的数量成倍增长时，所有的数据需要重新插入。
  4. 如果使用包含桶数量的构造器，构造`HashMap`时，可以节约不必要的重新构造分离链表的时间。

- **哈希桶扩容的临界值**

  1. 确定是否需要对桶进行扩展的临界值是 [loadFactor × currentBucketSize]，其中`loadFactor`是负载因子，`currentBucketSize`是当前桶的数量。
  2. 当数据量到达这个大小时，扩容就会发生，直到桶的数量达到`2^30`为止。
  3. 默认的负载因子是`0.75`，它与默认桶大小`16`，一同作为构造默认的Hash`M`ap的参数。

- **辅助哈希函数**

  ```java
  // 作用：使用辅助哈希函数的目的是通过改变初始的哈希值，降低发生哈希冲突的概率
  static final int hash(Object key) {
      int h;
      return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
  }
  ```

- **String对象的哈希函数**

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

数据结构上,`HashMap`是**数组+链表+红黑树**(JDK1.8增加了红黑树部分)实现的。

- **哈希表索引的数组(上图数组中的黑点)**

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

- **哈希表的实现**

  ```java
  map.put("美团","小美");
  /**
  * 1. 系统调用”美团”这个key的hashCode()方法得到其hashCode值(该方法适用于每个Java对象)。
  * 2. 然后再通过Hash算法的后两步运算(高位运算和取模运算)来定位该键值对的存储位置。
  * 3. 有时两个key会定位到相同的位置，表示发生了Hash碰撞。
  * 4. 发生Hash碰撞后,将两个key都存入到该索引上的链表中。
  **/
  ```

  1. 声明`Map`对象后,内部哈希表还没有初始化。当调用第一个`put()`方法时,才会初始化哈希表。
  2. 在每个数组元素上都一个链表结构。当数据被`Hash`后，得到数组下标，把数据放在对应下标元素的链表上。
  3. `Hash`算法计算结果越分散均匀，Hash碰撞的概率就越小，map的存取效率就会越高。
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

  1. 是哈希表在其容量自动增加之前，评价容量已经被填充的一种尺度。衡量的是一个散列表的空间的使用程度，负载因子越大表示散列表的装填程度越高，反之愈小。

  2. 对于使用链表法的散列表来说，查找一个元素的平均时间是`O(1+a)`。

     如果负载因子越大，对空间的利用更充分，然而后果是查找效率的降低。

     如果负载因子太小，那么散列表的数据将过于稀疏，对空间造成严重浪费。

  3. 系统默认负载因子为0.75，一般情况下是无需修改的。

  4. 当哈希表中的条目数超出了加载因子与当前容量的乘积时，则要对该哈希表进行`rehash`操作(即重建内部数据结构)，从而哈希表将具有大约两倍的桶数。

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

- **高位参与运算(扰动函数)以及取模运算**

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/9_哈希算法.jpg)

  1. `Object`类的`hashCode()`方法一般返回32bit的哈希值。

  2. h ^ (h >>> 16) ：哈希值右位移16位，正好是32bit的一半。然后将本身的高半区和低半区做异或，是为了 **混合原始哈希码的高位和低位，以此来加大低位的随机性，减少碰撞率**。

     **与运算是提取共同特征，或运算是综合各自特征，异或运算是只提取不同特征，舍弃共同特征。**

  3. ( n-1 ) & hash： HashMap的数组长度要取2的整次幂 ，这样低位都是“11111”，相当于一个“低位掩码”，只提取hash值的低位特征。

#### 2.2初始化哈希表(put方法)

- **基本流程**
  1. 传入一个`key`，通过哈希算法计算出散列表(哈希桶)的索引。
  2. 如果散列表当前索引处`(table[i])`为`null`，则新建一个节点当做链表的头结点。
  3. 如果`table[i]`不为`null`，判断头结点与插入值的key是否相等，如果相等则更新value值。
  4. 如果头结点`key`不相等，判断头结点是否为红黑树，如果是则将结点传入树内。如果不是，进入链表循环筛选。
  5. 判断头结点`p.next`是否为空，如果为空则插入当前键值对。判断链表长度，如果大于等于8则转为红黑树处理。
  6. 如`果p.next`不为空，则判断这个节点是否等于key，如果是则更新value值，如果不是则继续下一个节点。
  7. 如果e值为空则说明新建了一个节点，将size的长度增加1并检查散列表长度是否需要扩容。如果e值不为空，则说明用新value值覆盖了旧value值,返回旧value值,不用增加size的长度。

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

#### 3.1JDK1.8的扩容方法  

```java
/**
* 散列表(哈希表)扩容
**/
final Node<K,V>[] resize() {
    // 旧散列表数组
    Node<K,V>[] oldTab = table;
    // 旧散列表数组长度
    int oldCap = (oldTab == null) ? 0 : oldTab.length;
    // 阈值
    int oldThr = threshold;
    int newCap, newThr = 0;
    
    // 当数组长度 大于0
    if (oldCap > 0) {
        // static final int MAXIMUM_CAPACITY = 1 << 30; 数组最大长度 2^30
        if (oldCap >= MAXIMUM_CAPACITY) {
            // 当旧数组长度已经达到最大值,则将阈值提高到最大值[ 2^31-1 ],不再扩容返回旧数组
            threshold = Integer.MAX_VALUE;
            return oldTab;
        }
        /**
        * static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // 数组默认长度为 2^4[16]
        * static final int MAXIMUM_CAPACITY = 1 << 30; // 最大长度为 2^30
        * (newCap = oldCap << 1)：新容量为旧容量向左移动一位2^(n+1),同时新容量小于最大容量,旧容量大于默认
        *                         容量。
        **/
        else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY && oldCap >= DEFAULT_INITIAL_CAPACITY)
            // 阈值：新阈值等于旧阈值向左移动一位
            newThr = oldThr << 1; 
    }
    // 数组长度为0时,当旧阈值大于0,则新容量等于旧阈值
    else if (oldThr > 0) 
        newCap = oldThr;
    // 数组长度为0,旧阈值也为0时,初始容量等于默认长度2^4,初始阈值等于 0.75*16=12
    else {               
        newCap = DEFAULT_INITIAL_CAPACITY;
        newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
    }
    
    // 当新阈值等于0,计算新阈值
    if (newThr == 0) {
        // ft等于 新容量*负载因子
        float ft = (float)newCap * loadFactor;
        // 当新容量和新阈值 都小于最大容量2^30,取ft作为新阈值
        newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                  (int)ft : Integer.MAX_VALUE);
    }
    
    // HashMap的当前阈值 赋值为 新阈值
    threshold = newThr;
    @SuppressWarnings({"rawtypes","unchecked"})
   	// newCap 新容量初始化新数组 newTap
    Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
    // HashMap的当前散列表数组 赋值为新数组
    table = newTab;
    
    // 旧表不为空,表示旧表有数据,现在要把旧数据移动到新数组中
    if (oldTab != null) {
        // 循环旧散列表, 把旧bucket 移动到 新bucket
        for (int j = 0; j < oldCap; ++j) {

            Node<K,V> e;
            // 当数组内元素不等于空时
            if ((e = oldTab[j]) != null) {
                // 索引处置为空
                oldTab[j] = null;

                // 当索引的下一位为空,表示此哈希桶只有一个元素
                if (e.next == null)
                    // 直接取模运算将 旧元素放到新桶
                    newTab[e.hash & (newCap - 1)] = e;

                // 当索引处为红黑树,执行树操作
                else if (e instanceof TreeNode)
                    ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);

                // 当索引不为空且下一位不为空,表示此哈希桶是链表
                else {

                    // lo和hi就是low和high的意思。
                    // low表示扩容后不变位置的链表,即原数组部分。high表示重新散列到扩容的部分。
                    Node<K,V> loHead = null, loTail = null;
                    Node<K,V> hiHead = null, hiTail = null;
                    Node<K,V> next;

                    // 循环条件：索引处下一位一直不为空,就是循环整个链表
                    // 作用：把索引处的链表拆分成两条链表,一条是不移动位置的,一条是移动了 原索引+oldCap 
                    do {
                        next = e.next;

                        /**
                        * (e.hash & oldCap)作用是判断e的hash值是否大于旧数组长度,如果大于等于则重新分配,
                        *                       如果小于则保持不变
                        * 例如e的hash值是88,oldCap的大小是128,扩充的话是256。
                        * 所以无论扩充与否,这个e对数组长度取模还是88,这个位置是变不了的,所以不需要移动。
                        * 因为数组长度oldCap永远是2的幂次方,所以只有一个比特位。
                        * 1.当 e.hash < oldCap 的情况:
                        *      e.hash: 14  0000 1100
                        *      oldCap: 16  0001 0000
                        *      两者进行 & : 0000 0000          
                        * 这种情况是0,位置并没有改变,也就是e不移动。
                        *
                        * 2.当 e.hash = oldCap 的情况:
                        *      e.hash: 16  0001 0000
                        *      oldCap: 16  0001 0000
                        *      两者进行 & : 0001 0000     ->     
                        * 这种情况是16,也就是超出了oldCap的最大位置,要移动位置,新的位置是 原位置+oldCap。
                        * 原因是该元素应该放在oldCap的位置,但是原数组只有0~oldCap-1的索引,而新数组有
                        * 0~2*oldCap-1的索引。
                        * 例如：e.hash = 16 原本要放在索引为 [16&15 = 0] 的位置,但是由于数组要扩充,
                        * 所以要放到 16 % 31 = 16的位置。
                        *     16: 0001 0000
                        *     31: 0001 1111   
                        *     16 & 31 = 16;   
                        * 所以新数组的位置 16 = 原数组位置 0 + oldCap 16 = 16;
                        *
                        * 3.当 e.hash > oldCap 的情况:
                        *      e.hash: 20  0001 0100
                        *      oldCap: 16  0001 0000
                        *      两者进行 & : 0001 0000     ->     情况同上;
                        **/

                        // 此时e不移动位置,loHead指向链表头,loTail指向链表尾。
                        if ((e.hash & oldCap) == 0) {
                            if (loTail == null)
                                loHead = e;
                            else
                                loTail.next = e; 
                            loTail = e;
                        }
                        // 此时e会移动位置,hiHead指向链表头,hiTail指向链表尾。
                        else {
                            if (hiTail == null)
                                hiHead = e;
                            else
                                hiTail.next = e;
                            hiTail = e;
                        }
                    } while ((e = next) != null); // 循环至整个链表结束

                    // 原索引-不移动位置
                    if (loTail != null) {
                        loTail.next = null;
                        newTab[j] = loHead;
                    }

                    // 原索引+旧数组长度 - 移动位置
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

- **散列表扩容**
  
  1. 扩容(resize)就是重新计算容量，向HashMap对象里不停的添加元素。
  2. 当HashMap的散列表超过阈值时，就需要扩大数组长度。Java的数组是无法自动扩容的，只有使用一个新的数组代替已有的容量小的数组。
3. 数组扩容后,原本散列表的元素需要重新计算索引再插入到散列表中。
  
- **JDK1.8重建数组元素的过程**

  <img src="https://javanote.oss-cn-shenzhen.aliyuncs.com/10_map扩容.png" style="zoom:50%;" />

  1. 如果索引处只有一个元素，则保持原样。
  2. 如果索引处是红黑树，则交给红黑树解决。
  3. 如果索引处是链表。判断元素是否大于旧数组长度，如果小于则不动，如果大于等于则移动到 原索引+旧数组长度的位置。会将原链表拆分成两个链表，链表元素不会倒置。

- JDK1.7重建数组元素的过程

  <img src="https://javanote.oss-cn-shenzhen.aliyuncs.com/11_jdk7重建数组元素.png" style="zoom: 50%;" />

  1. 新建扩容后的数组对象。
  2. 将旧数组对象中的元素全部取出，重新计算索引后插入新的数组对象中。
  3. 重建后的数组元素，如果还处于同一个链表中，则会倒置。

- 扩容与性能

  1. 扩容是一个特别耗性能的操作，在初始化map时,给定估算的容量大小,避免map进行频繁的扩容。
  2. 负载因子是可以修改的，也可以大于1，一般不要轻易修改，除非情况非常特殊。
  3. HashMap是线程不安全的，不要在并发的环境中同时操作HashMap，建议使用ConcurrentHashMap。

### 4.线程安全

在多线程使用场景中，应该尽量避免使用线程不安全的`HashMap`，而使用线程安全的`ConcurrentHashMap`。

```java
/**
* 实例：HashMap是线程不安全的,会造成环形链表
**/

public class HashMapInfiniteLoop {
    // 初始数组大小为2,负载因子为0.75
	private static HashMap<Integer,String> map = new HashMap<Integer,String>(2，0.75f);  
	public static void main(String[] args) {  
		map.put(5， "C");  

		new Thread("Thread1") {  
		    public void run() {  
		        map.put(7, "B");  
		        System.out.println(map);  
		    };  
		}.start(); 

		new Thread("Thread2") {  
		    public void run() {  
		    	map.put(3, "A);  
		        System.out.println(map);  
		    };  
		}.start(); 
		       
    }  
} 

```

### 5.JDK1.8和JDK1.7性能对比 

`HashMap`中,如果哈希算法优秀,则key会均匀的分布在数组的所有索引位置上。
如果哈希算法比较垃圾,则会产生很多哈希碰撞,key会集中在一个桶里。
在JDK1.7中,集中在一个链表,时间复杂度为O(n)。在JDK1.8中,集中在一个红黑树,时间复杂度为O(logn)。

- 当hashcode均匀的情况下（hashCode较少重复,碰撞数小于8）

  1.7和1.8都使用链表,性能差不多。

- 当hashcode不均匀的情况下（hashCode较多重复,碰撞数大于等于8）

  1.8引入红黑树替换链表,性能提升明显。

## 具体方法实现

### 1.主要属性

```java
// 默认的初始容量 2^4
static final int DEFAULT_INITIAL_CAPACITY = 1 << 4;  	

// 最大的容量 2^30
static final int MAXIMUM_CAPACITY = 1 << 30;	 	

// 默认的加载因子
static final float DEFAULT_LOAD_FACTOR = 0.75f; 		

// 实际内存存储的数组 表示哈希桶(散列表)索引量，Entry类型数组
transient  Node<K,V>[]  table; 

// 实际存储键值对的长度	
transient int size;   		  

// 阈值,散列表中最大存储的索引数量,由 数组长度*负载因子 得到
int threshold;  

// 加载因子，默认为0.75
final float loadFactor;
```

### 2.构造方法

```java
/**
* 默认无参构造器：初始化负载因子,未初始化哈希表
**/
public HashMap() {
    // static final float DEFAULT_LOAD_FACTOR = 0.75f;
    this.loadFactor = DEFAULT_LOAD_FACTOR; 
}

/**
* initialCapacity：指定容量大小
**/
public HashMap(int initialCapacity) {
    this(initialCapacity, DEFAULT_LOAD_FACTOR);
}

/**
* initialCapacity：指定容量大小,指定负载因子大小
**/
public HashMap(int initialCapacity, float loadFactor) {
    // 容量小于0,报错
    if (initialCapacity < 0)
        throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
    
    // 容量大于最大容量,则等于最大容量
    if (initialCapacity > MAXIMUM_CAPACITY)
        initialCapacity = MAXIMUM_CAPACITY;
    
    // 负载因子小于0或空,报错
    if (loadFactor <= 0 || Float.isNaN(loadFactor))
        throw new IllegalArgumentException("Illegal load factor: " +loadFactor);
    
    // 初始化负载因子
    this.loadFactor = loadFactor;
    // 初始化阈值,此时阈值为初始化的容量
    this.threshold = tableSizeFor(initialCapacity);
}

// 作用：由于容量必须是2的幂次方,返回 大于输入参数且最近的2的整数次幂的数
static final int tableSizeFor(int cap) {
    // 让cap-1再赋值给n的目的是另找到的目标值大于或等于原值
    // 例如cap为8,不减少则找到的是16,减1找到的是8
    // 这个算法作用是，把第一个1开始后面的位都变成1,最后再加1就得到了幂次方
    int n = cap - 1;
    n |= n >>> 1;
    n |= n >>> 2;
    n |= n >>> 4;
    n |= n >>> 8;
    n |= n >>> 16;
    // 当n小于0时,指定为1。当n大于2^30,指定为2^30。
    return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
}

/**
* m：指定的map对象
**/
public HashMap(Map<? extends K, ? extends V> m) {
    this.loadFactor = DEFAULT_LOAD_FACTOR;
    putMapEntries(m, false);
}

final void putMapEntries(Map<? extends K, ? extends V> m, boolean evict) {
    // s为map的数组长度
    int s = m.size();
    if (s > 0) {
        // 如果数组为空
        if (table == null) { 
            // 阈值 = (容量/负载) + 1
            float ft = ((float)s / loadFactor) + 1.0F;
            
            int t = ((ft < (float)MAXIMUM_CAPACITY) ? (int)ft : MAXIMUM_CAPACITY);
            
            // 更新阈值为
            if (t > threshold)
                threshold = tableSizeFor(t);
        }
        
        // 如果小于阈值,则扩容
        else if (s > threshold)
            resize();
        
        // 遍历Map,将该map中的元素插入到map
        for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
            K key = e.getKey();
            V value = e.getValue();
            putVal(hash(key), key, value, false, evict);
        }
    }
}
```

### 3.常用方法

- get()方法
- remove()方法
- replace()方法
- containsValue()方法

```java
/**
* get()方法：根据key获取value值
* 1. 查找哈希表的索引,时间复杂度为O(1)
* 2. 索引处如果是链表,时间复杂度为O(n)
* 3. 索引处如果是红黑树,时间复杂度为O(log n)
**/
public V get(Object key) {
    Node<K,V> e;
    // 如果结点e为空则返回null,否则返回value值
    return (e = getNode(hash(key), key)) == null ? null : e.value;
}

final Node<K,V> getNode(int hash, Object key) {
    Node<K,V>[] tab; 
    Node<K,V> first, e; 
    int n; 
    K k;
    
    /**
    * (tab = table) != null：Map当前数组对象不为空
    * (n = tab.length) > 0：Map当前数组长度大于0
    * first = tab[(n - 1) & hash]：取模运算得到key的索引,并标记为链表的头结点
    **/
    if ((tab = table) != null && (n = tab.length) > 0 
        	&& (first = tab[(n - 1) & hash]) != null) {
        
        /**
        * 作用：直接返回头结点
        * first.hash == hash：比较hash值(必须满足)
        * (k = first.key) == key：比较key对象的内存地址(满足其一即可)
        * (key != null && key.equals(k))：比较key对象的自定义equal方法(满足其一即可)
        **/
        if (first.hash == hash && 
            	((k = first.key) == key || (key != null && key.equals(k))))
            return first;
        
        // 开始循环链表
        if ((e = first.next) != null) {
            // 如果是红黑树,调用树的查找方法
            if (first instanceof TreeNode)
                return ((TreeNode<K,V>)first).getTreeNode(hash, key);
            
            // 链表查找
            do {
                /**
                * e.hash == hash：比较hash值
                * ((k = e.key) == key || (key != null && key.equals(k)))):比较内存地址或equal
                **/
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    return e;
            } while ((e = e.next) != null);
        }
    }
    return null;
}


/**
* remove()方法：根据key值删除一个键值对
* 时间复杂度和查找的一样
**/
public V remove(Object key) {
    Node<K,V> e;
    return (e = removeNode(hash(key), key, null, false, true)) == null ? null : e.value;
}

final Node<K,V> removeNode(int hash, Object key, Object value,boolean matchValue, boolean movable) {
    // 局部变量
    Node<K,V>[] tab; 
    Node<K,V> p; 
    int n, index;
    
    /**
    * (tab = table) != null：Map当前数组不为空
    * (n = tab.length) > 0：Map当前数组长度大于0
    * (p = tab[index = (n - 1) & hash]) != null：取模运算获取索引处元素,且不为空
    **/
    if ((tab = table) != null && (n = tab.length) > 0 &&
        (p = tab[index = (n - 1) & hash]) != null) {
        
        Node<K,V> node = null, e; K k; V v;
        
        // 如果头结点的key相等,则取出头结点
        if (p.hash == hash && ((k = p.key) == key || (key != null && key.equals(k))))
            node = p;
        
        // 进入索引处
        else if ((e = p.next) != null) {
            
            // 如果是红黑树,调用树方法获取结点
            if (p instanceof TreeNode)
                node = ((TreeNode<K,V>)p).getTreeNode(hash, key);
            else {
                
                // 如果是链表,从头结点找到尾结点
                do {
                    if (e.hash == hash && 
                        	((k = e.key) == key || (key != null && key.equals(k)))) {
                        node = e;
                        break;
                    }
                    p = e;
                } while ((e = e.next) != null);
                
            }
        }
        
        /**
        * node表示和key匹配的结点,也就是待删除结点
        * 
        **/
        if (node != null && (!matchValue || (v = node.value) == value ||
                             (value != null && value.equals(v)))) {
            // 如果是红黑树,调用树的删除方法
            if (node instanceof TreeNode)
                ((TreeNode<K,V>)node).removeTreeNode(this, tab, movable);
            
            // 如果是索引处的头结点,用p.next替换掉索引处的p
            else if (node == p)
                tab[index] = node.next;
            
            // 如果是链表上的元素,用下一位替换
            else
                p.next = node.next;
            
          
            ++modCount;
            --size;
            afterNodeRemoval(node);
            return node;
        }
    }
    return null;
}

/**
* replace()：要修改的key,旧value值,新value值
**/
public boolean replace(K key, V oldValue, V newValue) {
     Node<K,V> e; V v;
     // getNode()方法：根据key值获取结点
     // 只有key值和value值命中,才能更新。
     if ((e = getNode(hash(key), key)) != null &&
         ((v = e.value) == oldValue || (v != null && v.equals(oldValue)))) {
         e.value = newValue;
         afterNodeAccess(e);
         return true;
     }
     return false;
 }

/**
* replace()：要修改的key,新value值
**/
public V replace(K key, V value) {
    Node<K,V> e;
    // 只要能根据key值找到结点,就直接更新
    if ((e = getNode(hash(key), key)) != null) {
        V oldValue = e.value;
        e.value = value;
        afterNodeAccess(e);
        return oldValue;
    }
    return null;
}

/**
* containsValue():查找map中是否含有value值的键值对
* 时间复杂度为O(n^2),最好少使用
**/ 
public boolean containsValue(Object value) {
    Node<K,V>[] tab; V v;
    
    // 当前map中的数组不为null且大于9
    if ((tab = table) != null && size > 0) {
        // 先循环整个数组
        for (int i = 0; i < tab.length; ++i) {
            // 在循环数组中的链表或者红黑树
            for (Node<K,V> e = tab[i]; e != null; e = e.next) {
                if ((v = e.value) == value ||
                    (value != null && value.equals(v)))
                    return true;
            }
        }
    }
    return false;
}

```

### 4.获取视图

```java
// 返回一个Map的视图,对视图的更改会映射到原map中
public Set<Map.Entry<K,V>> entrySet() {
    Set<Map.Entry<K,V>> es;
    return (es = entrySet) == null ? (entrySet = new EntrySet()) : es;
}

/**
* 实例：遍历Map
**/
public static void main(String[] args){
    Map<Integer,String> map = new HashMap<>();
    map.put(1,"北京");
    map.put(2,"上海");
    map.put(3,"广州");
    map.put(4,"深圳");

    for(Map.Entry entry : map.entrySet()) {
        System.out.println(String.format("key:[%s],value:[%s]",entry.getKey(),entry.getValue()));
    }

}
```



