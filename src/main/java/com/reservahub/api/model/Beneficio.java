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
@Table(name = "beneficio")
public class Beneficio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_beneficio")
    private Long idBeneficio;

    @Column(
            name = "descricao_beneficio",
            nullable = false,
            length = 255
    )
    private String descricaoBeneficio;

    /*
     * Muitos benefícios pertencem a um produto.
     *
     * beneficio.id_produto -> produto.id_produto
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "id_produto",
            nullable = false
    )
    @JsonIgnore
    private Produto produto;

    public Beneficio() {
    }

    public Long getIdBeneficio() {
        return idBeneficio;
    }

    public void setIdBeneficio(Long idBeneficio) {
        this.idBeneficio = idBeneficio;
    }

    public String getDescricaoBeneficio() {
        return descricaoBeneficio;
    }

    public void setDescricaoBeneficio(String descricaoBeneficio) {
        this.descricaoBeneficio = descricaoBeneficio;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }
}
