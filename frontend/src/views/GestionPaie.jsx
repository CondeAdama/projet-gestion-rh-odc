import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import {
  FileText, DollarSign, Search, RefreshCw, AlertCircle, CheckCircle2,
  User, ShieldAlert
} from 'lucide-react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import { useConfig } from '../context/ConfigContext';
import { Avatar, EmptyState, PageHeader, StatCard } from '../components/ui/Display';
import { formatGNF, MOIS_LABELS } from '../utils/format';
import { getApiError } from '../utils/validation';
import { QRCodeSVG } from 'qrcode.react';
import { DocumentHeader } from '../components/documents/CarteEmploye';
import { DocumentExportButtons } from '../components/documents/DocumentExportButtons';
import { PAYSLIP_PRINT_CSS } from '../utils/documentExport';

export default function GestionPaie() {
  const { hasPermission } = useAuth();
  const { config } = useConfig();
  const canViewOthers = hasPermission('PAIES', 'AFFICHER_AUTRUI');
  const canGenerate = hasPermission('PAIES', 'AJOUTER');

  const [fiches, setFiches] = useState([]);
  const [employes, setEmployes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [statsApi, setStatsApi] = useState({});

  const [selectedEmploye, setSelectedEmploye] = useState('');
  const [selectedContract, setSelectedContract] = useState(null);
  const [contractLoading, setContractLoading] = useState(false);
  const [payMonth, setPayMonth] = useState(new Date().getMonth() + 1);
  const [payYear, setPayYear] = useState(new Date().getFullYear());
  const [genLoading, setGenLoading] = useState(false);
  const [genSuccess, setGenSuccess] = useState(null);
  const [genError, setGenError] = useState(null);

  const [searchTerm, setSearchTerm] = useState('');
  const [filterMonth, setFilterMonth] = useState('ALL');
  const [filterYear, setFilterYear] = useState(String(new Date().getFullYear()));
  const [viewingFiche, setViewingFiche] = useState(null);

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    try {
      const requests = [api.get(canViewOthers ? '/fiches-paie' : '/fiches-paie/mes-fiches')];
      if (canViewOthers) {
        requests.push(api.get('/employes'), api.get('/fiches-paie/statistiques'));
      }
      const results = await Promise.all(requests);
      setFiches(results[0].data);
      if (canViewOthers) {
        setEmployes(results[1].data.filter(e => e.statutEmploi === 'ACTIF'));
        setStatsApi(results[2].data);
      }
    } catch (err) {
      setError(getApiError(err, 'Erreur lors de la récupération des données de paie.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchData(); }, [canViewOthers]);

  useEffect(() => {
    if (!selectedEmploye || !canGenerate) { setSelectedContract(null); return; }
    setContractLoading(true);
    api.get(`/contrats/employe/${selectedEmploye}/actif`)
      .then(res => setSelectedContract(res.data))
      .catch(() => setSelectedContract(null))
      .finally(() => setContractLoading(false));
  }, [selectedEmploye]);

  const handleGenerate = async (e) => {
    e.preventDefault();
    if (!selectedEmploye) { setGenError('Sélectionnez un collaborateur.'); return; }
    if (!selectedContract) { setGenError('Aucun contrat actif pour cet employé.'); return; }
    if (payMonth < 1 || payMonth > 12) { setGenError('Mois invalide.'); return; }
    if (payYear < 2000 || payYear > 2100) { setGenError('Année invalide.'); return; }
    setGenLoading(true);
    setGenError(null);
    setGenSuccess(null);
    try {
      const res = await api.post('/fiches-paie/generer', {
        employeId: Number(selectedEmploye),
        mois: payMonth,
        annee: payYear,
      });
      setGenSuccess('Bulletin généré avec succès !');
      setFiches(prev => [res.data, ...prev]);
      setSelectedEmploye('');
      setSelectedContract(null);
      if (canViewOthers) {
        const statsRes = await api.get('/fiches-paie/statistiques');
        setStatsApi(statsRes.data);
      }
    } catch (err) {
      setGenError(getApiError(err, 'Une fiche existe déjà pour cette période.'));
    } finally {
      setGenLoading(false);
    }
  };

  const filteredFiches = fiches.filter(f => {
    if (filterMonth !== 'ALL' && f.periodeMois !== parseInt(filterMonth)) return false;
    if (filterYear !== 'ALL' && f.periodeAnnee !== parseInt(filterYear)) return false;
    if (searchTerm) {
      const q = searchTerm.toLowerCase();
      const name = `${f.employe?.prenom || ''} ${f.employe?.nom || ''}`.toLowerCase();
      return name.includes(q) || f.employe?.matricule?.toLowerCase().includes(q) ||
        `${f.periodeAnnee}-${f.periodeMois}`.includes(q);
    }
    return true;
  });

  const stats = canViewOthers ? {
    totalNet: statsApi.masseSalariale || 0,
    totalCnss: statsApi.totalCnss || 0,
    totalRts: statsApi.totalRts || 0,
    totalEmployeesPaid: new Set(fiches.map(f => f.employeId)).size,
    tauxCnss: statsApi.tauxCnss ?? config?.tauxCnss ?? 5,
    tauxRts: statsApi.tauxRts ?? config?.tauxRts ?? 10,
  } : {
    totalNet: fiches.reduce((s, f) => s + Number(f.salaireNet), 0),
    lastNet: fiches[0]?.salaireNet || 0,
    count: fiches.length,
  };

  return (
    <div className="space-y-8">
      <style>{PAYSLIP_PRINT_CSS}</style>

      <PageHeader
        title={canViewOthers ? 'Gestion de la Paie' : 'Mes Fiches de Paie'}
        subtitle={canViewOthers ? 'Générez les bulletins et suivez la masse salariale.' : 'Consultez et imprimez vos bulletins de paie.'}
        action={
          <button onClick={fetchData} className="p-3 bg-white/60 border border-black/5 hover:bg-black/5 rounded-xl">
            <RefreshCw size={18} className={loading ? 'animate-spin text-gray-600' : 'text-gray-600'} />
          </button>
        }
      />

      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-6">
        {canViewOthers ? (
          <>
            <StatCard label="Masse salariale nette" value={formatGNF(stats.totalNet)} icon={DollarSign} color="from-emerald-500 to-teal-600" />
            <StatCard label={`CNSS retenue (${stats.tauxCnss}%)`} value={formatGNF(stats.totalCnss)} icon={ShieldAlert} color="from-orange-500 to-amber-600" />
            <StatCard label={`RTS retenus (${stats.tauxRts}%)`} value={formatGNF(stats.totalRts)} icon={FileText} color="from-indigo-500 to-violet-600" />
            <StatCard label="Collaborateurs payés" value={stats.totalEmployeesPaid} icon={User} />
          </>
        ) : (
          <>
            <StatCard label="Cumul net" value={formatGNF(stats.totalNet)} icon={DollarSign} color="from-emerald-500 to-teal-600" />
            <StatCard label="Dernier virement" value={formatGNF(stats.lastNet)} icon={CheckCircle2} color="from-indigo-500 to-violet-600" />
            <StatCard label="Bulletins" value={stats.count} icon={FileText} />
          </>
        )}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {canGenerate && (
          <div className="lg:col-span-1">
            <div className="bg-white/60 border border-white/20 p-6 rounded-2xl shadow-xl space-y-5">
              <h2 className="text-lg font-semibold">Calcul du Salaire</h2>
              {genSuccess && (
                <div className="bg-emerald-50 border border-emerald-100 text-emerald-700 text-xs p-3 rounded-xl flex gap-2">
                  <CheckCircle2 size={16} className="flex-shrink-0" /> {genSuccess}
                </div>
              )}
              {genError && (
                <div className="bg-red-50 border border-red-100 text-red-600 text-xs p-3 rounded-xl flex gap-2">
                  <AlertCircle size={16} className="flex-shrink-0" /> {genError}
                </div>
              )}
              <form onSubmit={handleGenerate} className="space-y-4">
                <div>
                  <label className="text-xs font-semibold text-gray-600">Collaborateur</label>
                  <select
                    value={selectedEmploye}
                    onChange={e => setSelectedEmploye(e.target.value)}
                    className="w-full mt-1 px-4 py-2.5 bg-white/40 border border-black/5 rounded-xl text-sm outline-none"
                  >
                    <option value="">Sélectionner...</option>
                    {employes.map(emp => (
                      <option key={emp.id} value={emp.id}>{emp.prenom} {emp.nom} ({emp.matricule})</option>
                    ))}
                  </select>
                </div>
                {selectedEmploye && (
                  <div className="p-4 bg-gray-50 rounded-xl border border-black/5 text-xs space-y-2">
                    {contractLoading ? (
                      <div className="flex gap-2 text-gray-400"><RefreshCw className="animate-spin" size={12} /> Chargement...</div>
                    ) : selectedContract ? (
                      <>
                        <div className="flex justify-between"><span className="text-gray-500">Contrat</span><span className="font-bold">{selectedContract.typeContrat} — ACTIF</span></div>
                        <div className="flex justify-between"><span className="text-gray-500">Base</span><span className="font-bold">{formatGNF(selectedContract.salaireBase)}</span></div>
                        <div className="flex justify-between"><span className="text-gray-500">Transport</span><span>{formatGNF(selectedContract.indemniteTransport)}</span></div>
                        <div className="flex justify-between"><span className="text-gray-500">Logement</span><span>{formatGNF(selectedContract.indemniteLogement)}</span></div>
                      </>
                    ) : (
                      <div className="flex gap-2 text-red-500"><ShieldAlert size={14} /> Aucun contrat actif.</div>
                    )}
                  </div>
                )}
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="text-xs font-semibold text-gray-600">Mois</label>
                    <select value={payMonth} onChange={e => setPayMonth(parseInt(e.target.value))} className="w-full mt-1 px-4 py-2.5 bg-white/40 border border-black/5 rounded-xl text-sm outline-none">
                      {MOIS_LABELS.map((m, i) => <option key={i} value={i + 1}>{m}</option>)}
                    </select>
                  </div>
                  <div>
                    <label className="text-xs font-semibold text-gray-600">Année</label>
                    <select value={payYear} onChange={e => setPayYear(parseInt(e.target.value))} className="w-full mt-1 px-4 py-2.5 bg-white/40 border border-black/5 rounded-xl text-sm outline-none">
                      {[2026, 2027].map(y => <option key={y} value={y}>{y}</option>)}
                    </select>
                  </div>
                </div>
                <button type="submit" disabled={genLoading || !selectedContract} className="w-full py-3 bg-black text-white rounded-xl text-sm font-semibold disabled:opacity-50 flex items-center justify-center gap-2">
                  {genLoading ? <RefreshCw className="animate-spin" size={16} /> : 'Générer le bulletin'}
                </button>
              </form>
            </div>
          </div>
        )}

        <div className={canGenerate ? 'lg:col-span-2' : 'lg:col-span-3'}>
          <div className="bg-white/60 border border-white/20 p-6 rounded-2xl shadow-xl space-y-6">
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
              <h2 className="text-lg font-semibold">Historique des Bulletins</h2>
              <div className="flex flex-wrap gap-3">
                <div className="relative">
                  <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={15} />
                  <input
                    type="text"
                    placeholder="Rechercher..."
                    value={searchTerm}
                    onChange={e => setSearchTerm(e.target.value)}
                    className="pl-9 pr-4 py-2 bg-white/40 border border-black/5 rounded-xl text-xs outline-none w-40"
                  />
                </div>
                <select value={filterMonth} onChange={e => setFilterMonth(e.target.value)} className="px-3 py-2 bg-white/40 border border-black/5 rounded-xl text-xs outline-none">
                  <option value="ALL">Tous les mois</option>
                  {MOIS_LABELS.map((m, i) => <option key={i} value={i + 1}>{m}</option>)}
                </select>
                <select value={filterYear} onChange={e => setFilterYear(e.target.value)} className="px-3 py-2 bg-white/40 border border-black/5 rounded-xl text-xs outline-none">
                  <option value="ALL">Toutes années</option>
                  <option value="2026">2026</option>
                  <option value="2027">2027</option>
                </select>
              </div>
            </div>

            {loading ? (
              <div className="flex justify-center py-20"><RefreshCw className="animate-spin text-gray-400" size={32} /></div>
            ) : error ? (
              <EmptyState icon={AlertCircle} title={error} action={<button onClick={fetchData} className="px-4 py-2 bg-red-50 text-red-600 rounded-xl text-xs font-semibold">Réessayer</button>} />
            ) : filteredFiches.length === 0 ? (
              <EmptyState icon={FileText} title="Aucun bulletin" description="Les bulletins générés apparaîtront ici." />
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full text-left text-sm">
                  <thead>
                    <tr className="border-b border-black/5 text-gray-400 text-[10px] font-semibold uppercase">
                      {canViewOthers && <th className="pb-3">Collaborateur</th>}
                      <th className="pb-3">Période</th>
                      <th className="pb-3">Brut</th>
                      <th className="pb-3">Net</th>
                      <th className="pb-3">Action</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-black/5">
                    {filteredFiches.map(fiche => (
                      <tr key={fiche.id} className="hover:bg-gray-50/50">
                        {canViewOthers && (
                          <td className="py-4">
                            <div className="flex items-center gap-3">
                              <Avatar src={fiche.employe?.photoUrl} prenom={fiche.employe?.prenom} nom={fiche.employe?.nom} size="sm" />
                              <div>
                                <p className="font-medium">{fiche.employe?.prenom} {fiche.employe?.nom}</p>
                                <p className="text-xs text-gray-400">{fiche.employe?.matricule}</p>
                              </div>
                            </div>
                          </td>
                        )}
                        <td className="py-4 font-semibold">{fiche.periodeLibelle || `${MOIS_LABELS[fiche.periodeMois - 1]} ${fiche.periodeAnnee}`}</td>
                        <td className="py-4 text-gray-600">{formatGNF(fiche.salaireBrut)}</td>
                        <td className="py-4 font-semibold text-emerald-600">{formatGNF(fiche.salaireNet)}</td>
                        <td className="py-4">
                          <button onClick={() => setViewingFiche(fiche)} className="px-3 py-1.5 bg-black/5 hover:bg-black/10 rounded-lg text-xs font-semibold">
                            Consulter
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      </div>

      <AnimatePresence>
        {viewingFiche && (
          <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/40 backdrop-blur-md overflow-y-auto">
            <motion.div initial={{ scale: 0.95, opacity: 0 }} animate={{ scale: 1, opacity: 1 }} exit={{ scale: 0.95, opacity: 0 }} className="bg-white rounded-3xl w-full max-w-3xl shadow-2xl overflow-hidden my-8">
              <div className="px-8 py-4 bg-gray-50 border-b flex justify-between items-center">
                <span className="text-xs font-semibold text-gray-500">Bulletin de paie</span>
                <button onClick={() => setViewingFiche(null)} className="px-4 py-2 bg-black/5 rounded-xl text-xs font-semibold">Fermer</button>
              </div>
              <div id="print-payslip" className="p-10 space-y-8 text-black print-target bg-white">
                <DocumentHeader
                  title="Bulletin de Paie"
                  subtitle={`Période : ${viewingFiche.periodeLibelle}`}
                />
                <div className="grid md:grid-cols-2 gap-6 text-xs bg-gray-50 p-6 rounded-xl">
                  <div className="space-y-2">
                    <h3 className="font-bold text-gray-500 uppercase text-[11px]">Collaborateur</h3>
                    <p><span className="text-gray-400">Nom :</span> <strong>{viewingFiche.employe?.nom} {viewingFiche.employe?.prenom}</strong></p>
                    <p><span className="text-gray-400">Matricule :</span> {viewingFiche.employe?.matricule}</p>
                    <p><span className="text-gray-400">Département :</span> {viewingFiche.employe?.departementLibelle}</p>
                    <p><span className="text-gray-400">Poste :</span> {viewingFiche.employe?.posteLibelle}</p>
                  </div>
                  <div className="space-y-2">
                    <h3 className="font-bold text-gray-500 uppercase text-[11px]">Détails</h3>
                    <p><span className="text-gray-400">N° :</span> {viewingFiche.id}</p>
                    <p><span className="text-gray-400">Généré le :</span> {viewingFiche.dateGeneration}</p>
                  </div>
                </div>
                <table className="w-full text-xs">
                  <thead>
                    <tr className="border-b border-black font-bold uppercase text-[10px]">
                      <th className="pb-2 text-left">Rubrique</th>
                      <th className="pb-2 text-right">Montant</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100">
                    <tr><td className="py-2 font-bold">Salaire Brut Global</td><td className="py-2 text-right font-semibold">{formatGNF(viewingFiche.salaireBrut)}</td></tr>
                    <tr><td className="py-2">CNSS ({config?.tauxCnss ?? 5}%)</td><td className="py-2 text-right text-red-600">-{formatGNF(viewingFiche.cotisationCnss)}</td></tr>
                    <tr><td className="py-2">RTS ({config?.tauxRts ?? 10}%)</td><td className="py-2 text-right text-red-600">-{formatGNF(viewingFiche.impotRts)}</td></tr>
                  </tbody>
                </table>
                <div className="bg-black text-white p-5 rounded-xl flex justify-between items-center">
                  <span className="font-bold text-xs uppercase">Net à Payer</span>
                  <span className="text-xl font-black">{formatGNF(viewingFiche.salaireNet)}</span>
                </div>
                <div className="flex items-center gap-4 pt-4 border-t border-dashed">
                  <div className="p-2 bg-white border rounded-lg">
                    <QRCodeSVG value={viewingFiche.qrCodeToken} size={64} />
                  </div>
                  <div className="text-[9px] text-gray-500">
                    <span className="font-bold text-black block">Vérification anti-fraude</span>
                    <code className="font-mono text-black font-bold">{viewingFiche.qrCodeToken}</code>
                  </div>
                </div>
              </div>
              <div className="px-8 py-5 bg-gray-50 border-t">
                <DocumentExportButtons
                  elementId="print-payslip"
                  basename={`bulletin-${viewingFiche.employe?.matricule || viewingFiche.id}-${viewingFiche.periodeMois}-${viewingFiche.periodeAnnee}`}
                  variant="document"
                  className="justify-center"
                />
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </div>
  );
}
