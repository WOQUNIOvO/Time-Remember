package com.timeremember;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.timeremember.mapper")
@SpringBootApplication
public class TimeRememberApplication {

    public static void main(String[] args) {
        SpringApplication.run(TimeRememberApplication.class, args);
    }
}
