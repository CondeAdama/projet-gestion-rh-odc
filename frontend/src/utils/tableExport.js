import * as XLSX from 'xlsx';
import { jsPDF } from 'jspdf';
import autoTable from 'jspdf-autotable';

/**
 * @typedef {{ header: string, key?: string, accessor?: (row: object) => unknown }} ExportColumn
 */

function cellValue(row, column) {
  const raw = typeof column.accessor === 'function'
    ? column.accessor(row)
    : row[column.key];
  if (raw == null) return '';
  return String(raw);
}

export function exportTableToExcel(columns, rows, basename) {
  if (!rows?.length) {
    throw new Error('Aucune ligne à exporter.');
  }
  const header = columns.map(c => c.header);
  const data = rows.map(row => columns.map(col => cellValue(row, col)));
  const sheet = XLSX.utils.aoa_to_sheet([header, ...data]);
  const book = XLSX.utils.book_new();
  XLSX.utils.book_append_sheet(book, sheet, 'Export');
  XLSX.writeFile(book, `${basename}.xlsx`);
}

export function exportTableToPdf(columns, rows, basename, title) {
  if (!rows?.length) {
    throw new Error('Aucune ligne à exporter.');
  }
  const landscape = columns.length > 6;
  const doc = new jsPDF({ orientation: landscape ? 'landscape' : 'portrait', unit: 'mm', format: 'a4' });
  const heading = title || basename;
  doc.setFontSize(14);
  doc.setTextColor(30, 58, 95);
  doc.text(heading, 14, 16);
  doc.setFontSize(9);
  doc.setTextColor(100, 100, 100);
  doc.text(`Export MINERVA RH — ${new Date().toLocaleString('fr-FR')}`, 14, 22);

  autoTable(doc, {
    head: [columns.map(c => c.header)],
    body: rows.map(row => columns.map(col => cellValue(row, col))),
    startY: 26,
    styles: { fontSize: 8, cellPadding: 2, overflow: 'linebreak' },
    headStyles: { fillColor: [30, 58, 95], textColor: 255 },
    alternateRowStyles: { fillColor: [245, 247, 250] },
    margin: { left: 14, right: 14 },
  });

  doc.save(`${basename}.pdf`);
}

export const FILTER_SELECT_CLASS =
  'px-3 py-2.5 bg-white/60 border border-black/5 rounded-xl text-sm outline-none focus:ring-2 focus:ring-black/10 min-w-[10rem]';
