import { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import {
  Users, Calendar, FileText, Clock, RefreshCw, TrendingUp, Activity, ArrowRight, Wallet, UserPlus
} from 'lucide-react';
import api from '../services/api';
import { useConfig } from '../context/ConfigContext';
import { PageHeader, StatCard } from '../components/ui/Display';
import { formatGNF, MOIS_LABELS } from '../utils/format';

export default function RapportsRH() {
  const { config, logoUrl } = useConfig();
  const [rapport, setRapport] = useState(null);
  const [loading, setLoading] = useState(true);

  const fetchData = async () => {
    setLoading(true);
    try {
      const res = await api.get('/rapports/synthese');
      setRapport(res.data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchData(); }, []);

  const now = new Date();
  const mois = MOIS_LABELS[now.getMonth()];

  if (loading) {
    return (
      <div className="flex justify-center py-20">
        <RefreshCw className="animate-spin text-gray-400" size={32} />
      </div>
    );
  }

  const emp = rapport?.employes || {};
  const cong = rapport?.conges || {};
  const pres = rapport?.presences || {};
  const paie = rapport?.paie || {};
  const vis = rapport?.visites || {};

  return (
    <div className="space-y-8">
      <PageHeader
        title="Rapports RH"
        subtitle={`Synthèse globale — ${mois} ${now.getFullYear()}`}
        action={
          <button onClick={fetchData} className="p-3 bg-white/60 border rounded-xl">
            <RefreshCw size={18} className="text-gray-600" />
          </button>
        }
      />

      {/* En-tête document avec logo */}
      <div className="bg-white/60 border border-white/20 rounded-2xl p-6 flex items-center gap-4 print:block">
        {logoUrl ? (
          <img src={logoUrl} alt="Logo" className="w-16 h-16 object-contain rounded-xl border bg-white p-1" />
        ) : (
          <div className="w-16 h-16 rounded-xl bg-[#1a4a8e] text-white flex items-center justify-center text-xs font-bold">RH</div>
        )}
        <div>
          <h2 className="text-xl font-bold">{config?.nomEntreprise || 'MINERVA GROUP'}</h2>
          <p className="text-sm text-gray-500">Rapport de synthèse des ressources humaines</p>
          <p className="text-xs text-gray-400 mt-1">
            Généré le {now.toLocaleDateString('fr-FR')}
            {config?.numeroCnss && <> · CNSS : {config.numeroCnss}</>}
          </p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-5">
        <StatCard label="Employés actifs" value={emp.actifs ?? 0} icon={Users} color="from-blue-600 to-indigo-600" />
        <StatCard label="Congés en attente" value={cong.enAttente ?? 0} icon={Calendar} color="from-amber-500 to-orange-500" />
        <StatCard label="Sur site" value={pres.presents ?? 0} icon={Activity} color="from-cyan-500 to-blue-600" />
        <StatCard label="Masse salariale" value={formatGNF(paie.masseSalariale)} icon={Wallet} color="from-emerald-500 to-green-600" />
      </div>

      <div className="grid md:grid-cols-2 gap-6">
        {[
          { title: 'Effectifs', icon: Users, items: [
            ['Total collaborateurs', emp.total],
            ['Actifs', emp.actifs],
            ['Suspendus', emp.suspendus],
            ['Licenciés', emp.licencies],
          ]},
          { title: 'Congés', icon: Calendar, items: [
            ['En attente', cong.enAttente],
            ['Approuvés', cong.approuves],
            ['Refusés', cong.refuses],
          ]},
          { title: 'Présences du jour', icon: Clock, items: [
            ['Passages', pres.total ?? pres.aujourdhui],
            ['Employés pointés', pres.employes],
            ['En règle', pres.enRegle],
            ['Retards', pres.retards],
            ['Sur site', pres.presents],
          ]},
          { title: 'Visites', icon: UserPlus, items: [
            ['En cours', vis.enCours],
            ['Terminées', vis.terminees],
          ]},
          { title: 'Paie & cotisations', icon: Wallet, items: [
            ['Bulletins générés', paie.totalFiches],
            ['Masse salariale nette', formatGNF(paie.masseSalariale)],
            [`CNSS retenue (${paie.tauxCnss ?? 5}%)`, formatGNF(paie.totalCnss)],
            [`RTS retenus (${paie.tauxRts ?? 10}%)`, formatGNF(paie.totalRts)],
          ]},
        ].map(section => (
          <div key={section.title} className="bg-white/60 border border-white/20 rounded-2xl p-6 shadow-xl">
            <div className="flex items-center gap-2 mb-4">
              <section.icon size={18} className="text-gray-600" />
              <h3 className="font-semibold">{section.title}</h3>
            </div>
            <div className="space-y-2">
              {section.items.map(([label, val]) => (
                <div key={label} className="flex justify-between text-sm py-1.5 border-b border-black/5 last:border-0">
                  <span className="text-gray-500">{label}</span>
                  <span className="font-semibold">{val ?? 0}</span>
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>

      <div className="bg-white/60 border border-white/20 rounded-2xl p-6">
        <h3 className="font-semibold mb-4 flex items-center gap-2"><TrendingUp size={18} /> Accès rapides</h3>
        <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-3">
          {[
            { to: '/dashboard/employes', label: 'Employés', icon: Users },
            { to: '/dashboard/conges', label: 'Congés', icon: Calendar },
            { to: '/dashboard/pointage', label: 'Pointage', icon: Activity },
            { to: '/dashboard/paie', label: 'Paie', icon: FileText },
          ].map(link => (
            <Link key={link.to} to={link.to} className="flex items-center justify-between p-4 bg-gray-50 hover:bg-gray-100 rounded-xl border border-black/5 text-sm font-medium transition">
              <span className="flex items-center gap-2"><link.icon size={16} /> {link.label}</span>
              <ArrowRight size={14} className="text-gray-400" />
            </Link>
          ))}
        </div>
      </div>
    </div>
  );
}
