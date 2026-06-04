package com.cafe.jenika.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "nhom_san_pham")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NhomSanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten_nhom", nullable = false)
    private String tenNhom;

    @OneToMany(mappedBy = "nhomSanPham", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<SanPham> sanPhams;
}
