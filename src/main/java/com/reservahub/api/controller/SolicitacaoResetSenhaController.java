package com.reservahub.api.controller;

import com.reservahub.api.dto.SolicitacaoResetSenhaRequest;
import com.reservahub.api.dto.SolicitacaoResetSenhaResponse;
import com.reservahub.api.service.SolicitacaoResetSenhaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class SolicitacaoResetSenhaController {

    private final SolicitacaoResetSenhaService solicitacaoService;

    public SolicitacaoResetSenhaController(
            SolicitacaoResetSenhaService solicitacaoService
    ) {
        this.solicitacaoService = solicitacaoService;
    }

    @PostMapping("/solicitacao-reset-senha")
    public ResponseEntity<SolicitacaoResetSenhaResponse> solicitar(
            @Valid @RequestBody SolicitacaoResetSenhaRequest request
    ) {
        SolicitacaoResetSenhaResponse response =
                solicitacaoService.solicitar(request);

        return ResponseEntity
                .status(HttpStatus.ACCEPTED)
                .body(response);
    }
}