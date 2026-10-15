package com.reservahub.api.repository;

import com.reservahub.api.model.Assinatura;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssinaturaRepository
        extends JpaRepository<Assinatura, Long> {
}