package org.aptech.backendmypham.configs;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aptech.backendmypham.configs.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

/**
 * Bộ lọc này chặn mỗi request một lần để kiểm tra JWT token.
 * Nếu token hợp lệ, nó sẽ thiết lập thông tin xác thực trong SecurityContext.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    // Sử dụng constructor để Spring tự động inject các dependency
    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Nếu không có header 'Authorization' hoặc không bắt đầu bằng "Bearer ",
        // thì chuyển request cho bộ lọc tiếp theo và không làm gì cả.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Trích xuất JWT từ header (bỏ đi "Bearer ")
        final String token = authHeader.substring(7);

        try {
            // Giải mã và xác thực token
            DecodedJWT decodedJWT = jwtService.verifyToken(token);
            String username = decodedJWT.getSubject(); // Lấy email/username từ 'sub' claim

            // Nếu có username và người dùng chưa được xác thực trong session hiện tại
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                logger.info("Token hợp lệ cho user '{}'. Bắt đầu quá trình xác thực...", username);

                String role = decodedJWT.getClaim("role").asString();

                // === LOGIC ĐÃ SỬA LẠI HOÀN CHỈNH ===
                // Logic này sẽ xử lý tất cả các vai trò, không chỉ Customer
                if ("ROLE_CUSTOMER".equals(role)) {
                    logger.info("Phát hiện vai trò 'ROLE_CUSTOMER'. Đang xử lý cho khách hàng...");

                    // Tải thông tin chi tiết người dùng từ database
                    UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                    // Thêm một bước kiểm tra token với userDetails để tăng cường bảo mật
                    if (jwtService.isTokenValid(token, userDetails)) {
                        // Nếu mọi thứ hợp lệ, tạo đối tượng Authentication
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails,      // Principal bây giờ là đối tượng UserDetails
                                null,             // Không cần credentials
                                userDetails.getAuthorities() // Lấy quyền từ UserDetails
                        );
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        logger.info("===> Xác thực thành công cho Customer: {}", username);
                    } else {
                        logger.warn("Token được giải mã nhưng không hợp lệ khi so sánh với UserDetails cho user '{}'", username);
                    }
                } else if ("ROLE_ADMIN".equals(role) || "ROLE_STAFF".equals(role)) {
                    logger.info("Phát hiện vai trò '{}'. Đang xử lý cho admin/staff...", role);

                    // Đối với ADMIN và STAFF, chúng ta sử dụng cách tiếp cận đơn giản hơn
                    // Tạo một danh sách authorities dựa trên role từ JWT
                    Collection<GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority(role));

                    // Tạo Authentication với username làm principal (String)
                    // Điều này phù hợp với logic trong Controller khi cast thành String
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            username,         // Principal là username (String) - phù hợp với logic trong Controller
                            null,             // Không cần credentials
                            authorities       // Quyền từ JWT
                    );
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.info("===> Xác thực thành công cho {}: {}", role, username);
                } else {
                    // Xử lý các vai trò khác nếu có, hoặc log cảnh báo
                    logger.warn("Token có vai trò '{}' không được hỗ trợ. Bỏ qua logic xác thực chi tiết.", role);
                }
            }
        } catch (JWTVerificationException e) {
            // Nếu token không thể giải mã hoặc không hợp lệ (sai chữ ký, hết hạn,...)
            logger.error("Xác thực token thất bại: {}", e.getMessage());
        }

        // Chuyển request và response cho bộ lọc tiếp theo trong chuỗi filter
        filterChain.doFilter(request, response);
    }
}