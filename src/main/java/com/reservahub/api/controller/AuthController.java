package com.reservahub.api.controller;

import com.reservahub.api.dto.LoginRequest;
import com.reservahub.api.dto.LoginResposta;
import com.reservahub.api.dto.UsuarioCadastro;
import com.reservahub.api.model.Usuario;
import com.reservahub.api.security.JwtService;
import com.reservahub.api.security.UsuarioAutenticado;
import com.reservahub.api.service.LogoutService;
import com.reservahub.api.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;
    private final LogoutService logoutService;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UsuarioService usuarioService,
            LogoutService logoutService) {

        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioService = usuarioService;
        this.logoutService = logoutService;
    }

    @PostMapping("/cadastro")
    public ResponseEntity<LoginResposta> cadastro(
            @Valid @RequestBody UsuarioCadastro usuarioCadastro) {

        Usuario usuario = usuarioService.cadastrar(usuarioCadastro);

        String token = jwtService.gerarToken(usuario);

        LoginResposta resposta = new LoginResposta(
                token,
                usuario.getNome()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resposta);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResposta> login(
            @RequestBody LoginRequest loginRequest) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequest.email(),
                                loginRequest.senha()
                        )
                );

        UsuarioAutenticado usuarioAutenticado =
                (UsuarioAutenticado) authentication.getPrincipal();

        String token = jwtService.gerarToken(
                usuarioAutenticado.getUsuario()
        );

        LoginResposta resposta = new LoginResposta(
                token,
                usuarioAutenticado.getUsuario().getNome()
        );

        return ResponseEntity.ok(resposta);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader("Authorization") String authorizationHeader) {

        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {

            return ResponseEntity.badRequest().build();
        }

        String token = authorizationHeader.substring(7);

        logoutService.logout(token);

        return ResponseEntity.noContent().build();
    }
}