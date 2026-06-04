package com.cafe.jenika.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "don_vi_tinh")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonViTinh {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten_don_vi", nullable = false)
    private String tenDonVi;

    @OneToMany(mappedBy = "donViTinh", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<SanPham> sanPhams;
}
