/**
* Function : 创建Bean容器前的准备工作
*			 1.记录启动时间 2.校验配置文件/读取配置文件 
* Position : 类AbstractApplicationContext
*/

protected void prepareRefresh() {
	// 记录启动时间
	this.startupDate = System.currentTimeMillis();

	/* AtomicBoolean是java.util.concurrent.atomic包下的原子变量，这个包里面提供了一组原子类。
	   其基本的特性就是在多线程环境下，当有多个线程同时执行这些类的实例包含的方法时，具有排他性，
	   即当某个线程进入方法，执行其中的指令时，不会被其他线程打断，而别的线程就像自旋锁一样，
	   一直等到该方法执行完成，才由JVM从等待队列中选择一个另一个线程进入，这只是一种逻辑上的理解。
	   实际上是借助硬件的相关指令来实现的，不会阻塞线程(或者说只是在硬件级别上阻塞了)。 */

	// 将 active 属性设置为 true，closed 属性设置为 false，它们都是 AtomicBoolean 类型
	// 由于原子变量同时只能让一个线程，所以当然两个线程同时处理原子变量时，可以保持同步
	// private final AtomicBoolean active = new AtomicBoolean();
	// private final AtomicBoolean closed = new AtomicBoolean();
	this.closed.set(false);
	this.active.set(true);
	
	// 打印日志，略过
	if (logger.isInfoEnabled()) {
		logger.info("Refreshing " + this);
	}

	// 该方法为空，略过
	initPropertySources();

	// 校验 xml 配置文件，TODO
	// 如何校验配置文件？
	// Enviroment是什么类？
	getEnvironment().validateRequiredProperties();

	// Allow for the collection of early ApplicationEvents,
	// to be published once the multicaster is available...
	this.earlyApplicationEvents = new LinkedHashSet<ApplicationEvent>();
}


