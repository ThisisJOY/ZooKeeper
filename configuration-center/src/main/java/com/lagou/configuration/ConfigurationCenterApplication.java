package com.lagou.configuration;

import com.lagou.configuration.utils.CustomDataSource;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.Connection;
import java.sql.PreparedStatement;

@SpringBootApplication
public class ConfigurationCenterApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(ConfigurationCenterApplication.class, args);
        CustomDataSource.init();
        Connection connection = CustomDataSource.getConnection();
        System.out.println(connection);
    }

}
