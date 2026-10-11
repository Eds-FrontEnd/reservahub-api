package com.reservahub.api.service;

import com.reservahub.api.dto.SolicitacaoResetSenhaRequest;
import com.reservahub.api.dto.SolicitacaoResetSenhaResponse;
import com.reservahub.api.model.SolicitacaoResetSenha;
import com.reservahub.api.model.StatusSolicitacaoResetSenha;
import com.reservahub.api.model.Usuario;
import com.reservahub.api.repository.SolicitacaoResetSenhaRepository;
import com.reservahub.api.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

@Service
public class SolicitacaoResetSenhaService {

    private static final String MENSAGEM_PADRAO =
            "Se os dados corresponderem a uma conta, "
                    + "a solicitação será analisada pela administração.";

    private final UsuarioRepository usuarioRepository;
    private final SolicitacaoResetSenhaRepository solicitacaoRepository;

    public SolicitacaoResetSenhaService(
            UsuarioRepository usuarioRepository,
            SolicitacaoResetSenhaRepository solicitacaoRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.solicitacaoRepository = solicitacaoRepository;
    }

    @Transactional
    public SolicitacaoResetSenhaResponse solicitar(
            SolicitacaoResetSenhaRequest request
    ) {
        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        Optional<Usuario> usuarioOptional =
                usuarioRepository.findByEmail(email);

        if (usuarioOptional.isPresent()) {
            Usuario usuario = usuarioOptional.get();

            boolean possuiSolicitacaoPendente =
                    solicitacaoRepository.existsByUsuario_IdAndStatus(
                            usuario.getId(),
                            StatusSolicitacaoResetSenha.PENDENTE
                    );

            if (!possuiSolicitacaoPendente) {
                SolicitacaoResetSenha solicitacao =
                        new SolicitacaoResetSenha(usuario);

                solicitacaoRepository.save(solicitacao);
            }
        }

        return new SolicitacaoResetSenhaResponse(MENSAGEM_PADRAO);
    }
}