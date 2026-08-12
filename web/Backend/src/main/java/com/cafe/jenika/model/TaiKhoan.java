package com.cafe.jenika.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "tai_khoan")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaiKhoan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "trang_thai", nullable = false, length = 20)
    @Builder.Default
    private String trangThai = "ACTIVE";

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "nhan_vien_id", referencedColumnName = "id", unique = true)
    private NhanVien nhanVien;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "tai_khoan_vai_tro",
        joinColumns = @JoinColumn(name = "tai_khoan_id"),
        inverseJoinColumns = @JoinColumn(name = "vai_tro_id")
    )
    private Set<VaiTro> vaiTros;

    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
    }
}
