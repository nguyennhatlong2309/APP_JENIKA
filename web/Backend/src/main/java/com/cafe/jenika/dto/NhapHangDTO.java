package com.cafe.jenika.dto;

import com.cafe.jenika.model.NhapHang;
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
public class NhapHangDTO {
    private Integer id;
    private LocalDateTime thoiGian;
    private BigDecimal tongTien;
    private BigDecimal daThanhToan;
    private BigDecimal tienNo;
    private String trangThai;
    private LocalDate ngayNhan;
    private DoiTacDTO doiTac;
    private NhanVienDTO nhanVien;
    private String ghiChu;
    private List<ChiTietNhapHangDTO> chiTietNhapHangs;

    public static NhapHangDTO fromEntity(NhapHang entity) {
        if (entity == null) return null;
        return NhapHangDTO.builder()
                .id(entity.getId())
                .thoiGian(entity.getThoiGian())
                .tongTien(entity.getTongTien())
                .daThanhToan(entity.getDaThanhToan())
                .tienNo(entity.getTienNo())
                .trangThai(entity.getTrangThai())
                .ngayNhan(entity.getNgayNhan())
                .doiTac(DoiTacDTO.fromEntity(entity.getDoiTac()))
                .nhanVien(NhanVienDTO.fromEntity(entity.getNhanVien()))
                .ghiChu(entity.getGhiChu())
                .chiTietNhapHangs(entity.getChiTietNhapHangs() != null ?
                        entity.getChiTietNhapHangs().stream()
                                .map(ChiTietNhapHangDTO::fromEntity)
                                .collect(Collectors.toList()) : null)
                .build();
    }

    public NhapHang toEntity() {
        NhapHang entity = NhapHang.builder()
                .id(this.id)
                .thoiGian(this.thoiGian)
                .tongTien(this.tongTien)
                .daThanhToan(this.daThanhToan)
                .tienNo(this.tienNo)
                .trangThai(this.trangThai)
                .ngayNhan(this.ngayNhan)
                .doiTac(this.doiTac != null ? this.doiTac.toEntity() : null)
                .nhanVien(this.nhanVien != null ? this.nhanVien.toEntity() : null)
                .ghiChu(this.ghiChu)
                .build();
        
        if (this.chiTietNhapHangs != null) {
            entity.setChiTietNhapHangs(this.chiTietNhapHangs.stream()
                    .map(dto -> {
                        com.cafe.jenika.model.ChiTietNhapHang item = dto.toEntity();
                        item.setNhapHang(entity);
                        if (item.getId() != null) {
                            item.getId().setIdNhapHang(this.id);
                        }
                        return item;
                    })
                    .collect(Collectors.toList()));
        }
        return entity;
    }
}
