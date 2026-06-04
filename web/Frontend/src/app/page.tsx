'use client';

import { useState, useEffect } from "react";
import Link from "next/link";

interface DashboardData {
  dailyRevenue: number;
  monthlyRevenue: number;
  totalDebt: number;
  lowStockCount: number;
  outOfStockCount: number;
  monthlyExpenses: number;
  totalProducts: number;
}

interface OrderItem {
  id: number;
  thoiGian: string;
  doiTac?: { ten: string } | null;
  tongTien: number;
  trangThai: string;
}

interface LowStockProduct {
  id: number;
  tenSanPham: string;
  soLuongTon: number;
  canhBaoTonKho: number;
}

export default function Home() {
  const [stats, setStats] = useState<DashboardData>({
    dailyRevenue: 0,
    monthlyRevenue: 0,
    totalDebt: 0,
    lowStockCount: 0,
    outOfStockCount: 0,
    monthlyExpenses: 0,
    totalProducts: 0
  });

  const [recentOrders, setRecentOrders] = useState<OrderItem[]>([]);
  const [lowStockProducts, setLowStockProducts] = useState<LowStockProduct[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchData() {
      try {
        // Fetch dashboard stats
        const statsRes = await fetch("http://localhost:8080/api/v1/bao-cao/dashboard");
        if (statsRes.ok) {
          const data = await statsRes.json();
          setStats(data);
        }

        // Fetch recent sales orders
        const ordersRes = await fetch("http://localhost:8080/api/v1/ban-hang");
        if (ordersRes.ok) {
          const orders = await ordersRes.json();
          setRecentOrders(orders.slice(0, 4)); // Show top 4 latest orders
        }

        // Fetch low stock warnings
        const lowStockRes = await fetch("http://localhost:8080/api/v1/san-pham/low-stock");
        if (lowStockRes.ok) {
          const products = await lowStockRes.json();
          setLowStockProducts(products.slice(0, 3)); // Show top 3 warnings
        }
      } catch (err) {
        console.error("Error fetching dashboard data:", err);
      } finally {
        setLoading(false);
      }
    }

    fetchData();
  }, []);

  const formatVND = (num: number) => {
    return num.toLocaleString('vi-VN') + ' ₫';
  };

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'Hoàn thành':
        return 'bg-success/10 text-success border border-success/20';
      case 'Hẹn':
        return 'bg-warning/10 text-warning border border-warning/20';
      case 'Hủy':
        return 'bg-error/10 text-error border border-error/20';
      default:
        return 'bg-primary/10 text-primary border border-primary/20';
    }
  };

  return (
    <div className="p-8 space-y-6 max-w-[1600px] mx-auto w-full">
      {loading ? (
        <div className="text-center py-20 text-white text-sm">Loading dashboard statistics...</div>
      ) : (
        <>
          {/* Stats Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-6">
            {/* Card 1: Daily Revenue */}
            <div className="glass-card p-6 rounded-2xl flex flex-col justify-between">
              <div className="flex justify-between items-start">
                <div className="w-12 h-12 bg-primary/10 rounded-xl flex items-center justify-center text-primary">
                  <span className="material-symbols-outlined">payments</span>
                </div>
                <div className="flex items-center gap-1 text-primary text-xs font-bold">
                  <span className="material-symbols-outlined text-sm">trending_up</span>
                  Live Sync
                </div>
              </div>
              <div className="mt-4">
                <p className="text-on-surface-variant text-xs font-semibold uppercase tracking-wider">Doanh thu hôm nay</p>
                <h3 className="text-2xl font-bold text-white mt-1">
                  {formatVND(stats.dailyRevenue)}
                </h3>
              </div>
            </div>

            {/* Card 2: Monthly Revenue */}
            <div className="glass-card p-6 rounded-2xl flex flex-col justify-between">
              <div className="flex justify-between items-start">
                <div className="w-12 h-12 bg-secondary/10 rounded-xl flex items-center justify-center text-secondary">
                  <span className="material-symbols-outlined">shopping_cart</span>
                </div>
                <span className="bg-success/15 text-success px-3 py-1 rounded-full text-[10px] font-bold uppercase">
                  Tháng này
                </span>
              </div>
              <div className="mt-4">
                <p className="text-on-surface-variant text-xs font-semibold uppercase tracking-wider">Doanh thu tháng này</p>
                <h3 className="text-2xl font-bold text-white mt-1">
                  {formatVND(stats.monthlyRevenue)}
                </h3>
              </div>
            </div>

            {/* Card 3: Coffee Stock Warnings */}
            <div className="glass-card p-6 rounded-2xl flex flex-col justify-between">
              <div className="flex justify-between items-start">
                <div className="w-12 h-12 bg-primary/10 rounded-xl flex items-center justify-center text-primary">
                  <span className="material-symbols-outlined">inventory_2</span>
                </div>
                {stats.lowStockCount > 0 ? (
                  <div className="flex items-center gap-1 text-error text-[10px] font-bold animate-pulse">
                    <span className="material-symbols-outlined text-sm">warning</span>
                    {stats.lowStockCount} HẾT/SẮP HẾT HÀNG
                  </div>
                ) : (
                  <div className="text-success text-[10px] font-bold flex items-center">
                    <span className="material-symbols-outlined text-sm mr-1">check_circle</span> AN TOÀN
                  </div>
                )}
              </div>
              <div className="mt-4">
                <p className="text-on-surface-variant text-xs font-semibold uppercase tracking-wider">Số mặt hàng cảnh báo kho</p>
                <h3 className="text-2xl font-bold text-white mt-1">
                  {stats.lowStockCount} <span className="text-xs font-normal text-on-surface-variant">Sản phẩm</span>
                </h3>
              </div>
            </div>

            {/* Card 4: Monthly Import Expenses */}
            <div className="glass-card p-6 rounded-2xl flex flex-col justify-between">
              <div className="flex justify-between items-start">
                <div className="w-12 h-12 bg-tertiary/10 rounded-xl flex items-center justify-center text-tertiary">
                  <span className="material-symbols-outlined">account_balance_wallet</span>
                </div>
                <div className="text-on-surface-variant text-xs font-bold">
                  Nhập hàng
                </div>
              </div>
              <div className="mt-4">
                <p className="text-on-surface-variant text-xs font-semibold uppercase tracking-wider">Chi phí nhập hàng tháng này</p>
                <h3 className="text-2xl font-bold text-white mt-1">
                  {formatVND(stats.monthlyExpenses)}
                </h3>
              </div>
            </div>
          </div>

          {/* Analytics Section */}
          <div className="grid grid-cols-1 xl:grid-cols-3 gap-6">
            {/* Sales Analytics Chart */}
            <div className="xl:col-span-2 glass-card p-8 rounded-2xl">
              <div className="flex justify-between items-center mb-8">
                <div>
                  <h4 className="text-lg font-bold text-white">Phân tích Bán hàng</h4>
                  <p className="text-on-surface-variant text-sm">Tổng quan hiệu suất hoạt động tuần này</p>
                </div>
                <div className="text-xs text-primary font-bold px-4 py-2 border border-primary/20 rounded-lg">
                  Thời gian thực
                </div>
              </div>
              <div className="relative h-[300px] w-full">
                <svg className="w-full h-full overflow-visible" viewBox="0 0 1000 300">
                  <defs>
                    <linearGradient id="chartGradient" x1="0" x2="0" y1="0" y2="1">
                      <stop className="chart-gradient-primary" offset="0%"></stop>
                      <stop className="chart-gradient-transparent" offset="100%"></stop>
                    </linearGradient>
                  </defs>
                  {/* Grid Lines */}
                  <line stroke="rgba(255,255,255,0.05)" strokeWidth="1" x1="0" x2="1000" y1="50" y2="50"></line>
                  <line stroke="rgba(255,255,255,0.05)" strokeWidth="1" x1="0" x2="1000" y1="125" y2="125"></line>
                  <line stroke="rgba(255,255,255,0.05)" strokeWidth="1" x1="0" x2="1000" y1="200" y2="200"></line>
                  <line stroke="rgba(255,255,255,0.1)" strokeWidth="1" x1="0" x2="1000" y1="275" y2="275"></line>
                  {/* Area Path */}
                  <path d="M0,275 L0,200 L150,150 L300,180 L450,100 L600,140 L750,60 L900,110 L1000,90 L1000,275 Z" fill="url(#chartGradient)"></path>
                  {/* Line Path */}
                  <path className="drop-shadow-[0_0_10px_rgba(73,252,223,0.5)]" d="M0,200 L150,150 L300,180 L450,100 L600,140 L750,60 L900,110 L1000,90" fill="none" stroke="#49fcdf" strokeLinecap="round" strokeLinejoin="round" strokeWidth="4"></path>
                  {/* Data Points */}
                  <circle cx="150" cy="150" fill="#49fcdf" r="6"></circle>
                  <circle cx="450" cy="100" fill="#49fcdf" r="6"></circle>
                  <circle className="animate-pulse" cx="750" cy="60" fill="#49fcdf" r="8"></circle>
                </svg>
                <div className="flex justify-between mt-4 text-[10px] text-on-surface-variant font-bold px-2">
                  <span>THỨ 2</span><span>THỨ 3</span><span>THỨ 4</span><span>THỨ 5</span><span>THỨ 6</span><span>THỨ 7</span><span>CHỦ NHẬT</span>
                </div>
              </div>
            </div>

            {/* Recent Sales Orders Table */}
            <div className="glass-card p-8 rounded-2xl flex flex-col justify-between">
              <div>
                <h4 className="text-lg font-bold text-white mb-6">Hóa đơn bán hàng gần đây</h4>
                <div className="space-y-4">
                  {recentOrders.length === 0 ? (
                    <div className="text-center py-8 text-xs text-on-surface-variant">Chưa có hóa đơn nào được tạo.</div>
                  ) : (
                    recentOrders.map((order) => (
                      <div key={order.id} className="group flex items-center justify-between p-3 rounded-xl hover:bg-white/5 transition-all border-b border-white/5 last:border-0">
                        <div className="flex items-center gap-3">
                          <div className="w-10 h-10 rounded-full bg-primary/20 flex items-center justify-center">
                            <span className="material-symbols-outlined text-primary text-sm">person</span>
                          </div>
                          <div>
                            <p className="text-sm font-semibold text-white">{order.doiTac ? order.doiTac.ten : "Khách vãng lai"}</p>
                            <p className="text-[11px] text-on-surface-variant">Mã số HĐ: BH-{order.id}</p>
                          </div>
                        </div>
                        <div className="text-right">
                          <p className="text-sm font-semibold text-white">{formatVND(order.tongTien)}</p>
                          <span className={`text-[8px] px-2 py-0.5 rounded-full font-bold uppercase ${getStatusBadge(order.trangThai)}`}>
                            {order.trangThai.toUpperCase()}
                          </span>
                        </div>
                      </div>
                    ))
                  )}
                </div>
              </div>
              <Link href="/sales" className="w-full mt-6 py-2 text-center text-primary text-xs font-semibold hover:bg-primary/5 transition-all rounded-lg border border-primary/10">
                Xem tất cả hóa đơn
              </Link>
            </div>
          </div>

          {/* Insights & Actions */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6 pb-12">
            {/* Low Stock Alerts */}
            <div className="glass-card p-8 rounded-2xl">
              <div className="flex justify-between items-center mb-6">
                <h4 className="text-lg font-bold text-white">Cảnh báo tồn kho nguyên liệu</h4>
                <span className="material-symbols-outlined text-error">notification_important</span>
              </div>
              <div className="space-y-6">
                {lowStockProducts.length === 0 ? (
                  <div className="text-center py-8 text-xs text-success font-semibold">Tất cả sản phẩm/nguyên liệu đều đủ tồn kho an toàn!</div>
                ) : (
                  lowStockProducts.map((p) => {
                    const ratio = Math.max(0, Math.min(100, Math.round((p.soLuongTon / (p.canhBaoTonKho || 5)) * 100)));
                    return (
                      <div key={p.id} className="space-y-2">
                        <div className="flex justify-between text-sm">
                          <span className="text-white font-medium">{p.tenSanPham}</span>
                          <span className="text-error font-bold">Còn {p.soLuongTon} sản phẩm (Ngưỡng: {p.canhBaoTonKho})</span>
                        </div>
                        <div className="h-2 w-full bg-black/20 rounded-full overflow-hidden">
                          <div
                            className="h-full bg-error rounded-full shadow-[0_0_10px_rgba(255,180,171,0.5)]"
                            style={{ width: `${ratio}%` }}
                          ></div>
                        </div>
                        <div className="flex justify-end">
                          <Link href="/purchases" className="mt-1 flex items-center gap-1 text-[10px] font-bold text-primary hover:neon-text-glow transition-all">
                            <span className="material-symbols-outlined text-sm">refresh</span> TẠO ĐƠN NHẬP HÀNG
                          </Link>
                        </div>
                      </div>
                    );
                  })
                )}
              </div>
            </div>

            {/* Quick Actions */}
            <div className="glass-card p-8 rounded-2xl">
              <h4 className="text-lg font-bold text-white mb-6">Thao tác nhanh</h4>
              <div className="grid grid-cols-2 gap-4">
                <Link href="/sales?create=true" className="flex flex-col items-center justify-center p-6 bg-primary/10 border border-primary/20 rounded-2xl hover:bg-primary/20 hover:neon-glow transition-all group">
                  <span className="material-symbols-outlined text-primary mb-2 text-3xl group-hover:scale-110 transition-transform">add_shopping_cart</span>
                  <span className="text-xs font-semibold text-white text-center">Tạo hóa đơn xuất</span>
                </Link>
                <Link href="/inventory?add=true" className="flex flex-col items-center justify-center p-6 bg-secondary/10 border border-secondary/20 rounded-2xl hover:bg-secondary/20 transition-all group">
                  <span className="material-symbols-outlined text-secondary mb-2 text-3xl group-hover:scale-110 transition-transform">add_to_photos</span>
                  <span className="text-xs font-semibold text-white text-center">Thêm món / sản phẩm</span>
                </Link>
                <Link href="/expenses?log=true" className="flex flex-col items-center justify-center p-6 bg-tertiary/10 border border-tertiary/20 rounded-2xl hover:bg-tertiary/20 transition-all group">
                  <span className="material-symbols-outlined text-tertiary mb-2 text-3xl group-hover:scale-110 transition-transform">receipt_long</span>
                  <span className="text-xs font-semibold text-white text-center">Ghi nhận chi phí</span>
                </Link>
                <Link href="/settings" className="flex flex-col items-center justify-center p-6 bg-surface-highest border border-white/5 rounded-2xl hover:bg-white/10 transition-all group">
                  <span className="material-symbols-outlined text-on-surface-variant mb-2 text-3xl group-hover:scale-110 transition-transform">settings_suggest</span>
                  <span className="text-xs font-semibold text-white text-center">Cấu hình hệ thống</span>
                </Link>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
