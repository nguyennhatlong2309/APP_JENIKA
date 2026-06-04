package com.cafe.jenika.dto;

import com.cafe.jenika.model.ChiTietNhapHang;
import com.cafe.jenika.model.ChiTietNhapHangId;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiTietNhapHangDTO {
    private SanPhamDTO sanPham;
    private Integer soLuong;
    private String donVi;
    private BigDecimal giaNhap;
    private BigDecimal thanhTien;

    public static ChiTietNhapHangDTO fromEntity(ChiTietNhapHang entity) {
        if (entity == null) return null;
        return ChiTietNhapHangDTO.builder()
                .sanPham(SanPhamDTO.fromEntity(entity.getSanPham()))
                .soLuong(entity.getSoLuong())
                .donVi(entity.getDonVi())
                .giaNhap(entity.getGiaNhap())
                .thanhTien(entity.getThanhTien())
                .build();
    }

    public ChiTietNhapHang toEntity() {
        ChiTietNhapHang entity = ChiTietNhapHang.builder()
                .sanPham(this.sanPham != null ? this.sanPham.toEntity() : null)
                .soLuong(this.soLuong)
                .donVi(this.donVi)
                .giaNhap(this.giaNhap)
                .thanhTien(this.thanhTien)
                .build();
        
        Integer spId = (this.sanPham != null) ? this.sanPham.getId() : null;
        entity.setId(ChiTietNhapHangId.builder()
                .idSanPham(spId)
                .build());
                
        return entity;
    }
}
