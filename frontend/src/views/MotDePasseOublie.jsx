import { useState } from 'react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { Mail, AlertCircle, CheckCircle2, ArrowLeft } from 'lucide-react';
import api from '../services/api';
import { getApiError, validateEmail, trimFields } from '../utils/validation';
import { useConfig } from '../context/ConfigContext';
import AuthLayout from '../components/auth/AuthLayout';

export default function MotDePasseOublie() {
  const { companyName } = useConfig();
  const [email, setEmail] = useState('');
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    const trimmed = trimFields({ email }, ['email']).email;
    const emailErr = validateEmail(trimmed);
    if (emailErr) { setError(emailErr); return; }
    setLoading(true);
    setError(null);
    setSuccess(null);
    try {
      const res = await api.post('/auth/mot-de-passe-oublie', { email: trimmed });
      setSuccess(res.data.message);
    } catch (err) {
      setError(getApiError(err, 'Erreur lors de la demande'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <AuthLayout
      title="Mot de passe oublié"
      subtitle={`Recevez un code sécurisé par e-mail ou SMS — ${companyName}`}
    >
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

      <form onSubmit={handleSubmit} className="space-y-5">
        <div>
          <label className="text-xs font-semibold text-slate-400 uppercase tracking-wider">E-mail professionnel</label>
          <div className="relative mt-2">
            <Mail size={16} className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-500" />
            <input
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              placeholder="vous@minerva.group"
              className="w-full pl-11 pr-4 py-3.5 bg-white/5 border border-white/10 rounded-xl text-sm text-white outline-none focus:ring-2 focus:ring-blue-500/40 placeholder:text-slate-600"
            />
          </div>
        </div>

        <motion.button
          whileHover={{ scale: 1.01 }}
          whileTap={{ scale: 0.99 }}
          type="submit"
          disabled={loading}
          className="w-full py-3.5 bg-gradient-to-r from-blue-600 to-violet-600 text-white rounded-xl font-semibold text-sm hover:from-blue-500 hover:to-violet-500 transition disabled:opacity-50 shadow-lg shadow-blue-600/25"
        >
          {loading ? 'Envoi...' : 'Envoyer le code'}
        </motion.button>
      </form>

      <div className="flex flex-col gap-3 mt-6 text-center text-sm">
        <Link to="/reinitialiser-mot-de-passe" className="text-blue-400 font-semibold hover:text-blue-300 transition">
          J'ai déjà reçu mon code
        </Link>
        <Link to="/login" className="text-slate-500 hover:text-slate-300 flex items-center justify-center gap-1 transition">
          <ArrowLeft size={14} /> Retour à la connexion
        </Link>
      </div>
    </AuthLayout>
  );
}
