package user.dao;

import static org.assertj.core.api.Assertions.*;

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import config.AppContext;
import user.TestAppContext;
import user.domain.Level;
import user.domain.User;

// Junit5는 spring 5.x버전 이상의 SpringExtension을 사용하므로 이후 코드와 호환성을 생각하여 우선 Junit4로 테스트를 변경함
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestAppContext.class, AppContext.class})
public class UserDaoTest {

    @Autowired
    private UserDao userDao;

    @Autowired
    private DataSource dataSource;
    private User user1;
    private User user2;
    private User user3;

    @Before
    public void setUp() {
        user1 = new User("deocks", "덕수", "ds@kiworkshop.com", "deocksword", Level.BASIC, 1, 0);
        user2 = new User("jj", "재주", "jj@kiworkshop.com", "jassword", Level.SILVER, 55, 10);
        user3 = new User("ki", "광일", "ki@kiworkshop.com", "jassword", Level.GOLD, 100, 40);
    }

    @After
    public void tearDown() {
        userDao.deleteAll();
    }

    @Test
    public void 새로운_User를_추가한다() {

        userDao.add(user1);
        userDao.add(user2);
        assertThat(userDao.getCount()).isEqualTo(2);

        User userGet1 = userDao.get(user1.getId());
        assertThat(userGet1.getName()).isEqualTo((user1.getName()));
        assertThat(userGet1.getPassword()).isEqualTo(user1.getPassword());

        User userGet2 = userDao.get(user2.getId());
        assertThat(userGet2.getName()).isEqualTo((user2.getName()));
        assertThat(userGet2.getPassword()).isEqualTo(user2.getPassword());
    }

    @Test
    public void 존재하지않는_Userid로_조회시_예외발생() {
        userDao.deleteAll();
        assertThat(userDao.getCount()).isEqualTo(0);

        assertThatThrownBy(() -> userDao.get("unknwon_id"))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }

    @Test
    public void count() {
        userDao.deleteAll();
        assertThat(userDao.getCount()).isEqualTo(0);
        userDao.add(user1);
        assertThat(userDao.getCount()).isEqualTo(1);
        userDao.add(user2);
        assertThat(userDao.getCount()).isEqualTo(2);
        userDao.add(user3);
        assertThat(userDao.getCount()).isEqualTo(3);
    }

    @Test
    public void getAll() {
        userDao.deleteAll();
        List<User> users = userDao.getAll();
        assertThat(users).size().isEqualTo(0);

        userDao.add(user1);
        List<User> users1 = userDao.getAll();
        assertThat(users1).size().isEqualTo(1);
        checkSameUser(user1, users1.get(0));

        userDao.add(user2);
        List<User> users2 = userDao.getAll();
        assertThat(users2).size().isEqualTo(2);
        checkSameUser(user1, users2.get(0));
        checkSameUser(user2, users2.get(1));

        userDao.add(user3);
        List<User> users3 = userDao.getAll();
        assertThat(users3).size().isEqualTo(3);
        checkSameUser(user1, users3.get(0));
        checkSameUser(user2, users3.get(1));
        checkSameUser(user3, users3.get(2));

        userDao.deleteAll();
        List<User> users0 = userDao.getAll();
        assertThat(users0).size().isEqualTo(0);
    }

    @Test
    public void update() {
        userDao.deleteAll();

        userDao.add(user1);
        userDao.add(user2);

        user1.setName("이성훈");
        user1.setPassword("seongsword");
        user1.setLevel(Level.GOLD);
        user1.setLogin(1000);
        user1.setRecommend(999);
        userDao.update(user1);

        User user1update = userDao.get(user1.getId());
        checkSameUser(user1, user1update);
        User user2same = userDao.get(user2.getId());
        checkSameUser(user2, user2same);
    }

    @Test
    public void 같은_id의_사용자를_등록하면_예외발생() {
        userDao.add(user1);

        assertThatThrownBy(() -> userDao.add(user1))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    public void sqlExceptionTranslate_학습테스트() {
        try {
            userDao.add(user1);
            userDao.add(user1);
        } catch (DuplicateKeyException exception) {
            SQLException sqlException = (SQLException) exception.getRootCause();
            SQLExceptionTranslator set = new SQLErrorCodeSQLExceptionTranslator(this.dataSource);

            assertThat(set.translate(null, null, sqlException))
                    .isInstanceOf(DuplicateKeyException.class);
        }
    }

    private void checkSameUser(User user1, User user2) {
        assertThat(user1.getId()).isEqualTo(user2.getId());
        assertThat(user1.getName()).isEqualTo(user2.getName());
        assertThat(user1.getPassword()).isEqualTo(user2.getPassword());
        assertThat(user1.getLevel()).isEqualTo(user2.getLevel());
        assertThat(user1.getLogin()).isEqualTo(user2.getLogin());
        assertThat(user1.getRecommend()).isEqualTo(user2.getRecommend());
    }
}
