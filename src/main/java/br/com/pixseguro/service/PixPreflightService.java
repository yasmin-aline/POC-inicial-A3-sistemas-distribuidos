package br.com.pixseguro.service;

import br.com.pixseguro.domain.PixTentativa;
import br.com.pixseguro.domain.PixTentativaRepository;
import br.com.pixseguro.domain.Risco;
import br.com.pixseguro.web.Dtos.DecisaoPreflight;
import br.com.pixseguro.web.Dtos.PreflightPixRequest;
import br.com.pixseguro.web.Dtos.PreflightPixResponse;
import br.com.pixseguro.web.Dtos.SinalRisco;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Orquestra o preflight fictício; não inicia nem bloqueia transações Pix reais. */
@Service
public class PixPreflightService {

    private final AnaliseMensagemService analiseMensagem;
    private final ChaveService chaveService;
    private final ChaveHasher hasher;
    private final PixTentativaRepository tentativas;
    private final ObjectMapper mapper;

    public PixPreflightService(AnaliseMensagemService analiseMensagem, ChaveService chaveService,
                               ChaveHasher hasher, PixTentativaRepository tentativas, ObjectMapper mapper) {
        this.analiseMensagem = analiseMensagem;
        this.chaveService = chaveService;
        this.hasher = hasher;
        this.tentativas = tentativas;
        this.mapper = mapper;
    }

    @Transactional
    public PreflightPixResponse analisar(PreflightPixRequest req) {
        List<SinalRisco> sinais = new ArrayList<>();
        int score = 0;

        if (!normalizarNome(req.nomeEsperado()).equals(normalizarNome(req.nomeRecebedor()))) {
            sinais.add(new SinalRisco("NOME_DIVERGENTE", "SIMULADOR_BANCARIO", 35,
                    "O nome esperado e o nome que aparece para o recebedor não correspondem."));
            score += 35;
        }
        if (req.novoDestinatario()) {
            sinais.add(new SinalRisco("DESTINATARIO_NOVO", "SIMULADOR_BANCARIO", 15,
                    "O destinatário não aparece como conhecido no histórico sintético."));
            score += 15;
        }
        if (!req.dispositivoConfiavel()) {
            sinais.add(new SinalRisco("DISPOSITIVO_NAO_CONHECIDO", "SIMULADOR_BANCARIO", 15,
                    "A tentativa veio de um dispositivo marcado como não conhecido na simulação."));
            score += 15;
        }
        if (req.valorForaDoPerfil()) {
            sinais.add(new SinalRisco("VALOR_FORA_DO_PERFIL", "SIMULADOR_BANCARIO", 20,
                    "O valor foi marcado como fora do perfil fictício desta conta."));
            score += 20;
        }

        var chave = chaveService.consultar(req.chave());
        if (chave.totalDenuncias() > 0) {
            int pontos = chave.totalDenuncias() >= 3 ? 40 : 20;
            sinais.add(new SinalRisco("RELATOS_COMUNITARIOS", "BASE_LOCAL_DA_POC", pontos,
                    chave.totalDenuncias() + " relato(s) local(is) associado(s) à chave. Não são verificação oficial nem prova de fraude."));
            score += pontos;
        }

        boolean iaDisponivel = false;
        if (req.mensagemSuspeita() != null && !req.mensagemSuspeita().isBlank()) {
            var analise = analiseMensagem.analisar(req.mensagemSuspeita());
            iaDisponivel = analise.iaDisponivel();
            int pontosMensagem = switch (analise.risco()) {
                case ALTO -> 35;
                case MEDIO -> 20;
                case BAIXO -> 0;
            };
            if (pontosMensagem > 0) {
                List<String> explicacoes = analise.sinais() == null || analise.sinais().isEmpty()
                        ? List.of("A análise textual encontrou sinais de risco.") : analise.sinais();
                sinais.add(new SinalRisco("RISCO_DA_MENSAGEM", "ANALISE_" + analise.motor(), pontosMensagem,
                        "A análise textual classificou a mensagem como risco " + analise.risco() + "."));
                for (String explicacao : explicacoes) {
                    sinais.add(new SinalRisco("EVIDENCIA_MENSAGEM", "ANALISE_" + analise.motor(), 0, explicacao));
                }
                score += pontosMensagem;
            }
        }

        score = Math.min(100, score);
        Risco risco = score >= 60 ? Risco.ALTO : (score >= 20 ? Risco.MEDIO : Risco.BAIXO);
        DecisaoPreflight decisao = switch (risco) {
            case ALTO -> DecisaoPreflight.NAO_CONFIRMAR_E_PEDIR_AJUDA;
            case MEDIO -> DecisaoPreflight.PAUSAR_E_CONFIRMAR;
            case BAIXO -> DecisaoPreflight.SEGUIR_COM_CAUTELA;
        };
        List<String> recomendacoes = recomendacoes(decisao);
        Instant agora = Instant.now();
        String id = UUID.randomUUID().toString();
        String sinaisJson;
        try {
            sinaisJson = mapper.writeValueAsString(sinais);
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível registrar os sinais da simulação", e);
        }
        tentativas.save(new PixTentativa(id, hasher.hash(req.chave()), req.valor(), risco, score,
                decisao, sinaisJson, agora));

        String aviso = "Pré-análise sintética: nenhuma transferência foi iniciada ou bloqueada. "
                + (req.mensagemSuspeita() != null && !iaDisponivel
                ? "A camada de linguagem usou regras locais ou está configurada sem Gemini. " : "")
                + "Denúncias locais são relatos não verificados; risco baixo não garante segurança.";
        return new PreflightPixResponse(id, risco, score, decisao, false, List.copyOf(sinais),
                recomendacoes, agora, aviso);
    }

    @Transactional(readOnly = true)
    public PreflightPixResponse consultar(String id) {
        PixTentativa tentativa = tentativas.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tentativa simulada não encontrada"));
        try {
            List<SinalRisco> sinais = mapper.readValue(tentativa.getSinaisJson(), new TypeReference<>() {});
            return new PreflightPixResponse(tentativa.getId(), tentativa.getRisco(), tentativa.getPontuacao(),
                    tentativa.getDecisao(), false, sinais, recomendacoes(tentativa.getDecisao()),
                    tentativa.getCriadaEm(), "Registro da simulação. Nenhuma transferência foi executada.");
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível ler o registro da simulação", e);
        }
    }

    private static List<String> recomendacoes(DecisaoPreflight decisao) {
        return switch (decisao) {
            case NAO_CONFIRMAR_E_PEDIR_AJUDA -> List.of(
                    "Não confirme esta tentativa simulada agora.",
                    "Ligue para a pessoa usando um número que você já conhecia; não use o contato enviado na mensagem suspeita.",
                    "Confira o nome do recebedor no aplicativo da sua instituição.");
            case PAUSAR_E_CONFIRMAR -> List.of(
                    "Pause e confirme o pedido por um canal independente que você já conhecia.",
                    "Confira com cuidado o nome do recebedor antes de qualquer confirmação.");
            case SEGUIR_COM_CAUTELA -> List.of(
                    "Poucos sinais foram encontrados nesta simulação; isso não prova que seja seguro.",
                    "Confira o nome do recebedor e o valor no aplicativo da sua instituição antes de confirmar.");
        };
    }

    private static String normalizarNome(String nome) {
        String semAcento = Normalizer.normalize(nome, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }
}
