package br.com.pixseguro.service;

import br.com.pixseguro.domain.ProximoPasso;
import br.com.pixseguro.ia.AnalisadorGemini;
import br.com.pixseguro.ia.AnalisadorPorRegras;
import br.com.pixseguro.ia.IaIndisponivelException;
import br.com.pixseguro.ia.ResultadoAnalise;
import br.com.pixseguro.web.Dtos.AnaliseMensagemResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/** Ramo "Recebi uma mensagem": análise por Gemini, com fallback local determinístico. */
@Service
public class AnaliseMensagemService {

    private final AnalisadorPorRegras regras;
    private final AnalisadorGemini gemini;
    private final boolean simularIndisponivel;

    public AnaliseMensagemService(AnalisadorPorRegras regras, AnalisadorGemini gemini,
                                  @Value("${pixseguro.ia.simular-indisponivel:false}") boolean simularIndisponivel) {
        this.regras = regras;
        this.gemini = gemini;
        this.simularIndisponivel = simularIndisponivel;
    }

    public AnaliseMensagemResponse analisar(String texto) {
        if (gemini.habilitado() && !simularIndisponivel) {
            try {
                return resposta(gemini.analisar(texto), true, null);
            } catch (IaIndisponivelException e) {
                return resposta(regras.analisar(texto), false,
                        "Gemini indisponível; análise local por regras foi usada como alternativa.");
            }
        }

        ResultadoAnalise local = regras.analisar(texto);
        String aviso = simularIndisponivel
                ? "Gemini indisponível; análise local por regras foi usada como alternativa."
                : "Análise feita pelo motor de regras local. Configure GEMINI_API_KEY para habilitar o Gemini.";
        return resposta(local, false, aviso);
    }

    private AnaliseMensagemResponse resposta(ResultadoAnalise resultado, boolean iaDisponivel, String aviso) {
        return new AnaliseMensagemResponse(iaDisponivel, resultado.motor(), resultado.risco(),
                resultado.pontuacao(), resultado.sinais(), resultado.dica(),
                ProximoPasso.paraRisco(resultado.risco()), aviso);
    }
}
