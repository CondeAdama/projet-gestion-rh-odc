const PROD_API = 'https://minerva-rh-api.onrender.com/api/v2';

const API_BASE = import.meta.env.VITE_API_URL
  || (import.meta.env.DEV ? '/api/v2' : PROD_API);

export function resolveAssetUrl(path) {
  if (!path) return null;
  if (path.startsWith('http')) return path;
  return `${API_BASE}${path}`;
}

export { API_BASE };
