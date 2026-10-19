
package com.reservahub.api.repository;

import com.reservahub.api.model.CupomPromocional;
import com.reservahub.api.model.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CupomPromocionalRepository
        extends JpaRepository<CupomPromocional, Long> {

    Optional<CupomPromocional> findByUsuario(Usuario usuario);

    boolean existsByCupomIgnoreCaseAndIdNot(String cupom, Long id);

    @EntityGraph(attributePaths = "usuario")
    List<CupomPromocional> findAllByOrderByDataCriacaoDesc();
}
