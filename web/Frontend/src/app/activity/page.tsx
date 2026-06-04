'use client';

import { useState, useEffect } from 'react';

interface ActivityLog {
  id: number;
  thoiGian: string;
  thaoTac: string; // THEM | SUA | XOA
  tab: string; // ban_hang | nhap_hang | san_pham | doi_tac
  maBanGhi: string;
  moTa: string;
}

export default function ActivityPage() {
  const [actionFilter, setActionFilter] = useState<'Tất cả' | 'THEM' | 'SUA' | 'XOA'>('Tất cả');
  const [moduleFilter, setModuleFilter] = useState<string>('Tất cả phân hệ');
  const [searchQuery, setSearchQuery] = useState('');

  const [logs, setLogs] = useState<ActivityLog[]>([]);
  const [loading, setLoading] = useState(true);

  const loadData = async () => {
    try {
      setLoading(true);
      const res = await fetch("http://localhost:8080/api/v1/metadata/nhat-ky");
      if (res.ok) setLogs(await res.json());
    } catch (err) {
      console.error("Error loading activity logs:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const getActionBadge = (action: string) => {
    switch (action) {
      case 'THEM':
        return 'bg-success/15 border-success/30 text-success';
      case 'SUA':
        return 'bg-warning/15 border-warning/30 text-warning';
      case 'XOA':
        return 'bg-error/15 border-error/30 text-error';
      default:
        return 'bg-primary/15 border-primary/30 text-primary';
    }
  };

  const getModuleLabel = (tab: string) => {
    switch (tab) {
      case 'ban_hang': return 'Bán Hàng';
      case 'nhap_hang': return 'Nhập Hàng';
      case 'san_pham': return 'Thực Đơn (Sản phẩm)';
      case 'doi_tac': return 'Đối Tác';
      case 'thu_chi': return 'Thu Chi';
      default: return tab;
    }
  };

  const getActionLabel = (action: string) => {
    switch (action) {
      case 'THEM': return 'THÊM MỚI';
      case 'SUA': return 'CẬP NHẬT';
      case 'XOA': return 'XÓA BỎ';
      default: return action;
    }
  };

  // Filters
  const filteredLogs = logs.filter((log) => {
    // Action filter
    if (actionFilter !== 'Tất cả' && log.thaoTac !== actionFilter) return false;

    // Module
    if (moduleFilter !== 'Tất cả phân hệ' && log.tab !== moduleFilter) return false;

    // Search
    const query = searchQuery.toLowerCase();
    const matchesSearch =
      `lg-${log.id}`.includes(query) ||
      log.moTa.toLowerCase().includes(query) ||
      log.thaoTac.toLowerCase().includes(query) ||
      (log.maBanGhi && log.maBanGhi.toLowerCase().includes(query));
    
    return matchesSearch;
  });

  return (
    <div className="p-8 space-y-6 max-w-[1600px] mx-auto w-full relative">
      {/* Top Header Section */}
      <div className="flex justify-between items-center h-16">
        <div className="flex items-center gap-6 flex-1">
          <div className="relative w-full max-w-md group">
            <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-on-surface-variant group-focus-within:text-primary transition-colors">
              search
            </span>
            <input
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full bg-surface-lowest border border-border-glass rounded-lg py-2 pl-10 pr-4 text-on-surface text-sm focus:outline-none focus:border-primary/50 focus:ring-1 focus:ring-primary/20 transition-all outline-none"
              placeholder="Tìm kiếm nhật ký theo nội dung, mã bản ghi..."
              type="text"
            />
          </div>
        </div>
        <div className="text-xs text-text-variant font-bold">
          Nhật ký hệ thống thời gian thực
        </div>
      </div>

      {loading ? (
        <div className="text-center py-20 text-white text-sm">Đang tải nhật ký hoạt động...</div>
      ) : (
        <>
          {/* Stats Row */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <div className="glass-card rounded-xl p-6 flex items-center gap-4">
              <div className="w-12 h-12 bg-white/[0.03] rounded-lg flex items-center justify-center text-primary border border-primary/20">
                <span className="material-symbols-outlined">history</span>
              </div>
              <div>
                <p className="text-on-surface-variant text-xs uppercase tracking-wider">Tổng số lượt lưu vết</p>
                <p className="text-xl font-bold text-primary">
                  {logs.length} <span className="text-xs font-medium opacity-60 text-white">Nhật ký</span>
                </p>
              </div>
            </div>

            <div className="glass-card rounded-xl p-6 flex items-center gap-4">
              <div className="w-12 h-12 bg-success/10 rounded-lg flex items-center justify-center text-success border border-success/30 glow-success">
                <span className="material-symbols-outlined">security</span>
              </div>
              <div>
                <p className="text-on-surface-variant text-xs uppercase tracking-wider">Trạng thái bảo mật</p>
                <p className="text-xl font-bold text-success">AN TOÀN</p>
              </div>
            </div>
          </div>

          {/* Logs Viewer Section */}
          <div className="glass-card rounded-2xl overflow-hidden shadow-2xl">
            {/* Table Toolbar */}
            <div className="p-6 border-b border-border-glass bg-white/[0.02] flex flex-col md:flex-row md:items-center justify-between gap-4">
              <div className="flex flex-wrap items-center gap-6">
                <div className="flex items-center gap-2">
                  <span className="text-on-surface-variant text-xs uppercase">Thao tác:</span>
                  <div className="flex bg-surface-lowest border border-border-glass rounded-lg p-1">
                    {(['Tất cả', 'THEM', 'SUA', 'XOA'] as const).map((sev) => (
                      <button
                        key={sev}
                        onClick={() => setActionFilter(sev)}
                        className={`px-3 py-1 rounded-md text-xs transition-all cursor-pointer ${
                          actionFilter === sev
                            ? 'bg-primary text-on-primary font-bold shadow-[0_0_10px_rgba(73,252,223,0.3)]'
                            : 'text-on-surface-variant hover:text-primary'
                        }`}
                      >
                        {getActionLabel(sev)}
                      </button>
                    ))}
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-on-surface-variant text-xs uppercase">Phân hệ:</span>
                  <select
                    value={moduleFilter}
                    onChange={(e) => setModuleFilter(e.target.value)}
                    className="bg-surface-lowest border border-border-glass rounded-lg py-1 px-3 text-xs text-white focus:outline-none focus:border-primary/50 cursor-pointer outline-none"
                  >
                    <option value="Tất cả phân hệ">Tất cả phân hệ</option>
                    <option value="ban_hang">Bán Hàng</option>
                    <option value="nhap_hang">Nhập Hàng</option>
                    <option value="san_pham">Thực Đơn (Sản phẩm)</option>
                    <option value="doi_tac">Đối Tác</option>
                    <option value="thu_chi">Thu Chi</option>
                  </select>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <span className="text-xs text-on-surface-variant">Hiển thị {filteredLogs.length} nhật ký</span>
              </div>
            </div>

            {/* Data Table */}
            <div className="overflow-x-auto">
              <table className="w-full text-left border-collapse">
                <thead>
                  <tr className="border-b border-border-glass bg-white/[0.01] text-xs text-on-surface-variant uppercase tracking-wider">
                    <th className="px-6 py-4">Thời gian xảy ra</th>
                    <th className="px-6 py-4">Mã nhật ký</th>
                    <th className="px-6 py-4">Thao tác</th>
                    <th className="px-6 py-4">Phân hệ ảnh hưởng</th>
                    <th className="px-6 py-4">Mã bản ghi</th>
                    <th className="px-6 py-4">Mô tả hành vi chi tiết</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-white/[0.03]">
                  {filteredLogs.map((log) => {
                    return (
                      <tr key={log.id} className="hover:bg-white/[0.03] transition-colors group">
                        <td className="px-6 py-4 text-xs text-on-surface-variant group-hover:text-on-surface">
                          {new Date(log.thoiGian).toLocaleString('vi-VN')}
                        </td>
                        <td className="px-6 py-4 text-xs text-on-surface opacity-80 font-mono">LG-{log.id}</td>
                        <td className="px-6 py-4">
                          <span
                            className={`px-3 py-1 rounded-full text-[9px] font-bold uppercase border ${getActionBadge(log.thaoTac)}`}
                          >
                            {getActionLabel(log.thaoTac)}
                          </span>
                        </td>
                        <td className="px-6 py-4">
                          <span className="bg-surface-low px-2.5 py-1 rounded text-xs border border-border-glass text-white font-medium">
                            {getModuleLabel(log.tab)}
                          </span>
                        </td>
                        <td className="px-6 py-4 text-xs text-primary font-mono font-bold">{log.maBanGhi || '---'}</td>
                        <td className="px-6 py-4 text-xs text-white font-medium">{log.moTa}</td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
