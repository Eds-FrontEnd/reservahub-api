
package com.reservahub.api.controller;

import com.reservahub.api.dto.AdminGraphicResponse;
import com.reservahub.api.service.AdminGraphicService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/graphic")
@Validated
public class AdminGraphicController {

    private final AdminGraphicService adminGraphicService;

    public AdminGraphicController(
            AdminGraphicService adminGraphicService) {
        this.adminGraphicService = adminGraphicService;
    }

    @GetMapping
    public ResponseEntity<AdminGraphicResponse> buscarDados(
            @RequestParam(defaultValue = "2026")
            @Min(2000)
            @Max(2100)
            int ano) {

        AdminGraphicResponse response =
                adminGraphicService.obterRelatorio(ano);

        return ResponseEntity.ok(response);
    }
}
