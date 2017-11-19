package com.marcosevaristo.tcc001.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Linha implements Serializable {
    private Long idSql;
    private String id;
    private Municipio municipio;
    private String numero;
    private String titulo;
    private String subtitulo;
    private Map<String, Carro> carros;
    private List<String> rota;

    private boolean ehFavorito = false;

    private static final long serialVersionUID = 1L;

    public Linha() {
    }

    public Long getIdSql() {
        return idSql;
    }

    public void setIdSql(Long idSql) {
        this.idSql = idSql;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getSubtitulo() {
        return subtitulo;
    }

    public void setSubtitulo(String subtitulo) {
        this.subtitulo = subtitulo;
    }

    public Map<String, Carro> getCarros() {
        return carros;
    }

    public void setCarros(Map<String, Carro> carros) {
        this.carros = carros;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.numero);
        sb.append(" - ");
        sb.append(this.titulo);
        if(this.subtitulo != null) {
            sb.append(" - ");
            sb.append(this.subtitulo);
        }
        return sb.toString();
    }

    public Municipio getMunicipio() {
        return municipio;
    }

    public void setMunicipio(Municipio municipio) {
        this.municipio = municipio;
    }

    public boolean ehFavorito() {
        return ehFavorito;
    }

    public void setEhFavorito(boolean ehFavorito) {
        this.ehFavorito = ehFavorito;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getRota() {
        return rota;
    }

    public void setRota(List<String> rota) {
        this.rota = rota;
    }
}
