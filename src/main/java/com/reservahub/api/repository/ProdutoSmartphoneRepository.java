package com.reservahub.api.repository;

import com.reservahub.api.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoSmartphoneRepository extends JpaRepository<Produto, Long> {
}