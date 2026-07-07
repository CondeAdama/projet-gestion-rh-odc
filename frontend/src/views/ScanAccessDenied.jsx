import { Scan, LogIn } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function ScanAccessDenied() {
  const { logout, user } = useAuth();
  const navigate = useNavigate();

  const changerCompte = () => {
    logout();
    navigate('/login', { state: { from: { pathname: '/scan' } }, replace: true });
  };

  return (
    <div className="min-h-screen bg-[#0A0A0B] text-white flex flex-col items-center justify-center p-6 text-center gap-4">
      <Scan size={48} className="text-white/30" />
      <h1 className="text-lg font-bold">Accès pointage refusé</h1>
      <p className="text-sm text-white/60 max-w-sm">
        Le compte <span className="text-white/80">{user?.email}</span> n&apos;a pas le droit de scanner les présences.
        Utilisez un compte <strong>Réception</strong>, <strong>RH</strong> ou <strong>Administrateur</strong>.
      </p>
      <button
        onClick={changerCompte}
        className="flex items-center gap-2 px-6 py-3 bg-white text-black rounded-xl text-sm font-bold"
      >
        <LogIn size={16} /> Changer de compte
      </button>
    </div>
  );
}
