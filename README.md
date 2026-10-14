# Pix Seguro — Sentinela Pix (PoC)

API Java 17 + Spring Boot que simula uma pré-análise de uma tentativa Pix **antes de uma confirmação fictícia**. Cruza dados sintéticos do simulador, relatos locais por hash e sinais de uma mensagem opcional. Retorna uma decisão explicável e registra o resultado.

> É uma simulação. Não inicia, intercepta, autoriza ou bloqueia transferências reais. Não se conecta ao Bradesco, ao Banco Central, ao DICT nem a titulares de chaves. Use somente dados fictícios.

## Executar localmente

```bash
mvn spring-boot:run
```

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Cenários sintéticos: `GET /api/sandbox/pix/cenarios`
- Pré-análise: `POST /api/sandbox/pix/preflight`
- Consulta do registro: `GET /api/sandbox/pix/tentativas/{id}`
- Saúde: `GET /actuator/health`

A raiz de testes é H2 em memória. Flyway aplica migrations versionadas; o Hibernate só valida o schema. Os exemplos de denúncia são seedados de forma idempotente, por padrão (`PIXSEGURO_SEED_DEMO=true`).

## Fluxo principal

`tentativa sintética` → validação pelo controller → `PixPreflightService` cruza sinais transacionais simulados, relatos locais e, se houver texto, análise Gemini/regras → classifica risco e recomenda ação → persiste hash, pontuação, decisão e snapshot dos sinais → devolve resposta com `transferenciaExecutada=false`.

Códigos da decisão:

- `SEGUIR_COM_CAUTELA`
- `PAUSAR_E_CONFIRMAR`
- `NAO_CONFIRMAR_E_PEDIR_AJUDA`

Os pesos são heurísticas demonstrativas, não probabilidades calibradas. Consulte [GUIA-CODIGO-E-SWAGGER.md](GUIA-CODIGO-E-SWAGGER.md) para o mapa completo dos componentes e cenários passo a passo no Swagger.

## Persistência e migrations

- `V1__cria_tabela_denuncia.sql`
- `V2__cria_tabela_pix_tentativa.sql`
- `V3__cria_cenarios_demo.sql`

H2 em memória é o padrão. PostgreSQL também é suportado pelas dependências. Para outra base, configure `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` e `SPRING_DATASOURCE_PASSWORD`. Um deploy padrão no Render não cria banco persistente por si só; configure PostgreSQL se os registros precisarem sobreviver a reinícios.

## Gemini e fallback

- Sem `GEMINI_API_KEY`, a mensagem é analisada pelo motor local de regras.
- Com a chave, o servidor chama Gemini; não coloque segredo no cliente/front-end.
- Modelo configurável por `GEMINI_MODEL` (padrão `gemini-2.5-flash`).
- Se o Gemini falhar, o motor local continua a análise.
- `PIXSEGURO_IA_OFFLINE=true` simula indisponibilidade remota.

## Testes

```bash
mvn clean test
```

## Rotas preservadas do fluxo original

`POST /api/chaves/consulta`, `POST /api/mensagens/analise`, `GET /api/autodiagnostico/perguntas`, `POST /api/autodiagnostico`, `POST /api/decisao` e `POST /api/denuncias` continuam disponíveis para os outros ramos do fluxograma.

## Limites

A reputação é local e baseada em relatos não verificados. A API de demonstração não tem autenticação, rate limiting nem moderação de denúncias; não a use para coletar relatos reais. H2 é volátil, SHA-256 simples não é segredo, e nenhum dado de tentativa deve ser interpretado como evidência bancária oficial.
