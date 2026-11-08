import { forwardBackendRequest } from "@/app/api/_lib/forward-backend";

export async function GET(
  request: Request,
  { params }: { params: Promise<{ idUsuario: string }> },
) {
  const { idUsuario } = await params;
  return forwardBackendRequest(request, `/api/carrinho/${encodeURIComponent(idUsuario)}`);
}
