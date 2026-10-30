"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useMemo, useRef, useState, type FormEvent } from "react";
import {
  ArrowLeft,
  ArrowRight,
  ArrowUpRight,
  CakeSlice,
  Check,
  CheckCircle2,
  Clock3,
  CreditCard,
  Heart,
  LockKeyhole,
  LogOut,
  MapPin,
  Minus,
  PackageCheck,
  Plus,
  Search,
  ShoppingBag,
  Sparkles,
  Trash2,
  Truck,
  UserRound,
  Wallet,
  Eye,
  EyeOff,
} from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { Separator } from "@/components/ui/separator";
import { Textarea } from "@/components/ui/textarea";
import { AdminDashboard } from "@/components/admin/admin-dashboard";
import { DEMO_ADMIN_CREDENTIALS, DEMO_CUSTOMER_CREDENTIALS, useShop } from "@/lib/shop-store";
import {
  calculateDelivery,
  categories,
  formatCep,
  formatCurrency,
  formatPhone,
  isValidEmail,
  productImage,
  products,
  type Address,
  type PaymentMethod,
  type Product,
} from "@/lib/shop-data";

type ShopRoute = "store" | "login" | "register" | "forgot" | "cart" | "delivery" | "payment" | "account" | "orders" | "order" | "admin";
type FieldErrors = Record<string, string>;

const blankAddress = (recipient = ""): Address => ({
  recipient,
  cep: "",
  street: "",
  number: "",
  complement: "",
  district: "",
  city: "",
  state: "",
  notes: "",
});

function validateAddress(address: Address) {
  const errors: FieldErrors = {};
  if (!address.recipient.trim()) errors.recipient = "Informe quem vai receber.";
  if (address.cep.replace(/\D/g, "").length !== 8) errors.cep = "Informe um CEP com 8 dígitos.";
  if (!address.street.trim()) errors.street = "Informe a rua ou avenida.";
  if (!address.number.trim()) errors.number = "Informe o número.";
  if (!address.district.trim()) errors.district = "Informe o bairro.";
  if (!address.city.trim()) errors.city = "Informe a cidade.";
  if (address.state.trim().length !== 2) errors.state = "Use a sigla do estado (2 letras).";
  return errors;
}

function Logo({ light = false }: { light?: boolean }) {
  return (
    <Link href="/" className={`brand ${light ? "brand-light" : ""}`} aria-label="Doce Miolo, página inicial">
      <span className="brand-mark"><CakeSlice size={20} strokeWidth={1.9} /></span>
      <span className="brand-name">doce <b>miolo</b><small>feito com carinho</small></span>
    </Link>
  );
}

function LoadingScreen() {
  return <main className="loading-screen"><span className="loading-mark"><CakeSlice size={24} /></span><span>Preparando uma fornada de carinho...</span></main>;
}

function Message({ children, kind = "error" }: { children: string; kind?: "error" | "success" }) {
  return <p className={`form-message ${kind}`} role={kind === "error" ? "alert" : "status"}>{children}</p>;
}

function FormField({
  label,
  name,
  error,
  children,
  hint,
}: {
  label: string;
  name: string;
  error?: string;
  children: React.ReactNode;
  hint?: string;
}) {
  return (
    <div className="field">
      <Label htmlFor={name}>{label}</Label>
      {children}
      {hint && <span className="field-hint">{hint}</span>}
      {error && <span className="field-error">{error}</span>}
    </div>
  );
}

export function ShopApp({ route, orderId = "" }: { route: ShopRoute; orderId?: string }) {
  const router = useRouter();
  const shop = useShop();
  const { ready, currentUser, cart, storageError } = shop;
  const [notice, setNotice] = useState("");
  const [noticeKind, setNoticeKind] = useState<"success" | "error">("success");
  const [busy, setBusy] = useState(false);

  const notify = (message: string, kind: "success" | "error" = "success") => {
    setNotice(message);
    setNoticeKind(kind);
    window.setTimeout(() => setNotice(""), 4000);
  };

  useEffect(() => {
    if (!ready) return;
    const isGuestPage = route === "login" || route === "register" || route === "forgot";
    if (!currentUser && !isGuestPage) router.replace("/login");
    if (currentUser && isGuestPage) router.replace(currentUser.role === "ADMIN" ? "/admin" : "/");
    if (currentUser?.role === "ADMIN" && route !== "admin" && !isGuestPage) router.replace("/admin");
    if (currentUser?.role === "CUSTOMER" && route === "admin") router.replace("/");
  }, [currentUser, ready, route, router]);

  if (!ready || ((!currentUser && !["login", "register", "forgot"].includes(route)) || (currentUser && ["login", "register", "forgot"].includes(route)))) {
    return <LoadingScreen />;
  }
  if (
    (currentUser?.role === "ADMIN" && route !== "admin") ||
    (currentUser?.role === "CUSTOMER" && route === "admin")
  ) {
    return <LoadingScreen />;
  }
  if (route === "admin") {
    if (currentUser?.role !== "ADMIN") return <LoadingScreen />;
    return <AdminDashboard userName={currentUser.name} onLogout={shop.logout} />;
  }

  if (route === "login" || route === "register" || route === "forgot") {
    return (
      <AuthPage
        route={route}
        busy={busy}
        setBusy={setBusy}
        onMessage={notify}
        storageError={storageError}
      />
    );
  }

  const count = cart.reduce((sum, item) => sum + item.quantity, 0);

  return (
    <div className="site-shell">
      <header className="site-header">
        <div className="header-main">
          <Logo />
          <nav className="desktop-nav" aria-label="Navegação principal">
            <Link href="/#catalogo">Cardápio</Link>
            <Link href="/#sobre">Nossa história</Link>
          </nav>
          <div className="header-actions">
            <Link href="/conta" className="account-link"><span className="account-avatar"><UserRound size={17} /></span><span className="account-label">Oi, {currentUser?.name.split(" ")[0]}</span></Link>
            <Link href="/carrinho" className="cart-link" aria-label={`Carrinho com ${count} itens`}>
              <ShoppingBag size={19} /><span className="cart-label">Sacola</span>
              <span className="cart-count">{count}</span>
            </Link>
          </div>
        </div>
        <div className="header-sub">
          <div className="header-sub-content"><MapPin size={14} /><span>{currentUser?.address ? `${currentUser.address.city}, ${currentUser.address.state}` : "Entrega fresquinha no seu endereço"}</span><Link href="/entrega">{currentUser?.address ? "Alterar" : "Adicionar endereço"} <ArrowRight size={13} /></Link></div>
          <span className="header-perk"><Sparkles size={13} /> Frete grátis a partir de R$ 120</span>
        </div>
      </header>

      {notice && <div className={`toast ${noticeKind}`} role={noticeKind === "error" ? "alert" : "status"}><CheckCircle2 size={18} />{notice}</div>}
      {storageError && <div className="storage-warning" role="alert">{storageError}</div>}

      <main className="page-content">
        {route === "store" && <Storefront notify={notify} />}
        {route === "cart" && <CartPage notify={notify} />}
        {route === "delivery" && <DeliveryPage notify={notify} />}
        {route === "payment" && <PaymentPage notify={notify} busy={busy} setBusy={setBusy} />}
        {route === "account" && <AccountPage notify={notify} />}
        {route === "orders" && <OrdersPage />}
        {route === "order" && <OrderPage orderId={orderId} />}
      </main>
      <Footer />
    </div>
  );
}

function AuthPage({
  route,
  busy,
  setBusy,
  onMessage,
  storageError,
}: {
  route: "login" | "register" | "forgot";
  busy: boolean;
  setBusy: (value: boolean) => void;
  onMessage: (message: string, kind?: "success" | "error") => void;
  storageError: string;
}) {
  const router = useRouter();
  const { login, register, resetPassword } = useShop();
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [fields, setFields] = useState({ name: "", email: "", phone: "", password: "", confirmPassword: "" });

  const submit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError("");
    setSuccess("");
    setBusy(true);
    try {
      if (!isValidEmail(fields.email)) throw new Error("Digite um e-mail válido.");
      if (route === "login") {
        if (!fields.password) throw new Error("Digite sua senha.");
        const user = await login(fields.email, fields.password);
        setSuccess("Acesso realizado. Que bom ter você de volta!");
        onMessage("Que bom ter você de volta!");
        await new Promise((resolve) => window.setTimeout(resolve, 700));
        router.push(user.role === "ADMIN" ? "/admin" : "/");
      } else if (route === "register") {
        if (fields.name.trim().length < 3) throw new Error("Informe seu nome completo.");
        if (fields.phone.replace(/\D/g, "").length < 10) throw new Error("Digite um telefone válido com DDD.");
        if (fields.password.length < 8) throw new Error("Sua senha precisa ter pelo menos 8 caracteres.");
        if (fields.password !== fields.confirmPassword) throw new Error("As senhas não coincidem.");
        await register(fields);
        setSuccess("Conta criada com sucesso! Vamos escolher algo bem gostoso.");
        onMessage("Conta criada! Vamos escolher algo bem gostoso.");
        await new Promise((resolve) => window.setTimeout(resolve, 700));
        router.push("/");
      } else {
        if (fields.password.length < 8) throw new Error("A nova senha precisa ter pelo menos 8 caracteres.");
        if (fields.password !== fields.confirmPassword) throw new Error("As senhas não coincidem.");
        await resetPassword(fields.email, fields.password);
        setSuccess("Senha atualizada. Entre com sua nova senha.");
        onMessage("Senha atualizada. Entre com sua nova senha.");
        await new Promise((resolve) => window.setTimeout(resolve, 700));
        router.push("/login");
      }
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "Não foi possível concluir. Tente novamente.");
    } finally {
      setBusy(false);
    }
  };

  const isRegister = route === "register";
  const isForgot = route === "forgot";
  return (
    <main className="auth-layout">
      <section className="auth-visual">
        <div className="auth-visual-photo" />
        <div className="auth-visual-overlay" />
        <div className="auth-visual-copy"><Logo light /><div><span className="eyebrow">UMA PAUSA MAIS DOCE</span><h1>Feito para<br /><em>derreter</em> seu dia.</h1><p>Pequenos momentos, grandes doçuras.<br />Sempre fresquinhos, sempre feitos com amor.</p></div><div className="auth-quote"><span>★★★★★</span><p>“O melhor cupcake que já provei. Dá vontade de pedir todos os dias!”</p><small>Mariana R. · cliente feliz</small></div></div>
        <span className="auth-photo-credit">Nossos bolinhos, feitos à mão todos os dias</span>
      </section>
      <section className="auth-panel">
        <div className="auth-panel-top"><Link href="/" className="auth-back">← Voltar para a loja</Link><span className="secure-note"><LockKeyhole size={13} /> Compra segura</span></div>
        <div className="auth-form-wrap">
          <span className="auth-mark"><CakeSlice size={22} /></span>
          <span className="eyebrow">{isRegister ? "SUA HISTÓRIA COMEÇA AQUI" : isForgot ? "VAMOS RESOLVER ISSO" : "QUE BOM TER VOCÊ AQUI"}</span>
          <h2>{isRegister ? "Crie sua conta" : isForgot ? "Nova senha, novo começo" : "Entre na sua conta"}</h2>
          <p className="auth-subtitle">{isRegister ? "Cadastre-se para pedir suas pequenas felicidades." : isForgot ? "Recupere o acesso à sua conta Doce Miolo." : "Acesse seus pedidos e continue escolhendo delícias."}</p>
          {isForgot && <div className="demo-note">Demonstração local: informe o e-mail cadastrado e defina uma nova senha.</div>}
          {!isRegister && !isForgot && <>
            <div className="demo-credentials-note"><b>Acesso de cliente demonstrativo</b><span>{DEMO_CUSTOMER_CREDENTIALS.email}</span><span>{DEMO_CUSTOMER_CREDENTIALS.password}</span><small>Entre para testar a loja, o carrinho e os pedidos.</small></div>
            <div className="demo-credentials-note"><b>Acesso administrativo demonstrativo</b><span>{DEMO_ADMIN_CREDENTIALS.email}</span><span>{DEMO_ADMIN_CREDENTIALS.password}</span><small>O dashboard usa dados de exemplo enquanto a API está em desenvolvimento.</small></div>
          </>}
          {storageError && <Message>{storageError}</Message>}
          <form className="auth-form" onSubmit={submit}>
            {isRegister && <FormField label="Nome completo" name="name"><Input id="name" autoComplete="name" placeholder="Como podemos te chamar?" value={fields.name} onChange={(event) => setFields({ ...fields, name: event.target.value })} required /></FormField>}
            <FormField label="E-mail" name="email"><Input id="email" type="email" autoComplete="email" placeholder="voce@email.com" value={fields.email} onChange={(event) => setFields({ ...fields, email: event.target.value })} required /></FormField>
            {isRegister && <FormField label="Telefone com DDD" name="phone"><Input id="phone" type="tel" autoComplete="tel" inputMode="tel" placeholder="(11) 99999-0000" value={fields.phone} onChange={(event) => setFields({ ...fields, phone: formatPhone(event.target.value) })} required /></FormField>}
            <FormField label={isForgot ? "Nova senha" : "Senha"} name="password">{passwordInput("password", isForgot ? "Crie uma senha com 8 caracteres" : "Sua senha", fields.password, (password) => setFields({ ...fields, password }), showPassword, setShowPassword, isRegister || isForgot ? "new-password" : "current-password")}</FormField>
            {(isRegister || isForgot) && <FormField label="Confirme sua senha" name="confirmPassword">{passwordInput("confirmPassword", "Digite a senha novamente", fields.confirmPassword, (confirmPassword) => setFields({ ...fields, confirmPassword }), showPassword, setShowPassword)}</FormField>}
            {!isRegister && !isForgot && <div className="forgot-row"><Link href="/recuperar-senha">Esqueci minha senha</Link></div>}
            {error && <Message>{error}</Message>}
            {success && <Message kind="success">{success}</Message>}
            <Button type="submit" className="button-primary auth-submit" disabled={busy}>{busy ? "Só um instante..." : isRegister ? "Criar minha conta" : isForgot ? "Salvar nova senha" : "Entrar na minha conta"}<ArrowRight size={17} /></Button>
          </form>
          <p className="auth-switch">{isRegister ? "Já tem uma conta?" : isForgot ? "Lembrou da senha?" : "Ainda não tem uma conta?"} <Link href={isRegister || isForgot ? "/login" : "/cadastro"}>{isRegister || isForgot ? "Entrar" : "Cadastre-se"}</Link></p>
          <div className="auth-trust"><span><Check size={14} /> Ingredientes de verdade</span><span><Heart size={14} /> Amor em cada receita</span></div>
        </div>
        <span className="auth-legal">Ao continuar, você concorda com nossos termos de uso e política de privacidade.</span>
      </section>
    </main>
  );
}

function passwordInput(
  id: string,
  placeholder: string,
  value: string,
  onChange: (value: string) => void,
  visible: boolean,
  setVisible: (value: boolean) => void,
  autoComplete: string = "new-password",
) {
  return (
    <div className="password-field">
      <Input id={id} type={visible ? "text" : "password"} autoComplete={autoComplete} placeholder={placeholder} value={value} onChange={(event) => onChange(event.target.value)} required minLength={id === "password" ? 1 : 8} />
      <button type="button" aria-label={visible ? "Ocultar senha" : "Mostrar senha"} onClick={() => setVisible(!visible)}>{visible ? <EyeOff size={17} /> : <Eye size={17} />}</button>
    </div>
  );
}

function Storefront({ notify }: { notify: (message: string, kind?: "success" | "error") => void }) {
  const { addToCart, currentUser } = useShop();
  const [search, setSearch] = useState("");
  const [category, setCategory] = useState("Todos");
  const [quantities, setQuantities] = useState<Record<string, number>>({});
  const searchRef = useRef<HTMLInputElement>(null);
  useEffect(() => {
    const focusSearch = (event: KeyboardEvent) => {
      if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === "k") {
        event.preventDefault();
        searchRef.current?.focus();
      }
    };
    window.addEventListener("keydown", focusSearch);
    return () => window.removeEventListener("keydown", focusSearch);
  }, []);
  const filtered = useMemo(() => products.filter((product) => (
    (category === "Todos" || product.category === category) &&
    `${product.name} ${product.description}`.toLowerCase().includes(search.toLowerCase().trim())
  )), [category, search]);

  return (
    <>
      <section className="hero">
        <div className="hero-copy">
          <span className="eyebrow"><Sparkles size={14} /> DOCE, FEITO NO NOSSO TEMPO</span>
          <h1>Pequenos momentos,<br /><em>grandes doçuras.</em></h1>
          <p>Cupcakes artesanais, preparados fresquinhos para transformar qualquer dia em uma ocasião especial.</p>
          <div className="hero-actions"><a className="button-primary" href="#catalogo">Escolha seu favorito <ArrowRight size={17} /></a><span className="hero-note"><span className="avatar-stack"><i>♡</i><i>♡</i><i>♡</i></span> Feito à mão, com carinho</span></div>
          <div className="hero-perks"><span><CheckCircle2 size={16} /> Assados todos os dias</span><span><Truck size={16} /> Entrega com cuidado</span></div>
        </div>
        <div className="hero-image" role="img" aria-label="Cupcakes artesanais com cobertura delicada"><div className="hero-sticker"><span>feito com</span><b>amor</b><Heart size={15} fill="currentColor" /></div><div className="hero-image-caption"><span>01 / 04</span><span>Uma fornada de felicidade</span><ArrowUpRight size={16} /></div></div>
        <div className="hero-side-note">CONFEITARIA ARTESANAL · DESDE 2024</div>
      </section>

      <section className="welcome-strip">
        <div className="welcome-icon"><CakeSlice size={19} /></div>
        <div><p>Um carinho especial pra você, {currentUser?.name.split(" ")[0]}!</p><span>Seu próximo momento feliz está a poucos cliques.</span></div>
        <a href="#catalogo">Ver cardápio <ArrowRight size={15} /></a>
      </section>

      <section id="catalogo" className="catalog-section">
        <div className="section-heading"><div><span className="eyebrow">ESCOLHIDOS A DEDO</span><h2>Um miolo de felicidade</h2><p>Pequenos no tamanho, gigantes no sabor.</p></div><span className="catalog-count"><CakeSlice size={16} /> {products.length} delícias</span></div>
        <div className="catalog-tools">
          <div className="search-box"><Search size={18} /><Input ref={searchRef} aria-label="Buscar cupcakes" placeholder="Qual sabor combina com hoje?" value={search} onChange={(event) => setSearch(event.target.value)} /><kbd>Ctrl K</kbd></div>
          <div className="category-list" aria-label="Filtrar por categoria">{categories.map((item) => <button key={item} className={`category-chip ${category === item ? "active" : ""}`} onClick={() => setCategory(item)} aria-pressed={category === item}>{item}</button>)}</div>
        </div>
        {filtered.length ? <div className="product-grid">{filtered.map((product, index) => (
          <ProductCard key={product.id} product={product} index={index} quantity={quantities[product.id] ?? 1} onQuantity={(quantity) => setQuantities({ ...quantities, [product.id]: quantity })} onAdd={() => {
            try {
              addToCart(product.id, quantities[product.id] ?? 1);
              notify(`${product.name} adicionado à sua sacola!`);
            } catch (error) {
              notify(error instanceof Error ? error.message : "Não foi possível adicionar o produto.", "error");
            }
          }} />
        ))}</div> : <div className="empty-search"><Search size={27} /><h3>Não encontramos esse sabor</h3><p>Tente outra palavra ou escolha uma categoria diferente.</p><Button variant="outline" onClick={() => { setSearch(""); setCategory("Todos"); }}>Ver todos os cupcakes</Button></div>}
      </section>

      <section className="feature-banner" id="sobre">
        <div className="feature-photo" role="img" aria-label="Cobertura artesanal sendo preparada" />
        <div className="feature-copy"><span className="eyebrow">CADA DETALHE IMPORTA</span><h2>Não é só um cupcake.<br /><em>É um abraço em forma de doce.</em></h2><p>Ingredientes selecionados, receitas da casa e um cuidado que dá pra sentir em cada pedacinho.</p><Link href="#catalogo" className="text-link">Conheça nossos sabores <ArrowRight size={16} /></Link></div>
      </section>
      <section className="testimonial"><span className="eyebrow">PALAVRAS DOCES</span><blockquote>“Tem gostinho de comemoração, mesmo numa terça-feira qualquer.”</blockquote><div className="testimonial-rating">★★★★★</div><span className="testimonial-author">Ana Clara · cliente Doce Miolo</span><div className="testimonial-dots"><i /><i className="selected" /><i /></div></section>
    </>
  );
}

function ProductCard({ product, index, quantity, onQuantity, onAdd }: {
  product: Product; index: number; quantity: number; onQuantity: (value: number) => void; onAdd: () => void;
}) {
  return (
    <Card className="product-card">
      <div className="product-image" style={{ backgroundImage: `url("${productImage(product.image)}")` }} role="img" aria-label={product.name}>
        {product.tag && <Badge className="product-tag">{product.tag}</Badge>}
        <span className="product-index">0{index + 1}</span>
        <button type="button" className="favorite-button" aria-label={`Adicionar ${product.name} aos favoritos`} onClick={(event) => { event.currentTarget.classList.toggle("is-favorite"); event.currentTarget.setAttribute("aria-pressed", String(event.currentTarget.classList.contains("is-favorite"))); }} aria-pressed="false"><Heart size={17} /></button>
      </div>
      <CardContent className="product-content">
        <span className="product-category">{product.category}</span>
        <h3>{product.name}</h3>
        <p>{product.description}</p>
        <div className="product-buy-row"><strong>{formatCurrency(product.price)}<small> / unidade</small></strong><div className="quantity-control" aria-label={`Quantidade de ${product.name}`}><button type="button" aria-label="Diminuir quantidade" onClick={() => onQuantity(Math.max(1, quantity - 1))}><Minus size={13} /></button><span>{quantity}</span><button type="button" aria-label="Aumentar quantidade" onClick={() => onQuantity(Math.min(20, quantity + 1))}><Plus size={13} /></button></div></div>
        <Button className="add-button" onClick={onAdd}>Adicionar à sacola <Plus size={16} /></Button>
      </CardContent>
    </Card>
  );
}

function PageHeading({ eyebrow, title, description }: { eyebrow: string; title: string; description: string }) {
  return <div className="page-heading"><span className="eyebrow">{eyebrow}</span><h1>{title}</h1><p>{description}</p></div>;
}

function EmptyCart() {
  return <div className="empty-state"><span className="empty-icon"><ShoppingBag size={27} /></span><h2>Sua sacola está esperando uma doçura</h2><p>Que tal escolher alguns cupcakes fresquinhos?</p><Link href="/#catalogo" className="button-primary">Explorar o cardápio <ArrowRight size={16} /></Link></div>;
}

function CartPage({ notify }: { notify: (message: string, kind?: "success" | "error") => void }) {
  const { cart, setCartQuantity, removeFromCart } = useShop();
  const router = useRouter();
  const lines = cart.map((item) => ({ ...item, product: products.find((product) => product.id === item.productId) })).filter((line) => line.product);
  const subtotal = lines.reduce((sum, item) => sum + item.product!.price * item.quantity, 0);
  const delivery = calculateDelivery(subtotal);
  if (!lines.length) return <div className="checkout-page"><PageHeading eyebrow="SUA SACOLA" title="Tudo começa com uma doçura" description="Seu espaço para reunir os pequenos grandes momentos." /><EmptyCart /></div>;
  return (
    <div className="checkout-page">
      <PageHeading eyebrow="SUA SACOLA" title="Uma sacola cheia de carinho" description={`${lines.reduce((sum, item) => sum + item.quantity, 0)} delícia(s) escolhida(s) para você.`} />
      <div className="checkout-layout">
        <div className="cart-list">
          {lines.map(({ product, quantity }) => <CartLine key={product!.id} product={product!} quantity={quantity} onChange={(amount) => setCartQuantity(product!.id, amount)} onRemove={() => { removeFromCart(product!.id); notify(`${product!.name} removido da sacola.`); }} />)}
          <Link href="/#catalogo" className="continue-link"><ArrowLeft size={16} /> Continuar escolhendo delícias</Link>
        </div>
        <OrderSummary subtotal={subtotal} delivery={delivery} total={subtotal + delivery} action={<Button className="button-primary full-button" onClick={() => router.push("/entrega")}>Continuar para entrega <ArrowRight size={17} /></Button>} />
      </div>
    </div>
  );
}

function CartLine({ product, quantity, onChange, onRemove }: { product: Product; quantity: number; onChange: (value: number) => void; onRemove: () => void }) {
  return <Card className="cart-line"><div className="cart-product-photo" style={{ backgroundImage: `url("${productImage(product.image)}")` }} role="img" aria-label={product.name} /><div className="cart-product-details"><span className="product-category">{product.category}</span><h3>{product.name}</h3><p>{formatCurrency(product.price)} <span>cada</span></p><div className="quantity-control"><button type="button" aria-label={`Diminuir quantidade de ${product.name}`} onClick={() => onChange(Math.max(0, quantity - 1))}><Minus size={13} /></button><span>{quantity}</span><button type="button" aria-label={`Aumentar quantidade de ${product.name}`} onClick={() => onChange(quantity + 1)}><Plus size={13} /></button></div></div><div className="cart-line-end"><strong>{formatCurrency(product.price * quantity)}</strong><button type="button" className="remove-button" onClick={onRemove} aria-label={`Remover ${product.name}`}><Trash2 size={16} /> Remover</button></div></Card>;
}

function OrderSummary({ subtotal, delivery, total, action, compact = false }: { subtotal: number; delivery: number; total: number; action?: React.ReactNode; compact?: boolean }) {
  return <Card className={`summary-card ${compact ? "summary-compact" : ""}`}><CardContent><span className="summary-eyebrow">RESUMO DO PEDIDO</span><div className="summary-row"><span>Subtotal dos produtos</span><strong>{formatCurrency(subtotal)}</strong></div><div className="summary-row"><span>Entrega <small>{subtotal >= 120 ? "· cortesia" : ""}</small></span><strong>{delivery === 0 ? "Grátis" : formatCurrency(delivery)}</strong></div>{subtotal > 0 && subtotal < 120 && <div className="delivery-progress"><span>Faltam {formatCurrency(120 - subtotal)} para ganhar entrega grátis</span><div><i style={{ width: `${Math.min(100, subtotal / 120 * 100)}%` }} /></div></div>}<Separator /><div className="summary-total"><span>Total</span><strong>{formatCurrency(total)}</strong></div>{action}</CardContent></Card>;
}

function AddressForm({ value, onChange, errors }: { value: Address; onChange: (value: Address) => void; errors: FieldErrors }) {
  const textField = (label: string, name: keyof Address, placeholder: string, options: { required?: boolean; maxLength?: number; className?: string } = {}) => (
    <FormField label={label} name={`address-${name}`} error={errors[name]}>
      <Input id={`address-${name}`} className={options.className} placeholder={placeholder} value={value[name] ?? ""} maxLength={options.maxLength} required={options.required} onChange={(event) => {
        const next = name === "cep" ? formatCep(event.target.value) : name === "state" ? event.target.value.replace(/[^a-z]/gi, "").slice(0, 2).toUpperCase() : event.target.value;
        onChange({ ...value, [name]: next });
      }} />
    </FormField>
  );
  return <div className="address-form">
    {textField("Nome de quem recebe", "recipient", "Nome completo", { required: true })}
    {textField("CEP", "cep", "00000-000", { required: true, maxLength: 9 })}
    <div className="form-grid"><div className="wide-field">{textField("Rua ou avenida", "street", "Nome da rua", { required: true })}</div>{textField("Número", "number", "Nº", { required: true })}{textField("Complemento", "complement", "Apto, bloco, casa...")}</div>
    <div className="form-grid">{textField("Bairro", "district", "Seu bairro", { required: true })}{textField("Cidade", "city", "Sua cidade", { required: true })}{textField("Estado", "state", "SP", { required: true, maxLength: 2 })}</div>
    <FormField label="Observações para a entrega" name="address-notes"><Textarea id="address-notes" placeholder="Referência, portaria ou um recadinho para quem vai entregar..." value={value.notes ?? ""} onChange={(event) => onChange({ ...value, notes: event.target.value })} /></FormField>
  </div>;
}

function DeliveryPage({ notify }: { notify: (message: string, kind?: "success" | "error") => void }) {
  const router = useRouter();
  const { currentUser, cart, saveAddress } = useShop();
  const [address, setAddress] = useState<Address>(() => currentUser?.address ? { ...currentUser.address } : blankAddress(currentUser?.name));
  const [errors, setErrors] = useState<FieldErrors>({});
  const [error, setError] = useState("");
  const [saved, setSaved] = useState(Boolean(currentUser?.address));
  const itemCount = cart.reduce((sum, item) => sum + item.quantity, 0);
  useEffect(() => { if (!itemCount) router.replace("/carrinho"); }, [itemCount, router]);
  if (!itemCount) return <LoadingScreen />;

  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const validation = validateAddress(address);
    setErrors(validation);
    setError("");
    if (Object.keys(validation).length) return;
    try {
      saveAddress({ ...address, state: address.state.toUpperCase() });
      setSaved(true);
      notify("Endereço de entrega salvo.");
      router.push("/pagamento");
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "Não foi possível salvar o endereço.");
    }
  };

  return <div className="checkout-page"><CheckoutSteps active={1} /><PageHeading eyebrow="ENTREGA COM CARINHO" title="Onde entregamos sua doçura?" description="Confirme o endereço para que sua fornada chegue fresquinha." />
    <form className="checkout-layout" onSubmit={submit} noValidate>
      <Card className="checkout-card"><CardContent><div className="card-heading"><span className="card-icon"><MapPin size={19} /></span><div><h2>Endereço de entrega</h2><p>Confira cada detalhe antes de continuar.</p></div></div><AddressForm value={address} onChange={(next) => { setAddress(next); setSaved(false); }} errors={errors} />{error && <Message>{error}</Message>}<div className="form-footnote"><LockKeyhole size={14} /> Seu endereço é usado apenas para esta entrega.</div></CardContent></Card>
      <div className="delivery-side"><OrderSummary subtotal={cart.reduce((sum, item) => sum + (products.find((product) => product.id === item.productId)?.price ?? 0) * item.quantity, 0)} delivery={calculateDelivery(cart.reduce((sum, item) => sum + (products.find((product) => product.id === item.productId)?.price ?? 0) * item.quantity, 0))} total={cart.reduce((sum, item) => sum + (products.find((product) => product.id === item.productId)?.price ?? 0) * item.quantity, 0) + calculateDelivery(cart.reduce((sum, item) => sum + (products.find((product) => product.id === item.productId)?.price ?? 0) * item.quantity, 0))} action={<><Button type="submit" className="button-primary full-button">{saved ? "Confirmar endereço" : "Salvar e continuar"} <ArrowRight size={17} /></Button><Link href="/carrinho" className="back-to-cart"><ArrowLeft size={14} /> Voltar à sacola</Link></>} /><div className="delivery-promise"><Clock3 size={17} /><span><b>Feito hoje, entregue com cuidado.</b><small>Preparamos cada pedido na hora.</small></span></div></div>
    </form>
  </div>;
}

function CheckoutSteps({ active }: { active: number }) {
  const steps = ["Sacola", "Entrega", "Pagamento"];
  return <div className="checkout-steps">{steps.map((step, index) => <div key={step} className={`checkout-step ${active === index ? "current" : ""} ${active > index ? "completed" : ""}`}><span>{active > index ? <Check size={13} /> : `0${index + 1}`}</span>{step}{index < steps.length - 1 && <i />}</div>)}</div>;
}

function PaymentPage({ notify, busy, setBusy }: { notify: (message: string, kind?: "success" | "error") => void; busy: boolean; setBusy: (value: boolean) => void }) {
  const router = useRouter();
  const { cart, currentUser, createOrder } = useShop();
  const [payment, setPayment] = useState<PaymentMethod>("Dinheiro");
  const [needsChange, setNeedsChange] = useState(false);
  const [changeFor, setChangeFor] = useState("");
  const [error, setError] = useState("");
  const items = cart.map((item) => ({ ...item, product: products.find((product) => product.id === item.productId) })).filter((item) => item.product);
  const subtotal = items.reduce((sum, item) => sum + item.product!.price * item.quantity, 0);
  const delivery = calculateDelivery(subtotal);
  const total = subtotal + delivery;
  useEffect(() => {
    if (!busy && !items.length) router.replace("/carrinho");
    else if (!busy && !currentUser?.address) router.replace("/entrega");
  }, [busy, currentUser?.address, items.length, router]);

  if (!items.length || !currentUser?.address) return <LoadingScreen />;
  const methods: { value: PaymentMethod; label: string; description: string; icon: typeof Wallet }[] = [
    { value: "Dinheiro", label: "Dinheiro", description: "Pague em espécie ao receber", icon: Wallet },
    { value: "Cartão de débito", label: "Cartão de débito", description: "Leve sua maquininha até a porta", icon: CreditCard },
    { value: "Cartão de crédito", label: "Cartão de crédito", description: "Parcele em até 2x na entrega", icon: CreditCard },
  ];

  const submit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError("");
    const changeValue = needsChange && payment === "Dinheiro" ? Number(changeFor.replace(",", ".")) : null;
    if (needsChange && payment === "Dinheiro" && (!changeValue || changeValue < total)) {
      setError(`Informe um valor de troco igual ou maior que ${formatCurrency(total)}.`);
      return;
    }
    setBusy(true);
    try {
      const order = createOrder(payment, changeValue);
      notify("Pedido confirmado com sucesso!");
      router.push(`/pedido/${encodeURIComponent(order.id)}`);
    } catch (caught) {
      setError(caught instanceof Error ? caught.message : "Não foi possível registrar seu pedido.");
      setBusy(false);
    }
  };

  return <div className="checkout-page"><CheckoutSteps active={2} /><PageHeading eyebrow="QUASE NA HORA" title="Como você prefere pagar?" description="O pagamento acontece na entrega. Nenhum dado de cartão é solicitado." />
    <form className="checkout-layout" onSubmit={submit}>
      <div className="payment-main">
        <Card className="checkout-card payment-card"><CardContent><div className="card-heading"><span className="card-icon"><Wallet size={19} /></span><div><h2>Pagamento na entrega</h2><p>Escolha como quer pagar quando seu pedido chegar.</p></div></div>
          <RadioGroup value={payment} onValueChange={(value) => setPayment(value as PaymentMethod)} className="payment-options">{methods.map(({ value, label, description, icon: Icon }) => <Label key={value} htmlFor={`payment-${value}`} className={`payment-option ${payment === value ? "selected" : ""}`}><RadioGroupItem id={`payment-${value}`} value={value} /><span className="payment-option-icon"><Icon size={19} /></span><span className="payment-option-copy"><b>{label}</b><small>{description}</small></span>{payment === value && <CheckCircle2 className="payment-check" size={18} />}</Label>)}</RadioGroup>
          {payment === "Dinheiro" && <div className="change-panel"><span className="change-question">Vai precisar de troco?</span><div className="change-choice"><button type="button" className={!needsChange ? "selected" : ""} onClick={() => setNeedsChange(false)}>Não, tenho o valor certinho</button><button type="button" className={needsChange ? "selected" : ""} onClick={() => setNeedsChange(true)}>Sim, preciso de troco</button></div>          {needsChange && <FormField label="Troco para quanto?" name="changeFor" hint={`Total do pedido: ${formatCurrency(total)}`}><Input id="changeFor" type="number" step="0.01" placeholder="Ex.: 100,00" value={changeFor} onChange={(event) => setChangeFor(event.target.value)} required aria-invalid={Boolean(error)} /></FormField>}</div>}
          <div className="payment-no-card"><LockKeyhole size={15} /> Não pedimos dados do seu cartão. Pagamento feito somente na entrega.</div>
        </CardContent></Card>
        <Card className="checkout-card address-review"><CardContent><div className="card-heading"><span className="card-icon"><MapPin size={19} /></span><div><h2>Endereço de entrega</h2><p>{currentUser.address.street}, {currentUser.address.number}{currentUser.address.complement ? ` · ${currentUser.address.complement}` : ""}<br />{currentUser.address.district} · {currentUser.address.city}/{currentUser.address.state} · CEP {currentUser.address.cep}</p></div><Link href="/entrega" className="edit-link">Alterar</Link></div></CardContent></Card>
      </div>
      <OrderSummary compact subtotal={subtotal} delivery={delivery} total={total} action={<><Button type="submit" className="button-primary full-button" disabled={busy}>{busy ? "Confirmando..." : "Confirmar meu pedido"} <ArrowRight size={17} /></Button>{error && <Message>{error}</Message>}<Link href="/carrinho" className="back-to-cart"><ArrowLeft size={14} /> Voltar à sacola</Link><p className="order-terms"><LockKeyhole size={12} /> Seus dados são salvos somente neste dispositivo.</p></>} />
    </form>
  </div>;
}

function AccountPage({ notify }: { notify: (message: string, kind?: "success" | "error") => void }) {
  const router = useRouter();
  const { currentUser, saveProfile, logout, orders } = useShop();
  const [name, setName] = useState(currentUser?.name ?? "");
  const [phone, setPhone] = useState(currentUser?.phone ?? "");
  const [error, setError] = useState("");
  const [editing, setEditing] = useState(false);
  if (!currentUser) return <LoadingScreen />;
  const save = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setError("");
    if (name.trim().length < 3) { setError("Informe seu nome completo."); return; }
    if (phone.replace(/\D/g, "").length < 10) { setError("Digite um telefone válido com DDD."); return; }
    try { saveProfile(name, phone); setEditing(false); notify("Seus dados foram atualizados."); }
    catch (caught) { setError(caught instanceof Error ? caught.message : "Não foi possível salvar seus dados."); }
  };
  const signOut = () => { logout(); router.push("/login"); };
  return <div className="checkout-page account-page"><PageHeading eyebrow="SEU CANTINHO" title={`Olá, ${currentUser.name.split(" ")[0]}!`} description="Aqui você cuida da sua conta e acompanha seus momentos doces." />
    <div className="account-grid"><Card className="checkout-card"><CardContent><div className="card-heading"><span className="card-icon"><UserRound size={19} /></span><div><h2>Seus dados</h2><p>As informações da sua conta.</p></div><Button variant="outline" size="sm" onClick={() => setEditing(!editing)}>{editing ? "Cancelar" : "Editar"}</Button></div>{editing ? <form className="profile-form" onSubmit={save}><FormField label="Nome completo" name="profile-name"><Input id="profile-name" value={name} onChange={(event) => setName(event.target.value)} required /></FormField><FormField label="E-mail" name="profile-email"><Input id="profile-email" type="email" value={currentUser.email} disabled /></FormField><FormField label="Telefone" name="profile-phone"><Input id="profile-phone" value={phone} onChange={(event) => setPhone(formatPhone(event.target.value))} required /></FormField>{error && <Message>{error}</Message>}<Button className="button-primary">Salvar alterações <Check size={16} /></Button></form> : <div className="profile-data"><div><span>Nome</span><b>{currentUser.name}</b></div><div><span>E-mail</span><b>{currentUser.email}</b></div><div><span>Telefone</span><b>{currentUser.phone}</b></div></div>}</CardContent></Card>
      <Card className="checkout-card"><CardContent><div className="card-heading"><span className="card-icon"><MapPin size={19} /></span><div><h2>Endereço salvo</h2><p>Para sua próxima fornada chegar até você.</p></div></div>{currentUser.address ? <div className="saved-address"><b>{currentUser.address.recipient}</b><span>{currentUser.address.street}, {currentUser.address.number}{currentUser.address.complement && ` · ${currentUser.address.complement}`}</span><span>{currentUser.address.district} · {currentUser.address.city}/{currentUser.address.state}</span><span>CEP {currentUser.address.cep}</span></div> : <p className="no-address">Você ainda não cadastrou um endereço.</p>}<Link href="/entrega" className="text-link">{currentUser.address ? "Editar endereço" : "Adicionar endereço"} <ArrowRight size={15} /></Link></CardContent></Card>
      <Card className="checkout-card account-orders"><CardContent><div className="card-heading"><span className="card-icon"><PackageCheck size={19} /></span><div><h2>Seus pedidos</h2><p>{orders.length ? `${orders.length} pedido(s) feito(s) com carinho.` : "Seus momentos doces aparecerão aqui."}</p></div><Link href="/pedidos" className="edit-link">Ver todos</Link></div>{orders.slice(0, 2).map((order) => <Link href={`/pedido/${encodeURIComponent(order.id)}`} className="mini-order" key={order.id}><span><b>{order.id}</b><small>{new Date(order.createdAt).toLocaleDateString("pt-BR")}</small></span><span>{formatCurrency(order.total)} <ArrowRight size={14} /></span></Link>)}</CardContent></Card></div>
    <button className="logout-button" onClick={signOut}><LogOut size={16} /> Sair da minha conta</button>
  </div>;
}

function OrdersPage() {
  const { orders } = useShop();
  return <div className="checkout-page"><PageHeading eyebrow="SEUS MOMENTOS DOCES" title="Meus pedidos" description="Uma coleção dos momentos especiais que você escolheu." />{orders.length ? <div className="orders-list">{orders.map((order) => <Link href={`/pedido/${encodeURIComponent(order.id)}`} className="order-history-card" key={order.id}><span className="history-icon"><PackageCheck size={20} /></span><span className="history-main"><b>{order.id}</b><small>{new Date(order.createdAt).toLocaleString("pt-BR", { dateStyle: "medium", timeStyle: "short" })} · {order.items.reduce((sum, item) => sum + item.quantity, 0)} item(ns)</small></span><span className="history-total">{formatCurrency(order.total)}<small>Pedido confirmado</small></span><ArrowRight size={17} /></Link>)}</div> : <div className="empty-state"><span className="empty-icon"><PackageCheck size={27} /></span><h2>Ainda não há pedidos por aqui</h2><p>Seu próximo momento doce começa com um cupcake.</p><Link href="/#catalogo" className="button-primary">Ver cardápio <ArrowRight size={16} /></Link></div>}</div>;
}

function OrderPage({ orderId }: { orderId: string }) {
  const { getOrder } = useShop();
  const order = getOrder(decodeURIComponent(orderId));
  if (!order) return <div className="checkout-page"><PageHeading eyebrow="PEDIDO NÃO ENCONTRADO" title="Não achamos esse pedido" description="Ele pode pertencer a outra conta ou não existir neste dispositivo." /><div className="empty-state"><Link href="/pedidos" className="button-primary">Ver meus pedidos <ArrowRight size={16} /></Link></div></div>;
  return <div className="confirmation-page">
    <div className="success-art"><span className="success-ring"><Check size={35} /></span><i className="success-spark one">✦</i><i className="success-spark two">✧</i><i className="success-spark three">✦</i></div>
    <span className="eyebrow">UMA ESCOLHA DELICIOSA</span><h1>Seu pedido está<br /><em>confirmado!</em></h1><p className="confirmation-intro">Obrigado por escolher a Doce Miolo.<br />Prepare-se para receber uma dose de doçura!</p>
    <div className="order-number"><span>NÚMERO DO PEDIDO</span><b>{order.id}</b><small>Feito em {new Date(order.createdAt).toLocaleString("pt-BR", { dateStyle: "long", timeStyle: "short" })}</small></div>
    <div className="confirmation-grid"><Card className="confirmation-card"><CardContent><div className="confirmation-card-heading"><PackageCheck size={18} /><h2>Seu pedido</h2><Badge>Confirmado</Badge></div>{order.items.map((item) => <div className="confirmation-item" key={item.productId}><span>{item.quantity}× {item.name}</span><b>{formatCurrency(item.price * item.quantity)}</b></div>)}<Separator /><div className="summary-row"><span>Produtos</span><b>{formatCurrency(order.subtotal)}</b></div><div className="summary-row"><span>Entrega</span><b>{order.delivery ? formatCurrency(order.delivery) : "Grátis"}</b></div><div className="summary-total"><span>Total</span><strong>{formatCurrency(order.total)}</strong></div></CardContent></Card>
      <Card className="confirmation-card"><CardContent><div className="confirmation-card-heading"><MapPin size={18} /><h2>Vai chegar aqui</h2></div><div className="saved-address"><b>{order.address.recipient}</b><span>{order.address.street}, {order.address.number}{order.address.complement && ` · ${order.address.complement}`}</span><span>{order.address.district} · {order.address.city}/{order.address.state}</span><span>CEP {order.address.cep}</span>{order.address.notes && <span>Obs.: {order.address.notes}</span>}</div><Separator /><div className="summary-row payment-summary"><span>Forma de pagamento</span><b>{order.payment}</b></div>{order.changeFor && <div className="summary-row"><span>Troco para</span><b>{formatCurrency(order.changeFor)}</b></div>}<div className="order-status"><span><i /> Pedido confirmado</span><small>Vamos começar a preparar com carinho.</small></div></CardContent></Card></div>
    <div className="confirmation-actions"><Link href="/" className="button-primary">Voltar para a loja <ArrowRight size={17} /></Link><Link href="/pedidos" className="secondary-button">Ver meus pedidos</Link></div>
  </div>;
}

function Footer() {
  return <footer className="site-footer"><div className="footer-main"><div className="footer-brand"><Logo light /><p>Pequenos momentos,<br />grandes doçuras.</p></div><div className="footer-col"><b>Explore</b><Link href="/#catalogo">Nosso cardápio</Link><Link href="/#sobre">Nossa história</Link><Link href="/pedidos">Meus pedidos</Link></div><div className="footer-col"><b>Fale com a gente</b><a href="mailto:oi@docemiolo.com.br">oi@docemiolo.com.br</a><a href="tel:+5511999990000">(11) 99999-0000</a><span>Seg a sáb, das 9h às 18h</span></div><div className="footer-note"><span>FEITO COM <Heart size={12} fill="currentColor" /> EM CADA DETALHE</span><p>Uma receita de carinho,<br />assada todos os dias.</p></div></div><div className="footer-bottom"><span>© 2026 Doce Miolo · Feito com carinho no Brasil</span><span>Confeitaria artesanal · Osasco, SP</span></div></footer>;
}
