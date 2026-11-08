import { forwardBackendRequest } from "@/app/api/_lib/forward-backend";

export async function DELETE(
  request: Request,
  { params }: { params: Promise<{ produtoId: string }> },
) {
  const { produtoId } = await params;
  return forwardBackendRequest(
    request,
    `/api/carrinho/itens/${encodeURIComponent(produtoId)}`,
  );
}
