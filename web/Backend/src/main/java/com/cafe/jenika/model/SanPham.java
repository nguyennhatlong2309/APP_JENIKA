package com.cafe.jenika.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "san_pham")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten_san_pham", nullable = false)
    private String tenSanPham;

    @Column(name = "gia_nhap_hien_tai", precision = 15, scale = 0)
    private BigDecimal giaNhapHienTai;

    @Column(name = "gia_ban_hien_tai", precision = 15, scale = 0)
    private BigDecimal giaBanHienTai;

    @Column(name = "so_luong_ton")
    private Integer soLuongTon;

    @Column(name = "canh_bao_ton_kho", nullable = false)
    private Integer canhBaoTonKho;

    @Column(name = "trang_thai", length = 20, nullable = false)
    private String trangThai;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_danh_muc")
    private DanhMuc danhMuc;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_don_vi")
    private DonViTinh donViTinh;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_nhom")
    private NhomSanPham nhomSanPham;

    @Column(name = "bi_xoa", nullable = false)
    private Boolean biXoa;

    @Column(name = "ghi_chu", columnDefinition = "TEXT")
    private String ghiChu;

    public void updateTrangThai() {
        if (this.soLuongTon == null || this.soLuongTon <= 0) {
            this.trangThai = "Hết hàng";
        } else if (this.soLuongTon <= this.canhBaoTonKho) {
            this.trangThai = "Cảnh báo";
        } else {
            this.trangThai = "Còn hàng";
        }
    }
}
