package br.com.pixseguro.web;

import br.com.pixseguro.service.AnaliseMensagemService;
import br.com.pixseguro.service.AutodiagnosticoService;
import br.com.pixseguro.service.CenarioPixDemoService;
import br.com.pixseguro.service.ChaveService;
import br.com.pixseguro.service.DecisaoService;
import br.com.pixseguro.service.DenunciaService;
import br.com.pixseguro.service.PixPreflightService;
import br.com.pixseguro.web.Dtos.AnaliseMensagemRequest;
import br.com.pixseguro.web.Dtos.AnaliseMensagemResponse;
import br.com.pixseguro.web.Dtos.AutodiagnosticoRequest;
import br.com.pixseguro.web.Dtos.AutodiagnosticoResponse;
import br.com.pixseguro.web.Dtos.CenarioDemo;
import br.com.pixseguro.web.Dtos.ConsultaChaveRequest;
import br.com.pixseguro.web.Dtos.ConsultaChaveResponse;
import br.com.pixseguro.web.Dtos.DecisaoRequest;
import br.com.pixseguro.web.Dtos.DecisaoResponse;
import br.com.pixseguro.web.Dtos.DenunciaRequest;
import br.com.pixseguro.web.Dtos.DenunciaResponse;
import br.com.pixseguro.web.Dtos.Pergunta;
import br.com.pixseguro.web.Dtos.PreflightPixRequest;
import br.com.pixseguro.web.Dtos.PreflightPixResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
    private final PixPreflightService preflightService;
    private final CenarioPixDemoService cenarioService;

    public PixSeguroController(ChaveService chaveService, AnaliseMensagemService analiseService,
                               AutodiagnosticoService autodiagnosticoService, DecisaoService decisaoService,
                               DenunciaService denunciaService, PixPreflightService preflightService,
                               CenarioPixDemoService cenarioService) {
        this.chaveService = chaveService;
        this.analiseService = analiseService;
        this.autodiagnosticoService = autodiagnosticoService;
        this.decisaoService = decisaoService;
        this.denunciaService = denunciaService;
        this.preflightService = preflightService;
        this.cenarioService = cenarioService;
    }

    @PostMapping("/chaves/consulta")
    public ConsultaChaveResponse consultarChave(@Valid @RequestBody ConsultaChaveRequest req) {
        return chaveService.consultar(req.chave());
    }

    @PostMapping("/mensagens/analise")
    public AnaliseMensagemResponse analisarMensagem(@Valid @RequestBody AnaliseMensagemRequest req) {
        return analiseService.analisar(req.texto());
    }

    @GetMapping("/autodiagnostico/perguntas")
    public List<Pergunta> perguntas() {
        return autodiagnosticoService.perguntas();
    }

    @PostMapping("/autodiagnostico")
    public AutodiagnosticoResponse autodiagnostico(@Valid @RequestBody AutodiagnosticoRequest req) {
        return autodiagnosticoService.avaliar(req.respostas());
    }

    @PostMapping("/decisao")
    public DecisaoResponse decisao(@Valid @RequestBody DecisaoRequest req) {
        return decisaoService.decidir(req.risco(), req.confirmacao());
    }

    @PostMapping("/denuncias")
    public DenunciaResponse denunciar(@Valid @RequestBody DenunciaRequest req) {
        return denunciaService.denunciar(req);
    }

    @Operation(summary = "Lista cenários sintéticos pré-carregados por migrations")
    @GetMapping("/sandbox/pix/cenarios")
    public List<CenarioDemo> cenariosDemo() {
        return cenarioService.listar();
    }

    @Operation(summary = "Simula a análise antes da confirmação; não faz Pix nem bloqueia transação real")
    @PostMapping("/sandbox/pix/preflight")
    public PreflightPixResponse preflightPix(@Valid @RequestBody PreflightPixRequest req) {
        return preflightService.analisar(req);
    }

    @Operation(summary = "Consulta a decisão gravada de uma tentativa Pix sintética")
    @GetMapping("/sandbox/pix/tentativas/{id}")
    public PreflightPixResponse consultarTentativa(@PathVariable String id) {
        return preflightService.consultar(id);
    }
}
