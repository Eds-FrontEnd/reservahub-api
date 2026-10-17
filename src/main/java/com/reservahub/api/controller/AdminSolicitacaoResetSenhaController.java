
package com.reservahub.api.controller;

import com.reservahub.api.dto.SolicitacaoResetSenhaResposta;
import com.reservahub.api.model.StatusSolicitacaoResetSenha;
import com.reservahub.api.model.Usuario;
import com.reservahub.api.repository.UsuarioRepository;
import com.reservahub.api.service.SolicitacaoResetSenhaService;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/admin/solicitacoes-reset-senha")
public class AdminSolicitacaoResetSenhaController {

    private final SolicitacaoResetSenhaService solicitacaoResetSenhaService;
    private final UsuarioRepository usuarioRepository;

    public AdminSolicitacaoResetSenhaController(
            SolicitacaoResetSenhaService solicitacaoResetSenhaService,
            UsuarioRepository usuarioRepository
    ) {
        this.solicitacaoResetSenhaService = solicitacaoResetSenhaService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public ResponseEntity<List<SolicitacaoResetSenhaResposta>> listar() {
        return ResponseEntity.ok(
                solicitacaoResetSenhaService.listarSolicitacoes()
        );
    }

    @GetMapping(
            value = "/eventos",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter acompanharAlteracoes() {
        return solicitacaoResetSenhaService.acompanharAlteracoesAdmin();
    }

    @PutMapping("/{id}/aprovar")
    public ResponseEntity<SolicitacaoResetSenhaResposta> aprovar(
            @PathVariable Long id,
            @AuthenticationPrincipal String emailAdministrador
    ) {
        Usuario administrador = buscarAdministrador(emailAdministrador);

        return ResponseEntity.ok(
                solicitacaoResetSenhaService.resolverSolicitacao(
                        id,
                        StatusSolicitacaoResetSenha.APROVADA,
                        administrador.getId()
                )
        );
    }

    @PutMapping("/{id}/recusar")
    public ResponseEntity<SolicitacaoResetSenhaResposta> recusar(
            @PathVariable Long id,
            @AuthenticationPrincipal String emailAdministrador
    ) {
        Usuario administrador = buscarAdministrador(emailAdministrador);

        return ResponseEntity.ok(
                solicitacaoResetSenhaService.resolverSolicitacao(
                        id,
                        StatusSolicitacaoResetSenha.REJEITADA,
                        administrador.getId()
                )
        );
    }

    private Usuario buscarAdministrador(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Não foi possível identificar o administrador autenticado."
            );
        }

        Usuario administrador = usuarioRepository.findByEmail(email.trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Administrador autenticado não encontrado."
                ));

        if (!"ADMIN".equals(administrador.getPerfil())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Apenas administradores podem processar solicitações."
            );
        }

        return administrador;
    }
}
