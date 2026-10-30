import { Suspense } from "react";
import { ShopApp } from "@/components/shop/shop-app";

async function OrderDetails({ params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;
  return <ShopApp route="order" orderId={id} />;
}

export default function OrderDetailsPage({ params }: { params: Promise<{ id: string }> }) {
  return (
    <Suspense fallback={<main className="loading-screen">Abrindo os detalhes do seu pedido...</main>}>
      <OrderDetails params={params} />
    </Suspense>
  );
}
