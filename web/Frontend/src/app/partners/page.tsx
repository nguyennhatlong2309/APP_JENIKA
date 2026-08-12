'use client';

import { useState, useEffect } from 'react';
import Pagination from '@/components/ui/Pagination';
import { BusinessPartner, EmployeeItem } from '@/types';
import { partnerService } from '@/services/partnerService';

export default function PartnersPage() {
  const [activeTab, setActiveTab] = useState<'partners' | 'employees'>('partners');
  const [isPartnerModalOpen, setIsPartnerModalOpen] = useState(false);
  const [isEmployeeModalOpen, setIsEmployeeModalOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [debouncedSearchQuery, setDebouncedSearchQuery] = useState('');

  // Editing state trackers
  const [editingPartner, setEditingPartner] = useState<BusinessPartner | null>(null);
  const [editingEmployee, setEditingEmployee] = useState<EmployeeItem | null>(null);

  // Form states - Partner
  const [partnerName, setPartnerName] = useState('');
  const [phone, setPhone] = useState('');
  const [address, setAddress] = useState('');

  // Form states - Employee
  const [employeeName, setEmployeeName] = useState('');
  const [employeePhone, setEmployeePhone] = useState('');
  const [employeeEmail, setEmployeeEmail] = useState('');
  const [employeeRole, setEmployeeRole] = useState('Nhân viên');

  // Data states
  const [partners, setPartners] = useState<BusinessPartner[]>([]);
  const [employees, setEmployees] = useState<EmployeeItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalError, setModalError] = useState<string | null>(null);

  // Pagination states
  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage, setItemsPerPage] = useState(10);
  const [totalItems, setTotalItems] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  // Debounce search query
  useEffect(() => {
    const handler = setTimeout(() => {
      setDebouncedSearchQuery(searchQuery);
      setCurrentPage(1);
    }, 400);
    return () => clearTimeout(handler);
  }, [searchQuery]);

  // Handle escape key
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        setIsPartnerModalOpen(false);
        setIsEmployeeModalOpen(false);
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  const handleTabChange = (tab: 'partners' | 'employees') => {
    setActiveTab(tab);
    setSearchQuery('');
    setDebouncedSearchQuery('');
    setCurrentPage(1);
    setModalError(null);
  };

  const loadData = async () => {
    try {
      setLoading(true);
      if (activeTab === 'partners') {
        const data = await partnerService.getPartnersPage({
          page: currentPage - 1,
          size: itemsPerPage,
          search: debouncedSearchQuery
        });
        setPartners(data.content);
        setTotalItems(data.totalElements);
        setTotalPages(data.totalPages);
      } else {
        const data = await partnerService.getEmployeesPage({
          page: currentPage - 1,
          size: itemsPerPage,
          search: debouncedSearchQuery
        });
        setEmployees(data.content);
        setTotalItems(data.totalElements);
        setTotalPages(data.totalPages);
      }
    } catch (err) {
      console.error("Error loading data:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [currentPage, itemsPerPage, debouncedSearchQuery, activeTab]);

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
      if (editingPartner) {
        await partnerService.updatePartner(editingPartner.id, payload);
      } else {
        await partnerService.createPartner(payload);
      }
      setPartnerName('');
      setPhone('');
      setAddress('');
      setEditingPartner(null);
      setIsPartnerModalOpen(false);
      loadData();
    } catch (err: any) {
      console.error(err);
      setModalError(`Lỗi lưu đối tác: ${err.message || 'Lỗi kết nối máy chủ'}`);
    }
  };

  const handleCreateEmployee = async (e: React.FormEvent) => {
    e.preventDefault();
    setModalError(null);

    if (!employeeName.trim()) {
      setModalError("Tên nhân viên không được để trống.");
      return;
    }

    const payload = {
      tenNhanVien: employeeName.trim(),
      sdt: employeePhone.trim() || undefined,
      email: employeeEmail.trim() || undefined,
      vaiTro: employeeRole.trim() || undefined
    };

    try {
      if (editingEmployee) {
        await partnerService.updateEmployee(editingEmployee.id, payload);
      } else {
        await partnerService.createEmployee(payload);
      }
      setEmployeeName('');
      setEmployeePhone('');
      setEmployeeEmail('');
      setEmployeeRole('Nhân viên');
      setEditingEmployee(null);
      setIsEmployeeModalOpen(false);
      loadData();
    } catch (err: any) {
      console.error(err);
      setModalError(`Lỗi lưu nhân viên: ${err.message || 'Lỗi kết nối máy chủ'}`);
    }
  };

  const handleEditPartner = (partner: BusinessPartner) => {
    setEditingPartner(partner);
    setPartnerName(partner.ten);
    setPhone(partner.sdt || '');
    setAddress(partner.diaChi || '');
    setModalError(null);
    setIsPartnerModalOpen(true);
  };

  const handleEditEmployee = (employee: EmployeeItem) => {
    setEditingEmployee(employee);
    setEmployeeName(employee.tenNhanVien);
    setEmployeePhone(employee.sdt || '');
    setEmployeeEmail(employee.email || '');
    setEmployeeRole(employee.vaiTro || 'Nhân viên');
    setModalError(null);
    setIsEmployeeModalOpen(true);
  };

  const handleDeletePartner = async (id: number, name: string) => {
    if (!window.confirm(`Bạn có chắc chắn muốn xóa đối tác "${name}" không?`)) {
      return;
    }
    try {
      await partnerService.deletePartner(id);
      loadData();
    } catch (err: any) {
      console.error(err);
      alert(`Không thể xóa đối tác: ${err.message || 'Có thể đối tác này đã được sử dụng trong các giao dịch hóa đơn/nhập hàng.'}`);
    }
  };

  const handleDeleteEmployee = async (id: number, name: string) => {
    if (!window.confirm(`Bạn có chắc chắn muốn xóa nhân viên "${name}" không?`)) {
      return;
    }
    try {
      await partnerService.deleteEmployee(id);
      loadData();
    } catch (err: any) {
      console.error(err);
      alert(`Không thể xóa nhân viên: ${err.message || 'Có thể nhân viên này đã được phân công trong các hóa đơn hoặc nhật ký thu chi.'}`);
    }
  };

  return (
    <div className="h-[calc(100vh-16px)] overflow-hidden flex flex-col pt-2 pb-2 px-4 space-y-3 w-full relative">
      {/* Top Header Controls with Tabs */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-white/10 pb-2 flex-shrink-0">
        <div className="flex flex-wrap items-center gap-6">
          <h2 className="text-xl font-bold text-white tracking-tight">
            {activeTab === 'partners' ? 'Quản lý Đối tác' : 'Quản lý Nhân viên'}
          </h2>
          
          {/* Tabs Navigation */}
          <div className="flex bg-white/5 p-1 rounded-lg gap-1">
            <button
              onClick={() => handleTabChange('partners')}
              className={`px-4 py-1.5 rounded-md text-xs font-semibold uppercase tracking-wider transition-colors cursor-pointer ${
                activeTab === 'partners' ? 'bg-primary text-on-primary font-bold' : 'text-on-surface-variant hover:text-white'
              }`}
            >
              Đối tác
            </button>
            <button
              onClick={() => handleTabChange('employees')}
              className={`px-4 py-1.5 rounded-md text-xs font-semibold uppercase tracking-wider transition-colors cursor-pointer ${
                activeTab === 'employees' ? 'bg-primary text-on-primary font-bold' : 'text-on-surface-variant hover:text-white'
              }`}
            >
              Nhân viên
            </button>
          </div>
        </div>

        {activeTab === 'partners' ? (
          <button
            onClick={() => {
              setEditingPartner(null);
              setPartnerName('');
              setPhone('');
              setAddress('');
              setModalError(null);
              setIsPartnerModalOpen(true);
            }}
            className="bg-primary text-on-primary px-5 py-2 rounded-lg font-semibold flex items-center gap-2 glow-button transition-all active:scale-95 cursor-pointer text-xs animate-in fade-in"
          >
            <span className="material-symbols-outlined text-base">add</span>
            <span>Thêm đối tác mới</span>
          </button>
        ) : (
          <button
            onClick={() => {
              setEditingEmployee(null);
              setEmployeeName('');
              setEmployeePhone('');
              setEmployeeEmail('');
              setEmployeeRole('Nhân viên');
              setModalError(null);
              setIsEmployeeModalOpen(true);
            }}
            className="bg-primary text-on-primary px-5 py-2 rounded-lg font-semibold flex items-center gap-2 glow-button transition-all active:scale-95 cursor-pointer text-xs animate-in fade-in"
          >
            <span className="material-symbols-outlined text-base">add</span>
            <span>Thêm nhân viên mới</span>
          </button>
        )}
      </div>

      {loading && (activeTab === 'partners' ? partners.length === 0 : employees.length === 0) ? (
        <div className="flex-1 flex items-center justify-center text-white text-sm">
          Đang tải danh sách {activeTab === 'partners' ? 'đối tác' : 'nhân viên'}...
        </div>
      ) : (
        <>
          {/* Stats Row */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 flex-shrink-0">
            <div className="glass-card py-2.5 px-4 rounded-xl flex items-center justify-between hover:border-primary/30 transition-all">
              <div className="flex items-center gap-3">
                <span className="material-symbols-outlined text-primary bg-primary/10 p-2 rounded-lg text-lg">
                  {activeTab === 'partners' ? 'group' : 'badge'}
                </span>
                <div>
                  <p className="text-on-surface-variant text-[10px] uppercase tracking-wider mb-0.5">
                    {activeTab === 'partners' ? 'Tổng số đối tác' : 'Tổng số nhân viên'}
                  </p>
                  <h3 className="text-sm font-bold text-on-surface">
                    {totalItems} {activeTab === 'partners' ? 'Đối tác' : 'Nhân viên'}
                  </h3>
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

          {/* List Section */}
          <div className="flex-1 flex flex-col min-h-0 glass-card rounded-xl overflow-hidden border border-white/5 bg-white/1 mt-1">
            <div className="p-3 border-b border-border-glass flex justify-between items-center flex-shrink-0">
              <span className="text-white font-semibold text-xs">
                {activeTab === 'partners' ? 'Danh sách Khách hàng & Nhà phân phối' : 'Danh sách Nhân viên cửa hàng'}
              </span>
              <div className="flex items-center gap-4">
                <div className="relative w-60">
                  <span className="material-symbols-outlined absolute left-2.5 top-1/2 -translate-y-1/2 text-xs text-on-surface-variant opacity-60">
                    search
                  </span>
                  <input
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="w-full bg-surface-lowest border border-border-glass rounded-lg py-1.5 pl-8 pr-3 text-xs focus:ring-1 focus:ring-primary/50 transition-all text-white outline-none"
                    placeholder={activeTab === 'partners' ? "Tìm tên, sdt, địa chỉ..." : "Tìm tên, sdt, email, vai trò..."}
                    type="text"
                  />
                </div>
                <div className="text-xs text-on-surface-variant font-semibold">
                  Hiển thị <span className="text-white font-bold">{totalItems}</span> {activeTab === 'partners' ? 'đối tác' : 'nhân viên'}
                </div>
              </div>
            </div>

            {/* Table Content */}
            <div className="flex-1 overflow-auto" data-lenis-prevent="">
              <table className="w-full text-left border-collapse">
                <thead className="sticky top-0 z-10 bg-[#131929] shadow-[0_1px_0_0_rgba(255,255,255,0.08)]">
                  {activeTab === 'partners' ? (
                    <tr className="text-[10px] font-bold text-on-surface-variant uppercase tracking-wider border-b border-border-glass bg-[#131929]">
                      <th className="px-4 py-3">Mã đối tác</th>
                      <th className="px-4 py-3">Tên đối tác</th>
                      <th className="px-4 py-3">Số điện thoại</th>
                      <th className="px-4 py-3">Địa chỉ giao dịch</th>
                      <th className="px-4 py-3 text-center w-24">Thao tác</th>
                    </tr>
                  ) : (
                    <tr className="text-[10px] font-bold text-on-surface-variant uppercase tracking-wider border-b border-border-glass bg-[#131929]">
                      <th className="px-4 py-3">Mã nhân viên</th>
                      <th className="px-4 py-3">Tên nhân viên</th>
                      <th className="px-4 py-3">Số điện thoại</th>
                      <th className="px-4 py-3">Email</th>
                      <th className="px-4 py-3">Vai trò / Chức vụ</th>
                      <th className="px-4 py-3 text-center w-24">Thao tác</th>
                    </tr>
                  )}
                </thead>
                <tbody className="divide-y divide-border-glass">
                  {activeTab === 'partners' ? (
                    partners.length === 0 ? (
                      <tr>
                        <td colSpan={5} className="px-4 py-8 text-center text-on-surface-variant text-xs font-semibold">
                          Không tìm thấy đối tác nào khớp với tìm kiếm.
                        </td>
                      </tr>
                    ) : (
                      partners.map((partner) => (
                        <tr key={partner.id} className="hover:bg-white/5 transition-colors group">
                          <td className="px-4 py-3 font-mono text-xs text-secondary">DT-{partner.id}</td>
                          <td className="px-4 py-3 font-bold text-white text-xs">{partner.ten}</td>
                          <td className="px-4 py-3 text-xs text-white">{partner.sdt || '---'}</td>
                          <td className="px-4 py-3 text-xs text-on-surface-variant">{partner.diaChi || '---'}</td>
                          <td className="px-4 py-3 text-center">
                            <div className="flex items-center justify-center gap-1.5">
                              <button
                                onClick={() => handleEditPartner(partner)}
                                className="p-1 hover:bg-primary/10 rounded text-on-surface-variant hover:text-primary transition-colors cursor-pointer inline-flex items-center"
                                title="Chỉnh sửa đối tác"
                              >
                                <span className="material-symbols-outlined text-base">edit</span>
                              </button>
                              <button
                                onClick={() => handleDeletePartner(partner.id, partner.ten)}
                                className="p-1 hover:bg-error/10 rounded text-on-surface-variant hover:text-error transition-colors cursor-pointer inline-flex items-center"
                                title="Xóa đối tác"
                              >
                                <span className="material-symbols-outlined text-base">delete</span>
                              </button>
                            </div>
                          </td>
                        </tr>
                      ))
                    )
                  ) : (
                    employees.length === 0 ? (
                      <tr>
                        <td colSpan={6} className="px-4 py-8 text-center text-on-surface-variant text-xs font-semibold">
                          Không tìm thấy nhân viên nào khớp với tìm kiếm.
                        </td>
                      </tr>
                    ) : (
                      employees.map((employee) => (
                        <tr key={employee.id} className="hover:bg-white/5 transition-colors group">
                          <td className="px-4 py-3 font-mono text-xs text-secondary">NV-{employee.id}</td>
                          <td className="px-4 py-3 font-bold text-white text-xs">{employee.tenNhanVien}</td>
                          <td className="px-4 py-3 text-xs text-white">{employee.sdt || '---'}</td>
                          <td className="px-4 py-3 text-xs text-white">{employee.email || '---'}</td>
                          <td className="px-4 py-3 text-xs text-on-surface-variant">
                            <span className="px-2 py-0.5 rounded bg-primary/10 text-primary border border-primary/20 text-[10px] font-semibold">
                              {employee.vaiTro || 'Nhân viên'}
                            </span>
                          </td>
                          <td className="px-4 py-3 text-center">
                            <div className="flex items-center justify-center gap-1.5">
                              <button
                                onClick={() => handleEditEmployee(employee)}
                                className="p-1 hover:bg-primary/10 rounded text-on-surface-variant hover:text-primary transition-colors cursor-pointer inline-flex items-center"
                                title="Chỉnh sửa nhân viên"
                              >
                                <span className="material-symbols-outlined text-base">edit</span>
                              </button>
                              <button
                                onClick={() => handleDeleteEmployee(employee.id, employee.tenNhanVien)}
                                className="p-1 hover:bg-error/10 rounded text-on-surface-variant hover:text-error transition-colors cursor-pointer inline-flex items-center"
                                title="Xóa nhân viên"
                              >
                                <span className="material-symbols-outlined text-base">delete</span>
                              </button>
                            </div>
                          </td>
                        </tr>
                      ))
                    )
                  )}
                </tbody>
              </table>
            </div>

            {/* Pagination Control */}
            <div className="flex-shrink-0">
              <Pagination
                currentPage={currentPage}
                totalPages={totalPages}
                onPageChange={setCurrentPage}
                totalItems={totalItems}
                itemsPerPage={itemsPerPage}
                onItemsPerPageChange={(size) => {
                  setItemsPerPage(size);
                  setCurrentPage(1);
                }}
              />
            </div>
          </div>

          {/* Add Partner Modal */}
          {isPartnerModalOpen && (
            <div className="fixed inset-0 z-50 flex items-center justify-center px-4 bg-background/85 backdrop-blur-md">
              <div className="absolute inset-0" onClick={() => setIsPartnerModalOpen(false)}></div>

              <div className="glass-card w-full max-w-2xl rounded-2xl p-8 relative z-50 animate-in fade-in zoom-in-95 duration-200">
                <button
                  className="absolute right-6 top-6 text-on-surface-variant hover:text-white cursor-pointer flex items-center justify-center"
                  onClick={() => setIsPartnerModalOpen(false)}
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
                      onClick={() => setIsPartnerModalOpen(false)}
                      className="flex-1 bg-white/5 border border-border-glass text-white font-bold py-3 rounded-xl hover:bg-white/10 transition-all cursor-pointer"
                    >
                      Hủy bỏ
                    </button>
                  </div>
                </form>
              </div>
            </div>
          )}

          {/* Add Employee Modal */}
          {isEmployeeModalOpen && (
            <div className="fixed inset-0 z-50 flex items-center justify-center px-4 bg-background/85 backdrop-blur-md">
              <div className="absolute inset-0" onClick={() => setIsEmployeeModalOpen(false)}></div>

              <div className="glass-card w-full max-w-2xl rounded-2xl p-8 relative z-50 animate-in fade-in zoom-in-95 duration-200">
                <button
                  className="absolute right-6 top-6 text-on-surface-variant hover:text-white cursor-pointer flex items-center justify-center"
                  onClick={() => setIsEmployeeModalOpen(false)}
                >
                  <span className="material-symbols-outlined">close</span>
                </button>
                <h3 className="text-xl font-bold text-primary mb-6">Thêm nhân viên mới</h3>

                {modalError && (
                  <div className="p-3 mb-4 text-xs font-semibold text-error bg-error/10 border border-error/20 rounded-xl">
                    ⚠️ {modalError}
                  </div>
                )}

                <form onSubmit={handleCreateEmployee} className="space-y-6">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div className="space-y-2">
                      <label className="text-xs font-bold uppercase tracking-widest text-on-surface-variant">Tên nhân viên</label>
                      <input
                        required
                        value={employeeName}
                        onChange={(e) => setEmployeeName(e.target.value)}
                        className="w-full bg-surface-lowest border border-border-glass rounded-lg px-4 py-3 text-white focus:outline-none focus:ring-2 focus:ring-primary/50 transition-all outline-none"
                        placeholder="Nhập họ và tên nhân viên..."
                        type="text"
                      />
                    </div>

                    <div className="space-y-2">
                      <label className="text-xs font-bold uppercase tracking-widest text-on-surface-variant">Vai trò / Chức vụ</label>
                      <select
                        value={employeeRole}
                        onChange={(e) => setEmployeeRole(e.target.value)}
                        className="w-full bg-[#131929] border border-border-glass rounded-lg px-4 py-3 text-white focus:outline-none focus:ring-2 focus:ring-primary/50 transition-all outline-none cursor-pointer"
                      >
                        <option value="Nhân viên">Nhân viên</option>
                        <option value="Quản lý">Quản lý</option>
                        <option value="Pha chế">Pha chế</option>
                        <option value="Thu ngân">Thu ngân</option>
                        <option value="Phục vụ">Phục vụ</option>
                      </select>
                    </div>

                    <div className="space-y-2">
                      <label className="text-xs font-bold uppercase tracking-widest text-on-surface-variant">Số điện thoại</label>
                      <input
                        value={employeePhone}
                        onChange={(e) => setEmployeePhone(e.target.value)}
                        className="w-full bg-surface-lowest border border-border-glass rounded-lg px-4 py-3 text-white focus:outline-none focus:ring-2 focus:ring-primary/50 transition-all outline-none"
                        placeholder="Ví dụ: 0912xxxxxx"
                        type="tel"
                      />
                    </div>

                    <div className="space-y-2">
                      <label className="text-xs font-bold uppercase tracking-widest text-on-surface-variant">Email</label>
                      <input
                        value={employeeEmail}
                        onChange={(e) => setEmployeeEmail(e.target.value)}
                        className="w-full bg-surface-lowest border border-border-glass rounded-lg px-4 py-3 text-white focus:outline-none focus:ring-2 focus:ring-primary/50 transition-all outline-none"
                        placeholder="example@cafe.com"
                        type="email"
                      />
                    </div>
                  </div>

                  <div className="flex space-x-4 pt-6">
                    <button
                      type="submit"
                      className="flex-1 bg-primary text-on-primary font-bold py-3 rounded-xl glow-teal hover:scale-[1.02] active:scale-95 transition-all cursor-pointer"
                    >
                      Lưu thông tin nhân viên
                    </button>
                    <button
                      type="button"
                      onClick={() => setIsEmployeeModalOpen(false)}
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

