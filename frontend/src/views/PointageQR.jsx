import { useState, useEffect, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import {
  Scan, MapPin, RefreshCw, CheckCircle2, AlertCircle, Activity,
  ArrowRight, Keyboard, Camera, Smartphone, X, Trash2, RotateCcw, Filter
} from 'lucide-react';
import { Html5QrcodeScanner } from 'html5-qrcode';
import { QRCodeSVG } from 'qrcode.react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import { getApiError } from '../utils/validation';
import { normaliserMatriculeScan } from '../utils/scan';
import { Avatar, PageHeader, EmptyState } from '../components/ui/Display';
import { STATUT_PRESENCE_STYLES } from '../utils/format';

const SCAN_URL = `${window.location.origin}/scan`;

function computeStatsFromPresences(presences) {
  const employes = new Set();
  let enRegle = 0;
  let retards = 0;
  let presents = 0;
  presences.forEach(p => {
    if (p.employeId) employes.add(p.employeId);
    if (p.statutPresence === 'EN_REGLE') enRegle += 1;
    if (p.statutPresence === 'RETARD') retards += 1;
    if (!p.heureSortie) presents += 1;
  });
  return {
    total: presences.length,
    employes: employes.size,
    enRegle,
    retards,
    presents,
  };
}

export default function PointageQR() {
  const { hasPermission } = useAuth();
  const today = new Date().toISOString().slice(0, 10);
  const [localisations, setLocalisations] = useState([]);
  const [departements, setDepartements] = useState([]);
  const [selectedLocal, setSelectedLocal] = useState('');
  const [presenceTab, setPresenceTab] = useState('jour');
  const [filters, setFilters] = useState({ dateDebut: today, dateFin: today, localisationId: '', departementId: '', statutPresence: '' });
  const [corbeille, setCorbeille] = useState([]);
  const [matriculeInput, setMatriculeInput] = useState('');
  const [loadingData, setLoadingData] = useState(true);
  const [scanning, setScanning] = useState(false);
  const [scanResult, setScanResult] = useState(null);
  const [recentPresences, setRecentPresences] = useState([]);
  const [loadingPresences, setLoadingPresences] = useState(true);
  const [employesList, setEmployesList] = useState([]);
  const [stats, setStats] = useState({ total: 0, employes: 0, enRegle: 0, retards: 0, presents: 0 });
  const [statsFiltered, setStatsFiltered] = useState(false);
  const [cameraMode, setCameraMode] = useState(false);
  const [showTransferModal, setShowTransferModal] = useState(false);

  const fetchData = async () => {
    setLoadingData(true);
    setLoadingPresences(true);
    try {
      const [locRes, presRes, empRes, statsRes, deptRes] = await Promise.all([
        api.get('/referentiels/localisations'),
        api.get('/presences/aujourdhui'),
        api.get('/employes'),
        api.get('/presences/statistiques'),
        api.get('/referentiels/departements'),
      ]);
      const locs = locRes.data.filter(l => l.statut === 'ACTIF');
      setLocalisations(locs);
      setDepartements(deptRes.data);
      if (locs.length > 0 && !selectedLocal) setSelectedLocal(String(locs[0].id));
      setRecentPresences(presRes.data);
      setEmployesList(empRes.data.filter(e => e.statutEmploi === 'ACTIF'));
      setStats(statsRes.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoadingData(false);
      setLoadingPresences(false);
    }
  };

  useEffect(() => { fetchData(); }, []);

  const refreshPresences = async () => {
    setLoadingPresences(true);
    try {
      if (presenceTab === 'corbeille') {
        const res = await api.get('/presences/corbeille');
        setCorbeille(res.data);
        setStatsFiltered(false);
      } else if (presenceTab === 'recherche') {
        const params = {};
        if (filters.dateDebut) params.dateDebut = filters.dateDebut;
        if (filters.dateFin) params.dateFin = filters.dateFin;
        if (filters.localisationId) params.localisationId = filters.localisationId;
        if (filters.departementId) params.departementId = filters.departementId;
        if (filters.statutPresence) params.statutPresence = filters.statutPresence;
        const res = await api.get('/presences/recherche', { params });
        setRecentPresences(res.data);
        setStats(computeStatsFromPresences(res.data));
        setStatsFiltered(true);
      } else {
        const [presRes, statsRes] = await Promise.all([
          api.get('/presences/aujourdhui'),
          api.get('/presences/statistiques'),
        ]);
        setRecentPresences(presRes.data);
        setStats(statsRes.data);
        setStatsFiltered(false);
      }
    } finally {
      setLoadingPresences(false);
    }
  };

  useEffect(() => { refreshPresences(); }, [presenceTab]);

  const handleDeletePresence = async (id) => {
    if (!confirm('Déplacer ce pointage vers la corbeille ?')) return;
    await api.delete(`/presences/${id}`);
    refreshPresences();
  };

  const handleRestorePresence = async (id) => {
    await api.post(`/presences/${id}/restaurer`);
    refreshPresences();
  };

  const displayedPresences = presenceTab === 'corbeille' ? corbeille : recentPresences;

  const doScan = useCallback(async (decoded) => {
    const clean = normaliserMatriculeScan(decoded);
    if (!clean) {
      setScanResult({
        success: false,
        message: 'QR invalide — scannez le badge employé (matricule), pas le QR de transfert mobile.',
      });
      return;
    }
    if (!selectedLocal) {
      setScanResult({ success: false, message: 'Veuillez sélectionner une localisation.' });
      return;
    }
    setScanning(true);
    setScanResult(null);
    try {
      const res = await api.post('/presences/scanner', null, {
        params: { matricule: clean, localisationId: selectedLocal },
      });
      setScanResult({ success: true, message: res.data.message, typeScan: res.data.typeScan });
      setMatriculeInput('');
      refreshPresences();
    } catch (err) {
      setScanResult({
        success: false,
        message: getApiError(err, 'Badge ou matricule inconnu.'),
      });
    } finally {
      setScanning(false);
    }
  }, [selectedLocal]);

  useEffect(() => {
    if (!cameraMode) return;
    const scanner = new Html5QrcodeScanner('reader', { qrbox: { width: 250, height: 250 }, fps: 10 });
    scanner.render(
      (decoded) => { scanner.clear().catch(() => {}); setCameraMode(false); doScan(decoded); },
      () => {}
    );
    return () => { scanner.clear().catch(() => {}); };
  }, [cameraMode, doScan]);

  const handleSubmit = (e) => {
    e.preventDefault();
    doScan(matriculeInput);
  };

  return (
    <div className="space-y-8">
      <PageHeader
        title="Scanner & Pointage"
        subtitle="Entrées et sorties multiples par jour — scan QR ou saisie matricule."
        action={
          <div className="flex flex-wrap items-center gap-3">
            {loadingData ? (
              <span className="text-xs text-gray-400">Chargement...</span>
            ) : (
              <div className="flex items-center gap-2 bg-white/60 border border-black/5 px-4 py-2.5 rounded-xl">
                <MapPin size={16} className="text-gray-500" />
                <select
                  value={selectedLocal}
                  onChange={e => setSelectedLocal(e.target.value)}
                  className="text-sm font-semibold bg-transparent outline-none border-none"
                >
                  {localisations.map(l => (
                    <option key={l.id} value={l.id}>{l.nom} ({l.ville})</option>
                  ))}
                </select>
              </div>
            )}
            <button
              onClick={() => setShowTransferModal(true)}
              className="flex items-center gap-2 px-4 py-2.5 bg-black hover:bg-gray-800 text-white rounded-xl text-sm font-medium shadow-md"
            >
              <Smartphone size={16} />
              <span className="hidden sm:inline">Transférer sur Mobile</span>
            </button>
            <button onClick={refreshPresences} className="p-3 bg-white/60 border border-black/5 hover:bg-black/5 rounded-xl">
              <RefreshCw size={18} className={loadingPresences ? 'animate-spin text-gray-600' : 'text-gray-600'} />
            </button>
          </div>
        }
      />

      <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
        {statsFiltered && (
          <p className="col-span-full text-xs text-gray-500 -mb-2">
            Statistiques calculées selon les filtres appliqués.
          </p>
        )}
        {[
          { label: 'Passages', value: stats.total, color: 'text-gray-800' },
          { label: 'Employés', value: stats.employes, color: 'text-indigo-600' },
          { label: 'En règle', value: stats.enRegle, color: 'text-emerald-600' },
          { label: 'Retards', value: stats.retards, color: 'text-amber-600' },
          { label: 'Sur site', value: stats.presents, color: 'text-blue-600' },
        ].map(s => (
          <div key={s.label} className="bg-white/60 border border-white/20 rounded-2xl p-4 text-center">
            <p className={`text-2xl font-bold ${s.color}`}>{s.value ?? 0}</p>
            <p className="text-xs text-gray-500 mt-1">{s.label}</p>
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-1 space-y-6">
          <div className="bg-white/60 backdrop-blur border border-white/20 p-6 rounded-2xl shadow-xl space-y-6">
            <div className="flex items-center gap-3">
              <div className="p-2.5 bg-blue-500/10 rounded-xl text-blue-600"><Scan size={20} /></div>
              <div>
                <h2 className="text-base font-semibold">Interface de Scan</h2>
                <p className="text-xs text-gray-500">Scannez le badge ou saisissez le matricule.</p>
              </div>
            </div>

            <div className="w-full min-h-[250px] bg-gradient-to-br from-gray-900 to-gray-700 rounded-2xl relative overflow-hidden flex flex-col items-center justify-center text-white">
              {cameraMode ? (
                <div id="reader" className="w-full bg-white text-black" />
              ) : (
                <>
                  <button
                    onClick={() => setCameraMode(true)}
                    className="z-10 flex items-center gap-2 px-5 py-3 bg-blue-600 hover:bg-blue-700 text-white rounded-xl text-sm font-semibold shadow-lg"
                  >
                    <Camera size={18} /> Ouvrir la Caméra
                  </button>
                  <span className="text-[10px] text-white/50 tracking-wider font-mono uppercase mt-4">Caméra désactivée</span>
                </>
              )}
            </div>

            <form onSubmit={handleSubmit} className="space-y-4">
              <label className="text-xs font-semibold text-gray-600 flex items-center gap-1.5">
                <Keyboard size={14} /> Matricule de Badge
              </label>
              <input
                type="text"
                required
                minLength={3}
                maxLength={50}
                placeholder="SNG-2026-..."
                value={matriculeInput}
                onChange={e => setMatriculeInput(e.target.value)}
                className="w-full px-4 py-3 bg-white/40 border border-black/5 rounded-xl text-sm outline-none font-mono tracking-wider"
              />
              <button
                type="submit"
                disabled={scanning || !selectedLocal}
                className="w-full py-3 bg-black hover:bg-gray-800 text-white rounded-xl text-sm font-semibold flex items-center justify-center gap-2 disabled:opacity-50"
              >
                {scanning ? <RefreshCw className="animate-spin" size={16} /> : 'Valider le pointage'}
              </button>
            </form>

            <AnimatePresence mode="wait">
              {scanResult && (
                <motion.div
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  exit={{ opacity: 0 }}
                  className={`p-4 rounded-xl flex items-start gap-3 border text-xs ${
                    scanResult.success ? 'bg-emerald-50 border-emerald-100 text-emerald-700' : 'bg-red-50 border-red-100 text-red-700'
                  }`}
                >
                  {scanResult.success ? <CheckCircle2 size={16} className="flex-shrink-0 mt-0.5" /> : <AlertCircle size={16} className="flex-shrink-0 mt-0.5" />}
                  <div>
                    <span className="font-bold block mb-1">{scanResult.success ? 'Succès' : 'Échec'}</span>
                    <span>{scanResult.message}</span>
                    {scanResult.typeScan && <span className="block mt-1 font-mono text-[10px] opacity-70">{scanResult.typeScan}</span>}
                  </div>
                </motion.div>
              )}
            </AnimatePresence>
          </div>

          <div className="bg-white/60 border border-white/20 p-6 rounded-2xl shadow-xl space-y-4">
            <h3 className="text-sm font-semibold">Simulateur de Badge</h3>
            <div className="space-y-2 max-h-56 overflow-y-auto">
              {employesList.map(emp => (
                <button
                  key={emp.id}
                  onClick={() => doScan(emp.matricule)}
                  className="w-full flex items-center justify-between p-2.5 bg-gray-50 hover:bg-gray-100 rounded-xl border border-black/5 text-left"
                >
                  <div className="flex items-center gap-2">
                    <Avatar src={emp.photoUrl} prenom={emp.prenom} nom={emp.nom} size="sm" />
                    <div>
                      <p className="text-xs font-semibold">{emp.prenom} {emp.nom}</p>
                      <p className="text-[10px] text-gray-400 font-mono">{emp.matricule}</p>
                    </div>
                  </div>
                  <ArrowRight size={14} className="text-gray-400" />
                </button>
              ))}
            </div>
          </div>
        </div>

        <div className="lg:col-span-2">
          <div className="bg-white/60 border border-white/20 p-6 rounded-2xl shadow-xl space-y-6">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div>
                <h2 className="text-base font-semibold">Historique des présences</h2>
                <p className="text-xs text-gray-500">Filtrez par date, site, département ou statut.</p>
              </div>
              <div className="flex flex-wrap gap-2">
                {['jour', 'recherche', 'corbeille'].map(t => (
                  <button key={t} onClick={() => setPresenceTab(t)}
                    className={`px-3 py-1.5 rounded-lg text-xs font-medium ${presenceTab === t ? 'bg-black text-white' : 'bg-gray-100'}`}>
                    {t === 'jour' ? "Aujourd'hui" : t === 'recherche' ? 'Recherche' : 'Corbeille'}
                  </button>
                ))}
              </div>
            </div>

            {presenceTab === 'recherche' && (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-5 gap-3 p-4 glass-card">
                <div>
                  <label className="text-[10px] font-semibold text-gray-500 uppercase">Date début</label>
                  <input type="date" value={filters.dateDebut} onChange={e => setFilters({ ...filters, dateDebut: e.target.value })}
                    className="w-full mt-1 px-3 py-2 border rounded-xl text-sm" />
                </div>
                <div>
                  <label className="text-[10px] font-semibold text-gray-500 uppercase">Date fin</label>
                  <input type="date" value={filters.dateFin} onChange={e => setFilters({ ...filters, dateFin: e.target.value })}
                    className="w-full mt-1 px-3 py-2 border rounded-xl text-sm" />
                </div>
                <div>
                  <label className="text-[10px] font-semibold text-gray-500 uppercase">Localisation</label>
                  <select value={filters.localisationId} onChange={e => setFilters({ ...filters, localisationId: e.target.value })}
                    className="w-full mt-1 px-3 py-2 border rounded-xl text-sm">
                    <option value="">Toutes</option>
                    {localisations.map(l => <option key={l.id} value={l.id}>{l.nom}</option>)}
                  </select>
                </div>
                <div>
                  <label className="text-[10px] font-semibold text-gray-500 uppercase">Département</label>
                  <select value={filters.departementId} onChange={e => setFilters({ ...filters, departementId: e.target.value })}
                    className="w-full mt-1 px-3 py-2 border rounded-xl text-sm">
                    <option value="">Tous</option>
                    {departements.map(d => <option key={d.id} value={d.id}>{d.libelle}</option>)}
                  </select>
                </div>
                <div>
                  <label className="text-[10px] font-semibold text-gray-500 uppercase">Statut</label>
                  <select value={filters.statutPresence} onChange={e => setFilters({ ...filters, statutPresence: e.target.value })}
                    className="w-full mt-1 px-3 py-2 border rounded-xl text-sm">
                    <option value="">Tous</option>
                    <option value="EN_REGLE">En règle</option>
                    <option value="RETARD">Retard</option>
                  </select>
                </div>
                <div className="sm:col-span-2 lg:col-span-5">
                  <button onClick={refreshPresences} className="flex items-center gap-2 px-4 py-2 bg-[#1e3a5f] text-white rounded-xl text-sm font-semibold">
                    <Filter size={14} /> Appliquer les filtres
                  </button>
                </div>
              </div>
            )}

            <div className="flex items-center justify-end">
              <span className="text-xs bg-black/5 px-2.5 py-1 rounded-full font-semibold text-gray-600">
                {displayedPresences.length} pointage{displayedPresences.length !== 1 ? 's' : ''}
              </span>
            </div>

            {loadingPresences ? (
              <div className="flex flex-col items-center py-20 gap-3">
                <RefreshCw className="animate-spin text-gray-400" size={32} />
              </div>
            ) : displayedPresences.length === 0 ? (
              <EmptyState icon={Activity} title="Aucun pointage" description="Ajustez les filtres ou attendez les premiers scans." />
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-left border-collapse text-sm">
                  <thead>
                    <tr className="border-b border-black/5 text-gray-400 text-[10px] font-semibold uppercase">
                      <th className="pb-3">Collaborateur</th>
                      <th className="pb-3">Site</th>
                      <th className="pb-3">Date</th>
                      <th className="pb-3">Passage</th>
                      <th className="pb-3">Entrée</th>
                      <th className="pb-3">Sortie</th>
                      <th className="pb-3">Statut</th>
                      {hasPermission('PRESENCES', 'SUPPRIMER') && <th className="pb-3" />}
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-black/5">
                    {displayedPresences.map(p => {
                      const st = STATUT_PRESENCE_STYLES[p.statutPresence] || STATUT_PRESENCE_STYLES.EN_REGLE;
                      return (
                        <tr key={p.id} className="hover:bg-gray-50/50">
                          <td className="py-3">
                            <div className="flex items-center gap-3">
                              <Avatar src={p.employe?.photoUrl} prenom={p.employe?.prenom} nom={p.employe?.nom} size="sm" />
                              <div>
                                <p className="font-semibold">{p.employe?.prenom} {p.employe?.nom}</p>
                                <p className="text-[10px] text-gray-400 font-mono">{p.employe?.matricule}</p>
                              </div>
                            </div>
                          </td>
                          <td className="py-3 text-xs">
                            <p className="font-medium">{p.localisationNom}</p>
                            <p className="text-gray-400">{p.localisationVille}</p>
                          </td>
                          <td className="py-3 text-xs text-gray-500">{p.dateJour}</td>
                          <td className="py-3 text-xs">
                            <span className="px-2 py-0.5 rounded-full bg-black/5 font-semibold text-gray-600">
                              #{p.numeroPassage || 1}
                            </span>
                          </td>
                          <td className="py-3 text-xs font-medium">{p.heureEntree?.slice?.(0, 5) || p.heureEntree}</td>
                          <td className="py-3 text-xs">
                            {p.heureSortie ? <span className="font-semibold">{p.heureSortie?.slice?.(0, 5) || p.heureSortie}</span> : <span className="italic text-gray-400">—</span>}
                          </td>
                          <td className="py-3">
                            <span className={`text-[10px] px-2.5 py-1 rounded-full font-semibold ${st.bg} ${st.text}`}>{st.label}</span>
                          </td>
                          {hasPermission('PRESENCES', 'SUPPRIMER') && (
                            <td className="py-3">
                              {presenceTab === 'corbeille' ? (
                                hasPermission('PRESENCES', 'MODIFIER') && (
                                  <button onClick={() => handleRestorePresence(p.id)} className="p-1.5 hover:bg-emerald-50 text-emerald-600 rounded-lg"><RotateCcw size={14} /></button>
                                )
                              ) : (
                                <button onClick={() => handleDeletePresence(p.id)} className="p-1.5 hover:bg-red-50 text-red-500 rounded-lg"><Trash2 size={14} /></button>
                              )}
                            </td>
                          )}
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      </div>

      <AnimatePresence>
        {showTransferModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-md">
            <motion.div
              initial={{ scale: 0.95, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.95, opacity: 0 }}
              className="bg-white rounded-3xl w-full max-w-sm shadow-2xl p-8 text-center space-y-6"
            >
              <div className="flex items-center justify-between">
                <h3 className="text-lg font-semibold">Scanner avec le mobile</h3>
                <button onClick={() => setShowTransferModal(false)} className="p-2 hover:bg-black/5 rounded-xl">
                  <X size={18} className="text-gray-500" />
                </button>
              </div>
              <p className="text-sm text-gray-500">
                Scannez ce QR avec votre téléphone, connectez-vous (compte Réception, RH ou Admin), puis scannez les badges employés.
              </p>
              <div className="flex justify-center p-4 bg-white rounded-2xl border border-black/10 shadow-sm mx-auto w-fit">
                <QRCodeSVG value={SCAN_URL} size={200} />
              </div>
              <p className="text-xs text-gray-400 font-mono break-all">{SCAN_URL}</p>
              <button onClick={() => setShowTransferModal(false)} className="w-full py-3 bg-black/5 hover:bg-black/10 rounded-xl text-sm font-semibold">
                Fermer
              </button>
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </div>
  );
}
