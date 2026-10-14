package br.com.pixseguro.service;

import br.com.pixseguro.domain.ProximoPasso;
import br.com.pixseguro.domain.Risco;
import br.com.pixseguro.web.Dtos.AutodiagnosticoResponse;
import br.com.pixseguro.web.Dtos.Pergunta;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

/** Ramo "Não sei bem": 5 perguntas de sim ou não, pontuação de risco e recomendações. */
@Service
public class AutodiagnosticoService {

    private record Item(int id, String texto, String recomendacao) {}

    private static final List<Item> ITENS = List.of(
            new Item(1, "Estão pedindo urgência, dizendo que você precisa pagar agora?",
                    "Pressa é a principal arma do golpista. Pare e só pague depois de confirmar por outro canal."),
            new Item(2, "O pedido veio de um número ou perfil novo, ou de alguém que disse ter mudado de número?",
                    "Ligue para a pessoa no número antigo, ou fale pessoalmente, antes de pagar."),
            new Item(3, "O nome do recebedor que aparece no app é diferente do da pessoa ou empresa com quem você falou?",
                    "Não pague se o nome não bate. Peça os dados por um canal oficial."),
            new Item(4, "A oferta parece boa demais para ser verdade (prêmio, desconto enorme, lucro garantido)?",
                    "Promessa de ganho fácil é sinal clássico de golpe. Pesquise o CNPJ e a reputação."),
            new Item(5, "Pediram segredo, um código recebido por SMS ou para pagar fora do canal oficial?",
                    "Nunca compartilhe códigos ou senhas e evite pagar fora da plataforma oficial.")
    );

    public List<Pergunta> perguntas() {
        return ITENS.stream().map(i -> new Pergunta(i.id(), i.texto())).toList();
    }

    public AutodiagnosticoResponse avaliar(List<Boolean> respostas) {
        if (respostas == null || respostas.size() != ITENS.size()) {
            throw new IllegalArgumentException("Envie exatamente " + ITENS.size() + " respostas.");
        }

        List<String> recomendacoes = new ArrayList<>();
        int sim = 0;
        for (int i = 0; i < ITENS.size(); i++) {
            if (Boolean.TRUE.equals(respostas.get(i))) {
                sim++;
                recomendacoes.add(ITENS.get(i).recomendacao());
            }
        }

        Risco risco = sim <= 1 ? Risco.BAIXO : (sim == 2 ? Risco.MEDIO : Risco.ALTO);

        if (risco == Risco.BAIXO) {
            recomendacoes.add("Poucos sinais de alerta. Mesmo assim, confira o nome do recebedor antes de confirmar.");
        } else {
            recomendacoes.add("Não pague agora. Confirme por outro canal e, se puder, consulte a chave no Pix Seguro.");
        }

        return new AutodiagnosticoResponse(sim * 20, risco, recomendacoes, ProximoPasso.paraRisco(risco));
    }
}
