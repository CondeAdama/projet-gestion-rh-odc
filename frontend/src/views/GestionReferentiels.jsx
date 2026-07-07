import { useState, useEffect } from 'react';
import { BookOpen, Plus, Trash2, Edit2, RotateCcw } from 'lucide-react';
import api from '../services/api';
import { getApiError } from '../utils/validation';
import { useAuth } from '../context/AuthContext';
import { Modal } from '../components/ui/Modal';
import { InputField, SelectField } from '../components/ui/FormFields';

function ReferentielTab({ type, label, fields, departements = [] }) {
  const [items, setItems] = useState([]);
  const [corbeille, setCorbeille] = useState([]);
  const [subTab, setSubTab] = useState('actifs');
  const [form, setForm] = useState({});
  const [showForm, setShowForm] = useState(false);
  const [editItem, setEditItem] = useState(null);
  const [error, setError] = useState(null);
  const { hasPermission } = useAuth();

  const fetchItems = async () => {
    setError(null);
    const [actifsRes, corbeilleRes] = await Promise.allSettled([
      api.get(`/referentiels/${type}`),
      api.get(`/referentiels/${type}/corbeille`),
    ]);
    if (actifsRes.status === 'fulfilled') {
      setItems(actifsRes.value.data);
    } else {
      setItems([]);
      setError(getApiError(actifsRes.reason, 'Erreur de chargement des éléments actifs'));
    }
    if (corbeilleRes.status === 'fulfilled') {
      setCorbeille(corbeilleRes.value.data);
    } else {
      setCorbeille([]);
      if (actifsRes.status === 'fulfilled') {
        setError(getApiError(corbeilleRes.reason, 'Corbeille indisponible'));
      }
    }
  };

  useEffect(() => { fetchItems(); }, [type]);

  const buildPayload = () => {
    const payload = {};
    fields.forEach(f => {
      const val = form[f.key];
      if (val !== undefined && val !== '') {
        payload[f.key] = typeof val === 'string' ? val.trim() : val;
      }
    });
    return payload;
  };

  const handleCreate = async (e) => {
    e.preventDefault();
    const missing = fields.find(f => f.required && !form[f.key]?.toString().trim());
    if (missing) { setError(`${missing.label} est obligatoire`); return; }
    setError(null);
    try {
      await api.post(`/referentiels/${type}`, buildPayload());
      setForm({});
      setShowForm(false);
      fetchItems();
    } catch (err) {
      setError(getApiError(err, 'Erreur'));
    }
  };

  const handleUpdate = async (e) => {
    e.preventDefault();
    setError(null);
    try {
      await api.put(`/referentiels/${type}/${editItem.id}`, buildPayload());
      setEditItem(null);
      setForm({});
      fetchItems();
    } catch (err) {
      setError(getApiError(err, 'Erreur'));
    }
  };

  const handleDelete = async (id) => {
    if (!confirm('Déplacer vers la corbeille ?')) return;
    setError(null);
    try {
      await api.delete(`/referentiels/${type}/${id}`);
      fetchItems();
    } catch (err) {
      setError(getApiError(err, 'Erreur'));
    }
  };

  const handleRestore = async (id) => {
    setError(null);
    try {
      await api.post(`/referentiels/${type}/${id}/restaurer`);
      fetchItems();
    } catch (err) {
      setError(getApiError(err, 'Erreur'));
    }
  };

  const openEdit = (item) => {
    setEditItem(item);
    const f = {};
    fields.forEach(field => {
      if (field.key === 'departementId') f.departementId = item.departementId ? String(item.departementId) : '';
      else f[field.key] = item[field.key] || item.libelle || item.nom || '';
    });
    setForm(f);
  };

  const liste = subTab === 'actifs' ? items : corbeille;

  return (
    <div>
      <div className="flex flex-wrap items-center justify-between gap-3 mb-4">
        <h3 className="font-semibold">{label}</h3>
        <div className="flex gap-2">
          <button onClick={() => setSubTab('actifs')} className={`px-3 py-1.5 rounded-lg text-xs font-medium ${subTab === 'actifs' ? 'bg-black text-white' : 'bg-gray-100'}`}>
            Actifs ({items.length})
          </button>
          <button onClick={() => setSubTab('corbeille')} className={`px-3 py-1.5 rounded-lg text-xs font-medium flex items-center gap-1 ${subTab === 'corbeille' ? 'bg-black text-white' : 'bg-gray-100'}`}>
            <Trash2 size={12} /> Corbeille ({corbeille.length})
          </button>
          {hasPermission('REFERENTIELS', 'AJOUTER') && subTab === 'actifs' && (
            <button onClick={() => { setShowForm(!showForm); setForm({}); }} className="flex items-center gap-1 px-3 py-1.5 bg-[#1e3a5f] text-white rounded-lg text-xs">
              <Plus size={14} /> Ajouter
            </button>
          )}
        </div>
      </div>

      {error && <p className="text-xs text-red-600 mb-3">{error}</p>}

      {showForm && subTab === 'actifs' && (
        <form onSubmit={handleCreate} className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3 mb-4 p-4 glass-card">
          {fields.map(f => f.key === 'departementId' ? (
            <SelectField key={f.key} label={f.label} value={form.departementId || ''} onChange={e => setForm({ ...form, departementId: e.target.value })}
              options={departements.map(d => ({ value: String(d.id), label: d.libelle }))} placeholder="— Département —" />
          ) : (
            <InputField key={f.key} label={f.label} required={f.required} placeholder={f.placeholder}
              value={form[f.key] || ''} onChange={e => setForm({ ...form, [f.key]: e.target.value })} />
          ))}
          <div className="flex items-end gap-2 sm:col-span-2 lg:col-span-3">
            <button type="submit" className="px-4 py-2.5 bg-black text-white rounded-xl text-sm font-semibold">Créer</button>
            <button type="button" onClick={() => setShowForm(false)} className="px-4 py-2.5 bg-gray-100 rounded-xl text-sm">Annuler</button>
          </div>
        </form>
      )}

      <div className="space-y-2">
        {liste.map(item => (
          <div key={item.id} className="flex items-center justify-between p-3 glass-card">
            <div>
              <span className="font-medium text-sm">{item.libelle || item.nom}</span>
              <span className="text-xs text-gray-400 ml-2">({item.code})</span>
              {item.departementLibelle && <span className="text-xs text-gray-400 ml-2">· {item.departementLibelle}</span>}
              {item.ville && <span className="text-xs text-gray-400 ml-2">· {item.ville}</span>}
            </div>
            <div className="flex gap-1">
              {subTab === 'actifs' && hasPermission('REFERENTIELS', 'MODIFIER') && (
                <button onClick={() => openEdit(item)} className="p-2 hover:bg-blue-50 text-blue-600 rounded-lg" title="Modifier"><Edit2 size={14} /></button>
              )}
              {subTab === 'actifs' && hasPermission('REFERENTIELS', 'SUPPRIMER') && (
                <button onClick={() => handleDelete(item.id)} className="p-2 hover:bg-red-50 text-red-500 rounded-lg" title="Supprimer"><Trash2 size={14} /></button>
              )}
              {subTab === 'corbeille' && hasPermission('REFERENTIELS', 'MODIFIER') && (
                <button onClick={() => handleRestore(item.id)} className="p-2 hover:bg-emerald-50 text-emerald-600 rounded-lg" title="Restaurer"><RotateCcw size={14} /></button>
              )}
            </div>
          </div>
        ))}
        {liste.length === 0 && <p className="text-sm text-gray-400 text-center py-6">{subTab === 'corbeille' ? 'Corbeille vide' : 'Aucun élément'}</p>}
      </div>

      <Modal open={!!editItem} onClose={() => { setEditItem(null); setForm({}); }} title={`Modifier — ${editItem?.libelle || editItem?.nom}`}>
        <form onSubmit={handleUpdate} className="space-y-4">
          {fields.filter(f => f.key !== 'code').map(f => f.key === 'departementId' ? (
            <SelectField key={f.key} label={f.label} value={form.departementId || ''} onChange={e => setForm({ ...form, departementId: e.target.value })}
              options={departements.map(d => ({ value: String(d.id), label: d.libelle }))} />
          ) : (
            <InputField key={f.key} label={f.label} required={f.required} value={form[f.key] || ''} onChange={e => setForm({ ...form, [f.key]: e.target.value })} />
          ))}
          <button type="submit" className="w-full py-3 bg-black text-white rounded-xl text-sm font-semibold">Enregistrer</button>
        </form>
      </Modal>
    </div>
  );
}

export default function GestionReferentiels() {
  const [tab, setTab] = useState('departements');
  const [departements, setDepartements] = useState([]);

  useEffect(() => {
    api.get('/referentiels/departements').then(r => setDepartements(r.data)).catch(() => {});
  }, []);

  const tabs = [
    { key: 'departements', label: 'Départements' },
    { key: 'postes', label: 'Postes' },
    { key: 'localisations', label: 'Localisations' },
  ];

  const fieldConfigs = {
    departements: [
      { key: 'code', label: 'Code', required: true, placeholder: 'FIN' },
      { key: 'libelle', label: 'Libellé', required: true, placeholder: 'Finance' },
      { key: 'description', label: 'Description', required: false, placeholder: 'Comptabilité et trésorerie' },
    ],
    postes: [
      { key: 'code', label: 'Code', required: true, placeholder: 'COMPT' },
      { key: 'libelle', label: 'Libellé', required: true, placeholder: 'Comptable' },
      { key: 'description', label: 'Description', required: false, placeholder: 'Gestion paie et CNSS' },
      { key: 'departementId', label: 'Département', required: false },
    ],
    localisations: [
      { key: 'code', label: 'Code', required: true, placeholder: 'SIEGE' },
      { key: 'nom', label: 'Nom', required: true, placeholder: 'Siège Social' },
      { key: 'adresse', label: 'Adresse', required: true, placeholder: 'Kaloum, Conakry' },
      { key: 'ville', label: 'Ville', required: false, placeholder: 'Conakry' },
    ],
  };

  return (
    <div className="space-y-6 page-enter">
      <h2 className="text-xl font-bold flex items-center gap-2">
        <BookOpen size={22} /> Référentiels dynamiques
      </h2>

      <div className="flex flex-wrap gap-2">
        {tabs.map(t => (
          <button key={t.key} onClick={() => setTab(t.key)}
            className={`px-4 py-2 rounded-xl text-sm font-medium transition ${tab === t.key ? 'bg-black text-white' : 'bg-white/50 text-gray-600 hover:bg-gray-100'}`}>
            {t.label}
          </button>
        ))}
      </div>

      <div className="glass-card p-4 sm:p-6">
        <ReferentielTab type={tab} label={tabs.find(t => t.key === tab)?.label} fields={fieldConfigs[tab]}
          departements={tab === 'postes' ? departements : []} />
      </div>
    </div>
  );
}
