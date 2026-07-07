import { useState } from 'react';
import { Eye, EyeOff, Lock } from 'lucide-react';

/**
 * Champ mot de passe avec bascule afficher/masquer.
 * variant="auth" : style sombre (pages connexion)
 * variant="default" : style clair (dashboard)
 */
export default function PasswordField({
  label,
  value,
  onChange,
  required,
  placeholder,
  disabled,
  variant = 'default',
  className = '',
  showForgotLink,
  forgotLink,
}) {
  const [visible, setVisible] = useState(false);

  const isAuth = variant === 'auth';

  const inputClass = isAuth
    ? 'w-full pl-11 pr-11 py-3.5 bg-white/5 border border-white/10 rounded-xl text-sm text-white outline-none focus:ring-2 focus:ring-blue-500/40 placeholder:text-slate-600'
    : `w-full px-4 py-2.5 pr-11 bg-white/60 border border-black/5 rounded-xl text-sm outline-none focus:ring-2 focus:ring-black/10 text-[#1D1D1F] placeholder-gray-400 transition disabled:bg-gray-50 disabled:text-gray-400 ${className}`;

  const labelClass = isAuth
    ? 'text-xs font-semibold text-slate-400 uppercase tracking-wider'
    : 'text-xs font-semibold text-gray-600';

  return (
    <div className="space-y-1.5">
      {(label || showForgotLink) && (
        <div className={`flex justify-between items-center ${isAuth ? '' : 'mb-0'}`}>
          {label && (
            <label className={labelClass}>
              {label}{required && <span className="text-red-500 ml-0.5">*</span>}
            </label>
          )}
          {showForgotLink && forgotLink}
        </div>
      )}
      <div className={`relative ${isAuth && label ? 'mt-2' : ''}`}>
        {isAuth && (
          <Lock size={16} className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-500 pointer-events-none" />
        )}
        <input
          type={visible ? 'text' : 'password'}
          value={value}
          onChange={onChange}
          required={required}
          placeholder={placeholder}
          disabled={disabled}
          autoComplete={isAuth ? 'current-password' : undefined}
          className={inputClass}
        />
        <button
          type="button"
          tabIndex={-1}
          onClick={() => setVisible(v => !v)}
          className={`absolute right-3 top-1/2 -translate-y-1/2 p-1 rounded-lg transition ${
            isAuth ? 'text-slate-500 hover:text-slate-300' : 'text-gray-400 hover:text-gray-600'
          }`}
          aria-label={visible ? 'Masquer le mot de passe' : 'Afficher le mot de passe'}
        >
          {visible ? <EyeOff size={16} /> : <Eye size={16} />}
        </button>
      </div>
    </div>
  );
}
