package br.com.pixseguro;

import br.com.pixseguro.domain.Denuncia;
import br.com.pixseguro.domain.DenunciaRepository;
import br.com.pixseguro.service.ChaveHasher;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Denúncias de exemplo para testar a consulta de chave logo após subir a aplicação. */
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
        for (int i = 0; i < 3; i++) {
            repo.save(new Denuncia(golpista, "Falso parente pedindo Pix", i == 0));
        }
        repo.save(new Denuncia(hasher.hash("suspeito@exemplo.com"), "Prêmio com taxa para liberar", false));
    }
}
