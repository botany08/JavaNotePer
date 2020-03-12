## SpringBoot异步调用

### 1.异步调用实例

#### 1.1使用条件

- `@EnableAsync`，通过在配置类或者Main类上加`@EnableAsync`开启对异步方法的支持。
- `@Async`，可以作用在类上或者方法上，作用在类上代表这个类的所有方法都是异步方法。

####1.2自定义执行器Executor

- 如果没有自定义`Executor`，`Spring`将创建一个 `SimpleAsyncTaskExecutor` 并使用。 
- `ThreadPoolTaskExecutor`饱和策略，指的是如果当前同时运行的线程数量达到最大线程数量时，执行的方案。
  1. `ThreadPoolExecutor.AbortPolicy`，抛出RejectedExecutionException来拒绝新任务的处理。
  2. `ThreadPoolExecutor.CallerRunsPolicy`，不会拒绝请求，通过增加队列容量来解决，但是这种策略会降低对于新任务提交速度，影响程序的整体性能。
  3. `ThreadPoolExecutor.DiscardPolicy`，不处理新任务，直接丢弃掉。
  4. `ThreadPoolExecutor.DiscardOldestPolicy`，此策略将丢弃最早的未处理的任务请求。

```java
package com.tcl.joker.productboot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 类 <code>{类名称}</code>{配置Spring调用的线程池}
 *
 * @author zangbao.lin
 * @version 2020/3/11
 * @since JDK 1.8
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    private static final int CORE_POOL_SIZE = 6;
    private static final int MAX_POOL_SIZE = 10;
    private static final int QUEUE_CAPACITY = 100;

    @Bean
    @Override
    public Executor getAsyncExecutor() {
        // Spring 默认配置是核心线程数大小为1，最大线程容量大小不受限制，队列容量也不受限制。
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数
        executor.setCorePoolSize(CORE_POOL_SIZE);
        // 最大线程数
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        // 队列大小
        executor.setQueueCapacity(QUEUE_CAPACITY);

        // 当最大池已满时，此策略保证不会丢失任务请求，但是可能会影响应用程序整体性能。
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setThreadNamePrefix("MyThreadPoolTaskExecutor-");
        executor.initialize();
        return executor;
    }
}

```



####1.3无返回值异步方法

```java
package com.tcl.joker.productboot.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 类 <code>{类名称}</code>{此类功能描述}
 *
 * @author zangbao.lin
 * @version 2020/3/11
 * @since JDK 1.8
 */
@Service
public class AsyncService {
    
    @Async
    public void taskA() {
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("执行任务A--用时2s");
    }
    
    @Async
    public void taskB() {
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("执行任务B--用时3s");
    }

    @Async
    public void taskC() {
        try {
            Thread.sleep(4000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("执行任务C--用时4s");
    }
}
```

#### 1.4异步回调-判断异步方法结束

```java
package com.tcl.joker.productboot.service;

import com.tcl.joker.productboot.dao.SkuQueryDao;
import com.tcl.joker.productboot.model.CarVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.Future;


/**
 * 类 <code>{类名称}</code>{此类功能描述}
 *
 * @author zangbao.lin
 * @version 2020/3/11
 * @since JDK 1.8
 */
@Service
public class AsyncService {

    @Autowired
    private SkuQueryDao skuQueryDao;

    @Async
    public void taskA() {
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("执行任务A--用时2s");
    }


    @Async
    public Future<List<CarVO>> taskB() {
        try {
            Thread.sleep(3000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("执行任务B--用时3s");

        List<CarVO> carVOList = skuQueryDao.querySkuById(1);
        return new AsyncResult<>(carVOList);
    }

    @Async
    public Future<List<CarVO>> taskC() {
        try {
            Thread.sleep(4000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("执行任务C--用时3s");

        List<CarVO> carVOList = skuQueryDao.querySkuById(2);
        return new AsyncResult<>(carVOList);

    }

    @Async
    public Future<List<CarVO>> taskD() {
        try {
            Thread.sleep(4000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("执行任务D--用时4s");

        List<CarVO> carVOList = skuQueryDao.querySkuById(3);
        return new AsyncResult<>(carVOList);

    }


}

```

