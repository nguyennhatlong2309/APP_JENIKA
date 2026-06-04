package com.cafe.jenika.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "doi_tac")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoiTac {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten", nullable = false)
    private String ten;

    @Column(name = "sdt", length = 20)
    private String sdt;

    @Column(name = "dia_chi", columnDefinition = "TEXT")
    private String diaChi;

    @Column(name = "email", length = 255)
    private String email;
}
