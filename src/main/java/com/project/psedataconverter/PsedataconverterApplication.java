package com.project.psedataconverter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PsedataconverterApplication {

    public static void main(String[] args) {

        ConfigurableApplicationContext context = SpringApplication.run(PsedataconverterApplication.class, args);
//        context.getBean(Manager.class).startTask();
    }

}
