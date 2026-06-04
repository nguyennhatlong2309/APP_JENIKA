package com.cafe.jenika.dto;

import com.cafe.jenika.model.ThuChi;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThuChiDTO {
    private Integer id;
    private LocalDateTime thoiGian;
    private Integer idLoai;
    private String tenLoai;
    private BigDecimal tienThu;
    private BigDecimal tienChi;
    private String moTa;
    private Integer idNhanVien;
    private String tenNhanVien;
    private Integer idBanHang;
    private Integer idNhapHang;
    private String phuongThuc;
    private String trangThai;

    public static ThuChiDTO fromEntity(ThuChi entity) {
        if (entity == null) return null;
        return ThuChiDTO.builder()
                .id(entity.getId())
                .thoiGian(entity.getThoiGian())
                .idLoai(entity.getLoaiThuChi() != null ? entity.getLoaiThuChi().getId() : null)
                .tenLoai(entity.getLoaiThuChi() != null ? entity.getLoaiThuChi().getTen() : null)
                .tienThu(entity.getTienThu())
                .tienChi(entity.getTienChi())
                .moTa(entity.getMoTa())
                .idNhanVien(entity.getNhanVien() != null ? entity.getNhanVien().getId() : null)
                .tenNhanVien(entity.getNhanVien() != null ? entity.getNhanVien().getTenNhanVien() : null)
                .idBanHang(entity.getBanHang() != null ? entity.getBanHang().getId() : null)
                .idNhapHang(entity.getNhapHang() != null ? entity.getNhapHang().getId() : null)
                .phuongThuc(entity.getPhuongThuc())
                .trangThai(entity.getTrangThai())
                .build();
    }

    public ThuChi toEntity() {
        return ThuChi.builder()
                .id(this.id)
                .thoiGian(this.thoiGian)
                .tienThu(this.tienThu)
                .tienChi(this.tienChi)
                .moTa(this.moTa)
                .phuongThuc(this.phuongThuc)
                .trangThai(this.trangThai)
                .build();
    }
}
