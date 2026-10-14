CREATE TABLE pix_demo_cenario (
    id VARCHAR(60) PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(300) NOT NULL,
    valor DECIMAL(18, 2) NOT NULL,
    chave VARCHAR(140) NOT NULL,
    nome_esperado VARCHAR(120) NOT NULL,
    nome_recebedor VARCHAR(120) NOT NULL,
    novo_destinatario BOOLEAN NOT NULL,
    dispositivo_confiavel BOOLEAN NOT NULL,
    valor_fora_perfil BOOLEAN NOT NULL,
    mensagem_suspeita TEXT
);

INSERT INTO pix_demo_cenario (id, nome, descricao, valor, chave, nome_esperado, nome_recebedor, novo_destinatario, dispositivo_confiavel, valor_fora_perfil, mensagem_suspeita) VALUES
('normal', 'Pagamento habitual', 'Favorecido conhecido, dispositivo confiável e sem mensagem suspeita.', 45.90, 'padaria@demo.invalid', 'Padaria Central', 'Padaria Central', FALSE, TRUE, FALSE, NULL),
('novo-favorecido', 'Favorecido e dispositivo novos', 'Dois sinais moderados sem evidência de golpe na mensagem.', 320.00, 'loja@demo.invalid', 'Loja Exemplo', 'Loja Exemplo', TRUE, FALSE, FALSE, 'Olá, seguem os dados para o pagamento combinado.'),
('falso-parente', 'Falso parente com urgência', 'Troca de número, pedido urgente e favorecido diferente do esperado.', 4500.00, 'conta-nova@demo.invalid', 'Ana Souza', 'Marcos Lima', TRUE, FALSE, TRUE, 'Oi mãe, mudei de número. Preciso de um Pix urgente, não liga agora.'),
('chave-denunciada', 'Chave com denúncias da PoC', 'Consulta o sinal comunitário local de uma chave seed de demonstração.', 780.00, 'golpista@exemplo.com', 'Loja Exemplo', 'Loja Exemplo', TRUE, TRUE, FALSE, NULL),
('falsa-central', 'Falsa central pedindo código', 'Mensagem simula suporte bancário e pede código de verificação.', 1250.00, 'suporte@demo.invalid', 'Banco Exemplo', 'Pessoa Física Exemplo', TRUE, FALSE, TRUE, 'Sou da central de segurança. Envie o código de verificação por SMS para evitar o bloqueio da conta.');
