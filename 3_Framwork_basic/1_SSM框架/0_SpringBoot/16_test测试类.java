//注解作用：本质是一个运行器，SpringRunner表示运行在Spring的测试环境中
//@RunWith(JUnit4.class)就是指用JUnit4来运行
//@RunWith(Suite.class)的话就是一套测试集合
@RunWith(SpringRunner.class)
//注解作用：配置文件属性的读取，允许使用properties属性定义自定义环境属性。
@SpringBootTest(properties = "application.yml")
//表示SpringBoot的启动类，可以自定义扫描的包
@SpringBootApplication(scanBasePackages = {
        "com.tcl.multimedia.nretail.central.ability.product",
        "com.tcl.multimedia.nretail.crm.ejb.boot"})
public class PdmSynServiceTest {

    @Autowired
    private PdmSynService pdmSynService;

    //@Test表示一个测试方法
    @Test
    //@Rollback作用：表明被注解方法的事务在完成后是否需要被回滚。 
    //如果true，事务将被回滚，否则事务将被提交。 使用@Rollback接口来在类级别覆写配置的默认回滚标志。
    @Rollback(value = false)
    public void testInit() {
        pdmSynService.initSeries();
        pdmSynService.initPdmAttr();
    }

    @Test
    @Rollback(value = false)
    public void testSyn() {
        pdmSynService.synFromCrm(null);
    }

}