import { useState, useEffect, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import {
  Scan, MapPin, RefreshCw, CheckCircle2, AlertCircle, Camera,
  Keyboard, LogOut, ArrowLeft
} from 'lucide-react';
import { Html5QrcodeScanner } from 'html5-qrcode';
import { useNavigate } from 'react-router-dom';
import api from '../services/api';
import { getApiError } from '../utils/validation';
import { normaliserMatriculeScan } from '../utils/scan';
import { useAuth } from '../context/AuthContext';
  const navigate = useNavigate();

  const [localisations, setLocalisations] = useState([]);
  const [selectedLocal, setSelectedLocal] = useState('');
  const [matriculeInput, setMatriculeInput] = useState('');
  const [scanning, setScanning] = useState(false);
  const [scanResult, setScanResult] = useState(null);
  const [cameraMode, setCameraMode] = useState(true);
  const [recentCount, setRecentCount] = useState(0);
  const [loadError, setLoadError] = useState(null);

  useEffect(() => {
    (async () => {
      try {
        const [locRes, presRes] = await Promise.all([
          api.get('/referentiels/localisations'),
          api.get('/presences/aujourdhui'),
        ]);
        const locs = locRes.data.filter(l => l.statut === 'ACTIF');
        setLocalisations(locs);
        if (locs.length > 0) setSelectedLocal(String(locs[0].id));
        else setLoadError('Aucune localisation active configurée');
        setRecentCount(presRes.data.length);
      } catch (err) {
        setLoadError(getApiError(err, 'Erreur de chargement des localisations'));
      }
    })();
  }, []);

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
      setRecentCount(c => c + 1);
      if (navigator.vibrate) navigator.vibrate(200);
    } catch (err) {
      setScanResult({
        success: false,
        message: getApiError(err, 'Scan refusé.'),
      });
      if (navigator.vibrate) navigator.vibrate([100, 50, 100]);
    } finally {
      setScanning(false);
    }
  }, [selectedLocal]);

  useEffect(() => {
    if (!cameraMode) return;
    const scanner = new Html5QrcodeScanner('mobile-reader', {
      qrbox: { width: 280, height: 280 },
      fps: 10,
      aspectRatio: 1,
    });
    scanner.render(
      (decoded) => { scanner.clear().catch(() => {}); doScan(decoded); },
      () => {}
    );
    return () => { scanner.clear().catch(() => {}); };
  }, [cameraMode, doScan]);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="min-h-screen bg-[#0A0A0B] text-white flex flex-col">
      <header className="flex items-center justify-between px-4 py-3 border-b border-white/10 bg-black/40 backdrop-blur-lg sticky top-0 z-10">
        <button onClick={() => navigate('/dashboard/pointage')} className="p-2 rounded-xl hover:bg-white/10">
          <ArrowLeft size={20} />
        </button>
        <div className="text-center">
          <h1 className="text-sm font-bold tracking-wide">POINTAGE MOBILE</h1>
          <p className="text-[10px] text-white/50">MINERVA GROUP</p>
        </div>
        <button onClick={handleLogout} className="p-2 rounded-xl hover:bg-white/10">
          <LogOut size={18} />
        </button>
      </header>

      <div className="px-4 py-3 flex items-center gap-2">
        <MapPin size={14} className="text-white/50 flex-shrink-0" />
        {localisations.length === 0 ? (
          <p className="text-xs text-red-400 flex-1">{loadError || 'Aucune localisation disponible'}</p>
        ) : (
          <select
            value={selectedLocal}
            onChange={e => setSelectedLocal(e.target.value)}
            required
            className="flex-1 bg-white/10 border border-white/10 rounded-xl px-3 py-2.5 text-sm font-medium outline-none"
          >
            {localisations.map(l => (
              <option key={l.id} value={l.id} className="text-black">{l.nom} — {l.ville}</option>
            ))}
          </select>
        )}
        <button onClick={() => setCameraMode(m => !m)} className="p-2.5 bg-white/10 rounded-xl">
          <Camera size={18} className={cameraMode ? 'text-emerald-400' : 'text-white/40'} />
        </button>
      </div>

      <div className="flex-1 flex flex-col px-4 pb-6 gap-4">
        {cameraMode ? (
          <div className="flex-1 min-h-[320px] bg-black rounded-3xl overflow-hidden border border-white/10 relative">
            <div id="mobile-reader" className="w-full h-full" />
            <div className="absolute bottom-3 left-0 right-0 text-center pointer-events-none">
              <span className="text-[10px] text-white/40 bg-black/60 px-3 py-1 rounded-full">
                Placez le badge dans le cadre
              </span>
            </div>
          </div>
        ) : (
          <div className="flex-1 min-h-[200px] bg-gradient-to-br from-gray-900 to-gray-800 rounded-3xl flex flex-col items-center justify-center border border-white/10 gap-4">
            <Scan size={48} className="text-white/30" />
            <button
              onClick={() => setCameraMode(true)}
              className="flex items-center gap-2 px-6 py-3 bg-blue-600 rounded-xl text-sm font-semibold"
            >
              <Camera size={18} /> Activer la caméra
            </button>
          </div>
        )}

        <form
          onSubmit={(e) => { e.preventDefault(); doScan(matriculeInput); }}
          className="bg-white/5 border border-white/10 rounded-2xl p-4 space-y-3"
        >
          <label className="text-xs font-semibold text-white/60 flex items-center gap-1.5">
            <Keyboard size={14} /> Saisie manuelle
          </label>
          <input
            type="text"
            placeholder="SNG-2026-001"
            value={matriculeInput}
            onChange={e => setMatriculeInput(e.target.value)}
            required
            minLength={3}
            maxLength={50}
            className="w-full px-4 py-3.5 bg-white/10 border border-white/10 rounded-xl text-sm font-mono outline-none focus:ring-2 focus:ring-blue-500/50"
          />
          <button
            type="submit"
            disabled={scanning || !selectedLocal}
            className="w-full py-3.5 bg-white text-black rounded-xl text-sm font-bold flex items-center justify-center gap-2 disabled:opacity-50"
          >
            {scanning ? <RefreshCw className="animate-spin" size={16} /> : 'Valider'}
          </button>
        </form>

        <AnimatePresence mode="wait">
          {scanResult && (
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0 }}
              className={`p-4 rounded-2xl flex items-start gap-3 text-sm ${
                scanResult.success ? 'bg-emerald-500/20 border border-emerald-500/30' : 'bg-red-500/20 border border-red-500/30'
              }`}
            >
              {scanResult.success ? <CheckCircle2 size={20} className="text-emerald-400 flex-shrink-0" /> : <AlertCircle size={20} className="text-red-400 flex-shrink-0" />}
              <div>
                <p className="font-bold">{scanResult.success ? scanResult.typeScan || 'OK' : 'Erreur'}</p>
                <p className="text-white/70 text-xs mt-0.5">{scanResult.message}</p>
              </div>
            </motion.div>
          )}
        </AnimatePresence>

        <div className="text-center text-xs text-white/30 pt-2">
          {recentCount} pointage{recentCount !== 1 ? 's' : ''} aujourd'hui · {user?.nomComplet || user?.email}
        </div>
      </div>
    </div>
  );
}
