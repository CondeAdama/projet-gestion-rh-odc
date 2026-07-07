import { useState, useEffect } from 'react';
import { Users, Trash2, RotateCcw, Shield, Mail, RefreshCw, Edit2 } from 'lucide-react';
import api from '../services/api';
import { getApiError } from '../utils/validation';
import { useAuth } from '../context/AuthContext';
import { PageHeader, EmptyState } from '../components/ui/Display';
import { Modal } from '../components/ui/Modal';
import { SelectField } from '../components/ui/FormFields';

export default function GestionUtilisateurs() {
  const { user, hasPermission } = useAuth();
  const isAdmin = user?.roles?.includes('ADMINISTRATEUR');
  const [utilisateurs, setUtilisateurs] = useState([]);
  const [corbeille, setCorbeille] = useState([]);
  const [roles, setRoles] = useState([]);
  const [tab, setTab] = useState('actifs');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [editUser, setEditUser] = useState(null);
  const [form, setForm] = useState({ roleCode: '', actif: true });

  const fetchData = async () => {
    setLoading(true);
    setError(null);
    const [usersRes, corbRes, rolesRes] = await Promise.allSettled([
      api.get('/utilisateurs'),
      api.get('/utilisateurs/corbeille'),
      api.get('/roles'),
    ]);
    if (usersRes.status === 'fulfilled') {
      setUtilisateurs(usersRes.value.data);
    } else {
      setUtilisateurs([]);
      setError(getApiError(usersRes.reason, 'Erreur de chargement des utilisateurs'));
    }
    if (corbRes.status === 'fulfilled') {
      setCorbeille(corbRes.value.data);
    } else {
      setCorbeille([]);
    }
    if (rolesRes.status === 'fulfilled') {
      const allRoles = rolesRes.value.data;
      setRoles(isAdmin ? allRoles : allRoles.filter(role => role.code !== 'ADMINISTRATEUR'));
    } else {
      setRoles([]);
      if (usersRes.status === 'fulfilled') {
        setError(getApiError(rolesRes.reason, 'Erreur de chargement des rôles'));
      }
    }
    setLoading(false);
  };

  useEffect(() => { fetchData(); }, [isAdmin]);

  const liste = tab === 'actifs' ? utilisateurs : corbeille;

  const handleDelete = async (id) => {
    if (!confirm('Déplacer ce compte vers la corbeille ?')) return;
    try {
      await api.delete(`/utilisateurs/${id}`);
      fetchData();
    } catch (err) {
      setError(getApiError(err, 'Erreur'));
    }
  };

  const handleRestore = async (id) => {
    try {
      await api.post(`/utilisateurs/${id}/restaurer`);
      fetchData();
    } catch (err) {
      setError(getApiError(err, 'Erreur'));
    }
  };

  const handleRenvoyer = async (id) => {
    try {
      await api.post(`/utilisateurs/${id}/renvoyer-activation`);
      alert('Code d\'activation renvoyé par e-mail/SMS.');
    } catch (err) {
      setError(getApiError(err, 'Erreur'));
    }
  };

  const openEdit = (user) => {
    const roleCode = user.roles?.[0] || '';
    setEditUser(user);
    setForm({ roleCode, actif: user.actif });
  };

  const handleSave = async (e) => {
    e.preventDefault();
    if (!form.roleCode) { setError('Sélectionnez un rôle'); return; }
    if (form.roleCode === 'ADMINISTRATEUR' && !editUser.roles?.includes('ADMINISTRATEUR')) {
      if (!confirm('Attribuer le rôle Administrateur Système ?\n\nCet utilisateur aura un accès total à toutes les fonctionnalités.')) {
        return;
      }
    }
    try {
      await api.put(`/utilisateurs/${editUser.id}`, {
        roleCodes: [form.roleCode],
        actif: form.actif,
      });
      setEditUser(null);
      fetchData();
    } catch (err) {
      setError(getApiError(err, 'Erreur'));
    }
  };

  const roleDefaut = roles.find(r => r.parDefaut);

  return (
    <div className="space-y-6 page-enter">
      <PageHeader
        title="Gestion des utilisateurs"
        subtitle={roleDefaut
          ? `Comptes d'accès, rôles et statut d'activation. Rôle par défaut : ${roleDefaut.libelle}.`
          : "Comptes d'accès, rôles et statut d'activation."}
        action={
          <button onClick={fetchData} className="p-3 glass-card rounded-xl">
            <RefreshCw size={18} className={loading ? 'animate-spin' : ''} />
          </button>
        }
      />

      {error && <p className="text-sm text-red-600">{error}</p>}

      <div className="flex gap-2">
        <button onClick={() => setTab('actifs')} className={`px-4 py-2 rounded-xl text-sm font-medium ${tab === 'actifs' ? 'bg-black text-white' : 'bg-white/50'}`}>
          Actifs ({utilisateurs.length})
        </button>
        <button onClick={() => setTab('corbeille')} className={`px-4 py-2 rounded-xl text-sm font-medium flex items-center gap-1 ${tab === 'corbeille' ? 'bg-black text-white' : 'bg-white/50'}`}>
          <Trash2 size={14} /> Corbeille ({corbeille.length})
        </button>
      </div>

      {loading ? (
        <div className="flex justify-center py-16"><div className="w-8 h-8 border-2 border-blue-600 border-t-transparent rounded-full animate-spin" /></div>
      ) : liste.length === 0 ? (
        <EmptyState icon={Users} title={tab === 'corbeille' ? 'Corbeille vide' : 'Aucun utilisateur'} />
      ) : (
        <div className="grid gap-3">
          {liste.map(u => (
            <div key={u.id} className="glass-card p-4 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
              <div>
                <p className="font-semibold">{u.nomComplet || u.email}</p>
                <p className="text-xs text-gray-500 flex items-center gap-1"><Mail size={10} /> {u.email}</p>
                <p className="text-xs text-gray-400 mt-1">{u.matricule} · {u.departementLibelle} · {u.posteLibelle}</p>
                <div className="flex flex-wrap gap-2 mt-2">
                  {[...(u.roles || [])].map(r => (
                    <span key={r} className="text-[10px] px-2 py-0.5 rounded-full bg-blue-100 text-blue-700 font-bold flex items-center gap-1">
                      <Shield size={10} /> {r}
                    </span>
                  ))}
                  <span className={`text-[10px] px-2 py-0.5 rounded-full font-bold ${u.confirme ? 'bg-emerald-100 text-emerald-700' : 'bg-amber-100 text-amber-700'}`}>
                    {u.confirme ? 'Activé' : 'En attente'}
                  </span>
                  <span className={`text-[10px] px-2 py-0.5 rounded-full font-bold ${u.actif ? 'bg-gray-100 text-gray-600' : 'bg-red-100 text-red-600'}`}>
                    {u.actif ? 'Actif' : 'Inactif'}
                  </span>
                </div>
              </div>
              <div className="flex gap-1 shrink-0">
                {tab === 'actifs' && hasPermission('UTILISATEURS', 'MODIFIER') && (
                  <>
                    <button onClick={() => openEdit(u)} className="p-2 hover:bg-blue-50 text-blue-600 rounded-lg" title="Modifier"><Edit2 size={16} /></button>
                    {!u.confirme && (
                      <button onClick={() => handleRenvoyer(u.id)} className="p-2 hover:bg-amber-50 text-amber-600 rounded-lg" title="Renvoyer activation"><Mail size={16} /></button>
                    )}
                  </>
                )}
                {tab === 'actifs' && hasPermission('UTILISATEURS', 'SUPPRIMER') && !u.roles?.includes('ADMINISTRATEUR') && (
                  <button onClick={() => handleDelete(u.id)} className="p-2 hover:bg-red-50 text-red-500 rounded-lg" title="Supprimer"><Trash2 size={16} /></button>
                )}
                {tab === 'corbeille' && hasPermission('UTILISATEURS', 'MODIFIER') && (
                  <button onClick={() => handleRestore(u.id)} className="p-2 hover:bg-emerald-50 text-emerald-600 rounded-lg" title="Restaurer"><RotateCcw size={16} /></button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      <Modal open={!!editUser} onClose={() => setEditUser(null)} title={`Modifier — ${editUser?.email}`}>
        <form onSubmit={handleSave} className="space-y-4">
          <SelectField label="Rôle" required value={form.roleCode} onChange={e => setForm({ ...form, roleCode: e.target.value })}
            options={roles.map(r => ({
              value: r.code,
              label: r.code === 'ADMINISTRATEUR'
                ? `${r.libelle} — accès total`
                : `${r.libelle}${r.parDefaut ? ' (défaut)' : ''}`,
            }))} />
          {form.roleCode === 'ADMINISTRATEUR' && (
            <p className="text-xs text-amber-700 bg-amber-50 p-3 rounded-xl">
              Le rôle Administrateur Système donne un accès complet à l'application (employés, paie, configuration, rôles…).
            </p>
          )}
          <label className="flex items-center gap-2 text-sm">
            <input type="checkbox" checked={form.actif} onChange={e => setForm({ ...form, actif: e.target.checked })} className="rounded" />
            Compte actif
          </label>
          <button type="submit" className="w-full py-3 bg-black text-white rounded-xl text-sm font-semibold">Enregistrer</button>
        </form>
      </Modal>
    </div>
  );
}
