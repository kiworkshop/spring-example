package user.config;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.castor.CastorMarshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.transaction.PlatformTransactionManager;
import org.xml.sax.InputSource;
import user.dao.UserDao;
import user.dao.UserDaoJdbc;
import user.domain.User;
import user.service.NameMatchClassMethodPointcut;
import user.service.TransactionAdvice;
import user.service.UserServiceImpl;
import user.sqlservice.*;
import user.sqlservice.updatable.ConcurrentHashMapSqlRegistry;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;

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
        UserDaoJdbc userDaoJdbc = new UserDaoJdbc(dataSource());
        userDaoJdbc.setSqlService(sqlService());
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
        return new ConcurrentHashMapSqlRegistry();
    }

    @Bean
    public Unmarshaller unmarshaller() {
        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setContextPath("user.sqlservice.jaxb");
        return jaxb2Marshaller;
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
