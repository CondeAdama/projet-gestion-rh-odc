import { useState, useEffect } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { motion } from 'framer-motion';
import { KeyRound, AlertCircle, CheckCircle2, Mail } from 'lucide-react';
import PasswordField from '../components/ui/PasswordField';
import api from '../services/api';
import { getApiError, validateEmail, validatePassword, trimFields } from '../utils/validation';
import { useAuth } from '../context/AuthContext';
import AuthLayout from '../components/auth/AuthLayout';

export default function ActivationCompte() {
  const [searchParams] = useSearchParams();
  const [form, setForm] = useState({ email: '', code: '', motDePasse: '', confirmPwd: '' });
  const [token, setToken] = useState('');
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [loading, setLoading] = useState(false);
  const [verifying, setVerifying] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    const emailParam = searchParams.get('email') || '';
    const tokenParam = searchParams.get('token') || '';
    if (emailParam) setForm(f => ({ ...f, email: emailParam }));
    if (tokenParam) {
      setToken(tokenParam);
      setVerifying(true);
      api.get('/auth/verifier-token', { params: { token: tokenParam } })
        .then(res => {
          setForm(f => ({ ...f, email: res.data.email }));
          setSuccess('Lien valide. Saisissez le code reçu et définissez votre mot de passe.');
        })
        .catch(err => setError(getApiError(err, 'Lien invalide ou expiré')))
        .finally(() => setVerifying(false));
    }
  }, [searchParams]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    const trimmed = trimFields(form, ['email', 'code', 'motDePasse']);
    const emailErr = validateEmail(trimmed.email);
    const pwdErr = validatePassword(trimmed.motDePasse);
    if (emailErr || pwdErr) { setError(emailErr || pwdErr); return; }
    if (form.motDePasse !== form.confirmPwd) { setError('Les mots de passe ne correspondent pas'); return; }
    if (!/^\d{6}$/.test(trimmed.code)) { setError('Le code doit contenir 6 chiffres'); return; }

    setLoading(true);
    setError(null);
    try {
      const res = await api.post('/auth/activer-compte', {
        email: trimmed.email,
        code: trimmed.code,
        motDePasse: trimmed.motDePasse,
        token: token || undefined,
      });
      login(res.data);
      navigate('/dashboard');
    } catch (err) {
      setError(getApiError(err, 'Activation impossible'));
    } finally {
      setLoading(false);
    }
  };

  const renvoyer = async () => {
    const emailErr = validateEmail(form.email);
    if (emailErr) { setError(emailErr); return; }
    try {
      await api.post('/auth/renvoyer-code', { email: form.email.trim() });
      setSuccess('Un nouveau code et lien ont été envoyés.');
      setError(null);
    } catch (err) {
      setError(getApiError(err, 'Erreur'));
    }
  };

  return (
    <AuthLayout
      title="Activer mon compte"
      subtitle="Votre compte a été créé par le service RH. Validez-le avec le code reçu."
    >
      {verifying && (
        <p className="text-sm text-slate-400 mb-4 animate-pulse">Vérification du lien...</p>
      )}

      {error && (
        <div className="flex items-center gap-2 bg-red-500/10 text-red-300 p-3 rounded-xl mb-4 text-sm border border-red-500/20">
          <AlertCircle size={16} /> {error}
        </div>
      )}
      {success && (
        <div className="flex items-center gap-2 bg-emerald-500/10 text-emerald-300 p-3 rounded-xl mb-4 text-sm border border-emerald-500/20">
          <CheckCircle2 size={16} /> {success}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="text-xs font-semibold text-slate-400 uppercase tracking-wider">E-mail</label>
          <div className="relative mt-2">
            <Mail size={16} className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-500" />
            <input
              type="email"
              name="email"
              autoComplete="email"
              value={form.email}
              onChange={e => setForm({ ...form, email: e.target.value })}
              required
              readOnly={!!token}
              placeholder="vous@minerva.group"
              className="w-full pl-11 pr-4 py-3 bg-white/5 border border-white/10 rounded-xl text-sm text-white outline-none focus:ring-2 focus:ring-blue-500/40 disabled:opacity-60"
            />
          </div>
        </div>

        <div>
          <label className="text-xs font-semibold text-slate-400 uppercase tracking-wider">Code reçu (SMS / E-mail)</label>
          <p className="text-xs text-slate-500 mt-1 mb-2">Saisissez les 6 chiffres reçus (ex. 482917)</p>
          <input
            type="text"
            name="one-time-code"
            autoComplete="one-time-code"
            autoCorrect="off"
            autoCapitalize="off"
            spellCheck={false}
            value={form.code}
            onChange={e => setForm({ ...form, code: e.target.value.replace(/\D/g, '').slice(0, 6) })}
            required
            maxLength={6}
            minLength={6}
            pattern="[0-9]{6}"
            inputMode="numeric"
            placeholder="482917"
            className="w-full px-4 py-3 bg-white/5 border border-white/10 rounded-xl text-center text-2xl tracking-[0.35em] placeholder:tracking-normal font-mono text-white outline-none focus:ring-2 focus:ring-blue-500/40"
          />
        </div>

        <PasswordField
          variant="auth"
          label="Nouveau mot de passe"
          value={form.motDePasse}
          onChange={e => setForm({ ...form, motDePasse: e.target.value })}
          required
          placeholder="8+ caractères, lettre + chiffre"
        />

        <PasswordField
          variant="auth"
          label="Confirmer le mot de passe"
          value={form.confirmPwd}
          onChange={e => setForm({ ...form, confirmPwd: e.target.value })}
          required
          placeholder="Répétez le mot de passe"
        />

        <button
          type="submit"
          disabled={loading}
          className="w-full flex items-center justify-center gap-2 py-3.5 bg-gradient-to-r from-emerald-600 to-teal-600 text-white rounded-xl font-semibold text-sm disabled:opacity-50 shadow-lg shadow-emerald-600/20"
        >
          <KeyRound size={16} /> {loading ? 'Activation...' : 'Activer et se connecter'}
        </button>
      </form>

      <div className="flex flex-col gap-2 mt-5 text-center text-sm">
        <button onClick={renvoyer} className="text-slate-400 hover:text-white transition">
          Renvoyer le code d'activation
        </button>
        <Link to="/login" className="text-blue-400 font-semibold hover:text-blue-300">
          Retour à la connexion
        </Link>
      </div>
    </AuthLayout>
  );
}
