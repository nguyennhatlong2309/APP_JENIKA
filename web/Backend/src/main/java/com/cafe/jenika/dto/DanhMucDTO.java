package com.cafe.jenika.dto;

import com.cafe.jenika.model.DanhMuc;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DanhMucDTO {
    private Integer id;
    private String tenDanhMuc;
    private String moTa;

    public static DanhMucDTO fromEntity(DanhMuc entity) {
        if (entity == null) return null;
        return DanhMucDTO.builder()
                .id(entity.getId())
                .tenDanhMuc(entity.getTenDanhMuc())
                .moTa(entity.getMoTa())
                .build();
    }

    public DanhMuc toEntity() {
        return DanhMuc.builder()
                .id(this.id)
                .tenDanhMuc(this.tenDanhMuc)
                .moTa(this.moTa)
                .build();
    }
}
