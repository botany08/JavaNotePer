Mybatis初始化

### 1. 工作内容

- MyBatis中的配置文件主要有两个，分别是mybatis-config.xml 配置文件和映射配置文件。
- 在MyBatis 初始化的过程中，除了会读取mybatis-config.xml 配置文件以及映射配置文件，还会加载配置文件指
  定的类，处理类中的注解，创建一些配置对象，最终完成框架中各个模块的初始化。



### 2.初始化流程

#### 2.1初始化入口SqlSessionFactoryBuilder.build()

- 创建了`XMLConfigBuilder`对象来解析`mybatis-config.xml`配置文件。
  1. 调用`XMLConfigBuilder.parse()`方法，得到配置信息`Configuration`。
  2. 通过`SqlSessionFactoryBuilder.build(Configuration)`方法，创建`DefaultSqlSessionFactory`对象。

#### 2.2解析mybatis-config.xml 配置文件，类XMLConfigBuilder

- 继承结构，父类为抽象类`BaseBuilder`

  ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/3_BaseBuilder继承体系.png)



- 抽象类`BaseBuilder`定义基础功能

  1. 持有一个Configuration对象，即配置信息的封装类。

  2. `BaseBuilder.resolveAlias()`方法，依赖`TypeAliasRegistry`解析别名。

  3. `BaseBuilder.resolveTypeHandler()`方法，依赖`TypeHandlerRegistry`查找指定的`TypeHandler`对象。

  4. resolveJdbcType()、resolveResultSetType()、resolveParameterMode()方法，将String转换成对应的枚举对象。

     在`Mybatis`中，`JdbcType`枚举类型表示`JDBC`类型，`ResultSetType`枚举类型表示结果集类型，`ParameterMode`枚举类型表示存储过程中的参数类型。

- 解析配置文件核心方法，`XMLConfigBuilder.parseConfiguration()`

  1. 解析配置文件中各个节点，如<typeAliases>别名节点，<mappers>映射文件节点等。
  2. 创建对象`XMLMapperBuilder`，加载映射文件。
  3. 如果映射配置文件存在相应的`Mapper`接口，也会加载相应的`Mapper`接口，解析其中的注解并完成向`MapperRegistry`的注册。

#### 2.3解析映射文件，类XMLMapperBuilder

- 解析入口

  1. `XMLMapperBuilder.parse()`方法是解析映射文件的入口，将每个节点的解析过程封装成了一个方法。

  2. 主要结点有

     | 节点名称                         | 作用         |
     | -------------------------------- | ------------ |
     | <mapper>                         | 略           |
     | <cache-ref>                      | 略           |
     | <cache>                          | 略           |
     | <resultMap>                      | 结果映射配置 |
     | <sql>                            | 略           |
     | <select><update><insert><delete> | SQL语句节点  |


- 解析resultMap节点

  1. `ResultMap`类，每个<resultMap>节点都会被解析成一个`ResultMap`对象，其中每个节点所定义的映射关系，则使用`ResultMapping`对象表示。

  2. 每个`ResultMapping`对象记录了，结果集中的一列与JavaBean中一个属性之间的映射关系。

  3. 对于<resultMap>中的每一个子节点<result>，通过别名的解析，读取到了`JavaBean`中属性的`JavaType`及属性名，并且关联到配置的`columnName`，最后封装成一个`ResultMapping`对象。
- 解析SQL语句节点
  1. 创建`XMLStatementBuilder`对象，用来解析带有`SQL`语句的节点。

#### 2.4解析SQL语句节点，类`XMLStatementBuilder`

- SQL语句节点类型，`MappedStatement`类

  1. `MappedStatement`类，表示映射配置文件中定义的SQL节点，包括`id`，`resultType`，语句主体等属性。
  2. 在类中，`SqlSource`接口表示映射文件或注解中定义的SQL语句，`SqlCommandType`类表示语句的类型增删改查。

- 解析SQL语句文本

  1. 创建SQL语句对象`XMLLanguageDriver.createSqlSource()`。

  2. 创建XMLScriptBuilder对象，用来解析子节点。动态SQL节点的判定，总共两个类型。

     纯文本节点Text中，包含${}符号，是动态SQL。

     标签节点，包含<where><if>等标签的节点，是动态SQL。

#### 2.5绑定命名空间和接口

- 方法入口
  1. `XMLMapperBuilder. bindMapperForNamespace()`，底层用一个Map来绑定namespace和接口名称。
  2. 其次，会调用`MapperAnnotationBuilder.parse()`方法解析Mapper接口中的注解信息。
  3. 这样就可以用接口代理类，调用对应的SQL节点封装对象。



