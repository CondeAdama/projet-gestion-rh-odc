import { useState } from 'react';
import { Printer, FileDown, Image as ImageIcon, Loader2 } from 'lucide-react';
import {
  downloadElementAsImage,
  downloadElementAsPdf,
  printElement,
  BADGE_WIDTH_MM,
  BADGE_HEIGHT_MM,
} from '../../utils/documentExport';

export function DocumentExportButtons({
  elementId,
  basename,
  variant = 'document',
  onPrint,
  className = '',
}) {
  const [busy, setBusy] = useState(null);

  const run = async (action, label) => {
    setBusy(label);
    try {
      await action();
    } catch (err) {
      console.error(err);
      alert(err.message || 'Erreur lors de l\'export. Réessayez.');
    } finally {
      setBusy(null);
    }
  };

  const isBadge = variant === 'badge';
  const badgeOpts = { widthMm: BADGE_WIDTH_MM, heightMm: BADGE_HEIGHT_MM, realSizeMm: BADGE_WIDTH_MM };
  const docOpts = { scale: 2 };

  const handlePrint = () => {
    if (onPrint) return onPrint();
    return printElement(elementId, isBadge ? badgeOpts : docOpts);
  };

  return (
    <div className={`flex flex-wrap gap-2 ${className}`}>
      <button
        type="button"
        disabled={!!busy}
        onClick={() => run(handlePrint, 'print')}
        className="flex items-center gap-1.5 px-4 py-2 bg-black text-white rounded-xl text-xs font-semibold disabled:opacity-50"
      >
        {busy === 'print' ? <Loader2 size={14} className="animate-spin" /> : <Printer size={14} />}
        Imprimer
      </button>

      <button
        type="button"
        disabled={!!busy}
        onClick={() => run(
          () => downloadElementAsPdf(elementId, basename, isBadge ? badgeOpts : { format: 'a4' }),
          'pdf'
        )}
        className="flex items-center gap-1.5 px-4 py-2 bg-[#1e3a5f] text-white rounded-xl text-xs font-semibold disabled:opacity-50"
      >
        {busy === 'pdf' ? <Loader2 size={14} className="animate-spin" /> : <FileDown size={14} />}
        PDF
      </button>

      <button
        type="button"
        disabled={!!busy}
        onClick={() => run(
          () => downloadElementAsImage(
            elementId,
            basename,
            isBadge ? { realSizeMm: BADGE_WIDTH_MM } : { scale: 2 }
          ),
          'image'
        )}
        className="flex items-center gap-1.5 px-4 py-2 bg-emerald-600 text-white rounded-xl text-xs font-semibold disabled:opacity-50"
      >
        {busy === 'image' ? <Loader2 size={14} className="animate-spin" /> : <ImageIcon size={14} />}
        Image
      </button>
    </div>
  );
}
