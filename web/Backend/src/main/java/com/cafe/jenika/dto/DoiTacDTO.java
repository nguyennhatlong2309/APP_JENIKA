package com.cafe.jenika.dto;

import com.cafe.jenika.model.DoiTac;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoiTacDTO {
    private Integer id;
    private String ten;
    private String sdt;
    private String diaChi;
    private String email;

    public static DoiTacDTO fromEntity(DoiTac entity) {
        if (entity == null) return null;
        return DoiTacDTO.builder()
                .id(entity.getId())
                .ten(entity.getTen())
                .sdt(entity.getSdt())
                .diaChi(entity.getDiaChi())
                .email(entity.getEmail())
                .build();
    }

    public DoiTac toEntity() {
        return DoiTac.builder()
                .id(this.id)
                .ten(this.ten)
                .sdt(this.sdt)
                .diaChi(this.diaChi)
                .email(this.email)
                .build();
    }
}
