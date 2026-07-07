import { useAuth } from '../context/AuthContext';
import { Users, Shield, BookOpen, ArrowRight, FileText, Calendar, Scan, Wallet, UserPlus, BarChart3, Settings } from 'lucide-react';
import { Link } from 'react-router-dom';

export default function AccueilDashboard() {
  const { user, hasPermission } = useAuth();

  const cards = [
    {
      title: 'Employés',
      desc: 'Gérer les collaborateurs, statuts et affectations',
      icon: Users,
      to: '/dashboard/employes',
      module: 'EMPLOYES',
      action: 'AFFICHER',
      color: 'from-blue-600 to-indigo-600',
    },
    {
      title: 'Contrats',
      desc: 'Contrats de travail, salaires et résiliations',
      icon: FileText,
      to: '/dashboard/contrats',
      module: 'CONTRATS',
      action: 'AFFICHER',
      color: 'from-violet-600 to-purple-600',
    },
    {
      title: 'Congés',
      desc: 'Demandes, approbations et suivi des absences',
      icon: Calendar,
      to: '/dashboard/conges',
      module: 'CONGES',
      action: 'AFFICHER',
      color: 'from-amber-500 to-orange-500',
    },
    {
      title: 'Pointage QR',
      desc: 'Scan badge, entrées/sorties et transfert mobile',
      icon: Scan,
      to: '/dashboard/pointage',
      module: 'PRESENCES',
      action: 'AFFICHER',
      color: 'from-cyan-500 to-blue-600',
    },
    {
      title: 'Paie',
      desc: 'Bulletins mensuels, CNSS, RTS et token QR',
      icon: Wallet,
      to: '/dashboard/paie',
      module: 'PAIES',
      action: 'AFFICHER',
      color: 'from-emerald-500 to-green-600',
    },
    {
      title: 'Visites',
      desc: 'Visiteurs, badges et suivi des entrées',
      icon: UserPlus,
      to: '/dashboard/visites',
      module: 'VISITES',
      action: 'AFFICHER',
      color: 'from-rose-500 to-pink-600',
    },
    {
      title: 'Rapports RH',
      desc: 'KPIs, synthèse effectifs, congés et paie',
      icon: BarChart3,
      to: '/dashboard/rapports',
      module: 'RAPPORTS',
      action: 'AFFICHER',
      color: 'from-indigo-500 to-violet-600',
    },
    {
      title: 'Configuration',
      desc: 'Identité entreprise, logo et coordonnées',
      icon: Settings,
      to: '/dashboard/configuration',
      module: 'CONFIGURATION',
      action: 'AFFICHER',
      color: 'from-slate-600 to-gray-800',
    },
    {
      title: 'Rôles & Permissions',
      desc: 'Gérer l\'autorisation granulaire par module',
      icon: Shield,
      to: '/dashboard/roles',
      module: 'ROLES',
      action: 'AFFICHER',
      color: 'from-purple-600 to-indigo-600',
    },
    {
      title: 'Référentiels',
      desc: 'Postes, départements et localisations',
      icon: BookOpen,
      to: '/dashboard/referentiels',
      module: 'REFERENTIELS',
      action: 'AFFICHER',
      color: 'from-blue-600 to-cyan-600',
    },
  ];

  return (
    <div className="space-y-8">
      <div>
        <h2 className="text-2xl font-bold">
          Bienvenue, {user?.nomComplet || user?.email}
        </h2>
        <p className="text-gray-500 mt-1">
          Application Gestion RH — MINERVA GROUP
        </p>
      </div>

      <div className="grid md:grid-cols-2 xl:grid-cols-4 gap-6">
        {cards.filter(c => !c.module || hasPermission(c.module, c.action)).map(card => (
          <div
            key={card.title}
            className="bg-white/60 backdrop-blur border border-white/20 rounded-2xl p-6 hover:shadow-lg transition"
          >
            <div className={`w-12 h-12 rounded-xl bg-gradient-to-br ${card.color} flex items-center justify-center text-white mb-4`}>
              <card.icon size={22} />
            </div>
            <h3 className="font-semibold text-lg">{card.title}</h3>
            <p className="text-sm text-gray-500 mt-1">{card.desc}</p>
            {card.to ? (
              <Link to={card.to} className="flex items-center gap-1 text-sm font-semibold mt-4 hover:gap-2 transition-all">
                Accéder <ArrowRight size={14} />
              </Link>
            ) : null}
          </div>
        ))}
      </div>

      <div className="bg-white/60 backdrop-blur border border-white/20 rounded-2xl p-6">
        <h3 className="font-semibold mb-3">Vos permissions</h3>
        <div className="flex flex-wrap gap-2">
          {user?.roles?.map(role => (
            <span key={role} className="px-3 py-1 bg-black text-white text-xs rounded-full">{role}</span>
          ))}
        </div>
      </div>
    </div>
  );
}
