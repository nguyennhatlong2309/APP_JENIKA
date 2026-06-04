package com.cafe.jenika.service;

import com.cafe.jenika.model.NhomSanPham;
import com.cafe.jenika.repository.NhomSanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class NhomSanPhamService {
    @Autowired
    private NhomSanPhamRepository nhomSanPhamRepository;
    
    public List<NhomSanPham> getAll() {
        return nhomSanPhamRepository.findAll();
    }

    @Transactional
    public NhomSanPham update(Integer id, NhomSanPham updated) {
        return nhomSanPhamRepository.findById(id)
                .map(existing -> {
                    existing.setTenNhom(updated.getTenNhom());
                    return nhomSanPhamRepository.save(existing);
                })
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhóm sản phẩm với id " + id));
    }

    @Transactional
    public NhomSanPham save(NhomSanPham nhomSanPham) {
        return nhomSanPhamRepository.save(nhomSanPham);
    }
}

