
package com.reservahub.api.service;

import com.reservahub.api.dto.PlanoAdminResponse;
import com.reservahub.api.dto.ProdutoAdminRequest;
import com.reservahub.api.dto.ProdutoAdminResponse;
import com.reservahub.api.model.Plano;
import com.reservahub.api.model.Produto;
import com.reservahub.api.model.TipoPlano;
import com.reservahub.api.repository.PlanoRepository;
import com.reservahub.api.repository.ProdutoSmartphoneRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class ProdutoAdminService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ProdutoAdminService.class);

    private static final Pattern CARACTERES_INVALIDOS =
            Pattern.compile("[^a-z0-9]+");

    private final ProdutoSmartphoneRepository produtoRepository;
    private final PlanoRepository planoRepository;
    private final ProdutoImagemService produtoImagemService;

    public ProdutoAdminService(
            ProdutoSmartphoneRepository produtoRepository,
            PlanoRepository planoRepository,
            ProdutoImagemService produtoImagemService
    ) {
        this.produtoRepository = produtoRepository;
        this.planoRepository = planoRepository;
        this.produtoImagemService = produtoImagemService;
    }

    @Transactional(readOnly = true)
    public Page<ProdutoAdminResponse> listar(
            String pesquisa,
            Pageable pageable
    ) {
        String termo = normalizarPesquisa(pesquisa);

        return produtoRepository
                .pesquisarComPlano(termo, pageable)
                .map(this::paraResponse);
    }

    @Transactional(readOnly = true)
    public ProdutoAdminResponse buscarPorId(String id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Produto não encontrado."
                ));

        return paraResponse(produto);
    }

    @Transactional(readOnly = true)
    public List<PlanoAdminResponse> listarPlanos() {
        return planoRepository.findAll()
                .stream()
                .sorted(Comparator.comparingInt(
                        plano -> prioridadePlano(plano.getTipoPlano())
                ))
                .map(plano -> new PlanoAdminResponse(
                        plano.getIdPlano(),
                        plano.getTipoPlano().name(),
                        plano.getMonthlyPrice()
                ))
                .toList();
    }

    @Transactional
    public ProdutoAdminResponse criar(ProdutoAdminRequest request) {
        validarImagemUrl(request.image_url());

        Produto produto = new Produto();

        produto.setIdProduto(gerarId(request.modelo_produto()));
        aplicarDados(produto, request);

        return paraResponse(produtoRepository.save(produto));
    }

    @Transactional
    public ProdutoAdminResponse atualizar(
            String id,
            ProdutoAdminRequest request
    ) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Produto não encontrado."
                ));

        String imagemRecebida = request.image_url();

        boolean imagemMantida = imagemRecebida != null
                && imagemRecebida.trim().equals(produto.getUrlImagem());

        if (!imagemMantida) {
            validarImagemUrl(imagemRecebida);
        }

        aplicarDados(produto, request);

        return paraResponse(produtoRepository.save(produto));
    }

    @Transactional
    public void excluir(String id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Produto não encontrado."
                ));

        if (produtoRepository.possuiAssinaturas(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Este produto possui assinaturas vinculadas e não pode ser excluído."
            );
        }

        String caminhoImagem = produto.getUrlImagem();

        produtoRepository.delete(produto);

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            produtoImagemService.excluir(caminhoImagem);
                        } catch (RuntimeException exception) {
                            LOGGER.error(
                                    "O produto {} foi excluído, mas não foi possível excluir sua imagem: {}",
                                    id,
                                    caminhoImagem,
                                    exception
                            );
                        }
                    }
                }
        );
    }

    private void aplicarDados(
            Produto produto,
            ProdutoAdminRequest request
    ) {
        if (request.stock() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe a quantidade em estoque."
            );
        }

        Plano plano = planoRepository.findById(request.id_plano())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "O plano selecionado não existe."
                ));

        produto.setNomeProduto(request.nome().trim());
        produto.setStock(request.stock());
        produto.setModelo(request.modelo_produto().trim());
        produto.setDescription(request.description().trim());
        produto.setUrlImagem(request.image_url().trim());
        produto.setPlano(plano);
    }

    private ProdutoAdminResponse paraResponse(Produto produto) {
        Plano plano = produto.getPlano();

        return new ProdutoAdminResponse(
                produto.getIdProduto(),
                produto.getNomeProduto(),
                produto.getStock(),
                produto.getModelo(),
                produto.getDescription(),
                produto.getUrlImagem(),
                plano.getIdPlano(),
                plano.getTipoPlano().name(),
                plano.getMonthlyPrice()
        );
    }

    private String gerarId(String modelo) {
        String normalizado = Normalizer
                .normalize(modelo, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);

        String slug = CARACTERES_INVALIDOS.matcher(normalizado)
                .replaceAll("-")
                .replaceAll("^-|-$", "");

        if (slug.isBlank()) {
            slug = "smartphone";
        }

        String id;

        do {
            id = "sub-" + slug + "-"
                    + UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 8);
        } while (produtoRepository.existsById(id));

        return id;
    }

    private String normalizarPesquisa(String pesquisa) {
        if (pesquisa == null || pesquisa.isBlank()) {
            return "";
        }

        return pesquisa.trim();
    }

    private int prioridadePlano(TipoPlano tipoPlano) {
        return tipoPlano == TipoPlano.ANUAL ? 0 : 1;
    }

    private void validarImagemUrl(String imageUrl) {
        if (imageUrl == null || !imageUrl.matches(
                "^/uploads/products/[a-z0-9-]+\\.(png|jpe?g|webp)$"
        )) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O caminho da imagem não é válido."
            );
        }
    }
}
