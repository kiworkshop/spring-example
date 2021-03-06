package user.config;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.transaction.PlatformTransactionManager;
import user.dao.UserDao;
import user.dao.UserDaoJdbc;
import user.service.TransactionAdvice;
import user.service.UserServiceImpl;

import javax.sql.DataSource;

@Configuration
public class UserConfig {

    @Bean
    public ProxyFactoryBean userService(UserServiceImpl userService, Advisor transactionAdvisor) {
        ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
        proxyFactoryBean.setTarget(userService);
        proxyFactoryBean.addAdvisor(transactionAdvisor);
        return proxyFactoryBean;
    }

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
        NameMatchMethodPointcut nameMatchMethodPointcut = new NameMatchMethodPointcut();
        nameMatchMethodPointcut.setMappedName("upgrade*");
        return nameMatchMethodPointcut;
    }

    @Bean
    public UserServiceImpl userServiceImpl() {
        UserServiceImpl userServiceImpl = new UserServiceImpl(userDao());
//        userServiceImpl.setMailSender(mailSender());
        return userServiceImpl;
    }

    @Bean
    public UserDao userDao() {
        return new UserDaoJdbc(dataSource());
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
