package br.com.pixseguro.service;

import br.com.pixseguro.domain.ConfirmacaoOutroCanal;
import br.com.pixseguro.domain.ProximoPasso;
import br.com.pixseguro.domain.Risco;
import br.com.pixseguro.web.Dtos.DecisaoResponse;
import org.springframework.stereotype.Service;

/** Nós "Qual o risco?" e "É golpe?" do fluxograma. */
@Service
public class DecisaoService {

    public DecisaoResponse decidir(Risco risco, ConfirmacaoOutroCanal confirmacao) {
        if (risco == Risco.BAIXO) {
            return seguir();
        }
        if (confirmacao == null) {
            return new DecisaoResponse(ProximoPasso.NAO_PAGAR_E_CONFIRMAR_POR_OUTRO_CANAL,
                    "Risco " + risco.name().toLowerCase() + ". Não pague e confirme por outro canal (ligação ou pessoalmente).");
        }
        return switch (confirmacao) {
            case E_GOLPE, NAO_DEU_PARA_CONFIRMAR -> new DecisaoResponse(ProximoPasso.DENUNCIAR_CHAVE,
                    "Sem confirmação segura, trate como golpe. Denuncie a chave para alertar os próximos usuários.");
            case NAO_E_GOLPE -> seguir();
        };
    }

    private DecisaoResponse seguir() {
        return new DecisaoResponse(ProximoPasso.SEGUIR_COM_PAGAMENTO_CONFERINDO_NOME,
                "Pode seguir com o pagamento, conferindo o nome do recebedor antes de confirmar.");
    }
}
