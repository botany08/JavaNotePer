## ConcurrentHashMap

### 1.基本介绍

- **继承结构**

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/1_ConcurrentHashMap继承结构.png)

- **跟HashMap，HashTable的对比**

  1. `HashMap`不是线程安全的，所以在处理并发的时候会出现问题。
  2. `HashTable`虽然是线程安全的，但是是通过整个来加锁的方式，当一个线程在写操作的时候，另外的线程则不能进行读写。
  3. `ConcurrentHashMap`则可以支持并发的读写，性能比`HashTable`高。

### 2.底层原理

- **数据结构**
  1. 在`ConcurrentHashMap`中通过一个`Node<K,V>[]`数组来保存添加到`map`中的键值对，而在同一个数组位置是通过链表和红黑树的形式来保存的。 
  2. 和`HashMap`一样，通过`哈希表+链表+红黑树`来保存数据。不同的是，通过`CAS+synchronized`来保证线程安全。

###3.类中属性以及内部类

- **类中属性**

  ```java
  // 数组最大长度
  private static final int MAXIMUM_CAPACITY = 1 << 30;
  // 数组默认长度
  private static final int DEFAULT_CAPACITY = 16;
  // 转换为红黑树的长度
  static final int TREEIFY_THRESHOLD = 8;
  // 转换为链表的长度
  static final int UNTREEIFY_THRESHOLD = 6;
  // 最小扩容长度
  static final int MIN_TREEIFY_CAPACITY = 64;
  // 表示正在转移
  static final int MOVED     = -1; 
  // 表示已经转换成树
  static final int TREEBIN   = -2; 
  static final int RESERVED  = -3; 
  static final int HASH_BITS = 0x7fffffff; 
  // 默认定义的数组,用来保存元素,还未初始化
  transient volatile Node<K,V>[] table;
  // 扩容元素转移时,临时数组
  private transient volatile Node<K,V>[] nextTable;
  
  /**
   * 用来控制表初始化和扩容的,默认值为0.扩容的阈值,当超过这个值时会发生扩容.
   * 当为负的时候,说明表正在初始化或扩容. -1表示初始化, -(1+n) n:表示活动的扩容线程.
  **/
  private transient volatile int sizeCtl;
  ```

  

- **内部类**

  1. **Node<K,V>**：构成每个元素的基本类，是`ConcurrentHashMap`中的基本节点类。

     ```java
     static class Node<K,V> implements Map.Entry<K,V> {
         final int hash;    //key的hash值
         final K key;       //key
         volatile V val;    //value
         volatile Node<K,V> next; //表示链表中的下一个节点
     
         Node(int hash, K key, V val, Node<K,V> next) {
             this.hash = hash;
             this.key = key;
             this.val = val;
             this.next = next;
         }
         
         public final K getKey()       { return key; }
         public final V getValue()     { return val; }
         public final int hashCode()   { return key.hashCode() ^ val.hashCode(); }
     }
     ```

     

  2. **TreeNode**：构造树的节点

     ```java
     static final class TreeNode<K,V> extends Node<K,V> {
         TreeNode<K,V> parent;  // red-black tree links
         TreeNode<K,V> left;
         TreeNode<K,V> right;
         TreeNode<K,V> prev;    // needed to unlink next upon deletion
         boolean red;
     
         TreeNode(int hash, K key, V val, Node<K,V> next,
                  TreeNode<K,V> parent) {
             super(hash, key, val, next);
             this.parent = parent;
         }
     }
     ```

     

  3. **TreeBin**：用作树的头结点，只存储root和first节点，不存储节点的key、value值。

     ```java
     static final class TreeBin<K,V> extends Node<K,V> {
         TreeNode<K,V> root;
         volatile TreeNode<K,V> first;
         volatile Thread waiter;
         volatile int lockState;
         // values for lockState
         static final int WRITER = 1; // set while holding write lock
         static final int WAITER = 2; // set when waiting for write lock
         static final int READER = 4; // increment value for setting read lock
     }
     ```

     

  4. **ForwardingNode**：表示在转移的时候放在头部的节点，是一个空节点。

     ```java
     static final class ForwardingNode<K,V> extends Node<K,V> {
         final Node<K,V>[] nextTable;
         ForwardingNode(Node<K,V>[] tab) {
             super(MOVED, null, null, null);
             this.nextTable = tab;
         }
     }
     ```



### 4.Unsafe类

- **Unsafe类的作用**

  Java不能直接访问操作系统底层，而是通过本地方法来访问。Unsafe类提供了硬件级别的原子操作，通过直接操作内存的方式来保证并发处理的安全性

- **ConcurrentHashMap中的unsafe方法**

  ```java
  /*
   * 用来返回节点数组的指定位置的节点的原子操作
   */
  @SuppressWarnings("unchecked")
  static final <K,V> Node<K,V> tabAt(Node<K,V>[] tab, int i) {
      return (Node<K,V>)U.getObjectVolatile(tab, ((long)i << ASHIFT) + ABASE);
  }
  
  /*
   * cas原子操作，在指定位置设定值
   */
  static final <K,V> boolean casTabAt(Node<K,V>[] tab, int i,
                                      Node<K,V> c, Node<K,V> v) {
      return U.compareAndSwapObject(tab, ((long)i << ASHIFT) + ABASE, c, v);
  }
  
  /*
   * 原子操作，在指定位置设定值
   */
  static final <K,V> void setTabAt(Node<K,V>[] tab, int i, Node<K,V> v) {
      U.putObjectVolatile(tab, ((long)i << ASHIFT) + ABASE, v);
  }
  ```



### 5.ConcurrentHashMap的初始化

- **构造方法**

  ```java
  /**
  * 空的构造
  **/
  public ConcurrentHashMapDebug() {}
  
  /**
  * 如果在实例化对象的时候指定了容量，则初始化sizeCtl
  **/
  public ConcurrentHashMapDebug(int initialCapacity) {
      if (initialCapacity < 0)  
          throw new IllegalArgumentException();
      
      int cap = ((initialCapacity >= (MAXIMUM_CAPACITY >>> 1)) ?
                 MAXIMUM_CAPACITY :
                 tableSizeFor(initialCapacity + (initialCapacity >>> 1) + 1));
      this.sizeCtl = cap;
  }
  
  /**
  * 当出入一个Map的时候，先设定sizeCtl为默认容量，在添加元素
  **/
  public ConcurrentHashMapDebug(Map<? extends K, ? extends V> m) {
      this.sizeCtl = DEFAULT_CAPACITY;
      putAll(m);
  }
  ```

- **哈希表(数组)的初始化**

  `ConcurrentHashMap`使用了懒加载的方式，只有在第一次put操作的时候，才进行数组的初始化。

  ```java
  /**
  * 初始化数组table的过程
  * 1.如果sizeCtl小于0,说明别的数组正在进行初始化，则让出执行权
  * 2.如果sizeCtl大于0的话,则初始化一个大小为sizeCtl的数组
  * 3.否则的话初始化一个默认大小(16)的数组,然后设置sizeCtl的值为数组长度的3/4
  **/
  private final Node<K,V>[] initTable() {
      Node<K,V>[] tab; int sc;
      
      // 第一次put的时候,table还没被初始化,进入while
      while ((tab = table) == null || tab.length == 0) {
          // sizeCtl初始值为0，当小于0的时候表示在别的线程在初始化表或扩展表
          if ((sc = sizeCtl) < 0)
              // 当前线程暂时让出执行权,进入等待状态
              Thread.yield();
          
          // SIZECTL：表示当前对象的内存偏移量,sc表示期望值,-1表示要替换的值,设定为-1表示要初始化表了
          else if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {    
              try {
                  if ((tab = table) == null || tab.length == 0) {
                      // 指定了大小的时候就创建指定大小的Node数组,否则创建指定大小(16)的Node数组
                      int n = (sc > 0) ? sc : DEFAULT_CAPACITY;        
                      
                      @SuppressWarnings("unchecked")
                      Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n];
                      table = tab = nt;
                      sc = n - (n >>> 2);
                  }
              } finally {
                  // 初始化后，sizeCtl长度为数组长度的3/4
                  sizeCtl = sc;            
              }
              break;
          }
      }
      return tab;
  }
  ```

### 6.put()方法详解

- **`put(K,V)`方法**

  ```java
  /*
   *  单纯的额调用putVal方法，并且putVal的第三个参数设置为false.
   *  如果是false,表示这个value一定会设置.如果是true,只有当这个key的value为空的时候才会设置.
   */
  public V put(K key, V value) {
      return putVal(key, value, false);
  }
  ```

  

- **`putVal(K,V,boolean)`方法**

  ```java
  /*
   * 具体流程
   * 1.当添加一对键值对的时候，首先判断哈希表有没有初始化,如果没有的话就初始化数组.
   * 2.计算key的hash值来确定数组的索引,如果为空则直接添加.如果不为空的话,则取出这个节点.
   * 3.如果取出来的节点的hash值是MOVED(-1)的话,则表示当前正在对这个数组进行扩容,复制到新的数组,则当前线程
   *   也去帮助复制.
   * 4.如果这个节点不为空,也不在扩容,则通过synchronized来加锁,进行添加操作.
   * 5.然后判断当前取出的节点位置存放的是链表还是树
   * 6.如果是链表的话,则遍历整个链表,直到取出来的节点的key来个要放的key进行比较.如果key相等,并且key的hash值
       也相等的话,则说明是同一个key,则覆盖掉value,否则的话则添加到链表的末尾.
   * 7.如果是树的话,则调用putTreeVal方法把这个元素添加到树中去.
   * 8.最后在添加完成之后,会判断在该节点处共有多少个节点(注意是添加前的个数),如果达到8个以上了的话,则调用
   *   treeifyBin方法来尝试将处的链表转为树，或者扩容数组
   */
  final V putVal(K key, V value, boolean onlyIfAbsent) {
      // K,V都不能为空，否则的话跑出异常
      if (key == null || value == null) throw new NullPointerException();
      
      // 计算key的hash值
      int hash = spread(key.hashCode());
      // 用来计算在这个节点总共有多少个元素，用来控制扩容或者转移为树
      int binCount = 0;
      
      for (Node<K,V>[] tab = table;;) {    
          Node<K,V> f; int n, i, fh;
          
          if (tab == null || (n = tab.length) == 0)
              // 第一次put的时候table没有初始化，则初始化table
              tab = initTable();
          
          // 第一种情况：当前位置为空,则通过CAS直接添加
          // 通过哈希计算出表的位置.因为数组长度n是2的幂次方，所以(n-1)&hash肯定不会出现数组越界.
          else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) {
              // 如果这个位置没有元素的话，则通过cas的方式尝试添加，注意这个时候是没有加锁的
              // 创建一个Node添加到数组中区，null表示的是下一个节点为空
              if (casTabAt(tab, i, null,new Node<K,V>(hash, key, value, null)))        
                  break;                   
          }
          
          /*
           * 第二种情况：当前位置正在进行数组扩容的数组复制阶段
           * 如果检测到某个节点的hash值是MOVED,则表示正在进行数组扩容的数据复制阶段,
           * 则当前线程也会参与去复制,通过允许多线程复制的功能,以此来减少数组的复制所带来的性能损失
           */
          else if ((fh = f.hash) == MOVED)    
              tab = helpTransfer(tab, f);
          
          // 第三种情况：当前位置存在值,发生哈希冲突
          else {
              /*
               * 1.如果在这个位置有元素的话,就采用synchronized的方式加锁,
               * 2.如果是链表的话(hash大于0)，就对这个链表的所有元素进行遍历,如果找到了key和key的hash值都
               *   一样的节点,替换节点.如果没找到的话，则添加在链表的最后面.
               * 3.是树的话，则调用putTreeVal方法添加到树中去.
               * 4.在添加完之后,会对该节点上关联的的数目进行判断,如果在8个以上的话,则会调用treeifyBin方
               *   法,来尝试转化为树,或者是扩容.
               */
              V oldVal = null;
              synchronized (f) {
                  // 再次取出要存储的位置的元素，跟前面取出来的比较
                  if (tabAt(tab, i) == f) {
                      // 取出来的元素的hash值大于0,当转换为树之后,hash值为-2
                      if (fh >= 0) {                
                          binCount = 1;
                           // 遍历这个链表
                          for (Node<K,V> e = f;; ++binCount) {   
                              K ek;
                              // key跟要存储的位置的节点的相同的时候,替换掉该节点的value即可
                              if (e.hash == hash &&        
                                  ((ek = e.key) == key ||(ek != null && key.equals(ek)))) {
                                  oldVal = e.val;
                                  // 当使用putIfAbsent的时候，只有在这个key没有设置值得时候才设置
                                  if (!onlyIfAbsent)        
                                      e.val = value;
                                  break;
                              }
                              Node<K,V> pred = e;
                              // 如果不是同样的hash,同样的key的时候,则判断该节点的下一个节点是否为空.
                              if ((e = e.next) == null) {    
                                  // 为空的话把这个要加入的节点设置为当前节点的下一个节点
                                  pred.next = new Node<K,V>(hash, key,value, null);
                                  break;
                              }
                          }
                      }
                      
                      // 表示已经转化成红黑树类型了
                      else if (f instanceof TreeBin) {    
                          Node<K,V> p;
                          binCount = 2;
                          // 调用putTreeVal方法，将该元素添加到树中去
                          if ((p = ((TreeBin<K,V>)f).putTreeVal(hash, key, value)) != null) {
                              oldVal = p.val;
                              if (!onlyIfAbsent)
                                  p.val = value;
                          }
                      }
                  }
              }
              
              if (binCount != 0) {
                  // 当在同一个节点的数目达到8个的时候，则扩张数组或将给节点的数据转为tree
                  if (binCount >= TREEIFY_THRESHOLD)    
                      treeifyBin(tab, i);    
                  if (oldVal != null)
                      return oldVal;
                  break;
              }
          }
      }
      addCount(1L, binCount);    
      return null;
  }
  ```

  

### 7.ConcurrentHashMap哈希表扩容机制

- **哈希表扩容的两种情况**
  1. 当发生哈希冲突的元素达到8个，且哈希表长度小于64。
  2. 当哈希表的使用超过了阈值，就是`长度x负载因子`。

- **treeifyBin()方法**

  ```java
  /**
   * 当数组长度小于64的时候，扩张数组长度一倍，否则的话把链表转为树
   */
  private final void treeifyBin(Node<K,V>[] tab, int index) {
      Node<K,V> b; int n, sc;
      if (tab != null) {
          // MIN_TREEIFY_CAPACITY=64,当链表长度大于等于8,且数组长度小于64,则扩容数组*2.
          if ((n = tab.length) < MIN_TREEIFY_CAPACITY)
              tryPresize(n << 1);
          
          // 将链表转换为树
          else if ((b = tabAt(tab, index)) != null && b.hash >= 0) {
              // 使用synchronized同步器，将该节点出的链表转为树
              synchronized (b) {    
                  if (tabAt(tab, index) == b) {
                      // hd：树的头(head)
                      TreeNode<K,V> hd = null, tl = null;    
                      for (Node<K,V> e = b; e != null; e = e.next) {
                          TreeNode<K,V> p =
                              new TreeNode<K,V>(e.hash, e.key, e.val,null, null);
                          // 把Node组成的链,转化为TreeNode的链表,头结点任然放在相同的位置
                          if ((p.prev = tl) == null) 
                              // 设置head
                              hd = p;    
                          else
                              tl.next = p;
                          tl = p;
                      }
                      // 把TreeNode的链表放入容器TreeBin中
                      setTabAt(tab, index, new TreeBin<K,V>(hd));
                  }
              }
          }
      }
  }
  
  ```

- **tryPresize()方法**

  ```java
  /**
   * 扩容哈希表,size为原长度的2倍,实际上是2的幂次方
   * 假设原来的数组长度为16,则在调用tryPresize的时候,size参数的值为16<<1(32),此时sizeCtl的值为12.
   * 计算出来c的值为64,则要扩容到小于等于sizeCtl为止.
   * 第一次扩容之后 数组长：32 sizeCtl：24
   * 第二次扩容之后 数组长：64 sizeCtl：48
   * 第二次扩容之后 数组长：128 sizeCtl：94 --> 这个时候才会退出扩容
   */
  private final void tryPresize(int size) {
      /*
       * MAXIMUM_CAPACITY = 1 << 30
       * 如果给定的大小超过最大容量一般,则直接使用最大容量,否则使用tableSizeFor算出来.
       */
      int c = (size >= (MAXIMUM_CAPACITY >>> 1)) ? 
          // tableSizeFor算出来为 size*1.5+1
          MAXIMUM_CAPACITY : tableSizeFor(size + (size >>> 1) + 1);
      int sc;
      
      while ((sc = sizeCtl) >= 0) {
          Node<K,V>[] tab = table; int n;
          
          /*
           * 作用：如果数组table还没有被初始化,则初始化一个大小为sizeCtrl和刚刚算出来的c中较大的一个大小的
           * 		数组初始化的时候,设置sizeCtrl为-1,初始化完成之后把sizeCtrl设置为数组长度的3/4.
           * 为什么要在扩张的地方来初始化数组呢？
           * 因为如果第一次put的时候不是put单个元素,而是调用putAll方法直接put一个map的话,在putAll方法中没
           * 有调用initTable方法去初始化table,而是直接调用了tryPresize方法,所以这里需要做一个是不是需要初
           * 始化table的判断
           */
          if (tab == null || (n = tab.length) == 0) {
              n = (sc > c) ? sc : c;
              // 初始化tab的时候,把sizeCtl设为-1
              if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {    
                  try {
                      if (table == tab) {
                          @SuppressWarnings("unchecked")
                          Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n];
                          table = nt;
                          sc = n - (n >>> 2);
                      }
                  } finally {
                      sizeCtl = sc;
                  }
              }
          }
          
          /*
           * 一直扩容到的c小于等于sizeCtl或者数组长度大于最大长度的时候,则退出所以在一次扩容之后,不是原来长
           * 度的两倍,而是2的n次方倍
           */
          else if (c <= sc || n >= MAXIMUM_CAPACITY) {
                  break;    
          }
          
          
          else if (tab == table) {
              int rs = resizeStamp(n);
              /*
               * sc < 0 表示正在扩容.
               * 如果正在扩容Table的话,则帮助扩容,否则的话,开始新的扩容.
               * 在transfer操作,将第一个参数的table中的元素,移动到第二个元素的table中去,
               * 虽然此时第二个参数设置的是null,但是,在transfer方法中,当第二个参数为null的时候,
               * 会创建一个两倍大小的table
               */
              if (sc < 0) {
                  Node<K,V>[] nt;
                  if ((sc >>> RESIZE_STAMP_SHIFT) != rs || sc == rs + 1 ||
                      sc == rs + MAX_RESIZERS || (nt = nextTable) == null ||
                      transferIndex <= 0)
                      break;
                  
                  /*
                   * transfer的线程数加一,该线程将进行transfer的帮忙
                   * 在transfer的时候,sc表示在transfer工作的线程数
                   */
                  if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1))
                      transfer(tab, nt);
              }
              
              /*
               * 没有在初始化或扩容,则开始扩容
               */
              else if (U.compareAndSwapInt(this, SIZECTL, sc,
                                           (rs << RESIZE_STAMP_SHIFT) + 2)) {
                      transfer(tab, null);
              }
          }
      }
  }
  ```

- **transfer() - 数组扩容的主要方法**

  1. 复制之后的新链表不是旧链表的绝对倒序。
  2. 在扩容的时候每个线程都有处理的步长，最少为16，在这个步长范围内的数组节点只有自己一个线程来处理。

  ```java
  /**
   * 把数组中的节点复制到新的数组的相同位置,或者移动到扩张部分的相同位置.
   * 1.首先会计算一个步长,表示一个线程处理的数组长度,用来控制对CPU的使用.
   * 2.每个CPU最少处理16个长度的数组元素,如果一个数组的长度只有16,那只有一个线程会对其进行扩容的复制移动操作.
   * 3.扩容的时候会一直遍历,直到复制完所有节点.每处理一个节点的时候会在链表的头部设置一个fwd节点,其他线程
   *   就会跳过他.
   * 4.复制后在新数组中的链表不是绝对的反序的
   */
  private final void transfer(Node<K,V>[] tab, Node<K,V>[] nextTab) {
      int n = tab.length, stride;
      
      // MIN_TRANSFER_STRIDE=16 用来控制不要占用太多CPU
      if ((stride = (NCPU > 1) ? (n >>> 3) / NCPU : n) < MIN_TRANSFER_STRIDE)
          stride = MIN_TRANSFER_STRIDE;
      
      /*
       * 如果复制的目标nextTab为null的话,则初始化一个table两倍长的nextTab
       * 此时nextTable被设置值了(在初始情况下是为null的)
       * 如果有一个线程开始扩容表,其他线程也会一起扩容,而第一个开始扩容的线程需要初始化下目标数组.
       */
      if (nextTab == null) {            
          try {
              @SuppressWarnings("unchecked")
              Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n << 1];
              nextTab = nt;
          } catch (Throwable ex) {     
              sizeCtl = Integer.MAX_VALUE;
              return;
          }
          nextTable = nextTab;
          transferIndex = n;
      }
      
      int nextn = nextTab.length;
      
      
      /*
       * 创建一个fwd节点
       * 用来控制并发,当一个节点为空或已经被转移之后,就设置为fwd节点,是一个空的标志节点
       */
      ForwardingNode<K,V> fwd = new ForwardingNode<K,V>(nextTab);
      
      // 是否继续向前查找的标志位
      boolean advance = true;
      boolean finishing = false;
      
      // 在完成之前重新在扫描一遍数组,看看有没完成的没
      for (int i = 0, bound = 0;;) {
          Node<K,V> f; int fh;
          while (advance) {
              int nextIndex, nextBound;
              if (--i >= bound || finishing) {
                  advance = false;
              }
              else if ((nextIndex = transferIndex) <= 0) {
                  i = -1;
                  advance = false;
              }
              else if (U.compareAndSwapInt
                       (this, TRANSFERINDEX, nextIndex,
                        nextBound = (nextIndex > stride ? nextIndex - stride : 0))) {
                  bound = nextBound;
                  i = nextIndex - 1;
                  advance = false;
              }
          }
          if (i < 0 || i >= n || i + n >= nextn) {
              int sc;
              // 已经完成转移
              if (finishing) {        
                  nextTable = null;
                  table = nextTab;
                  // 设置sizeCtl为扩容后的0.75
                  sizeCtl = (n << 1) - (n >>> 1);    
                  return;
              }
              if (U.compareAndSwapInt(this, SIZECTL, sc = sizeCtl, sc - 1)) {
                  if ((sc - 2) != resizeStamp(n) << RESIZE_STAMP_SHIFT) {
                          return;
                  }
                  finishing = advance = true;
                  i = n; 
              }
          }
          
          // 数组中把null的元素设置为ForwardingNode节点(hash值为MOVED[-1])
          else if ((f = tabAt(tab, i)) == null)            
              advance = casTabAt(tab, i, null, fwd);
          else if ((fh = f.hash) == MOVED)
              advance = true; 
          else {
              // 加锁操作
              synchronized (f) {                
                  if (tabAt(tab, i) == f) {
                      Node<K,V> ln, hn;
                      
                      //该节点的hash值大于等于0,说明是一个No
                      if (fh >= 0) {
                          
                          /*
                           * 因为n的值为数组的长度,且是power(2,x)的,所以在&操作的结果只可能是0或者n
                           * 根据这个规则
                           * 0-->  放在新表的相同位置
                           * n-->  放在新表的（n+原来位置）
                           */
                          int runBit = fh & n; 
                          Node<K,V> lastRun = f;
                          
                          /*
                           * 1.lastRun表示的是需要复制的最后一个节点.
                           * 2.每当新节点的hash&n->b发生变化的时候,就把runBit设置为这个结果b.
                           * 3.这样for循环之后,runBit的值就是最后不变的hash&n的值.
                           * 4.而lastRun的值就是最后一次导致hash&n发生变化的节点(假设为p节点)
                           
                           * p节点后面的节点的hash&n值跟p节点是一样的,所以在复制到新的table的时候,肯定还
                           * 是跟p节点在同一个位置.在复制完p节点之后,p节点的next节点还是指向它原来的节点,
                           * 就不需要进行复制了,就被带过去了,所以复制后的链表的顺序并不一定是原来的倒序.
                           */
                          for (Node<K,V> p = f.next; p != null; p = p.next) {
                              // n的值为扩张前的数组的长度
                              int b = p.hash & n;    
                              if (b != runBit) {
                                  runBit = b;
                                  lastRun = p;
                              }
                          }
                          if (runBit == 0) {
                              ln = lastRun;
                              hn = null;
                          }
                          else {
                              hn = lastRun;
                              ln = null;
                          }
                          
                          /*
                           * 构造两个链表,顺序大部分和原来是反的
                           * 分别放到原来的位置和新增加的长度的相同位置(i/n+i)
                           */
                          for (Node<K,V> p = f; p != lastRun; p = p.next) {
                              int ph = p.hash; K pk = p.key; V pv = p.val;
                              
                              /*
                               * 假设runBit的值为0,
                               * 则第一次进入这个设置的时候相当于把旧的序列的最后一次发生hash变化
                               * 的节点(该节点后面可能还有hash计算后同为0的节点)设置到旧的table的
                               * 第一个hash计算后为0的节点下一个节点
                               * 并且把自己返回,然后在下次进来的时候把它自己设置为后面节点的下一个节点
                               */
                              if ((ph & n) == 0)
                                  ln = new Node<K,V>(ph, pk, pv, ln);
                              else
                                  /*
                                   * 假设runBit的值不为0,
                                   * 则第一次进入这个设置的时候,相当于把旧的序列的最后一次发生hash变化的
                                   * 节点(该节点后面可能还有hash计算后同不为0的节点),设置到旧的table的第
                                   * 一个hash计算后不为0的节点下一个节点,并且把自己返回.然后在下次进来的
                                   * 时候把它自己设置为后面节点的下一个节点
                                   */
                                  hn = new Node<K,V>(ph, pk, pv, hn);    
                          }
                          setTabAt(nextTab, i, ln);    
                          setTabAt(nextTab, i + n, hn);
                          setTabAt(tab, i, fwd);
                          advance = true;
                      }
                      
                      // 否则的话是一个树节点
                      else if (f instanceof TreeBin) {    
                          TreeBin<K,V> t = (TreeBin<K,V>)f;
                          TreeNode<K,V> lo = null, loTail = null;
                          TreeNode<K,V> hi = null, hiTail = null;
                          int lc = 0, hc = 0;
                          for (Node<K,V> e = t.first; e != null; e = e.next) {
                              int h = e.hash;
                              TreeNode<K,V> p = new TreeNode<K,V>
                                  (h, e.key, e.val, null, null);
                              if ((h & n) == 0) {
                                  if ((p.prev = loTail) == null)
                                      lo = p;
                                  else
                                      loTail.next = p;
                                  loTail = p;
                                  ++lc;
                              }
                              else {
                                  if ((p.prev = hiTail) == null)
                                      hi = p;
                                  else
                                      hiTail.next = p;
                                  hiTail = p;
                                  ++hc;
                              }
                          }
                          
                          /*
                           * 在复制完树节点之后,判断该节点处构成的树还有几个节点,
                           * 如果≤6个的话,就转回为一个链表
                           */
                          ln = (lc <= UNTREEIFY_THRESHOLD) ? untreeify(lo) :
                              (hc != 0) ? new TreeBin<K,V>(lo) : t;
                          hn = (hc <= UNTREEIFY_THRESHOLD) ? untreeify(hi) :
                              (lc != 0) ? new TreeBin<K,V>(hi) : t;
                          setTabAt(nextTab, i, ln);
                          setTabAt(nextTab, i + n, hn);
                          setTabAt(tab, i, fwd);
                          advance = true;
                      }
                  }
              }
          }
      }
  }
  
  ```

  

### 8.get()方法详解

- **get()方法**

  ```java
  /*
   * 1.相比put方法,get支持并发操作.
   * 2.当key为null的时候回抛出NullPointerException的异常.
   * 3.get操作通过首先计算key的hash值来确定该元素放在数组的哪个位置.
   * 4.然后遍历该位置的所有节点,如果不存在的话返回null.
   */
  public V get(Object key) {
      Node<K,V>[] tab; Node<K,V> e, p; int n, eh; K ek;
      int h = spread(key.hashCode());
      if ((tab = table) != null && (n = tab.length) > 0 &&
          (e = tabAt(tab, (n - 1) & h)) != null) {
          if ((eh = e.hash) == h) {
              if ((ek = e.key) == key || (ek != null && key.equals(ek)))
                  return e.val;
          }
          else if (eh < 0)
              return (p = e.find(h, key)) != null ? p.val : null;
          while ((e = e.next) != null) {
              if (e.hash == h &&
                  ((ek = e.key) == key || (ek != null && key.equals(ek))))
                  return e.val;
          }
      }
      return null;
  }
  ```



### 9.同步机制

- **引起哈希数组扩容(transfer方法)的情况**

  1. **`tryPresize`方法**

     在`treeIfybin`和`putAll`方法中调用，`treeIfybin`主要是在put添加元素完之后，判断该数组节点相关元素是不是已经超过8个的时候，如果超过则会调用这个方法来扩容数组(数组长度小于64)或者把链表转为树。

     在往`map`中添加元素的时候，在某一个节点的数目已经超过了8个，同时数组的长度又小于64的时候，才会触发数组的扩容。

  2. **`helpTransfer`方法**

     在当一个线程要对`table`中元素进行操作的时候，如果检测到节点的`HASH`值为`MOVED`的时候，就会调用`helpTransfer`方法，在`helpTransfer`中再调用`transfer`方法来帮助完成数组的扩容

  3. **`addCount`方法**

     在当对数组进行操作，使得数组中存储的元素个数发生了变化的时候会调用的方法。如果数组个数超过了阈值，则会发生扩容。当数组中元素达到了`sizeCtl`的数量的时候，则会调用transfer方法来进行扩容。

- **读操作**

  在get操作中，根本没有使用同步机制，也没有使用unsafe方法，所以读操作是支持并发操作的。

- **写操作**

  1. 当在进行数组扩容的时候，如果当前节点还没有被处理(还没有设置为fwd节点)，那就可以进行设置操作。
  2. 如果该节点已经被处理了，则当前线程也会加入到扩容的操作中去。

- **多线程的同步处理**

  同步处理主要是通过`Synchronized`和`unsafe`两种方式来完成的。
  1. 在取得`sizeCtl`、某个位置的`Node`的时候，使用的都是`unsafe`的方法，来达到并发安全的目的。
  2. 当需要在某个位置设置节点的时候，则会通过`Synchronized`的同步机制来锁定该位置的节点。
  3. 在数组扩容的时候，则通过处理的步长和`fwd`节点来达到并发安全的目的，通过设置`hash`值为`MOVED`。
  4. 当把某个位置的节点复制到扩张后的`table`的时候，也通过`Synchronized`的同步机制来保证线程安全。

