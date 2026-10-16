package com.reservahub.api.controller;

import com.reservahub.api.dto.CompraRequest;
import com.reservahub.api.dto.CompraResponse;
import com.reservahub.api.service.CompraService;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/compras")
public class CompraController {

    private final CompraService compraService;

    public CompraController(CompraService compraService) {
        this.compraService = compraService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompraResponse concluirCompra(
            @RequestBody CompraRequest request,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "É necessário autenticar-se para concluir a compra."
            );
        }

        return compraService.concluirCompra(
                authentication.getName(),
                request
        );
    }

    @PostMapping("/validar-cupom")
    public ValidacaoCupomResponse validarCupom(
            @RequestBody CompraRequest request,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "É necessário autenticar-se para validar o cupom."
            );
        }

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe um cupom de desconto."
            );
        }

        compraService.validarCupom(
                authentication.getName(),
                request.cupomDesconto()
        );

        return new ValidacaoCupomResponse(
                true,
                "Cupom aplicado com sucesso."
        );
    }

    public record ValidacaoCupomResponse(
            boolean valido,
            String mensagem
    ) {
    }
}
