import { useConfig } from '../../context/ConfigContext';
import MinervaLogo from '../auth/MinervaLogo';
import { LogoImage } from '../ui/Display';

/**
 * Affichage centralisé du logo et du nom d'entreprise (config dynamique).
 */
export default function CompanyBrand({ size = 'md', showSubtitle = true, className = '', dark = false }) {
  const { logoUrl, config } = useConfig();
  const companyName = config?.nomEntreprise || 'MINERVA GROUP';

  const sizes = {
    sm: { img: 'w-10 h-10', title: 'text-sm', sub: 'text-[10px]' },
    md: { img: 'w-14 h-14', title: 'text-lg', sub: 'text-xs' },
    lg: { img: 'w-20 h-20', title: 'text-2xl', sub: 'text-sm' },
  };
  const s = sizes[size] || sizes.md;
  const textMain = dark ? 'text-white' : 'text-[#1D1D1F]';
  const textMuted = dark ? 'text-white/60' : 'text-gray-500';

  if (logoUrl) {
    return (
      <div className={`flex items-center gap-3 ${className}`}>
        <LogoImage
          src={logoUrl}
          alt={companyName}
          companyName={companyName}
          className={`${s.img} object-contain rounded-2xl shadow-lg bg-white p-0.5`}
          fallbackClassName={`${s.img} rounded-2xl shadow-lg bg-gradient-to-br from-violet-800 to-gray-900 text-white flex items-center justify-center text-sm font-bold`}
        />
        <div className="min-w-0">
          <p className={`${s.title} font-bold tracking-tight truncate ${textMain}`}>{companyName}</p>
          {showSubtitle && <p className={`${s.sub} ${textMuted}`}>Gestion RH</p>}
        </div>
      </div>
    );
  }

  return <MinervaLogo size={size} companyName={companyName} className={className} dark={dark} />;
}
