'use client';

import { useState } from 'react';

interface Employee {
  email: string;
  name: string;
  role: string;
  lastActive: string;
  status: 'Hoạt động' | 'Ngừng';
}

const INITIAL_EMPLOYEES: Employee[] = [
  { name: 'Alex Murphy', email: 'alex@cafe-di-rom.vn', role: 'Quản trị viên (Admin)', lastActive: 'Đang hoạt động', status: 'Hoạt động' },
  { name: 'Sarah Jenkins', email: 'sarah@cafe-di-rom.vn', role: 'Quản lý cửa hàng', lastActive: '2 giờ trước', status: 'Hoạt động' },
  { name: 'Tom Lee', email: 'tom@cafe-di-rom.vn', role: 'Nhân viên phục vụ', lastActive: 'Hôm qua', status: 'Hoạt động' },
];

export default function SettingsPage() {
  // Store form states
  const [storeName, setStoreName] = useState('CAFE DI ROM');
  const [supportEmail, setSupportEmail] = useState('support@cafe-di-rom.vn');
  const [hotline, setHotline] = useState('+84 923 456 789');
  const [currency, setCurrency] = useState('VND (₫)');
  const [address, setAddress] = useState('123 Đường Cà Phê, Quận 1, Thành phố Hồ Chí Minh, Việt Nam');

  // Design system settings states
  const [selectedAccent, setSelectedAccent] = useState('#03DFC3');
  const [selectedTheme, setSelectedTheme] = useState('Giao diện tối');

  // Employee data list state
  const [employees, setEmployees] = useState<Employee[]>(INITIAL_EMPLOYEES);

  const handleAddEmployee = () => {
    const randomNames = ['Nguyễn Văn Nam', 'Trần Thị Lan', 'Phạm Minh Đức', 'Lê Hoàng Yến'];
    const selectedName = randomNames[Math.floor(Math.random() * randomNames.length)];
    const username = selectedName.toLowerCase().replace(/ /g, '');
    const newEmp: Employee = {
      name: selectedName,
      email: `${username}@cafe-di-rom.vn`,
      role: 'Nhân viên phục vụ',
      lastActive: 'Vừa xong',
      status: 'Hoạt động',
    };
    setEmployees((prev) => [...prev, newEmp]);
  };

  const handleDeleteEmployee = (email: string) => {
    setEmployees((prev) => prev.filter((e) => e.email !== email));
  };

  const handleSave = () => {
    alert('Cấu hình hệ thống đã được lưu thành công!');
  };

  const handleBackupAction = (action: string) => {
    alert(`Thao tác dữ liệu: "${action}" đã được kích hoạt thành công.`);
  };

  return (
    <div className="h-[calc(100vh-16px)] overflow-hidden flex flex-col pt-2 pb-2 px-4 space-y-3 w-full relative">
      {/* Top Header Buttons */}
      <div className="flex justify-between items-center flex-shrink-0">
        <div>
          <h2 className="text-xl font-bold text-white">Cấu hình Hệ thống</h2>
          <p className="text-[10px] text-on-surface-variant mt-0.5">Quản lý nhận diện thương hiệu, quyền truy cập của nhân viên và môi trường cơ sở dữ liệu.</p>
        </div>
        <div className="flex gap-2">
          <button 
            type="button"
            onClick={() => {
              setStoreName('CAFE DI ROM');
              setSupportEmail('support@cafe-di-rom.vn');
              setHotline('+84 923 456 789');
              setCurrency('VND (₫)');
              setAddress('123 Đường Cà Phê, Quận 1, Thành phố Hồ Chí Minh, Việt Nam');
            }}
            className="px-4 py-2 rounded-lg border border-border-glass text-on-surface hover:bg-surface-highest/50 transition-colors font-semibold text-xs cursor-pointer outline-none active:scale-95"
          >
            Hủy thay đổi
          </button>
          <button 
            type="button"
            onClick={handleSave}
            className="px-5 py-2 rounded-lg bg-primary text-on-primary font-bold glow-teal transition-transform active:scale-95 text-xs cursor-pointer outline-none"
          >
            Lưu Cấu hình
          </button>
        </div>
      </div>

      {/* Grid panels layout */}
      <div className="flex-1 overflow-y-auto pr-1 min-h-0 space-y-3">
        <div className="grid grid-cols-12 gap-4 pb-2">
          {/* Card 1: Store Information */}
          <section className="col-span-12 lg:col-span-7 glass-card rounded-xl p-3.5 flex flex-col gap-3">
            <div className="flex items-center gap-2 border-b border-border-glass pb-2">
              <span className="material-symbols-outlined text-primary text-lg">store</span>
              <h3 className="text-xs font-bold text-on-surface">Thông tin Cửa hàng / Quán</h3>
            </div>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              <div className="space-y-1">
                <label className="text-[10px] font-semibold text-on-surface-variant ml-1">Tên cửa hàng</label>
                <input
                  value={storeName}
                  onChange={(e) => setStoreName(e.target.value)}
                  className="w-full bg-[#121824] border border-white/10 rounded-lg px-3 py-2 text-xs focus:border-primary focus:ring-1 focus:ring-primary/50 outline-none text-white transition-all"
                  type="text"
                />
              </div>
              <div className="space-y-1">
                <label className="text-[10px] font-semibold text-on-surface-variant ml-1">Email hỗ trợ</label>
                <input
                  value={supportEmail}
                  onChange={(e) => setSupportEmail(e.target.value)}
                  className="w-full bg-[#121824] border border-white/10 rounded-lg px-3 py-2 text-xs focus:border-primary focus:ring-1 focus:ring-primary/50 outline-none text-white transition-all"
                  type="email"
                />
              </div>
              <div className="space-y-1">
                <label className="text-[10px] font-semibold text-on-surface-variant ml-1">Đường dây nóng (Hotline)</label>
                <input
                  value={hotline}
                  onChange={(e) => setHotline(e.target.value)}
                  className="w-full bg-[#121824] border border-white/10 rounded-lg px-3 py-2 text-xs focus:border-primary focus:ring-1 focus:ring-primary/50 outline-none text-white transition-all"
                  type="text"
                />
              </div>
              <div className="space-y-1">
                <label className="text-[10px] font-semibold text-on-surface-variant ml-1">Tiền tệ mặc định</label>
                <select
                  value={currency}
                  onChange={(e) => setCurrency(e.target.value)}
                  className="w-full bg-[#121824] border border-white/10 rounded-lg px-3 py-2 text-xs focus:border-primary focus:ring-1 focus:ring-primary/50 outline-none text-white transition-all appearance-none cursor-pointer font-semibold"
                >
                  <option value="VND (₫)">VND (₫)</option>
                  <option value="USD ($)">USD ($)</option>
                  <option value="EUR (€)">EUR (€)</option>
                </select>
              </div>
              <div className="col-span-1 md:col-span-2 space-y-1">
                <label className="text-[10px] font-semibold text-on-surface-variant ml-1">Địa chỉ hoạt động</label>
                <textarea
                  value={address}
                  onChange={(e) => setAddress(e.target.value)}
                  className="w-full bg-[#121824] border border-white/10 rounded-lg px-3 py-2 text-xs focus:border-primary focus:ring-1 focus:ring-primary/50 outline-none text-white transition-all resize-none"
                  rows={2}
                ></textarea>
              </div>
            </div>
            <div className="mt-1">
              <button
                type="button"
                onClick={() => handleBackupAction('Cập nhật thông tin quán')}
                className="px-4 py-2 rounded-lg bg-primary text-on-primary font-bold glow-teal w-fit flex items-center gap-1.5 transition-transform active:scale-95 cursor-pointer text-xs"
              >
                <span className="material-symbols-outlined text-sm">save</span>
                Cập nhật thông tin
              </button>
            </div>
          </section>

          {/* Card 2: Interface & Accents */}
          <section className="col-span-12 lg:col-span-5 glass-card rounded-xl p-3.5 flex flex-col gap-3">
            <div className="flex items-center gap-2 border-b border-border-glass pb-2">
              <span className="material-symbols-outlined text-primary text-lg">palette</span>
              <h3 className="text-xs font-bold text-on-surface">Giao diện &amp; Màu sắc</h3>
            </div>
            <div className="space-y-4">
              <div className="space-y-2">
                <p className="text-[10px] font-semibold text-on-surface-variant">Tông màu chủ đạo thương hiệu</p>
                <div className="flex flex-wrap gap-2.5">
                  {['#03DFC3', '#47d6ff', '#ffba4e', '#EF4444'].map((color) => {
                    const isSel = selectedAccent === color;
                    return (
                      <button
                        key={color}
                        type="button"
                        onClick={() => setSelectedAccent(color)}
                        className="w-8 h-8 rounded-full cursor-pointer hover:scale-105 transition-all outline-none"
                        style={{
                          backgroundColor: color,
                          boxShadow: isSel ? `0 0 0 2px #0A0E17, 0 0 0 3px ${color}` : 'none',
                        }}
                      ></button>
                    );
                  })}
                </div>
              </div>
              <div className="space-y-2">
                <p className="text-[10px] font-semibold text-on-surface-variant">Chế độ giao diện</p>
                <div className="grid grid-cols-2 gap-3">
                  <button
                    type="button"
                    onClick={() => setSelectedTheme('Giao diện tối')}
                    className={`flex items-center justify-center gap-2 p-2.5 rounded-lg border transition-all cursor-pointer ${
                      selectedTheme === 'Giao diện tối'
                        ? 'bg-surface-highest/40 border-primary text-primary font-bold shadow-[0_0_10px_rgba(73,252,223,0.2)]'
                        : 'bg-black/20 border-white/5 text-on-surface-variant hover:bg-surface-highest/20'
                    }`}
                  >
                    <span className="material-symbols-outlined text-sm">dark_mode</span>
                    <span className="text-[11px] font-semibold">Giao diện tối</span>
                  </button>
                  <button
                    type="button"
                    onClick={() => setSelectedTheme('Giao diện sáng')}
                    className={`flex items-center justify-center gap-2 p-2.5 rounded-lg border transition-all cursor-pointer ${
                      selectedTheme === 'Giao diện sáng'
                        ? 'bg-surface-highest/40 border-primary text-primary font-bold shadow-[0_0_10px_rgba(73,252,223,0.2)]'
                        : 'bg-black/20 border-white/5 text-on-surface-variant hover:bg-surface-highest/20'
                    }`}
                  >
                    <span className="material-symbols-outlined text-sm">light_mode</span>
                    <span className="text-[11px] font-semibold">Giao diện sáng</span>
                  </button>
                </div>
              </div>
            </div>
          </section>

          {/* Card 3: Employee Accounts */}
          <section className="col-span-12 glass-card rounded-xl p-3.5">
            <div className="flex justify-between items-center border-b border-border-glass pb-2 mb-3">
              <div className="flex items-center gap-2">
                <span className="material-symbols-outlined text-primary text-lg">badge</span>
                <h3 className="text-xs font-bold text-on-surface">Tài khoản &amp; Quyền truy cập</h3>
              </div>
              <button
                type="button"
                onClick={handleAddEmployee}
                className="px-3 py-1.5 rounded-lg bg-surface-highest/40 hover:bg-surface-highest border border-border-glass text-primary transition-all flex items-center gap-1.5 text-xs font-semibold cursor-pointer"
              >
                <span className="material-symbols-outlined text-sm">person_add</span>
                Thêm nhân viên
              </button>
            </div>
            <div className="overflow-x-auto">
              <table className="w-full text-left">
                <thead>
                  <tr className="border-b border-border-glass text-on-surface-variant text-[10px] uppercase tracking-wider">
                    <th className="px-3 py-2">Nhân viên</th>
                    <th className="px-3 py-2">Chức vụ</th>
                    <th className="px-3 py-2">Hoạt động</th>
                    <th className="px-3 py-2">Trạng thái</th>
                    <th className="px-3 py-2 text-right">Thao tác</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-white/5">
                  {employees.map((emp) => (
                    <tr key={emp.email} className="hover:bg-white/5 transition-colors group">
                      <td className="px-3 py-2.5">
                        <div className="flex items-center gap-2.5">
                          <div className="w-7 h-7 rounded-full bg-primary/10 flex items-center justify-center text-primary font-bold text-xs uppercase">
                            {emp.name.split(' ').map((n) => n[0]).join('')}
                          </div>
                          <div>
                            <p className="text-xs font-semibold text-white">{emp.name}</p>
                            <p className="text-[10px] text-on-surface-variant">{emp.email}</p>
                          </div>
                        </div>
                      </td>
                      <td className="px-3 py-2.5 text-xs text-secondary">{emp.role}</td>
                      <td className="px-3 py-2.5 text-on-surface-variant text-xs">{emp.lastActive}</td>
                      <td className="px-3 py-2.5">
                        <span className="px-2 py-0.5 rounded-full text-[9px] font-bold uppercase tracking-tight bg-success/10 border border-success text-success">
                          {emp.status}
                        </span>
                      </td>
                      <td className="px-3 py-2.5 text-right">
                        <button
                          onClick={() => handleDeleteEmployee(emp.email)}
                          className="text-on-surface-variant hover:text-error transition-colors cursor-pointer"
                        >
                          <span className="material-symbols-outlined text-base">delete</span>
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>

          {/* Card 4: Database & Backup Management */}
          <section className="col-span-12 glass-card rounded-xl p-3.5 border-t-2 border-t-error/30">
            <div className="flex items-center gap-2 border-b border-border-glass pb-2 mb-3">
              <span className="material-symbols-outlined text-error text-lg">database</span>
              <h3 className="text-xs font-bold text-on-surface">Cơ sở dữ liệu &amp; Sao lưu</h3>
            </div>
            <div className="flex flex-col md:flex-row gap-4 items-start">
              <div className="flex-1 space-y-2">
                <p className="text-xs text-on-surface-variant leading-relaxed">
                  Duy trì tính toàn vẹn dữ liệu cho quán. Chúng tôi khuyến nghị thực hiện sao lưu cơ sở dữ liệu hàng ngày để phòng ngừa mất mát dữ liệu. 
                  Các file sao lưu được SQL nén chứa toàn bộ nhật ký giao dịch, kho hàng và thông tin khách hàng.
                </p>
                <div className="p-2 bg-error/5 border border-error/20 rounded-lg flex gap-2">
                  <span className="material-symbols-outlined text-error text-sm">warning</span>
                  <p className="text-[10px] text-error/95 uppercase tracking-wide">
                    <strong>Vùng nguy hiểm:</strong> Thao tác xóa trắng bảng dữ liệu là vĩnh viễn và không thể hoàn tác. Luôn xuất file sao lưu trước khi thực hiện.
                  </p>
                </div>
              </div>
              <div className="w-full md:w-fit flex flex-col gap-2">
                <button
                  type="button"
                  onClick={() => handleBackupAction('Xuất sao lưu ZIP')}
                  className="flex items-center justify-between gap-4 px-4 py-2 rounded-lg bg-surface-highest/20 hover:bg-surface-highest/45 border border-border-glass transition-all w-full md:w-56 group cursor-pointer text-xs font-semibold"
                >
                  <div className="flex items-center gap-2">
                    <span className="material-symbols-outlined text-secondary text-base group-hover:scale-110 transition-transform">folder_zip</span>
                    <span>Xuất file ZIP</span>
                  </div>
                  <span className="material-symbols-outlined text-xs">download</span>
                </button>
                <button
                  type="button"
                  onClick={() => handleBackupAction('Nhập sao lưu SQL')}
                  className="flex items-center justify-between gap-4 px-4 py-2 rounded-lg bg-surface-highest/20 hover:bg-surface-highest/45 border border-border-glass transition-all w-full md:w-56 group cursor-pointer text-xs font-semibold"
                >
                  <div className="flex items-center gap-2">
                    <span className="material-symbols-outlined text-secondary text-base group-hover:scale-110 transition-transform">upload_file</span>
                    <span>Nhập file SQL</span>
                  </div>
                  <span className="material-symbols-outlined text-xs">upload</span>
                </button>
                <button
                  type="button"
                  onClick={() => handleBackupAction('Xóa trắng các bảng dữ liệu')}
                  className="flex items-center justify-center gap-2 px-4 py-2 rounded-lg bg-error/10 hover:bg-error text-error hover:text-on-error border border-error/30 transition-all w-full md:w-56 font-bold active:scale-95 cursor-pointer text-xs"
                >
                  <span className="material-symbols-outlined text-base">delete_forever</span>
                  Xóa sạch dữ liệu
                </button>
              </div>
            </div>
          </section>
        </div>
      </div>
    </div>
  );
}
