## Mybatis环境搭建简单实例

### 1.配置文件

#### 1.1Pom.xml引入依赖

```xml
<!-- ALI连接池 -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid</artifactId>
    <version>1.1.21</version>
</dependency>

<!-- Mysql连接-->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>5.1.47</version>
</dependency>

<!-- mybatis框架 -->
<dependency>
    <groupId>org.mybatis</groupId>
    <artifactId>mybatis</artifactId>
    <version>3.4.6</version>
</dependency>

<!-- 编译文件位置,将mybatis的xml文件编译进去 -->
<build>
    <resources>
        <resource>
            <directory>src/main/java</directory>
            <includes>
                <include>**/*.xml</include>
            </includes>
            <filtering>true</filtering>
        </resource>
    </resources>
</build>
```

#### 1.2框架配置configuration.xml

- `Configuration.xml`配置文件，是`Mybatis`用来初始化`sessionFactory`及相关类使用。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE configuration
        PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
    <!-- 为实体类指定别名，别名为CarMapper.xml中resultType对应的名称 -->
    <typeAliases>
        <typeAlias type="com.joker.springBasic.mybatisFramework.model.Car" alias="Car" />
    </typeAliases>

    <environments default="development">
        <environment id="development">
            <!-- 事务管理模块配置 -->
            <transactionManager type="JDBC"></transactionManager>
            <!-- 数据源配置 -->
            <dataSource type="POOLED">
                <property name="driver" value="com.mysql.jdbc.Driver" />
                <property name="url" 
                          value="jdbc:mysql://10.73.129.202:3306/car?useUnicode=true&amp;
                                 characterEncoding=utf8&amp;zeroDateTimeBehavior
                                 =convertToNull&amp;useSSL=false" />
                <property name="username" value="root" />
                <property name="password" value="123456" />
            </dataSource>
        </environment>
    </environments>

    <!-- 类的xml映射文件位置 -->
    <mappers>
        <mapper resource="com/joker/springBasic/mybatisFramework/mapper/CarMapper.xml" />
    </mappers>
</configuration>
```



###2.定义实体类及xml映射文件

- Car，用来接收查询返回的结果

  ```java
  package com.joker.springBasic.mybatisFramework.model;
  
  public class Car {
  
      private String name;
  
      private String color;
  
      public String getName() {
          return name;
      }
  
      public void setName(String name) {
          this.name = name;
      }
  
      public String getColor() {
          return color;
      }
  
      public void setColor(String color) {
          this.color = color;
      }
  }
  ```

- CarMapper.xml

  ```xml
  <?xml version="1.0" encoding="UTF-8"?>
  <!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
  
  <!-- namespace属性,是xml映射文件对应的类名,如果不是接口编程,可随便定义 -->
  <mapper namespace="com.mapper.CarMapper">
  
      <!-- resultType可使用别名 -->
      <select id="queryCar" resultType="Car">
        select * from car;
      </select>
  </mapper>
  ```

- 执行入口

  ```java
  public class StartPoint {
      public static void main(String[] args) throws Exception{
          // 加载配置文件
          Reader reader = Resources.getResourceAsReader("configuration.xml");
          SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader);
          SqlSession session = factory.openSession();
  
          // 此处加载的类,需要和xml文件的namespace保持一致
          List<Car> carList = session.selectList("com.mapper.CarMapper.queryCar");
  
          for(Car car : carList) {
              System.out.println("名称："+car.getName()+" 颜色："+car.getColor());
          }
      }
  }
  ```

### 3.接口编程

- 新建一个接口，类名对应xml配置文件的namespace，方法名对应id

```java
/**
* 映射接口文件
**/
public interface CarMapper {
    List<Car> queryCar();
}

/**
* 执行查询
**/
public class StartPoint {
    public static void main(String[] args) throws Exception{

        // 初始化流程
        Reader reader = Resources.getResourceAsReader("configuration.xml");
        SqlSessionFactory factory = new SqlSessionFactoryBuilder().build(reader);
        SqlSession session = factory.openSession();

        // 获取接口对象执行查询语句,本质上是为接口生成了代理对象
        List<Car> carList = session.getMapper(CarMapper.class).queryCar();

        for(Car car : carList) {
            System.out.println("名称："+car.getName()+" 颜色："+car.getColor());
        }

    }
}
```

