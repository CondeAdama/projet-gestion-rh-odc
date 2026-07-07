import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import {
  FileText, Plus, Edit2, Trash2, RotateCcw, XCircle,
  AlertCircle, Save, Calendar, Banknote
} from 'lucide-react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import { Modal, ConfirmDialog } from '../components/ui/Modal';
import { InputField, SelectField, SearchBar } from '../components/ui/FormFields';
import { Avatar, StatusBadge, StatCard, EmptyState, PageHeader, TabBar } from '../components/ui/Display';
import { STATUT_CONTRAT_STYLES, TYPE_CONTRAT_COLORS, formatGNF, formatDate } from '../utils/format';
import { getApiError, validateContratDates, validatePositiveNumber } from '../utils/validation';

const EMPTY_FORM = {
  employeId: '', typeContrat: 'CDI', salaireBase: '',
  indemniteTransport: '0', indemniteLogement: '0', autresAvantages: '0',
  dateDebut: '', dateFin: ''
};

export default function GestionContrats() {
  const { hasPermission } = useAuth();
  const [contrats, setContrats] = useState([]);
  const [corbeille, setCorbeille] = useState([]);
  const [employes, setEmployes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState('actifs');
  const [search, setSearch] = useState('');
  const [filterType, setFilterType] = useState('');

  const [modal, setModal] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [confirm, setConfirm] = useState(null);
  const [actionLoading, setActionLoading] = useState(false);

  const fetchAll = async () => {
    setLoading(true);
    try {
      const [contRes, corbRes, empRes] = await Promise.all([
        api.get('/contrats'),
        api.get('/contrats/corbeille'),
        api.get('/employes'),
      ]);
      setContrats(contRes.data);
      setCorbeille(corbRes.data);
      setEmployes(empRes.data.filter(e => e.statutEmploi === 'ACTIF'));
    } catch {
      setError('Erreur de chargement');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchAll(); }, []);

  const liste = tab === 'actifs' ? contrats : corbeille;
  const filtered = liste.filter(c => {
    const q = search.toLowerCase();
    const emp = c.employe;
    const matchSearch = !q ||
      emp?.nom?.toLowerCase().includes(q) ||
      emp?.prenom?.toLowerCase().includes(q) ||
      emp?.matricule?.toLowerCase().includes(q) ||
      c.typeContrat?.toLowerCase().includes(q);
    const matchType = !filterType || c.typeContrat === filterType;
    return matchSearch && matchType;
  });

  const stats = {
    total: contrats.length,
    actifs: contrats.filter(c => c.statutContrat === 'ACTIF').length,
    archives: contrats.filter(c => c.statutContrat === 'ARCHIVE').length,
    resilie: contrats.filter(c => c.statutContrat === 'RESILIE').length,
  };

  const openCreate = () => {
    setForm(EMPTY_FORM);
    setEditing(null);
    setError(null);
    setModal(true);
  };

  const openEdit = (c) => {
    setForm({
      employeId: c.employeId || '',
      typeContrat: c.typeContrat || 'CDI',
      salaireBase: c.salaireBase || '',
      indemniteTransport: c.indemniteTransport || '0',
      indemniteLogement: c.indemniteLogement || '0',
      autresAvantages: c.autresAvantages || '0',
      dateDebut: c.dateDebut || '',
      dateFin: c.dateFin || '',
    });
    setEditing(c);
    setError(null);
    setModal(true);
  };

  const handleSave = async (e) => {
    e.preventDefault();
    if (!form.employeId) { setError('Sélectionnez un employé'); return; }
    const dateErr = validateContratDates(form.typeContrat, form.dateDebut, form.dateFin);
    if (dateErr) { setError(dateErr); return; }
    const salaireErr = validatePositiveNumber(form.salaireBase, 'Le salaire de base');
    if (salaireErr) { setError(salaireErr); return; }
    setSaving(true);
    setError(null);
    try {
      const payload = {
        ...form,
        employeId: parseInt(form.employeId),
        salaireBase: parseFloat(form.salaireBase),
        indemniteTransport: parseFloat(form.indemniteTransport) || 0,
        indemniteLogement: parseFloat(form.indemniteLogement) || 0,
        autresAvantages: parseFloat(form.autresAvantages) || 0,
        dateFin: form.dateFin || null,
      };
      if (editing) {
        await api.put(`/contrats/${editing.id}`, payload);
      } else {
        await api.post('/contrats', payload);
      }
      setModal(false);
      fetchAll();
    } catch (err) {
      setError(getApiError(err, 'Erreur lors de l\'enregistrement'));
    } finally {
      setSaving(false);
    }
  };

  const handleAction = async () => {
    if (!confirm) return;
    setActionLoading(true);
    try {
      const { type, id } = confirm;
      if (type === 'delete') await api.delete(`/contrats/${id}`);
      else if (type === 'restore') await api.post(`/contrats/${id}/restaurer`);
      else if (type === 'purge') await api.delete(`/contrats/${id}/definitif`);
      else if (type === 'resilier') await api.put(`/contrats/${id}/resilier`);
      setConfirm(null);
      fetchAll();
    } catch (err) {
      setError(getApiError(err, 'Erreur'));
    } finally {
      setActionLoading(false);
    }
  };

  const f = (key) => ({
    value: form[key],
    onChange: e => setForm({ ...form, [key]: e.target.value }),
  });

  const salaireBrutPreview = () => {
    const base = parseFloat(form.salaireBase) || 0;
    const transport = parseFloat(form.indemniteTransport) || 0;
    const logement = parseFloat(form.indemniteLogement) || 0;
    const autres = parseFloat(form.autresAvantages) || 0;
    return base + transport + logement + autres;
  };

  return (
    <div className="space-y-6">
      <PageHeader
        title="Gestion des Contrats"
        subtitle="Contrats de travail, salaires et avantages"
        action={hasPermission('CONTRATS', 'AJOUTER') && tab === 'actifs' && (
          <button onClick={openCreate} className="flex items-center gap-2 px-5 py-2.5 bg-black text-white rounded-xl text-sm font-semibold hover:bg-gray-800 transition shadow-lg">
            <Plus size={16} /> Nouveau contrat
          </button>
        )}
      />

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard label="Total contrats" value={stats.total} icon={FileText} color="from-violet-600 to-purple-600" />
        <StatCard label="Actifs" value={stats.actifs} icon={FileText} color="from-emerald-600 to-teal-600" />
        <StatCard label="Archivés" value={stats.archives} icon={FileText} color="from-gray-500 to-gray-700" />
        <StatCard label="Résiliés" value={stats.resilie} icon={XCircle} color="from-red-500 to-rose-600" />
      </div>

      <div className="flex flex-col sm:flex-row gap-4 items-start sm:items-center justify-between">
        <TabBar
          tabs={[
            { key: 'actifs', label: 'Contrats', icon: FileText, count: contrats.length },
            { key: 'corbeille', label: 'Corbeille', icon: Trash2, count: corbeille.length },
          ]}
          active={tab}
          onChange={setTab}
        />
        <div className="flex gap-3 w-full sm:w-auto">
          {tab === 'actifs' && (
            <select value={filterType} onChange={e => setFilterType(e.target.value)}
              className="px-3 py-2.5 bg-white/60 border border-black/5 rounded-xl text-sm outline-none">
              <option value="">Tous les types</option>
              <option value="CDI">CDI</option>
              <option value="CDD">CDD</option>
              <option value="STAGE">STAGE</option>
            </select>
          )}
          <div className="flex-1 sm:w-64">
            <SearchBar value={search} onChange={e => setSearch(e.target.value)} placeholder="Employé, matricule, type..." />
          </div>
        </div>
      </div>

      {loading ? (
        <div className="flex justify-center py-20">
          <div className="w-8 h-8 border-2 border-black border-t-transparent rounded-full animate-spin" />
        </div>
      ) : filtered.length === 0 ? (
        <div className="bg-white/60 backdrop-blur border border-white/20 rounded-3xl">
          <EmptyState
            icon={FileText}
            title="Aucun contrat trouvé"
            description="Créez un contrat pour un employé actif."
            action={hasPermission('CONTRATS', 'AJOUTER') && (
              <button onClick={openCreate} className="flex items-center gap-2 px-4 py-2 bg-black text-white rounded-xl text-sm font-semibold">
                <Plus size={16} /> Créer un contrat
              </button>
            )}
          />
        </div>
      ) : (
        <div className="space-y-3">
          <AnimatePresence>
            {filtered.map((c, i) => {
              const emp = c.employe;
              const gradient = TYPE_CONTRAT_COLORS[c.typeContrat] || 'from-gray-600 to-gray-800';
              return (
                <motion.div
                  key={c.id}
                  initial={{ opacity: 0, x: -10 }}
                  animate={{ opacity: 1, x: 0 }}
                  exit={{ opacity: 0 }}
                  transition={{ delay: i * 0.03 }}
                  className="bg-white/60 backdrop-blur-xl border border-white/20 rounded-2xl overflow-hidden hover:shadow-lg transition group"
                >
                  <div className={`h-1 bg-gradient-to-r ${gradient}`} />
                  <div className="p-5 flex flex-col lg:flex-row lg:items-center gap-4">
                    <div className="flex items-center gap-4 flex-1 min-w-0">
                      <Avatar src={emp?.photoUrl} prenom={emp?.prenom} nom={emp?.nom} size="md" />
                      <div className="min-w-0">
                        <div className="flex items-center gap-2 flex-wrap">
                          <h3 className="font-semibold text-[#1D1D1F]">{emp?.prenom} {emp?.nom}</h3>
                          <span className="text-xs font-mono text-gray-400">{emp?.matricule}</span>
                          <span className={`text-xs font-bold px-2 py-0.5 rounded-full bg-gradient-to-r ${gradient} text-white`}>{c.typeContrat}</span>
                          <StatusBadge statut={c.statutContrat} styles={STATUT_CONTRAT_STYLES} />
                        </div>
                        <p className="text-xs text-gray-500 mt-1">
                          {emp?.posteLibelle && `${emp.posteLibelle} · `}{emp?.departementLibelle}
                        </p>
                      </div>
                    </div>

                    <div className="flex flex-wrap gap-6 lg:gap-8">
                      <div>
                        <p className="text-xs text-gray-400 font-medium">Salaire brut</p>
                        <p className="text-sm font-bold text-[#1D1D1F]">{formatGNF(c.salaireBrut)}</p>
                      </div>
                      <div>
                        <p className="text-xs text-gray-400 font-medium">Salaire base</p>
                        <p className="text-sm font-semibold text-gray-700">{formatGNF(c.salaireBase)}</p>
                      </div>
                      <div>
                        <p className="text-xs text-gray-400 font-medium flex items-center gap-1"><Calendar size={11} /> Période</p>
                        <p className="text-sm text-gray-700">{formatDate(c.dateDebut)} → {c.dateFin ? formatDate(c.dateFin) : 'Indéterminée'}</p>
                      </div>
                    </div>

                    <div className="flex gap-1 lg:opacity-0 lg:group-hover:opacity-100 transition">
                      {tab === 'actifs' ? (
                        <>
                          {hasPermission('CONTRATS', 'MODIFIER') && c.statutContrat !== 'RESILIE' && (
                            <>
                              <button onClick={() => openEdit(c)} className="p-2 hover:bg-gray-100 rounded-lg transition" title="Modifier">
                                <Edit2 size={15} />
                              </button>
                              {c.statutContrat === 'ACTIF' && (
                                <button onClick={() => setConfirm({ type: 'resilier', id: c.id, title: 'Résilier le contrat', message: `Résilier le contrat de ${emp?.prenom} ${emp?.nom} ?`, danger: true })} className="p-2 hover:bg-orange-50 text-orange-600 rounded-lg transition" title="Résilier">
                                  <XCircle size={15} />
                                </button>
                              )}
                            </>
                          )}
                          {hasPermission('CONTRATS', 'SUPPRIMER') && (
                            <button onClick={() => setConfirm({ type: 'delete', id: c.id, title: 'Supprimer', message: 'Déplacer ce contrat vers la corbeille ?', danger: true })} className="p-2 hover:bg-red-50 text-red-500 rounded-lg transition" title="Supprimer">
                              <Trash2 size={15} />
                            </button>
                          )}
                        </>
                      ) : (
                        <>
                          {hasPermission('CONTRATS', 'MODIFIER') && (
                            <button onClick={() => setConfirm({ type: 'restore', id: c.id, title: 'Restaurer', message: 'Restaurer ce contrat ?' })} className="p-2 hover:bg-emerald-50 text-emerald-600 rounded-lg transition">
                              <RotateCcw size={15} />
                            </button>
                          )}
                          {hasPermission('CONTRATS', 'SUPPRIMER') && (
                            <button onClick={() => setConfirm({ type: 'purge', id: c.id, title: 'Suppression définitive', message: 'Supprimer définitivement ce contrat ?', danger: true })} className="p-2 hover:bg-red-50 text-red-600 rounded-lg transition">
                              <Trash2 size={15} />
                            </button>
                          )}
                        </>
                      )}
                    </div>
                  </div>
                </motion.div>
              );
            })}
          </AnimatePresence>
        </div>
      )}

      <Modal open={modal} onClose={() => setModal(false)} title={editing ? 'Modifier le contrat' : 'Nouveau contrat'} size="lg">
        {error && (
          <div className="flex items-center gap-2 bg-red-50 text-red-600 p-3 rounded-xl mb-4 text-sm">
            <AlertCircle size={16} /> {error}
          </div>
        )}
        <form onSubmit={handleSave} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <SelectField label="Employé" required value={form.employeId}
              onChange={e => setForm({ ...form, employeId: e.target.value })}
              options={employes.map(e => ({ value: e.id, label: `${e.prenom} ${e.nom} (${e.matricule})` }))}
              disabled={!!editing} />
            <SelectField label="Type de contrat" required value={form.typeContrat}
              onChange={e => setForm({ ...form, typeContrat: e.target.value })}
              options={[{ value: 'CDI', label: 'CDI' }, { value: 'CDD', label: 'CDD' }, { value: 'STAGE', label: 'Stage' }]} />
            <InputField label="Salaire de base (GNF)" type="number" required min="0" {...f('salaireBase')} placeholder="3500000" />
            <InputField label="Indemnité transport" type="number" min="0" {...f('indemniteTransport')} placeholder="500000" />
            <InputField label="Indemnité logement" type="number" min="0" {...f('indemniteLogement')} placeholder="800000" />
            <InputField label="Autres avantages" type="number" min="0" {...f('autresAvantages')} placeholder="200000" />
            <InputField label="Date de début" type="date" required {...f('dateDebut')} />
            <InputField label="Date de fin" type="date" required={form.typeContrat === 'CDD'} {...f('dateFin')} placeholder="Vide pour CDI" />
          </div>

          <div className="bg-gradient-to-r from-gray-50 to-gray-100 rounded-2xl p-4 flex items-center gap-3">
            <Banknote size={20} className="text-gray-500" />
            <div>
              <p className="text-xs text-gray-500 font-medium">Salaire brut estimé</p>
              <p className="text-lg font-bold text-[#1D1D1F]">{formatGNF(salaireBrutPreview())}</p>
            </div>
          </div>

          {!editing && (
            <p className="text-xs text-gray-400 bg-blue-50 text-blue-600 p-3 rounded-xl">
              Un contrat actif existant sera automatiquement archivé. Une notification sera envoyée à l'employé.
            </p>
          )}

          <button type="submit" disabled={saving} className="flex items-center gap-2 px-6 py-2.5 bg-black text-white rounded-xl text-sm font-semibold disabled:opacity-50">
            <Save size={16} /> {saving ? 'Enregistrement...' : 'Enregistrer'}
          </button>
        </form>
      </Modal>

      <ConfirmDialog
        open={!!confirm}
        onClose={() => setConfirm(null)}
        onConfirm={handleAction}
        title={confirm?.title}
        message={confirm?.message}
        danger={confirm?.danger}
        loading={actionLoading}
      />
    </div>
  );
}
