# Documentação: Projeto integrador II

**Projeto:** Loja de capcakes Web

**Tecnologias utilizadas:** 
*Java, Spring, Next.js* [![My Skills](https://skillicons.dev/icons?i=java,spring,nextjs&perline=3)](https://skillicons.dev) 

---

**Situação 1:**

<br/>
<br/>

# Escopo da Aplicação Web — Loja de Cupcakes

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

## 11. Sugestão de evolução do produto

Para uma primeira versão (MVP), recomendo priorizar:

1. Autenticação com perfis de cliente e administrador.
2. Catálogo, carrinho e fluxo de compra.
3. Integração de pagamento e acompanhamento de pedidos.
4. Dashboard com indicadores diários, gráficos mensais e tabela de pedidos.
5. Cadastro e aplicação de cupons percentuais.

Em uma segunda etapa, poderão ser adicionados relatórios exportáveis, análise de produtos mais vendidos, comparação entre períodos, gestão de estoque, controle de custos e margem de lucro, campanhas promocionais e notificações automáticas de atualização dos pedidos.

Resultado esperado: uma aplicação web integrada para operação da loja de cupcakes, combinando uma experiência simples de compra para o cliente com ferramentas administrativas para acompanhar vendas, faturamento, pedidos e promoções em um único painel de gestão.

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

