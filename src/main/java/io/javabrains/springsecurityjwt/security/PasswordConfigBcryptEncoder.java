package io.javabrains.springsecurityjwt.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordConfigBcryptEncoder {

    @Bean
    //oznacio kao bean i onda je napravljena instanca ovog PasswordEncoding-a pa cu moci da ga Autowire-ujem gde zelim tj u Config klasi za security
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(10);
    }

}
