package com.reservahub.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "plano_assinatura",
        schema = "public"
)
public class Plano {

    @Id
    @Column(name = "id_plano")
    private String idPlano;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "tipo_plano",
            nullable = false,
            length = 50
    )
    private TipoPlano tipoPlano;

    @Column(
            name = "monthly_price",
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal monthlyPrice;

    @Column(
            name = "description",
            nullable = true,
            length = 500
    )
    private String description;

    @Column(
            name = "image_url",
            nullable = true,
            length = 255
    )
    private String imageUrl;

    @OneToMany(
            mappedBy = "plano",
            fetch = FetchType.LAZY
    )
    @JsonIgnore
    private List<Produto> produtos = new ArrayList<>();

    @OneToMany(
            mappedBy = "plano",
            fetch = FetchType.LAZY
    )
    @JsonIgnore
    private List<Assinatura> assinaturas = new ArrayList<>();

    public Plano() {
    }

    public String getIdPlano() {
        return idPlano;
    }

    public void setIdPlano(String idPlano) {
        this.idPlano = idPlano;
    }

    public TipoPlano getTipoPlano() {
        return tipoPlano;
    }

    public void setTipoPlano(TipoPlano tipoPlano) {
        this.tipoPlano = tipoPlano;
    }

    public BigDecimal getMonthlyPrice() {
        return monthlyPrice;
    }

    public void setMonthlyPrice(BigDecimal monthlyPrice) {
        this.monthlyPrice = monthlyPrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<Produto> getProdutos() {
        return produtos;
    }

    public void setProdutos(List<Produto> produtos) {
        this.produtos = produtos;
    }

    public List<Assinatura> getAssinaturas() {
        return assinaturas;
    }

    public void setAssinaturas(List<Assinatura> assinaturas) {
        this.assinaturas = assinaturas;
    }
}