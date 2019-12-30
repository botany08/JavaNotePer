/**   		BeanDefinitionDocumentReader     文档读取器，读取Bean
*		     		↓ (implements)
*		DefaultBeanDefinitionDocumentReader  实现类
*/
/**
* 解析DOC对象 - 此时XML已经转换为doc对象
* 只有一个该方法
* @param XmlReaderContext XmlReaderContext extends ReaderContext(ROOT类)
* POSITION : DefaultBeanDefinitionDocumentReader implements BeanDefinitionDocumentReader(ROOT接口)
*/
@Override
public void registerBeanDefinitions(Document doc, XmlReaderContext readerContext) {
   this.readerContext = readerContext;
   logger.debug("Loading bean definitions");
   // 获取doc对象的根节点
   Element root = doc.getDocumentElement();
   // 从 xml 根节点开始解析文件
   doRegisterBeanDefinitions(root);
}


/**
* 解析XML配置文件中 Bean定义，是主要用来解析XML的类 
* 判断profile属性，表示环境，选择对应环境的配置文件
* 工具类 BeanDefinitionParserDelegate IMPORT
* 只有一个该方法
* POSITION : DefaultBeanDefinitionDocumentReader implements BeanDefinitionDocumentReader(ROOT接口)
*/
protected void doRegisterBeanDefinitions(Element root) {
   // BeanDefinitionParserDelegate 必定是一个重要的类，负责解析 Bean 定义，
   // 这里为什么要定义一个 parent? 看到后面就知道了，是递归问题，
   // 因为 <beans/> 内部是可以定义 <beans/> 的，所以这个方法的 root 其实不一定就是 xml 的根节点，也可以是嵌套在里面的 <beans /> 节点，从源码分析的角度，我们当做根节点就好了
   BeanDefinitionParserDelegate parent = this.delegate;

   // private BeanDefinitionParserDelegate delegate;
   this.delegate = createDelegate(getReaderContext(), root, parent);

   if (this.delegate.isDefaultNamespace(root)) {
   	  // public static final String PROFILE_ATTRIBUTE = "profile";
   	  // 解析配置文件<beans>标签profile属性，就是环境属性。
      // 获取根节点 <beans ... profile="dev" /> 中的 profile属性， 判断是否是当前环境需要的，
      String profileSpec = root.getAttribute(PROFILE_ATTRIBUTE);

      // 如果当前环境配置的 profile 不包含此 profile，那就直接 return 了，不对此 <beans /> 解析
      if (StringUtils.hasText(profileSpec)) {
      	 //public static final String MULTI_VALUE_ATTRIBUTE_DELIMITERS = ",; ";
      	 //多环境，可以用 , 或者 ; 来间隔
         String[] specifiedProfiles = StringUtils.tokenizeToStringArray(
               profileSpec, BeanDefinitionParserDelegate.MULTI_VALUE_ATTRIBUTE_DELIMITERS);
         //spring.profiles.active，查询指定属性值是否配置，匹配profile属性，
         //如果不匹配直接return，不再解析。
         //如何获取spring.profiles.active属性值，TODO
         if (!getReaderContext().getEnvironment().acceptsProfiles(specifiedProfiles)) {
            if (logger.isInfoEnabled()) {
               logger.info("Skipped XML bean definition file due to specified profiles [" + profileSpec +
                     "] not matching: " + getReaderContext().getResource());
            }
            return;
         }
      }
   }

   /*
   钩子方法
   钩子方法源于设计模式中模板方法模式，模板方法模式中分为两大类：模版方法和基本方法，而基本方法又分为：抽象方法，具体方法，钩子方法。
   钩子方法，是对于抽象方法或者接口中定义的方法的一个空实现
   在实际中的应用，比如有一个接口，接口里有7个方法而只想实现其中一个方法，
   那么可以写一个抽象类实现这个接口，在这个抽象类里将你要用的那个方法设置为abstract,其它方法进行空实现，
   然后再继承这个抽象类，就不需要实现其它不用的方法，这就是钩子方法的作用。
   */
   preProcessXml(root);

   // 主要解析方法 IMPORT
   parseBeanDefinitions(root, this.delegate);

   //钩子方法
   postProcessXml(root);

   this.delegate = parent;
}

/**
* 解析DOC对象，分为默认标签和其他标签
* 工具类 BeanDefinitionParserDelegate IMPORT
* 只有一个该方法
* POSITION : DefaultBeanDefinitionDocumentReader implements BeanDefinitionDocumentReader(ROOT接口)
*/
protected void parseBeanDefinitions(Element root, BeanDefinitionParserDelegate delegate) {
   //defaultNamespace,默认命名空间，"http://www.springframework.org/schema/beans"
   //默认四个标签 <import/>、 <alias/>、 <bean/> 、<beans/>	
   if (delegate.isDefaultNamespace(root)) {
      NodeList nl = root.getChildNodes();
      //遍历子节点
      for (int i = 0; i < nl.getLength(); i++) {
         Node node = nl.item(i);
         if (node instanceof Element) {
            Element ele = (Element) node;
            if (delegate.isDefaultNamespace(ele)) {
               // 解析 default namespace 下面四个标签
               parseDefaultElement(ele, delegate);
            }
            else {
               // 解析其他 namespace 的元素
               delegate.parseCustomElement(ele);
            }
         }
      }
   }
   else {
      delegate.parseCustomElement(root);
   }
}

/**
* 处理默认命名空间的标签，<import/>、 <alias/>、 <bean/> 、<beans/>	
* 工具类 BeanDefinitionParserDelegate IMPORT
* 只有一个该方法
* POSITION : DefaultBeanDefinitionDocumentReader implements BeanDefinitionDocumentReader(ROOT接口)
*/
private void parseDefaultElement(Element ele, BeanDefinitionParserDelegate delegate) {
   if (delegate.nodeNameEquals(ele, IMPORT_ELEMENT)) {
      // 处理<import/>，标签作用：引入其他配置文件
   	  // <import resource="classpath*:">
      importBeanDefinitionResource(ele);
   }
   else if (delegate.nodeNameEquals(ele, ALIAS_ELEMENT)) {
      // 处理<alias/>，标签作用：为bean设置别名
      // <alias name="fromName" alias="toName"/>
      processAliasRegistration(ele);
   }
   else if (delegate.nodeNameEquals(ele, BEAN_ELEMENT)) {
      // 处理<bean/>，标签作用：定义bean，IMPORT
      processBeanDefinition(ele, delegate);
   }
   else if (delegate.nodeNameEquals(ele, NESTED_BEANS_ELEMENT)) {
      // 嵌套的 <beans/> 标签，需要递归处理
      doRegisterBeanDefinitions(ele);
   }
}


/**
* 处理标签<bean/>,解析标签信息，注册bean
* 工具类 BeanDefinitionParserDelegate IMPORT
* 只有一个该方法
* POSITION : DefaultBeanDefinitionDocumentReader implements BeanDefinitionDocumentReader(ROOT接口)
*/
protected void processBeanDefinition(Element ele, BeanDefinitionParserDelegate delegate) {
   // 将 <bean/> 节点中的信息提取出来，然后封装到一个 BeanDefinitionHolder 中，
   // 委托给BeanDefinitionParserDelegate类进行处理，方法：parseBeanDefinitionElement
   BeanDefinitionHolder bdHolder = delegate.parseBeanDefinitionElement(ele);

   // Bean加载完成
   if (bdHolder != null) {
   	  // 如果有自定义属性的话，进行相应的解析,TODO
      bdHolder = delegate.decorateBeanDefinitionIfRequired(ele, bdHolder);
      try {
         // 注册Bean
         BeanDefinitionReaderUtils.registerBeanDefinition(bdHolder, getReaderContext().getRegistry());
      }
      catch (BeanDefinitionStoreException ex) {
         getReaderContext().error("Failed to register bean definition with name '" +
               bdHolder.getBeanName() + "'", ele, ex);
      }
      // 注册完成，发送事件，TODO
      getReaderContext().fireComponentRegistered(new BeanComponentDefinition(bdHolder));
   }
}