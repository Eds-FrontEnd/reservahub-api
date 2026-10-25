
package com.reservahub.api.controller;

import com.reservahub.api.dto.ImagemUploadResponse;
import com.reservahub.api.dto.PlanoAdminResponse;
import com.reservahub.api.dto.ProdutoAdminRequest;
import com.reservahub.api.dto.ProdutoAdminResponse;
import com.reservahub.api.service.ProdutoAdminService;
import com.reservahub.api.service.ProdutoImagemService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin/produtos")
public class ProdutoAdminController {

    private final ProdutoAdminService produtoAdminService;
    private final ProdutoImagemService produtoImagemService;

    public ProdutoAdminController(
            ProdutoAdminService produtoAdminService,
            ProdutoImagemService produtoImagemService
    ) {
        this.produtoAdminService = produtoAdminService;
        this.produtoImagemService = produtoImagemService;
    }

    @GetMapping
    public Page<ProdutoAdminResponse> listar(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10, sort = "nomeProduto")
            Pageable pageable
    ) {
        return produtoAdminService.listar(search, pageable);
    }

    @GetMapping("/planos")
    public List<PlanoAdminResponse> listarPlanos() {
        return produtoAdminService.listarPlanos();
    }

    @GetMapping("/{id}")
    public ProdutoAdminResponse buscarPorId(
            @PathVariable String id
    ) {
        return produtoAdminService.buscarPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProdutoAdminResponse criar(
            @Valid @RequestBody ProdutoAdminRequest request
    ) {
        return produtoAdminService.criar(request);
    }

    @PutMapping("/{id}")
    public ProdutoAdminResponse atualizar(
            @PathVariable String id,
            @Valid @RequestBody ProdutoAdminRequest request
    ) {
        return produtoAdminService.atualizar(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable String id) {
        produtoAdminService.excluir(id);
    }

    @PostMapping(
        value = "/imagens",
        consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @ResponseStatus(HttpStatus.CREATED)
    public ImagemUploadResponse enviarImagem(
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam("modelo_produto") String modeloProduto
    ) {
        return new ImagemUploadResponse(
                produtoImagemService.salvar(arquivo, modeloProduto)
        );
    }
}
