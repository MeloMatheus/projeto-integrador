import { forwardBackendRequest } from "@/app/api/_lib/forward-backend";

export async function GET(request: Request) {
  const { search } = new URL(request.url);
  return forwardBackendRequest(request, `/api/admin/dashboard${search}`);
}
