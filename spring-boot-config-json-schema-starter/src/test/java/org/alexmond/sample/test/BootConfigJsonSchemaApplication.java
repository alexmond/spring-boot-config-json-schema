package org.alexmond.sample.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.test.context.ActiveProfiles;

@SpringBootApplication
@ActiveProfiles("test")
public class BootConfigJsonSchemaApplication {

    public static void main(String[] args) {
        SpringApplication.run(BootConfigJsonSchemaApplication.class, args);
    }

}
