package com.reservahub.api.repository;

import com.reservahub.api.model.SolicitacaoResetSenha;
import com.reservahub.api.model.StatusSolicitacaoResetSenha;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitacaoResetSenhaRepository
        extends JpaRepository<SolicitacaoResetSenha, Long> {

    boolean existsByUsuario_IdAndStatus(
            Long usuarioId,
            StatusSolicitacaoResetSenha status
    );

    Page<SolicitacaoResetSenha> findAllByStatusOrderByCriadoEmDesc(
            StatusSolicitacaoResetSenha status,
            Pageable pageable
    );
}