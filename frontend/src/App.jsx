import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { ConfigProvider } from './context/ConfigContext';
import ProtectedRoute from './components/ProtectedRoute';
import Login from './views/Login';
import ActivationCompte from './views/ActivationCompte';
import MonProfil from './views/MonProfil';
import Dashboard from './views/Dashboard';
import AccueilDashboard from './views/AccueilDashboard';
import GestionRoles from './views/GestionRoles';
import GestionReferentiels from './views/GestionReferentiels';
import GestionEmployes from './views/GestionEmployes';
import GestionContrats from './views/GestionContrats';
import GestionConges from './views/GestionConges';
import PointageQR from './views/PointageQR';
import PointageMobile from './views/PointageMobile';
import GestionPaie from './views/GestionPaie';
import GestionVisites from './views/GestionVisites';
import RapportsRH from './views/RapportsRH';
import GestionUtilisateurs from './views/GestionUtilisateurs';
import GestionConfiguration from './views/GestionConfiguration';
import MotDePasseOublie from './views/MotDePasseOublie';
import ReinitialiserMotDePasse from './views/ReinitialiserMotDePasse';

export default function App() {
  return (
    <AuthProvider>
      <ConfigProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/activer" element={<ActivationCompte />} />
          <Route path="/mot-de-passe-oublie" element={<MotDePasseOublie />} />
          <Route path="/reinitialiser-mot-de-passe" element={<ReinitialiserMotDePasse />} />
          <Route path="/inscription" element={<Navigate to="/activer" replace />} />
          <Route path="/scan" element={
            <ProtectedRoute module="PRESENCES" action="AJOUTER"><PointageMobile /></ProtectedRoute>
          } />
          <Route path="/dashboard" element={
            <ProtectedRoute><Dashboard /></ProtectedRoute>
          }>
            <Route index element={<AccueilDashboard />} />
            <Route path="roles" element={
              <ProtectedRoute module="ROLES" action="AFFICHER"><GestionRoles /></ProtectedRoute>
            } />
            <Route path="referentiels" element={
              <ProtectedRoute module="REFERENTIELS" action="AFFICHER"><GestionReferentiels /></ProtectedRoute>
            } />
            <Route path="employes" element={
              <ProtectedRoute module="EMPLOYES" action="AFFICHER"><GestionEmployes /></ProtectedRoute>
            } />
            <Route path="contrats" element={
              <ProtectedRoute module="CONTRATS" action="AFFICHER"><GestionContrats /></ProtectedRoute>
            } />
            <Route path="conges" element={
              <ProtectedRoute module="CONGES" action="AFFICHER"><GestionConges /></ProtectedRoute>
            } />
            <Route path="pointage" element={
              <ProtectedRoute module="PRESENCES" action="AFFICHER"><PointageQR /></ProtectedRoute>
            } />
            <Route path="paie" element={
              <ProtectedRoute module="PAIES" action="AFFICHER"><GestionPaie /></ProtectedRoute>
            } />
            <Route path="visites" element={
              <ProtectedRoute module="VISITES" action="AFFICHER"><GestionVisites /></ProtectedRoute>
            } />
            <Route path="rapports" element={
              <ProtectedRoute module="RAPPORTS" action="AFFICHER"><RapportsRH /></ProtectedRoute>
            } />
            <Route path="utilisateurs" element={
              <ProtectedRoute module="UTILISATEURS" action="AFFICHER"><GestionUtilisateurs /></ProtectedRoute>
            } />
            <Route path="profil" element={<MonProfil />} />
            <Route path="configuration" element={
              <ProtectedRoute module="CONFIGURATION" action="AFFICHER"><GestionConfiguration /></ProtectedRoute>
            } />
          </Route>
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </BrowserRouter>
      </ConfigProvider>
    </AuthProvider>
  );
}
