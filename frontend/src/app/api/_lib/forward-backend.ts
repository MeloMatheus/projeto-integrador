// const BACKEND_URL = "https://projeto-integrador-jqoo.onrender.com";
const BACKEND_URL = "http://localhost:8081";

export async function forwardBackendRequest(
  request: Request,
  path: string,
  requiresAuthorization = true,
) {
  const authorization = request.headers.get("authorization");
  if (requiresAuthorization && !authorization) {
    return Response.json({ message: "Authorization header is required." }, { status: 401 });
  }

  const headers = new Headers();
  if (authorization) headers.set("Authorization", authorization);
  const contentType = request.headers.get("content-type");
  if (contentType) headers.set("Content-Type", contentType);
  const method = request.method.toUpperCase();
  const body = method === "GET" || method === "HEAD" ? undefined : await request.text();

  const backendResponse = await fetch(`${BACKEND_URL}${path}`, {
    method,
    headers,
    body,
    cache: "no-store",
  });

  const responseHeaders = new Headers();
  const responseContentType = backendResponse.headers.get("content-type");
  if (responseContentType) responseHeaders.set("content-type", responseContentType);

  return new Response(backendResponse.body, {
    status: backendResponse.status,
    headers: responseHeaders,
  });
}
