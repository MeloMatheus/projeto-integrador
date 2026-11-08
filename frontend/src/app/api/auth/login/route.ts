import { forwardBackendRequest } from "@/app/api/_lib/forward-backend";

export async function POST(request: Request) {
  return forwardBackendRequest(request, "/api/auth/login", false);
}
