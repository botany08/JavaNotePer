## 基础概念

### 1.Set接口

- Set表示的是没有重复元素、且不保证顺序的容器接口。扩展了Collection，没有定义任何新的方法，重写了Collection中的一些方法。

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

### 3.底层实现原理

### 4.构造方法

## 具体方法实现

### 1.添加

### 2.删除

### 3.包含



