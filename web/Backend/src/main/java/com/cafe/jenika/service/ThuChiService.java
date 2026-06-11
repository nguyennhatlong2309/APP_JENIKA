package com.cafe.jenika.service;

import com.cafe.jenika.dto.ThuChiDTO;
import com.cafe.jenika.model.*;
import com.cafe.jenika.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ThuChiService {

    @Autowired
    private ThuChiRepository thuChiRepository;

    @Autowired
    private LoaiThuChiRepository loaiThuChiRepository;

    @Autowired
    private NhanVienRepository nhanVienRepository;

    @Autowired
    private BanHangRepository banHangRepository;

    @Autowired
    private NhapHangRepository nhapHangRepository;

    @Autowired
    private NhatKyService nhatKyService;

    public List<ThuChiDTO> getAllTransactions() {
        return thuChiRepository.findAllByOrderByThoiGianDesc().stream()
                .map(ThuChiDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ThuChiDTO saveTransaction(ThuChiDTO dto) {
        ThuChi entity = dto.toEntity();
        
        if (entity.getThoiGian() == null) {
            entity.setThoiGian(LocalDateTime.now());
        }

        if (dto.getIdLoai() != null) {
            LoaiThuChi loai = loaiThuChiRepository.findById(dto.getIdLoai()).orElse(null);
            entity.setLoaiThuChi(loai);
        }

        if (dto.getIdNhanVien() != null) {
            NhanVien nv = nhanVienRepository.findById(dto.getIdNhanVien()).orElse(null);
            entity.setNhanVien(nv);
        }

        if (dto.getIdBanHang() != null) {
            BanHang bh = banHangRepository.findById(dto.getIdBanHang()).orElse(null);
            entity.setBanHang(bh);
        }

        if (dto.getIdNhapHang() != null) {
            NhapHang nh = nhapHangRepository.findById(dto.getIdNhapHang()).orElse(null);
            entity.setNhapHang(nh);
        }

        ThuChi saved = thuChiRepository.save(entity);

        // Ghi lại hoạt động vào bảng nhat_ky
        String loaiGiaoDich = (saved.getTienThu() != null && saved.getTienThu().compareTo(java.math.BigDecimal.ZERO) > 0) ? "Thu" : "Chi";
        java.math.BigDecimal amount = "Thu".equals(loaiGiaoDich) ? saved.getTienThu() : saved.getTienChi();
        
        nhatKyService.log("THEM", "thu_chi", "TC-" + saved.getId(),
                "Ghi nhận khoản " + loaiGiaoDich + " mới. Phân loại: " + (saved.getLoaiThuChi() != null ? saved.getLoaiThuChi().getTen() : "Không phân loại") +
                ". Số tiền: " + amount + "đ. Mô tả: " + saved.getMoTa());

        return ThuChiDTO.fromEntity(saved);
    }

    @Transactional
    public ThuChiDTO updateTransaction(Integer id, ThuChiDTO dto) {
        ThuChi entity = thuChiRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch với ID: " + id));
        
        entity.setThoiGian(dto.getThoiGian() != null ? dto.getThoiGian() : LocalDateTime.now());
        entity.setTienThu(dto.getTienThu());
        entity.setTienChi(dto.getTienChi());
        entity.setMoTa(dto.getMoTa());
        entity.setPhuongThuc(dto.getPhuongThuc());
        entity.setTrangThai(dto.getTrangThai());
        
        if (dto.getIdLoai() != null) {
            LoaiThuChi loai = loaiThuChiRepository.findById(dto.getIdLoai()).orElse(null);
            entity.setLoaiThuChi(loai);
        } else {
            entity.setLoaiThuChi(null);
        }

        if (dto.getIdNhanVien() != null) {
            NhanVien nv = nhanVienRepository.findById(dto.getIdNhanVien()).orElse(null);
            entity.setNhanVien(nv);
        } else {
            entity.setNhanVien(null);
        }

        if (dto.getIdBanHang() != null) {
            BanHang bh = banHangRepository.findById(dto.getIdBanHang()).orElse(null);
            entity.setBanHang(bh);
        } else {
            entity.setBanHang(null);
        }

        if (dto.getIdNhapHang() != null) {
            NhapHang nh = nhapHangRepository.findById(dto.getIdNhapHang()).orElse(null);
            entity.setNhapHang(nh);
        } else {
            entity.setNhapHang(null);
        }

        ThuChi saved = thuChiRepository.save(entity);
        
        String loaiGiaoDich = (saved.getTienThu() != null && saved.getTienThu().compareTo(java.math.BigDecimal.ZERO) > 0) ? "Thu" : "Chi";
        java.math.BigDecimal amount = "Thu".equals(loaiGiaoDich) ? saved.getTienThu() : saved.getTienChi();
        
        nhatKyService.log("SUA", "thu_chi", "TC-" + saved.getId(),
                "Cập nhật giao dịch " + loaiGiaoDich + " TC-" + saved.getId() + ". Phân loại: " + 
                (saved.getLoaiThuChi() != null ? saved.getLoaiThuChi().getTen() : "Không phân loại") +
                ". Số tiền: " + amount + "đ. Mô tả: " + saved.getMoTa());
                
        return ThuChiDTO.fromEntity(saved);
    }

    @Transactional
    public void deleteTransaction(Integer id) {
        ThuChi entity = thuChiRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch với ID: " + id));
        thuChiRepository.delete(entity);
        nhatKyService.log("XOA", "thu_chi", "TC-" + id,
                "Xóa giao dịch thu chi TC-" + id + ". Mô tả: " + entity.getMoTa());
    }
}

