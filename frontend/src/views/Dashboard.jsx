import { useState, useEffect } from 'react';
import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import {
  LayoutDashboard, Shield, BookOpen, LogOut, Users, FileText, Calendar,
  Scan, Wallet, UserPlus, BarChart3, Settings, UserCircle, Menu, X,
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import CompanyBrand from '../components/brand/CompanyBrand';
import { Avatar } from '../components/ui/Display';

const menuItems = [
  { to: '/dashboard', label: 'Tableau de bord', icon: LayoutDashboard, module: null },
  { to: '/dashboard/employes', label: 'Employés', icon: Users, module: 'EMPLOYES', action: 'AFFICHER' },
  { to: '/dashboard/contrats', label: 'Contrats', icon: FileText, module: 'CONTRATS', action: 'AFFICHER' },
  { to: '/dashboard/conges', label: 'Congés', icon: Calendar, module: 'CONGES', action: 'AFFICHER' },
  { to: '/dashboard/pointage', label: 'Pointage QR', icon: Scan, module: 'PRESENCES', action: 'AFFICHER' },
  { to: '/dashboard/paie', label: 'Paie', icon: Wallet, module: 'PAIES', action: 'AFFICHER' },
  { to: '/dashboard/visites', label: 'Visites', icon: UserPlus, module: 'VISITES', action: 'AFFICHER' },
  { to: '/dashboard/rapports', label: 'Rapports RH', icon: BarChart3, module: 'RAPPORTS', action: 'AFFICHER' },
  { to: '/dashboard/configuration', label: 'Configuration', icon: Settings, module: 'CONFIGURATION', action: 'AFFICHER' },
  { to: '/dashboard/utilisateurs', label: 'Utilisateurs', icon: Users, module: 'UTILISATEURS', action: 'AFFICHER' },
  { to: '/dashboard/roles', label: 'Rôles & Permissions', icon: Shield, module: 'ROLES', action: 'AFFICHER' },
  { to: '/dashboard/referentiels', label: 'Référentiels', icon: BookOpen, module: 'REFERENTIELS', action: 'AFFICHER' },
];

function SidebarContent({ visibleMenu, user, onNavigate, onLogout, onClose }) {
  const isAdmin = user?.roles?.includes('ADMINISTRATEUR');
  const prenom = user?.prenom || user?.nomComplet?.split(' ')?.[0] || '';
  const nom = user?.nom || user?.nomComplet?.split(' ')?.slice(1)?.join(' ') || '';

  return (
    <>
      <div className="mb-8">
        <CompanyBrand size="sm" />
      </div>

      <nav className="flex-1 space-y-1 overflow-y-auto">
        {visibleMenu.map(({ to, label, icon: Icon }) => (
          <NavLink
            key={to}
            to={to}
            end={to === '/dashboard'}
            onClick={onClose}
            className={({ isActive }) =>
              `flex items-center gap-3 px-4 py-2.5 rounded-xl text-sm font-medium transition-all duration-200 ${
                isActive
                  ? 'bg-gradient-to-r from-[#1e3a5f] to-[#2563eb] text-white shadow-md'
                  : 'text-gray-600 hover:bg-white/80 hover:text-[#1D1D1F]'
              }`
            }
          >
            <Icon size={18} /> {label}
          </NavLink>
        ))}
      </nav>

      <div className="border-t border-gray-200/80 pt-4 mt-4">
        <button
          onClick={() => { onNavigate('/dashboard/profil'); onClose?.(); }}
          className="flex items-center gap-3 w-full mb-3 p-2 rounded-xl hover:bg-white/80 transition text-left"
        >
          <div className="relative shrink-0">
            <Avatar src={user?.photoUrl} prenom={prenom} nom={nom} size="sm" />
            {isAdmin && (
              <span
                className="absolute -bottom-0.5 -right-0.5 w-4 h-4 bg-amber-500 rounded-full flex items-center justify-center ring-2 ring-white shadow-sm"
                title="Administrateur Système"
              >
                <Shield size={9} className="text-white" />
              </span>
            )}
          </div>
          <div className="min-w-0 flex-1">
            <p className="text-sm font-semibold truncate text-[#1D1D1F] flex items-center gap-1.5 flex-wrap">
              <span className="truncate">{user?.nomComplet || user?.email}</span>
              {isAdmin && (
                <span className="text-[9px] px-1.5 py-0.5 bg-amber-100 text-amber-800 rounded font-bold uppercase tracking-wide shrink-0">
                  Admin
                </span>
              )}
            </p>
            <p className="text-xs text-gray-500 truncate flex items-center gap-1">
              <UserCircle size={10} /> Mon profil
            </p>
          </div>
        </button>
        <button
          onClick={onLogout}
          className="flex items-center gap-2 w-full px-3 py-2 text-sm text-gray-500 hover:text-red-500 hover:bg-red-50 rounded-xl transition"
        >
          <LogOut size={16} /> Déconnexion
        </button>
      </div>
    </>
  );
}

export default function Dashboard() {
  const { user, logout, hasPermission, refreshProfil } = useAuth();
  const navigate = useNavigate();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  useEffect(() => {
    if (user) refreshProfil();
  }, []);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const visibleMenu = menuItems.filter(item =>
    !item.module || hasPermission(item.module, item.action)
  );

  return (
    <div className="min-h-screen flex bg-[#F5F5F7]">
      {/* Sidebar desktop */}
      <aside className="hidden lg:flex w-64 shrink-0 bg-white/70 backdrop-blur-2xl border-r border-white/30 p-6 flex-col shadow-sm">
        <SidebarContent
          visibleMenu={visibleMenu}
          user={user}
          onNavigate={navigate}
          onLogout={handleLogout}
        />
      </aside>

      {/* Sidebar mobile overlay */}
      <AnimatePresence>
        {sidebarOpen && (
          <>
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="fixed inset-0 bg-black/40 backdrop-blur-sm z-40 lg:hidden"
              onClick={() => setSidebarOpen(false)}
            />
            <motion.aside
              initial={{ x: -280 }}
              animate={{ x: 0 }}
              exit={{ x: -280 }}
              transition={{ type: 'spring', damping: 28, stiffness: 320 }}
              className="fixed inset-y-0 left-0 w-72 z-50 bg-white/95 backdrop-blur-2xl border-r border-white/30 p-6 flex flex-col shadow-2xl lg:hidden"
            >
              <button
                onClick={() => setSidebarOpen(false)}
                className="absolute top-4 right-4 p-2 rounded-xl hover:bg-black/5"
                aria-label="Fermer le menu"
              >
                <X size={20} />
              </button>
              <SidebarContent
                visibleMenu={visibleMenu}
                user={user}
                onNavigate={navigate}
                onLogout={handleLogout}
                onClose={() => setSidebarOpen(false)}
              />
            </motion.aside>
          </>
        )}
      </AnimatePresence>

      <div className="flex-1 flex flex-col min-w-0">
        {/* Header mobile */}
        <header className="lg:hidden sticky top-0 z-30 flex items-center gap-4 px-4 py-3 bg-white/80 backdrop-blur-xl border-b border-white/40 shadow-sm">
          <button
            onClick={() => setSidebarOpen(true)}
            className="p-2.5 rounded-xl bg-black/5 hover:bg-black/10 transition"
            aria-label="Ouvrir le menu"
          >
            <Menu size={20} />
          </button>
          <CompanyBrand size="sm" showSubtitle={false} />
        </header>

        <main className="flex-1 p-4 sm:p-6 lg:p-8 overflow-y-auto page-enter">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
