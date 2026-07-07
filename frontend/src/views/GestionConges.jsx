import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import {
  Calendar, Plus, Clock, CheckCircle2, XCircle, MessageSquare,
  AlertCircle, Save, Trash2, RotateCcw, Filter
} from 'lucide-react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import { Modal, ConfirmDialog } from '../components/ui/Modal';
import { InputField, SelectField, SearchBar } from '../components/ui/FormFields';
import { Avatar, StatusBadge, StatCard, EmptyState, PageHeader, TabBar } from '../components/ui/Display';
import {
  STATUT_CONGE_STYLES, TYPE_CONGE_LABELS, TYPE_CONGE_COLORS, formatDate
} from '../utils/format';
import { getApiError, validateDateRange } from '../utils/validation';

const EMPTY_FORM = { typeConge: 'ANNUEL', dateDebut: '', dateFin: '', motif: '', employeId: '' };
const TYPES_CONGE = Object.keys(TYPE_CONGE_LABELS).map(k => ({ value: k, label: TYPE_CONGE_LABELS[k] }));

export default function GestionConges() {
  const { user, hasPermission } = useAuth();
  const canViewOthers = hasPermission('CONGES', 'AFFICHER_AUTRUI');
  const canManage = hasPermission('CONGES', 'MODIFIER');
  const isRh = canViewOthers || canManage;
  const employeId = user?.employeId;

  const [conges, setConges] = useState([]);
  const [corbeille, setCorbeille] = useState([]);
  const [employes, setEmployes] = useState([]);
  const [stats, setStats] = useState({ enAttente: 0, approuves: 0, refuses: 0 });
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState('demandes');
  const [filterStatut, setFilterStatut] = useState('');
  const [search, setSearch] = useState('');

  const [modal, setModal] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);

  const [traitement, setTraitement] = useState(null);
  const [commentaireRh, setCommentaireRh] = useState('');
  const [traitementLoading, setTraitementLoading] = useState(false);

  const [confirm, setConfirm] = useState(null);
  const [actionLoading, setActionLoading] = useState(false);

  const fetchAll = async () => {
    setLoading(true);
    try {
      const requests = [api.get(canViewOthers ? '/conges' : '/conges/mes-conges')];
      if (canViewOthers) {
        requests.push(api.get('/conges/statistiques'), api.get('/employes'), api.get('/conges/corbeille'));
      }
      const results = await Promise.all(requests);
      const mesConges = results[0].data;
      setConges(mesConges);
      if (canViewOthers) {
        setStats(results[1].data);
        setEmployes(results[2].data.filter(e => e.statutEmploi === 'ACTIF'));
        setCorbeille(results[3].data);
      } else {
        setStats({
          enAttente: mesConges.filter(c => c.statutConge === 'EN_ATTENTE').length,
          approuves: mesConges.filter(c => c.statutConge === 'APPROUVE').length,
          refuses: mesConges.filter(c => c.statutConge === 'REFUSE').length,
        });
      }
    } catch {
      setError('Erreur de chargement des congés');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchAll(); }, [canViewOthers]);

  const liste = tab === 'corbeille' ? corbeille : conges;
  const filtered = liste.filter(c => {
    const q = search.toLowerCase();
    const matchSearch = !q ||
      c.employe?.nom?.toLowerCase().includes(q) ||
      c.employe?.prenom?.toLowerCase().includes(q) ||
      c.motif?.toLowerCase().includes(q) ||
      TYPE_CONGE_LABELS[c.typeConge]?.toLowerCase().includes(q);
    const matchStatut = !filterStatut || c.statutConge === filterStatut;
    return matchSearch && matchStatut;
  });

  const joursApprouves = isRh ? null : conges
    .filter(c => c.statutConge === 'APPROUVE')
    .reduce((s, c) => s + c.nombreJours, 0);

  const openCreate = () => {
    setForm({ ...EMPTY_FORM, employeId: isRh ? '' : employeId });
    setEditing(null);
    setError(null);
    setModal(true);
  };

  const openEdit = (c) => {
    setForm({
      typeConge: c.typeConge,
      dateDebut: c.dateDebut,
      dateFin: c.dateFin,
      motif: c.motif || '',
      employeId: c.employeId,
    });
    setEditing(c);
    setError(null);
    setModal(true);
  };

  const calcJours = () => {
    if (!form.dateDebut || !form.dateFin) return 0;
    const d = (new Date(form.dateFin) - new Date(form.dateDebut)) / 86400000 + 1;
    return d > 0 ? Math.round(d) : 0;
  };

  const handleSave = async (e) => {
    e.preventDefault();
    const dateErr = validateDateRange(form.dateDebut, form.dateFin);
    if (dateErr) { setError(dateErr); return; }
    if (calcJours() <= 0) { setError('La durée du congé doit être d\'au moins 1 jour'); return; }
    if (isRh && !form.employeId) { setError('Sélectionnez un employé'); return; }
    setSaving(true);
    setError(null);
    try {
      const payload = { ...form, employeId: form.employeId ? parseInt(form.employeId) : employeId };
      if (editing) {
        await api.put(`/conges/${editing.id}`, payload);
      } else if (isRh && payload.employeId) {
        await api.post(`/conges/employe/${payload.employeId}`, payload);
      } else {
        await api.post('/conges', payload);
      }
      setModal(false);
      fetchAll();
    } catch (err) {
      setError(getApiError(err, 'Erreur lors de la soumission'));
    } finally {
      setSaving(false);
    }
  };

  const handleTraiter = async (statut) => {
    setTraitementLoading(true);
    try {
      await api.put(`/conges/${traitement.id}/traiter`, { statut, commentaireRh });
      setTraitement(null);
      setCommentaireRh('');
      fetchAll();
    } catch (err) {
      setError(getApiError(err, 'Erreur lors du traitement'));
    } finally {
      setTraitementLoading(false);
    }
  };

  const handleAction = async () => {
    if (!confirm) return;
    setActionLoading(true);
    try {
      const { type, id } = confirm;
      if (type === 'delete') await api.delete(`/conges/${id}`);
      else if (type === 'restore') await api.post(`/conges/${id}/restaurer`);
      else if (type === 'purge') await api.delete(`/conges/${id}/definitif`);
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

  return (
    <div className="space-y-6">
      <PageHeader
        title="Gestion des Congés"
        subtitle={isRh ? 'Validation des demandes et suivi RH' : 'Vos demandes de congé'}
        action={hasPermission('CONGES', 'AJOUTER') && tab !== 'corbeille' && (
          <button onClick={openCreate} className="flex items-center gap-2 px-5 py-2.5 bg-black text-white rounded-xl text-sm font-semibold hover:bg-gray-800 transition shadow-lg">
            <Plus size={16} /> Nouvelle demande
          </button>
        )}
      />

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {isRh ? (
          <>
            <StatCard label="En attente" value={stats.enAttente} icon={Clock} color="from-amber-500 to-orange-500" />
            <StatCard label="Approuvés" value={stats.approuves} icon={CheckCircle2} color="from-emerald-600 to-teal-600" />
            <StatCard label="Refusés" value={stats.refuses} icon={XCircle} color="from-red-500 to-rose-600" />
            <StatCard label="Total demandes" value={conges.length} icon={Calendar} color="from-violet-600 to-purple-600" />
          </>
        ) : (
          <>
            <StatCard label="Jours approuvés" value={joursApprouves} icon={CheckCircle2} color="from-emerald-600 to-teal-600" />
            <StatCard label="En attente" value={conges.filter(c => c.statutConge === 'EN_ATTENTE').length} icon={Clock} color="from-amber-500 to-orange-500" />
            <StatCard label="Solde estimé" value={Math.max(0, 30 - joursApprouves)} icon={Calendar} color="from-sky-600 to-blue-600" />
            <StatCard label="Total demandes" value={conges.length} icon={Filter} color="from-violet-600 to-purple-600" />
          </>
        )}
      </div>

      <div className="flex flex-col sm:flex-row gap-4 items-start sm:items-center justify-between">
        {isRh ? (
          <TabBar
            tabs={[
              { key: 'demandes', label: 'Demandes', icon: Calendar, count: conges.length },
              { key: 'corbeille', label: 'Corbeille', icon: Trash2, count: corbeille.length },
            ]}
            active={tab}
            onChange={setTab}
          />
        ) : (
          <div className="flex gap-2 flex-wrap">
            {['', 'EN_ATTENTE', 'APPROUVE', 'REFUSE'].map(s => (
              <button key={s} onClick={() => setFilterStatut(s)}
                className={`px-3 py-1.5 rounded-xl text-xs font-semibold transition ${
                  filterStatut === s ? 'bg-black text-white' : 'bg-white/60 text-gray-600 hover:bg-gray-100'
                }`}>
                {s === '' ? 'Tous' : s === 'EN_ATTENTE' ? 'En attente' : s === 'APPROUVE' ? 'Approuvés' : 'Refusés'}
              </button>
            ))}
          </div>
        )}
        <div className="flex gap-3 w-full sm:w-auto">
          {isRh && tab === 'demandes' && (
            <select value={filterStatut} onChange={e => setFilterStatut(e.target.value)}
              className="px-3 py-2.5 bg-white/60 border border-black/5 rounded-xl text-sm outline-none">
              <option value="">Tous les statuts</option>
              <option value="EN_ATTENTE">En attente</option>
              <option value="APPROUVE">Approuvé</option>
              <option value="REFUSE">Refusé</option>
            </select>
          )}
          <div className="flex-1 sm:w-64">
            <SearchBar value={search} onChange={e => setSearch(e.target.value)} placeholder="Rechercher..." />
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
            icon={Calendar}
            title="Aucune demande de congé"
            description={isRh ? 'Aucune demande ne correspond à vos filtres.' : 'Soumettez votre première demande de congé.'}
            action={hasPermission('CONGES', 'AJOUTER') && (
              <button onClick={openCreate} className="flex items-center gap-2 px-4 py-2 bg-black text-white rounded-xl text-sm font-semibold">
                <Plus size={16} /> Nouvelle demande
              </button>
            )}
          />
        </div>
      ) : (
        <div className="space-y-3">
          <AnimatePresence>
            {filtered.map((c, i) => {
              const gradient = TYPE_CONGE_COLORS[c.typeConge] || 'from-gray-500 to-slate-600';
              const emp = c.employe;
              return (
                <motion.div
                  key={c.id}
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0 }}
                  transition={{ delay: i * 0.03 }}
                  className="bg-white/60 backdrop-blur-xl border border-white/20 rounded-2xl overflow-hidden hover:shadow-lg transition group"
                >
                  <div className={`h-1 bg-gradient-to-r ${gradient}`} />
                  <div className="p-5">
                    <div className="flex flex-col lg:flex-row lg:items-center gap-4">
                      <div className="flex items-center gap-4 flex-1 min-w-0">
                        {isRh && emp && <Avatar src={emp.photoUrl} prenom={emp.prenom} nom={emp.nom} size="md" />}
                        <div className="min-w-0 flex-1">
                          <div className="flex items-center gap-2 flex-wrap">
                            {isRh && emp && (
                              <h3 className="font-semibold text-[#1D1D1F]">{emp.prenom} {emp.nom}</h3>
                            )}
                            <span className={`text-xs font-bold px-2.5 py-0.5 rounded-full bg-gradient-to-r ${gradient} text-white`}>
                              {TYPE_CONGE_LABELS[c.typeConge] || c.typeConge}
                            </span>
                            <StatusBadge statut={c.statutConge} styles={STATUT_CONGE_STYLES} />
                          </div>
                          <p className="text-sm text-gray-600 mt-1.5 flex items-center gap-2">
                            <Calendar size={14} className="text-gray-400" />
                            {formatDate(c.dateDebut)} → {formatDate(c.dateFin)}
                            <span className="text-xs bg-gray-100 px-2 py-0.5 rounded-full font-semibold text-gray-600">
                              {c.nombreJours} jour{c.nombreJours > 1 ? 's' : ''}
                            </span>
                          </p>
                          {c.motif && (
                            <p className="text-xs text-gray-500 mt-1 flex items-start gap-1.5">
                              <MessageSquare size={12} className="mt-0.5 shrink-0" /> {c.motif}
                            </p>
                          )}
                          {c.commentaireRh && (
                            <p className="text-xs text-blue-600 mt-1.5 bg-blue-50 px-3 py-1.5 rounded-lg">
                              RH : {c.commentaireRh}
                            </p>
                          )}
                        </div>
                      </div>

                      <div className="flex gap-1 lg:opacity-0 lg:group-hover:opacity-100 transition shrink-0">
                        {tab === 'demandes' && c.statutConge === 'EN_ATTENTE' && (
                          <>
                            {(isRh || c.employeId === employeId) && hasPermission('CONGES', 'AJOUTER') && (
                              <button onClick={() => openEdit(c)} className="p-2 hover:bg-gray-100 rounded-lg transition text-sm" title="Modifier">
                                Modifier
                              </button>
                            )}
                            {isRh && (
                              <>
                                <button onClick={() => { setTraitement(c); setCommentaireRh(''); }}
                                  className="flex items-center gap-1 px-3 py-1.5 bg-emerald-600 text-white rounded-lg text-xs font-semibold hover:bg-emerald-700 transition">
                                  <CheckCircle2 size={14} /> Approuver
                                </button>
                                <button onClick={() => { setTraitement({ ...c, action: 'REFUSE' }); setCommentaireRh(''); }}
                                  className="flex items-center gap-1 px-3 py-1.5 bg-red-50 text-red-600 rounded-lg text-xs font-semibold hover:bg-red-100 transition">
                                  <XCircle size={14} /> Refuser
                                </button>
                              </>
                            )}
                          </>
                        )}
                        {isRh && hasPermission('CONGES', 'SUPPRIMER') && (
                          <button onClick={() => setConfirm({
                            type: tab === 'corbeille' ? 'purge' : 'delete', id: c.id,
                            title: tab === 'corbeille' ? 'Suppression définitive' : 'Supprimer',
                            message: 'Confirmer cette action ?', danger: true
                          })} className="p-2 hover:bg-red-50 text-red-500 rounded-lg transition">
                            <Trash2 size={15} />
                          </button>
                        )}
                        {tab === 'corbeille' && isRh && (
                          <button onClick={() => setConfirm({ type: 'restore', id: c.id, title: 'Restaurer', message: 'Restaurer cette demande ?' })}
                            className="p-2 hover:bg-emerald-50 text-emerald-600 rounded-lg transition">
                            <RotateCcw size={15} />
                          </button>
                        )}
                      </div>
                    </div>
                  </div>
                </motion.div>
              );
            })}
          </AnimatePresence>
        </div>
      )}

      {/* Modal demande */}
      <Modal open={modal} onClose={() => setModal(false)} title={editing ? 'Modifier la demande' : 'Nouvelle demande de congé'} size="md">
        {error && (
          <div className="flex items-center gap-2 bg-red-50 text-red-600 p-3 rounded-xl mb-4 text-sm">
            <AlertCircle size={16} /> {error}
          </div>
        )}
        <form onSubmit={handleSave} className="space-y-4">
          {isRh && !editing && (
            <SelectField label="Employé" required value={form.employeId}
              onChange={e => setForm({ ...form, employeId: e.target.value })}
              options={employes.map(e => ({ value: e.id, label: `${e.prenom} ${e.nom} (${e.matricule})` }))} />
          )}
          <SelectField label="Type de congé" required value={form.typeConge}
            onChange={e => setForm({ ...form, typeConge: e.target.value })} options={TYPES_CONGE} />
          <div className="grid grid-cols-2 gap-4">
            <InputField label="Date de début" type="date" required {...f('dateDebut')} />
            <InputField label="Date de fin" type="date" required {...f('dateFin')} />
          </div>
          {calcJours() > 0 && (
            <div className="bg-sky-50 text-sky-700 px-4 py-2.5 rounded-xl text-sm font-medium">
              Durée : {calcJours()} jour{calcJours() > 1 ? 's' : ''}
            </div>
          )}
          <div>
            <label className="text-xs font-semibold text-gray-600">Motif (optionnel)</label>
            <textarea
              value={form.motif}
              onChange={e => setForm({ ...form, motif: e.target.value })}
              rows={3}
              className="w-full mt-1.5 px-4 py-2.5 bg-white/60 border border-black/5 rounded-xl text-sm outline-none focus:ring-2 focus:ring-black/10 resize-none"
              placeholder="Ex. : Congé annuel, mariage à Conakry..."
            />
          </div>
          <button type="submit" disabled={saving} className="flex items-center gap-2 px-6 py-2.5 bg-black text-white rounded-xl text-sm font-semibold disabled:opacity-50">
            <Save size={16} /> {saving ? 'Envoi...' : editing ? 'Mettre à jour' : 'Soumettre la demande'}
          </button>
        </form>
      </Modal>

      {/* Modal traitement RH */}
      <Modal
        open={!!traitement}
        onClose={() => setTraitement(null)}
        title={traitement?.action === 'REFUSE' ? 'Refuser la demande' : 'Approuver la demande'}
        size="sm"
      >
        {traitement && (
          <div className="space-y-4">
            <div className="bg-gray-50 rounded-xl p-4 text-sm">
              <p className="font-semibold">{traitement.employe?.prenom} {traitement.employe?.nom}</p>
              <p className="text-gray-500 mt-1">
                {TYPE_CONGE_LABELS[traitement.typeConge]} — {formatDate(traitement.dateDebut)} au {formatDate(traitement.dateFin)}
                ({traitement.nombreJours} jours)
              </p>
            </div>
            <div>
              <label className="text-xs font-semibold text-gray-600">Commentaire RH {traitement.action === 'REFUSE' ? '(recommandé)' : '(optionnel)'}</label>
              <textarea
                value={commentaireRh}
                onChange={e => setCommentaireRh(e.target.value)}
                rows={3}
                className="w-full mt-1.5 px-4 py-2.5 bg-white/60 border border-black/5 rounded-xl text-sm outline-none focus:ring-2 focus:ring-black/10 resize-none"
                placeholder="Votre commentaire..."
              />
            </div>
            <div className="flex gap-3 justify-end">
              <button onClick={() => setTraitement(null)} className="px-4 py-2 text-sm text-gray-600 hover:bg-gray-100 rounded-xl">Annuler</button>
              <button
                onClick={() => handleTraiter(traitement.action === 'REFUSE' ? 'REFUSE' : 'APPROUVE')}
                disabled={traitementLoading}
                className={`px-5 py-2 text-sm font-semibold text-white rounded-xl disabled:opacity-50 ${
                  traitement.action === 'REFUSE' ? 'bg-red-600 hover:bg-red-700' : 'bg-emerald-600 hover:bg-emerald-700'
                }`}
              >
                {traitementLoading ? 'Traitement...' : traitement.action === 'REFUSE' ? 'Confirmer le refus' : 'Confirmer l\'approbation'}
              </button>
            </div>
            <p className="text-xs text-gray-400">Une notification SMS et E-mail sera envoyée à l'employé.</p>
          </div>
        )}
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
