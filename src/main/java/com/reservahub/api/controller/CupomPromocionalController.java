
package com.reservahub.api.controller;

import com.reservahub.api.dto.CupomPromocionalRequest;
import com.reservahub.api.service.CupomPromocionalService;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/cupons")
public class CupomPromocionalController {

    private final CupomPromocionalService cupomService;

    public CupomPromocionalController(CupomPromocionalService cupomService) {
        this.cupomService = cupomService;
    }

    @PostMapping("/promocional")
    public CupomPromocionalService.Resultado emitirCupomPromocional(
            @RequestBody CupomPromocionalRequest request,
            Authentication authentication
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "É necessário autenticar-se para obter um cupom."
            );
        }

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe o estado do usuário."
            );
        }

        return cupomService.emitirCupom(
                authentication.getName(),
                request.uf()
        );
    }
}
