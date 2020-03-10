## Spring组合注解

### 1.元注解定义

#### 1.1Java的元注解

Java 原生的元注解(Meta-annotations) 基本就是指内置的 @Retention, @Target, @Documented, @Inherited, @Repeatable那一干注解，以及自定义注解加上 @Target({ElementType.ANNOTATION_TYPE}) 实现的自定义元注解 。

#### 1.2Spring的元注解

Spring 的元注解概念是不一样的，它认为能用于注解的注解就是元注解，即 @Target({ElementType.Type}) 标识的也是元注解，因为其他的注解也是Type. 



### 2.Spring元注解的工作原理

- Spring提供了`AnnotationUtils`和`AnnotatedElementUtils`工作类，来查找注解类。比如设置了@Profile注解的类，同时也能扫描出@Conditional注解。组合注解相当于是，叠加了两个注解在类或方法上。

```java
/**
* 注解@Conditional定义
**/
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Conditional {

	Class<? extends Condition>[] value();
}


/**
* 注解@Conditional也被认为是@Profile的元注解
**/
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(ProfileCondition.class)
public @interface Profile {

	String[] value();
}

```

