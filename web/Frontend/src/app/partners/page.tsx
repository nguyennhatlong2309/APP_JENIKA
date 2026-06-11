'use client';

import { useState, useEffect } from 'react';
import Pagination from '@/components/ui/Pagination';
import { BusinessPartner } from '@/types';
import { partnerService } from '@/services/partnerService';

export default function PartnersPage() {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');

  // Form states
  const [partnerName, setPartnerName] = useState('');
  const [phone, setPhone] = useState('');
  const [address, setAddress] = useState('');

  // Data state
  const [partners, setPartners] = useState<BusinessPartner[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalError, setModalError] = useState<string | null>(null);

  // Pagination states
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage, setItemsPerPage] = useState(10);

  // Handle escape key
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        setIsModalOpen(false);
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  const loadData = async () => {
    try {
      setLoading(true);
      const data = await partnerService.getPartners();
      setPartners(data);
    } catch (err) {
      console.error("Error loading partners:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  // Reset page when search query changes
  useEffect(() => {
    setCurrentPage(1);
  }, [searchQuery]);

  const handleCreatePartner = async (e: React.FormEvent) => {
    e.preventDefault();
    setModalError(null);

    if (!partnerName.trim()) {
      setModalError("Tên đối tác không được để trống.");
      return;
    }

    const payload = {
      ten: partnerName.trim(),
      sdt: phone.trim(),
      diaChi: address.trim()
    };

    try {
      await partnerService.createPartner(payload);
      setPartnerName('');
      setPhone('');
      setAddress('');
      setIsModalOpen(false);
      loadData();
    } catch (err: any) {
      console.error(err);
      setModalError(`Lỗi lưu đối tác: ${err.message || 'Lỗi kết nối máy chủ'}`);
    }
  };

  // Filters
  const filteredPartners = partners.filter((partner) => {
    const q = searchQuery.toLowerCase();
    return (
      partner.ten.toLowerCase().includes(q) ||
      `dt-${partner.id}`.includes(q) ||
      (partner.sdt && partner.sdt.includes(q)) ||
      (partner.diaChi && partner.diaChi.toLowerCase().includes(q))
    );
  });

  // Calculate paginated partners list
  const totalPages = Math.ceil(filteredPartners.length / itemsPerPage);
  const startIndex = (currentPage - 1) * itemsPerPage;
  const paginatedPartners = filteredPartners.slice(startIndex, startIndex + itemsPerPage);

  return (
    <div className="h-[calc(100vh-16px)] overflow-hidden flex flex-col pt-2 pb-2 px-4 space-y-3 w-full relative">
      {/* Top Header Controls */}
      <div className="flex justify-between items-center flex-shrink-0">
        <div>
          <h2 className="text-xl font-bold text-white tracking-wide">Quản lý Đối tác</h2>
          <p className="text-[10px] text-on-surface-variant mt-0.5">Quản lý danh sách khách hàng và nhà cung cấp dịch vụ của cửa hàng.</p>
        </div>
        <button
          onClick={() => setIsModalOpen(true)}
          className="bg-primary text-on-primary px-5 py-2.5 rounded-lg font-semibold flex items-center gap-2 glow-button transition-all active:scale-95 cursor-pointer text-xs animate-in fade-in"
        >
          <span className="material-symbols-outlined text-base">add</span>
          <span>Thêm đối tác mới</span>
        </button>
      </div>

      {loading ? (
        <div className="flex-1 flex items-center justify-center text-white text-sm">Đang tải danh sách đối tác...</div>
      ) : (
        <>
          {/* Partners Stats Row */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 flex-shrink-0">
            <div className="glass-card py-2.5 px-4 rounded-xl flex items-center justify-between hover:border-primary/30 transition-all">
              <div className="flex items-center gap-3">
                <span className="material-symbols-outlined text-primary bg-primary/10 p-2 rounded-lg text-lg">group</span>
                <div>
                  <p className="text-on-surface-variant text-[10px] uppercase tracking-wider mb-0.5">Tổng số đối tác</p>
                  <h3 className="text-sm font-bold text-on-surface">{partners.length} Đối tác</h3>
                </div>
              </div>
            </div>

            <div className="glass-card py-2.5 px-4 rounded-xl flex items-center justify-between hover:border-primary/30 transition-all">
              <div className="flex items-center gap-3">
                <span className="material-symbols-outlined text-success bg-success/10 p-2 rounded-lg text-lg">sync</span>
                <div>
                  <p className="text-on-surface-variant text-[10px] uppercase tracking-wider mb-0.5">Trạng thái đồng bộ</p>
                  <h3 className="text-sm font-bold text-success">Thời gian thực</h3>
                </div>
              </div>
            </div>
          </div>

          {/* Partners List Section */}
          <div className="flex-1 flex flex-col min-h-0 glass-card rounded-xl overflow-hidden border border-white/5 bg-white/1 mt-1">
            <div className="p-3 border-b border-border-glass flex justify-between items-center flex-shrink-0">
              <span className="text-white font-semibold text-xs">Danh sách Khách hàng &amp; Nhà phân phối</span>
              <div className="flex items-center gap-4">
                <div className="relative w-60">
                  <span className="material-symbols-outlined absolute left-2.5 top-1/2 -translate-y-1/2 text-xs text-on-surface-variant opacity-60">
                    search
                  </span>
                  <input
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="w-full bg-surface-lowest border border-border-glass rounded-lg py-1.5 pl-8 pr-3 text-xs focus:ring-1 focus:ring-primary/50 transition-all text-white outline-none"
                    placeholder="Tìm tên, sdt, địa chỉ..."
                    type="text"
                  />
                </div>
                <div className="text-xs text-on-surface-variant font-semibold">
                  Hiển thị <span className="text-white font-bold">{filteredPartners.length}</span> đối tác
                </div>
              </div>
            </div>

            {/* Table Content */}
            <div className="flex-1 overflow-auto">
              <table className="w-full text-left border-collapse">
                <thead className="sticky top-0 z-10 bg-[#131929] shadow-[0_1px_0_0_rgba(255,255,255,0.08)]">
                  <tr className="text-[10px] font-bold text-on-surface-variant uppercase tracking-wider border-b border-border-glass bg-[#131929]">
                    <th className="px-4 py-3">Mã đối tác</th>
                    <th className="px-4 py-3">Tên đối tác</th>
                    <th className="px-4 py-3">Số điện thoại</th>
                    <th className="px-4 py-3">Địa chỉ giao dịch</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border-glass">
                  {paginatedPartners.length === 0 ? (
                    <tr>
                      <td colSpan={4} className="px-4 py-8 text-center text-on-surface-variant text-xs font-semibold">
                        Không tìm thấy đối tác nào khớp với tìm kiếm.
                      </td>
                    </tr>
                  ) : (
                    paginatedPartners.map((partner) => (
                      <tr key={partner.id} className="hover:bg-white/5 transition-colors group">
                        <td className="px-4 py-3 font-mono text-xs text-secondary">DT-{partner.id}</td>
                        <td className="px-4 py-3 font-bold text-white text-xs">{partner.ten}</td>
                        <td className="px-4 py-3 text-xs text-white">{partner.sdt || '---'}</td>
                        <td className="px-4 py-3 text-xs text-on-surface-variant">{partner.diaChi || '---'}</td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>

            {/* Reusable Pagination Control */}
            <div className="flex-shrink-0">
              <Pagination
                currentPage={currentPage}
                totalPages={totalPages}
                onPageChange={setCurrentPage}
                totalItems={filteredPartners.length}
                itemsPerPage={itemsPerPage}
                onItemsPerPageChange={(size) => {
                  setItemsPerPage(size);
                  setCurrentPage(1);
                }}
              />
            </div>
          </div>

          {/* Add Partner Modal */}
          {isModalOpen && (
            <div className="fixed inset-0 z-50 flex items-center justify-center px-4 bg-background/85 backdrop-blur-md">
              {/* Backdrop */}
              <div className="absolute inset-0" onClick={() => setIsModalOpen(false)}></div>

              <div className="glass-card w-full max-w-2xl rounded-2xl p-8 relative z-50 animate-in fade-in zoom-in-95 duration-200">
                <button
                  className="absolute right-6 top-6 text-on-surface-variant hover:text-white cursor-pointer flex items-center justify-center"
                  onClick={() => setIsModalOpen(false)}
                >
                  <span className="material-symbols-outlined">close</span>
                </button>
                <h3 className="text-xl font-bold text-primary mb-6">Thêm đối tác giao dịch mới</h3>
                
                {modalError && (
                  <div className="p-3 mb-4 text-xs font-semibold text-error bg-error/10 border border-error/20 rounded-xl">
                    ⚠️ {modalError}
                  </div>
                )}

                <form onSubmit={handleCreatePartner} className="space-y-6">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div className="col-span-1 md:col-span-2 space-y-2">
                      <label className="text-xs font-bold uppercase tracking-widest text-on-surface-variant">Tên đối tác / Nhà cung cấp</label>
                      <input
                        required
                        value={partnerName}
                        onChange={(e) => setPartnerName(e.target.value)}
                        className="w-full bg-surface-lowest border border-border-glass rounded-lg px-4 py-3 text-white focus:outline-none focus:ring-2 focus:ring-primary/50 transition-all outline-none"
                        placeholder="Nhập tên đối tác hoặc nhà phân phối..."
                        type="text"
                      />
                    </div>
                    
                    <div className="space-y-2">
                      <label className="text-xs font-bold uppercase tracking-widest text-on-surface-variant">Số điện thoại liên hệ</label>
                      <input
                        value={phone}
                        onChange={(e) => setPhone(e.target.value)}
                        className="w-full bg-surface-lowest border border-border-glass rounded-lg px-4 py-3 text-white focus:outline-none focus:ring-2 focus:ring-primary/50 transition-all outline-none"
                        placeholder="Ví dụ: 0912xxxxxx"
                        type="tel"
                      />
                    </div>

                    <div className="space-y-2">
                      <label className="text-xs font-bold uppercase tracking-widest text-on-surface-variant">Địa chỉ liên hệ / Giao nhận</label>
                      <input
                        value={address}
                        onChange={(e) => setAddress(e.target.value)}
                        className="w-full bg-surface-lowest border border-border-glass rounded-lg px-4 py-3 text-white focus:outline-none focus:ring-2 focus:ring-primary/50 transition-all outline-none"
                        placeholder="Số nhà, Tên đường, Quận/Huyện..."
                        type="text"
                      />
                    </div>
                  </div>

                  <div className="flex space-x-4 pt-6">
                    <button
                      type="submit"
                      className="flex-1 bg-primary text-on-primary font-bold py-3 rounded-xl glow-teal hover:scale-[1.02] active:scale-95 transition-all cursor-pointer"
                    >
                      Lưu thông tin đối tác
                    </button>
                    <button
                      type="button"
                      onClick={() => setIsModalOpen(false)}
                      className="flex-1 bg-white/5 border border-border-glass text-white font-bold py-3 rounded-xl hover:bg-white/10 transition-all cursor-pointer"
                    >
                      Hủy bỏ
                    </button>
                  </div>
                </form>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
}
