package user.config;

import java.io.IOException;

import javax.sql.DataSource;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.transaction.PlatformTransactionManager;

import user.dao.UserDao;
import user.dao.UserDaoJdbc;
import user.service.NameMatchClassMethodPointcut;
import user.service.TransactionAdvice;
import user.service.UserServiceImpl;
import user.sqlservice.JaxbXmlSqlReader;
import user.sqlservice.OxmSqlService;
import user.sqlservice.SqlReader;
import user.sqlservice.SqlRegistry;
import user.sqlservice.SqlService;
import user.sqlservice.updatable.EmbeddedDbSqlRegistry;

@Configuration
public class UserConfig {

//    @Bean
//    public ProxyFactoryBean userService(UserServiceImpl userService, Advisor transactionAdvisor) {
//        ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
//        proxyFactoryBean.setTarget(userService);
//        proxyFactoryBean.addAdvisor(transactionAdvisor);
//        return proxyFactoryBean;
//    }

    @Bean
    public Advisor transactionAdvisor(Pointcut transactionPointcut, Advice transactionAdvice) {
        return new DefaultPointcutAdvisor(transactionPointcut, transactionAdvice);
    }

    @Bean
    public TransactionAdvice transactionAdvice() {
        return new TransactionAdvice(transactionManager());
    }

    @Bean
    public NameMatchMethodPointcut transactionPointcut() {
        NameMatchClassMethodPointcut nameMatchMethodPointcut = new NameMatchClassMethodPointcut();
        nameMatchMethodPointcut.setMappedClassName("*ServiceImpl"); // 클래스 이름 패턴
        nameMatchMethodPointcut.setMappedName("upgrade*"); // 메소드 이름 패턴
        return nameMatchMethodPointcut;
    }

    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        return new DefaultAdvisorAutoProxyCreator();
    }

    @Bean
    public UserServiceImpl userService() throws IOException {
        UserServiceImpl userServiceImpl = new UserServiceImpl(userDao());
//        userServiceImpl.setMailSender(mailSender());
        return userServiceImpl;
    }

    @Bean
    public UserDao userDao() throws IOException {
        UserDaoJdbc userDaoJdbc = new UserDaoJdbc();
        userDaoJdbc.setSqlService(sqlService());
        userDaoJdbc.setJdbcTemplate(dataSource());
        return userDaoJdbc;
    }

    @Bean
    public SqlService sqlService() throws IOException {
        OxmSqlService oxmSqlService = new OxmSqlService();
        oxmSqlService.setUnmarshaller(unmarshaller());
        oxmSqlService.setSqlRegistry(sqlRegistry());
        return oxmSqlService;
    }

    @Bean
    public SqlReader sqlReader() {
        JaxbXmlSqlReader jaxbXmlSqlReader = new JaxbXmlSqlReader();
        jaxbXmlSqlReader.setSqlmapFile("/sqlmap.xml");
        return jaxbXmlSqlReader;
    }

    @Bean
    public SqlRegistry sqlRegistry() {
        EmbeddedDbSqlRegistry embeddedDbSqlRegistry = new EmbeddedDbSqlRegistry();
        embeddedDbSqlRegistry.setDataSource(embeddedDatabase());
        return embeddedDbSqlRegistry;
    }

    @Bean
    public Unmarshaller unmarshaller() {
        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setContextPath("user.sqlservice.jaxb");
        return jaxb2Marshaller;
    }

    @Bean
    public EmbeddedDatabase embeddedDatabase() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.HSQL)
                .addScript("sqlRegistrySchema.sql")
                .build();
    }

    @Bean
    public DataSource dataSource() {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();

        dataSource.setDriverClass(com.mysql.jdbc.Driver.class);
        dataSource.setUrl("jdbc:mysql://localhost:53306/springbook?characterEncoding=UTF-8");
        dataSource.setUsername("spring");
        dataSource.setPassword("book");

        return dataSource;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

//    @Bean
//    public MailSender mailSender() {
//        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
//        mailSender.setHost("mail.server.com");
//        return mailSender;
//    }
}
