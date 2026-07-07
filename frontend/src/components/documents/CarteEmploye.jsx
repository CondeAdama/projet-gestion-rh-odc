import { QRCodeSVG } from 'qrcode.react';
import { resolveAssetUrl } from '../../utils/assets';
import { useConfig } from '../../context/ConfigContext';

export function DocumentHeader({ title, subtitle, className = '' }) {
  const { config, logoUrl } = useConfig();

  return (
    <div className={`flex justify-between items-start border-b-2 border-gray-900 pb-4 ${className}`}>
      <div className="flex items-start gap-3">
        {logoUrl ? (
          <img src={logoUrl} alt="Logo" className="w-14 h-14 object-contain rounded-lg border border-gray-200 bg-white p-1" crossOrigin="anonymous" />
        ) : (
          <div className="w-14 h-14 rounded-lg bg-[#1a4a8e] text-white flex items-center justify-center text-xs font-bold">
            LOGO
          </div>
        )}
        <div>
          <h2 className="text-lg font-bold text-gray-900">{config?.nomEntreprise || 'MINERVA GROUP'}</h2>
          {config?.slogan && <p className="text-[10px] text-gray-500 uppercase tracking-wider">{config.slogan}</p>}
          {config?.adresse && <p className="text-[10px] text-gray-500 mt-0.5">{config.adresse}</p>}
          {config?.nif && <p className="text-[10px] text-gray-500">NIF: {config.nif}</p>}
          {config?.numeroCnss && <p className="text-[10px] text-gray-500">CNSS: {config.numeroCnss}</p>}
        </div>
      </div>
      <div className="text-right">
        <h1 className="text-base font-black uppercase text-gray-900">{title}</h1>
        {subtitle && <p className="text-xs font-semibold text-gray-700 uppercase mt-1">{subtitle}</p>}
      </div>
    </div>
  );
}

export default function CarteEmploye({ employe, id = 'carte-employe' }) {
  const { config, logoUrl } = useConfig();
  const photoUrl = resolveAssetUrl(employe?.photoUrl);
  const generated = new Date().toLocaleDateString('fr-FR');
  const validUntil = new Date(Date.now() + 365 * 24 * 60 * 60 * 1000).toLocaleDateString('fr-FR');

  return (
    <div
      id={id}
      className="w-[260px] h-[400px] mx-auto rounded-2xl overflow-hidden shadow-2xl flex flex-col text-left print-target print:shadow-none"
      style={{ fontFamily: 'system-ui, sans-serif' }}
    >
      {/* En-tête bleu */}
      <div className="bg-[#1a4a8e] text-white px-4 pt-4 pb-3">
        <div className="flex items-center gap-2 mb-2">
          {logoUrl ? (
            <img src={logoUrl} alt="" className="w-10 h-10 object-contain bg-white rounded p-0.5" crossOrigin="anonymous" />
          ) : (
            <div className="w-10 h-10 bg-white/20 border border-white/40 rounded text-[7px] flex items-center justify-center font-bold">
              LOGO
            </div>
          )}
          <div className="min-w-0">
            <p className="text-[9px] font-bold uppercase truncate">{config?.nomEntreprise || 'MINERVA GROUP'}</p>
            <p className="text-[7px] text-white/70 uppercase">Gestion RH</p>
          </div>
        </div>
        <p className="text-[8px] font-semibold tracking-wide">Badge d'Identification Employé</p>
        <div className="h-0.5 bg-[#f39200] mt-2 rounded-full" />
      </div>

      {/* Corps blanc */}
      <div className="flex-1 bg-white px-4 py-3 flex gap-3">
        <div className="flex flex-col items-center w-[90px] flex-shrink-0">
          {photoUrl ? (
            <img src={photoUrl} alt="" className="w-[72px] h-[88px] object-cover rounded border-2 border-[#1a4a8e]/20" crossOrigin="anonymous" />
          ) : (
            <div className="w-[72px] h-[88px] rounded bg-gray-100 border-2 border-[#1a4a8e]/20 flex items-center justify-center text-xl font-bold text-[#1a4a8e]">
              {(employe?.prenom?.[0] || '')}{(employe?.nom?.[0] || '')}
            </div>
          )}
          <p className="text-[9px] font-black text-[#1a4a8e] uppercase text-center mt-2 leading-tight">
            {employe?.nom}<br />{employe?.prenom}
          </p>
          <p className="text-[7px] text-gray-600 text-center mt-1 leading-tight">
            {employe?.posteLibelle || employe?.poste || 'Collaborateur'}
          </p>
        </div>

        <div className="flex-1 flex flex-col items-center justify-center">
          <p className="text-[8px] font-bold text-[#1a4a8e] uppercase mb-1.5">Pointage Présence</p>
          <div className="border-2 border-[#1a4a8e]/30 rounded-lg p-1.5 bg-white relative">
            <div className="absolute top-0 left-0 w-3 h-3 border-t-2 border-l-2 border-[#1a4a8e]" />
            <div className="absolute top-0 right-0 w-3 h-3 border-t-2 border-r-2 border-[#1a4a8e]" />
            <div className="absolute bottom-0 left-0 w-3 h-3 border-b-2 border-l-2 border-[#1a4a8e]" />
            <div className="absolute bottom-0 right-0 w-3 h-3 border-b-2 border-r-2 border-[#1a4a8e]" />
            <QRCodeSVG value={employe?.matricule || 'N/A'} size={88} />
          </div>
          <p className="text-[8px] font-bold text-gray-800 mt-2 font-mono">
            MATRICULE: {employe?.matricule}
          </p>
        </div>
      </div>

      {/* Pied bleu */}
      <div className="bg-[#1a4a8e] text-white px-4 py-2.5">
        <div className="h-0.5 bg-[#f39200] mb-2 rounded-full" />
        <div className="text-[7px] text-white/80 space-y-0.5">
          <p>Valide jusqu'au : {validUntil}</p>
          <p>Généré le : {generated}</p>
        </div>
      </div>
    </div>
  );
}
