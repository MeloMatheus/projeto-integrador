# Documentação: Projeto integrador II

**Projeto:** Loja de capcakes Web

**Tecnologias utilizadas:** 
*Java, Spring, Next.js* [![My Skills](https://skillicons.dev/icons?i=java,spring,nextjs&perline=3)](https://skillicons.dev) 

---
<br/>
<br/>
<br/>

# Escopo da Aplicação Web — Loja de Cupcakes

<br/>

## 1. Objetivo

Desenvolver uma aplicação web para uma loja de cupcakes, permitindo que os clientes realizem seu cadastro, naveguem pelo catálogo de produtos, efetuem compras, acompanhem seus pedidos e utilizem cupons de desconto.

A aplicação também deverá possuir um painel administrativo (Dashboard), destinado aos usuários com perfil de administrador, para gerenciar as vendas, acompanhar os pedidos, visualizar indicadores financeiros e administrar cupons promocionais.

O sistema deverá possuir controle de acesso baseado em perfis de usuário, garantindo que clientes e administradores sejam direcionados para suas respectivas áreas após a autenticação.

## 2. Perfis de usuário

A aplicação contará com dois perfis principais:

- Cliente: poderá realizar cadastro, autenticar-se, visualizar produtos, gerenciar o carrinho, efetuar pagamentos e acompanhar pedidos.
- Administrador: poderá acessar o dashboard de gestão, visualizar indicadores de vendas e financeiros, consultar pedidos, acompanhar seus status e cadastrar e gerenciar cupons de desconto.

## 3. Telas da aplicação

### 3.1. Área do cliente

- Tela de Login.
- Tela de Cadastro.
- Tela da Loja (Catálogo de Produtos).
- Carrinho de Compras.
- Tela de Pagamento.
- Tela de Acompanhamento de Pedido.

### 3.2. Área administrativa

- Dashboard de Gestão: visualização de indicadores comerciais, financeiros e operacionais.
- Gestão de Pedidos: consulta e acompanhamento dos pedidos realizados, com filtros por status e data.
- Gestão de Cupons: cadastro, consulta, edição, ativação e desativação de cupons promocionais.

## 4. Fluxo de navegação e autenticação

Ao acessar a aplicação, o sistema deverá verificar se o usuário possui uma sessão autenticada e identificar seu perfil de acesso.

- Cliente autenticado: será direcionado automaticamente para a Tela da Loja.
- Administrador autenticado: será direcionado automaticamente para o Dashboard de Gestão.
- Usuário não autenticado: será direcionado para a Tela de Login, com a opção de acessar a Tela de Cadastro.

Após o login, o sistema deverá identificar o perfil do usuário e aplicar as permissões correspondentes.

O acesso às rotas administrativas deverá ser restrito a usuários com perfil de administrador, com validação de permissões também no backend.

## 5. Funcionalidades da área do cliente

### 5.1. Cadastro e autenticação

- Permitir o cadastro de novos clientes.
- Permitir login utilizando e-mail e senha.
- Validar as credenciais informadas.
- Manter e encerrar sessões de forma segura.
- Redirecionar o usuário conforme seu perfil de acesso.

### 5.2. Catálogo de produtos

- Exibir os cupcakes disponíveis para compra.
- Apresentar nome, descrição, imagem e preço dos produtos.
- Permitir selecionar produtos para adicionar ao carrinho.

### 5.3. Carrinho de compras

- Adicionar produtos ao carrinho.
- Alterar a quantidade de cada item.
- Remover produtos selecionados.
- Calcular o subtotal e o valor total da compra.
- Permitir inserir e validar cupons de desconto.
- Exibir o desconto aplicado e o valor final a pagar.

### 5.4. Pagamento e confirmação do pedido

- Exibir o resumo dos produtos e valores da compra.
- Permitir informar os dados necessários para a modalidade de pagamento selecionada.
- Validar o cupom de desconto, quando aplicável.
- Integrar o sistema a um provedor de pagamentos.
- Registrar o pedido após a confirmação do pagamento, conforme as regras da integração.
- Redirecionar o cliente para a Tela de Acompanhamento do Pedido após a confirmação da compra.

### 5.5. Acompanhamento de pedidos

O cliente poderá visualizar as informações do pedido e acompanhar seu andamento por meio das seguintes etapas:

1. Pedido recebido: pedido registrado pelo sistema.
2. Pedido em preparação: produtos sendo preparados.
3. Pedido em transporte: pedido encaminhado para entrega.
4. Pedido entregue: entrega concluída.

O status deverá ser atualizado conforme as alterações realizadas pela operação da loja. O cliente deverá visualizar o status mais recente, a identificação do pedido e a data da compra.

## 6. Dashboard administrativo — Gestão da aplicação

O dashboard será a tela inicial do administrador após a autenticação. Seu objetivo será oferecer uma visão consolidada do desempenho comercial, financeiro e operacional da loja, facilitando a tomada de decisões.

### 6.1. Indicadores principais (cards)

Na parte superior do dashboard, deverão ser exibidos cards com os principais indicadores:

![](<assets-doc/modelo-report-status.png>)

Representação conceitual dos indicadores. Os valores serão carregados a partir dos dados reais da aplicação.

Os indicadores deverão ser atualizados de acordo com os dados registrados no sistema, considerando o período selecionado e as regras de cálculo definidas para cada métrica.

### 6.2. Gráfico de vendas e faturamento por período

O dashboard deverá conter um gráfico para análise do desempenho da loja nos últimos X meses.

Funcionalidades previstas:

- Selecionar o período de análise: últimos 3, 6, 12 meses ou um intervalo personalizado.
- Visualizar a quantidade de vendas ou pedidos realizados por mês.
- Visualizar o faturamento mensal em reais (R$).
- Alternar entre visualização de quantidade de pedidos e faturamento.
- Comparar o desempenho entre os meses.
- Atualizar o gráfico conforme o período selecionado.

O gráfico poderá utilizar barras para representar o faturamento ou a quantidade de pedidos, com opção de visualização em linhas para facilitar a análise de tendências.

### 6.3. Gráfico de pedidos por status

O dashboard deverá apresentar a distribuição dos pedidos conforme seu status atual, permitindo identificar rapidamente o volume de pedidos em cada etapa.

Os status previstos são:

- Pedido recebido.
- Pedido em preparação.
- Pedido em transporte.
- Pedido entregue.

A visualização poderá ser feita por meio de um gráfico de rosca ou de barras, acompanhado da quantidade e da porcentagem de pedidos em cada status.

Como regra de negócio, pedidos cancelados e pagamentos pendentes ou recusados deverão ser identificados separadamente, caso essas situações sejam suportadas pelo fluxo de pedidos.

### 6.4. Tabela de pedidos por status

O administrador deverá ter acesso a uma tabela com os pedidos registrados no sistema.

| Campo | Descrição |
| --- | --- |
| Nº do pedido | Identificador único do pedido |
| Cliente | Nome do cliente responsável pela compra |
| Data e hora | Momento do registro do pedido |
| Valor total | Valor final da compra, considerando descontos |
| Pagamento | Situação do pagamento |
| Status do pedido | Etapa atual do pedido |
| Ações | Visualizar detalhes e atualizar status |

A tabela deverá disponibilizar:

- Filtro por status do pedido.
- Filtro por período ou data.
- Busca por número do pedido ou nome do cliente.
- Ordenação por data e valor.
- Paginação dos resultados.
- Acesso aos detalhes do pedido.
- Atualização do status conforme as permissões do administrador.

O administrador deverá conseguir consultar rapidamente os pedidos recebidos, em preparação, em transporte e entregues.

### 6.5. Gestão de cupons de desconto

A aplicação deverá possuir uma área específica para cadastro e gerenciamento de cupons promocionais.

Funcionalidades previstas:

- Cadastrar novos cupons.
- Editar informações de cupons existentes.
- Ativar e desativar cupons.
- Consultar cupons cadastrados.
- Definir a porcentagem de desconto.
- Definir o período de validade.
- Definir a quantidade máxima de utilizações.
- Definir o valor mínimo do pedido, quando aplicável.
- Consultar a quantidade de vezes que cada cupom foi utilizado.

Dados do cadastro de cupom:

| Campo | Descrição |
| --- | --- |
| Código do cupom | Código informado pelo cliente no carrinho |
| Descrição | Finalidade ou campanha promocional |
| Tipo de desconto | Percentual ou valor fixo, se suportado |
| Porcentagem de desconto | Percentual aplicado sobre a base elegível |
| Data de início | Início da validade do cupom |
| Data de término | Fim da validade do cupom |
| Limite de utilizações | Número máximo de usos permitidos |
| Valor mínimo do pedido | Valor mínimo para aplicação |
| Status | Ativo ou inativo |

O percentual de desconto deverá ser configurável pelo administrador, por exemplo, 5%, 10%, 15% ou 20%.

A aplicação deverá validar automaticamente o cupom no momento da compra, verificando se está ativo, dentro do período de validade, dentro do limite de utilizações e de acordo com as demais regras configuradas.

O valor do desconto deverá ser calculado no backend, evitando que alterações no frontend permitam descontos indevidos.

## 7. Requisitos funcionais consolidados

| ID | Requisito |
| --- | --- |
| RF01 | Permitir o cadastro de novos clientes. |
| RF02 | Permitir a autenticação por e-mail e senha. |
| RF03 | Identificar o perfil do usuário após o login. |
| RF04 | Redirecionar clientes para a loja e administradores para o dashboard. |
| RF05 | Restringir as funcionalidades administrativas por perfil de acesso. |
| RF06 | Exibir o catálogo de cupcakes disponíveis. |
| RF07 | Permitir adicionar, alterar e remover itens do carrinho. |
| RF08 | Calcular subtotal, descontos e valor total da compra. |
| RF09 | Permitir pagamento e registro de pedidos. |
| RF10 | Permitir o acompanhamento do status dos pedidos pelo cliente. |
| RF11 | Exibir indicadores de pedidos e faturamento no dashboard. |
| RF12 | Apresentar gráficos de vendas e faturamento por período. |
| RF13 | Apresentar a distribuição de pedidos por status. |
| RF14 | Permitir consultar, filtrar e pesquisar pedidos. |
| RF15 | Permitir que o administrador atualize o status dos pedidos. |
| RF16 | Permitir cadastrar, editar, ativar e desativar cupons. |
| RF17 | Validar cupons e aplicar descontos conforme as regras configuradas. |
| RF18 | Exibir informações atualizadas de vendas, pedidos e cupons. |

## 8. Requisitos não funcionais

- Segurança: senhas armazenadas com hash seguro, controle de acesso por perfil e validação de permissões no backend.
- Integridade financeira: cálculos de preços, descontos e valores finais validados no servidor.
- Confiabilidade: registro consistente de pedidos e confirmação de pagamento por integração confiável.
- Usabilidade: interface intuitiva, responsiva e adaptada para computadores, tablets e celulares.
- Desempenho: carregamento eficiente dos gráficos, tabelas e indicadores administrativos.
- Atualização de dados: dashboard e acompanhamento de pedidos deverão refletir os dados mais recentes disponíveis.
- Auditoria: registrar alterações relevantes nos status dos pedidos e nas configurações dos cupons.
- Privacidade: proteger os dados pessoais e financeiros dos clientes, observando a legislação aplicável, incluindo a LGPD.

## 9. Regras de negócio importantes

1. O acesso ao dashboard deverá ser permitido exclusivamente a usuários com perfil de administrador.
2. A existência de uma sessão autenticada não será suficiente para liberar funcionalidades administrativas; o perfil e as permissões também deverão ser validados.
3. Os pedidos somente deverão ser considerados vendas pagas após a confirmação efetiva do pagamento pelo provedor.
4. O faturamento deverá ser calculado com base em uma regra consistente, considerando os pagamentos confirmados e tratando cancelamentos, estornos e reembolsos de forma apropriada.
5. A quantidade de pedidos realizados no dia deverá considerar a data local da loja.
6. A atualização do status de um pedido deverá refletir tanto na área administrativa quanto na tela de acompanhamento do cliente.
7. Os cupons deverão respeitar suas regras de validade, limites de utilização e critérios de elegibilidade.
8. O sistema deverá manter o histórico das operações importantes para facilitar consultas e auditorias.

## 10. Critérios de aceite

A aplicação será considerada funcional quando os seguintes cenários forem atendidos:

- Um cliente consegue criar uma conta, realizar login e acessar a loja.
- Um administrador consegue realizar login e é direcionado ao dashboard.
- Um cliente não consegue acessar rotas ou funcionalidades administrativas.
- O cliente consegue adicionar produtos ao carrinho, modificar quantidades e concluir o fluxo de compra.
- O cliente consegue visualizar o status atualizado do pedido após a compra.
- O dashboard apresenta a quantidade de pedidos do dia e os indicadores financeiros calculados com dados reais.
- O administrador consegue selecionar períodos e visualizar os gráficos de vendas e faturamento.
- O administrador consegue filtrar pedidos por status e consultar seus detalhes.
- O administrador consegue cadastrar um cupom com desconto percentual e configurar sua validade.
- Um cupom válido aplica o desconto correto, enquanto um cupom inválido é rejeitado.
- As informações financeiras e os status apresentados são consistentes com os registros do sistema.

<br/>
<br/>
<br/>

## **Diagramas**

***Diagrama de caso de uso***

<br/>

![alt text](<assets-doc/UML use case.svg>)

***Diagrama de atividade***
<br/>

![alt text](<assets-doc/UML atividade.svg>)

***Diagrama de classes***
<br/>

![alt text](<assets-doc/diagrama de classe.svg>)

***Diagrama de sequência***
<br/>

![alt text](<assets-doc/diagrama de sequencia.svg>)

<br/>

## IHC

**Mapa conceitual**

![Mapa conceitual IHC](<assets-doc/mapa conceitual ihc.svg>)

**Protótipo design**

***Protótipo Web - acesso via mobile (cliente)***

![Login](<assets-doc/Cliente · Entrar.png>)
<br/>

![Criar conta](<assets-doc/Cliente · Criar conta.png>)
<br/>

![Loja mobile](<assets-doc/Cliente · Loja mobile.png>)
<br/>

![Carrinho mobile](<assets-doc/Cliente · Carrinho mobile.png>)
<br/>

![Pagamento mobile](<assets-doc/Cliente · Pagamento.png>)
<br/>

![Comprovante pagamento mobile](<assets-doc/Cliente · Compra confirmada.png>)
<br/>

![Acompanhar pedido mobile](<assets-doc/Cliente · Acompanhar pedido.png>)
<br/>

<br/>

***Protótipo Web - acesso via mobile (administrador)***

![Visao geral mobile](<assets-doc/Administração · Visão geral mobile.png>)

<br/>
<br/>

***Protóripo Web (cliente)***

![Loja desktop](<assets-doc/Cliente · Loja desktop.png>)
<br/>

![Carrinho desktop](<assets-doc/Cliente · Carrinho desktop.png>)
<br/>

***Protótipo Administração via desktop***
![Visao geral desktop](<assets-doc/Administração · Visão geral.png>)
<br/>

![Relatorios](<assets-doc/Administração · Relatórios.png>)
<br/>

![Pedidos](<assets-doc/Administração · Pedidos.png>)
<br/>

![Cupons](<assets-doc/Administração · Cupons.png>)
<br/>

![Editar cupons](<assets-doc/Administração · Editar cupom.png>)
<br/>

## Diagrama de classes de persistencia

![Diagram de classe de persistencia](<assets-doc/mapa conceitual persistencia.svg>)

<br/>
<br/>

***Projeto lógico normalizado***

![Projeto lógico normalizado](<assets-doc/projeto logico normalizado.svg>)

## Dicionário de dados

O dicionário documenta a estrutura de cada tabela, os tipos físicos e as regras de integridade. Os tipos foram definidos considerando o Postgres.

### Tabelas de domínio e usuários

| Tabela | Campo | Tipo | Restrição / finalidade |
| --- | --- | --- | --- |
| `perfil` | `id_perfil` | CHAR(36) | PK, UUID v4 |
| `perfil` | `nome` | VARCHAR(30) | Obrigatório, único |
| `perfil` | `descricao` | VARCHAR(150) | Descrição do perfil |
| `usuario` | `id_usuario` | CHAR(36) | PK, UUID v4 |
| `usuario` | `id_perfil` | CHAR(36) | FK → `perfil` |
| `usuario` | `nome` | VARCHAR(120) | Nome do usuário |
| `usuario` | `email` | VARCHAR(180) | Obrigatório, único |
| `usuario` | `senha_hash` | VARCHAR(255) | Hash seguro da senha |
| `usuario` | `telefone` | VARCHAR(25) | Opcional |
| `usuario` | `ativo` | BOOLEAN | Ativo por padrão |
| `usuario` | `criado_em` | DATETIME(3) | Data de criação |
| `usuario` | `atualizado_em` | DATETIME(3) | Atualização automática |
| `endereco` | `id_endereco` | CHAR(36) | PK, UUID v4 |
| `endereco` | `id_usuario` | CHAR(36) | FK → `usuario` |
| `endereco` | `identificacao` | VARCHAR(50) | Ex.: Casa, Trabalho |
| `endereco` | `destinatario` | VARCHAR(120) | Nome do destinatário |
| `endereco` | `cep` | VARCHAR(10) | CEP do endereço |
| `endereco` | `logradouro` | VARCHAR(150) | Rua, avenida etc. |
| `endereco` | `numero` | VARCHAR(20) | Número do imóvel |
| `endereco` | `complemento` | VARCHAR(100) | Opcional |
| `endereco` | `bairro` | VARCHAR(100) | Bairro |
| `endereco` | `cidade` | VARCHAR(100) | Cidade |
| `endereco` | `uf` | CHAR(2) | Unidade federativa |
| `endereco` | `referencia` | VARCHAR(200) | Ponto de referência |
| `endereco` | `principal` | BOOLEAN | Indica endereço preferencial |
| `endereco` | `criado_em` | DATETIME(3) | Data de cadastro |

### Produtos e carrinho

| Tabela | Campo | Tipo | Restrição / finalidade |
| --- | --- | --- | --- |
| `produto` | `id_produto` | CHAR(36) | PK, UUID v4 |
| `produto` | `nome` | VARCHAR(120) | Nome comercial |
| `produto` | `descricao` | TEXT | Descrição do cupcake |
| `produto` | `imagem_url` | VARCHAR(500) | Caminho ou URL da imagem |
| `produto` | `preco` | DECIMAL(10,2) | Não negativo |
| `produto` | `disponivel` | BOOLEAN | Disponibilidade para compra |
| `produto` | `criado_em` | DATETIME(3) | Data de criação |
| `produto` | `atualizado_em` | DATETIME(3) | Atualização automática |
| `carrinho` | `id_carrinho` | CHAR(36) | PK, UUID v4 |
| `carrinho` | `id_usuario` | CHAR(36) | FK → `usuario` |
| `carrinho` | `status` | VARCHAR(20) | ATIVO, FINALIZADO ou ABANDONADO |
| `carrinho` | `criado_em` | DATETIME(3) | Criação do carrinho |
| `carrinho` | `atualizado_em` | DATETIME(3) | Última atualização |
| `item_carrinho` | `id_item_carrinho` | CHAR(36) | PK, UUID v4 |
| `item_carrinho` | `id_carrinho` | CHAR(36) | FK → `carrinho` |
| `item_carrinho` | `id_produto` | CHAR(36) | FK → `produto` |
| `item_carrinho` | `quantidade` | INT UNSIGNED | Maior que zero |
| `item_carrinho` | `preco_unitario` | DECIMAL(10,2) | Preço registrado no carrinho |

A restrição `UNIQUE (id_carrinho, id_produto)` impede que o mesmo produto apareça em várias linhas do mesmo carrinho. A alteração de quantidade deve atualizar a linha existente.

### Pedidos e itens

| Tabela | Campo | Tipo | Restrição / finalidade |
| --- | --- | --- | --- |
| `status_pedido` | `id_status_pedido` | CHAR(36) | PK, UUID v4 |
| `status_pedido` | `nome` | VARCHAR(30) | Único; identifica a etapa |
| `status_pedido` | `descricao` | VARCHAR(150) | Descrição da etapa |
| `status_pedido` | `ordem` | TINYINT UNSIGNED | Ordem de exibição |
| `pedido` | `id_pedido` | CHAR(36) | PK, UUID v4 |
| `pedido` | `numero_pedido` | VARCHAR(30) | Identificador público único |
| `pedido` | `id_usuario` | CHAR(36) | FK → `usuario` |
| `pedido` | `id_status_pedido` | CHAR(36) | FK → `status_pedido` |
| `pedido` | `data_pedido` | DATETIME(3) | Data e hora do registro |
| `pedido` | `subtotal` | DECIMAL(10,2) | Valor dos itens antes do desconto e frete |
| `pedido` | `valor_desconto` | DECIMAL(10,2) | Desconto total aplicado |
| `pedido` | `valor_frete` | DECIMAL(10,2) | Custo da entrega |
| `pedido` | `valor_total` | DECIMAL(10,2) | Total final registrado |
| `pedido` | Campos `*_entrega` | VARCHAR / CHAR | Cópia do endereço na compra |
| `item_pedido` | `id_item_pedido` | CHAR(36) | PK, UUID v4 |
| `item_pedido` | `id_pedido` | CHAR(36) | FK → `pedido` |
| `item_pedido` | `id_produto` | CHAR(36) | FK → `produto` |
| `item_pedido` | `nome_produto_snapshot` | VARCHAR(120) | Nome na data da compra |
| `item_pedido` | `quantidade` | INT UNSIGNED | Quantidade comprada |
| `item_pedido` | `preco_unitario` | DECIMAL(10,2) | Preço histórico |
| `item_pedido` | `subtotal` | DECIMAL(12,2) | Coluna calculada: quantidade × preço |

Os campos de endereço do pedido são deliberadamente armazenados como cópia histórica. Assim, uma alteração futura no endereço cadastrado pelo cliente não modifica os dados de uma entrega anterior.

### Pagamentos e histórico

| Tabela | Campo | Tipo | Restrição / finalidade |
| --- | --- | --- | --- |
| `status_pagamento` | `id_status_pagamento` | CHAR(36) | PK, UUID v4 |
| `status_pagamento` | `nome` | VARCHAR(30) | Único |
| `status_pagamento` | `descricao` | VARCHAR(150) | Descrição do estado |
| `pagamento` | `id_pagamento` | CHAR(36) | PK, UUID v4 |
| `pagamento` | `id_pedido` | CHAR(36) | FK → `pedido` |
| `pagamento` | `id_status_pagamento` | CHAR(36) | FK → `status_pagamento` |
| `pagamento` | `metodo` | VARCHAR(40) | Modalidade de pagamento |
| `pagamento` | `valor` | DECIMAL(10,2) | Valor da tentativa ou transação |
| `pagamento` | `transacao_externa` | VARCHAR(150) | Referência do provedor |
| `pagamento` | `chave_idempotencia` | VARCHAR(100) | Única por operação |
| `pagamento` | `data_criacao` | DATETIME(3) | Registro da tentativa |
| `pagamento` | `data_confirmacao` | DATETIME(3) | Confirmação efetiva |
| `pagamento` | `atualizado_em` | DATETIME(3) | Última atualização |
| `historico_status_pedido` | `id_historico` | CHAR(36) | PK, UUID v4 |
| `historico_status_pedido` | `id_pedido` | CHAR(36) | FK → `pedido` |
| `historico_status_pedido` | `id_status_pedido` | CHAR(36) | FK → `status_pedido` |
| `historico_status_pedido` | `id_usuario_responsavel` | CHAR(36) | FK → `usuario`, opcional |
| `historico_status_pedido` | `data_alteracao` | DATETIME(3) | Momento da alteração |
| `historico_status_pedido` | `observacao` | VARCHAR(500) | Justificativa ou detalhe |

### Cupons e auditoria

| Tabela | Campo | Tipo | Restrição / finalidade |
| --- | --- | --- | --- |
| `tipo_desconto` | `id_tipo_desconto` | CHAR(36) | PK, UUID v4 |
| `tipo_desconto` | `nome` | VARCHAR(20) | PERCENTUAL ou VALOR\_FIXO |
| `tipo_desconto` | `descricao` | VARCHAR(100) | Descrição |
| `cupom` | `id_cupom` | CHAR(36) | PK, UUID v4 |
| `cupom` | `id_tipo_desconto` | CHAR(36) | FK → `tipo_desconto` |
| `cupom` | `codigo` | VARCHAR(50) | Único |
| `cupom` | `descricao` | VARCHAR(255) | Finalidade promocional |
| `cupom` | `valor_desconto` | DECIMAL(10,2) | Percentual ou valor fixo |
| `cupom` | `data_inicio` | DATETIME | Início da validade |
| `cupom` | `data_fim` | DATETIME | Fim da validade |
| `cupom` | `limite_utilizacoes` | INT UNSIGNED | Limite global; nulo significa sem limite |
| `cupom` | `valor_minimo_pedido` | DECIMAL(10,2) | Valor mínimo elegível |
| `cupom` | `ativo` | BOOLEAN | Ativação administrativa |
| `cupom` | `criado_em` | DATETIME(3) | Criação |
| `cupom` | `atualizado_em` | DATETIME(3) | Atualização automática |
| `uso_cupom` | `id_uso_cupom` | CHAR(36) | PK, UUID v4 |
| `uso_cupom` | `id_cupom` | CHAR(36) | FK → `cupom` |
| `uso_cupom` | `id_pedido` | CHAR(36) | FK → `pedido`, único |
| `uso_cupom` | `id_usuario` | CHAR(36) | FK → `usuario` |
| `uso_cupom` | `data_utilizacao` | DATETIME(3) | Momento da aplicação |
| `uso_cupom` | `valor_desconto_aplicado` | DECIMAL(10,2) | Desconto histórico |
| `auditoria` | `id_auditoria` | CHAR(36) | PK, UUID v4 |
| `auditoria` | `id_usuario` | CHAR(36) | FK → `usuario`, opcional |
| `auditoria` | `entidade` | VARCHAR(80) | Entidade afetada |
| `auditoria` | `id_registro` | VARCHAR(80) | Identificador do registro afetado |
| `auditoria` | `operacao` | VARCHAR(20) | INSERT, UPDATE, DELETE ou STATUS |
| `auditoria` | `detalhes` | JSON | Dados relevantes da operação |
| `auditoria` | `data_evento` | DATETIME(3) | Data e hora do evento |

