package com.cafe.jenika.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "nhat_ky")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NhatKy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "thoi_gian", nullable = false)
    private LocalDateTime thoiGian;

    @Column(name = "thao_tac", length = 20, nullable = false)
    private String thaoTac; // THEM | SUA | XOA

    @Column(name = "tab", length = 50, nullable = false)
    private String tab; // ban_hang | nhap_hang | thu_chi

    @Column(name = "ma_ban_ghi", length = 30)
    private String maBanGhi; // VD: BH-12, NH-5, TC-3

    @Column(name = "mo_ta", columnDefinition = "TEXT")
    private String moTa;
}
