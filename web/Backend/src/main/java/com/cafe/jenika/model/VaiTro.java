package com.cafe.jenika.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity
@Table(name = "vai_tro")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VaiTro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten_vai_tro", unique = true, nullable = false, length = 50)
    private String tenVaiTro;

    @Column(name = "mo_ta", length = 255)
    private String moTa;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "vai_tro_quyen",
        joinColumns = @JoinColumn(name = "vai_tro_id"),
        inverseJoinColumns = @JoinColumn(name = "quyen_id")
    )
    private Set<Quyen> quyens;
}
