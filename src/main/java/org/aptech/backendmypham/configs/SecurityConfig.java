package org.aptech.backendmypham.configs;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor // Dùng Lombok để tự tạo constructor
public class SecurityConfig {

    // Spring sẽ tự động inject bean JwtAuthenticationFilter đã được tạo ở file khác
    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // BƯỚC 1: Cấu hình CORS (Nên đặt ở đây thay vì WebConfig)
                .cors(cors -> {
                    CorsConfiguration configuration = new CorsConfiguration();
                    configuration.setAllowedOrigins(Arrays.asList("http://localhost:3001", "http://localhost:3002", "http://localhost:3003"));
                    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                    configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
                    configuration.setAllowCredentials(true);
                    cors.configurationSource(request -> configuration);
                })

                // BƯỚC 2: Tắt CSRF vì dùng JWT
                .csrf(csrf -> csrf.disable())

                // BƯỚC 3: Cấu hình phân quyền cho các endpoint
                .authorizeHttpRequests(req -> req
                        // Các endpoint công khai, không cần đăng nhập
                        .requestMatchers(
                                "/api/v1/auth/**", // Endpoint đăng nhập/đăng ký
                                "/uploads/**"      // Cho phép truy cập ảnh public
                        ).permitAll()
                        // Cho phép xem (GET) dịch vụ, sản phẩm,... mà không cần đăng nhập
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/services/**",
                                "/api/v1/products/**",
                                "/api/v1/reviews/item/**"
                        ).permitAll()

                        // Endpoint tạo review (POST) yêu cầu phải ĐĂNG NHẬP
                        .requestMatchers(HttpMethod.POST, "/api/v1/reviews").authenticated()
                        // Endpoint sửa/xóa review (PUT/DELETE) yêu cầu phải ĐĂNG NHẬP
                        .requestMatchers(HttpMethod.PUT, "/api/v1/reviews/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/reviews/**").authenticated()

                        // Các request đến /admin/** yêu cầu quyền ADMIN
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Bất kỳ request nào khác chưa được định nghĩa ở trên đều yêu cầu phải ĐĂNG NHẬP
                        .anyRequest().authenticated()
                )

                // BƯỚC 4: Cấu hình session stateless
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // BƯỚC 5 (QUAN TRỌNG NHẤT): Thêm JWT filter vào đúng vị trí
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}