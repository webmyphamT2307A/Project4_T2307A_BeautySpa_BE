package org.aptech.backendmypham.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.
                csrf().disable() //todo: sau này thêm phân quyền, thêm xác thực qua jwt
                .authorizeHttpRequests(requests -> requests
                        .anyRequest().permitAll());

        return http.build();
    }

    @Bean
    //bean mã hóa password
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
