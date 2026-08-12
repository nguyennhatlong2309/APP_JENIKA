'use client';

import { useState, useEffect } from 'react';
import storeConfigService from '@/services/storeConfigService';
import { StoreConfig } from '@/types';

const DEFAULT_CONFIG: StoreConfig = {
  shopName: "JENKA COFFEE SHOP",
  shopNamePnh: "Jenka Coffee Shop",
  shopAddr: "Địa chỉ: Số 12 Trần Thị Do - Khu phố 24 - Phường Tân Thới Hiệp - TP HCM",
  shopTel: "Điện thoại: 0817909090 - 0827909090",
  shopBank: "Số TK: 2050103869999 - Ngân hàng MB bank - Chủ tài khoản: Dương Văn Công",
  shopNotes: "   - Khi mua hàng Nếu có sai lệch về hàng hoá và số lượng so với HĐBH/ phiếu giao nhận của dịch vụ vận chuyển, hãy liên hệ ngay với NVKD để được giải quyết (Chúng tôi chỉ giải quyết khiếu nại về giao nhận trong ngày Quý khách nhận được hàng).\n" +
          "   - Về đơn hàng: Chúng tôi chỉ giải quyết khiếu nại trong 2 ngày kể từ ngày Quý khách nhận được hàng (bao gồm các trường hợp về số lượng sản phẩm và trình trạng hàng hoá như: vỡ hỏng, móp méo, lỗi). Quý khách vui lòng cung cấp hình ảnh, video hàng hoá thực nhận cho NVKD để khiếu nại.\n" +
          "   - Trong trường hợp bảo hành máy, Quý khách vui lòng gửi máy về cửa hàng để kiểm tra và sửa chữa cho quý khách được thuận tiện và nhanh nhất.",
  shopPolicy: "Nếu khách hàng muốn đổi,  trả lại máy thì phải chịu phí 30% giá trị máy.\n" +
          " - Sau 01 tháng thì tuỳ thuộc vào giá thị trường và độ hao mòn của máy.\n" +
          " - Khi trả lại máy cho nhà cung cấp thì sau 7-10 ngày sẽ hoàn trả lại tiền theo quy định trên.",
  shopWarranty: "- Chế độ bảo hành chính hãng chỉ có hiệu lực với các sự cố do lỗi của nhà sản xuất. Nội dung bảo hành thực hiện theo chính sách bảo hành của nhà sản xuất. Các trường hợp lỗi do chập cháy, thiên tai, hoả hoạn hoặc sử dụng, bảo quản thiết bị không đúng chỉ dẫn của nhà sản xuất, do lỗi nguyên nhân chủ quan sẽ không được bảo hành.\n" +
          "- Các phụ kiện không được bảo hành: Vỏ ngoài, pin, các thiết bị hao mòn: Trục Socker, lưỡi dao, cối đựng, que khuấy, gioăng cao su, lưỡi ép...",
  shopWarrantyLimit: "- Bảo hành 3-6 tháng với máy cũ và 12 tháng với máy mới."
};

export default function ExcelConfigPage() {
  const [config, setConfig] = useState<StoreConfig>({
    shopName: '',
    shopNamePnh: '',
    shopAddr: '',
    shopTel: '',
    shopBank: '',
    shopNotes: '',
    shopPolicy: '',
    shopWarranty: '',
    shopWarrantyLimit: ''
  });
  
  const [loading, setLoading] = useState<boolean>(true);
  const [saving, setSaving] = useState<boolean>(false);
  const [notification, setNotification] = useState<{ text: string; type: 'success' | 'error' | '' }>({ text: '', type: '' });

  // Load config on mount
  useEffect(() => {
    fetchConfig();
  }, []);

  // Auto-resize textareas to fit content automatically
  useEffect(() => {
    if (!loading) {
      const timer = setTimeout(() => {
        const textareas = document.querySelectorAll('textarea');
        textareas.forEach((el) => {
          el.style.height = 'auto';
          el.style.height = `${el.scrollHeight}px`;
        });
      }, 50);
      return () => clearTimeout(timer);
    }
  }, [loading, config]);

  const fetchConfig = async () => {
    try {
      setLoading(true);
      const data = await storeConfigService.getConfig();
      setConfig(data);
    } catch (error) {
      console.error('Error fetching store config:', error);
      showNotification('Không thể tải cấu hình cửa hàng từ hệ thống.', 'error');
    } finally {
      setLoading(false);
    }
  };

  const showNotification = (text: string, type: 'success' | 'error') => {
    setNotification({ text, type });
    setTimeout(() => {
      setNotification({ text: '', type: '' });
    }, 4000);
  };

  const handleInputChange = (field: keyof StoreConfig, value: string) => {
    setConfig(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const validateConfig = (): boolean => {
    return (
      config.shopName.trim() !== '' &&
      config.shopNamePnh.trim() !== '' &&
      config.shopAddr.trim() !== '' &&
      config.shopTel.trim() !== '' &&
      config.shopBank.trim() !== '' &&
      config.shopNotes.trim() !== '' &&
      config.shopPolicy.trim() !== '' &&
      config.shopWarranty.trim() !== '' &&
      config.shopWarrantyLimit.trim() !== ''
    );
  };

  const handleSaveConfig = async () => {
    if (!validateConfig()) {
      showNotification('Vui lòng điền đầy đủ tất cả các trường thông tin!', 'error');
      return;
    }

    try {
      setSaving(true);
      const saved = await storeConfigService.updateConfig(config);
      setConfig(saved);
      showNotification('Đã lưu cấu hình cửa hàng mới thành công!', 'success');
    } catch (error) {
      console.error('Error saving store config:', error);
      showNotification('Đã xảy ra lỗi khi lưu cấu hình.', 'error');
    } finally {
      setSaving(false);
    }
  };

  const handleResetToDefault = async () => {
    const confirmReset = window.confirm("Bạn có chắc chắn muốn khôi phục thông tin mặc định của cửa hàng?");
    if (!confirmReset) return;

    try {
      setSaving(true);
      const saved = await storeConfigService.updateConfig(DEFAULT_CONFIG);
      setConfig(saved);
      showNotification('Đã khôi phục cấu hình cửa hàng về mặc định!', 'success');
    } catch (error) {
      console.error('Error resetting store config:', error);
      showNotification('Đã xảy ra lỗi khi khôi phục cấu hình mặc định.', 'error');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="h-[calc(100vh-16px)] overflow-hidden flex flex-col pt-2 pb-2 px-4 space-y-3 w-full relative">
      {/* Toast Notification */}
      {notification.text && (
        <div className={`absolute top-4 right-4 z-50 px-4 py-3 rounded-lg shadow-xl border text-xs font-semibold flex items-center gap-2 animate-in fade-in slide-in-from-top-4 duration-300 ${
          notification.type === 'success' 
            ? 'bg-success/15 border-success/30 text-success glow-success' 
            : 'bg-error/15 border-error/30 text-error glow-error'
        }`}>
          <span className="material-symbols-outlined text-base">
            {notification.type === 'success' ? 'check_circle' : 'warning'}
          </span>
          {notification.text}
        </div>
      )}

      {/* Page Header */}
      <div className="flex justify-between items-center flex-shrink-0">
        <div>
          <h2 className="text-xl font-bold text-white flex items-center gap-2">
            <span className="material-symbols-outlined text-primary text-xl">store</span>
            Cấu hình Cửa hàng
          </h2>
          <p className="text-[10px] text-on-surface-variant mt-0.5">
            Quản lý thông tin quán hiển thị mặc định trên hóa đơn và phiếu nhập khi xuất dữ liệu ra file Excel (.xlsx) hoặc Word (.docx).
          </p>
        </div>
        <div className="flex gap-2">
          <button 
            type="button"
            disabled={loading || saving}
            onClick={handleResetToDefault}
            className="px-4 py-2 rounded-lg border border-border-glass text-on-surface hover:bg-surface-highest/50 transition-all font-semibold text-xs cursor-pointer outline-none active:scale-95 disabled:opacity-50 disabled:pointer-events-none"
            id="btn-reset-excel"
          >
            Đặt lại mặc định
          </button>
          <button 
            type="button"
            disabled={loading || saving}
            onClick={handleSaveConfig}
            className="px-5 py-2 rounded-lg bg-primary text-on-primary font-bold glow-teal transition-all active:scale-95 text-xs cursor-pointer outline-none flex items-center gap-1.5 disabled:opacity-50 disabled:pointer-events-none"
            id="btn-save-excel"
          >
            {saving ? (
              <>
                <span className="w-3.5 h-3.5 border-2 border-on-primary border-t-transparent rounded-full animate-spin"></span>
                Đang lưu...
              </>
            ) : (
              <>
                <span className="material-symbols-outlined text-sm">save</span>
                Lưu cấu hình
              </>
            )}
          </button>
        </div>
      </div>

      {loading ? (
        <div className="flex-1 flex flex-col items-center justify-center gap-3">
          <div className="w-10 h-10 border-4 border-primary border-t-transparent rounded-full animate-spin"></div>
          <p className="text-xs text-on-surface-variant font-semibold">Đang tải cấu hình cửa hàng...</p>
        </div>
      ) : (
        /* Main Scrollable Content Area */
        <div className="flex-1 overflow-y-auto pr-1 min-h-0 space-y-4">
          <div className="grid grid-cols-12 gap-4 pb-4">
            
            {/* Left Card: Basic Store Information */}
            <section className="col-span-12 lg:col-span-6 glass-card rounded-xl p-4 flex flex-col gap-4">
              <div className="flex items-center gap-2 border-b border-border-glass pb-2.5">
                <span className="material-symbols-outlined text-primary text-lg">info</span>
                <h3 className="text-xs font-bold text-on-surface">Thông tin cơ bản</h3>
              </div>

              <div className="space-y-3.5">
                {/* Shop Name (Invoice) */}
                <div className="space-y-1">
                  <label className="text-[10px] font-bold text-on-surface-variant uppercase tracking-wider ml-1">
                    Tên cửa hàng (Hóa đơn - In hoa)
                  </label>
                  <input
                    value={config.shopName}
                    onChange={(e) => handleInputChange('shopName', e.target.value)}
                    className="w-full bg-[#121824] border border-white/10 rounded-lg px-3 py-2 text-xs focus:border-primary focus:ring-1 focus:ring-primary/50 outline-none text-white transition-all font-semibold"
                    type="text"
                    placeholder="VD: JENKA COFFEE SHOP"
                  />
                </div>

                {/* Shop Name (Purchase Order) */}
                <div className="space-y-1">
                  <label className="text-[10px] font-bold text-on-surface-variant uppercase tracking-wider ml-1">
                    Tên cửa hàng (Phiếu nhập)
                  </label>
                  <input
                    value={config.shopNamePnh}
                    onChange={(e) => handleInputChange('shopNamePnh', e.target.value)}
                    className="w-full bg-[#121824] border border-white/10 rounded-lg px-3 py-2 text-xs focus:border-primary focus:ring-1 focus:ring-primary/50 outline-none text-white transition-all"
                    type="text"
                    placeholder="VD: Jenka Coffee Shop"
                  />
                </div>

                {/* Shop Address */}
                <div className="space-y-1">
                  <label className="text-[10px] font-bold text-on-surface-variant uppercase tracking-wider ml-1">
                    Địa chỉ cửa hàng
                  </label>
                  <textarea
                    data-lenis-prevent
                    value={config.shopAddr}
                    onChange={(e) => handleInputChange('shopAddr', e.target.value)}
                    className="w-full bg-[#121824] border border-white/10 rounded-lg px-3 py-2 text-xs focus:border-primary focus:ring-1 focus:ring-primary/50 outline-none text-white transition-all resize-y font-sans"
                    rows={2}
                    placeholder="Nhập địa chỉ chi tiết..."
                  />
                </div>

                {/* Shop Telephone */}
                <div className="space-y-1">
                  <label className="text-[10px] font-bold text-on-surface-variant uppercase tracking-wider ml-1">
                    Điện thoại liên hệ
                  </label>
                  <input
                    value={config.shopTel}
                    onChange={(e) => handleInputChange('shopTel', e.target.value)}
                    className="w-full bg-[#121824] border border-white/10 rounded-lg px-3 py-2 text-xs focus:border-primary focus:ring-1 focus:ring-primary/50 outline-none text-white transition-all"
                    type="text"
                    placeholder="Số điện thoại hỗ trợ..."
                  />
                </div>

                {/* Shop Bank */}
                <div className="space-y-1">
                  <label className="text-[10px] font-bold text-on-surface-variant uppercase tracking-wider ml-1">
                    Tài khoản ngân hàng / Thanh toán
                  </label>
                  <textarea
                    data-lenis-prevent
                    value={config.shopBank}
                    onChange={(e) => handleInputChange('shopBank', e.target.value)}
                    className="w-full bg-[#121824] border border-white/10 rounded-lg px-3 py-2 text-xs focus:border-primary focus:ring-1 focus:ring-primary/50 outline-none text-white transition-all resize-y font-sans"
                    rows={2}
                    placeholder="Thông tin thanh toán ngân hàng..."
                  />
                </div>
              </div>
            </section>

            {/* Right Card: Rules, Warranty & Notes */}
            <section className="col-span-12 lg:col-span-6 glass-card rounded-xl p-4 flex flex-col gap-4">
              <div className="flex items-center gap-2 border-b border-border-glass pb-2.5">
                <span className="material-symbols-outlined text-primary text-lg">gavel</span>
                <h3 className="text-xs font-bold text-on-surface">Chính sách &amp; Lưu ý</h3>
              </div>

              <div className="space-y-3.5">
                {/* Shop Notes */}
                <div className="space-y-1">
                  <label className="text-[10px] font-bold text-on-surface-variant uppercase tracking-wider ml-1">
                    Lưu ý khách hàng
                  </label>
                  <textarea
                    data-lenis-prevent
                    value={config.shopNotes}
                    onChange={(e) => handleInputChange('shopNotes', e.target.value)}
                    className="w-full bg-[#121824] border border-white/10 rounded-lg px-3 py-2 text-xs focus:border-primary focus:ring-1 focus:ring-primary/50 outline-none text-white transition-all resize-y font-sans"
                    rows={5}
                    placeholder="Các lưu ý chung khi nhận hàng..."
                  />
                </div>

                {/* Shop Policy */}
                <div className="space-y-1">
                  <label className="text-[10px] font-bold text-on-surface-variant uppercase tracking-wider ml-1">
                    Quy định đổi và hoàn trả hàng
                  </label>
                  <textarea
                    data-lenis-prevent
                    value={config.shopPolicy}
                    onChange={(e) => handleInputChange('shopPolicy', e.target.value)}
                    className="w-full bg-[#121824] border border-white/10 rounded-lg px-3 py-2 text-xs focus:border-primary focus:ring-1 focus:ring-primary/50 outline-none text-white transition-all resize-y font-sans"
                    rows={4}
                    placeholder="Chính sách hoàn trả hàng..."
                  />
                </div>

                {/* Shop Warranty */}
                <div className="space-y-1">
                  <label className="text-[10px] font-bold text-on-surface-variant uppercase tracking-wider ml-1">
                    Thời gian bảo hành theo từng sản phẩm
                  </label>
                  <textarea
                    data-lenis-prevent
                    value={config.shopWarranty}
                    onChange={(e) => handleInputChange('shopWarranty', e.target.value)}
                    className="w-full bg-[#121824] border border-white/10 rounded-lg px-3 py-2 text-xs focus:border-primary focus:ring-1 focus:ring-primary/50 outline-none text-white transition-all resize-y font-sans"
                    rows={5}
                    placeholder="Quy định bảo hành và phụ kiện..."
                  />
                </div>

                {/* Shop Warranty Limit */}
                <div className="space-y-1">
                  <label className="text-[10px] font-bold text-on-surface-variant uppercase tracking-wider ml-1">
                    Dòng thời gian bảo hành
                  </label>
                  <textarea
                    data-lenis-prevent
                    value={config.shopWarrantyLimit}
                    onChange={(e) => handleInputChange('shopWarrantyLimit', e.target.value)}
                    className="w-full bg-[#121824] border border-white/10 rounded-lg px-3 py-2 text-xs focus:border-primary focus:ring-1 focus:ring-primary/50 outline-none text-white transition-all resize-y font-sans"
                    rows={2}
                    placeholder="VD: - Bảo hành 3-6 tháng với máy cũ và 12 tháng với máy mới."
                  />
                </div>
              </div>
            </section>

          </div>
        </div>
      )}
    </div>
  );
}
