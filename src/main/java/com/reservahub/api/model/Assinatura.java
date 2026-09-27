package com.reservahub.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "assinatura")
public class Assinatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalhe")
    private Long idDetalhe;

    @Column(
            name = "quantidade",
            nullable = false
    )
    private Integer quantidade;

    /*
     * Muitas assinaturas podem fazer referência
     * ao mesmo produto.
     *
     * assinatura.id_produto -> produto.id_produto
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "id_produto",
            nullable = false
    )
    @JsonIgnore
    private Produto produto;

    /*
     * Muitas assinaturas podem fazer referência
     * ao mesmo plano.
     *
     * assinatura.id_plano -> plano_assinatura.id_plano
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "id_plano",
            nullable = false
    )
    @JsonIgnore
    private Plano plano;

    public Assinatura() {
    }

    public Long getIdDetalhe() {
        return idDetalhe;
    }

    public void setIdDetalhe(Long idDetalhe) {
        this.idDetalhe = idDetalhe;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public Plano getPlano() {
        return plano;
    }

    public void setPlano(Plano plano) {
        this.plano = plano;
    }
}
