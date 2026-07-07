import { useState, useEffect, useRef } from 'react';
import { motion } from 'framer-motion';
import { User, Lock, Save, AlertCircle, CheckCircle2, Shield, Camera } from 'lucide-react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import { getApiError, validatePhone, validatePassword, validateImageFile } from '../utils/validation';
import { PageHeader } from '../components/ui/Display';
import { InputField } from '../components/ui/FormFields';
import PasswordField from '../components/ui/PasswordField';
import { Avatar } from '../components/ui/Display';

export default function MonProfil() {
  const { refreshProfil } = useAuth();
  const fileRef = useRef(null);
  const [profil, setProfil] = useState(null);
  const [form, setForm] = useState({ prenom: '', nom: '', telephone: '' });
  const [pwdForm, setPwdForm] = useState({ actuel: '', nouveau: '', confirm: '' });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [photoSaving, setPhotoSaving] = useState(false);
  const [pwdSaving, setPwdSaving] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [photoError, setPhotoError] = useState(null);
  const [pwdError, setPwdError] = useState(null);
  const [pwdSuccess, setPwdSuccess] = useState(null);

  const loadProfil = () => {
    return api.get('/auth/moi').then(res => {
      setProfil(res.data);
      setForm({
        prenom: res.data.prenom || '',
        nom: res.data.nom || '',
        telephone: res.data.telephone || '',
      });
    });
  };

  useEffect(() => {
    loadProfil().finally(() => setLoading(false));
  }, []);

  const handlePhotoChange = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    const imgErr = validateImageFile(file);
    if (imgErr) { setPhotoError(imgErr); return; }
    setPhotoSaving(true);
    setPhotoError(null);
    try {
      const fd = new FormData();
      fd.append('file', file);
      const res = await api.post('/auth/moi/photo', fd, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      setProfil(res.data);
      await refreshProfil();
    } catch (err) {
      setPhotoError(getApiError(err, 'Erreur lors du téléversement'));
    } finally {
      setPhotoSaving(false);
      if (fileRef.current) fileRef.current.value = '';
    }
  };

  const handleSaveProfil = async (e) => {
    e.preventDefault();
    const phoneErr = validatePhone(form.telephone);
    if (phoneErr) { setError(phoneErr); return; }
    setSaving(true);
    setError(null);
    setSuccess(null);
    try {
      const res = await api.put('/auth/moi', form);
      setProfil(res.data);
      await refreshProfil();
      setSuccess('Profil mis à jour.');
    } catch (err) {
      setError(getApiError(err, 'Erreur'));
    } finally {
      setSaving(false);
    }
  };

  const handleChangePwd = async (e) => {
    e.preventDefault();
    const pwdErr = validatePassword(pwdForm.nouveau);
    if (pwdErr) { setPwdError(pwdErr); return; }
    if (pwdForm.nouveau !== pwdForm.confirm) { setPwdError('Les mots de passe ne correspondent pas'); return; }
    setPwdSaving(true);
    setPwdError(null);
    setPwdSuccess(null);
    try {
      await api.put('/auth/moi/mot-de-passe', {
        motDePasseActuel: pwdForm.actuel,
        nouveauMotDePasse: pwdForm.nouveau,
      });
      setPwdForm({ actuel: '', nouveau: '', confirm: '' });
      setPwdSuccess('Mot de passe modifié avec succès.');
    } catch (err) {
      setPwdError(getApiError(err, 'Erreur'));
    } finally {
      setPwdSaving(false);
    }
  };

  if (loading) {
    return <div className="flex justify-center py-20"><div className="w-8 h-8 border-2 border-blue-600 border-t-transparent rounded-full animate-spin" /></div>;
  }

  const roles = profil?.roles ? [...profil.roles] : [];

  return (
    <div className="space-y-8 max-w-3xl">
      <PageHeader title="Mon profil" subtitle="Gérez vos informations personnelles et votre sécurité" />

      <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }}
        className="glass-card p-6 flex flex-col sm:flex-row items-center gap-5">
        <div className="relative group">
          <Avatar src={profil?.photoUrl} prenom={profil?.prenom} nom={profil?.nom} size="lg" />
          <button
            type="button"
            onClick={() => fileRef.current?.click()}
            disabled={photoSaving}
            className="absolute inset-0 flex items-center justify-center bg-black/40 rounded-2xl opacity-0 group-hover:opacity-100 transition-opacity"
            aria-label="Changer la photo"
          >
            {photoSaving ? (
              <div className="w-6 h-6 border-2 border-white border-t-transparent rounded-full animate-spin" />
            ) : (
              <Camera size={24} className="text-white" />
            )}
          </button>
          <input ref={fileRef} type="file" accept="image/jpeg,image/png,image/webp,image/gif" className="hidden" onChange={handlePhotoChange} />
        </div>
        <div className="text-center sm:text-left">
          <h2 className="text-xl font-bold">{profil?.nomComplet}</h2>
          <p className="text-sm text-gray-500">{profil?.email}</p>
          <p className="text-xs text-gray-400 mt-1">{profil?.matricule} · {profil?.posteLibelle}</p>
          {photoError && <p className="text-xs text-red-500 mt-2 flex items-center gap-1 justify-center sm:justify-start"><AlertCircle size={12} />{photoError}</p>}
          <button
            type="button"
            onClick={() => fileRef.current?.click()}
            disabled={photoSaving}
            className="mt-3 text-xs text-blue-600 font-semibold hover:text-blue-700 transition"
          >
            {photoSaving ? 'Téléversement...' : 'Modifier la photo'}
          </button>
          <div className="flex gap-2 mt-2 flex-wrap justify-center sm:justify-start">
            {roles.map(r => (
              <span key={r} className="text-[10px] px-2 py-0.5 rounded-full bg-blue-100 text-blue-700 font-bold flex items-center gap-1">
                <Shield size={10} /> {r}
              </span>
            ))}
          </div>
        </div>
      </motion.div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <form onSubmit={handleSaveProfil} className="glass-card p-6 space-y-4">
          <h3 className="font-semibold flex items-center gap-2"><User size={18} /> Informations personnelles</h3>
          {error && <div className="text-red-600 text-sm flex gap-2"><AlertCircle size={14} />{error}</div>}
          {success && <div className="text-emerald-600 text-sm flex gap-2"><CheckCircle2 size={14} />{success}</div>}
          <InputField label="Prénom" value={form.prenom} onChange={e => setForm({ ...form, prenom: e.target.value })} placeholder="Prénom" />
          <InputField label="Nom" value={form.nom} onChange={e => setForm({ ...form, nom: e.target.value })} placeholder="Nom" />
          <InputField label="Téléphone" value={form.telephone} onChange={e => setForm({ ...form, telephone: e.target.value })} placeholder="+224 620 00 00 00" />
          <p className="text-xs text-gray-400">E-mail et matricule sont gérés par le service RH.</p>
          <button type="submit" disabled={saving} className="flex items-center gap-2 px-5 py-2.5 bg-black text-white rounded-xl text-sm font-semibold disabled:opacity-50 transition hover:opacity-90">
            <Save size={14} /> {saving ? 'Enregistrement...' : 'Enregistrer'}
          </button>
        </form>

        <form onSubmit={handleChangePwd} className="glass-card p-6 space-y-4">
          <h3 className="font-semibold flex items-center gap-2"><Lock size={18} /> Mot de passe</h3>
          {pwdError && <div className="text-red-600 text-sm flex gap-2"><AlertCircle size={14} />{pwdError}</div>}
          {pwdSuccess && <div className="text-emerald-600 text-sm flex gap-2"><CheckCircle2 size={14} />{pwdSuccess}</div>}
          <PasswordField label="Mot de passe actuel" value={pwdForm.actuel} onChange={e => setPwdForm({ ...pwdForm, actuel: e.target.value })} required />
          <PasswordField label="Nouveau mot de passe" value={pwdForm.nouveau} onChange={e => setPwdForm({ ...pwdForm, nouveau: e.target.value })} placeholder="8+ caractères" required />
          <PasswordField label="Confirmer" value={pwdForm.confirm} onChange={e => setPwdForm({ ...pwdForm, confirm: e.target.value })} required />
          <button type="submit" disabled={pwdSaving} className="flex items-center gap-2 px-5 py-2.5 bg-[#1e3a5f] text-white rounded-xl text-sm font-semibold disabled:opacity-50 transition hover:opacity-90">
            <Lock size={14} /> {pwdSaving ? 'Modification...' : 'Changer le mot de passe'}
          </button>
        </form>
      </div>
    </div>
  );
}
