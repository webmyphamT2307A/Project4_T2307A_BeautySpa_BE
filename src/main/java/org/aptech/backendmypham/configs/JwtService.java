package org.aptech.backendmypham.configs;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.aptech.backendmypham.models.User;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JwtService {

    private static final String SECRET_KEY = "your_secret_key"; // Cần thay đổi key này để bảo mật hơn
    private static final String ISSUER = "your_issuer"; // Cần thiết lập một Issuer

    // Sinh JWT token
    public String generateToken(User user) {
        return JWT.create()
                .withSubject(user.getEmail())
                .withIssuer(ISSUER)
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // Hết hạn sau 1 ngày
                .sign(Algorithm.HMAC256(SECRET_KEY));
    }

    // Xác thực token
    public DecodedJWT verifyToken(String token) {
        return JWT.require(Algorithm.HMAC256(SECRET_KEY))
                .withIssuer(ISSUER)
                .build()
                .verify(token);
    }

    // Lấy thông tin từ token
    public String getSubjectFromToken(String token) {
        return verifyToken(token).getSubject();
    }
}
