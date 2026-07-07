import { useState, useEffect } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { motion } from 'framer-motion';
import { KeyRound, AlertCircle, CheckCircle2, Mail, ArrowLeft } from 'lucide-react';
import PasswordField from '../components/ui/PasswordField';
import api from '../services/api';
import { getApiError, validateEmail, validatePassword, trimFields } from '../utils/validation';
import AuthLayout from '../components/auth/AuthLayout';

export default function ReinitialiserMotDePasse() {
  const [searchParams] = useSearchParams();
  const [form, setForm] = useState({ email: '', code: '', nouveauMotDePasse: '', confirm: '' });
  const [token, setToken] = useState('');
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [loading, setLoading] = useState(false);
  const [verifying, setVerifying] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    const emailParam = searchParams.get('email') || '';
    const tokenParam = searchParams.get('token') || '';
    if (emailParam) setForm(f => ({ ...f, email: emailParam }));
    if (tokenParam) {
      setToken(tokenParam);
      setVerifying(true);
      api.get('/auth/verifier-token-reinitialisation', { params: { token: tokenParam } })
        .then(res => {
          setForm(f => ({ ...f, email: res.data.email }));
          setSuccess('Lien valide. Saisissez le code reçu et votre nouveau mot de passe.');
        })
        .catch(err => setError(getApiError(err, 'Lien invalide ou expiré')))
        .finally(() => setVerifying(false));
    }
  }, [searchParams]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    const trimmed = trimFields(form, ['email', 'code', 'nouveauMotDePasse']);
    const emailErr = validateEmail(trimmed.email);
    if (emailErr) { setError(emailErr); return; }
    if (!trimmed.code || trimmed.code.length !== 6) { setError('Le code doit contenir 6 chiffres'); return; }
    const pwdErr = validatePassword(trimmed.nouveauMotDePasse);
    if (pwdErr) { setError(pwdErr); return; }
    if (trimmed.nouveauMotDePasse !== form.confirm) { setError('Les mots de passe ne correspondent pas'); return; }

    setLoading(true);
    setError(null);
    setSuccess(null);
    try {
      const res = await api.post('/auth/reinitialiser-mot-de-passe', {
        email: trimmed.email,
        code: trimmed.code,
        nouveauMotDePasse: trimmed.nouveauMotDePasse,
        token: token || undefined,
      });
      setSuccess(res.data.message);
      setTimeout(() => navigate('/login'), 2000);
    } catch (err) {
      setError(getApiError(err, 'Erreur lors de la réinitialisation'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout
      title="Réinitialiser le mot de passe"
      subtitle="Saisissez le code reçu et définissez un nouveau mot de passe sécurisé"
    >
      {verifying && (
        <div className="flex justify-center py-4">
          <div className="w-6 h-6 border-2 border-blue-500 border-t-transparent rounded-full animate-spin" />
        </div>
      )}

      {error && (
        <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }}
          className="flex items-center gap-2 bg-red-500/10 text-red-300 p-3 rounded-xl mb-5 text-sm border border-red-500/20">
          <AlertCircle size={16} /> {error}
        </motion.div>
      )}
      {success && (
        <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }}
          className="flex items-center gap-2 bg-emerald-500/10 text-emerald-300 p-3 rounded-xl mb-5 text-sm border border-emerald-500/20">
          <CheckCircle2 size={16} /> {success}
        </motion.div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="text-xs font-semibold text-slate-400 uppercase tracking-wider">E-mail</label>
          <div className="relative mt-2">
            <Mail size={16} className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-500" />
            <input
              type="email"
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
              required
              readOnly={!!token}
              className="w-full pl-11 pr-4 py-3.5 bg-white/5 border border-white/10 rounded-xl text-sm text-white outline-none focus:ring-2 focus:ring-blue-500/40 disabled:opacity-70"
            />
          </div>
        </div>

        <div>
          <label className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Code à 6 chiffres</label>
          <div className="relative mt-2">
            <KeyRound size={16} className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-500" />
            <input
              type="text"
              inputMode="numeric"
              maxLength={6}
              value={form.code}
              onChange={(e) => setForm({ ...form, code: e.target.value.replace(/\D/g, '') })}
              required
              placeholder="123456"
              className="w-full pl-11 pr-4 py-3.5 bg-white/5 border border-white/10 rounded-xl text-sm text-white outline-none focus:ring-2 focus:ring-blue-500/40 tracking-[0.3em] font-mono"
            />
          </div>
        </div>

        <PasswordField
          variant="auth"
          label="Nouveau mot de passe"
          value={form.nouveauMotDePasse}
          onChange={(e) => setForm({ ...form, nouveauMotDePasse: e.target.value })}
          required
          placeholder="8+ caractères, lettre et chiffre"
        />

        <PasswordField
          variant="auth"
          label="Confirmer"
          value={form.confirm}
          onChange={(e) => setForm({ ...form, confirm: e.target.value })}
          required
        />

        <motion.button
          whileHover={{ scale: 1.01 }}
          whileTap={{ scale: 0.99 }}
          type="submit"
          disabled={loading || verifying}
          className="w-full py-3.5 bg-gradient-to-r from-blue-600 to-violet-600 text-white rounded-xl font-semibold text-sm disabled:opacity-50 shadow-lg shadow-blue-600/25"
        >
          {loading ? 'Réinitialisation...' : 'Réinitialiser'}
        </motion.button>
      </form>

      <div className="flex flex-col gap-3 mt-6 text-center text-sm">
        <Link to="/mot-de-passe-oublie" className="text-blue-400 font-semibold hover:text-blue-300 transition">
          Demander un nouveau code
        </Link>
        <Link to="/login" className="text-slate-500 hover:text-slate-300 flex items-center justify-center gap-1 transition">
          <ArrowLeft size={14} /> Retour à la connexion
        </Link>
      </div>
    </AuthLayout>
  );
}
