import { forwardBackendRequest } from "@/app/api/_lib/forward-backend";

export async function PUT(
  request: Request,
  { params }: { params: Promise<{ id: string }> },
) {
  const { id } = await params;
  return forwardBackendRequest(request, `/api/pedidos/admin/${encodeURIComponent(id)}/status`);
}
