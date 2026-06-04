package com.cafe.jenika.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "chi_tiet_nhap_hang")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiTietNhapHang {

    @EmbeddedId
    private ChiTietNhapHangId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idNhapHang")
    @JoinColumn(name = "id_nhap_hang", nullable = false)
    @JsonIgnore
    private NhapHang nhapHang;

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("idSanPham")
    @JoinColumn(name = "id_san_pham", nullable = false)
    private SanPham sanPham;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    @Column(name = "don_vi", length = 50)
    private String donVi;

    @Column(name = "gia_nhap", precision = 15, scale = 0, nullable = false)
    private BigDecimal giaNhap;

    @Column(name = "thanh_tien", precision = 15, scale = 0, nullable = false)
    private BigDecimal thanhTien;
}
