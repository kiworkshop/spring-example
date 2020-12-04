package user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import user.dao.DaoFactory;
import user.dao.UserDao;
import user.dao.UserDaoJdbc;
import user.domain.Level;
import user.domain.User;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


class UserServiceTest {

    private UserDao userDao;
    private UserService userService;
    private List<User> users;

    @BeforeEach
    void setUp() {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(DaoFactory.class);
        userService = applicationContext.getBean("userService", UserService.class);
        userDao = applicationContext.getBean("userDao", UserDaoJdbc.class);
        users = Arrays.asList(
                new User("deocks", "덕수", "deocksword", Level.BASIC, 49, 0),
                new User("jj", "재주", "jassword", Level.BASIC, 50, 0),
                new User("ki", "광일", "jassword", Level.SILVER, 60, 29),
                new User("harris", "성훈", "seongsword", Level.SILVER, 60, 30),
                new User("jeongkyo", "정교", "jeongsword", Level.GOLD, 100, 100)
        );
    }

    @Test
    public void bean() {
        assertThat(this.userService).isNotNull();
    }

    @Test
    void upgradeLevels() {
        userDao.deleteAll();
        for (User user : users) {
            userDao.add(user);
        }

        userService.upgradeLevels();

        checkLevel(users.get(0), Level.BASIC);
        checkLevel(users.get(1), Level.SILVER);
        checkLevel(users.get(2), Level.SILVER);
        checkLevel(users.get(3), Level.GOLD);
        checkLevel(users.get(4), Level.GOLD);
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

    private void checkLevel(User user, Level expectedLevel) {
        User userUpdate = userDao.get(user.getId());
        assertThat(userUpdate.getLevel()).isEqualTo(expectedLevel);
    }
}