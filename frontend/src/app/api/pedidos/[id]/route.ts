import { forwardBackendRequest } from "@/app/api/_lib/forward-backend";

export async function GET(
  request: Request,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params;
  return forwardBackendRequest(request, `/api/pedidos/${encodeURIComponent(id)}`);
}
