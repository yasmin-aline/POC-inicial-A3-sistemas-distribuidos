package br.com.pixseguro.domain;

import br.com.pixseguro.web.Dtos.DecisaoPreflight;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "pix_tentativa")
public class PixTentativa {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @Column(name = "chave_hash", length = 64, nullable = false)
    private String chaveHash;

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    private Risco risco;

    @Column(nullable = false)
    private int pontuacao;

    @Enumerated(EnumType.STRING)
    @Column(length = 40, nullable = false)
    private DecisaoPreflight decisao;

    @Column(name = "sinais_json", columnDefinition = "TEXT", nullable = false)
    private String sinaisJson;

    @Column(name = "criada_em", nullable = false)
    private Instant criadaEm;

    protected PixTentativa() {}

    public PixTentativa(String id, String chaveHash, BigDecimal valor, Risco risco, int pontuacao,
                        DecisaoPreflight decisao, String sinaisJson, Instant criadaEm) {
        this.id = id;
        this.chaveHash = chaveHash;
        this.valor = valor;
        this.risco = risco;
        this.pontuacao = pontuacao;
        this.decisao = decisao;
        this.sinaisJson = sinaisJson;
        this.criadaEm = criadaEm;
    }

    public String getId() { return id; }
    public String getChaveHash() { return chaveHash; }
    public BigDecimal getValor() { return valor; }
    public Risco getRisco() { return risco; }
    public int getPontuacao() { return pontuacao; }
    public DecisaoPreflight getDecisao() { return decisao; }
    public String getSinaisJson() { return sinaisJson; }
    public Instant getCriadaEm() { return criadaEm; }
}
