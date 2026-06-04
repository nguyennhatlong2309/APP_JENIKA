package com.cafe.jenika.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "thu_chi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThuChi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "thoi_gian")
    private LocalDateTime thoiGian;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_loai")
    private LoaiThuChi loaiThuChi;

    @Column(name = "tien_thu", precision = 15, scale = 0)
    private BigDecimal tienThu;

    @Column(name = "tien_chi", precision = 15, scale = 0)
    private BigDecimal tienChi;

    @Column(name = "mo_ta", columnDefinition = "TEXT")
    private String moTa;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_nhan_vien")
    private NhanVien nhanVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ban_hang")
    private BanHang banHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhap_hang")
    private NhapHang nhapHang;

    @Column(name = "phuong_thuc", length = 50)
    private String phuongThuc; // Tiền mặt, Chuyển khoản ngân hàng, Thẻ tín dụng, Ví điện tử

    @Column(name = "trang_thai", length = 50)
    private String trangThai; // Đã chi, Đang xử lý
}
