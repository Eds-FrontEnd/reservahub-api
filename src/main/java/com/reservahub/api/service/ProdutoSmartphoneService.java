package com.reservahub.api.service;

import com.reservahub.api.dto.ProdutoSmartphone;
import com.reservahub.api.model.Produto;
import com.reservahub.api.model.TipoPlano;
import com.reservahub.api.repository.ProdutoSmartphoneRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProdutoSmartphoneService {

    private final ProdutoSmartphoneRepository produtoSmartphoneRepository;

    public ProdutoSmartphoneService(
            ProdutoSmartphoneRepository produtoSmartphoneRepository
    ) {
        this.produtoSmartphoneRepository = produtoSmartphoneRepository;
    }

    public List<ProdutoSmartphone> listarTodosProdutos() {
        return produtoSmartphoneRepository.findAllComPlano()
                .stream()
                .map(this::paraDTO)
                .toList();
    }

    public Page<ProdutoSmartphone> listarProdutosPaginados(Pageable pageable) {
        return produtoSmartphoneRepository.findAllComPlano(pageable)
                .map(this::paraDTO);
    }

    public Optional<ProdutoSmartphone> buscarPorId(String id) {
        return listarTodosProdutos()
                .stream()
                .filter(produto -> produto.id().equals(id))
                .findFirst();
    }

    public List<ProdutoSmartphone> buscarPorNome(String nome) {
        String pesquisa = nome.trim().toLowerCase();

        return listarTodosProdutos()
                .stream()
                .filter(produto ->
                        produto.nome().toLowerCase().contains(pesquisa)
                                || produto.modelo_produto()
                                .toLowerCase()
                                .contains(pesquisa)
                )
                .toList();
    }

    public List<ProdutoSmartphone> listarDisponiveis() {
        return listarTodosProdutos()
                .stream()
                .filter(produto -> produto.stock() > 0)
                .toList();
    }

    public List<ProdutoSmartphone> listarEsgotados() {
        return listarTodosProdutos()
                .stream()
                .filter(produto -> produto.stock() == 0)
                .toList();
    }

    private ProdutoSmartphone paraDTO(Produto produto) {
        return new ProdutoSmartphone(
                produto.getIdProduto(),
                produto.getNomeProduto(),
                produto.getStock(),
                produto.getModelo(),
                produto.getDescription(),
                produto.getUrlImagem(),
                produto.getPlano().getIdPlano(),
                obterNomePlano(produto.getPlano().getTipoPlano()),
                produto.getPlano().getMonthlyPrice()
        );
    }

    private String obterNomePlano(TipoPlano tipoPlano) {
        return switch (tipoPlano) {
            case MENSAL -> "Mensal";
            case ANUAL -> "Anual";
            case VINTE_E_QUATRO_MESES -> "24 meses";
        };
    }
}