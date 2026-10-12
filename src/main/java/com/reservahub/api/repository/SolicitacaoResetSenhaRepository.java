
package com.reservahub.api.repository;

import com.reservahub.api.model.SolicitacaoResetSenha;
import com.reservahub.api.model.StatusSolicitacaoResetSenha;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SolicitacaoResetSenhaRepository
        extends JpaRepository<SolicitacaoResetSenha, Long> {

    boolean existsByUsuario_IdAndStatus(
            Long usuarioId,
            StatusSolicitacaoResetSenha status
    );

    List<SolicitacaoResetSenha> findAllByOrderByCriadoEmDesc();

    List<SolicitacaoResetSenha> findAllByUsuario_IdAndStatus(
            Long usuarioId,
            StatusSolicitacaoResetSenha status
    );

    Optional<SolicitacaoResetSenha>
    findFirstByUsuario_IdAndStatusAndCodigoUtilizadoEmIsNullOrderByResolvidoEmDesc(
            Long usuarioId,
            StatusSolicitacaoResetSenha status
    );

    Optional<SolicitacaoResetSenha> findByIdAndStatusAndCodigoUtilizadoEmIsNull(
            Long id,
            StatusSolicitacaoResetSenha status
    );
}
