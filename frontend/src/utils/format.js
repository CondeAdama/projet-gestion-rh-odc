export function formatGNF(amount) {
  if (amount == null) return '—';
  return new Intl.NumberFormat('fr-GN', { style: 'decimal', maximumFractionDigits: 0 }).format(amount) + ' GNF';
}

export function formatDate(date) {
  if (!date) return '—';
  return new Date(date).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' });
}

/** Affiche une date-heure ISO (ex. visites) */
export function formatDateTime(value) {
  if (!value) return '—';
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return '—';
  return d.toLocaleString('fr-FR', {
    day: '2-digit', month: 'short', year: 'numeric',
    hour: '2-digit', minute: '2-digit',
  });
}

/** Heure seule — accepte ISO datetime ou "HH:mm:ss" */
export function formatTime(value) {
  if (!value) return '—';
  if (typeof value === 'string' && /^\d{2}:\d{2}/.test(value)) {
    return value.slice(0, 5);
  }
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return '—';
  return d.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
}

export function getInitials(prenom, nom) {
  return `${(prenom || '')[0] || ''}${(nom || '')[0] || ''}`.toUpperCase();
}

export const STATUT_EMPLOI_STYLES = {
  ACTIF: { bg: 'bg-emerald-100', text: 'text-emerald-700', dot: 'bg-emerald-500' },
  SUSPENDU: { bg: 'bg-amber-100', text: 'text-amber-700', dot: 'bg-amber-500' },
  LICENCIE: { bg: 'bg-red-100', text: 'text-red-700', dot: 'bg-red-500' },
};

export const STATUT_CONTRAT_STYLES = {
  ACTIF: { bg: 'bg-emerald-100', text: 'text-emerald-700' },
  ARCHIVE: { bg: 'bg-gray-100', text: 'text-gray-600' },
  RESILIE: { bg: 'bg-red-100', text: 'text-red-700' },
};

export const STATUT_CONGE_STYLES = {
  EN_ATTENTE: { bg: 'bg-amber-100', text: 'text-amber-700', dot: 'bg-amber-500' },
  APPROUVE: { bg: 'bg-emerald-100', text: 'text-emerald-700', dot: 'bg-emerald-500' },
  REFUSE: { bg: 'bg-red-100', text: 'text-red-700', dot: 'bg-red-500' },
};

export const TYPE_CONGE_LABELS = {
  ANNUEL: 'Congé annuel',
  PAYE: 'Congé payé',
  MALADIE: 'Maladie',
  MATERNITE: 'Maternité',
  SANS_SOLDE: 'Sans solde',
};

export const TYPE_CONGE_COLORS = {
  ANNUEL: 'from-sky-500 to-blue-600',
  PAYE: 'from-emerald-500 to-teal-600',
  MALADIE: 'from-orange-500 to-amber-600',
  MATERNITE: 'from-pink-500 to-rose-600',
  SANS_SOLDE: 'from-gray-500 to-slate-600',
};

export const TYPE_CONTRAT_COLORS = {
  CDI: 'from-blue-600 to-indigo-600',
  CDD: 'from-violet-600 to-purple-600',
  STAGE: 'from-teal-600 to-cyan-600',
};

export const STATUT_PRESENCE_STYLES = {
  EN_REGLE: { bg: 'bg-emerald-100', text: 'text-emerald-700', label: 'En règle' },
  RETARD: { bg: 'bg-amber-100', text: 'text-amber-700', label: 'Retard' },
  REFUSE: { bg: 'bg-red-100', text: 'text-red-700', label: 'Refusé' },
};

export const MOIS_LABELS = [
  'Janvier', 'Février', 'Mars', 'Avril', 'Mai', 'Juin',
  'Juillet', 'Août', 'Septembre', 'Octobre', 'Novembre', 'Décembre',
];
