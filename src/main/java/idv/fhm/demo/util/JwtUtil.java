package idv.fhm.demo.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.util.*;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class JwtUtil {

    // 建議長一點的金鑰，至少 256 bits
    private static final String SECRET_KEY = "mySuperSecretKeyForJwtTokenThatIsAtLeast32CharsLong!";

    private static final Key KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    /**
     * 產生 JWT Token
     * @param account 帳號（例如 email）
     * @return JWT Token
     */
    public static String generateToken(String account) {
        Instant now = Instant.now();
        String jwt = Jwts.builder()
                .setSubject(account)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(1, ChronoUnit.HOURS))) // token 有效期 1 小時
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
        return jwt;
    }

    /**
     * 驗證並解析 JWT Token
     * @param token JWT token 字串
     * @return 帳號（subject）
     */
    public static String validateToken(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token);
            return claimsJws.getBody().getSubject();
        } catch (JwtException e) {
            System.err.println("❌ Invalid JWT: " + e.getMessage());
            return null;
        }
    }

    // 測試
    public static void main(String[] args) {
        String account = "user@example.com";
        String token = generateToken(account);

        // 驗證 token
        String subject = validateToken(token);
    }
}
