
package com.reservahub.api.security;

import com.reservahub.api.model.Usuario;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class JwtService {

    private final SecretKey jwtKey;
    private final long expiracaoMinutos;

    private final Map<String, Date> tokensRevogados =
            new ConcurrentHashMap<>();

    public JwtService(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.expiracao-minutos:60}") long expiracaoMinutos
    ) {
        if (expiracaoMinutos <= 0) {
            throw new IllegalArgumentException(
                    "A expiração do JWT deve ser maior que zero."
            );
        }

        try {
            byte[] chaveDecodificada = Decoders.BASE64.decode(jwtSecret);

            if (chaveDecodificada.length < 32) {
                throw new IllegalStateException(
                        "JWT_SECRET inválida. A chave deve possuir "
                        + "pelo menos 32 bytes após a decodificação Base64."
                );
            }

            this.jwtKey = Keys.hmacShaKeyFor(chaveDecodificada);

        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "JWT_SECRET inválida. Configure uma chave Base64 válida.",
                    e
            );
        }

        this.expiracaoMinutos = expiracaoMinutos;
    }

    public String gerarToken(Usuario usuario) {
        Instant agora = Instant.now();
        Instant expiraEm = agora.plus(
                expiracaoMinutos,
                ChronoUnit.MINUTES
        );

        return Jwts.builder()
                .subject(usuario.getEmail())
                .claim("nome", usuario.getNome())
                .claim("userId", usuario.getId())
                .issuedAt(Date.from(agora))
                .expiration(Date.from(expiraEm))
                .signWith(jwtKey)
                .compact();
    }

    public String extrairEmail(String token) {
        return Jwts.parser()
                .verifyWith(jwtKey)
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
                    .verifyWith(jwtKey)
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
                    .verifyWith(jwtKey)
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
