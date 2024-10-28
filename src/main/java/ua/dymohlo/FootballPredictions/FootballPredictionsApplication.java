package ua.dymohlo.FootballPredictions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = {
        org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration.class
})
@EnableScheduling
@PropertySource({"application.properties"})
public class FootballPredictionsApplication {

    public static void main(String[] args) {
        SpringApplication.run(FootballPredictionsApplication.class, args);
    }

}
