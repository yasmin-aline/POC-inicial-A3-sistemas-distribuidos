package br.com.pixseguro;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "pixseguro.ia.simular-indisponivel=true")
@AutoConfigureMockMvc
class PixPreflightTest {

    @Autowired
    MockMvc mvc;

    @Test
    void listaCenariosCarregadosPelasMigrations() throws Exception {
        mvc.perform(get("/api/sandbox/pix/cenarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[?(@.id == 'falso-parente')]" ).exists())
                .andExpect(jsonPath("$[?(@.id == 'normal')]" ).exists());
    }

    @Test
    void tentativaNormalRetornaRiscoBaixoMasNaoExecutaTransferencia() throws Exception {
        mvc.perform(post("/api/sandbox/pix/preflight").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"valor":45.90,"chave":"padaria@demo.invalid","nomeEsperado":"Padaria Central",
                                 "nomeRecebedor":"Padaria Central","novoDestinatario":false,
                                 "dispositivoConfiavel":true,"valorForaDoPerfil":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.risco").value("BAIXO"))
                .andExpect(jsonPath("$.decisao").value("SEGUIR_COM_CAUTELA"))
                .andExpect(jsonPath("$.pontuacao").value(0))
                .andExpect(jsonPath("$.transferenciaExecutada").value(false));
    }

    @Test
    void destinatarioNovoEDispositivoDesconhecidoExigemPausa() throws Exception {
        mvc.perform(post("/api/sandbox/pix/preflight").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"valor":320.00,"chave":"loja@demo.invalid","nomeEsperado":"Loja Exemplo",
                                 "nomeRecebedor":"Loja Exemplo","novoDestinatario":true,
                                 "dispositivoConfiavel":false,"valorForaDoPerfil":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.risco").value("MEDIO"))
                .andExpect(jsonPath("$.pontuacao").value(30))
                .andExpect(jsonPath("$.decisao").value("PAUSAR_E_CONFIRMAR"));
    }

    @Test
    void falsoParenteComNomeDivergenteEhRiscoAltoExplicavel() throws Exception {
        var resultado = mvc.perform(post("/api/sandbox/pix/preflight").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"valor":4500.00,"chave":"conta-nova@demo.invalid","nomeEsperado":"Ana Souza",
                                 "nomeRecebedor":"Marcos Lima","novoDestinatario":true,
                                 "dispositivoConfiavel":false,"valorForaDoPerfil":true,
                                 "mensagemSuspeita":"Oi mãe, mudei de número. Preciso de um Pix urgente, não liga agora."}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.risco").value("ALTO"))
                .andExpect(jsonPath("$.pontuacao").value(100))
                .andExpect(jsonPath("$.decisao").value("NAO_CONFIRMAR_E_PEDIR_AJUDA"))
                .andExpect(jsonPath("$.transferenciaExecutada").value(false))
                .andExpect(jsonPath("$.sinais[?(@.codigo == 'NOME_DIVERGENTE')]" ).exists())
                .andExpect(jsonPath("$.sinais[?(@.codigo == 'EVIDENCIA_MENSAGEM')]" ).exists())
                .andReturn();

        String body = resultado.getResponse().getContentAsString();
        String id = com.jayway.jsonpath.JsonPath.read(body, "$.tentativaId");
        mvc.perform(get("/api/sandbox/pix/tentativas/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.risco").value("ALTO"))
                .andExpect(jsonPath("$.transferenciaExecutada").value(false));
    }

    @Test
    void relatosComunitariosLocaisAumentamSinalMasNaoSaoProvaOficial() throws Exception {
        mvc.perform(post("/api/sandbox/pix/preflight").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"valor":780.00,"chave":"golpista@exemplo.com","nomeEsperado":"Loja Exemplo",
                                 "nomeRecebedor":"Loja Exemplo","novoDestinatario":false,
                                 "dispositivoConfiavel":true,"valorForaDoPerfil":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.risco").value("MEDIO"))
                .andExpect(jsonPath("$.sinais[?(@.codigo == 'RELATOS_COMUNITARIOS')]" ).exists())
                .andExpect(jsonPath("$.aviso").value(org.hamcrest.Matchers.containsString("não garante segurança")));
    }

    @Test
    void falsaCentralQuePedeCodigoElevaRiscoEExibeEvidencias() throws Exception {
        mvc.perform(post("/api/sandbox/pix/preflight").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"valor":1250.00,"chave":"suporte@demo.invalid","nomeEsperado":"Banco Exemplo",
                                 "nomeRecebedor":"Pessoa Física Exemplo","novoDestinatario":true,
                                 "dispositivoConfiavel":false,"valorForaDoPerfil":true,
                                 "mensagemSuspeita":"Sou da central de segurança. Envie o código de verificação por SMS para evitar o bloqueio da conta."}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.risco").value("ALTO"))
                .andExpect(jsonPath("$.decisao").value("NAO_CONFIRMAR_E_PEDIR_AJUDA"))
                .andExpect(jsonPath("$.sinais[?(@.codigo == 'RISCO_DA_MENSAGEM')]" ).exists());
    }

    @Test
    void mensagemDeRiscoMedioContinuaComoRiscoMedioNoPreflight() throws Exception {
        mvc.perform(post("/api/sandbox/pix/preflight").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"valor":70.00,"chave":"amigo@demo.invalid","nomeEsperado":"Amigo",
                                 "nomeRecebedor":"Amigo","novoDestinatario":false,
                                 "dispositivoConfiavel":true,"valorForaDoPerfil":false,
                                 "mensagemSuspeita":"É urgente, pode conversar?"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.risco").value("MEDIO"))
                .andExpect(jsonPath("$.pontuacao").value(20))
                .andExpect(jsonPath("$.decisao").value("PAUSAR_E_CONFIRMAR"));
    }

    @Test
    void nomesComAcentuacaoEquivalenteNaoGeramDivergencia() throws Exception {
        mvc.perform(post("/api/sandbox/pix/preflight").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"valor":30.00,"chave":"cafe@demo.invalid","nomeEsperado":"Café da Praça",
                                 "nomeRecebedor":"Cafe da Praca","novoDestinatario":false,
                                 "dispositivoConfiavel":true,"valorForaDoPerfil":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.risco").value("BAIXO"))
                .andExpect(jsonPath("$.sinais[?(@.codigo == 'NOME_DIVERGENTE')]" ).doesNotExist());
    }

    @Test
    void valorInvalidoRetornaBadRequest() throws Exception {
        mvc.perform(post("/api/sandbox/pix/preflight").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"valor":0,"chave":"teste@demo.invalid","nomeEsperado":"Loja",
                                 "nomeRecebedor":"Loja","novoDestinatario":false,
                                 "dispositivoConfiavel":true,"valorForaDoPerfil":false}
                                """))
                .andExpect(status().isBadRequest());
    }
}
