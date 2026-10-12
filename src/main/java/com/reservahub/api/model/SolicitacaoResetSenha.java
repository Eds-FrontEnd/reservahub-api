
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
import java.time.LocalDateTime;

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
    @Column(nullable = false, length = 20)
    private StatusSolicitacaoResetSenha status =
            StatusSolicitacaoResetSenha.PENDENTE;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "resolvido_em")
    private LocalDateTime resolvidoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administrador_id")
    private Usuario administrador;

    @Column(name = "codigo_hash", length = 100)
    private String codigoHash;

    @Column(name = "codigo_expira_em")
    private LocalDateTime codigoExpiraEm;

    @Column(name = "codigo_utilizado_em")
    private LocalDateTime codigoUtilizadoEm;

    protected SolicitacaoResetSenha() {
    }

    public SolicitacaoResetSenha(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException(
                    "O usuário é obrigatório."
            );
        }

        this.usuario = usuario;
        this.status = StatusSolicitacaoResetSenha.PENDENTE;
    }

    @PrePersist
    protected void prePersist() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
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
                && novoStatus != StatusSolicitacaoResetSenha.REJEITADA) {
            throw new IllegalArgumentException(
                    "Status de resolução inválido."
            );
        }

        if (administrador == null) {
            throw new IllegalArgumentException(
                    "O administrador é obrigatório."
            );
        }

        this.status = novoStatus;
        this.administrador = administrador;
        this.resolvidoEm = LocalDateTime.now();
    }

    public boolean podeRedefinirSenha() {
        return status == StatusSolicitacaoResetSenha.APROVADA
                && codigoUtilizadoEm == null;
    }

    public void marcarSolicitacaoComoUtilizada() {
    if (status != StatusSolicitacaoResetSenha.APROVADA) {
        throw new IllegalStateException(
                "A solicitação não está aprovada para redefinição."
        );
    }

    if (codigoUtilizadoEm != null) {
        throw new IllegalStateException(
                "A solicitação já foi utilizada."
        );
    }

    this.status = StatusSolicitacaoResetSenha.FINALIZADA;
    this.codigoUtilizadoEm = LocalDateTime.now();
    this.codigoHash = null;
    this.codigoExpiraEm = null;
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

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getResolvidoEm() {
        return resolvidoEm;
    }

    public Usuario getAdministrador() {
        return administrador;
    }

    public String getCodigoHash() {
        return codigoHash;
    }
}
