'use client';

import { usePathname } from 'next/navigation';
import { useState } from 'react';

export default function Header() {
  const pathname = usePathname();
  const [isFocused, setIsFocused] = useState(false);

  const getPageTitle = (path: string) => {
    switch (path) {
      case '/':
        return 'Tổng quan Hệ thống';
      case '/inventory':
        return 'Quản lý Hàng hóa';
      case '/sales':
        return 'Bán hàng & Hóa đơn';
      case '/purchases':
        return 'Nhập hàng';
      case '/partners':
        return 'Đối tác và Nhân viên';
      case '/expenses':
        return 'Nhật ký Thu chi';
      case '/activity':
        return 'Nhật ký Hoạt động';
      case '/excel-config':
        return 'Cấu hình Excel';
      case '/settings':
        return 'Cài đặt Hệ thống';
      default:
        return 'Tổng quan Quản lý';
    }
  };

  return (
    <header className="flex justify-between items-center px-8 h-20 sticky top-0 z-30 bg-surface/60 backdrop-blur-md border-b border-white/5">
      <div className="flex items-center gap-8 flex-1">
        <h2 className="text-2xl text-white font-bold tracking-tight">{getPageTitle(pathname)}</h2>
        <div className={`relative w-80 group ${isFocused ? 'neon-glow' : ''} transition-all duration-300 rounded-full`}>
          <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-on-surface-variant">
            search
          </span>
          <input
            className="w-full h-11 bg-black/20 border border-white/10 rounded-full pl-12 pr-4 text-xs text-white focus:outline-none focus:ring-1 focus:ring-primary/50 transition-all placeholder-on-surface-variant/50"
            placeholder="Tìm kiếm sản phẩm, hóa đơn, nhật ký..."
            type="text"
            onFocus={() => setIsFocused(true)}
            onBlur={() => setIsFocused(false)}
          />
        </div>
      </div>
      <div className="flex items-center gap-6">
        <div className="flex items-center gap-2 px-3 py-1.5 glass-card rounded-full">
          <span className="w-2 h-2 rounded-full bg-primary animate-pulse-dot"></span>
          <span className="text-xs font-semibold text-primary">Hệ thống Trực tuyến</span>
        </div>
        <button className="material-symbols-outlined text-on-surface-variant hover:text-primary transition-all p-2 rounded-full hover:bg-white/5 relative">
          notifications
          <span className="absolute top-1.5 right-1.5 w-2 h-2 bg-error rounded-full"></span>
        </button>
        <button className="material-symbols-outlined text-on-surface-variant hover:text-primary transition-all p-2 rounded-full hover:bg-white/5">
          settings
        </button>
      </div>
    </header>
  );
}

