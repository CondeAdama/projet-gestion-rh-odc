import { useEffect, useRef } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, CheckCircle2, AlertCircle, Info } from 'lucide-react';

const FOCUSABLE = 'button:not([disabled]), [href], input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])';

function useModalA11y(open, onClose, panelRef) {
  useEffect(() => {
    if (!open) return undefined;

    const panel = panelRef.current;
    const previouslyFocused = document.activeElement;

    const focusables = panel?.querySelectorAll(FOCUSABLE);
    if (focusables?.length) {
      focusables[0].focus();
    } else {
      panel?.focus();
    }

    const onKeyDown = (e) => {
      if (e.key === 'Escape') {
        e.preventDefault();
        onClose();
        return;
      }
      if (e.key !== 'Tab' || !panel) return;

      const items = [...panel.querySelectorAll(FOCUSABLE)].filter(el => el.offsetParent !== null);
      if (items.length === 0) return;

      const first = items[0];
      const last = items[items.length - 1];
      if (e.shiftKey && document.activeElement === first) {
        e.preventDefault();
        last.focus();
      } else if (!e.shiftKey && document.activeElement === last) {
        e.preventDefault();
        first.focus();
      }
    };

    document.addEventListener('keydown', onKeyDown);
    return () => {
      document.removeEventListener('keydown', onKeyDown);
      if (previouslyFocused instanceof HTMLElement && previouslyFocused.isConnected) {
        previouslyFocused.focus();
      }
    };
  }, [open, onClose, panelRef]);
}

export function Modal({ open, onClose, title, children, size = 'md' }) {
  const sizes = { sm: 'max-w-md', md: 'max-w-lg', lg: 'max-w-2xl', xl: 'max-w-4xl' };
  const panelRef = useRef(null);
  useModalA11y(open, onClose, panelRef);

  return (
    <AnimatePresence>
      {open && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-sm"
          onClick={onClose}
          role="presentation"
        >
          <motion.div
            ref={panelRef}
            tabIndex={-1}
            role="dialog"
            aria-modal="true"
            aria-labelledby={title ? 'modal-title' : undefined}
            initial={{ opacity: 0, scale: 0.95, y: 10 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            exit={{ opacity: 0, scale: 0.95, y: 10 }}
            transition={{ type: 'spring', damping: 25, stiffness: 300 }}
            className={`bg-white/95 backdrop-blur-xl border border-white/40 rounded-3xl shadow-2xl w-full ${sizes[size]} max-h-[90vh] overflow-hidden flex flex-col outline-none`}
            onClick={e => e.stopPropagation()}
          >
            <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100">
              <h3 id="modal-title" className="text-lg font-semibold text-[#1D1D1F]">{title}</h3>
              <button type="button" onClick={onClose} aria-label="Fermer" className="p-2 hover:bg-gray-100 rounded-xl transition">
                <X size={18} />
              </button>
            </div>
            <div className="px-6 py-5 overflow-y-auto flex-1">{children}</div>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}

export function ConfirmDialog({ open, onClose, onConfirm, title, message, confirmLabel = 'Confirmer', danger = false, loading = false }) {
  return (
    <Modal open={open} onClose={onClose} title={title} size="sm">
      <p className="text-sm text-gray-600 mb-6 whitespace-pre-line">{message}</p>
      <div className="flex gap-3 justify-end">
        <button type="button" onClick={onClose} className="px-4 py-2.5 text-sm font-medium text-gray-600 hover:bg-gray-100 rounded-xl transition">
          Annuler
        </button>
        <button
          type="button"
          onClick={onConfirm}
          disabled={loading}
          className={`px-5 py-2.5 text-sm font-semibold text-white rounded-xl transition disabled:opacity-50 ${
            danger ? 'bg-red-600 hover:bg-red-700' : 'bg-gradient-to-r from-violet-700 to-gray-900 hover:from-violet-800 hover:to-black'
          }`}
        >
          {loading ? 'En cours...' : confirmLabel}
        </button>
      </div>
    </Modal>
  );
}

const ALERT_ICONS = {
  success: { Icon: CheckCircle2, ring: 'bg-emerald-100 text-emerald-600' },
  error: { Icon: AlertCircle, ring: 'bg-red-100 text-red-600' },
  info: { Icon: Info, ring: 'bg-violet-100 text-violet-700' },
};

export function AlertDialog({ open, onClose, title, message, variant = 'info', confirmLabel = 'OK' }) {
  const { Icon, ring } = ALERT_ICONS[variant] || ALERT_ICONS.info;

  return (
    <Modal open={open} onClose={onClose} title={title} size="sm">
      <div className="flex gap-4 mb-6">
        <div className={`w-11 h-11 rounded-2xl flex items-center justify-center flex-shrink-0 ${ring}`}>
          <Icon size={22} />
        </div>
        <p className="text-sm text-gray-600 whitespace-pre-line pt-2">{message}</p>
      </div>
      <div className="flex justify-end">
        <button
          type="button"
          onClick={onClose}
          className="px-5 py-2.5 text-sm font-semibold text-white rounded-xl bg-gradient-to-r from-violet-700 to-gray-900 hover:from-violet-800 hover:to-black transition"
        >
          {confirmLabel}
        </button>
      </div>
    </Modal>
  );
}
