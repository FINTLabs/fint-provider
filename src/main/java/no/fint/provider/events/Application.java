package no.fint.provider.events;

import no.fint.audit.EnableFintAudit;
import no.fint.events.annotations.EnableFintEvents;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableFintAudit
@EnableFintEvents
@EnableScheduling
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
