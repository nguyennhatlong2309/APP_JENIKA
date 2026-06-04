package com.cafe.jenika.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "nhap_hang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NhapHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "thoi_gian", nullable = false)
    private LocalDateTime thoiGian;

    @Column(name = "tong_tien", precision = 15, scale = 0)
    private BigDecimal tongTien;

    @Column(name = "da_thanh_toan", precision = 15, scale = 0)
    private BigDecimal daThanhToan;

    @Column(name = "tien_no", precision = 15, scale = 0)
    private BigDecimal tienNo;

    @Column(name = "trang_thai", length = 50, nullable = false)
    private String trangThai; // Chờ nhận | Hoàn thành | Hủy

    @Column(name = "ngay_nhan")
    private LocalDate ngayNhan;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_doi_tac")
    private DoiTac doiTac;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_nhan_vien")
    private NhanVien nhanVien;

    @Column(name = "ghi_chu", columnDefinition = "TEXT")
    private String ghiChu;

    @OneToMany(mappedBy = "nhapHang", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ChiTietNhapHang> chiTietNhapHangs;
}
