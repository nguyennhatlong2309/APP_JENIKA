'use client';

import { useState, useEffect } from 'react';

interface ProductItem {
  id: number;
  tenSanPham: string;
  giaNhapHienTai: number;
  donViTinh?: { tenDonVi: string } | null;
}

interface CategoryItem {
  id: number;
  tenDanhMuc: string;
}

interface UnitItem {
  id: number;
  tenDonVi: string;
}

interface GroupItem {
  id: number;
  tenNhom: string;
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

interface ImportDetail {
  sanPham: { id: number; tenSanPham?: string };
  soLuong: number;
  donVi?: string;
  giaNhap: number;
  thanhTien?: number;
}

interface PurchaseOrder {
  id: number;
  thoiGian: string;
  ngayNhan?: string;
  doiTac?: PartnerItem | null;
  nhanVien?: EmployeeItem | null;
  tongTien: number;
  daThanhToan: number;
  tienNo: number;
  trangThai: string; // Chờ nhận | Hoàn thành | Hủy
  ghiChu?: string;
  chiTietNhapHangs?: ImportDetail[];
}

export default function PurchasesPage() {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isViewOpen, setIsViewOpen] = useState(false);
  const [selectedViewOrder, setSelectedViewOrder] = useState<PurchaseOrder | null>(null);
  const [editingOrder, setEditingOrder] = useState<PurchaseOrder | null>(null);

  // Metadata states
  const [availableProducts, setAvailableProducts] = useState<ProductItem[]>([]);
  const [partners, setPartners] = useState<PartnerItem[]>([]);
  const [employees, setEmployees] = useState<EmployeeItem[]>([]);

  // New Product Modal states
  const [isNewProductModalOpen, setIsNewProductModalOpen] = useState(false);
  const [activeRowIndex, setActiveRowIndex] = useState<number | null>(null);
  const [categories, setCategories] = useState<CategoryItem[]>([]);
  const [groups, setGroups] = useState<GroupItem[]>([]);
  const [units, setUnits] = useState<UnitItem[]>([]);

  // New Product Form states
  const [newProdName, setNewProdName] = useState('');
  const [newProdGiaNhap, setNewProdGiaNhap] = useState<number>(0);
  const [newProdGiaBan, setNewProdGiaBan] = useState<number>(0);
  const [newProdSoLuong, setNewProdSoLuong] = useState<number>(0);
  const [newProdCanhBao, setNewProdCanhBao] = useState<number>(5);
  const [newProdDanhMucId, setNewProdDanhMucId] = useState<string>('');
  const [newProdNhomId, setNewProdNhomId] = useState<string>('');
  const [newProdDonViId, setNewProdDonViId] = useState<string>('');
  const [newProdGhiChu, setNewProdGhiChu] = useState('');

  // Form states
  const [formOrderId, setFormOrderId] = useState<number | ''>('');
  const [formPartnerId, setFormPartnerId] = useState<string>('');
  const [formEmployeeId, setFormEmployeeId] = useState<string>('');
  const [formStatus, setFormStatus] = useState<string>('Chờ nhận'); // Chờ nhận | Hoàn thành | Hủy
  const [formPaid, setFormPaid] = useState<number>(0);
  const [formNotes, setFormNotes] = useState<string>('');
  const [formDiaChiGiaoHang, setFormDiaChiGiaoHang] = useState<string>('');
  const [formThoiGian, setFormThoiGian] = useState<string>('');
  const [formNgayLap, setFormNgayLap] = useState<string>(''); // Day of actual receipt

  // Separated lists for purchased and gifted details
  const [purchasedDetails, setPurchasedDetails] = useState<ImportDetail[]>([
    { sanPham: { id: 0 }, soLuong: 10, giaNhap: 0 }
  ]);
  const [giftDetails, setGiftDetails] = useState<ImportDetail[]>([]);

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

  // Tab filter
  const [statusFilter, setStatusFilter] = useState<'Tất cả' | 'Chờ nhận' | 'Hoàn thành' | 'Hủy'>('Tất cả');
  const [searchQuery, setSearchQuery] = useState('');

  // Data states
  const [orders, setOrders] = useState<PurchaseOrder[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalError, setModalError] = useState<string | null>(null);

  // Load Metadata
  useEffect(() => {
    async function loadMetadata() {
      try {
        const prodRes = await fetch("http://localhost:8080/api/v1/san-pham");
        if (prodRes.ok) setAvailableProducts(await prodRes.json());

        const partnerRes = await fetch("http://localhost:8080/api/v1/metadata/doi-tac");
        if (partnerRes.ok) {
          const list = await partnerRes.json();
          setPartners(list);
          if (list.length > 0) setFormPartnerId(list[0].id.toString());
        }

        const empRes = await fetch("http://localhost:8080/api/v1/metadata/nhan-vien");
        if (empRes.ok) {
          const list = await empRes.json();
          setEmployees(list);
          if (list.length > 0) setFormEmployeeId(list[0].id.toString());
        }

        const catRes = await fetch("http://localhost:8080/api/v1/metadata/danh-muc");
        if (catRes.ok) setCategories(await catRes.json());

        const groupRes = await fetch("http://localhost:8080/api/v1/metadata/nhom-san-pham");
        if (groupRes.ok) setGroups(await groupRes.json());

        const unitRes = await fetch("http://localhost:8080/api/v1/metadata/don-vi");
        if (unitRes.ok) setUnits(await unitRes.json());
      } catch (err) {
        console.error("Error fetching purchases metadata:", err);
      }
    }
    loadMetadata();
  }, []);

  // Fetch core list
  const loadData = async () => {
    try {
      setLoading(true);
      const res = await fetch("http://localhost:8080/api/v1/nhap-hang");
      if (res.ok) setOrders(await res.json());

      const prodRes = await fetch("http://localhost:8080/api/v1/san-pham");
      if (prodRes.ok) setAvailableProducts(await prodRes.json());
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

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

  const openCreateModal = () => {
    setEditingOrder(null);
    const nextId = orders.length > 0 ? Math.max(...orders.map(o => o.id)) + 1 : 1;
    setFormOrderId(nextId);
    setFormPartnerId(partners.length > 0 ? partners[0].id.toString() : '');
    const firstPartner = partners.length > 0 ? partners[0] : null;
    setCustomerQuery(firstPartner ? firstPartner.ten : '');
    setFormEmployeeId(employees.length > 0 ? employees[0].id.toString() : '');
    const firstEmployee = employees.length > 0 ? employees[0] : null;
    setEmployeeQuery(firstEmployee ? firstEmployee.tenNhanVien : '');
    setFormStatus('Chờ nhận');
    setFormPaid(0);
    setFormNotes('');
    setFormDiaChiGiaoHang(firstPartner?.diaChi || '');

    const now = new Date();
    const tzOffset = now.getTimezoneOffset() * 60000;
    const localISOTime = (new Date(now.getTime() - tzOffset)).toISOString().slice(0, 16);
    setFormThoiGian(localISOTime);
    setFormNgayLap('');

    setPurchasedDetails([{ sanPham: { id: 0 }, soLuong: 10, giaNhap: 0 }]);
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
    setIsModalOpen(true);
  };

  const openEditModal = (order: PurchaseOrder) => {
    setEditingOrder(order);
    setFormOrderId(order.id);
    setFormPartnerId(order.doiTac ? order.doiTac.id.toString() : '');
    setCustomerQuery(order.doiTac ? order.doiTac.ten : '');
    setFormEmployeeId(order.nhanVien ? order.nhanVien.id.toString() : '');
    setEmployeeQuery(order.nhanVien ? order.nhanVien.tenNhanVien : '');
    setFormStatus(order.trangThai);
    setFormPaid(order.daThanhToan);
    setFormNotes(order.ghiChu || '');
    setFormDiaChiGiaoHang(order.doiTac?.diaChi || '');
    setFormThoiGian(order.thoiGian ? order.thoiGian.slice(0, 16) : '');
    setFormNgayLap(order.ngayNhan || '');

    const details = order.chiTietNhapHangs || [];
    const pDetails = details.map(item => ({
      sanPham: { id: item.sanPham.id },
      soLuong: item.soLuong,
      giaNhap: item.giaNhap,
      donVi: item.donVi
    }));

    setPurchasedDetails(pDetails.length > 0 ? pDetails : [{ sanPham: { id: 0 }, soLuong: 10, giaNhap: 0 }]);
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
    setIsModalOpen(true);
  };

  const handlePurchasedDetailsChange = (index: number, key: keyof ImportDetail, value: any) => {
    if (key === 'sanPham' && value === 'NEW_PRODUCT') {
      setActiveRowIndex(index);

      // Reset form variables for new product modal
      setNewProdName('');
      setNewProdGiaNhap(0);
      setNewProdGiaBan(0);
      setNewProdSoLuong(0);
      setNewProdCanhBao(5);
      setNewProdDanhMucId(categories.length > 0 ? categories[0].id.toString() : '');
      setNewProdNhomId(groups.length > 0 ? groups[0].id.toString() : '');
      setNewProdDonViId(units.length > 0 ? units[0].id.toString() : '');
      setNewProdGhiChu('');

      setIsNewProductModalOpen(true);
      return;
    }

    const next = [...purchasedDetails];
    if (key === 'sanPham') {
      const spId = parseInt(value) || 0;
      const sp = availableProducts.find(p => p.id === spId);
      next[index] = {
        ...next[index],
        sanPham: { id: spId },
        giaNhap: sp ? sp.giaNhapHienTai : 0
      };
    } else {
      next[index] = { ...next[index], [key]: value } as any;
    }
    setPurchasedDetails(next);
    setModalError(null);
  };

  const handleGiftDetailsChange = (index: number, key: keyof ImportDetail, value: any) => {
    const next = [...giftDetails];
    if (key === 'sanPham') {
      const spId = parseInt(value) || 0;
      next[index] = {
        ...next[index],
        sanPham: { id: spId },
        giaNhap: 0
      };
    } else {
      next[index] = { ...next[index], [key]: value } as any;
    }
    setGiftDetails(next);
    setModalError(null);
  };

  const addPurchasedRow = () => {
    setPurchasedDetails([...purchasedDetails, { sanPham: { id: 0 }, soLuong: 10, giaNhap: 0 }]);
  };

  const removePurchasedRow = (index: number) => {
    if (purchasedDetails.length === 1) return;
    setPurchasedDetails(purchasedDetails.filter((_, i) => i !== index));
  };

  const addGiftRow = () => {
    setGiftDetails([...giftDetails, { sanPham: { id: 0 }, soLuong: 10, giaNhap: 0 }]);
  };

  const removeGiftRow = (index: number) => {
    setGiftDetails(giftDetails.filter((_, i) => i !== index));
  };

  const calculateTotal = () => {
    return purchasedDetails.reduce((sum, item) => {
      if (!item.sanPham.id) return sum;
      return sum + (item.giaNhap * item.soLuong);
    }, 0);
  };

  const handleCreatePartnerQuick = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newCustomerName.trim()) {
      alert("Vui lòng nhập tên nhà cung cấp mới.");
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
        alert(`Không thể tạo nhà cung cấp: ${txt}`);
      }
    } catch (err) {
      console.error(err);
      alert("Lỗi kết nối khi tạo nhà cung cấp.");
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

  const handleCreateProductQuick = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newProdName.trim()) {
      alert("Vui lòng nhập tên sản phẩm.");
      return;
    }

    const payload = {
      tenSanPham: newProdName,
      giaNhapHienTai: newProdGiaNhap,
      giaBanHienTai: newProdGiaBan || newProdGiaNhap,
      soLuongTon: newProdSoLuong,
      canhBaoTonKho: newProdCanhBao,
      danhMuc: newProdDanhMucId ? { id: parseInt(newProdDanhMucId) } : null,
      nhomSanPham: newProdNhomId ? { id: parseInt(newProdNhomId) } : null,
      donViTinh: newProdDonViId ? { id: parseInt(newProdDonViId) } : null,
      ghiChu: newProdGhiChu || null
    };

    try {
      const res = await fetch("http://localhost:8080/api/v1/san-pham", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });
      if (res.ok) {
        const savedProduct = await res.json();

        // Reload product list
        const prodRes = await fetch("http://localhost:8080/api/v1/san-pham");
        let updatedProducts = availableProducts;
        if (prodRes.ok) {
          const list = await prodRes.json();
          setAvailableProducts(list);
          updatedProducts = list;
        }

        // Auto select in row
        if (activeRowIndex !== null) {
          const next = [...purchasedDetails];
          next[activeRowIndex] = {
            ...next[activeRowIndex],
            sanPham: { id: savedProduct.id },
            giaNhap: savedProduct.giaNhapHienTai
          };
          setPurchasedDetails(next);
        }

        // Close modal and reset
        setIsNewProductModalOpen(false);
        setActiveRowIndex(null);
      } else {
        const txt = await res.text();
        alert(`Không thể tạo sản phẩm: ${txt}`);
      }
    } catch (err) {
      console.error(err);
      alert("Lỗi kết nối khi tạo sản phẩm.");
    }
  };

  const handleCreatePurchaseOrder = async (e: React.FormEvent) => {
    e.preventDefault();
    setModalError(null);

    const combinedDetails = purchasedDetails;
    if (combinedDetails.some(item => !item.sanPham.id)) {
      setModalError("Vui lòng chọn sản phẩm cho toàn bộ các dòng.");
      return;
    }

    const productIds = combinedDetails.map(item => item.sanPham.id);
    const duplicatedId = productIds.find((id, index) => productIds.indexOf(id) !== index);
    if (duplicatedId) {
      const duplicateProduct = availableProducts.find(p => p.id === duplicatedId);
      const prodName = duplicateProduct ? duplicateProduct.tenSanPham : "sản phẩm";
      setModalError(`Sản phẩm "${prodName}" bị trùng lặp trong danh sách. Vui lòng gộp số lượng của chúng lại thành một dòng.`);
      return;
    }

    const payload = {
      id: formOrderId || null,
      doiTac: formPartnerId ? { id: parseInt(formPartnerId) } : null,
      nhanVien: formEmployeeId ? { id: parseInt(formEmployeeId) } : null,
      trangThai: formStatus,
      daThanhToan: formPaid,
      ghiChu: formNotes,
      thoiGian: formThoiGian ? new Date(formThoiGian).toISOString() : null,
      ngayNhan: formNgayLap || null,
      chiTietNhapHangs: combinedDetails.map(item => {
        const sp = availableProducts.find(p => p.id === item.sanPham.id)!;
        return {
          sanPham: { id: sp.id },
          soLuong: item.soLuong,
          giaNhap: item.giaNhap,
          donVi: sp.donViTinh ? sp.donViTinh.tenDonVi : 'ly'
        };
      })
    };

    try {
      const url = editingOrder
        ? `http://localhost:8080/api/v1/nhap-hang/${editingOrder.id}`
        : "http://localhost:8080/api/v1/nhap-hang";
      const method = editingOrder ? "PUT" : "POST";

      const res = await fetch(url, {
        method: method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });

      if (res.ok) {
        setFormPaid(0);
        setFormNotes('');
        setPurchasedDetails([{ sanPham: { id: 0 }, soLuong: 10, giaNhap: 0 }]);
        setGiftDetails([]);
        setEditingOrder(null);
        setIsModalOpen(false);
        loadData();
      } else {
        const txt = await res.text();
        setModalError(`Lỗi lưu đơn nhập: ${txt}`);
      }
    } catch (err) {
      console.error(err);
      setModalError("Không thể kết nối đến máy chủ.");
    }
  };

  const handleUpdateStatus = async (orderId: number, status: string) => {
    try {
      const res = await fetch(`http://localhost:8080/api/v1/nhap-hang/${orderId}/status?status=${encodeURIComponent(status)}`, {
        method: "PUT"
      });
      if (res.ok) {
        loadData();
      } else {
        alert("Không thể cập nhật trạng thái đơn nhập. Có thể tồn kho sản phẩm không đủ để hoàn tác.");
      }
    } catch (err) {
      console.error(err);
      alert("Lỗi kết nối máy chủ.");
    }
  };

  const exportToCSV = () => {
    let csvContent = '\uFEFF'; // UTF-8 BOM
    csvContent += 'Mã nhập,Nhà cung cấp,Ngày lập,Tổng cộng,Đã trả,Còn nợ,Trạng thái\n';
    orders.forEach(o => {
      const date = new Date(o.thoiGian).toLocaleString('vi-VN');
      const ncc = o.doiTac ? o.doiTac.ten : '---';
      csvContent += `NH-${o.id},"${ncc}","${date}",${o.tongTien},${o.daThanhToan},${o.tienNo},"${o.trangThai}"\n`;
    });
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.setAttribute("href", url);
    link.setAttribute("download", "danh_sach_don_nhap_hang.csv");
    link.click();
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

  // Filters
  const filteredOrders = orders.filter((o) => {
    if (statusFilter !== 'Tất cả' && o.trangThai !== statusFilter) return false;
    if (searchQuery) {
      const q = searchQuery.toLowerCase();
      const ncc = o.doiTac ? o.doiTac.ten.toLowerCase() : '';
      if (!ncc.includes(q) && !`nh-${o.id}`.includes(q)) return false;
    }
    return true;
  });

  // Procurement Stats
  const totalProcurement = orders.filter(o => o.trangThai === 'Hoàn thành').reduce((sum, o) => sum + o.tongTien, 0);
  const pendingShipments = orders.filter(o => o.trangThai === 'Chờ nhận').length;
  const uniqueSuppliers = Array.from(new Set(orders.map(o => o.doiTac?.id).filter(id => id !== undefined))).length;

  return (
    <div className="p-8 space-y-6 max-w-[1600px] mx-auto w-full relative">
      {/* Page Header */}
      <div className="flex justify-between items-end">
        <div>
          <h2 className="text-3xl font-bold text-white">Quản lý Đơn Nhập Hàng</h2>
          <p className="text-text-variant text-sm mt-1">Lập đơn, theo dõi tiến độ nhập nguyên liệu/sản phẩm và quản lý công nợ nhà cung cấp.</p>
        </div>
        <button
          onClick={openCreateModal}
          className="flex items-center gap-2 bg-primary text-on-primary px-5 py-2.5 rounded-lg font-semibold glow-teal transition-all active:scale-95 cursor-pointer"
        >
          <span className="material-symbols-outlined text-[20px]">add</span>
          Lập đơn nhập hàng mới
        </button>
      </div>

      {loading ? (
        <div className="text-center py-20 text-white text-sm">Đang tải danh sách đơn nhập hàng...</div>
      ) : (
        <>
          {/* Stats Row */}
          <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
            <div className="glass-surface p-6 rounded-xl group hover:border-primary/40 transition-colors">
              <div className="flex items-center justify-between mb-4">
                <span className="text-on-surface-variant text-sm font-semibold">Tổng tiền nhập hàng</span>
                <span className="material-symbols-outlined text-primary">account_balance_wallet</span>
              </div>
              <div className="text-xl font-bold text-white">
                {formatVND(totalProcurement)}
              </div>
              <div className="mt-2 text-xs text-on-surface-variant">
                Đơn hàng hoàn thành tháng này
              </div>
            </div>

            <div className="glass-surface p-6 rounded-xl group hover:border-warning/40 transition-colors">
              <div className="flex items-center justify-between mb-4">
                <span className="text-on-surface-variant text-sm font-semibold">Đơn hàng chờ nhận</span>
                <span className="material-symbols-outlined text-warning">local_shipping</span>
              </div>
              <div className="text-xl font-bold text-white">{pendingShipments} Đơn hàng</div>
              <div className="mt-2 text-xs text-on-surface-variant">
                Đang vận chuyển / Chờ kiểm kho
              </div>
            </div>

            <div className="glass-surface p-6 rounded-xl group hover:border-primary/40 transition-colors">
              <div className="flex items-center justify-between mb-4">
                <span className="text-on-surface-variant text-sm font-semibold">Số nhà phân phối</span>
                <span className="material-symbols-outlined text-primary">handshake</span>
              </div>
              <div className="text-xl font-bold text-white">{uniqueSuppliers} Đối tác</div>
              <div className="mt-2 text-xs text-on-surface-variant">
                Đã thực hiện giao dịch nhập
              </div>
            </div>

            <div className="glass-surface p-6 rounded-xl group hover:border-primary/40 transition-colors">
              <div className="flex items-center justify-between mb-4">
                <span className="text-on-surface-variant text-sm font-semibold">Tỉ lệ hoàn thành đơn</span>
                <span className="material-symbols-outlined text-primary">pie_chart</span>
              </div>
              <div className="text-xl font-bold text-white">
                {orders.length > 0 ? Math.round((orders.filter(o => o.trangThai === 'Hoàn thành').length / orders.length) * 100) : 0}%
              </div>
              <div className="mt-2 text-xs text-on-surface-variant">
                Tiến độ xử lý chuỗi cung ứng
              </div>
            </div>
          </div>

          {/* Main Table Section */}
          <div className="glass-surface rounded-2xl overflow-hidden">
            {/* Table Content */}
            <div className="overflow-x-auto">
              <table className="w-full text-left">
                <thead className="bg-white/[0.03] text-on-surface-variant uppercase text-[10px] tracking-widest font-bold">
                  <tr>
                    <th className="px-6 py-4 border-b border-border-glass">Mã đơn nhập</th>
                    <th className="px-6 py-4 border-b border-border-glass">Nhà cung cấp</th>
                    <th className="px-6 py-4 border-b border-border-glass">Ngày nhập</th>
                    <th className="px-6 py-4 border-b border-border-glass">Tổng cộng</th>
                    <th className="px-6 py-4 border-b border-border-glass">Đã thanh toán</th>
                    <th className="px-6 py-4 border-b border-border-glass">Công nợ nợ</th>
                    <th className="px-6 py-4 border-b border-border-glass text-center">Trạng thái</th>
                    <th className="px-6 py-4 border-b border-border-glass text-center">Thao tác</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-white/[0.04]">
                  {filteredOrders.map((order) => (
                    <tr key={order.id} className="hover:bg-white/[0.02] transition-colors group">
                      <td className="px-6 py-4 font-mono text-primary text-sm font-bold">NH-{order.id}</td>
                      <td className="px-6 py-4 font-semibold text-xs text-white">
                        {order.doiTac ? order.doiTac.ten : '---'}
                      </td>
                      <td className="px-6 py-4 text-on-surface-variant text-xs">
                        {new Date(order.thoiGian).toLocaleString('vi-VN')}
                      </td>
                      <td className="px-6 py-4 font-semibold text-xs text-white">
                        {formatVND(order.tongTien)}
                      </td>
                      <td className="px-6 py-4 text-xs text-success">{formatVND(order.daThanhToan)}</td>
                      <td className="px-6 py-4 text-xs text-error font-semibold">{formatVND(order.tienNo)}</td>
                      <td className="px-6 py-4 text-center">
                        <span
                          className={`px-2.5 py-1 rounded-full text-[9px] font-bold uppercase tracking-wider border ${order.trangThai === 'Hoàn thành'
                              ? 'bg-success/10 border-success/30 text-success'
                              : order.trangThai === 'Chờ nhận'
                                ? 'bg-warning/10 border-warning/30 text-warning'
                                : 'bg-error/10 border-error/30 text-error'
                            }`}
                        >
                          {order.trangThai}
                        </span>
                      </td>
                      <td className="px-6 py-4 text-center">
                        <div className="flex items-center justify-center gap-2">
                          <button
                            onClick={async () => {
                              try {
                                const res = await fetch(`http://localhost:8080/api/v1/nhap-hang/${order.id}`);
                                if (res.ok) {
                                  setSelectedViewOrder(await res.json());
                                  setIsViewOpen(true);
                                }
                              } catch (err) {
                                console.error(err);
                              }
                            }}
                            className="p-1 hover:bg-white/5 rounded text-on-surface-variant hover:text-primary transition-colors cursor-pointer"
                            title="Xem chi tiết"
                          >
                            <span className="material-symbols-outlined text-sm">visibility</span>
                          </button>

                          <button
                            onClick={async () => {
                              try {
                                const res = await fetch(`http://localhost:8080/api/v1/nhap-hang/${order.id}`);
                                if (res.ok) {
                                  openEditModal(await res.json());
                                }
                              } catch (err) {
                                console.error(err);
                              }
                            }}
                            className="p-1 hover:bg-white/5 rounded text-on-surface-variant hover:text-primary transition-colors cursor-pointer"
                            title="Chỉnh sửa đơn"
                          >
                            <span className="material-symbols-outlined text-sm">edit</span>
                          </button>

                          {/* Quick Edit Dropdown */}
                          <select
                            value={order.trangThai}
                            onChange={(e) => handleUpdateStatus(order.id, e.target.value)}
                            className="bg-surface-lowest text-[10px] text-white border border-border-glass rounded px-1.5 py-0.5 outline-none cursor-pointer"
                          >
                            <option value="Chờ nhận">Chờ nhận</option>
                            <option value="Hoàn thành">Hoàn thành</option>
                            <option value="Hủy">Hủy</option>
                          </select>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {/* Create/Edit Purchase Order Modal */}
          {isModalOpen && (
            <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 md:p-6 overflow-y-auto bg-black/75 backdrop-blur-sm">
              {/* Backdrop (no click event to prevent closing on background click) */}
              <div className="absolute inset-0 bg-background/40"></div>

              <div className="relative w-full max-w-6xl bg-[#0A0E17]/95 border border-white/[0.02] rounded-2xl overflow-hidden shadow-2xl z-50 animate-in fade-in zoom-in-95 duration-200 flex flex-col max-h-[90vh]">
                {/* Modal Header */}
                <div className="py-2 px-6 border-b border-border-glass flex justify-between items-center bg-white/[0.02]">
                  <h2 className="text-sm font-bold text-white flex items-center gap-2">
                    <span className="material-symbols-outlined text-primary text-lg">add_shopping_cart</span>
                    {editingOrder ? `Chỉnh sửa đơn nhập NH-${editingOrder.id}` : 'Lập đơn nhập mới'}
                  </h2>
                  <button
                    type="button"
                    className="p-1 hover:bg-white/10 rounded-full transition-colors text-on-surface-variant cursor-pointer flex items-center justify-center"
                    onClick={() => setIsModalOpen(false)}
                  >
                    <span className="material-symbols-outlined text-lg">close</span>
                  </button>
                </div>

                {modalError && (
                  <div className="p-3 mx-6 mt-4 text-xs font-semibold text-error bg-error/10 border border-error/20 rounded-xl">
                    ⚠️ {modalError}
                  </div>
                )}

                <form onSubmit={handleCreatePurchaseOrder} className="flex-1 flex flex-col overflow-hidden">
                  <div className="flex-1 overflow-y-auto p-6 md:p-8 space-y-6 pr-4">

                    {/* Row 1: Số HD/ID, Ngày tạo, Nhân viên */}
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

                      {/* Field 3: Nhân viên */}
                      <div className="relative">
                        <label className="block text-[10px] uppercase tracking-wider text-text-variant mb-2 font-bold">Nhân viên</label>
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
                            placeholder="Tìm nhân viên nhận..."
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

                    {/* Row 2: NCC, SDT */}
                    <div className="grid grid-cols-3 gap-4">
                      {/* Field 4: NCC */}
                      <div className="relative col-span-2">
                        <label className="block text-[10px] uppercase tracking-wider text-text-variant mb-2 font-bold">NCC</label>
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
                            placeholder="Tìm nhà cung cấp hoặc nhập mới..."
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
                              <span className="material-symbols-outlined text-sm">person_add</span> Thêm nhà cung cấp mới...
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

                      {/* Field 5: SDT */}
                      <div>
                        <label className="block text-[10px] uppercase tracking-wider text-text-variant mb-2 font-bold">SDT</label>
                        <input
                          type="text"
                          readOnly
                          value={
                            partners.find(p => p.id.toString() === formPartnerId)?.sdt || 'Chưa cập nhật'
                          }
                          className="w-full bg-surface-low/40 border border-border-glass rounded-xl px-4 py-2.5 text-xs text-on-surface-variant font-medium outline-none cursor-not-allowed"
                        />
                      </div>
                    </div>

                    {/* Row 3: Trạng thái, Ngày nhận, Tiền đã trả */}
                    <div className="grid grid-cols-3 gap-4">
                      {/* Field 6: Trạng thái */}
                      <div>
                        <label className="block text-[10px] uppercase tracking-wider text-text-variant mb-2 font-bold">Trạng thái</label>
                        <select
                          value={formStatus}
                          onChange={(e) => setFormStatus(e.target.value)}
                          className="w-full bg-surface-low border border-border-glass rounded-xl px-4 py-2.5 text-xs outline-none text-white cursor-pointer"
                        >
                          <option value="Chờ nhận">Chờ nhận</option>
                          <option value="Hoàn thành">Hoàn thành</option>
                          <option value="Hủy">Hủy</option>
                        </select>
                      </div>

                      {/* Field 7: Ngày nhận */}
                      <div>
                        <label className="block text-[10px] uppercase tracking-wider text-text-variant mb-2 font-bold">Ngày nhận</label>
                        <input
                          type="date"
                          value={formNgayLap}
                          onChange={(e) => setFormNgayLap(e.target.value)}
                          className="w-full bg-surface-low border border-border-glass rounded-xl px-4 py-2.5 text-xs outline-none text-white focus:border-primary transition-all cursor-pointer"
                        />
                      </div>

                      {/* Field 8: Tiền đã trả */}
                      <div>
                        <label className="block text-[10px] uppercase tracking-wider text-text-variant mb-2 font-bold">Tiền đã trả (đ)</label>
                        <input
                          type="text"
                          value={formatNumberWithDots(formPaid)}
                          onChange={(e) => setFormPaid(parseNumberFromDots(e.target.value))}
                          className="w-full bg-surface-low border border-border-glass rounded-xl px-4 py-2.5 text-xs focus:border-primary outline-none text-white transition-all font-mono"
                          placeholder="Số tiền đã thanh toán..."
                        />
                      </div>
                    </div>

                    {/* Field 10: Ghi chú */}
                    <div>
                      <label className="block text-[10px] uppercase tracking-wider text-text-variant mb-2 font-bold">10. Ghi chú đơn nhập</label>
                      <textarea
                        value={formNotes}
                        onChange={(e) => setFormNotes(e.target.value)}
                        className="w-full bg-surface-low border border-border-glass rounded-xl px-4 py-2.5 text-xs focus:border-primary outline-none text-white h-16 resize-none"
                        placeholder="Ghi chú thêm thông tin về đợt nhập hàng..."
                      />
                    </div>

                    {/* Field 11: Danh sách sản phẩm mua */}
                    <div className="space-y-3 pt-2">
                      <div className="flex justify-between items-center">
                        <label className="block text-xs uppercase tracking-wider text-text-variant font-bold">11. Danh sách nguyên liệu/sản phẩm nhập</label>
                        <button
                          type="button"
                          onClick={addPurchasedRow}
                          className="text-[10px] bg-primary/20 text-primary border border-primary/20 hover:bg-primary/30 px-3 py-1 rounded-md font-bold uppercase transition-all flex items-center gap-1 cursor-pointer"
                        >
                          <span className="material-symbols-outlined text-xs">add</span> Thêm mặt hàng
                        </button>
                      </div>

                      {/* Header row for purchase items */}
                      <div className="flex items-center gap-3 px-3 text-[10px] uppercase tracking-wider text-text-variant font-bold">
                        <div className="flex-1">Sản phẩm</div>
                        <div className="w-16 text-center">SL</div>
                        <div className="w-10 text-center">ĐVT</div>
                        <div className="w-32 text-right">Giá nhập</div>
                        <div className="w-32 text-right">Thành tiền</div>
                        <div className="w-8"></div>
                      </div>

                      <div className="space-y-3 max-h-[450px] overflow-y-auto pr-1">
                        {purchasedDetails.map((row, idx) => {
                          const selectedSp = availableProducts.find(p => p.id === row.sanPham.id);
                          return (
                            <div key={idx} className="flex items-center gap-3 p-3 bg-white/5 border border-border-glass rounded-xl relative group">
                              <div className="flex-1">
                                <select
                                  required
                                  value={row.sanPham.id || ''}
                                  onChange={(e) => handlePurchasedDetailsChange(idx, 'sanPham', e.target.value)}
                                  className="w-full bg-[#111625] border border-border-glass rounded-lg px-3 py-2 text-[11px] outline-none text-white cursor-pointer"
                                >
                                  <option value="">-- Chọn sản phẩm/món uống --</option>
                                  <option value="NEW_PRODUCT" className="text-primary font-bold bg-[#141b2e]">✨ Thêm sản phẩm mới...</option>
                                  {availableProducts.map(p => (
                                    <option key={p.id} value={p.id}>
                                      {p.tenSanPham} (Gốc: {formatVND(p.giaNhapHienTai)})
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

                              <div className="w-32">
                                <input
                                  required
                                  type="text"
                                  value={formatNumberWithDots(row.giaNhap)}
                                  onChange={(e) => handlePurchasedDetailsChange(idx, 'giaNhap', parseNumberFromDots(e.target.value))}
                                  className="w-full bg-[#111625] border border-border-glass rounded-lg px-2 py-2 text-xs outline-none text-white text-right font-mono"
                                  placeholder="Giá nhập"
                                />
                              </div>

                              <div className="w-32 text-right font-mono text-xs text-primary font-bold pr-2">
                                {formatVND(row.soLuong * row.giaNhap)}
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

                  </div>

                  {/* Modal Footer (Summary & Submit) */}
                  <div className="py-4 px-6 bg-[#0A0E17] border-t border-border-glass flex justify-between items-center sticky bottom-0 z-10">
                    {/* Left: Summary Metrics */}
                    <div className="flex items-center gap-6 text-xs">
                      <div className="flex items-center gap-1.5">
                        <span className="text-text-variant font-bold">Tổng cộng:</span>
                        <span className="font-bold text-primary font-mono">{formatVND(calculateTotal())}</span>
                      </div>
                      <div className="h-4 w-px bg-white/10"></div>
                      <div className="flex items-center gap-1.5">
                        <span className="text-text-variant font-bold">Đã trả:</span>
                        <span className="font-bold text-success font-mono">{formatVND(formPaid)}</span>
                      </div>
                      <div className="h-4 w-px bg-white/10"></div>
                      <div className="flex items-center gap-1.5">
                        <span className="text-text-variant font-bold">Còn nợ:</span>
                        <span className="font-bold text-error font-mono">
                          {formatVND(Math.max(0, calculateTotal() - formPaid))}
                        </span>
                      </div>
                    </div>

                    {/* Right: Actions */}
                    <div className="flex items-center gap-3">
                      <button
                        type="button"
                        onClick={() => setIsModalOpen(false)}
                        className="px-5 py-2.5 rounded-lg text-xs font-semibold text-on-surface-variant hover:text-white bg-white/5 border border-border-glass transition-colors cursor-pointer"
                      >
                        Hủy
                      </button>
                      <button
                        type="submit"
                        className="px-6 py-2.5 bg-primary text-on-primary font-bold rounded-lg transition-all glow-teal active:scale-95 cursor-pointer"
                      >
                        {editingOrder ? 'Cập nhật đơn nhập' : 'Lập đơn nhập kho'}
                      </button>
                    </div>
                  </div>
                </form>
              </div>
            </div>
          )}

          {/* VIEW DETAILS MODAL */}
          {isViewOpen && selectedViewOrder && (
            <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
              {/* Backdrop */}
              <div className="absolute inset-0 bg-background/80 backdrop-blur-sm" onClick={() => setIsViewOpen(false)}></div>

              <div className="relative w-full max-w-lg bg-[#0A0E17]/95 border border-white/[0.02] backdrop-blur-[12px] p-6 rounded-2xl shadow-2xl flex flex-col z-50">
                <div className="flex justify-between items-center mb-4">
                  <h3 className="text-lg font-bold text-white">Chi tiết đơn nhập hàng NH-{selectedViewOrder.id}</h3>
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
                      <p>Thời gian lập: <span className="text-white font-medium">{new Date(selectedViewOrder.thoiGian).toLocaleString('vi-VN')}</span></p>
                      <p className="mt-1">Nhà cung cấp: <span className="text-white font-medium">{selectedViewOrder.doiTac ? selectedViewOrder.doiTac.ten : '---'}</span></p>
                    </div>
                    <div>
                      <p>Nhân viên nhận: <span className="text-white font-medium">{selectedViewOrder.nhanVien ? selectedViewOrder.nhanVien.tenNhanVien : '---'}</span></p>
                      <p className="mt-1">Trạng thái: <span className="text-primary font-bold">{selectedViewOrder.trangThai}</span></p>
                    </div>
                  </div>

                  {/* Items List */}
                  <div>
                    <p className="font-semibold text-white uppercase text-[10px] tracking-wider mb-2">Chi tiết nguyên liệu nhập</p>
                    <div className="space-y-2 max-h-48 overflow-y-auto pr-1">
                      {selectedViewOrder.chiTietNhapHangs?.map((ct, idx) => (
                        <div key={idx} className="flex justify-between items-center p-2.5 bg-white/5 rounded-lg border border-border-glass">
                          <div>
                            <p className="font-semibold text-white text-[11px]">{ct.sanPham.tenSanPham}</p>
                            <p className="text-[10px] text-text-variant">
                              Đơn giá: {formatVND(ct.giaNhap)} x {ct.soLuong} {ct.donVi}
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
                    <p className="text-text-variant">Tổng chi phí: <span className="text-sm font-bold text-white ml-2">{formatVND(selectedViewOrder.tongTien)}</span></p>
                    <p className="text-text-variant">Đã thanh toán: <span className="text-xs font-bold text-success ml-2">{formatVND(selectedViewOrder.daThanhToan)}</span></p>
                    <p className="text-text-variant">Nợ nhà cung cấp: <span className="text-xs font-bold text-error ml-2">{formatVND(selectedViewOrder.tienNo)}</span></p>
                  </div>
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
                    Thêm nhà cung cấp mới
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
                    <label className="block text-[10px] uppercase tracking-wider text-text-variant mb-1.5 font-bold">Tên nhà cung cấp *</label>
                    <input
                      type="text"
                      required
                      value={newCustomerName}
                      onChange={(e) => setNewCustomerName(e.target.value)}
                      className="w-full bg-surface-low border border-border-glass rounded-xl px-3.5 py-2 text-xs text-white outline-none focus:border-primary transition-all"
                      placeholder="Nhập tên nhà cung cấp..."
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
                      className="w-full bg-surface-low border border-border-glass rounded-xl px-3.5 py-2 text-xs text-white outline-none focus:border-primary transition-all font-mono"
                      placeholder="Nhập email..."
                    />
                  </div>

                  <div>
                    <label className="block text-[10px] uppercase tracking-wider text-text-variant mb-1.5 font-bold">Địa chỉ</label>
                    <textarea
                      value={newCustomerAddress}
                      onChange={(e) => setNewCustomerAddress(e.target.value)}
                      className="w-full bg-surface-low border border-border-glass rounded-xl px-3.5 py-2 text-xs text-white outline-none focus:border-primary transition-all h-20 resize-none"
                      placeholder="Nhập địa chỉ nhà cung cấp..."
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

          {/* QUICK CREATE PRODUCT MODAL */}
          {isNewProductModalOpen && (
            <div className="fixed inset-0 z-[110] flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-in fade-in duration-200">
              <div className="relative w-full max-w-2xl bg-[#0A0E17]/95 border border-white/[0.08] backdrop-blur-[12px] p-6 rounded-2xl shadow-2xl flex flex-col z-50">
                <div className="flex justify-between items-center mb-4 pb-2 border-b border-white/5">
                  <h3 className="text-sm font-bold text-white flex items-center gap-2">
                    <span className="material-symbols-outlined text-primary text-base">add_box</span>
                    Thêm sản phẩm mới (Tự động gộp nếu trùng tên + giá nhập)
                  </h3>
                  <button
                    type="button"
                    onClick={() => {
                      setIsNewProductModalOpen(false);
                      setActiveRowIndex(null);
                    }}
                    className="p-1 hover:bg-white/10 rounded-full text-on-surface-variant cursor-pointer transition-colors"
                  >
                    <span className="material-symbols-outlined text-base">close</span>
                  </button>
                </div>

                <form onSubmit={handleCreateProductQuick} className="space-y-4">
                  {/* Grid 1: Name and Unit */}
                  <div className="grid grid-cols-3 gap-4">
                    <div className="col-span-2">
                      <label className="block text-[10px] uppercase tracking-wider text-text-variant mb-1.5 font-bold">Tên sản phẩm / nguyên liệu *</label>
                      <input
                        type="text"
                        required
                        value={newProdName}
                        onChange={(e) => setNewProdName(e.target.value)}
                        className="w-full bg-surface-low border border-border-glass rounded-xl px-3.5 py-2 text-xs text-white outline-none focus:border-primary transition-all"
                        placeholder="Nhập tên sản phẩm..."
                      />
                    </div>
                    <div>
                      <label className="block text-[10px] uppercase tracking-wider text-text-variant mb-1.5 font-bold">Đơn vị tính *</label>
                      <select
                        required
                        value={newProdDonViId}
                        onChange={(e) => setNewProdDonViId(e.target.value)}
                        className="w-full bg-[#111625] border border-border-glass rounded-xl px-3.5 py-2.5 text-xs text-white outline-none focus:border-primary transition-all cursor-pointer"
                      >
                        <option value="">-- Chọn ĐVT --</option>
                        {units.map(u => (
                          <option key={u.id} value={u.id}>{u.tenDonVi}</option>
                        ))}
                      </select>
                    </div>
                  </div>

                  {/* Grid 2: Prices */}
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-[10px] uppercase tracking-wider text-text-variant mb-1.5 font-bold">Giá nhập hiện tại (đ) *</label>
                      <input
                        type="text"
                        required
                        value={formatNumberWithDots(newProdGiaNhap)}
                        onChange={(e) => setNewProdGiaNhap(parseNumberFromDots(e.target.value))}
                        className="w-full bg-surface-low border border-border-glass rounded-xl px-3.5 py-2 text-xs text-white outline-none focus:border-primary transition-all font-mono"
                        placeholder="0"
                      />
                    </div>
                    <div>
                      <label className="block text-[10px] uppercase tracking-wider text-text-variant mb-1.5 font-bold">Giá bán hiện tại (đ)</label>
                      <input
                        type="text"
                        value={formatNumberWithDots(newProdGiaBan)}
                        onChange={(e) => setNewProdGiaBan(parseNumberFromDots(e.target.value))}
                        className="w-full bg-surface-low border border-border-glass rounded-xl px-3.5 py-2 text-xs text-white outline-none focus:border-primary transition-all font-mono"
                        placeholder="Để trống = Giá nhập"
                      />
                    </div>
                  </div>

                  {/* Grid 3: Quantities */}
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-[10px] uppercase tracking-wider text-text-variant mb-1.5 font-bold">Số lượng tồn kho ban đầu</label>
                      <input
                        type="number"
                        min="0"
                        value={newProdSoLuong}
                        onChange={(e) => setNewProdSoLuong(parseInt(e.target.value) || 0)}
                        className="w-full bg-surface-low border border-border-glass rounded-xl px-3.5 py-2 text-xs text-white outline-none focus:border-primary transition-all"
                        placeholder="0"
                      />
                    </div>
                    <div>
                      <label className="block text-[10px] uppercase tracking-wider text-text-variant mb-1.5 font-bold">Cảnh báo tồn kho (Dưới mức này)</label>
                      <input
                        type="number"
                        min="0"
                        value={newProdCanhBao}
                        onChange={(e) => setNewProdCanhBao(parseInt(e.target.value) || 0)}
                        className="w-full bg-surface-low border border-border-glass rounded-xl px-3.5 py-2 text-xs text-white outline-none focus:border-primary transition-all"
                        placeholder="5"
                      />
                    </div>
                  </div>

                  {/* Grid 4: Category and Group */}
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-[10px] uppercase tracking-wider text-text-variant mb-1.5 font-bold">Danh mục sản phẩm *</label>
                      <select
                        required
                        value={newProdDanhMucId}
                        onChange={(e) => setNewProdDanhMucId(e.target.value)}
                        className="w-full bg-[#111625] border border-border-glass rounded-xl px-3.5 py-2.5 text-xs text-white outline-none focus:border-primary transition-all cursor-pointer"
                      >
                        <option value="">-- Chọn danh mục --</option>
                        {categories.map(c => (
                          <option key={c.id} value={c.id}>{c.tenDanhMuc}</option>
                        ))}
                      </select>
                    </div>
                    <div>
                      <label className="block text-[10px] uppercase tracking-wider text-text-variant mb-1.5 font-bold">Nhóm sản phẩm *</label>
                      <select
                        required
                        value={newProdNhomId}
                        onChange={(e) => setNewProdNhomId(e.target.value)}
                        className="w-full bg-[#111625] border border-border-glass rounded-xl px-3.5 py-2.5 text-xs text-white outline-none focus:border-primary transition-all cursor-pointer"
                      >
                        <option value="">-- Chọn nhóm sản phẩm --</option>
                        {groups.map(g => (
                          <option key={g.id} value={g.id}>{g.tenNhom}</option>
                        ))}
                      </select>
                    </div>
                  </div>

                  {/* Textarea: Notes */}
                  <div>
                    <label className="block text-[10px] uppercase tracking-wider text-text-variant mb-1.5 font-bold">Ghi chú sản phẩm</label>
                    <textarea
                      value={newProdGhiChu}
                      onChange={(e) => setNewProdGhiChu(e.target.value)}
                      className="w-full bg-surface-low border border-border-glass rounded-xl px-3.5 py-2 text-xs text-white outline-none focus:border-primary transition-all h-20 resize-none"
                      placeholder="Nhập ghi chú hoặc mô tả ngắn gọn..."
                    />
                  </div>

                  <div className="pt-2 flex justify-end gap-3 border-t border-white/5 mt-4">
                    <button
                      type="button"
                      onClick={() => {
                        setIsNewProductModalOpen(false);
                        setActiveRowIndex(null);
                      }}
                      className="px-4 py-2 rounded-xl text-xs font-semibold text-on-surface-variant hover:text-white bg-white/5 border border-border-glass transition-colors cursor-pointer"
                    >
                      Hủy
                    </button>
                    <button
                      type="submit"
                      className="px-5 py-2 bg-primary text-on-primary text-xs font-bold rounded-xl transition-all glow-teal active:scale-95 cursor-pointer"
                    >
                      Tạo sản phẩm
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
