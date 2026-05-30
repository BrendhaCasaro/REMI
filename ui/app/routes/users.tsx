import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import type { ColumnDef } from "@tanstack/react-table";
import { ArrowUpDown, MoreHorizontal } from "lucide-react";
import { toast } from "sonner";
import { Button } from "~/components/ui/button";
import { Input } from "~/components/ui/input";
import { Label } from "~/components/ui/label";
import { Badge } from "~/components/ui/badge";
import { DataTable } from "~/components/ui/data-table";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "~/components/ui/dialog";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "~/components/ui/dropdown-menu";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "~/components/ui/select";
import { listUsers, createUser, updateUser, deleteUser } from "~/lib/api";
import type { UserResponse, Role } from "~/lib/types";

export default function Users() {
  const { t } = useTranslation("users");
  const { t: tc } = useTranslation("common");
  const [users, setUsers] = useState<UserResponse[]>([]);
  const [loading, setLoading] = useState(true);

  const [formOpen, setFormOpen] = useState(false);
  const [editingUser, setEditingUser] = useState<UserResponse | null>(null);
  const [formUsername, setFormUsername] = useState("");
  const [formPassword, setFormPassword] = useState("");
  const [formRole, setFormRole] = useState<Role>("USER");
  const [saving, setSaving] = useState(false);

  const [deleteTarget, setDeleteTarget] = useState<UserResponse | null>(null);
  const [deleting, setDeleting] = useState(false);

  const columns: ColumnDef<UserResponse>[] = [
    {
      accessorKey: "id",
      header: ({ column }) => (
        <Button
          variant="ghost"
          className="-ml-4"
          onClick={() => column.toggleSorting(column.getIsSorted() === "asc")}
        >
          {t("table.id")}
          <ArrowUpDown className="ml-2 h-4 w-4" />
        </Button>
      ),
    },
    {
      accessorKey: "username",
      header: ({ column }) => (
        <Button
          variant="ghost"
          className="-ml-4"
          onClick={() => column.toggleSorting(column.getIsSorted() === "asc")}
        >
          {t("table.username")}
          <ArrowUpDown className="ml-2 h-4 w-4" />
        </Button>
      ),
    },
    {
      id: "role",
      header: t("table.role"),
      cell: () => <Badge variant="outline">USER</Badge>,
    },
    {
      id: "actions",
      cell: ({ row }) => {
        const user = row.original;
        return (
          <div className="text-right">
            <DropdownMenu>
              <DropdownMenuTrigger asChild>
                <Button variant="ghost" size="sm" className="h-8 w-8 p-0">
                  <span className="sr-only">{tc("actions.openMenu")}</span>
                  <MoreHorizontal className="h-4 w-4" />
                </Button>
              </DropdownMenuTrigger>
              <DropdownMenuContent align="end">
                <DropdownMenuItem onClick={() => openEdit(user)}>
                  {tc("actions.edit")}
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => setDeleteTarget(user)}>
                  {tc("actions.delete")}
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          </div>
        );
      },
    },
  ];

  useEffect(() => {
    loadUsers();
  }, []);

  async function loadUsers() {
    setLoading(true);
    try {
      const data = await listUsers();
      setUsers(data);
    } catch {
      toast.error(t("loadError"));
    } finally {
      setLoading(false);
    }
  }

  function openCreate() {
    setEditingUser(null);
    setFormUsername("");
    setFormPassword("");
    setFormRole("USER");
    setFormOpen(true);
  }

  function openEdit(user: UserResponse) {
    setEditingUser(user);
    setFormUsername(user.username);
    setFormPassword("");
    setFormRole("USER");
    setFormOpen(true);
  }

  async function handleSave() {
    if (!formUsername || !formPassword) {
      toast.error(t("validation"));
      return;
    }
    setSaving(true);
    try {
      if (editingUser) {
        await updateUser(editingUser.id, {
          username: formUsername,
          password: formPassword,
          role: formRole,
        });
        toast.success(t("saveSuccess"));
      } else {
        await createUser({
          username: formUsername,
          password: formPassword,
          role: formRole,
        });
        toast.success(t("saveSuccess"));
      }
      setFormOpen(false);
      await loadUsers();
    } catch {
      toast.error(t("saveError"));
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!deleteTarget) return;
    setDeleting(true);
    try {
      await deleteUser(deleteTarget.id);
      setUsers((prev) => prev.filter((u) => u.id !== deleteTarget.id));
      toast.success(t("deleteSuccess"));
    } catch {
      toast.error(t("deleteError"));
    } finally {
      setDeleting(false);
      setDeleteTarget(null);
    }
  }

  return (
    <>
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">{t("title")}</h1>
          <p className="text-muted-foreground">{t("description")}</p>
        </div>
        <Button onClick={openCreate}>{tc("actions.create")}</Button>
      </div>

      <DataTable columns={columns} data={users} loading={loading} selectable emptyMessage={t("empty")} />

      <Dialog open={formOpen} onOpenChange={(open) => !open && setFormOpen(false)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {editingUser ? t("edit.title") : t("create.title")}
            </DialogTitle>
            <DialogDescription>
              {editingUser
                ? t("edit.description", { username: editingUser.username })
                : t("create.description")}
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="username">{t("form.username")}</Label>
              <Input
                id="username"
                value={formUsername}
                onChange={(e) => setFormUsername(e.target.value)}
                placeholder={t("form.usernamePlaceholder")}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="password">{t("form.password")}</Label>
              <Input
                id="password"
                type="password"
                value={formPassword}
                onChange={(e) => setFormPassword(e.target.value)}
                placeholder={editingUser ? t("form.newPassword") : t("form.passwordPlaceholder")}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="role">{t("form.role")}</Label>
              <Select
                value={formRole}
                onValueChange={(v) => setFormRole(v as Role)}
              >
                <SelectTrigger className="w-full">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ADMIN">{t("form.admin")}</SelectItem>
                  <SelectItem value="USER">{t("form.user")}</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setFormOpen(false)}>
              {tc("actions.cancel")}
            </Button>
            <Button onClick={handleSave} disabled={saving}>
              {saving ? tc("actions.saving") : tc("actions.save")}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog
        open={!!deleteTarget}
        onOpenChange={(open) => !open && setDeleteTarget(null)}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{t("confirmDelete.title")}</DialogTitle>
            <DialogDescription>
              {deleteTarget
                ? t("confirmDelete.description").replace("{{username}}", deleteTarget.username)
                : ""}
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDeleteTarget(null)}>
              {tc("actions.cancel")}
            </Button>
            <Button
              variant="destructive"
              onClick={handleDelete}
              disabled={deleting}
            >
              {deleting ? tc("actions.deleting") : tc("actions.delete")}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}
