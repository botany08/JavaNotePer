/**   		BeanDefinitionReader     容器用来读取Bean的接口
*		     		↓ (implements)
*		AbstractBeanDefinitionReader  抽象类
*					↓ (extends)
*		  XmlBeanDefinitionReader    用来读取XML文档中Bean的实现类
*/

/**
* 用来读取Bean
* 类AbstractBeanDefinitionReader 有四个重载方法 loadBeanDefinitions
* public int loadBeanDefinitions(Resource... resources)
* public int loadBeanDefinitions(String location)
* public int loadBeanDefinitions(String location, Set<Resource> actualResources)
* public int loadBeanDefinitions(String... locations)
* 
* Position : AbstractBeanDefinitionReader implements  EnvironmentCapable,BeanDefinitionReader
*/
@Override
public int loadBeanDefinitions(Resource... resources) throws BeanDefinitionStoreException {
	//断言
   Assert.notNull(resources, "Resource array must not be null");
   int counter = 0;
   // 注意这里是个 for 循环，也就是每个文件是一个 resource
   for (Resource resource : resources) {
      // 继续往下看
      counter += loadBeanDefinitions(resource);
   }
   // counter，表示总共加载BeanDefinition的数量
   return counter;
}


/**
* 用来读取Bean
* 类AbstractBeanDefinitionReader 有四个重载方法 loadBeanDefinitions
* public int loadBeanDefinitions(Resource resource)   ---跳转到第二个方法EncodeResource
* public int loadBeanDefinitions(EncodedResource encodedResource)
* public int loadBeanDefinitions(InputSource inputSource)  -- 跳转到第四个方法InputSource，String
* public int loadBeanDefinitions(InputSource inputSource, String resourceDescription)
* 
* Position : XmlBeanDefinitionReader extends AbstractBeanDefinitionReader
*/
public int loadBeanDefinitions(EncodedResource encodedResource) throws BeanDefinitionStoreException {
	//判空，断言
   Assert.notNull(encodedResource, "EncodedResource must not be null");

   //日志输出
   if (logger.isInfoEnabled()) {
      logger.info("Loading XML bean definitions from " + encodedResource.getResource());
   }

   /** 
   * private final ThreadLocal<Set<EncodedResource>> resourcesCurrentlyBeingLoaded
   * ThreadLocal是一个关于创建线程局部变量的类。 -- 同步
   * 通常情况下，我们创建的变量是可以被任何一个线程访问并修改的。
   * 使用ThreadLocal创建的变量只能被当前线程访问，其他线程则无法访问和修改。
   */
   Set<EncodedResource> currentResources = this.resourcesCurrentlyBeingLoaded.get();
   //判空，则重新创建并放进ThreadLocal类中
   if (currentResources == null) {
      currentResources = new HashSet<EncodedResource>(4);
      this.resourcesCurrentlyBeingLoaded.set(currentResources);
   }

   //资源添加失败时
   if (!currentResources.add(encodedResource)) {
      throw new BeanDefinitionStoreException(
            "Detected cyclic loading of " + encodedResource + " - check your import definitions!");
   }

   //重要的操作代码块
   try {
   	  //创建字节读取流
      InputStream inputStream = encodedResource.getResource().getInputStream();
      try {
      	 //import org.xml.sax.InputSource;
      	 //InputSource为读取XML的处理流，主要作用是将XML文档转换成DOM对象
         InputSource inputSource = new InputSource(inputStream);
         //设置解码方式
         if (encodedResource.getEncoding() != null) {
            inputSource.setEncoding(encodedResource.getEncoding());
         }
         //输入处理流以及资源，核心部分，IMPORT
         return doLoadBeanDefinitions(inputSource, encodedResource.getResource());
      }
      finally {
         inputStream.close();
      }
   }
   catch (IOException ex) {
      throw new BeanDefinitionStoreException(
            "IOException parsing XML document from " + encodedResource.getResource(), ex);
   }
   finally {
      currentResources.remove(encodedResource);
      if (currentResources.isEmpty()) {
         this.resourcesCurrentlyBeingLoaded.remove();
      }
   }
}



/**
* 将XML文档转换成Document对象，并调用处理方法
* 只有一个该方法
* 
* Position : XmlBeanDefinitionReader extends AbstractBeanDefinitionReader
*/
protected int doLoadBeanDefinitions(InputSource inputSource, Resource resource) throws BeanDefinitionStoreException {
	try {
		//读取doc对象
		Document doc = doLoadDocument(inputSource, resource);
		//读取xml数据，返回读取的bean
		return registerBeanDefinitions(doc, resource);
	}
	catch (BeanDefinitionStoreException ex) {
		throw ex;
	}
	//...多个catch
}

/**
* 利用BeanDefinitionDocumentReader读取DOC对象里的Bean数据
* 返回此次读取的Bean个数
* 
* Position : XmlBeanDefinitionReader extends AbstractBeanDefinitionReader
*/
public int registerBeanDefinitions(Document doc, Resource resource) throws BeanDefinitionStoreException {
   //初始化BeanDefinitionDocumentReader
   BeanDefinitionDocumentReader documentReader = createBeanDefinitionDocumentReader();
   //countBefore表示已加载的bean个数
   int countBefore = getRegistry().getBeanDefinitionCount();
   //调用documentReader读取doc对象
   documentReader.registerBeanDefinitions(doc, createReaderContext(resource));
   return getRegistry().getBeanDefinitionCount() - countBefore;
}