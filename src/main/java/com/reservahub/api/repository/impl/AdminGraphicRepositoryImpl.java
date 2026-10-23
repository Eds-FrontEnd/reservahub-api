
package com.reservahub.api.repository.impl;

import com.reservahub.api.dto.AdminGraphicCompraResponse;
import com.reservahub.api.dto.AdminGraphicMesResponse;
import com.reservahub.api.repository.AdminGraphicRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class AdminGraphicRepositoryImpl implements AdminGraphicRepository {

    private static final String ID_PLANO_MENSAL = "plano-mensal";
    private static final String ID_PLANO_ANUAL = "plano-anual";

    private final JdbcTemplate jdbcTemplate;

    public AdminGraphicRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<AdminGraphicCompraResponse> listarCompras(int ano) {
        String sql = """
                SELECT id_detalhe, quantidade, id_plano, id_produto,
                       valor_original, cupom_desconto, valor_total, data_compra
                FROM public.compra_assinatura
                WHERE data_compra >= ? AND data_compra < ?
                ORDER BY data_compra DESC, id_detalhe DESC
                """;

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapCompra(rs),
                Timestamp.valueOf(inicioDoAno(ano)),
                Timestamp.valueOf(inicioDoAno(ano + 1))
        );
    }

    @Override
    public List<AdminGraphicMesResponse> resumirPorMes(int ano) {
        String sql = """
                SELECT EXTRACT(MONTH FROM data_compra)::integer AS mes,
                       COALESCE(
                           SUM(CASE
                               WHEN id_plano = ? THEN quantidade
                               ELSE 0
                           END),
                           0
                       )::bigint AS quantidade_mensal,
                       COALESCE(
                           SUM(CASE
                               WHEN id_plano = ? THEN quantidade
                               ELSE 0
                           END),
                           0
                       )::bigint AS quantidade_anual,
                       COALESCE(
                           SUM(CASE
                               WHEN id_plano = ? THEN valor_total
                               ELSE 0
                           END),
                           0
                       )::numeric(14,2) AS faturamento_mensal,
                       COALESCE(
                           SUM(CASE
                               WHEN id_plano = ? THEN valor_total
                               ELSE 0
                           END),
                           0
                       )::numeric(14,2) AS faturamento_anual,
                       COALESCE(
                           SUM(valor_total),
                           0
                       )::numeric(14,2) AS faturamento_total
                FROM public.compra_assinatura
                WHERE data_compra >= ? AND data_compra < ?
                GROUP BY EXTRACT(MONTH FROM data_compra)
                ORDER BY mes
                """;

        Timestamp inicio = Timestamp.valueOf(inicioDoAno(ano));
        Timestamp fim = Timestamp.valueOf(inicioDoAno(ano + 1));

        return jdbcTemplate.query(
                sql,
                (rs, rowNum) -> mapMes(rs),
                ID_PLANO_MENSAL,
                ID_PLANO_ANUAL,
                ID_PLANO_MENSAL,
                ID_PLANO_ANUAL,
                inicio,
                fim
        );
    }

    private AdminGraphicCompraResponse mapCompra(ResultSet rs)
            throws SQLException {

        String idPlano = rs.getString("id_plano");
        PlanoInfo plano = classificarPlano(idPlano);

        Timestamp timestamp = rs.getTimestamp("data_compra");
        LocalDateTime dataCompra = timestamp == null
                ? null
                : timestamp.toLocalDateTime();

        return new AdminGraphicCompraResponse(
                rs.getLong("id_detalhe"),
                rs.getInt("quantidade"),
                idPlano,
                rs.getString("id_produto"),
                plano.meses(),
                plano.tipo(),
                rs.getBigDecimal("valor_original"),
                rs.getString("cupom_desconto"),
                rs.getBigDecimal("valor_total"),
                dataCompra
        );
    }

    private AdminGraphicMesResponse mapMes(ResultSet rs)
            throws SQLException {

        return new AdminGraphicMesResponse(
                rs.getInt("mes"),
                rs.getLong("quantidade_mensal"),
                rs.getLong("quantidade_anual"),
                rs.getBigDecimal("faturamento_mensal"),
                rs.getBigDecimal("faturamento_anual"),
                rs.getBigDecimal("faturamento_total")
        );
    }

    private PlanoInfo classificarPlano(String idPlano) {
        if (ID_PLANO_MENSAL.equals(idPlano)) {
            return new PlanoInfo(1, "MENSAL");
        }

        if (ID_PLANO_ANUAL.equals(idPlano)) {
            return new PlanoInfo(12, "ANUAL");
        }

        return new PlanoInfo(null, "OUTRO");
    }

    private LocalDateTime inicioDoAno(int ano) {
        return LocalDateTime.of(ano, 1, 1, 0, 0);
    }

    private record PlanoInfo(Integer meses, String tipo) {
    }
}
