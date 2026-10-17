
package com.reservahub.api.service;

import com.reservahub.api.dto.SolicitacaoResetSenhaResposta;
import com.reservahub.api.dto.SolicitacaoResetSenhaStatusResposta;
import com.reservahub.api.model.SolicitacaoResetSenha;
import com.reservahub.api.model.StatusSolicitacaoResetSenha;
import com.reservahub.api.model.Usuario;
import com.reservahub.api.repository.SolicitacaoResetSenhaRepository;
import com.reservahub.api.repository.UsuarioRepository;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class SolicitacaoResetSenhaService {

    private static final String MENSAGEM_PADRAO =
            "Se os dados corresponderem a uma conta, a solicitação será analisada pela administração.";

    private static final long SSE_TIMEOUT = 0L;

    private final UsuarioRepository usuarioRepository;
    private final SolicitacaoResetSenhaRepository solicitacaoRepository;
    private final PasswordEncoder passwordEncoder;

    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> acompanhamentos =
            new ConcurrentHashMap<>();

    private final CopyOnWriteArrayList<SseEmitter> acompanhamentosAdmin =
            new CopyOnWriteArrayList<>();

    public SolicitacaoResetSenhaService(
            UsuarioRepository usuarioRepository,
            SolicitacaoResetSenhaRepository solicitacaoRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.solicitacaoRepository = solicitacaoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public SolicitacaoResetSenhaResposta solicitar(String emailInformado) {
        String email = normalizarEmail(emailInformado);

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        MENSAGEM_PADRAO
                ));

        Optional<SolicitacaoResetSenha> existente =
                solicitacaoRepository
                        .findFirstByUsuario_IdAndStatusAndCodigoUtilizadoEmIsNullOrderByResolvidoEmDesc(
                                usuario.getId(),
                                StatusSolicitacaoResetSenha.PENDENTE
                        );

        SolicitacaoResetSenha solicitacao;

        if (existente.isPresent()) {
            solicitacao = existente.get();
        } else {
            solicitacao = solicitacaoRepository.save(
                    new SolicitacaoResetSenha(usuario)
            );

            publicarAlteracaoAdminAposCommit();
        }

        return paraResposta(solicitacao);
    }

    @Transactional(readOnly = true)
    public SolicitacaoResetSenhaStatusResposta consultarStatus(
            Long solicitacaoId
    ) {
        if (solicitacaoId == null || solicitacaoId <= 0) {
            throw new IllegalArgumentException(
                    "Identificador da solicitação inválido."
            );
        }

        SolicitacaoResetSenha solicitacao =
                solicitacaoRepository.findById(solicitacaoId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Solicitação não encontrada."
                        ));

        return paraStatus(solicitacao);
    }

    @Transactional(readOnly = true)
    public SseEmitter acompanharStatus(Long solicitacaoId) {
        if (solicitacaoId == null || solicitacaoId <= 0) {
            throw new IllegalArgumentException(
                    "Identificador da solicitação inválido."
            );
        }

        SolicitacaoResetSenha solicitacao =
                solicitacaoRepository.findById(solicitacaoId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Solicitação não encontrada."
                        ));

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        CopyOnWriteArrayList<SseEmitter> conexoes =
                acompanhamentos.computeIfAbsent(
                        solicitacaoId,
                        id -> new CopyOnWriteArrayList<>()
                );

        Runnable remover = () -> {
            conexoes.remove(emitter);

            if (conexoes.isEmpty()) {
                acompanhamentos.remove(solicitacaoId, conexoes);
            }
        };

        emitter.onCompletion(remover);
        emitter.onTimeout(() -> {
            remover.run();
            emitter.complete();
        });
        emitter.onError(erro -> remover.run());

        synchronized (conexoes) {
            try {
                conexoes.add(emitter);
                enviarStatus(emitter, paraStatus(solicitacao));
            } catch (IOException exception) {
                remover.run();
                emitter.completeWithError(exception);
            }
        }

        return emitter;
    }

    @Transactional(readOnly = true)
    public SseEmitter acompanharAlteracoesAdmin() {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        Runnable remover = () -> acompanhamentosAdmin.remove(emitter);

        emitter.onCompletion(remover);
        emitter.onTimeout(() -> {
            remover.run();
            emitter.complete();
        });
        emitter.onError(erro -> remover.run());

        acompanhamentosAdmin.add(emitter);

        try {
            emitter.send(
                    SseEmitter.event()
                            .name("conectado")
                            .data("conectado", MediaType.TEXT_PLAIN)
            );
        } catch (IOException exception) {
            remover.run();
            emitter.completeWithError(exception);
        }

        return emitter;
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoResetSenhaResposta> listarSolicitacoes() {
        return solicitacaoRepository.findAllByOrderByCriadoEmDesc()
                .stream()
                .map(this::paraResposta)
                .toList();
    }

    @Transactional
    public SolicitacaoResetSenhaResposta resolverSolicitacao(
            Long solicitacaoId,
            StatusSolicitacaoResetSenha novoStatus,
            Long administradorId
    ) {
        if (novoStatus != StatusSolicitacaoResetSenha.APROVADA
                && novoStatus != StatusSolicitacaoResetSenha.REJEITADA) {
            throw new IllegalArgumentException(
                    "Status de resolução inválido."
            );
        }

        SolicitacaoResetSenha solicitacao =
                solicitacaoRepository.findById(solicitacaoId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Solicitação não encontrada."
                        ));

        if (solicitacao.getStatus()
                != StatusSolicitacaoResetSenha.PENDENTE) {
            throw new IllegalStateException(
                    "A solicitação já foi resolvida."
            );
        }

        Usuario administrador = usuarioRepository.findById(administradorId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Administrador não encontrado."
                ));

        Long usuarioId = solicitacao.getUsuario().getId();

        List<SolicitacaoResetSenha> pendentes =
                solicitacaoRepository.findAllByUsuario_IdAndStatus(
                        usuarioId,
                        StatusSolicitacaoResetSenha.PENDENTE
                );

        SolicitacaoResetSenhaResposta resposta = null;

        for (SolicitacaoResetSenha pendente : pendentes) {
            pendente.resolver(novoStatus, administrador);

            SolicitacaoResetSenha solicitacaoSalva =
                    solicitacaoRepository.save(pendente);

            publicarStatusAposCommit(solicitacaoSalva);

            if (solicitacaoId.equals(solicitacaoSalva.getId())) {
                resposta = paraResposta(solicitacaoSalva);
            }
        }

        if (resposta == null) {
            throw new IllegalStateException(
                    "Não foi possível resolver a solicitação selecionada."
            );
        }

        publicarAlteracaoAdminAposCommit();

        return resposta;
    }

    @Transactional
    public void redefinirSenha(
            Long solicitacaoId,
            String novaSenha
    ) {
        if (solicitacaoId == null || solicitacaoId <= 0) {
            throw new IllegalArgumentException(
                    "Identificador da solicitação inválido."
            );
        }

        if (novaSenha == null || novaSenha.isBlank()) {
            throw new IllegalArgumentException(
                    "A nova senha é obrigatória."
            );
        }

        if (novaSenha.length() < 6) {
            throw new IllegalArgumentException(
                    "A senha deve ter pelo menos 6 caracteres."
            );
        }

        SolicitacaoResetSenha solicitacao =
                solicitacaoRepository
                        .findByIdAndStatusAndCodigoUtilizadoEmIsNull(
                                solicitacaoId,
                                StatusSolicitacaoResetSenha.APROVADA
                        )
                        .orElseThrow(() -> new IllegalStateException(
                                "Não existe uma solicitação aprovada para redefinir a senha."
                        ));

        Usuario usuario = solicitacao.getUsuario();

        usuario.setSenha(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);

        StatusSolicitacaoResetSenha statusAnterior =
                solicitacao.getStatus();

        solicitacao.marcarSolicitacaoComoUtilizada();

        SolicitacaoResetSenha solicitacaoSalva =
                solicitacaoRepository.save(solicitacao);

        if (statusAnterior != solicitacaoSalva.getStatus()) {
            publicarStatusAposCommit(solicitacaoSalva);
        }

        publicarAlteracaoAdminAposCommit();
    }

    private void enviarStatus(
            SseEmitter emitter,
            SolicitacaoResetSenhaStatusResposta status
    ) throws IOException {
        emitter.send(
                SseEmitter.event()
                        .name("status")
                        .data(status, MediaType.APPLICATION_JSON)
        );
    }

    private void publicarStatusAposCommit(
            SolicitacaoResetSenha solicitacao
    ) {
        SolicitacaoResetSenhaStatusResposta status =
                paraStatus(solicitacao);

        executarAposCommit(() -> publicarStatus(status));
    }

    private void publicarAlteracaoAdminAposCommit() {
        executarAposCommit(this::publicarAlteracaoAdmin);
    }

    private void executarAposCommit(Runnable acao) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            acao.run();
                        }
                    }
            );
        } else {
            acao.run();
        }
    }

    private void publicarStatus(
            SolicitacaoResetSenhaStatusResposta status
    ) {
        CopyOnWriteArrayList<SseEmitter> conexoes =
                acompanhamentos.get(status.id());

        if (conexoes == null) {
            return;
        }

        synchronized (conexoes) {
            for (SseEmitter emitter : conexoes) {
                try {
                    enviarStatus(emitter, status);
                } catch (IOException | IllegalStateException exception) {
                    conexoes.remove(emitter);
                    emitter.completeWithError(exception);
                }
            }

            if (conexoes.isEmpty()) {
                acompanhamentos.remove(status.id(), conexoes);
            }
        }
    }

    private void publicarAlteracaoAdmin() {
        for (SseEmitter emitter : acompanhamentosAdmin) {
            try {
                emitter.send(
                        SseEmitter.event()
                                .name("alteracao")
                                .data("atualizacao", MediaType.TEXT_PLAIN)
                );
            } catch (IOException | IllegalStateException exception) {
                acompanhamentosAdmin.remove(emitter);
                emitter.completeWithError(exception);
            }
        }
    }

    private SolicitacaoResetSenhaStatusResposta paraStatus(
            SolicitacaoResetSenha solicitacao
    ) {
        return new SolicitacaoResetSenhaStatusResposta(
                solicitacao.getId(),
                solicitacao.getStatus().name(),
                solicitacao.getCriadoEm()
        );
    }

    private String normalizarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("E-mail inválido.");
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }

    private SolicitacaoResetSenhaResposta paraResposta(
            SolicitacaoResetSenha solicitacao
    ) {
        return new SolicitacaoResetSenhaResposta(
                solicitacao.getId(),
                solicitacao.getUsuario().getEmail(),
                solicitacao.getStatus().name(),
                solicitacao.getCriadoEm()
        );
    }
}
