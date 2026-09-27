package com.reservahub.api.service;

import com.reservahub.api.dto.ProdutoSmartphone;
import com.reservahub.api.model.Produto;
import com.reservahub.api.repository.ProdutoSmartphoneRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProdutoSmartphoneService {

    private final ProdutoSmartphoneRepository produtoSmartphoneRepository;

    public ProdutoSmartphoneService(ProdutoSmartphoneRepository produtoSmartphoneRepository) {
        this.produtoSmartphoneRepository = produtoSmartphoneRepository;
    }

    public List<ProdutoSmartphone> listarTodosProdutos() {
        return produtoSmartphoneRepository.findAll()
                .stream()
                .map(this::paraDTO)
                .toList();
    }

    private ProdutoSmartphone paraDTO(Produto produto) {
        return new ProdutoSmartphone(
                produto.getIdProduto(),
                produto.getNomeProduto(),
                produto.getStock(),
                produto.getModelo(),
                produto.getUrlImagem(),
                produto.getPlano().getIdPlano()
        );
    }

    public Optional<ProdutoSmartphone> buscarPorId(String id) {
        return listarTodosProdutos()
                .stream()
                .filter(produto -> produto.id().equals(id))
                .findFirst();
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

    public List<ProdutoSmartphone> buscarPorNome(String nome) {
        String pesquisa = nome.trim().toLowerCase();

        return listarTodosProdutos()
                .stream()
                .filter(produto ->
                        produto.nome().toLowerCase().contains(pesquisa)
                                || produto.modelo_produto().toLowerCase().contains(pesquisa)
                )
                .toList();
    }
}