package user.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import user.domain.Level;
import user.domain.User;

import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import javax.sql.DataSource;

class UserDaoTest {
    private UserDao userDao;
    private DataSource dataSource;
    private User user1;
    private User user2;
    private User user3;

    @BeforeEach
    void setUp() {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(DaoFactory.class);
        userDao = applicationContext.getBean("userDao", UserDaoJdbc.class);
        dataSource = applicationContext.getBean("dataSource", DataSource.class);
        user1 = new User("deocks", "덕수", "ds@kiworkshop.com", "deocksword", Level.BASIC, 1, 0);
        user2 = new User("jj", "재주", "jj@kiworkshop.com", "jassword", Level.SILVER, 55, 10);
        user3 = new User("ki", "광일", "ki@kiworkshop.com", "jassword", Level.GOLD, 100, 40);
    }

    @AfterEach
    void tearDown() {
        userDao.deleteAll();
    }

    @Test
    @DisplayName("새로운 User를 추가한다.")
    void add() throws SQLException {

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
    @DisplayName("존재하지 않는 User id로 조회시 예외가 발생한다.")
    void getUserFailure() throws SQLException {
        userDao.deleteAll();
        assertThat(userDao.getCount()).isEqualTo(0);

        assertThatThrownBy(() -> userDao.get("unknwon_id"))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }

    @Test
    @DisplayName("총 User가 몇 명인지 조회한다.")
    void count() throws SQLException {
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
    @DisplayName("같은 id의 사용자를 등록하면 예외 발생")
    void duplicateKey() {
        userDao.add(user1);

        assertThatThrownBy(() -> userDao.add(user1))
                .isInstanceOf(DuplicateKeyException.class);
    }

    @Test
    @DisplayName("SQLException 전환 기능의 학습 테스트")
    void sqlExceptionTranslate() {
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
