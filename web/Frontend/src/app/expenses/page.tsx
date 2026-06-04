'use client';

import { Suspense, useState, useEffect } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';

interface ExpenseItem {
  id: string;
  name: string;
  category: 'Mặt bằng' | 'Quảng cáo' | 'Điện nước' | 'Lương nhân viên' | 'Phần mềm';
  date: string;
  amount: number;
  method: string;
  methodIcon: string;
  status: 'Đã chi' | 'Đang xử lý';
}

const INITIAL_EXPENSES: ExpenseItem[] = [
  { id: '#CP-8402', name: 'Chạy quảng cáo Facebook', category: 'Quảng cáo', date: '24/05/2026', amount: 4200000, method: 'Thẻ tín dụng ·· 4421', methodIcon: 'credit_card', status: 'Đã chi' },
  { id: '#CP-8401', name: 'Tiền thuê mặt bằng quán', category: 'Mặt bằng', date: '01/05/2026', amount: 15000000, method: 'Chuyển khoản ngân hàng', methodIcon: 'account_balance', status: 'Đã chi' },
  { id: '#CP-8399', name: 'Hóa đơn tiền điện tháng 5', category: 'Điện nước', date: '28/05/2026', amount: 3200000, method: 'Ví điện tử', methodIcon: 'payments', status: 'Đang xử lý' },
  { id: '#CP-8395', name: 'Gói cước Internet cáp quang', category: 'Điện nước', date: '20/05/2026', amount: 1200000, method: 'Thẻ tín dụng ·· 4421', methodIcon: 'credit_card', status: 'Đã chi' },
  { id: '#CP-8390', name: 'Phí bản quyền phần mềm quản lý', category: 'Phần mềm', date: '15/05/2026', amount: 2400000, method: 'Thẻ tín dụng ·· 4421', methodIcon: 'credit_card', status: 'Đã chi' },
  { id: '#CP-8388', name: 'Lương nhân viên tháng 5', category: 'Lương nhân viên', date: '30/05/2026', amount: 8200000, method: 'Chuyển khoản ngân hàng', methodIcon: 'account_balance', status: 'Đang xử lý' },
];

function ExpensesContent() {
  const searchParams = useSearchParams();
  const router = useRouter();

  const [isModalOpen, setIsModalOpen] = useState(false);
  const [activeTab, setActiveTab] = useState<string>('Tất cả');
  const [searchQuery, setSearchQuery] = useState('');

  // Form states
  const [title, setTitle] = useState('');
  const [category, setCategory] = useState<'Mặt bằng' | 'Quảng cáo' | 'Điện nước' | 'Lương nhân viên' | 'Phần mềm'>('Mặt bằng');
  const [date, setDate] = useState('');
  const [amount, setAmount] = useState<number>(0);
  const [paymentMethod, setPaymentMethod] = useState<string>('Tiền mặt');
  const [notes, setNotes] = useState('');

  // Data states
  const [expenses, setExpenses] = useState<ExpenseItem[]>([]);
  const [categories, setCategories] = useState<{ id: number; ten: string }[]>([]);
  const [loading, setLoading] = useState(true);

  interface ThuChiDbItem {
    id: number;
    thoiGian: string;
    idLoai: number | null;
    tenLoai: string | null;
    tienThu: number | null;
    tienChi: number | null;
    moTa: string | null;
    phuongThuc: string | null;
    trangThai: string | null;
  }

  const mapDbItemToExpense = (item: ThuChiDbItem): ExpenseItem => {
    const isIncome = item.tienThu && item.tienThu > 0;
    const amount = isIncome ? item.tienThu! : (item.tienChi || 0);
    
    let methodIcon = 'payments';
    const method = item.phuongThuc || 'Tiền mặt';
    if (method.includes('tín dụng') || method.includes('Thẻ')) methodIcon = 'credit_card';
    if (method.includes('khoản') || method.includes('Chuyển')) methodIcon = 'account_balance';
    if (method.includes('điện tử') || method.includes('Ví')) methodIcon = 'payments';

    let categoryName: 'Mặt bằng' | 'Quảng cáo' | 'Điện nước' | 'Lương nhân viên' | 'Phần mềm' = 'Mặt bằng';
    const dbCategory = item.tenLoai || '';
    const dbCategoryLower = dbCategory.toLowerCase();
    if (dbCategoryLower.includes('mặt bằng')) categoryName = 'Mặt bằng';
    else if (dbCategoryLower.includes('marketing') || dbCategoryLower.includes('quảng cáo')) categoryName = 'Quảng cáo';
    else if (dbCategoryLower.includes('điện') || dbCategoryLower.includes('nước')) categoryName = 'Điện nước';
    else if (dbCategoryLower.includes('lương') || dbCategoryLower.includes('nhân viên')) categoryName = 'Lương nhân viên';
    else if (dbCategoryLower.includes('phần mềm') || dbCategoryLower.includes('kỹ thuật') || dbCategoryLower.includes('setup')) categoryName = 'Phần mềm';

    let formattedDate = '';
    try {
      const dateObj = new Date(item.thoiGian);
      formattedDate = dateObj.toLocaleDateString('vi-VN');
    } catch (e) {
      formattedDate = item.thoiGian;
    }

    return {
      id: `#TC-${item.id}`,
      name: item.moTa || (isIncome ? 'Thu nhập khác' : 'Chi phí phát sinh'),
      category: categoryName,
      date: formattedDate,
      amount,
      method,
      methodIcon,
      status: item.trangThai === 'Đang xử lý' ? 'Đang xử lý' : 'Đã chi',
    };
  };

  const fetchExpenses = async () => {
    try {
      setLoading(true);
      const res = await fetch("http://localhost:8080/api/v1/thu-chi");
      if (res.ok) {
        const data: ThuChiDbItem[] = await res.json();
        setExpenses(data.map(mapDbItemToExpense));
      }
    } catch (error) {
      console.error("Lỗi khi tải nhật ký thu chi:", error);
    } finally {
      setLoading(false);
    }
  };

  const fetchCategories = async () => {
    try {
      const res = await fetch("http://localhost:8080/api/v1/metadata/loai-thu-chi");
      if (res.ok) {
        const data = await res.json();
        setCategories(data);
      }
    } catch (error) {
      console.error("Lỗi khi tải phân loại chi phí:", error);
    }
  };

  useEffect(() => {
    fetchExpenses();
    fetchCategories();
  }, []);

  // Open modal if query is log=true
  useEffect(() => {
    if (searchParams.get('log') === 'true') {
      setIsModalOpen(true);
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

  const closeModal = () => {
    setIsModalOpen(false);
    router.replace('/expenses');
  };

  const handleCreateExpense = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!title.trim() || amount <= 0) return;

    let categoryId: number | null = null;
    const findIdByName = (keywords: string[]) => {
      const found = categories.find(c => keywords.some(k => c.ten.toLowerCase().includes(k)));
      return found ? found.id : null;
    };

    if (category === 'Mặt bằng') categoryId = findIdByName(['mặt bằng', 'thuê']) || 6;
    else if (category === 'Quảng cáo') categoryId = findIdByName(['marketing', 'quảng cáo', 'chi phí marketing']) || 8;
    else if (category === 'Điện nước') categoryId = findIdByName(['điện', 'nước', 'tiện ích']) || 7;
    else if (category === 'Lương nhân viên') categoryId = findIdByName(['lương', 'nhân viên', 'trả lương']) || 5;
    else if (category === 'Phần mềm') categoryId = findIdByName(['phần mềm', 'kỹ thuật', 'setup']) || 10;

    const body = {
      tienChi: amount,
      tienThu: 0,
      moTa: title + (notes.trim() ? ` (${notes})` : ''),
      idLoai: categoryId,
      phuongThuc: paymentMethod,
      trangThai: 'Đã chi',
      thoiGian: date ? new Date(date).toISOString() : new Date().toISOString()
    };

    try {
      const res = await fetch("http://localhost:8080/api/v1/thu-chi", {
        method: "POST",
        headers: {
          "Content-Type": "application/json"
        },
        body: JSON.stringify(body)
      });
      
      if (res.ok) {
        fetchExpenses();
        setTitle('');
        setCategory('Mặt bằng');
        setDate('');
        setAmount(0);
        setPaymentMethod('Tiền mặt');
        setNotes('');
        closeModal();
      } else {
        alert("Có lỗi xảy ra khi ghi nhận chi phí!");
      }
    } catch (err) {
      console.error("Lỗi khi gửi yêu cầu:", err);
      alert("Không thể kết nối tới máy chủ Backend!");
    }
  };

  // Calculations
  const totalExpenses = expenses.reduce((sum, e) => sum + e.amount, 0);
  const rentExpenses = expenses.filter((e) => e.category === 'Mặt bằng').reduce((sum, e) => sum + e.amount, 0);
  const marketingExpenses = expenses.filter((e) => e.category === 'Quảng cáo').reduce((sum, e) => sum + e.amount, 0);
  const utilitiesExpenses = expenses.filter((e) => e.category === 'Điện nước').reduce((sum, e) => sum + e.amount, 0);
  const softwareExpenses = expenses.filter((e) => e.category === 'Phần mềm').reduce((sum, e) => sum + e.amount, 0);
  const payrollExpenses = expenses.filter((e) => e.category === 'Lương nhân viên').reduce((sum, e) => sum + e.amount, 0);

  const formatVND = (num: number) => {
    return num.toLocaleString('vi-VN') + ' ₫';
  };

  // Filter
  const filteredExpenses = expenses.filter((expense) => {
    const matchesTab = activeTab === 'Tất cả' || expense.category === activeTab;
    const matchesSearch =
      expense.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      expense.id.toLowerCase().includes(searchQuery.toLowerCase()) ||
      expense.category.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesTab && matchesSearch;
  });

  if (loading) {
    return <div className="p-8 text-center text-white text-sm">Đang tải dữ liệu chi phí thực tế từ máy chủ...</div>;
  }

  return (
    <div className="p-8 space-y-6 max-w-[1600px] mx-auto w-full relative">
      {/* Top Header Controls */}
      <div className="flex justify-between items-center">
        <div className="flex items-center gap-6">
          <div className="relative w-80">
            <span className="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-on-surface-variant text-xl">
              search
            </span>
            <input
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full bg-surface-glass border border-border-glass rounded-full py-2 pl-12 pr-4 text-sm focus:outline-none focus:ring-2 focus:ring-primary/50 transition-all text-white outline-none"
              placeholder="Tìm kiếm chi phí phát sinh..."
              type="text"
            />
          </div>
        </div>
        <button
          onClick={() => setIsModalOpen(true)}
          className="bg-primary text-on-primary font-bold px-6 py-2.5 rounded-full flex items-center gap-2 glow-teal active:scale-95 transition-all cursor-pointer"
        >
          <span className="material-symbols-outlined text-[20px]">add</span>
          Ghi nhận chi phí mới
        </button>
      </div>

      {/* Stats Row */}
      <section className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <div className="glass-card p-6 border-l-4 border-primary">
          <p className="text-on-surface-variant text-sm font-medium mb-1">Tổng chi phí trong tháng</p>
          <h3 className="text-2xl font-bold text-white">
            {formatVND(totalExpenses)}
          </h3>
          <div className="mt-3 text-xs text-on-surface-variant">
            Số liệu cập nhật tự động
          </div>
        </div>

        <div className="glass-card p-6">
          <p className="text-on-surface-variant text-sm font-medium mb-1">Mặt bằng kinh doanh</p>
          <h3 className="text-2xl font-bold text-white">
            {formatVND(rentExpenses)}
          </h3>
          <div className="mt-3 w-full bg-white/5 rounded-full h-1.5 overflow-hidden">
            <div className="bg-primary h-full rounded-full" style={{ width: '100%' }}></div>
          </div>
        </div>

        <div className="glass-card p-6">
          <p className="text-on-surface-variant text-sm font-medium mb-1">Quảng cáo &amp; Marketing</p>
          <h3 className="text-2xl font-bold text-white">
            {formatVND(marketingExpenses)}
          </h3>
          <div className="mt-3 w-full bg-white/5 rounded-full h-1.5 overflow-hidden">
            <div className="bg-secondary h-full rounded-full" style={{ width: '65%' }}></div>
          </div>
        </div>

        <div className="glass-card p-6">
          <p className="text-on-surface-variant text-sm font-medium mb-1">Điện nước, Lương &amp; Phần mềm</p>
          <h3 className="text-2xl font-bold text-white">
            {formatVND(utilitiesExpenses + softwareExpenses + payrollExpenses)}
          </h3>
          <div className="mt-3 w-full bg-white/5 rounded-full h-1.5 overflow-hidden">
            <div className="bg-tertiary h-full rounded-full" style={{ width: '42%' }}></div>
          </div>
        </div>
      </section>

      {/* Expense Log Table Container */}
      <section className="glass-card overflow-hidden">
        {/* Filter Bar */}
        <div className="px-6 py-5 border-b border-border-glass flex flex-wrap items-center justify-between gap-4 bg-white/5">
          <div className="flex items-center gap-4">
            <span className="text-white font-semibold">Nhật ký chi phí</span>
            <div className="h-6 w-[1px] bg-border-glass"></div>
            <div className="flex items-center gap-2 overflow-x-auto no-scrollbar">
              {['Tất cả', 'Mặt bằng', 'Quảng cáo', 'Điện nước', 'Lương nhân viên', 'Phần mềm'].map((tab) => (
                <button
                  key={tab}
                  onClick={() => setActiveTab(tab)}
                  className={`px-4 py-1.5 rounded-full text-xs font-medium transition-all cursor-pointer ${
                    activeTab === tab
                      ? 'bg-primary text-on-primary font-bold'
                      : 'bg-white/5 border border-border-glass text-on-surface-variant hover:border-primary'
                  }`}
                >
                  {tab}
                </button>
              ))}
            </div>
          </div>
        </div>

        {/* Table */}
        <div className="overflow-x-auto">
          <table className="w-full text-left">
            <thead className="bg-white/[0.02] text-on-surface-variant text-xs uppercase tracking-wider font-semibold border-b border-border-glass">
              <tr>
                <th className="px-6 py-4">Mã CP</th>
                <th className="px-6 py-4">Nội dung chi tiêu</th>
                <th className="px-6 py-4">Phân loại</th>
                <th className="px-6 py-4">Ngày chi</th>
                <th className="px-6 py-4 text-right">Số tiền</th>
                <th className="px-6 py-4">Phương thức</th>
                <th className="px-6 py-4">Trạng thái</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border-glass/50">
              {filteredExpenses.map((expense) => (
                <tr key={expense.id} className="hover:bg-white/5 transition-colors group">
                  <td className="px-6 py-4 text-xs text-on-surface-variant font-mono">{expense.id}</td>
                  <td className="px-6 py-4 text-white font-medium">{expense.name}</td>
                  <td className="px-6 py-4">
                    <span
                      className={`border px-3 py-1 rounded-full text-[10px] font-bold uppercase tracking-wider ${
                        expense.category === 'Quảng cáo'
                          ? 'bg-secondary/10 border-secondary/30 text-secondary'
                          : expense.category === 'Mặt bằng'
                          ? 'bg-primary/10 border-primary/30 text-primary'
                          : expense.category === 'Điện nước'
                          ? 'bg-tertiary/10 border-tertiary/30 text-tertiary'
                          : expense.category === 'Lương nhân viên'
                          ? 'bg-inverse-primary/10 border-inverse-primary/30 text-inverse-primary'
                          : 'bg-outline/10 border-outline/30 text-outline'
                      }`}
                    >
                      {expense.category}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-sm text-on-surface-variant">{expense.date}</td>
                  <td className="px-6 py-4 text-right font-bold text-white">
                    {formatVND(expense.amount)}
                  </td>
                  <td className="px-6 py-4">
                    <div className="flex items-center gap-2 text-sm text-on-surface-variant">
                      <span className="material-symbols-outlined text-base">{expense.methodIcon}</span>
                      {expense.method}
                    </div>
                  </td>
                  <td className="px-6 py-4">
                    <span
                      className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-[10px] font-bold uppercase tracking-wider border ${
                        expense.status === 'Đã chi'
                          ? 'bg-success/10 text-success border-success/40 glow-success'
                          : 'bg-warning/10 text-warning border-warning/40 glow-warning'
                      }`}
                    >
                      <span
                        className={`w-1.5 h-1.5 rounded-full ${expense.status === 'Đã chi' ? 'bg-success' : 'bg-warning'}`}
                      ></span>
                      {expense.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>

      {/* Log New Expense Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-background/80 backdrop-blur-md">
          {/* Backdrop */}
          <div className="absolute inset-0" onClick={closeModal}></div>

          {/* Modal Card */}
          <div className="relative w-full max-w-xl glass-card overflow-hidden shadow-2xl z-50 animate-in fade-in zoom-in-95 duration-200">
            <div className="px-8 py-6 border-b border-border-glass flex justify-between items-center bg-white/5">
              <h2 className="text-lg font-bold text-white flex items-center gap-3">
                <span className="material-symbols-outlined text-primary">add_circle</span>
                Ghi nhận chi phí phát sinh
              </h2>
              <button
                className="text-on-surface-variant hover:text-error transition-all cursor-pointer"
                onClick={closeModal}
              >
                <span className="material-symbols-outlined">close</span>
              </button>
            </div>
            <form onSubmit={handleCreateExpense}>
              <div className="p-8 space-y-6">
                <div className="grid grid-cols-2 gap-6">
                  <div className="col-span-2 space-y-2">
                    <label className="block text-xs font-bold uppercase tracking-wider text-on-surface-variant">Nội dung chi tiêu</label>
                    <input
                      required
                      value={title}
                      onChange={(e) => setTitle(e.target.value)}
                      className="w-full bg-surface-low border border-border-glass rounded-lg px-4 py-3 text-white focus:outline-none focus:border-primary transition-all outline-none"
                      placeholder="Ví dụ: Mua sắm nguyên liệu phụ, Đóng tiền nước..."
                      type="text"
                    />
                  </div>
                  <div className="space-y-2">
                    <label className="block text-xs font-bold uppercase tracking-wider text-on-surface-variant">Phân loại chi phí</label>
                    <select
                      value={category}
                      onChange={(e) => setCategory(e.target.value as any)}
                      className="w-full bg-surface-low border border-border-glass rounded-lg px-4 py-3 text-white focus:outline-none focus:border-primary transition-all appearance-none cursor-pointer outline-none"
                    >
                      <option value="Mặt bằng">Mặt bằng kinh doanh</option>
                      <option value="Quảng cáo">Quảng cáo &amp; Marketing</option>
                      <option value="Điện nước">Điện nước &amp; Tiện ích</option>
                      <option value="Lương nhân viên">Lương nhân viên</option>
                      <option value="Phần mềm">Phần mềm quản lý</option>
                    </select>
                  </div>
                  <div className="space-y-2">
                    <label className="block text-xs font-bold uppercase tracking-wider text-on-surface-variant">Ngày thực hiện chi</label>
                    <input
                      required
                      value={date}
                      onChange={(e) => setDate(e.target.value)}
                      className="w-full bg-surface-low border border-border-glass rounded-lg px-4 py-3 text-white focus:outline-none focus:border-primary transition-all outline-none cursor-pointer"
                      type="date"
                    />
                  </div>
                  <div className="space-y-2">
                    <label className="block text-xs font-bold uppercase tracking-wider text-on-surface-variant">Số tiền chi (VND)</label>
                    <input
                      required
                      value={amount}
                      onChange={(e) => setAmount(parseInt(e.target.value) || 0)}
                      className="w-full bg-surface-low border border-border-glass rounded-lg px-4 py-3 text-white focus:outline-none focus:border-primary transition-all outline-none"
                      placeholder="Nhập số tiền chi..."
                      type="number"
                      min="0"
                    />
                  </div>
                  <div className="space-y-2">
                    <label className="block text-xs font-bold uppercase tracking-wider text-on-surface-variant">Phương thức thanh toán</label>
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
                  <div className="col-span-2 space-y-2">
                    <label className="block text-xs font-bold uppercase tracking-wider text-on-surface-variant">Ghi chú thêm</label>
                    <textarea
                      value={notes}
                      onChange={(e) => setNotes(e.target.value)}
                      className="w-full bg-surface-low border border-border-glass rounded-lg px-4 py-3 text-white focus:outline-none focus:border-primary transition-all resize-none outline-none"
                      placeholder="Thông tin thêm (nhà cung cấp dịch vụ, mục đích chi)..."
                      rows={3}
                    ></textarea>
                  </div>
                </div>
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
                    Lưu ghi nhận chi
                  </button>
                </div>
              </div>
            </form>
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
