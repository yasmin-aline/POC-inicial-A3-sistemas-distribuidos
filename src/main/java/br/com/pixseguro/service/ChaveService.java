package br.com.pixseguro.service;

import br.com.pixseguro.domain.Denuncia;
import br.com.pixseguro.domain.DenunciaRepository;
import br.com.pixseguro.domain.ProximoPasso;
import br.com.pixseguro.domain.Risco;
import br.com.pixseguro.web.Dtos.ConsultaChaveResponse;
import java.time.Instant;
import org.springframework.stereotype.Service;

/** Ramo "Tenho a chave": digita a chave e ve quantas denuncias ela ja tem. */
@Service
public class ChaveService {

    private final DenunciaRepository repo;
    private final ChaveHasher hasher;

    public ChaveService(DenunciaRepository repo, ChaveHasher hasher) {
        this.repo = repo;
        this.hasher = hasher;
    }

    public ConsultaChaveResponse consultar(String chave) {
        String hash = hasher.hash(chave);
        long total = repo.countByChaveHash(hash);
        Risco risco = Risco.deDenuncias(total);
        Instant ultima = repo.findFirstByChaveHashOrderByCriadaEmDesc(hash)
                .map(Denuncia::getCriadaEm)
                .orElse(null);

        String mensagem = total == 0
                ? "Nenhuma denúncia para esta chave. Isso não garante que seja segura: confira o nome do recebedor antes de pagar."
                : "Esta chave já tem " + total + " denúncia(s). Não pague e confirme por outro canal.";

        return new ConsultaChaveResponse(risco, total, ultima, mensagem, ProximoPasso.paraRisco(risco));
    }
}
