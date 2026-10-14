package br.com.pixseguro;

import br.com.pixseguro.domain.Denuncia;
import br.com.pixseguro.domain.DenunciaRepository;
import br.com.pixseguro.service.ChaveHasher;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Relatos fictícios idempotentes para testar consulta e pré-análise. */
@Component
@ConditionalOnProperty(name = "pixseguro.seed-demo", havingValue = "true", matchIfMissing = true)
public class DadosDemo implements ApplicationRunner {

    private final DenunciaRepository repo;
    private final ChaveHasher hasher;

    public DadosDemo(DenunciaRepository repo, ChaveHasher hasher) {
        this.repo = repo;
        this.hasher = hasher;
    }

    @Override
    public void run(ApplicationArguments args) {
        String golpista = hasher.hash("golpista@exemplo.com");
        for (String relato : List.of("Falso parente pedindo Pix", "Mudança de número e urgência",
                "Pedido de segredo e transferência")) {
            criarSeAusente(golpista, relato, false);
        }
        criarSeAusente(hasher.hash("suspeito@exemplo.com"), "Prêmio com taxa para liberar", false);
    }

    private void criarSeAusente(String hash, String descricao, boolean jaFezPix) {
        if (!repo.existsByChaveHashAndDescricao(hash, descricao)) {
            repo.save(new Denuncia(hash, descricao, jaFezPix));
        }
    }
}
