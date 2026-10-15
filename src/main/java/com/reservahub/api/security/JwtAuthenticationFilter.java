
package com.reservahub.api.security;

import com.reservahub.api.model.Usuario;
import com.reservahub.api.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UsuarioRepository usuarioRepository
    ) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String caminho = request.getServletPath();
        String metodo = request.getMethod();

        if ("OPTIONS".equalsIgnoreCase(metodo)) {
            return true;
        }

        if ("POST".equalsIgnoreCase(metodo)) {
            return "/api/auth/login".equals(caminho)
                    || "/api/auth/solicitar-reset-senha".equals(caminho)
                    || "/api/auth/validar-codigo-reset-senha".equals(caminho)
                    || "/api/usuarios".equals(caminho);
        }

        return false;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String cabecalhoAuth = request.getHeader("Authorization");

        if (cabecalhoAuth == null
                || !cabecalhoAuth.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = cabecalhoAuth.substring(7).trim();

        if (token.isEmpty() || !jwtService.validarToken(token)) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Token JWT inválido ou expirado."
            );
            return;
        }

        String email = jwtService.extrairEmail(token);

        if (email == null || email.isBlank()) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "O token JWT não contém um e-mail válido."
            );
            return;
        }

        Optional<Usuario> usuarioOptional =
                usuarioRepository.findByEmail(email.trim());

        if (usuarioOptional.isEmpty()) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Usuário não encontrado."
            );
            return;
        }

        Usuario usuario = usuarioOptional.get();
        String perfil = usuario.getPerfil();

        if (!"USER".equals(perfil) && !"ADMIN".equals(perfil)) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Perfil de usuário inválido."
            );
            return;
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        usuario.getEmail(),
                        null,
                        List.of(
                                new SimpleGrantedAuthority("ROLE_" + perfil)
                        )
                );

        SecurityContextHolder.getContext()
                .setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
