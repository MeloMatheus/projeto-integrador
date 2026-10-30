CREATE TABLE perfil (
    id_perfil UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE usuario (
    id_usuario UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_perfil UUID NOT NULL REFERENCES perfil(id_perfil) ON DELETE RESTRICT,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(180) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    telefone VARCHAR(30),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(180),
    updated_by VARCHAR(180)
);
CREATE INDEX idx_usuario_perfil ON usuario(id_perfil);

CREATE TABLE produto (
    id_produto UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nome VARCHAR(120) NOT NULL,
    descricao TEXT,
    imagem_url VARCHAR(500),
    preco NUMERIC(10,2) NOT NULL CHECK (preco >= 0),
    disponivel BOOLEAN NOT NULL DEFAULT TRUE,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(180),
    updated_by VARCHAR(180)
);
CREATE INDEX idx_produto_ativo_nome ON produto(ativo, nome);

CREATE TABLE carrinho (
    id_carrinho UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_usuario UUID NOT NULL REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    status VARCHAR(20) NOT NULL CHECK (status IN ('ABERTO', 'FINALIZADO', 'CANCELADO')),
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    atualizado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(180),
    updated_by VARCHAR(180)
);
CREATE INDEX idx_carrinho_usuario_status ON carrinho(id_usuario, status);
CREATE UNIQUE INDEX uk_carrinho_aberto_usuario ON carrinho(id_usuario) WHERE status = 'ABERTO';

CREATE TABLE item_carrinho (
    id_item_carrinho UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_carrinho UUID NOT NULL REFERENCES carrinho(id_carrinho) ON DELETE CASCADE,
    id_produto UUID NOT NULL REFERENCES produto(id_produto) ON DELETE RESTRICT,
    quantidade INTEGER NOT NULL CHECK (quantidade > 0),
    preco_unitario NUMERIC(10,2) NOT NULL CHECK (preco_unitario >= 0),
    CONSTRAINT uk_item_carrinho_produto UNIQUE (id_carrinho, id_produto)
);
CREATE INDEX idx_item_carrinho_produto ON item_carrinho(id_produto);

CREATE TABLE status_pedido (
    id_status_pedido INTEGER PRIMARY KEY,
    nome VARCHAR(30) NOT NULL UNIQUE,
    descricao VARCHAR(150)
);

CREATE TABLE pedido (
    id_pedido UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_usuario UUID NOT NULL REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    id_status_pedido INTEGER NOT NULL REFERENCES status_pedido(id_status_pedido) ON DELETE RESTRICT,
    numero_pedido VARCHAR(30) NOT NULL UNIQUE,
    data_pedido TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    subtotal NUMERIC(10,2) NOT NULL CHECK (subtotal >= 0),
    valor_desconto NUMERIC(10,2) NOT NULL CHECK (valor_desconto >= 0),
    valor_total NUMERIC(10,2) NOT NULL CHECK (valor_total >= 0),
    created_by VARCHAR(180),
    updated_by VARCHAR(180)
);
CREATE INDEX idx_pedido_usuario_data ON pedido(id_usuario, data_pedido DESC);
CREATE INDEX idx_pedido_status ON pedido(id_status_pedido);

CREATE TABLE item_pedido (
    id_item_pedido UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_pedido UUID NOT NULL REFERENCES pedido(id_pedido) ON DELETE RESTRICT,
    id_produto UUID NOT NULL REFERENCES produto(id_produto) ON DELETE RESTRICT,
    quantidade INTEGER NOT NULL CHECK (quantidade > 0),
    preco_unitario NUMERIC(10,2) NOT NULL CHECK (preco_unitario >= 0),
    subtotal NUMERIC(10,2) NOT NULL CHECK (subtotal >= 0)
);
CREATE INDEX idx_item_pedido_pedido ON item_pedido(id_pedido);

CREATE TABLE status_pagamento (
    id_status_pagamento INTEGER PRIMARY KEY,
    nome VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE pagamento (
    id_pagamento UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_pedido UUID NOT NULL REFERENCES pedido(id_pedido) ON DELETE RESTRICT,
    id_status_pagamento INTEGER NOT NULL REFERENCES status_pagamento(id_status_pagamento) ON DELETE RESTRICT,
    metodo VARCHAR(40) NOT NULL,
    valor NUMERIC(10,2) NOT NULL CHECK (valor > 0),
    transacao_externa VARCHAR(150) UNIQUE,
    data_criacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    data_confirmacao TIMESTAMP,
    created_by VARCHAR(180),
    updated_by VARCHAR(180)
);
CREATE INDEX idx_pagamento_pedido ON pagamento(id_pedido);

CREATE TABLE historico_status_pedido (
    id_historico UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_pedido UUID NOT NULL REFERENCES pedido(id_pedido) ON DELETE RESTRICT,
    id_status_pedido INTEGER NOT NULL REFERENCES status_pedido(id_status_pedido) ON DELETE RESTRICT,
    id_usuario_responsavel UUID REFERENCES usuario(id_usuario) ON DELETE SET NULL,
    data_alteracao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observacao VARCHAR(500),
    created_by VARCHAR(180),
    updated_by VARCHAR(180)
);
CREATE INDEX idx_historico_pedido_data ON historico_status_pedido(id_pedido, data_alteracao);

CREATE TABLE tipo_desconto (
    id_tipo_desconto INTEGER PRIMARY KEY,
    nome VARCHAR(20) NOT NULL UNIQUE
);

CREATE TABLE cupom (
    id_cupom UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_tipo_desconto INTEGER NOT NULL REFERENCES tipo_desconto(id_tipo_desconto) ON DELETE RESTRICT,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    descricao VARCHAR(255),
    valor_desconto NUMERIC(10,2) NOT NULL CHECK (valor_desconto > 0),
    data_inicio TIMESTAMP NOT NULL,
    data_fim TIMESTAMP NOT NULL,
    limite_utilizacoes INTEGER CHECK (limite_utilizacoes IS NULL OR limite_utilizacoes > 0),
    valor_minimo_pedido NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (valor_minimo_pedido >= 0),
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    created_by VARCHAR(180),
    updated_by VARCHAR(180),
    CONSTRAINT ck_cupom_periodo CHECK (data_fim >= data_inicio)
);

CREATE TABLE uso_cupom (
    id_uso_cupom UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_cupom UUID NOT NULL REFERENCES cupom(id_cupom) ON DELETE RESTRICT,
    id_pedido UUID NOT NULL UNIQUE REFERENCES pedido(id_pedido) ON DELETE RESTRICT,
    id_usuario UUID NOT NULL REFERENCES usuario(id_usuario) ON DELETE RESTRICT,
    data_utilizacao TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    valor_desconto_aplicado NUMERIC(10,2) NOT NULL CHECK (valor_desconto_aplicado >= 0)
);
CREATE INDEX idx_uso_cupom_cupom ON uso_cupom(id_cupom);
CREATE INDEX idx_uso_cupom_usuario ON uso_cupom(id_usuario);

INSERT INTO perfil (nome) VALUES ('ADMIN'), ('CLIENTE');
INSERT INTO status_pedido (id_status_pedido, nome, descricao) VALUES
    (1, 'PENDENTE_PAGAMENTO', 'Aguardando confirmação do pagamento'),
    (2, 'PAGO', 'Pagamento confirmado'),
    (3, 'EM_PREPARACAO', 'Pedido em preparação'),
    (4, 'ENVIADO', 'Pedido enviado'),
    (5, 'CONCLUIDO', 'Pedido concluído'),
    (6, 'CANCELADO', 'Pedido cancelado');
INSERT INTO status_pagamento (id_status_pagamento, nome) VALUES
    (1, 'PENDENTE'), (2, 'APROVADO'), (3, 'RECUSADO'), (4, 'CANCELADO');
INSERT INTO tipo_desconto (id_tipo_desconto, nome) VALUES
    (1, 'PERCENTUAL'), (2, 'VALOR_FIXO');
