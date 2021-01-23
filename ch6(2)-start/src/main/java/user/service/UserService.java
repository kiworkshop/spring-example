package user.service;

import user.domain.User;

import java.sql.SQLException;

public interface UserService {
    void add(User user);
    void upgradeLevels() throws SQLException;
}
