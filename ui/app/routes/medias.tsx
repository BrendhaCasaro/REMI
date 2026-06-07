import { useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import type { ColumnDef } from "@tanstack/react-table";
import { ArrowUpDown, MoreHorizontal, Search } from "lucide-react";
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
import {
  InputGroup,
  InputGroupAddon,
  InputGroupInput,
} from "~/components/ui/input-group";
import { listMedia, uploadMedia, deleteMedia, downloadMedia } from "~/lib/api";
import type { MediaResponse } from "~/lib/types";
import { useNavigate } from "react-router";

export default function Medias() {
  const { t } = useTranslation("medias");
  const { t: tc } = useTranslation("common");
  const navigate = useNavigate();
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [medias, setMedias] = useState<MediaResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");
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
        const storedMedia = row.original;
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
                <DropdownMenuItem onClick={() => handleDownload(storedMedia)}>
                  {tc("actions.download")}
                </DropdownMenuItem>
                <DropdownMenuItem onClick={() => setDeleteTarget(storedMedia)}>
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
      await uploadMedia(file);
      toast.success(`${file.name} ${t("uploadSuccess")}`);
      navigate("/");
    } catch {
      toast.error(t("uploadError"));
    } finally {
      setUploading(false);
      if (fileInputRef.current) fileInputRef.current.value = "";
    }
  }

  async function handleDownload(storedMedia: MediaResponse) {
    try {
      const blob = await downloadMedia(storedMedia.id);
      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = storedMedia.name;
      link.click();
      URL.revokeObjectURL(url);
    } catch {
      toast.error(t("downloadError"));
    }
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

      <div className="mb-4">
        <InputGroup>
          <InputGroupInput
            placeholder={t("search")}
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          <InputGroupAddon>
            <Search className="size-4" />
          </InputGroupAddon>
        </InputGroup>
      </div>

      <DataTable
        columns={columns}
        data={medias.filter((m) => m.name.toLowerCase().includes(search.toLowerCase()))}
        loading={loading}
        selectable
        emptyMessage={t("empty")}
      />

      <Dialog
        open={!!deleteTarget}
        onOpenChange={(open) => !open && setDeleteTarget(null)}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{t("confirmDelete.title")}</DialogTitle>
            <DialogDescription>
              {deleteTarget
                ? t("confirmDelete.description", { name: deleteTarget.name })
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
