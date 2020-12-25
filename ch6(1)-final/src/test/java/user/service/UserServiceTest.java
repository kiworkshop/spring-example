package user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.transaction.PlatformTransactionManager;
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
import static org.assertj.core.api.Assertions.fail;
import static user.service.UserServiceImpl.MIN_LOGCOUNT_FOR_SILVER;
import static user.service.UserServiceImpl.MIN_RECOMMEND_FOR_GOLD;


class UserServiceTest {

    private UserDao userDao;
    private UserServiceImpl userServiceImpl;
    private PlatformTransactionManager transactionManager;
    private MailSender mailSender;
    private List<User> users;

    @BeforeEach
    void setUp() {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(UserConfig.class);
        userServiceImpl = applicationContext.getBean("userServiceImpl", UserServiceImpl.class);
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
    public void bean() {
        assertThat(this.userServiceImpl).isNotNull();
    }

    @Test
    void upgradeLevels() throws SQLException {
        userDao.deleteAll();
        for (User user : users) {
            userDao.add(user);
        }
        MockMailSender mockMailSender = new MockMailSender();
        userServiceImpl.setMailSender(mockMailSender);

        userServiceImpl.upgradeLevels();

        checkLevelUpgraded(users.get(0), false);
        checkLevelUpgraded(users.get(1), true);
        checkLevelUpgraded(users.get(2), false);
        checkLevelUpgraded(users.get(3), true);
        checkLevelUpgraded(users.get(4), false);

        List<String> request = mockMailSender.getRequests();
        assertThat(request.size()).isEqualTo(2);
        assertThat(request.get(0)).isEqualTo(users.get(1).getEmail());
        assertThat(request.get(1)).isEqualTo(users.get(3).getEmail());
    }

    @Test
    void add() {
        userDao.deleteAll();

        User userWithLevel = users.get(4);
        User userWithoutLevel = users.get(0);
        userWithoutLevel.setLevel(null);

        userServiceImpl.add(userWithLevel);
        userServiceImpl.add(userWithoutLevel);

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
    void upgradeAllOrNothing() throws SQLException {
        UserServiceImpl testUserService = new TestUserService(users.get(3).getId());
        testUserService.setUserDao(this.userDao);
        testUserService.setMailSender(mailSender);

        UserServiceTx txUserService = new UserServiceTx();
        txUserService.setTransactionManager(transactionManager);
        txUserService.setUserService(testUserService);

        userDao.deleteAll();
        for (User user : users) {
            userDao.add(user);
        }

        try {
            txUserService.upgradeLevels();
            fail("TestUserServiceException expected");
        } catch (TestUserServiceException e) {
            System.out.println(e);
        }
        checkLevelUpgraded(users.get(1), false);
    }

    static class TestUserService extends UserServiceImpl {
        private String id;

        private TestUserService(String id) {
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

    static class MockMailSender implements MailSender{
        private List<String> requests = new ArrayList<String>();
        public List<String> getRequests(){
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
}
