package com.wjh.interviewbacked;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.wjh.interviewbacked.mapper")
public class InterViewBackedApplication {

    public static void main(String[] args) {
        SpringApplication.run(InterViewBackedApplication.class, args);
    }

}
