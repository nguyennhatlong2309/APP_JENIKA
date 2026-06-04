'use client';

import { useState, useEffect } from 'react';
import Pagination from '@/components/Pagination';

interface BusinessPartner {
  id: number;
  ten: string;
  sdt?: string;
  diaChi?: string;
}

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
      const res = await fetch("http://localhost:8080/api/v1/metadata/doi-tac");
      if (res.ok) setPartners(await res.json());
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
      const res = await fetch("http://localhost:8080/api/v1/metadata/doi-tac", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });

      if (res.ok) {
        setPartnerName('');
        setPhone('');
        setAddress('');
        setIsModalOpen(false);
        loadData();
      } else {
        const text = await res.text();
        setModalError(`Lỗi lưu đối tác: ${text}`);
      }
    } catch (err) {
      console.error(err);
      setModalError("Lỗi kết nối máy chủ.");
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
    <div className="p-8 space-y-6 max-w-[1600px] mx-auto w-full relative">
      {/* Top Bar Navigation */}
      <div className="flex justify-between items-center">
        <div className="flex items-center gap-8 flex-1">
          <div className="relative w-96 group">
            <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-on-surface-variant group-focus-within:text-primary">
              search
            </span>
            <input
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full bg-surface-lowest border border-border-glass rounded-lg pl-10 pr-4 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/50 transition-all text-white outline-none"
              placeholder="Tìm kiếm đối tác, nhà cung cấp, khách hàng..."
              type="text"
            />
          </div>
        </div>
        <button
          onClick={() => setIsModalOpen(true)}
          className="bg-primary text-on-primary px-6 py-2 rounded-lg font-semibold glow-teal flex items-center transition-all hover:scale-105 active:scale-95 cursor-pointer"
        >
          <span className="material-symbols-outlined mr-2 text-lg">add</span>
          Thêm đối tác mới
        </button>
      </div>

      {loading ? (
        <div className="text-center py-20 text-white text-sm">Đang tải danh sách đối tác...</div>
      ) : (
        <>
          {/* Partners Stats Row */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <div className="glass-card p-6 rounded-xl flex items-center space-x-4">
              <div className="w-12 h-12 rounded-lg bg-primary/10 flex items-center justify-center text-primary">
                <span className="material-symbols-outlined">group</span>
              </div>
              <div>
                <p className="text-xs text-on-surface-variant uppercase tracking-wider">Tổng số đối tác</p>
                <p className="text-xl font-bold text-white">{partners.length} Tài khoản</p>
              </div>
            </div>

            <div className="glass-card p-6 rounded-xl flex items-center space-x-4">
              <div className="w-12 h-12 rounded-lg bg-success/10 flex items-center justify-center text-success">
                <span className="material-symbols-outlined">person_pin_circle</span>
              </div>
              <div>
                <p className="text-xs text-on-surface-variant uppercase tracking-wider">Đã đồng bộ</p>
                <p className="text-xl font-bold text-white">Thời gian thực</p>
              </div>
            </div>
          </div>

          {/* Partners List Section */}
          <div className="glass-card rounded-xl overflow-hidden">
            <div className="p-6 border-b border-border-glass flex justify-between items-center">
              <span className="text-white font-semibold">Danh sách Khách hàng &amp; Nhà phân phối</span>
              <div className="text-xs text-text-variant font-semibold">
                Tìm thấy <span className="text-white font-bold">{filteredPartners.length}</span> tài khoản đối tác
              </div>
            </div>

            {/* Table Content */}
            <div className="overflow-x-auto">
              <table className="w-full text-left">
                <thead>
                  <tr className="bg-white/5 text-xs text-on-surface-variant uppercase tracking-wider border-b border-border-glass">
                    <th className="px-6 py-4 font-semibold">Mã đối tác</th>
                    <th className="px-6 py-4 font-semibold">Tên đối tác</th>
                    <th className="px-6 py-4 font-semibold">Số điện thoại</th>
                    <th className="px-6 py-4 font-semibold">Địa chỉ giao dịch</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border-glass">
                  {paginatedPartners.length === 0 ? (
                    <tr>
                      <td colSpan={4} className="px-6 py-12 text-center text-on-surface-variant text-xs font-semibold">
                        Không tìm thấy đối tác nào khớp với tìm kiếm.
                      </td>
                    </tr>
                  ) : (
                    paginatedPartners.map((partner) => (
                      <tr key={partner.id} className="hover:bg-white/5 transition-colors group">
                        <td className="px-6 py-4 font-mono text-sm text-secondary">DT-{partner.id}</td>
                        <td className="px-6 py-4 font-bold text-white">{partner.ten}</td>
                        <td className="px-6 py-4 text-xs text-white">{partner.sdt || '---'}</td>
                        <td className="px-6 py-4 text-xs text-on-surface-variant">{partner.diaChi || '---'}</td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>

            {/* Reusable Pagination Control */}
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
