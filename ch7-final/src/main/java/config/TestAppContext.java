package config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailSender;

import user.service.DummyMailSender;
import user.service.TestUserService;
import user.service.UserService;

@Configuration
@Profile("test")
public class TestAppContext {

    @Bean
    public UserService testUserService() {
        return new TestUserService();
    }

    @Bean
    public MailSender mailSender() {
        return new DummyMailSender();
    }
}
