package prashant.springboot.pdf.generator.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@SpringBootApplication
@ComponentScan(basePackages = "prashant.springboot.pdf.generator")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
