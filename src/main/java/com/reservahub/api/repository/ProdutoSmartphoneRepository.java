
package com.reservahub.api.repository;

import com.reservahub.api.model.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProdutoSmartphoneRepository
        extends JpaRepository<Produto, String> {

    @Query("""
            SELECT p
            FROM Produto p
            JOIN FETCH p.plano
            """)
    List<Produto> findAllComPlano();

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

    @Query(
            value = """
                    SELECT p
                    FROM Produto p
                    JOIN FETCH p.plano
                    WHERE COALESCE(:termo, '') = ''
                       OR LOWER(p.nomeProduto)
                          LIKE CONCAT('%', LOWER(COALESCE(:termo, '')), '%')
                       OR LOWER(p.modelo)
                          LIKE CONCAT('%', LOWER(COALESCE(:termo, '')), '%')
                    """,
            countQuery = """
                    SELECT COUNT(p)
                    FROM Produto p
                    WHERE COALESCE(:termo, '') = ''
                       OR LOWER(p.nomeProduto)
                          LIKE CONCAT('%', LOWER(COALESCE(:termo, '')), '%')
                       OR LOWER(p.modelo)
                          LIKE CONCAT('%', LOWER(COALESCE(:termo, '')), '%')
                    """
    )
    Page<Produto> pesquisarComPlano(
            @Param("termo") String termo,
            Pageable pageable
    );

    @Query("""
            SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
            FROM Produto p
            JOIN p.assinaturas a
            WHERE p.idProduto = :produtoId
            """)
    boolean possuiAssinaturas(
            @Param("produtoId") String produtoId
    );

    @Modifying(
            flushAutomatically = true,
            clearAutomatically = true
    )
    @Query("""
            UPDATE Produto p
            SET p.stock = p.stock - :quantidade
            WHERE p.idProduto = :produtoId
              AND p.stock >= :quantidade
            """)
    int decrementarEstoque(
            @Param("produtoId") String produtoId,
            @Param("quantidade") int quantidade
    );
}
