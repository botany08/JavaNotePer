## SpringBoot自动配置

### 1.注解@Conditional

#### 1.1基本定义

-  根据某个条件创建特定的Bean，通过实现Condition接口，并重写matches接口来构造判断条件。通常结合@Bean来使用。

```java
/**
* 注解@Conditionnal定义
* 1.可以用在类和方法上,运行时生效,需要传入一个实现了Condition接口class数组.
* 2.只有满足Condition接口match方法为true,才会创建一个Bean.
*/
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Conditional {
	Class<? extends Condition>[] value();
}
```

#### 1.2注解@Profile - @Conditional的简单应用

```java
/**
* 配置类, @Profile作用是根据不同的运行环境,让Spring容器注册Bean
**/
@Configuration
public class AppConfig {
	@Bean
	@Profile("DEV")
	public DataSource devDataSource() {
		...
	}
	
	@Bean
	@Profile("PROD")
	public DataSource prodDataSource() {
		...
	}
}
```

-  设置对应的`Profile`属性，具体有三种方式：

  1.  通过`context.getEnvironment().setActiveProfiles("PROD")`来设置`Profile`属性。

  2.  通过设定`jvm`的`spring.profiles.active`参数来设置环境(`SpringBoot`中可以直接在`application.properties`配置文件中设置该属性)。 

  3.  通过在`DispatcherServlet`的初始参数中设置。

     ```xml
     <servlet>
     	<servlet-name>dispatcher</servlet-name>
     	<servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
     	<init-param>
     		<param-name>spring.profiles.active</param-name>
     		<param-value>PROD</param-value>
     	</init-param>
     </servlet>
     ```

      

- 注解`@Profile`的实现原理

  ```java
  /**
  * 注解@Profile定义
  * Spring的组合注解,会同时扫描到@Conditionnal和@Profile
  **/
  @Target({ElementType.TYPE, ElementType.METHOD})
  @Retention(RetentionPolicy.RUNTIME)
  @Documented
  @Conditional(ProfileCondition.class)
  public @interface Profile {
  	String[] value();
  }
  
  
  /**
  * @注解Conditional的判断方法
  **/
  class ProfileCondition implements Condition {
  
  	@Override
  	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
           // 获取带有Profile注解的值
  		MultiValueMap<String, Object> attrs =
              metadata.getAllAnnotationAttributes(Profile.class.getName());
  		if (attrs != null) {
  			for (Object value : attrs.get("value")) {
                    // 当运行环境参数,与profile值匹配时,注解@Profile才会生效
  				if (context.getEnvironment().acceptsProfiles(Profiles.of((String[]) value))) {
  					return true;
  				}
  			}
  			return false;
  		}
  		return true;
  	}
  
  }
  ```

  

#### 1.3使用@Conditional注解来提供灵活的条件判断

```java
/**
* 1.根据系统参数判断
**/
public class MySQLDatabaseTypeCondition implements Condition {
	@Override
	public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata metadata) {
         // 获得系统参数 dbType
 		String enabledDBType = System.getProperty("dbType"); 
 		// 如果该值等于MySql，则条件成立
 		return (enabledDBType != null && enabledDBType.equalsIgnoreCase("MySql"));
 	}
}

/**
* 2.根据当前工程的类路径中是否存在MongoDB的驱动类来确认
**/
public class MongoDriverPresentsCondition implements Condition {
	@Override
	public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata metadata) {
		try {
			Class.forName("com.mongodb.Server");
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
}

/**
* 3.根据类是否在容器中注册过,来判断
**/
public class UserDAOBeanNotPresentsCondition implements Condition {
	@Override
	public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata metadata) {
		UserDAO userDAO = conditionContext.getBeanFactory().getBean(UserDAO.class);
		return (userDAO == null);
	}
}

/**
* 4.根据配置文件中的某项属性来决定是否注册MongoDAO
**/
public class MongoDbTypePropertyCondition implements Condition {
	@Override
	public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata metadata) {
		String dbType = conditionContext.getEnvironment().getProperty("app.dbType");
		return "MONGO".equalsIgnoreCase(dbType);
	}
}
```



### 2.AutoConfigure(自动配置)源码分析

#### 2.1启动注解@SpringBootApplication

- 基本定义

```java
/**
* SpringBoot启动类
**/
@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}

/**
* 注解@SpringBootApplication定义
* 1. 同时声明了@Configuration、@EnableAutoConfiguration与@ComponentScan三个注解
**/
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(
    excludeFilters = {@Filter(
    type = FilterType.CUSTOM,
    classes = {TypeExcludeFilter.class}
), @Filter(
    type = FilterType.CUSTOM,
    classes = {AutoConfigurationExcludeFilter.class}
)}
)
public @interface SpringBootApplication {
    @AliasFor(
        annotation = EnableAutoConfiguration.class,
        attribute = "exclude"
    )
    Class<?>[] exclude() default {};
    @AliasFor(
        annotation = EnableAutoConfiguration.class,
        attribute = "excludeName"
    )
    String[] excludeName() default {};
    @AliasFor(
        annotation = ComponentScan.class,
        attribute = "basePackages"
    )
    String[] scanBasePackages() default {};
    @AliasFor(
        annotation = ComponentScan.class,
        attribute = "basePackageClasses"
    )
    Class<?>[] scanBasePackageClasses() default {};
}
```



#### 2.2自动配置注解@EnableAutoConfiguration

```java
/**
* @EnableAutoConfiguration定义
**/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@AutoConfigurationPackage
@Import(AutoConfigurationImportSelector.class)
public @interface EnableAutoConfiguration {
	String ENABLED_OVERRIDE_PROPERTY = "spring.boot.enableautoconfiguration";

	Class<?>[] exclude() default {};

	String[] excludeName() default {};

}

/**
* @Import注解作用：可以导入配置类或者Bean到当前类中
**/
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Import {
	Class<?>[] value();
}
```

- 自动配置的主要配置类为`AutoConfigurationImportSelector`。

#### 2.3配置类AutoConfigurationImportSelector

- 读取Spring的默认配置文件`META-INF/spring.factories`

```java
public class AutoConfigurationImportSelector implements .... {
    
    ......
    
    // 自动配置方法入口 - selectImports()
    @Override
	public String[] selectImports(AnnotationMetadata annotationMetadata) {
		if (!isEnabled(annotationMetadata)) {
			return NO_IMPORTS;
		}
		AutoConfigurationMetadata autoConfigurationMetadata = 
            AutoConfigurationMetadataLoader.loadMetadata(this.beanClassLoader);
		AutoConfigurationEntry autoConfigurationEntry =
            getAutoConfigurationEntry(autoConfigurationMetadata,annotationMetadata);
		return StringUtils.toStringArray(autoConfigurationEntry.getConfigurations());
	}
    
    // 返回了自动配置类的信息列表 - getCandidateConfigurations()
    protected List<String> getCandidateConfigurations(
        AnnotationMetadata metadata, AnnotationAttributes attributes) {
		List<String> configurations = 
            // 调用SpringFactoriesLoader.loadFactoryNames()读取META-INF/spring.factories的配置内容
            SpringFactoriesLoader.loadFactoryNames(getSpringFactoriesLoaderFactoryClass(),
				getBeanClassLoader());
		Assert.notEmpty(configurations, 
                        "No auto configuration classes found in META-INF/spring.factories. If you "
				+ "are using a custom packaging, make sure that file is correct.");
		return configurations;
	}
   
}

```



#### 2.4默认配置文件spring.factories

- `spring.factories`配置文件中，记录了Spring中的自动配置类

```properties
# Initializers
org.springframework.context.ApplicationContextInitializer=\
org.springframework.boot.autoconfigure.SharedMetadataReaderFactoryContextInitializer,\
org.springframework.boot.autoconfigure.logging.ConditionEvaluationReportLoggingListener

# Application Listeners
org.springframework.context.ApplicationListener=\
org.springframework.boot.autoconfigure.BackgroundPreinitializer

# Auto Configuration Import Listeners
org.springframework.boot.autoconfigure.AutoConfigurationImportListener=\
org.springframework.boot.autoconfigure.condition.ConditionEvaluationReportAutoConfigurationImportListener

# Auto Configuration Import Filters
org.springframework.boot.autoconfigure.AutoConfigurationImportFilter=\
org.springframework.boot.autoconfigure.condition.OnBeanCondition,\
org.springframework.boot.autoconfigure.condition.OnClassCondition,\
org.springframework.boot.autoconfigure.condition.OnWebApplicationCondition

# Auto Configure
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration,\
org.springframework.boot.autoconfigure.aop.AopAutoConfiguration,\
org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration,\
org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration,\
org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration,\
org.springframework.boot.autoconfigure.cassandra.CassandraAutoConfiguration,\
org.springframework.boot.autoconfigure.cloud.CloudServiceConnectorsAutoConfiguration,\
org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration,\
org.springframework.boot.autoconfigure.context.MessageSourceAutoConfiguration,\
org.springframework.boot.autoconfigure.context.PropertyPlaceholderAutoConfiguration,\
org.springframework.boot.autoconfigure.couchbase.CouchbaseAutoConfiguration,\
org.springframework.boot.autoconfigure.dao.PersistenceExceptionTranslationAutoConfiguration,\
org.springframework.boot.autoconfigure.data.cassandra.CassandraDataAutoConfiguration,\
org.springframework.boot.autoconfigure.data.cassandra.CassandraReactiveDataAutoConfiguration,\

......

```



### 3.自动配置类

- 举例：MongoDataAutoConfiguration

  注解`@ConditionalOnClass`的含义是， 指定的类必须存在于类路径下，`MongoDataAutoConfiguration`类中声明了类路径下必须含有`MongoClient.class`, `com.mongodb.client.MongoClient.class`, `MongoTemplate.class`这三个类，否则该自动配置类不会被加载。 

```java
/**
* MongoDataAutoConfiguration类,使用注解@ConditionalOnClass
**/
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass({ MongoClient.class, com.mongodb.client.MongoClient.class, MongoTemplate.class })
@EnableConfigurationProperties(MongoProperties.class)
@Import({ MongoDataConfiguration.class, MongoDbFactoryConfiguration.class, 
         MongoDbFactoryDependentConfiguration.class })
@AutoConfigureAfter(MongoAutoConfiguration.class)
public class MongoDataAutoConfiguration {

}

/**
* 注解@ConditionalOnClass定义,组合了注解@Conditional
**/
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
// 当类OnClassCondition.match()返回为true时,才会加载bean
@Conditional(OnClassCondition.class)
public @interface ConditionalOnClass {
	Class<?>[] value() default {};
	String[] name() default {};
}


```



### 4.SpringBoot加载自动配置类过程

```java
/**
* 启动入口
**/
@SpringBootApplication
public class ProductBootApplication {
	public static void main(String[] args) {
		SpringApplication.run(ProductBootApplication.class, args);
	}

}
```

#### 4.1相关注解作用

- **@SpringBootApplication**

  相当于`@Configuration` `@EnableAutoConfiguration` `@ComponentScan` 的集合。

- **@Configuration**

  `JavaConfig`形式的`SpringIOC`容器的配置类使用`@Configuration`。启动类标注了`@Configuration`之后，本身其实也是一个`IOC`容器的配置类。

- **@EnableAutoConfiguration**

  `@EnableAutoConfiguration`借助`@Import`的帮助，将所有符合自动配置条件的`bean`定义加载到当前`SpringIOC`容器

- **@ComponentScan**

  自动扫描并加载符合条件的组件(比如`@Component`和`@Repository`等)或者bean定义，最终将这些`bean`定义加载到`IOC`容器中。

#### 4.2加载流程

1. 调用`SpringApplication.run()`方法，完成`application.properties`等相关配置文件的加载，初始化一个`SpringIOC`容器。
2. 扫描注解`@Configuration`，标记当前类为配置类。将自动配置相关类，实例化`Bean`到`SpringIOC`容器中。
3. 根据加载配置类的步骤，结合`application.properties`配置文件，加载配置。

