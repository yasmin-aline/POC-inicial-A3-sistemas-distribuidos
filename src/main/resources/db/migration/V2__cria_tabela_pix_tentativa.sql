CREATE TABLE pix_tentativa (
    id VARCHAR(36) PRIMARY KEY,
    chave_hash VARCHAR(64) NOT NULL,
    valor DECIMAL(18, 2) NOT NULL,
    risco VARCHAR(10) NOT NULL,
    pontuacao INTEGER NOT NULL,
    decisao VARCHAR(40) NOT NULL,
    sinais_json TEXT NOT NULL,
    criada_em TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_pix_tentativa_criada_em ON pix_tentativa (criada_em);
CREATE INDEX idx_pix_tentativa_risco ON pix_tentativa (risco);
