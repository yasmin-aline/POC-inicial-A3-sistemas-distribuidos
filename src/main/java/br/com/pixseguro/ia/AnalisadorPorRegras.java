package br.com.pixseguro.ia;

import br.com.pixseguro.domain.Risco;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Motor local, sem IA: procura padrões comuns de golpe no texto.
 * Serve como fallback e para rodar a PoC sem chave de API.
 */
@Component
public class AnalisadorPorRegras {

    public static final String MOTOR = "REGRAS";

    private record Regra(Pattern padrao, int peso, String sinal) {}

    private static Regra r(String regex, int peso, String sinal) {
        return new Regra(Pattern.compile(regex), peso, sinal);
    }

    // Os padrões já estão sem acento e em minúsculas, o texto é normalizado antes.
    private static final List<Regra> REGRAS = List.of(
            r("\\b(urgente|urgencia|imediatamente|ultimo dia|so hoje|rapido|agora mesmo)\\b", 2,
                    "Pressão de urgência"),
            r("(mudei de (numero|celular)|novo numero|numero novo|troquei de (numero|celular)|celular (quebrou|caiu))", 3,
                    "Diz ter trocado de número (golpe do falso parente)"),
            r("(me empresta|me ajuda com|preciso de um pix|faz um pix|me manda um pix|paga (pra|para) mim|pode pagar)", 2,
                    "Pedido direto de dinheiro"),
            r("(voce ganhou|parabens.{0,30}ganhou|premio|sorteio|renda extra|lucro garantido|investimento garantido|dobrar (seu|o) dinheiro)", 3,
                    "Promessa de prêmio ou ganho fácil"),
            r("(taxa|tarifa|custas?)\\b.{0,40}\\b(liberar|liberacao|desbloque\\w*|receber)", 3,
                    "Cobrança de taxa para liberar valor"),
            r("(conta (sera|foi|esta|ficara) (bloqueada|suspensa|encerrada)|bloqueio da sua conta|cpf (irregular|suspenso|cancelado))", 3,
                    "Ameaça de bloqueio ou irregularidade"),
            r("(https?://|www\\.|bit\\.ly|tinyurl|wa\\.me)", 2,
                    "Contém link"),
            r("\\b(codigo de verificacao|token|senha|cvv)\\b", 4,
                    "Pede código, token ou senha"),
            r("(nao (conte|fale|comente)|nao conta pra ninguem|segredo|so entre nos)", 3,
                    "Pede sigilo"),
            r("(central de seguranca|setor de seguranca|equipe de seguranca|suporte (do|da) (banco|app)|gerente do seu banco)", 2,
                    "Diz ser do banco ou da central de segurança"),
            r("(pix (foi )?(errado|por engano)|caiu na minha conta por engano|devolve (o )?(valor|dinheiro)|devolver o (valor|dinheiro))", 3,
                    "História de Pix feito por engano"),
            r("(fora d[ao] (plataforma|app|site|aplicativo)|direto comigo|negociar por fora|pagar por fora)", 2,
                    "Quer pagar fora do canal oficial")
    );

    public ResultadoAnalise analisar(String texto) {
        String t = normalizar(texto);
        List<String> sinais = new ArrayList<>();
        int pontos = 0;
        for (Regra regra : REGRAS) {
            if (regra.padrao().matcher(t).find()) {
                pontos += regra.peso();
                sinais.add(regra.sinal());
            }
        }

        Risco risco = pontos >= 4 ? Risco.ALTO : (pontos >= 2 ? Risco.MEDIO : Risco.BAIXO);
        String dica = switch (risco) {
            case ALTO -> "Não pague. Fale com a pessoa ou empresa por outro canal (ligação ou pessoalmente) e consulte a chave no Pix Seguro.";
            case MEDIO -> "Desconfie. Confirme por outro canal antes de pagar e confira o nome do recebedor.";
            case BAIXO -> "Não encontramos sinais comuns de golpe, mas confira o nome do recebedor antes de confirmar.";
        };
        return new ResultadoAnalise(risco, pontos, sinais, dica, MOTOR);
    }

    static String normalizar(String texto) {
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toLowerCase(Locale.ROOT);
    }
}
