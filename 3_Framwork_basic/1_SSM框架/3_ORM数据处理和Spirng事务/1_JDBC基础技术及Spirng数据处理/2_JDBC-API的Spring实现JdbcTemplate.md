## JDBC规范

### 1.基本步骤

```java
// 所有接口都是JAVA所定义的JDBC规范
import java.sql.*;

/**
* JDBC应用步骤
* 1. 加载驱动程序
* 2. 获得数据库连接
* 3. 创建Statement对象
* 4. 执行SQL语句
* 5. 处理结果
* 6. 关闭JDBC对象
**/
public class DatabaseForJava {

    private static final String URL = "jdbc:mysql://127.0.0.1:3306/nretail_tlink_server";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "123456";

    public static void main(String[] args){

        try {
            // 1. 加载驱动程序
            Class.forName("com.mysql.jdbc.Driver");

            // 2. 获得数据库连接
            Connection conn = DriverManager.getConnection(URL,USERNAME,PASSWORD);

            /**
             * 创建Statement对象
             * 3. 创建Statement对象
             * 4. 执行SQL语句
             * 5. 处理结果
             * 6. 关闭JDBC对象
             */
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select * from tlink_product_info");
            while (rs.next()) {
                System.out.println(rs.getString("SKU_CODE")+","+rs.getString("SKU_NAME"));
            }
            rs.close();
            stmt.close();
            conn.close();


            /**
             * 创建PreparedStatement对象
             * 3. 创建PreparedStatement对象
             * 4. 设置参数执行SQL语句
             * 5. 处理结果
             * 6. 关闭JDBC对象
             */
            String sql = "select * from tlink_product_info where SKU_CODE = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1,"0101380319MW");
            ResultSet rp = pstmt.executeQuery();
            while (rp.next()) {
                System.out.println(rp.getString("SKU_CODE")+","+rp.getString("SKU_NAME"));
            }
            rp.close();
            pstmt.close();
            conn.close();

        } catch (ClassNotFoundException c) {
            System.out.println("加载驱动程序失败!");
            c.printStackTrace();
        } catch (SQLException s) {
            System.out.println("SQL执行失败!");
            s.printStackTrace();
        }

    }
}
```



## JdbcTemplate,JDBC-API的实现

### 1.基本概念

- 定义

	1. 为了解决`JDBC-API`在实际使用中的各种问题，`Spring`框架提出了`org.springframe-work.jdbc.core.JdbcTemplate`作为数据访问的`Helper`类。
	2. `JdbcTemplate`是整个Spring数据抽象层所有`JDBC-API`实现的基础，框架内其他更加方便的`Helper`类以及更高层次的抽象，全部构建于`JdbcTemplate`之上。
	3. `JdbcTernplate`主要是通过模板方法模式对基于`JDBC`的数据访问代码进行统一封装。
- `JdbcTemplate`两个关键
  1. 封装所有基于`JDBC`的数据访问代码，以统一的格式和规范来使用`JDBC-API`。所有基于`JDBC`的数据访问需求现在全部通过`JdbcTemplate`进行。
  2. 对`SQLException`所提供的异常信息，在框架内进行统一转译,将基于`JDBC`的数据访问异常纳入`Spring`自身的异常层次体系中，统一了数据接口的定义，简化了客户端代码对数据访问异常的处理。

###2.JdbcTemplate封装类实现

#### 2.1数据库连接池设置

#####2.1.1连接池概念

1. 对数据库连接的管理能显著影响到整个应用程序的伸缩性和健壮性，影响到程序的性能指标。
2. 数据库连接池负责分配、管理和释放数据库连接。允许应用程序重复使用一个现有的数据库连接，释放空闲时间超过最大空闲时间的数据库连接，来避免因为没有释放数据库连接而引起的数据库连接泄露。

##### 2.1.2Spirng常用的连接池

- DBCP

  1. DBCP类包为commons-dbcp.jar，DBCP是一个依赖 Jakarta commons-poo l对象池机制的数据库连接池，所以还应包含commons-pool.jar。
  2. DBCP在实践中存在BUG，在某些种情会产生很多空连接不能释放，Hibernate3.0已经放弃了对其的支持。

- C3P0

  1. C3P0是一个开放源代码的JDBC数据源实现项目，它在lib目录中与Hibernate一起发布，实现了JDBC3和JDBC2扩展规范说明的 Connection 和Statement 池。C3P0类包为c3p0-0.9.0.4.jar。
  2. C3P0比较耗费资源，效率方面可能要低一点。

- Proxool

  1. Sourceforge下的一个开源项目,这个项目提供一个健壮、易用的连接池，最为关键的是这个连接池提供监控的功能，方便易用，便于发现连接泄漏的情况。
  2. Proxool的负面评价较少，现在比较推荐它，而且它还提供即时监控连接池状态的功能，便于发现连接泄漏的情况。

  ```xml
  <dependency>
      <groupId>org.logicalcobwebs</groupId> 
      <artifactId>com.springsource.org.logicalcobwebs.proxool</artifactId>
      <version>0.9.1</version>
  </dependency
  ```

- Druid

  1. 阿里的druid不仅仅是一个数据库连接池，还包含一个ProxyDriver，一系列内置的JDBC组件库，一个SQL Parser。
  2. 强大的监控特性，通过Druid提供的监控功能，可以清楚知道连接池和SQL的工作情况。
  3. Druid提供了Filter-Chain模式的扩展API，可以编写Filter拦截JDBC中的任何方法，可以在上面做任何事情，比如说性能监控、SQL审计、用户名密码加密、日志等等。

  ```xml
  <dependency>
      <groupId>com.alibaba</groupId>
      <artifactId>druid</artifactId>
      <version>1.1.21</version>
  </dependency>
  ```
##### 2.1.3配置方法

- 配置文件

  ```yml
  spring:
      datasource:
          driver-class-name: com.mysql.jdbc.Driver
          type: com.alibaba.druid.pool.DruidDataSource
          url: jdbc:mysql://127.0.0.1:3306/nretail_tlink_server?useUnicode=true
          username: admin_user_tlink
          password: Vg&343mO&
  ```

- Java配置类

  ```java
  @Configuration
  public class DruidConfig {
      
      // 变量设置声明
      ......
      
      @Bean
      @Primary
      public DataSource dataSource() {
          DruidDataSource datasource = new DruidDataSource();
          log.info("----------- druid datasource ----------");
          datasource.setUrl(this.dbUrl);
          datasource.setUsername(username);
          datasource.setPassword(password);
          datasource.setDriverClassName(driverClassName);
  
          datasource.setInitialSize(initialSize);
          datasource.setMinIdle(minIdle);
          datasource.setMaxActive(maxActive);
          datasource.setMaxWait(maxWait);
          datasource.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
          datasource.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
          datasource.setValidationQuery(validationQuery);
          datasource.setTestWhileIdle(testWhileIdle);
          datasource.setTestOnBorrow(testOnBorrow);
          datasource.setTestOnReturn(testOnReturn);
          datasource.setPoolPreparedStatements(poolPreparedStatements);
  
          try {
              datasource.setFilters(filters);
          } catch (SQLException e) {
              e.printStackTrace();
          }
          datasource.setConnectionProperties(connectionProperties);
          return datasource;
      }
  }
  ```


#### 2.2具体方法分类

##### 2.2.1 execute方法

- 作用：可以用于执行任何SQL语句，一般用于执行DDL语句 

##### 2.2.2 update方法及batchUpdate方法 

- 作用：update方法用于执行新增、修改、删除等语句；batchUpdate方法用于执行批处理相关语句 

##### 2.2.3 query方法及queryForXXX方法 

- 作用：用于执行查询相关语句

##### 2.2.4 call方法 

- 作用：用于执行存储过程、函数相关语句 

#### 2.3查询实例

- Spring配置文件--ApplicationContext.xml

```xml
<!-- 开启注解 -->
<context:annotation-config/>

<!--开启组件扫描范围-->
<context:component-scan base-package="com.joker.springBasic.springIOC"/>
```

- 依赖包导入

```xml
<!-- ALI连接池 -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid</artifactId>
    <version>1.1.21</version>
</dependency>

<!--Spring整合第三方orm框架的实现 -->
<dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-orm</artifactId>
    <version>4.3.11.RELEASE</version>
</dependency>

<!-- Mysql连接-->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>5.1.47</version>
</dependency>
```



- 利用JdbcTemplate查询

```java
/**
* 配置类,注册Bean
**/
@Configuration
public class DataSourceConf {

    @Bean
    public DataSource dataSource() {
        DruidDataSource dataSource = new DruidDataSource();

        String username = "root";
        String password = "123456";
        String url = "jdbc:mysql://127.0.0.1:3306/nretail_tlink_server?"+
            "useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull";

        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setUrl(url);

        return dataSource;
    }

    @Bean(name = "jdbcTemplate")
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(dataSource());
    }
}

/**
* JdbcTemplate实例查询
**/
public class TestDatabase {
    public static void main(String[] args) throws Exception{
        ApplicationContext ctx = 
            new ClassPathXmlApplicationContext("classpath*:ApplicationContext.xml");

        JdbcTemplate jdbcTemplate = (JdbcTemplate) ctx.getBean("jdbcTemplate");

        String sql = "select * from tlink_product_info limit 0,1";

        Map<String,Object> map = jdbcTemplate.queryForMap(sql);
        for(Map.Entry<String,Object> entry : map.entrySet()) {
            System.out.println(entry.getKey()+":"+entry.getValue());
        }
    }
}
```

