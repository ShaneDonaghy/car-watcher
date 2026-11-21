package ie.shep.carwatchdog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CarWatchdogApplication {
    public static void main(String[] args) {
        SpringApplication.run(CarWatchdogApplication.class, args);
    }
}
