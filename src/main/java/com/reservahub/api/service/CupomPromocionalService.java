
package com.reservahub.api.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.reservahub.api.model.CupomPromocional;
import com.reservahub.api.model.Usuario;
import com.reservahub.api.repository.CupomPromocionalRepository;
import com.reservahub.api.repository.UsuarioRepository;

@Service
public class CupomPromocionalService {

    private static final Map<String, Integer> DESCONTOS_POR_UF = Map.ofEntries(
            Map.entry("AC", 20),
            Map.entry("AL", 12),
            Map.entry("AP", 20),
            Map.entry("AM", 20),
            Map.entry("BA", 20),
            Map.entry("CE", 20),
            Map.entry("DF", 6),
            Map.entry("ES", 12),
            Map.entry("GO", 12),
            Map.entry("MA", 20),
            Map.entry("MT", 12),
            Map.entry("MS", 12),
            Map.entry("MG", 12),
            Map.entry("PA", 20),
            Map.entry("PB", 12),
            Map.entry("PR", 6),
            Map.entry("PE", 20),
            Map.entry("PI", 12),
            Map.entry("RJ", 12),
            Map.entry("RN", 12),
            Map.entry("RS", 6),
            Map.entry("RO", 20),
            Map.entry("RR", 20),
            Map.entry("SC", 6),
            Map.entry("SP", 6),
            Map.entry("SE", 12),
            Map.entry("TO", 20)
    );

    private final CupomPromocionalRepository cupomRepository;
    private final UsuarioRepository usuarioRepository;

    public CupomPromocionalService(
            CupomPromocionalRepository cupomRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.cupomRepository = cupomRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public synchronized Resultado emitirCupom(String emailAutenticado, String uf) {
        Usuario usuario = buscarUsuario(emailAutenticado);

        Optional<CupomPromocional> cupomExistente =
                cupomRepository.findByUsuario(usuario);

        if (cupomExistente.isPresent()) {
            CupomPromocional cupom = cupomExistente.get();

            return new Resultado(
                    false,
                    cupom.getCupom(),
                    cupom.getPercentual(),
                    "O usuário já possui um cupom promocional."
            );
        }

        if (uf == null || uf.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Não foi possível identificar o estado do usuário."
            );
        }

        String ufNormalizada = uf.trim().toUpperCase();
        Integer percentual = DESCONTOS_POR_UF.get(ufNormalizada);

        if (percentual == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Estado inválido para emissão do cupom."
            );
        }

        CupomPromocional cupom = new CupomPromocional();
        cupom.setUsuario(usuario);
        cupom.setUf(ufNormalizada);
        cupom.setPercentual(percentual);
        cupom.setCupom("DESC" + percentual + "%");
        cupom.setDataCriacao(LocalDateTime.now());

        CupomPromocional cupomSalvo = cupomRepository.save(cupom);

        return new Resultado(
                true,
                cupomSalvo.getCupom(),
                cupomSalvo.getPercentual(),
                "Cupom promocional registrado com sucesso."
        );
    }

    private Usuario buscarUsuario(String emailAutenticado) {
        if (emailAutenticado == null || emailAutenticado.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Não foi possível identificar o usuário autenticado."
            );
        }

        return usuarioRepository
                .findByEmailIgnoreCase(emailAutenticado.trim())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuário autenticado não encontrado."
                ));
    }

    public record Resultado(
            boolean novo,
            String cupom,
            int percentual,
            String mensagem
    ) {
    }
}
