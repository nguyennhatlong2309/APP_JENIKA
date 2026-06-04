package com.cafe.jenika.dto;

import com.cafe.jenika.model.DonViTinh;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonViTinhDTO {
    private Integer id;
    private String tenDonVi;

    public static DonViTinhDTO fromEntity(DonViTinh entity) {
        if (entity == null) return null;
        return DonViTinhDTO.builder()
                .id(entity.getId())
                .tenDonVi(entity.getTenDonVi())
                .build();
    }

    public DonViTinh toEntity() {
        return DonViTinh.builder()
                .id(this.id)
                .tenDonVi(this.tenDonVi)
                .build();
    }
}
