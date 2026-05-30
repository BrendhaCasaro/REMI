import { useEffect, useState } from "react";
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
      toast.error("Erro ao carregar nodes");
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
      toast.error("Preencha URL e capacidade");
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
        toast.success("Node atualizado");
      } else {
        const node = await createNode({
          url: formUrl,
          totalCapacity: Number(formCapacity),
          key: formKey || crypto.randomUUID(),
          status: formStatus,
        });
        setNodes((prev) => [...prev, node]);
        toast.success("Node criado");
      }
      setFormOpen(false);
      await loadNodes();
    } catch {
      toast.error("Erro ao salvar node");
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
      toast.success("Node excluído");
    } catch {
      toast.error("Erro ao excluir");
    } finally {
      setDeleting(false);
      setDeleteTarget(null);
    }
  }

  return (
    <>
      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold">Nodes</h1>
          <p className="text-muted-foreground">Gerencie os nodes de armazenamento</p>
        </div>
        <Button onClick={openCreate}>Novo node</Button>
      </div>

      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>URL</TableHead>
            <TableHead>Capacidade (GB)</TableHead>
            <TableHead className="w-24">Status</TableHead>
            <TableHead className="w-48 text-right">Ações</TableHead>
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
                Nenhum node cadastrado.
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
                    {node.status === "ONLINE" ? "Online" : "Offline"}
                  </Badge>
                </TableCell>
                <TableCell className="text-right">
                  <div className="flex justify-end gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => openEdit(i)}
                    >
                      Editar
                    </Button>
                    <Button
                      variant="destructive"
                      size="sm"
                      onClick={() => setDeleteTarget(i)}
                    >
                      Excluir
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
              {editingIndex !== null ? "Editar node" : "Novo node"}
            </DialogTitle>
            <DialogDescription>
              {editingIndex !== null
                ? `Editando ${nodes[editingIndex]?.url}`
                : "Cadastre um novo node de armazenamento"}
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="url">URL</Label>
              <Input
                id="url"
                value={formUrl}
                onChange={(e) => setFormUrl(e.target.value)}
                placeholder="http://node1.local:8081"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="capacity">Capacidade total (GB)</Label>
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
                <Label htmlFor="key">Chave de autenticação</Label>
                <Input
                  id="key"
                  value={formKey}
                  onChange={(e) => setFormKey(e.target.value)}
                  placeholder="UUID"
                />
              </div>
            )}
            <div className="space-y-2">
              <Label htmlFor="status">Status</Label>
              <Select
                value={formStatus}
                onValueChange={(v) => setFormStatus(v as NodeStatus)}
              >
                <SelectTrigger className="w-full">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ONLINE">Online</SelectItem>
                  <SelectItem value="OFFLINE">Offline</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setFormOpen(false)}>
              Cancelar
            </Button>
            <Button onClick={handleSave} disabled={saving}>
              {saving ? "Salvando..." : "Salvar"}
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
            <DialogTitle>Confirmar exclusão</DialogTitle>
            <DialogDescription>
              Deseja realmente excluir o node{" "}
              <strong>{deleteTarget !== null ? nodes[deleteTarget]?.url : ""}</strong>?
              Todas as mídias armazenadas neste node também serão removidas.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDeleteTarget(null)}>
              Cancelar
            </Button>
            <Button
              variant="destructive"
              onClick={handleDelete}
              disabled={deleting}
            >
              {deleting ? "Excluindo..." : "Excluir"}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}
