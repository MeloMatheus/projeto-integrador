# Doce Miolo

Loja virtual demonstrativa de cupcakes artesanais, criada com Next.js (App Router), TypeScript, Tailwind CSS e componentes shadcn/ui.

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

1. Crie uma conta em **Cadastre-se** ou entre com uma conta já criada neste navegador.
2. Escolha produtos no cardápio e adicione-os à sacola.
3. Revise as quantidades, informe o endereço e selecione o pagamento na entrega.
4. Confirme para registrar o pedido e consultar sua confirmação ou o histórico em **Minha conta**.

A sacola, o endereço, o perfil e os pedidos ficam separados por cliente e são mantidos em `localStorage`. As senhas da demonstração não são salvas em texto puro: a aplicação deriva um hash PBKDF2 com salt no navegador. O link **Esqueci minha senha** redefine a senha localmente usando o e-mail cadastrado.

> Esta é uma demonstração acadêmica sem backend. `localStorage` e autenticação no navegador não substituem autenticação, armazenamento de senhas ou persistência seguros em produção; os dados ficam apenas no dispositivo e podem ser apagados pelo usuário. A confirmação do pedido registra a compra somente neste armazenamento local.

As fotografias de produto e banner são carregadas do Unsplash; é necessária conexão à internet para exibi-las.

## Dashboard administrativo

A tela `/admin` está protegida por perfil no fluxo demonstrativo do frontend. Use a conta administrativa de demonstração apresentada na tela de login para abrir o painel. O dashboard oferece períodos de hoje, 7 dias, 30 dias e intervalo personalizado, com indicadores, gráfico diário, situação dos pedidos e pagamentos e estados de carregamento e vazio.

O resumo é obtido por `adminDashboardService` em `src/lib/admin-dashboard.ts`. Esse serviço retorna pedidos mockados até que o backend esteja pronto; faturamento considera somente pedidos com pagamento recebido, excluindo pendentes e cancelados. Substitua a implementação `getSummary` por uma chamada à API administrativa quando a integração do backend estiver disponível.

> Os dados e as credenciais administrativas do dashboard são exclusivamente demonstrativos. A autorização no cliente não constitui controle de acesso real; a API/backend deverá autenticar a sessão e validar o perfil administrador em cada endpoint antes da implantação.
