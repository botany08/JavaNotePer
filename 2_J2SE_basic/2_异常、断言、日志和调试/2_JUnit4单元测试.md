## 单元测试

### 1.单元测试的必要性

- 帮助理解需求
单元测试应该反映Use Case，把被测单元当成黑盒测试其外部行为。
- 提高实现质量
单元测试不保证程序做正确的事，但能帮助保证程序正确地做事，从而提高实现质量。
- 测试成本低
相比集成测试、验收测试，单元测试所依赖的外部环境少，自动化程度高，时间短，节约了测试成本。
- 反馈速度快
单元测试提供快速反馈，把bug消灭在开发阶段，减少问题流到集成测试、验收测试和用户，降低了软件质量控制的成本。
- 利于重构
由于有单元测试作为回归测试用例，有助于预防在重构过程中引入bug。
- 文档作用
单元测试提供了被测单元的使用场景，起到了使用文档的作用。
- 对设计的反馈
一个模块很难进行单元测试通常是不良设计的信号，单元测试可以反过来指导设计出高内聚、低耦合的模块。

### 2.Junit单元测试规范

JUnit 是一个 Java 编程语言的单元测试框架。JUnit 在测试驱动的开发方面有很重要的发展，是起源于 JUnit 的一个统称为 xUnit 的单元测试框架之一。

- 单元测试类后面加Test。 
- 测试方法使用@Test标注。 
- 测试方法名之前加test。 
- 所有测试方法返回类型必须为void且无参数。 
- 每个测试方法之间相互独立。

### 3.JUnit4常用注解

-  普通注解

  ```java
  /**
  * @Before：每一个测试方法执行前自动调用一次。
  * @After：每一个测试方法执行完自动调用一次。
  * @BeforeClass：所有测试方法执行前执行一次，在测试类还没有实例化就已经被加载，所以用static修饰。
  * @AfterClass：所有测试方法执行完执行一次，在测试类还没有实例化就已经被加载，所以用static修饰。
  **/
  @RunWith(SpringRunner.class)
  @SpringBootTest
  @WebAppConfiguration
  public class TestRoot {
      @Before
      public void init() {
          System.out.println("开始测试-----------------");
      }
      @After
      public void after() {
          System.out.println("测试结束-----------------");
      }
  }
  ```

- @Test注解

  ```java
public class TestExam {
      /**
     * timeout属性,用来指定时间上限,如果超时则抛出异常。
       */
    @Test(timeout = 10)
      public void testTime() throws Exception {
          System.out.println("超时测试");
          int result = 10*2;
          Assert.assertEquals(8,result);
          Thread.sleep(100);
      }
  
      /**
       * expected属性,用来指定期望抛出的异常类型
       */
      @Test(expected = NullPointerException.class)
      public void testException() throws Exception {
          System.out.println("错误测试");
          String str = null;
          str.equals("3");
      }
      
      /**
      * @Ignore注解表示,忽略这个测试方法,不执行
      **/
      @Ignore
      @Test(expected = NullPointerException.class)
      public void testIgnore() throws Exception {
          System.out.println("错误测试");
          String str = null;
          str.equals("3");
      }
  }
  ```

### 4.JUnit4常用断言方法

- assetNull(Object object) 	检查对象是否为空

- assertNotNull(Object object) 	检查对象是否不为空

- assertEquals(long expected, long actual) 	检查long类型的值是否相等

- assertEquals(double expected, double actual, double delta) 	检查指定精度的double值是否相等

- assertFalse(boolean condition) 	检查条件是否为假

- assertTrue(boolean condition) 	检查条件是否为真

- assertSame(Object expected, Object actual) 	检查两个对象引用是否引用同一对象（即对象是否相等）

- assertNotSame(Object unexpected, Object actual)    检查两个对象引用是否不引用统一对象(即对象不等)

- fail(String string)  在没有报告的情况下使测试不通过

  

