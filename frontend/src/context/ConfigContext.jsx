import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import api from '../services/api';
import { resolveAssetUrl } from '../utils/assets';

const ConfigContext = createContext(null);

const DEFAULT_CONFIG = {
  nomEntreprise: 'MINERVA GROUP',
};

export function ConfigProvider({ children }) {
  const [config, setConfig] = useState(DEFAULT_CONFIG);
  const [loading, setLoading] = useState(true);

  const refresh = useCallback(async () => {
    setLoading(true);
    try {
      const res = await api.get('/configuration');
      setConfig(res.data);
    } catch {
      setConfig(DEFAULT_CONFIG);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    refresh();
  }, [refresh]);

  const logoUrl = resolveAssetUrl(config?.logoUrl);
  const companyName = config?.nomEntreprise || DEFAULT_CONFIG.nomEntreprise;

  return (
    <ConfigContext.Provider value={{ config, logoUrl, companyName, loading, refresh }}>
      {children}
    </ConfigContext.Provider>
  );
}

export function useConfig() {
  const ctx = useContext(ConfigContext);
  if (!ctx) throw new Error('useConfig doit être utilisé dans ConfigProvider');
  return ctx;
}
