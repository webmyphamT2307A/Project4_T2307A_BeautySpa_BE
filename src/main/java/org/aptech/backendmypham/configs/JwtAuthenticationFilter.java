package org.aptech.backendmypham.configs;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);  // Loại bỏ tiền tố "Bearer "

            try {
                // Xác thực token
                DecodedJWT decodedJWT = jwtUtil.verifyToken(token);
                String username = decodedJWT.getSubject();

                // Tạo đối tượng Authentication và thiết lập chi tiết yêu cầu
                AbstractAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username, null, null); // Không cần password vì đã xác thực qua JWT
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Cập nhật SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JWTVerificationException e) {
                logger.error("Token không hợp lệ: {}", e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token không hợp lệ");
                return;  // Dừng xử lý tiếp nếu token không hợp lệ
            } catch (Exception e) {
                logger.error("Lỗi xác thực JWT: {}", e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Lỗi xác thực");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

}
