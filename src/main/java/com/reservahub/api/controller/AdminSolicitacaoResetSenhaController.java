package com.reservahub.api.controller;

import com.reservahub.api.dto.SolicitacaoResetSenhaResposta;
import com.reservahub.api.model.StatusSolicitacaoResetSenha;
import com.reservahub.api.model.Usuario;
import com.reservahub.api.service.SolicitacaoResetSenhaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/solicitacoes-reset-senha")
public class AdminSolicitacaoResetSenhaController {

    private final SolicitacaoResetSenhaService solicitacaoResetSenhaService;

    public AdminSolicitacaoResetSenhaController(
            SolicitacaoResetSenhaService solicitacaoResetSenhaService
    ) {
        this.solicitacaoResetSenhaService = solicitacaoResetSenhaService;
    }

    @GetMapping
    public ResponseEntity<List<SolicitacaoResetSenhaResposta>> listar() {
        return ResponseEntity.ok(
                solicitacaoResetSenhaService.listarSolicitacoes()
        );
    }

    @PutMapping("/{id}/aprovar")
    public ResponseEntity<SolicitacaoResetSenhaResposta> aprovar(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario administrador
    ) {
        SolicitacaoResetSenhaResposta resposta =
                solicitacaoResetSenhaService.resolverSolicitacao(
                        id,
                        StatusSolicitacaoResetSenha.APROVADA,
                        administrador.getId()
                );

        return ResponseEntity.ok(resposta);
    }

    @PutMapping("/{id}/recusar")
    public ResponseEntity<SolicitacaoResetSenhaResposta> recusar(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario administrador
    ) {
        SolicitacaoResetSenhaResposta resposta =
                solicitacaoResetSenhaService.resolverSolicitacao(
                        id,
                        StatusSolicitacaoResetSenha.REJEITADA,
                        administrador.getId()
                );

        return ResponseEntity.ok(resposta);
    }
}