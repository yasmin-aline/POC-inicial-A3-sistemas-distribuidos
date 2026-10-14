package br.com.pixseguro.ia;

import br.com.pixseguro.domain.Risco;
import java.util.List;

public record ResultadoAnalise(Risco risco, int pontuacao, List<String> sinais, String dica, String motor) {}
