package br.com.pixseguro.domain;

/** Os "nos" de decisao e de acao do fluxograma, para o cliente saber o que mostrar em seguida. */
public enum ProximoPasso {
    CONSULTAR_CHAVE,
    NAO_PAGAR_E_CONFIRMAR_POR_OUTRO_CANAL,
    DENUNCIAR_CHAVE,
    SEGUIR_COM_PAGAMENTO_CONFERINDO_NOME,
    PEDIR_DEVOLUCAO_MED_E_REGISTRAR_BO,
    ALERTA_PARA_PROXIMOS_USUARIOS;

    /** Nó "Qual o risco?": baixo segue, medio ou alto nao paga e confirma por outro canal. */
    public static ProximoPasso paraRisco(Risco risco) {
        return risco == Risco.BAIXO
                ? SEGUIR_COM_PAGAMENTO_CONFERINDO_NOME
                : NAO_PAGAR_E_CONFIRMAR_POR_OUTRO_CANAL;
    }
}
