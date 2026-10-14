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

@SpringBootTest
@AutoConfigureMockMvc
class FluxoCompletoTest {

    @Autowired
    MockMvc mvc;

    @Test
    void chaveDenunciadaTresVezesViraRiscoAltoParaOsProximos() throws Exception {
        for (int i = 0; i < 3; i++) {
            mvc.perform(post("/api/denuncias").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"chave\":\"teste.fluxo@exemplo.com\",\"descricao\":\"golpe\",\"jaFezPix\":false}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.proximoPasso").value("ALERTA_PARA_PROXIMOS_USUARIOS"));
        }
        mvc.perform(post("/api/chaves/consulta").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chave\":\" TESTE.fluxo@exemplo.com \"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.risco").value("ALTO"))
                .andExpect(jsonPath("$.totalDenuncias").value(3));
    }

    @Test
    void denunciaDeQuemJaPagouOrientaMedEBo() throws Exception {
        mvc.perform(post("/api/denuncias").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"chave\":\"11999990000\",\"jaFezPix\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.proximoPasso").value("PEDIR_DEVOLUCAO_MED_E_REGISTRAR_BO"));
    }

    @Test
    void autodiagnosticoFuncionaDePontaAPonta() throws Exception {
        mvc.perform(get("/api/autodiagnostico/perguntas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5));
        mvc.perform(post("/api/autodiagnostico").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"respostas\":[true,true,false,false,false]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.risco").value("MEDIO"));
    }

    @Test
    void validacaoRetorna400() throws Exception {
        mvc.perform(post("/api/chaves/consulta").contentType(MediaType.APPLICATION_JSON).content("{\"chave\":\"\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/autodiagnostico").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"respostas\":[true]}"))
                .andExpect(status().isBadRequest());
    }
}
