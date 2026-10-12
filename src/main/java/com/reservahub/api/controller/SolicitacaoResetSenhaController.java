
package com.reservahub.api.controller;

import com.reservahub.api.dto.SolicitacaoResetSenhaRequest;
import com.reservahub.api.dto.SolicitacaoResetSenhaResposta;
import com.reservahub.api.dto.SolicitacaoResetSenhaStatusResposta;
import com.reservahub.api.service.SolicitacaoResetSenhaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/auth")
public class SolicitacaoResetSenhaController {

    private final SolicitacaoResetSenhaService solicitacaoResetSenhaService;

    public SolicitacaoResetSenhaController(
            SolicitacaoResetSenhaService solicitacaoResetSenhaService
    ) {
        this.solicitacaoResetSenhaService = solicitacaoResetSenhaService;
    }

    @PostMapping("/solicitar-reset-senha")
    public ResponseEntity<SolicitacaoResetSenhaResposta> solicitar(
            @Valid @RequestBody SolicitacaoResetSenhaRequest request
    ) {
        SolicitacaoResetSenhaResposta resposta =
                solicitacaoResetSenhaService.solicitar(request.email());

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(resposta);
    }

    @GetMapping("/solicitacao-reset-senha/{id}")
    public ResponseEntity<SolicitacaoResetSenhaStatusResposta> consultarStatus(
            @PathVariable Long id
    ) {
        try {
            SolicitacaoResetSenhaStatusResposta resposta =
                    solicitacaoResetSenhaService.consultarStatus(id);

            return ResponseEntity.ok(resposta);
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping(
            value = "/solicitacao-reset-senha/{id}/eventos",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public ResponseEntity<SseEmitter> acompanharStatus(
            @PathVariable Long id
    ) {
        try {
            return ResponseEntity.ok(
                    solicitacaoResetSenhaService.acompanharStatus(id)
            );
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.notFound().build();
        }
    }
}