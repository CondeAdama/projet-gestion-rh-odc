import { QRCodeSVG } from 'qrcode.react';
import { useConfig } from '../../context/ConfigContext';

export default function CarteVisiteur({
  visiteur,
  numeroCarte,
  motif,
  dateEntree,
  id = 'carte-visiteur',
}) {
  const { config, logoUrl } = useConfig();
  const generated = new Date().toLocaleDateString('fr-FR');
  const qrValue = numeroCarte || 'VISITEUR';

  return (
    <div
      id={id}
      className="w-[260px] h-[400px] mx-auto rounded-2xl overflow-hidden shadow-2xl flex flex-col text-left print-target print:shadow-none"
      style={{ fontFamily: 'system-ui, sans-serif' }}
    >
      <div className="bg-[#1e3a5f] text-white px-4 pt-4 pb-3">
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
            <p className="text-[7px] text-white/70 uppercase">Accueil & Visites</p>
          </div>
        </div>
        <p className="text-[8px] font-semibold tracking-wide">Badge Visiteur</p>
        <div className="h-0.5 bg-[#f39200] mt-2 rounded-full" />
      </div>

      <div className="flex-1 bg-white px-4 py-3 flex flex-col gap-2">
        <div className="text-center">
          {visiteur ? (
            <>
              <p className="text-[11px] font-black text-[#1e3a5f] uppercase leading-tight">
                {visiteur.nom}<br />{visiteur.prenom}
              </p>
              <p className="text-[8px] text-gray-600 mt-1">
                {visiteur.entreprise || 'Visiteur'}
              </p>
              {visiteur.contact && (
                <p className="text-[7px] text-gray-500 mt-0.5">{visiteur.contact}</p>
              )}
            </>
          ) : (
            <>
              <p className="text-[11px] font-black text-[#1e3a5f] uppercase">Visiteur</p>
              <p className="text-[8px] text-gray-500 mt-1">Carte non assignée</p>
            </>
          )}
        </div>

        {motif && (
          <p className="text-[7px] text-gray-600 text-center leading-tight px-1">
            <span className="font-bold text-[#1e3a5f]">Motif :</span> {motif}
          </p>
        )}

        <div className="flex-1 flex flex-col items-center justify-center">
          <div className="border-2 border-[#1e3a5f]/30 rounded-lg p-1.5 bg-white relative">
            <div className="absolute top-0 left-0 w-3 h-3 border-t-2 border-l-2 border-[#1e3a5f]" />
            <div className="absolute top-0 right-0 w-3 h-3 border-t-2 border-r-2 border-[#1e3a5f]" />
            <div className="absolute bottom-0 left-0 w-3 h-3 border-b-2 border-l-2 border-[#1e3a5f]" />
            <div className="absolute bottom-0 right-0 w-3 h-3 border-b-2 border-r-2 border-[#1e3a5f]" />
            <QRCodeSVG value={qrValue} size={80} />
          </div>
          <p className="text-[9px] font-bold text-gray-800 mt-2 font-mono">
            {numeroCarte || '—'}
          </p>
        </div>
      </div>

      <div className="bg-[#1e3a5f] text-white px-4 py-2.5">
        <div className="h-0.5 bg-[#f39200] mb-2 rounded-full" />
        <div className="text-[7px] text-white/80 space-y-0.5">
          {dateEntree && <p>Entrée : {dateEntree.replace('T', ' ').slice(0, 16)}</p>}
          <p>Généré le : {generated}</p>
        </div>
      </div>
    </div>
  );
}
