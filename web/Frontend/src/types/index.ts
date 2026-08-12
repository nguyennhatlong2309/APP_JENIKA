export interface CategoryItem {
  id: number;
  tenDanhMuc: string;
  moTa?: string;
}

export interface ExpenseCategoryItem {
  id: number;
  ten: string;
}

export interface UnitItem {
  id: number;
  tenDonVi: string;
}

export interface GroupItem {
  id: number;
  tenNhom: string;
}

export interface ProductItem {
  id: number;
  tenSanPham: string;
  giaNhapHienTai: number;
  giaBanHienTai: number;
  soLuongTon: number;
  canhBaoTonKho: number;
  trangThai: string;
  danhMuc?: CategoryItem | null;
  donViTinh?: UnitItem | null;
  nhomSanPham?: GroupItem | null;
  biXoa: boolean;
  ghiChu?: string;
}

export interface BusinessPartner {
  id: number;
  ten: string;
  sdt?: string;
  diaChi?: string;
  email?: string;
}

export type PartnerItem = BusinessPartner;

export interface EmployeeItem {
  id: number;
  tenNhanVien: string;
  sdt?: string;
  email?: string;
  vaiTro?: string;
}

export type Employee = EmployeeItem;

export interface ActivityLog {
  id: number;
  thoiGian: string;
  thaoTac: string; // THEM | SUA | XOA
  tab: string; // ban_hang | nhap_hang | san_pham | doi_tac
  maBanGhi: string;
  moTa: string;
}

export interface ImportDetail {
  sanPham: { id: number; tenSanPham?: string };
  soLuong: number;
  donVi?: string;
  giaNhap: number;
  thanhTien?: number;
}

export interface PurchaseOrder {
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
  anhHoaDonUrl?: string;
  chiTietNhapHangs?: ImportDetail[];
}

export interface SalesOrderDetail {
  sanPham: { id: number; tenSanPham?: string };
  soLuong: number;
  donVi?: string;
  giaBan?: number;
  thanhTien?: number;
  isGift?: boolean;
  giaVon?: number;
}

export interface SaleOrder {
  id: number;
  thoiGian: string;
  doiTac?: PartnerItem | null;
  nhanVien?: EmployeeItem | null;
  tongTien: number;
  tienDaThanhToan: number;
  tienNo: number;
  trangThai: string; // Hoàn thành | Hủy
  ghiChu?: string;
  diaChiGiaoHang?: string | null;
  ngayLap?: string | null;
  tongCost?: number;
  loiNhuan?: number;
  tienQuaTang?: number;
  anhHoaDonUrl?: string;
  chiTietBanHangs?: SalesOrderDetail[];
}

export interface ExpenseItem {
  dbId: number;
  id: string; // e.g. '#TC-1'
  name: string; // moTa
  category: string; // tenLoai
  idLoai: number | null;
  date: string; // formatted to locales
  rawDate: string; // YYYY-MM-DD
  tienThu: number;
  tienChi: number;
  method: string;
  methodIcon: string;
  status: string;
  idNhanVien: number | null;
  tenNhanVien: string | null;
}

export interface ThuChiDbItem {
  id: number;
  thoiGian: string;
  idLoai: number | null;
  tenLoai: string | null;
  tienThu: number | null;
  tienChi: number | null;
  moTa: string | null;
  phuongThuc: string | null;
  trangThai: string | null;
  idNhanVien: number | null;
  tenNhanVien: string | null;
}

export interface ExcelColumn {
  key: string;
  label: string;
  selected: boolean;
}

export interface ExcelTemplate {
  id: number;
  tenMau: string;
  loaiBaoCao: 'inventory' | 'sales' | 'purchases' | 'expenses';
  moTa?: string;
  columnConfig?: ExcelColumn[];
  biXoa: boolean;
}

export interface DashboardData {
  dailyRevenue: number;
  monthlyRevenue: number;
  totalDebt: number;
  lowStockCount: number;
  outOfStockCount: number;
  monthlyExpenses: number;
  totalProducts: number;
}

export interface LowStockProduct {
  id: number;
  tenSanPham: string;
  soLuongTon: number;
  canhBaoTonKho: number;
}

export interface ProductSaleItem {
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

export interface Tab1Stats {
  completedRevenue: number;
  scheduledDeposit: number;
  scheduledCount: number;
  unpaidCompletedCount: number;
}

export interface Tab2Stats {
  prodRevenue: number;
  prodProfit: number;
  qtySold: number;
  qtyGifted: number;
}

export interface StoreConfig {
  id?: number;
  shopName: string;
  shopNamePnh: string;
  shopAddr: string;
  shopTel: string;
  shopBank: string;
  shopNotes: string;
  shopPolicy: string;
  shopWarranty: string;
  shopWarrantyLimit: string;
}


