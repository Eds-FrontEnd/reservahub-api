package com.reservahub.api.repository;

import com.reservahub.api.model.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProdutoSmartphoneRepository extends JpaRepository<Produto, String> {

    @Query("""
            SELECT p
            FROM Produto p
            JOIN FETCH p.plano
            """)
    java.util.List<Produto> findAllComPlano();

    @Query(
            value = """
                    SELECT p
                    FROM Produto p
                    JOIN FETCH p.plano
                    """,
            countQuery = """
                    SELECT COUNT(p)
                    FROM Produto p
                    """
    )
    Page<Produto> findAllComPlano(Pageable pageable);
}