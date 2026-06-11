'use client';

import { useState, useEffect } from 'react';

import { ActivityLog } from '@/types';
import { activityService } from '@/services/activityService';

export default function ActivityPage() {
  const [actionFilter, setActionFilter] = useState<'Tất cả' | 'THEM' | 'SUA' | 'XOA'>('Tất cả');
  const [moduleFilter, setModuleFilter] = useState<string>('Tất cả phân hệ');
  const [searchQuery, setSearchQuery] = useState('');

  const [logs, setLogs] = useState<ActivityLog[]>([]);
  const [loading, setLoading] = useState(true);

  const loadData = async () => {
    try {
      setLoading(true);
      const data = await activityService.getActivityLogs();
      setLogs(data);
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

  const renderTableBody = () => {
    if (loading) {
      return (
        <tr>
          <td colSpan={6} className="px-6 py-12 text-center text-on-surface-variant text-xs font-semibold animate-pulse">
            Đang tải nhật ký hoạt động...
          </td>
        </tr>
      );
    }

    if (filteredLogs.length === 0) {
      return (
        <tr>
          <td colSpan={6} className="px-6 py-12 text-center text-on-surface-variant text-xs font-semibold">
            Không tìm thấy nhật ký nào khớp với bộ lọc.
          </td>
        </tr>
      );
    }

    return filteredLogs.map((log) => (
      <tr key={log.id} className="hover:bg-white/5 transition-all group">
        <td className="px-4 py-2.5 text-xs text-on-surface-variant group-hover:text-on-surface">
          {new Date(log.thoiGian).toLocaleString('vi-VN')}
        </td>
        <td className="px-4 py-2.5 text-xs text-on-surface opacity-80 font-mono">LG-{log.id}</td>
        <td className="px-4 py-2.5">
          <span
            className={`px-2 py-0.5 rounded-full text-[9px] font-bold uppercase border ${getActionBadge(log.thaoTac)}`}
          >
            {getActionLabel(log.thaoTac)}
          </span>
        </td>
        <td className="px-4 py-2.5">
          <span className="bg-surface-low px-1.5 py-0.5 rounded text-[10px] border border-border-glass text-white font-medium">
            {getModuleLabel(log.tab)}
          </span>
        </td>
        <td className="px-4 py-2.5 text-xs text-primary font-mono font-bold">{log.maBanGhi || '---'}</td>
        <td className="px-4 py-2.5 text-xs text-white font-medium max-w-md truncate" title={log.moTa}>{log.moTa}</td>
      </tr>
    ));
  };

  return (
    <div className="h-[calc(100vh-16px)] overflow-hidden flex flex-col pt-2 pb-2 px-4 space-y-3 w-full relative">
      {/* Top Header Controls */}
      <div className="flex justify-between items-center flex-shrink-0">
        <div>
          <h2 className="text-xl font-bold text-white tracking-wide">Nhật ký hoạt động</h2>
          <p className="text-[10px] text-on-surface-variant mt-0.5">Theo dõi lịch sử chỉnh sửa, cập nhật của hệ thống.</p>
        </div>
        <div className="relative w-60">
          <span className="material-symbols-outlined absolute left-2.5 top-1/2 -translate-y-1/2 text-xs text-on-surface-variant opacity-60">
            search
          </span>
          <input
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full bg-surface-lowest border border-border-glass rounded-lg py-1.5 pl-8 pr-3 text-xs focus:ring-1 focus:ring-primary/50 transition-all text-white outline-none"
            placeholder="Tìm kiếm nhật ký..."
            type="text"
          />
        </div>
      </div>

      {/* Stats Row */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 flex-shrink-0">
        <div className="glass-card py-2.5 px-4 rounded-xl flex items-center justify-between hover:border-primary/30 transition-all">
          <div className="flex items-center gap-3">
            <span className="material-symbols-outlined text-primary bg-primary/10 p-2 rounded-lg text-lg">history</span>
            <div>
              <p className="text-on-surface-variant text-[10px] uppercase tracking-wider mb-0.5">Tổng số lượt lưu vết</p>
              <h3 className="text-sm font-bold text-on-surface">{logs.length} Nhật ký</h3>
            </div>
          </div>
        </div>

        <div className="glass-card py-2.5 px-4 rounded-xl flex items-center justify-between hover:border-primary/30 transition-all">
          <div className="flex items-center gap-3">
            <span className="material-symbols-outlined text-success bg-success/10 p-2 rounded-lg text-lg">security</span>
            <div>
              <p className="text-on-surface-variant text-[10px] uppercase tracking-wider mb-0.5">Trạng thái bảo mật</p>
              <h3 className="text-sm font-bold text-success">AN TOÀN</h3>
            </div>
          </div>
        </div>
      </div>

      {/* Logs Viewer Section */}
      <div className="flex-1 w-full flex flex-col h-full overflow-hidden glass-card rounded-xl">
        {/* Table Toolbar */}
        <div className="flex justify-between items-center p-3 border-b border-border-glass flex-wrap gap-3 flex-shrink-0 bg-white/1">
          <div className="flex flex-wrap items-center gap-4">
            <div className="flex items-center gap-2">
              <span className="text-on-surface-variant text-[10px] uppercase font-bold">Thao tác:</span>
              <div className="flex bg-surface-lowest border border-border-glass rounded-lg p-0.5">
                {(['Tất cả', 'THEM', 'SUA', 'XOA'] as const).map((sev) => (
                  <button
                    key={sev}
                    onClick={() => setActionFilter(sev)}
                    className={`px-2.5 py-0.5 rounded text-[10px] transition-all cursor-pointer ${
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
              <span className="text-on-surface-variant text-[10px] uppercase font-bold">Phân hệ:</span>
              <select
                value={moduleFilter}
                onChange={(e) => setModuleFilter(e.target.value)}
                className="bg-surface-lowest border border-border-glass rounded-lg py-1 px-2 text-[10px] text-white focus:outline-none focus:border-primary/50 cursor-pointer outline-none font-semibold"
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
          <div className="text-xs text-on-surface-variant font-semibold">
            Hiển thị <span className="text-white font-bold">{filteredLogs.length}</span> nhật ký
          </div>
        </div>

        {/* Data Table */}
        <div className="flex-1 overflow-auto">
          <table className="w-full text-left border-collapse relative">
            <thead className="sticky top-0 z-20 shadow-[0_1px_0_0_rgba(255,255,255,0.08)] bg-[#131929]">
              <tr className="text-[10px] font-bold text-on-surface-variant uppercase tracking-widest">
                <th className="px-4 py-2.5 bg-[#131929] backdrop-blur-md">Thời gian</th>
                <th className="px-4 py-2.5 bg-[#131929] backdrop-blur-md">Mã</th>
                <th className="px-4 py-2.5 bg-[#131929] backdrop-blur-md">Thao tác</th>
                <th className="px-4 py-2.5 bg-[#131929] backdrop-blur-md">Phân hệ</th>
                <th className="px-4 py-2.5 bg-[#131929] backdrop-blur-md">Mã bản ghi</th>
                <th className="px-4 py-2.5 bg-[#131929] backdrop-blur-md">Mô tả hành vi chi tiết</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border-glass">
              {renderTableBody()}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
