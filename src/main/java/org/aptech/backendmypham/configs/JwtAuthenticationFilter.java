package org.aptech.backendmypham.configs;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    // Constructor đã được cập nhật
    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // Loại bỏ tiền tố "Bearer "

            try {
                // Xác thực token
                DecodedJWT decodedJWT = jwtService.verifyToken(token);
                String username = decodedJWT.getSubject(); // username ở đây là email
                logger.info("Token hợp lệ cho user: {}", username);

                // --- PHẦN SỬA LỖI QUAN TRỌNG ---

                // 1. Tải lại đầy đủ thông tin UserDetails từ DB dựa vào username (email)
                // Đây là bước quan trọng nhất để lấy được đối tượng CustomUserDetails
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // 2. Tạo đối tượng Authentication với `userDetails` làm Principal
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, // <-- QUAN TRỌNG: Principal là đối tượng UserDetails, không phải String
                        null,
                        userDetails.getAuthorities() // Lấy quyền từ UserDetails
                );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 3. Set đối tượng Authentication vào SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (JWTVerificationException e) {
                logger.error("Token không hợp lệ: {}", e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token không hợp lệ hoặc đã hết hạn");
                return; // Dừng lại ngay khi token không hợp lệ
            } catch (Exception e) {
                logger.error("Lỗi trong quá trình xác thực: {}", e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Lỗi xác thực");
                return; // Dừng lại khi có lỗi khác
            }
        }

        // Tiếp tục chuỗi filter
        filterChain.doFilter(request, response);
    }
}