# Pix Seguro (PoC)

API em Java 21 + Spring Boot 3 que implementa o fluxograma do Pix Seguro: antes de pagar um Pix, a pessoa consulta a chave, cola a mensagem recebida ou responde um autodiagnóstico, recebe um risco e é guiada até pagar, confirmar por outro canal ou denunciar.

## Rodar local

```bash
mvn spring-boot:run
```

- Swagger (para testar sem front): http://localhost:8080/swagger-ui.html
- Saúde: http://localhost:8080/actuator/health
- Banco H2 em memória, os dados somem ao reiniciar. Sobe com 2 chaves de exemplo: `golpista@exemplo.com` (3 denúncias) e `suspeito@exemplo.com` (1).

## Fluxograma para endpoints

| Nó do fluxograma | Endpoint |
|---|---|
| Tenho a chave, "Vê quantas denúncias a chave já tem" | `POST /api/chaves/consulta` |
| Recebi uma mensagem, "IA analisa e mostra risco" | `POST /api/mensagens/analise` |
| IA fora do ar, vai para digitar a chave | resposta com `iaDisponivel=false` e `proximoPasso=CONSULTAR_CHAVE` |
| Não sei bem, 5 perguntas | `GET /api/autodiagnostico/perguntas` e `POST /api/autodiagnostico` |
| Qual o risco? e Em golpe? | `POST /api/decisao` |
| Já sei que é golpe, Denuncia a chave, Já tinha feito o Pix? | `POST /api/denuncias` |
| Pede devolução pelo MED e registra B.O. | `proximoPasso=PEDIR_DEVOLUCAO_MED_E_REGISTRAR_BO` na denúncia |
| Próximos usuários veem o alerta | a denúncia aumenta o total retornado em `/api/chaves/consulta` |

Toda resposta traz `proximoPasso`, que diz ao cliente qual nó do fluxograma vem a seguir.

## Exemplos

```bash
# consultar chave
curl -s localhost:8080/api/chaves/consulta -H 'Content-Type: application/json' \
  -d '{"chave":"golpista@exemplo.com"}'

# analisar mensagem
curl -s localhost:8080/api/mensagens/analise -H 'Content-Type: application/json' \
  -d '{"texto":"Oi mãe, mudei de número, me faz um Pix urgente?"}'

# autodiagnóstico (5 respostas, true = sim)
curl -s localhost:8080/api/autodiagnostico -H 'Content-Type: application/json' \
  -d '{"respostas":[true,true,false,false,false]}'

# decisão depois de confirmar por outro canal (E_GOLPE, NAO_DEU_PARA_CONFIRMAR, NAO_E_GOLPE)
curl -s localhost:8080/api/decisao -H 'Content-Type: application/json' \
  -d '{"risco":"ALTO","confirmacao":"NAO_DEU_PARA_CONFIRMAR"}'

# denunciar (jaFezPix=true devolve as orientações de MED e B.O.)
curl -s localhost:8080/api/denuncias -H 'Content-Type: application/json' \
  -d '{"chave":"novo@golpe.com","descricao":"Falso parente","jaFezPix":true}'
```

## Análise de mensagem: IA ou regras

- Sem `ANTHROPIC_API_KEY`: usa o motor de regras local (`motor: "REGRAS"`), que funciona offline.
- Com `ANTHROPIC_API_KEY`: usa a IA (`motor: "IA"`). Modelo configurável em `ANTHROPIC_MODEL`.
- Se a IA falhar, ou com `PIXSEGURO_IA_OFFLINE=true`, a resposta cai no ramo "IA fora do ar".

## Deploy no Render

1. Suba o projeto para um repositório no GitHub.
2. No Render: New, Blueprint (ele lê o `render.yaml`) ou New, Web Service com Runtime Docker.
3. Opcional: defina `ANTHROPIC_API_KEY` nas variáveis de ambiente.
4. No plano free o serviço hiberna após inatividade (primeira chamada demora) e o H2 em memória zera a cada reinício.

## Limitações conhecidas da PoC

- Sem autenticação nem limite de requisições: qualquer pessoa pode denunciar qualquer chave, então denúncias falsas podem difamar uma chave legítima.
- Chaves são guardadas apenas como hash SHA-256 (nada de chave em texto puro).
- Telefone digitado com e sem DDI (+55) gera hashes diferentes.
- Dados em memória. Para persistir, troque o H2 por Postgres.
