import { useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import type { ColumnDef } from "@tanstack/react-table";
import { ArrowUpDown, MoreHorizontal } from "lucide-react";
import { toast } from "sonner";
import { Button } from "~/components/ui/button";
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
import { listMedia, uploadMedia, deleteMedia } from "~/lib/api";
import type { MediaResponse } from "~/lib/types";

export default function Medias() {
  const { t } = useTranslation("medias");
  const { t: tc } = useTranslation("common");
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [medias, setMedias] = useState<MediaResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<MediaResponse | null>(null);
  const [deleting, setDeleting] = useState(false);

  const columns: ColumnDef<MediaResponse>[] = [
    {
      accessorKey: "name",
      header: ({ column }) => (
        <Button
          variant="ghost"
          className="-ml-4"
          onClick={() => column.toggleSorting(column.getIsSorted() === "asc")}
        >
          {t("table.name")}
          <ArrowUpDown className="ml-2 h-4 w-4" />
        </Button>
      ),
    },
    {
      accessorKey: "createdAt",
      header: ({ column }) => (
        <Button
          variant="ghost"
          className="-ml-4"
          onClick={() => column.toggleSorting(column.getIsSorted() === "asc")}
        >
          {t("table.createdAt")}
          <ArrowUpDown className="ml-2 h-4 w-4" />
        </Button>
      ),
      cell: ({ getValue }) => {
        const date = getValue<string>();
        return new Date(date).toLocaleDateString(undefined, {
          day: "2-digit",
          month: "short",
          year: "numeric",
        });
      },
    },
    {
      id: "actions",
      cell: ({ row }) => {
        const media = row.original;
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
                <DropdownMenuItem onClick={() => handleDownload(media)}>
                  {tc("actions.download")}
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => setDeleteTarget(media)}>
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
    loadMedias();
  }, []);

  async function loadMedias() {
    setLoading(true);
    try {
      const data = await listMedia();
      setMedias(data);
    } catch {
      toast.error(t("loadError"));
    } finally {
      setLoading(false);
    }
  }

  async function handleUpload(files: FileList | null) {
    const file = files?.[0];
    if (!file) return;
    setUploading(true);
    try {
      const media = await uploadMedia(file);
      setMedias((prev) => [media, ...prev]);
      toast.success(`${file.name} ${t("uploadSuccess")}`);
    } catch {
      toast.error(t("uploadError"));
    } finally {
      setUploading(false);
      if (fileInputRef.current) fileInputRef.current.value = "";
    }
  }

  function handleDownload(media: MediaResponse) {
    const link = document.createElement("a");
    link.href = `http://localhost:8080/api/files/download/${media.id}`;
    link.download = media.name;
    link.click();
  }

  async function handleDelete() {
    if (!deleteTarget) return;
    setDeleting(true);
    try {
      await deleteMedia(deleteTarget.id);
      setMedias((prev) => prev.filter((m) => m.id !== deleteTarget.id));
      toast.success(`${deleteTarget.name} ${t("deleteSuccess")}`);
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
        <Button
          onClick={() => fileInputRef.current?.click()}
          disabled={uploading}
        >
          {uploading ? tc("actions.uploading") : tc("actions.upload")}
        </Button>
        <input
          ref={fileInputRef}
          type="file"
          className="hidden"
          onChange={(e) => handleUpload(e.target.files)}
        />
      </div>

      <DataTable columns={columns} data={medias} loading={loading} selectable emptyMessage={t("empty")} />

      <Dialog
        open={!!deleteTarget}
        onOpenChange={(open) => !open && setDeleteTarget(null)}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{t("confirmDelete.title")}</DialogTitle>
            <DialogDescription>
              {deleteTarget
                ? t("confirmDelete.description").replace("{{name}}", deleteTarget.name)
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
