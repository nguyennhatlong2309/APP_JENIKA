package com.cafe.jenika.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "chi_tiet_ban_hang", uniqueConstraints = {
    @UniqueConstraint(name = "uq_bh_sp_gift", columnNames = {"id_ban_hang", "id_san_pham", "is_gift"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiTietBanHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ban_hang", nullable = false)
    @JsonIgnore
    private BanHang banHang;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_san_pham", nullable = false)
    private SanPham sanPham;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    @Column(name = "don_vi", length = 50)
    private String donVi;

    @Column(name = "gia_ban", precision = 15, scale = 0, nullable = false)
    private BigDecimal giaBan;

    @Column(name = "thanh_tien", precision = 15, scale = 0, nullable = false)
    private BigDecimal thanhTien;

    @Column(name = "is_gift", nullable = false)
    private Boolean isGift;

    @Column(name = "gia_von", precision = 15, scale = 0)
    private BigDecimal giaVon;
}
