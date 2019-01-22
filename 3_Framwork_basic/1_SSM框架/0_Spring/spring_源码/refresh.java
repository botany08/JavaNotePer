//位于类AbstractApplicationContext
public void refresh() throws BeansException, IllegalStateException {
		//加锁，同步的代码块
		//private final Object startupShutdownMonitor = new Object();
		synchronized (this.startupShutdownMonitor) {
			// 准备工作，记录下容器的启动时间、标记“已启动”状态、处理配置文件中的占位符
			prepareRefresh();
			
			// 虽然ApplicationContext也实现了BeanFactory接口，但是在启动过程中另外注册了一个新的BeanFactory实例。
			// 解析配置文件，提取配置信息并且注册到BeanFactory中
			// 这步比较关键，这步完成后，配置文件就会解析成一个个 Bean 定义，注册到 BeanFactory 中，
			// 当然，这里说的 Bean 还没有初始化，只是配置信息都提取出来了，
			// 注册也只是将这些信息都保存到了注册中心(说到底核心是一个 beanName-> beanDefinition 的 map)
			ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();

			// 设置 BeanFactory 的类加载器，添加几个 BeanPostProcessor，手动注册几个特殊的 bean
			prepareBeanFactory(beanFactory);

			try {
				// 【这里需要知道 BeanFactoryPostProcessor 这个知识点，Bean 如果实现了此接口，
				// 那么在容器初始化以后，Spring 会负责调用里面的 postProcessBeanFactory 方法。】

				// 这里是提供给子类的扩展点，到这里的时候，所有的 Bean 都加载、注册完成了，但是都还没有初始化
				// 具体的子类可以在这步的时候添加一些特殊的 BeanFactoryPostProcessor 的实现类或做点什么事
				postProcessBeanFactory(beanFactory);

				// 调用 BeanFactoryPostProcessor 各个实现类的 postProcessBeanFactory(factory) 方法
				invokeBeanFactoryPostProcessors(beanFactory);

				// Bean还没初始化！！！
				// 注册 BeanPostProcessor 的实现类，注意看和 BeanFactoryPostProcessor 的区别
				// 此接口两个方法: postProcessBeforeInitialization 和 postProcessAfterInitialization
				// 两个方法分别在 Bean 初始化之前和初始化之后得到执行。
				registerBeanPostProcessors(beanFactory);
				
				// 初始化当前 ApplicationContext 的 MessageSource，国际化这里就不展开说了，不然没完没了了
				initMessageSource();

				// 初始化当前 ApplicationContext 的事件广播器，
				initApplicationEventMulticaster();

				// 从方法名就可以知道，典型的模板方法(钩子方法)，
				// 具体的子类可以在这里初始化一些特殊的 Bean（在初始化 singleton beans 之前）
				onRefresh();

				// 注册事件监听器，监听器需要实现 ApplicationListener 接口。
				registerListeners();

				// 初始化所有的 singleton beans
				finishBeanFactoryInitialization(beanFactory);

				// 最后，广播事件，ApplicationContext 初始化完成
				finishRefresh();
			}

			catch (BeansException ex) {
				if (logger.isWarnEnabled()) {
					logger.warn("Exception encountered during context initialization - " +
							"cancelling refresh attempt: " + ex);
				}

				// Destroy already created singletons to avoid dangling resources.
				destroyBeans();

				// Reset 'active' flag.
				cancelRefresh(ex);

				// Propagate exception to caller.
				throw ex;
			}

			finally {
				// Reset common introspection caches in Spring's core, since we
				// might not ever need metadata for singleton beans anymore...
				resetCommonCaches();
			}
		}
	}