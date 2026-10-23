
package com.reservahub.api.service;

import com.reservahub.api.dto.AdminGraphicCompraResponse;
import com.reservahub.api.dto.AdminGraphicMesResponse;
import com.reservahub.api.dto.AdminGraphicResponse;
import com.reservahub.api.repository.AdminGraphicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminGraphicService {

    private static final int PRIMEIRO_MES = 1;
    private static final int ULTIMO_MES = 12;
    private static final int ANO_MINIMO = 2000;
    private static final int ANO_MAXIMO = 2100;

    private static final BigDecimal VALOR_ZERO = BigDecimal.ZERO;

    private final AdminGraphicRepository adminGraphicRepository;

    public AdminGraphicService(
            AdminGraphicRepository adminGraphicRepository) {
        this.adminGraphicRepository = adminGraphicRepository;
    }

    @Transactional(readOnly = true)
    public AdminGraphicResponse obterRelatorio(int ano) {
        validarAno(ano);

        List<AdminGraphicCompraResponse> compras =
                adminGraphicRepository.listarCompras(ano);

        List<AdminGraphicMesResponse> resumoMensal =
                adminGraphicRepository.resumirPorMes(ano);

        List<AdminGraphicMesResponse> faturamentoPorMes =
                preencherMesesSemMovimentacao(resumoMensal);

        long totalRegistros = compras.size();

        long totalUnidades = compras.stream()
                .mapToLong(compra -> valorOuZero(compra.quantidade()))
                .sum();

        long planosMensais = faturamentoPorMes.stream()
                .mapToLong(AdminGraphicMesResponse::quantidadeMensal)
                .sum();

        long planosAnuais = faturamentoPorMes.stream()
                .mapToLong(AdminGraphicMesResponse::quantidadeAnual)
                .sum();

        BigDecimal faturamentoTotal = faturamentoPorMes.stream()
                .map(AdminGraphicMesResponse::faturamentoTotal)
                .reduce(VALOR_ZERO, BigDecimal::add);

        return new AdminGraphicResponse(
                ano,
                totalRegistros,
                totalUnidades,
                planosMensais,
                planosAnuais,
                faturamentoTotal,
                faturamentoPorMes,
                compras
        );
    }

    private List<AdminGraphicMesResponse> preencherMesesSemMovimentacao(
            List<AdminGraphicMesResponse> resumoMensal) {

        Map<Integer, AdminGraphicMesResponse> resumoPorMes =
                new HashMap<>();

        for (AdminGraphicMesResponse resumo : resumoMensal) {
            resumoPorMes.put(resumo.mes(), resumo);
        }

        List<AdminGraphicMesResponse> resultado =
                new ArrayList<>(ULTIMO_MES);

        for (int mes = PRIMEIRO_MES; mes <= ULTIMO_MES; mes++) {
            AdminGraphicMesResponse resumo = resumoPorMes.get(mes);

            if (resumo == null) {
                resultado.add(criarResumoVazio(mes));
                continue;
            }

            resultado.add(resumo);
        }

        return List.copyOf(resultado);
    }

    private AdminGraphicMesResponse criarResumoVazio(int mes) {
        return new AdminGraphicMesResponse(
                mes,
                0L,
                0L,
                VALOR_ZERO,
                VALOR_ZERO,
                VALOR_ZERO
        );
    }

    private long valorOuZero(Integer valor) {
        return valor == null ? 0L : valor.longValue();
    }

    private void validarAno(int ano) {
        if (ano < ANO_MINIMO || ano > ANO_MAXIMO) {
            throw new IllegalArgumentException(
                    "O ano deve estar entre "
                            + ANO_MINIMO
                            + " e "
                            + ANO_MAXIMO
                            + "."
            );
        }
    }
}
