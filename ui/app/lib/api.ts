import type {
  LoginRequest,
  LoginResponse,
  UserResponse,
  UserRequest,
  NodeResponse,
  NodeConfigRequest,
  NodePatchRequest,
  MediaResponse,
} from "./types";
import {
  mockLogin,
  mockListUsers,
  mockCreateUser,
  mockUpdateUser,
  mockDeleteUser,
  mockListNode,
  mockCreateNode,
  mockPatchNode,
  mockDeleteNode,
  mockListMedia,
  mockUploadMedia,
  mockDeleteMedia,
} from "./mock";

const USE_MOCK = true;

export const API_BASE = "http://localhost:8080";

function authHeaders(): Record<string, string> {
  const token = localStorage.getItem("token");
  return token ? { Authorization: `Bearer ${token}` } : {};
}

async function api<T>(path: string, options?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...authHeaders(),
      ...(options?.headers as Record<string, string>),
    },
  });
  if (res.status === 401) {
    localStorage.removeItem("token");
    if (typeof window !== "undefined") window.location.href = "/login";
    throw new Error("Unauthorized");
  }
  if (!res.ok) {
    const body = await res.json().catch(() => ({ message: res.statusText }));
    throw new Error(body.message ?? res.statusText);
  }
  if (res.status === 204) return undefined as T;
  return res.json();
}

// Auth
export function login(request: LoginRequest): Promise<LoginResponse> {
  if (USE_MOCK) return mockLogin(request);
  return api("/api/login", { method: "POST", body: JSON.stringify(request) });
}

// Users
export function listUsers(): Promise<UserResponse[]> {
  if (USE_MOCK) return mockListUsers();
  return api("/api/users");
}

export function createUser(request: UserRequest): Promise<UserResponse> {
  if (USE_MOCK) return mockCreateUser(request);
  return api("/api/users", { method: "POST", body: JSON.stringify(request) });
}

export function updateUser(id: number, request: UserRequest): Promise<UserResponse> {
  if (USE_MOCK) return mockUpdateUser(id, request);
  return api(`/api/users/${id}`, { method: "PUT", body: JSON.stringify(request) });
}

export function deleteUser(id: number): Promise<void> {
  if (USE_MOCK) return mockDeleteUser(id);
  return api(`/api/users/${id}`, { method: "DELETE" });
}

// Nodes
export function listNodes(): Promise<NodeResponse[]> {
  if (USE_MOCK) return mockListNode();
  return api("/nodes");
}

export function createNode(request: NodeConfigRequest): Promise<NodeResponse> {
  if (USE_MOCK) return mockCreateNode(request);
  return api("/nodes", { method: "POST", body: JSON.stringify(request) });
}

export function patchNode(id: number, request: NodePatchRequest): Promise<NodeResponse> {
  if (USE_MOCK) return mockPatchNode(id, request);
  return api(`/nodes/${id}`, { method: "PATCH", body: JSON.stringify(request) });
}

export function deleteNode(id: number): Promise<void> {
  if (USE_MOCK) return mockDeleteNode(id);
  return api(`/nodes/${id}`, { method: "DELETE" });
}

// Media
export function listMedia(): Promise<MediaResponse[]> {
  if (USE_MOCK) return mockListMedia();
  return api("/api/files/");
}

export function uploadMedia(file: File): Promise<MediaResponse> {
  if (USE_MOCK) return mockUploadMedia(file);
  const formData = new FormData();
  formData.append("file", file);
  return api("/api/files/upload", { method: "POST", body: formData });
}

export function deleteMedia(id: string): Promise<void> {
  if (USE_MOCK) return mockDeleteMedia(id);
  return api(`/api/files/${id}`, { method: "DELETE" });
}
