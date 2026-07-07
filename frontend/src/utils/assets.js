const API_BASE = import.meta.env.VITE_API_URL
  || (import.meta.env.DEV ? '/api/v2' : 'http://localhost:8080/api/v2');

export function resolveAssetUrl(path) {
  if (!path) return null;
  if (path.startsWith('http')) return path;
  return `${API_BASE}${path}`;
}

export { API_BASE };
