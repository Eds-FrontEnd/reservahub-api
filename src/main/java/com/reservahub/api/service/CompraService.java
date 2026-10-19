
package com.reservahub.api.service;

import com.reservahub.api.dto.CompraRequest;
import com.reservahub.api.dto.CompraResponse;
import com.reservahub.api.dto.ItemCompraRequest;
import com.reservahub.api.model.Assinatura;
import com.reservahub.api.model.CupomPromocional;
import com.reservahub.api.model.Plano;
import com.reservahub.api.model.Produto;
import com.reservahub.api.model.Usuario;
import com.reservahub.api.repository.AssinaturaRepository;
import com.reservahub.api.repository.CupomPromocionalRepository;
import com.reservahub.api.repository.PlanoRepository;
import com.reservahub.api.repository.ProdutoSmartphoneRepository;
import com.reservahub.api.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
public class CompraService {

    private static final int MAX_ITENS_POR_COMPRA = 50;

    private static final BigDecimal ZERO =
            BigDecimal.ZERO.setScale(2);

    private static final Map<Integer, String> PLANOS_POR_PRAZO = Map.of(
            1, "plano-mensal",
            12, "plano-anual"
    );

    private final AssinaturaRepository assinaturaRepository;
    private final CupomPromocionalRepository cupomRepository;
    private final ProdutoSmartphoneRepository produtoRepository;
    private final PlanoRepository planoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EstoqueSseService estoqueSseService;

    public CompraService(
            AssinaturaRepository assinaturaRepository,
            CupomPromocionalRepository cupomRepository,
            ProdutoSmartphoneRepository produtoRepository,
            PlanoRepository planoRepository,
            UsuarioRepository usuarioRepository,
            EstoqueSseService estoqueSseService
    ) {
        this.assinaturaRepository = assinaturaRepository;
        this.cupomRepository = cupomRepository;
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

        Usuario usuario = buscarUsuarioAutenticado(emailAutenticado);

        String cupomAplicado = normalizarCupom(
                request.cupomDesconto()
        );

        CupomPromocional cupomPromocional =
                validarCupomDisponivel(usuario, cupomAplicado);

        int percentualDesconto = cupomPromocional == null
                ? 0
                : cupomPromocional.getPercentual();

        List<ItemCompraRequest> itens = request.items();

        List<LinhaCompra> linhas = calcularValores(
                itens,
                cupomAplicado,
                percentualDesconto
        );

        Set<String> idsProdutosAtualizados = new LinkedHashSet<>();

        for (LinhaCompra linha : linhas) {
            String idProdutoAtualizado = registrarItem(linha, usuario);
            idsProdutosAtualizados.add(idProdutoAtualizado);
        }

        if (cupomPromocional != null) {
            cupomPromocional.setUtilizado(true);
            cupomPromocional.setAtivo(false);
            cupomRepository.save(cupomPromocional);
        }

        boolean cupomFoiAplicado = cupomPromocional != null;

        if (!idsProdutosAtualizados.isEmpty() || cupomFoiAplicado) {
            List<String> idsParaNotificar =
                    List.copyOf(idsProdutosAtualizados);

            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            if (!idsParaNotificar.isEmpty()) {
                                estoqueSseService.notificarEstoqueAtualizado(
                                        idsParaNotificar
                                );
                            }

                            if (cupomFoiAplicado) {
                                estoqueSseService.notificarCupomAtualizado();
                            }
                        }
                    }
            );
        }

        int totalItens = itens.stream()
                .mapToInt(ItemCompraRequest::quantity)
                .sum();

        return new CompraResponse(
                "Assinatura realizada com sucesso.",
                totalItens
        );
    }

    @Transactional(readOnly = true)
    public CupomPromocional validarCupom(
            String emailAutenticado,
            String cupomDesconto
    ) {
        Usuario usuario = buscarUsuarioAutenticado(emailAutenticado);

        String cupomNormalizado = normalizarCupom(cupomDesconto);

        if (cupomNormalizado == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe um cupom de desconto."
            );
        }

        return validarCupomDisponivel(usuario, cupomNormalizado);
    }

    private CupomPromocional validarCupomDisponivel(
            Usuario usuario,
            String cupomDesconto
    ) {
        if (cupomDesconto == null) {
            return null;
        }

        Optional<CupomPromocional> cupomEncontrado =
                cupomRepository.findByUsuario(usuario);

        CupomPromocional cupom = cupomEncontrado.orElseThrow(
                () -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Este cupom não está cadastrado para sua conta."
                )
        );

        if (cupom.getCupom() == null
                || !cupom.getCupom().equalsIgnoreCase(cupomDesconto)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O cupom informado não corresponde ao seu cupom promocional."
            );
        }

        if (!Boolean.TRUE.equals(cupom.getAtivo())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Este cupom promocional está desativado."
            );
        }

        if (Boolean.TRUE.equals(cupom.getUtilizado())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Este cupom já foi utilizado na sua conta."
            );
        }

        Integer percentual = cupom.getPercentual();

        if (percentual == null || percentual < 1 || percentual > 100) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "O percentual cadastrado para este cupom é inválido."
            );
        }

        return cupom;
    }

    private Usuario buscarUsuarioAutenticado(
            String emailAutenticado
    ) {
        if (emailAutenticado == null || emailAutenticado.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Não foi possível identificar o usuário autenticado."
            );
        }

        String emailNormalizado = emailAutenticado.trim();

        return usuarioRepository
                .findByEmailIgnoreCase(emailNormalizado)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuário autenticado não encontrado."
                ));
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

    private List<LinhaCompra> calcularValores(
            List<ItemCompraRequest> itens,
            String cupomDesconto,
            int percentualDesconto
    ) {
        List<LinhaCompra> linhas = new ArrayList<>();
        BigDecimal valorOriginalTotal = ZERO;

        for (ItemCompraRequest item : itens) {
            String idPlano = PLANOS_POR_PRAZO.get(item.planMonths());

            Plano plano = planoRepository.findById(idPlano)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "O plano selecionado não está cadastrado."
                    ));

            BigDecimal valorOriginal = plano.getMonthlyPrice()
                    .multiply(BigDecimal.valueOf(item.quantity()))
                    .multiply(BigDecimal.valueOf(item.planMonths()))
                    .setScale(2, RoundingMode.HALF_UP);

            valorOriginalTotal = valorOriginalTotal.add(valorOriginal);

            linhas.add(new LinhaCompra(
                    item,
                    valorOriginal,
                    ZERO,
                    valorOriginal,
                    cupomDesconto
            ));
        }

        BigDecimal valorDescontoTotal = valorOriginalTotal
                .multiply(BigDecimal.valueOf(percentualDesconto))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal descontoRestante = valorDescontoTotal;

        for (int i = 0; i < linhas.size(); i++) {
            LinhaCompra linha = linhas.get(i);
            BigDecimal valorDesconto;

            if (i == linhas.size() - 1) {
                valorDesconto = descontoRestante;
            } else if (valorOriginalTotal.signum() > 0) {
                valorDesconto = linha.valorOriginal()
                        .multiply(valorDescontoTotal)
                        .divide(valorOriginalTotal, 2, RoundingMode.HALF_UP)
                        .min(descontoRestante)
                        .max(ZERO);
            } else {
                valorDesconto = ZERO;
            }

            BigDecimal valorTotal = linha.valorOriginal()
                    .subtract(valorDesconto)
                    .setScale(2, RoundingMode.HALF_UP);

            linhas.set(i, new LinhaCompra(
                    linha.item(),
                    linha.valorOriginal(),
                    valorDesconto,
                    valorTotal,
                    linha.cupomDesconto()
            ));

            descontoRestante = descontoRestante.subtract(valorDesconto);
        }

        return linhas;
    }

    private String normalizarCupom(String cupomDesconto) {
        if (cupomDesconto == null || cupomDesconto.isBlank()) {
            return null;
        }

        return cupomDesconto.trim().toUpperCase(Locale.ROOT);
    }

    private String registrarItem(
            LinhaCompra linha,
            Usuario usuario
    ) {
        ItemCompraRequest item = linha.item();

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
        assinatura.setValorOriginal(linha.valorOriginal());
        assinatura.setCupomDesconto(linha.cupomDesconto());
        assinatura.setValorTotal(linha.valorTotal());

        assinaturaRepository.save(assinatura);

        return item.productId();
    }

    private record LinhaCompra(
            ItemCompraRequest item,
            BigDecimal valorOriginal,
            BigDecimal valorDesconto,
            BigDecimal valorTotal,
            String cupomDesconto
    ) {
    }
}
