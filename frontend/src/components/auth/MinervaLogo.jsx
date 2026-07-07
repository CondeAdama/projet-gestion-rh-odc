export default function MinervaLogo({ size = 'md', className = '', companyName = 'MINERVA GROUP', dark = false }) {
  const sizes = {
    sm: { box: 'w-10 h-10 text-sm', title: 'text-lg' },
    md: { box: 'w-14 h-14 text-lg', title: 'text-2xl' },
    lg: { box: 'w-20 h-20 text-2xl', title: 'text-3xl' },
  };
  const s = sizes[size] || sizes.md;

  const parts = companyName.trim().split(/\s+/);
  const primary = parts[0] || 'MINERVA';
  const secondary = parts.slice(1).join(' ') || 'GROUP';

  const titleClass = dark ? 'text-white' : 'text-[#0f172a]';
  const subClass = dark ? 'text-white/60' : 'text-[#64748b]';

  return (
    <div className={`flex items-center gap-3 ${className}`}>
      <div className={`${s.box} rounded-2xl bg-gradient-to-br from-[#1e3a5f] via-[#2563eb] to-[#7c3aed] flex items-center justify-center text-white font-black shadow-lg shadow-blue-500/25`}>
        {primary.charAt(0).toUpperCase()}
      </div>
      <div>
        <p className={`${s.title} font-bold tracking-tight ${titleClass}`}>{primary}</p>
        <p className={`text-[10px] font-semibold uppercase tracking-[0.25em] ${subClass}`}>
          {secondary} · RH
        </p>
      </div>
    </div>
  );
}
