package br.com.pixseguro.domain;

public enum Risco {
    BAIXO, MEDIO, ALTO;

    /** Regra da consulta de chave: 0 denuncias = baixo, 1 a 2 = medio, 3 ou mais = alto. */
    public static Risco deDenuncias(long total) {
        if (total >= 3) return ALTO;
        if (total >= 1) return MEDIO;
        return BAIXO;
    }
}
