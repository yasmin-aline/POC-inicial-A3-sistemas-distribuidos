package br.com.pixseguro.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DenunciaRepository extends JpaRepository<Denuncia, Long> {

    long countByChaveHash(String chaveHash);

    boolean existsByChaveHashAndDescricao(String chaveHash, String descricao);

    Optional<Denuncia> findFirstByChaveHashOrderByCriadaEmDesc(String chaveHash);
}
