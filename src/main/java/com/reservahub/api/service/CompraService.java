package com.reservahub.api.service;

import com.reservahub.api.dto.CompraRequest;
import com.reservahub.api.dto.CompraResponse;
import com.reservahub.api.dto.ItemCompraRequest;
import com.reservahub.api.model.Assinatura;
import com.reservahub.api.model.Plano;
import com.reservahub.api.model.Produto;
import com.reservahub.api.model.Usuario;
import com.reservahub.api.repository.AssinaturaRepository;
import com.reservahub.api.repository.PlanoRepository;
import com.reservahub.api.repository.ProdutoSmartphoneRepository;
import com.reservahub.api.repository.UsuarioRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
public class CompraService {

    private static final int MAX_ITENS_POR_COMPRA = 50;

    private static final Map<Integer, String> PLANOS_POR_PRAZO = Map.of(
            1, "plano-mensal",
            12, "plano-anual"
    );

    private final AssinaturaRepository assinaturaRepository;
    private final ProdutoSmartphoneRepository produtoRepository;
    private final PlanoRepository planoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EstoqueSseService estoqueSseService;

    public CompraService(
            AssinaturaRepository assinaturaRepository,
            ProdutoSmartphoneRepository produtoRepository,
            PlanoRepository planoRepository,
            UsuarioRepository usuarioRepository,
            EstoqueSseService estoqueSseService
    ) {
        this.assinaturaRepository = assinaturaRepository;
        this.produtoRepository = produtoRepository;
        this.planoRepository = planoRepository;
        this.usuarioRepository = usuarioRepository;
        this.estoqueSseService = estoqueSseService;
    }

    @Transactional
    public CompraResponse concluirCompra(
            String emailAutenticado,
            CompraRequest request
    ) {
        validarRequisicao(request);

        if (emailAutenticado == null || emailAutenticado.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Não foi possível identificar o usuário autenticado."
            );
        }

        String emailNormalizado = emailAutenticado.trim();

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(emailNormalizado)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuário autenticado não encontrado."
                ));

        List<ItemCompraRequest> itens = request.items();

        for (ItemCompraRequest item : itens) {
            registrarItem(item, usuario);
        }

        List<String> idsProdutosAtualizados = itens.stream()
                .map(ItemCompraRequest::productId)
                .distinct()
                .toList();

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        estoqueSseService.notificarEstoqueAtualizado(
                                idsProdutosAtualizados
                        );
                    }
                }
        );

        int totalItens = itens.stream()
                .mapToInt(ItemCompraRequest::quantity)
                .sum();

        return new CompraResponse(
                "Assinatura realizada com sucesso.",
                totalItens
        );
    }

    private void validarRequisicao(CompraRequest request) {
        if (request == null
                || request.items() == null
                || request.items().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O carrinho está vazio."
            );
        }

        if (request.items().size() > MAX_ITENS_POR_COMPRA) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A compra excede o limite de itens permitido."
            );
        }

        for (ItemCompraRequest item : request.items()) {
            if (item == null
                    || item.productId() == null
                    || item.productId().isBlank()
                    || item.quantity() == null
                    || item.quantity() <= 0
                    || item.planMonths() == null
                    || !PLANOS_POR_PRAZO.containsKey(item.planMonths())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Produto, quantidade ou plano inválido."
                );
            }
        }
    }

    private void registrarItem(
            ItemCompraRequest item,
            Usuario usuario
    ) {
        if (!produtoRepository.existsById(item.productId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Um dos smartphones não foi encontrado."
            );
        }

        int unidadesAtualizadas = produtoRepository.decrementarEstoque(
                item.productId(),
                item.quantity()
        );

        if (unidadesAtualizadas == 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Estoque insuficiente para o smartphone "
                            + item.productId() + "."
            );
        }

        Produto produto = produtoRepository.findById(item.productId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Smartphone não encontrado."
                ));

        String idPlano = PLANOS_POR_PRAZO.get(item.planMonths());

        Plano plano = planoRepository.findById(idPlano)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "O plano selecionado não está cadastrado."
                ));

        Assinatura assinatura = new Assinatura();
        assinatura.setUsuario(usuario);
        assinatura.setProduto(produto);
        assinatura.setPlano(plano);
        assinatura.setQuantidade(item.quantity());

        assinaturaRepository.save(assinatura);
    }
}