package br.com.pixseguro;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.pixseguro.domain.ConfirmacaoOutroCanal;
import br.com.pixseguro.domain.ProximoPasso;
import br.com.pixseguro.domain.Risco;
import br.com.pixseguro.service.AutodiagnosticoService;
import br.com.pixseguro.service.DecisaoService;
import java.util.List;
import org.junit.jupiter.api.Test;

class ServicosTest {

    private final AutodiagnosticoService auto = new AutodiagnosticoService();
    private final DecisaoService decisao = new DecisaoService();

    @Test
    void riscoPorDenuncias() {
        assertEquals(Risco.BAIXO, Risco.deDenuncias(0));
        assertEquals(Risco.MEDIO, Risco.deDenuncias(2));
        assertEquals(Risco.ALTO, Risco.deDenuncias(3));
    }

    @Test
    void autodiagnosticoPontuaERecomenda() {
        var r = auto.avaliar(List.of(true, true, true, false, false));
        assertEquals(60, r.pontuacao());
        assertEquals(Risco.ALTO, r.risco());
        assertEquals(ProximoPasso.NAO_PAGAR_E_CONFIRMAR_POR_OUTRO_CANAL, r.proximoPasso());
    }

    @Test
    void autodiagnosticoExigeCincoRespostas() {
        assertThrows(IllegalArgumentException.class, () -> auto.avaliar(List.of(true, false)));
    }

    @Test
    void decisaoSegueOFluxograma() {
        assertEquals(ProximoPasso.SEGUIR_COM_PAGAMENTO_CONFERINDO_NOME,
                decisao.decidir(Risco.BAIXO, null).proximoPasso());
        assertEquals(ProximoPasso.NAO_PAGAR_E_CONFIRMAR_POR_OUTRO_CANAL,
                decisao.decidir(Risco.ALTO, null).proximoPasso());
        assertEquals(ProximoPasso.DENUNCIAR_CHAVE,
                decisao.decidir(Risco.MEDIO, ConfirmacaoOutroCanal.NAO_DEU_PARA_CONFIRMAR).proximoPasso());
        assertEquals(ProximoPasso.SEGUIR_COM_PAGAMENTO_CONFERINDO_NOME,
                decisao.decidir(Risco.ALTO, ConfirmacaoOutroCanal.NAO_E_GOLPE).proximoPasso());
    }
}
