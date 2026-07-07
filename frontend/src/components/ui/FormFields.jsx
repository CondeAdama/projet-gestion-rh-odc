export function InputField({ label, type = 'text', value, onChange, required, placeholder, disabled, min, max, minLength, maxLength, pattern, step, error }) {
  return (
    <div className="space-y-1.5">
      <label className="text-xs font-semibold text-gray-600">
        {label}{required && <span className="text-red-500 ml-0.5">*</span>}
      </label>
      <input
        type={type}
        value={value}
        onChange={onChange}
        required={required}
        placeholder={placeholder}
        disabled={disabled}
        min={min}
        max={max}
        minLength={minLength}
        maxLength={maxLength}
        pattern={pattern}
        step={step}
        className={`w-full px-4 py-2.5 bg-white/60 border rounded-xl text-sm outline-none focus:ring-2 text-[#1D1D1F] placeholder-gray-400 transition disabled:bg-gray-50 disabled:text-gray-400 ${
          error ? 'border-red-300 focus:ring-red-200' : 'border-black/5 focus:ring-black/10'
        }`}
      />
      {error && <p className="text-xs text-red-500">{error}</p>}
    </div>
  );
}

export function SelectField({ label, value, onChange, options, required, placeholder = '— Sélectionner —', disabled, error }) {
  return (
    <div className="space-y-1.5">
      <label className="text-xs font-semibold text-gray-600">
        {label}{required && <span className="text-red-500 ml-0.5">*</span>}
      </label>
      <select
        value={value}
        onChange={onChange}
        required={required}
        disabled={disabled}
        className={`w-full px-4 py-2.5 bg-white/60 border rounded-xl text-sm outline-none focus:ring-2 text-[#1D1D1F] transition appearance-none disabled:bg-gray-50 disabled:text-gray-400 ${
          error ? 'border-red-300 focus:ring-red-200' : 'border-black/5 focus:ring-black/10'
        }`}
      >
        <option value="">{placeholder}</option>
        {options.map(opt => (
          <option key={opt.value} value={opt.value}>{opt.label}</option>
        ))}
      </select>
      {error && <p className="text-xs text-red-500">{error}</p>}
    </div>
  );
}

export function SearchBar({ value, onChange, placeholder = 'Rechercher...' }) {
  return (
    <div className="relative">
      <svg className="absolute left-3.5 top-1/2 -translate-y-1/2 text-gray-400" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
        <circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/>
      </svg>
      <input
        type="text"
        value={value}
        onChange={onChange}
        placeholder={placeholder}
        className="w-full pl-10 pr-4 py-2.5 bg-white/60 border border-black/5 rounded-xl text-sm outline-none focus:ring-2 focus:ring-black/10 transition"
      />
    </div>
  );
}
