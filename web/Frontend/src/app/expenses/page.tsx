'use client';

import { Suspense, useState, useEffect } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';

import { ExpenseItem, ExpenseCategoryItem, EmployeeItem, ThuChiDbItem } from '@/types';
import { expenseService } from '@/services/expenseService';
import { partnerService } from '@/services/partnerService';
import { formatVND } from '@/lib/utils';

function ExpensesContent() {
  const searchParams = useSearchParams();
  const router = useRouter();

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [activeTab, setActiveTab] = useState<string>('Tất cả');
  const [searchQuery, setSearchQuery] = useState('');
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');
  const [viewingExpense, setViewingExpense] = useState<ExpenseItem | null>(null);
  const [isViewModalOpen, setIsViewModalOpen] = useState(false);

  // Form states
  const [editingExpense, setEditingExpense] = useState<ExpenseItem | null>(null);
  const [title, setTitle] = useState('');
  const [transactionType, setTransactionType] = useState<'THU' | 'CHI'>('CHI');
  const [formIdLoai, setFormIdLoai] = useState<string>('');
  const [formIdNhanVien, setFormIdNhanVien] = useState<string>('');
  const [date, setDate] = useState('');
  const [amount, setAmount] = useState<number>(0);
  const [paymentMethod, setPaymentMethod] = useState<string>('Tiền mặt');
  const [notes, setNotes] = useState('');
  const [formStatus, setFormStatus] = useState<string>('Đã chi');

  // Data states
  const [expenses, setExpenses] = useState<ExpenseItem[]>([]);
  const [categories, setCategories] = useState<ExpenseCategoryItem[]>([]);
  const [employees, setEmployees] = useState<EmployeeItem[]>([]);
  const [loading, setLoading] = useState(true);

  const mapDbItemToExpense = (item: ThuChiDbItem): ExpenseItem => {
    const tienThu = item.tienThu || 0;
    const tienChi = item.tienChi || 0;
    
    let methodIcon = 'payments';
    const method = item.phuongThuc || 'Tiền mặt';
    if (method.includes('tín dụng') || method.includes('Thẻ')) methodIcon = 'credit_card';
    if (method.includes('khoản') || method.includes('Chuyển')) methodIcon = 'account_balance';
    if (method.includes('điện tử') || method.includes('Ví')) methodIcon = 'payments';

    let formattedDate = '';
    let rawDate = '';
    try {
      const dateObj = new Date(item.thoiGian);
      formattedDate = dateObj.toLocaleDateString('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
      });
      // Format to YYYY-MM-DD for standard html date inputs
      const yyyy = dateObj.getFullYear();
      const mm = String(dateObj.getMonth() + 1).padStart(2, '0');
      const dd = String(dateObj.getDate()).padStart(2, '0');
      rawDate = `${yyyy}-${mm}-${dd}`;
    } catch (e) {
      formattedDate = item.thoiGian;
      rawDate = item.thoiGian ? item.thoiGian.split('T')[0] : '';
    }

    return {
      dbId: item.id,
      id: `#TC-${item.id}`,
      name: item.moTa || '',
      category: item.tenLoai || 'Chưa phân loại',
      idLoai: item.idLoai,
      date: formattedDate,
      rawDate,
      tienThu,
      tienChi,
      method,
      methodIcon,
      status: item.trangThai || 'Đã chi',
      idNhanVien: item.idNhanVien,
      tenNhanVien: item.tenNhanVien || 'Hệ thống',
    };
  };

  const fetchExpenses = async () => {
    try {
      setLoading(true);
      const data = await expenseService.getExpenses();
      setExpenses(data.map(mapDbItemToExpense));
    } catch (error) {
      console.error("Lỗi khi tải nhật ký thu chi:", error);
    } finally {
      setLoading(false);
    }
  };

  const fetchCategories = async () => {
    try {
      const data = await expenseService.getExpenseCategories();
      setCategories(data);
    } catch (error) {
      console.error("Lỗi khi tải phân loại chi phí:", error);
    }
  };

  const fetchEmployees = async () => {
    try {
      const data = await partnerService.getEmployees();
      setEmployees(data as any);
    } catch (error) {
      console.error("Lỗi khi tải danh sách nhân viên:", error);
    }
  };

  useEffect(() => {
    fetchExpenses();
    fetchCategories();
    fetchEmployees();
  }, []);

  // Open modal if query is log=true
  useEffect(() => {
    if (searchParams.get('log') === 'true') {
      openCreateModal();
    }
  }, [searchParams]);

  // Handle escape key
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        closeModal();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  const resetForm = () => {
    setTitle('');
    setTransactionType('CHI');
    setFormIdLoai('');
    setFormIdNhanVien('');
    setDate(new Date().toISOString().split('T')[0]);
    setAmount(0);
    setPaymentMethod('Tiền mặt');
    setNotes('');
    setFormStatus('Đã chi');
    setEditingExpense(null);
  };

  const openCreateModal = () => {
    resetForm();
    setIsModalOpen(true);
  };

  const openEditModal = (expense: ExpenseItem) => {
    setEditingExpense(expense);
    setTransactionType(expense.tienThu > 0 ? 'THU' : 'CHI');
    
    // Check if moTa has a notes suffix in parentheses
    let parsedTitle = expense.name;
    let parsedNotes = '';
    const match = expense.name.match(/(.*)\s*\(([^)]+)\)$/);
    if (match) {
      parsedTitle = match[1].trim();
      parsedNotes = match[2].trim();
    }

    setTitle(parsedTitle);
    setNotes(parsedNotes);
    setFormIdLoai(expense.idLoai ? expense.idLoai.toString() : '');
    setFormIdNhanVien(expense.idNhanVien ? expense.idNhanVien.toString() : '');
    setDate(expense.rawDate);
    setAmount(expense.tienThu > 0 ? expense.tienThu : expense.tienChi);
    setPaymentMethod(expense.method);
    setFormStatus(expense.status);
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
    resetForm();
    router.replace('/expenses');
  };

  const handleSaveTransaction = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim() || amount <= 0) return;

    const tienThuVal = transactionType === 'THU' ? amount : 0;
    const tienChiVal = transactionType === 'CHI' ? amount : 0;

    const body = {
      tienThu: tienThuVal,
      tienChi: tienChiVal,
      moTa: title.trim() + (notes.trim() ? ` (${notes.trim()})` : ''),
      idLoai: formIdLoai ? parseInt(formIdLoai) : null,
      idNhanVien: formIdNhanVien ? parseInt(formIdNhanVien) : null,
      phuongThuc: paymentMethod,
      trangThai: formStatus,
      thoiGian: date ? new Date(date).toISOString() : new Date().toISOString()
    };

    try {
      if (editingExpense) {
        await expenseService.updateExpense(editingExpense.dbId, body);
      } else {
        await expenseService.createExpense(body);
      }
      fetchExpenses();
      closeModal();
    } catch (err) {
      console.error("Lỗi khi gửi yêu cầu:", err);
      alert("Có lỗi xảy ra hoặc không thể kết nối tới máy chủ Backend!");
    }
  };

  const handleDeleteExpense = async (dbId: number) => {
    if (!confirm("Bạn có chắc chắn muốn xóa giao dịch này?")) return;
    try {
      await expenseService.deleteExpense(dbId);
      fetchExpenses();
    } catch (err) {
      console.error("Lỗi khi xóa giao dịch:", err);
      alert("Có lỗi xảy ra hoặc không thể kết nối tới máy chủ Backend!");
    }
  };

  // Filter
  const filteredExpenses = expenses.filter((expense) => {
    const matchesTab = activeTab === 'Tất cả' || expense.category === activeTab;
    const matchesSearch =
      expense.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      expense.id.toLowerCase().includes(searchQuery.toLowerCase()) ||
      expense.category.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (expense.tenNhanVien && expense.tenNhanVien.toLowerCase().includes(searchQuery.toLowerCase()));
    if (!matchesTab || !matchesSearch) return false;
    if (fromDate && expense.rawDate < fromDate) return false;
    if (toDate && expense.rawDate > toDate) return false;
    return true;
  });

  const dateFilteredExpenses = expenses.filter((expense) => {
    if (fromDate && expense.rawDate < fromDate) return false;
    if (toDate && expense.rawDate > toDate) return false;
    return true;
  });

  // Calculations
  const totalIncome = dateFilteredExpenses.reduce((sum, e) => sum + e.tienThu, 0);
  const totalExpenses = dateFilteredExpenses.reduce((sum, e) => sum + e.tienChi, 0);
  const netProfit = totalIncome - totalExpenses;
  const transactionCount = dateFilteredExpenses.length;

  if (loading) {
    return <div className="p-8 text-center text-white text-sm">Đang tải dữ liệu thu chi thực tế từ máy chủ...</div>;
  }

  // Generate dynamic category tabs
  const tabOptions = ['Tất cả', ...Array.from(new Set(expenses.map(e => e.category)))];

  return (
    <div className="h-[calc(100vh-16px)] overflow-hidden flex flex-col pt-2 pb-2 px-4 space-y-3 w-full relative">
      {/* Top Header Controls */}
      <div className="flex justify-between items-center flex-shrink-0">
        <div>
          <h2 className="text-xl font-bold text-white tracking-wide">Nhật ký Thu Chi</h2>
          <p className="text-[10px] text-on-surface-variant mt-0.5">Theo dõi lịch sử thu chi thực tế, doanh thu và dòng tiền của cửa hàng.</p>
        </div>
        <button
          onClick={openCreateModal}
          className="bg-primary text-on-primary px-5 py-2.5 rounded-lg font-semibold flex items-center gap-2 glow-button transition-all active:scale-95 cursor-pointer text-xs animate-in fade-in"
        >
          <span className="material-symbols-outlined text-base">add</span>
          <span>Ghi nhận giao dịch mới</span>
        </button>
      </div>

      {/* Stats Row */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 flex-shrink-0 animate-in fade-in">
        <div className="glass-card py-2.5 px-4 rounded-xl flex items-center justify-between border-l-4 border-emerald-500 hover:border-emerald-400 transition-all">
          <div className="flex items-center gap-3">
            <span className="material-symbols-outlined text-emerald-400 bg-emerald-500/10 p-2 rounded-lg text-lg">payments</span>
            <div>
              <p className="text-on-surface-variant text-[10px] uppercase tracking-wider mb-0.5">Tổng thu trong tháng</p>
              <h3 className="text-sm font-bold text-emerald-400">{formatVND(totalIncome)}</h3>
            </div>
          </div>
        </div>

        <div className="glass-card py-2.5 px-4 rounded-xl flex items-center justify-between border-l-4 border-rose-500 hover:border-rose-400 transition-all">
          <div className="flex items-center gap-3">
            <span className="material-symbols-outlined text-rose-400 bg-rose-500/10 p-2 rounded-lg text-lg">payments</span>
            <div>
              <p className="text-on-surface-variant text-[10px] uppercase tracking-wider mb-0.5">Tổng chi trong tháng</p>
              <h3 className="text-sm font-bold text-rose-400">{formatVND(totalExpenses)}</h3>
            </div>
          </div>
        </div>

        <div className="glass-card py-2.5 px-4 rounded-xl flex items-center justify-between border-l-4 border-primary hover:border-primary/80 transition-all">
          <div className="flex items-center gap-3">
            <span className={`material-symbols-outlined p-2 rounded-lg text-lg ${netProfit >= 0 ? 'text-emerald-400 bg-emerald-500/10' : 'text-rose-400 bg-rose-500/10'}`}>trending_up</span>
            <div>
              <p className="text-on-surface-variant text-[10px] uppercase tracking-wider mb-0.5">Lợi nhuận ròng</p>
              <h3 className={`text-sm font-bold ${netProfit >= 0 ? 'text-emerald-400' : 'text-rose-400'}`}>{formatVND(netProfit)}</h3>
            </div>
          </div>
        </div>

        <div className="glass-card py-2.5 px-4 rounded-xl flex items-center justify-between border-l-4 border-warning hover:border-warning/80 transition-all">
          <div className="flex items-center gap-3">
            <span className="material-symbols-outlined text-warning bg-warning/10 p-2 rounded-lg text-lg">history</span>
            <div>
              <p className="text-on-surface-variant text-[10px] uppercase tracking-wider mb-0.5">Tổng số giao dịch</p>
              <h3 className="text-sm font-bold text-warning">{transactionCount} Giao dịch</h3>
            </div>
          </div>
        </div>
      </div>

      {/* Expense Log Table Container */}
      <div className="flex-1 flex flex-col min-h-0 glass-card rounded-xl overflow-hidden border border-white/5 bg-white/1 mt-1">
        {/* Filter Bar */}
        <div className="p-3 border-b border-border-glass flex flex-wrap items-center justify-between gap-3 flex-shrink-0 bg-white/1">
          <div className="flex items-center gap-3 flex-wrap">
            <span className="text-white font-semibold text-xs">Nhật ký Thu Chi</span>
            <div className="h-4 w-[1px] bg-border-glass"></div>
            <div className="flex items-center gap-1.5 overflow-x-auto no-scrollbar">
              {tabOptions.map((tab) => (
                <button
                  key={tab}
                  onClick={() => setActiveTab(tab)}
                  className={`px-3 py-1 rounded-md text-[10px] font-semibold uppercase tracking-wider transition-all cursor-pointer whitespace-nowrap ${
                    activeTab === tab
                      ? 'bg-primary text-on-primary font-bold'
                      : 'bg-white/5 border border-border-glass text-on-surface-variant hover:text-white'
                  }`}
                >
                  {tab}
                </button>
              ))}
            </div>
          </div>
          <div className="flex items-center gap-3 flex-wrap">
            <div className="relative w-56">
              <span className="material-symbols-outlined absolute left-2.5 top-1/2 -translate-y-1/2 text-xs text-on-surface-variant opacity-60">
                search
              </span>
              <input
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full bg-surface-lowest border border-border-glass rounded-lg py-1.5 pl-8 pr-3 text-xs focus:ring-1 focus:ring-primary/50 transition-all text-white outline-none"
                placeholder="Tìm tên, danh mục, nhân viên..."
                type="text"
              />
            </div>

            {/* Dates */}
            <div className="flex items-center gap-2">
              <input
                type="date"
                value={fromDate}
                onChange={(e) => setFromDate(e.target.value)}
                className="bg-surface-lowest border border-border-glass rounded-lg px-2 py-1 text-xs text-white outline-none cursor-pointer"
              />
              <span className="text-xs text-text-variant">đến</span>
              <input
                type="date"
                value={toDate}
                onChange={(e) => setToDate(e.target.value)}
                className="bg-surface-lowest border border-border-glass rounded-lg px-2 py-1 text-xs text-white outline-none cursor-pointer"
              />
            </div>

            <span className="text-xs text-text-variant">Tìm thấy {filteredExpenses.length} giao dịch</span>
          </div>
        </div>

        {/* Table */}
        <div className="flex-1 overflow-auto">
          <table className="w-full text-left border-collapse">
            <thead className="sticky top-0 z-10 bg-[#131929] shadow-[0_1px_0_0_rgba(255,255,255,0.08)]">
              <tr className="text-on-surface-variant text-[10px] uppercase tracking-wider font-bold border-b border-border-glass bg-[#131929]">
                <th className="px-4 py-3">Ngày</th>
                <th className="px-4 py-3">Danh mục</th>
                <th className="px-4 py-3 text-right">Thu</th>
                <th className="px-4 py-3 text-right">Chi</th>
                <th className="px-4 py-3 text-right">Lợi nhuận</th>
                <th className="px-4 py-3">Nhân viên</th>
                <th className="px-4 py-3">Mô tả</th>
                <th className="px-4 py-3 text-center">Thao tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border-glass/50">
              {filteredExpenses.map((expense) => {
                const profit = expense.tienThu - expense.tienChi;
                return (
                  <tr 
                    key={expense.id} 
                    onDoubleClick={() => {
                      setViewingExpense(expense);
                      setIsViewModalOpen(true);
                    }}
                    className="hover:bg-white/5 transition-colors group cursor-pointer select-none"
                    title="Nhấp đúp chuột để xem chi tiết"
                  >
                    <td className="px-4 py-2.5 text-xs text-on-surface-variant whitespace-nowrap">{expense.date}</td>
                    <td className="px-4 py-2.5">
                      <span className="bg-primary/10 border border-primary/30 text-primary px-2 py-0.5 rounded-full text-[9px] font-bold uppercase tracking-wider whitespace-nowrap">
                        {expense.category}
                      </span>
                    </td>
                    <td className="px-4 py-2.5 text-right font-semibold text-emerald-400 text-xs font-mono">
                      {expense.tienThu > 0 ? formatVND(expense.tienThu) : '—'}
                    </td>
                    <td className="px-4 py-2.5 text-right font-semibold text-rose-400 text-xs font-mono">
                      {expense.tienChi > 0 ? formatVND(expense.tienChi) : '—'}
                    </td>
                    <td className={`px-4 py-2.5 text-right font-bold text-xs font-mono ${profit >= 0 ? 'text-emerald-400' : 'text-rose-400'}`}>
                      {profit > 0 ? '+' : ''}{formatVND(profit)}
                    </td>
                    <td className="px-4 py-2.5 text-xs text-on-surface-variant font-medium whitespace-nowrap">
                      {expense.tenNhanVien}
                    </td>
                    <td className="px-4 py-2.5 text-white text-xs max-w-[280px] truncate" title={expense.name}>
                      {expense.name}
                      {expense.status !== 'Đã chi' && expense.status !== 'Hoàn thành' && (
                        <span className="ml-2 text-warning text-xs font-semibold">({expense.status})</span>
                      )}
                    </td>
                    <td className="px-4 py-2.5 text-center">
                      <div className="flex items-center justify-center gap-1.5">
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            openEditModal(expense);
                          }}
                          className="p-1 hover:bg-white/5 rounded text-on-surface-variant hover:text-primary transition-colors cursor-pointer inline-flex items-center"
                          title="Sửa giao dịch"
                        >
                          <span className="material-symbols-outlined text-lg">edit</span>
                        </button>
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            handleDeleteExpense(expense.dbId);
                          }}
                          className="p-1 hover:bg-white/5 rounded text-on-surface-variant hover:text-error transition-colors cursor-pointer inline-flex items-center"
                          title="Xóa giao dịch"
                        >
                          <span className="material-symbols-outlined text-lg">delete</span>
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>

      {/* Log/Edit Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-background/80 backdrop-blur-md">
          {/* Backdrop */}
          <div className="absolute inset-0" onClick={closeModal}></div>

          {/* Modal Card */}
          <div className="relative w-full max-w-xl glass-card overflow-hidden shadow-2xl z-50 animate-in fade-in zoom-in-95 duration-200">
            <div className="px-8 py-6 border-b border-border-glass flex justify-between items-center bg-white/5">
              <h2 className="text-lg font-bold text-white flex items-center gap-3">
                <span className="material-symbols-outlined text-primary">
                  {editingExpense ? 'edit_document' : 'add_circle'}
                </span>
                {editingExpense ? 'Cập nhật giao dịch' : 'Ghi nhận giao dịch mới'}
              </h2>
              <button
                className="text-on-surface-variant hover:text-error transition-all cursor-pointer"
                onClick={closeModal}
              >
                <span className="material-symbols-outlined">close</span>
              </button>
            </div>
            <form onSubmit={handleSaveTransaction}>
              <div className="p-8 space-y-6">
                <div className="grid grid-cols-2 gap-6">
                  {/* Transaction Type Choice */}
                  <div className="col-span-2 space-y-2">
                    <label className="block text-xs font-bold uppercase tracking-wider text-on-surface-variant">Loại giao dịch</label>
                    <div className="grid grid-cols-2 gap-4">
                      <button
                        type="button"
                        onClick={() => setTransactionType('CHI')}
                        className={`py-3 rounded-lg text-sm font-bold transition-all border flex items-center justify-center gap-2 cursor-pointer ${
                          transactionType === 'CHI'
                            ? 'bg-rose-500/20 border-rose-500 text-rose-400 shadow-md shadow-rose-950/20'
                            : 'bg-surface-low border-border-glass text-on-surface-variant hover:border-white/20'
                        }`}
                      >
                        <span className="material-symbols-outlined text-base">remove_circle</span>
                        Khoản Chi
                      </button>
                      <button
                        type="button"
                        onClick={() => setTransactionType('THU')}
                        className={`py-3 rounded-lg text-sm font-bold transition-all border flex items-center justify-center gap-2 cursor-pointer ${
                          transactionType === 'THU'
                            ? 'bg-emerald-500/20 border-emerald-500 text-emerald-400 shadow-md shadow-emerald-950/20'
                            : 'bg-surface-low border-border-glass text-on-surface-variant hover:border-white/20'
                        }`}
                      >
                        <span className="material-symbols-outlined text-base">add_circle</span>
                        Khoản Thu
                      </button>
                    </div>
                  </div>

                  {/* Nội dung chi tiêu */}
                  <div className="col-span-2 space-y-2">
                    <label className="block text-xs font-bold uppercase tracking-wider text-on-surface-variant">Nội dung / Mô tả</label>
                    <input
                      required
                      value={title}
                      onChange={(e) => setTitle(e.target.value)}
                      className="w-full bg-surface-low border border-border-glass rounded-lg px-4 py-3 text-white focus:outline-none focus:border-primary transition-all outline-none"
                      placeholder={transactionType === 'CHI' ? "Ví dụ: Mua sắm nguyên liệu phụ, Đóng tiền nước..." : "Ví dụ: Doanh thu bán hàng ngoài giờ, Hoàn tiền đặt cọc..."}
                      type="text"
                    />
                  </div>

                  {/* Phân loại chi phí */}
                  <div className="space-y-2">
                    <label className="block text-xs font-bold uppercase tracking-wider text-on-surface-variant">Danh mục</label>
                    <select
                      value={formIdLoai}
                      onChange={(e) => setFormIdLoai(e.target.value)}
                      className="w-full bg-surface-low border border-border-glass rounded-lg px-4 py-3 text-white focus:outline-none focus:border-primary transition-all appearance-none cursor-pointer outline-none"
                    >
                      <option value="">-- Chọn danh mục --</option>
                      {categories.map((c) => (
                        <option key={c.id} value={c.id.toString()}>{c.ten}</option>
                      ))}
                    </select>
                  </div>

                  {/* Ngày thực hiện */}
                  <div className="space-y-2">
                    <label className="block text-xs font-bold uppercase tracking-wider text-on-surface-variant">Ngày thực hiện</label>
                    <input
                      required
                      value={date}
                      onChange={(e) => setDate(e.target.value)}
                      className="w-full bg-surface-low border border-border-glass rounded-lg px-4 py-3 text-white focus:outline-none focus:border-primary transition-all outline-none cursor-pointer"
                      type="date"
                    />
                  </div>

                  {/* Số tiền */}
                  <div className="space-y-2">
                    <label className="block text-xs font-bold uppercase tracking-wider text-on-surface-variant">Số tiền (VND)</label>
                    <input
                      required
                      value={amount || ''}
                      onChange={(e) => setAmount(parseInt(e.target.value) || 0)}
                      className="w-full bg-surface-low border border-border-glass rounded-lg px-4 py-3 text-white focus:outline-none focus:border-primary transition-all outline-none font-mono"
                      placeholder="Nhập số tiền..."
                      type="number"
                      min="1"
                    />
                  </div>

                  {/* Nhân viên */}
                  <div className="space-y-2">
                    <label className="block text-xs font-bold uppercase tracking-wider text-on-surface-variant">Nhân viên thực hiện</label>
                    <select
                      value={formIdNhanVien}
                      onChange={(e) => setFormIdNhanVien(e.target.value)}
                      className="w-full bg-surface-low border border-border-glass rounded-lg px-4 py-3 text-white focus:outline-none focus:border-primary transition-all appearance-none cursor-pointer outline-none"
                    >
                      <option value="">-- Hệ thống / Tự động --</option>
                      {employees.map((emp) => (
                        <option key={emp.id} value={emp.id.toString()}>{emp.tenNhanVien} ({emp.vaiTro})</option>
                      ))}
                    </select>
                  </div>

                  {/* Phương thức thanh toán */}
                  <div className="space-y-2">
                    <label className="block text-xs font-bold uppercase tracking-wider text-on-surface-variant">Phương thức</label>
                    <select
                      value={paymentMethod}
                      onChange={(e) => setPaymentMethod(e.target.value)}
                      className="w-full bg-surface-low border border-border-glass rounded-lg px-4 py-3 text-white focus:outline-none focus:border-primary transition-all appearance-none cursor-pointer outline-none"
                    >
                      <option value="Tiền mặt">Tiền mặt</option>
                      <option value="Chuyển khoản ngân hàng">Chuyển khoản</option>
                      <option value="Thẻ tín dụng">Thẻ Visa/Master</option>
                      <option value="Ví điện tử">Ví điện tử MoMo/ZaloPay</option>
                    </select>
                  </div>

                  {/* Trạng thái */}
                  <div className="space-y-2">
                    <label className="block text-xs font-bold uppercase tracking-wider text-on-surface-variant">Trạng thái</label>
                    <select
                      value={formStatus}
                      onChange={(e) => setFormStatus(e.target.value)}
                      className="w-full bg-surface-low border border-border-glass rounded-lg px-4 py-3 text-white focus:outline-none focus:border-primary transition-all appearance-none cursor-pointer outline-none"
                    >
                      <option value="Đã chi">Đã chi</option>
                      <option value="Đang xử lý">Đang xử lý</option>
                      <option value="Hoàn thành">Hoàn thành</option>
                    </select>
                  </div>

                  {/* Ghi chú thêm */}
                  <div className="col-span-2 space-y-2">
                    <label className="block text-xs font-bold uppercase tracking-wider text-on-surface-variant">Ghi chú thêm</label>
                    <textarea
                      value={notes}
                      onChange={(e) => setNotes(e.target.value)}
                      className="w-full bg-surface-low border border-border-glass rounded-lg px-4 py-3 text-white focus:outline-none focus:border-primary transition-all resize-none outline-none"
                      placeholder="Thông tin thêm (nhà cung cấp dịch vụ, mục đích chi)..."
                      rows={2}
                    ></textarea>
                  </div>
                </div>
                
                {/* Submit buttons */}
                <div className="flex items-center justify-end gap-4 pt-4 border-t border-border-glass">
                  <button
                    type="button"
                    onClick={closeModal}
                    className="px-6 py-2.5 rounded-full text-on-surface-variant font-bold hover:bg-white/5 transition-all cursor-pointer"
                  >
                    Hủy bỏ
                  </button>
                  <button
                    type="submit"
                    className="bg-primary text-on-primary font-bold px-10 py-2.5 rounded-full glow-teal active:scale-95 transition-transform cursor-pointer"
                  >
                    {editingExpense ? 'Lưu cập nhật' : 'Ghi nhận giao dịch'}
                  </button>
                </div>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* View Detail Modal */}
      {isViewModalOpen && viewingExpense && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-background/80 backdrop-blur-md">
          {/* Backdrop */}
          <div className="absolute inset-0" onClick={() => setIsViewModalOpen(false)}></div>

          {/* Modal Card */}
          <div className="relative w-full max-w-lg glass-card overflow-hidden shadow-2xl z-50 animate-in fade-in zoom-in-95 duration-200">
            <div className="px-8 py-6 border-b border-border-glass flex justify-between items-center bg-white/5">
              <h2 className="text-lg font-bold text-white flex items-center gap-3">
                <span className="material-symbols-outlined text-primary">visibility</span>
                Chi tiết giao dịch {viewingExpense.id}
              </h2>
              <button
                className="text-on-surface-variant hover:text-error transition-all cursor-pointer"
                onClick={() => setIsViewModalOpen(false)}
              >
                <span className="material-symbols-outlined">close</span>
              </button>
            </div>
            
            <div className="p-8 space-y-6">
              <div className="grid grid-cols-2 gap-y-4 gap-x-6 text-sm">
                <div>
                  <p className="text-xs text-on-surface-variant uppercase tracking-wider font-bold">Loại giao dịch</p>
                  <div className="mt-1 font-semibold">
                    {viewingExpense.tienThu > 0 ? (
                      <span className="text-emerald-400 flex items-center gap-1.5">
                        <span className="material-symbols-outlined text-base">add_circle</span>
                        Khoản Thu (Doanh thu)
                      </span>
                    ) : (
                      <span className="text-rose-400 flex items-center gap-1.5">
                        <span className="material-symbols-outlined text-base">remove_circle</span>
                        Khoản Chi (Chi phí)
                      </span>
                    )}
                  </div>
                </div>
                <div>
                  <p className="text-xs text-on-surface-variant uppercase tracking-wider font-bold">Ngày thực hiện</p>
                  <p className="mt-1 text-white font-medium">{viewingExpense.date}</p>
                </div>

                <div className="border-t border-white/5 pt-4 col-span-2 grid grid-cols-2 gap-y-4 gap-x-6">
                  <div>
                    <p className="text-xs text-on-surface-variant uppercase tracking-wider font-bold">Danh mục</p>
                    <p className="mt-1">
                      <span className="bg-primary/10 border border-primary/30 text-primary px-3 py-0.5 rounded-full text-xs font-bold uppercase">
                        {viewingExpense.category}
                      </span>
                    </p>
                  </div>
                  <div>
                    <p className="text-xs text-on-surface-variant uppercase tracking-wider font-bold">Số tiền</p>
                    <p className="mt-1 text-white font-bold text-base">
                      {formatVND(viewingExpense.tienThu > 0 ? viewingExpense.tienThu : viewingExpense.tienChi)}
                    </p>
                  </div>

                  <div>
                    <p className="text-xs text-on-surface-variant uppercase tracking-wider font-bold">Nhân viên thực hiện</p>
                    <p className="mt-1 text-white font-medium">{viewingExpense.tenNhanVien}</p>
                  </div>
                  <div>
                    <p className="text-xs text-on-surface-variant uppercase tracking-wider font-bold">Phương thức</p>
                    <p className="mt-1 text-white font-medium flex items-center gap-1.5">
                      <span className="material-symbols-outlined text-base">{viewingExpense.methodIcon}</span>
                      {viewingExpense.method}
                    </p>
                  </div>

                  <div>
                    <p className="text-xs text-on-surface-variant uppercase tracking-wider font-bold">Trạng thái</p>
                    <p className="mt-1">
                      <span className="inline-flex items-center gap-1.5 px-3 py-0.5 rounded-full text-[10px] font-bold uppercase tracking-wider border bg-success/10 text-success border-success/40">
                        {viewingExpense.status}
                      </span>
                    </p>
                  </div>
                </div>

                <div className="border-t border-white/5 pt-4 col-span-2">
                  <p className="text-xs text-on-surface-variant uppercase tracking-wider font-bold">Nội dung mô tả</p>
                  <p className="mt-1 text-white font-medium leading-relaxed bg-white/5 p-3 rounded-lg border border-white/5">
                    {viewingExpense.name}
                  </p>
                </div>
              </div>

              <div className="flex items-center justify-end pt-4 border-t border-border-glass">
                <button
                  type="button"
                  onClick={() => setIsViewModalOpen(false)}
                  className="bg-primary text-on-primary font-bold px-8 py-2 rounded-full glow-teal active:scale-95 transition-transform cursor-pointer text-sm"
                >
                  Đóng
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default function ExpensesPage() {
  return (
    <Suspense fallback={<div className="p-8 text-center text-white text-sm">Đang tải dữ liệu chi phí...</div>}>
      <ExpensesContent />
    </Suspense>
  );
}
