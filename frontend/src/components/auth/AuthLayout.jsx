import { motion } from 'framer-motion';
import { useConfig } from '../../context/ConfigContext';
import CompanyBrand from '../brand/CompanyBrand';

export default function AuthLayout({ children, title, subtitle }) {
  const { companyName } = useConfig();

  return (
    <div className="min-h-screen flex bg-[#0b1220]">
      <motion.div
        initial={{ opacity: 0, x: -30 }}
        animate={{ opacity: 1, x: 0 }}
        transition={{ duration: 0.6 }}
        className="hidden lg:flex lg:w-1/2 relative overflow-hidden"
      >
        <div className="absolute inset-0 bg-gradient-to-br from-[#1e3a5f] via-[#1d4ed8] to-[#6d28d9]" />
        <div
          className="absolute inset-0 opacity-30"
          style={{
            backgroundImage: 'radial-gradient(circle at 20% 50%, white 1px, transparent 1px), radial-gradient(circle at 80% 20%, white 1px, transparent 1px)',
            backgroundSize: '48px 48px',
          }}
        />
        <motion.div
          animate={{ y: [0, -12, 0], rotate: [0, 2, 0] }}
          transition={{ duration: 8, repeat: Infinity, ease: 'easeInOut' }}
          className="absolute top-20 right-16 w-48 h-48 rounded-full bg-white/10 blur-2xl"
        />
        <motion.div
          animate={{ y: [0, 16, 0] }}
          transition={{ duration: 6, repeat: Infinity, ease: 'easeInOut' }}
          className="absolute bottom-24 left-12 w-64 h-64 rounded-full bg-violet-400/20 blur-3xl"
        />

        <div className="relative z-10 flex flex-col justify-between p-12 text-white w-full">
          <CompanyBrand size="lg" dark className="[&_img]:shadow-blue-900/40" />

          <div className="space-y-6 max-w-md">
            <h2 className="text-4xl font-bold leading-tight">
              Gestion des ressources humaines, simplifiée.
            </h2>
            <p className="text-white/70 text-lg leading-relaxed">
              Plateforme sécurisée pour le suivi des employés, la paie, les congés et les présences — {companyName}.
            </p>
            <div className="flex flex-wrap gap-3 pt-4">
              {['Employés', 'Paie', 'Congés', 'Pointage'].map((item, i) => (
                <motion.span
                  key={item}
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ delay: 0.3 + i * 0.1 }}
                  className="px-3 py-1.5 rounded-full bg-white/10 backdrop-blur text-xs font-medium border border-white/20"
                >
                  {item}
                </motion.span>
              ))}
            </div>
          </div>

          <p className="text-xs text-white/40">© {new Date().getFullYear()} {companyName} — Tous droits réservés</p>
        </div>
      </motion.div>

      <div className="flex-1 flex items-center justify-center p-6 sm:p-10">
        <motion.div
          initial={{ opacity: 0, y: 24 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5, delay: 0.1 }}
          className="w-full max-w-md"
        >
          <div className="lg:hidden mb-8 flex justify-center">
            <CompanyBrand dark />
          </div>

          {(title || subtitle) && (
            <div className="mb-8 text-center lg:text-left">
              {title && <h1 className="text-2xl font-bold text-white mb-2">{title}</h1>}
              {subtitle && <p className="text-sm text-slate-400">{subtitle}</p>}
            </div>
          )}

          <div className="bg-white/5 backdrop-blur-2xl border border-white/10 rounded-3xl p-6 sm:p-8 shadow-2xl shadow-black/40">
            {children}
          </div>
        </motion.div>
      </div>
    </div>
  );
}
