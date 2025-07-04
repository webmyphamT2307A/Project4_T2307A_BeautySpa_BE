package org.aptech.backendmypham.configs;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.aptech.backendmypham.models.User;
import org.aptech.backendmypham.models.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String SECRET_KEY = "ban_mat_bao_mat_cua_ban";
    private static final String ISSUER = "my_beauty_spa";

    public String generateTokenForUser(User user) {
        return generateToken(user.getEmail(), user.getRole().getName()); // Lấy vai trò thực tế từ DB
    }

    public String generateTokenForAdmin(User user) {
        return generateToken(user.getEmail(), user.getRole().getName()); // Lấy vai trò thực tế từ DB
    }
    // Sinh token cho Customer
    public String generateTokenForCustomer(Customer customer) {
        return generateToken(customer.getEmail(), "CUSTOMER");
    }

    // Tạo token chung
    private String generateToken(String email, String role) {
        return JWT.create()
                .withSubject(email)
                .withIssuer(ISSUER)
                .withClaim("role", "ROLE_" + role.toUpperCase())
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 1 ngày
                .sign(Algorithm.HMAC256(SECRET_KEY));
    }

    // Xác thực token
    public DecodedJWT verifyToken(String token) {
        return JWT.require(Algorithm.HMAC256(SECRET_KEY))
                .withIssuer(ISSUER)
                .build()
                .verify(token);
    }

    // Lấy email từ token
    public String getSubjectFromToken(String token) {
        return verifyToken(token).getSubject();
    }

    // Lấy role từ token
    public String getRoleFromToken(String token) {
        return verifyToken(token).getClaim("role").asString();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            // Lấy username (email) từ trong token
            final String username = getSubjectFromToken(token);
            // 1. So sánh username trong token với username từ UserDetails.
            // 2. Hàm getSubjectFromToken đã gọi verifyToken(), tự động kiểm tra token có hết hạn hay không.
            // Nếu token hết hạn, nó sẽ ném ra exception và bị bắt ở khối catch.
            return username.equals(userDetails.getUsername());
        } catch (JWTVerificationException e) {
            // Nếu có bất kỳ lỗi nào trong quá trình xác thực (hết hạn, sai chữ ký,...)
            // thì token không hợp lệ.
            logger.info("Kiểm tra isTokenValid thất bại: {}", e.getMessage());
            return false;
        }
    }
}
