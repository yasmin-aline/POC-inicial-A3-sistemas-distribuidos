// API mock do Pix Seguro: mesmo contrato do backend Spring (POC da Yasmin).
// Serve para rodar o front sem o backend e para gravar demo/prints.
// Uso: node mock-api/server.js   (porta 8080, mesma do Spring)
// Regras copiadas de AnalisadorPorRegras, AutodiagnosticoService, DenunciaService e ChaveService.
const http = require('http');
const crypto = require('crypto');

const PORT = process.env.PORT || 8080;
const denuncias = new Map(); // hash -> [datas]

const normalizarChave = (c) => {
  const t = String(c).trim().toLowerCase();
  return t.includes('@') ? t : t.replace(/[\s.\-/()+]/g, '');
};
const hash = (c) => crypto.createHash('sha256').update(normalizarChave(c)).digest('hex');
const riscoDeDenuncias = (n) => (n >= 3 ? 'ALTO' : n >= 1 ? 'MEDIO' : 'BAIXO');
const passoParaRisco = (r) =>
  ({ BAIXO: 'SEGUIR_COM_PAGAMENTO_CONFERINDO_NOME', MEDIO: 'NAO_PAGAR_E_CONFIRMAR_POR_OUTRO_CANAL', ALTO: 'DENUNCIAR_CHAVE' })[r];

// Chave de demonstração já com 3 denúncias (risco alto)
const chaveDemo = hash('11987654321');
denuncias.set(chaveDemo, [new Date(Date.now() - 864e5 * 3), new Date(Date.now() - 864e5), new Date(Date.now() - 36e5 * 5)]);

const REGRAS = [
  [/\b(urgente|urgencia|imediatamente|ultimo dia|so hoje|rapido|agora mesmo)\b/, 2, 'Pressão de urgência'],
  [/(mudei de (numero|celular)|novo numero|numero novo|troquei de (numero|celular)|celular (quebrou|caiu))/, 3, 'Diz ter trocado de número (golpe do falso parente)'],
  [/(me empresta|me ajuda com|preciso de um pix|faz um pix|me manda um pix|paga (pra|para) mim|pode pagar)/, 2, 'Pedido direto de dinheiro'],
  [/(voce ganhou|parabens.{0,30}ganhou|premio|sorteio|renda extra|lucro garantido|investimento garantido|dobrar (seu|o) dinheiro)/, 3, 'Promessa de prêmio ou ganho fácil'],
  [/(taxa|tarifa|custas?)\b.{0,40}\b(liberar|liberacao|desbloque\w*|receber)/, 3, 'Cobrança de taxa para liberar valor'],
  [/(conta (sera|foi|esta|ficara) (bloqueada|suspensa|encerrada)|bloqueio da sua conta|cpf (irregular|suspenso|cancelado))/, 3, 'Ameaça de bloqueio ou irregularidade'],
  [/(https?:\/\/|www\.|bit\.ly|tinyurl|wa\.me)/, 2, 'Contém link'],
  [/\b(codigo de verificacao|token|senha|cvv)\b/, 4, 'Pede código, token ou senha'],
  [/(nao (conte|fale|comente)|nao conta pra ninguem|segredo|so entre nos)/, 3, 'Pede sigilo'],
  [/(central de seguranca|setor de seguranca|equipe de seguranca|suporte (do|da) (banco|app)|gerente do seu banco)/, 2, 'Diz ser do banco ou da central de segurança'],
  [/(pix (foi )?(errado|por engano)|caiu na minha conta por engano|devolve (o )?(valor|dinheiro)|devolver o (valor|dinheiro))/, 3, 'História de Pix feito por engano'],
  [/(fora d[ao] (plataforma|app|site|aplicativo)|direto comigo|negociar por fora|pagar por fora)/, 2, 'Quer pagar fora do canal oficial'],
];
const DICAS = {
  ALTO: 'Não pague. Fale com a pessoa ou empresa por outro canal (ligação ou pessoalmente) e consulte a chave no Pix Seguro.',
  MEDIO: 'Desconfie. Confirme por outro canal antes de pagar e confira o nome do recebedor.',
  BAIXO: 'Não encontramos sinais comuns de golpe, mas confira o nome do recebedor antes de confirmar.',
};

const PERGUNTAS = [
  [1, 'Estão pedindo urgência, dizendo que você precisa pagar agora?', 'Pressa é a principal arma do golpista. Pare e só pague depois de confirmar por outro canal.'],
  [2, 'O pedido veio de um número ou perfil novo, ou de alguém que disse ter mudado de número?', 'Ligue para a pessoa no número antigo, ou fale pessoalmente, antes de pagar.'],
  [3, 'O nome do recebedor que aparece no app é diferente do da pessoa ou empresa com quem você falou?', 'Não pague se o nome não bate. Peça os dados por um canal oficial.'],
  [4, 'A oferta parece boa demais para ser verdade (prêmio, desconto enorme, lucro garantido)?', 'Promessa de ganho fácil é sinal clássico de golpe. Pesquise o CNPJ e a reputação.'],
  [5, 'Pediram segredo, um código recebido por SMS ou para pagar fora do canal oficial?', 'Nunca compartilhe códigos ou senhas e evite pagar fora da plataforma oficial.'],
];

const problema = (res, status, detail, campos) => {
  res.writeHead(status, { 'Content-Type': 'application/problem+json' });
  res.end(JSON.stringify({ type: 'about:blank', title: 'Requisição inválida', status, detail, ...(campos ? { campos } : {}) }));
};
const ok = (res, body) => { res.writeHead(200, { 'Content-Type': 'application/json' }); res.end(JSON.stringify(body)); };
const branco = (v) => typeof v !== 'string' || !v.trim();

const rotas = {
  'POST /api/chaves/consulta': (b, res) => {
    if (branco(b.chave)) return problema(res, 400, 'Dados inválidos', { chave: 'não deve estar em branco' });
    const lista = denuncias.get(hash(b.chave)) || [];
    const risco = riscoDeDenuncias(lista.length);
    ok(res, {
      risco, totalDenuncias: lista.length,
      ultimaDenunciaEm: lista.length ? lista[lista.length - 1].toISOString() : null,
      mensagem: lista.length === 0
        ? 'Nenhuma denúncia para esta chave. Isso não garante que seja segura: confira o nome do recebedor antes de pagar.'
        : `Esta chave já tem ${lista.length} denúncia(s). Não pague e confirme por outro canal.`,
      proximoPasso: passoParaRisco(risco),
    });
  },
  'POST /api/mensagens/analise': (b, res) => {
    if (branco(b.texto)) return problema(res, 400, 'Dados inválidos', { texto: 'não deve estar em branco' });
    if (b.texto.length > 2000) return problema(res, 400, 'Dados inválidos', { texto: 'o tamanho deve ser entre 0 e 2000' });
    const t = b.texto.normalize('NFD').replace(/\p{M}/gu, '').toLowerCase();
    let pontos = 0; const sinais = [];
    for (const [re, peso, sinal] of REGRAS) if (re.test(t)) { pontos += peso; sinais.push(sinal); }
    const risco = pontos >= 4 ? 'ALTO' : pontos >= 2 ? 'MEDIO' : 'BAIXO';
    setTimeout(() => ok(res, {
      iaDisponivel: false, motor: 'REGRAS', risco, pontuacao: pontos, sinais, dica: DICAS[risco],
      proximoPasso: passoParaRisco(risco),
      aviso: 'Análise feita pelo motor de regras local. Configure GEMINI_API_KEY para habilitar o Gemini.',
    }), 600);
  },
  'GET /api/autodiagnostico/perguntas': (_b, res) => ok(res, PERGUNTAS.map(([id, texto]) => ({ id, texto }))),
  'POST /api/autodiagnostico': (b, res) => {
    const r = b.respostas;
    if (!Array.isArray(r) || r.length !== 5 || r.some((x) => typeof x !== 'boolean'))
      return problema(res, 400, 'Envie exatamente 5 respostas.');
    const recomendacoes = []; let sim = 0;
    r.forEach((v, i) => { if (v) { sim++; recomendacoes.push(PERGUNTAS[i][2]); } });
    const risco = sim <= 1 ? 'BAIXO' : sim === 2 ? 'MEDIO' : 'ALTO';
    recomendacoes.push(risco === 'BAIXO'
      ? 'Poucos sinais de alerta. Mesmo assim, confira o nome do recebedor antes de confirmar.'
      : 'Não pague agora. Confirme por outro canal e, se puder, consulte a chave no Pix Seguro.');
    ok(res, { pontuacao: sim * 20, risco, recomendacoes, proximoPasso: passoParaRisco(risco) });
  },
  'POST /api/denuncias': (b, res) => {
    const campos = {};
    if (branco(b.chave)) campos.chave = 'não deve estar em branco';
    if (b.descricao && b.descricao.length > 500) campos.descricao = 'o tamanho deve ser entre 0 e 500';
    if (typeof b.jaFezPix !== 'boolean') campos.jaFezPix = 'não deve ser nulo';
    if (Object.keys(campos).length) return problema(res, 400, 'Dados inválidos', campos);
    const h = hash(b.chave);
    const lista = denuncias.get(h) || []; lista.push(new Date()); denuncias.set(h, lista);
    const orientacoes = [];
    if (b.jaFezPix) orientacoes.push(
      'Acione o seu banco ou app agora e peça a devolução pelo MED (Mecanismo Especial de Devolução).',
      'Registre um boletim de ocorrência (B.O.), de preferência na delegacia online do seu estado.',
      'Guarde comprovante, prints da conversa e os dados da chave.');
    orientacoes.push('Denúncia registrada. Os próximos usuários verão o alerta ao consultar essa chave.');
    ok(res, {
      totalDenunciasChave: lista.length, risco: riscoDeDenuncias(lista.length), alertaPublicado: true,
      proximoPasso: b.jaFezPix ? 'PEDIR_DEVOLUCAO_MED_E_REGISTRAR_BO' : 'ALERTA_PARA_PROXIMOS_USUARIOS', orientacoes,
    });
  },
};

http.createServer((req, res) => {
  let dados = '';
  req.on('data', (c) => (dados += c));
  req.on('end', () => {
    const rota = rotas[`${req.method} ${req.url.split('?')[0]}`];
    if (!rota) return problema(res, 404, 'Rota não encontrada');
    let corpo = {};
    try { corpo = dados ? JSON.parse(dados) : {}; } catch { return problema(res, 400, 'JSON inválido'); }
    rota(corpo, res);
  });
}).listen(PORT, () => console.log(`Mock do Pix Seguro em http://localhost:${PORT} (chave demo com 3 denúncias: 11987654321)`));
