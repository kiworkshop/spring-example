package user.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {
    String id;
    String name;
    String password;
    Level level;
    int login;
    int recommend;

    public User() {
    }

    public User(String id, String name, String password, Level level, int login, int recommend) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.level = level;
        this.login = login;
        this.recommend = recommend;
    }
}
