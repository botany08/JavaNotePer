## 流库

###1.从迭代到流的操作

- 流的定义

  l流是一种比集合更高的概念级别上的数据视图，可以获取和转换数据。

- 流的特点

  流遵循了“做什么而非怎么做"的原则。在流中，不用指定该操作应该以什么顺序或者在哪个线程中执行。

- 流和集合的区别

  1. 流并不存储其元素。元素可能存储在底层的集合中，也可能是按需生成。
  2. 流的操作不会修改其数据源。对流对象的操作，会生成一个新的流对象，不会对数据源产生影响。
  3. 流的操作是尽可能惰性执行的。直到程序需要其结果时，操作才会执行。

- 操作流的基本流程

  ```java
  /**
  * 基本流程
  * 1. 创建一个流.
  * 2. 指定将初始流转换为其他流的中间操作，可能包含多个步骤.
  * 3. 应用终止操作，从而产生结果.这个操作会强制执行之前的惰性操作.从此之后，这个流就再也不能用了.
  **/
  public class StreamDemo {
      public static void main(String[] args){
          List<String> employeeList = new LinkedList<>();
          employeeList.add("zangbaolin");
          employeeList.add("wuyanling");
          employeeList.add("ols");
          employeeList.add("zs");
          employeeList.add("wanghao");
          
          // 集合转换为流,并进行过滤
          long count = employeeList.stream().filter(w -> w.length()>7).count();
          System.out.println(count);
      }
  }
  
  // java.util.Collection<E> 
  // 产生当前集合中所有元素的顺序流
  default Stream<E> stream();
  // 产生当前集合中所有元素的并行流
  default Stream<E> parallel Stream();
      
  // java.util.stream.Stream<T>    
  // 产生一个流，其中包含当前流中满足P的所有元素。
  Stream<T> filter(Predicate<? super T> p);
  // 产生当前流中元素的数量。这是一个终止操作。
  long count();
  ```

###2.流的创建

```java
/**
* 数组创建流
**/
// Stream接口静态方法,可变长参数,可以传入数组
public static<T> Stream<T> of(T... values) {
    return Arrays.stream(values);
}
// Array类静态方法,将数组转换为流
public static <T> Stream<T> stream(T[] array) {
    return stream(array, 0, array.length);
}
// Array类静态方法,截取数组生成一个流
public static <T> Stream<T> stream(T[] array, int startInclusive, int endExclusive);
    
/**
* 集合创建流
**/
// Collection接口,将集合转换为流对象
default Stream<E> stream();

/**
* 创建元素为空的流
**/
// Stream接口静态方法
public static<T> Stream<T> empty();

/**
* 创建无限流
**/
// Stream接口静态方法,流对象中的值取决于Supplier返回的值
public static<T> Stream<T> generate(Supplier<T> s);
// 函数式接口,返回一个T类型对象
public interface Supplier<T> {
    T get();
}
// 实例：创建一个随机数的流
Stream<Double> randoms = Stream.generate(Math:: random);


// Stream接口静态方法,
public static<T> Stream<T> iterate(final T seed, final UnaryOperator<T> f);
// 函数式接口,作用是反复地将该函数应用到之前的结果上
public interface UnaryOperator<T> extends Function<T, T> {
    static <T> UnaryOperator<T> identity() {
        return t -> t;
    }
}
// 实例：创建一个等差为1的无限序列的流,0 1 2 3 4 5
Stream<BigInteger> integers= Stream.iterate(BigInteger.ZERO, n -> n.add(BigInteger.ONE));


/**
* 文件对象创建流
**/
// 位于java.nio.file中,指定文件中的行,生一个流。
public static Stream<String> lines(Path path) throws IOException;
public static Stream<String> lines(Path path, Charset cs) throws IOException;
```

###3.Stream接口中的filter、map和flatMap方法

```java
/**
* filter方法：对源数据的流对象进行过滤,产生一个新的流对象
**/
// 如果Predicate判断为true,则放入新流,否则舍弃.
Stream<T> filter(Predicate<? super T> predicate);

// 函数式接口,作用是传入一个T类型对象,返回一个Boolean.
public interface Predicate<T> {
    boolean test(T t);
}

/**
* map方法：对源数据的流根据某种规则进行转换,产生一个新的流对象
**/
// 原始流对象中是T类型对象,新流对象中是R类型对象.该函数接口中的方法,会应用到流中的所有元素.
<R> Stream<R> map(Function<? super T, ? extends R> mapper);

// 函数式接口,传入一个T类型对象,返回一个R类型对象
public interface Function<T, R> {
    R apply(T t);
}

/**
* flatMap方法：通过将mapper应用于当前流中所有元素所产生的结果连接到一起,产生一个新的流对象.
*             该方法返回的每一个结果都是一个流对象.
**/
<R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper);
```

###4.抽取子流和连接流

```java
/**
* 生成一个子流对象
**/
// 生成一个流对象,包含当前流的前maxSize个元素
Stream<T> limit(long maxSize);

// 生成一个流对象,包含当前流前n个元素外的所有元素
Stream<T> skip(long n);

/**
* 连接两个流对象,产生一个新的流对象
**/
public static <T> Stream<T> concat(Stream<? extends T> a, Stream<? extends T> b);



// 实例：抽取子流和连接子流
public class StreamDemo {
    public static void main(String[] args){
        // 创建一个流
        String[] song = new String[]{"gently","down","up","red","black","white","yellow"};

        // 当前流中除了前n个元素之外的所有元素,生成一个新的流对象
        Stream<String> songStreamSkip = Stream.of(song).skip(4);

        // 包含了当前流中最初的maxSize个元素,生成一个新的流对象
        Stream<String> songStreamLimit = Stream.of(song).limit(4);

        // 将两个子流对象连接起来,生成一个新的流对象
        Stream<String> songSteamConcat = Stream.concat(songStreamSkip,songStreamLimit);
    }
}
```

###5.其他的流转换

```java
// 剔除重复元素,产生一个新流对象
Stream<T> distinct();

// 流的排序
Stream<T> sorted();
Stream<T> sorted(Comparator<? super T> comparator);



// 遍历整个流在每次获取一个元素时，都会调用一个函数
Stream<T> peek(Consumer<? super T> action);
// 函数式接口
public interface Consumer<T> {
    void accept(T t);
}
// peek方法实例
public class StreamDemo {
    public static void main(String[] args){
        // 创建一个流
        String[] song = new String[]{"gently","down","up","red","black","white","yellow","yellow"};

        // 剔除重复元素,产生一个新流对象
        Stream<String> streamDistinct = Stream.of(song).distinct();
        // peek方法,对每个元素进行处理,如果不加toArray()是无法打印的,说明流是惰性处理.
        streamDistinct.peek(e -> System.out.println(e)).limit(100).toArray();
    }
}
```

###6.简单约简

- 基本定义

  约简是一种终结操作(terminaloperation), 会将流约简为可以在程序中使用的非流值。

- 约简方法

  ```java
  // 返回流对象中,元素的数量
  long count();
  
  // 返回流对象中,最大值
  Optional<T> max(Comparator<? super T> comparator);
  
  // 返回流对象中,最小值
  Optional<T> min(Comparator<? super T> comparator);
  
  // findFirst返回的是非空集合中的第一个值,通常与filter方法搭配使用
  Optional<T> findFirst();
  
  // 返回一个任意匹配的值
  Optional<T> findAny();
  
  // 流中任意一个元素匹配,返回true
  boolean anyMatch(Predicate<? super T> predicate);
  
  // 流中所有元素都匹配,返回true
  boolean allMatch(Predicate<? super T> predicate);
  
  // 流中没有一个元素匹配,返回true
  boolean noneMatch(Predicate<? super T> predicate);
  ```

###7.Optional类型 

- 基本定义
  1. Optional<T>对象是一种包装器对象，要么包装了类型T的对象，要么没有包装任何对象.
  2. Optional<T>类型被当作一种更安全的方式，用来替代类型T的引用，这种引用要么引用某个对象，要么为null。

####7.1使用Optional值 

```java
// 返回Optional的值,如果为空则抛出异常
public T get() {
    if (value == null) {
        throw new NoSuchElementException("No value present");
    }
    return value;
}

// 返回Optional的值,如果为空则返回other
public T orElse(T other) {
    return value != null ? value : other;
}

// 返回Optional的值,如果为空则返回函数接口other的值
public T orElseGet(Supplier<? extends T> other) {
    return value != null ? value : other.get();
}

// 返回Optional的值,如果为空则抛出异常
public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
    if (value != null) {
        return value;
    } else {
        throw exceptionSupplier.get();
    }
}

// 判断optional的值是否存在,存在返回true
public boolean isPresent() {
    return value != null;
}

// 如果optional值存在,则传入该值到函数接口处理
public void ifPresent(Consumer<? super T> consumer) {
    if (value != null)
        consumer.accept(value);
}

// 如果值不存在返回空对象optional,如果存在则放到Function进行转换,并返回一个新的optional对象
public<U> Optional<U> map(Function<? super T, ? extends U> mapper) {
    Objects.requireNonNull(mapper);
    if (!isPresent())
        return empty();
    else {
        // Function函数接口,将T类型转换成R类型返回
        return Optional.ofNullable(mapper.apply(value));
    }
}

// 如果值为空则返回一个空optional对象,否则返回新的value值包装类optional
public static <T> Optional<T> ofNullable(T value) {
    return value == null ? empty() : of(value);
}

// 返回一个value值的包装类Optional
public static <T> Optional<T> of(T value) {
    return new Optional<>(value);
}
```

####7.2创建Optional值 

```java
// 如果值为空则返回一个空optional对象,否则返回新的value值包装类optional
public static <T> Optional<T> ofNullable(T value) {
    return value == null ? empty() : of(value);
}

// 返回一个value值的包装类Optional
public static <T> Optional<T> of(T value) {
    return new Optional<>(value);
}

// 返回一个空的optional对象
public static<T> Optional<T> empty() {
    @SuppressWarnings("unchecked")
    Optional<T> t = (Optional<T>) EMPTY;
    return t;
}
```

####7.3用flatMap来构建Optional值的函数 

```java
// 将mapper应用于option的值,并返回一个新optional对象
public<U> Optional<U> flatMap(Function<? super T, Optional<U>> mapper) {
    Objects.requireNonNull(mapper);
    if (!isPresent())
        return empty();
    else {
        // T为当前optional值的类型,Optional<U>为返回的新类型
        return Objects.requireNonNull(mapper.apply(value));
    }
}

// 函数接口,将T类型转换为R类型返回
public interface Function<T, R> {
    R apply(T t);
}

// 如果obj为空则报错,否则返回obj对象
public static <T> T requireNonNull(T obj) {
    if (obj == null)
        throw new NullPointerException();
    return obj;
}
       
```

###8.收集结果 

```java
/**
* 1. 遍历流对象中的所有元素
**/
// 位于java.util.stream.BaseStream,返回一个迭代器
Iterator<T> iterator();

// 以任意顺序遍历各个元素,适用于并行流
void forEach(Consumer<? super T> action);

// 以流的顺序遍历元素,不适用于并行流,会降低并行优势
void forEachOrdered(Consumer<? super T> action);

// 函数接口,对传入值进行处理
public interface Consumer<T> {
    void accept(T t);
}

/**
* 2. 将流转换为数据结构,数组集合等
**/
// 将流对象转换为一个对象数组
Object[] toArray();

// 将引用A[]::new传递给构造器时，返回一个A类型的数组
<A> A[] toArray(IntFunction<A[]> generator);

// 传入int类型参数,返回一个R类型对象
public interface IntFunction<R> {
    R apply(int value);
}

// 实例
String[] result = stream.toArray(String[]::new);

/**
* 3. 将流中的元素收集到R对象中
**/
// 参数为一个Collector接口的实例，Collector接口位于java.util.stream.Collector
<R, A> R collect(Collector<? super T, A, R> collector);

// 实例
List<String> result= stream.collect(Collectors.tolist());
Set<String> result= stream.collect(Collectors.toSet());
TreeSet<String> result = stream.collect(Co11ectors. toCo11ection(TreeSet::new));
String result= stream.collect(Collectors.joining());
String result= stream.collect(Collectors.joining(", "));
String result= stream.map(Object::toString).collect(Collectors.joining(", "));

/**
* 将流的结果约简为总和、平均值、最大值或最小值，可以使用summarizing(Int/Long/Double)方法中的某一个
**/
Collector<T, ?, IntSummaryStatistics> summarizingInt(ToIntFunction<? super T> mapper);
Collector<T, ?, LongSummaryStatistics> summarizingLong(ToLongFunction<? super T> mapper);
Collector<T, ?, DoubleSummaryStatistics> summarizingDouble(ToDoubleFunction<? super T> mapper);
```

###9.收集到映射表中Map

```java
// 假设流中元素为类Person,通过collect方法收集到Map中
<R, A> R collect(Collector<? super T, A, R> collector);
Collector<T, ?, Map<K,U>> toMap(Function<? super T, ? extends K> keyMapper,
                                Function<? super T, ? extends U> valueMapper);

// 实例
Map<Integer, String> idToName = people().collect(
    Collectors.toMap(Person::getId,Person::getName));

```

###10.约简操作 

- 基本定义

  reduce方法是一种用于从流中计算某个值的通用机制，其最简单的形式将接受一个二元函数，并从前两个元素开始持续应用它。

- 使用

  1. 可结合操作：通常，如果reduce方法有一项约简操作op，该约简就会产生v0 op v1 op v2 op… 。如果可以将函数调用op(v1,v1+1 ) 写作v1 op v1+1，则该操作是可结合的。
  2. 可结合操作，例如求和、乘积、字符串连接、取最大值和最小值、求集的并与交等。

  ```java
  public class ReduceDemo {
      public static void main(String[] args){
          // 创建一个流
          Integer[] age = new Integer[]{2,3,7,8,13,23};
          Stream<Integer> ageStream = Stream.of(age);
  
  //        Optional<Integer> sum = ageStream.reduce((x,y)->x+y);
  		
          // stream流对象只能被处理一次,不能被反复使用
          Optional<Integer> sumS = ageStream.reduce(Integer::sum);
  
          System.out.println(sumS.get());
      }
  }
  ```

###11.基本类型流 

- 流的分类 - 基本类型和引用类型

  1. 流库中具有专门的类型IntStream、LongStream和DoubleStream, 用来存储基本类型值，而无需使用包装器。
  2. short、 char、 byte和boolean, 可以使用IntStream, 而对于float, 可以使用DoubleStream。

- 基本流和对象流的转换

  ```java
  /**
  * 对象流转换为基本类型流
  * 可以用mapTolnt、mapToLong和mapToDouble将其转换为基本类型流。
  **/
  Stream<String> words = ... ;
  IntStream lengths = words.mapToint(String::length);
  
  /**
  * 基本类型流转换为对象流，使用boxed方法
  **/
  Stream<Integer> integers = IntStream.range(0,100).boxed();
  ```

- 对象流和基本类型流的区别

  1. 基本类型流toArray方法会返回基本类型数组。
  2. 基本类型流产生可选结果的方法，会返回一个OptionalInt、OptionalLong或OptionalDouble。与Optional类类似，但是具有getAslnt、 getAsLong和getAsDouble方法，而不是get方法。
  3. 基本类型流具有返回总和、平均值、 最大值和最小值的sum、 average、 max和min方法。对象流没有定义这些方法。
  4. 基本类型流summaryStatistics方法会产生一个类型为IntSummaryStatistics、LongSummary-Statistics或DoubleSummaryStatistics的对象，它们可以同时报告流的总和、平均值、 最大值和最小值。

###12.并行流 

- 创建并行流

  ```java
  // Collection.parallelStream()方法从任何集合中获取一个并行流
  Stream<String> parallelWords = words.parallelStream();
  
  // para11el方法可以将任意的顺序流转换为并行流
  Stream<String> parallelWords = Stream.of(wordArray).parallel();
  
  ```

- 并行流特点

  1. 只要在终结方法执行时，流处于并行模式，那么所有的中间流操作都将被并行化。
  2. 当流操作并行运行时，目标是要让其返回结果与顺序执行时返回的结果相同。在并行流中，操作可以任意执行。

- 顺序流

  1. 默认情况下，从有序集合(数组和列表)、范围、生成器和迭代产生的流，或者通过调用Stream.sorted产生的流，都是有序的。
  2. 顺序流结果是按照原来元素的顺序累积的，因此是完全可预知的。如果运行相同的操作两次，将会得到完全相同的结果。

- 并行流正常工作的条件

  1. 数据应该在内存中。必须等到数据到达是非常低效的。
  2. 流应该可以被高效地分成若于个子部分。由数组或平衡二叉树支撑的流都可以工作得很好，但是Stream.iterate返回的结果不行。
  3. 流操作的工作量应该具有较大的规模。
  4. 流操作不应该被阻塞。
  5. 不要将所有的流都转换为并行流。只有在对已经位于内存中的数据执行大批计算操作时，才应该使用并行流。