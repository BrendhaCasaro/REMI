export type Role = "ADMIN" | "USER";

export type NodeStatus = "ONLINE" | "OFFLINE";

// Auth
export type LoginRequest = {
  username: string;
  password: string;
};

export type LoginResponse = {
  token: string;
};

// Users
export type UserResponse = {
  id: number;
  username: string;
  role: Role;
};

export type UserRequest = {
  username: string;
  password: string;
  role: Role;
};

// Nodes
export type NodeResponse = {
  id: number;
  url: string;
  totalCapacity: number;
  status: NodeStatus;
};

export type NodeConfigRequest = {
  url: string;
  totalCapacity: number;
  key: string;
  status: NodeStatus;
};

export type NodePatchRequest = {
  url?: string;
  totalCapacity?: number;
  key?: string;
  status?: NodeStatus;
};

// Media
export type MediaResponse = {
  id: string;
  name: string;
  createdAt: string;
};

// Generic paginated/error shapes the API might use
export type ApiError = {
  message: string;
};
