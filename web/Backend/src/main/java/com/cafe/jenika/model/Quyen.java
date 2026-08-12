package com.cafe.jenika.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "quyen")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quyen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten_quyen", unique = true, nullable = false, length = 50)
    private String tenQuyen;

    @Column(name = "mo_ta", length = 255)
    private String moTa;
}
