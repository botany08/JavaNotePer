## AtomicInteger原理

### 1.常用的API

```java
// 获取当前的值
public final int get()
    
// 获取当前的值，并设置新的值
public final int getAndSet(int newValue)
    
// 获取当前的值，并自增
public final int getAndIncrement()
    
// 获取当前的值，并自减
public final int getAndDecrement()
    
// 获取当前的值，并加上预期的值
public final int getAndAdd(int delta)
    
// 最终会设置成newValue,使用lazySet设置值后，可能导致其他线程在之后的一小段时间内还是可以读到旧的值。
void lazySet(int newValue)
```



### 2.源码解析

- `AtomicInteger`底层用的是`volatile`的变量和`CAS`来进行更改数据
  1. `volatile`保证线程的可见性，多线程并发时，一个线程修改数据，可以保证其它线程立马看到修改后的值。
  2. `CAS`保证数据更新的原子性。

```java
public class AtomicInteger extends Number implements java.io.Serializable {
    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final long valueOffset;
    static {
        try {
            // 用于获取value字段相对当前对象的“起始地址”的偏移量
            valueOffset = unsafe.objectFieldOffset(AtomicInteger.class.getDeclaredField("value"));
        } catch (Exception ex) { throw new Error(ex); }
    }

    private volatile int value;

    // 返回当前值
    public final int get() {
        return value;
    }

    // 递增加detla
    public final int getAndAdd(int delta) {
        // 三个参数，1、当前的实例 2、value实例变量的偏移量 3、当前value要加上的数(value+delta)。
        return unsafe.getAndAddInt(this, valueOffset, delta);
    }

    // 递增加1
    public final int incrementAndGet() {
        return unsafe.getAndAddInt(this, valueOffset, 1) + 1;
    }
...
}
```

