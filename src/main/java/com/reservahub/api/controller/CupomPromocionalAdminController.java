
package com.reservahub.api.controller;

import com.reservahub.api.dto.CupomPromocionalEdicaoRequest;
import com.reservahub.api.dto.CupomPromocionalRequest;
import com.reservahub.api.dto.CupomPromocionalResponse;
import com.reservahub.api.dto.CupomPromocionalStatusRequest;
import com.reservahub.api.service.CupomPromocionalAdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/admin/cupons")
public class CupomPromocionalAdminController {

    private final CupomPromocionalAdminService adminService;

    public CupomPromocionalAdminController(
            CupomPromocionalAdminService adminService
    ) {
        this.adminService = adminService;
    }

    @GetMapping
    public List<CupomPromocionalResponse> listarCupons() {
        return adminService.listarCupons();
    }

    @PostMapping
    public CupomPromocionalResponse criarCupom(
            @Valid @RequestBody CupomPromocionalRequest request
    ) {
        return adminService.criarCupom(request);
    }

    @PutMapping("/{id}")
    public CupomPromocionalResponse editarCupom(
            @PathVariable Long id,
            @Valid @RequestBody CupomPromocionalEdicaoRequest request
    ) {
        return adminService.editarCupom(id, request);
    }

    @PatchMapping("/{id}/ativo")
    public CupomPromocionalResponse alterarStatus(
            @PathVariable Long id,
            @RequestBody CupomPromocionalStatusRequest request
    ) {
        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe o novo status do cupom."
            );
        }

        return adminService.alterarStatus(id, request.ativo());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirCupom(@PathVariable Long id) {
        adminService.excluirCupom(id);
    }
}
