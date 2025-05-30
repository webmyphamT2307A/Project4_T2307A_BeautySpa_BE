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

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = request.getHeader("Authorization");

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);  // Loại bỏ tiền tố "Bearer "

            try {
                logger.info("Token nhận được: {}", token);
                DecodedJWT decodedJWT = jwtService.verifyToken(token);
                String username = decodedJWT.getSubject();
                String role = decodedJWT.getClaim("role").asString();
                logger.info("Token hợp lệ. User: {}, Role: {}", username, role);

                AbstractAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        username, null, null);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (JWTVerificationException e) {
                logger.error("Token không hợp lệ: {}", e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token không hợp lệ");
                return;
            } catch (Exception e) {
                logger.error("Lỗi xác thực JWT: {}", e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Lỗi xác thực");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

}
