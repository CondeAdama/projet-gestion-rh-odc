export const PHONE_PATTERN = /^\+?[0-9\s\-]{8,20}$/;
export const MATRICULE_PATTERN = /^[A-Z0-9\-_]{3,50}$/i;
export const MAX_IMAGE_SIZE = 2 * 1024 * 1024;
export const ALLOWED_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];

export function trimFields(obj, keys) {
  const out = { ...obj };
  keys.forEach(k => { if (typeof out[k] === 'string') out[k] = out[k].trim(); });
  return out;
}

export function validatePhone(phone) {
  if (!phone?.trim()) return 'Le téléphone est obligatoire';
  if (!PHONE_PATTERN.test(phone.trim())) return 'Format de téléphone invalide';
  return null;
}

export function validateEmail(email) {
  if (!email?.trim()) return 'L\'e-mail est obligatoire';
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim())) return 'Format d\'e-mail invalide';
  return null;
}

export function validatePassword(password) {
  if (!password) return 'Le mot de passe est obligatoire';
  if (password.length < 8) return 'Le mot de passe doit contenir au moins 8 caractères';
  if (!/(?=.*[A-Za-z])(?=.*\d)/.test(password)) return 'Le mot de passe doit contenir une lettre et un chiffre';
  return null;
}

export function validateDateRange(debut, fin) {
  if (!debut || !fin) return 'Les dates sont obligatoires';
  if (fin < debut) return 'La date de fin doit être postérieure à la date de début';
  return null;
}

export function validateContratDates(typeContrat, dateDebut, dateFin) {
  if (!dateDebut) return 'La date de début est obligatoire';
  if (typeContrat === 'CDD' && !dateFin) return 'La date de fin est obligatoire pour un CDD';
  if (dateFin && dateFin < dateDebut) return 'La date de fin doit être postérieure à la date de début';
  return null;
}

export function validatePositiveNumber(value, label) {
  const n = parseFloat(value);
  if (isNaN(n) || n < 0) return `${label} doit être un nombre positif ou nul`;
  return null;
}

export function validateImageFile(file) {
  if (!file) return null;
  if (!ALLOWED_IMAGE_TYPES.includes(file.type)) return 'Format non autorisé (JPG, PNG, WEBP, GIF)';
  if (file.size > MAX_IMAGE_SIZE) return 'Le fichier ne doit pas dépasser 2 Mo';
  return null;
}

export function getApiError(err, fallback = 'Une erreur est survenue') {
  const data = err?.response?.data;
  if (!data) return fallback;
  if (data.errors && typeof data.errors === 'object') {
    const first = Object.values(data.errors)[0];
    if (first) return first;
  }
  return data.message || fallback;
}
