import { useState, useEffect } from 'react';
import {
  Settings, Save, RefreshCw, Building2, Upload, CheckCircle2, AlertCircle, Image,
  Mail, MessageSquare, Send, FileText, RotateCcw
} from 'lucide-react';
import api from '../services/api';
import { useConfig } from '../context/ConfigContext';
import { getApiError, validateImageFile, validatePhone, validateEmail } from '../utils/validation';
import { SMS_PROVIDERS, getSmsProvider, getNestedValue, setNestedValue } from '../utils/smsProviders';
import { PageHeader } from '../components/ui/Display';
import { InputField, SelectField } from '../components/ui/FormFields';
import PasswordField from '../components/ui/PasswordField';

const EMPTY_ENTREPRISE = {
  nomEntreprise: '', adresse: '', telephone: '', email: '', nif: '',
  numeroCnss: '', tauxCnss: '5', tauxRts: '10', slogan: '', devise: 'GNF',
};

const EMPTY_NOTIF = {
  modeEnvoi: 'MOCK',
  appUrl: 'http://localhost:5173',
  emailActif: false,
  smtpHost: 'smtp.gmail.com',
  smtpPort: '587',
  smtpUsername: '',
  smtpPassword: '',
  smtpFromEmail: '',
  smtpFromName: '',
  smtpAuth: true,
  smtpStarttls: true,
  smsActif: false,
  smsProvider: 'TWILIO',
  smsAccountSid: '',
  smsApiSecret: '',
  smsSenderId: '',
  smsExtra: {},
  modeles: {},
};

const MODELES_META = [
  { key: 'ACTIVATION_COMPTE', label: 'Activation de compte', vars: ['entreprise', 'code', 'lien'] },
  { key: 'REINITIALISATION_MDP', label: 'Réinitialisation mot de passe', vars: ['entreprise', 'code', 'lien'] },
  { key: 'CODE_CONFIRMATION', label: 'Code de confirmation', vars: ['entreprise', 'code'] },
  { key: 'CONGE_APPROUVE', label: 'Congé approuvé', vars: ['entreprise', 'prenom', 'nom', 'typeConge', 'dateDebut', 'dateFin', 'commentaireRh'] },
  { key: 'CONGE_REFUSE', label: 'Congé refusé', vars: ['entreprise', 'prenom', 'nom', 'typeConge', 'dateDebut', 'dateFin', 'commentaireRh'] },
  { key: 'CREATION_CONTRAT', label: 'Création de contrat', vars: ['entreprise', 'prenom', 'nom', 'typeContrat', 'salaireBase', 'dateDebut'] },
  { key: 'LICENCIEMENT', label: 'Licenciement', vars: ['entreprise', 'prenom', 'nom'] },
  { key: 'SUSPENSION_COMPTE', label: 'Suspension de compte', vars: ['entreprise', 'prenom', 'nom'] },
  { key: 'TEST_EMAIL', label: 'E-mail de test (configuration)', vars: ['entreprise'] },
  { key: 'TEST_SMS', label: 'SMS de test (configuration)', vars: ['entreprise'] },
];

export default function GestionConfiguration() {
  const { config, refresh, logoUrl } = useConfig();
  const [tab, setTab] = useState('entreprise');
  const [form, setForm] = useState(EMPTY_ENTREPRISE);
  const [notif, setNotif] = useState(EMPTY_NOTIF);
  const [smtpConfigured, setSmtpConfigured] = useState(false);
  const [smsConfigured, setSmsConfigured] = useState(false);
  const [logoFile, setLogoFile] = useState(null);
  const [saving, setSaving] = useState(false);
  const [savingNotif, setSavingNotif] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [testingEmail, setTestingEmail] = useState(false);
  const [testingSms, setTestingSms] = useState(false);
  const [testEmail, setTestEmail] = useState('');
  const [testPhone, setTestPhone] = useState('');
  const [success, setSuccess] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (config) {
      setForm({
        nomEntreprise: config.nomEntreprise || '',
        adresse: config.adresse || '',
        telephone: config.telephone || '',
        email: config.email || '',
        nif: config.nif || '',
        numeroCnss: config.numeroCnss || '',
        tauxCnss: config.tauxCnss != null ? String(config.tauxCnss) : '5',
        tauxRts: config.tauxRts != null ? String(config.tauxRts) : '10',
        slogan: config.slogan || '',
        devise: config.devise || 'GNF',
      });
    }
  }, [config]);

  const loadNotifications = async () => {
    try {
      const res = await api.get('/configuration/notifications');
      const d = res.data;
      setNotif({
        modeEnvoi: d.modeEnvoi || 'MOCK',
        appUrl: d.appUrl || 'http://localhost:5173',
        emailActif: !!d.emailActif,
        smtpHost: d.smtpHost || '',
        smtpPort: d.smtpPort != null ? String(d.smtpPort) : '587',
        smtpUsername: d.smtpUsername || '',
        smtpPassword: '',
        smtpFromEmail: d.smtpFromEmail || '',
        smtpFromName: d.smtpFromName || '',
        smtpAuth: d.smtpAuth !== false,
        smtpStarttls: d.smtpStarttls !== false,
        smsActif: !!d.smsActif,
        smsProvider: d.smsProvider || 'TWILIO',
        smsAccountSid: d.smsAccountSid || '',
        smsApiSecret: '',
        smsSenderId: d.smsSenderId || '',
        smsExtra: d.smsExtra || {},
        modeles: d.modeles || {},
      });
      setSmtpConfigured(!!d.smtpPasswordConfigure);
      setSmsConfigured(!!d.smsApiSecretConfigure);
      setTestEmail(config?.email || '');
      setTestPhone(config?.telephone || '');
    } catch (err) {
      setError(getApiError(err, 'Impossible de charger la configuration des notifications'));
    }
  };

  useEffect(() => { loadNotifications(); }, []);

  const f = (key) => ({ value: form[key], onChange: e => setForm({ ...form, [key]: e.target.value }) });
  const n = (key) => ({ value: notif[key], onChange: e => setNotif({ ...notif, [key]: e.target.value }) });

  const updateModele = (key, field, value) => {
    setNotif(prev => ({
      ...prev,
      modeles: {
        ...prev.modeles,
        [key]: { ...(prev.modeles[key] || {}), [field]: value },
      },
    }));
  };

  const handleResetModeles = async () => {
    if (!window.confirm('Réinitialiser tous les modèles aux textes par défaut ?')) return;
    setError(null);
    setSuccess(null);
    try {
      const res = await api.post('/configuration/notifications/reinitialiser-modeles');
      setNotif(prev => ({ ...prev, modeles: res.data.modeles || {} }));
      setSuccess('Modèles de messages réinitialisés.');
    } catch (err) {
      setError(getApiError(err, 'Impossible de réinitialiser les modèles.'));
    }
  };

  const handleSave = async (e) => {
    e.preventDefault();
    if (!form.nomEntreprise.trim()) { setError('Le nom de l\'entreprise est obligatoire'); return; }
    const phoneErr = form.telephone ? validatePhone(form.telephone) : null;
    const emailErr = form.email ? validateEmail(form.email) : null;
    if (phoneErr || emailErr) { setError(phoneErr || emailErr); return; }
    setSaving(true);
    setError(null);
    setSuccess(null);
    try {
      await api.put('/configuration', {
        ...form,
        nomEntreprise: form.nomEntreprise.trim(),
        email: form.email.trim(),
        telephone: form.telephone.replace(/\s+/g, ''),
        numeroCnss: form.numeroCnss.trim(),
        tauxCnss: parseFloat(form.tauxCnss) || 5,
        tauxRts: parseFloat(form.tauxRts) || 10,
        logoUrl: config?.logoUrl,
      });
      await refresh();
      setSuccess('Configuration entreprise enregistrée.');
    } catch (err) {
      setError(getApiError(err, 'Erreur lors de la sauvegarde.'));
    } finally {
      setSaving(false);
    }
  };

  const handleSaveNotifications = async (e) => {
    e.preventDefault();
    setSavingNotif(true);
    setError(null);
    setSuccess(null);
    try {
      const payload = {
        ...notif,
        smtpPort: parseInt(notif.smtpPort, 10) || 587,
        smtpPassword: notif.smtpPassword || undefined,
        smsApiSecret: notif.smsApiSecret || undefined,
        smsExtra: notif.smsExtra,
        modeles: notif.modeles,
      };
      await api.put('/configuration/notifications', payload);
      await loadNotifications();
      setSuccess('Configuration e-mail / SMS enregistrée.');
    } catch (err) {
      setError(getApiError(err, 'Erreur lors de la sauvegarde des notifications.'));
    } finally {
      setSavingNotif(false);
    }
  };

  const handleTestEmail = async () => {
    const emailErr = validateEmail(testEmail);
    if (emailErr) { setError(emailErr); return; }
    setTestingEmail(true);
    setError(null);
    try {
      const res = await api.post('/configuration/notifications/test-email', { email: testEmail });
      setSuccess(res.data.message);
    } catch (err) {
      setError(getApiError(err, 'Échec du test e-mail'));
    } finally {
      setTestingEmail(false);
    }
  };

  const handleTestSms = async () => {
    const phoneErr = validatePhone(testPhone);
    if (phoneErr) { setError(phoneErr); return; }
    setTestingSms(true);
    setError(null);
    try {
      const res = await api.post('/configuration/notifications/test-sms', { telephone: testPhone.replace(/\s+/g, '') });
      setSuccess(res.data.message);
    } catch (err) {
      setError(getApiError(err, 'Échec du test SMS'));
    } finally {
      setTestingSms(false);
    }
  };

  const handleLogoUpload = async () => {
    if (!logoFile) return;
    const fileErr = validateImageFile(logoFile);
    if (fileErr) { setError(fileErr); return; }
    setUploading(true);
    setError(null);
    try {
      const fd = new FormData();
      fd.append('file', logoFile);
      await api.post('/configuration/upload-logo', fd, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      setLogoFile(null);
      await refresh();
      setSuccess('Logo mis à jour.');
    } catch (err) {
      setError(getApiError(err, 'Erreur lors du téléversement du logo.'));
    } finally {
      setUploading(false);
    }
  };

  const previewLogo = logoFile ? URL.createObjectURL(logoFile) : logoUrl;
  const tabs = [
    { key: 'entreprise', label: 'Entreprise', icon: Building2 },
    { key: 'notifications', label: 'E-mail & SMS', icon: Mail },
  ];

  return (
    <div className="space-y-8 max-w-4xl">
      <PageHeader
        title="Configuration"
        subtitle="Identité de l'entreprise, paramètres de paie et notifications."
        action={
          <button onClick={() => { refresh(); loadNotifications(); }} className="p-3 bg-white/60 border border-black/5 hover:bg-black/5 rounded-xl">
            <RefreshCw size={18} className="text-gray-600" />
          </button>
        }
      />

      <div className="flex flex-wrap gap-2">
        {tabs.map(t => (
          <button key={t.key} onClick={() => setTab(t.key)}
            className={`flex items-center gap-2 px-4 py-2 rounded-xl text-sm font-medium transition ${tab === t.key ? 'bg-black text-white' : 'bg-white/50 text-gray-600 hover:bg-gray-100'}`}>
            <t.icon size={16} /> {t.label}
          </button>
        ))}
      </div>

      {success && (
        <div className="flex items-center gap-2 bg-emerald-50 text-emerald-700 p-4 rounded-xl text-sm border border-emerald-100">
          <CheckCircle2 size={18} /> {success}
        </div>
      )}
      {error && (
        <div className="flex items-center gap-2 bg-red-50 text-red-600 p-4 rounded-xl text-sm border border-red-100">
          <AlertCircle size={18} /> {error}
        </div>
      )}

      {tab === 'entreprise' && (
        <div className="grid md:grid-cols-2 gap-8">
          <form onSubmit={handleSave} className="bg-white/60 border border-white/20 rounded-2xl p-6 shadow-xl space-y-4">
            <h2 className="font-semibold flex items-center gap-2"><Building2 size={18} /> Informations générales</h2>
            <InputField label="Nom de l'entreprise" required {...f('nomEntreprise')} placeholder="MINERVA GROUP" />
            <InputField label="Adresse" {...f('adresse')} />
            <InputField label="Téléphone" {...f('telephone')} placeholder="+224 620 00 00 00" />
            <InputField label="E-mail contact" type="email" {...f('email')} placeholder="contact@minerva.group" />
            <InputField label="NIF" {...f('nif')} />
            <InputField label="Slogan" {...f('slogan')} />
            <InputField label="Devise" {...f('devise')} placeholder="GNF" />
            <h2 className="font-semibold pt-2 border-t border-black/5">Paie & fiscalité</h2>
            <InputField label="N° employeur CNSS" {...f('numeroCnss')} />
            <div className="grid grid-cols-2 gap-4">
              <InputField label="Taux CNSS (%)" type="number" min="0" max="100" step="0.01" {...f('tauxCnss')} />
              <InputField label="Taux RTS (%)" type="number" min="0" max="100" step="0.01" {...f('tauxRts')} />
            </div>
            <button type="submit" disabled={saving} className="flex items-center gap-2 px-6 py-2.5 bg-black text-white rounded-xl text-sm font-semibold disabled:opacity-50">
              <Save size={16} /> {saving ? 'Enregistrement...' : 'Enregistrer'}
            </button>
          </form>

          <div className="bg-white/60 border border-white/20 rounded-2xl p-6 shadow-xl space-y-5">
            <h2 className="font-semibold flex items-center gap-2"><Image size={18} /> Logo entreprise</h2>
            <div className="flex items-center justify-center p-6 bg-gray-50 rounded-2xl border border-dashed border-gray-200 min-h-[140px]">
              {previewLogo ? (
                <img src={previewLogo} alt="Logo" className="max-h-24 max-w-full object-contain" />
              ) : (
                <div className="text-center text-gray-400">
                  <Settings size={32} className="mx-auto mb-2 opacity-40" />
                  <p className="text-xs">Aucun logo</p>
                </div>
              )}
            </div>
            <input type="file" accept="image/jpeg,image/png,image/webp,image/gif"
              onChange={e => { const file = e.target.files[0]; const err = validateImageFile(file); if (err) { setError(err); return; } setLogoFile(file); setError(null); }}
              className="text-xs text-gray-500 w-full" />
            <button onClick={handleLogoUpload} disabled={!logoFile || uploading}
              className="w-full flex items-center justify-center gap-2 py-3 bg-[#1a4a8e] text-white rounded-xl text-sm font-semibold disabled:opacity-50">
              {uploading ? <RefreshCw className="animate-spin" size={16} /> : <Upload size={16} />}
              Téléverser le logo
            </button>
          </div>
        </div>
      )}

      {tab === 'notifications' && (
        <form onSubmit={handleSaveNotifications} className="space-y-6">
          <div className="bg-white/60 border border-white/20 rounded-2xl p-6 shadow-xl space-y-4">
            <h2 className="font-semibold">Mode d'envoi</h2>
            <SelectField label="Mode" value={notif.modeEnvoi} onChange={e => setNotif({ ...notif, modeEnvoi: e.target.value })}
              options={[
                { value: 'MOCK', label: 'Simulation (console + journal)' },
                { value: 'LIVE', label: 'Envoi réel (SMTP + SMS)' },
              ]} />
            <InputField label="URL de l'application (liens d'activation)" {...n('appUrl')} placeholder="http://localhost:5173" />
            <p className="text-xs text-gray-400">
              En mode <strong>Simulation</strong>, les messages sont journalisés sans envoi réel.
              En mode <strong>Envoi réel</strong>, configurez SMTP et SMS ci-dessous.
            </p>
          </div>

          <div className="bg-white/60 border border-white/20 rounded-2xl p-6 shadow-xl space-y-4">
            <div className="flex items-center justify-between">
              <h2 className="font-semibold flex items-center gap-2"><Mail size={18} /> Configuration e-mail (SMTP)</h2>
              <label className="flex items-center gap-2 text-sm">
                <input type="checkbox" checked={notif.emailActif} onChange={e => setNotif({ ...notif, emailActif: e.target.checked })} className="rounded" />
                Activer
              </label>
            </div>
            <div className="grid md:grid-cols-2 gap-4">
              <InputField label="Serveur SMTP" {...n('smtpHost')} placeholder="smtp.gmail.com" />
              <InputField label="Port" type="number" {...n('smtpPort')} placeholder="587" />
              <InputField label="Utilisateur SMTP" {...n('smtpUsername')} placeholder="noreply@minerva.group" />
              <PasswordField label={`Mot de passe SMTP${smtpConfigured ? ' (configuré — laisser vide pour conserver)' : ''}`}
                value={notif.smtpPassword} onChange={e => setNotif({ ...notif, smtpPassword: e.target.value })} placeholder="••••••••" />
              <InputField label="E-mail expéditeur" type="email" {...n('smtpFromEmail')} placeholder="noreply@minerva.group" />
              <InputField label="Nom expéditeur" {...n('smtpFromName')} placeholder="MINERVA GROUP RH" />
            </div>
            <div className="flex flex-wrap gap-4 text-sm">
              <label className="flex items-center gap-2">
                <input type="checkbox" checked={notif.smtpAuth} onChange={e => setNotif({ ...notif, smtpAuth: e.target.checked })} />
                Authentification SMTP
              </label>
              <label className="flex items-center gap-2">
                <input type="checkbox" checked={notif.smtpStarttls} onChange={e => setNotif({ ...notif, smtpStarttls: e.target.checked })} />
                STARTTLS
              </label>
            </div>
            <div className="flex flex-wrap gap-2 pt-2 border-t border-black/5">
              <InputField label="E-mail de test" type="email" value={testEmail} onChange={e => setTestEmail(e.target.value)} placeholder="admin@minerva.group" className="flex-1 min-w-[200px]" />
              <button type="button" onClick={handleTestEmail} disabled={testingEmail || notif.modeEnvoi !== 'LIVE'}
                className="self-end px-4 py-2.5 bg-[#1e3a5f] text-white rounded-xl text-xs font-semibold disabled:opacity-50 flex items-center gap-1.5">
                {testingEmail ? <RefreshCw className="animate-spin" size={14} /> : <Send size={14} />}
                Tester l'e-mail
              </button>
            </div>
          </div>

          <div className="bg-white/60 border border-white/20 rounded-2xl p-6 shadow-xl space-y-4">
            <div className="flex items-center justify-between">
              <h2 className="font-semibold flex items-center gap-2"><MessageSquare size={18} /> Configuration SMS</h2>
              <label className="flex items-center gap-2 text-sm">
                <input type="checkbox" checked={notif.smsActif} onChange={e => setNotif({ ...notif, smsActif: e.target.checked })} className="rounded" />
                Activer
              </label>
            </div>
            <SelectField label="Fournisseur" value={notif.smsProvider}
              onChange={e => setNotif({ ...notif, smsProvider: e.target.value })}
              options={SMS_PROVIDERS.map(p => ({ value: p.value, label: p.label }))} />
            <p className="text-xs text-gray-400">{getSmsProvider(notif.smsProvider).description}</p>
            <div className="grid md:grid-cols-2 gap-4">
              {getSmsProvider(notif.smsProvider).fields.map(field => {
                if (field.secret) {
                  const suffix = smsConfigured ? ' (configuré)' : '';
                  return (
                    <PasswordField
                      key={field.key}
                      label={`${field.label}${suffix}${field.required ? '' : ' (optionnel)'}`}
                      value={notif.smsApiSecret}
                      onChange={e => setNotif({ ...notif, smsApiSecret: e.target.value })}
                      placeholder={field.placeholder || '••••••••'}
                    />
                  );
                }
                if (field.extra) {
                  const val = getNestedValue(notif, field.key) || '';
                  return (
                    <InputField
                      key={field.key}
                      label={field.label}
                      value={val}
                      onChange={e => setNotif(setNestedValue(notif, field.key, e.target.value))}
                      placeholder={field.placeholder}
                      required={field.required}
                    />
                  );
                }
                return (
                  <InputField
                    key={field.key}
                    label={field.label}
                    value={notif[field.key] || ''}
                    onChange={e => setNotif({ ...notif, [field.key]: e.target.value })}
                    placeholder={field.placeholder}
                    required={field.required}
                  />
                );
              })}
            </div>
            <div className="flex flex-wrap gap-2 pt-2 border-t border-black/5">
              <InputField label="Téléphone de test" value={testPhone} onChange={e => setTestPhone(e.target.value)} placeholder="+224 620 00 00 00" className="flex-1 min-w-[200px]" />
              <button type="button" onClick={handleTestSms} disabled={testingSms || notif.modeEnvoi !== 'LIVE'}
                className="self-end px-4 py-2.5 bg-emerald-600 text-white rounded-xl text-xs font-semibold disabled:opacity-50 flex items-center gap-1.5">
                {testingSms ? <RefreshCw className="animate-spin" size={14} /> : <Send size={14} />}
                Tester le SMS
              </button>
            </div>
          </div>

          <div className="bg-white/60 border border-white/20 rounded-2xl p-6 shadow-xl space-y-4">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <h2 className="font-semibold flex items-center gap-2"><FileText size={18} /> Modèles de messages</h2>
              <button type="button" onClick={handleResetModeles}
                className="flex items-center gap-1.5 px-3 py-2 text-xs font-semibold text-gray-600 bg-gray-100 hover:bg-gray-200 rounded-xl">
                <RotateCcw size={14} /> Réinitialiser les modèles
              </button>
            </div>
            <p className="text-xs text-gray-400">
              Personnalisez les textes e-mail et SMS. Utilisez les variables entre doubles accolades, ex. <code className="bg-gray-100 px-1 rounded">{'{{prenom}}'}</code>.
            </p>
            <div className="space-y-4">
              {MODELES_META.map(({ key, label, vars }) => {
                const m = notif.modeles[key] || {};
                return (
                  <details key={key} className="border border-black/5 rounded-xl overflow-hidden">
                    <summary className="cursor-pointer px-4 py-3 bg-gray-50/80 font-medium text-sm hover:bg-gray-100/80">
                      {label}
                    </summary>
                    <div className="p-4 space-y-3 border-t border-black/5">
                      <p className="text-[11px] text-gray-400">
                        Variables : {vars.map(v => `{{${v}}}`).join(', ')}
                      </p>
                      {key !== 'TEST_SMS' && (
                        <InputField label="Objet e-mail" value={m.emailSujet || ''}
                          onChange={e => updateModele(key, 'emailSujet', e.target.value)} />
                      )}
                      {key !== 'TEST_SMS' && (
                        <div>
                          <label className="text-xs font-semibold text-gray-600">Corps e-mail</label>
                          <textarea value={m.emailCorps || ''} rows={4}
                            onChange={e => updateModele(key, 'emailCorps', e.target.value)}
                            className="w-full mt-1 px-4 py-2.5 bg-white/60 border border-black/5 rounded-xl text-sm outline-none focus:ring-2 focus:ring-black/10 resize-y" />
                        </div>
                      )}
                      {key !== 'TEST_EMAIL' && (
                        <div>
                          <label className="text-xs font-semibold text-gray-600">Message SMS</label>
                          <textarea value={m.smsCorps || ''} rows={2}
                            onChange={e => updateModele(key, 'smsCorps', e.target.value)}
                            className="w-full mt-1 px-4 py-2.5 bg-white/60 border border-black/5 rounded-xl text-sm outline-none focus:ring-2 focus:ring-black/10 resize-y" />
                        </div>
                      )}
                    </div>
                  </details>
                );
              })}
            </div>
          </div>

          <button type="submit" disabled={savingNotif} className="flex items-center gap-2 px-6 py-2.5 bg-black text-white rounded-xl text-sm font-semibold disabled:opacity-50">
            <Save size={16} /> {savingNotif ? 'Enregistrement...' : 'Enregistrer les notifications'}
          </button>
        </form>
      )}
    </div>
  );
}
