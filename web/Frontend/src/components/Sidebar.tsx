'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';

export default function Sidebar() {
  const pathname = usePathname();

  const menuItems = [
    { name: 'Tổng quan', path: '/', icon: 'dashboard' },
    { name: 'Quản lý hàng hóa', path: '/inventory', icon: 'inventory_2' },
    { name: 'Bán hàng', path: '/sales', icon: 'shopping_cart' },
    { name: 'Nhập hàng', path: '/purchases', icon: 'local_shipping' },
    { name: 'Đối tác và nhân viên', path: '/partners', icon: 'handshake' },
    { name: 'Nhật ký thu chi', path: '/expenses', icon: 'payments' },
    { name: 'Nhật ký hoạt động', path: '/activity', icon: 'history' },
    { name: 'Cấu hình Excel', path: '/excel-config', icon: 'table_chart' },
    { name: 'Cài đặt hệ thống', path: '/settings', icon: 'settings' },
  ];

  return (
    <aside className="flex flex-col w-[260px] h-screen fixed left-0 top-0 z-40 bg-surface/80 backdrop-blur-xl border-r border-white/10 shadow-2xl">
      <div className="p-8">
        <h1 className="text-3xl font-bold text-primary drop-shadow-[0_0_10px_rgba(73,252,223,0.4)]">CAFE DI ROM</h1>
        <p className="text-[10px] tracking-widest text-on-surface-variant/60 uppercase mt-1">Cafe &amp; F&amp;B Management</p>
      </div>
      <nav className="flex-1 overflow-y-auto mt-4 px-4 space-y-2">
        {menuItems.map((item) => {
          const isActive = pathname === item.path;
          return (
            <Link
              key={item.path}
              href={item.path}
              className={`flex items-center gap-3 px-6 py-3 transition-colors duration-150 rounded-lg group ${isActive
                ? 'bg-primary/15 text-primary'
                : 'text-on-surface-variant hover:bg-white/5 hover:text-primary'
                }`}
            >
              <span
                className="material-symbols-outlined transition-colors duration-150"
                style={isActive ? { fontVariationSettings: "'FILL' 1" } : {}}
              >
                {item.icon}
              </span>
              <span className="text-xs font-semibold uppercase tracking-wider">{item.name}</span>
            </Link>
          );
        })}
      </nav>
      <div className="p-4 border-t border-white/5">
        <div className="flex items-center gap-3 px-4 py-3 glass-card rounded-xl">
          <img
            alt="Admin Avatar"
            className="w-10 h-10 rounded-full object-cover border-2 border-primary/30"
            src="https://lh3.googleusercontent.com/aida-public/AB6AXuBoL6tlE6MoLQvaAoWy4_HpFTUlhb8MDGBDaS9EJuBLj-ljP_XVl9I-6JpkD04QzfaLkAHt1JXjVMCZA6cPjZLgpyJFck595sNJ_hOBjq1-sCamghQRnTHwzHywRo9u0t4zhrkgCKj6DjxmukwPhA2f0F2aMPAQG6bwbzkA42uob0u72jZqyIQyEwkClh35VMwfnBGWVw3HTpQdxS9uM6u1d1ydXCmHzaiCyUvs8TATXVZG3ngvKjuVf7vfi6Q-Mq9jOxbQPAhpGPw"
          />
          <div>
            <p className="text-sm font-semibold text-white">Alex Tran</p>
            <p className="text-[10px] text-on-surface-variant">Quản lý cửa hàng</p>
          </div>
        </div>
      </div>
    </aside>
  );
}

