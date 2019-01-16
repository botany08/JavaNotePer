/**
* Function : 创建Bean容器，加载Bean，定义注册Bean
* ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();
* 类AbstractApplicationContext
*/
protected ConfigurableListableBeanFactory obtainFreshBeanFactory() {
	//关闭旧的 BeanFactory (如果有)，创建新的 BeanFactory，加载 Bean, 定义、注册 Bean 等等
	refreshBeanFactory();

	//返回刚刚创建的 BeanFactory
	ConfigurableListableBeanFactory beanFactory = getBeanFactory();

	//打印日志，略过
	if (logger.isDebugEnabled()) {
		logger.debug("Bean factory for " + getDisplayName() + ": " + beanFactory);
	}
	return beanFactory;
}



/**
* Function : 1.关闭旧Bean容器，2.创建Bean容器，3.加载Bean，4.定义注册Bean
* ConfigurableListableBeanFactory beanFactory = obtainFreshBeanFactory();
* 类AbstractRefreshableApplicationContext
*/
protected final void refreshBeanFactory() throws BeansException {
	// 如果 ApplicationContext 中已经加载过 BeanFactory 了，销毁所有 Bean，关闭 BeanFactory
    // 注意，应用中 BeanFactory 本来就是可以多个的，这里可不是说应用全局是否有 BeanFactory，而是当前
    // ApplicationContext 是否有 BeanFactory
	if (hasBeanFactory()) {
		//销毁所有Bean,存储bean实例的为set和map，直接调用方法clear()
		destroyBeans();		
		//关闭BeanFactory,将ApplicationContext中的BeanFactory置为空
		closeBeanFactory(); 
	}
	try {
		// 初始化一个 DefaultListableBeanFactory.
		DefaultListableBeanFactory beanFactory = createBeanFactory();

		// 用于 BeanFactory 的序列化，大部分人用不到
		beanFactory.setSerializationId(getId());

		// 设置 BeanFactory 的两个配置属性：是否允许 Bean 覆盖、是否允许循环引用，IMPORT
		customizeBeanFactory(beanFactory);

		// 加载 Bean 到 BeanFactory 中,重要 IMPORT
		loadBeanDefinitions(beanFactory);

		//beanFactoryMonitor 为一个 new Object()
		synchronized (this.beanFactoryMonitor) {
			//实例化ApplicationContext对象中的beanFactory属性
			this.beanFactory = beanFactory;
		}
	}
	catch (IOException ex) {
		//抛出异常
		throw new ApplicationContextException("I/O error parsing bean definition source for " + getDisplayName(), ex);
	}
}

/**
* Function : 实例化BeanFactory,初始化一个DefaultListableBeanFactory
* Position : 类AbstractRefreshableApplicationContext
*/
protected DefaultListableBeanFactory createBeanFactory() {
		return new DefaultListableBeanFactory(getInternalParentBeanFactory());
	}


/**
* Function : 设置 BeanFactory 的两个配置属性：是否允许 Bean 覆盖、是否允许循环引用
* Position : 类AbstractRefreshableApplicationContext
*/
protected void customizeBeanFactory(DefaultListableBeanFactory beanFactory) {
	//是否允许 Bean 定义覆盖
	if (this.allowBeanDefinitionOverriding != null) {
		beanFactory.setAllowBeanDefinitionOverriding(this.allowBeanDefinitionOverriding);
	}
	//是否允许 Bean 间的循环依赖
	if (this.allowCircularReferences != null) {
		beanFactory.setAllowCircularReferences(this.allowCircularReferences);
	}
}

/**
* 根据配置加载Bean，并放到BeanFactory中，参数为BeanFactory，重载
* 通过一个XmlBeanDefinitionReader实例来加载各个Bean。
* 
* Position : AbstractXmlApplicationContext extends  AbstractRefreshableApplicationContext
*/
@Override
protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws BeansException, IOException {
   // 给这个 BeanFactory 实例化一个 XmlBeanDefinitionReader
   XmlBeanDefinitionReader beanDefinitionReader = new XmlBeanDefinitionReader(beanFactory);

   //使用beanDefinitionReader加载配置环境
   beanDefinitionReader.setEnvironment(this.getEnvironment());
   beanDefinitionReader.setResourceLoader(this);
   beanDefinitionReader.setEntityResolver(new ResourceEntityResolver(this));

   // 初始化 BeanDefinitionReader，没有实现，是提供给子类覆写的，
   initBeanDefinitionReader(beanDefinitionReader);

   // 使用初始化的reader加载XML配置，参数为XmlBeanDefinitionReader，重载
   loadBeanDefinitions(beanDefinitionReader);
}