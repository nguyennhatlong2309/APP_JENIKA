package com.cafe.jenika.dto;

import com.cafe.jenika.model.NhomSanPham;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NhomSanPhamDTO {
    private Integer id;
    private String tenNhom;

    public static NhomSanPhamDTO fromEntity(NhomSanPham entity) {
        if (entity == null) return null;
        return NhomSanPhamDTO.builder()
                .id(entity.getId())
                .tenNhom(entity.getTenNhom())
                .build();
    }

    public NhomSanPham toEntity() {
        return NhomSanPham.builder()
                .id(this.id)
                .tenNhom(this.tenNhom)
                .build();
    }
}
