package br.com.pixseguro.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "pix_demo_cenario")
public class CenarioPixDemo {

    @Id
    @Column(length = 60, nullable = false)
    private String id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 300)
    private String descricao;

    @Column(precision = 18, scale = 2, nullable = false)
    private BigDecimal valor;

    @Column(nullable = false, length = 140)
    private String chave;

    @Column(name = "nome_esperado", nullable = false, length = 120)
    private String nomeEsperado;

    @Column(name = "nome_recebedor", nullable = false, length = 120)
    private String nomeRecebedor;

    @Column(name = "novo_destinatario", nullable = false)
    private boolean novoDestinatario;

    @Column(name = "dispositivo_confiavel", nullable = false)
    private boolean dispositivoConfiavel;

    @Column(name = "valor_fora_perfil", nullable = false)
    private boolean valorForaDoPerfil;

    @Column(name = "mensagem_suspeita", columnDefinition = "TEXT")
    private String mensagemSuspeita;

    protected CenarioPixDemo() {}

    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public BigDecimal getValor() { return valor; }
    public String getChave() { return chave; }
    public String getNomeEsperado() { return nomeEsperado; }
    public String getNomeRecebedor() { return nomeRecebedor; }
    public boolean isNovoDestinatario() { return novoDestinatario; }
    public boolean isDispositivoConfiavel() { return dispositivoConfiavel; }
    public boolean isValorForaDoPerfil() { return valorForaDoPerfil; }
    public String getMensagemSuspeita() { return mensagemSuspeita; }
}
