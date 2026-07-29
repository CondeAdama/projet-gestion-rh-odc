import { useState } from 'react';
import { FileDown, FileSpreadsheet, Loader2 } from 'lucide-react';
import { useDialog } from '../../context/DialogContext';
import { exportTableToExcel, exportTableToPdf } from '../../utils/tableExport';

export function TableExportButtons({
  columns,
  rows,
  basename,
  title,
  disabled = false,
  className = '',
}) {
  const [busy, setBusy] = useState(null);
  const { alert } = useDialog();
  const count = rows?.length ?? 0;

  const run = async (label, action) => {
    if (disabled || count === 0) {
      await alert({
        title: 'Export impossible',
        message: 'Aucune donnée filtrée à exporter.',
        variant: 'warning',
      });
      return;
    }
    setBusy(label);
    try {
      await action();
    } catch (err) {
      console.error(err);
      await alert({
        title: 'Export impossible',
        message: err.message || "Erreur lors de l'export.",
        variant: 'error',
      });
    } finally {
      setBusy(null);
    }
  };

  return (
    <div className={`flex flex-wrap gap-2 ${className}`}>
      <button
        type="button"
        disabled={!!busy || disabled || count === 0}
        onClick={() => run('excel', () => exportTableToExcel(columns, rows, basename))}
        className="flex items-center gap-1.5 px-3 py-2 bg-emerald-600 text-white rounded-xl text-xs font-semibold disabled:opacity-50"
        title={`Exporter ${count} ligne(s) en Excel`}
      >
        {busy === 'excel' ? <Loader2 size={14} className="animate-spin" /> : <FileSpreadsheet size={14} />}
        Excel
      </button>
      <button
        type="button"
        disabled={!!busy || disabled || count === 0}
        onClick={() => run('pdf', () => exportTableToPdf(columns, rows, basename, title))}
        className="flex items-center gap-1.5 px-3 py-2 bg-[#1e3a5f] text-white rounded-xl text-xs font-semibold disabled:opacity-50"
        title={`Exporter ${count} ligne(s) en PDF`}
      >
        {busy === 'pdf' ? <Loader2 size={14} className="animate-spin" /> : <FileDown size={14} />}
        PDF
      </button>
    </div>
  );
}
