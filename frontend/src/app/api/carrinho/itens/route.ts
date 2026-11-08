import { forwardBackendRequest } from "@/app/api/_lib/forward-backend";

export async function PUT(request: Request) {
  return forwardBackendRequest(request, "/api/carrinho/itens");
}
