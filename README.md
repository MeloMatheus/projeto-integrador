# Documentação: Projeto integrador II

**Projeto:** Loja de capcakes Web

**Tecnologias utilizadas:** 
*Java, Spring, Next.js* [![My Skills](https://skillicons.dev/icons?i=java,spring,nextjs&perline=3)](https://skillicons.dev) 

---

**Situação 1:**

<br/>
<br/>

**Escopo da Aplicação Web - Loja de Cupcakes**

***Objetivo***

Desenvolver uma aplicação web para uma loja de cupcakes, permitindo que os clientes realizem seu cadastro, naveguem pelos produtos, efetuem compras e acompanhem o status de seus pedidos.

***Telas da Aplicação***

A aplicação deverá conter as seguintes telas:

- Tela de Login
- Tela de Cadastro
- Tela da Loja (Catálogo de Produtos)
- Carrinho de Compras
- Tela de Pagamento
- Tela de Acompanhamento de Pedido


***Fluxo de Navegação***

Ao acessar a aplicação, o sistema deverá verificar se o cliente possui uma sessão ativa.

Caso o cliente esteja autenticado, ele deverá ser direcionado automaticamente para a Tela da Loja.
Caso não esteja autenticado, ele deverá ser redirecionado para a Tela de Login, onde poderá realizar o login ou acessar a Tela de Cadastro para criar uma nova conta.

Após efetuar o login, o cliente terá acesso à Tela da Loja, onde poderá visualizar os cupcakes disponíveis e selecionar os produtos desejados.

Ao selecionar um produto, ele deverá ser adicionado ao Carrinho de Compras. O cliente poderá acessar o carrinho a qualquer momento para:

- Visualizar os produtos selecionados;
- Alterar a quantidade de itens;
- Remover produtos do carrinho;
- Visualizar o valor total da compra.

Ao prosseguir com a compra, o sistema deverá direcionar o cliente para a Tela de Pagamento, onde serão informados os dados necessários para a conclusão do pedido.

Após a confirmação do pagamento, o pedido deverá ser registrado no sistema e o cliente será automaticamente direcionado para a Tela de Acompanhamento de Pedido.

***Funcionalidades da Tela de Acompanhamento***

A tela de acompanhamento deverá exibir informações atualizadas sobre o status do pedido, incluindo etapas como:

- Pedido recebido;
- Pedido em preparação;
- Pedido em transporte;
- Pedido entregue.

***Requisitos Funcionais***

- Permitir cadastro de novos clientes.
- Permitir autenticação de usuários.
- Exibir catálogo de cupcakes disponíveis.
- Adicionar produtos ao carrinho de compras.
- Alterar quantidade de produtos no carrinho.
- Remover produtos do carrinho.
- Calcular o valor total da compra.
- Processar o pagamento do pedido.
- Permitir o acompanhamento do status do pedido após a compra.

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