##JDK线程池源码分析

### 1.JUC线程池框架

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/1_线程池框架图.png)

- JUC线程池框架中的其他接口或类都直接或间接的继承了Executor接口。虽然Executors与其他类或者接口没有明显的关系，但是Executors是线程池的工具类，利用它可以生成各种线程池。 

#### 1.1具体接口/类说明

- **Executors**
  1. Executors是一个工具类，用其可以创建ExecutorService、ScheduledExecutorService、ThreadFactory、Callable等对象。

- **Executor**
  2. Executor接口提供一种，将任务提交与每个任务将如何运行的机制(包括线程使用的细节、调度等)分离开来的方法。通常使用Executor而不是显式地创建线程。

- **ExecutorService**
  1. ExecutorService继承自Executor接口。
  2. ExecutorService提供了管理终止的方法，以及可为跟踪一个或多个异步任务执行状况而生成Future的方法。 
  3. 可以关闭ExecutorService，这将导致其停止接受新任务。关闭后，执行程序将最后终止，这时没有任务在执行，也没有任务在等待执行，并且无法提交新任务。

- **ScheduledExecutorService**
  1. ScheduledExecutorService继承自ExecutorService接口，可安排在给定的延迟后运行或定期执行的命令。

- **ScheduledThreadPoolExecutor**
  1. ScheduledThreadPoolExecutor实现ScheduledExecutorService接口。
  2. 可安排在给定的延迟后运行命令，或者定期执行命令。
  3. 需要多个辅助线程时，或者要求ThreadPoolExecutor具有额外的灵活性或功能时，此类要优于Timer。

- **AbstractExecutorService**
  1. AbstractExecutorService继承自ExecutorService接口，其提供ExecutorService执行方法的默认实现。
  2. 此类使用newTaskFor返回的RunnableFuture，实现submit、invokeAny和invokeAll方法。
  3. 默认情况下，RunnableFuture是此包中提供的FutureTask类。

- **ThreadPoolExecutor**
  1. ThreadPoolExecutor实现了AbstractExecutorService接口，也是一 ExecutorService。
  2. 使用可能的几个池线程之一执行每个提交的任务，通常使用Executors工厂方法配置。 
  3. **线程池可以解决两个不同问题**
     - 由于减少了每个任务调用的开销，通常可以在执行大量异步任务时提供增强的性能。
     - 还可以提供绑定和管理资源(包括执行任务集时使用的线程)的方法。
     - 每个ThreadPoolExecutor还维护着一些基本的统计数据，如完成的任务数。



### 2.ThreadPoolExecutor数据结构

- 在ThreadPoolExecutor的内部，主要由`BlockingQueue`和`AbstractQueuedSynchronizer`对其提供支持。

#### 2.1接口BlockingQueue

- `BlockingQueue`接口有多种数据结构的实现，如`LinkedBlockingQueue`、`ArrayBlockingQueue`等。

#### 2.2抽象类AbstractQueuedSynchronizer

- `AbstractQueuedSynchronizer`抽象类提供了一个基于FIFO队列，可以用于构建锁或者其他相关同步装置的基础框架。 



### 3.ThreadPoolExecutor内部类Worker

####3.1ThreadPoolExecutor内部类

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/2_Worker继承图.png)

- **内部类**
  1. `ThreadPoolExecutor`的核心内部类为`Worker`，继承了`AQS`抽象类并且实现了`Runnable`接口，作用是**对资源进行了复用，减少创建线程的开销**。
  2. 其他内部类则为**拒绝策略类**，是拒绝任务提交时的所采用的不同策略，比如`AbortPolicy`、`CallerRunsPolicy`、`DiscardOldestPolicy`、`DiscardPolicy`。
  3. **`AbortPolicy`**，用于被拒绝任务的处理程序，将抛出`RejectedExecutionException`。
  4. **`CallerRunsPolicy`**，用于被拒绝任务的处理程序，直接在`execute`方法的调用线程中运行被拒绝的任务。如果执行程序已关闭，则会丢弃该任务。
  5. **`DiscardOldestPolicy`**，用于被拒绝任务的处理程序，默认情况下将丢弃被拒绝的任务。
  6. **`DiscardPolicy`**，用于被拒绝任务的处理程序，放弃最旧的未处理请求，然后重试execute。如果执行程序已关闭，则会丢弃该任务。

####3.2Worker类

- **类的继承关系**

  ```java
  private final class Worker extends AbstractQueuedSynchronizer implements Runnable {}
  ```

  1. `Worker`继承了`AQS`抽象类，其重写了`AQS`的一些方法。

  2. 可作为一个`Runnable`对象，从而可以创建线程`Thread`。

     

- **类的属性**

  ```java
  private final class Worker extends AbstractQueuedSynchronizer implements Runnable {
      // 序列化ID
      private static final long serialVersionUID = 6138294804551838833L;
  
      // 1.用来封装worker(因为worker为Runnable对象),表示一个线程.
      final Thread thread;
  
      // 2.表示该worker所包含的Runnable对象,即用户自定义的Runnable对象,完成用户自定义的逻辑的Runnable对象.
      Runnable firstTask;
  
      // 3.表示已完成的任务数量.
      volatile long completedTasks;
  }
  ```

  

- **类的构造函数**

  ```java
  /**
  *  作用：构造一个worker对象,并设置AQS的state为-1,同时初始化了对应的域.
  **/
  Worker(Runnable firstTask) {
      // 设置状态为-1
      setState(-1); 
      // 初始化第一个任务
      this.firstTask = firstTask;
      // 根据当前worker，初始化线程
      this.thread = getThreadFactory().newThread(this);
  }
  ```

  

####3.3Worker类的核心函数

```java
private final class Worker extends AbstractQueuedSynchronizer implements Runnable {
    // 重写Runnable接口的run()方法
    public void run() {
        runWorker(this);
    }
    
    // 是否被独占,0代表未被独占,1代表被独占
    protected boolean isHeldExclusively() {
        return getState() != 0;
    }

    // 尝试获取
	protected boolean tryAcquire(int unused) {
        // 比较并设置状态成功
	    if (compareAndSetState(0, 1)) {
            // 设置独占线程
	        setExclusiveOwnerThread(Thread.currentThread());
	        return true;
	    }
	    return false;
	}

    // 尝试释放
	protected boolean tryRelease(int unused) {
        // 设置独占线程为null
	    setExclusiveOwnerThread(null);
        // 设置状态为0
	    setState(0);
	    return true;
	}

    // 获取锁
	public void lock() {
        acquire(1); 
    }
    
    // 尝试获取锁
	public boolean tryLock() {
        return tryAcquire(1); 
    }
    
    // 释放锁
	public void unlock() {
        release(1); 
    }
    
    // 是否被独占
	public boolean isLocked() {
        return isHeldExclusively(); 
    }

    /**
    * 线程中断,是给目标线程发送一个中断信号,如果目标线程没有接收线程中断的信号并结束线程,线程则不会终止,
    * 具体是否退出或者执行其他逻辑由目标线程决定.
    **/
	void interruptIfStarted() {
	    Thread t;
        // AQS状态大于等于0并且worker对应的线程不为null并且该线程没有被中断
	    if (getState() >= 0 && (t = thread) != null && !t.isInterrupted()) {
	        try {
                // 中断线程
	            t.interrupt();
	        } catch (SecurityException ignore) {
	        }
	    }
	}
        
}
```



###4.ThreadPoolExecutor类的属性

```java
public class ThreadPoolExecutor extends AbstractExecutorService {
    // 线程池的控制状态(用来表示线程池的运行状态(整形的高3位)和运行的worker数量(低29位))
    private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));
    // 29位的偏移量
    private static final int COUNT_BITS = Integer.SIZE - 3;
    // 最大容量(2^29 - 1)
    private static final int CAPACITY   = (1 << COUNT_BITS) - 1;

    // runState is stored in the high-order bits
    // 线程运行状态，总共有5个状态，需要3位来表示(所以偏移量的29 = 32 - 3)
    private static final int RUNNING    = -1 << COUNT_BITS;
    private static final int SHUTDOWN   =  0 << COUNT_BITS;
    private static final int STOP       =  1 << COUNT_BITS;
    private static final int TIDYING    =  2 << COUNT_BITS;
    private static final int TERMINATED =  3 << COUNT_BITS;
    
    // 阻塞队列
    private final BlockingQueue<Runnable> workQueue;
    // 可重入锁
    private final ReentrantLock mainLock = new ReentrantLock();
    // 存放工作线程集合
    private final HashSet<Worker> workers = new HashSet<Worker>();
    // 终止条件
    private final Condition termination = mainLock.newCondition();
    // 最大线程池容量
    private int largestPoolSize;
    // 已完成任务数量
    private long completedTaskCount;
    // 线程工厂
    private volatile ThreadFactory threadFactory;
    // 拒绝执行处理器
    private volatile RejectedExecutionHandler handler;
    // 线程等待运行时间
    private volatile long keepAliveTime;
    // 是否运行核心线程超时
    private volatile boolean allowCoreThreadTimeOut;
    // 核心池的大小
    private volatile int corePoolSize;
    // 最大线程池大小
    private volatile int maximumPoolSize;
    // 默认拒绝执行处理器
    private static final RejectedExecutionHandler defaultHandler =
        new AbortPolicy();
    //
    private static final RuntimePermission shutdownPerm =
        new RuntimePermission("modifyThread");
}
```



#### 4.1线程池的控制状态变量

- **AtomicInteger原子变量**

  1. `AtomicInteger`类型的ctl属性，ctl为线程池的控制状态，用来表示线程池的运行状态(整型的高3位)和运行的`worker`数量(低29位)。
  2. 由于有5种状态，最少需要3位表示，所以采用的AtomicInteger的高3位来表示，低29位用来表示worker的数量，即最多表示2^29 - 1。

- **线程池状态变换**

  1. 线程池被一旦被创建，就处于`RUNNING`状态，并且线程池中的任务数为0。

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/3_线程池的状态变换.png)

```java
// 线程池的控制状态(用来表示线程池的运行状态(整形的高3位)和运行的worker数量(低29位))
private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));

// 29位的偏移量, Integer.SIZE=32
private static final int COUNT_BITS = Integer.SIZE - 3;

/**
* RUNNING：接受新任务并且处理已经进入阻塞队列的任务.
* SHUTDOWN：不接受新任务，但是处理已经进入阻塞队列的任务.
* STOP：不接受新任务，不处理已经进入阻塞队列的任务并且中断正在运行的任务.
* TIDYING：所有的任务都已经终止,workerCount为0,线程转化为TIDYING状态并且调用terminated钩子函数.
* TERMINATED：terminated钩子函数已经运行完成.
**/
private static final int RUNNING    = -1 << COUNT_BITS;
private static final int SHUTDOWN   =  0 << COUNT_BITS;
private static final int STOP       =  1 << COUNT_BITS;
private static final int TIDYING    =  2 << COUNT_BITS;
private static final int TERMINATED =  3 << COUNT_BITS;
```

###5.ThreadPoolExecutor类的构造函数

```java
/**
* 线程池构造方法参数
* 1.corePoolSize：用于指定核心线程池大小
* 2.maximumPoolSize：用于指定最大线程池大小
* 3.keepAliveTime：和unit一起,用于指定线程池中空闲(Idle)线程的最大存活时间
* 4.unit：和keepAliveTime一起,用于指定线程池中空闲(Idle)线程的最大存活时间
* 5.workQueue：阻塞队列,被称为工作队列
* 6.threadFactory：指定用于创建工作者线程的线程工厂
* 7.handler：表示当工作队列满且达到最大线程数,客户提交的任务被拒绝时,线程池执行的处理策略
**/
public ThreadPoolExecutor(int corePoolSize,
                          int maximumPoolSize,
                          long keepAliveTime,
                          TimeUnit unit,
                          BlockingQueue<Runnable> workQueue,
                          ThreadFactory threadFactory,
                          RejectedExecutionHandler handler) {
    // 核心线程数大小不能小于0,线程池的初始最大容量不能小于0.
    // 初始最大容量不能小于核心大小,keepAliveTime不能小于0.
    if (corePoolSize < 0 || maximumPoolSize <= 0 || maximumPoolSize < corePoolSize ||
            keepAliveTime < 0)
        throw new IllegalArgumentException();
    
    // 当工作队列为null,或线程池为null,拒绝处理为null
    if (workQueue == null || threadFactory == null || handler == null)
        throw new NullPointerException();
    
    // 初始化相应的域
    this.acc = System.getSecurityManager() == null ? null : AccessController.getContext();
    this.corePoolSize = corePoolSize;
    this.maximumPoolSize = maximumPoolSize;
    this.workQueue = workQueue;
    this.keepAliveTime = unit.toNanos(keepAliveTime);
    this.threadFactory = threadFactory;
    this.handler = handler;
}

// 1.默认的线程工厂和拒绝策略处理
public ThreadPoolExecutor(int corePoolSize,
                          int maximumPoolSize,
                          long keepAliveTime,
                          TimeUnit unit,
                          BlockingQueue<Runnable> workQueue) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
             Executors.defaultThreadFactory(), defaultHandler);
    }

// 2.默认的线程工厂
public ThreadPoolExecutor(int corePoolSize,
                          int maximumPoolSize,
                          long keepAliveTime,
                          TimeUnit unit,
                          BlockingQueue<Runnable> workQueue,
                          RejectedExecutionHandler handler) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
             Executors.defaultThreadFactory(), handler);
    }

// 3.默认的拒绝策略处理
public ThreadPoolExecutor(int corePoolSize,
                          int maximumPoolSize,
                          long keepAliveTime,
                          TimeUnit unit,
                          BlockingQueue<Runnable> workQueue,
                          ThreadFactory threadFactory) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
             threadFactory, defaultHandler);
    }
```

### 6.线程池新建工作线程execute()

#### 6.1主要执行流程

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/5_线程池执行逻辑.png)

#### 6.2execute()函数

- 当在客户端调用`submit`时，之后会间接调用到`execute`函数，其在将来某个时间执行给定任务，此方法中并不会直接运行给定的任务。 
- **核心线程数`corePoolSize`**
  1. **如果运行的线程少于`corePoolSize`，则创建新线程来处理请求**，即使其他辅助线程是空闲的。 
- **阻塞队列大小`maxPoolSzie`** 
  1. 如果运行的线程多于`corePoolSize`而少于`maximumPoolSize`，则仅当阻塞队列满时才创建新线程。
  2. 如果设置的`corePoolSize`和`maximumPoolSize`相同，相当于创建了固定大小的线程池。 
  3. 如果将`maximumPoolSize`设置为基本的无界值(如`Integer.MAX_VALUE`)，则允许池适应任意数量的并发任务。

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/4_execute函数执行逻辑.png)

```java
public void execute(Runnable command) {
    // Runnable实例为null,抛出异常.
    if (command == null) 
        throw new NullPointerException();
    /*
    * 进行下面三步
    *
    * 1. 如果运行的线程小于corePoolSize,则尝试使用用户定义的Runnalbe对象创建一个新的线程,调用addWorker函数会
    *    原子性的检查runState和workCount,通过返回false来防止在不应该添加线程时添加了线程.
    * 2. 如果一个任务能够成功入队列，在添加一个线程时仍需要进行双重检查.有可能出现在前一次检查后该线程池死亡了,
    *    或者当进入到此方法时,线程池已经shutdown,所以需要再次检查状态.若有必要,当停止时还需要回滚入队列操作,或
    *    者当线程池没有线程时需要创建一个新线程.
    * 3. 如果无法入队列,那么需要增加一个新线程.如果此操作失败，那么就意味着线程池已经shutdown或者已经饱和了,所
    *    以拒绝任务.
    */
    
    // 获取线程池控制状态,默认为RUNNING状态.
    int c = ctl.get();
    
    /**
    * 1.COUNT_BITS值为29,[(1 << COUNT_BITS)-1]值为高3位的线程状态(都为0)和低29位的线程数(都为1)
    *   private static final int CAPACITY   = (1 << COUNT_BITS) - 1;
    * 
    * 2. [c & CAPACITY]值为高3位线程状态(都为0),取出低29位为1的值.TODO需要知道低29位怎么赋值.
    *    private static int workerCountOf(int c)  { return c & CAPACITY; }
    *
    * 3. 表示当worker数量小于corePoolSize核心线程数,workerCountOf(c)默认为0.
    *    workerCountOf(c) < corePoolSize
    **/
    // 判断条件一：线程池状态c的线程数,小于核心线程数.
    if (workerCountOf(c) < corePoolSize) { 
        /**
        * 添加worker,成功则返回
        * 第二个参数,true表示数量限制为corePoolSize(核心线程数),false为maximumPoolSize(最大线程数)
        **/
        if (addWorker(command, true)) 
            return;
        
        // 不成功则再次获取线程池控制状态
        c = ctl.get();
    }
    
    /**
    * 判断条件二：线程池处于RUNNING状态,将Runnable实例添加到阻塞队列中
    * 线程池处于RUNNING状态,将命令(用户自定义的Runnable对象)添加进workQueue队列
    **/
    if (isRunning(c) && workQueue.offer(command)) { 
        // 再次检查，获取线程池控制状态
        int recheck = ctl.get();
        
        // 线程池不处于RUNNING状态,将命令从workQueue队列中移除,执行拒绝策略
        if (! isRunning(recheck) && remove(command)) {
            reject(command);
        }
        /**
        * 线程池处于RUNNING状态,但是没有执行线程.利用null参数创建一个线程,但是不启动.
        * 因为此时Runnable实例在阻塞队列中.
        **/
        else if (workerCountOf(recheck) == 0) {
            // worker数量等于0,添加worker
            addWorker(null, false);
        }
            
    } 
    // 判断条件三：线程池不处于RUNNING状态或者无法入队列时,如果线程数小于最大线程数,添加worker.
    else if (!addWorker(command, false)) {
        // 添加worker失败,执行拒绝策略.表示线程池已经shutdown或者饱和.
        reject(command);
    } 
        
}
```



####6.3addWorker()函数

- `execute()`函数中主要会调用到`addWorker()`函数，作用是**在线程池中创建一个新的线程并执行**。
- 执行流程
  1. 原子性的增加`workerCount`.
  2. 将用户给定的任务封装成为一个`worker`,并将此`worker`添加进`workers`集合中.
  3. 启动`worker`对应的线程,并启动该线程,运行`worker`的run方法.
  4. 回滚`worker`的创建动作,即将`worker`从`workers`集合中删除,并原子性的减少`workerCount`.

```java
/**
* firstTask：指定新增线程执行的第一个任务或者不执行任务.
* core：core如果为true则使用corePoolSize绑定,否则为maximumPoolSize.
**/
private boolean addWorker(Runnable firstTask, boolean core) {
    // 标识break跳转的位置
    retry:
    
    // 循环作用：当线程池状态为RUNNING时,原子增加线程数+1
    for (;;) { 
        // 获取线程池控制状态,包括高3位的状态和低29位的数量
        int c = ctl.get();

        /**
        * 1. [c & ~CAPACITY]值为高3位的线程状态,低29位全为0
        *    private static int runStateOf(int c)     { return c & ~CAPACITY; }
        **/
        int rs = runStateOf(c);

        
        if (
            // 状态大于等于SHUTDOWN,为四种状态：SHUTDOWN、STOP、TIDYING、TERMINATED
            rs >= SHUTDOWN && 
            // 状态为SHUTDOWN,且第一个任务为null,且worker队列不为空
            ! (rs == SHUTDOWN &&  firstTask == null && ! workQueue.isEmpty())
           )     
            // 添加Runnable实例失败
            return false;

        /** 不断循环的条件：原子增加线程数失败,且运行状态未改变.
        * 若原子增加线程数成功,则break,执行其他逻辑.
        * 如果运行状态改变,则continue,继续原子增加线程数.
        **/
        for (;;) {
            // 获取worker数量,低29位的数量
            int wc = workerCountOf(c);
            // worker数量大于等于最大容量,或者worker数量大于等于核心线程数(最大线程池数),由第二个参数决定.
            if (wc >= CAPACITY || wc >= (core ? corePoolSize : maximumPoolSize))   
                // 添加worker失败
                return false;
            
            // 原子增加运行的线程数,增加成功则跳出循环
            if (compareAndIncrementWorkerCount(c))
                break retry;
            
            // 重新获取线程池状态,判断是否状态更改
            c = ctl.get();  
            
            // 如果当前的运行状态不等于rs,说明状态已被改变,返回重新执行
            if (runStateOf(c) != rs) 
                continue retry;
        }
    }

    // worker被启动的标识
    boolean workerStarted = false;
    // worker被添加到工作线程中的标识
    boolean workerAdded = false;

    Worker w = null;
    try {
        /** 根据Runnable实例,初始化worker.
        *   Worker(Runnable firstTask) {
        *       setState(-1);  设置AbstractQueuedSynchronizer状态为-1
        *       this.firstTask = firstTask;  
        *       this.thread = getThreadFactory().newThread(this);  通过线程工厂获取一个线程
        *   }
        **/
        w = new Worker(firstTask);
        // 获取worker对应的线程
        final Thread t = w.thread;
        
        
        // 当线程不为null
        if (t != null) { 
            /**
            * 线程池锁,可重入锁指的是在一个线程中可以多次获取同一把锁.
            * 一个线程在执行一个带锁的方法,该方法中又调用了另一个需要相同锁的方法,
            * 则该线程可以直接执行调用的方法，而无需重新获得锁.
            **/
            final ReentrantLock mainLock = this.mainLock;
            // 获取锁
            mainLock.lock();
            
            // 作用：持有锁的情况下,将worker添加到工作线程中
            try {
                // 获取线程池的运行状态,只有高3位的运行状态
                int rs = runStateOf(ctl.get());
                
                /**
                * 运行状态为RUNNING,或者 状态SHUTDOWN且执行实例为null
                * firstTask == null证明只新建线程而不执行任务
                **/
                if (rs < SHUTDOWN || (rs == SHUTDOWN && firstTask == null)) {
                    // 线程刚添加进来,还未启动就存活,抛出线程状态异常
                    if (t.isAlive()) 
                        throw new IllegalThreadStateException();
                    
                    // 将worker添加到worker集合,workers包含池中的所有工作线程,仅在持有mainLock时访问.
                    workers.add(w);
                    // 获取工作线程数量
                    int s = workers.size();
                    
                    // largestPoolSize记录着线程池中出现过的最大线程数量,如果s比它还要大,更新
                    if (s > largestPoolSize)
                        largestPoolSize = s;
                    
                    // worker的添加工作状态改为true
                    workerAdded = true;
                }
            } finally {
                // 释放锁
                mainLock.unlock();
            }
            
            // 当工作线程被添加后,启动线程
            if (workerAdded) {
                t.start();
                workerStarted = true;
            }
        }
        
    } finally {
        // worker没有启动,添加worker失败
        if (! workerStarted)
            addWorkerFailed(w);
    }
    
    // 返回工作线程被启动的标识
    return workerStarted;
}

// 添加worker失败的处理
private void addWorkerFailed(Worker w) {
    // 获取可重入锁并持有
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    try {
        if (w != null)
            // 从工作线程集合中移除
            workers.remove(w);
        // 原子减少线程数-1
        decrementWorkerCount();
        tryTerminate(); 
    } finally {
        mainLock.unlock();
    }
}
```



### 7.工作线程执行任务runWorker()

#### 7.1runWorker()函数

- **作用**
  1. 主要是执行用户给定的`Runnable`实例。
  2. 并且当给定任务完成后，会继续从阻塞队列中取任务，直到阻塞队列为空(即任务全部完成)。
  3. 在执行给定任务时，会调用钩子函数，利用钩子函数可以完成用户自定义的一些逻辑。 
- **执行流程**
  1. `while`循环中，不断地通过`getTask()`方法从`workerQueue`中获取任务。
  2. 如果线程池正在停止，则中断线程。否则调用3。
  3. 调用`task.run()`执行任务。
  4. 如果`task`为`null`则跳出循环，执行`processWorkerExit()`方法，销毁线程`workers.remove(w)`。
- **Worker执行任务上锁的原因**
  1. `Worker`继承了`AbstractQueuedSynchronizer`抽象类，来简化在执行任务时的获取、释放锁。
  2. 用`AQS`锁来控制中断，当运行时上锁，就不能中断，`TreadPoolExecutor`的`shutdown()`方法中断前都要获取`worker`锁。
  3. 只有在等待从`workQueue`中获取任务`getTask()`时才能中断，此时没有上锁。
  4. 但是`Worker`执行前，也会先判断中断标志。
- **Worker实现的是不可重入锁原因**
  1. 不想让`Worker`在执行任务时，在调用本身线程池实例，比如`setCorePoolSize()`这种线程池控制方法时可以再次获取锁(重入)。
  2. `ThreadPoolExecutor.setCorePoolSize()`时可能会`interruptIdleWorkers()`，在对一个线程`interrupt`时会要`w.tryLock()`。
  3. 防止`Worker`执行时，自己中断自己。

```java
final void runWorker(Worker w) {
    // 获取当前线程
    Thread wt = Thread.currentThread();
    // 获取当前任务,即Runnable实例
    Runnable task = w.firstTask;
    // 将Worker.firstTask置空并且释放锁
    w.firstTask = null;
    // 释放锁(设置state为0，允许中断)
    w.unlock();
    
    boolean completedAbruptly = true;
    try {
        // 任务不为null或者阻塞队列还存在任务,循环执行
        while (task != null || (task = getTask()) != null) { 
            // 执行的时候,获取不可重入锁.保证两点：1.不去重入  2.不被重入(在线程池中再次调用线程池执行)
            w.lock();
            
            /**
            * 作用：是否中断当前线程
            * 保证两点：1. 线程池没有停止  2. 保证线程没有中断
            **/
            if (
                // 当线程是状态处于TIDYING、TERMINATED
                (runStateAtLeast(ctl.get(), STOP) ||
                 // 线程中断标记为true,且线程状态是TIDYING、TERMINATED,且当前线程没有被中断
                (Thread.interrupted() && runStateAtLeast(ctl.get(), STOP))) && !wt.isInterrupted()
            ) {
                // 中断当前线程
                wt.interrupt();
            }
            
            // 作用：执行用户的Runnable实例方法
            try {
                // 在执行之前调用钩子函数
                beforeExecute(wt, task);
                Throwable thrown = null;
                try {
                    // 执行Runnable实例的run()方法
                    task.run();
                } catch (RuntimeException x) {
                    thrown = x; throw x;
                } catch (Error x) {
                    thrown = x; throw x;
                } catch (Throwable x) {
                    thrown = x; throw new Error(x);
                } finally {
                    // 执行完后调用钩子函数
                    afterExecute(task, thrown);
                }
            } finally {
                // 执行完后,将task置空,完成任务++,释放锁
                task = null;
                w.completedTasks++;
                w.unlock();
            }
            
        }
        completedAbruptly = false;
        
    } finally {
        // 处理完成后，调用钩子函数
        processWorkerExit(w, completedAbruptly);
    }
}
```

#### 7.2getTask()函数

- **作用**
  1. 于从`workerQueue`阻塞队列中获取`Runnable`对象。
  2. 于是阻塞队列，所以支持有限时间等待(`poll`)和无限时间等待(`take`)。
  3. 该函数中还会响应`shutDown`和`shutDownNow`函数的操作，若检测到线程池处于`SHUTDOWN`或`STOP`状态，则会返回null，而不再返回阻塞队列中的`Runnalbe`对象。
- **中断线程**
  1. 在`getTask()`函数中，从阻塞队列中获取下一个任务。如果当前线程超过了核心线程，就会有超时控制。一旦超时，工作线程就会尝试中断

```java
private Runnable getTask() {
    boolean timedOut = false; 
    
    // 无限循环，确保操作成功
    for (;;) { 
        
        // 获取线程池控制状态,和运行状态的高3位
        int c = ctl.get();
        int rs = runStateOf(c);

        // 表示调用了shutDown,且[调用了shutDownNow或worker阻塞队列为空]
        if (rs >= SHUTDOWN && (rs >= STOP || workQueue.isEmpty())) { 
            // 原子减少工作线程数量-1,返回null表示阻塞队列中没有任务
            decrementWorkerCount();
            return null;
        }
        
        // 获取工作线程数量
        int wc = workerCountOf(c);

        // 是否允许coreThread超时,或workerCount大于核心大小
        /**
        * timed变量用于判断是否需要进行超时控制.
        * 1. allowCoreThreadTimeOut默认是false,也就是核心线程不允许进行超时控制.
        * 2. wc > corePoolSize,示当前线程池中的线程数量大于核心线程数量,
        *    对于超过核心线程数量的这些线程,需要进行超时控制.
        **/
        boolean timed = allowCoreThreadTimeOut || wc > corePoolSize; 

        if (
            // worker数量大于maximumPoolSize(最大线程数)
            (wc > maximumPoolSize || (timed && timedOut))     
            &&
            // workerCount大于1或者worker阻塞队列为空(在阻塞队列不为空时，需要保证至少有一个wc)
            (wc > 1 || workQueue.isEmpty())) { 
            
              // 减少当前状态的工作线程数量,返回null表示阻塞队列中没有任务
              if (compareAndDecrementWorkerCount(c))
                  return null;
           
              // 跳过剩余部分，继续循环
              continue;
        }

        try {
            // 如果设置了超时时间,或者工作线程数大于核心线程数
            Runnable r = timed ?
                // 等待指定时间
                workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) :  
                // 一直等待，直到有元素
                workQueue.take();
            
            // 拿到Runnable实例,返回.
            if (r != null)
                return r;
            
            // 等待指定时间后，没有获取元素，则超时
            timedOut = true;
        } catch (InterruptedException retry) {
            // 抛出了被中断异常，重试，没有超时
            timedOut = false;
        }
    }
}
```



#### 7.3processWorkerExit()函数

- **作用**
  1. 在worker退出时调用到的钩子函数。
  2. 会根据是否中断了空闲线程来确定是否减少workerCount的值。
  3. 将worker从workers集合中移除并且会尝试终止线程池。
- **引起worker退出的原因**
  1. 阻塞队列已经为空，即没有任务可以运行了。 
  2. 调用了`shutDown`或`shutDownNow`函数。
- **当工作线程worker退出时，会尝试终止线程池**
  1. 当状态还是RUNNING时，表示没有手动关闭，不会终止。
  2. 当状态是SHUTDOWN时，且阻塞队列中有任务，不会终止。
  3. 当工作线程不为0时，不会终止。

```java
private void processWorkerExit(Worker w, boolean completedAbruptly) {
    // 如果被中断,工作线程数-1
    if (completedAbruptly)     
        decrementWorkerCount();

    // 获取可重入锁,主要是用来更新工作线程的数量
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    
    try {
        // 将worker完成的任务添加到总的完成任务中
        completedTaskCount += w.completedTasks;
        // 从workers集合中移除该worker
        workers.remove(w);
    } finally {
        // 释放锁
        mainLock.unlock();
    }
    
    /**
    *
    *
    **/
    // 尝试终止线程池状态,此时阻塞队列中已经没有任务
    tryTerminate();
    // 获取线程池控制状态
    int c = ctl.get();
    
    
    // 小于STOP的运行状态,状态为:RUNNING、SHUTDOWN.表示还要继续执行任务
    if (runStateLessThan(c, STOP)) {
        
        if (!completedAbruptly) {
            // allowCoreThreadTimeOut表示允许空闲线程继续存活一段时间
            int min = allowCoreThreadTimeOut ? 0 : corePoolSize;
            
            // 如果空闲线程允许存活,且阻塞队列中还有任务,则剩余工作线程还需要至少一个.
            if (min == 0 && ! workQueue.isEmpty()) 
                min = 1;
            
            /**
            * 先获取当前工作线程的数量，然后有两种情况:
            * 1.此时工作线程还至少有一个,可以继续执行,满足空闲线程允许存活的情况.
            * 2.此时工作线程大于等于核心线程数,表示极端情况下核心线程被占满,还有线程执行.
            **/
            if (workerCountOf(c) >= min) 
                // 直接返回
                return; 
        }
        // 如果线程池中已经没有了工作线程,则新建一个工作线程.
        addWorker(null, false);
    }
}
```



### 8.关闭线程池shutdown()

#### 8.1shutdown()函数

- **作用**
  1. 按过去执行已提交任务的顺序发起一个有序的关闭，但是不接受新任务。
  2. 首先会检查是否具有shutdown的权限，然后设置线程池的控制状态为`SHUTDOWN`，之后中断空闲的`worker`。
  3. 最后尝试终止线程池。
- **shutdown()和shutdownNow()区别**
  1. `shutdownNow`会尝试停止所有的活动执行任务、暂停等待任务的处理，并返回等待执行的任务列表，
  2. 但是其会终止所有的`worker`，而并非空闲的`worker`。 

```java
public void shutdown() {
    // 获取一个可重入锁
    final ReentrantLock mainLock = this.mainLock;
    mainLock.lock();
    
    try {
        // 检查shutdown权限
        checkShutdownAccess();
        // 设置线程池控制状态为SHUTDOWN
        advanceRunState(SHUTDOWN);
        // 中断空闲worker
        interruptIdleWorkers();
        // 调用shutdown钩子函数
        onShutdown(); 
    } finally {
        mainLock.unlock();
    }
    // 尝试终止线程池
    tryTerminate();
}
```



####8.2tryTerminate()函数

- **作用**
  1. 当移除一个工作线程时，会尝试终止线程池。
  2. 当执行shutdown()，会尝试终止线程池。

```java
final void tryTerminate() {
    // 无限循环，确保操作成功
    for (;;) { 
        // 获取线程池控制状态
        int c = ctl.get();
        
         
        if (
            // 线程池的运行状态为RUNNING,
            isRunning(c) ||
            // 线程池的运行状态为,TIDYING或TERMINATED,则不需要设置了
            runStateAtLeast(c, TIDYING) || 
            // 线程池的运行状态为SHUTDOWN并且workQueue队列不为null
            (runStateOf(c) == SHUTDOWN && ! workQueue.isEmpty()))  
            return;
        
        // 线程池正在运行的worker数量不为0
        if (workerCountOf(c) != 0) {  
            // 仅仅中断一个空闲的worker
            interruptIdleWorkers(ONLY_ONE);
            return;
        }
        
        // 获取线程池的锁
        final ReentrantLock mainLock = this.mainLock;
        // 获取锁
        mainLock.lock();
        try {
            // 比较并设置线程池控制状态为TIDYING
            if (ctl.compareAndSet(c, ctlOf(TIDYING, 0))) { 
                try {
                    // 终止，钩子函数
                    terminated();
                } finally {
                    // 设置线程池控制状态为TERMINATED
                    ctl.set(ctlOf(TERMINATED, 0));
                    // 释放在termination条件上等待的所有线程
                    termination.signalAll();
                }
                return;
            }
        } finally {
            // 释放锁
            mainLock.unlock();
        }
    }
}
```



#### 8.3 interruptIdleWorkers()函数

- **作用**
  1.  将会中断正在等待任务的空闲`worker`。 

```java
private void interruptIdleWorkers(boolean onlyOne) {
    // 线程池的锁
    final ReentrantLock mainLock = this.mainLock;
    // 获取锁
    mainLock.lock();
    try {
        
        // 遍历workers队列
        for (Worker w : workers) { 
            // worker对应的线程
            Thread t = w.thread;
            
            // 线程未被中断并且成功获得锁
            if (!t.isInterrupted() && w.tryLock()) { 
                try {
                    // 中断线程
                    t.interrupt();
                } catch (SecurityException ignore) {
                } finally {
                    // 释放锁
                    w.unlock();
                }
            }
            // 若只中断一个，则跳出循环
            if (onlyOne) 
                break;
        }
        
    } finally {
        // 释放锁
        mainLock.unlock();
    }
}
```





