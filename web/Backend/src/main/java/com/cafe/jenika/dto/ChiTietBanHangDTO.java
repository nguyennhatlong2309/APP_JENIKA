package com.cafe.jenika.dto;

import com.cafe.jenika.model.ChiTietBanHang;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiTietBanHangDTO {
    private Integer id;
    private SanPhamDTO sanPham;
    private Integer soLuong;
    private String donVi;
    private BigDecimal giaBan;
    private BigDecimal thanhTien;
    private Boolean isGift;

    public static ChiTietBanHangDTO fromEntity(ChiTietBanHang entity) {
        if (entity == null) return null;
        return ChiTietBanHangDTO.builder()
                .id(entity.getId())
                .sanPham(SanPhamDTO.fromEntity(entity.getSanPham()))
                .soLuong(entity.getSoLuong())
                .donVi(entity.getDonVi())
                .giaBan(entity.getGiaBan())
                .thanhTien(entity.getThanhTien())
                .isGift(entity.getIsGift())
                .build();
    }

    public ChiTietBanHang toEntity() {
        return ChiTietBanHang.builder()
                .id(this.id)
                .sanPham(this.sanPham != null ? this.sanPham.toEntity() : null)
                .soLuong(this.soLuong)
                .donVi(this.donVi)
                .giaBan(this.giaBan)
                .thanhTien(this.thanhTien)
                .isGift(this.isGift)
                .build();
    }
}
