package bg.svetozar.tastypizza.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.access-expiration-ms}")
    private long accessExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    private static final String ACCESS =  "access";
    private static final String REFRESH = "refresh";

    private Key getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private String buildToken(Map<String, Object> extraClaims, String subject, long expirationMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Map<String, Object> buildCommonClaims(CustomUserDetails custom, String type) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", custom.getUser().getId());
        claims.put("role", custom.getUser().getRole().name());
        claims.put("ver", custom.getUser().getTokenVersion());
        claims.put("type", type);
        return claims;
    }

    public String generateAccessToken(CustomUserDetails userDetails) {
        return buildToken(
                buildCommonClaims(userDetails, ACCESS),
                userDetails.getUsername(),
                accessExpirationMs
        );
    }

    public String generateRefreshToken(CustomUserDetails userDetails) {
        return buildToken(
                buildCommonClaims(userDetails, REFRESH),
                userDetails.getUsername(),
                refreshExpirationMs
        );
    }

    public boolean isAccessTokenValid(String token, CustomUserDetails userDetails) {
        return isTokenValidInternal(token, userDetails, ACCESS);
    }

    public boolean isRefreshTokenValid(String token, CustomUserDetails userDetails) {
        return isTokenValidInternal(token, userDetails, REFRESH);
    }

    private boolean isTokenValidInternal(String token, CustomUserDetails userDetails, String expectedType) {
        String username;
        Claims claims;
        try {
            claims = extractAllClaims(token);
            username = claims.getSubject();
        } catch (JwtException ex) {
            return false;
        }

        if (!username.equals(userDetails.getUsername())) {
            return false;
        }

        Date expiration = claims.getExpiration();
        if (expiration.before(new Date())) {
            return false;
        }

        String type = claims.get("type", String.class);
        if (!expectedType.equals(type)) {
            return false;
        }

        Integer tokenVersion = claims.get("ver", Integer.class);
        if (tokenVersion == null) {
            return false;
        }

        return tokenVersion == userDetails.getUser().getTokenVersion();
    }
}
