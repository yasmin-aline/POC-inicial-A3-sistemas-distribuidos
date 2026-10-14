package br.com.pixseguro.web;

import br.com.pixseguro.domain.ConfirmacaoOutroCanal;
import br.com.pixseguro.domain.ProximoPasso;
import br.com.pixseguro.domain.Risco;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class Dtos {

    private Dtos() {}

    public record ConsultaChaveRequest(@NotBlank @Size(max = 140) String chave) {}

    public record ConsultaChaveResponse(Risco risco, long totalDenuncias, Instant ultimaDenunciaEm,
                                        String mensagem, ProximoPasso proximoPasso) {}

    public record AnaliseMensagemRequest(@NotBlank @Size(max = 2000) String texto) {}

    public record AnaliseMensagemResponse(boolean iaDisponivel, String motor, Risco risco, Integer pontuacao,
                                          List<String> sinais, String dica, ProximoPasso proximoPasso,
                                          String aviso) {}

    public record Pergunta(int id, String texto) {}

    public record AutodiagnosticoRequest(@NotNull @Size(min = 5, max = 5) List<@NotNull Boolean> respostas) {}

    public record AutodiagnosticoResponse(int pontuacao, Risco risco, List<String> recomendacoes,
                                          ProximoPasso proximoPasso) {}

    public record DecisaoRequest(@NotNull Risco risco, ConfirmacaoOutroCanal confirmacao) {}

    public record DecisaoResponse(ProximoPasso proximoPasso, String orientacao) {}

    public record DenunciaRequest(@NotBlank @Size(max = 140) String chave, @Size(max = 500) String descricao,
                                  boolean jaFezPix) {}

    public record DenunciaResponse(long totalDenunciasChave, Risco risco, boolean alertaPublicado,
                                   ProximoPasso proximoPasso, List<String> orientacoes) {}

    /** Todos os campos descrevem uma tentativa fictícia; nunca representam uma ordem de pagamento real. */
    public record PreflightPixRequest(
            @NotNull @DecimalMin(value = "0.01", message = "O valor deve ser positivo") BigDecimal valor,
            @NotBlank @Size(max = 140) String chave,
            @NotBlank @Size(max = 120) String nomeEsperado,
            @NotBlank @Size(max = 120) String nomeRecebedor,
            boolean novoDestinatario,
            boolean dispositivoConfiavel,
            boolean valorForaDoPerfil,
            @Size(max = 2000) String mensagemSuspeita) {}

    public record SinalRisco(String codigo, String origem, int pontos, String explicacao) {}

    public record PreflightPixResponse(String tentativaId, Risco risco, int pontuacao,
                                       DecisaoPreflight decisao, boolean transferenciaExecutada,
                                       List<SinalRisco> sinais, List<String> recomendacoes,
                                       Instant criadaEm, String aviso) {}

    public record CenarioDemo(String id, String nome, String descricao, PreflightPixRequest solicitacao) {}

    public enum DecisaoPreflight {
        SEGUIR_COM_CAUTELA,
        PAUSAR_E_CONFIRMAR,
        NAO_CONFIRMAR_E_PEDIR_AJUDA
    }
}
