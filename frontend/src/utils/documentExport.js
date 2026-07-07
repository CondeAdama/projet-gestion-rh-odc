import html2canvas from 'html2canvas';
import { jsPDF } from 'jspdf';

/** Dimensions badge CR80 vertical (taille réelle) */
export const BADGE_WIDTH_MM = 54;
export const BADGE_HEIGHT_MM = 86;
const DPI = 300;
const MAX_SCALE = 5;

const UNSUPPORTED_COLOR = /oklab|oklch|lab\(|lch\(|color-mix|color\(/i;

const STYLE_PROPS = [
  'display', 'position', 'top', 'left', 'right', 'bottom',
  'width', 'height', 'minWidth', 'minHeight', 'maxWidth', 'maxHeight',
  'margin', 'marginTop', 'marginRight', 'marginBottom', 'marginLeft',
  'padding', 'paddingTop', 'paddingRight', 'paddingBottom', 'paddingLeft',
  'border', 'borderTop', 'borderRight', 'borderBottom', 'borderLeft',
  'borderRadius', 'borderTopLeftRadius', 'borderTopRightRadius',
  'borderBottomLeftRadius', 'borderBottomRightRadius',
  'borderWidth', 'borderStyle',
  'flex', 'flexDirection', 'flexWrap', 'alignItems', 'justifyContent',
  'alignSelf', 'gap', 'flexShrink', 'flexGrow', 'flexBasis',
  'gridTemplateColumns', 'gridTemplateRows', 'gridColumn', 'gridRow',
  'fontFamily', 'fontSize', 'fontWeight', 'fontStyle', 'lineHeight',
  'letterSpacing', 'textAlign', 'textTransform', 'textDecoration',
  'verticalAlign', 'whiteSpace', 'wordBreak', 'overflow', 'overflowX', 'overflowY',
  'opacity', 'objectFit', 'boxSizing', 'boxShadow', 'transform', 'zIndex',
];

const COLOR_PROPS = [
  'color', 'backgroundColor', 'borderTopColor', 'borderRightColor',
  'borderBottomColor', 'borderLeftColor', 'outlineColor', 'fill', 'stroke',
];

function getElement(elementOrId) {
  if (typeof elementOrId === 'string') {
    return document.getElementById(elementOrId);
  }
  return elementOrId;
}

function scaleForRealSize(widthPx, widthMm) {
  const targetPx = (widthMm / 25.4) * DPI;
  return Math.min(Math.max(targetPx / widthPx, 1), MAX_SCALE);
}

/** Convertit oklab/oklch/etc. en rgb via le moteur canvas du navigateur */
function toSafeColor(value) {
  if (!value || value === 'transparent' || value === 'none' || value === 'initial') {
    return value;
  }
  if (!UNSUPPORTED_COLOR.test(value)) {
    return value;
  }
  try {
    const canvas = document.createElement('canvas');
    canvas.width = canvas.height = 1;
    const ctx = canvas.getContext('2d');
    ctx.fillStyle = '#ffffff';
    ctx.fillRect(0, 0, 1, 1);
    ctx.fillStyle = value;
    ctx.fillRect(0, 0, 1, 1);
    const [r, g, b, a] = ctx.getImageData(0, 0, 1, 1).data;
    if (a === 0) return 'transparent';
    if (a === 255) return `rgb(${r}, ${g}, ${b})`;
    return `rgba(${r}, ${g}, ${b}, ${(a / 255).toFixed(3)})`;
  } catch {
    return '#000000';
  }
}

function copySafeStyles(source, target) {
  const computed = window.getComputedStyle(source);

  for (const prop of STYLE_PROPS) {
    let value = computed[prop];
    if (!value || value === 'auto' || value === 'normal') continue;
    if (typeof value === 'string' && UNSUPPORTED_COLOR.test(value)) {
      if (prop === 'backgroundImage') {
        target.style.backgroundImage = 'none';
      }
      continue;
    }
    if (prop === 'backgroundImage' && value && value !== 'none') {
      target.style.backgroundImage = 'none';
      target.style.backgroundColor = toSafeColor(computed.backgroundColor);
      continue;
    }
    try {
      target.style[prop] = value;
    } catch {
      /* propriété non supportée en inline */
    }
  }

  for (const prop of COLOR_PROPS) {
    target.style[prop] = toSafeColor(computed[prop]);
  }

  if (UNSUPPORTED_COLOR.test(computed.backgroundImage)) {
    target.style.backgroundImage = 'none';
    target.style.backgroundColor = toSafeColor(computed.backgroundColor);
  }

  target.style.boxShadow = 'none';
}

function mirrorStyles(sourceRoot, targetRoot) {
  copySafeStyles(sourceRoot, targetRoot);
  const sourceNodes = sourceRoot.querySelectorAll('*');
  const targetNodes = targetRoot.querySelectorAll('*');
  sourceNodes.forEach((src, i) => {
    if (targetNodes[i]) copySafeStyles(src, targetNodes[i]);
  });
}

async function urlToDataUrl(url) {
  if (!url || url.startsWith('data:') || url.startsWith('blob:')) return url;
  try {
    const response = await fetch(url, { mode: 'cors', credentials: 'omit' });
    if (!response.ok) return null;
    const blob = await response.blob();
    return await new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onloadend = () => resolve(reader.result);
      reader.onerror = reject;
      reader.readAsDataURL(blob);
    });
  } catch {
    return null;
  }
}

async function svgToPngDataUrl(svg) {
  const rect = svg.getBoundingClientRect();
  const w = Number(svg.getAttribute('width')) || rect.width || 88;
  const h = Number(svg.getAttribute('height')) || rect.height || 88;
  const clone = svg.cloneNode(true);
  clone.setAttribute('xmlns', 'http://www.w3.org/2000/svg');
  if (!clone.getAttribute('width')) clone.setAttribute('width', String(w));
  if (!clone.getAttribute('height')) clone.setAttribute('height', String(h));

  const svgString = new XMLSerializer().serializeToString(clone);
  const blob = new Blob([svgString], { type: 'image/svg+xml;charset=utf-8' });
  const objectUrl = URL.createObjectURL(blob);

  try {
    return await new Promise((resolve, reject) => {
      const img = new Image();
      img.onload = () => {
        const canvas = document.createElement('canvas');
        canvas.width = w;
        canvas.height = h;
        const ctx = canvas.getContext('2d');
        ctx.fillStyle = '#ffffff';
        ctx.fillRect(0, 0, w, h);
        ctx.drawImage(img, 0, 0, w, h);
        URL.revokeObjectURL(objectUrl);
        resolve(canvas.toDataURL('image/png'));
      };
      img.onerror = () => {
        URL.revokeObjectURL(objectUrl);
        reject(new Error('Conversion SVG impossible'));
      };
      img.src = objectUrl;
    });
  } catch {
    URL.revokeObjectURL(objectUrl);
    return `data:image/svg+xml;charset=utf-8,${encodeURIComponent(svgString)}`;
  }
}

async function inlineImages(container) {
  const imgs = [...container.querySelectorAll('img')];
  await Promise.all(imgs.map(async (img) => {
    const src = img.currentSrc || img.getAttribute('src');
    if (!src || src.startsWith('data:') || src.startsWith('blob:')) return;
    const dataUrl = await urlToDataUrl(src);
    if (dataUrl) {
      img.removeAttribute('crossorigin');
      img.src = dataUrl;
      await img.decode().catch(() => {});
    }
  }));
}

async function replaceSvgsWithImages(container) {
  const svgs = [...container.querySelectorAll('svg')];
  for (const svg of svgs) {
    const rect = svg.getBoundingClientRect();
    const w = Number(svg.getAttribute('width')) || rect.width || 88;
    const h = Number(svg.getAttribute('height')) || rect.height || 88;
    const dataUrl = await svgToPngDataUrl(svg);
    const img = document.createElement('img');
    img.src = dataUrl;
    img.width = w;
    img.height = h;
    img.style.width = `${w}px`;
    img.style.height = `${h}px`;
    img.style.display = 'block';
    svg.replaceWith(img);
    await img.decode().catch(() => {});
  }
}

function stripClasses(root) {
  root.removeAttribute('class');
  root.querySelectorAll('[class]').forEach((node) => node.removeAttribute('class'));
}

/** Iframe isolé sans Tailwind → évite l'erreur oklab de html2canvas */
async function buildIsolatedClone(sourceEl) {
  const width = sourceEl.offsetWidth || sourceEl.getBoundingClientRect().width;
  const height = sourceEl.offsetHeight || sourceEl.getBoundingClientRect().height;
  if (!width || !height) {
    throw new Error('Élément non visible pour l\'export');
  }

  const iframe = document.createElement('iframe');
  iframe.setAttribute('aria-hidden', 'true');
  iframe.style.cssText = `position:fixed;left:-10000px;top:0;border:0;width:${width}px;height:${height + 40}px;opacity:0;pointer-events:none;`;
  document.body.appendChild(iframe);

  const doc = iframe.contentDocument;
  doc.open();
  doc.write(`<!DOCTYPE html><html><head><meta charset="utf-8"><style>
    * { box-sizing: border-box; }
    html, body { margin: 0; padding: 0; background: #ffffff; }
  </style></head><body></body></html>`);
  doc.close();

  const clone = sourceEl.cloneNode(true);
  clone.removeAttribute('id');
  mirrorStyles(sourceEl, clone);
  stripClasses(clone);
  clone.style.margin = '0';
  clone.style.transform = 'none';
  clone.style.boxShadow = 'none';

  doc.body.appendChild(clone);
  await replaceSvgsWithImages(clone);
  await inlineImages(clone);
  await new Promise((resolve) => requestAnimationFrame(() => requestAnimationFrame(resolve)));

  return { iframe, clone, width, height: clone.offsetHeight || height };
}

export async function captureElement(elementOrId, { scale = 2, realSizeMm } = {}) {
  const el = getElement(elementOrId);
  if (!el) throw new Error('Élément introuvable pour l\'export');

  const { iframe, clone, width, height } = await buildIsolatedClone(el);
  const finalScale = realSizeMm ? scaleForRealSize(width, realSizeMm) : scale;

  try {
    return await html2canvas(clone, {
      scale: finalScale,
      useCORS: true,
      allowTaint: true,
      backgroundColor: '#ffffff',
      logging: false,
      foreignObjectRendering: false,
      width,
      height,
      windowWidth: width,
      windowHeight: height,
    });
  } finally {
    document.body.removeChild(iframe);
  }
}

export async function downloadElementAsImage(elementOrId, filename, options = {}) {
  const canvas = await captureElement(elementOrId, {
    scale: options.scale ?? 3,
    realSizeMm: options.realSizeMm ?? options.widthMm,
  });
  const link = document.createElement('a');
  link.download = filename.endsWith('.png') ? filename : `${filename}.png`;
  link.href = canvas.toDataURL('image/png', 1.0);
  link.click();
}

export async function downloadElementAsPdf(elementOrId, filename, options = {}) {
  const { widthMm, heightMm, format = 'a4', orientation = 'portrait' } = options;
  const isBadge = widthMm && heightMm;
  const canvas = await captureElement(elementOrId, {
    realSizeMm: isBadge ? widthMm : undefined,
    scale: isBadge ? undefined : 2,
  });
  const imgData = canvas.toDataURL('image/png');

  const pdf = isBadge
    ? new jsPDF({
        orientation: heightMm > widthMm ? 'portrait' : 'landscape',
        unit: 'mm',
        format: [widthMm, heightMm],
      })
    : new jsPDF({ orientation, unit: 'mm', format });

  if (isBadge) {
    pdf.addImage(imgData, 'PNG', 0, 0, widthMm, heightMm);
  } else {
    const pageW = pdf.internal.pageSize.getWidth();
    const pageH = pdf.internal.pageSize.getHeight();
    const margin = 10;
    const maxW = pageW - margin * 2;
    const maxH = pageH - margin * 2;
    const ratio = canvas.width / canvas.height;
    let w = maxW;
    let h = w / ratio;
    if (h > maxH) {
      h = maxH;
      w = h * ratio;
    }
    const x = (pageW - w) / 2;
    const y = margin;
    pdf.addImage(imgData, 'PNG', x, y, w, h);
  }

  pdf.save(filename.endsWith('.pdf') ? filename : `${filename}.pdf`);
}

export async function printElement(elementOrId, options = {}) {
  const { widthMm, heightMm, realSizeMm, scale = 2 } = options;
  const isBadge = widthMm && heightMm;
  const canvas = await captureElement(elementOrId, {
    realSizeMm: realSizeMm ?? (isBadge ? widthMm : undefined),
    scale: isBadge ? undefined : scale,
  });
  const dataUrl = canvas.toDataURL('image/png');

  const pageStyle = isBadge
    ? `@page { size: ${widthMm}mm ${heightMm}mm; margin: 0; }
       body { margin: 0; }
       img { width: ${widthMm}mm; height: ${heightMm}mm; display: block; }`
    : `@page { size: A4 portrait; margin: 10mm; }
       body { margin: 0; display: flex; justify-content: center; }
       img { max-width: 100%; height: auto; }`;

  const win = window.open('', '_blank', 'noopener,noreferrer');
  if (!win) {
    throw new Error('Autorisez les pop-ups pour imprimer ce document');
  }

  win.document.write(`<!DOCTYPE html><html><head><title>Impression</title>
    <style>${pageStyle}</style></head>
    <body><img src="${dataUrl}" alt="Document" /></body></html>`);
  win.document.close();

  await new Promise((resolve) => {
    const img = win.document.querySelector('img');
    const trigger = () => {
      win.focus();
      win.print();
      resolve();
    };
    if (img?.complete) trigger();
    else img.onload = trigger;
  });

  setTimeout(() => win.close(), 500);
}

export const BADGE_PRINT_CSS = `
  @media print {
    aside, nav, header, .no-print { display: none !important; }
    body * { visibility: hidden !important; }
    .print-target, .print-target * { visibility: visible !important; }
    .print-target {
      position: fixed !important;
      left: 50% !important;
      top: 50% !important;
      transform: translate(-50%, -50%) !important;
      box-shadow: none !important;
      margin: 0 !important;
    }
  }
`;

export const PAYSLIP_PRINT_CSS = `
  @media print {
    aside, nav, .no-print { display: none !important; }
    body * { visibility: hidden; }
    #print-payslip, #print-payslip * { visibility: visible; }
    #print-payslip {
      position: absolute;
      left: 0;
      top: 0;
      width: 100%;
      background: white !important;
      padding: 2rem !important;
    }
  }
`;
