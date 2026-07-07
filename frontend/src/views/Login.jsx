import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { motion } from 'framer-motion';
import { LogIn, AlertCircle, Mail } from 'lucide-react';
import PasswordField from '../components/ui/PasswordField';
import api from '../services/api';
import { getApiError, validateEmail, trimFields } from '../utils/validation';
import { useAuth } from '../context/AuthContext';
import { useConfig } from '../context/ConfigContext';
import AuthLayout from '../components/auth/AuthLayout';

export default function Login() {
  const { companyName } = useConfig();
  const [form, setForm] = useState({ email: '', motDePasse: '' });
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    const trimmed = trimFields(form, ['email', 'motDePasse']);
    const emailErr = validateEmail(trimmed.email);
    if (emailErr) { setError(emailErr); return; }
    if (!trimmed.motDePasse) { setError('Le mot de passe est obligatoire'); return; }
    setLoading(true);
    setError(null);
    try {
      const res = await api.post('/auth/login', trimmed);
      login(res.data);
      navigate('/dashboard');
    } catch (err) {
      setError(getApiError(err, 'Identifiants invalides'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout
      title="Connexion"
      subtitle={`Accédez à votre espace Gestion RH — ${companyName}`}
    >
      {error && (
        <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }}
          className="flex items-center gap-2 bg-red-500/10 text-red-300 p-3 rounded-xl mb-5 text-sm border border-red-500/20">
          <AlertCircle size={16} /> {error}
        </motion.div>
      )}

      <form onSubmit={handleSubmit} className="space-y-5">
        <div>
          <label className="text-xs font-semibold text-slate-400 uppercase tracking-wider">E-mail professionnel</label>
          <div className="relative mt-2">
            <Mail size={16} className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-500" />
            <input
              type="email"
              value={form.email}
              onChange={(e) => setForm({ ...form, email: e.target.value })}
              required
              placeholder="vous@minerva.group"
              className="w-full pl-11 pr-4 py-3.5 bg-white/5 border border-white/10 rounded-xl text-sm text-white outline-none focus:ring-2 focus:ring-blue-500/40 placeholder:text-slate-600"
            />
          </div>
        </div>

        <PasswordField
          variant="auth"
          label="Mot de passe"
          value={form.motDePasse}
          onChange={(e) => setForm({ ...form, motDePasse: e.target.value })}
          required
          placeholder="••••••••"
          showForgotLink
          forgotLink={
            <Link to="/mot-de-passe-oublie" className="text-xs text-blue-400 hover:text-blue-300 transition">
              Mot de passe oublié ?
            </Link>
          }
        />

        <motion.button
          whileHover={{ scale: 1.01 }}
          whileTap={{ scale: 0.99 }}
          type="submit"
          disabled={loading}
          className="w-full flex items-center justify-center gap-2 py-3.5 bg-gradient-to-r from-blue-600 to-violet-600 text-white rounded-xl font-semibold text-sm hover:from-blue-500 hover:to-violet-500 transition disabled:opacity-50 shadow-lg shadow-blue-600/25"
        >
          <LogIn size={16} /> {loading ? 'Connexion...' : 'Se connecter'}
        </motion.button>
      </form>

      <p className="text-center text-sm text-slate-500 mt-6">
        Première connexion ?{' '}
        <Link to="/activer" className="text-blue-400 font-semibold hover:text-blue-300 transition">
          Activer mon compte
        </Link>
      </p>
    </AuthLayout>
  );
}
