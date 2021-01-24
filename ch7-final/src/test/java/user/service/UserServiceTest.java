package user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import user.config.UserConfig;
import user.dao.UserDao;
import user.dao.UserDaoJdbc;
import user.domain.Level;
import user.domain.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;
import static user.service.UserServiceImpl.MIN_LOGCOUNT_FOR_SILVER;
import static user.service.UserServiceImpl.MIN_RECOMMEND_FOR_GOLD;


class UserServiceTest {

    private UserDao userDao;
    private UserService userService;
    private MailSender mailSender;
    private List<User> users;
    private ApplicationContext applicationContext;
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void setUp() {
        applicationContext = new AnnotationConfigApplicationContext(UserConfig.class);
        userService = applicationContext.getBean("userService", UserService.class);
        mailSender = mock(MailSender.class);
        userDao = applicationContext.getBean("userDao", UserDaoJdbc.class);
        transactionManager = applicationContext.getBean("transactionManager", DataSourceTransactionManager.class);
        mailSender = new DummyMailSender();
        users = Arrays.asList(
            new User("1deocks", "덕수", "ds@kiworshop.com", "deocksword", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER - 1, 0),
            new User("2jj", "재주", "jj@kiworshop.com", "jassword", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER, 0),
            new User("3ki", "광일", "ki@kiworshop.com", "jassword", Level.SILVER, 60, MIN_RECOMMEND_FOR_GOLD - 1),
            new User("4harris", "성훈", "sh@kiworshop.com", "seongsword", Level.SILVER, 60, MIN_RECOMMEND_FOR_GOLD),
            new User("5jeongkyo", "정교", "jk@kiworshop.com", "jeongsword", Level.GOLD, 100, Integer.MAX_VALUE)
        );
    }

    @Test
    public void transactionSync() {
        userDao.deleteAll();
        assertThat(userDao.getCount()).isEqualTo(0);

        DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition();
        TransactionStatus txStatus = transactionManager.getTransaction(txDefinition);

        userService.add(users.get(0));
        userService.add(users.get(1));
        assertThat(userDao.getCount()).isEqualTo(2);

        transactionManager.rollback(txStatus);

        assertThat(userDao.getCount()).isEqualTo(0);
    }

    @Test
    public void bean() {
        assertThat(this.userService).isNotNull();
    }

    @Test
    void upgradeLevels() throws SQLException {
        UserServiceImpl userServiceImpl = new UserServiceImpl();

        MockUserDao mockUserDao = new MockUserDao(this.users);
        userServiceImpl.setUserDao(mockUserDao);

        MockMailSender mockMailSender = new MockMailSender();
        userServiceImpl.setMailSender(mockMailSender);

        userServiceImpl.upgradeLevels();

        List<User> updated = mockUserDao.getUpdated();
        assertThat(updated.size()).isEqualTo(2);
        checkUserAndLevel(updated.get(0), "2jj", Level.SILVER);
        checkUserAndLevel(updated.get(1), "4harris", Level.GOLD);

        List<String> request = mockMailSender.getRequests();
        assertThat(request.size()).isEqualTo(2);
        assertThat(request.get(0)).isEqualTo(users.get(1).getEmail());
        assertThat(request.get(1)).isEqualTo(users.get(3).getEmail());
    }

    @Test
    public void mockUpgradeLevels() {
        UserServiceImpl userServiceImpl = new UserServiceImpl();

        UserDao mockUserDao = mock(UserDao.class);
        when(mockUserDao.getAll()).thenReturn(this.users);
        userServiceImpl.setUserDao(mockUserDao);

        MailSender mockMailSender = mock(MailSender.class);
        userServiceImpl.setMailSender(mockMailSender);

        userServiceImpl.upgradeLevels();

        verify(mockUserDao, times(2)).update(any(User.class));
        verify(mockUserDao, times(2)).update(any(User.class));
        verify(mockUserDao).update(users.get(1));
        assertThat(users.get(1).getLevel()).isEqualTo(Level.SILVER);
        verify(mockUserDao).update(users.get(3));
        assertThat(users.get(3).getLevel()).isEqualTo(Level.GOLD);

        ArgumentCaptor<SimpleMailMessage> mailMessageArg = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mockMailSender, times(2)).send(mailMessageArg.capture());
        List<SimpleMailMessage> mailMessages = mailMessageArg.getAllValues();
        assertThat(mailMessages.get(0).getTo()[0]).isEqualTo(users.get(1).getEmail());
        assertThat(mailMessages.get(1).getTo()[0]).isEqualTo(users.get(3).getEmail());
    }

    private void checkUserAndLevel(User updated, String expectedId, Level exptectedLevel) {
        assertThat(updated.getId()).isEqualTo(expectedId);
        assertThat(updated.getLevel()).isEqualTo(exptectedLevel);
    }

    @Test
    void add() {
        userDao.deleteAll();

        User userWithLevel = users.get(4);
        User userWithoutLevel = users.get(0);
        userWithoutLevel.setLevel(null);

        userService.add(userWithLevel);
        userService.add(userWithoutLevel);

        User userWithLevelRead = userDao.get(userWithLevel.getId());
        User userWithoutLevelRead = userDao.get(userWithoutLevel.getId());

        assertThat(userWithLevelRead.getLevel()).isEqualTo(Level.GOLD);
        assertThat(userWithoutLevelRead.getLevel()).isEqualTo(Level.BASIC);
    }

    private void checkLevelUpgraded(User user, boolean upgraded) {
        User userUpdate = userDao.get(user.getId());
        if (upgraded) {
            assertThat(userUpdate.getLevel()).isEqualTo(user.getLevel().nextLevel());
        } else {
            assertThat(userUpdate.getLevel()).isEqualTo(user.getLevel());
        }
    }

    @Test
    @DirtiesContext
    void upgradeAllOrNothing() throws Exception {
        UserServiceImpl testUserService = new TestUserServiceImpl(users.get(1).getId());
        testUserService.setUserDao(this.userDao);
        testUserService.setMailSender(mailSender);

//        ProxyFactoryBean transactionProxyFactoryBean = applicationContext.getBean("&userService", ProxyFactoryBean.class);
//        transactionProxyFactoryBean.setTarget(testUserService);

//        UserService userService = (UserService) transactionProxyFactoryBean.getObject();

        userDao.deleteAll();
        for (User user : users) {
            userDao.add(user);
        }

        try {
            testUserService.upgradeLevels();
            fail("TestUserServiceException expected");
        } catch (TestUserServiceException e) {
            System.out.println(e);
        }

        checkLevelUpgraded(users.get(1), false);
    }


    static class TestUserServiceImpl extends UserServiceImpl {
        private String id = "3ki"; // 3번째 id 값으로 고정

        private TestUserServiceImpl(String id) {
            this.id = id;
        }

        public void upgradeLevel(User user) {
            if (user.getId().equals(this.id))
                throw new TestUserServiceException();
            super.upgradeLevel(user);
        }
    }

    static class TestUserServiceException extends RuntimeException {
    }

    static class MockMailSender implements MailSender {
        private List<String> requests = new ArrayList<String>();

        public List<String> getRequests() {
            return requests;
        }

        @Override
        public void send(SimpleMailMessage simpleMessage) throws MailException {
            requests.add(simpleMessage.getTo()[0]);
        }

        @Override
        public void send(SimpleMailMessage[] simpleMessages) throws MailException {

        }
    }

    static class MockUserDao implements UserDao {
        private List<User> users;
        private List<User> updated = new ArrayList<>();

        public MockUserDao(List<User> users) {
            this.users = users;
        }

        public List<User> getUpdated() {
            return this.updated;
        }

        @Override
        public void update(User user) {
            updated.add(user);
        }

        @Override
        public List<User> getAll() {
            return this.users;
        }

        @Override
        public void add(User user) {
            throw new UnsupportedOperationException();
        }

        @Override
        public User get(String id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteAll() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getCount() {
            throw new UnsupportedOperationException();
        }
    }
}
