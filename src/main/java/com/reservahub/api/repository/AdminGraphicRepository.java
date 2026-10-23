
package com.reservahub.api.repository;

import com.reservahub.api.dto.AdminGraphicCompraResponse;
import com.reservahub.api.dto.AdminGraphicMesResponse;

import java.util.List;

public interface AdminGraphicRepository {

    List<AdminGraphicCompraResponse> listarCompras(int ano);

    List<AdminGraphicMesResponse> resumirPorMes(int ano);
}
