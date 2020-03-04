## ORM框架简介及Mybatis整体架构

### 1.ORM框架简介

- ORM框架的作用，解决的问题
  1. 根据映射配置文件，完成数据在对象模型与关系模型之间的映射。
  2. ORM框架都提供了集成第三方缓存、第三方数据源等组件的接口，而且这些接口都是业界统一的，开发和运维人员可以通过简单的配置完成第三方组件的集成

#### 1.1Hibernate

- 优点
  1. `Hibernate`封装了数据库层面的全部操作，可以通过调用对象方法直接完成SQL操作
  2. `Hibernate`还提供了一种称为`HQL (Hibernate Query Language ）`的语言，从语句的结构上来看，HQL 语句与SQL语句十分类似，但它是一种面向对象的查询语言。
  3. `Hibernate`的`API` 没有侵入性，业务逻辑不需要继承`Hibernate` 的任何接口。
  4. `Hibernate`默认提供了一级缓存和二级缓存，这有利于提高系统的性能，降低数据库压力。
  5. 支持透明的持久化、延迟加载、由对象模型自动生成数据库表等。
- 缺点
  1. 应用层面上不能直接操作原生的`SQL`，无法应用`索引/存储过程/函数`等对SQL进行优化，会导致慢查询。
  2. 在大数据量，高并发，低延迟的场景之下，不是很适合。

#### 1.2JPA

- 背景
  1. `JPA (Java Persistence API)` 是`EJB 3.0` 中持久化部分的规范，`JPA`仅仅是一个持久化的规范，它并没有提供具体的实现。
  2. `Hibernate、EclipseLink`等都提供了`JPA`规范的具体实现。

#### 1.3SpringJDBC

- 定义
  1. 严格来说，`SpringJDBC`并不能算是一个`ORM`框架，仅仅是使用模板方式对原生`JDBC`进行了一层非常薄的封装。
  2. `SpringJDBC`中没有映射文件、对象查询语言、缓存等概念，而是直接执行原生`SQL`语句。
  3. `SpringJDBC`还提供了很多`ORM`化的`Callback` ，这些`Callback`可以将`ResultSet`转化成相应的对象列表。
  4. `SpringJDBC`可以算作一个封装良好、功能强大的`JDBC`工具集。

#### 1.4Mybatis

- 优点
  1. `MyBatis`通过映射配置文件或相应注解将`ResultSet`映射为`Java`对象，其映射规则可以嵌套其他映射规则以及子查询，从而实现复杂的映射逻辑，也可以实现一对一、一对多、多对多映射以及双向映射。
  2. 直接在映射配置文件中编写待执行的原生`SQL`语句，让`SQL`语句选择合适的索引，能更好地提高系统的性能，比较适合大数据量、高并发等场景。
  3. `MyBatis`提供了强大的动态`SQL`功能，`MyBatis`就可以根据执行时传入的实际参数值拼凑出完整的、可执行的`SQL`语句。



###2.Mybatis整体架构

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/1_Mybatis整体架构.png)

- My Batis 的整体架构分为三层，分别是基础支持层、核心处理层和接口层。

#### 2.1基础支持层

##### 2.1.1反射模块

- MyBatis中专门提供了反射模块，该模块对Java原生的反射进行了良好的封装。

  对反射操作进行了一系列优化，例如缓存了类的元数据，提高了反射操作的性能。

##### 2.1.2类型转换

- MyBatis 为简化配置文件提供了别名机制，该机制是类型转换模块的主要功能之一。
- 实现JDBC类型与Java类型之间的转换，用于为SQL语句绑定实参以及映射查询结果集。
  1. 绑定实参。由Java类型转换为JDBC类型。
  2. 映射结果集。由JDBC类型转换为Java类型。

##### 2.1.3日志模块

- 提供详细的日志输出信息
- 集成第三方日志框架

##### 2.1.4资源加载

- 对类加载器进行封装，确定类加载器的使用顺序，并提供了加载类文件以及其他资源文件的功能。

##### 2.1.5解析器模块

- 对`XPath`进行封装，为`MyBatis`初始化时解析`mybatis-config.xml`配置文件以及映射配置文件提供支持。
- 为处理动态`SQL`语句中的占位符提供支持。

##### 2.1.6数据源模块

- MyBatis自身提供了相应的数据源实现，当然MyBatis 也提供了与第三方数据源集成的接口。

##### 2.1.7事务管理模块

- 自身提供了相应的事务接口和简单实现。
- 大多数场景下，`MyBatis`会与`Spring`框架集成，并由`Spring`框架管理事务。

##### 2.1.8缓存模块

- MyBatis中提供了一级缓存和二级缓存，而这两级缓存都是依赖于基础支持层中的缓存模块实现的。
- MyBatis 中自带的这两级缓存与MyBatis以及整个应用是运行在同一个JVM中的，共享同一块堆内存。当需要缓存大量数据时，优先考虑使用Redis、Memcache 等缓存产品。

##### 2.1.9Binding模块

- `MyBatis`通过Binding模块将用户自定义的`Mapper`接口与映射配置文件关联起来，系统可以通过调用自定义`Mapper` 接口中的方法执行相应的`SQL`语句完成数据库操作。

#### 2.2核心处理层

##### 2.2.1配置解析及Mybatis初始化

- 加载`mybatis-config.xml`配置文件、映射配置文件以及`Mapper`接口中的注解信息，解析后的配置信息会形成相应的对象并保存到`Configuration`对象中。
- 利用该`Configuration`对象创建Sq!`SessionFactory`对象。
- `MyBatis`初始化之后，开发人员可以通过初始化得到`SqlSessionFactory`创建`SqlSession`对象并完成数据库操作。

##### 2.2.2SOL 解析与scripting 模块

- 根据用户传入的实参，解析映射文件中定义的动态SQL节点，并形成数据库可执行的SQL语句。
- 处理SQL语句中的占位符，绑定用户传入的实参。

##### 2.2.3SQL执行及结果集映射

<img src="https://javanote.oss-cn-shenzhen.aliyuncs.com/2_SQL执行流程.png" style="zoom: 67%;" />

- Executor

  `Executor`主要负责维护一级缓存和二级缓存，并提供事务管理的相关操作，它会将数据库相关操作委托给`StatementHandler`完成。

- StatementHandler

  1. `StatementHandler`首先通过`ParameterHandler`完成SQL语句的实参绑定

  2. 然后通过`java.sql.Statement`对象执行`SQL`语句并得到结果集

  3. 最后通过`ResultSetHandler`完成结果集的映射，得到结果对象并返回

- ParameterHandler

  SQL语句的实参绑定

- ResultSetHandler

  结果集的映射

##### 2.2.1插件

#### 2.3接口层

- SqlSession对象
  1. 其核心是`SqlSession`接口，该接口中定义了`MyBatis`暴露给应用程序调用的`API` ，也就是上层应用与`MyBatis`交互的桥梁。
  2. 接口层在接收到调用请求时，会调用核心处理层的相应模块来完成具体的数据库操作。

