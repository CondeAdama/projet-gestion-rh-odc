import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { Shield, Plus, Edit2, Trash2, X, Save, Star } from 'lucide-react';
import api from '../services/api';
import { getApiError } from '../utils/validation';

const ACTIONS = ['AFFICHER', 'AFFICHER_AUTRUI', 'AJOUTER', 'MODIFIER', 'SUPPRIMER'];
const ACTION_LABELS = {
  AFFICHER: 'Afficher',
  AFFICHER_AUTRUI: 'Infos des autres',
  AJOUTER: 'Ajouter',
  MODIFIER: 'Modifier',
  SUPPRIMER: 'Supprimer',
};

export default function GestionRoles() {
  const [roles, setRoles] = useState([]);
  const [modules, setModules] = useState([]);
  const [actions, setActions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modal, setModal] = useState(null);
  const [form, setForm] = useState({ code: '', libelle: '', description: '', permissions: {} });
  const [editingId, setEditingId] = useState(null);
  const [error, setError] = useState(null);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [rolesRes, modulesRes] = await Promise.all([
        api.get('/roles'),
        api.get('/roles/modules'),
      ]);
      setRoles(rolesRes.data);
      setModules(modulesRes.data.modules || []);
      setActions(modulesRes.data.actions || ACTIONS);
    } catch {
      setError('Erreur de chargement');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchData(); }, []);

  const openCreate = () => {
    setForm({ code: '', libelle: '', description: '', permissions: {} });
    setEditingId(null);
    setModal('form');
  };

  const openEdit = (role) => {
    setForm({
      code: role.code,
      libelle: role.libelle,
      description: role.description || '',
      permissions: role.matrice || {},
    });
    setEditingId(role.id);
    setModal('form');
  };

  const togglePermission = (module, action) => {
    setForm(prev => {
      const perms = { ...prev.permissions };
      const moduleActions = new Set(perms[module] || []);
      if (moduleActions.has(action)) {
        moduleActions.delete(action);
      } else {
        moduleActions.add(action);
      }
      perms[module] = [...moduleActions];
      return { ...prev, permissions: perms };
    });
  };

  const handleSave = async (e) => {
    e.preventDefault();
    if (!form.code.trim()) { setError('Le code est obligatoire'); return; }
    if (!form.libelle.trim()) { setError('Le libellé est obligatoire'); return; }
    setError(null);
    try {
      const payload = {
        ...form,
        code: form.code.trim().toUpperCase(),
        libelle: form.libelle.trim(),
        description: form.description?.trim() || '',
      };
      if (editingId) {
        await api.put(`/roles/${editingId}`, payload);
      } else {
        await api.post('/roles', payload);
      }
      setModal(null);
      fetchData();
    } catch (err) {
      setError(getApiError(err, 'Erreur de sauvegarde'));
    }
  };

  const handleDelete = async (id) => {
    if (!confirm('Supprimer ce rôle ? (suppression logique)')) return;
    try {
      await api.delete(`/roles/${id}`);
      fetchData();
    } catch (err) {
      setError(getApiError(err, 'Erreur'));
    }
  };

  const handleSetDefault = async (role) => {
    if (role.parDefaut || role.code === 'ADMINISTRATEUR') return;
    try {
      await api.put(`/roles/${role.id}/par-defaut`);
      fetchData();
    } catch (err) {
      setError(getApiError(err, 'Impossible de définir ce rôle par défaut'));
    }
  };

  const roleParDefaut = roles.find(r => r.parDefaut);

  if (loading) return <div className="p-8 text-center text-gray-500">Chargement...</div>;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-bold flex items-center gap-2">
            <Shield size={22} /> Gestion des Rôles
          </h2>
          <p className="text-sm text-gray-500 mt-1">
            {roles.length} rôle(s) — Autorisation granulaire par module
            {roleParDefaut && (
              <span className="ml-2 text-emerald-700 font-medium">
                · Par défaut : {roleParDefaut.libelle} ({roleParDefaut.code})
              </span>
            )}
          </p>
        </div>
        <button onClick={openCreate} className="flex items-center gap-2 px-4 py-2 bg-black text-white rounded-xl text-sm font-semibold">
          <Plus size={16} /> Nouveau rôle
        </button>
      </div>

      <div className="grid gap-4">
        {roles.map(role => (
          <motion.div
            key={role.id}
            layout
            className="bg-white/60 backdrop-blur border border-white/20 rounded-2xl p-5"
          >
            <div className="flex items-start justify-between">
              <div>
                <div className="flex items-center gap-2 flex-wrap">
                  <h3 className="font-semibold">{role.libelle}</h3>
                  <span className="text-xs bg-gray-100 px-2 py-0.5 rounded-full">{role.code}</span>
                  {role.systeme && <span className="text-xs bg-blue-100 text-blue-700 px-2 py-0.5 rounded-full">Système</span>}
                  {role.parDefaut && (
                    <span className="text-xs bg-emerald-100 text-emerald-700 px-2 py-0.5 rounded-full flex items-center gap-1">
                      <Star size={10} fill="currentColor" /> Par défaut
                    </span>
                  )}
                </div>
                {role.description && <p className="text-xs text-gray-500 mt-1">{role.description}</p>}
                {!role.parDefaut && role.code !== 'ADMINISTRATEUR' && (
                  <button
                    type="button"
                    onClick={() => handleSetDefault(role)}
                    className="mt-2 text-xs text-emerald-700 hover:text-emerald-900 font-semibold flex items-center gap-1"
                  >
                    <Star size={12} /> Définir comme rôle par défaut (nouveaux employés)
                  </button>
                )}
              </div>
              {!role.systeme && (
                <div className="flex gap-2">
                  <button onClick={() => openEdit(role)} className="p-2 hover:bg-gray-100 rounded-lg"><Edit2 size={16} /></button>
                  <button onClick={() => handleDelete(role.id)} className="p-2 hover:bg-red-50 text-red-500 rounded-lg"><Trash2 size={16} /></button>
                </div>
              )}
            </div>

            {role.matrice && (
              <div className="mt-4 overflow-x-auto">
                <table className="w-full text-xs">
                  <thead>
                    <tr className="border-b border-gray-200">
                      <th className="text-left py-2 pr-4 font-semibold text-gray-600">Module</th>
                      {actions.map(a => (
                        <th key={a} className="text-center py-2 px-2 font-semibold text-gray-600">{ACTION_LABELS[a] || a}</th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {Object.keys(role.matrice).map(module => (
                      <tr key={module} className="border-b border-gray-100">
                        <td className="py-2 pr-4 font-medium">{module}</td>
                        {actions.map(action => (
                          <td key={action} className="text-center py-2 px-2">
                            {role.matrice[module]?.includes(action) ? (
                              <span className="inline-block w-4 h-4 bg-green-500 rounded-full" />
                            ) : (
                              <span className="inline-block w-4 h-4 bg-gray-200 rounded-full" />
                            )}
                          </td>
                        ))}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </motion.div>
        ))}
      </div>

      <AnimatePresence>
        {modal === 'form' && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 bg-black/30 backdrop-blur-sm flex items-center justify-center z-50 p-4"
            onClick={() => setModal(null)}
          >
            <motion.div
              initial={{ scale: 0.95 }}
              animate={{ scale: 1 }}
              exit={{ scale: 0.95 }}
              className="bg-white rounded-2xl p-6 w-full max-w-2xl max-h-[90vh] overflow-y-auto"
              onClick={e => e.stopPropagation()}
            >
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-bold">{editingId ? 'Modifier le rôle' : 'Nouveau rôle'}</h3>
                <button onClick={() => setModal(null)}><X size={20} /></button>
              </div>

              {error && <p className="text-red-500 text-sm mb-3">{error}</p>}

              <form onSubmit={handleSave} className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="text-xs font-semibold text-gray-600">Code</label>
                    <input
                      value={form.code}
                      onChange={e => setForm({ ...form, code: e.target.value })}
                      disabled={!!editingId}
                      required
                      placeholder="COMPTABLE"
                      className="w-full mt-1 px-3 py-2 border rounded-xl text-sm disabled:bg-gray-100"
                    />
                  </div>
                  <div>
                    <label className="text-xs font-semibold text-gray-600">Libellé</label>
                    <input
                      value={form.libelle}
                      onChange={e => setForm({ ...form, libelle: e.target.value })}
                      required
                      placeholder="Comptable Paie"
                      className="w-full mt-1 px-3 py-2 border rounded-xl text-sm"
                    />
                  </div>
                </div>

                <div>
                  <label className="text-xs font-semibold text-gray-600">Description</label>
                  <input
                    value={form.description}
                    onChange={e => setForm({ ...form, description: e.target.value })}
                    placeholder="Gestion bulletins et déclarations CNSS"
                    className="w-full mt-1 px-3 py-2 border rounded-xl text-sm"
                  />
                </div>

                <div>
                  <label className="text-xs font-semibold text-gray-600 mb-2 block">Matrice des permissions</label>
                  <div className="overflow-x-auto border rounded-xl">
                    <table className="w-full text-xs">
                      <thead>
                        <tr className="bg-gray-50">
                          <th className="text-left p-2 font-semibold">Module</th>
                          {ACTIONS.map(a => (
                            <th key={a} className="text-center p-2 font-semibold">{ACTION_LABELS[a]}</th>
                          ))}
                        </tr>
                      </thead>
                      <tbody>
                        {modules.map(module => (
                          <tr key={module} className="border-t">
                            <td className="p-2 font-medium">{module}</td>
                            {actions.map(action => (
                              <td key={action} className="text-center p-2">
                                <input
                                  type="checkbox"
                                  checked={form.permissions[module]?.includes(action) || false}
                                  onChange={() => togglePermission(module, action)}
                                  className="w-4 h-4 accent-black"
                                />
                              </td>
                            ))}
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>

                <button type="submit" className="flex items-center gap-2 px-6 py-2.5 bg-black text-white rounded-xl text-sm font-semibold">
                  <Save size={16} /> Enregistrer
                </button>
              </form>
            </motion.div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
