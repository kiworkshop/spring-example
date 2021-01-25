package user.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import user.domain.Level;
import user.domain.User;
import user.sqlservice.SqlService;

import javax.sql.DataSource;
import java.util.List;

public class UserDaoJdbc implements UserDao {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<User> userMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getString("id"));
        user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email"));
        user.setPassword(rs.getString("password"));
        user.setLevel(Level.valueOf(rs.getInt("level")));
        user.setLogin(rs.getInt("login"));
        user.setRecommend(rs.getInt("recommend"));
        return user;
    };
    private SqlService sqlService;

    public UserDaoJdbc(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void setSqlService(SqlService sqlService) {
        this.sqlService = sqlService;
    }

    public void add(User user) {
        this.jdbcTemplate.update(this.sqlService.getSql("userAdd"),
                user.getId(), user.getName(), user.getPassword(), user.getEmail(),
                user.getLevel().intValue(), user.getLogin(), user.getRecommend());

    }

    public User get(String id) {
        return this.jdbcTemplate.queryForObject(this.sqlService.getSql("userGet"),
                new Object[]{id}, this.userMapper);
    }

    public List<User> getAll() {
        return this.jdbcTemplate.query(this.sqlService.getSql("userGetAll"), this.userMapper);
    }

    public void deleteAll() {
        this.jdbcTemplate.update(this.sqlService.getSql("userDeleteAll"));
    }

    public int getCount() {
        return this.jdbcTemplate.queryForInt(this.sqlService.getSql("userGetCount"));
    }

    public void update(User user) {
        this.jdbcTemplate.update(this.sqlService.getSql("userUpdate"),
                user.getName(), user.getPassword(), user.getEmail(),
                user.getLevel().intValue(), user.getLogin(), user.getRecommend(),
                user.getId()
        );
    }
}
