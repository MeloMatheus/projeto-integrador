import type { Metadata } from "next";
import { ShopProvider } from "@/lib/shop-store";
import "./globals.css";

export const metadata: Metadata = {
  title: "Doce Miolo | Pequenos momentos, grandes doçuras",
  description: "Cupcakes artesanais, assados fresquinhos e feitos com carinho para você.",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html lang="pt-BR" className="h-full antialiased" data-scroll-behavior="smooth">
      <body className="min-h-full flex flex-col">
        <ShopProvider>{children}</ShopProvider>
      </body>
    </html>
  );
}
