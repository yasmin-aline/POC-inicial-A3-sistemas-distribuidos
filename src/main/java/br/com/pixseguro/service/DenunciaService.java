package br.com.pixseguro.service;

import br.com.pixseguro.domain.Denuncia;
import br.com.pixseguro.domain.DenunciaRepository;
import br.com.pixseguro.domain.ProximoPasso;
import br.com.pixseguro.domain.Risco;
import br.com.pixseguro.web.Dtos.DenunciaRequest;
import br.com.pixseguro.web.Dtos.DenunciaResponse;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** "Denuncia a chave Pix no sistema" e o que acontece depois (MED, B.O. e alerta). */
@Service
public class DenunciaService {

    private final DenunciaRepository repo;
    private final ChaveHasher hasher;

    public DenunciaService(DenunciaRepository repo, ChaveHasher hasher) {
        this.repo = repo;
        this.hasher = hasher;
    }

    @Transactional
    public DenunciaResponse denunciar(DenunciaRequest req) {
        String hash = hasher.hash(req.chave());
        String descricao = req.descricao() == null || req.descricao().isBlank() ? null : req.descricao().trim();
        repo.save(new Denuncia(hash, descricao, req.jaFezPix()));

        long total = repo.countByChaveHash(hash);
        List<String> orientacoes = new ArrayList<>();
        ProximoPasso passo;

        if (req.jaFezPix()) {
            passo = ProximoPasso.PEDIR_DEVOLUCAO_MED_E_REGISTRAR_BO;
            orientacoes.add("Acione o seu banco ou app agora e peça a devolução pelo MED (Mecanismo Especial de Devolução).");
            orientacoes.add("Registre um boletim de ocorrência (B.O.), de preferência na delegacia online do seu estado.");
            orientacoes.add("Guarde comprovante, prints da conversa e os dados da chave.");
        } else {
            passo = ProximoPasso.ALERTA_PARA_PROXIMOS_USUARIOS;
        }
        orientacoes.add("Denúncia registrada. Os próximos usuários verão o alerta ao consultar essa chave.");

        return new DenunciaResponse(total, Risco.deDenuncias(total), true, passo, orientacoes);
    }
}
