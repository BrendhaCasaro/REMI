import { useEffect, useRef, useState } from "react";
import { useTranslation } from "react-i18next";
import { toast } from "sonner";
import { Button } from "~/components/ui/button";
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
import { Skeleton } from "~/components/ui/skeleton";
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

      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>{t("table.name")}</TableHead>
            <TableHead>{t("table.createdAt")}</TableHead>
            <TableHead className="w-48 text-right">{tc("actions.download")}</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {loading &&
            Array.from({ length: 3 }).map((_, i) => (
              <TableRow key={i}>
                <TableCell><Skeleton className="h-4 w-48" /></TableCell>
                <TableCell><Skeleton className="h-4 w-32" /></TableCell>
                <TableCell><Skeleton className="h-8 w-24 ml-auto" /></TableCell>
              </TableRow>
            ))}
          {!loading && medias.length === 0 && (
            <TableRow>
              <TableCell colSpan={3} className="py-12 text-center text-muted-foreground">
                {t("empty")}
              </TableCell>
            </TableRow>
          )}
          {!loading &&
            medias.map((media) => (
              <TableRow key={media.id}>
                <TableCell className="font-medium">{media.name}</TableCell>
                <TableCell>
                  {new Date(media.createdAt).toLocaleDateString(undefined, {
                    day: "2-digit",
                    month: "short",
                    year: "numeric",
                  })}
                </TableCell>
                <TableCell className="text-right">
                  <div className="flex justify-end gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => handleDownload(media)}
                    >
                      {tc("actions.download")}
                    </Button>
                    <Button
                      variant="destructive"
                      size="sm"
                      onClick={() => setDeleteTarget(media)}
                    >
                      {tc("actions.delete")}
                    </Button>
                  </div>
                </TableCell>
              </TableRow>
            ))}
        </TableBody>
      </Table>

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
