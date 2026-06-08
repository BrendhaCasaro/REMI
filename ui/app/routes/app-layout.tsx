import { useState } from "react";
import { Link, Outlet, redirect, useLocation, useNavigate } from "react-router";
import { useTranslation } from "react-i18next";
import { LogOut, Moon, Sun } from "lucide-react";
import { Button } from "~/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "~/components/ui/select";
import { getRole, clearAuth, isTokenExpired, ADMIN_ROUTES } from "~/lib/auth";

const languages = [
  { code: "pt-BR", label: "pt-BR" },
  { code: "en", label: "EN" },
];

function useTheme() {
  const [theme, setThemeState] = useState(() => {
    if (typeof document === "undefined") return "light";
    return document.documentElement.classList.contains("dark") ? "dark" : "light";
  });

  function toggleTheme() {
    const next = theme === "dark" ? "light" : "dark";
    setThemeState(next);
    document.documentElement.classList.toggle("dark", next === "dark");
    localStorage.setItem("theme", next);
  }

  return { theme, toggleTheme };
}

export async function clientLoader() {
  if (typeof window !== "undefined") {
    const token = localStorage.getItem("token");
    if (!token) throw redirect("/login");
    if (isTokenExpired(token)) {
      clearAuth();
      throw redirect("/login");
    }
    const role = localStorage.getItem("role");
    const path = window.location.pathname;
    if (role === "USER" && (ADMIN_ROUTES as readonly string[]).includes(path)) {
      throw redirect("/medias");
    }
  }
  return null;
}

export function HydrateFallback() {
  return null;
}

export default function AppLayout() {
  const location = useLocation();
  const navigate = useNavigate();
  const { t, i18n } = useTranslation("common");
  const { theme, toggleTheme } = useTheme();
  const role = getRole();

  const navItems = [
    { to: "/medias", label: t("nav.medias") },
    ...(role === "ADMIN"
      ? [
          { to: "/users" as const, label: t("nav.users") },
          { to: "/nodes" as const, label: t("nav.nodes") },
        ]
      : []),
  ];

  function handleLogout() {
    clearAuth();
    navigate("/login", { replace: true });
  }

  return (
    <div className="min-h-screen">
      <header className="sticky top-0 z-50 w-full border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
        <div className="mx-auto flex h-14 max-w-6xl items-center justify-between px-4 sm:px-6 lg:px-8">
          <Link to="/medias" className="text-xl font-bold tracking-tight">
            REMI
          </Link>
          <div className="flex items-center gap-2">
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
            <Button
              variant="ghost"
              size="sm"
              className="h-8 w-8 p-0"
              onClick={toggleTheme}
              title={theme === "dark" ? "Light mode" : "Dark mode"}
            >
              {theme === "dark" ? (
                <Sun className="size-4" />
              ) : (
                <Moon className="size-4" />
              )}
            </Button>
            <Select
              value={i18n.language}
              onValueChange={(code) => i18n.changeLanguage(code)}
            >
              <SelectTrigger className="h-8 w-22">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {languages.map(({ code, label }) => (
                  <SelectItem key={code} value={code}>
                    {label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <Button
              variant="ghost"
              size="sm"
              className="h-8 w-8 p-0"
              onClick={handleLogout}
              title={t("actions.logout")}
            >
              <LogOut className="size-4" />
            </Button>
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-4 py-6 sm:px-6 lg:px-8">
        <Outlet />
      </main>
    </div>
  );
}
