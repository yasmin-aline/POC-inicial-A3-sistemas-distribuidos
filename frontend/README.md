# Pix Seguro — Front-end (Angular)

Front do A3 de Sistemas Distribuídos e Mobile (Golpe do Pix). Angular 19, componentes standalone, layout mobile.
Consome a API Spring Boot da Yasmin: https://github.com/yasmin-aline/POC-inicial-A3-sistemas-distribuidos

## Telas
| Rota | Tela | Endpoint |
|---|---|---|
| `/` | Início (4 funções + dica) | — |
| `/consultar` | Consultar chave Pix | `POST /api/chaves/consulta` |
| `/analisar` | Analisar mensagem com IA (Gemini, fallback por regras) | `POST /api/mensagens/analise` |
| `/autodiagnostico` | 5 perguntas sim/não | `GET /api/autodiagnostico/perguntas`, `POST /api/autodiagnostico` |
| `/denunciar` | Denunciar chave (aceita `?chave=` vindo da consulta) | `POST /api/denuncias` |

## Pré-requisitos
- Node.js 18.19+ ou 20+ (recomendado 20 LTS)
- Backend da Yasmin rodando na porta **8080** (ou o mock abaixo)

## Como rodar
```bash
npm install
npm start            # ng serve em http://localhost:4200
```
Abra http://localhost:4200 (dica: F12 > modo celular, 390x844).

O `proxy.conf.json` repassa tudo que começa com `/api` para `http://localhost:8080`.
Por isso **não precisa configurar CORS no backend** em desenvolvimento.

### Com o backend real (Spring)
Na pasta do backend: `./mvnw spring-boot:run` (sobe na 8080). Depois `npm start` aqui.
Para a IA de verdade, a `GEMINI_API_KEY` vai **só no backend** (variável de ambiente). Nunca no front nem no GitHub.

### Sem o backend (mock)
```bash
npm run mock         # API falsa na 8080, mesmo contrato do Spring
npm start            # em outro terminal
```
O mock usa as mesmas regras do backend (motor de regras, autodiagnóstico, denúncia). Dados ficam em memória.
Chave de demonstração com 3 denúncias (risco alto): `11987654321`.
Atenção: não rode o mock e o Spring ao mesmo tempo (os dois usam a 8080).

## Estrutura
```
src/app/
  api.service.ts          tipos do contrato + chamadas HTTP + leitura de erros (ProblemDetail)
  shared/icon.component   ícones Lucide em SVG
  shared/ui.ts            topbar e card de risco
  pages/                  home, consultar, analisar, autodiagnostico, denunciar
src/styles.css            paleta do protótipo (Figma)
mock-api/server.js        API mock em Node, sem dependências
```

## Build de produção
`npm run build` gera `dist/pix-seguro-front/browser`. Em produção o front precisa ficar no mesmo domínio da API
ou o backend precisa liberar CORS para o domínio do front.
