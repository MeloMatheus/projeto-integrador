# Cupcake API

API REST em Java 21 e Spring Boot 3.5.7 com PostgreSQL, Spring Security, JWT e migrações Flyway.

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

   A API fica disponível em `http://localhost:8080`; a documentação interativa em `/swagger-ui.html`. Flyway cria o esquema e os perfis `ADMIN` e `CLIENTE`, além dos status e tipos de desconto.

Para executar sem Docker, configure `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET` e `JWT_EXPIRATION`, então rode:

```powershell
.\mvnw.cmd spring-boot:run
```

As migrações são aplicadas no início da aplicação e o Hibernate valida o esquema. `JWT_SECRET` é obrigatório; o token é assinado com HS256 e expira após `JWT_EXPIRATION` segundos. Tokens emitidos não são revogados automaticamente por logout.

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
POST /api/carrinho/itens
{"idProduto":"UUID_DO_PRODUTO","quantidade":2}
POST /api/pedidos
{"codigoCupom":"DOCE10"}
POST /api/pedidos/UUID_DO_PEDIDO/pagamentos
{"metodo":"PIX"}
GET /api/pedidos
```

O cliente autenticado só acessa seu carrinho, pedidos e pagamentos. O carrinho usa o preço atual do produto quando o item é adicionado/atualizado e o checkout recalcula e registra o preço vigente no pedido. Pedidos cujo total seja zero após desconto são finalizados como pagos sem criar uma tentativa de cobrança. Produtos e cupons são gerenciados por `ADMIN`; as alterações de status ficam restritas a rotas administrativas e geram histórico.

Cupons são criados por um administrador em `POST /api/admin/cupons`; por exemplo, envie `{"codigo":"DOCE10","tipoDesconto":"PERCENTUAL","valorDesconto":10,"dataInicio":"2026-01-01T00:00:00","dataFim":"2027-01-01T00:00:00","valorMinimoPedido":0,"ativo":true}`. Para alterar o status do pedido, use `PUT /api/pedidos/admin/UUID_DO_PEDIDO/status` com `{"status":"EM_PREPARACAO","observacao":"Produção iniciada"}`. A confirmação demonstrativa do pagamento usa `POST /api/admin/pagamentos/UUID_DO_PAGAMENTO/confirmacao` com `{"resultado":"APROVADO","transacaoExterna":"ref-do-operador"}`.

O dashboard administrativo fica em `GET /api/admin/dashboard` e contabiliza pagamentos aprovados para o faturamento confirmado.

O fluxo de pagamento é demonstrativo: a criação gera uma tentativa pendente e apenas um administrador autenticado pode registrar a confirmação. Não há integração com adquirente, cobrança real nem armazenamento de dados de cartão. Uma confirmação aprovada exige uma referência externa e é idempotente por tentativa.

O registro público não aceita atribuição de perfil; não existe promoção de conta por requisição da API.

## Testes

```powershell
.\mvnw.cmd test
```

Os testes usam H2 em memória. O perfil de produção/local utiliza PostgreSQL e Flyway.
