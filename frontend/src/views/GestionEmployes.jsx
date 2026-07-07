import { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import {
  Users, Plus, Edit2, Trash2, RotateCcw, UserX, ShieldOff,
  Mail, Phone, Building2, Briefcase, AlertCircle, Save, Printer, Send, Shield
} from 'lucide-react';
import api from '../services/api';
import { useAuth } from '../context/AuthContext';
import { Modal, ConfirmDialog } from '../components/ui/Modal';
import { InputField, SelectField, SearchBar } from '../components/ui/FormFields';
import { Avatar, StatusBadge, StatCard, EmptyState, PageHeader, TabBar } from '../components/ui/Display';
import { getApiError, validateImageFile, validatePhone } from '../utils/validation';
import CarteEmploye from '../components/documents/CarteEmploye';
import { DocumentExportButtons } from '../components/documents/DocumentExportButtons';
import { BADGE_PRINT_CSS } from '../utils/documentExport';
import { STATUT_EMPLOI_STYLES, formatDate } from '../utils/format';

const EMPTY_FORM = {
  matricule: '', nom: '', prenom: '', email: '', telephone: '',
  dateNaissance: '', departementId: '', posteId: '', photoUrl: '', statutEmploi: 'ACTIF',
  roleCode: 'EMPLOYE',
};

export default function GestionEmployes() {
  const { hasPermission } = useAuth();
  const [employes, setEmployes] = useState([]);
  const [corbeille, setCorbeille] = useState([]);
  const [departements, setDepartements] = useState([]);
  const [postes, setPostes] = useState([]);
  const [roles, setRoles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [tab, setTab] = useState('actifs');
  const [search, setSearch] = useState('');
  const [filterStatut, setFilterStatut] = useState('');

  const [modal, setModal] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY_FORM);
  const [photoFile, setPhotoFile] = useState(null);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);

  const [confirm, setConfirm] = useState(null);
  const [actionLoading, setActionLoading] = useState(false);
  const [printingEmp, setPrintingEmp] = useState(null);
  const [roleParDefaut, setRoleParDefaut] = useState('EMPLOYE');

  const fetchAll = async () => {
    setLoading(true);
    try {
      const [empRes, corbRes, deptRes, posteRes, rolesRes] = await Promise.all([
        api.get('/employes'),
        api.get('/employes/corbeille'),
        api.get('/referentiels/departements'),
        api.get('/referentiels/postes'),
        api.get('/roles'),
      ]);
      setEmployes(empRes.data);
      setCorbeille(corbRes.data);
      setDepartements(deptRes.data);
      setPostes(posteRes.data);
      setRoles(rolesRes.data.filter(r => r.code !== 'ADMINISTRATEUR'));
      const def = rolesRes.data.find(r => r.parDefaut);
      if (def) setRoleParDefaut(def.code);
    } catch {
      setError('Erreur de chargement des employés');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchAll(); }, []);

  const liste = tab === 'actifs' ? employes : corbeille;
  const filtered = liste.filter(e => {
    const q = search.toLowerCase();
    const matchSearch = !q ||
      e.nom?.toLowerCase().includes(q) ||
      e.prenom?.toLowerCase().includes(q) ||
      e.matricule?.toLowerCase().includes(q) ||
      e.email?.toLowerCase().includes(q);
    const matchStatut = !filterStatut || e.statutEmploi === filterStatut;
    return matchSearch && matchStatut;
  });

  const stats = {
    total: employes.length,
    actifs: employes.filter(e => e.statutEmploi === 'ACTIF').length,
    suspendus: employes.filter(e => e.statutEmploi === 'SUSPENDU').length,
    corbeille: corbeille.length,
  };

  const openCreate = () => {
    setForm({ ...EMPTY_FORM, roleCode: roleParDefaut });
    setPhotoFile(null);
    setEditing(null);
    setError(null);
    setModal(true);
  };

  const openEdit = (emp) => {
    setForm({
      matricule: emp.matricule || '',
      nom: emp.nom || '',
      prenom: emp.prenom || '',
      email: emp.email || '',
      telephone: emp.telephone || '',
      dateNaissance: emp.dateNaissance || '',
      departementId: emp.departementId || '',
      posteId: emp.posteId || '',
      photoUrl: emp.photoUrl || '',
      statutEmploi: emp.statutEmploi || 'ACTIF',
      roleCode: emp.roleCode || roleParDefaut,
    });
    setPhotoFile(null);
    setEditing(emp);
    setError(null);
    setModal(true);
  };

  const handleSave = async (e) => {
    e.preventDefault();
    const phoneErr = validatePhone(form.telephone);
    if (phoneErr) { setError(phoneErr); return; }
    if (!form.departementId || !form.posteId) {
      setError('Le département et le poste sont obligatoires');
      return;
    }
    if (photoFile) {
      const fileErr = validateImageFile(photoFile);
      if (fileErr) { setError(fileErr); return; }
    }
    setSaving(true);
    setError(null);
    try {
      let photoUrl = form.photoUrl;
      if (photoFile) {
        const fd = new FormData();
        fd.append('file', photoFile);
        const uploadRes = await api.post('/employes/upload-photo', fd, {
          headers: { 'Content-Type': 'multipart/form-data' }
        });
        photoUrl = uploadRes.data.url;
      }
      const payload = {
        ...form,
        nom: form.nom.trim(),
        prenom: form.prenom.trim(),
        email: form.email.trim(),
        telephone: form.telephone.replace(/\s+/g, ''),
        matricule: form.matricule.trim(),
        photoUrl,
        departementId: parseInt(form.departementId),
        posteId: parseInt(form.posteId),
      };
      if (editing) {
        await api.put(`/employes/${editing.id}`, { ...payload, roleCode: form.roleCode || roleParDefaut });
      } else {
        await api.post('/employes', { ...payload, roleCode: form.roleCode || roleParDefaut });
      }
      setModal(false);
      fetchAll();
    } catch (err) {
      setError(getApiError(err, 'Erreur lors de l\'enregistrement'));
    } finally {
      setSaving(false);
    }
  };

  const handleAction = async () => {
    if (!confirm) return;
    setActionLoading(true);
    try {
      const { type, id } = confirm;
      if (type === 'delete') await api.delete(`/employes/${id}`);
      else if (type === 'restore') await api.post(`/employes/${id}/restaurer`);
      else if (type === 'purge') await api.delete(`/employes/${id}/definitif`);
      else if (type === 'suspend') await api.post(`/employes/${id}/suspendre`);
      else if (type === 'licencier') await api.post(`/employes/${id}/licencier`);
      setConfirm(null);
      fetchAll();
    } catch (err) {
      setError(getApiError(err, 'Erreur'));
    } finally {
      setActionLoading(false);
    }
  };

  const handleRenvoyerActivation = async (emp) => {
    try {
      await api.post(`/employes/${emp.id}/renvoyer-activation`);
      alert(`Code d'activation renvoyé à ${emp.email}`);
    } catch (err) {
      setError(getApiError(err, 'Erreur'));
    }
  };

  const compteBadge = (emp) => {
    if (emp.compteConfirme === false || emp.compteConfirme === null && emp.compteActif === false) {
      return <span className="text-[10px] bg-amber-100 text-amber-700 px-2 py-0.5 rounded-full font-bold">En attente activation</span>;
    }
    if (emp.compteActif === false) return <span className="text-[10px] bg-red-100 text-red-600 px-2 py-0.5 rounded-full font-bold">Compte inactif</span>;
    if (emp.roleLibelle) return <span className="text-[10px] bg-blue-50 text-blue-600 px-2 py-0.5 rounded-full font-medium flex items-center gap-0.5"><Shield size={9} />{emp.roleLibelle}</span>;
    return null;
  };

  const f = (key) => ({
    value: form[key],
    onChange: e => setForm({ ...form, [key]: e.target.value }),
  });

  return (
    <div className="space-y-6">
      <style>{BADGE_PRINT_CSS}</style>
      <PageHeader
        title="Gestion des Employés"
        subtitle="Collaborateurs, statuts et affectations"
        action={hasPermission('EMPLOYES', 'AJOUTER') && tab === 'actifs' && (
          <button onClick={openCreate} className="flex items-center gap-2 px-5 py-2.5 bg-black text-white rounded-xl text-sm font-semibold hover:bg-gray-800 transition shadow-lg">
            <Plus size={16} /> Nouvel employé
          </button>
        )}
      />

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard label="Total employés" value={stats.total} icon={Users} color="from-blue-600 to-indigo-600" />
        <StatCard label="Actifs" value={stats.actifs} icon={Users} color="from-emerald-600 to-teal-600" />
        <StatCard label="Suspendus" value={stats.suspendus} icon={ShieldOff} color="from-amber-500 to-orange-500" />
        <StatCard label="Corbeille" value={stats.corbeille} icon={Trash2} color="from-gray-600 to-gray-800" />
      </div>

      <div className="flex flex-col sm:flex-row gap-4 items-start sm:items-center justify-between">
        <TabBar
          tabs={[
            { key: 'actifs', label: 'Employés actifs', icon: Users, count: employes.length },
            { key: 'corbeille', label: 'Corbeille', icon: Trash2, count: corbeille.length },
          ]}
          active={tab}
          onChange={setTab}
        />
        <div className="flex gap-3 w-full sm:w-auto">
          {tab === 'actifs' && (
            <select
              value={filterStatut}
              onChange={e => setFilterStatut(e.target.value)}
              className="px-3 py-2.5 bg-white/60 border border-black/5 rounded-xl text-sm outline-none"
            >
              <option value="">Tous les statuts</option>
              <option value="ACTIF">Actif</option>
              <option value="SUSPENDU">Suspendu</option>
              <option value="LICENCIE">Licencié</option>
            </select>
          )}
          <div className="flex-1 sm:w-64">
            <SearchBar value={search} onChange={e => setSearch(e.target.value)} placeholder="Nom, matricule, email..." />
          </div>
        </div>
      </div>

      {loading ? (
        <div className="flex justify-center py-20">
          <div className="w-8 h-8 border-2 border-black border-t-transparent rounded-full animate-spin" />
        </div>
      ) : filtered.length === 0 ? (
        <div className="bg-white/60 backdrop-blur border border-white/20 rounded-3xl">
          <EmptyState
            icon={Users}
            title={tab === 'corbeille' ? 'Corbeille vide' : 'Aucun employé trouvé'}
            description={tab === 'corbeille' ? 'Les employés supprimés apparaîtront ici.' : 'Ajoutez votre premier collaborateur.'}
            action={tab === 'actifs' && hasPermission('EMPLOYES', 'AJOUTER') && (
              <button onClick={openCreate} className="flex items-center gap-2 px-4 py-2 bg-black text-white rounded-xl text-sm font-semibold">
                <Plus size={16} /> Ajouter un employé
              </button>
            )}
          />
        </div>
      ) : (
        <div className="grid sm:grid-cols-2 xl:grid-cols-3 gap-4">
          <AnimatePresence>
            {filtered.map((emp, i) => (
              <motion.div
                key={emp.id}
                initial={{ opacity: 0, y: 15 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, scale: 0.95 }}
                transition={{ delay: i * 0.04 }}
                className="bg-white/60 backdrop-blur-xl border border-white/20 rounded-2xl p-5 hover:shadow-lg hover:border-white/40 transition group"
              >
                <div className="flex items-start gap-4">
                  <Avatar src={emp.photoUrl} prenom={emp.prenom} nom={emp.nom} size="lg" />
                  <div className="flex-1 min-w-0">
                    <div className="flex items-start justify-between gap-2">
                      <div>
                        <h3 className="font-semibold text-[#1D1D1F] truncate">{emp.prenom} {emp.nom}</h3>
                        <p className="text-xs text-gray-400 font-mono mt-0.5">{emp.matricule}</p>
                      </div>
                      <StatusBadge statut={emp.statutEmploi} styles={STATUT_EMPLOI_STYLES} />
                    </div>
                    <div className="mt-3 space-y-1.5">
                      <p className="text-xs text-gray-500 flex items-center gap-1.5 truncate">
                        <Mail size={12} /> {emp.email}
                      </p>
                      <p className="text-xs text-gray-500 flex items-center gap-1.5">
                        <Phone size={12} /> {emp.telephone}
                      </p>
                      {emp.departementLibelle && (
                        <p className="text-xs text-gray-500 flex items-center gap-1.5">
                          <Building2 size={12} /> {emp.departementLibelle}
                        </p>
                      )}
                      {emp.posteLibelle && (
                        <p className="text-xs text-gray-500 flex items-center gap-1.5">
                          <Briefcase size={12} /> {emp.posteLibelle}
                        </p>
                      )}
                    </div>
                  </div>
                </div>

                <div className="flex items-center justify-between mt-4 pt-4 border-t border-gray-100">
                  <div className="flex gap-1">
                    {emp.aContratActif && (
                      <span className="text-xs bg-blue-50 text-blue-600 px-2 py-0.5 rounded-full font-medium">Contrat actif</span>
                    )}
                    {compteBadge(emp)}
                    {emp.dateNaissance && (
                      <span className="text-xs text-gray-400">{formatDate(emp.dateNaissance)}</span>
                    )}
                  </div>
                  <div className="flex gap-1 opacity-0 group-hover:opacity-100 transition">
                    {tab === 'actifs' ? (
                      <>
                        {hasPermission('EMPLOYES', 'MODIFIER') && (
                          <>
                            <button onClick={() => setPrintingEmp(emp)} className="p-2 hover:bg-blue-50 text-blue-600 rounded-lg transition" title="Imprimer badge">
                              <Printer size={15} />
                            </button>
                            <button onClick={() => openEdit(emp)} className="p-2 hover:bg-gray-100 rounded-lg transition" title="Modifier">
                              <Edit2 size={15} />
                            </button>
                            {emp.compteConfirme === false && (
                              <button onClick={() => handleRenvoyerActivation(emp)} className="p-2 hover:bg-violet-50 text-violet-600 rounded-lg transition" title="Renvoyer activation">
                                <Send size={15} />
                              </button>
                            )}
                            <button onClick={() => setConfirm({ type: 'suspend', id: emp.id, title: 'Suspendre', message: `Suspendre ${emp.prenom} ${emp.nom} ?`, danger: true })} className="p-2 hover:bg-amber-50 text-amber-600 rounded-lg transition" title="Suspendre">
                              <ShieldOff size={15} />
                            </button>
                            <button onClick={() => setConfirm({ type: 'licencier', id: emp.id, title: 'Licencier', message: `Licencier ${emp.prenom} ${emp.nom} ? Une notification sera envoyée.`, danger: true })} className="p-2 hover:bg-orange-50 text-orange-600 rounded-lg transition" title="Licencier">
                              <UserX size={15} />
                            </button>
                          </>
                        )}
                        {hasPermission('EMPLOYES', 'SUPPRIMER') && (
                          <button onClick={() => setConfirm({ type: 'delete', id: emp.id, title: 'Supprimer', message: `Déplacer ${emp.prenom} ${emp.nom} vers la corbeille ?`, danger: true })} className="p-2 hover:bg-red-50 text-red-500 rounded-lg transition" title="Supprimer">
                            <Trash2 size={15} />
                          </button>
                        )}
                      </>
                    ) : (
                      <>
                        {hasPermission('EMPLOYES', 'MODIFIER') && (
                          <button onClick={() => setConfirm({ type: 'restore', id: emp.id, title: 'Restaurer', message: `Restaurer ${emp.prenom} ${emp.nom} ?` })} className="p-2 hover:bg-emerald-50 text-emerald-600 rounded-lg transition" title="Restaurer">
                            <RotateCcw size={15} />
                          </button>
                        )}
                        {hasPermission('EMPLOYES', 'SUPPRIMER') && (
                          <button onClick={() => setConfirm({ type: 'purge', id: emp.id, title: 'Suppression définitive', message: `Supprimer définitivement ${emp.prenom} ${emp.nom} ? Cette action est irréversible.`, danger: true })} className="p-2 hover:bg-red-50 text-red-600 rounded-lg transition" title="Supprimer définitivement">
                            <Trash2 size={15} />
                          </button>
                        )}
                      </>
                    )}
                  </div>
                </div>
              </motion.div>
            ))}
          </AnimatePresence>
        </div>
      )}

      <Modal open={modal} onClose={() => setModal(false)} title={editing ? 'Modifier l\'employé' : 'Nouvel employé'} size="lg">
        {error && (
          <div className="flex items-center gap-2 bg-red-50 text-red-600 p-3 rounded-xl mb-4 text-sm">
            <AlertCircle size={16} /> {error}
          </div>
        )}
        <form onSubmit={handleSave} className="space-y-4">
          <div className="flex items-center gap-4 mb-2">
            <Avatar src={form.photoUrl} prenom={form.prenom} nom={form.nom} size="xl" />
            <div>
              <label className="text-xs font-semibold text-gray-600 block mb-1">Photo de profil</label>
              <input type="file" accept="image/jpeg,image/png,image/webp,image/gif" onChange={e => {
                const f = e.target.files[0];
                const err = validateImageFile(f);
                if (err) { setError(err); setPhotoFile(null); return; }
                setPhotoFile(f);
                setError(null);
              }} className="text-xs text-gray-500" />
            </div>
          </div>
          <div className="grid grid-cols-2 gap-4">
            <InputField label="Matricule" required {...f('matricule')} placeholder="SNG-2026-001" />
            <SelectField label="Statut emploi" value={form.statutEmploi} onChange={e => setForm({ ...form, statutEmploi: e.target.value })}
              options={[{ value: 'ACTIF', label: 'Actif' }, { value: 'SUSPENDU', label: 'Suspendu' }, { value: 'LICENCIE', label: 'Licencié' }]} />
            <InputField label="Prénom" required {...f('prenom')} placeholder="Fatoumata" />
            <InputField label="Nom" required {...f('nom')} placeholder="Camara" />
            <InputField label="E-mail" type="email" required {...f('email')} placeholder="employe@minerva.group" />
            <InputField label="Téléphone" required {...f('telephone')} placeholder="+224 622 11 22 33" />
            <InputField label="Date de naissance" type="date" required {...f('dateNaissance')} />
            <SelectField label="Département" required value={form.departementId} onChange={e => setForm({ ...form, departementId: e.target.value })}
              options={departements.map(d => ({ value: d.id, label: d.libelle }))} />
            <SelectField label="Poste" required value={form.posteId} onChange={e => setForm({ ...form, posteId: e.target.value })}
              options={postes.map(p => ({ value: p.id, label: p.libelle }))} />
            {((!editing && hasPermission('EMPLOYES', 'AJOUTER')) || (editing && hasPermission('EMPLOYES', 'MODIFIER'))) && (
              <SelectField
                label={editing ? 'Rôle du compte utilisateur' : 'Rôle à attribuer'}
                value={form.roleCode || roleParDefaut}
                onChange={e => setForm({ ...form, roleCode: e.target.value })}
                options={roles.map(r => ({ value: r.code, label: `${r.libelle} (${r.code})${r.parDefaut ? ' — défaut' : ''}` }))}
              />
            )}
          </div>
          {!editing && (
            <p className="text-xs text-blue-600 bg-blue-50 p-3 rounded-xl">
              Un compte utilisateur sera créé automatiquement avec le rôle{' '}
              <strong>{roles.find(r => r.code === (form.roleCode || roleParDefaut))?.libelle || form.roleCode || roleParDefaut}</strong>.
              Un code d'activation et un lien seront envoyés par e-mail et SMS pour définir le mot de passe.
            </p>
          )}
          {editing && hasPermission('EMPLOYES', 'MODIFIER') && (
            <p className="text-xs text-gray-500 bg-gray-50 p-3 rounded-xl">
              La modification du rôle met à jour les permissions de connexion de cet employé.
            </p>
          )}
          <button type="submit" disabled={saving} className="flex items-center gap-2 px-6 py-2.5 bg-black text-white rounded-xl text-sm font-semibold disabled:opacity-50">
            <Save size={16} /> {saving ? 'Enregistrement...' : 'Enregistrer'}
          </button>
        </form>
      </Modal>

      <ConfirmDialog
        open={!!confirm}
        onClose={() => setConfirm(null)}
        onConfirm={handleAction}
        title={confirm?.title}
        message={confirm?.message}
        danger={confirm?.danger}
        loading={actionLoading}
        confirmLabel={confirm?.type === 'purge' ? 'Supprimer définitivement' : 'Confirmer'}
      />

      <Modal open={!!printingEmp} onClose={() => setPrintingEmp(null)} title="Carte employé" size="sm">
        {printingEmp && (
          <div className="space-y-6 no-print">
            <CarteEmploye employe={printingEmp} id="print-badge-employe" />
            <div className="flex flex-col gap-3">
              <DocumentExportButtons
                elementId="print-badge-employe"
                basename={`badge-${printingEmp.matricule}`}
                variant="badge"
              />
              <button onClick={() => setPrintingEmp(null)} className="w-full py-3 bg-black/5 rounded-xl text-sm font-semibold">Fermer</button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
