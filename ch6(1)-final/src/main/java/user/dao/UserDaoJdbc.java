package user.dao;

import java.sql.PreparedStatement;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import user.domain.Level;
import user.domain.User;

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

    public UserDaoJdbc(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void add(User user) {
        jdbcTemplate.update(
                "insert into users(id, name, email, password, level, login, recommend) values(?,?,?,?,?,?,?)",
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPassword(),
                user.getLevel().intValue(),
                user.getLogin(),
                user.getRecommend());
    }

    public User get(String id) {
        return this.jdbcTemplate.queryForObject("select * from users where id = ?",
                new Object[]{id}, userMapper);
    }

    public List<User> getAll() {
        return this.jdbcTemplate.query("select * from users order by id", userMapper);
    }

    public void update(User user) {
        this.jdbcTemplate.update(
                "update users set name = ?, email = ?, password = ?, level = ?, login = ?, "
                        + "recommend = ? where id = ?", user.getName(), user.getEmail(), user.getPassword(), user.getLevel().intValue(), user.getLogin(), user.getRecommend(), user.getId()
        );
    }

    public void deleteAll() {
        jdbcTemplate.update(connection -> connection.prepareStatement("delete from users"));
    }

    public int getCount() {
        return this.jdbcTemplate.queryForInt("select count(*) from users");
    }
}
