package com.reservahub.api.repository;

import com.reservahub.api.model.Plano;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanoRepository extends JpaRepository<Plano, String> {
}