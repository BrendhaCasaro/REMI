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

let nextUserId = 4;
let nextNodeId = 4;

const mockUsers: UserResponse[] = [
  { id: 1, username: "admin", role: "ADMIN" },
  { id: 2, username: "joao", role: "USER" },
  { id: 3, username: "maria", role: "USER" },
];

const mockNodes: NodeResponse[] = [
  { id: 1, url: "http://node1.local:8081", totalCapacity: 500, status: "ONLINE" },
  { id: 2, url: "http://node2.local:8081", totalCapacity: 1000, status: "ONLINE" },
  { id: 3, url: "http://node3.local:8081", totalCapacity: 250, status: "OFFLINE" },
];

const mockMedia: MediaResponse[] = [
  { id: "a1b2c3d4-0001-4000-8000-000000000001", name: "relatorio.pdf", createdAt: "2026-05-20T10:30:00Z" },
  { id: "a1b2c3d4-0002-4000-8000-000000000002", name: "banner-site.png", createdAt: "2026-05-22T14:15:00Z" },
  { id: "a1b2c3d4-0003-4000-8000-000000000003", name: "apresentacao.mp4", createdAt: "2026-05-25T08:00:00Z" },
];

function delay<T>(data: T, ms = 400): Promise<T> {
  return new Promise((resolve) => setTimeout(() => resolve(data), ms));
}

// Auth
export function mockLogin(_request: LoginRequest): Promise<LoginResponse> {
  return delay({ token: "mock-jwt-token-abc123" });
}

// Users
export function mockListUsers(): Promise<UserResponse[]> {
  return delay([...mockUsers]);
}

export function mockCreateUser(request: UserRequest): Promise<UserResponse> {
  const user: UserResponse = {
    id: nextUserId++,
    username: request.username,
    role: request.role,
  };
  mockUsers.push(user);
  return delay(user);
}

export function mockUpdateUser(id: number, request: UserRequest): Promise<UserResponse> {
  const user: UserResponse = { id, username: request.username, role: request.role };
  const idx = mockUsers.findIndex((u) => u.id === id);
  if (idx !== -1) mockUsers[idx] = user;
  return delay(user);
}

export function mockDeleteUser(id: number): Promise<void> {
  const idx = mockUsers.findIndex((u) => u.id === id);
  if (idx !== -1) mockUsers.splice(idx, 1);
  return delay(undefined);
}

// Nodes
export function mockListNode(): Promise<NodeResponse[]> {
  return delay([...mockNodes]);
}

export function mockCreateNode(request: NodeConfigRequest): Promise<NodeResponse> {
  const node: NodeResponse = {
    id: nextNodeId++,
    url: request.url,
    totalCapacity: request.totalCapacity,
    status: request.status,
  };
  mockNodes.push(node);
  return delay(node);
}

export function mockPatchNode(id: number, request: NodePatchRequest): Promise<NodeResponse> {
  const existing = mockNodes.find((n) => n.id === id);
  if (!existing) return Promise.reject(new Error("Node not found"));
  const updated = {
    id: existing.id,
    url: request.url ?? existing.url,
    totalCapacity: request.totalCapacity ?? existing.totalCapacity,
    status: request.status ?? existing.status,
  };
  Object.assign(existing, updated);
  return delay(updated);
}

export function mockDeleteNode(id: number): Promise<void> {
  const idx = mockNodes.findIndex((n) => n.id === id);
  if (idx !== -1) mockNodes.splice(idx, 1);
  return delay(undefined);
}

// Media
export function mockListMedia(): Promise<MediaResponse[]> {
  return delay([...mockMedia]);
}

export function mockUploadMedia(file: File): Promise<MediaResponse> {
  const storedMedia: MediaResponse = {
    id: crypto.randomUUID(),
    name: file.name,
    createdAt: new Date().toISOString(),
  };
  mockMedia.unshift(storedMedia);
  return delay(storedMedia);
}

export function mockDeleteMedia(id: string): Promise<void> {
  const idx = mockMedia.findIndex((m) => m.id === id);
  if (idx !== -1) mockMedia.splice(idx, 1);
  return delay(undefined);
}

export function mockDownloadMedia(): Promise<Blob> {
  return delay(new Blob(["mock content"], { type: "text/plain" }));
}
