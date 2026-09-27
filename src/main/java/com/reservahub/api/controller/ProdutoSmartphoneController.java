package com.reservahub.api.controller;

import com.reservahub.api.dto.ProdutoSmartphone;
import com.reservahub.api.service.ProdutoSmartphoneService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
public class ProdutoSmartphoneController {

    private final ProdutoSmartphoneService produtoSmartphoneService;

    public ProdutoSmartphoneController(
            ProdutoSmartphoneService produtoSmartphoneService
    ) {
        this.produtoSmartphoneService = produtoSmartphoneService;
    }

    /*
     * GET /api/produtos
     *
     * Lista todos os produtos.
     */
    @GetMapping
    public List<ProdutoSmartphone> listar() {

        return produtoSmartphoneService.listarTodosProdutos();
    }

    /*
     * GET /api/produtos/smartphones
     *
     * Lista os smartphones.
     */
    @GetMapping("/smartphones")
    public List<ProdutoSmartphone> listarSmartphones() {

        return produtoSmartphoneService.listarTodosProdutos();
    }

    /*
     * GET /api/produtos/{id}
     *
     * Busca um produto pelo ID.
     *
     * Exemplo:
     * GET /api/produtos/sub-ph-15pro
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProdutoSmartphone> buscarPorId(
            @PathVariable String id
    ) {

        return produtoSmartphoneService
                .buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /*
     * GET /api/produtos/buscar?nome=iphone
     *
     * Pesquisa por nome ou modelo.
     *
     * Exemplos:
     *
     * /api/produtos/buscar?nome=iphone
     * /api/produtos/buscar?nome=samsung
     * /api/produtos/buscar?nome=S24
     */
    @GetMapping("/buscar")
    public List<ProdutoSmartphone> buscar(
            @RequestParam String nome
    ) {

        return produtoSmartphoneService.buscarPorNome(nome);
    }

    /*
     * GET /api/produtos/estoque/disponiveis
     *
     * Lista somente produtos disponíveis.
     */
    @GetMapping("/estoque/disponiveis")
    public List<ProdutoSmartphone> listarDisponiveis() {

        return produtoSmartphoneService.listarDisponiveis();
    }

    /*
     * GET /api/produtos/estoque/esgotados
     *
     * Lista produtos sem estoque.
     */
    @GetMapping("/estoque/esgotados")
    public List<ProdutoSmartphone> listarEsgotados() {

        return produtoSmartphoneService.listarEsgotados();
    }
}