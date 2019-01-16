/**
* 持有一个BeanDefinition，beanName，aliases
* 类BeanDefinitionHolder implements BeanMetadataElement
*/
public class BeanDefinitionHolder implements BeanMetadataElement{}


/**
* 用来注册Bean的入口
* 类BeanDefinitionReaderUtils
* 方法registerBeanDefinition() 为 入口方法
*/
public static void registerBeanDefinition(
   BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry)
   throws BeanDefinitionStoreException {

   // 获取bean的名称
   String beanName = definitionHolder.getBeanName();
   // 注册这个Bean
   registry.registerBeanDefinition(beanName, definitionHolder.getBeanDefinition());

   // 获取所有别名
   String[] aliases = definitionHolder.getAliases();
   if (aliases != null) {
      for (String alias : aliases) {
         // alias -> beanName 保存它们的别名信息，这个很简单，用一个 map 保存一下就可以了，
         // 获取的时候，会先将 alias 转换为 beanName，然后再查找
         registry.registerAlias(beanName, alias);
      }
   }
}

/**
* 注册Bean的具体实现 -- 原理是有一个BeanMap来接收已经注册的BeanDefinition
* 定义该功能的接口为： public interface BeanDefinitionRegistry extends AliasRegistry
* 实现功能的子类为： DefaultListableBeanFactory
* public class DefaultListableBeanFactory extends AbstractAutowireCapableBeanFactory
*		implements ConfigurableListableBeanFactory, BeanDefinitionRegistry, Serializable
*/
public void registerBeanDefinition(String beanName, BeanDefinition beanDefinition)
      throws BeanDefinitionStoreException {
   //断言判断空字符串和null
   Assert.hasText(beanName, "Bean name must not be empty");
   Assert.notNull(beanDefinition, "BeanDefinition must not be null");

   if (beanDefinition instanceof AbstractBeanDefinition) {
      try {
         ((AbstractBeanDefinition) beanDefinition).validate();
      }
      catch (BeanDefinitionValidationException ex) {
         throw new BeanDefinitionStoreException(...);
      }
   }

   // old? 还记得 “允许 bean 覆盖” 这个配置吗？allowBeanDefinitionOverriding
   // 定义一个新的BeanDefinition
   BeanDefinition oldBeanDefinition;

   // 所有的 Bean 注册后会放入这个 beanDefinitionMap 中
   // 获取这个beanName的BeanDefinition实例
   oldBeanDefinition = this.beanDefinitionMap.get(beanName);

   // 处理重复名称的 Bean 定义的情况
   // 改BeanName已经存在
   if (oldBeanDefinition != null) {
      if (!isAllowBeanDefinitionOverriding()) {
         // 如果不允许覆盖的话，抛异常
         throw new BeanDefinitionStoreException(beanDefinition.getResourceDescription()...
      }
      else if (oldBeanDefinition.getRole() < beanDefinition.getRole()) {
         // log...用框架定义的 Bean 覆盖用户自定义的 Bean 
      }
      else if (!beanDefinition.equals(oldBeanDefinition)) {
         // log...用新的 Bean 覆盖旧的 Bean
      }
      else {
         // log...用同等的 Bean 覆盖旧的 Bean，这里指的是 equals 方法返回 true 的 Bean
      }
      // 覆盖
      this.beanDefinitionMap.put(beanName, beanDefinition);
   }
   else {
      // 功能：判断是否已经有其他的 Bean 开始初始化了.
      // 注意，"注册Bean" 这个动作结束，Bean 依然还没有初始化，我们后面会有大篇幅说初始化过程，
      // 在 Spring 容器启动的最后，会 预初始化 所有的 singleton beans
      if (hasBeanCreationStarted()) {
         // 上锁
         synchronized (this.beanDefinitionMap) {
         	// 将该BeanDefinition注册到beanDefinitionMap
            this.beanDefinitionMap.put(beanName, beanDefinition);

            // 将所有的beanName添加到beanDefinitionNames中
            List<String> updatedDefinitions = new ArrayList<String>(this.beanDefinitionNames.size() + 1);
            updatedDefinitions.addAll(this.beanDefinitionNames);
            updatedDefinitions.add(beanName);
            this.beanDefinitionNames = updatedDefinitions;

            // manualSingletonNames如果含有这个beanName，则删除。
            if (this.manualSingletonNames.contains(beanName)) {
               Set<String> updatedSingletons = new LinkedHashSet<String>(this.manualSingletonNames);
               updatedSingletons.remove(beanName);
               this.manualSingletonNames = updatedSingletons;
            }
         }
      }
      else {
         // 正常的分支，1.不存在beanName。2.没有bean在初始化。

         // 将 BeanDefinition 放到这个 map 中，这个 map 保存了所有的 BeanDefinition
         this.beanDefinitionMap.put(beanName, beanDefinition);

         // 这是个 ArrayList，所以会按照 bean 配置的顺序保存每一个注册的 Bean 的名字
         this.beanDefinitionNames.add(beanName);
         
         // 这是个 LinkedHashSet，代表的是手动注册的 singleton bean，
         // 注意这里是 remove 方法，到这里的 Bean 当然不是手动注册的
         // 手动指的是通过调用以下方法注册的 bean ：
         //     registerSingleton(String beanName, Object singletonObject)
         // 这不是重点，解释只是为了不让大家疑惑。Spring 会在后面"手动"注册一些 Bean，
         // 如 "environment"、"systemProperties" 等 bean，我们自己也可以在运行时注册 Bean 到容器中的
         this.manualSingletonNames.remove(beanName);
      }
      // 这个不重要，在预初始化的时候会用到，不必管它。
      this.frozenBeanDefinitionNames = null;
   }

   if (oldBeanDefinition != null || containsSingleton(beanName)) {
      resetBeanDefinition(beanName);
   }
}