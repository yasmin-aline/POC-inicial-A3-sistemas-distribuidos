package br.com.pixseguro.web;

import br.com.pixseguro.service.AnaliseMensagemService;
import br.com.pixseguro.service.AutodiagnosticoService;
import br.com.pixseguro.service.ChaveService;
import br.com.pixseguro.service.DecisaoService;
import br.com.pixseguro.service.DenunciaService;
import br.com.pixseguro.web.Dtos.AnaliseMensagemRequest;
import br.com.pixseguro.web.Dtos.AnaliseMensagemResponse;
import br.com.pixseguro.web.Dtos.AutodiagnosticoRequest;
import br.com.pixseguro.web.Dtos.AutodiagnosticoResponse;
import br.com.pixseguro.web.Dtos.ConsultaChaveRequest;
import br.com.pixseguro.web.Dtos.ConsultaChaveResponse;
import br.com.pixseguro.web.Dtos.DecisaoRequest;
import br.com.pixseguro.web.Dtos.DecisaoResponse;
import br.com.pixseguro.web.Dtos.DenunciaRequest;
import br.com.pixseguro.web.Dtos.DenunciaResponse;
import br.com.pixseguro.web.Dtos.Pergunta;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PixSeguroController {

    private final ChaveService chaveService;
    private final AnaliseMensagemService analiseService;
    private final AutodiagnosticoService autodiagnosticoService;
    private final DecisaoService decisaoService;
    private final DenunciaService denunciaService;

    public PixSeguroController(ChaveService chaveService, AnaliseMensagemService analiseService,
                               AutodiagnosticoService autodiagnosticoService, DecisaoService decisaoService,
                               DenunciaService denunciaService) {
        this.chaveService = chaveService;
        this.analiseService = analiseService;
        this.autodiagnosticoService = autodiagnosticoService;
        this.decisaoService = decisaoService;
        this.denunciaService = denunciaService;
    }

    /** Ramo "Tenho a chave". */
    @PostMapping("/chaves/consulta")
    public ConsultaChaveResponse consultarChave(@Valid @RequestBody ConsultaChaveRequest req) {
        return chaveService.consultar(req.chave());
    }

    /** Ramo "Recebi uma mensagem". */
    @PostMapping("/mensagens/analise")
    public AnaliseMensagemResponse analisarMensagem(@Valid @RequestBody AnaliseMensagemRequest req) {
        return analiseService.analisar(req.texto());
    }

    /** Ramo "Não sei bem", passo 1: as 5 perguntas. */
    @GetMapping("/autodiagnostico/perguntas")
    public List<Pergunta> perguntas() {
        return autodiagnosticoService.perguntas();
    }

    /** Ramo "Não sei bem", passo 2: pontuação e recomendações. */
    @PostMapping("/autodiagnostico")
    public AutodiagnosticoResponse autodiagnostico(@Valid @RequestBody AutodiagnosticoRequest req) {
        return autodiagnosticoService.avaliar(req.respostas());
    }

    /** Nós "Qual o risco?" e "É golpe?". */
    @PostMapping("/decisao")
    public DecisaoResponse decisao(@Valid @RequestBody DecisaoRequest req) {
        return decisaoService.decidir(req.risco(), req.confirmacao());
    }

    /** "Já sei que é golpe" e também o fim dos ramos confirmados: denuncia a chave. */
    @PostMapping("/denuncias")
    public DenunciaResponse denunciar(@Valid @RequestBody DenunciaRequest req) {
        return denunciaService.denunciar(req);
    }
}
