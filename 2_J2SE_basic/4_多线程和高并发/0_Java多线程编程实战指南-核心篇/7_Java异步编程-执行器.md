## Java的异步编程

###1 同步计算与异步计算

- **同步任务与异步任务**

  1. 同步任务

     任务的发起与任务的执行是串行的。对于任务来说，任务的发起和执行需要在同一个线程中完成。

  2. 异步任务

     任务的发起与任务的执行是并发的。对于任务来说，任务的发起和执行是在不同的线程。

- **同步任务的阻塞和非阻塞**

  1. **阻塞的同步**
1. 同步任务的发起线程在其发起该任务之后，必须等待该任务执行结束才能够执行其他操作。
     2. 阻塞意味着在同步任务执行结束前，该任务的发起线程并没有在运行(其生命周期状态不为RUNNABLE)。
     
2. **非阻塞的同步**
  1. 同步任务也并不一定总是会使其发起线程被阻塞。
   2. 同步任务的发起线程也可能以轮询的方式来等待任务的结束。
  3. 轮询(`Polling`) 是指，**任务的发起线程不断地检查其发起的任务是否执行结束**，**若任务已执行结束则执行下一步操作，否则继续检查任务，直到该任务完成**。
   4. 轮询意味着**在同步任务执行结束前，该任务的发起线程仍然在运行，只不过此时该线程的主要动作是检查相应的任务是否执行结束**。
  
3. **执行方式**
  1. 同步任务的发起线程，是采用阻塞的方式还是轮询方式来等待任务的结束很大程度上取决于使用的API。
   2. 在使用单个线程的情况下，多个同步任务只能够以同步的方式执行。
  
- **异步任务的阻塞和非阻塞**

  1. **阻塞的异步**

     1. 在向线程池提交一个任务之后立刻调用Future.get()来试图获取该任务的处理结果。
     2. 那么尽管该任务是异步执行的，发起线程仍然可能由于`Future.get()`调用时该任务尚未被线程池执行结束而被阻塞。

  2. **非阻塞的异步**

     1. 一个线程通过`ThreadPooIExecutor.submit(Callable<T>)`调用向线程池提交一个任务(任务的发起)。
     2. 在该调用返回之后该线程便可以执行其他操作了，而该任务可能在此之后才被线程池中的某一个工作者线程所执行。
     3. 任务的提交和任务的执行是在不同的线程中完成的。

  3. **执行方式**

     异步任务的执行需要多个线程来实现，单线程是无法实现异步的。

- **同步异步和阻塞**

  1. 阻塞与非阻塞只是任务执行方式`(同步/异步)`本身的一种属性，与任务执行方式之间并未有必然的联系。
  2. 异步任务既可能是非阻塞的，也可能是阻塞的。同步任务既可能是阻塞的，也可能是非阻塞的。
  3. 同步方式与异步方式的说法是相对的，它取决于任务的执行方式以及我们的观察角度。

- **同步异步任务的优缺点**

  1. 同步任务

     优点是代码简单、直观。

     缺点是同步在默认情况下，一般意味着阻塞，而阻塞会限制系统的吞吐率。

  2. 异步任务

     优点是异步在默认情况下，一般是非阻塞，有利于提高系统的吞吐率。

     缺点是更为复杂的代码和更多的资源投入(线程资源)。

###2 Java Executor框架

### 2.1任务执行抽象接口Executor

- `java.util.concurren.Executor`接口是对任务的执行进行抽象的接口。
  1. 只能为客户端代码执行任务，而无法将任务的处理结果返回给客户端代码。
- `ExecutorService`接口继承自`Executor`接口。
  1. 定义了多个提交方法，可以返回任务处理结果数据对象`Future`。
  2. 定义了`shutdown()`方法和`shutdownNow()`方法来关闭相应的服务。
- `ThreadPoolExecutor`是`ExecutorService`的默认实现类。
  1. 默认的线程池类。

####2.2 实用工具类Executors

- **返回ExecutorService实例的方法**

  ```java
  /**
  * 应用场景
  * 1. 适合用于执行大最耗时较短且提交比较频繁的任务。
  * 2. 如果提交的任务执行耗时较长，那么可能导致线程池中的工作者线程无限制地增加，最后导致过多的上下文切换，从而
  *    使得整个系统变慢。
  **/
  public static ExecutorService newCachedThreadPool();
  public static ExecutorService newCachedThreadPool(ThreadFactory threadFactory);
  
  
  /**
  * 应用场景
  * 1. 由于该方法返回的线程池的核心线程池大小,等于其最大线程池大小,因此该线程池中的工作者线程永远不会超时.
  *    必须在不再需要该线程池时主动将其关闭.
  **/
  public static ExecutorService newFixedThreadPool(int nThreads);
  public static ExecutorService newFixedThreadPool(int nThreads, ThreadFactory threadFactory);
  
  
  /**
  * 应用场景
  * 1. 适合用来实现单(多)生产者—单消费者模式。该方法的返回值无法被转换为ThreadPoolExecutor类型.
  **/
  public static ExecutorService newSingleThreadExecutor();
  public static ExecutorService newSingleThreadExecutor(ThreadFactory threadFactory);
  ```

- **Executors.newCachedThreadPool()**

  ```java
  /**
  * 新建的线程池配置
  * 1. 核心线程池大小为0,最大线程池大小不受限
  * 2. 工作者线程允许的最大空闲时间(keepAliveTime)为60秒，
  * 3. 内部以SynchronousQueue为工作队列
  **/
  public static ExecutorService newCachedThreadPool() {
          return new ThreadPoolExecutor(0, Integer.MAX_VALUE,60L, TimeUnit.SECONDS,
                                        new SynchronousQueue<Runnable>());
  }
  ```

  1. **工作者线程**

     该线程池中的所有工作者线程，在空闲了指定的时间后都可以被自动清理掉。

     在极端情况下，给线程池每提交一个任务都会导致一个新的工作者线程被启动。最终会导致系统中线程过多，从而导致过多的上下文切换降低系统的效率。

  2. **工作队列`SynchronousQueue`** 

     `SynchronousQueue`内部并不维护用于存储队列元素的实际存储空间，因此也可以看做阻塞队列长度为1。

     当提交一个任务到队列后，需要等该任务被取走执行，才能往队列中放入下一个任务。

- **Executors.newFixedThreadPool(int nThreads)**

  ```java
  /**
  * 新建的线程池配置
  * 1. 核心线程池大小和最大线程池大小均为nThreads
  * 2. 以无界队列为工作队列
  **/
  public static ExecutorService newFixedThreadPool(int nThreads) {
      return new ThreadPoolExecutor(nThreads, nThreads,0L, TimeUnit.MILLISECONDS,
                                    new LinkedBlockingQueue<Runnable>());
  }
  ```

  1. 工作者线程

     线程池大小一旦达到其核心线程池大小，就既不会增加也不会减少工作者线程的固定大小，即工作者线程不会被清除。线程池实例一旦不再需要，必须主动将其关闭。

- **Executors.newSingleThreadExecutor( )**

  ```java
  /**
  * 新建的ExecutorService实例
  * 1. 相当于Executors.newFixedThreadPool(1)所返回的线程池配置,但并非ThreadPoolExecutor实例.
  * 2. 是一个封装了ThreadPoolExecutor实例,且对外仅暴露ExecutorService接口所定义的方法的一个
  *    ExecutorService实例.
  * 3. 实例中只有核心线程和最大线程数都只为1,工作队列为无界队列,所以实现了 多生产者-单消费者 模式.
  **/
  public static ExecutorService newSingleThreadExecutor() {
      return new FinalizableDelegatedExecutorService
          (new ThreadPoolExecutor(1, 1,0L, TimeUnit.MILLISECONDS,
                                  new LinkedBlockingQueue<Runnable>()));
  }
  ```

  1. 任务的执行

     确保了在任意一个时刻只有一个任务会被执行，形成了类似锁**将原本并发的操作改为串行的操作的效果**。

  2. 适用场景

     用来执行访问了非线程安全对象，但是又不想引入锁的任务。

     用来执行I/O操作，如果使用一个线程执行I/O足以满足要求，那么仅使用一个线程即可.

####2.3 异步任务的批量执行：CompletionService

- **CompletionService接口的作用**

  1. `java.util.concurrent.CompletionService`接口主要用于,异步任务的批量提交以及获取这些任务的处理结果。

  ```java
  /**
  * 用于提交异步任务且有返回值
  * 1. task 参数代表待执行的异步任务.
  * 2. 该方法的返回值用于获取相应异步任务的处理结果.
  **/
  Future<V> submit(Callable<V> task);
  Future<V> submit(Runnable task, V result);
  
  /**
  * 用于获取异步任务的结果数据
  * 1. 阻塞方法,返回值是一个已经执行结束的异步任务对应的Future实例.
  * 2. 如果take()被调用时没有已执行结束的异步任务，那么take()的执行线程就会被暂停，直到有异步任务执行结束.
  * 3. 批量提交了多少个异步任务，则多少次连续调用CompletionService.take()便可以获取这些任务的处理结果.
  **/
  Future<V> take() throws InterruptedException;
  
  /**
  * 用于获取异步任务的结果数据
  * 1. 非阻塞方法,返回值是已执行结束的异步任务对应的Future实例.
  * 2. 相当于是take()方法的非阻塞版本,用法和take()差不多.
  **/
  Future<V> poll();
  Future<V> poll(long timeout, TimeUnit unit) throws InterruptedException;
  ```

- **实现类ExecutorCompletionService** 

  ```java
  /**
  * 构造方法一
  * 默认的阻塞队列为：LinkedBlockingQueue<Future<V>>
  **/
  public ExecutorCompletionService(Executor executor);
  
  /**
  * 构造方法二
  * 参数Executor：负责接收并执行异步任务.
  * 参数BlockingQueue：则用于存储已执行完毕的异步任务对应的Future实例.
  **/
  public ExecutorCompletionService(Executor executor,BlockingQueue<Future<V>> completionQueue);
  
  ```

- **ExecutorService接口**

  ```java
  /**
  * 用来批量提交异步任务
  * 1. 能够并发执行tasks参数所指定的一批任务.
  * 2. 只有在tasks参数所指定的一批任务中的所有任务都执行结束之后才返回,返回值是一个包含各个任务对应的Future实例
  *    的列表(List).
  **/
  <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)throws InterruptedException;
  ```

  

###3 异步任务的抽象：FutureTask类

#### 3.1基础概念

- **融合Runnable和Callable接口**

  1. `FutureTask`实现了`Runnable`接口。

     `FutureTask`表示的异步任务可以交给专门的工作者线程执行，也可以交给`Executor`实例(比如线程池)执行。

  2. `FutureTask`实现了`Future`接口

     `FutureTask`能够直接返回其代表的异步任务的处理结果，其本身就可以当做一个结果数据对象。

  3. 构造器

     ```java
     /**
     * 将Callable实例转换为Runnable实例
     * 1. 将任务的处理逻辑封装在一个Callable实例中，并以该实例为参数创建一个FutureTask实例。
     * 2. 相当于Callable实例转换为Runnable实例，而FutureTask实例本身也代表了要执行的任务。
     **/
     public FutureTask(Callable<V> callable);
     ```

- **FutureTask类的使用**

  1. 任务的提交

     用`FutureTask`实例(`Runnable`实例)为参数来创建并启动一个工作者线程以执行相应的任务。

     将`Futureask`实例交给`Executor`执行。

  2. 任务结果的获取

     一个工作者线程(可以是线程池中的一个工作者线程)负责调用`FutureTask.run()`执行相应的任务，另外一个线程则调用`FutureTask.get()`来获取任务的执行结果。

     **实现了任务的执行和对任务执行结果的处理并发执行**。

- 回调函数处理任务结果

  ```java
  /**
  * 当FutureTask实例所代表的任务执行结束后,FutureTask.done()会被执行.
  **/
  protected void done() { }
  ```

  1. 子类重写处理逻辑

     `FutureTask.done()`是`protected`方法，`FutureTask`子类可以覆盖该方法，实现对任务执行结果的处理。

     `FutureTask.done()`中的代码可以通过`FutureTask.get()`调用来获取任务的执行结果，此时由于任务已经执行结束，因此`FutureTask.get()`调用并不会使得当前线程暂停。

####3.2 可重复执行的异步任务

- 可重复执行但不返回结果

  ```java
  /**
  * FutureTask类
  * 1. FutureTask.runAndReset()方法可以使,一个FutureTask 实例所代表的任务能够多次被执行.
  **/
  protected boolean runAndReset();
  ```

- 可重复执行且返回结果

  抽象异步任务类`AsyncTask`，可以用来重复执行同一个对象所表示的任务，并且对该任务每次的执行结果进行处理。

  由于`AsyncTask`为`Android`方面的类，故不再继续详细看。

###4 计划任务

- **基本定义**

  1. 计划任务是指，在指定的时间或者周期性地被执行的任务。
  2. 典型的计划任务包括清理系统垃圾数据、系统监控、数据备份等。

- **`ScheduledExecutorService`接口的定义及创建**

  1. `ExecutorService`接口的子类`ScheduledExecutorService`接口定义了一组方法用于执行计划任务。

  2. `ScheduledExecutorService` 接口的默认实现类是`java.util.concurrent.ScheduledT hreadPooIExecutor`类，同时继承了`ThreadPoolExecutor`类。

  3. `Executors`提供了两个静态工厂方法用于创建`ScheduledExecutorService`实例

     ```java
     public static ScheduledExecutorService newScheduledThreadPool(int corePoolSize) {
         return new ScheduledThreadPoolExecutor(corePoolSize);
     }
     
     public static ScheduledExecutorService newScheduledThreadPool(
         int corePoolSize, ThreadFactory threadFactory) {
         return new ScheduledThreadPoolExecutor(corePoolSize, threadFactory);
     }
     ```

     

- **ScheduledExecutorService接口的方法**

  1. **延迟执行提交的任务**

     ```java
     /**
     * 1. 可以分别采用Callable实例或Runnable实例提交
     * 2. delay参数和unit参数一起用来表示,被提交的任务自其提交的那一刻到其开始执行之间的时间差，即延时。
     * 3. 返回值类型ScheduledFuture继承自Future接口,可以用来获取所提交的计划任务的处理结果。
     **/
     public <V> ScheduledFuture<V> schedule(Callable<V> callable,long delay, TimeUnit unit);
     public ScheduledFuture<?> schedule(Runnable command,long delay, TimeUnit unit);
     ```

  2. **周期性地执行提交的任务**

     执行周期指，同一个任务任意两次执行的开始时间之间的时间差。

     任务的耗时指，—个任务从其开始执行到其执行结束所需的时间。

     ```java
     /**
     * 固定的执行周期period
     * 1. initiaIDelay参数和unit参数指定了一个时间偏移，任务首次执行的开始时间就是任务提交时间加上这个偏移.
     * 2. 执行周期为Interval =max(Execution Tirne,period), 在任务耗时和指定的period取最大值，则有可能出
     *    现不固定的执行周期.
     **/
     public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,long initialDelay,
                                                       long period,TimeUnit unit);
     
     /**
     * 变化的时间周期：任务耗时+时间间隔delay
     * 1. initia!Delay参数和unit参数指定了一个时间偏移，任务首次执行的开始时间就是任务提交时间加上这个偏移.
     * 2. 执行周期为：Interval = Execution Time + delay,执行周期会随任务的耗时变化而变化.
     **/
     public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,long initialDelay,
                                                          long delay,TimeUnit unit);
     
     //一个任务的执行耗时超过period或者delay所表示的时间只会导致该任务的下一次执行时间被相应地推迟，而不会导致该任务被并发执行
     ```

  3. **任务执行的结果**

     - 延迟执行的任务最多只会被执行一次，`schedule`方法的返回值(`ScheduledFuture`实例)便能获取这种计划任务的执行结果、执行过程中抛出的异常以及取消任务的执行。

     - 周期性执行的任务会不断地被执行，直到任务被取消或者相应的`ScheduledExecutorService` 实例被关闭。返回值`(ScheduledFuture<?>)`能够取消相应的任务，但是它无法获取计划任务的一次或者多次的执行结果。

     - 提交给`ScheduledExecutorService`执行的计划任务在其执行过程中如果抛出未捕获的异常(`Uncaught` `Exception`), 那么该任务后续就不会再被执行。

  

