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
import { listNodes, createNode, patchNode, deleteNode } from "~/lib/api";
import type { NodeResponse, NodeStatus } from "~/lib/types";

export default function Nodes() {
  const { t } = useTranslation("nodes");
  const { t: tc } = useTranslation("common");
  const [nodes, setNodes] = useState<NodeResponse[]>([]);
  const [loading, setLoading] = useState(true);

  const [formOpen, setFormOpen] = useState(false);
  const [editingNode, setEditingNode] = useState<NodeResponse | null>(null);
  const [formUrl, setFormUrl] = useState("");
  const [formCapacity, setFormCapacity] = useState("");
  const [formKey, setFormKey] = useState("");
  const [formStatus, setFormStatus] = useState<NodeStatus>("ONLINE");
  const [saving, setSaving] = useState(false);

  const [deleteTarget, setDeleteTarget] = useState<NodeResponse | null>(null);
  const [deleting, setDeleting] = useState(false);

  const columns: ColumnDef<NodeResponse>[] = [
    {
      accessorKey: "id",
      header: ({ column }) => (
        <Button
          variant="ghost"
          className="-ml-4"
          onClick={() => column.toggleSorting(column.getIsSorted() === "asc")}
        >
          ID
          <ArrowUpDown className="ml-2 h-4 w-4" />
        </Button>
      ),
    },
    {
      accessorKey: "url",
      header: ({ column }) => (
        <Button
          variant="ghost"
          className="-ml-4"
          onClick={() => column.toggleSorting(column.getIsSorted() === "asc")}
        >
          {t("table.url")}
          <ArrowUpDown className="ml-2 h-4 w-4" />
        </Button>
      ),
      cell: ({ row }) => (
        <span className="font-mono text-sm">{row.original.url}</span>
      ),
    },
    {
      accessorKey: "totalCapacity",
      header: ({ column }) => (
        <Button
          variant="ghost"
          className="-ml-4"
          onClick={() => column.toggleSorting(column.getIsSorted() === "asc")}
        >
          {t("table.capacity")}
          <ArrowUpDown className="ml-2 h-4 w-4" />
        </Button>
      ),
      cell: ({ getValue }) => `${getValue<number>()} GB`,
    },
    {
      accessorKey: "status",
      header: ({ column }) => (
        <Button
          variant="ghost"
          className="-ml-4"
          onClick={() => column.toggleSorting(column.getIsSorted() === "asc")}
        >
          {t("table.status")}
          <ArrowUpDown className="ml-2 h-4 w-4" />
        </Button>
      ),
      cell: ({ getValue }) => {
        const status = getValue<NodeStatus>();
        return (
          <Badge variant={status === "ONLINE" ? "default" : "secondary"}>
            {status === "ONLINE" ? t("form.online") : t("form.offline")}
          </Badge>
        );
      },
    },
    {
      id: "actions",
      cell: ({ row }) => {
        const node = row.original;
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
                <DropdownMenuItem onClick={() => openEdit(node)}>
                  {tc("actions.edit")}
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => setDeleteTarget(node)}>
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
    loadNodes();
  }, []);

  async function loadNodes() {
    setLoading(true);
    try {
      const data = await listNodes();
      setNodes(data);
    } catch {
      toast.error(t("loadError"));
    } finally {
      setLoading(false);
    }
  }

  function openCreate() {
    setEditingNode(null);
    setFormUrl("");
    setFormCapacity("");
    setFormKey(crypto.randomUUID());
    setFormStatus("ONLINE");
    setFormOpen(true);
  }

  function openEdit(node: NodeResponse) {
    setEditingNode(node);
    setFormUrl(node.url);
    setFormCapacity(String(node.totalCapacity));
    setFormKey("");
    setFormStatus(node.status);
    setFormOpen(true);
  }

  async function handleSave() {
    if (!formUrl || !formCapacity) {
      toast.error(t("validation"));
      return;
    }
    setSaving(true);
    try {
      if (editingNode) {
        await patchNode(editingNode.id, {
          url: formUrl,
          totalCapacity: Number(formCapacity),
          key: formKey || undefined,
          status: formStatus,
        });
        toast.success(t("saveSuccess"));
      } else {
        await createNode({
          url: formUrl,
          totalCapacity: Number(formCapacity),
          key: formKey || crypto.randomUUID(),
          status: formStatus,
        });
        toast.success(t("saveSuccess"));
      }
      setFormOpen(false);
      await loadNodes();
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
      await deleteNode(deleteTarget.id);
      setNodes((prev) => prev.filter((n) => n.id !== deleteTarget.id));
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

      <DataTable columns={columns} data={nodes} loading={loading} selectable emptyMessage={t("empty")} />

      <Dialog open={formOpen} onOpenChange={(open) => !open && setFormOpen(false)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {editingNode ? t("edit.title") : t("create.title")}
            </DialogTitle>
            <DialogDescription>
              {editingNode
                ? t("edit.description", { url: editingNode.url })
                : t("create.description")}
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="url">{t("form.url")}</Label>
              <Input
                id="url"
                value={formUrl}
                onChange={(e) => setFormUrl(e.target.value)}
                placeholder={t("form.urlPlaceholder")}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="capacity">{t("form.capacity")}</Label>
              <Input
                id="capacity"
                type="number"
                value={formCapacity}
                onChange={(e) => setFormCapacity(e.target.value)}
                placeholder="500"
              />
            </div>
            {!editingNode && (
              <div className="space-y-2">
                <Label htmlFor="key">{t("form.key")}</Label>
                <Input
                  id="key"
                  value={formKey}
                  onChange={(e) => setFormKey(e.target.value)}
                  placeholder="UUID"
                />
              </div>
            )}
            <div className="space-y-2">
              <Label htmlFor="status">{t("form.status")}</Label>
              <Select
                value={formStatus}
                onValueChange={(v) => setFormStatus(v as NodeStatus)}
              >
                <SelectTrigger className="w-full">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ONLINE">{t("form.online")}</SelectItem>
                  <SelectItem value="OFFLINE">{t("form.offline")}</SelectItem>
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
                ? t("confirmDelete.description", { url: deleteTarget.url })
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
