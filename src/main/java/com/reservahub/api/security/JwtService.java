package com.reservahub.api.security;

import com.reservahub.api.model.Usuario;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiracao-minutos}")
    private Long expiracaoMinutos;

    private final Map<String, Date> tokensRevogados = new ConcurrentHashMap<>();

    @Bean
    public SecretKey jwtKey() {
        return new SecretKeySpec(
                jwtSecret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );
    }

    public String gerarToken(Usuario usuario) {
        Date agora = new Date();
        Date expiraEm = new Date(
                agora.getTime() + expiracaoMinutos * 60L * 1000L
        );

        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("nome", usuario.getNome())
                .claim("userId", usuario.getId())
                .issuedAt(agora)
                .expiration(expiraEm)
                .signWith(jwtKey())
                .compact();
    }

    public String extrairEmail(String token) {
        return Jwts.parser()
                .verifyWith(jwtKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validarToken(String token) {
        try {
            removerTokensExpirados();

            if (tokensRevogados.containsKey(token)) {
                return false;
            }

            Jwts.parser()
                    .verifyWith(jwtKey())
                    .build()
                    .parseSignedClaims(token);

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void revogarToken(String token) {
        try {
            Date expiracao = Jwts.parser()
                    .verifyWith(jwtKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();

            tokensRevogados.put(token, expiracao);
        } catch (Exception e) {
            tokensRevogados.put(token, new Date());
        }
    }

    private void removerTokensExpirados() {
        Date agora = new Date();

        tokensRevogados.entrySet().removeIf(
                entry -> entry.getValue().before(agora)
        );
    }
}