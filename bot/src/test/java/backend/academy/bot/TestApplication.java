package backend.academy.bot;

import org.springframework.boot.SpringApplication;

public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.from(BotApplication::main)
                .with(TestEnvConfiguration.class)
                .run(args);
    }
}
