package br.com.pixseguro.service;

import br.com.pixseguro.domain.CenarioPixDemo;
import br.com.pixseguro.domain.CenarioPixDemoRepository;
import br.com.pixseguro.web.Dtos.CenarioDemo;
import br.com.pixseguro.web.Dtos.PreflightPixRequest;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CenarioPixDemoService {

    private final CenarioPixDemoRepository repository;

    public CenarioPixDemoService(CenarioPixDemoRepository repository) {
        this.repository = repository;
    }

    public List<CenarioDemo> listar() {
        return repository.findAllByOrderByIdAsc().stream().map(this::paraDto).toList();
    }

    private CenarioDemo paraDto(CenarioPixDemo cenario) {
        PreflightPixRequest request = new PreflightPixRequest(cenario.getValor(), cenario.getChave(),
                cenario.getNomeEsperado(), cenario.getNomeRecebedor(), cenario.isNovoDestinatario(),
                cenario.isDispositivoConfiavel(), cenario.isValorForaDoPerfil(), cenario.getMensagemSuspeita());
        return new CenarioDemo(cenario.getId(), cenario.getNome(), cenario.getDescricao(), request);
    }
}
