import { forwardBackendRequest } from "@/app/api/_lib/forward-backend";

export async function DELETE(
  request: Request,
  { params }: { params: Promise<{ idUsuario: string; produtoId: string }> },
) {
  const { idUsuario, produtoId } = await params;
  return forwardBackendRequest(
    request,
    `/api/carrinho/${encodeURIComponent(idUsuario)}/itens/${encodeURIComponent(produtoId)}`,
  );
}
