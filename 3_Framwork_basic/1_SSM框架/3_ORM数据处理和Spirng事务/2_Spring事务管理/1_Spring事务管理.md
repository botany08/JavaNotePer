## Spring事务管理

### 1.事务分类

- 局部和全局事务
  1. 局部事务是特定于单一的事务资源，如一个JDBC连接。而全局事务可以跨多个事务资源事务，如在一个分布式系统中的事务。
  2. 局部事务管理在一个集中的计算环境中是有用的，该计算环境中应用程序组件和资源位于一个单位点。而事务管理只涉及到一个运行在单一机器中的本地数据管理器。局部事务更容易实现。
  3. 全局事务管理需要在分布式计算环境中，所有的资源都分布在多个系统中。在这种情况下事务管理需要同时在局部和全局范围内进行。分布式或全局事务跨多个系统执行，需要全局事务管理系统和所有相关系统的局部数据管理人员之间的协调。

### 2.Spring事务底层实现原理

- Sping事务底层和数据库的交互，都是使用javax.sql.*的API，只是对其进行封装。
  1. `PlatformTransactionManager`事务管理器，调用方法`getTransaction()`，获取一个`TransactionStatus`事务状态对象。
  2. 通过`DataSourceUtils.getConnection(DataSource)`方法，获取`Connection`对象并绑定到当前的事务状态对象。
  3. 通过`Connection`对象，执行业务的访问逻辑。
  4. `PlatformTransactionManager`事务管理器，调用`commit(TransactionStatus)`方法，提交事务。
  5. 调用`DataSourceUtils.releaseConnection(connection, dataSource)`，释放连接。
  

####2.1编程式事务原理

- 编程式事务，即显式调用事务相关接口，进行事务的开启和提交。
  
  ```java
  /**
  * 编程式事务
  * 利用JdbcTemplate封装 第2步、第3步、第5步
  * 利用TransactionTemplate封装 第1步、第4步
  **/
  public void testPlatformTransactionManager() {
      // 1.通过事务管理器，获取一个事务状态对象
      DefaultTransactionDefinition def = new DefaultTransactionDefinition();
      def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);  
      def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED); 
      TransactionStatus status = txManager.getTransaction(def); 
      
      // 2.从事务同步管理器中,获取本线程绑定的Connection对象
      Connection connection = DataSourceUtils.getConnection(dataSource);
      
      // 3.执行业务访问逻辑
      try{
          connection.prepareStatement(CREATE_TABLE_SQL).execute();  
          PreparedStatement pstmt = connection.prepareStatement(INSERT_SQL);  
          pstmt.setString(1, "test");  
          pstmt.execute();  
          connection.prepareStatement(DROP_TABLE_SQL).execute(); 
          
          // 4. 事务管理器提交事务
          txManager.commit(status);
      }catch(Exception ex){
          status.setRollbackOnly();  
          txManager.rollback(status);
      }finally{
          // 5.释放连接
          DataSourceUtils.releaseConnection(connection, dataSource);
      }
  }
  
  /**
  * 编程式事务-引用JdbcTemplate、TransactionTemplate
  */
  public void testTransactionTemplate(){
      TransactionTemplate transactionTemplate = new TransactionTemplate(txManager);
      // 利用事务做写操作
      transactionTemplate.execute(new TransactionCallbackWithoutResult() {
          @Override
          protected void doInTransactionWithoutResult(TransactionStatus arg0) {
              jdbcTemplate.execute(CREATE_TABLE_SQL);
              jdbcTemplate.update(INSERT_SQL, "test");
          }
      });
      
      // 事务提交后,再执行查询语句
      String COUNT_ALL = "select count(*) from test";  
      Number count = jdbcTemplate.queryForInt(COUNT_ALL);
      Assert.assertEquals(1, count.intValue());
  
      jdbcTemplate.execute(DROP_TABLE_SQL);
  }
  ```
  

#### 2.2声明式事务原理

- 声明式事务本质上，是拦截器用SpringAOP切入编程式事务的代码，做动态代理。

```java
public class PrototypeTransactionlnterceptor implements Methodinterceptor {

    // 初始化事务管理器
	private PlatformTransactionManager transactionManager;

	// 动态代理
	public Object invoke(Methodinvocation invocation) throws Throwable {
		Method method = invocation.getMethod();
		// 1.获取事务状态对象
		TransactionDefinition definition = getTransactionDefinitionByMethod(method); 
		Transactionstatus txStatus = transactionManager.getTransaction(definition); 
		Object result = null;
		try {
			// 2.业务数据访问逻辑
			result = invocation.proceed();
		} catch(Throwable t) {
			if(needRollbackOn(t)) {
				transactionManager.rollback(txStatus);
			} else {
				transactionManager.commit(txStatus);
			}
			throw t;
		}

         // 3.事务提交
		transactionManager.commit(txStatus);
		return result;
	}

	protected boolean needRollbackOn(Throwable t) ( 
		// TODO・・・更多实现细节
		return false;
	}

	protected TransactionDefinition getTransactionDefinitionByMethod(Method method) { 
		// TODO・・・更多实现细节
		return null;
	}

	public PlatformTransactionManager getTransactionManager() {
		return transactionManager;
	}

	public void setTransactionManager(PlatformTransactionManager transactionManager) (
		this.transactionManager = transactionManager;
	}
}
```



### 3.Spring的事务抽象接口

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/1_Spring事务接口关系图.png)

- 抽象接口的定义

  1. `org.springframework.transaction.PlatformTransactionManager`

     负责界定事务边界。

  2. `org.springframework.transaction.TransactionDefinition`

     负责定义事务相关属性，包括隔离级别、传播行为、事务超时时间和只读事务。主要用于声明式事务。

  3. `org.springframework.transaction.TransactionStatus`

     负责事务开启之后到事务结束期间的事务状态，以及有限的事务控制。主要用于编程式事务。

#### 3.1 TransactionDefinition

##### 3.1.1事务的隔离（Isolation）级别

- 5个常量用于标志可供选择的隔离级别

| 常量名(是表示可避免，否表示无法避免) | 隔离级别          | 脏读 | 不可重复读 | 幻读 |
| ------------------------------------ | ----------------- | ---- | ---------- | ---- |
| `ISOLATION_DEFAULT`                  | `ReadCommitted`   | 是   | 否         | 否   |
| `ISOLATION_READ_UNCOMMITTED`         | `ReadUncommitted` | 否   | 否         | 否   |
| `ISOLATION_READ_COMMITTED`           | `ReadCommitted`   | 是   | 否         | 否   |
| `ISOLATION_REPEATABLE_READ`          | `RepeatableRead`  | 是   | 是         | 否   |
| `ISOLATION_SERIALIZABLE`             | `Serializable`    | 是   | 是         | 是   |

##### 3.1.2事务的传播行为（Propagation Behavior）

- 基本概念

  事务的传播行为表示整个事务处理过程所跨越的业务对象，将以什么样的行为参与事务，声明式事务将会用到。

- 以下常量表示**传播行为**

  1. `PROPAGATION_REQUIRED`

     如果当前存在一个事务，则加入当前事务。如果不存在任何事务，则创建一个新的事务。总之，要至少保证在一个事务中运行。PROPAGATION_REQUIRED通常作为默认的事务传播行为。

  2. `PROPAGATION_SUPPORTS`

     如果当前存在一个事务，则加入当前事务。如果当前不存在事务，则直接执行。

     对于查询方法来说，比较适合该传播行为。如果存在事务，可以读取到该事务的更新信息。

  3. `PROPAGATION_MANDATORY`

     强制要求当前存在一个事务，如果不存在，则抛出异常。如果某个方法需要事务支持，但自身又不管理事务提交或者回滚，那么比较适合使用。

  4. `PROPAGATION_REQUIRES_NEW`

     不管当前是否存在事务，都会创建新的事务。如果当前存在事务，会将当前的事务挂起(Suspend)。

     如果某个业务对象所做的事情不想影响到外层事务，可以选择。假设当前的业务方法需要向数据库中更新某些日志信息，但即使这些日志信息更新失败，我们也不想因为该业务方法的事务回滚, 而影响到外层事务的成功提交。

  5. `PROPAGATION_NOT_SUPPORTE`

     不支持当前事务，而是在没有事务的情况下执行。如果当前存在事务的话，当前事务原则上将被挂起(`Suspend`),但这要看对应的`PlatfonnTransactionManager`实现类是否支持事务的挂起。

  6. `PR0PAGATI0N_NEVER`

     永远不需要当前存在事务，如果存在当前事务，则抛出异常。

  7. `PR0PAGATI0N_NESTED`

     如果存在当前事务，则在当前事务的一个嵌套事务中执行。否则，即创建新的事务，在新创建的事务中执行。

##### 3.1.3事务的超时时间（Timeout）

- 常量TIMEOUT_DEFAULT

  用来指定事务的超时时间。`TIMEOUT_DEFAULT`默认值为-1,这会采用当前事务系统默认的超时时间。可以通过

  `TransactionDefinition`的具体实现类提供自定义的事务超时时间。

##### 3.1.4是否为只读（Readonly）事务

- 如果需要创建一个只读的事务的话，可以通过`TransactionDefinition`的相关实现类进行设置。

#### 3.2 TransactionStatus

- 接口定义

  表示整个事务处理过程中的事务状态。

- 主要用途

  1. 使用`Transactionstatus`提供的相应方法，查询事务状态。
  2. 通过`setRollbackOnly ()`方法标记当前事务以使其回滚。
  3. 如果相应的`PlatformTransactionManager`支持`Savepoint` , 可以通过`TransactionStatus`在当前事务中创建内部嵌套事务。

#### 3.3 PlatformTransactionManager

##### 3.3.1局部事务实现类概览

- `PlatformTransactionManager`的整个抽象体系基于策略模式，由接口对事务界定进行统一抽象，而具体的界定策略的实现则交由具体的实现类。

| 数据访问技术                 | 实现类                         |
| ---------------------------- | ------------------------------ |
| `JDBC/iBATIS`                | `DataSourceTransactionManager` |
| `Hibernate`                  | `HibernateTransactionManager`  |
| `JDO`                        | `JdoTransactionManager`        |
| `JPA (Java Persistence API)` | `JpaTransactionManager`        |
| `TopLink`                    | `TopLinkTransaetionManager`    |
| `JMS`                        | `JmsTransactionManager`        |
| `JCA Local Transaction`      | `CciLocalTransactionManager`   |



#####3.3.2接口运行原理

- 抽象类`AbstractPlatformTransactionManager`，通过模板方法模式，定义了子类的内部处理逻辑。
  1. 判定是否存在当前事务，然后根据判断结果执行不同的处理逻辑。
  2. 结合是否存在当前事务的情况，根据`TransactionDefinition`中指定的传播行为的不同语义执行后继逻辑，根据情况挂起或者恢复事务。
  3. 提交事务之前检查`readonly`字段是否被设置，如果是的话，以事务的回滚代替事务的提交。
  4. 在事务回滚的情况下，清理并恢复事务状态。
  5. 如果事务的`Synchonization`处于`active`状态，在事务处理的规定时点触发注册的Synchonization回调接口。



###4.声明式事务的使用

- 主要为注解@Transactional，待整理



### 5.Spring的事务同步机制

#### 5.1问题1

- **描述**
  1. 在多线程环境下(多个请求访问)，`SpringMVC`中的`Controller`、`Service`等都是无状态的单例`Bean`，即没有状态变量，可以保证线程安全。
  2. 对于连接池`Connection`来说，假设有10个连接对象可供分配。就相当于是多线程共享这10个连接对象，容易出现线程安全问题。
  3. 关键的问题在于，保证每个线程拿到的`Connection`对象是独立的，达到线程安全的要求。
- **解决方法**
1. 事务同步管理类`org.springframework.transaction.support.TransactionSynchronizationManager`
  2. 此类内部使用了很多的**`ThreadLocal`变量，为不同的事务线程提供了独立的资源副本**。同时维护这些事务的配置属性和运行状态信息(比如强大的事务嵌套、传播属性和这个强相关)。
  3. 其中`DataSourceUtils`工具类，可以给用户提供线程本地变量，数据库连接池对象。
- **保证同一个事务获取到同一个Connection对象**
  1. `JdbcTemplate`执行`SQL`的方法主要分为`update`和`query`方法，底层最终都是依赖于`execute`方法去执行。
  2. 在`execute`方法中，通过`DataSourceUtils.getConnection(obtainDataSource())`获取到事务相关的连接对象。
  3. `TransactionSynchronizationManager`内部用`ThreadLocal<Map<Object, Object>>`对象存储资源，`key`为`DataSource`、`value`为`connection`对应的`ConnectionHolder`对象。

#### 5.2问题2

- **描述**
  1. `Spring`如何保证在数据库事务提交成功后，进行异步操作不出错。
  2. 如进行一个插入语句的事务，同时建立线程进行查询语句，由于不能保证事务已经提交，所以查询语句不一定可以查出最新的结果。

- **解决方法**
1. `Spring`提供了一个事务同步器类，`TransactionSynchronization`，用于事务同步回调的接口。
  2. 将同步器注册到事务同步管理类，可以实现在本事务提交后的回调。

#### 5.3DataSourceUtils工具类

- **作用**

  1. 某些场景下，可能无法使用`Spring`提供的模板类来达到效果，而是需要直接操作原生`API Connection`。此工具栏用来获取原生的`JDBC`。
  2. 必须要保证和当前MaBatis线程使用的是同一个连接，才能进入到事务管理中。

- **具体源码**

  ```java
  public abstract class DataSourceUtils {
  	// 从TransactionSynchronizationManager里拿到一个现成的Connection
  	public static Connection getConnection(DataSource dataSource) 
          throws CannotGetJdbcConnectionException { ... }
  
  	// 把definition和connection进行一些准备工作~
  	public static Integer prepareConnectionForTransaction(
          Connection con, @Nullable TransactionDefinition definition) throws SQLException { ...}
  
  	// 判断该JDBC Connection是否为当前事务内的连接
  	public static boolean isConnectionTransactional(
          Connection con, @Nullable DataSource dataSource) { ... }
  
  	// Statement 给他设置超时时间,不传timeout表示不超时
  	public static void applyTransactionTimeout(
          Statement stmt, @Nullable DataSource dataSource) throws SQLException { ... }
  	public static void applyTimeout(
          Statement stmt, @Nullable DataSource dataSource, int timeout) throws SQLException { ... }
  
  	// 此处可能是归还给连接池,也有可能是close,和连接池参数有关
  	public static void releaseConnection(
          @Nullable Connection con, @Nullable DataSource dataSource) { ... }
  	public static void doReleaseConnection(
          @Nullable Connection con, @Nullable DataSource dataSource) throws SQLException { ... }
  
  	// 这个是真close
  	public static void doCloseConnection(
          Connection con, @Nullable DataSource dataSource) throws SQLException { ... }
  	
  	// 如果链接是代理，会拿到最底层的connection
  	public static Connection getTargetConnection(Connection con) { ... }
  }
  ```

#### 5.4TransactionSynchronizationManager事务同步管理器

1. 开启新的事务时初始化。第一次开启事务分为两种情况：real首次 或 已存在事务但是REQUIRES_NEW。
2. 在事务的嵌套过程中，`TransactionSynchronizationManager`属性不断更新最终清除。外层事务挂起或者事务提交，这两个点需要更新`TransactionSynchronizationManager`属性。
3. 内部类`AbstractPlatformTransactionManager.SuspendedResourcesHolder`是负责事务挂起时候，保存事物属性的对象，用于恢复外层事务。
4. 当恢复外层事务时，根据`SuspendedResourcesHolder`对象，调用底层事务框架恢复事务属性，并恢复`TransactionSynchronizationManager`。

```java
/**
* TransactionSynchronizationManager是抽象类,没有子类,所有方法和参数都是静态
**/
public abstract class TransactionSynchronizationManager {

    // 声明了很多ThreadLocal变量,是核心存储的数据

	private static final ThreadLocal<Map<Object, Object>> resources = 
        new NamedThreadLocal<>("Transactional resources");

	// 同步器类TransactionSynchronization,用于应用扩展,每个线程都可以注册N多个同步器.
	private static final ThreadLocal<Set<TransactionSynchronization>> synchronizations = 
        new NamedThreadLocal<>("Transaction synchronizations");
	
	// 事务的名称  
	private static final ThreadLocal<String> currentTransactionName = 
        new NamedThreadLocal<>("Current transaction name");
    
	// 事务是否是只读  
	private static final ThreadLocal<Boolean> currentTransactionReadOnly = 
        new NamedThreadLocal<>("Current transaction read-only status");
    
	// 事务的隔离级别
	private static final ThreadLocal<Integer> currentTransactionIsolationLevel = 
        new NamedThreadLocal<>("Current transaction isolation level");
    
	// 事务是否开启
	private static final ThreadLocal<Boolean> actualTransactionActive = 
        new NamedThreadLocal<>("Actual transaction active");

	// 返回的是个只读视图
	public static Map<Object, Object> getResourceMap() {
		Map<Object, Object> map = resources.get();
		return (map != null ? Collections.unmodifiableMap(map) : Collections.emptyMap());
	}

	public static boolean hasResource(Object key) { ... }
	public static Object getResource(Object key) { ... }
	
	
	@Nullable
	private static Object doGetResource(Object actualKey) {
		Map<Object, Object> map = resources.get();
		if (map == null) {
			return null;
		}
		Object value = map.get(actualKey);
		// Transparently remove ResourceHolder that was marked as void...
		// 如果ResourceHolder 被标记为了void空白了。此处直接从map里移除掉对应的key 
		// ~~~~~~~并且返回null~~~~~~~~~~~
		if (value instanceof ResourceHolder && ((ResourceHolder) value).isVoid()) {
			map.remove(actualKey);
			// Remove entire ThreadLocal if empty...
			if (map.isEmpty()) {
				resources.remove();
			}
			value = null;
		}
		return value;
	}

	// 作用：和当前线程绑定一个Map,并且处理ResourceHolder,如果isVoid就抛错
	public static void bindResource(Object key, Object value) throws IllegalStateException {
		Object actualKey = TransactionSynchronizationUtils.unwrapResourceIfNecessary(key);
		Assert.notNull(value, "Value must not be null");
		Map<Object, Object> map = resources.get();
        
		if (map == null) {
			map = new HashMap<>();
			resources.set(map);
		}
		Object oldValue = map.put(actualKey, value);
		if (oldValue instanceof ResourceHolder && ((ResourceHolder) oldValue).isVoid()) {
			oldValue = null;
		}
		if (oldValue != null) {
			throw new IllegalStateException("Already value [" + oldValue + "] for key [" +
					actualKey + "] bound to thread [" + Thread.currentThread().getName() + "]");
		}
	}

	public static Object unbindResource(Object key) throws IllegalStateException { ... }
	public static Object unbindResourceIfPossible(Object key) { ... }
	

	// 作用：判断同步器是否是激活状态,若是激活状态就可以执行同步器里的相关回调方法.
	public static boolean isSynchronizationActive() {
		return (synchronizations.get() != null);
	}

	// 作用：初始化同步器.如果事务已经开启了,就不能再初始化同步器了,而是直接注册
	public static void initSynchronization() throws IllegalStateException {
		if (isSynchronizationActive()) {
			throw new IllegalStateException(
                "Cannot activate transaction synchronization - already active");
		}
		logger.trace("Initializing transaction synchronization");
		synchronizations.set(new LinkedHashSet<>());
	}

	// 作用：注册同步器TransactionSynchronization,注册的时候要求当前线程的事务已经是激活状态的
	public static void registerSynchronization(TransactionSynchronization synchronization) 
        throws IllegalStateException {
		Assert.notNull(synchronization, "TransactionSynchronization must not be null");
		if (!isSynchronizationActive()) {
			throw new IllegalStateException("Transaction synchronization is not active");
		}
		synchronizations.get().add(synchronization);
	}
	
	// 作用：清除所有和当前线程相关的信息
	public static void clear() {
		synchronizations.remove();
		currentTransactionName.remove();
		currentTransactionReadOnly.remove();
		currentTransactionIsolationLevel.remove();
		actualTransactionActive.remove();
	}
}
```



#### 5.5TransactionSynchronization事务同步器

- **具体源码**

  ```java
  /**
  * TransactionSynchronization事务同步器
  * 1. 用于事务同步回调的接口,自定义的同步器可以实现Ordered接口来定制化顺序，若没实现接口就按照添加的顺序执行.
  **/
  public interface TransactionSynchronization extends Flushable {
  
  	int STATUS_COMMITTED = 0;
  	int STATUS_ROLLED_BACK = 1;
  	int STATUS_UNKNOWN = 2;
  
      /**
      * 事务暂停suspend的时候调用此方法,一般是用来释放掉绑定的resources.
      * TransactionSynchronizationManager#unbindResource
      **/
  	default void suspend() {}
      
      /**
      * 事务恢复时候调用.TransactionSynchronizationManager#bindResource
      **/
  	default void resume() {}
  
  	// 将基础会话刷新到数据存储区(如果适用),比如Hibernate/Jpa的session
  	@Override
  	default void flush() {}
  
      /**
      * 在事务提交之前触发.在AbstractPlatformTransactionManager.processCommit方法里,commit之前触发
      * 事务提交之前，比如flushing SQL statements to the database,发生了异常会回滚.
      **/
  	default void beforeCommit(boolean readOnly) {}
      
      /**
      * 1. 在beforeCommit之后,在commit/rollback之前执行.
      * 2. 和beforeCommit不同,即使beforeCommit抛出异常了,beforeCompletion也会执行
      **/
  	default void beforeCompletion() {}
  
      /**
      * 1. 在事务提交(成功提交并持久化到数据库,并没有抛出异常)后执行,可以用来异步发短信或email
      * 2. 事务提交后,事务资源(链接connection)可能仍然是活动的和可访问的,并恢复了自动提交.
      * 3. 在此方法中触发的任何数据访问代码,仍将参与原始事务,允许执行一些清理,但是不再执行事务提交操作.
      * 4. 此时执行SQL仍然是可以执行的,因为此时恢复了自动提交机制.
      **/
  	default void afterCommit() {}
  
  	// 和afterCommit不同,即使抛出异常回滚了,afterCompletion也会执行的.
  	default void afterCompletion(int status) {}
  }
  ```



### 6.Spring的事务监听机制

- 用于监听事务的状态，然后做出其他操作

 https://cloud.tencent.com/developer/article/1497715 

