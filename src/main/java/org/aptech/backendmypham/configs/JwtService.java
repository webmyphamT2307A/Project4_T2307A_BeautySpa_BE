package org.aptech.backendmypham.configs;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.aptech.backendmypham.models.User;
import org.aptech.backendmypham.models.Customer;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    private static final String SECRET_KEY = "your_secret_key"; // Đổi thành key bảo mật
    private static final String ISSUER = "your_issuer"; // Đổi thành tên hệ thống

    // Sinh token cho User
    public String generateTokenForUser(User user) {
        return generateToken(user.getEmail(), "USER");
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
                .withClaim("role", role)
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
}
