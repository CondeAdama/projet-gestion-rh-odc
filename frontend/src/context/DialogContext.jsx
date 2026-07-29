import { createContext, useCallback, useContext, useRef, useState } from 'react';
import { ConfirmDialog, AlertDialog } from '../components/ui/Modal';

const DialogContext = createContext(null);

export function DialogProvider({ children }) {
  const [confirmState, setConfirmState] = useState(null);
  const [alertState, setAlertState] = useState(null);
  const confirmResolver = useRef(null);
  const alertResolver = useRef(null);

  const confirm = useCallback((options) => new Promise((resolve) => {
    confirmResolver.current = resolve;
    setConfirmState({ ...options, open: true });
  }), []);

  const alert = useCallback((options) => new Promise((resolve) => {
    alertResolver.current = resolve;
    setAlertState({ ...options, open: true });
  }), []);

  const finishConfirm = (accepted) => {
    setConfirmState(null);
    confirmResolver.current?.(accepted);
    confirmResolver.current = null;
  };

  const finishAlert = () => {
    setAlertState(null);
    alertResolver.current?.();
    alertResolver.current = null;
  };

  return (
    <DialogContext.Provider value={{ confirm, alert }}>
      {children}
      <ConfirmDialog
        open={!!confirmState?.open}
        title={confirmState?.title || 'Confirmation'}
        message={confirmState?.message || ''}
        confirmLabel={confirmState?.confirmLabel || 'Confirmer'}
        danger={!!confirmState?.danger}
        onClose={() => finishConfirm(false)}
        onConfirm={() => finishConfirm(true)}
      />
      <AlertDialog
        open={!!alertState?.open}
        title={alertState?.title || 'Information'}
        message={alertState?.message || ''}
        variant={alertState?.variant || 'info'}
        confirmLabel={alertState?.confirmLabel || 'OK'}
        onClose={finishAlert}
      />
    </DialogContext.Provider>
  );
}

export function useDialog() {
  const ctx = useContext(DialogContext);
  if (!ctx) {
    throw new Error('useDialog doit être utilisé dans DialogProvider');
  }
  return ctx;
}
