/** Ignore les QR contenant une URL (ex. QR de transfert /scan) — ne garder que les matricules. */
export function normaliserMatriculeScan(decoded) {
  const clean = (decoded || '').replace(/\s+/g, '').trim();
  if (!clean) return '';
  if (/^https?:\/\//i.test(clean) || clean.includes('/scan')) {
    return '';
  }
  return clean;
}
