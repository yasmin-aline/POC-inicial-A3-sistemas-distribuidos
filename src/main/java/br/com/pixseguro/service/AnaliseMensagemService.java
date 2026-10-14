package br.com.pixseguro.service;

import br.com.pixseguro.domain.ProximoPasso;
import br.com.pixseguro.ia.AnalisadorClaude;
import br.com.pixseguro.ia.AnalisadorPorRegras;
import br.com.pixseguro.ia.IaIndisponivelException;
import br.com.pixseguro.ia.ResultadoAnalise;
import br.com.pixseguro.web.Dtos.AnaliseMensagemResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Ramo "Recebi uma mensagem": cola o texto, a IA analisa e mostra risco, sinais e dica. */
@Service
public class AnaliseMensagemService {

    private final AnalisadorPorRegras regras;
    private final AnalisadorClaude claude;
    private final boolean simularIndisponivel;

    public AnaliseMensagemService(AnalisadorPorRegras regras, AnalisadorClaude claude,
                                  @Value("${pixseguro.ia.simular-indisponivel:false}") boolean simularIndisponivel) {
        this.regras = regras;
        this.claude = claude;
        this.simularIndisponivel = simularIndisponivel;
    }

    public AnaliseMensagemResponse analisar(String texto) {
        if (simularIndisponivel) {
            return iaForaDoAr();
        }
        try {
            ResultadoAnalise r = claude.habilitado() ? claude.analisar(texto) : regras.analisar(texto);
            String aviso = AnalisadorPorRegras.MOTOR.equals(r.motor())
                    ? "Análise feita pelo motor de regras local, sem IA. Defina ANTHROPIC_API_KEY para usar IA."
                    : null;
            return new AnaliseMensagemResponse(true, r.motor(), r.risco(), r.pontuacao(), r.sinais(),
                    r.dica(), ProximoPasso.paraRisco(r.risco()), aviso);
        } catch (IaIndisponivelException e) {
            return iaForaDoAr();
        }
    }

    /** Linha pontilhada do fluxograma: "IA fora do ar" leva para digitar a chave Pix. */
    private AnaliseMensagemResponse iaForaDoAr() {
        return new AnaliseMensagemResponse(false, null, null, null, null, null,
                ProximoPasso.CONSULTAR_CHAVE,
                "A análise por IA está fora do ar. Digite a chave Pix para ver as denúncias.");
    }
}
