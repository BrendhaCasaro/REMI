import { Link, Outlet, useLocation } from "react-router";

const navItems = [
  { to: "/medias", label: "Mídias" },
  { to: "/users", label: "Usuários" },
  { to: "/nodes", label: "Nodes" },
];

export default function AppLayout() {
  const location = useLocation();

  return (
    <div className="min-h-screen">
      <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
        <div className="mx-auto flex h-14 max-w-6xl items-center justify-between px-4 sm:px-6 lg:px-8">
          <Link to="/medias" className="text-xl font-bold tracking-tight">
            REMI
          </Link>
          <nav className="flex items-center gap-1">
            {navItems.map(({ to, label }) => (
              <Link
                key={to}
                to={to}
                className={`rounded-md px-3 py-2 text-sm font-medium transition-colors ${
                  location.pathname === to
                    ? "bg-accent text-accent-foreground"
                    : "text-muted-foreground hover:bg-accent hover:text-accent-foreground"
                }`}
              >
                {label}
              </Link>
            ))}
          </nav>
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-4 py-6 sm:px-6 lg:px-8">
        <Outlet />
      </main>
    </div>
  );
}
