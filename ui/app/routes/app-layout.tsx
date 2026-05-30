import { Link, Outlet, useLocation } from "react-router";
import { useTranslation } from "react-i18next";
import { Button } from "~/components/ui/button";

const languages = [
  { code: "pt-BR", label: "PT" },
  { code: "en", label: "EN" },
];

export default function AppLayout() {
  const location = useLocation();
  const { t, i18n } = useTranslation("common");

  const navItems = [
    { to: "/medias", label: t("nav.medias") },
    { to: "/users", label: t("nav.users") },
    { to: "/nodes", label: t("nav.nodes") },
  ];

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
            <div className="ml-2 flex gap-1">
              {languages.map(({ code, label }) => (
                <Button
                  key={code}
                  variant={i18n.language === code ? "default" : "ghost"}
                  size="sm"
                  className="h-8 w-8 p-0 text-xs"
                  onClick={() => i18n.changeLanguage(code)}
                >
                  {label}
                </Button>
              ))}
            </div>
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-4 py-6 sm:px-6 lg:px-8">
        <Outlet />
      </main>
    </div>
  );
}
