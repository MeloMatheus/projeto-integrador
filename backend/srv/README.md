# Cupcake API

API REST em Java 21 e Spring Boot 3.5.7 com PostgreSQL, Spring Security e JWT.

## Execução local

1. Copie `.env.example` para `.env` e substitua o valor de `JWT_SECRET` por uma chave Base64 aleatória que decodifique para pelo menos 32 bytes. No PowerShell:

   ```powershell
   $bytes = [byte[]]::new(32)
   [System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
   [Convert]::ToBase64String($bytes)
   ```

2. Inicie PostgreSQL e a API com Docker Compose:

   ```powershell
   docker compose up --build
   ```

   A API fica disponível em `http://localhost:8081`; a documentação interativa em `/swagger-ui.html`. Em uma instalação nova, o PostgreSQL executa `src/main/resources/db/schema.sql` ao criar o volume, inicializando o esquema e os dados de referência.

O arquivo `.env` é lido pelo Docker Compose, não por uma execução direta da aplicação ou pela IDE. Para executar sem Docker, crie o banco e execute `src/main/resources/db/schema.sql` nele antes de iniciar a aplicação. Configure `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` e `JWT_EXPIRATION` no ambiente do processo. No PowerShell, gere e defina uma chave apenas para a sessão atual e inicie a API no mesmo terminal:

```powershell
$bytes = [byte[]]::new(32)
[System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
.\mvnw.cmd spring-boot:run
```

Ao iniciar pela IDE, configure `JWT_SECRET` nas variáveis de ambiente da configuração de execução usando uma chave Base64 aleatória que decodifique para pelo menos 32 bytes. Não use a chave de exemplo em produção nem a versione no repositório.

O Hibernate valida o esquema existente ao iniciar; a aplicação não cria nem atualiza tabelas automaticamente. `JWT_SECRET` é obrigatório; o token é assinado com HS256 e expira após `JWT_EXPIRATION` segundos. Tokens emitidos não são revogados automaticamente por logout.

Se a inicialização falhar ao determinar o dialeto, confirme que PostgreSQL está em execução, que o banco indicado por `DB_URL` existe e que `DB_USERNAME`/`DB_PASSWORD` estão corretos. O dialeto PostgreSQL é configurado explicitamente; isso não elimina a necessidade de uma conexão JDBC funcional nem da criação prévia do esquema.

Para provisionar o primeiro administrador, preencha `INITIAL_ADMIN_EMAIL` e `INITIAL_ADMIN_PASSWORD` no `.env` (senha de pelo menos 12 caracteres) antes do primeiro início. O usuário é criado apenas se o e-mail ainda não existir; a configuração não promove nem altera contas existentes. Remova a senha do ambiente após o provisionamento.

## Fluxos principais

```http
POST /api/auth/register
Content-Type: application/json

{"nome":"Cliente","email":"cliente@example.com","senha":"senha-com-8"}
```

Registro público sempre cria `CLIENTE`. Autentique com `POST /api/auth/login` usando `{"email":"cliente@example.com","senha":"senha-com-8"}` e envie o token retornado como `Authorization: Bearer <token>`.

```http
GET /api/produtos?page=0&size=10&sort=nome,asc
GET /api/enderecos/UUID_DO_USUARIO
POST /api/enderecos
{"identificacao":"Casa","destinatario":"Cliente","cep":"01000-000","logradouro":"Rua Exemplo","numero":"10","bairro":"Centro","cidade":"São Paulo","uf":"SP","principal":true}
GET /api/carrinho/UUID_DO_USUARIO
POST /api/carrinho/itens
{"idProduto":"UUID_DO_PRODUTO","quantidade":2}
POST /api/pedidos
{"idEndereco":"UUID_DO_ENDERECO","codigoCupom":"DOCE10"}
GET /api/pedidos/usuario/UUID_DO_USUARIO
POST /api/pedidos/UUID_DO_PEDIDO/pagamentos
{"metodo":"PIX","chaveIdempotencia":"CHAVE_UNICA_DA_TENTATIVA"}
GET /api/pedidos
```

As consultas de endereços e carrinho recebem o ID do usuário na rota; a consulta de pedidos filtrada por usuário usa `/api/pedidos/usuario/{idUsuario}` para não conflitar com a busca de um pedido por ID. Clientes só podem consultar os próprios dados; administradores podem filtrar pedidos por qualquer usuário. O checkout exige um endereço previamente salvo e copia seus dados para o pedido; o frete permanece zerado até haver uma regra de cálculo. A chave de idempotência da tentativa de pagamento permite que o cliente repita a requisição sem criar outra cobrança. O carrinho usa o preço atual do produto quando o item é adicionado/atualizado e o checkout recalcula e registra o preço e nome vigentes no pedido. Pedidos cujo total seja zero após desconto são finalizados como pagos sem criar uma tentativa de cobrança. Produtos e cupons são gerenciados por `ADMIN`; as alterações de status ficam restritas a rotas administrativas e geram histórico.

Cupons são criados por um administrador em `POST /api/admin/cupons`; por exemplo, envie `{"codigo":"DOCE10","tipoDesconto":"PERCENTUAL","valorDesconto":10,"dataInicio":"2026-01-01T00:00:00","dataFim":"2027-01-01T00:00:00","valorMinimoPedido":0,"ativo":true}`. Para alterar o status do pedido, use `PUT /api/pedidos/admin/UUID_DO_PEDIDO/status` com `{"status":"EM_PREPARACAO","observacao":"Produção iniciada"}`. A confirmação demonstrativa do pagamento usa `POST /api/admin/pagamentos/UUID_DO_PAGAMENTO/confirmacao` com `{"resultado":"APROVADO","transacaoExterna":"ref-do-operador"}`.

O dashboard administrativo fica em `GET /api/admin/dashboard` e contabiliza pagamentos aprovados para o faturamento confirmado.

O fluxo de pagamento é demonstrativo: a criação gera uma tentativa pendente e apenas um administrador autenticado pode registrar a confirmação. Não há integração com adquirente, cobrança real nem armazenamento de dados de cartão. Uma confirmação aprovada exige uma referência externa e é idempotente por tentativa.

O registro público não aceita atribuição de perfil; não existe promoção de conta por requisição da API.

## Testes

```powershell
.\mvnw.cmd test
```

Os testes usam H2 em memória. O perfil de produção/local utiliza PostgreSQL e exige que o esquema já tenha sido criado.
