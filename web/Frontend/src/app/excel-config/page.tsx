'use client';

import { useState } from 'react';

interface ExcelColumn {
  key: string;
  label: string;
  defaultLabel: string;
  enabled: boolean;
}

interface ExcelTemplate {
  id: string;
  name: string;
  description: string;
  icon: string;
  columns: ExcelColumn[];
  headerTitle: string;
  showSignature: boolean;
  useFormulas: boolean;
  themeColor: string;
}

const INITIAL_TEMPLATES: ExcelTemplate[] = [
  {
    id: 'sales',
    name: 'Hóa đơn bán hàng',
    description: 'Xuất dữ liệu hóa đơn bán hàng cho khách hàng hoặc lưu trữ.',
    icon: 'shopping_cart',
    headerTitle: 'HÓA ĐƠN BÁN HÀNG - CAFE DI ROM',
    showSignature: true,
    useFormulas: true,
    themeColor: '#03DFC3',
    columns: [
      { key: 'stt', label: 'STT', defaultLabel: 'STT', enabled: true },
      { key: 'id', label: 'Mã Hàng', defaultLabel: 'Mã Hàng', enabled: true },
      { key: 'name', label: 'Tên Sản Phẩm', defaultLabel: 'Tên Sản Phẩm', enabled: true },
      { key: 'qty', label: 'Số Lượng', defaultLabel: 'Số Lượng', enabled: true },
      { key: 'price', label: 'Đơn Giá', defaultLabel: 'Đơn Giá', enabled: true },
      { key: 'total', label: 'Thành Tiền', defaultLabel: 'Thành Tiền', enabled: true },
      { key: 'note', label: 'Ghi Chú', defaultLabel: 'Ghi Chú', enabled: true },
    ]
  },
  {
    id: 'inventory',
    name: 'Báo cáo hàng tồn kho',
    description: 'Xuất danh sách sản phẩm, số lượng tồn kho và giá trị nhập hàng.',
    icon: 'inventory_2',
    headerTitle: 'BÁO CÁO HÀNG TỒN KHO',
    showSignature: true,
    useFormulas: false,
    themeColor: '#ffba4e',
    columns: [
      { key: 'stt', label: 'STT', defaultLabel: 'STT', enabled: true },
      { key: 'sku', label: 'Mã SKU', defaultLabel: 'Mã SKU', enabled: true },
      { key: 'name', label: 'Tên Hàng Hóa', defaultLabel: 'Tên Hàng Hóa', enabled: true },
      { key: 'category', label: 'Nhóm Hàng', defaultLabel: 'Nhóm Hàng', enabled: true },
      { key: 'stock', label: 'Tồn Kho', defaultLabel: 'Tồn Kho', enabled: true },
      { key: 'unit', label: 'Đơn Vị Tính', defaultLabel: 'Đơn Vị Tính', enabled: true },
      { key: 'cost', label: 'Giá Vốn', defaultLabel: 'Giá Vốn', enabled: true },
      { key: 'value', label: 'Giá Trị Tồn', defaultLabel: 'Giá Trị Tồn', enabled: true },
    ]
  },
  {
    id: 'expenses',
    name: 'Báo cáo thu chi',
    description: 'Thống kê nhật ký thu chi phát sinh trong tháng.',
    icon: 'payments',
    headerTitle: 'BÁO CÁO NHẬT KÝ THU CHI',
    showSignature: false,
    useFormulas: true,
    themeColor: '#47d6ff',
    columns: [
      { key: 'stt', label: 'STT', defaultLabel: 'STT', enabled: true },
      { key: 'id', label: 'Mã Phiếu', defaultLabel: 'Mã Phiếu', enabled: true },
      { key: 'name', label: 'Nội Dung Chi', defaultLabel: 'Nội Dung Chi', enabled: true },
      { key: 'category', label: 'Phân Loại', defaultLabel: 'Phân Loại', enabled: true },
      { key: 'date', label: 'Ngày Chi', defaultLabel: 'Ngày Chi', enabled: true },
      { key: 'amount', label: 'Số Tiền', defaultLabel: 'Số Tiền', enabled: true },
      { key: 'method', label: 'Phương Thức', defaultLabel: 'Phương Thức', enabled: true },
    ]
  }
];

export default function ExcelConfigPage() {
  const [templates, setTemplates] = useState<ExcelTemplate[]>(INITIAL_TEMPLATES);
  const [selectedTemplateId, setSelectedTemplateId] = useState<string>('sales');
  
  const currentTemplate = templates.find(t => t.id === selectedTemplateId) || templates[0];

  const handleUpdateHeaderTitle = (val: string) => {
    setTemplates(prev => prev.map(t => t.id === selectedTemplateId ? { ...t, headerTitle: val } : t));
  };

  const handleToggleSignature = () => {
    setTemplates(prev => prev.map(t => t.id === selectedTemplateId ? { ...t, showSignature: !t.showSignature } : t));
  };

  const handleToggleFormulas = () => {
    setTemplates(prev => prev.map(t => t.id === selectedTemplateId ? { ...t, useFormulas: !t.useFormulas } : t));
  };

  const handleThemeColorChange = (color: string) => {
    setTemplates(prev => prev.map(t => t.id === selectedTemplateId ? { ...t, themeColor: color } : t));
  };

  const handleToggleColumn = (colKey: string) => {
    setTemplates(prev => prev.map(t => {
      if (t.id !== selectedTemplateId) return t;
      return {
        ...t,
        columns: t.columns.map(c => c.key === colKey ? { ...c, enabled: !c.enabled } : c)
      };
    }));
  };

  const handleColumnLabelChange = (colKey: string, newLabel: string) => {
    setTemplates(prev => prev.map(t => {
      if (t.id !== selectedTemplateId) return t;
      return {
        ...t,
        columns: t.columns.map(c => c.key === colKey ? { ...c, label: newLabel } : c)
      };
    }));
  };

  const handleSaveConfig = () => {
    alert(`Cấu hình mẫu Excel "${currentTemplate.name}" đã được lưu thành công!`);
  };

  const handleResetToDefault = () => {
    const defaultTemplate = INITIAL_TEMPLATES.find(t => t.id === selectedTemplateId);
    if (defaultTemplate) {
      setTemplates(prev => prev.map(t => t.id === selectedTemplateId ? { ...defaultTemplate } : t));
      alert(`Đã đặt lại cấu hình tệp Excel "${defaultTemplate.name}" về mặc định.`);
    }
  };

  return (
    <div className="h-[calc(100vh-16px)] overflow-hidden flex flex-col pt-2 pb-2 px-4 space-y-3 w-full relative">
      {/* Page Header */}
      <div className="flex justify-between items-center flex-shrink-0">
        <div>
          <h2 className="text-xl font-bold text-white flex items-center gap-2">
            <span className="material-symbols-outlined text-primary text-xl">table_chart</span>
            Cấu hình Mẫu Excel
          </h2>
          <p className="text-[10px] text-on-surface-variant mt-0.5">
            Tùy biến tiêu đề báo cáo, định dạng các cột hiển thị và cài đặt bố cục khi xuất dữ liệu ra file Excel (.xlsx).
          </p>
        </div>
        <div className="flex gap-2">
          <button 
            type="button"
            onClick={handleResetToDefault}
            className="px-4 py-2 rounded-lg border border-border-glass text-on-surface hover:bg-surface-highest/50 transition-colors font-semibold text-xs cursor-pointer outline-none active:scale-95"
            id="btn-reset-excel"
          >
            Đặt lại mặc định
          </button>
          <button 
            type="button"
            onClick={handleSaveConfig}
            className="px-5 py-2 rounded-lg bg-primary text-on-primary font-bold glow-teal transition-transform active:scale-95 text-xs cursor-pointer outline-none"
            id="btn-save-excel"
          >
            Lưu cấu hình
          </button>
        </div>
      </div>

      {/* Main Grid Content */}
      <div className="flex-1 grid grid-cols-12 gap-4 overflow-hidden min-h-0">
        
        {/* Left Side: Template Selector */}
        <div className="col-span-12 lg:col-span-4 flex flex-col space-y-3 overflow-y-auto pr-1">
          <section className="glass-card rounded-xl p-3 flex flex-col gap-2">
            <div className="flex items-center gap-2 border-b border-border-glass pb-2">
              <span className="material-symbols-outlined text-primary text-lg">description</span>
              <h3 className="text-xs font-bold text-on-surface">Chọn mẫu báo cáo</h3>
            </div>
            <div className="space-y-2">
              {templates.map((template) => {
                const isSelected = template.id === selectedTemplateId;
                return (
                  <button
                    key={template.id}
                    type="button"
                    onClick={() => setSelectedTemplateId(template.id)}
                    className={`w-full text-left p-3 rounded-lg border transition-all flex items-start gap-3 cursor-pointer ${
                      isSelected
                        ? 'bg-surface-highest/40 border-primary text-primary shadow-[0_0_10px_rgba(73,252,223,0.15)] font-bold'
                        : 'bg-black/20 border-white/5 text-on-surface-variant hover:bg-surface-highest/20 hover:text-white'
                    }`}
                  >
                    <span 
                      className="material-symbols-outlined p-1.5 rounded bg-white/5 text-base"
                      style={isSelected ? { color: template.themeColor } : {}}
                    >
                      {template.icon}
                    </span>
                    <div className="flex-1 min-w-0">
                      <p className={`text-xs ${isSelected ? 'text-white' : 'text-on-surface'} font-semibold`}>
                        {template.name}
                      </p>
                      <p className="text-[10px] text-on-surface-variant/80 mt-0.5 line-clamp-2 leading-tight">
                        {template.description}
                      </p>
                    </div>
                  </button>
                );
              })}
            </div>
          </section>

          {/* Settings Options Card */}
          <section className="glass-card rounded-xl p-3 flex flex-col gap-3">
            <div className="flex items-center gap-2 border-b border-border-glass pb-2">
              <span className="material-symbols-outlined text-primary text-lg">settings</span>
              <h3 className="text-xs font-bold text-on-surface">Tùy chọn chung</h3>
            </div>
            
            <div className="space-y-3">
              <div className="space-y-1">
                <label className="text-[10px] font-semibold text-on-surface-variant ml-1">Tiêu đề chính trong tệp Excel</label>
                <input
                  value={currentTemplate.headerTitle}
                  onChange={(e) => handleUpdateHeaderTitle(e.target.value)}
                  className="w-full bg-[#121824] border border-white/10 rounded-lg px-3 py-2 text-xs focus:border-primary focus:ring-1 focus:ring-primary/50 outline-none text-white transition-all"
                  type="text"
                />
              </div>

              {/* Theme Color Picker */}
              <div className="space-y-1.5">
                <p className="text-[10px] font-semibold text-on-surface-variant ml-1">Màu sắc chủ đạo của tiêu đề cột</p>
                <div className="flex gap-2 ml-1">
                  {['#03DFC3', '#ffba4e', '#47d6ff', '#EF4444', '#10B981'].map((color) => {
                    const isSel = currentTemplate.themeColor === color;
                    return (
                      <button
                        key={color}
                        type="button"
                        onClick={() => handleThemeColorChange(color)}
                        className="w-6 h-6 rounded-full cursor-pointer hover:scale-110 transition-all outline-none"
                        style={{
                          backgroundColor: color,
                          boxShadow: isSel ? `0 0 0 2px #0A0E17, 0 0 0-4px ${color}, 0 0 10px ${color}` : 'none',
                        }}
                      ></button>
                    );
                  })}
                </div>
              </div>

              <div className="h-[1px] bg-border-glass my-1"></div>

              {/* Checkboxes for additional features */}
              <div className="space-y-2">
                <label className="flex items-center gap-2.5 cursor-pointer select-none">
                  <input
                    type="checkbox"
                    checked={currentTemplate.showSignature}
                    onChange={handleToggleSignature}
                    className="w-3.5 h-3.5 rounded border-white/10 bg-black/40 text-primary focus:ring-primary/50 cursor-pointer accent-primary"
                  />
                  <div>
                    <span className="text-[11px] font-semibold text-white">Thêm phần ký tên cuối trang</span>
                    <p className="text-[9px] text-on-surface-variant leading-tight">Thêm block người lập, kế toán, thủ kho vào hàng cuối</p>
                  </div>
                </label>

                <label className="flex items-center gap-2.5 cursor-pointer select-none">
                  <input
                    type="checkbox"
                    checked={currentTemplate.useFormulas}
                    onChange={handleToggleFormulas}
                    className="w-3.5 h-3.5 rounded border-white/10 bg-black/40 text-primary focus:ring-primary/50 cursor-pointer accent-primary"
                  />
                  <div>
                    <span className="text-[11px] font-semibold text-white">Sử dụng công thức Excel gốc</span>
                    <p className="text-[9px] text-on-surface-variant leading-tight">Sử dụng các hàm `=SUM()`, `=IFERROR()` thay vì xuất text tĩnh</p>
                  </div>
                </label>
              </div>
            </div>
          </section>
        </div>

        {/* Right Side: Column Configurator and Live Spreadsheet Preview */}
        <div className="col-span-12 lg:col-span-8 flex flex-col space-y-3 overflow-y-auto pr-1">
          
          {/* Columns Selector Grid */}
          <section className="glass-card rounded-xl p-3">
            <div className="flex items-center gap-2 border-b border-border-glass pb-2 mb-3">
              <span className="material-symbols-outlined text-primary text-lg">view_week</span>
              <h3 className="text-xs font-bold text-on-surface">Cấu hình các cột hiển thị</h3>
            </div>
            
            <div className="grid grid-cols-1 md:grid-cols-2 gap-2">
              {currentTemplate.columns.map((col) => (
                <div 
                  key={col.key}
                  className="flex items-center justify-between p-2 rounded-lg border border-white/5 bg-black/20 hover:border-primary/20 transition-all"
                >
                  <label className="flex items-center gap-2 cursor-pointer select-none">
                    <input
                      type="checkbox"
                      checked={col.enabled}
                      onChange={() => handleToggleColumn(col.key)}
                      className="w-3.5 h-3.5 rounded border-white/10 bg-black/40 text-primary focus:ring-primary/50 cursor-pointer accent-primary"
                    />
                    <span className="text-[10px] font-semibold text-white uppercase tracking-wider">{col.defaultLabel}</span>
                  </label>
                  
                  {col.enabled && (
                    <input
                      value={col.label}
                      onChange={(e) => handleColumnLabelChange(col.key, e.target.value)}
                      className="w-28 bg-black/40 border border-white/10 rounded px-2 py-1 text-[11px] text-white focus:border-primary focus:ring-0 outline-none text-right transition-all font-semibold animate-in fade-in"
                      placeholder="Đổi tên cột..."
                      type="text"
                    />
                  )}
                </div>
              ))}
            </div>
          </section>

          {/* Live Preview spreadsheet representation */}
          <section className="glass-card rounded-xl p-3 flex flex-col min-h-0">
            <div className="flex justify-between items-center border-b border-border-glass pb-2 mb-3">
              <div className="flex items-center gap-2">
                <span className="material-symbols-outlined text-primary text-lg">preview</span>
                <h3 className="text-xs font-bold text-on-surface">Xem trước bảng Excel mô phỏng</h3>
              </div>
              <span className="px-2 py-0.5 rounded-full text-[9px] font-bold uppercase tracking-wider bg-success/15 border border-success/30 text-success glow-success flex items-center gap-1.5">
                <span className="w-1.5 h-1.5 rounded-full bg-success"></span>
                Trực quan thời gian thực
              </span>
            </div>

            {/* Simulated Excel Sheet Container */}
            <div className="bg-[#121824] rounded-lg border border-white/10 p-4 overflow-x-auto select-none">
              <div className="text-center font-bold text-white text-sm tracking-wider uppercase mb-4" style={{ color: currentTemplate.themeColor }}>
                {currentTemplate.headerTitle || 'TÊN TIÊU ĐỀ BÁO CÁO'}
              </div>
              
              <table className="w-full text-left border-collapse border border-white/15 min-w-[500px]">
                <thead>
                  <tr className="bg-white/5">
                    {currentTemplate.columns.filter(c => c.enabled).map((col) => (
                      <th 
                        key={col.key} 
                        className="border border-white/15 px-2 py-1 text-[10px] font-bold text-white text-center uppercase tracking-wide"
                        style={{ backgroundColor: `${currentTemplate.themeColor}15`, borderTop: `2px solid ${currentTemplate.themeColor}` }}
                      >
                        {col.label || col.defaultLabel}
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {[1, 2, 3].map((rowIdx) => (
                    <tr key={rowIdx} className="hover:bg-white/[0.02]">
                      {currentTemplate.columns.filter(c => c.enabled).map((col) => {
                        let sampleVal = '';
                        if (col.key === 'stt') sampleVal = `${rowIdx}`;
                        else if (col.key === 'id' || col.key === 'sku') sampleVal = selectedTemplateId === 'sales' ? `#HDB-${8400 + rowIdx}` : `#SP-${100 + rowIdx}`;
                        else if (col.key === 'name') {
                          sampleVal = selectedTemplateId === 'sales' 
                            ? (rowIdx === 1 ? 'Cà phê sữa đá' : rowIdx === 2 ? 'Trà đào cam sả' : 'Bánh Croissant')
                            : (rowIdx === 1 ? 'Hạt cà phê Robusta' : rowIdx === 2 ? 'Sữa đặc Ngôi sao' : 'Trà Lipton túi lọc');
                        }
                        else if (col.key === 'qty' || col.key === 'stock') sampleVal = `${rowIdx * 5 + 2}`;
                        else if (col.key === 'price' || col.key === 'cost') sampleVal = (rowIdx === 1 ? 29000 : rowIdx === 2 ? 35000 : 25000).toLocaleString('vi-VN') + ' ₫';
                        else if (col.key === 'total' || col.key === 'value') {
                          if (currentTemplate.useFormulas) {
                            sampleVal = `=IFERROR(D${rowIdx + 1} * E${rowIdx + 1}, 0)`;
                          } else {
                            sampleVal = ((rowIdx * 5 + 2) * (rowIdx === 1 ? 29000 : rowIdx === 2 ? 35000 : 25000)).toLocaleString('vi-VN') + ' ₫';
                          }
                        }
                        else if (col.key === 'unit') sampleVal = rowIdx === 2 ? 'Hộp' : 'Ly';
                        else if (col.key === 'category') sampleVal = rowIdx === 1 ? 'Cà phê' : 'Nguyên liệu';
                        else if (col.key === 'date') sampleVal = `28/05/2026`;
                        else if (col.key === 'amount') {
                          if (currentTemplate.useFormulas) sampleVal = `=SUM(F${rowIdx + 1})`;
                          else sampleVal = '150.000 ₫';
                        }
                        else if (col.key === 'method') sampleVal = 'Tiền mặt';
                        else if (col.key === 'note') sampleVal = 'Xuất từ hệ thống';

                        const isTotalCol = col.key === 'total' || col.key === 'value' || col.key === 'amount';
                        const isQtyCol = col.key === 'qty' || col.key === 'stock' || col.key === 'stt';

                        return (
                          <td 
                            key={col.key} 
                            className={`border border-white/10 px-2 py-1 text-[10px] font-mono ${
                              isTotalCol ? 'text-right font-bold text-emerald-400' : isQtyCol ? 'text-center text-on-surface-variant' : 'text-on-surface'
                            }`}
                          >
                            {sampleVal}
                          </td>
                        );
                      })}
                    </tr>
                  ))}
                  
                  {/* Total summary row */}
                  <tr className="bg-white/5 font-semibold">
                    <td 
                      colSpan={currentTemplate.columns.filter(c => c.enabled).length - 1} 
                      className="border border-white/15 px-2 py-1 text-[10px] text-right text-white font-bold"
                    >
                      Tổng cộng:
                    </td>
                    <td className="border border-white/15 px-2 py-1 text-[10px] text-right text-emerald-400 font-bold font-mono">
                      {currentTemplate.useFormulas ? `=SUM(...)` : '533.000 ₫'}
                    </td>
                  </tr>
                </tbody>
              </table>

              {currentTemplate.showSignature && (
                <div className="grid grid-cols-3 gap-2 mt-4 text-center text-on-surface-variant">
                  <div className="space-y-4">
                    <p className="text-[8px] font-semibold uppercase tracking-wider">Người lập phiếu</p>
                    <p className="text-[9px] font-bold text-white">(Ký & rõ họ tên)</p>
                  </div>
                  <div className="space-y-4">
                    <p className="text-[8px] font-semibold uppercase tracking-wider">Kế toán trưởng</p>
                    <p className="text-[9px] font-bold text-white">(Ký & rõ họ tên)</p>
                  </div>
                  <div className="space-y-4">
                    <p className="text-[8px] font-semibold uppercase tracking-wider">Thủ trưởng đơn vị</p>
                    <p className="text-[9px] font-bold text-white">(Ký, đóng dấu & họ tên)</p>
                  </div>
                </div>
              )}
            </div>
          </section>
        </div>

      </div>
    </div>
  );
}
