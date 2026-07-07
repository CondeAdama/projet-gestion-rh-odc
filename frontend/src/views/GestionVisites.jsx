import { useState, useEffect } from 'react';
import { Plus, RefreshCw, Play, Square, UserPlus, KeySquare, AlertCircle, Edit2, Trash2, RotateCcw, Printer } from 'lucide-react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import { getApiError, validatePhone } from '../utils/validation';
import { formatDate, formatTime } from '../utils/format';
import { Modal } from '../components/ui/Modal';
import { InputField, SelectField } from '../components/ui/FormFields';
import { PageHeader, StatCard, EmptyState } from '../components/ui/Display';
import CarteVisiteur from '../components/documents/CarteVisiteur';
import { DocumentExportButtons } from '../components/documents/DocumentExportButtons';
import { BADGE_PRINT_CSS } from '../utils/documentExport';

export default function GestionVisites() {
  const { hasPermission } = useAuth();
  const canDemarrer = hasPermission('VISITES', 'AJOUTER');

  const [visiteurs, setVisiteurs] = useState([]);
  const [cartes, setCartes] = useState([]);
  const [visites, setVisites] = useState([]);
  const [stats, setStats] = useState({});
  const [loading, setLoading] = useState(true);

  const [visiteursCorbeille, setVisiteursCorbeille] = useState([]);
  const [visiteurTab, setVisiteurTab] = useState('actifs');
  const [editVisiteur, setEditVisiteur] = useState(null);
  const [visiteurModal, setVisiteurModal] = useState(false);
  const [carteModal, setCarteModal] = useState(false);
  const [demarrerModal, setDemarrerModal] = useState(false);

  const [visiteurForm, setVisiteurForm] = useState({ nom: '', prenom: '', contact: '', entreprise: '' });
  const [carteForm, setCarteForm] = useState({ numeroCarte: '' });
  const [visiteForm, setVisiteForm] = useState({ visiteurId: '', carteId: '', motif: '' });
  const [error, setError] = useState(null);
  const [modalError, setModalError] = useState(null);
  const [saving, setSaving] = useState(false);
  const [printingBadge, setPrintingBadge] = useState(null);

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    const [vRes, vcRes, cRes, viRes, sRes] = await Promise.allSettled([
      api.get('/visites/visiteurs'),
      api.get('/visites/visiteurs/corbeille'),
      api.get('/visites/cartes'),
      api.get('/visites'),
      api.get('/visites/statistiques'),
    ]);
    if (vRes.status === 'fulfilled') setVisiteurs(vRes.value.data);
    else { setVisiteurs([]); setError(getApiError(vRes.reason, 'Erreur de chargement des visiteurs')); }
    if (vcRes.status === 'fulfilled') setVisiteursCorbeille(vcRes.value.data);
    else setVisiteursCorbeille([]);
    if (cRes.status === 'fulfilled') setCartes(cRes.value.data);
    else setCartes([]);
    if (viRes.status === 'fulfilled') setVisites(viRes.value.data);
    else setVisites([]);
    if (sRes.status === 'fulfilled') setStats(sRes.value.data);
    else setStats({});
    setLoading(false);
  };

  useEffect(() => { fetchData(); }, []);

  const cartesDisponibles = cartes.filter(c => c.statut === 'DISPONIBLE');

  const openDemarrerModal = () => {
    if (!canDemarrer) {
      setError('Vous n\'avez pas la permission de démarrer une visite.');
      return;
    }
    if (visiteurs.length === 0) {
      setError('Créez au moins un visiteur avant de démarrer une visite.');
      return;
    }
    if (cartesDisponibles.length === 0) {
      setError('Aucune carte disponible. Clôturez une visite en cours ou créez une nouvelle carte.');
      return;
    }
    setError(null);
    setModalError(null);
    setVisiteForm({
      visiteurId: String(visiteurs[0].id),
      carteId: String(cartesDisponibles[0].id),
      motif: '',
    });
    setDemarrerModal(true);
  };

  const handleCreateVisiteur = async (e) => {
    e.preventDefault();
    const phoneErr = validatePhone(visiteurForm.contact);
    if (phoneErr) { setModalError(phoneErr); return; }
    setSaving(true);
    setModalError(null);
    try {
      await api.post('/visites/visiteurs', {
        ...visiteurForm,
        nom: visiteurForm.nom.trim(),
        prenom: visiteurForm.prenom.trim(),
        contact: visiteurForm.contact.replace(/\s+/g, ''),
        entreprise: visiteurForm.entreprise.trim(),
      });
      setVisiteurModal(false);
      setVisiteurForm({ nom: '', prenom: '', contact: '', entreprise: '' });
      fetchData();
    } catch (err) {
      setModalError(getApiError(err, 'Erreur lors de la création du visiteur'));
    } finally {
      setSaving(false);
    }
  };

  const handleUpdateVisiteur = async (e) => {
    e.preventDefault();
    const phoneErr = validatePhone(visiteurForm.contact);
    if (phoneErr) { setModalError(phoneErr); return; }
    setSaving(true);
    setModalError(null);
    try {
      await api.put(`/visites/visiteurs/${editVisiteur.id}`, {
        nom: visiteurForm.nom.trim(),
        prenom: visiteurForm.prenom.trim(),
        contact: visiteurForm.contact.replace(/\s+/g, ''),
        entreprise: visiteurForm.entreprise.trim(),
      });
      setEditVisiteur(null);
      setVisiteurForm({ nom: '', prenom: '', contact: '', entreprise: '' });
      fetchData();
    } catch (err) {
      setModalError(getApiError(err, 'Erreur'));
    } finally {
      setSaving(false);
    }
  };

  const handleDeleteVisiteur = async (id) => {
    if (!confirm('Déplacer ce visiteur vers la corbeille ?')) return;
    try {
      await api.delete(`/visites/visiteurs/${id}`);
      fetchData();
    } catch (err) {
      setError(getApiError(err, 'Erreur'));
    }
  };

  const handleRestoreVisiteur = async (id) => {
    try {
      await api.post(`/visites/visiteurs/${id}/restaurer`);
      fetchData();
    } catch (err) {
      setError(getApiError(err, 'Erreur'));
    }
  };

  const openEditVisiteur = (v) => {
    setEditVisiteur(v);
    setVisiteurForm({ nom: v.nom, prenom: v.prenom, contact: v.contact, entreprise: v.entreprise || '' });
    setModalError(null);
  };

  const listeVisiteurs = visiteurTab === 'actifs' ? visiteurs : visiteursCorbeille;

  const handleCreateCarte = async (e) => {
    e.preventDefault();
    if (!carteForm.numeroCarte.trim()) { setModalError('Le numéro de carte est obligatoire'); return; }
    setSaving(true);
    setModalError(null);
    try {
      await api.post('/visites/cartes', { numeroCarte: carteForm.numeroCarte.trim() });
      setCarteModal(false);
      setCarteForm({ numeroCarte: '' });
      fetchData();
    } catch (err) {
      setModalError(getApiError(err, 'Erreur lors de la création de la carte'));
    } finally {
      setSaving(false);
    }
  };

  const handleDemarrer = async (e) => {
    e.preventDefault();
    if (!visiteForm.visiteurId || !visiteForm.carteId || !visiteForm.motif.trim()) {
      setModalError('Visiteur, carte et motif sont obligatoires');
      return;
    }
    setSaving(true);
    setModalError(null);
    try {
      await api.post('/visites/demarrer', {
        visiteurId: parseInt(visiteForm.visiteurId, 10),
        carteId: parseInt(visiteForm.carteId, 10),
        motif: visiteForm.motif.trim(),
      });
      setDemarrerModal(false);
      setVisiteForm({ visiteurId: '', carteId: '', motif: '' });
      fetchData();
    } catch (err) {
      setModalError(getApiError(err, 'Erreur lors du démarrage de la visite'));
    } finally {
      setSaving(false);
    }
  };

  const handleCloturer = async (id) => {
    if (!window.confirm('Clôturer cette visite et libérer la carte ?')) return;
    setError(null);
    try {
      await api.put(`/visites/${id}/cloturer`);
      fetchData();
    } catch (err) {
      setError(getApiError(err, 'Erreur lors de la clôture'));
    }
  };

  const demarrerDisabled = !canDemarrer || visiteurs.length === 0 || cartesDisponibles.length === 0;

  return (
    <div className="space-y-6 sm:space-y-8">
      <style>{BADGE_PRINT_CSS}</style>
      <PageHeader
        title="Gestion des Visites"
        subtitle="Enregistrement des visiteurs et assignation des badges."
        action={
          <button onClick={fetchData} className="p-3 glass-card hover:bg-white/90 transition rounded-xl">
            <RefreshCw size={18} className={loading ? 'animate-spin text-gray-600' : 'text-gray-600'} />
          </button>
        }
      />

      {error && (
        <div className="flex items-center gap-2 bg-red-50 text-red-600 p-4 rounded-xl text-sm border border-red-100">
          <AlertCircle size={18} /> {error}
        </div>
      )}

      <div className="grid grid-cols-2 md:grid-cols-4 gap-3 sm:gap-4">
        <StatCard label="Visites en cours" value={stats.enCours ?? 0} icon={Play} color="from-amber-500 to-orange-500" />
        <StatCard label="Terminées" value={stats.terminees ?? 0} icon={Square} color="from-emerald-500 to-teal-600" />
        <StatCard label="Visiteurs" value={stats.visiteurs ?? 0} icon={UserPlus} />
        <StatCard label="Cartes dispo." value={stats.cartesDisponibles ?? 0} icon={KeySquare} color="from-blue-500 to-indigo-600" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-4 sm:gap-6">
        <div className="glass-card p-4 sm:p-6">
          <div className="flex flex-wrap justify-between items-center gap-2 mb-4">
            <h2 className="font-semibold">Visiteurs</h2>
            <div className="flex gap-1">
              <button onClick={() => setVisiteurTab('actifs')} className={`px-2 py-1 rounded-lg text-[10px] font-bold ${visiteurTab === 'actifs' ? 'bg-black text-white' : 'bg-gray-100'}`}>Actifs</button>
              <button onClick={() => setVisiteurTab('corbeille')} className={`px-2 py-1 rounded-lg text-[10px] font-bold flex items-center gap-1 ${visiteurTab === 'corbeille' ? 'bg-black text-white' : 'bg-gray-100'}`}><Trash2 size={10} />Corbeille</button>
              {hasPermission('VISITES', 'AJOUTER') && visiteurTab === 'actifs' && (
                <button onClick={() => { setVisiteurModal(true); setModalError(null); }} className="p-2 bg-black/5 hover:bg-black/10 rounded-lg transition ml-1">
                  <Plus size={16} />
                </button>
              )}
            </div>
          </div>
          <div className="space-y-2 max-h-64 overflow-y-auto">
            {listeVisiteurs.map(v => (
              <div key={v.id} className="p-3 bg-gray-50/80 rounded-xl text-sm border border-black/5 flex justify-between items-start gap-2">
                <div>
                  <p className="font-semibold">{v.prenom} {v.nom}</p>
                  <p className="text-xs text-gray-500">{v.contact} · {v.entreprise || 'Particulier'}</p>
                </div>
                <div className="flex gap-1 shrink-0">
                  {visiteurTab === 'actifs' && hasPermission('VISITES', 'MODIFIER') && (
                    <button onClick={() => openEditVisiteur(v)} className="p-1.5 hover:bg-blue-50 text-blue-600 rounded-lg"><Edit2 size={12} /></button>
                  )}
                  {visiteurTab === 'actifs' && hasPermission('VISITES', 'SUPPRIMER') && (
                    <button onClick={() => handleDeleteVisiteur(v.id)} className="p-1.5 hover:bg-red-50 text-red-500 rounded-lg"><Trash2 size={12} /></button>
                  )}
                  {visiteurTab === 'corbeille' && hasPermission('VISITES', 'MODIFIER') && (
                    <button onClick={() => handleRestoreVisiteur(v.id)} className="p-1.5 hover:bg-emerald-50 text-emerald-600 rounded-lg"><RotateCcw size={12} /></button>
                  )}
                </div>
              </div>
            ))}
            {listeVisiteurs.length === 0 && <p className="text-xs text-gray-400 text-center py-4">Aucun visiteur</p>}
          </div>
        </div>

        <div className="glass-card p-4 sm:p-6">
          <div className="flex justify-between items-center mb-4">
            <h2 className="font-semibold">Cartes / Badges</h2>
            {hasPermission('VISITES', 'AJOUTER') && (
              <button onClick={() => { setCarteModal(true); setModalError(null); }} className="p-2 bg-black/5 hover:bg-black/10 rounded-lg transition">
                <Plus size={16} />
              </button>
            )}
          </div>
          <div className="space-y-2 max-h-64 overflow-y-auto">
            {cartes.map(c => (
              <div key={c.id} className="p-3 bg-gray-50/80 rounded-xl text-sm flex justify-between items-center border border-black/5 gap-2">
                <span className="font-mono font-semibold">{c.numeroCarte}</span>
                <div className="flex items-center gap-2 shrink-0">
                  <span className={`text-[10px] px-2 py-0.5 rounded-full font-bold ${c.statut === 'DISPONIBLE' ? 'bg-emerald-100 text-emerald-700' : 'bg-amber-100 text-amber-700'}`}>
                    {c.statut}
                  </span>
                  <button
                    onClick={() => setPrintingBadge({ numeroCarte: c.numeroCarte, visiteur: null, motif: null, dateEntree: null })}
                    className="p-1.5 hover:bg-blue-50 text-blue-600 rounded-lg"
                    title="Imprimer le badge"
                  >
                    <Printer size={12} />
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="bg-gradient-to-br from-[#1e3a5f] to-[#2563eb] text-white rounded-2xl p-4 sm:p-6 shadow-xl flex flex-col justify-center gap-4">
          <h2 className="text-lg font-semibold text-center">Nouvelle visite</h2>
          {!canDemarrer && (
            <p className="text-xs text-white/70 text-center">Permission « Ajouter » requise pour démarrer une visite.</p>
          )}
          {canDemarrer && cartesDisponibles.length === 0 && visiteurs.length > 0 && (
            <p className="text-xs text-amber-200 text-center">Toutes les cartes sont assignées. Clôturez une visite ou créez une carte.</p>
          )}
          <button
            onClick={openDemarrerModal}
            disabled={demarrerDisabled}
            className="w-full py-3 bg-white text-[#1e3a5f] rounded-xl font-semibold text-sm disabled:opacity-50 flex items-center justify-center gap-2 transition hover:bg-white/90"
          >
            <Play size={16} /> Démarrer une visite
          </button>
        </div>
      </div>

      <div className="glass-card p-4 sm:p-6">
        <h2 className="font-semibold mb-4">Historique des visites</h2>
        {visites.length === 0 ? (
          <EmptyState icon={UserPlus} title="Aucune visite" description="Les visites enregistrées apparaîtront ici." />
        ) : (
          <div className="overflow-x-auto -mx-2 sm:mx-0">
            <table className="w-full text-sm min-w-[600px]">
              <thead>
                <tr className="border-b text-gray-400 text-[10px] uppercase">
                  <th className="pb-3 text-left px-2">Visiteur</th>
                  <th className="pb-3 text-left px-2">Carte</th>
                  <th className="pb-3 text-left px-2 hidden sm:table-cell">Motif</th>
                  <th className="pb-3 text-left px-2">Date</th>
                  <th className="pb-3 text-left px-2">Entrée</th>
                  <th className="pb-3 text-left px-2">Sortie</th>
                  <th className="pb-3 text-left px-2">Statut</th>
                  <th className="pb-3 px-2" />
                </tr>
              </thead>
              <tbody className="divide-y divide-black/5">
                {visites.map(v => (
                  <tr key={v.id} className="hover:bg-gray-50/50">
                    <td className="py-3 font-medium px-2">{v.visiteur?.prenom} {v.visiteur?.nom}</td>
                    <td className="py-3 font-mono text-xs px-2">{v.numeroCarte}</td>
                    <td className="py-3 text-xs text-gray-600 px-2 hidden sm:table-cell">{v.motif}</td>
                    <td className="py-3 text-xs px-2">{formatDate(v.dateJour || v.dateHeureEntree)}</td>
                    <td className="py-3 text-xs font-medium px-2">{formatTime(v.heureEntree || v.dateHeureEntree)}</td>
                    <td className="py-3 text-xs px-2">
                      {v.heureSortie || v.dateHeureSortie
                        ? <span className="font-semibold">{formatTime(v.heureSortie || v.dateHeureSortie)}</span>
                        : <span className="italic text-gray-400">—</span>}
                    </td>
                    <td className="py-3 px-2">
                      <span className={`text-[10px] px-2 py-0.5 rounded-full font-bold ${v.statut === 'EN_COURS' ? 'bg-amber-100 text-amber-700' : 'bg-emerald-100 text-emerald-700'}`}>
                        {v.statut}
                      </span>
                    </td>
                    <td className="py-3 px-2">
                      <div className="flex gap-1 justify-end">
                        {v.statut === 'EN_COURS' && (
                          <button
                            onClick={() => setPrintingBadge({
                              visiteur: v.visiteur,
                              numeroCarte: v.numeroCarte,
                              motif: v.motif,
                              dateEntree: v.dateHeureEntree,
                            })}
                            className="p-1.5 hover:bg-blue-50 text-blue-600 rounded-lg"
                            title="Imprimer le badge visiteur"
                          >
                            <Printer size={14} />
                          </button>
                        )}
                        {v.statut === 'EN_COURS' && hasPermission('VISITES', 'MODIFIER') && (
                          <button onClick={() => handleCloturer(v.id)} className="text-xs px-3 py-1 bg-black text-white rounded-lg hover:opacity-90 transition">
                            Clôturer
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <Modal open={visiteurModal} onClose={() => { setVisiteurModal(false); setModalError(null); }} title="Nouveau visiteur">
        {modalError && <div className="mb-4 text-red-600 text-sm flex gap-2"><AlertCircle size={14} />{modalError}</div>}
        <form onSubmit={handleCreateVisiteur} className="space-y-4">
          <InputField label="Nom" required maxLength={100} placeholder="Diallo" value={visiteurForm.nom} onChange={e => setVisiteurForm({ ...visiteurForm, nom: e.target.value })} />
          <InputField label="Prénom" required maxLength={100} placeholder="Mamadou" value={visiteurForm.prenom} onChange={e => setVisiteurForm({ ...visiteurForm, prenom: e.target.value })} />
          <InputField label="Contact" required type="tel" placeholder="+224 628 44 55 66" value={visiteurForm.contact} onChange={e => setVisiteurForm({ ...visiteurForm, contact: e.target.value })} />
          <InputField label="Entreprise" maxLength={150} placeholder="Orange Guinée" value={visiteurForm.entreprise} onChange={e => setVisiteurForm({ ...visiteurForm, entreprise: e.target.value })} />
          <button type="submit" disabled={saving} className="w-full py-3 bg-black text-white rounded-xl text-sm font-semibold disabled:opacity-50">Enregistrer</button>
        </form>
      </Modal>

      <Modal open={carteModal} onClose={() => { setCarteModal(false); setModalError(null); }} title="Nouvelle carte visiteur">
        {modalError && <div className="mb-4 text-red-600 text-sm flex gap-2"><AlertCircle size={14} />{modalError}</div>}
        <form onSubmit={handleCreateCarte} className="space-y-4">
          <InputField label="Numéro de carte" required placeholder="VIS-006" maxLength={50} value={carteForm.numeroCarte} onChange={e => setCarteForm({ numeroCarte: e.target.value })} />
          <button type="submit" disabled={saving} className="w-full py-3 bg-black text-white rounded-xl text-sm font-semibold disabled:opacity-50">Créer</button>
        </form>
      </Modal>

      <Modal open={!!editVisiteur} onClose={() => { setEditVisiteur(null); setModalError(null); }} title="Modifier le visiteur">
        {modalError && <div className="mb-4 text-red-600 text-sm flex gap-2"><AlertCircle size={14} />{modalError}</div>}
        <form onSubmit={handleUpdateVisiteur} className="space-y-4">
          <InputField label="Nom" required value={visiteurForm.nom} onChange={e => setVisiteurForm({ ...visiteurForm, nom: e.target.value })} />
          <InputField label="Prénom" required value={visiteurForm.prenom} onChange={e => setVisiteurForm({ ...visiteurForm, prenom: e.target.value })} />
          <InputField label="Contact" required type="tel" value={visiteurForm.contact} onChange={e => setVisiteurForm({ ...visiteurForm, contact: e.target.value })} />
          <InputField label="Entreprise" value={visiteurForm.entreprise} onChange={e => setVisiteurForm({ ...visiteurForm, entreprise: e.target.value })} />
          <button type="submit" disabled={saving} className="w-full py-3 bg-black text-white rounded-xl text-sm font-semibold disabled:opacity-50">Enregistrer</button>
        </form>
      </Modal>

      <Modal open={demarrerModal} onClose={() => { setDemarrerModal(false); setModalError(null); }} title="Démarrer une visite">
        {modalError && <div className="mb-4 text-red-600 text-sm flex gap-2"><AlertCircle size={14} />{modalError}</div>}
        <form onSubmit={handleDemarrer} className="space-y-4">
          <SelectField label="Visiteur" required value={visiteForm.visiteurId} onChange={e => setVisiteForm({ ...visiteForm, visiteurId: e.target.value })}
            options={visiteurs.map(v => ({ value: String(v.id), label: `${v.prenom} ${v.nom}` }))} />
          <SelectField label="Carte disponible" required value={visiteForm.carteId} onChange={e => setVisiteForm({ ...visiteForm, carteId: e.target.value })}
            options={cartesDisponibles.map(c => ({ value: String(c.id), label: c.numeroCarte }))} />
          <InputField label="Motif de visite" required maxLength={500} placeholder="Entretien RH — rendez-vous 14h" value={visiteForm.motif} onChange={e => setVisiteForm({ ...visiteForm, motif: e.target.value })} />
          <button type="submit" disabled={saving} className="w-full py-3 bg-[#1e3a5f] text-white rounded-xl text-sm font-semibold disabled:opacity-50">Démarrer</button>
        </form>
      </Modal>

      <Modal open={!!printingBadge} onClose={() => setPrintingBadge(null)} title="Badge visiteur" size="sm">
        {printingBadge && (
          <div className="space-y-6 no-print">
            <CarteVisiteur
              id="print-badge-visiteur"
              visiteur={printingBadge.visiteur}
              numeroCarte={printingBadge.numeroCarte}
              motif={printingBadge.motif}
              dateEntree={printingBadge.dateEntree}
            />
            <div className="flex flex-col gap-3">
              <DocumentExportButtons
                elementId="print-badge-visiteur"
                basename={`badge-visiteur-${printingBadge.numeroCarte}`}
                variant="badge"
              />
              <button onClick={() => setPrintingBadge(null)} className="w-full py-3 bg-black/5 rounded-xl text-sm font-semibold">Fermer</button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
