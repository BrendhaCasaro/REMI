import { type RouteConfig, index, route, layout } from "@react-router/dev/routes";

export default [
  route("login", "routes/login.tsx"),
  layout("routes/app-layout.tsx", [
    index("routes/_index.tsx"),
    route("medias", "routes/medias.tsx"),
    route("users", "routes/users.tsx"),
    route("nodes", "routes/nodes.tsx"),
  ]),
] satisfies RouteConfig;
