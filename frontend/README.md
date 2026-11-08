# Doce Miolo

Loja virtual de cupcakes artesanais, criada com Next.js (App Router), TypeScript, Tailwind CSS e componentes shadcn/ui.

## Executar localmente

Requisitos: Node.js 20.9 ou superior e npm.

```bash
npm install
npm run dev
```

Acesse [http://localhost:3000](http://localhost:3000). Para validar o projeto:

```bash
npm run lint
npx tsc --noEmit
npm run build
```

## Fluxo da loja

1. Crie uma conta em **Cadastre-se** ou entre com uma conta da API.
2. Escolha produtos no cardápio e adicione-os à sacola.
3. Informe o endereço e selecione o pagamento na entrega.
4. Ao confirmar, a sacola é sincronizada com o catálogo e o pedido é registrado na API.

O cardápio consulta `GET /api/produtos` ao iniciar a aplicação. Nome, preço, imagem e disponibilidade vêm da API; produtos indisponíveis aparecem sinalizados e não podem ser adicionados. A disponibilidade é conferida novamente antes de confirmar o pedido. Login e cadastro usam `/api/auth/login` e `/api/auth/register`. A sacola fica no navegador até o checkout; endereços e pedidos são persistidos pelo backend. O token de sessão fica salvo no `localStorage` para manter a sessão entre visitas. A recuperação de senha ainda não está disponível na API.

Os proxies Next.js encaminham chamadas para `https://projeto-integrador-jqoo.onrender.com`, repassando o cabeçalho de autorização nas rotas protegidas. O checkout envia `idEndereco` e `codigoCupom` para criar o pedido; o carrinho do servidor é atualizado antes dessa solicitação.

> O token no `localStorage` é acessível ao JavaScript do navegador. Para produção, considere uma sessão em cookie `HttpOnly`, além das proteções correspondentes contra CSRF e XSS.

As fotografias de produto e banner são carregadas do Unsplash; é necessária conexão à internet para exibi-las.

## Dashboard administrativo

A tela `/admin` usa o perfil retornado pela API. O dashboard oferece períodos de hoje, 7 dias, 30 dias e intervalo personalizado, com indicadores, gráfico diário, situação dos pedidos e pagamentos e estados de carregamento e vazio.

O resumo é obtido por `adminDashboardService` em `src/lib/admin-dashboard.ts`. Esse serviço ainda retorna pedidos mockados; substitua `getSummary` por uma chamada à API administrativa quando essa integração for implementada.

> A API/backend deve validar a autorização de administrador em cada endpoint; a verificação de perfil no cliente não substitui esse controle.
