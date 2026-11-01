package com.curso.jogos.api.mapper;

import com.curso.jogos.api.dto.ItemVendaResponse;
import com.curso.jogos.api.dto.VendaResponse;
import com.curso.jogos.api.dto.VendaResumoResponse;
import com.curso.jogos.domain.ItemVenda;
import com.curso.jogos.domain.Venda;
import org.springframework.stereotype.Component;

@Component
public class VendaMapper {
    public VendaResponse toResponse(Venda venda) {
        return new VendaResponse(venda.getId(), venda.getCliente().getId(),
                venda.getCliente().getPessoa().getNome(), venda.getVendedor().getId(),
                venda.getVendedor().getPessoa().getNome(), venda.getDataVenda(), venda.getStatus(),
                venda.getItens().stream().map(this::toItemResponse).toList(), venda.calcularTotal());
    }

    public VendaResumoResponse toResumo(Venda venda) {
        return new VendaResumoResponse(venda.getId(), venda.getCliente().getId(),
                venda.getCliente().getPessoa().getNome(), venda.getVendedor().getId(),
                venda.getVendedor().getPessoa().getNome(), venda.getDataVenda(), venda.getStatus());
    }

    private ItemVendaResponse toItemResponse(ItemVenda item) {
        return new ItemVendaResponse(item.getId(), item.getJogo().getId(), item.getJogo().getTitulo(),
                item.getQuantidade(), item.getPrecoUnitario(), item.calcularSubtotal());
    }
}
