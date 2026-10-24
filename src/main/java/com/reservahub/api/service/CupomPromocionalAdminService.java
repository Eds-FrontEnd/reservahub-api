
package com.reservahub.api.service;

import com.reservahub.api.dto.CupomPromocionalEdicaoRequest;
import com.reservahub.api.dto.CupomPromocionalRequest;
import com.reservahub.api.dto.CupomPromocionalResponse;
import com.reservahub.api.model.CupomPromocional;
import com.reservahub.api.model.Usuario;
import com.reservahub.api.repository.CupomPromocionalRepository;
import com.reservahub.api.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class CupomPromocionalAdminService {

    private final CupomPromocionalRepository cupomRepository;
    private final UsuarioRepository usuarioRepository;

    public CupomPromocionalAdminService(
            CupomPromocionalRepository cupomRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.cupomRepository = cupomRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<CupomPromocionalResponse> listarCupons() {
        return cupomRepository.findAllByOrderByDataCriacaoDesc()
                .stream()
                .map(this::converterResposta)
                .toList();
    }

    @Transactional
    public CupomPromocionalResponse criarCupom(
            CupomPromocionalRequest request
    ) {
        validarRequisicao(request);

        String email = request.emailUsuario().trim();

        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário não encontrado."
                ));

        if (cupomRepository.findByUsuario(usuario).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Este usuário já possui um cupom promocional."
            );
        }

        String uf = usuario.getUf();

        if (uf == null || uf.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O usuário precisa ter uma UF cadastrada para receber um cupom."
            );
        }

        CupomPromocional cupom = new CupomPromocional();
        cupom.setUsuario(usuario);
        cupom.setCupom(normalizarCodigo(request.cupom()));
        cupom.setPercentual(request.percentual());
        cupom.setUf(uf.trim().toUpperCase(Locale.ROOT));
        cupom.setDataCriacao(LocalDateTime.now());
        cupom.setUtilizado(false);
        cupom.setAtivo(request.ativo() == null || request.ativo());

        return converterResposta(cupomRepository.save(cupom));
    }

    @Transactional
    public CupomPromocionalResponse editarCupom(
            Long id,
            CupomPromocionalEdicaoRequest request
    ) {
        validarId(id);

        if (request == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe o código e o percentual do cupom."
            );
        }

        validarEdicao(request);

        CupomPromocional cupom = buscarCupom(id);
        String codigo = normalizarCodigo(request.cupom());

        if (cupomRepository.existsByCupomIgnoreCaseAndIdNot(codigo, id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Já existe outro cupom com esse código."
            );
        }

        cupom.setCupom(codigo);
        cupom.setPercentual(request.percentual());
        cupom.setUtilizado(request.utilizado());

        return converterResposta(cupomRepository.save(cupom));
    }

    @Transactional
    public CupomPromocionalResponse alterarStatus(
            Long id,
            Boolean ativo
    ) {
        validarId(id);

        if (ativo == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe se o cupom deve ficar ativo."
            );
        }

        CupomPromocional cupom = buscarCupom(id);
        cupom.setAtivo(ativo);

        return converterResposta(cupomRepository.save(cupom));
    }

    @Transactional
    public void excluirCupom(Long id) {
        validarId(id);

        CupomPromocional cupom = buscarCupom(id);

        try {
            cupomRepository.delete(cupom);
            cupomRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Não foi possível excluir o cupom porque existem referências relacionadas a ele.",
                    exception
            );
        }
    }

    private void validarId(Long id) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Identificador de cupom inválido."
            );
        }
    }

    private CupomPromocional buscarCupom(Long id) {
        return cupomRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cupom promocional não encontrado."
                ));
    }

    private void validarRequisicao(CupomPromocionalRequest request) {
        if (request == null
                || request.emailUsuario() == null
                || request.emailUsuario().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe o e-mail do usuário."
            );
        }

        validarCodigo(request.cupom());
        validarPercentual(request.percentual());
    }

    private void validarEdicao(CupomPromocionalEdicaoRequest request) {
        validarCodigo(request.cupom());
        validarPercentual(request.percentual());

        if (request.utilizado() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe o status de utilização do cupom."
            );
        }
    }

    private void validarCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Informe o código do cupom."
            );
        }

        String codigoNormalizado = normalizarCodigo(codigo);

        if (!codigoNormalizado.matches("[A-Z0-9_%\\-]{3,20}")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O código deve conter de 3 a 20 caracteres: letras, números, hífen, sublinhado ou percentual."
            );
        }
    }

    private void validarPercentual(Integer percentual) {
        if (percentual == null || percentual < 1 || percentual > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O percentual de desconto deve estar entre 1 e 100."
            );
        }
    }

    private String normalizarCodigo(String codigo) {
        if (codigo == null) {
            return "";
        }

        return codigo.trim().toUpperCase(Locale.ROOT);
    }

    private CupomPromocionalResponse converterResposta(
            CupomPromocional cupom
    ) {
        Usuario usuario = cupom.getUsuario();

        return new CupomPromocionalResponse(
                cupom.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                cupom.getCupom(),
                cupom.getPercentual(),
                cupom.getUf(),
                cupom.getDataCriacao(),
                cupom.getUtilizado(),
                cupom.getAtivo()
        );
    }
}
