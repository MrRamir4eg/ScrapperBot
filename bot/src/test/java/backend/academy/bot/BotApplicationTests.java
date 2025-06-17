package backend.academy.bot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestEnvConfiguration.class)
@SpringBootTest
class BotApplicationTests {

    @Test
    void contextLoads() {}
}
