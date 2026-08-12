'use client';

import { useState, useEffect } from "react";
import Link from "next/link";

import { DashboardData, SaleOrder as OrderItem, LowStockProduct } from "@/types";
import { productService } from "@/services/productService";
import { saleService } from "@/services/saleService";
import { reportService } from "@/services/reportService";
import { formatVND } from "@/lib/utils";

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
  const [weeklyRevenue, setWeeklyRevenue] = useState<number[]>([0, 0, 0, 0, 0]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function fetchData() {
      try {
        // Fetch all dashboard data in parallel to avoid async waterfall
        const [data, orders, products] = await Promise.all([
          reportService.getDashboardData(),
          saleService.getSaleOrders(),
          productService.getLowStockProducts()
        ]);

        setStats(data);
        setRecentOrders(orders.slice(0, 4)); // Show top 4 latest orders
        setLowStockProducts(products.slice(0, 3) as any); // Show top 3 warnings

        // Calculate weekly revenue for current month
        const now = new Date();
        const y = now.getFullYear();
        const m = now.getMonth();
        const tempWeekly = [0, 0, 0, 0, 0];
        
        orders.forEach((order: any) => {
          if (order.trangThai === 'Hủy') return;
          const orderDate = new Date(order.thoiGian);
          if (orderDate.getFullYear() === y && orderDate.getMonth() === m) {
            const day = orderDate.getDate();
            if (day >= 1 && day <= 7) tempWeekly[0] += order.tongTien;
            else if (day >= 8 && day <= 14) tempWeekly[1] += order.tongTien;
            else if (day >= 15 && day <= 21) tempWeekly[2] += order.tongTien;
            else if (day >= 22 && day <= 28) tempWeekly[3] += order.tongTien;
            else if (day >= 29) tempWeekly[4] += order.tongTien;
          }
        });
        setWeeklyRevenue(tempWeekly);
      } catch (err) {
        console.error("Error fetching dashboard data:", err);
      } finally {
        setLoading(false);
      }
    }

    fetchData();
  }, []);


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

  const maxVal = Math.max(...weeklyRevenue);
  const maxRevenue = maxVal > 0 ? maxVal : 1000000;
  const getChartY = (val: number) => {
    const height = 180;
    const ratio = val / maxRevenue;
    return 220 - (ratio * height);
  };

  const y1 = getChartY(weeklyRevenue[0]);
  const y2 = getChartY(weeklyRevenue[1]);
  const y3 = getChartY(weeklyRevenue[2]);
  const y4 = getChartY(weeklyRevenue[3]);
  const y5 = getChartY(weeklyRevenue[4]);

  return (
    <div className="h-[calc(100vh-16px)] overflow-hidden flex flex-col pt-2 pb-2 px-4 space-y-3 w-full relative">
      {/* Top Header Controls */}
      <div className="flex justify-between items-center flex-shrink-0">
        <div>
          <h2 className="text-xl font-bold text-white tracking-wide">
            Tổng quan Báo cáo <span className="text-xs text-primary font-medium ml-2">(Tháng {new Date().getMonth() + 1}/{new Date().getFullYear()})</span>
          </h2>
        </div>
      </div>

      {loading ? (
        <div className="flex-1 flex items-center justify-center text-white text-sm">
          Đang tải dữ liệu báo cáo...
        </div>
      ) : (
        <>
          {/* Stats Row */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 flex-shrink-0 animate-in fade-in">
            {/* Card 1: Daily Revenue */}
            <div className="glass-card py-2.5 px-4 rounded-xl flex items-center justify-between hover:border-primary/30 transition-all">
              <div className="flex items-center gap-3">
                <span className="material-symbols-outlined text-primary bg-primary/10 p-2 rounded-lg text-lg">payments</span>
                <div>
                  <p className="text-on-surface-variant text-[10px] uppercase tracking-wider mb-0.5">Doanh thu hôm nay</p>
                  <h3 className="text-sm font-bold text-on-surface">{formatVND(stats.dailyRevenue)}</h3>
                </div>
              </div>
            </div>

            {/* Card 2: Monthly Revenue */}
            <div className="glass-card py-2.5 px-4 rounded-xl flex items-center justify-between hover:border-primary/30 transition-all">
              <div className="flex items-center gap-3">
                <span className="material-symbols-outlined text-secondary bg-secondary/10 p-2 rounded-lg text-lg">shopping_cart</span>
                <div>
                  <p className="text-on-surface-variant text-[10px] uppercase tracking-wider mb-0.5">Doanh thu tháng này</p>
                  <h3 className="text-sm font-bold text-on-surface">{formatVND(stats.monthlyRevenue)}</h3>
                </div>
              </div>
            </div>

            {/* Card 3: Coffee Stock Warnings */}
            <div className="glass-card py-2.5 px-4 rounded-xl flex items-center justify-between hover:border-primary/30 transition-all">
              <div className="flex items-center gap-3">
                <span className={`material-symbols-outlined p-2 rounded-lg text-lg ${stats.lowStockCount > 0 ? 'text-error bg-error/10' : 'text-primary bg-primary/10'}`}>inventory_2</span>
                <div>
                  <p className="text-on-surface-variant text-[10px] uppercase tracking-wider mb-0.5">Mặt hàng cảnh báo kho</p>
                  <h3 className="text-sm font-bold text-on-surface">{stats.lowStockCount} Sản phẩm</h3>
                </div>
              </div>
            </div>

            {/* Card 4: Monthly Import Expenses */}
            <div className="glass-card py-2.5 px-4 rounded-xl flex items-center justify-between hover:border-primary/30 transition-all">
              <div className="flex items-center gap-3">
                <span className="material-symbols-outlined text-tertiary bg-tertiary/10 p-2 rounded-lg text-lg">account_balance_wallet</span>
                <div>
                  <p className="text-on-surface-variant text-[10px] uppercase tracking-wider mb-0.5">Chi phí nhập tháng này</p>
                  <h3 className="text-sm font-bold text-on-surface">{formatVND(stats.monthlyExpenses)}</h3>
                </div>
              </div>
            </div>
          </div>

          {/* Content Scrollable Container */}
          <div className="flex-1 overflow-y-auto pr-1 space-y-4 min-h-0 pb-2">
            {/* Analytics Section */}
            <div className="grid grid-cols-1 xl:grid-cols-3 gap-4">
              {/* Sales Analytics Chart */}
              <div className="xl:col-span-2 glass-card p-5 rounded-xl">
                <div className="flex justify-between items-center mb-6">
                  <div>
                    <h4 className="text-sm font-bold text-white">Phân tích Bán hàng</h4>
                    <p className="text-[10px] text-on-surface-variant">Tổng quan hiệu suất hoạt động trong tháng hiện tại</p>
                  </div>
                  <div className="text-[10px] text-primary font-bold px-3 py-1.5 border border-primary/20 rounded-lg">
                    Thời gian thực
                  </div>
                </div>
                <div className="relative h-[240px] w-full">
                  <svg className="w-full h-full overflow-visible" viewBox="0 0 1000 240">
                    <defs>
                      <linearGradient id="chartGradient" x1="0" x2="0" y1="0" y2="1">
                        <stop className="chart-gradient-primary" offset="0%"></stop>
                        <stop className="chart-gradient-transparent" offset="100%"></stop>
                      </linearGradient>
                    </defs>
                    {/* Grid Lines */}
                    <line stroke="rgba(255,255,255,0.05)" strokeWidth="1" x1="0" x2="1000" y1="40" y2="40"></line>
                    <line stroke="rgba(255,255,255,0.05)" strokeWidth="1" x1="0" x2="1000" y1="100" y2="100"></line>
                    <line stroke="rgba(255,255,255,0.05)" strokeWidth="1" x1="0" x2="1000" y1="160" y2="160"></line>
                    <line stroke="rgba(255,255,255,0.1)" strokeWidth="1" x1="0" x2="1000" y1="220" y2="220"></line>
                    {/* Area Path */}
                    <path d={`M0,220 L0,${y1} L250,${y2} L500,${y3} L750,${y4} L1000,${y5} L1000,220 Z`} fill="url(#chartGradient)"></path>
                    {/* Line Path */}
                    <path className="drop-shadow-[0_0_10px_rgba(73,252,223,0.5)]" d={`M0,${y1} L250,${y2} L500,${y3} L750,${y4} L1000,${y5}`} fill="none" stroke="#49fcdf" strokeLinecap="round" strokeLinejoin="round" strokeWidth="4"></path>
                    {/* Data Points */}
                    <circle cx="0" cy={y1} fill="#49fcdf" r="5"><title>Tuần 1: {formatVND(weeklyRevenue[0])}</title></circle>
                    <circle cx="250" cy={y2} fill="#49fcdf" r="5"><title>Tuần 2: {formatVND(weeklyRevenue[1])}</title></circle>
                    <circle cx="500" cy={y3} fill="#49fcdf" r="5"><title>Tuần 3: {formatVND(weeklyRevenue[2])}</title></circle>
                    <circle cx="750" cy={y4} fill="#49fcdf" r="5"><title>Tuần 4: {formatVND(weeklyRevenue[3])}</title></circle>
                    <circle className="animate-pulse" cx="1000" cy={y5} fill="#49fcdf" r="7"><title>Tuần 5: {formatVND(weeklyRevenue[4])}</title></circle>
                  </svg>
                  <div className="absolute bottom-2 left-0 right-0 flex justify-between text-[9px] text-on-surface-variant font-bold px-2">
                    <span>TUẦN 1</span><span>TUẦN 2</span><span>TUẦN 3</span><span>TUẦN 4</span><span>TUẦN 5</span>
                  </div>
                </div>
              </div>

              {/* Recent Sales Orders Table */}
              <div className="glass-card p-5 rounded-xl flex flex-col justify-between">
                <div>
                  <h4 className="text-sm font-bold text-white mb-4">Hóa đơn bán hàng gần đây</h4>
                  <div className="space-y-3">
                    {recentOrders.length === 0 ? (
                      <div className="text-center py-8 text-xs text-on-surface-variant">Chưa có hóa đơn nào được tạo.</div>
                    ) : (
                      recentOrders.map((order) => (
                        <div key={order.id} className="group flex items-center justify-between p-2.5 rounded-lg hover:bg-white/5 transition-all border-b border-white/5 last:border-0">
                          <div className="flex items-center gap-2.5">
                            <div className="w-8 h-8 rounded-full bg-primary/20 flex items-center justify-center">
                              <span className="material-symbols-outlined text-primary text-xs">person</span>
                            </div>
                            <div>
                              <p className="text-xs font-semibold text-white">{order.doiTac ? order.doiTac.ten : "Khách vãng lai"}</p>
                              <p className="text-[9px] text-on-surface-variant">Mã số HĐ: BH-{order.id}</p>
                            </div>
                          </div>
                          <div className="text-right">
                            <p className="text-xs font-semibold text-white">{formatVND(order.tongTien)}</p>
                            <span className={`text-[8px] px-1.5 py-0.5 rounded-full font-bold uppercase ${getStatusBadge(order.trangThai)}`}>
                              {order.trangThai.toUpperCase()}
                            </span>
                          </div>
                        </div>
                      ))
                    )}
                  </div>
                </div>
                <Link href="/sales" className="w-full mt-4 py-2 text-center text-primary text-[10px] font-semibold hover:bg-primary/5 transition-all rounded-lg border border-primary/10">
                  Xem tất cả hóa đơn
                </Link>
              </div>
            </div>

            {/* Insights & Actions */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pb-4">
              {/* Low Stock Alerts */}
              <div className="glass-card p-5 rounded-xl">
                <div className="flex justify-between items-center mb-4">
                  <h4 className="text-sm font-bold text-white">Cảnh báo tồn kho nguyên liệu</h4>
                  <span className="material-symbols-outlined text-error text-lg">notification_important</span>
                </div>
                <div className="space-y-4">
                  {lowStockProducts.length === 0 ? (
                    <div className="text-center py-8 text-xs text-success font-semibold">Tất cả sản phẩm/nguyên liệu đều đủ tồn kho an toàn!</div>
                  ) : (
                    lowStockProducts.map((p) => {
                      const ratio = Math.max(0, Math.min(100, Math.round((p.soLuongTon / (p.canhBaoTonKho || 5)) * 100)));
                      return (
                        <div key={p.id} className="space-y-1">
                          <div className="flex justify-between text-xs">
                            <span className="text-white font-medium">{p.tenSanPham}</span>
                            <span className="text-error font-bold">Còn {p.soLuongTon} (Ngưỡng: {p.canhBaoTonKho})</span>
                          </div>
                          <div className="h-1.5 w-full bg-black/20 rounded-full overflow-hidden">
                            <div
                              className="h-full bg-error rounded-full shadow-[0_0_10px_rgba(255,180,171,0.5)]"
                              style={{ width: `${ratio}%` }}
                            ></div>
                          </div>
                          <div className="flex justify-end">
                            <Link href="/purchases" className="mt-0.5 flex items-center gap-1 text-[9px] font-bold text-primary hover:neon-text-glow transition-all">
                              <span className="material-symbols-outlined text-xs">refresh</span> TẠO ĐƠN NHẬP HÀNG
                            </Link>
                          </div>
                        </div>
                      );
                    })
                  )}
                </div>
              </div>

              {/* Quick Actions */}
              <div className="glass-card p-5 rounded-xl">
                <h4 className="text-sm font-bold text-white mb-4">Thao tác nhanh</h4>
                <div className="grid grid-cols-2 gap-3">
                  <Link href="/sales?create=true" className="flex flex-col items-center justify-center p-4 bg-primary/10 border border-primary/20 rounded-xl hover:bg-primary/20 hover:neon-glow transition-all group">
                    <span className="material-symbols-outlined text-primary mb-1.5 text-2xl group-hover:scale-110 transition-transform">add_shopping_cart</span>
                    <span className="text-[10px] font-semibold text-white text-center">Tạo hóa đơn xuất</span>
                  </Link>
                  <Link href="/inventory?add=true" className="flex flex-col items-center justify-center p-4 bg-secondary/10 border border-secondary/20 rounded-xl hover:bg-secondary/20 transition-all group">
                    <span className="material-symbols-outlined text-secondary mb-1.5 text-2xl group-hover:scale-110 transition-transform">add_to_photos</span>
                    <span className="text-[10px] font-semibold text-white text-center">Thêm món / sản phẩm</span>
                  </Link>
                  <Link href="/expenses?log=true" className="flex flex-col items-center justify-center p-4 bg-tertiary/10 border border-tertiary/20 rounded-xl hover:bg-tertiary/20 transition-all group">
                    <span className="material-symbols-outlined text-tertiary mb-1.5 text-2xl group-hover:scale-110 transition-transform">receipt_long</span>
                    <span className="text-[10px] font-semibold text-white text-center">Ghi nhận chi phí</span>
                  </Link>
                  <Link href="/settings" className="flex flex-col items-center justify-center p-4 bg-surface-highest border border-white/5 rounded-xl hover:bg-white/10 transition-all group">
                    <span className="material-symbols-outlined text-on-surface-variant mb-1.5 text-2xl group-hover:scale-110 transition-transform">settings_suggest</span>
                    <span className="text-[10px] font-semibold text-white text-center">Cấu hình hệ thống</span>
                  </Link>
                </div>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
