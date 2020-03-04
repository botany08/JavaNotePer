## SQL语句的解析

### 1.封装类

- 映射配置文件中定义的`SQL`节点会被解析成`MappedStatement`对象。
- `SQL`语句会被解析成`SqISource`对象。
- `SQL`语句中定义的动态`SQL`节点、文本节点等，则由`SqlNode`接口的相应实现表示。

### 1.1SqlSource接口

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/4SqlSource接口实现.png)

```java
public interface SqlSource {
    // 通过解析得到BoundSql对象,可以直接执行，其中封装了包含”？”占位符的SQL语句，参数映射关系以及绑定的实参。
  BoundSql getBoundSql(Object parameterObject);
}
```

1. `DynamicSqlSource`负责处理动态SQL语句，`RawSqlSource`负责处理静态语句，两者最终都会将处理后的SQL语句封装成`StaticSqlSource`返回。

2. 如果节点只包含`“#{}”`占位符，而不包含动态SQL节点标签或未解析的`“${}”`占位符的话，则不是动态SQL语句。

   因为`“#{}”`占位符可以直接替换成`？`，而后传入参数交由JDBC原生语句处理。而`“${}”`占位符需要替换成参数，并组成SQL，再交由JDBC处理。

3. SQL解析时机的区别，`DynamicSqlSource`是在实际执行SQL语句之前，`RawSqlSource`是在MyBatis初始化时完成SQL语句的解析。

4. 无论是`StaticSqlSource` 、`DynamicSqlSource`还是`RawSqlSource` ，最终都会统一生成`BoundSql`对象。



### 2.DynamicContext容器

#### 2.1作用

- 用于记录解析动态SQL语句之后产生的SQL语句片段，是一个用于记录动态SQL语句解析结果的容器。
- 内部通过一个`StringBuilder`对象，用来记录SQL语句。

#### 2.2内部类ContextMap

- 继承于Map，作用是将用户传入的参数封装成了MetaObject对象，用来存储运行时传入的参数值。



###3.SqlNode接口

####3.1SqlNode接口主体及其实现类

- 接口定义

```java
/**
* apply()方法根据用户传入的实参参数,解析该SqlNode所记录的动态SQL节点.
* 并调用DynamicContext.appendSql()方法将解析后的SQL片段追加到DynamicContext.sqlBuilder中保存.
* 当SQL 节点下的所有SqlNode完成解析后，我们就可以从DynamicContext中获取一条动态生成的完整的SQL语句.
**/
public interface SqlNode {
  boolean apply(DynamicContext context);
}
```

- 接口实现类

![](https://javanote.oss-cn-shenzhen.aliyuncs.com/5_SqlNode接口实现.png)

#### 3.2StaticTextSqlNode

- 使用text字段`(String类型)`记录了对应的非动态SQL语句节点，其`apply()`方法直接将text字段追加到DynamicContext.sqlBuilder字段中。
- 如果节点只包含`“#{}”`占位符，而不包含动态SQL节点标签或未解析的`“${}”`占位符的话，是静态语句。

#### 3.3MixedSqlNode

- `MixedSqlNode`中使用`contents`字段`(List<SqlNode>类型)`记录其子节点对应的`SqlNode`对象集合，其`apply()`方法会循环调用`contents`集合中所有`SqlNode`对象的`apply()`方法。

#### 3.4TextSqlNode

- `TextSqlNode`表示的是包含`“${}”`占位符的动态SQL节点。

  `TextSqlNode.apply()`方法会使用`GenericTokenParser`解析`“${}”`占位符，并直接替换成用户给定的实际参数值。

- 解析过程

  1. 定位到`${}`占位符中的属性名称。
  2. 通过`Ognl`表达式，从`DynamicContext.ContextMap`中取出对应的运行时属性值。
  3. 将`${}`占位符替换成具体的属性值。

#### 3.5lfSqlNode

- 首先会通过`ExpressionEvaluator.evaluateBoolean()`方法检测其test表达式是否为`true`，然后根据`test`表达式的结果，决定是否执行其子节点的`apply()`方法。

#### 3.6TrimSqlNode&WhereSqlNode&SetSqlNode

- TrimSqlNode会根据子节点的解析结果，添加或删除相应的前缀或后缀。

#### 3.7ForeachSqlNode

- 略

#### 3.8ChooseSqlNode

- MyBatis 会将<choose>标签解析成ChooseSqINode ，将<when>标签解析成lfSqlNode ，将<otherwise>标签解析成MixedSqINode

#### 3.9VarDeclSqlNode

- `VarDeclSqlNode`表示的是动态SQL语句中的<bind>节点，该节点可以从OGNL表达式中创建一个变量并将其记录到上下文中。

### 4.SqlSourceBuilder

#### 4.1JDBC中PreparedStatement的用法

```java
// SQL语句已发送给数据库，并编译好为执行作好准备
PreparedStatement pstmt = con.prepareStatement("UPDATE emp SET job= ? WHERE empno = ?");
// 对占位符进行初始化 
pstmt.setLong(1, "Manager");
pstmt.setInt(2,1001);
// 执行SQL语句
pstmt.executeUpdate();
```

#### 4.2主要作用

- `SqlNode.apply()`方法的解析之后，SQL语句会被传递到`SqlSourceBuilder`中进行进一步的解析。
  1. 解析SQL语句中的`#{}`占位符中定义的属性。
  2. 将SQL语句中的`#{}`占位符替换成`?`占位符。

#### 4.3解析流程

1. 取得`#{}`占位符参数名称，读取用户传入的实参。
2. 调用`TypeHandler`类，进行`JavaType`到`JdbcType`类型解析，并将属性封装成`ParameterMapping`类。
3. 将SQL语句中`#{}`占位符替换成`?`占位符。
4. 最后创建一个`StaticSqlSource`对象，以供执行器调用。



### 5.DynamicSqlSource

#### 5.1主要作用

- `DynamicSqlSource`负责解析动态SQL语句，通过调用`MixedSqlNode`对象的`apply()`方法，循环调用了所有动态SQL的`apply()`方法。
- 解析的时间点，一般是运行时调用之前。



### 6.RawSqlSource

#### 6.1主要作用

- 负责解析静态SQL语句，在Mybatis初始化的时候就解析完成。
- 首先会调用`getSql()`方法其中通过调用`SqINode.apply()`方法完成SQL语句的拼装和初步处理，之后会使用Sq!SourceBuilder完成占位符的替换和`ParameterMapping`集合的创建，井返回`StaticSqlSource`对象。





