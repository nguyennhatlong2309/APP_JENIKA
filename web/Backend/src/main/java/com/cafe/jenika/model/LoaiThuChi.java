package com.cafe.jenika.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "loai_thu_chi")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoaiThuChi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten", length = 100, nullable = false)
    private String ten;
}
