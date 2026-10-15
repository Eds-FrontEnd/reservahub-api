package com.reservahub.api.controller;

import com.reservahub.api.dto.ProdutoSmartphone;
import com.reservahub.api.service.EstoqueSseService;
import com.reservahub.api.service.ProdutoSmartphoneService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/smartphones")
public class ProdutoSmartphoneController {

    private final ProdutoSmartphoneService produtoSmartphoneService;
    private final EstoqueSseService estoqueSseService;

    public ProdutoSmartphoneController(
            ProdutoSmartphoneService produtoSmartphoneService,
            EstoqueSseService estoqueSseService
    ) {
        this.produtoSmartphoneService = produtoSmartphoneService;
        this.estoqueSseService = estoqueSseService;
    }

    @GetMapping
    public Page<ProdutoSmartphone> listarProdutosPaginados(
            @PageableDefault(size = 4) Pageable pageable
    ) {
        return produtoSmartphoneService.listarProdutosPaginados(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProdutoSmartphone> buscarPorId(
            @PathVariable String id
    ) {
        return produtoSmartphoneService
                .buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar")
    public List<ProdutoSmartphone> buscar(
            @RequestParam String nome
    ) {
        return produtoSmartphoneService.buscarPorNome(nome);
    }

    @GetMapping(
            value = "/estoque/eventos",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE
    )
    public SseEmitter eventosEstoque() {
        return estoqueSseService.conectar();
    }
}