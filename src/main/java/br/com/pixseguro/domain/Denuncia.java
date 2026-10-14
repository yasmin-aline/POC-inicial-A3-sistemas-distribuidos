package br.com.pixseguro.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "denuncia", indexes = @Index(name = "idx_denuncia_chave", columnList = "chave_hash"))
public class Denuncia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Guardamos apenas o hash da chave Pix, nunca a chave em texto puro. */
    @Column(name = "chave_hash", nullable = false, length = 64)
    private String chaveHash;

    @Column(length = 500)
    private String descricao;

    @Column(name = "ja_fez_pix", nullable = false)
    private boolean jaFezPix;

    @Column(name = "criada_em", nullable = false)
    private Instant criadaEm = Instant.now();

    protected Denuncia() {
    }

    public Denuncia(String chaveHash, String descricao, boolean jaFezPix) {
        this.chaveHash = chaveHash;
        this.descricao = descricao;
        this.jaFezPix = jaFezPix;
    }

    public Long getId() { return id; }
    public String getChaveHash() { return chaveHash; }
    public String getDescricao() { return descricao; }
    public boolean isJaFezPix() { return jaFezPix; }
    public Instant getCriadaEm() { return criadaEm; }
}
