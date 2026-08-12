'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useAuth } from '@/components/providers/AuthContext';

function getRoleDisplayName(roles: string[] | undefined): string {
  if (!roles || roles.length === 0) return 'Nhân viên';
  if (roles.includes('ROLE_ADMIN')) return 'Quản trị viên';
  if (roles.includes('ROLE_MANAGER')) return 'Quản lý cửa hàng';
  if (roles.includes('ROLE_CASHIER')) return 'Thu ngân';
  if (roles.includes('ROLE_BARISTA')) return 'Pha chế';
  if (roles.includes('ROLE_WAITER')) return 'Phục vụ';
  return 'Nhân viên';
}

export default function Sidebar() {
  const pathname = usePathname();
  const { user, logout } = useAuth();

  const menuItems = [
    { name: 'Tổng quan', path: '/', icon: 'dashboard', permission: 'DASHBOARD_VIEW' },
    { name: 'Quản lý hàng hóa', path: '/inventory', icon: 'inventory_2', permission: 'INVENTORY_VIEW' },
    { name: 'Bán hàng', path: '/sales', icon: 'shopping_cart', permission: 'SALES_VIEW' },
    { name: 'Nhập hàng', path: '/purchases', icon: 'local_shipping', permission: 'PURCHASE_VIEW' },
    { name: 'Đối tác và nhân viên', path: '/partners', icon: 'handshake', permission: 'PARTNERS_VIEW' },
    { name: 'Nhật ký thu chi', path: '/expenses', icon: 'payments', permission: 'EXPENSE_VIEW' },
    { name: 'Nhật ký hoạt động', path: '/activity', icon: 'history', permission: 'ACTIVITY_LOGS_VIEW' },
    { name: 'Cấu hình Excel', path: '/excel-config', icon: 'table_chart', permission: 'EXCEL_CONFIG' },
    { name: 'Quản lý tài khoản', path: '/accounts', icon: 'manage_accounts', permission: 'ACCOUNTS_MANAGE' },
  ];

  if (pathname === '/login') {
    return null;
  }

  const filteredMenuItems = menuItems.filter(item => {
    if (!user || !user.roles) return false;
    return user.roles.includes(item.permission);
  });

  return (
    <aside className="flex flex-col w-[210px] h-screen fixed left-0 top-0 z-40 bg-surface/80 backdrop-blur-xl border-r border-white/10 shadow-2xl">
      <div className="py-6 px-4">
        <h1 className="text-2xl font-bold text-primary drop-shadow-[0_0_10px_rgba(73,252,223,0.4)]">JenkaM</h1>
        <p className="text-[9px] tracking-widest text-on-surface-variant/60 uppercase mt-1">Cafe &amp; F&amp;B Management</p>
      </div>
      <nav className="flex-1 overflow-y-auto mt-4 px-3 space-y-1.5">
        {filteredMenuItems.map((item) => {
          const isActive = pathname === item.path;
          return (
            <Link
              key={item.path}
              href={item.path}
              className={`flex items-center gap-2.5 px-4 py-2.5 transition-colors duration-150 rounded-lg group ${isActive
                ? 'bg-primary/15 text-primary'
                : 'text-on-surface-variant hover:bg-white/5 hover:text-primary'
                }`}
            >
              <span
                className="material-symbols-outlined transition-colors duration-150 text-xl"
                style={isActive ? { fontVariationSettings: "'FILL' 1" } : {}}
              >
                {item.icon}
              </span>
              <span className="text-[11px] font-semibold uppercase tracking-wider">{item.name}</span>
            </Link>
          );
        })}
      </nav>
      <div className="p-3 border-t border-white/5">
        <div className="flex items-center justify-between px-3 py-2.5 glass-card rounded-xl">
          <div className="flex items-center gap-2.5 min-w-0">
            <img
              alt="User Avatar"
              className="w-8 h-8 rounded-full object-cover border-2 border-primary/30 flex-shrink-0"
              src="/user.png"
            />
            <div className="min-w-0">
              <p className="text-xs font-semibold text-white truncate">{user?.tenNhanVien || user?.username || 'Guest'}</p>
              <p className="text-[9px] text-on-surface-variant truncate">{getRoleDisplayName(user?.roles)}</p>
            </div>
          </div>
          <button
            onClick={logout}
            className="p-1 hover:bg-error/10 hover:text-error rounded text-on-surface-variant transition-colors cursor-pointer flex items-center justify-center flex-shrink-0"
            title="Đăng xuất"
          >
            <span className="material-symbols-outlined text-lg">logout</span>
          </button>
        </div>
      </div>
    </aside>
  );
}
