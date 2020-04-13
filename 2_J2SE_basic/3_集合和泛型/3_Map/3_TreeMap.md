## 基础概念

### 1.定义及特点

- 在`TreeMap`中,键值对之间有序排列,`TreeMap`的实现基础是**排序二叉树(红黑树)**。
- `TreeMap`实现了`Map,SortedMap`和`NavigableMap`接口。可以方便的根据键的顺序进行查找，如第一个、最后一个、某一范围的键、邻近键等。
- 为了按键有序，`TreeMap`要求键实现`Comparable`接口或通过构造方法提供一个`Comparator`对象。
- `TreeMap`使用键的比较结果对键进行排重。

###2.性能及时间复杂度

- `put()，get()`及`remove()`方法的时间复杂度为`O(h)` , h为树的高度。在树平衡的情况下,h为log2(N),N为节点数。
- 不要求排序，优先考虑`HashMap`。要求排序，再考虑`TreeMap`。

### 3.构造方法

- 构造参数如果有比较器对象`comparator`,不要求键实现`Comparable`接口。
- 如果没有,则要求键实现`Comparable`接口,且重写`compareTo`方法。

```java
/**
* 默认无参构造方法
* 要求Map中的键实现Comparabe接口，TreeMap内部进行各种比较时会调用键的Comparable接口中的compareTo方法。
**/
public TreeMap() {
    comparator = null;
}

/**
* 参数为比较器对象comparator
* 如果comparator不为null，在TreeMap内部进行比较时会调用这个comparator的compare方法，不再要求键实现
* Comparable接口。
**/
public TreeMap(Comparator<? super K> comparator) {
    this.comparator = comparator;
}

/**
* 参数为Map对象
* 依赖键值对的compareTo方法,要求键实现Comparable接口
**/
public TreeMap(Map<? extends K, ? extends V> m) {
    comparator = null;
    putAll(m);
}

/**
* SortedMap接口，扩展了Map接口，表示有序的Map。
* 该接口中comparator()方法返回一个比较器对象，TreeMap直接使用该比较器对象的比较方法就可以。
**/
public TreeMap(SortedMap<K, ? extends V> m) {
    comparator = m.comparator();
    try {
        buildFromSorted(m.size(), m.entrySet().iterator(), null, null);
    } catch (java.io.IOException cannotHappen) {
    } catch (ClassNotFoundException cannotHappen) {
    }
}
```

### 4.接口Comparable和Comparator区别  

- `Comparable`位于`java.lang`包里，绝大多数的类实现了这个接口，比较方法是compareTo。称为内比较器，可以和自身比较。
- `Comparator`位于`java.util`包里，实现Compare接口一般是没有实现Comparable接口或者对compareTo方法不满意，只需要重写compare方法即可。一般称为外比较器。

### 5.比较器对象实例

- TreeMap使用键的比较结果对键进行排重。
  1. 即使键实际上不同，但只要比较结果相同，就会被认为相同，键只会保存一份。
  2. 比如传入大小写不一样，但是如果利用了忽略大小写的比较器，就会被认为是一样的，只会保存一份。

```java
/**
* 实例：使用内比较器Comparable
* 排序规则：按照String类中的比较器排序
**/
public static void main(String[] args){
    // 默认使用键的compareTo()排序方法
    Map<String,String> map = new TreeMap<>();
    map.put("a", "abstract");
    map.put("c", "call");
    map.put("b", "basic");
    map.put("T", "tree");

    // 循环取出Map中元素
    // 输出：T=tree  a=abstract  b=basic  c=call
    for(Map.Entry<String,String> kv : map.entrySet()) {
        System.out.print(kv.getKey()+"="+kv.getValue()+" ");
    }
}

// String类的compareTo()方法: 将字符转换为ASCII码进行比较
// A1小于A2,返回负数
// A1大于A2,返回正数
// A1等于于A2,返回0
public int compareTo(String anotherString) {
    int len1 = value.length;
    int len2 = anotherString.value.length;
    int lim = Math.min(len1, len2);
    char v1[] = value;
    char v2[] = anotherString.value;

    int k = 0;
    while (k < lim) {
        char c1 = v1[k];
        char c2 = v2[k];
        if (c1 != c2) {
            return c1 - c2;
        }
        k++;
    }
    return len1 - len2;
}

/**
* 实例：使用外比较器Comparator,重写了compare方法
* 比较规则：如果是正数则交换位置,否则保持不变.
**/
public static void main(String[] args){
    // String.CASE_INSENSITIVE_ORDER为String类中外比较器对象,作用是忽略大小写进行比较
    Map<String,String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    map.put("a", "abstract");
    map.put("c", "call");
    map.put("b", "basic");
    map.put("T", "tree");

    // 循环取出Map中元素
    for(Map.Entry entry : map.entrySet()) {
        System.out.println(String.format("key:[%s],value:[%s]",entry.getKey(),entry.getValue()));
    }
}

/**
* 比较器对象
* String.CASE_INSENSITIVE_ORDER: 忽略字符大小写比较,正序
* Collections.reverseOrder()：逆序
* Collections.reverseOrder(String.CASE_INSENSITIVE_ORDER)：忽略字符大小写比较,逆序
**/
```

## TreeMap底层实现原理

### 1.实例变量(基本属性)

```java
// 比较器对象，如果没传就是null
private final Comparator<? super K> comparator; 
// 指向树的根结点
private transient Entry<K,V> root = null;
// 当前键值对的数目
private transient int size = 0;	
// 修改次数
private transient int modCount = 0;				
```

### 2.Entry内部类

```java
// Entry类为TreeMap的组成结点类
static final class Entry<K,V> implements Map.Entry<K,V> {
    // 当前key值
    K key;	
    // 当前value值
    V value;	
    // 指向左子节点
    Entry<K,V> left;	
    // 指向右子节点
    Entry<K,V> right;
    // 指向父节点
    Entry<K,V> parent;	
    
    // color表示颜色，用于红黑树(BLACK==true  RED==false)
    boolean color = BLACK;
    
    Entry(K key, V value, Entry<K,V> parent) {
        this.key = key;
        this.value = value;
        this.parent = parent;
    }
}

```

### 3.添加元素put()

```java
// 插入元素
public V put(K key, V value) {
    // t为当前根结点
    Entry<K,V> t = root;
    // 当前根结点为空,表示添加第一个结点
    if (t == null) {
        // compare()方法：选择使用compareTo()方法[Comparable接口]还是compare()方法[Comparator接口]
        compare(key, key);
        // 新建Entry结点对象,父结点设置为空
        root = new Entry<>(key, value, null);
        size = 1;
        modCount++;
        return null;
    }
    
    
    int cmp;
    Entry<K,V> parent;
    // 表示传入的外比较器对象
    Comparator<? super K> cpr = comparator;
    // 目的：寻找适合当前传入结点的父结点
    // 外比较器Comparable
    if (cpr != null) {
        do {
            // 父结点先默认为根结点
            parent = t;
            // 调用比较方法,前者大于后者返回正数,小于返回负数,等于返回0
            cmp = cpr.compare(key, t.key);
            // key小于t.key：key放入t的左子树,父结点取左
            if (cmp < 0)
                t = t.left;
            // key大于t.key：key放入t的右子树,父结点取右
            else if (cmp > 0)
                t = t.right;
            // key等于t.key：更新相同的key的value值,因为Map中的key不允许重复
            else
                return t.setValue(value);
        } while (t != null);// 一直遍历到叶子节点
    }
    // 内比较器Comparator
    else {
        // key不能是null
        if (key == null)
            throw new NullPointerException();
        @SuppressWarnings("unchecked")
        Comparable<? super K> k = (Comparable<? super K>) key;
        
        do {
            // 父结点默认为根节点
            parent = t;
            // 调用比较方法,前者大于后者返回正数,小于返回负数,等于返回0
            cmp = k.compareTo(t.key);
            if (cmp < 0)
                t = t.left;
            else if (cmp > 0)
                t = t.right;
            else
                return t.setValue(value);
        } while (t != null);
    }
    
    // 找到父结点后,新建一个结点Entry插入
    Entry<K,V> e = new Entry<>(key, value, parent);
    // cmp为 当前插入结点和父结点比较的值：大于0表示比父结点大,小于0表示比父结点小
    if (cmp < 0)
        parent.left = e;
    else
        parent.right = e;
    // 将排序二叉树优化为红黑树,保持大致平衡
    fixAfterInsertion(e);
    size++;
    modCount++;
    return null;
}

// 红黑树算法
private void fixAfterInsertion(Entry<K,V> x) {
    x.color = RED;

    while (x != null && x != root && x.parent.color == RED) {
        if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
            Entry<K,V> y = rightOf(parentOf(parentOf(x)));
            if (colorOf(y) == RED) {
                setColor(parentOf(x), BLACK);
                setColor(y, BLACK);
                setColor(parentOf(parentOf(x)), RED);
                x = parentOf(parentOf(x));
            } else {
                if (x == rightOf(parentOf(x))) {
                    x = parentOf(x);
                    rotateLeft(x);
                }
                setColor(parentOf(x), BLACK);
                setColor(parentOf(parentOf(x)), RED);
                rotateRight(parentOf(parentOf(x)));
            }
        } else {
            Entry<K,V> y = leftOf(parentOf(parentOf(x)));
            if (colorOf(y) == RED) {
                setColor(parentOf(x), BLACK);
                setColor(y, BLACK);
                setColor(parentOf(parentOf(x)), RED);
                x = parentOf(parentOf(x));
            } else {
                if (x == leftOf(parentOf(x))) {
                    x = parentOf(x);
                    rotateRight(x);
                }
                setColor(parentOf(x), BLACK);
                setColor(parentOf(parentOf(x)), RED);
                rotateLeft(parentOf(parentOf(x)));
            }
        }
    }
    root.color = BLACK;
}
```

### 4.获取元素get()

```java
// 根据key获取value
public V get(Object key) {
    Entry<K,V> p = getEntry(key);
    return (p==null ? null : p.value);
}

// 获取Map中的结点
final Entry<K,V> getEntry(Object key) {
    // 外比较器存在时,调用外比较器的方法
    if (comparator != null)
        return getEntryUsingComparator(key);
    // 传入的key不能为空
    if (key == null)
        throw new NullPointerException();
    @SuppressWarnings("unchecked")
    Comparable<? super K> k = (Comparable<? super K>) key;
    
    // p默认为根结点
    Entry<K,V> p = root;
    while (p != null) {
        int cmp = k.compareTo(p.key);
        // k小于p.key：往左子树找
        if (cmp < 0)
            p = p.left;
        // k大于p.key: 往右子树找
        else if (cmp > 0)
            p = p.right;
        // k等于p.key：返回p
        else
            return p;
    }
    // 找不到则返回null
    return null;
}

// 外比较器存在,获取结点
final Entry<K,V> getEntryUsingComparator(Object key) {
    @SuppressWarnings("unchecked")
    K k = (K) key;
    Comparator<? super K> cpr = comparator;
    // 当外比较器不为空
    if (cpr != null) {
        // p默认为根结点
        Entry<K,V> p = root;
        // 从根结点开始,一直遍历到叶子结点
        while (p != null) {
            // 比较k和p.key
            int cmp = cpr.compare(k, p.key);
            // k小于p.key：往左子树找
            if (cmp < 0)
                p = p.left;
            // k大于p.key: 往右子树找
            else if (cmp > 0)
                p = p.right;
            // k等于p.key：返回p
            else
                return p;
        }
    }
    // 找不到则返回null
    return null;
}
```

### 5.判断包含contain()

```java
// containsKey判断是否存在key值,调用获取结点的方法,时间复杂度和get()一样
public boolean containsKey(Object key) {
    return getEntry(key) != null;
}

// containsValue判断是否存在value值
public boolean containsValue(Object value) {
    // 遍历整个红黑树
    for (Entry<K,V> e = getFirstEntry(); e != null; e = successor(e))
        // 比较value值
        if (valEquals(value, e.value))
            return true;
    return false;
}

// 获取最左边的叶子结点,因为红黑树中最左边的结点最小
final Entry<K,V> getFirstEntry() {
    Entry<K,V> p = root;
    if (p != null)
        while (p.left != null)
            p = p.left;
    return p;
}

/**
* 获取树结点的后继结点
* 从最左边的结点开始，依次找后继结点。给定一个结点，找其后继结点的算法为： 
* 1. 如果有右子结点，则后继结点取整个右子树中最小的结点。
* 2. 如果没有右子结点，则后继结点取父结点或某个祖先结点。
*    是父结点的左孩子,后继结点取父结点。
*    是父结点的右孩子,找它的父结点,判断父结点是不是右孩子,直到其父结点不是右孩子才停止,则此父结点就是后继结点。如
*    果找不到,则后继为空遍历结束。
**/
static <K,V> TreeMap.Entry<K,V> successor(Entry<K,V> t) {
    // 如果为空,则表示下一个为空,返回空以便退出循环。
    if (t == null)
        return null;
    
    // 存在右结点,找右子树中的最左结点
    else if (t.right != null) {
        Entry<K,V> p = t.right;
        // 寻找最左结点
        while (p.left != null)
            p = p.left;
        return p;    
    } 
    // 没有右结点,找祖先结点
    else {
        // 先找父结点
        Entry<K,V> p = t.parent;
        Entry<K,V> ch = t;
        // 当父结点不为空,且当前结点为右子结点
        while (p != null && ch == p.right) {
            ch = p;
            p = p.parent;
        }
        // 寻找祖先结点。当某一个祖先结点为左子结点时,返回其父结点
        // 如果找不到这样的父结点,返回本身
        return p;
    }
}

// 当o1和o2都是null时,返回true.  否则返回equals()方法比较结果.
static final boolean valEquals(Object o1, Object o2) {
    return (o1==null ? o2==null : o1.equals(o2));
}

```

### 6.删除元素remove()

```java
/**
* 删除算法
* 叶子结点：直接修改父节点对应引用置null即可。
* 只有一个子结点：在其父亲节点和孩子节点直接建立链接。
* 拥有两个子结点：寻找后继结点,替换当前节点的内容为后继节点,再删除后继节点。因为后继结点一定没有左子树,删除后继就
* 相当于删除一个子结点的情况。
**/

// 删除key对应的结点
public V remove(Object key) {
    // 根据key获取结点
    Entry<K,V> p = getEntry(key);
    if (p == null)
        return null;
    
    V oldValue = p.value;
    deleteEntry(p);
    // 返回已删除的value值
    return oldValue;
}

// 删除当前传入结点p
private void deleteEntry(Entry<K,V> p) {
    modCount++;
    size--;

    // p有两个子结点,将p转换为后继结点,后继结点一定没有左子树,转换成了只有一个结点的情况
    if (p.left != null && p.right != null) {
        // 找到后继结点,用后继结点s的内容替换当前结点p
        Entry<K,V> s = successor(p);
        p.key = s.key;
        p.value = s.value;
        // 指针p从 将要删除的节点替换为后继结点
        p = s;
    } 

    // p只有一个子结点,优先获取左节点,否则取右结点
    Entry<K,V> replacement = (p.left != null ? p.left : p.right);
	
    // 当p不是叶子结点,就是只有一个结点的情况：直接在父亲结点和子结点建立链接 
    if (replacement != null) {
        // 取p的父结点
        replacement.parent = p.parent;
        
        // p为根结点,其子结点就变成了根结点
        if (p.parent == null)
            root = replacement;
        
        // p有一个子结点,且子节点为左节点
        else if (p == p.parent.left)
            p.parent.left  = replacement;
        else
            p.parent.right = replacement;

        // 将p的所有引用全部置为null
        p.left = p.right = p.parent = null;

        // 红黑树处理--
        if (p.color == BLACK)
            fixAfterDeletion(replacement);
    } 
    
    
    // p为叶子节点的情况
    // p为叶子结点,且p为根结点,树只有一个结点
    else if (p.parent == null) { // return if we are the only node.
        root = null;
    // p为叶子节点,树的结点大于1
    } else { 
        // 红黑树处理！！
        if (p.color == BLACK)
            fixAfterDeletion(p);
        
	    // 直接删除p就行,将p.parent引用置为null	
        if (p.parent != null) {
            if (p == p.parent.left)
                p.parent.left = null;
            else if (p == p.parent.right)
                p.parent.right = null;
            p.parent = null;
        }
    }
}
```

