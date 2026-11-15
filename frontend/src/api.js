const BASE_URL = "/api";

async function handleResponse(response) {
  const contentType = response.headers.get("content-type") || "";
  const body = contentType.includes("application/json")
    ? await response.json()
    : null;

  if (!response.ok) {
    const message = body?.error || `Request failed (${response.status})`;
    throw new Error(message);
  }

  return body;
}

export function getProfile(name) {
  return fetch(`${BASE_URL}/profile/${encodeURIComponent(name)}`).then(
    handleResponse
  );
}

export function saveProfile(profile) {
  return fetch(`${BASE_URL}/profile`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(profile),
  }).then(handleResponse);
}

export function sendChatMessage(name, message) {
  return fetch(`${BASE_URL}/chat`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ name, message }),
  }).then(handleResponse);
}
