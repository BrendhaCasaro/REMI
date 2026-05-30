import { useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { toast } from "sonner";
import { Button } from "~/components/ui/button";
import { Input } from "~/components/ui/input";
import { Label } from "~/components/ui/label";
import { Badge } from "~/components/ui/badge";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "~/components/ui/table";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "~/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "~/components/ui/select";
import { Skeleton } from "~/components/ui/skeleton";
import { listNodes, createNode, patchNode, deleteNode } from "~/lib/api";
import type { NodeResponse, NodeStatus } from "~/lib/types";

export default function Nodes() {
  const { t } = useTranslation("nodes");
  const { t: tc } = useTranslation("common");
  const [nodes, setNodes] = useState<NodeResponse[]>([]);
  const [loading, setLoading] = useState(true);

  const [formOpen, setFormOpen] = useState(false);
  const [editingIndex, setEditingIndex] = useState<number | null>(null);
  const [formUrl, setFormUrl] = useState("");
  const [formCapacity, setFormCapacity] = useState("");
  const [formKey, setFormKey] = useState("");
  const [formStatus, setFormStatus] = useState<NodeStatus>("ONLINE");
  const [saving, setSaving] = useState(false);

  const [deleteTarget, setDeleteTarget] = useState<number | null>(null);
  const [deleting, setDeleting] = useState(false);

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
    setEditingIndex(null);
    setFormUrl("");
    setFormCapacity("");
    setFormKey(crypto.randomUUID());
    setFormStatus("ONLINE");
    setFormOpen(true);
  }

  function openEdit(index: number) {
    const node = nodes[index];
    setEditingIndex(index);
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
      if (editingIndex !== null) {
        await patchNode(editingIndex + 1, {
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
    if (deleteTarget === null) return;
    setDeleting(true);
    try {
      await deleteNode(deleteTarget + 1);
      setNodes((prev) => prev.filter((_, i) => i !== deleteTarget));
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

      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>{t("table.url")}</TableHead>
            <TableHead>{t("table.capacity")}</TableHead>
            <TableHead className="w-24">{t("table.status")}</TableHead>
            <TableHead className="w-48 text-right">{tc("actions.edit")}</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {loading &&
            Array.from({ length: 3 }).map((_, i) => (
              <TableRow key={i}>
                <TableCell><Skeleton className="h-4 w-64" /></TableCell>
                <TableCell><Skeleton className="h-4 w-20" /></TableCell>
                <TableCell><Skeleton className="h-5 w-16" /></TableCell>
                <TableCell><Skeleton className="h-8 w-32 ml-auto" /></TableCell>
              </TableRow>
            ))}
          {!loading && nodes.length === 0 && (
            <TableRow>
              <TableCell colSpan={4} className="py-12 text-center text-muted-foreground">
                {t("empty")}
              </TableCell>
            </TableRow>
          )}
          {!loading &&
            nodes.map((node, i) => (
              <TableRow key={i}>
                <TableCell className="font-mono text-sm">{node.url}</TableCell>
                <TableCell>{node.totalCapacity} GB</TableCell>
                <TableCell>
                  <Badge variant={node.status === "ONLINE" ? "default" : "secondary"}>
                    {node.status === "ONLINE" ? t("form.online") : t("form.offline")}
                  </Badge>
                </TableCell>
                <TableCell className="text-right">
                  <div className="flex justify-end gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => openEdit(i)}
                    >
                      {tc("actions.edit")}
                    </Button>
                    <Button
                      variant="destructive"
                      size="sm"
                      onClick={() => setDeleteTarget(i)}
                    >
                      {tc("actions.delete")}
                    </Button>
                  </div>
                </TableCell>
              </TableRow>
            ))}
        </TableBody>
      </Table>

      <Dialog open={formOpen} onOpenChange={(open) => !open && setFormOpen(false)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              {editingIndex !== null ? t("edit.title") : t("create.title")}
            </DialogTitle>
            <DialogDescription>
              {editingIndex !== null
                ? t("edit.description", { url: nodes[editingIndex]?.url })
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
            {editingIndex === null && (
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
        open={deleteTarget !== null}
        onOpenChange={(open) => !open && setDeleteTarget(null)}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{t("confirmDelete.title")}</DialogTitle>
            <DialogDescription>
              {deleteTarget !== null
                ? t("confirmDelete.description").replace("{{url}}", nodes[deleteTarget]?.url ?? "")
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
