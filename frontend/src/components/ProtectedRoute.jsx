import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import ScanAccessDenied from '../views/ScanAccessDenied';

export default function ProtectedRoute({ children, module, action, deniedTo = 'dashboard' }) {
  const { user, loading, hasPermission } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin w-8 h-8 border-2 border-black border-t-transparent rounded-full" />
      </div>
    );
  }

  if (!user?.token) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  if (module && action && !hasPermission(module, action)) {
    if (deniedTo === 'scan-denied') {
      return <ScanAccessDenied />;
    }
    return <Navigate to="/dashboard" replace />;
  }

  return children;
}
