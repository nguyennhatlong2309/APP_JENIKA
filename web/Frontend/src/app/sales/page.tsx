'use client';

import { Suspense, useState, useEffect } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';

interface ProductItem {
  id: number;
  tenSanPham: string;
  giaBanHienTai: number;
  giaNhapHienTai: number;
  soLuongTon: number;
  donViTinh?: { tenDonVi: string } | null;
}

interface PartnerItem {
  id: number;
  ten: string;
  sdt?: string;
  diaChi?: string;
  email?: string;
}

interface EmployeeItem {
  id: number;
  tenNhanVien: string;
  sdt?: string;
  email?: string;
}

interface SalesOrderDetail {
  id?: number;
  sanPham: { id: number; tenSanPham?: string };
  soLuong: number;
  donVi?: string;
  giaBan?: number;
  thanhTien?: number;
  isGift: boolean;
}

interface OrderItem {
  id: number;
  thoiGian: string;
  ngayLap?: string;
  doiTac?: PartnerItem | null;
  nhanVien?: EmployeeItem | null;
  tongTien: number;
  tienDaThanhToan: number;
  tienNo: number;
  trangThai: string;
  ghiChu?: string;
  diaChiGiaoHang?: string | null;
  chiTietBanHangs?: SalesOrderDetail[];
}

interface ProductSaleItem {
  sanPham: string;
  phanLoai: 'Bán' | 'Tặng';
  maHD: string;
  ngayBan: string;
  giaNhap: number;
  giaBan: number;
  soLuong: number;
  loiNhuan: number;
  khachHang: string;
}

interface Tab1Stats {
  completedRevenue: number;
  scheduledDeposit: number;
  scheduledCount: number;
  unpaidCompletedCount: number;
}

interface Tab2Stats {
  prodRevenue: number;
  prodProfit: number;
  qtySold: number;
  qtyGifted: number;
}

function SalesContent() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const [activeTab, setActiveTab] = useState(0);
  const [isPanelOpen, setIsPanelOpen] = useState(false);
  const [isPrintOpen, setIsPrintOpen] = useState(false);
  const [isViewOpen, setIsViewOpen] = useState(false);
  const [selectedPrintOrder, setSelectedPrintOrder] = useState<OrderItem | null>(null);
  const [selectedViewOrder, setSelectedViewOrder] = useState<OrderItem | null>(null);
  const [editingOrder, setEditingOrder] = useState<OrderItem | null>(null);
  const [availableProducts, setAvailableProducts] = useState<ProductItem[]>([]);
  const [partners, setPartners] = useState<PartnerItem[]>([]);
  const [employees, setEmployees] = useState<EmployeeItem[]>([]);

  // Filter States - Tab 1
  const [searchInvoice, setSearchInvoice] = useState('');
  const [statusFilter, setStatusFilter] = useState('All'); // All, Hoàn thành, Hẹn, Hủy
  const [fromDateInvoice, setFromDateInvoice] = useState('');
  const [toDateInvoice, setToDateInvoice] = useState('');

  // Filter States - Tab 2
  const [searchProduct, setSearchProduct] = useState('');
  const [classFilter, setClassFilter] = useState('All'); // All, Bán, Tặng
  const [fromDateProduct, setFromDateProduct] = useState('');
  const [toDateProduct, setToDateProduct] = useState('');

  // Dynamic Statistics States
  const [tab1Stats, setTab1Stats] = useState<Tab1Stats>({
    completedRevenue: 0,
    scheduledDeposit: 0,
    scheduledCount: 0,
    unpaidCompletedCount: 0
  });

  const [tab2Stats, setTab2Stats] = useState<Tab2Stats>({
    prodRevenue: 0,
    prodProfit: 0,
    qtySold: 0,
    qtyGifted: 0
  });

  // Core Data States
  const [orders, setOrders] = useState<OrderItem[]>([]);
  const [productSales, setProductSales] = useState<ProductSaleItem[]>([]);
  const [loading, setLoading] = useState(true);

  // Filtered lists calculated client-side to prevent lagging on search input keypresses
  const filteredOrders = orders.filter(item => {
    if (statusFilter !== 'All' && item.trangThai !== statusFilter) return false;
    if (searchInvoice) {
      const query = searchInvoice.toLowerCase();
      const khach = item.doiTac ? item.doiTac.ten.toLowerCase() : 'khách vãng lai';
      const code = `bh-${item.id}`;
      if (!khach.includes(query) && !code.includes(query)) return false;
    }
    if (fromDateInvoice) {
      const itemDate = new Date(item.thoiGian);
      const from = new Date(fromDateInvoice);
      if (itemDate < from) return false;
    }
    if (toDateInvoice) {
      const itemDate = new Date(item.thoiGian);
      const to = new Date(toDateInvoice);
      to.setHours(23, 59, 59, 999);
      if (itemDate > to) return false;
    }
    return true;
  });

  const filteredProductSales = productSales.filter(item => {
    if (classFilter !== 'All') {
      const isGiftFilter = classFilter === 'Tặng';
      const isItemGift = item.phanLoai === 'Tặng';
      if (isGiftFilter !== isItemGift) return false;
    }
    if (searchProduct) {
      const query = searchProduct.toLowerCase();
      const prodName = item.sanPham.toLowerCase();
      const partnerName = item.khachHang.toLowerCase();
      const code = item.maHD.toLowerCase();
      if (!prodName.includes(query) && !partnerName.includes(query) && !code.includes(query)) return false;
    }
    return true;
  });

  // Form Creation State
  const [formOrderId, setFormOrderId] = useState<number | ''>('');
  const [formPartnerId, setFormPartnerId] = useState<string>('walk-in');
  const [formEmployeeId, setFormEmployeeId] = useState<string>('');
  const [formStatus, setFormStatus] = useState('Hoàn thành'); // Hoàn thành | Hẹn | Hủy
  const [formDeposit, setFormDeposit] = useState<number>(0);
  const [formGhiChu, setFormGhiChu] = useState('');
  const [formDiaChiGiaoHang, setFormDiaChiGiaoHang] = useState('');
  const [formThoiGian, setFormThoiGian] = useState('');
  const [formNgayLap, setFormNgayLap] = useState('');

  // Separated lists for purchased and gifted details
  const [purchasedDetails, setPurchasedDetails] = useState<SalesOrderDetail[]>([
    { sanPham: { id: 0 }, soLuong: 1, isGift: false }
  ]);
  const [giftDetails, setGiftDetails] = useState<SalesOrderDetail[]>([]);

  // Autocomplete / Inline Search & Creation States
  const [customerQuery, setCustomerQuery] = useState('');
  const [showCustomerDropdown, setShowCustomerDropdown] = useState(false);
  const [newCustomerName, setNewCustomerName] = useState('');
  const [newCustomerPhone, setNewCustomerPhone] = useState('');
  const [newCustomerEmail, setNewCustomerEmail] = useState('');
  const [newCustomerAddress, setNewCustomerAddress] = useState('');
  const [isNewPartnerModalOpen, setIsNewPartnerModalOpen] = useState(false);

  const [employeeQuery, setEmployeeQuery] = useState('');
  const [showEmployeeDropdown, setShowEmployeeDropdown] = useState(false);
  const [newEmployeeName, setNewEmployeeName] = useState('');
  const [newEmployeePhone, setNewEmployeePhone] = useState('');
  const [newEmployeeEmail, setNewEmployeeEmail] = useState('');
  const [isNewEmployeeModalOpen, setIsNewEmployeeModalOpen] = useState(false);

  // Alert/Error inside Modal
  const [modalError, setModalError] = useState<string | null>(null);

  // Load Initial Metadata
  useEffect(() => {
    async function loadMetadata() {
      try {
        const prodRes = await fetch("http://localhost:8080/api/v1/san-pham");
        if (prodRes.ok) setAvailableProducts(await prodRes.json());

        const partnerRes = await fetch("http://localhost:8080/api/v1/metadata/doi-tac");
        if (partnerRes.ok) setPartners(await partnerRes.json());

        const empRes = await fetch("http://localhost:8080/api/v1/metadata/nhan-vien");
        if (empRes.ok) {
          const list = await empRes.json();
          setEmployees(list);
          if (list.length > 0) setFormEmployeeId(list[0].id.toString());
        }
      } catch (err) {
        console.error("Error fetching metadata:", err);
      }
    }
    loadMetadata();
  }, []);

  // Fetch Core Data & Statistics
  const loadData = async () => {
    setLoading(true);
    try {
      if (activeTab === 0) {
        // Tab 1: Orders
        // Fetch Orders List
        const ordersRes = await fetch(`http://localhost:8080/api/v1/ban-hang`);
        if (ordersRes.ok) {
          const list: OrderItem[] = await ordersRes.json();
          setOrders(list);
        }

        // Fetch Stats
        const statsQuery = new URLSearchParams();
        if (fromDateInvoice) statsQuery.append('fromDate', fromDateInvoice);
        if (toDateInvoice) statsQuery.append('toDate', toDateInvoice);
        const statsRes = await fetch(`http://localhost:8080/api/v1/ban-hang/stats?${statsQuery.toString()}`);
        if (statsRes.ok) setTab1Stats(await statsRes.json());
      } else {
        // Tab 2: Product Breakdown
        const queryParams = new URLSearchParams();
        if (fromDateProduct) queryParams.append('fromDate', fromDateProduct);
        if (toDateProduct) queryParams.append('toDate', toDateProduct);

        const prodSalesRes = await fetch(`http://localhost:8080/api/v1/bao-cao/san-pham-ban?${queryParams.toString()}`);
        if (prodSalesRes.ok) setProductSales(await prodSalesRes.json());

        // Fetch Stats
        const statsQuery = new URLSearchParams();
        if (fromDateProduct) statsQuery.append('fromDate', fromDateProduct);
        if (toDateProduct) statsQuery.append('toDate', toDateProduct);
        const statsRes = await fetch(`http://localhost:8080/api/v1/bao-cao/san-pham-ban/stats?${statsQuery.toString()}`);
        if (statsRes.ok) setTab2Stats(await statsRes.json());
      }
    } catch (err) {
      console.error("Error fetching data:", err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [activeTab, fromDateInvoice, toDateInvoice, fromDateProduct, toDateProduct]);

  // Open modal if search query contains create=true
  useEffect(() => {
    if (searchParams.get('create') === 'true') {
      openCreatePanel();
    }
  }, [searchParams, employees]);

  // Escape key handler to close panel
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        closePanel();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  const closePanel = () => {
    setIsPanelOpen(false);
    setModalError(null);
    setEditingOrder(null);
    // Clear query parameter
    router.replace('/sales');
  };

  const openCreatePanel = () => {
    setEditingOrder(null);
    const nextId = orders.length > 0 ? Math.max(...orders.map(o => o.id)) + 1 : 1;
    setFormOrderId(nextId);
    setFormPartnerId('walk-in');
    setCustomerQuery('Khách vãng lai (Khách lẻ)');
    setFormEmployeeId(employees.length > 0 ? employees[0].id.toString() : '');
    const defaultEmp = employees.length > 0 ? employees[0] : null;
    setEmployeeQuery(defaultEmp ? defaultEmp.tenNhanVien : '');
    setFormStatus('Hoàn thành');
    setFormDeposit(0);
    setFormGhiChu('');
    setFormDiaChiGiaoHang('');
    
    const now = new Date();
    const tzOffset = now.getTimezoneOffset() * 60000;
    const localISOTime = (new Date(now.getTime() - tzOffset)).toISOString().slice(0, 16);
    setFormThoiGian(localISOTime);
    setFormNgayLap('');
    
    setPurchasedDetails([{ sanPham: { id: 0 }, soLuong: 1, isGift: false }]);
    setGiftDetails([]);
    
    setNewCustomerName('');
    setNewCustomerPhone('');
    setNewCustomerEmail('');
    setNewCustomerAddress('');
    setIsNewPartnerModalOpen(false);
    setNewEmployeeName('');
    setNewEmployeePhone('');
    setNewEmployeeEmail('');
    setIsNewEmployeeModalOpen(false);
    
    setModalError(null);
    setIsPanelOpen(true);
  };

  const openEditPanel = (order: OrderItem) => {
    setEditingOrder(order);
    setFormOrderId(order.id);
    setFormPartnerId(order.doiTac ? order.doiTac.id.toString() : 'walk-in');
    setCustomerQuery(order.doiTac ? order.doiTac.ten : 'Khách vãng lai (Khách lẻ)');
    setFormEmployeeId(order.nhanVien ? order.nhanVien.id.toString() : '');
    setEmployeeQuery(order.nhanVien ? order.nhanVien.tenNhanVien : '');
    setFormStatus(order.trangThai);
    setFormDeposit(order.tienDaThanhToan);
    setFormGhiChu(order.ghiChu || '');
    setFormDiaChiGiaoHang(order.diaChiGiaoHang || '');
    setFormThoiGian(order.thoiGian ? order.thoiGian.slice(0, 16) : '');
    setFormNgayLap(order.ngayLap || '');
    
    const details = order.chiTietBanHangs || [];
    const pDetails = details.filter(d => !d.isGift).map(item => ({
      sanPham: { id: item.sanPham.id },
      soLuong: item.soLuong,
      isGift: false,
      donVi: item.donVi
    }));
    const gDetails = details.filter(d => d.isGift).map(item => ({
      sanPham: { id: item.sanPham.id },
      soLuong: item.soLuong,
      isGift: true,
      donVi: item.donVi
    }));
    
    setPurchasedDetails(pDetails.length > 0 ? pDetails : [{ sanPham: { id: 0 }, soLuong: 1, isGift: false }]);
    setGiftDetails(gDetails);
    
    setNewCustomerName('');
    setNewCustomerPhone('');
    setNewCustomerEmail('');
    setNewCustomerAddress('');
    setIsNewPartnerModalOpen(false);
    setNewEmployeeName('');
    setNewEmployeePhone('');
    setNewEmployeeEmail('');
    setIsNewEmployeeModalOpen(false);
    
    setModalError(null);
    setIsPanelOpen(true);
  };

  const handlePurchasedDetailsChange = (index: number, key: keyof SalesOrderDetail, value: any) => {
    const next = [...purchasedDetails];
    if (key === 'sanPham') {
      next[index] = { ...next[index], sanPham: { id: parseInt(value) || 0 } };
    } else {
      next[index] = { ...next[index], [key]: value } as any;
    }
    setPurchasedDetails(next);
    setModalError(null);
  };

  const handleGiftDetailsChange = (index: number, key: keyof SalesOrderDetail, value: any) => {
    const next = [...giftDetails];
    if (key === 'sanPham') {
      next[index] = { ...next[index], sanPham: { id: parseInt(value) || 0 } };
    } else {
      next[index] = { ...next[index], [key]: value } as any;
    }
    setGiftDetails(next);
    setModalError(null);
  };

  const addPurchasedRow = () => {
    setPurchasedDetails([...purchasedDetails, { sanPham: { id: 0 }, soLuong: 1, isGift: false }]);
  };

  const removePurchasedRow = (index: number) => {
    if (purchasedDetails.length === 1) return;
    setPurchasedDetails(purchasedDetails.filter((_, i) => i !== index));
  };

  const addGiftRow = () => {
    setGiftDetails([...giftDetails, { sanPham: { id: 0 }, soLuong: 1, isGift: true }]);
  };

  const removeGiftRow = (index: number) => {
    setGiftDetails(giftDetails.filter((_, i) => i !== index));
  };

  const calculateTotal = () => {
    return purchasedDetails.reduce((sum, item) => {
      if (!item.sanPham.id) return sum;
      const sp = availableProducts.find(p => p.id === item.sanPham.id);
      if (!sp) return sum;
      return sum + (sp.giaBanHienTai * item.soLuong);
    }, 0);
  };

  const handleCreatePartnerQuick = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newCustomerName.trim()) {
      alert("Vui lòng nhập tên khách hàng mới.");
      return;
    }
    try {
      const res = await fetch("http://localhost:8080/api/v1/metadata/doi-tac", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          ten: newCustomerName,
          sdt: newCustomerPhone || null,
          email: newCustomerEmail || null,
          diaChi: newCustomerAddress || null
        })
      });
      if (res.ok) {
        const newPartner = await res.json();
        setPartners(prev => [...prev, newPartner]);
        setFormPartnerId(newPartner.id.toString());
        setCustomerQuery(newPartner.ten);
        setFormDiaChiGiaoHang(newPartner.diaChi || '');
        
        // Reset states
        setNewCustomerName('');
        setNewCustomerPhone('');
        setNewCustomerEmail('');
        setNewCustomerAddress('');
        setIsNewPartnerModalOpen(false);
      } else {
        const txt = await res.text();
        alert(`Không thể tạo khách hàng: ${txt}`);
      }
    } catch (err) {
      console.error(err);
      alert("Lỗi kết nối khi tạo khách hàng.");
    }
  };

  const handleCreateEmployeeQuick = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newEmployeeName.trim()) {
      alert("Vui lòng nhập tên nhân viên mới.");
      return;
    }
    try {
      const res = await fetch("http://localhost:8080/api/v1/metadata/nhan-vien", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          tenNhanVien: newEmployeeName,
          sdt: newEmployeePhone || null,
          email: newEmployeeEmail || null,
          vaiTro: "Nhân viên"
        })
      });
      if (res.ok) {
        const newEmp = await res.json();
        setEmployees(prev => [...prev, newEmp]);
        setFormEmployeeId(newEmp.id.toString());
        setEmployeeQuery(newEmp.tenNhanVien);
        
        // Reset states
        setNewEmployeeName('');
        setNewEmployeePhone('');
        setNewEmployeeEmail('');
        setIsNewEmployeeModalOpen(false);
      } else {
        const txt = await res.text();
        alert(`Không thể tạo nhân viên: ${txt}`);
      }
    } catch (err) {
      console.error(err);
      alert("Lỗi kết nối khi tạo nhân viên.");
    }
  };

  const handleCreateOrder = async (e: React.FormEvent) => {
    e.preventDefault();
    setModalError(null);

    const combinedDetails = [...purchasedDetails, ...giftDetails];
    if (combinedDetails.some(item => !item.sanPham.id)) {
      setModalError("Vui lòng chọn sản phẩm cho toàn bộ các dòng.");
      return;
    }

    const uniqueKeys = combinedDetails.map(item => `${item.sanPham.id}_${item.isGift}`);
    const duplicatedKey = uniqueKeys.find((key, index) => uniqueKeys.indexOf(key) !== index);
    if (duplicatedKey) {
      const [dupId, dupIsGiftStr] = duplicatedKey.split('_');
      const duplicateProduct = availableProducts.find(p => p.id === parseInt(dupId));
      const prodName = duplicateProduct ? duplicateProduct.tenSanPham : "sản phẩm";
      const type = dupIsGiftStr === 'true' ? "tặng" : "bán";
      setModalError(`Sản phẩm "${prodName}" (${type}) bị trùng lặp trong danh sách. Vui lòng gộp số lượng của chúng lại thành một dòng.`);
      return;
    }

    for (const item of combinedDetails) {
      const sp = availableProducts.find(p => p.id === item.sanPham.id);
      if (!sp) continue;
      
      let existingQty = 0;
      if (editingOrder && editingOrder.chiTietBanHangs && editingOrder.trangThai.toLowerCase() !== 'hủy') {
        const oldDetail = editingOrder.chiTietBanHangs.find(d => d.sanPham.id === item.sanPham.id);
        if (oldDetail) {
          existingQty = oldDetail.soLuong;
        }
      }

      if (sp.soLuongTon + existingQty < item.soLuong) {
        setModalError(`Sản phẩm '${sp.tenSanPham}' không đủ tồn kho. Tồn kho khả dụng (tính cả đơn này): ${sp.soLuongTon + existingQty}. Yêu cầu: ${item.soLuong}`);
        return;
      }
    }

    const payload = {
      id: formOrderId || null,
      doiTac: formPartnerId === 'walk-in' ? null : { id: parseInt(formPartnerId) },
      nhanVien: formEmployeeId ? { id: parseInt(formEmployeeId) } : null,
      trangThai: formStatus,
      tienDaThanhToan: formDeposit,
      ghiChu: formGhiChu,
      diaChiGiaoHang: formDiaChiGiaoHang || null,
      thoiGian: formThoiGian ? new Date(formThoiGian).toISOString() : null,
      ngayLap: formNgayLap || null,
      chiTietBanHangs: combinedDetails.map(item => {
        const sp = availableProducts.find(p => p.id === item.sanPham.id)!;
        return {
          sanPham: { id: sp.id },
          soLuong: item.soLuong,
          isGift: item.isGift,
          donVi: sp.donViTinh ? sp.donViTinh.tenDonVi : 'ly'
        };
      })
    };

    try {
      const url = editingOrder 
        ? `http://localhost:8080/api/v1/ban-hang/${editingOrder.id}`
        : "http://localhost:8080/api/v1/ban-hang";
      const method = editingOrder ? "PUT" : "POST";

      const res = await fetch(url, {
        method: method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });

      if (res.ok) {
        setFormPartnerId('walk-in');
        setFormDeposit(0);
        setFormGhiChu('');
        setPurchasedDetails([{ sanPham: { id: 0 }, soLuong: 1, isGift: false }]);
        setGiftDetails([]);
        closePanel();
        loadData();
      } else {
        const errText = await res.text();
        setModalError(`Lỗi từ Backend: ${errText}`);
      }
    } catch (err) {
      console.error(err);
      setModalError("Không thể kết nối đến máy chủ.");
    }
  };

  const handleUpdateStatus = async (orderId: number, status: string) => {
    try {
      const res = await fetch(`http://localhost:8080/api/v1/ban-hang/${orderId}/status?status=${encodeURIComponent(status)}`, {
        method: "PUT"
      });
      if (res.ok) {
        loadData();
      } else {
        alert("Không thể cập nhật trạng thái hóa đơn. Có thể sản phẩm không đủ hàng để khôi phục.");
      }
    } catch (err) {
      console.error(err);
      alert("Lỗi kết nối máy chủ.");
    }
  };

  // Export dynamically to CSV
  const exportToCSV = () => {
    let csvContent = '\uFEFF'; // UTF-8 BOM
    if (activeTab === 0) {
      // Tab 1 export
      csvContent += 'Mã HĐ,Ngày bán,Khách hàng,Nhân viên,Tổng tiền,Đã trả,Còn nợ,Trạng thái\n';
      filteredOrders.forEach(o => {
        const date = new Date(o.thoiGian).toLocaleString('vi-VN');
        const khach = o.doiTac ? o.doiTac.ten : 'Khách vãng lai';
        const nv = o.nhanVien ? o.nhanVien.tenNhanVien : '---';
        csvContent += `BH-${o.id},"${date}","${khach}","${nv}",${o.tongTien},${o.tienDaThanhToan},${o.tienNo},"${o.trangThai}"\n`;
      });
      triggerDownload(csvContent, 'danh_sach_hoa_don_xuat.csv');
    } else {
      // Tab 2 export
      csvContent += 'Sản phẩm,Phân loại,Mã HĐ,Ngày bán,Giá nhập,Giá bán,Số lượng,Lợi nhuận lý thuyết,Khách hàng\n';
      filteredProductSales.forEach(item => {
        const date = new Date(item.ngayBan).toLocaleString('vi-VN');
        csvContent += `"${item.sanPham}","${item.phanLoai}",${item.maHD},"${date}",${item.giaNhap},${item.giaBan},${item.soLuong},${item.loiNhuan},"${item.khachHang}"\n`;
      });
      triggerDownload(csvContent, 'chi_tiet_san_pham_da_ban.csv');
    }
  };

  const triggerDownload = (content: string, filename: string) => {
    const blob = new Blob([content], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.setAttribute("href", url);
    link.setAttribute("download", filename);
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const formatVND = (num: number) => {
    return num.toLocaleString('vi-VN') + ' ₫';
  };

  const formatNumberWithDots = (num: number | string) => {
    if (num === undefined || num === null) return '';
    const clean = num.toString().replace(/\D/g, '');
    if (!clean) return '0';
    return clean.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
  };

  const parseNumberFromDots = (val: string) => {
    const clean = val.replace(/\D/g, '');
    return parseInt(clean) || 0;
  };

  return (
    <div className="p-6 space-y-4 max-w-[1600px] mx-auto w-full relative">
      {/* Compact Header & Tabs Section */}
      <div className="flex flex-col md:flex-row md:items-center justify-between gap-4 border-b border-white/10 pb-4">
        <div className="flex flex-wrap items-center gap-6">
          <h2 className="text-xl font-bold text-white tracking-tight">Hóa đơn Bán</h2>
          
          {/* Tabs Layout */}
          <div className="flex bg-white/5 p-1 rounded-lg gap-1">
            <button
              onClick={() => setActiveTab(0)}
              className={`px-4 py-1.5 rounded-md text-xs font-semibold uppercase tracking-wider transition-colors cursor-pointer ${
                activeTab === 0 ? 'bg-primary text-on-primary font-bold' : 'text-on-surface-variant hover:text-white'
              }`}
            >
              Hóa đơn
            </button>
            <button
              onClick={() => setActiveTab(1)}
              className={`px-4 py-1.5 rounded-md text-xs font-semibold uppercase tracking-wider transition-colors cursor-pointer ${
                activeTab === 1 ? 'bg-primary text-on-primary font-bold' : 'text-on-surface-variant hover:text-white'
              }`}
            >
              Sản phẩm
            </button>
          </div>
        </div>

        <button
          onClick={openCreatePanel}
          className="glow-button flex items-center bg-primary text-on-primary font-bold px-4 py-2 rounded-lg transition-all glow-teal active:scale-95 cursor-pointer text-xs"
        >
          <span className="material-symbols-outlined mr-1.5 text-sm">add</span>
          Tạo hóa đơn
        </button>
      </div>

      {loading ? (
        <div className="text-center py-20 text-white text-sm">Đang tải dữ liệu từ server...</div>
      ) : (
        <>
          {/* TAB 1: DANH SÁCH HÓA ĐƠN */}
          {activeTab === 0 && (
            <div className="space-y-6">
              {/* Stats Row */}
              <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
                <div className="glass-surface p-5 rounded-xl">
                  <p className="text-text-variant text-[10px] font-bold uppercase tracking-wider mb-2">Doanh thu hoàn thành</p>
                  <h3 className="text-xl font-bold text-success">
                    {formatVND(tab1Stats.completedRevenue)}
                  </h3>
                </div>

                <div className="glass-surface p-5 rounded-xl">
                  <p className="text-text-variant text-[10px] font-bold uppercase tracking-wider mb-2">Tiền cọc đơn hẹn</p>
                  <h3 className="text-xl font-bold text-primary">
                    {formatVND(tab1Stats.scheduledDeposit)}
                  </h3>
                </div>

                <div className="glass-surface p-5 rounded-xl">
                  <p className="text-text-variant text-[10px] font-bold uppercase tracking-wider mb-2">Số lịch hẹn chờ</p>
                  <h3 className="text-xl font-bold text-warning">
                    {tab1Stats.scheduledCount} lịch hẹn
                  </h3>
                </div>

                <div className="glass-surface p-5 rounded-xl">
                  <p className="text-text-variant text-[10px] font-bold uppercase tracking-wider mb-2">Hóa đơn khách nợ</p>
                  <h3 className="text-xl font-bold text-error">
                    {tab1Stats.unpaidCompletedCount} hóa đơn
                  </h3>
                </div>
              </div>

              {/* Table Container */}
              <section className="glass-surface rounded-2xl overflow-hidden">
                {/* Filter Bar */}
                <div className="p-6 border-b border-border-glass flex flex-wrap items-center justify-between gap-4">
                  <div className="flex flex-wrap items-center gap-4">
                    {/* Search */}
                    <div className="relative w-64">
                      <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-on-surface-variant text-sm">
                        search
                      </span>
                      <input
                        type="text"
                        placeholder="Tìm khách hàng hoặc mã HĐ..."
                        value={searchInvoice}
                        onChange={(e) => setSearchInvoice(e.target.value)}
                        className="w-full bg-surface-lowest border border-border-glass rounded-lg pl-9 pr-4 py-2 text-xs text-white focus:outline-none focus:ring-1 focus:ring-primary/50"
                      />
                    </div>

                    {/* Status */}
                    <div className="relative">
                      <select
                        value={statusFilter}
                        onChange={(e) => setStatusFilter(e.target.value)}
                        className="bg-surface-lowest border border-border-glass rounded-lg px-4 py-2 text-xs appearance-none focus:outline-none focus:ring-1 focus:ring-primary/50 text-white cursor-pointer outline-none"
                      >
                        <option value="All">Tất cả trạng thái</option>
                        <option value="Hoàn thành">Hoàn thành</option>
                        <option value="Hẹn">Hẹn</option>
                        <option value="Hủy">Đã Hủy</option>
                      </select>
                    </div>

                    {/* Dates */}
                    <div className="flex items-center gap-2">
                      <input
                        type="date"
                        value={fromDateInvoice}
                        onChange={(e) => setFromDateInvoice(e.target.value)}
                        className="bg-surface-lowest border border-border-glass rounded-lg px-3 py-1.5 text-xs text-white outline-none cursor-pointer"
                      />
                      <span className="text-xs text-text-variant">đến</span>
                      <input
                        type="date"
                        value={toDateInvoice}
                        onChange={(e) => setToDateInvoice(e.target.value)}
                        className="bg-surface-lowest border border-border-glass rounded-lg px-3 py-1.5 text-xs text-white outline-none cursor-pointer"
                      />
                    </div>
                  </div>

                  <div className="flex items-center gap-3">
                    <button
                      onClick={exportToCSV}
                      className="px-4 py-2 text-xs font-semibold bg-white/5 border border-border-glass text-white rounded-lg hover:bg-white/10 flex items-center gap-1 cursor-pointer"
                    >
                      <span className="material-symbols-outlined text-sm">download</span> Xuất CSV
                    </button>
                     <span className="text-xs text-text-variant">Tìm thấy {filteredOrders.length} hóa đơn</span>
                  </div>
                </div>

                {/* Table */}
                <div className="overflow-x-auto">
                  <table className="w-full text-left border-collapse">
                    <thead>
                      <tr className="text-text-variant text-xs uppercase tracking-wider border-b border-border-glass">
                        <th className="px-6 py-4 font-semibold">Mã HĐ</th>
                        <th className="px-6 py-4 font-semibold">Ngày tạo</th>
                        <th className="px-6 py-4 font-semibold">Khách hàng</th>
                        <th className="px-6 py-4 font-semibold">Thu ngân</th>
                        <th className="px-6 py-4 font-semibold">Tổng tiền</th>
                        <th className="px-6 py-4 font-semibold">Đặt cọc</th>
                        <th className="px-6 py-4 font-semibold">Còn nợ</th>
                        <th className="px-6 py-4 font-semibold">Trạng thái</th>
                        <th className="px-6 py-4 font-semibold text-center">Thao tác</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-border-glass">
                      {filteredOrders.map((o) => (
                        <tr key={o.id} className="hover:bg-white/[0.03] transition-colors group">
                          <td className="px-6 py-4 font-mono text-xs text-primary font-bold">BH-{o.id}</td>
                          <td className="px-6 py-4 text-xs text-text-variant">
                            {new Date(o.thoiGian).toLocaleString('vi-VN')}
                          </td>
                          <td className="px-6 py-4 font-medium text-xs">
                            {o.doiTac ? o.doiTac.ten : 'Khách vãng lai'}
                          </td>
                          <td className="px-6 py-4 text-xs text-text-variant">
                            {o.nhanVien ? o.nhanVien.tenNhanVien : '---'}
                          </td>
                          <td className="px-6 py-4 font-bold text-xs text-white">{formatVND(o.tongTien)}</td>
                          <td className="px-6 py-4 text-xs text-success">{formatVND(o.tienDaThanhToan)}</td>
                          <td className="px-6 py-4 text-xs text-error font-bold">{formatVND(o.tienNo)}</td>
                          <td className="px-6 py-4">
                            <span
                              className={`px-3 py-1 rounded-full text-[9px] font-bold uppercase border ${
                                o.trangThai === 'Hoàn thành'
                                  ? 'bg-success/10 border-success text-success'
                                  : o.trangThai === 'Hẹn'
                                  ? 'bg-warning/10 border-warning text-warning'
                                  : 'bg-error/10 border-error text-error'
                              }`}
                            >
                              {o.trangThai}
                            </span>
                          </td>
                          <td className="px-6 py-4 text-center">
                            <div className="flex items-center justify-center gap-2">
                              {/* View Details */}
                              <button
                                onClick={async () => {
                                  try {
                                    const res = await fetch(`http://localhost:8080/api/v1/ban-hang/${o.id}`);
                                    if (res.ok) {
                                      setSelectedViewOrder(await res.json());
                                      setIsViewOpen(true);
                                    }
                                  } catch (err) {
                                    console.error(err);
                                  }
                                }}
                                className="p-1.5 hover:bg-primary/10 rounded-lg text-on-surface-variant hover:text-primary transition-colors cursor-pointer text-xs flex items-center"
                                title="Xem chi tiết"
                              >
                                <span className="material-symbols-outlined text-base">visibility</span>
                              </button>

                              {/* Edit details */}
                              <button
                                onClick={async () => {
                                  try {
                                    const res = await fetch(`http://localhost:8080/api/v1/ban-hang/${o.id}`);
                                    if (res.ok) {
                                      openEditPanel(await res.json());
                                    }
                                  } catch (err) {
                                    console.error(err);
                                  }
                                }}
                                className="p-1.5 hover:bg-primary/10 rounded-lg text-on-surface-variant hover:text-primary transition-colors cursor-pointer text-xs flex items-center"
                                title="Chỉnh sửa hóa đơn"
                              >
                                <span className="material-symbols-outlined text-base">edit</span>
                              </button>

                              {/* Edit Status Dropdown */}
                              <select
                                value={o.trangThai}
                                onChange={(e) => handleUpdateStatus(o.id, e.target.value)}
                                className="bg-surface-lowest text-[10px] text-white border border-border-glass rounded px-1.5 py-0.5 outline-none cursor-pointer"
                              >
                                <option value="Hoàn thành">Hoàn thành</option>
                                <option value="Hẹn">Hẹn</option>
                                <option value="Hủy">Hủy</option>
                              </select>

                              {/* Print ASCII Receipt */}
                              <button
                                onClick={async () => {
                                  try {
                                    const res = await fetch(`http://localhost:8080/api/v1/ban-hang/${o.id}`);
                                    if (res.ok) {
                                      setSelectedPrintOrder(await res.json());
                                      setIsPrintOpen(true);
                                    }
                                  } catch (err) {
                                    console.error(err);
                                  }
                                }}
                                className="p-1.5 hover:bg-warning/10 rounded-lg text-on-surface-variant hover:text-warning transition-colors cursor-pointer text-xs flex items-center"
                                title="In hóa đơn nhiệt"
                              >
                                <span className="material-symbols-outlined text-base">print</span>
                              </button>
                            </div>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </section>
            </div>
          )}

          {/* TAB 2: XEM THEO SẢN PHẨM */}
          {activeTab === 1 && (
            <div className="space-y-6">
              {/* Stats Row */}
              <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
                <div className="glass-surface p-5 rounded-xl">
                  <p className="text-text-variant text-[10px] font-bold uppercase tracking-wider mb-2">Doanh thu sản phẩm</p>
                  <h3 className="text-xl font-bold text-success">
                    {formatVND(tab2Stats.prodRevenue)}
                  </h3>
                </div>

                <div className="glass-surface p-5 rounded-xl">
                  <p className="text-text-variant text-[10px] font-bold uppercase tracking-wider mb-2">Lợi nhuận lý thuyết</p>
                  <h3 className="text-xl font-bold text-primary">
                    {formatVND(tab2Stats.prodProfit)}
                  </h3>
                </div>

                <div className="glass-surface p-5 rounded-xl">
                  <p className="text-text-variant text-[10px] font-bold uppercase tracking-wider mb-2">Số lượng đã bán</p>
                  <h3 className="text-xl font-bold text-warning">
                    {tab2Stats.qtySold} sản phẩm
                  </h3>
                </div>

                <div className="glass-surface p-5 rounded-xl">
                  <p className="text-text-variant text-[10px] font-bold uppercase tracking-wider mb-2">Số lượng quà tặng</p>
                  <h3 className="text-xl font-bold text-error">
                    {tab2Stats.qtyGifted} món quà
                  </h3>
                </div>
              </div>

              {/* Table Container */}
              <section className="glass-surface rounded-2xl overflow-hidden">
                {/* Filter Bar */}
                <div className="p-6 border-b border-border-glass flex flex-wrap items-center justify-between gap-4">
                  <div className="flex flex-wrap items-center gap-4">
                    {/* Search */}
                    <div className="relative w-64">
                      <span className="material-symbols-outlined absolute left-3 top-1/2 -translate-y-1/2 text-on-surface-variant text-sm">
                        search
                      </span>
                      <input
                        type="text"
                        placeholder="Tìm sản phẩm, hóa đơn..."
                        value={searchProduct}
                        onChange={(e) => setSearchProduct(e.target.value)}
                        className="w-full bg-surface-lowest border border-border-glass rounded-lg pl-9 pr-4 py-2 text-xs text-white focus:outline-none focus:ring-1 focus:ring-primary/50"
                      />
                    </div>

                    {/* Classification filter */}
                    <div className="relative">
                      <select
                        value={classFilter}
                        onChange={(e) => setClassFilter(e.target.value)}
                        className="bg-surface-lowest border border-border-glass rounded-lg px-4 py-2 text-xs appearance-none focus:outline-none focus:ring-1 focus:ring-primary/50 text-white cursor-pointer outline-none"
                      >
                        <option value="All">Bán &amp; Tặng</option>
                        <option value="Bán">Bán</option>
                        <option value="Tặng">Tặng</option>
                      </select>
                    </div>

                    {/* Dates */}
                    <div className="flex items-center gap-2">
                      <input
                        type="date"
                        value={fromDateProduct}
                        onChange={(e) => setFromDateProduct(e.target.value)}
                        className="bg-surface-lowest border border-border-glass rounded-lg px-3 py-1.5 text-xs text-white outline-none cursor-pointer"
                      />
                      <span className="text-xs text-text-variant">đến</span>
                      <input
                        type="date"
                        value={toDateProduct}
                        onChange={(e) => setToDateProduct(e.target.value)}
                        className="bg-surface-lowest border border-border-glass rounded-lg px-3 py-1.5 text-xs text-white outline-none cursor-pointer"
                      />
                    </div>
                  </div>

                  <div className="flex items-center gap-3">
                    <button
                      onClick={exportToCSV}
                      className="px-4 py-2 text-xs font-semibold bg-white/5 border border-border-glass text-white rounded-lg hover:bg-white/10 flex items-center gap-1 cursor-pointer"
                    >
                      <span className="material-symbols-outlined text-sm">download</span> Xuất CSV
                    </button>
                     <span className="text-xs text-text-variant">Tổng chi tiết: {filteredProductSales.length}</span>
                  </div>
                </div>

                {/* Table */}
                <div className="overflow-x-auto">
                  <table className="w-full text-left border-collapse">
                    <thead>
                      <tr className="text-text-variant text-xs uppercase tracking-wider border-b border-border-glass">
                        <th className="px-6 py-4 font-semibold">Sản phẩm</th>
                        <th className="px-6 py-4 font-semibold">Phân loại</th>
                        <th className="px-6 py-4 font-semibold">Mã HĐ</th>
                        <th className="px-6 py-4 font-semibold">Ngày bán</th>
                        <th className="px-6 py-4 font-semibold">Khách hàng</th>
                        <th className="px-6 py-4 font-semibold">Giá nhập</th>
                        <th className="px-6 py-4 font-semibold">Giá bán</th>
                        <th className="px-6 py-4 font-semibold">Số lượng</th>
                        <th className="px-6 py-4 font-semibold">Lợi nhuận lý thuyết</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-border-glass">
                      {filteredProductSales.map((item, idx) => (
                        <tr key={idx} className="hover:bg-white/[0.03] transition-colors group">
                          <td className="px-6 py-4 font-medium text-xs text-white">{item.sanPham}</td>
                          <td className="px-6 py-4">
                            <span
                              className={`px-3 py-1 rounded-full text-[9px] font-bold uppercase border ${
                                item.phanLoai === 'Bán'
                                  ? 'bg-success/10 border-success text-success'
                                  : 'bg-warning/10 border-warning text-warning'
                              }`}
                            >
                              {item.phanLoai}
                            </span>
                          </td>
                          <td className="px-6 py-4 font-mono text-xs text-primary font-bold">{item.maHD}</td>
                          <td className="px-6 py-4 text-xs text-text-variant">
                            {new Date(item.ngayBan).toLocaleString('vi-VN')}
                          </td>
                          <td className="px-6 py-4 text-xs font-semibold text-white">{item.khachHang}</td>
                          <td className="px-6 py-4 text-xs text-text-variant font-mono">{formatVND(item.giaNhap)}</td>
                          <td className="px-6 py-4 text-xs font-bold font-mono text-white">{formatVND(item.giaBan)}</td>
                          <td className="px-6 py-4 text-xs text-white font-bold">{item.soLuong}</td>
                          <td
                            className={`px-6 py-4 text-xs font-bold font-mono ${
                              item.loiNhuan >= 0 ? 'text-success' : 'text-error'
                            }`}
                          >
                            {formatVND(item.loiNhuan)}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </section>
            </div>
          )}
        </>
      )}

      {/* CREATE NEW SALES ORDER DRAWER OVERLAY */}
      {isPanelOpen && (
        <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 md:p-6 overflow-y-auto bg-black/75 backdrop-blur-sm">
          {/* Backdrop (no click event to prevent closing on background click) */}
          <div className="absolute inset-0 bg-background/40"></div>

          {/* Panel */}
          <div className="relative w-full max-w-6xl bg-[#0A0E17]/95 border border-white/10 backdrop-blur-[12px] rounded-2xl shadow-2xl flex flex-col max-h-[90vh] overflow-hidden z-50 transition-all animate-in fade-in zoom-in-95 duration-200">
            <div className="flex justify-between items-center py-2 px-6 border-b border-white/10">
              <h3 className="text-sm font-bold text-white font-sans flex items-center gap-2">
                <span className="material-symbols-outlined text-primary text-lg">receipt_long</span>
                {editingOrder ? `Chỉnh sửa hóa đơn BH-${editingOrder.id}` : 'Lập hóa đơn xuất mới'}
              </h3>
              <button type="button" className="p-1 hover:bg-white/10 rounded-full text-on-surface-variant cursor-pointer transition-colors flex items-center justify-center" onClick={closePanel}>
                <span className="material-symbols-outlined text-lg">close</span>
              </button>
            </div>
            
            {modalError && (
              <div className="p-3 mx-6 mt-4 text-xs font-semibold text-error bg-error/10 border border-error/20 rounded-xl">
                ⚠️ {modalError}
              </div>
            )}

            <form onSubmit={handleCreateOrder} className="flex-1 flex flex-col overflow-hidden">
              <div className="flex-1 overflow-y-auto p-6 md:p-8 space-y-6 pr-4">
              
              {/* Row 1: số hóa đơn, ngày tạo, nhân viên */}
              <div className="grid grid-cols-3 gap-4">
                {/* Field 1: Số HD/ID */}
                <div>
                  <label className="block text-[10px] uppercase tracking-wider text-text-variant mb-2 font-bold">Số HD/ID</label>
                  <input
                    type="number"
                    value={formOrderId}
                    onChange={(e) => setFormOrderId(e.target.value === '' ? '' : parseInt(e.target.value) || 0)}
                    className="w-full bg-surface-low border border-border-glass rounded-xl px-4 py-2.5 text-xs outline-none text-white focus:border-primary transition-all font-mono font-bold"
                    placeholder="Auto-increment..."
                  />
                </div>

                {/* Field 2: Ngày tạo */}
                <div>
                  <label className="block text-[10px] uppercase tracking-wider text-text-variant mb-2 font-bold">Ngày tạo</label>
                  <input
                    type="datetime-local"
                    value={formThoiGian}
                    onChange={(e) => setFormThoiGian(e.target.value)}
                    className="w-full bg-surface-low border border-border-glass rounded-xl px-4 py-2.5 text-xs outline-none text-white focus:border-primary transition-all cursor-pointer"
                  />
                </div>

                {/* Field 3: Nhân viên phụ trách */}
                <div className="relative">
                  <label className="block text-[10px] uppercase tracking-wider text-text-variant mb-2 font-bold">Nhân viên phụ trách</label>
                  <div className="relative">
                    <input
                      type="text"
                      value={employeeQuery}
                      onChange={(e) => {
                        setEmployeeQuery(e.target.value);
                        setShowEmployeeDropdown(true);
                      }}
                      onFocus={() => setShowEmployeeDropdown(true)}
                      className="w-full bg-surface-low border border-border-glass rounded-xl px-4 py-2.5 text-xs outline-none text-white focus:border-primary transition-all"
                      placeholder="Tìm nhân viên..."
                    />
                    <button
                      type="button"
                      onClick={() => setShowEmployeeDropdown(!showEmployeeDropdown)}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-on-surface-variant hover:text-white cursor-pointer"
                    >
                      <span className="material-symbols-outlined text-sm">arrow_drop_down</span>
                    </button>
                  </div>
                  {showEmployeeDropdown && (
                    <div className="absolute left-0 right-0 mt-1 max-h-48 overflow-y-auto bg-[#141b2e] border border-border-glass rounded-xl z-50 shadow-2xl p-1">
                      <div
                        onClick={() => {
                          setIsNewEmployeeModalOpen(true);
                          setShowEmployeeDropdown(false);
                        }}
                        className="px-3 py-2.5 text-xs text-primary font-bold hover:bg-white/5 rounded-lg cursor-pointer flex items-center gap-1.5 border-b border-white/5"
                      >
                        <span className="material-symbols-outlined text-sm">person_add</span> Thêm nhân viên mới...
                      </div>
                      {employees
                        .filter(emp => emp.tenNhanVien.toLowerCase().includes(employeeQuery.toLowerCase()))
                        .map(emp => (
                          <div
                            key={emp.id}
                            onClick={() => {
                              setFormEmployeeId(emp.id.toString());
                              setEmployeeQuery(emp.tenNhanVien);
                              setShowEmployeeDropdown(false);
                            }}
                            className="px-3 py-2 text-xs text-white hover:bg-white/5 rounded-lg cursor-pointer"
                          >
                            {emp.tenNhanVien}
                          </div>
                        ))}
                    </div>
                  )}
                </div>
              </div>

              {/* Row 2: khách hàng , SDT KH, địa chỉ */}
              <div className="grid grid-cols-3 gap-4">
                {/* Field 4: Chọn Khách hàng (Searchable dropdown) */}
                <div className="relative">
                  <label className="block text-[10px] uppercase tracking-wider text-text-variant mb-2 font-bold">Khách hàng</label>
                  <div className="relative">
                    <input
                      type="text"
                      value={customerQuery}
                      onChange={(e) => {
                        setCustomerQuery(e.target.value);
                        setShowCustomerDropdown(true);
                      }}
                      onFocus={() => setShowCustomerDropdown(true)}
                      className="w-full bg-surface-low border border-border-glass rounded-xl px-4 py-2.5 text-xs outline-none text-white focus:border-primary transition-all"
                      placeholder="Tìm khách hàng hoặc nhập mới..."
                    />
                    <button
                      type="button"
                      onClick={() => setShowCustomerDropdown(!showCustomerDropdown)}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-on-surface-variant hover:text-white cursor-pointer"
                    >
                      <span className="material-symbols-outlined text-sm">arrow_drop_down</span>
                    </button>
                  </div>
                  {showCustomerDropdown && (
                    <div className="absolute left-0 right-0 mt-1 max-h-48 overflow-y-auto bg-[#141b2e] border border-border-glass rounded-xl z-50 shadow-2xl p-1">
                      <div
                        onClick={() => {
                          setIsNewPartnerModalOpen(true);
                          setShowCustomerDropdown(false);
                        }}
                        className="px-3 py-2.5 text-xs text-primary font-bold hover:bg-white/5 rounded-lg cursor-pointer flex items-center gap-1.5 border-b border-white/5"
                      >
                        <span className="material-symbols-outlined text-sm">person_add</span> Thêm khách hàng mới...
                      </div>
                      <div
                        onClick={() => {
                          setFormPartnerId('walk-in');
                          setCustomerQuery('Khách vãng lai (Khách lẻ)');
                          setFormDiaChiGiaoHang('');
                          setShowCustomerDropdown(false);
                        }}
                        className="px-3 py-2 text-xs text-white hover:bg-white/5 rounded-lg cursor-pointer"
                      >
                        Khách vãng lai (Khách lẻ)
                      </div>
                      {partners
                        .filter(p => p.ten.toLowerCase().includes(customerQuery.toLowerCase()) || (p.sdt && p.sdt.includes(customerQuery)))
                        .map(p => (
                          <div
                            key={p.id}
                            onClick={() => {
                              setFormPartnerId(p.id.toString());
                              setCustomerQuery(p.ten);
                              setFormDiaChiGiaoHang(p.diaChi || '');
                              setShowCustomerDropdown(false);
                            }}
                            className="px-3 py-2 text-xs text-white hover:bg-white/5 rounded-lg cursor-pointer flex justify-between items-center"
                          >
                            <span>{p.ten}</span>
                            <span className="text-[10px] text-text-variant font-mono">{p.sdt || ''}</span>
                          </div>
                        ))}
                    </div>
                  )}
                </div>

                {/* Field 5: Số điện thoại tự fill */}
                <div>
                  <label className="block text-[10px] uppercase tracking-wider text-text-variant mb-2 font-bold">SDT KH</label>
                  <input
                    type="text"
                    readOnly
                    value={
                      formPartnerId === 'walk-in'
                        ? 'Khách vãng lai'
                        : partners.find(p => p.id.toString() === formPartnerId)?.sdt || 'Chưa cập nhật'
                    }
                    className="w-full bg-surface-low/40 border border-border-glass rounded-xl px-4 py-2.5 text-xs text-on-surface-variant font-medium outline-none cursor-not-allowed"
                  />
                </div>

                {/* Field 6: Địa chỉ */}
                <div>
                  <label className="block text-[10px] uppercase tracking-wider text-text-variant mb-2 font-bold">Địa chỉ</label>
                  <input
                    type="text"
                    value={formDiaChiGiaoHang}
                    onChange={(e) => setFormDiaChiGiaoHang(e.target.value)}
                    className="w-full bg-surface-low border border-border-glass rounded-xl px-4 py-2.5 text-xs outline-none text-white focus:border-primary transition-all"
                    placeholder="Nhập địa chỉ giao hàng..."
                  />
                </div>
              </div>

              {/* Row 3: trạng thái, ngày lắp máy, tiên đã trả */}
              <div className="grid grid-cols-3 gap-4">
                {/* Field 7: Trạng thái */}
                <div>
                  <label className="block text-[10px] uppercase tracking-wider text-text-variant mb-2 font-bold">Trạng thái</label>
                  <select
                    value={formStatus}
                    onChange={(e) => setFormStatus(e.target.value)}
                    className="w-full bg-surface-low border border-border-glass rounded-xl px-4 py-2.5 text-xs outline-none text-white cursor-pointer"
                  >
                    <option value="Hoàn thành">Hoàn thành (Đã thanh toán / Phục vụ)</option>
                    <option value="Hẹn">Hẹn (Đặt bàn trước / Ship lắp đặt)</option>
                    <option value="Hủy">Đã Hủy</option>
                  </select>
                </div>

                {/* Field 8: Ngày lắp đặt máy */}
                <div>
                  <label className="block text-[10px] uppercase tracking-wider text-text-variant mb-2 font-bold">Ngày lắp máy</label>
                  <input
                    type="date"
                    value={formNgayLap}
                    onChange={(e) => setFormNgayLap(e.target.value)}
                    className="w-full bg-surface-low border border-border-glass rounded-xl px-4 py-2.5 text-xs outline-none text-white focus:border-primary transition-all cursor-pointer"
                  />
                </div>

                {/* Field 9: Tiền đã thanh toán */}
                <div>
                  <label className="block text-[10px] uppercase tracking-wider text-text-variant mb-2 font-bold">Tiền đã trả (đ)</label>
                  <input
                    type="text"
                    value={formatNumberWithDots(formDeposit)}
                    onChange={(e) => setFormDeposit(parseNumberFromDots(e.target.value))}
                    className="w-full bg-surface-low border border-border-glass rounded-xl px-4 py-2.5 text-xs focus:border-primary outline-none text-white font-mono"
                    placeholder="Số tiền đã trả..."
                  />
                </div>
              </div>

              {/* Field 10: Ghi chú */}
              <div>
                <label className="block text-[10px] uppercase tracking-wider text-text-variant mb-2 font-bold">10. Ghi chú hóa đơn</label>
                <textarea
                  value={formGhiChu}
                  onChange={(e) => setFormGhiChu(e.target.value)}
                  className="w-full bg-surface-low border border-border-glass rounded-xl px-4 py-2.5 text-xs focus:border-primary outline-none text-white h-16 resize-none"
                  placeholder="Ghi chú chi tiết hóa đơn: bàn số mấy, các lưu ý khác..."
                />
              </div>

              {/* Field 11: Danh sách sản phẩm mua */}
              <div className="space-y-3 pt-2">
                <div className="flex justify-between items-center">
                  <label className="block text-xs uppercase tracking-wider text-text-variant font-bold">11. Danh sách sản phẩm mua</label>
                  <button
                    type="button"
                    onClick={addPurchasedRow}
                    className="text-[10px] bg-primary/20 text-primary border border-primary/20 hover:bg-primary/30 px-3 py-1 rounded-md font-bold uppercase transition-all flex items-center gap-1 cursor-pointer"
                  >
                    <span className="material-symbols-outlined text-xs">add</span> Thêm món
                  </button>
                </div>

                <div className="space-y-3 max-h-52 overflow-y-auto pr-1">
                  {purchasedDetails.map((row, idx) => {
                    const selectedSp = availableProducts.find(p => p.id === row.sanPham.id);
                    return (
                      <div key={idx} className="flex items-center gap-3 p-3 bg-white/5 border border-border-glass rounded-xl relative group">
                        <div className="flex-1">
                          <select
                            required
                            value={row.sanPham.id || ''}
                            onChange={(e) => handlePurchasedDetailsChange(idx, 'sanPham', e.target.value)}
                            className="w-full bg-[#111625] border border-border-glass rounded-lg px-3 py-2 text-xs outline-none text-white cursor-pointer"
                          >
                            <option value="">-- Chọn sản phẩm/món uống --</option>
                            {availableProducts.map(p => (
                              <option key={p.id} value={p.id}>
                                {p.tenSanPham} (Tồn: {p.soLuongTon} {p.donViTinh?.tenDonVi || 'ly'}) - {formatVND(p.giaBanHienTai)}
                              </option>
                            ))}
                          </select>
                        </div>

                        <div className="w-16">
                          <input
                            required
                            type="number"
                            min="1"
                            value={row.soLuong}
                            onChange={(e) => handlePurchasedDetailsChange(idx, 'soLuong', parseInt(e.target.value) || 1)}
                            className="w-full bg-[#111625] border border-border-glass rounded-lg px-2 py-2 text-xs outline-none text-white text-center"
                            placeholder="SL"
                          />
                        </div>

                        <div className="w-10 text-xs text-text-variant font-medium text-center">
                          {selectedSp?.donViTinh?.tenDonVi || 'ly'}
                        </div>

                        <div className="w-24 text-right text-xs font-semibold text-white font-mono">
                          {formatVND(selectedSp ? selectedSp.giaBanHienTai * row.soLuong : 0)}
                        </div>

                        <button
                          type="button"
                          onClick={() => removePurchasedRow(idx)}
                          className="p-1 hover:bg-error/20 rounded-md text-on-surface-variant hover:text-error transition-colors cursor-pointer"
                        >
                          <span className="material-symbols-outlined text-sm">delete</span>
                        </button>
                      </div>
                    );
                  })}
                </div>
              </div>

              {/* Field 12: Danh sách sản phẩm tặng kèm */}
              <div className="space-y-3 pt-2 border-t border-white/5">
                <div className="flex justify-between items-center">
                  <label className="block text-xs uppercase tracking-wider text-warning font-bold">12. Danh sách sản phẩm tặng kèm</label>
                  <button
                    type="button"
                    onClick={addGiftRow}
                    className="text-[10px] bg-warning/20 text-warning border border-warning/20 hover:bg-warning/30 px-3 py-1 rounded-md font-bold uppercase transition-all flex items-center gap-1 cursor-pointer"
                  >
                    <span className="material-symbols-outlined text-xs">add</span> Thêm quà tặng
                  </button>
                </div>

                <div className="space-y-3 max-h-48 overflow-y-auto pr-1">
                  {giftDetails.map((row, idx) => {
                    const selectedSp = availableProducts.find(p => p.id === row.sanPham.id);
                    return (
                      <div key={idx} className="flex items-center gap-3 p-3 bg-warning/5 border border-warning/10 rounded-xl relative group">
                        <div className="flex-1">
                          <select
                            required
                            value={row.sanPham.id || ''}
                            onChange={(e) => handleGiftDetailsChange(idx, 'sanPham', e.target.value)}
                            className="w-full bg-[#111625] border border-border-glass rounded-lg px-3 py-2 text-xs outline-none text-white cursor-pointer"
                          >
                            <option value="">-- Chọn sản phẩm tặng kèm --</option>
                            {availableProducts.map(p => (
                              <option key={p.id} value={p.id}>
                                {p.tenSanPham} (Tồn: {p.soLuongTon} {p.donViTinh?.tenDonVi || 'ly'})
                              </option>
                            ))}
                          </select>
                        </div>

                        <div className="w-16">
                          <input
                            required
                            type="number"
                            min="1"
                            value={row.soLuong}
                            onChange={(e) => handleGiftDetailsChange(idx, 'soLuong', parseInt(e.target.value) || 1)}
                            className="w-full bg-[#111625] border border-border-glass rounded-lg px-2 py-2 text-xs outline-none text-white text-center"
                            placeholder="SL"
                          />
                        </div>

                        <div className="w-10 text-xs text-text-variant font-medium text-center">
                          {selectedSp?.donViTinh?.tenDonVi || 'ly'}
                        </div>

                        <div className="w-24 text-right text-xs font-bold text-warning uppercase">
                          Quà tặng (0đ)
                        </div>

                        <button
                          type="button"
                          onClick={() => removeGiftRow(idx)}
                          className="p-1 hover:bg-error/20 rounded-md text-on-surface-variant hover:text-error transition-colors cursor-pointer"
                        >
                          <span className="material-symbols-outlined text-sm">delete</span>
                        </button>
                      </div>
                    );
                  })}
                </div>
              </div>

              </div>

              {/* Summary Calculations Footer & Submit */}
              <div className="py-4 px-6 bg-[#0A0E17] border-t border-border-glass flex justify-between items-center sticky bottom-0 z-10">
                {/* Left: Summary Metrics */}
                <div className="flex items-center gap-6 text-xs">
                  <div className="flex items-center gap-1.5">
                    <span className="text-text-variant font-bold">Tổng cộng:</span>
                    <span className="font-bold text-white font-mono">{formatVND(calculateTotal())}</span>
                  </div>
                  <div className="h-4 w-px bg-white/10"></div>
                  <div className="flex items-center gap-1.5">
                    <span className="text-text-variant font-bold">Đã trả:</span>
                    <span className="font-bold text-success font-mono">{formatVND(formDeposit)}</span>
                  </div>
                  <div className="h-4 w-px bg-white/10"></div>
                  <div className="flex items-center gap-1.5">
                    <span className="text-text-variant font-bold">Còn nợ:</span>
                    <span className="font-bold text-error font-mono">
                      {formatVND(Math.max(0, calculateTotal() - formDeposit))}
                    </span>
                  </div>
                </div>
                
                {/* Right: Actions */}
                <div className="flex items-center gap-3">
                  <button
                    type="button"
                    onClick={closePanel}
                    className="px-5 py-2.5 rounded-lg text-xs font-semibold text-on-surface-variant hover:text-white bg-white/5 border border-border-glass transition-colors cursor-pointer"
                  >
                    Hủy
                  </button>
                  <button
                    type="submit"
                    className="px-6 py-2.5 bg-primary text-on-primary font-bold rounded-lg transition-all glow-teal active:scale-95 cursor-pointer"
                  >
                    {editingOrder ? 'Cập nhật Hóa đơn' : 'Lưu hóa đơn'}
                  </button>
                </div>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* VIEW ORDER DETAILS MODAL OVERLAY */}
      {isViewOpen && selectedViewOrder && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          {/* Backdrop */}
          <div className="absolute inset-0 bg-background/80 backdrop-blur-sm" onClick={() => setIsViewOpen(false)}></div>

          {/* Modal content */}
          <div className="relative w-full max-w-lg bg-[#0A0E17]/95 border border-white/10 backdrop-blur-[12px] p-6 rounded-2xl shadow-2xl flex flex-col z-50">
            <div className="flex justify-between items-center mb-4">
              <h3 className="text-lg font-bold text-white">Chi tiết hóa đơn BH-{selectedViewOrder.id}</h3>
              <button
                onClick={() => setIsViewOpen(false)}
                className="p-1 hover:bg-white/10 rounded-full text-on-surface-variant cursor-pointer"
              >
                <span className="material-symbols-outlined">close</span>
              </button>
            </div>

            <div className="space-y-4 text-xs">
              <div className="grid grid-cols-2 gap-4 text-text-variant border-b border-white/5 pb-3">
                <div>
                  <p>Thời gian: <span className="text-white font-medium">{new Date(selectedViewOrder.thoiGian).toLocaleString('vi-VN')}</span></p>
                  <p className="mt-1">Khách hàng: <span className="text-white font-medium">{selectedViewOrder.doiTac ? selectedViewOrder.doiTac.ten : 'Khách vãng lai'}</span></p>
                </div>
                <div>
                  <p>Thu ngân: <span className="text-white font-medium">{selectedViewOrder.nhanVien ? selectedViewOrder.nhanVien.tenNhanVien : '---'}</span></p>
                  <p className="mt-1">Trạng thái: <span className="text-primary font-bold">{selectedViewOrder.trangThai}</span></p>
                </div>
              </div>

              {/* Items List */}
              <div>
                <p className="font-semibold text-white uppercase text-[10px] tracking-wider mb-2">Danh sách sản phẩm / món nước</p>
                <div className="space-y-2 max-h-48 overflow-y-auto pr-1">
                  {selectedViewOrder.chiTietBanHangs?.map((ct, idx) => (
                    <div key={idx} className="flex justify-between items-center p-2.5 bg-white/5 rounded-lg border border-border-glass">
                      <div>
                        <p className="font-semibold text-white">{ct.sanPham.tenSanPham}</p>
                        <p className="text-[10px] text-text-variant">
                          {ct.isGift ? <span className="text-warning font-bold">[QUÀ TẶNG]</span> : formatVND(ct.giaBan || 0)} x {ct.soLuong} {ct.donVi}
                        </p>
                      </div>
                      <div className="font-bold text-white text-right">
                        {formatVND(ct.thanhTien || 0)}
                      </div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Ghi chú */}
              {selectedViewOrder.ghiChu && (
                <div className="p-3 bg-white/5 border border-border-glass rounded-xl">
                  <p className="font-semibold text-white uppercase text-[10px] tracking-wider mb-1">Ghi chú</p>
                  <p className="text-text-variant">{selectedViewOrder.ghiChu}</p>
                </div>
              )}

              {/* Calculations */}
              <div className="pt-4 border-t border-border-glass space-y-1.5 text-right font-medium">
                <p className="text-text-variant">Tổng tiền: <span className="text-sm font-bold text-white ml-2">{formatVND(selectedViewOrder.tongTien)}</span></p>
                <p className="text-text-variant">Đã thanh toán: <span className="text-xs font-bold text-success ml-2">{formatVND(selectedViewOrder.tienDaThanhToan)}</span></p>
                <p className="text-text-variant">Khách còn nợ: <span className="text-xs font-bold text-error ml-2">{formatVND(selectedViewOrder.tienNo)}</span></p>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* PRINT RECEIPT MODAL OVERLAY */}
      {isPrintOpen && selectedPrintOrder && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          {/* Backdrop */}
          <div className="absolute inset-0 bg-background/80 backdrop-blur-sm" onClick={() => setIsPrintOpen(false)}></div>

          {/* Modal container */}
          <div className="relative w-full max-w-[400px] bg-white text-black p-6 rounded-2xl shadow-2xl flex flex-col z-50 max-h-[90vh]">
            <div className="flex justify-between items-center mb-4 border-b border-black/10 pb-2">
              <h3 className="text-sm font-bold text-black uppercase tracking-wider">Xem trước Hóa đơn in nhiệt</h3>
              <button
                onClick={() => setIsPrintOpen(false)}
                className="p-1 hover:bg-black/5 rounded-full text-black cursor-pointer"
              >
                <span className="material-symbols-outlined text-sm">close</span>
              </button>
            </div>

            {/* ASCII thermal receipt simulation */}
            <div className="bg-slate-100 p-4 border border-slate-300 font-mono text-[11px] rounded-lg overflow-y-auto flex-1 leading-relaxed selection:bg-slate-300">
              <div className="text-center font-bold text-xs uppercase mb-1">☕ CAFE DI ROM ☕</div>
              <div className="text-center mb-2">Hương vị cà phê truyền thống</div>
              <div className="border-t border-dashed border-black/30 my-2"></div>
              
              <p>MÃ HÓA ĐƠN: BH-{selectedPrintOrder.id}</p>
              <p>NGÀY LẬP: {new Date(selectedPrintOrder.thoiGian).toLocaleString('vi-VN')}</p>
              <p>KHÁCH HÀNG: {selectedPrintOrder.doiTac ? selectedPrintOrder.doiTac.ten : 'Khách vãng lai'}</p>
              <p>THU NGÂN: {selectedPrintOrder.nhanVien ? selectedPrintOrder.nhanVien.tenNhanVien : '---'}</p>
              {selectedPrintOrder.ghiChu && <p>GHI CHÚ: {selectedPrintOrder.ghiChu}</p>}
              
              <div className="border-t border-dashed border-black/30 my-2"></div>
              
              {/* Items Grid */}
              <div className="grid grid-cols-12 gap-1 font-bold">
                <span className="col-span-6">Sản phẩm</span>
                <span className="col-span-2 text-center">SL</span>
                <span className="col-span-4 text-right">T.Tiền</span>
              </div>
              
              {selectedPrintOrder.chiTietBanHangs?.map((ct, idx) => (
                <div key={idx} className="grid grid-cols-12 gap-1 text-[10px] mt-1 font-normal">
                  <span className="col-span-6 truncate">{ct.sanPham.tenSanPham}</span>
                  <span className="col-span-2 text-center">{ct.soLuong}</span>
                  <span className="col-span-4 text-right">{ct.isGift ? '0' : ct.thanhTien?.toLocaleString('vi-VN')}</span>
                </div>
              ))}
              
              <div className="border-t border-dashed border-black/30 my-2"></div>
              
              <div className="flex justify-between font-bold">
                <span>TỔNG CỘNG:</span>
                <span>{selectedPrintOrder.tongTien.toLocaleString('vi-VN')} đ</span>
              </div>
              <div className="flex justify-between">
                <span>ĐÃ THANH TOÁN:</span>
                <span>{selectedPrintOrder.tienDaThanhToan.toLocaleString('vi-VN')} đ</span>
              </div>
              <div className="flex justify-between text-red-600 font-bold">
                <span>CÒN NỢ:</span>
                <span>{selectedPrintOrder.tienNo.toLocaleString('vi-VN')} đ</span>
              </div>
              
              <div className="border-t border-dashed border-black/30 my-2"></div>
              <div className="text-center font-bold text-[10px] mt-2">CẢM ƠN QUÝ KHÁCH HẸN GẶP LẠI!</div>
            </div>

            <div className="mt-4 flex justify-end gap-2">
              <button
                onClick={() => window.print()}
                className="px-4 py-2 bg-primary text-on-primary font-bold text-xs rounded-xl hover:neon-glow active:scale-95 transition-all cursor-pointer"
              >
                🖨 Kích hoạt lệnh In
              </button>
            </div>
          </div>
        </div>
      )}

      {/* QUICK CREATE PARTNER MODAL */}
      {isNewPartnerModalOpen && (
        <div className="fixed inset-0 z-[110] flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-in fade-in duration-200">
          <div className="relative w-full max-w-md bg-[#0A0E17]/95 border border-white/[0.08] backdrop-blur-[12px] p-6 rounded-2xl shadow-2xl flex flex-col z-50">
            <div className="flex justify-between items-center mb-4 pb-2 border-b border-white/5">
              <h3 className="text-sm font-bold text-white flex items-center gap-2">
                <span className="material-symbols-outlined text-primary text-base">person_add</span>
                Thêm khách hàng mới
              </h3>
              <button
                type="button"
                onClick={() => setIsNewPartnerModalOpen(false)}
                className="p-1 hover:bg-white/10 rounded-full text-on-surface-variant cursor-pointer transition-colors"
              >
                <span className="material-symbols-outlined text-base">close</span>
              </button>
            </div>

            <form onSubmit={handleCreatePartnerQuick} className="space-y-4">
              <div>
                <label className="block text-[10px] uppercase tracking-wider text-text-variant mb-1.5 font-bold">Tên khách hàng *</label>
                <input
                  type="text"
                  required
                  value={newCustomerName}
                  onChange={(e) => setNewCustomerName(e.target.value)}
                  className="w-full bg-surface-low border border-border-glass rounded-xl px-3.5 py-2 text-xs text-white outline-none focus:border-primary transition-all"
                  placeholder="Nhập tên khách hàng..."
                />
              </div>

              <div>
                <label className="block text-[10px] uppercase tracking-wider text-text-variant mb-1.5 font-bold">Số điện thoại</label>
                <input
                  type="text"
                  value={newCustomerPhone}
                  onChange={(e) => setNewCustomerPhone(e.target.value)}
                  className="w-full bg-surface-low border border-border-glass rounded-xl px-3.5 py-2 text-xs text-white outline-none focus:border-primary transition-all font-mono"
                  placeholder="Nhập số điện thoại..."
                />
              </div>

              <div>
                <label className="block text-[10px] uppercase tracking-wider text-text-variant mb-1.5 font-bold">Email</label>
                <input
                  type="email"
                  value={newCustomerEmail}
                  onChange={(e) => setNewCustomerEmail(e.target.value)}
                  className="w-full bg-surface-low border border-border-glass rounded-xl px-3.5 py-2 text-xs text-white outline-none focus:border-primary transition-all"
                  placeholder="Nhập email..."
                />
              </div>

              <div>
                <label className="block text-[10px] uppercase tracking-wider text-text-variant mb-1.5 font-bold">Địa chỉ</label>
                <textarea
                  value={newCustomerAddress}
                  onChange={(e) => setNewCustomerAddress(e.target.value)}
                  className="w-full bg-surface-low border border-border-glass rounded-xl px-3.5 py-2 text-xs text-white outline-none focus:border-primary transition-all h-20 resize-none"
                  placeholder="Nhập địa chỉ..."
                />
              </div>

              <div className="pt-2 flex justify-end gap-3 border-t border-white/5 mt-4">
                <button
                  type="button"
                  onClick={() => setIsNewPartnerModalOpen(false)}
                  className="px-4 py-2 rounded-xl text-xs font-semibold text-on-surface-variant hover:text-white bg-white/5 border border-border-glass transition-colors cursor-pointer"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 bg-primary text-on-primary text-xs font-bold rounded-xl transition-all glow-teal active:scale-95 cursor-pointer"
                >
                  Tạo &amp; Chọn
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* QUICK CREATE EMPLOYEE MODAL */}
      {isNewEmployeeModalOpen && (
        <div className="fixed inset-0 z-[110] flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-in fade-in duration-200">
          <div className="relative w-full max-w-md bg-[#0A0E17]/95 border border-white/[0.08] backdrop-blur-[12px] p-6 rounded-2xl shadow-2xl flex flex-col z-50">
            <div className="flex justify-between items-center mb-4 pb-2 border-b border-white/5">
              <h3 className="text-sm font-bold text-white flex items-center gap-2">
                <span className="material-symbols-outlined text-primary text-base">person_add</span>
                Thêm nhân viên mới
              </h3>
              <button
                type="button"
                onClick={() => setIsNewEmployeeModalOpen(false)}
                className="p-1 hover:bg-white/10 rounded-full text-on-surface-variant cursor-pointer transition-colors"
              >
                <span className="material-symbols-outlined text-base">close</span>
              </button>
            </div>

            <form onSubmit={handleCreateEmployeeQuick} className="space-y-4">
              <div>
                <label className="block text-[10px] uppercase tracking-wider text-text-variant mb-1.5 font-bold">Tên nhân viên *</label>
                <input
                  type="text"
                  required
                  value={newEmployeeName}
                  onChange={(e) => setNewEmployeeName(e.target.value)}
                  className="w-full bg-surface-low border border-border-glass rounded-xl px-3.5 py-2 text-xs text-white outline-none focus:border-primary transition-all"
                  placeholder="Nhập tên nhân viên..."
                />
              </div>

              <div>
                <label className="block text-[10px] uppercase tracking-wider text-text-variant mb-1.5 font-bold">Số điện thoại</label>
                <input
                  type="text"
                  value={newEmployeePhone}
                  onChange={(e) => setNewEmployeePhone(e.target.value)}
                  className="w-full bg-surface-low border border-border-glass rounded-xl px-3.5 py-2 text-xs text-white outline-none focus:border-primary transition-all font-mono"
                  placeholder="Nhập số điện thoại..."
                />
              </div>

              <div>
                <label className="block text-[10px] uppercase tracking-wider text-text-variant mb-1.5 font-bold">Email</label>
                <input
                  type="email"
                  value={newEmployeeEmail}
                  onChange={(e) => setNewEmployeeEmail(e.target.value)}
                  className="w-full bg-surface-low border border-border-glass rounded-xl px-3.5 py-2 text-xs text-white outline-none focus:border-primary transition-all font-mono"
                  placeholder="Nhập email..."
                />
              </div>

              <div className="pt-2 flex justify-end gap-3 border-t border-white/5 mt-4">
                <button
                  type="button"
                  onClick={() => setIsNewEmployeeModalOpen(false)}
                  className="px-4 py-2 rounded-xl text-xs font-semibold text-on-surface-variant hover:text-white bg-white/5 border border-border-glass transition-colors cursor-pointer"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 bg-primary text-on-primary text-xs font-bold rounded-xl transition-all glow-teal active:scale-95 cursor-pointer"
                >
                  Tạo &amp; Chọn
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default function SalesPage() {
  return (
    <Suspense fallback={<div className="p-8 text-center text-white text-sm">Đang tải trang hóa đơn...</div>}>
      <SalesContent />
    </Suspense>
  );
}
