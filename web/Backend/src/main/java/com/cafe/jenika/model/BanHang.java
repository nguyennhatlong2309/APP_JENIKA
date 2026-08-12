package com.cafe.jenika.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "ban_hang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BanHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "thoi_gian", nullable = false)
    private LocalDateTime thoiGian;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_doi_tac")
    private DoiTac doiTac;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_nhan_vien")
    private NhanVien nhanVien;

    @Column(name = "tong_tien", precision = 15, scale = 0)
    private BigDecimal tongTien;

    @Column(name = "tien_da_thanh_toan", precision = 15, scale = 0)
    private BigDecimal tienDaThanhToan;

    @Column(name = "tien_no", precision = 15, scale = 0)
    private BigDecimal tienNo;

    @Column(name = "dia_chi_giao_hang", columnDefinition = "TEXT")
    private String diaChiGiaoHang;

    @Column(name = "ngay_lap")
    private LocalDate ngayLap;

    @Column(name = "trang_thai", length = 50, nullable = false)
    private String trangThai; // Hoàn thành | Hẹn | Hủy

    @Column(name = "ghi_chu", columnDefinition = "TEXT")
    private String ghiChu;

    @Column(name = "anh_hoa_don_url", length = 500)
    private String anhHoaDonUrl;

    @Column(name = "tong_cost", precision = 15, scale = 0)
    private BigDecimal tongCost;

    @Column(name = "loi_nhuan", precision = 15, scale = 0)
    private BigDecimal loiNhuan;

    @Column(name = "tien_qua_tang", precision = 15, scale = 0)
    private BigDecimal tienQuaTang;

    @OneToMany(mappedBy = "banHang", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ChiTietBanHang> chiTietBanHangs;
}
