package com.reservahub.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "solicitacao_reset_senha", schema = "public")
public class SolicitacaoResetSenha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusSolicitacaoResetSenha status =
            StatusSolicitacaoResetSenha.PENDENTE;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "resolvido_em")
    private Instant resolvidoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administrador_id")
    private Usuario administrador;

    protected SolicitacaoResetSenha() {
    }

    public SolicitacaoResetSenha(Usuario usuario) {
        this.usuario = Objects.requireNonNull(usuario);
        this.status = StatusSolicitacaoResetSenha.PENDENTE;
    }

    @PrePersist
    protected void prePersist() {
        if (criadoEm == null) {
            criadoEm = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public StatusSolicitacaoResetSenha getStatus() {
        return status;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public Instant getResolvidoEm() {
        return resolvidoEm;
    }

    public Usuario getAdministrador() {
        return administrador;
    }

    public void resolver(
            StatusSolicitacaoResetSenha novoStatus,
            Usuario administrador
    ) {
        if (status != StatusSolicitacaoResetSenha.PENDENTE) {
            throw new IllegalStateException(
                    "A solicitação já foi resolvida."
            );
        }

        if (novoStatus != StatusSolicitacaoResetSenha.APROVADA
                && novoStatus != StatusSolicitacaoResetSenha.RECUSADA
                && novoStatus != StatusSolicitacaoResetSenha.CANCELADA) {
            throw new IllegalArgumentException(
                    "Status de resolução inválido."
            );
        }

        this.status = novoStatus;
        this.administrador = Objects.requireNonNull(administrador);
        this.resolvidoEm = Instant.now();
    }
}