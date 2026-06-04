package com.cafe.jenika.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiTietNhapHangId implements Serializable {

    @Column(name = "id_nhap_hang")
    private Integer idNhapHang;

    @Column(name = "id_san_pham")
    private Integer idSanPham;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChiTietNhapHangId that = (ChiTietNhapHangId) o;
        return Objects.equals(idNhapHang, that.idNhapHang) &&
                Objects.equals(idSanPham, that.idSanPham);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idNhapHang, idSanPham);
    }
}
