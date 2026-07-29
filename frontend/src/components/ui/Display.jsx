import { useState, useEffect } from 'react';
import { motion } from 'framer-motion';
import { resolveAssetUrl } from '../../utils/assets';

export function initialsFromName(prenom, nom, fallback = '?') {
  const fromPerson = `${(prenom || '')[0] || ''}${(nom || '')[0] || ''}`.toUpperCase();
  if (fromPerson) return fromPerson;
  return fallback;
}

export function initialsFromLabel(label, fallback = 'MG') {
  if (!label || !label.trim()) return fallback;
  const parts = label.trim().split(/\s+/).filter(Boolean);
  if (parts.length >= 2) {
    return `${parts[0][0]}${parts[1][0]}`.toUpperCase();
  }
  return parts[0].slice(0, 2).toUpperCase();
}

export function Badge({ children, variant = 'default' }) {
  const styles = {
    default: 'bg-gray-100 text-gray-700',
    success: 'bg-emerald-100 text-emerald-700',
    warning: 'bg-amber-100 text-amber-700',
    danger: 'bg-red-100 text-red-700',
    info: 'bg-blue-100 text-blue-700',
  };
  return (
    <span className={`inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-semibold ${styles[variant]}`}>
      {children}
    </span>
  );
}

export function StatusBadge({ statut, styles }) {
  const s = styles[statut] || { bg: 'bg-gray-100', text: 'text-gray-600', dot: 'bg-gray-400' };
  return (
    <span className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-semibold ${s.bg} ${s.text}`}>
      {s.dot && <span className={`w-1.5 h-1.5 rounded-full ${s.dot}`} />}
      {statut}
    </span>
  );
}

export function Avatar({ src, prenom, nom, size = 'md' }) {
  const sizes = { sm: 'w-9 h-9 text-xs', md: 'w-11 h-11 text-sm', lg: 'w-16 h-16 text-lg', xl: 'w-20 h-20 text-xl' };
  const initials = initialsFromName(prenom, nom);
  const photoUrl = resolveAssetUrl(src);
  const [imgFailed, setImgFailed] = useState(false);

  useEffect(() => {
    setImgFailed(false);
  }, [src, photoUrl]);

  const fallback = (
    <div className={`${sizes[size]} rounded-2xl bg-gradient-to-br from-violet-800 to-gray-900 text-white flex items-center justify-center font-bold shadow-md ring-2 ring-white`}>
      {initials || '?'}
    </div>
  );

  if (!photoUrl || imgFailed) return fallback;

  return (
    <img
      src={photoUrl}
      alt=""
      className={`${sizes[size]} rounded-2xl object-cover ring-2 ring-white shadow-md`}
      onError={() => setImgFailed(true)}
    />
  );
}

export function LogoImage({ src, alt = '', className, fallbackClassName, companyName, crossOrigin }) {
  const url = src?.startsWith('http') || src?.startsWith('blob:') ? src : resolveAssetUrl(src);
  const [imgFailed, setImgFailed] = useState(false);
  const initials = initialsFromLabel(companyName, 'MG');

  useEffect(() => {
    setImgFailed(false);
  }, [src, url]);

  const fallback = (
    <div className={fallbackClassName || `${className || ''} bg-gradient-to-br from-violet-800 to-[#1a4a8e] text-white flex items-center justify-center font-bold`.trim()}>
      {initials}
    </div>
  );

  if (!url || imgFailed) return fallback;

  return (
    <img
      src={url}
      alt={alt}
      className={className}
      crossOrigin={crossOrigin}
      onError={() => setImgFailed(true)}
    />
  );
}

export function PhotoImage({ src, prenom, nom, className, fallbackClassName, crossOrigin }) {
  const url = resolveAssetUrl(src);
  const [imgFailed, setImgFailed] = useState(false);
  const initials = initialsFromName(prenom, nom);

  useEffect(() => {
    setImgFailed(false);
  }, [src, url]);

  const fallback = (
    <div className={fallbackClassName || `${className || ''} bg-gray-100 flex items-center justify-center font-bold text-[#1a4a8e]`.trim()}>
      {initials || '?'}
    </div>
  );

  if (!url || imgFailed) return fallback;

  return (
    <img
      src={url}
      alt=""
      className={className}
      crossOrigin={crossOrigin}
      onError={() => setImgFailed(true)}
    />
  );
}

export function StatCard({ label, value, icon: Icon, color = 'from-gray-800 to-black' }) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      className="bg-white/60 backdrop-blur-xl border border-white/20 rounded-2xl p-5 flex items-center gap-4"
    >
      <div className={`w-12 h-12 rounded-2xl bg-gradient-to-br ${color} flex items-center justify-center text-white shadow-lg`}>
        <Icon size={22} />
      </div>
      <div>
        <p className="text-2xl font-bold text-[#1D1D1F]">{value}</p>
        <p className="text-xs text-gray-500 font-medium">{label}</p>
      </div>
    </motion.div>
  );
}

export function EmptyState({ icon: Icon, title, description, action }) {
  return (
    <div className="flex flex-col items-center justify-center py-16 text-center">
      <div className="w-16 h-16 rounded-2xl bg-gray-100 flex items-center justify-center mb-4">
        <Icon size={28} className="text-gray-400" />
      </div>
      <h3 className="font-semibold text-[#1D1D1F] mb-1">{title}</h3>
      <p className="text-sm text-gray-500 max-w-sm mb-4">{description}</p>
      {action}
    </div>
  );
}

export function PageHeader({ title, subtitle, action }) {
  return (
    <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-8">
      <div>
        <h1 className="text-2xl font-bold text-[#1D1D1F]">{title}</h1>
        {subtitle && <p className="text-sm text-gray-500 mt-1">{subtitle}</p>}
      </div>
      {action}
    </div>
  );
}

export function TabBar({ tabs, active, onChange }) {
  return (
    <div className="flex gap-1 p-1 bg-white/50 backdrop-blur border border-black/5 rounded-2xl w-fit">
      {tabs.map(tab => (
        <button
          key={tab.key}
          onClick={() => onChange(tab.key)}
          className={`flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-medium transition ${
            active === tab.key ? 'bg-black text-white shadow-sm' : 'text-gray-600 hover:bg-white/80'
          }`}
        >
          {tab.icon && <tab.icon size={15} />}
          {tab.label}
          {tab.count != null && (
            <span className={`text-xs px-1.5 py-0.5 rounded-full ${
              active === tab.key ? 'bg-white/20' : 'bg-gray-200 text-gray-600'
            }`}>{tab.count}</span>
          )}
        </button>
      ))}
    </div>
  );
}
