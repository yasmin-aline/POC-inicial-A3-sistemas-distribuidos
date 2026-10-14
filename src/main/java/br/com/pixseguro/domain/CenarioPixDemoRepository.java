package br.com.pixseguro.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CenarioPixDemoRepository extends JpaRepository<CenarioPixDemo, String> {
    List<CenarioPixDemo> findAllByOrderByIdAsc();
}
