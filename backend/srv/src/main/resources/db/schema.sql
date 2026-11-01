CREATE TABLE perfil (
    id_perfil UUID PRIMARY KEY,
    nome VARCHAR(30) NOT NULL CONSTRAINT uq_perfil_nome UNIQUE,
    descricao VARCHAR(150)
);

CREATE TABLE status_pedido (
    id_status_pedido UUID PRIMARY KEY,
    nome VARCHAR(30) NOT NULL CONSTRAINT uq_status_pedido_nome UNIQUE,
    descricao VARCHAR(150),
    ordem SMALLINT NOT NULL CONSTRAINT uq_status_pedido_ordem UNIQUE,
    CONSTRAINT status_pedido_ordem_check CHECK (ordem BETWEEN 0 AND 255)
);

CREATE TABLE status_pagamento (
    id_status_pagamento UUID PRIMARY KEY,
    nome VARCHAR(30) NOT NULL CONSTRAINT uq_status_pagamento_nome UNIQUE,
    descricao VARCHAR(150)
);

CREATE TABLE tipo_desconto (
    id_tipo_desconto UUID PRIMARY KEY,
    nome VARCHAR(20) NOT NULL CONSTRAINT uq_tipo_desconto_nome UNIQUE,
    descricao VARCHAR(100)
);

CREATE TABLE usuario (
    id_usuario UUID PRIMARY KEY,
    id_perfil UUID NOT NULL,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL CONSTRAINT uq_usuario_email UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    telefone VARCHAR(25),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuario_perfil FOREIGN KEY (id_perfil) REFERENCES perfil(id_perfil)
        ON DELETE RESTRICT ON UPDATE RESTRICT
);
CREATE INDEX idx_usuario_perfil_ativo ON usuario(id_perfil, ativo);

CREATE TABLE produto (
    id_produto UUID PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    descricao TEXT,
    imagem_url VARCHAR(500),
    preco NUMERIC(10, 2) NOT NULL,
    disponivel BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_produto_preco CHECK (preco >= 0)
);
CREATE INDEX idx_produto_disponivel_nome ON produto(disponivel, nome);

CREATE TABLE carrinho (
    id_carrinho UUID PRIMARY KEY,
    id_usuario UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ATIVO',
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_carrinho_status CHECK (status IN ('ATIVO', 'FINALIZADO', 'ABANDONADO')),
    CONSTRAINT fk_carrinho_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
        ON DELETE RESTRICT ON UPDATE RESTRICT
);
CREATE INDEX idx_carrinho_usuario_status ON carrinho(id_usuario, status);

CREATE TABLE endereco (
    id_endereco UUID PRIMARY KEY,
    id_usuario UUID NOT NULL,
    identificacao VARCHAR(50),
    destinatario VARCHAR(120) NOT NULL,
    cep VARCHAR(10) NOT NULL,
    logradouro VARCHAR(150) NOT NULL,
    numero VARCHAR(20) NOT NULL,
    complemento VARCHAR(100),
    bairro VARCHAR(100) NOT NULL,
    cidade VARCHAR(100) NOT NULL,
    uf CHAR(2) NOT NULL,
    referencia VARCHAR(200),
    principal BOOLEAN NOT NULL DEFAULT FALSE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_endereco_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
        ON DELETE RESTRICT ON UPDATE RESTRICT
);
CREATE INDEX idx_endereco_usuario ON endereco(id_usuario);

CREATE TABLE cupom (
    id_cupom UUID PRIMARY KEY,
    id_tipo_desconto UUID NOT NULL,
    codigo VARCHAR(50) NOT NULL CONSTRAINT uq_cupom_codigo UNIQUE,
    descricao VARCHAR(255),
    valor_desconto NUMERIC(10, 2) NOT NULL,
    data_inicio TIMESTAMP NOT NULL,
    data_fim TIMESTAMP NOT NULL,
    limite_utilizacoes BIGINT,
    valor_minimo_pedido NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_cupom_limite CHECK (
        limite_utilizacoes IS NULL OR limite_utilizacoes BETWEEN 1 AND 4294967295
    ),
    CONSTRAINT ck_cupom_minimo CHECK (valor_minimo_pedido >= 0),
    CONSTRAINT ck_cupom_periodo CHECK (data_fim > data_inicio),
    CONSTRAINT ck_cupom_valor CHECK (valor_desconto > 0),
    CONSTRAINT fk_cupom_tipo FOREIGN KEY (id_tipo_desconto) REFERENCES tipo_desconto(id_tipo_desconto)
        ON DELETE RESTRICT ON UPDATE RESTRICT
);
CREATE INDEX idx_cupom_ativo_validade ON cupom(ativo, data_inicio, data_fim);

CREATE TABLE pedido (
    id_pedido UUID PRIMARY KEY,
    numero_pedido VARCHAR(30) NOT NULL CONSTRAINT uq_pedido_numero UNIQUE,
    id_usuario UUID NOT NULL,
    id_status_pedido UUID NOT NULL,
    data_pedido TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    subtotal NUMERIC(10, 2) NOT NULL,
    valor_desconto NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    valor_frete NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    valor_total NUMERIC(10, 2) NOT NULL,
    destinatario VARCHAR(120) NOT NULL,
    cep_entrega VARCHAR(10) NOT NULL,
    logradouro_entrega VARCHAR(150) NOT NULL,
    numero_entrega VARCHAR(20) NOT NULL,
    complemento_entrega VARCHAR(100),
    bairro_entrega VARCHAR(100) NOT NULL,
    cidade_entrega VARCHAR(100) NOT NULL,
    uf_entrega CHAR(2) NOT NULL,
    referencia_entrega VARCHAR(200),
    CONSTRAINT ck_pedido_desconto CHECK (valor_desconto >= 0),
    CONSTRAINT ck_pedido_frete CHECK (valor_frete >= 0),
    CONSTRAINT ck_pedido_subtotal CHECK (subtotal >= 0),
    CONSTRAINT ck_pedido_total CHECK (valor_total >= 0),
    CONSTRAINT fk_pedido_status FOREIGN KEY (id_status_pedido) REFERENCES status_pedido(id_status_pedido)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_pedido_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
        ON DELETE RESTRICT ON UPDATE RESTRICT
);
CREATE INDEX idx_pedido_data ON pedido(data_pedido);
CREATE INDEX idx_pedido_status_data ON pedido(id_status_pedido, data_pedido);
CREATE INDEX idx_pedido_usuario_data ON pedido(id_usuario, data_pedido);

CREATE TABLE item_carrinho (
    id_item_carrinho UUID PRIMARY KEY,
    id_carrinho UUID NOT NULL,
    id_produto UUID NOT NULL,
    quantidade BIGINT NOT NULL,
    preco_unitario NUMERIC(10, 2) NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_item_carrinho_produto UNIQUE (id_carrinho, id_produto),
    CONSTRAINT ck_item_carrinho_preco CHECK (preco_unitario >= 0),
    CONSTRAINT ck_item_carrinho_quantidade CHECK (quantidade BETWEEN 1 AND 4294967295),
    CONSTRAINT fk_item_carrinho_carrinho FOREIGN KEY (id_carrinho) REFERENCES carrinho(id_carrinho)
        ON DELETE CASCADE ON UPDATE RESTRICT,
    CONSTRAINT fk_item_carrinho_produto FOREIGN KEY (id_produto) REFERENCES produto(id_produto)
        ON DELETE RESTRICT ON UPDATE RESTRICT
);
CREATE INDEX idx_item_carrinho_produto ON item_carrinho(id_produto);

CREATE TABLE item_pedido (
    id_item_pedido UUID PRIMARY KEY,
    id_pedido UUID NOT NULL,
    id_produto UUID NOT NULL,
    nome_produto_snapshot VARCHAR(120) NOT NULL,
    quantidade BIGINT NOT NULL,
    preco_unitario NUMERIC(10, 2) NOT NULL,
    subtotal NUMERIC(12, 2) GENERATED ALWAYS AS (quantidade::NUMERIC * preco_unitario) STORED,
    CONSTRAINT ck_item_pedido_preco CHECK (preco_unitario >= 0),
    CONSTRAINT ck_item_pedido_quantidade CHECK (quantidade BETWEEN 1 AND 4294967295),
    CONSTRAINT fk_item_pedido_pedido FOREIGN KEY (id_pedido) REFERENCES pedido(id_pedido)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_item_pedido_produto FOREIGN KEY (id_produto) REFERENCES produto(id_produto)
        ON DELETE RESTRICT ON UPDATE RESTRICT
);
CREATE INDEX idx_item_pedido_pedido ON item_pedido(id_pedido);
CREATE INDEX idx_item_pedido_produto ON item_pedido(id_produto);

CREATE TABLE pagamento (
    id_pagamento UUID PRIMARY KEY,
    id_pedido UUID NOT NULL,
    id_status_pagamento UUID NOT NULL,
    metodo VARCHAR(40) NOT NULL,
    valor NUMERIC(10, 2) NOT NULL,
    transacao_externa VARCHAR(150) CONSTRAINT uq_pagamento_transacao UNIQUE,
    chave_idempotencia VARCHAR(100) NOT NULL CONSTRAINT uq_pagamento_idempotencia UNIQUE,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_confirmacao TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_pagamento_valor CHECK (valor > 0),
    CONSTRAINT fk_pagamento_pedido FOREIGN KEY (id_pedido) REFERENCES pedido(id_pedido)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_pagamento_status FOREIGN KEY (id_status_pagamento) REFERENCES status_pagamento(id_status_pagamento)
        ON DELETE RESTRICT ON UPDATE RESTRICT
);
CREATE INDEX idx_pagamento_data_status ON pagamento(data_confirmacao, id_status_pagamento);
CREATE INDEX idx_pagamento_pedido_status ON pagamento(id_pedido, id_status_pagamento);

CREATE TABLE historico_status_pedido (
    id_historico UUID PRIMARY KEY,
    id_pedido UUID NOT NULL,
    id_status_pedido UUID NOT NULL,
    id_usuario_responsavel UUID,
    data_alteracao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observacao VARCHAR(500),
    CONSTRAINT fk_historico_pedido FOREIGN KEY (id_pedido) REFERENCES pedido(id_pedido)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_historico_status FOREIGN KEY (id_status_pedido) REFERENCES status_pedido(id_status_pedido)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_historico_usuario FOREIGN KEY (id_usuario_responsavel) REFERENCES usuario(id_usuario)
        ON DELETE RESTRICT ON UPDATE RESTRICT
);
CREATE INDEX idx_historico_pedido_data ON historico_status_pedido(id_pedido, data_alteracao);

CREATE TABLE uso_cupom (
    id_uso_cupom UUID PRIMARY KEY,
    id_cupom UUID NOT NULL,
    id_pedido UUID NOT NULL CONSTRAINT uq_uso_cupom_pedido UNIQUE,
    id_usuario UUID NOT NULL,
    data_utilizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valor_desconto_aplicado NUMERIC(10, 2) NOT NULL,
    CONSTRAINT ck_uso_cupom_desconto CHECK (valor_desconto_aplicado >= 0),
    CONSTRAINT fk_uso_cupom_cupom FOREIGN KEY (id_cupom) REFERENCES cupom(id_cupom)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_uso_cupom_pedido FOREIGN KEY (id_pedido) REFERENCES pedido(id_pedido)
        ON DELETE RESTRICT ON UPDATE RESTRICT,
    CONSTRAINT fk_uso_cupom_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
        ON DELETE RESTRICT ON UPDATE RESTRICT
);
CREATE INDEX idx_uso_cupom_cupom_data ON uso_cupom(id_cupom, data_utilizacao);
CREATE INDEX idx_uso_cupom_usuario ON uso_cupom(id_usuario);

CREATE TABLE auditoria (
    id_auditoria UUID PRIMARY KEY,
    id_usuario UUID,
    entidade VARCHAR(80) NOT NULL,
    id_registro VARCHAR(80) NOT NULL,
    operacao VARCHAR(20) NOT NULL,
    detalhes JSONB,
    data_evento TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_auditoria_operacao CHECK (operacao IN ('INSERT', 'UPDATE', 'DELETE', 'STATUS')),
    CONSTRAINT fk_auditoria_usuario FOREIGN KEY (id_usuario) REFERENCES usuario(id_usuario)
        ON DELETE RESTRICT ON UPDATE RESTRICT
);
CREATE INDEX idx_auditoria_entidade_data ON auditoria(entidade, data_evento);
CREATE INDEX idx_auditoria_usuario_data ON auditoria(id_usuario, data_evento);

INSERT INTO perfil (id_perfil, nome, descricao) VALUES
    (gen_random_uuid(), 'ADMIN', 'Administrador da loja'),
    (gen_random_uuid(), 'CLIENTE', 'Cliente da loja');

INSERT INTO status_pedido (id_status_pedido, nome, descricao, ordem) VALUES
    (gen_random_uuid(), 'PENDENTE_PAGAMENTO', 'Aguardando confirmação do pagamento', 1),
    (gen_random_uuid(), 'PAGO', 'Pagamento confirmado', 2),
    (gen_random_uuid(), 'EM_PREPARACAO', 'Pedido em preparação', 3),
    (gen_random_uuid(), 'ENVIADO', 'Pedido enviado', 4),
    (gen_random_uuid(), 'CONCLUIDO', 'Pedido concluído', 5),
    (gen_random_uuid(), 'CANCELADO', 'Pedido cancelado', 6);

INSERT INTO status_pagamento (id_status_pagamento, nome, descricao) VALUES
    (gen_random_uuid(), 'PENDENTE', 'Aguardando confirmação'),
    (gen_random_uuid(), 'APROVADO', 'Pagamento aprovado'),
    (gen_random_uuid(), 'RECUSADO', 'Pagamento recusado'),
    (gen_random_uuid(), 'CANCELADO', 'Pagamento cancelado');

INSERT INTO tipo_desconto (id_tipo_desconto, nome, descricao) VALUES
    (gen_random_uuid(), 'PERCENTUAL', 'Desconto percentual'),
    (gen_random_uuid(), 'VALOR_FIXO', 'Desconto em valor fixo');
