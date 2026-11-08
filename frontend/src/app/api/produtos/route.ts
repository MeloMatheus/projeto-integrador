import { forwardBackendRequest } from "@/app/api/_lib/forward-backend";

export async function GET(request: Request) {
  return forwardBackendRequest(request, "/api/produtos", false);
}
