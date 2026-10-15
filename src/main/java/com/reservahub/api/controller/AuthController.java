package com.reservahub.api.controller;

import com.reservahub.api.dto.LoginRequest;
import com.reservahub.api.dto.LoginResposta;
import com.reservahub.api.dto.RedefinirSenhaRequest;
import com.reservahub.api.dto.UsuarioCadastro;
import com.reservahub.api.model.Usuario;
import com.reservahub.api.security.JwtService;
import com.reservahub.api.security.UsuarioAutenticado;
import com.reservahub.api.service.LogoutService;
import com.reservahub.api.service.SolicitacaoResetSenhaService;
import com.reservahub.api.service.UsuarioService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
    private final SolicitacaoResetSenhaService solicitacaoResetSenhaService;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UsuarioService usuarioService,
            LogoutService logoutService,
            SolicitacaoResetSenhaService solicitacaoResetSenhaService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioService = usuarioService;
        this.logoutService = logoutService;
        this.solicitacaoResetSenhaService = solicitacaoResetSenhaService;
    }

    @PostMapping("/cadastro")
    public ResponseEntity<LoginResposta> cadastro(
            @Valid @RequestBody UsuarioCadastro usuarioCadastro
    ) {
        Usuario usuario = usuarioService.cadastrar(usuarioCadastro);
        String token = jwtService.gerarToken(usuario);

        LoginResposta resposta = new LoginResposta(
                token,
                usuario.getNome(),
                usuario.getPerfil()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest loginRequest
    ) {
        try {
            Authentication authentication =
                    authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    loginRequest.email(),
                                    loginRequest.senha()
                            )
                    );

            UsuarioAutenticado usuarioAutenticado =
                    (UsuarioAutenticado) authentication.getPrincipal();

            Usuario usuario = usuarioAutenticado.getUsuario();
            String token = jwtService.gerarToken(usuario);

            LoginResposta resposta = new LoginResposta(
                    token,
                    usuario.getNome(),
                    usuario.getPerfil()
            );

            return ResponseEntity.ok(resposta);
        } catch (BadCredentialsException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "mensagem",
                            "E-mail ou senha inválidos."
                    ));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorizationHeader
    ) {
        if (authorizationHeader == null
                || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().build();
        }

        String token = authorizationHeader.substring(7).trim();

        if (token.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        logoutService.logout(token);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/redefinir-senha")
    public ResponseEntity<Map<String, String>> redefinirSenha(
            @Valid @RequestBody RedefinirSenhaRequest request
    ) {
        try {
            solicitacaoResetSenhaService.redefinirSenha(
                    request.solicitacaoId(),
                    request.novaSenha()
            );

            return ResponseEntity.ok(
                    Map.of("mensagem", "Senha redefinida com sucesso.")
            );
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensagem", exception.getMessage()));
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensagem", exception.getMessage()));
        }
    }

    @GetMapping("/cep")
    public ResponseEntity<Map<String, String>> obterCep(
            Authentication authentication
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "mensagem",
                            "Usuário não autenticado."
                    ));
        }

        String email = authentication.getName();

        Usuario usuario = usuarioService.buscarPorEmail(email);

        String cep = usuario.getCep();

        if (cep == null || cep.isBlank()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "mensagem",
                            "Nenhum CEP cadastrado para este usuário."
                    ));
        }

        return ResponseEntity.ok(Map.of("cep", cep));
    }
}