
package com.reservahub.api.dto;

import java.util.List;

public record CompraRequest(
        List<ItemCompraRequest> items,
        String cupomDesconto
) {
}
