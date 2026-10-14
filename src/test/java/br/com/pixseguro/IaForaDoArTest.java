package br.com.pixseguro;

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
class IaForaDoArTest {

    @Autowired
    MockMvc mvc;

    @Test
    void geminiForaDoArUsaRegrasLocaisEContinuaOrientando() throws Exception {
        mvc.perform(post("/api/mensagens/analise").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\":\"Oi, mudei de número, me faz um Pix urgente?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iaDisponivel").value(false))
                .andExpect(jsonPath("$.motor").value("REGRAS"))
                .andExpect(jsonPath("$.risco").value("ALTO"))
                .andExpect(jsonPath("$.proximoPasso").value("NAO_PAGAR_E_CONFIRMAR_POR_OUTRO_CANAL"));
    }
}
