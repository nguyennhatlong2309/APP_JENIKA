'use client';

import React from 'react';

interface PaginationProps {
  currentPage: number;
  totalPages: number;
  onPageChange: (page: number) => void;
  totalItems?: number;
  itemsPerPage?: number;
  onItemsPerPageChange?: (size: number) => void;
  itemsPerPageOptions?: number[];
}

export default function Pagination({
  currentPage,
  totalPages,
  onPageChange,
  totalItems,
  itemsPerPage,
  onItemsPerPageChange,
  itemsPerPageOptions = [10, 25, 50, 100],
}: PaginationProps) {
  if (totalPages <= 1 && !onItemsPerPageChange) return null;

  // Generate pagination pages array with ellipsis (e.g. 1, '...', 4, 5, 6, '...', 10)
  const getPageNumbers = () => {
    const pages: (number | string)[] = [];
    const delta = 1; // Number of active pages around current page
    const left = currentPage - delta;
    const right = currentPage + delta + 1;
    const range: number[] = [];
    let l: number | undefined;

    for (let i = 1; i <= totalPages; i++) {
      if (i === 1 || i === totalPages || (i >= left && i < right)) {
        range.push(i);
      }
    }

    for (const i of range) {
      if (l !== undefined) {
        if (i - l === 2) {
          pages.push(l + 1);
        } else if (i - l > 2) {
          pages.push('...');
        }
      }
      pages.push(i);
      l = i;
    }

    return pages;
  };

  const handlePrev = () => {
    if (currentPage > 1) onPageChange(currentPage - 1);
  };

  const handleNext = () => {
    if (currentPage < totalPages) onPageChange(currentPage + 1);
  };

  const handleFirst = () => {
    if (currentPage > 1) onPageChange(1);
  };

  const handleLast = () => {
    if (currentPage < totalPages) onPageChange(totalPages);
  };

  // Calculate standard showing index range label (e.g. "Hiển thị 1 - 10 trong số 100")
  const renderRangeLabel = () => {
    if (totalItems === undefined || itemsPerPage === undefined) return null;
    const start = (currentPage - 1) * itemsPerPage + 1;
    const end = Math.min(currentPage * itemsPerPage, totalItems);
    if (totalItems === 0) return <span className="text-on-surface-variant">Không có mục nào</span>;
    return (
      <span className="text-xs text-on-surface-variant font-medium">
        Hiển thị <span className="text-white font-bold">{start}</span> -{' '}
        <span className="text-white font-bold">{end}</span> trong số{' '}
        <span className="text-primary font-bold">{totalItems}</span> bản ghi
      </span>
    );
  };

  return (
    <div className="flex flex-col sm:flex-row items-center justify-between gap-4 py-3.5 px-6 border-t border-border-glass bg-white/1 w-full text-xs">
      {/* Left section: Range label / Info */}
      <div className="flex items-center gap-2">
        {renderRangeLabel()}
      </div>

      {/* Right section: Controls + Items per page selector */}
      <div className="flex items-center flex-wrap gap-4 sm:gap-6 justify-center">
        {/* Page size dropdown */}
        {onItemsPerPageChange && itemsPerPage !== undefined && (
          <div className="flex items-center gap-2">
            <span className="text-on-surface-variant text-xs">Hiển thị:</span>
            <select
              value={itemsPerPage}
              onChange={(e) => onItemsPerPageChange(parseInt(e.target.value))}
              className="bg-surface-lowest border border-border-glass rounded px-2 py-1 text-white text-xs outline-none focus:ring-1 focus:ring-primary/50 cursor-pointer transition-all"
            >
              {itemsPerPageOptions.map((opt) => (
                <option key={opt} value={opt} className="bg-surface-lowest">
                  {opt} hàng
                </option>
              ))}
            </select>
          </div>
        )}

        {/* Navigation Buttons */}
        <div className="flex items-center gap-1 bg-white/2 border border-border-glass rounded-lg p-0.5">
          {/* First page button */}
          <button
            onClick={handleFirst}
            disabled={currentPage === 1}
            className="p-1.5 rounded hover:bg-white/5 disabled:opacity-30 disabled:pointer-events-none text-on-surface-variant hover:text-white transition-all cursor-pointer flex items-center justify-center"
            title="Đầu trang"
          >
            <span className="material-symbols-outlined text-sm md:text-base">first_page</span>
          </button>

          {/* Prev page button */}
          <button
            onClick={handlePrev}
            disabled={currentPage === 1}
            className="p-1.5 rounded hover:bg-white/5 disabled:opacity-30 disabled:pointer-events-none text-on-surface-variant hover:text-white transition-all cursor-pointer flex items-center justify-center"
            title="Trang trước"
          >
            <span className="material-symbols-outlined text-sm md:text-base">chevron_left</span>
          </button>

          {/* Page numbers */}
          <div className="flex items-center gap-0.5">
            {getPageNumbers().map((pageNum, idx) => {
              if (pageNum === '...') {
                return (
                  <span
                    key={`dots-${idx}`}
                    className="px-2.5 py-1 text-on-surface-variant font-bold select-none text-xs"
                  >
                    ...
                  </span>
                );
              }

              const isCurrent = pageNum === currentPage;
              return (
                <button
                  key={`page-${pageNum}`}
                  onClick={() => onPageChange(pageNum as number)}
                  className={`px-3 py-1 rounded text-xs font-bold transition-all cursor-pointer ${
                    isCurrent
                      ? 'bg-primary text-on-primary font-bold shadow-md shadow-primary/20'
                      : 'text-on-surface-variant hover:bg-white/5 hover:text-white'
                  }`}
                >
                  {pageNum}
                </button>
              );
            })}
          </div>

          {/* Next page button */}
          <button
            onClick={handleNext}
            disabled={currentPage === totalPages || totalPages === 0}
            className="p-1.5 rounded hover:bg-white/5 disabled:opacity-30 disabled:pointer-events-none text-on-surface-variant hover:text-white transition-all cursor-pointer flex items-center justify-center"
            title="Trang sau"
          >
            <span className="material-symbols-outlined text-sm md:text-base">chevron_right</span>
          </button>

          {/* Last page button */}
          <button
            onClick={handleLast}
            disabled={currentPage === totalPages || totalPages === 0}
            className="p-1.5 rounded hover:bg-white/5 disabled:opacity-30 disabled:pointer-events-none text-on-surface-variant hover:text-white transition-all cursor-pointer flex items-center justify-center"
            title="Cuối trang"
          >
            <span className="material-symbols-outlined text-sm md:text-base">last_page</span>
          </button>
        </div>
      </div>
    </div>
  );
}
