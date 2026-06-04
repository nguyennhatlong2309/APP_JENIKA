package com.cafe.jenika.dto;

import com.cafe.jenika.model.NhanVien;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NhanVienDTO {
    private Integer id;
    private String tenNhanVien;
    private String sdt;
    private String vaiTro;
    private String email;

    public static NhanVienDTO fromEntity(NhanVien entity) {
        if (entity == null) return null;
        return NhanVienDTO.builder()
                .id(entity.getId())
                .tenNhanVien(entity.getTenNhanVien())
                .sdt(entity.getSdt())
                .vaiTro(entity.getVaiTro())
                .email(entity.getEmail())
                .build();
    }

    public NhanVien toEntity() {
        return NhanVien.builder()
                .id(this.id)
                .tenNhanVien(this.tenNhanVien)
                .sdt(this.sdt)
                .vaiTro(this.vaiTro)
                .email(this.email)
                .build();
    }
}
