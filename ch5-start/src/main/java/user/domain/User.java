package user.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {
    private String id;
    private String name;
    private String email;
    private String password;
    private Level level;
    private int login;
    private int recommend;

    public User() {
    }

    public User(String id, String name, String email, String password, Level level, int login, int recommend) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.level = level;
        this.login = login;
        this.recommend = recommend;
    }

    public void upgradeLevel() {
        Level nextLevel = this.level.nextLevel();
        if (nextLevel == null) {
            throw new IllegalStateException(this.level + "은 업그레이드가 불가능합니다.");
        } else {
            this.level = nextLevel;
        }
    }
}
