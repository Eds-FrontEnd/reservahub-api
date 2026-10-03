package com.reservahub.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "produto",
        schema = "public"
)
public class Produto {

    @Id
    @Column(name = "id_produto")
    private String idProduto;

    @Column(
            name = "nome_produto",
            nullable = false,
            length = 150
    )
    private String nomeProduto;

    @Column(
            name = "stock",
            nullable = false
    )
    private Integer stock;

    @Column(
            name = "description",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String description;

    @Column(
            name = "modelo",
            nullable = false,
            length = 100
    )
    private String modelo;

    @Column(
            name = "imagem_url",
            nullable = false,
            length = 255
    )
    private String urlImagem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "id_plano",
            nullable = false
    )
    private Plano plano;

    @OneToMany(
            mappedBy = "produto",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL
    )
    @JsonIgnore
    private List<Beneficio> beneficios = new ArrayList<>();

    @OneToMany(
            mappedBy = "produto",
            fetch = FetchType.LAZY
    )
    @JsonIgnore
    private List<Assinatura> assinaturas = new ArrayList<>();

    public Produto() {
    }

    public String getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(String idProduto) {
        this.idProduto = idProduto;
    }

    public String getNomeProduto() {
        return nomeProduto;
    }

    public void setNomeProduto(String nomeProduto) {
        this.nomeProduto = nomeProduto;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getUrlImagem() {
        return urlImagem;
    }

    public void setUrlImagem(String urlImagem) {
        this.urlImagem = urlImagem;
    }

    public Plano getPlano() {
        return plano;
    }

    public void setPlano(Plano plano) {
        this.plano = plano;
    }

    public List<Beneficio> getBeneficios() {
        return beneficios;
    }

    public void setBeneficios(List<Beneficio> beneficios) {
        this.beneficios = beneficios;
    }

    public List<Assinatura> getAssinaturas() {
        return assinaturas;
    }

    public void setAssinaturas(List<Assinatura> assinaturas) {
        this.assinaturas = assinaturas;
    }
}