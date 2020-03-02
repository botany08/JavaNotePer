## 统一的数据访问异常层次体系

### 1.Spring数据访问层

####1.1 数据访问层的划分
- 统一的数据访问异常层次体系

  1. `Spring`框架将特定的数据访问技术相关的异常`(Exception)`进行转译，然后封装为一套标准的异常层次体系。
  2. 通过这套标准异常层次体系，不管使用的数据访问技术如何变化，客户端对象只需要捕获并处理这套标准的异常就可以。

- 以Spirng的方式实现了JDBC-API

  1. `JDBC`作为一套数据访问标准，规范了各个数据库厂商之间的数据访问接口。

     异常定义不足之处，`SQLException`的设计本身太过于宽泛。

     日常使用不足之处，`JDBC API`较为贴近底层，使用上比较烦琐。

  2. `Spring`用`JdbcTemplat`封装了原始的`JDBC-API`规范。

- 以统一的方式对各种ORM方案的集成

  1. 建立在`JDBC`基础上，`ORM`框架主要用来屏蔽对象与关系数据库之间结构的非一致性。
  2. `Spirng`提供了各种`ORM`框架的继承解决方案。

#### 1.2相关概念

- 数据访问标准

  Java本身定义了JDBC-API数据访问标准，作为访问数据库的实现方式。

- Spring数据访问层

  1. `Spring`用`JdbcTemplat`封装了原始的`JDBC-API`规范。
  2. 提供了其他更高级的ORM框架集成。
  3. 定义`JPA`规范，全称为`Java Persistence API` ，Java持久化API是Sun公司在java EE 5规范中提出的Java持久化接口。JPA 中能够支持面向对象的高级特性，如类之间的继承、多态和类之间的复杂关系。

- ORM框架

  1. 以更高级的方式，封装了JDBC-API。Hibernate基于Spring的JPA规范，Mybatis利用了另外一套方式封装。
  2. JPA是一种规范，而Hibernate是其一种实现。JPA的注解已经是hibernate的核心，hibernate只提供了一些补充。

### 2.DAO模式的背景

- 应用背景

  1. 为了统一和简化相关的数据访问操作，`J2EE`核心模式提出了`DAO`(`Data Access Object`,数据访问对象)模式。使用`DAO`模式，可以完全分离数据的访问和存储，很好地屏蔽了各种数据访问方式的差异性。
  2. DAO模式仅仅是一种宽泛的定义，类似于MVC模式。并没有定义相关的接口，如果有定义接口则可以称为规范了，类似于JDBC-API规范以及JPA规范。

- 模式结构

  1. 定义DAO接口为数据操作的工具类，封装了业务数据访问操作，具体操作还是JDBC-API。
  2. 客户端直接实例化DAO接口，调用DAO接口中的方法进行数据库的访问。

  ```java
  /**
  * DAO接口,封装了JDBC的数据访问
  **/
  public interface ICustomerDao {
      Customer findCustomerByPK(String customerId);
      void updateCustomerStatus(Customer customer);
  }
  
  /**
  * 客户端
  **/
  public class Customerservice {
      // 持有DAO对象
      private ICustomerDao customerDao;
      
      public ICustomerDao getCustomerDao() {
      	return customerDao;
      }
      
      public void setCustomerDao(ICustomerDao customerDao) {
      	this.customerDao = customerDao;
      }
      
      // 具体业务调用DAO接口的数据访问方法
      public void disableCustomerCampain(String customerId) {
          Customer customer = getCustomerDao().findCustomerByPK(customerId);
          customer.setCampainStatus(CampainStatus.DISABLE);
          getCustomerDao().updateCustomerStatus(customer);
      }
  }
  ```

  

### 3.异常层次体系

#### 3.1问题背景

- 使用`JDBC`进行数据访问，当出现问题的时候，`JDBC-API`会抛出`SQLException`表明问题的发生。`SQLException`属于`checked exception`，在`DAO`接口中需要捕捉或抛出错误，达不到统一规范访问的目的，且客户端不需要捕捉异常。

#### 3.2Spring解决方法-将检查异常抛出为运行期异常

1. 在`DAO`中将`SQLException`转换成unchecked exception，并根据数据库的不同，抛出不同的异常。

2. `Spring`自定义异常类型，继承`RuntimeException`异常，提供给`DAO`接口进行捕捉。

   ![](https://javanote.oss-cn-shenzhen.aliyuncs.com/1_统一的异常体系.png)

3. 如果对于特定的数据访问方式来说，以上的异常类型无法描述当前数据访问方式中特定的异常情况，那么可以通过扩展UncategorizedDataAccessException来进一步细化特定的数据访问异常类型。



