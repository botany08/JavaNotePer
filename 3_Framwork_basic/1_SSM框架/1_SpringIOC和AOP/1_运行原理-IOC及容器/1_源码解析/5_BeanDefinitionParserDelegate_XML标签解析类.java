/**
* 处理标签<bean/> , 加载bean
* 类BeanDefinitionParserDelegate
* 有三个重载方法：
* public BeanDefinitionHolder parseBeanDefinitionElement(Element ele) -- 跳转到第二个方法
* public BeanDefinitionHolder parseBeanDefinitionElement(Element ele, BeanDefinition containingBean) -- 会调用第三个方法
* public AbstractBeanDefinition parseBeanDefinitionElement(Element ele, String beanName, BeanDefinition containingBean)
*/

/**
* 解析标签，跳转方法
*/
public BeanDefinitionHolder parseBeanDefinitionElement(Element ele) {
    return parseBeanDefinitionElement(ele, null);
}

/**
* 具体解析bean元素
* POSITION : 类BeanDefinitionParserDelegate
*/
public BeanDefinitionHolder parseBeanDefinitionElement(Element ele, BeanDefinition containingBean) {
   //public static final String ID_ATTRIBUTE = "id";  id属性	
   String id = ele.getAttribute(ID_ATTRIBUTE);
   //public static final String NAME_ATTRIBUTE = "name"; name属性
   String nameAttr = ele.getAttribute(NAME_ATTRIBUTE);

   List<String> aliases = new ArrayList<String>();

   //public static final String MULTI_VALUE_ATTRIBUTE_DELIMITERS = ",; ";
   // 将 name 属性的定义按照 “逗号、分号、空格” 切分，形成一个别名列表数组，
   // 当然，如果不定义 name 属性的话，就是空的了
   if (StringUtils.hasLength(nameAttr)) {
   	  //tokenizeToStringArray方法 TODO
      String[] nameArr = StringUtils.tokenizeToStringArray(nameAttr, MULTI_VALUE_ATTRIBUTE_DELIMITERS);
      aliases.addAll(Arrays.asList(nameArr));
   }

   String beanName = id;
   // 如果没有指定id, 那么用别名(name属性值)列表的第一个名字作为beanName
   if (!StringUtils.hasText(beanName) && !aliases.isEmpty()) {
      beanName = aliases.remove(0);
      if (logger.isDebugEnabled()) {
         logger.debug("No XML 'id' specified - using '" + beanName +
               "' as bean name and " + aliases + " as aliases");
      }
   }

   // 验证id或者alias是否重复，若重复则报错
   if (containingBean == null) {
      checkNameUniqueness(beanName, aliases, ele);
   }

   // 创建一个BeanDefinition实例 -- IMPORT
   // 根据 <bean ...>...</bean> 中的配置创建 BeanDefinition，然后把配置中的信息都设置到实例中,
   AbstractBeanDefinition beanDefinition = parseBeanDefinitionElement(ele, beanName, containingBean);

   // 到这里，整个 <bean /> 标签就算解析结束了，一个 BeanDefinition 就形成了。
   if (beanDefinition != null) {
      // 有beanDefinition实例，没有设置id或者name，执行。 -- NOTIMPORT
   	  // 作用 ： 自动生成beanname
      if (!StringUtils.hasText(beanName)) {
         try {
         	//当containingBean不为空
            if (containingBean != null) {
               beanName = BeanDefinitionReaderUtils.generateBeanName(
                     beanDefinition, this.readerContext.getRegistry(), true);
            }
            else {
               // 如果我们不定义 id 和 name，那么我们引言里的那个例子：
               // 1. beanName 为：com.javadoop.example.MessageServiceImpl#0
               // 2. beanClassName 为：com.javadoop.example.MessageServiceImpl
               beanName = this.readerContext.generateBeanName(beanDefinition);

               String beanClassName = beanDefinition.getBeanClassName();
               if (beanClassName != null &&
               		 beanName.startsWith(beanClassName) && 
               		 beanName.length() > beanClassName.length() &&
                     !this.readerContext.getRegistry().isBeanNameInUse(beanClassName)) {
                  // 把 beanClassName 设置为 Bean 的别名
                  aliases.add(beanClassName);
               }
            }
            if (logger.isDebugEnabled()) {
               logger.debug("Neither XML 'id' nor 'name' specified - " +
                     "using generated bean name [" + beanName + "]");
            }
         }
         catch (Exception ex) {
            error(ex.getMessage(), ele);
            return null;
         }
      }
      //将别名集合 重新转换为 别名数组
      String[] aliasesArray = StringUtils.toStringArray(aliases);
      // 返回 BeanDefinitionHolder
      return new BeanDefinitionHolder(beanDefinition, beanName, aliasesArray);
   }
   return null;
}

/**
* 根据配置创建一个BeanDefinition实例
* POSITION : 类BeanDefinitionParserDelegate
*/
public AbstractBeanDefinition parseBeanDefinitionElement(Element ele, String beanName, BeanDefinition containingBean) {

   this.parseState.push(new BeanEntry(beanName));

   // public static final String CLASS_ATTRIBUTE = "class";
   // 获取class属性值
   String className = null;
   if (ele.hasAttribute(CLASS_ATTRIBUTE)) {
      className = ele.getAttribute(CLASS_ATTRIBUTE).trim();
   }

   try {
   	  // public static final String PARENT_ATTRIBUTE = "parent";
   	  // 获取parent属性值
      String parent = null;
      if (ele.hasAttribute(PARENT_ATTRIBUTE)) {
         parent = ele.getAttribute(PARENT_ATTRIBUTE);
      }
      // 创建BeanDefinition，类AbstractBeanDefinition，参数为className，parent
      AbstractBeanDefinition bd = createBeanDefinition(className, parent);
      

      // 设置 BeanDefinition 的一堆属性，这些属性定义在 AbstractBeanDefinition 中
      parseBeanDefinitionAttributes(ele, beanName, containingBean, bd);

      // public static final String DESCRIPTION_ELEMENT = "description";
      // description标签，描述改bean
      bd.setDescription(DomUtils.getChildElementValueByTagName(ele, DESCRIPTION_ELEMENT));

      /**
       * 下面的一堆是解析 <bean>......</bean> 内部的子元素，
       * 解析出来以后的信息都放到 bd 的属性中
       */

      // 解析 <meta/> , 附加到bean定义的任意元数据。 TODO
      // 属性name，value
      parseMetaElements(ele, bd);

      // 解析 <lookup-method/> ， 查找方法使IoC容器覆盖给定方法，并返回bean属性中给出的名称的bean。这是方法注入的一种形式。
      parseLookupOverrideSubElements(ele, bd.getMethodOverrides());

      // 解析 <replaced-method/> ， 与查找方法机制类似，被替换的方法元素用于控制IoC容器方法覆盖：方法注入。
      parseReplacedMethodSubElements(ele, bd.getMethodOverrides());

      // 解析 <constructor-arg/> ， Bean定义可以指定零个或多个构造函数参数。这是“autowire构造函数”的替代方法。
      parseConstructorArgElements(ele, bd);

      // 解析 <property/> ， bean当中的属性值
      parsePropertyElements(ele, bd);

      // 解析 <qualifier/> ， Bean定义可以提供限定符以匹配字段或参数上的注释，以进行细粒度的自动线候选解析。
      parseQualifierElements(ele, bd);

      bd.setResource(this.readerContext.getResource());
      bd.setSource(extractSource(ele));

      return bd;
   }
   catch (ClassNotFoundException ex) {
      error("Bean class [" + className + "] not found", ele, ex);
   }
   catch (NoClassDefFoundError err) {
      error("Class that bean class [" + className + "] depends on not found", ele, err);
   }
   catch (Throwable ex) {
      error("Unexpected failure during bean definition parsing", ele, ex);
   }
   finally {
      this.parseState.pop();
   }

   return null;
}


/**
* 根据<bean>标签属性，设置BeanDefinition的一系列属性
* POSITION : 类BeanDefinitionParserDelegate
*/
public AbstractBeanDefinition parseBeanDefinitionAttributes(Element ele, String beanName,
			BeanDefinition containingBean, AbstractBeanDefinition bd) {
		// private static final String SINGLETON_ATTRIBUTE = "singleton";
		// 弃用属性singleton，新属性scope
		if (ele.hasAttribute(SINGLETON_ATTRIBUTE)) {
			error("Old 1.x 'singleton' attribute in use - upgrade to 'scope' declaration", ele);
		}

		// public static final String SCOPE_ATTRIBUTE = "scope";
		// 设置属性scope
		else if (ele.hasAttribute(SCOPE_ATTRIBUTE)) {
			bd.setScope(ele.getAttribute(SCOPE_ATTRIBUTE));
		}

		// 如果内部存在该bean，则使用内部bean的scope
		else if (containingBean != null) {
			// Take default from containing bean in case of an inner bean definition.
			bd.setScope(containingBean.getScope());
		}

		/* 
		   public static final String ABSTRACT_ATTRIBUTE = "abstract";
		   public static final String TRUE_VALUE = "true";
		   abstract - 抽象属性，意味着这个bean不能被实例化，不能通过ApplicationContext.getBean()的方式来获取到该bean，
           也不能使用ref属性引用这个bean。否则会抛出BeanIsAbstractException的异常。
        */
		if (ele.hasAttribute(ABSTRACT_ATTRIBUTE)) {
			bd.setAbstract(TRUE_VALUE.equals(ele.getAttribute(ABSTRACT_ATTRIBUTE)));
		}

		// public static final String LAZY_INIT_ATTRIBUTE = "lazy-init";
		// public static final String DEFAULT_VALUE = "default";
		// 属性懒加载 - 启动时统一初始化并加载至spring的applicationContext中，通过其getBean方法获取。
		// 但有时为了加快系统启动速度，并不需启动时立刻初始化并加载，可以在使用其时才初始化并加载。
		String lazyInit = ele.getAttribute(LAZY_INIT_ATTRIBUTE);
		if (DEFAULT_VALUE.equals(lazyInit)) {
			lazyInit = this.defaults.getLazyInit();
		}
		bd.setLazyInit(TRUE_VALUE.equals(lazyInit));

		// public static final String AUTOWIRE_ATTRIBUTE = "autowire";
		// 属性autowire，控制bean属性是否为“自动装配”，不用在bean标签内注入别的bean
		String autowire = ele.getAttribute(AUTOWIRE_ATTRIBUTE);
		bd.setAutowireMode(getAutowireMode(autowire));

		// public static final String DEPENDENCY_CHECK_ATTRIBUTE = "dependency-check";
		// 属性dependency-check,依赖检查模式-已被弃用，依赖检查主要用于自动装配中判断bean与装配到bean中的数据类型或对象类型是否能装配成功。
		String dependencyCheck = ele.getAttribute(DEPENDENCY_CHECK_ATTRIBUTE);
		bd.setDependencyCheck(getDependencyCheck(dependencyCheck));

		// public static final String DEPENDS_ON_ATTRIBUTE = "depends-on";
		// public static final String MULTI_VALUE_ATTRIBUTE_DELIMITERS = ",; ";
		// 属性depends-on,此bean依赖于初始化的bean的名称。 bean工厂将保证在这个bean之前初始化这些bean。
		if (ele.hasAttribute(DEPENDS_ON_ATTRIBUTE)) {
			String dependsOn = ele.getAttribute(DEPENDS_ON_ATTRIBUTE);
			bd.setDependsOn(StringUtils.tokenizeToStringArray(dependsOn, MULTI_VALUE_ATTRIBUTE_DELIMITERS));
		}

		// public static final String AUTOWIRE_CANDIDATE_ATTRIBUTE = "autowire-candidate";
		// public static final String DEFAULT_VALUE = "default";
		// 属性autowire-candidate,指示在查找匹配的候选项以满足另一个bean的自动装配要求时是否应该考虑此bean
		String autowireCandidate = ele.getAttribute(AUTOWIRE_CANDIDATE_ATTRIBUTE);
		if ("".equals(autowireCandidate) || DEFAULT_VALUE.equals(autowireCandidate)) {
			String candidatePattern = this.defaults.getAutowireCandidates();
			if (candidatePattern != null) {
				String[] patterns = StringUtils.commaDelimitedListToStringArray(candidatePattern);
				bd.setAutowireCandidate(PatternMatchUtils.simpleMatch(patterns, beanName));
			}
		}
		else {
			bd.setAutowireCandidate(TRUE_VALUE.equals(autowireCandidate));
		}

		// public static final String PRIMARY_ATTRIBUTE = "primary";
		// 属性primary，指定当多个候选者有资格自动装配单值依赖项时，应该优先考虑此bean。如果候选者中只存在一个“主”bean，则它将是自动装配的值。
		if (ele.hasAttribute(PRIMARY_ATTRIBUTE)) {
			bd.setPrimary(TRUE_VALUE.equals(ele.getAttribute(PRIMARY_ATTRIBUTE)));
		}

		// public static final String INIT_METHOD_ATTRIBUTE = "init-method";
		// 属性init-method，设置bean属性后要调用的自定义初始化方法的名称。该方法必须没有参数，但可能会抛出任何异常。
		if (ele.hasAttribute(INIT_METHOD_ATTRIBUTE)) {
			String initMethodName = ele.getAttribute(INIT_METHOD_ATTRIBUTE);
			if (!"".equals(initMethodName)) {
				bd.setInitMethodName(initMethodName);
			}
		}
		else {
			if (this.defaults.getInitMethod() != null) {
				bd.setInitMethodName(this.defaults.getInitMethod());
				bd.setEnforceInitMethod(false);
			}
		}

		// public static final String DESTROY_METHOD_ATTRIBUTE = "destroy-method";
		// 属性destroy-method,要在bean工厂关闭时调用的自定义destroy方法的名称。该方法必须没有参数，但可能会抛出任何异常。
		// 注意：仅在生命周期完全由工厂控制的bean上调用 - 对于单例来说总是如此，但对于任何其他范围都不能保证。
		if (ele.hasAttribute(DESTROY_METHOD_ATTRIBUTE)) {
			String destroyMethodName = ele.getAttribute(DESTROY_METHOD_ATTRIBUTE);
			bd.setDestroyMethodName(destroyMethodName);
		}
		else {
			if (this.defaults.getDestroyMethod() != null) {
				bd.setDestroyMethodName(this.defaults.getDestroyMethod());
				bd.setEnforceDestroyMethod(false);
			}
		}

		// public static final String FACTORY_METHOD_ATTRIBUTE = "factory-method";
		// 属性factory-method，用于创建此对象的工厂方法的名称。如果采用参数，则使用constructor-arg元素指定工厂方法的参数。自动装配不适用于工厂方法。
		if (ele.hasAttribute(FACTORY_METHOD_ATTRIBUTE)) {
			bd.setFactoryMethodName(ele.getAttribute(FACTORY_METHOD_ATTRIBUTE));
		}

		// public static final String FACTORY_BEAN_ATTRIBUTE = "factory-bean";
		// 属性factory-bean，替代工厂方法用法的类属性。如果指定了此项，则不应使用类属性。
		if (ele.hasAttribute(FACTORY_BEAN_ATTRIBUTE)) {
			bd.setFactoryBeanName(ele.getAttribute(FACTORY_BEAN_ATTRIBUTE));
		}

		return bd;
	}