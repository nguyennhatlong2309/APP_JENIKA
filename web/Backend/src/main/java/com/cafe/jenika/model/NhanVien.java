package com.cafe.jenika.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "nhan_vien")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NhanVien {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ten_nhan_vien", nullable = false)
    private String tenNhanVien;

    @Column(name = "sdt", length = 20)
    private String sdt;

    @Column(name = "vai_tro", length = 100)
    private String vaiTro;

    @Column(name = "email", length = 255)
    private String email;
}
