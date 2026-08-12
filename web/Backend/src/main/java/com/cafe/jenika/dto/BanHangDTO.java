package com.cafe.jenika.dto;

import com.cafe.jenika.model.BanHang;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BanHangDTO {
    private Integer id;
    private LocalDateTime thoiGian;
    private DoiTacDTO doiTac;
    private NhanVienDTO nhanVien;
    private BigDecimal tongTien;
    private BigDecimal tienDaThanhToan;
    private BigDecimal tienNo;
    private String diaChiGiaoHang;
    private LocalDate ngayLap;
    private String trangThai;
    private String ghiChu;
    private String anhHoaDonUrl;
    private BigDecimal tongCost;
    private BigDecimal loiNhuan;
    private BigDecimal tienQuaTang;
    private List<ChiTietBanHangDTO> chiTietBanHangs;

    public static BanHangDTO fromEntity(BanHang entity) {
        if (entity == null) return null;
        return BanHangDTO.builder()
                .id(entity.getId())
                .thoiGian(entity.getThoiGian())
                .doiTac(DoiTacDTO.fromEntity(entity.getDoiTac()))
                .nhanVien(NhanVienDTO.fromEntity(entity.getNhanVien()))
                .tongTien(entity.getTongTien())
                .tienDaThanhToan(entity.getTienDaThanhToan())
                .tienNo(entity.getTienNo())
                .diaChiGiaoHang(entity.getDiaChiGiaoHang())
                .ngayLap(entity.getNgayLap())
                .trangThai(entity.getTrangThai())
                .ghiChu(entity.getGhiChu())
                .anhHoaDonUrl(entity.getAnhHoaDonUrl())
                .tongCost(entity.getTongCost())
                .loiNhuan(entity.getLoiNhuan())
                .tienQuaTang(entity.getTienQuaTang())
                .chiTietBanHangs(entity.getChiTietBanHangs() != null ?
                        entity.getChiTietBanHangs().stream()
                                .map(ChiTietBanHangDTO::fromEntity)
                                .collect(Collectors.toList()) : null)
                .build();
    }

    public BanHang toEntity() {
        BanHang entity = BanHang.builder()
                .id(this.id)
                .thoiGian(this.thoiGian)
                .doiTac(this.doiTac != null ? this.doiTac.toEntity() : null)
                .nhanVien(this.nhanVien != null ? this.nhanVien.toEntity() : null)
                .tongTien(this.tongTien)
                .tienDaThanhToan(this.tienDaThanhToan)
                .tienNo(this.tienNo)
                .diaChiGiaoHang(this.diaChiGiaoHang)
                .ngayLap(this.ngayLap)
                .trangThai(this.trangThai)
                .ghiChu(this.ghiChu)
                .anhHoaDonUrl(this.anhHoaDonUrl)
                .tongCost(this.tongCost)
                .loiNhuan(this.loiNhuan)
                .tienQuaTang(this.tienQuaTang)
                .build();
        
        if (this.chiTietBanHangs != null) {
            entity.setChiTietBanHangs(this.chiTietBanHangs.stream()
                    .map(dto -> {
                        com.cafe.jenika.model.ChiTietBanHang item = dto.toEntity();
                        item.setBanHang(entity);
                        return item;
                    })
                    .collect(Collectors.toList()));
        }
        return entity;
    }
}
