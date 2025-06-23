package org.aptech.backendmypham.configs;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Cấu hình CORS được xử lý ở file khác.
        http
                .csrf(csrf -> csrf.disable())

                // Cấu hình phân quyền cho các HTTP request
                .authorizeHttpRequests(req -> req
                        // Các endpoint CÔNG KHAI, không cần đăng nhập
                        .requestMatchers(
                                "/**"
                        ).permitAll()
                        // Cho phép mọi request GET (xem sản phẩm, dịch vụ, review,...)
                        .requestMatchers(HttpMethod.GET).permitAll()

                        // MỌI REQUEST CÒN LẠI đều yêu cầu phải được xác thực (đã đăng nhập)
                        .anyRequest().authenticated()
                )

                // Cấu hình session stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Thêm bộ lọc JWT vào đúng vị trí
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}