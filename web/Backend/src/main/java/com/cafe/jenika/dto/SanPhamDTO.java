package com.cafe.jenika.dto;

import com.cafe.jenika.model.SanPham;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanPhamDTO {
    private Integer id;
    private String tenSanPham;
    private BigDecimal giaNhapHienTai;
    private BigDecimal giaBanHienTai;
    private Integer soLuongTon;
    private Integer canhBaoTonKho;
    private String trangThai;
    private DanhMucDTO danhMuc;
    private DonViTinhDTO donViTinh;
    private NhomSanPhamDTO nhomSanPham;
    private Boolean biXoa;
    private String ghiChu;

    public static SanPhamDTO fromEntity(SanPham entity) {
        if (entity == null) return null;
        return SanPhamDTO.builder()
                .id(entity.getId())
                .tenSanPham(entity.getTenSanPham())
                .giaNhapHienTai(entity.getGiaNhapHienTai())
                .giaBanHienTai(entity.getGiaBanHienTai())
                .soLuongTon(entity.getSoLuongTon())
                .canhBaoTonKho(entity.getCanhBaoTonKho())
                .trangThai(entity.getTrangThai())
                .danhMuc(DanhMucDTO.fromEntity(entity.getDanhMuc()))
                .donViTinh(DonViTinhDTO.fromEntity(entity.getDonViTinh()))
                .nhomSanPham(NhomSanPhamDTO.fromEntity(entity.getNhomSanPham()))
                .biXoa(entity.getBiXoa())
                .ghiChu(entity.getGhiChu())
                .build();
    }

    public SanPham toEntity() {
        return SanPham.builder()
                .id(this.id)
                .tenSanPham(this.tenSanPham)
                .giaNhapHienTai(this.giaNhapHienTai)
                .giaBanHienTai(this.giaBanHienTai)
                .soLuongTon(this.soLuongTon)
                .canhBaoTonKho(this.canhBaoTonKho)
                .trangThai(this.trangThai)
                .danhMuc(this.danhMuc != null ? this.danhMuc.toEntity() : null)
                .donViTinh(this.donViTinh != null ? this.donViTinh.toEntity() : null)
                .nhomSanPham(this.nhomSanPham != null ? this.nhomSanPham.toEntity() : null)
                .biXoa(this.biXoa)
                .ghiChu(this.ghiChu)
                .build();
    }
}
