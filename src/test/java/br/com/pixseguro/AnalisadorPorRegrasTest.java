package br.com.pixseguro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.pixseguro.domain.Risco;
import br.com.pixseguro.ia.AnalisadorPorRegras;
import br.com.pixseguro.ia.ResultadoAnalise;
import org.junit.jupiter.api.Test;

class AnalisadorPorRegrasTest {

    private final AnalisadorPorRegras analisador = new AnalisadorPorRegras();

    @Test
    void falsoParenteComUrgenciaEAltoRisco() {
        ResultadoAnalise r = analisador.analisar("Oi mãe, mudei de número, me faz um Pix urgente?");
        assertEquals(Risco.ALTO, r.risco());
        assertTrue(r.sinais().size() >= 3);
    }

    @Test
    void mensagemNormalEBaixoRisco() {
        ResultadoAnalise r = analisador.analisar("Oi, vamos almoçar amanhã?");
        assertEquals(Risco.BAIXO, r.risco());
        assertTrue(r.sinais().isEmpty());
    }

    @Test
    void pedidoDeCodigoEAltoRisco() {
        assertEquals(Risco.ALTO, analisador.analisar("Me passa o código de verificação que chegou no SMS").risco());
    }
}
